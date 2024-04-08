package com.github.krystianmuchla.mnemo.http

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class ServiceFactory {
    companion object {
        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit {
            return INSTANCE ?: synchronized(this) {
                val instance = Retrofit.Builder()
                    .baseUrl("http://192.168.0.69:80")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
