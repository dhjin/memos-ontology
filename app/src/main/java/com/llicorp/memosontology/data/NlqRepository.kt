package com.llicorp.memosontology.data

import kotlinx.coroutines.flow.first

class NlqRepository(private val serverConfig: ServerConfig) {

    private suspend fun api(): MemoApiService {
        val url = serverConfig.serverUrl.first()
        return RetrofitClient.createService(url)
    }

    suspend fun query(question: String): String {
        val response = api().nlQuery(question)
        if (!response.isSuccessful) {
            error("Server returned ${response.code()}")
        }
        val html = response.body()?.string() ?: return "(no response)"
        return stripHtml(html)
    }

    private fun stripHtml(html: String): String {
        // Remove script/style blocks
        var text = html.replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
        // Replace block-level tags with newlines
        text = text.replace(Regex("<(br|p|div|li|tr|h[1-6])[^>]*>", RegexOption.IGNORE_CASE), "\n")
        // Strip remaining tags
        text = text.replace(Regex("<[^>]+>"), "")
        // Decode common HTML entities
        text = text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
        // Collapse whitespace
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
            .trim()
    }
}
