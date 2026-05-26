package com.renium.sipkasku

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.room.Room

import com.renium.sipkasku.data.local.AppDatabase
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.NavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        ).build()

        val repository = TransactionRepository(
            db.transactionDao()
        )

        setContent {

            MaterialTheme {

                Surface {
                    NavGraph(repository)
                }
            }
        }
    }
}
