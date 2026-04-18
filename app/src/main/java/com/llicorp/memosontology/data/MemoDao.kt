package com.llicorp.memosontology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memos: List<MemoEntity>)

    @Query("SELECT * FROM memos ORDER BY timestamp DESC")
    suspend fun getAll(): List<MemoEntity>

    @Query("SELECT * FROM memos WHERE title LIKE :q OR content LIKE :q ORDER BY timestamp DESC")
    suspend fun search(q: String): List<MemoEntity>

    @Query("DELETE FROM memos")
    suspend fun clearAll()
}
