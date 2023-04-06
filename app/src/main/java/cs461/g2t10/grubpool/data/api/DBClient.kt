package cs461.g2t10.grubpool.data.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val S3_BASE_URL = "https://mobile-legend-thumbnails.s3.ap-southeast-1.amazonaws.com/"
const val BASE_API_ENDPOINT = "https://gepzvdvxai.execute-api.ap-southeast-1.amazonaws.com/api/"

object DbClient {
    fun getClient(): DbInterface {
        val requestInterceptor = Interceptor { chain ->

            val url: HttpUrl = chain.request()
                .url()
                .newBuilder()
                .build()

            val request: Request = chain.request()
                .newBuilder()
                .url(url)
                .build()
            return@Interceptor chain.proceed(request)
        }

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .connectTimeout(50, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_API_ENDPOINT)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DbInterface::class.java)
    }


}