# QRSA
RSA OTP Android Authentication App based on QR codes

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=eu.sebbe.www.qrsa)

This is an app to authenticate users via their cell phone.
Note that this is not designed to be a 2FA application, even if it can be used as such.

The intended flow is, that the user enters their username, press "next" button. Then the user's public key is fetched, and a QR code is generated using the user's public key, containing the correct one-time password to be used to authenticate, that is shown along with a field to enter said OTP.
The security goals of the system makes the system sufficently secure for being used as a single physical factor.

Note that there is no device attestation support, so a user could easily generate a keypair insecurely, outside of secure hardware, and submit it for enrolling.
If a system administrator wants to ensure that the key is stored securely, its recommended that the system administrator enrolls the phone for the user. Once enrolled, it will be impossible, yes completely IMPOSSIBLE, to extract the private key out of app.

The technical goals for the app is:

1. The app shouldn't require any communications with the outside world when authenticating. Enrolling is a another thing.
2. There should be a link between the authentication request and the app, which in this case is done using QR codes.

The security threat model, or security goal is:

1. If someone does not have access to the phone, person should not be able to authenticate to the service in question.
2. If someone does have acces to the phone, person should be considered authorized.
3. It does not matter if the phone is stolen or whatever, its the authorized person's responsibility to protect his phone.
4. If someone loses access to a phone, for example if a person turns in his job mobile after finished a employment, person should no longer have access.
5. It must be impossible to copy or clone the sensitive authentication data (private key) from the app.

A comparision to threat model can be a car key. A car key does not require authentication to use, but the car key must be impossible to duplicate.
This because a car rental company or private person may rent out or lend out a car.
After the car has been returned, it should not be possible for the adversiary to access the car any longer.

Prerequistes for running the app:

1. The phone must support hardware based storage. This is a storage that uses a "Security Chip" inside the phone, making it impossible to copy the key off the phone.
2. The store must be initalized. Sometimes its possible to initalize the store by setting up a PIN lock screen, and then just generating a key. Removing the lock screen will usally keep the key, unless the key properties was setup to require lock screen.
3. In some cases, a secure lock screen MUST be used. This is dependent on phone model.
4. The secure chip inside phone, must support operations based on 2048 bit RSA/ECB/PKCS1.5
5. In some cases, a rooted phone may permanently disable the security chip for security reasons.


Why enrollment and other urls does not require any form of confirmation:

Enrollment can seem to be a sensitive action, but it is really not. When a key is enrolled, the app first checks if there is already a key enrolled. In that case, that public key, is returned. Otherwise a new key is enrolled. As the public key is not sensitive, user action is instead clicking a link or scanning a QR code.

How the "Message" function works in the web service:

The web service must return the encrypted equvalient of PADDING::OTP::MESSAGE::HASH::PADDING.
The idea behind pre/post padding is to place the sensitive info on a random place inside the ciphertext, making chosen-chiphertext attacks
and known-plaintext-attacks harder.
The OTP is the code the user will use to authenticate. When user authenticated by scanning the QR code, this will be shown on-screen.
If the user authenticate by clicking the link inside the mobile (if the user accesses the web service from mobile), the OTP will be put in clipboard.
This means the user can authenticate directly by just pasting inside the OTP field in web application.
The HASH is the md5 out of OTP + MESSAGE + OTP, where + denotes string concatenation. The sandwiched construction prevents a attacker from moving the separator between OTP and MESSAGE. This HASH is added to prevent some rough forms of malleability attacks.

There are 4 actions available:

- s = Scanned action. This performs a DECRYPT action on the supplied data and shows the OTP on screen.
- c = Clicked action. This performs a DECRYPT action on the supplied data and puts OTP in clipboard.
- e = Enroll action. This generates a new keypair unless one already exists, and then exports Public key to clipboard.
- u = URL Enroll action. This generates a new keypair unless one already exists, and then appends the public key after the URL and redirects user to this.

How "u" is designed to be used:
The idea behind u is to allow enroll via a QR scan. The url is constructed by either using qrsa://us for HTTPS, and qrsa://u'anything' för HTTP, like qrsa://uh. Immediately after this, you put the URL, without scheme, as URLSafe Base64 encoded data.
This can then be shown as a QR code.

Please note that its important to add authentication data to the URL in question, like a session ID or one-time password, so a adversiary cannot enroll their own public keys to the user's account.

Note that the enroll script must check the key content for the error code "INCOMPATIBLE_DEVICE". If that is shown, this means the user's device doesn't support the app OR the user's key storage is not properly activated or initialized. A good thing with submitting this error code, is that the web service in question can pre-expire any enroll attempts that fail, for security reasons.

Note that the "e" enroll action is still recommended for when the user enroll directly on their mobile browser, as triggering the callback URL from inside the mobile browser can have unintended side effects.

OTP can be any format, but its generally a good idea to keep it short and secure. A good idea is using base32,
and then using a length somewhere 10 characters, which are a good balance between typing the OTP and security.

MESSAGE inside the application, is however something more useful, that is always shown on screen, regardless of mode.
The idea behind the MESSAGE, is a "Sign-What-You-See" system.
This means the web service, can attach a message, that will be delivered using the very same secure channel as the OTP code.

The intent behind "MESSAGE", is that the web service can use this to indicate the action being performed.
Take for example a user "SomeOne" wants to change their password.
MESSAGE could be set to like:

"Change password for user account SomeOne".

Same in a banking application, MESSAGE could be set to like:

"transfer $100 from account 1234 to account 3245"

Thus user will instantly notice if a attacker attempts to replace that transaction with another, as the
MESSAGE will not match the user's intent.

Thus, even if the user is subject to a MITM attack where a attacker replaces a QR code for a action the user wants to do (For example, post a new message),
with a action to compromise the user, the user will be able to detect the attack, as the message is affixed to the OTP code.
