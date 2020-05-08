# Changelog

#### v0.8.0

 * Internal: Update Gradle
 * Internal: Min API Level set to 22 (Lollipop	5.1)

#### v0.7.1.1

 * Bug fix: Fix migration of old tap-to-reveal setting
 * Bug fix: Cache encryption key when recreating main activity

#### v0.7.1

 * Deprecation notice: This will be the last version to support Android versions below 5.1
 * New feature: Show QR codes of stored accounts (PR #501 by @tilosp)
 * New feature: Support Steam URIs (Issue #510)
 * New feature: Move Steam out of the Special features
 * New feature: Unify the edit dialog for entries (Issue #241)
 * New feature: Add an option to hide the global timeout bar (Issue #166)
 * New feature: Add an option to show individual timeout bars for all cards (Issue #166)
 * New feature: Add options to configure single and double taps on entries (Issue #489)
 * Improvement: Increase the iterations for the password generation to 150000
 * Improvement: Show cards as transparent while dragging (Issue #487, PR #488 by @Ullas-Aithal)
 * Improvement: Rewording of the last used dialog (Issue #485)
 * Improvement: Handle the back key to close open drawers or the FAB overlay (Issue #499)
 * Improvement: Validate secrets during manual entry (Issue #500)
 * Bug fix: Fix some remaining issues with the intro dialog (Issue #486)
 * Bug fix: Fix images containing gradients on API versions below 24 (Issue #539)
 * Thumbnails: Lots of new ones (thanks to all contributors)
 * Translations: Bulgarian (thanks to all the contributors on Crowdin)

#### v0.7.0

 * New feature: Generate a new HOTP token when revealing (Issue #334, PR #366 by @moritzgloeckl)
 * New feature: Split issuer and label (Issue #258, PR #372 by @lucavallerini)
 * New feature: Automatic thumbnail selection based on the issuer (Issue #388, PR #389 by @schwedenmut)
 * New feature: Allow searching the tags and issuers in addition to the label (Issue #327)
 * New feature: Turn tokens red if they are about to expire (Issue #311, PR #410 by @Ullas-Aithal)
 * New feature: Handle otpauth:// intents from other apps (Issue #324, PR #393 by @schwedenmut)
 * New feature: Create an encrypted backup every time the entries are changed (PR #397 and PR #421 by @RichyHBM)
 * New feature: Different layouts for the entry cards (compact, default and full)
 * New feature: New thumbnail size "Tiny"
 * New feature: Block accessibility services from seeing sensitive input fields via a new settings item
 * New feature: Import QR codes from image files (Issue #377, PR #425 by @Ullas-Aithal)
 * New feature: Move the app to the background after copying a token (Issue #373, PR #392 by @Ullas-Aithal)
 * New feature: Re-lock the app after a certain time of inactivity (Issue #28, PR #390 by @LizardWithHat)
 * New feature: Re-lock when being send to the background (Issue #216)
 * New feature: Sort tokens by "most used" (Issue #443, PR #467 by @Ullas-Aithal)
 * Improvement: Hide the token list on screen off (Issue #264, PR #390 by @LizardWithHat)
 * Improvement: Scale the font of the default thumbnail with its size
 * Improvement: Do not use auto-completion for the account secret (PR #430 by @duchampdev)
 * Improvement: Enable Android backup by default (Issue #341, PR #342 by @RichyHBM)
 * Improvement: Remove whitespaces from manually entered secrets (Issue #253, PR #426 by @Ullas-Aithal)
 * Improvement: Fallback method for opening backup files (based on PR #358 by @theobch)
 * Improvement: Allow the backup directory to be set independent of the "ask for filename" setting
 * Bug fix: Fix black navigation bar on OxygenOS devices (PR #417 by @Ullas-Aithal)
 * Internal: Migrate to AndroidX
 * Internal: Update Gradle and a lot of dependencies
 * Thumbnails: Lots of new ones (thanks to all contributors)
 * Translations: Greek, Hindi (thanks to all the contributors on Crowdin)

#### v0.6.3.1

 * Introduce build flavors:
   - fdroid: Shows donation links in the About section
   - play: Doesn't show donation links in the About section

#### v0.6.3

 * Security: Improved password derivation for the password protected backups
 * New feature: Prevent screencapture in the Authentication and QR scanner screen (Issue #378, PR #386 by @schwedenmut)
 * New feature: Color navbar according to the theme (Issue #284, PR #371 by @HarryJohnso)
 * Bug fix: Fix "all tags" only selecting visible tags (Issue #333, PR #350 by @RichyHBM)
 * Bug fix: Focus the password/PIN input field on start (Issue #356, PR #357 by @schwedenmut)
 * Bug fix: Fix spelling of "QR code" (PR #368 by @yegortimoshenko)
 * Bug fix: Always use arabic numerals for the tokens (Issue #359)
 * Bug fix: Refactor storage access code to allow importing and exporting from cloud storage directly
 * Bug fix: Hardcode the black background color to avoid strange behaviour on some custom ROMs
 * Bug fix: Force English locales for saving AuthMethod
 * Misc: Update donation links (PR #351)
 * Thumbnails: Lots of new thumbnails
 * Translations: Hungarian

#### v0.6.2

 * Bug fix: Proper handling of RTL layouts by forcing LTR for the tokens (PR #280 by @ahangarha)
 * Internal: Image compression (thanks to @Peppernrino)
 * Thumbnail: Add a LOT of new thumbnails (thanks to everybody that contributed)
 * Translation: New Arabic, Traditional Chinese, Japanese, Persian and Swedish translations (thanks to all contributors on Crowdin)

#### v0.6.1

 * New feature: Enable Android Backup by default if available and using the password encryption (PR #252)
 * Bug fix: Fix crash in the manual entry dialog on KitKat
 * Bug fix: Fix thumbnail generation on KitKat
 * Thumbnail: Add MediaWiki (PR #246 by @MeLlamoPablo)

#### v0.6.0

 * New feature: **HOTP support**
 * New feature: Settings item to activate Broadcast backups
 * New feature: Re-locking of the app on screen off is now optional (Issue #28)
 * New feature: Allow PGP backups with only a public key (Issue #31)
 * New feature: Show individual timeout bars on the cards for non-default periods
 * New feature: App shortcuts to add new entries (Issue #185)
 * New feature: Ask for the backup password if it's not available (Issue #182)
 * New feature: Allow installation on external storage (PR #206 by @leggewie)
 * Bug fix: Avoid crash on empty PIN/Password an API 23 (Issue #159, PR #160 by magnus anderssen)
 * Bug fix: Honor the system accessibility settings for the font size (Issue #71, PR #192 by @mbertram)
 * Bug fix: Make the new entry dialog scrollable (Issue #196)
 * Bug fix: Fix autofill of the password fields (Issue #215, PR #218 by @z3ntu)
 * Bug fix: Extend thumbnail generation to non-latin letters and digits (PR #234 by @jeandeaual)
 * Bug fix: Show new entries at the top of the list when using last used sorting (Issue #211)
 * Bug fix: Fix a crash on the settings page (Issue #197)
 * Internal: Replace custon FAB menu with Floating Action Button Speed Dial library (Issue #155 and #186)
 * Style/UI: Use AboutLibraries instead of LicenseDialog and rework the About section (Issue #155)
 * Style/UI: Show a disclaimer about the included thumbnails in the About screen
 * Update: ZXing Android Embedded (3.6.0), Constraint Layout (1.1.2) and all support libraries (27.1.1)
 * F-Droid: Add the feature graphic and some screenshots (PR #117 by @jaller94)
 * Thumbnails: lots of new thumbnails (see the wiki)

#### v0.5.1 (Google Play only)

 * Like v0.5.0 but with Password-based encryption offered by default during setup

#### v0.5.0

 * New feature: **Intro screen when staring the app for the first time to setup encryption and authentication**
 * New feature: **Broadcast receivers to trigger backups from Tasker** (PR #115)
 * New feature: **Add support for using Android Backup** (Issue #109, PR #111)
 * New feature: Optionally append date to backups (PR #124)
 * New feature: Check if entries are valid when entering manually (Issue #135, PR #136 by Björn Richter)
 * New feature: Offer different options when using the tag selection (Issue #133, PR #134)
 * New feature: Show a warning before changing the encryption
 * Bug fix: Fix crash when saving an empty label (Issue #138, PR #139 by Björn Richter)
 * Bug fix: Fix visibility of thumbnails in dark themes (Issue #88, PR #90)
 * Bug fix: Don't require credentials again after screen rotation (Issue #152)
 * Thumbnails: new thumbnails (see the wiki)

#### v0.4.0

 * New feature: **Password-based encryption** (a big thanks to all the testers)
 * New feature: Enforce a minimum password / PIN length (Issue #107)
 * New feature: Add an additional unlock button to the authentication (Issue #87)
 * New feature: The thumbnail toggle is now in the size selector (Issue #98, PR #102)
 * New feature: Split the tokens into blocks (Issue #83, PR #83 by DanielWeigl)
 * New feature: Account name is now shown in the removal confirmation (Issue #84)
 * New feature: Advanced options are now hidden in the manual entry dialog (Issue #85)
 * New special feature: Clear the KeyStore (use with caution)
 * Bug fix: Change the format used to store and set the language (Issue #112)
 * Bug fix: Add some extra padding the the RecyclerView (Issue #95)
 * Bug fix: Remove gradients from vector thumbnails (Issue #103, PR #97)
 * Thumbnails: a lot of new thumbnails (check the wiki for details)
 * Translation: Catalan (ca-rES) thanks to isard

#### v0.3.1

 * Move: the Github repository was moved from flocke/andOTP to andOTP/andOTP for better organization of collaborators
 * New feature: assign (predefined) images to entries (Issue #14, PR #75, again thanks to @RichyHBM for the implementation)
 * New feature: sort labels locale-sensitive (PR #74 by carmebar)
 * New feature: re-hide the revealed entries after a configurable timeout (Issue #77)
 * New feature: add sorting by last usage (Issue #67)
 * New feature: improved error messages during the import of backups
 * New feature: make the replace switch default to false (Issue #80)
 * New special feature: disable Special features again
 * New special feature: enable screenshots in the main Activity
 * Bug fix: use sp for font sizes (to make them scalable)
 * Bug fix: disable the save button in a manual entry until label and secret are not empty (Issue #82)
 * Style/UI: better description of the replace switch
 * Update: Android SDK 27 (Issue #76)
 * Update: Android Gradle plugin 3.0.1
 * Translation: Chinese Simplified (zh-rCN) thanks to Cp0204

#### v0.3.0

 * New feature: tagging support (Issue #37, PR #64, big thanks to @RichyHBM for actually implementing this)
 * New feature: settings option to scroll overlong labels instead of just truncating them
 * New feature: option to append entries during import instead of just replacing everything
 * New feature: in-app language switcher (Issue #53)
 * Bug fix: convert secrets to upper case when importing from JSON (Issue #55)
 * Bug fix: some layout fixes for certain translations (Issue #58)
 * Style/UI: new adaptive icon for Android 8+ (Issue #65)
 * Style/UI: remove card elevation
 * Update: Android Studio 3
 * Update: Gradle 4.1 / Android Gradle Plugin 3.0

#### v0.2.8

 * New feature: store authentication credentials hashed (Issue #49)
 * New feature: store backup password encrypted (Issue #49)
 * New feature: set a static backup dir to disable the file selector (Issue #52)
 * New feature: special features (see wiki)
 * New special feature: SteamGuard tokens (Issue #38)
 * Style/UI: black theme (Issue #47)
 * Bug fix: keep authentication settings when receiving a Panic Trigger (Issue #50)
 * Bug fix: progress bar animation with default duration scale
 * Translation: Czech (cs-rCZ) thanks to Picard0147

#### v0.2.7

 * New feature: require authentication again after screen lock (Issue #28)
 * New feature: make response to Panic Trigger configurable (Issue #35)
 * Bug fix: prevent adding duplicate entries (Issue #41)
 * Update: Android SDK 26 (Oreo)
 * Update: Apache Commons Codec 1.10
 * Code: lot of internal changes (mostly due to the Android 26 update)
 * Translation: French (fr-rFR) thanks to Johan Fleury
 * Translation: Durch (nl-rNL) thanks to T-v-Gerwen and rain2reign
 * Translation: Galician (gl-rES) thanks to Triskel
 * Translation: Russian (ru-rRU) thanks to Victor Nidens, Ilia Drogaitsev and Dmitry

#### v0.2.6

 * New feature: custom password preference with confirmation (Issue #26)
 * New feature: use an individual password or PIN to lock the app (Issue #23)
 * New feature: support for Panic Trigger (PR #27 by carmebar)
 * New feature: support for variable digits lenths (PR #30 by SuperVirus)
 * Bug fix: OpenPGP with security token (Issue #20, PR #25 by carmebar)
 * Style/UI: add Contributors, Translators and Translate to About
 * Code: internal refactoring
 * Translation: German (de-rDE) thanks to SuperVirus

#### v0.2.5

 * New feature: sort the entries by label (Issue #12)
 * New feature: add support for SHA256 and SHA512 (Issue #24)
 * Bug fix: show current theme in the settings
 * Bug fix: don't show FloatingActionMenu when scrolling while searching
 * Code: lots of internal refactoring
 * Translation: Polish (pl-rPL) thanks to Daniel Pustuła
 * Translation: Spanish (es-rES) thanks to Carlos Melero

#### v0.2.4

 * New feature: make the font size of the labels configurable (Issue #18)
 * Style/UI: Dark theme (Issue #3)
 * Bug fix: make the backup activity scrollable (Issue #15)
 * Bug fix: remove swipe-to-dismiss to avoide accidental deletions (Issue #13)
 * Bug fix: use the whole card for tap-to-reveal, not just the token (Issue #10)
 * Code: internal changes (as always)

#### v0.2.3

 * New feature: encrypted backups with password
 * New feature: show a warning about backups on the first launch
 * Style/UI: rename Export and Import to Backup and Restore
 * Bug fix: don't require device authentication again after screen rotation (Issue #7)
 * Bug fix: hide the FloatingActionMenu on scroll (Issue #8)
 * Bug fix: rename the apps launcher icon to "andOTP" (Issue #6)
 * Bug fix: restrict the label size so they don't overlap with the buttons (Issue #9)
 * Code: lots of internal refactoring

#### v0.2.2

 * Bug fix: resume import and export after permission request
 * Bug fix: implement a working hashCode function for the Entry class
 * Code: add missing copyright headers
 * Code: fix some tests
 * Code: remove outdated tests

#### v0.2.1

 * New feature: encrypted backups using OpenPGP
 * Style: new about screen
 * Style: new backup screen
 * Code: a lot of refactoring

#### v0.2.0

 * New feature: copy token to clipboard
 * New feature: device credentials to unlock app
 * New feature: manually enter account details
 * New feature: search
 * New feature: settings activity
 * New feature: tap to reveal
 * Style: replace FAB with a custom FloatingActionMenu
 * Style: replace all Snackbars with Toasts
 * Update: ZXing Android Embedded v3.5.0
 * Code: a lot of internal fixes and refactoring
 * Code: initial groundwork to support different types of OTP tokens (e.g. HOTP)

#### v0.1.0

 * Initial release (beta)

