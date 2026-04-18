package com.llicorp.memosontology.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SparqlResult(
    val variables: List<String>,
    val rows: List<Map<String, String>>
)

data class EntailmentPair(val subject: String, val obj: String)

class FusekiRepository(private val serverConfig: ServerConfig) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun fusekiEndpoint(): String {
        val base = serverConfig.fusekiUrl.first().trimEnd('/')
        return if (base.isEmpty()) error("Fuseki URL이 설정되지 않았습니다. 설정 화면에서 입력해 주세요.") else base
    }

    /** Generic SPARQL SELECT → SparqlResult */
    suspend fun query(sparql: String): SparqlResult = withContext(Dispatchers.IO) {
        val endpoint = fusekiEndpoint()
        val body = FormBody.Builder()
            .add("query", sparql)
            .build()
        val request = Request.Builder()
            .url("$endpoint/sparql")
            .addHeader("Accept", "application/sparql-results+json")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("Fuseki returned ${response.code}: ${response.message}")

        val json = JSONObject(response.body!!.string())
        val head = json.getJSONObject("head")
        val vars = head.getJSONArray("vars").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        val results = json.getJSONObject("results")
        val bindings = results.getJSONArray("bindings")
        val rows = (0 until bindings.length()).map { i ->
            val binding = bindings.getJSONObject(i)
            vars.associateWith { v ->
                if (binding.has(v)) binding.getJSONObject(v).optString("value", "") else ""
            }
        }
        SparqlResult(vars, rows)
    }

    /** Load all entailment pairs (:entails relationships) */
    suspend fun getEntailments(): List<EntailmentPair> {
        val sparql = """
            PREFIX : <http://example.org/ontology#>
            SELECT ?s ?o WHERE {
              ?s :entails ?o .
            }
            ORDER BY ?s
        """.trimIndent()
        return query(sparql).rows.map { row ->
            EntailmentPair(
                subject = row["s"]?.substringAfterLast("#") ?: "",
                obj = row["o"]?.substringAfterLast("#") ?: ""
            )
        }
    }

    /** Find entailments related to a given memo content (label match) */
    suspend fun getRelatedEntailments(keywords: List<String>): List<EntailmentPair> {
        if (keywords.isEmpty()) return emptyList()
        val filter = keywords.joinToString(" || ") { kw ->
            val escaped = kw.replace("\"", "\\\"")
            "CONTAINS(LCASE(STR(?s)), \"${escaped.lowercase()}\") || CONTAINS(LCASE(STR(?o)), \"${escaped.lowercase()}\")"
        }
        val sparql = """
            PREFIX : <http://example.org/ontology#>
            SELECT ?s ?o WHERE {
              ?s :entails ?o .
              FILTER($filter)
            }
        """.trimIndent()
        return query(sparql).rows.map { row ->
            EntailmentPair(
                subject = row["s"]?.substringAfterLast("#") ?: "",
                obj = row["o"]?.substringAfterLast("#") ?: ""
            )
        }
    }

    /** Load EpistemicStatus instances with their types */
    suspend fun getEpistemicInstances(): SparqlResult {
        val sparql = """
            PREFIX : <http://example.org/ontology#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            SELECT ?instance ?type ?confidence WHERE {
              ?instance rdf:type ?type .
              ?type rdfs:subClassOf* :EpistemicStatus .
              OPTIONAL { ?instance :confidence ?confidence }
            }
            ORDER BY ?type ?instance
        """.trimIndent()
        return query(sparql)
    }
}
