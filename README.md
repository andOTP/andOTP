#  andOTP -  Android OTP Authenticator
![andOTP](./assets/logo.png)

andOTP is a two-factor authentication App for Android 4.4+.

It implements Time-based One-time Passwords (TOTP) like specified in RFC 6238.  
Simply scan the QR code and login with the generated 6-digit code. 

This is a fork of the great OTP Authenticator app written by Bruno Bierbaumer,
which has sadly been inactive since 2015. All credit for the original version
goes to Bruno.

## Features:

 * Free and Open-Source
 * Requires minimal permissions
   - Camera access for QR code scanning
   - Storage access for import and export of the database
 * Encrypted storage
 * Sleek minimalistic Material Design
 * Great Usability
 * Compatible with Google Authenticator

## TODO

 * Export the secret key (maybe)

<!--

## Download:
### Google Play:

<a href="https://play.google.com/store/apps/details?id=net.bierbaumer.otp_authenticator&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img width=250 alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" /></a>


### F-Droid:
[OTP Authenticator on F-Droid](https://f-droid.org/repository/browse/?fdfilter=otp&fdid=net.bierbaumer.otp_authenticator)

-->

## Download
### APK:
[![Download latest version (v0.1.0)](https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=https://github.com/flocke/andOTP/releases/download/v0.1.0/andOTP_v0.1.0.apk)](https://github.com/flocke/andOTP/releases/download/v0.1.0/andOTP_v0.1.0.apk)

## Acknowledgments:
#### Open-source components used:

 * [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/)
 * [MaterialProgressBar](https://github.com/DreaminginCodeZH/MaterialProgressBar)
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
