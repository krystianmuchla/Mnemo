package com.github.krystianmuchla.mnemo.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

abstract class ServiceFactory {
    companion object {
        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit {
            return INSTANCE ?: synchronized(this) {
                val instance = Retrofit.Builder()
                    .baseUrl("http://192.168.0.69:80")
                    .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
