package com.appload.einkapp.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.appload.einkapp.R
import com.appload.einkapp.databinding.ActivityLoginBinding
import com.appload.einkapp.model.ErrorResponse
import com.appload.einkapp.model.request.LoginRequest
import com.appload.einkapp.repository.ApiRepository
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.base.BaseActivity
import com.appload.einkapp.view.common.ErrorDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.options
import com.onesignal.OneSignal


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val viewModelApi: ApiViewModel by viewModels {
        ApiViewModel.Factory(
            ApiRepository()
        )
    }

    val startSignInForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        //if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
        //}
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            Log.d("LOGGED ACCOUNT", account.toString())
            if (account.email != null) {
                //val loginRequest = LoginRequest(email = account.email!!, pushId = null)
                val pushId = OneSignal.getDeviceState()?.userId
                val loginRequest = LoginRequest(
                    email = account.email!!,
                    pushId = UserPreferences.getPushId(this),
                    nome = account.givenName,
                    cognome = account.familyName ?: ""
                )

                if(account.photoUrl != null) {
                    UserPreferences.setUserPhoto(this,account.photoUrl!!.toString())
                }

                login(loginRequest)
            } else if (!isFinishing)
                Toast.makeText(this, "Errore durante l'accesso", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            val firebaseOptions = Firebase.options.toString()
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            if (!isFinishing)
                Toast.makeText(this, "Errore durante l'accesso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(loginRequest: LoginRequest) {
        //Faccio la chiamata login, se email uniurb -> Mi ritornano tutte le informazioni
        //se email gmail -> Mi generano un id per l'account
        viewModelApi.login(loginRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("244395318958-uins34dklasc72cfsab356987f33k25o.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModelApi.error.observe(this) {
            if (it.error.isNullOrBlank().not())
                ErrorDialog.showError(this, it)
            else {
                val error = ErrorResponse(
                    it.errorCode,
                    "Impossibile effettuare l'accesso, riprova con un altro account."
                )
                ErrorDialog.showError(this, error)
            }
        }
        viewModelApi.user.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        UserPreferences.setUser(this, it.container.data)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }

                is NetworkResult.Failure -> {
                    Utils.createDialog(
                        this,
                        resources.getString(R.string.attenzione),
                        it.exception.error
                    )
                }

                is NetworkResult.Loading -> {}
            }
        }
    }


    override fun prepareStage() {}

    override fun setListeners() {
        binding.accedi.setOnClickListener {
            signInWithGoogle()
        }

        binding.accediOspite.setOnClickListener {
            //TODO INSERIRE INTENT
            //startActivity()
            UserPreferences.setGuest(this, true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startSignInForResult.launch(signInIntent)
    }

    override fun getActivityView(): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        const val SIGN_IN_RC = 100
    }
}