package com.llicorp.memosontology.data

data class Memo(
    val id: String = "",          // ES doc_id
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val timestamp: String = "",
    val status: String = "etc",
    val epistemicStatus: String = "Presumed"   // Asserted/Presumed/Defeasible/Defeated
)

data class MemosResponse(
    val success: Boolean,
    val memos: List<Memo> = emptyList(),
    val error: String? = null
)

data class SaveMemoRequest(
    val content: String,
    val tags: List<String>,
    val status: String
)

data class SaveResponse(
    val success: Boolean,
    val id: String? = null,
    val error: String? = null
)

// Search uses JSON body so both client and server agree on the contract
data class SearchRequest(val query: String)
