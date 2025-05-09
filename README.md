# Table of content
1. [RoFFu](#roffu)
2. [Demo](#demo)
3. [Want to try it?](#want-to-try-it)
4. [Screenshots](#screenshots)
5. [Tech Used](#tech-used)
6. [Want to connect?](#do-you-want-more)
7. [You liked this?](#you-like-what-iam-doing)
8. [Payment Flow](#payment-flow)
9. [Note for Future Developers](#note-for-future-developers)

# 👟RoFFu

RoFFu is an online store specialized in sports shoes & Sneakers of many famous companies like Nike & Adidas, with a modern UI & beautiful animations like adding products to cart, bookmarking and change the sizes 🤩🔥

# 🤳🏾Demo

<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/demo/ruffo_demo.gif" width="200">

# 🧐Want to try it?

You can download the demo from [here](https://github.com/mustfaibra/RoFFu/blob/master/app/release/roffu.apk).

# 📸Screenshots

<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/splash.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/landing.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/home.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/details.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/cart.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/login.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/checkout.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/profile.jpg" width="200">
<img src="https://github.com/mustfaibra/RoFFu/blob/master/app/screenshots/history.jpg" width="200">

# 🧑🏾‍💻Tech used

* [Jetpack compose](https://developer.android.com/jetpack/compose) for UI
* Compose [Navigation](https://developer.android.com/jetpack/compose/navigation)
* [Hilt](https://developer.android.com/training/dependency-injection/hilt-jetpack) for dependency Injection
* [Room](https://developer.android.com/training/data-storage/room) for storing complex data locally
* [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for shared preferences like app's launch state and language, etc.
* [MacroBenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) for measuring app performance.
* [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) to optimize performance to fasten up the startup process and also reducing heavy animations junk.
* [LottieFiles](https://github.com/airbnb/lottie/blob/master/android-compose.md) for loading animations with Compose.
* [Animations & Transitions](https://developer.android.com/jetpack/compose/animation) (Updated & Infinite)
* [Timber](https://github.com/JakeWharton/timber) for logging (Specially for lazy developers 🤣)
* [Coil](https://coil-kt.github.io/coil/compose/) for loading images with Jetpack Compose.
* Accompanist [Pager layout](https://google.github.io/accompanist/pager/)
* Accompanist [Placeholder](https://google.github.io/accompanist/placeholder/)
* [WhatIf](https://github.com/skydoves/WhatIf) for handling single if-else statements, nullable, collections, and boolean smoothly and in readable & cleaner way.

# 🔗Do you Want more?

Feel free to connect or to follow me here 👇🏾

[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com//in/mustafa-ibrahim-58b918206/)

[![twitter](https://img.shields.io/badge/twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/mustfaibra)

Also, don't forget to ⭐ this repository 😁

# 😍You like what Iam doing?

How about buying me a coffee so that I keep doing it 😁👇🏾

[☕ Buy me a coffee](https://www.buymeacoffee.com/mustfaibra)

# 💳 Payment Flow

## Checkout Screen
- Cart information: individual products, quantity per item, price per item
- Delivery address: no map view, select city, district, ward, specific address, recipient's phone number
- Payment method selection: COD or card payment

## Address Selection Screen
- Select delivery address

## Payment Method Screen
- Card API will be provided by us, we will create and check it
- Allow users to create new cards here
- Add money to cards in two ways: transfer from this account to another or we will access the backend admin

## Success/Failed Screen
- Show payment result

# 📝 Note for Future Developers

- For payment, Mr. HA said coding VnPay API is more difficult than coding backend. So whoever wants to do it, go ahead.
- For checklist, Mr. HA requires adding price field + filter sorting
