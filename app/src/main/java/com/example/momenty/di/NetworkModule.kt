package com.example.momenty.di

import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.moment.MomentsApi
import com.example.momenty.data.remote.profile.ProfileApi
import com.example.momenty.global.mock.MockApiInterceptor
import com.example.momenty.global.security.AuthInterceptor
import com.example.momenty.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.momenty.com/"

    private val useMockApi: Boolean
        get() = true

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideMockApiInterceptor(): MockApiInterceptor =
        MockApiInterceptor().apply {
            MockApiInterceptor.isMockEnabled = useMockApi
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        mockApiInterceptor: MockApiInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .apply { if (useMockApi) addInterceptor(mockApiInterceptor) }
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    fun getRetrofit(): Retrofit {
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit
    }

    @Provides
    @Singleton
    fun provideMomentsApi(retrofit: Retrofit): MomentsApi =
        retrofit.create(MomentsApi::class.java)

}
