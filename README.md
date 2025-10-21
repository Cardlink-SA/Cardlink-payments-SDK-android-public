# Cardlink Payments SDK for Android
<p align="center"><img src="logo.png" alt="Cardlink logo" /></p>

## Introduction
Cardlink Payments SDK for Android is a comprehensive, easy-to-integrate solution specifically designed for eCommerce applications on the Android platform. This powerful SDK enables seamless processing of payments through various methods, such as Credit Cards, IRIS Online Payments, and PayPal. It aims to provide your customers with a smooth and secure transaction experience, while also offering developers a straightforward integration process.
Also, it enables users to conveniently store their credit cards on secure Cardlink servers, allowing for seamless reuse without re-entering card information multiple times. As the cards are never retained on the user's device and encrypted card data is transmitted to the server only once, the SDK delivers heightened security while simultaneously enhancing user experience.
Read on for more details on the integration process and sample code to get you started with the Cardlink Android Payments SDK. We look forward to making your eCommerce application more powerful, secure, and user-friendly.

## Features
* Seamless Payment Processing: Effortlessly handle transactions through a variety of payment options, including Credit Cards, IRIS Online Payments, and PayPal, ensuring an all-encompassing payment solution for your eCommerce application.
* Secure Storage: The Cardlink Payments SDK for Android allows users to securely store their credit card information on the protected Cardlink servers, enabling seamless reuse without the need for re-entering card details multiple times.
* Enhanced Security: With the Cardlink Payments SDK for Android, users' card data is never stored on their devices, and encrypted card information is transmitted to the server only once. This approach significantly heightens the security measures in place, safeguarding sensitive information.
* Optimized User Experience: By providing secure storage and seamless payment processing, the SDK ensures an improved user experience that minimizes friction during transactions and promotes customer satisfaction.
* Straightforward Integration: Designed with developers in mind, the Cardlink Payments SDK for Android offers a simple and efficient integration process, allowing you to quickly incorporate its features into your eCommerce application.

## Requirements
Android API 26+

## Publish library to local maven
Run `./gradlew clean payments-sdk:publishReleasePublicationToMavenLocal` or select the `publishReleasePublicationToMavenLocal` gradle task from Android Studio.

## Installation via Gradle
Add repository
```groovy
maven { url "https://maven.pkg.github.com/novidea-dev/Cardlink-payments-SDK-android-public" }
```

Add dependency to `build.gradle`:
```
implementation "gr.cardlink.payments:payments-sdk:1.x"
```

## Usage
Follow these steps to start processing payments with Cardlink Android Payments SDK:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentsSdk.init(
            appContext = this,
            baseUrl = "https://baseUrl/",
            // optional headers param
            headers = mapOf(
                "Authorization" to "Bearer YourAuthToken",
                "Cookie" to "key=value"
            )
        )
    }
}
```

There is a convenient function `PaymentsSdk.newIntent()` that accepts as parameter a `PaymentRequest` object and launches the payment flow.

```kotlin 
data class PaymentRequest(
    // Total amount in the smallest currency unit (eg. cents) 
    val paymentTotalCents: Long, 
    // Currency code (eg. EUR), or number (eg. 971) 
    val currencyCode: String,
    // Purchace description 
    val description: String, 
    // Address line 
    val addressLine: String,
    // City 
    val city: String,
    // Country code (eg. GR) or number (eg. 370) 
    val countryCode: String, 
    // Zip/postal code 
    val postalCode: String,
    // Recurring frequency
    val recurringFrequency: Int? = null,
    // Recurring end date with format YYYYMMDD. This is mandatory if recurringFrequency is present.
    val recurringEndDate: String? = null
)
```

Start activity for result. SDK will post back, with key `PaymentActivity.KEY_PAYMENT_RESPONSE`, the following response:

```kotlin 
data class PaymentResponse( 
    val orderId: String?, 
    val transactionId: String?, 
    val errorCode: String?, 
    val description: String?, 
    val orderAmount: String?, 
    val paymentTotal: String?, 
    val currency: String?, 
    val paymentReference: String? 
) 
```

#### Example
```kotlin
// register for activity result
private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val paymentResponse = it.data?.getSerializableExtra(PaymentsSdk.KEY_PAYMENT_RESPONSE) as? PaymentResponse
            paymentResponse?.let { 
                // do something with response
            }
        }

private fun makePayment() {
    val paymentRequest = PaymentRequest(/* request params */)
    val intent = PaymentsSdk.newIntent(this, paymentRequest)
    activityResultLauncher.launch(intent)
}
```

#### Common error codes
| Code                      | Description           |
| ------------------------- | --------------------- |
| error_network             | network error |
| error_invalid_input       | when request values are invalid |
| error_remote_encryption   | remote encryption script failure  |
| error_payment_methods     | no valid payment methods available |
| error_accepted_card_types | no valid card types available |

## Currency
### Specifying amounts
Currency amounts should be provided via the amount parameter as positive integers, representing how much to charge in the smallest currency unit (eg. 100 cents to charge €1.00, or 50050 cents to charge €500.50).

### Specifying code/number
The three-letter currency code, as defined by ISO-4217, of the transaction, such as "EUR" for Euros, or the respective number "978".

## Recurring payments
### Recurring frequency
Indicates the frequency of recurring payments and defines the minimum number of days between two subsequent payments. 28 is special value indicating that transactions are to be initiated on monthly basis. Max value is 30.

### Recurring end date
Recurring end date with format YYYYMMDD. This is mandatory if recurring frequency is present.

## Flows
### Successful payment flow (existing card)
<img width="1210" alt="payment_success" src="https://user-images.githubusercontent.com/9013629/230711410-f97cf837-5d9a-4ba8-903d-7f749d90c85d.png">

### Unsuccessful payment flow (existing card)
<img width="1206" alt="payment_failure" src="https://user-images.githubusercontent.com/9013629/230711436-81a1a30a-d75b-4959-88be-0b58dd5eebe5.png">

### IRIS payment
<img width="287" alt="iris" src="https://user-images.githubusercontent.com/9013629/230711478-e9d970f5-31c8-4514-9439-7924a56c39fb.png">

### Paypal payment
<img width="287" alt="paypal" src="https://user-images.githubusercontent.com/9013629/230711485-369c13eb-afef-48da-8222-4290d45103f5.png">

### Payment with new card
<img width="911" alt="card_add" src="https://user-images.githubusercontent.com/9013629/230711532-9d78770c-c25c-43d0-8639-789a8a3648bf.png">

### Add card with NFC
<img width="1221" alt="card_nfc" src="https://user-images.githubusercontent.com/9013629/230711551-ace94c73-6c24-4352-94ce-21f50d691095.png">

### Delete card
<img width="599" alt="card_delete" src="https://user-images.githubusercontent.com/9013629/230711561-ab8d7c36-bfa0-4249-abc5-f7fd9a747670.png">

### Instalments
<img width="1218" alt="instalments" src="https://user-images.githubusercontent.com/9013629/230711574-da691689-804b-4c39-830a-b656d1e1809a.png">

## License
Cardlink Payments SDK for Android is released under the MIT license. [See LICENSE](/LICENSE.md) for details.
