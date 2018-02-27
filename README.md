#  andOTP -  Android OTP Authenticator

[![Build Status](https://travis-ci.org/andOTP/andOTP.svg?branch=master)](https://travis-ci.org/andOTP/andOTP)
[![Current release](https://img.shields.io/github/release/andOTP/andOTP/all.svg)](https://github.com/andOTP/andOTP/releases/download/v0.4.0.1/andOTP_v0.4.0.1.apk)

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
 * Encrypted storage with two backends:
   - Android KeyStore
   - Password / PIN
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

### Automatic backups:

 * BroadcastReceivers: AndOTP supports a number of broadcasts to perform automated backups, eg. via Tasker. These will get saved to the defined backup directory. **These only work when KeyStore is used as the encryption mechanism**
   - **org.shadowice.flocke.andotp.broadcast.PLAIN_TEXT_BACKUP**: Perform a plain text backup. **WARNING**: This will save your 2FA tokens onto the disk in an unencrypted manner!
   - **org.shadowice.flocke.andotp.broadcast.ENCRYPTED_BACKUP**: Perform an encrypted backup of your 2FA database using the selected password in settings.

## Migration:

Check out [this](https://github.com/andOTP/andOTP/wiki/Migration) wiki page to learn about the different ways to migrate to andOTP from other 2FA apps.

## Downloads:

[<img height=80 alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" />](https://play.google.com/store/apps/details?id=org.shadowice.flocke.andotp)
[<img height=80 alt="Get it on F-Droid" src="https://f-droid.org/badge/get-it-on.png" />](https://f-droid.org/packages/org.shadowice.flocke.andotp/)
[<img height=80 alt="Get it on GitHub" src="https://raw.githubusercontent.com/flocke/andOTP/master/assets/badges/get-it-on-github.png" />](https://github.com/andOTP/andOTP/releases)

**Warning**: All three versions (Google Play, F-Droid and the APKs) are not compatible (not signed by the same key)!
You will have to uninstall one to install the other, which will delete all your data.
So make sure you have a **current backup** before switching!

## Contribute:

 * **Translation**: If you want to help translate andOTP into your language head over to the [Crowdin project](https://crowdin.com/project/andotp).
 * **Bug reports and feature requests**: You can report bugs and request features in the [Issue tracker](https://github.com/andOTP/andOTP/issues) on GitHub.
 * **Requesting thumbnails**: If you are missing a thumbnail you can request it by editing the [wiki](https://github.com/andOTP/andOTP/wiki/Thumbnails#thumbnail-requests)
 * **Discussion and support**: 
   - [XDA thread](https://forum.xda-developers.com/android/apps-games/app-andotp-android-otp-authenticator-t3636993) (please keep off-topic to a minimum)
   - Telegram group [@andOTP](https://t.me/andOTP) (if you just want important updates you can mute the group so you only get notified about pinned messages)

#### Developers:

 * [Jakob Nixdorf](https://github.com/flocke)
 * [Richy HBM](https://github.com/RichyHBM)

#### Contributors:

 * [Carlos Melero](https://github.com/carmebar) ([view contributions](https://github.com/andOTP/andOTP/commits/master?author=carmebar))
 * [SuperVirus](https://github.com/SuperVirus) ([view contributions](https://github.com/andOTP/andOTP/commits/master?author=SuperVirus))
 * [DanielWeigl](https://github.com/DanielWeigl) ([view contributions](https://github.com/andOTP/andOTP/commits/master?author=DanielWeigl))
 * [Matthias Bertram](https://github.com/mbertram) ([view contributions](https://github.com/andOTP/andOTP/commits?author=mbertram))
 * [Björn Richter](https://github.com/x3rAx) ([view contributions](https://github.com/andOTP/andOTP/commits?author=x3rAx))

#### Translators:

&nbsp; | Language          | Translators
------ | ----------------- | -----------
🇵🇱   | Polish (pl-rPL)   | [Daniel Pustuła](https://github.com/9Cube-dpustula)
:es:   | Spanish (es-rES)  | [Carlos Melero](https://crowdin.com/profile/carmebar)
:de:   | German (de-rDE)   | [SuperVirus](https://crowdin.com/profile/SuperVirus), [Jan](https://crowdin.com/profile/Dagefoerde)
:fr:   | French (fr-rFR)   | [Johan Fleury](https://github.com/johanfleury), [David Sferruzza](https://crowdin.com/profile/dsferruzza), [primokorn](https://crowdin.com/profile/primokorn), [Poussinou](https://crowdin.com/profile/Poussinou)
🇳🇱   | Dutch (nl-rNL)    | Toon, [rain2reign](https://crowdin.com/profile/rain2reign), [thinkwell](https://crowdin.com/profile/thinkwell), [cpu20](https://crowdin.com/profile/cpu20)
&nbsp; | Galician (gl-rES) | [Triskel](https://crowdin.com/profile/triskel), [Xosé M.](https://crowdin.com/profile/XoseM)
:ru:   | Russian (ru-rRU)  | [Victor Nidens](https://crowdin.com/profile/vnidens), [Ilia Drogaitsev](https://crowdin.com/profile/waytoroot), [Dmitry](https://crowdin.com/profile/SaintDI)
🇨🇿   | Czech (cs-rCZ)    | [Picard0147](https://crowdin.com/profile/Picard0147)
:cn: | Chinese Simplified (zh-rCN) | [Cp0204](https://crowdin.com/profile/Cp0204)
&nbsp; | Catalan (ca-rES) | [isard](https://crowdin.com/profile/isard)

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
 * [Expandable Layout](https://github.com/AAkira/ExpandableLayout)
 * [LicensesDialog](https://github.com/PSDev/LicensesDialog)
 * [MaterialProgressBar](https://github.com/DreaminginCodeZH/MaterialProgressBar)
 * [OpenPGP API library](https://github.com/open-keychain/openpgp-api)
 * [VNTNumberPickerPreference](https://github.com/vanniktech/VNTNumberPickerPreference)
 * [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded)

#### Code examples used:

 * [Android-ItemTouchHelper-Demo](https://github.com/iPaulPro/Android-ItemTouchHelper-Demo/tree/master/app/src/main/java/co/paulburke/android/itemtouchhelperdemo/helper)
 * [Code Parts from Google's Android Samples](https://android.googlesource.com/platform/development/+/master/samples/Vault/src/com/example/android/vault)
 * [FloatingActionMenuAndroid](https://github.com/pmahsky/FloatingActionMenuAndroid)
 * [LetterBitmap](http://stackoverflow.com/questions/23122088/colored-boxed-with-letters-a-la-gmail)
 * [DimensionConverter](https://stackoverflow.com/questions/8343971/how-to-parse-a-dimension-string-and-convert-it-to-a-dimension-value)

## License:
```
Copyright (C) 2017-2018 Jakob Nixdorf <flocke@shadowice.org>
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
