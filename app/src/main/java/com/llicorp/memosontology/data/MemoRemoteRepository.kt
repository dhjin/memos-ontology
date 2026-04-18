package com.llicorp.memosontology.data

import kotlinx.coroutines.flow.first
import java.io.IOException

class MemoRemoteRepository(
    private val serverConfig: ServerConfig,
    private val db: AppDatabase
) {
    private val dao get() = db.memoDao()

    private suspend fun api(): MemoApiService {
        val url = serverConfig.serverUrl.first()
        return RetrofitClient.createService(url)
    }

    suspend fun login(): Boolean {
        val user = serverConfig.username.first()
        val pass = serverConfig.password.first()
        return try {
            api().login(user, pass).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllMemos(): List<Memo> {
        return try {
            val response = api().getMemos()
            if (response.isSuccessful) {
                val memos = response.body()?.memos ?: emptyList()
                dao.clearAll()
                dao.insertAll(memos.map { it.toEntity() })
                memos
            } else {
                dao.getAll().map { it.toMemo() }
            }
        } catch (e: IOException) {
            dao.getAll().map { it.toMemo() }
        }
    }

    suspend fun searchMemos(query: String): List<Memo> {
        return try {
            val response = api().searchMemos(SearchRequest(query))
            if (response.isSuccessful) response.body()?.memos ?: emptyList()
            else dao.search("%$query%").map { it.toMemo() }
        } catch (e: IOException) {
            dao.search("%$query%").map { it.toMemo() }
        }
    }

    suspend fun saveMemo(memo: Memo): Boolean = try {
        api().saveMemo(SaveMemoRequest(memo.content, memo.tags, memo.status)).isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun deleteMemo(memoId: String): Boolean = try {
        api().deleteMemo(memoId).isSuccessful
    } catch (e: Exception) {
        false
    }
}
