package com.websarva.wings.android.mycreatures.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.websarva.wings.android.mycreatures.R
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.io.InputStream
import java.lang.Integer.min
import java.net.URL


class WebActivity : AppCompatActivity() {
    private lateinit var currentUrl: String
    var title: String = ""
    var bitmap: Bitmap? = null
    var imgSrc: String? = null
    private var apgKey = arrayListOf<String>()
    private var apgValue = arrayListOf<String>()
    var apgExplanation: String = ""

    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        //val myWebView = WebView(applicationContext)
        //setContentView(myWebView)
        // レイアウトで指定したWebViewのIDを指定する
        val myWebView: WebView = findViewById(R.id.webview)
        //javascriptを許可する
        myWebView.getSettings().setJavaScriptEnabled(true);
        //myWebView.addJavascriptInterface(JavaScriptInterface(), "showHtml")

        //リンクをタップしたときに標準ブラウザを起動させない
        myWebView.webViewClient = WebViewClient()
//        myWebView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView, url: String) {
//                Log.d("html", "onPageFinished is called")
//                myWebView.loadUrl("javascript:window.showHtml.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
//            }
        //}

        myWebView.loadUrl("https://ja.wikipedia.org/wiki/Category:%E6%A4%8D%E7%89%A9%E5%88%86%E9%A1%9E")

    }

    public fun addCurrentItem(view: View) {
        val myWebView: WebView = findViewById(R.id.webview)
        currentUrl = myWebView.url.toString()
        Log.i("addCurrentItem is called", "")
        Toast.makeText(this@WebActivity, myWebView.url, Toast.LENGTH_LONG).show()
        //val document =  Jsoup.connect(myWebView.url).get()
        //val words = document.text()
        //Log.i("addCurrentItem", words)
        WebScratch().execute()

    }

    inner class WebScratch : AsyncTask<Void, Void, Void>() {
        private lateinit var words: String
        override fun doInBackground(vararg params: Void): Void? {
            try {
                val document =  Jsoup.connect(currentUrl).get()
                words = document.text()

                // parse main image
                parseMainImage(document)
                // parse table
                parseTable(document)
                // parse explanation
                parseExplanation(document)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun parseMainImage(document: Document) {
            //Get the logo source of the website
            val img = document.select("img")[3]
            Log.i("document.image", img.toString())
            // Locate the src attribute
            val imgSrc = img?.absUrl("src")
            Log.i("document.image", imgSrc.toString())
            // Download image from URL
            val input: InputStream = URL(imgSrc).openStream()
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input)
        }

        fun parseTable(document: Document) {
            val table =  document.select(".borderless").first()
            val rows = table?.select("td");

            var next = 0
            if (rows == null) {
                return
            }
            for (i in 0 until rows.size) { //first row is the col names so skip it.
                val row = rows[i]
                val cols: Elements = row.select("td")
                if (cols.text() == ":") {
                    next = 1
                } else {
                    if (next == 0) {
                        apgKey.add(cols.text())
                    } else { // next == 1
                        apgValue.add(cols.text())
                        next = 0
                    }
                }
            }
            for (i in 0 until apgKey.size) {
                Log.i("document.table", apgKey[i] + " : " + apgValue[i])
            }
        }

        fun parseExplanation(document: Document) {
            val mwContentText = document.select("#mw-content-text");
            val explanations = mwContentText.select("p")
            if (explanations == null) {
                return
            }
            //for (i in 0 until explanations.size) {
            for (i in 0 until min(1, explanations.size)) {
                apgExplanation += explanations[i].text()
                Log.i("document.explanation", explanations[i].text())
            }
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            Log.i("addCurrentItem", words)
            //Toast.makeText(this@WebActivity, words, Toast.LENGTH_LONG).show()
            Log.i("MyPaintpediA", "cratePhylogeneticTreeView called.")
            val intent2AddItemFromWebActivity = Intent(this@WebActivity, AddItemFromWebActivity::class.java).apply {
                putExtra("title", title)
                putExtra("image", bitmap)
                putExtra("key", apgKey)
                putExtra("value", apgValue)
                putExtra("explanation", apgExplanation)
            }
            startActivity(intent2AddItemFromWebActivity)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val myWebView: WebView = findViewById(R.id.webview)
        // 端末の戻るボタンでブラウザバック
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}