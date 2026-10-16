# Paddock — driver photography

## How portraits are resolved

The Jolpica/Ergast payload carries **no image URL** (`DriverDto` only exposes
`driverId`, `permanentNumber`, `code`, `url`, `givenName`, `familyName`, `dateOfBirth`,
`nationality`), so portraits ship with the app and are matched by the **stable
`driverId`**, never by list position or display name.

* Location: `app/src/main/res/drawable-nodpi/`
* Filename: `paddock_driver_<driverId>.jpg`
* Resolver: `view/PaddockImages.kt` → `portraitAssetName()` / `driverPortraitResId()`
* Call site: `view/PaddockComponents.kt` → `DriverPortrait(driverId = ...)`

If an asset is absent the resolver returns `0` and the existing editorial plate
(`paddock_driver_editorial.png`) is drawn instead, so a missing photograph can never
break a layout.

To add a driver, drop `paddock_driver_<driverId>.jpg` into `drawable-nodpi/` using the
`driverId` from the API (for example `paddock_driver_norris.jpg`). No code change is
needed. `res/raw/keep.xml` stops the resource shrinker stripping these name-referenced
files from a release build.

## Bundled portraits

All five are 1200 × 1600 (3:4) JPEGs, produced by cropping and resizing the Commons
originals listed below. `src` sizes are the downloaded originals, not the packaged files.

| Driver | `driverId` | Packaged asset | Author | Licence |
| --- | --- | --- | --- | --- |
| Andrea Kimi Antonelli (Mercedes) | `antonelli` | `paddock_driver_antonelli.jpg` | Yu Chu Chin (User:Pangalau) | CC BY-SA 4.0 |
| George Russell (Mercedes) | `russell` | `paddock_driver_russell.jpg` | Peter Menzel (Flickr) | CC BY-SA 2.0 |
| Lewis Hamilton (Ferrari) | `hamilton` | `paddock_driver_hamilton.jpg` | Liauzh | CC BY-SA 4.0 |
| Max Verstappen (Red Bull Racing) | `max_verstappen` | `paddock_driver_max_verstappen.jpg` | Yu Chu Chin (User:Pangalau) | CC BY-SA 4.0 |
| Charles Leclerc (Ferrari) | `leclerc` | `paddock_driver_leclerc.jpg` | Steffen Prößdorf (User:Stepro) | CC BY-SA 4.0 |

### Source files (Wikimedia Commons)

| Driver | Commons file |
| --- | --- |
| Antonelli | `File:Kimi Antonelli at the Melbourne Walk during the 2026 Australian Grand Prix (028A7923) cropped.jpg` |
| Russell | `File:George Russell (53837544922).jpg` |
| Hamilton | `File:2025 Japan GP - Ferrari - Lewis Hamilton - Fanzone Stage (cropped).jpg` |
| Verstappen | `File:Max Verstappen at the Red Bull Fan Zone – Crown Riverwalk, Melbourne (028A7659) - cropped.jpg` |
| Leclerc | `File:2024-08-25 Motorsport, Formel 1, Großer Preis der Niederlande 2024 STP 3978 by Stepro (cropped2).jpg` |

Descriptions page: `https://commons.wikimedia.org/wiki/<file name, underscores>`

### Identity verification

Each portrait was confirmed against the Commons file description and category metadata
rather than by filename alone — for example the Leclerc file is titled only
`…STP 3978 by Stepro (cropped2).jpg` and is identified by its description
("Charles Leclerc (MON, Scuderia Ferrari)") and its category `Charles Leclerc in 2024`.
Team colours in the lower third of each image were also checked (Ferrari red for
Hamilton/Leclerc, Red Bull navy for Verstappen, Mercedes grey for Russell/Antonelli).

## Licence obligations — read before shipping

All five files are **CC BY-SA**, which is not the same as public domain:

1. **Attribution is required.** Credit the author, the licence, and note that the image
   was cropped/resized. The table above is the attribution record.
2. **Share-alike applies to the adapted crops.** Cropping and resizing creates an adapted
   work, so `paddock_driver_*.jpg` must themselves be made available under the same
   CC BY-SA terms.
3. Nothing above is legal advice; confirm the terms for your distribution before release.

The app currently surfaces **no in-app attribution**, because the design was frozen and no
credits screen exists. A credits/licences surface (or a link to this file in the store
listing) still needs to be added before publishing.
