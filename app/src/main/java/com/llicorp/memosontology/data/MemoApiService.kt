package com.llicorp.memosontology.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface MemoApiService {
    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @GET("api/v1/memos")
    suspend fun getMemos(): Response<MemosResponse>

    @POST("api/v1/save")
    suspend fun saveMemo(@Body request: SaveMemoRequest): Response<SaveResponse>

    // JSON body — matches Flask api_search_memos which reads request.get_json()
    @POST("api/v1/search")
    suspend fun searchMemos(@Body request: SearchRequest): Response<MemosResponse>

    @POST("nl_query")
    @FormUrlEncoded
    suspend fun nlQuery(@Field("nl_question") question: String): Response<ResponseBody>

    @DELETE("api/v1/delete/{id}")
    suspend fun deleteMemo(@Path("id") id: String): Response<ResponseBody>
}
