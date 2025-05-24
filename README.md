# chat AI
一个简单的小demo，实现了询问ai，获取结果，并返回给用户的功能

当前代码调用了硅基流动的api，你也可以切换成任意别的大模型

```kotlin
    private fun getResponse(question: String, callback: (String) -> Unit) {
    val apiKey = "sk-trtxvuk****bwnnvoji"  // 硅基流动的apikey，改成自己的apikey
    val url = "https://api.siliconflow.cn/v1/chat/completions" // 硅基流动的url,可以改成自己的

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
        """.trimIndent()   //model：模型名称，可以改成自己的模型名称，如：gpt-3.5-turbo

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
```



<img src="https://cdn.jsdelivr.net/gh/SunJianBai/pictures@main/img/202505241847703.png" alt="image-20250524184728434" style="zoom: 33%;" />

