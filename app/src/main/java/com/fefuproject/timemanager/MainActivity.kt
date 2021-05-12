package com.fefuproject.timemanager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mainAuth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainAuth = Firebase.auth
        onListeners()
    }

    fun onListeners() {
        if (mainAuth.currentUser == null) {
            findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_auth)
        } else {
            findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_main)
            mainAuth.currentUser!!.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "onListener: task = is success")
                    Toast.makeText(this, "Reload successful!", Toast.LENGTH_SHORT).show()
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

    fun setAuth(auth: FirebaseAuth) {
        mainAuth = auth
    }

    fun getAuth(): FirebaseAuth = mainAuth as FirebaseAuth

    fun setNullAuth() {
        mainAuth = null as FirebaseAuth
    }
}