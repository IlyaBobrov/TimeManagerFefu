package com.fefuproject.timemanager.ui.views.auth.presenter

import android.content.Context
import android.util.Log
import com.fefuproject.timemanager.ui.base.presenter.BasePresenter
import com.fefuproject.timemanager.ui.views.auth.view.AuthView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AuthPresenter<V : AuthView> :
    BasePresenter<V>(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job
    private lateinit var jobAuthGetGSO: Job

    private val handler = CoroutineExceptionHandler { _, throwable ->
        getView()?.showMessage("Ошибка подключения к серверу")
        throwable.printStackTrace()
    }

    fun getGoogleSignInOptions(context: Context) {
        Log.d("TAG", "getGoogleSignInOptions")
        jobAuthGetGSO = launch(handler) {
            getView()?.showLoader()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            getView()?.setGoogleSignInOptions(GoogleSignIn.getClient(context, gso))
        }
    }

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("TAG", "handleSignInResult")
            getView()?.updateUI(account)
        } catch (e: ApiException) {
            Log.w("TAG", "signInResult:failed code = " + e.statusCode)
            getView()?.updateUI(null)
        }
    }

    fun isAuthJobFinished(): Boolean =
        this::jobAuthGetGSO.isInitialized && jobAuthGetGSO.isCompleted

}
