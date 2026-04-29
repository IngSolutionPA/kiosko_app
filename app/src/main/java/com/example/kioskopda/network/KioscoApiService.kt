package com.example.kioskopda.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface KioscoApiService {
    @FormUrlEncoded
    @POST("kiosco/login/")
    suspend fun login(
        @Field("pin") pin: String,
        @Field("imei") imei: String
    ): Response<LoginResponse>

    @POST("kiosco/validacion/")
    suspend fun postValidacion(
        @Body request: ValidacionRequest
    ): Response<ValidacionResponse>

    @POST("kiosco/notificacion/")
    suspend fun postNotificacion(
        @Body request: NotificacionRequest
    ): Response<NotificacionResponse>

    @GET("kiosco/notificacion_kiosco/")
    suspend fun getNotificaciones(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<NotificacionesResponse>
}

object RetrofitClient {

    private const val BASE_URL = "https://api.ima.gob.pa:30443/"

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private fun buildOkHttpClient(): OkHttpClient {
        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: KioscoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KioscoApiService::class.java)
    }
}

