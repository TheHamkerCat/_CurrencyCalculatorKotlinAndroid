package com.hamker.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var usd: Double = 0.0
    private var eur: Double = 0.0
    private var aud: Double = 0.0
    private var cad: Double = 0.0
    private var gbp: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun clearTextView(all: Boolean) {
            if (tvInput.text.isNotEmpty()) {
                if (all) {
                    tvInput.text = ""
                } else {
                    tvInput.text = tvInput.text.subSequence(0, tvInput.text.length - 1)
                }
            }
        }
        btnAllClear.setOnClickListener {
            clearTextView(all=true)
        }

        btnDEL.setOnClickListener {
            clearTextView(all=false)
        }

        val url = "https://thearq.tech/exchange/latest"
        val queue = Volley.newRequestQueue(this)

        fun setCurrencies(json: JSONObject) {
            val c = json.getJSONObject("result").getJSONObject("currency")
            usd = c.get("USD") as Double
            eur = c.get("EUR") as Double
            aud = c.get("AUD") as Double
            cad = c.get("CAD") as Double
            gbp = c.get("GBP") as Double
        }

        val jsonRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                setCurrencies(response)
            },
            { Toast.makeText(this, "$it\nARQ API ERROR", Toast.LENGTH_LONG).show() }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-API-KEY"] = "AHXMAW-YEKWXI-ZTEQLG-KAGQPD-ARQ"
                return headers
            }
        }

        queue.add(jsonRequest)
    }


    fun onDigit(view: View) {
        tvInput.append((view as Button).text)
    }

    fun onDot(view: View) {
        if (tvInput.text[tvInput.text.length - 1] != '.') {
                tvInput.append((view as Button).text)
        }
    }

    private fun containsOperator(t: String): Boolean {
        if (t.isEmpty()) return false

        return if (t.startsWith("-")) {
            if (t.length == 1) {
                true
            } else {
                containsOperator(t.removePrefix("-"))
            }
        } else {
            t.contains("+") ||
            t.contains("/") ||
            t.contains("*") ||
            t.contains("%") ||
            t.contains("-")
        }
    }

    fun onOperator(view: View) {
        if (tvInput.text.isNotEmpty()) {
            // can only use operators if there is no other operator in textView
            if (!containsOperator(tvInput.text.toString())) {
                    tvInput.append((view as Button).text)
            }
        } else {
            // Can only use subtract operator if text view is empty
            if ((view as Button).text == "-") {
                tvInput.append(view.text)
            }
        }
    }

    private fun add(a: String, b: String): Double {
        return a.toDouble() + b.toDouble()
    }

    private fun subtract(a: String, b: String): Double {
        return a.toDouble() - b.toDouble()
    }

    private fun multiply(a: String, b: String): Double {
        return a.toDouble() * b.toDouble()
    }

    private fun divide(a: String, b: String): Double {
        return a.toDouble() / b.toDouble()
    }

    private fun percentage(a: String, b: String): Double {
        return a.toDouble() % b.toDouble()
    }

    private fun solve(e: String): String {
        // e is the expression, 5 + 4
        val isNegative = e.startsWith("-")

        val e = e.removePrefix("-") // Removes "-" prefix if exists

        val result = when {
            e.contains("+") -> add(e.split("+")[0], e.split("+")[1])
            e.contains("-") -> subtract(e.split("-")[0], e.split("-")[1])
            e.contains("*") -> multiply(e.split("*")[0], e.split("*")[1])
            e.contains("/") -> divide(e.split("/")[0], e.split("/")[1])
            e.contains("%") -> percentage(e.split("%")[0], e.split("%")[1])
            else -> 0.0
        }
        return if (isNegative) "-$result" else result.toString()
    }

    fun onEquals(view: View) {
        val t = tvInput.text.toString()
        if (
            t[t.length - 1].isDigit()
            && containsOperator(t)
        ) {
            tvInput.text = solve(t)
        }
    }

    private fun appendCurrency(currency: Double) {
        val lastChar: Char = (
                if (tvInput.text.isEmpty()) '*' // return an arbitrary char
                else tvInput.text[tvInput.text.length - 1]
                )

        if (!lastChar.isDigit() && lastChar != '.') {
            tvInput.append(currency.toString())
        }
    }

    fun onCurrency(view: View) {
        when ((view as Button).text.toString()) {
            "USD" -> appendCurrency(usd)
            "EUR" -> appendCurrency(eur)
            "AUD" -> appendCurrency(aud)
            "CAD" -> appendCurrency(cad)
            "GBP" -> appendCurrency(gbp)
        }
    }
}