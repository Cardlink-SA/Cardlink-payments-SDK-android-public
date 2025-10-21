package gr.cardlink.payments.app.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.api.PaymentResponse
import gr.cardlink.payments.api.PaymentsSdk

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getSerializableExtra(PaymentsSdk.KEY_PAYMENT_RESPONSE) as? PaymentResponse
            data?.let { response ->
                Log.d(TAG, "response: $response")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val amountEditText: EditText? = findViewById(R.id.amountEditText)
        val currencyEditText: EditText? = findViewById(R.id.currencyEditText)
        val descriptionEditText: EditText? = findViewById(R.id.descriptionEditText)
        val addressEditText: EditText? = findViewById(R.id.addressLineEditText)
        val cityEditText: EditText? = findViewById(R.id.cityEditText)
        val countryEditText: EditText? = findViewById(R.id.countryEditText)
        val zipEditText: EditText? = findViewById(R.id.zipCodeEditText)
        val recurringFrequencyEditText: EditText? = findViewById(R.id.recurringFrequency)
        val recurringEndDateEditText: EditText? = findViewById(R.id.recurringEndDate)

        val proceedButton: Button? = findViewById(R.id.proceedButton)

        proceedButton?.setOnClickListener {

            val paymentTotal = amountEditText?.text?.toString()
            if (paymentTotal.isNullOrBlank()) {
                return@setOnClickListener
            }

            val paymentRequest = PaymentRequest(
                paymentTotalCents = amountEditText.text?.toString()?.toLong() ?: 0,
                currencyCode = currencyEditText?.text?.toString() ?: "978",
                description = descriptionEditText?.text?.toString() ?: "",
                addressLine = addressEditText?.text?.toString() ?: "",
                city = cityEditText?.text?.toString() ?: "",
                countryCode = countryEditText?.text?.toString() ?: "",
                postalCode = zipEditText?.text?.toString() ?: "",
                recurringFrequency = recurringFrequencyEditText?.text?.toString()?.toIntOrNull(),
                recurringEndDate = recurringEndDateEditText?.text?.toString()
            )

            val intent = PaymentsSdk.newIntent(this, paymentRequest)
            activityResultLauncher.launch(intent)
        }
    }
}