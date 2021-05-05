package com.fefuproject.timemanager.ui.views.auth.view

import com.fefuproject.timemanager.ui.base.view.IView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient

interface AuthView : IView {

    fun updateUI(account: GoogleSignInAccount?)

    fun showMessage(msg: String?)

    fun setGoogleSignInOptions(client: GoogleSignInClient)

    fun showLoader()

    fun hideLoader()
}