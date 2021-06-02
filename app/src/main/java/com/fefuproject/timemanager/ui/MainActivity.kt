package com.fefuproject.timemanager.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.components.Constants.APP_PREF_SETTINGS
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mainAuth: FirebaseAuth
        lateinit var sharedPreferences: SharedPreferences
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainAuth = Firebase.auth

        sharedPreferences = getSharedPreferences(APP_PREF_SETTINGS, MODE_PRIVATE)
        Log.d("TAG", "!!! ${sharedPreferences.getBoolean(APP_PREF_OFFLINE, false)} ")
        onClick()
//        connectDB()
    }

    private lateinit var database: AppDatabase

    fun getDB(): AppDatabase = database

    /*private fun connectDB() {
        database = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME)
            .build()
    }*/

    private fun onClick() {
        if (mainAuth.currentUser == null) {
            findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_auth)
        } else {
            findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_main)
            mainAuth.currentUser!!.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "onListener: task = is success")
                    Toast.makeText(this, "email: ${mainAuth.currentUser?.email}", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Log.e("TAG", "onListener: task = is not success")
//                (requireActivity() as MainActivity).setNullAuth()
                    findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_auth)
                    Log.e("TAG", "reload", task.exception)
                    Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getOut() {
        mainAuth.signOut()
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_auth)
    }

}