package com.example.lutautosync.data

import android.content.Context
import androidx.room.Room

object AppContainer {
    lateinit var db: AppDatabase
    fun init(context: Context) { db = Room.databaseBuilder(context, AppDatabase::class.java, "lut-autosync.db").build() }
}
