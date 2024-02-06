package com.appload.einkapp.view.base

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appload.einkapp.utils.Utils


abstract class BaseActivity : AppCompatActivity() {
    private var timer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getActivityView())

        prepareStage()

        setListeners()
    }

    abstract fun prepareStage()

    abstract fun setListeners()

    abstract fun getActivityView(): View

//    override fun onResume() {
//        super.onResume()
//        initTimer()
//    }

//    override fun onPause() {
//        super.onPause()
//        stopTimer()
//    }
//
//    private fun stopTimer() {
//        Log.e("TIMER", "OFF")
//        timer?.cancel()
//    }

//    private fun initTimer() {
//        val timeToCountDown = getCountDownTime()
//        if (timeToCountDown != -1L) {
//            Log.e("TIME TO UPDATE TOKEN IS", "${timeToCountDown / 1000} sec")
//        }
//        if (timeToCountDown == -1L) {
//            tokenExpired()
//        } else {
//            timer = object : CountDownTimer(timeToCountDown, SECOND) {
//                override fun onTick(millisUntilFinished: Long) {
//                    if (!USER_IS_LOGGED) {
//                        val user = MyPreferences.getUser(this@BaseActivity)
//                        if (user != null) {
//                            USER_IS_LOGGED = true
//                            initTimer()
//                            timer?.cancel()
//                        }
//                    }
//                }
//
//                override fun onFinish() {
//                    val loginRequest = MyPreferences.getLoginData(this@BaseActivity)
//                    if (loginRequest != null) {
//                        Log.e("REFRESHING TOKEN", "STARTED")
//                        AuthRepository.login(
//                            loginRequest
//                        ) {
//                            when (it) {
//                                is NetworkResult.Success -> {
//                                    saveToken(it.data)
//                                    initTimer()
//                                    Log.e("REFRESHING TOKEN", "FINISHED")
//                                }
//                                is NetworkResult.Failure -> {
//                                    Log.e(
//                                        "REFRESHING TOKEN",
//                                        "FAILED, caused by: \n${it.exception.errorCode}"
//                                    )
//                                }
//                            }
//                        }
//                    } else {
//                        initTimer()
//                    }
//                }
//            }
//            timer?.start()
//            Log.e("TIMER", "ON")
//        }
//    }
//
//    private fun saveToken(data: UserLoginResponse) {
//        MyPreferences.saveUser(this, data)
//    }
//
//    private fun tokenExpired() {
//        val request = MyPreferences.getLoginData(this)
//        if (request != null) {
//            AuthRepository.login(request) {
//                when (it) {
//                    is NetworkResult.Success -> {
//                        MyPreferences.saveUser(this, it.data)
//                        initTimer()
//                    }
//                    is NetworkResult.Failure -> {
//                        Toast.makeText(
//                            this,
//                            "Non è stato possibile accedere, riprova",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "Non è stato possibile accedere, riprova", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//
//    private fun getCountDownTime(): Long {
//        val user = MyPreferences.getUser(this)
//        return if (user != null) {
//            USER_IS_LOGGED = true
//            val now = Date().time
//            val tokenExpirationDate = Utils.formatDateFromServer(user.validTo)
//            if (tokenExpirationDate != null) {
//                if (tokenExpirationDate.time > now) {
//                    (tokenExpirationDate.time - now) - TOKEN_EXPIRATION_THRESHOLD
//                } else {
//                    -1
//                }
//            } else {
//                -1
//            }
//        } else {
//            USER_IS_LOGGED = false
//            1000L * 60 * 60
//        }
//    }


    fun showKeyboard() =
        Utils.showKeyboard(this)

    fun closeKeyboard() =
        Utils.closeKeyboard(this)

    companion object {
        private const val SECOND = 1000L
        private const val TOKEN_EXPIRATION_THRESHOLD = (1000 * 60).toLong()
        var USER_IS_LOGGED = false
    }
}