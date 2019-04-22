package com.universe.rodrf.mapas8

import android.app.Application
import com.google.android.libraries.places.api.Places

//Para la inicializacion de SDKs
class KotlinMapsAplicaciont: Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(this@KotlinMapsAplicaciont,"AIzaSyDGzP2IMHhDdXU-xjj_U_QxnFLRWrhTL7g")
    }
}