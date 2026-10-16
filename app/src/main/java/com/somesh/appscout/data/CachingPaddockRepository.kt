package com.somesh.appscout.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Memoising decorator.
 *
 * The app has no offline database by design, so this only removes duplicate network
 * round-trips inside a session (for example re-opening a race detail screen). Failures are
 * never cached and never replaced with invented data — they propagate so the UI can offer
 * an honest retry.
 */
class CachingPaddockRepository(
    private val remoteRepository: PaddockRepository = RemotePaddockRepository()
) : PaddockRepository {

    private val driversCache = OneShot<List<Driver>>()
    private val constructorsCache = OneShot<List<Constructor>>()
    private val racesCache = OneShot<List<Race>>()
    private val resultsCache = mutableMapOf<Int, List<RaceResult>>()
    private val resultsLock = Mutex()
    /** One slot per round so concurrent callers share a download instead of starting their own. */
    private val resultsInFlight = mutableMapOf<Int, CompletableDeferred<List<RaceResult>>>()

    override suspend fun drivers(): List<Driver> = driversCache.get { remoteRepository.drivers() }

    override suspend fun constructors(): List<Constructor> =
        constructorsCache.get { remoteRepository.constructors() }

    override suspend fun races(): List<Race> = racesCache.get { remoteRepository.races() }

    override suspend fun results(round: Int): List<RaceResult> = withContext(Dispatchers.IO) {
        resultsLock.withLock { resultsCache[round] }?.let { return@withContext it }

        // The mutex only guards the cache maps. It is deliberately never held across the network
        // call, because that would make one slow round block every other classification request.
        val (isOwner, gate) = resultsLock.withLock {
            val pending = resultsInFlight[round]
            if (pending != null) false to pending
            else true to CompletableDeferred<List<RaceResult>>().also { resultsInFlight[round] = it }
        }
        if (!isOwner) return@withContext gate.await()

        try {
            // Cache empty payloads too: a round that has not run yet will not gain a result
            // within a session, and re-requesting on every visit is pure waste.
            val loaded = remoteRepository.results(round)
            resultsLock.withLock {
                resultsCache[round] = loaded
                resultsInFlight.remove(round)
            }
            gate.complete(loaded)
            loaded
        } catch (t: Throwable) {
            resultsLock.withLock { resultsInFlight.remove(round) }
            gate.completeExceptionally(t)
            throw t
        }
    }

    /** Clears memoised payloads so a manual retry genuinely re-hits the API. */
    suspend fun invalidate() {
        driversCache.clear()
        constructorsCache.clear()
        racesCache.clear()
        resultsLock.withLock { resultsCache.clear() }
    }

    /** Drops a single cached classification so it can be re-requested. */
    suspend fun forgetResults(round: Int) {
        resultsLock.withLock { resultsCache.remove(round) }
    }

    private class OneShot<T> {
        @Volatile private var value: T? = null
        private val lock = Mutex()

        suspend fun get(load: suspend () -> T): T {
            value?.let { return it }
            return lock.withLock { value ?: load().also { value = it } }
        }

        suspend fun clear() {
            lock.withLock { value = null }
        }
    }
}
