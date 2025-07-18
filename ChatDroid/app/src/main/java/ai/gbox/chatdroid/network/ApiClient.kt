package ai.gbox.chatdroid.network

import ai.gbox.chatdroid.datastore.AuthPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ai.gbox.chatdroid.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    // Base URL is provided via BuildConfig (see build.gradle.kts)
    private val BASE_URL: String = BuildConfig.BASE_URL

    // Inject bearer token if available
    private val authInterceptor = Interceptor { chain ->
        val token = AuthPreferences.currentToken()
        val original = chain.request()
        if (!token.isNullOrBlank()) {
            val request = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        } else {
            chain.proceed(original)
        }
    }

    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor().apply {
        (this as HttpLoggingInterceptor).setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .client(client)
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
} 