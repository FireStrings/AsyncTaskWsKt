package br.edu.ifsp.scl.asynctaskwskt

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import br.edu.ifsp.scl.asynctaskwskt.MainActivity.constantes.URL_BASE
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    object constantes {
        val URL_BASE = "http://www.nobile.pro.br/sdm/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buscarInformacoesBt.setOnClickListener {

            val buscarTextoAt = BuscarTextoAt()

            buscarTextoAt.execute(URL_BASE + "texto.php")
            buscarData(URL_BASE + "data.php")
        }

    }

    private inner class BuscarTextoAt : AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            toast("Buscando String no Web Service")

            progressBar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {

            val url = params[0]

            val stringBufferResposta: StringBuffer = StringBuffer()
            try {

                val conexao = URL(url).openConnection() as HttpURLConnection
                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {

                    val inputStream = conexao.inputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    val respostaList = bufferedReader.readLines()

                    respostaList.forEach { stringBufferResposta.append(it) }
                }
            } catch (ioe: IOException) {
                toast("Erro na conexão!")
            }

            for (i in 1..10) {
                publishProgress(i)
                sleep(500)
            }

            return stringBufferResposta.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            toast("Texto recuperado com sucesso")

            textoTv.text = result

            progressBar?.visibility = View.GONE
        }

        override fun onProgressUpdate(vararg values: Int?) {

            values[0]?.apply { progressBar?.progress = this }
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun buscarData(url: String) {
        val buscaDataAS = object : AsyncTask<String, Void, JSONObject>() {
            override fun onPreExecute() {
                super.onPreExecute()
                progressBar.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg strings: String): JSONObject? {
                var jsonObject: JSONObject? = null
                val sb = StringBuilder()
                try {
                    val url = strings[0]
                    val conexao = URL(url).openConnection() as HttpURLConnection
                    if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                        val iss = conexao.inputStream
                        val br = BufferedReader(InputStreamReader(iss))
                        //var temp: String
                        for (temp in br.readLine()) {
                            sb.append(temp)
                        }
                    }
                    jsonObject = JSONObject(sb.toString())
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                } catch (jsone: JSONException) {
                    jsone.printStackTrace()
                }

                return jsonObject
            }

            override fun onPostExecute(s: JSONObject) {
                var data: String? = null
                var hora: String? = null
                var ds: String? = null
                super.onPostExecute(s)
                try {
                    data = s.getInt("mday").toString() + "/" + s.getInt("mon") + "/" + s.getInt("year")
                    hora = s.getInt("hours").toString() + ":" + s.getInt("minutes") + ":" + s.getInt("seconds")
                    ds = s.getString("weekday")
                } catch (jsone: JSONException) {
                    jsone.printStackTrace()
                }

                (tv_data as TextView).text = data + "\n" + hora + "\n" + ds
                progressBar.visibility = View.GONE
            }
        }
        buscaDataAS.execute(url)
    }

}