package com.somesh.appscout.di

import com.somesh.appscout.data.CachingPaddockRepository
/**
 * Small manual container. Keeps data-source replacement straightforward without pulling in
 * a DI framework for a single-screen-graph app.
 */
object PaddockContainer {
    val repository: CachingPaddockRepository by lazy { CachingPaddockRepository() }
}
