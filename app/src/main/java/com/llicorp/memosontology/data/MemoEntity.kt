package com.llicorp.memosontology.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val tagsJson: String,   // List<String> serialized as JSON
    val timestamp: String,
    val status: String,
    val epistemicStatus: String
)

private val gson = Gson()
private val listType = object : TypeToken<List<String>>() {}.type

fun MemoEntity.toMemo() = Memo(
    id = id,
    title = title,
    content = content,
    tags = gson.fromJson(tagsJson, listType) ?: emptyList(),
    timestamp = timestamp,
    status = status,
    epistemicStatus = epistemicStatus
)

fun Memo.toEntity() = MemoEntity(
    id = id,
    title = title,
    content = content,
    tagsJson = gson.toJson(tags),
    timestamp = timestamp,
    status = status,
    epistemicStatus = epistemicStatus
)
