#  andOTP -  Android OTP Authenticator

[![Build Status](https://travis-ci.org/flocke/andOTP.svg?branch=master)](https://travis-ci.org/flocke/andOTP)
[![Current release](https://img.shields.io/github/release/flocke/andOTP/all.svg)](https://github.com/flocke/andOTP/releases/download/v0.2.7/andOTP_v0.2.7.apk)

![andOTP](./assets/logo.png)

andOTP is a two-factor authentication App for Android 4.4+.

It implements Time-based One-time Passwords (TOTP) like specified in RFC 6238.  
Simply scan the QR code and login with the generated 6-digit code. 

This is a fork of the great OTP Authenticator app written by Bruno Bierbaumer,
which has sadly been inactive since 2015. All credit for the original version
goes to Bruno.

## Help wanted:
I currently don't have that much time to spend developing andOTP, so any contributions are always welcome.
Don't worry, I will still continue to develop andOTP it will just slow down from the incredible speed I had going in the beginning.

## Features:

 * Free and Open-Source
 * Requires minimal permissions
   - Camera access for QR code scanning
   - Storage access for import and export of the database
 * Encrypted storage
 * Multiple backup options:
   - Plain-text
   - Password-protected
   - OpenPGP-encrypted
 * Sleek minimalistic Material Design with a Dark and Light theme
 * Great Usability
 * Compatible with Google Authenticator

## Backups:

To keep your account information as secure as possible andOTP only stores it in
encrypted data files. A part of the encryption key used for that is stored in the
Android KeyStore system. The advantage of this approach is that the key is kept
separate from the apps data and, as a bonus, can be backed by hardware cryptography
(if your device supports this).

However, due to that separation, backups with 3rd-party apps like Titanium Backup can not
be used with andOTP. Such apps only backup the encrypted data files and not the encryption
key, which renders them useless.

**Please only use the internal backup functions provided by andOTP to backup your accounts!**
**Everything else WILL result in data loss.**

### Opening the backups on your PC:

 * [OpenPGP](http://openpgp.org/): OpenPGP can be used to easily decrypt the OpenPGP-encrypted backups on your PC.
 * [andOTP-decrypt](https://github.com/asmw/andOTP-decrypt): A python script written by @asmw to decrypt password-protected backups on your PC (needs more testing).

## Migration:

Check out [this](https://github.com/flocke/andOTP/wiki/Migration) wiki page to learn about the different ways to migrate to andOTP from other 2FA apps.

## Downloads:

[<img height=80 alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" />](https://play.google.com/store/apps/details?id=org.shadowice.flocke.andotp)
[<img height=80 alt="Get it on F-Droid" src="https://f-droid.org/badge/get-it-on.png" />](https://f-droid.org/packages/org.shadowice.flocke.andotp/)
[<img height=80 alt="Get it on GitHub" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/badges/get-it-on-github.png" />](https://github.com/flocke/andOTP/releases)

**Warning**: All three versions (Google Play, F-Droid and the APKs) are not compatible (not signed by the same key)!
You will have to uninstall one to install the other, which will delete all your data.
So make sure you have a **current backup** before switching!

## Contribute:

 * **Translation**: If you want to help translate andOTP into your language head over to the [Crowdin project](https://crowdin.com/project/andotp).
 * **Bug reports and feature requests**: You can report bugs and request features in the [Issue tracker](https://github.com/flocke/andOTP/issues) on GitHub.
 * **Discussion and support**: 
   - [XDA thread](https://forum.xda-developers.com/android/apps-games/app-andotp-android-otp-authenticator-t3636993) (please keep off-topic to a minimum)
   - Telegram channel [@andOTP](https://t.me/andOTP)

#### Contributors:

 * [Carlos Melero](https://github.com/carmebar) ([view contributions](https://github.com/flocke/andOTP/commits/master?author=carmebar))
 * [SuperVirus](https://github.com/SuperVirus) ([view contributions](https://github.com/flocke/andOTP/commits/master?author=SuperVirus))

#### Translators:

 * 🇵🇱 Polish (pl-rPL): [Daniel Pustuła](https://github.com/9Cube-dpustula)
 * :es: Spanish (es-rES): [Carlos Melero](https://crowdin.com/profile/carmebar)
 * :de: German (de-rDE): [SuperVirus](https://crowdin.com/profile/SuperVirus), [Chris Heitkamp](https://crowdin.com/profile/christophheitkamp)
 * :fr: French (fr-rFR): [Johan Fleury](https://github.com/johanfleury)
 * 🇳🇱 Dutch (nl-rNL): Toon, [rain2reign](https://crowdin.com/profile/rain2reign)
 * :es: Galician (gl-rES): [Triskel](https://crowdin.com/profile/triskel) (sorry for the wrong flag, but there is no unicode character for the Galician flag)
 * :ru: Russian (ru-rRU): [Victor Nidens](https://crowdin.com/profile/vnidens), [Ilia Drogaitsev](https://crowdin.com/profile/waytoroot), [Dmitry](https://crowdin.com/profile/SaintDI)
 * 🇨🇿 Czech (cs-rCZ): [Picard0147](https://crowdin.com/profile/Picard0147)

## Screenshots:

[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/main_activity.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/main_activity.png)
[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/settings_activity.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/settings_activity.png)
[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/backup_activity.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/backup_activity.png)

[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/main_activity_dark.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/main_activity_dark.png)
[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/settings_activity_dark.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/settings_activity_dark.png)
[<img width=200 alt="Main Activity" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/backup_activity_dark.png">](https://raw.githubusercontent.com/flocke/andOTP/master/assets/screenshots/backup_activity_dark.png)

## Acknowledgments:
#### Open-source components used:

 * [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/)
 * [LicensesDialog](https://github.com/PSDev/LicensesDialog)
 * [MaterialProgressBar](https://github.com/DreaminginCodeZH/MaterialProgressBar)
 * [OpenPGP API library](https://github.com/open-keychain/openpgp-api)
 * [VNTNumberPickerPreference](https://github.com/vanniktech/VNTNumberPickerPreference)
 * [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded)

#### Code examples used:

 * [Android-ItemTouchHelper-Demo](https://github.com/iPaulPro/Android-ItemTouchHelper-Demo/tree/master/app/src/main/java/co/paulburke/android/itemtouchhelperdemo/helper)
 * [Code Parts from Google's Android Samples](https://android.googlesource.com/platform/development/+/master/samples/Vault/src/com/example/android/vault)
 * [FloatingActionMenuAndroid](https://github.com/pmahsky/FloatingActionMenuAndroid)

## License:
```
Copyright (C) 2017 Jakob Nixdorf <flocke@shadowice.org>
Copyright (C) 2015 Bruno Bierbaumer

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in the
Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
```
