package com.fefuproject.timemanager.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.fefuproject.timemanager.ui.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.ui.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.ui.auth.fragments.multifactorauth.MultiFactorFragment
import com.fefuproject.timemanager.ui.auth.fragments.multifactorauth.MultiFactorSignInFragment
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentEmailPasswordBinding
import com.fefuproject.timemanager.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class EmailPasswordFragment : BaseFragment() {

    companion object {
        private const val TAG = "AUTH_TAG"
        private const val RC_SIGN_IN = 100
    }

    private var _binding: FragmentEmailPasswordBinding? = null
    private val binding: FragmentEmailPasswordBinding
        get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private var offline: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProgressBar(binding.progressBar)
        onClick()
        sharedPreferences.edit().putBoolean(APP_PREF_OFFLINE, false).apply()
        Log.d(TAG, "onViewCreated: ${sharedPreferences.getBoolean(APP_PREF_OFFLINE, false)}")
        //для авториззации через гугл
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun onClick() {
        with(binding) {
            emailSignInButton.setOnClickListener {
                val email = binding.fieldEmail.text.toString()
                val password = binding.fieldPassword.text.toString()
                signIn(email, password)
            }
            emailCreateAccountButton.setOnClickListener {
                findNavController().navigate(R.id.RegistrationFragment)
            }
            //обработчик кнопки войти с Google
            signInWithGoogleButton.setOnClickListener {
                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
            signInOffline.setOnClickListener {
                showProgressBar()
                offline = true
                updateUI(null)
            }
        }
    }

    //принимаем результат входа с Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(context, "Error: $e", Toast.LENGTH_LONG).show()
            }
        }
    }

    //какая то магия обрабатвающая токен гугла
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mainAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mainAuth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    /*private fun revokeAccess() {
        // Firebase sign out
        mainAuth.signOut()
        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }*/

    /*private fun signOut() {
        // Firebase sign out
        auth.signOut()
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }*/

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        if (mainAuth.currentUser != null) {
            startHome()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    private fun startHome() {
        Log.d(TAG, "startHome: ")
        findNavController().setGraph(R.navigation.nav_graph_main)
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) return
        showProgressBar()

        mainAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(mainAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                    checkForMultiFactorFailure(task.exception!!)
                }

                if (!task.isSuccessful) {
                    binding.status.setText(R.string.auth_failed)
                }
                hideProgressBar()
            }
    }


    /*private fun signOut() {
        auth.signOut()
        updateUI(null)
    }*/

    //отправить подтверждение регистрации на почту
    /*private fun sendEmailVerification() {
        // Disable button
        binding.verifyEmailButton.isEnabled = false

        // Send verification email
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(requireActivity()) { task ->
                // Re-enable button
                binding.verifyEmailButton.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Verification email sent to ${user.email} ",
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
            }
    }*/

    //обновить данные
    /*private fun reload() {
        mainAuth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(mainAuth.currentUser)
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
        val email = binding.fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldEmail.error = getString(R.string.required)
            valid = false
        } else {
            binding.fieldEmail.error = null
        }
        val password = binding.fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldPassword.error = getString(R.string.required)
            valid = false
        } else {
            binding.fieldPassword.error = null
        }
        return valid
    }


    private fun updateUI(user: FirebaseUser?) {
        Log.d(TAG, "updateUI: ")
        if (offline) {
            sharedPreferences.edit().putBoolean(APP_PREF_OFFLINE, true).apply()
            startHome()
            return
        }
        if (user != null) {
            binding.status.text = getString(
                R.string.emailpassword_status_fmt,
                user.email, user.isEmailVerified
            )
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)
            if (!user.isEmailVerified) {
                Toast.makeText(context, "Не забудьте подтвердить аккаунт", Toast.LENGTH_SHORT)
                    .show()
            }
            startHome()
        } else {
            binding.status.setText(R.string.signed_out)
            binding.detail.text = null
            Toast.makeText(context, "Ошибка авторизации!", Toast.LENGTH_SHORT).show()
        }
        hideProgressBar()
    }

    //непонятно зачем (из документации файры)
    private fun checkForMultiFactorFailure(e: Exception) {
        // Multi-factor authentication with SMS is currently only available for
        // Google Cloud Identity Platform projects. For more information:
        // https://cloud.google.com/identity-platform/docs/android/mfa
        if (e is FirebaseAuthMultiFactorException) {
            Log.w(TAG, "multiFactorFailure", e)
            val resolver = e.resolver
            val args = bundleOf(
                MultiFactorSignInFragment.EXTRA_MFA_RESOLVER to resolver,
                MultiFactorFragment.RESULT_NEEDS_MFA_SIGN_IN to true
            )
            findNavController().navigate(R.id.action_emailpassword_to_mfa, args)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}