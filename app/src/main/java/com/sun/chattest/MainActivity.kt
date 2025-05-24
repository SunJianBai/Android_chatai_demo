package com.sun.chattest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import io.noties.markwon.Markwon
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val question = findViewById<EditText>(R.id.etQuestion)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val txtResponse = findViewById<TextView>(R.id.txtResponse)
        val markwon = Markwon.create(this)


        btnSubmit.setOnClickListener {
            val questionText = question.text.toString()
            Toast.makeText(this, "提问中: $questionText", Toast.LENGTH_SHORT).show()

            getResponse(questionText) { response ->
                runOnUiThread {
                    val formattedResponse = response.replace("\\n", "\n")
                    markwon.setMarkdown(txtResponse, formattedResponse)
                }
            }
        }
    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKey = "sk-trtxvukyresekejjnymgityktjyybtmtccvnlbwhbwnnvoji"  // 建议通过 gradle.properties 或安全方式存储
        val url = "https://api.siliconflow.cn/v1/chat/completions" // 示例URL，请根据实际修改

        val json = """
            {
                "model": "Qwen/QwQ-32B",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "$question"}
                ],
                "temperature": 0.7,
                "max_tokens": 1024
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("请求失败: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback("服务器错误: ${response.code}")
                    return
                }

                val bodyString = response.body?.string()
                // 注意：你需要从 JSON 中提取实际回答内容
                // 假设返回 JSON 类似：{"choices":[{"message":{"content":"答案"}}]}
                val content = Regex("\"content\"\\s*:\\s*\"(.*?)\"")
                    .find(bodyString ?: "")?.groups?.get(1)?.value

                callback(content ?: "解析失败")
            }
        })
    }
}
