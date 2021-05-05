package com.fefuproject.timemanager.ui.views.auth.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.ui.views.auth.presenter.AuthPresenter
import com.fefuproject.timemanager.ui.views.main.MainActivity.Companion.RC_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity(), AuthView {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var presenter: AuthPresenter<AuthView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        presenter = AuthPresenter()
        presenter.onAttach(this)

        onClickListeners()

        presenter.getGoogleSignInOptions(this) //setGoogleSignInOptions
    }

    override fun setGoogleSignInOptions(client: GoogleSignInClient) {
        mGoogleSignInClient = client
        hideLoader()
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        Log.d("TAG", "onStart: account: $account")
        Toast.makeText(this, "$account", Toast.LENGTH_LONG).show()
        updateUI(account)
    }

    private fun onClickListeners() {
        auth_sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    @SuppressLint("SetTextI18n")
    override fun updateUI(account: GoogleSignInAccount?) {
        Log.d("TAG", "updateUI")

        //test
        auth_tv_account_info.text = "Account info:\n " +
                "Id: ${account?.id.toString()}\n" +
                "Email: ${account?.email.toString()}\n" +
                "Token: ${account?.idToken.toString()}\n" +
                "Name: ${account?.displayName.toString()}\n"
        "ServerAuthCode: ${account?.serverAuthCode.toString()}\n"
        "Account: ${account?.account.toString()}\n"

        if (account != null) {
            openApp(account)
        } else {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_LONG).show()
        }
    }

    private fun openApp(account: GoogleSignInAccount?) {
        //todo войти в приложение
    }

    override fun showMessage(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun showLoader() {
        auth_loader.visibility = View.VISIBLE

    }

    override fun hideLoader() {
        auth_loader.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            presenter.handleSignInResult(task) //updateUI
            Toast.makeText(this, "Привет!", Toast.LENGTH_LONG).show()
        }
    }


}