package com.fefuproject.timemanager.auth.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fefuproject.timemanager.MainActivity
import com.fefuproject.timemanager.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistrationFragment : BaseFragment() {

    companion object {
        private const val TAG = "AUTH_TAG"
    }

//    njfvf
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBarReg)
        with(binding) {
            regButton.setOnClickListener {
                val email = binding.fieldRegEmail.text.toString()
                val password = binding.fieldRegPassword.text.toString()
                createAccount(email, password)
            }
        }
        auth = Firebase.auth
    }


    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) return

        showProgressBar()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
                hideProgressBar()
            }
    }

    //отправить подтверждение регистрации на почту
    private fun sendEmailVerification() {
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Сообщение отправлено на почту ${user.email} ",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(
                        context,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                (requireActivity() as MainActivity).onBackPressed()
            }
    }

    //обновить данные
    /*private fun reload() {
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(auth.currentUser)
                Toast.makeText(context, "Reload successful!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "reload", task.exception)
                Toast.makeText(context, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    //проверка ввода
    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.fieldRegEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldRegEmail.error = "Required."
            valid = false
        } else {
            binding.fieldRegEmail.error = null
        }
        val password = binding.fieldRegPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldRegPassword.error = "Required."
            valid = false
        } else {
            binding.fieldRegPassword.error = null
        }

        return valid
    }


    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            if (!user.isEmailVerified) {
                sendEmailVerification()
                Toast.makeText(context, "Не забудьте подтвердить аккаунт", Toast.LENGTH_LONG).show()
            }
            //todo войти в приложение
            mainAuth = auth
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
