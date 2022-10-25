package eu.webdad.dasbibelquiz

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)

        if (Build.VERSION.SDK_INT < 30) {
            val decorView: View = window.decorView
            val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        } else {
            window.decorView.windowInsetsController?.hide(
                WindowInsets.Type.systemBars()
            )
        }

        setContentView(R.layout.activity_main)


        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("MyApplication", "${message.message()} -- From line " +
                        "${message.lineNumber()}")
                return true
            }
        }
        val settings: WebSettings = myWebView.settings

        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT;
        settings.userAgentString = "bibelquiz-android-app";

        val deviceOnline = isDeviceOnline(this.applicationContext)
        val internetAvailable = isInternetAvailable()
        if(!deviceOnline || !internetAvailable) {
                setContentView(R.layout.activity_main_offline)
                val textView = findViewById<View>(R.id.textView)
                val button = findViewById<View>(R.id.button)
                button.setOnClickListener(View.OnClickListener {
                    val intent = intent
                    this.finish()
                    startActivity(intent)
                })
        }
        myWebView.loadUrl("https://dasbibelquiz.de")
        //setContentView(myWebView)
    }
    private fun isDeviceOnline(context: Context): Boolean {
        val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
            if (networkCapabilities == null) {
                return false
            } else {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                ) {
                    return true
                } else {
                    return false
                }
            }
        } else {
            // below Marshmallow
            val activeNetwork = connManager.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting == true && activeNetwork.isAvailable
        }
    }

    fun isInternetAvailable(): Boolean {
        val urlConnection =
            URL("https://clients3.google.com/generate_204").openConnection() as HttpsURLConnection
        try {
            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 1000
            urlConnection.connect()
            return urlConnection.responseCode == 204
        } catch (e: Exception) {
            return false
        }
    }
}