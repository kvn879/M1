package com.example.cpen321application

import retrofit2.Call
import retrofit2.http.GET


interface ApiService {
    @GET("/api/server-ip")
    fun getServerIp(): Call<ServerIpResponse>

    @GET("/api/server-time")
    fun getServerTime(): Call<ServerTimeResponse>

    @GET("/api/name")
    fun getName(): Call<NameResponse>

}

data class ServerIpResponse(val ip: String, val clientIp: String)
data class ServerTimeResponse(val time: String)
data class NameResponse(val firstName: String, val lastName: String)