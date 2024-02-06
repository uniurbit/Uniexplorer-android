package com.appload.einkapp.view.profile

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.appload.einkapp.R
import com.appload.einkapp.databinding.FragmentProfileBinding
import com.appload.einkapp.model.request.LogoutRequest
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.LoginActivity
import com.appload.einkapp.view.base.BaseFragment
import com.appload.einkapp.view.common.LoadingDialog
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>


    override fun setListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.impostazioni.setOnClickListener {
            val action= ProfileFragmentDirections.actionProfiloFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun setExitButtonListener() {
        binding.buttonExitLogin.setOnClickListener {
            val idUtente = UserPreferences.getUser(requireContext())?.id.toString()
            val pushId = UserPreferences.getPushId(requireContext())
            if (!idUtente.isNullOrBlank() && !pushId.isNullOrBlank()) {
                val loading = LoadingDialog(requireContext())
                viewModelApi.logout(LogoutRequest(idUtente, pushId))
                loading.show()
                viewModelApi.userLogout.observe(this) {
                    when (it) {
                        is NetworkResult.Success -> {
                            if (it.container != null) {
                                UserPreferences.logout(requireContext())
                                signOutFromGoogle()
                                requireActivity().finish()
                                startActivity(Intent(requireContext(), LoginActivity::class.java))
                            }
                        }
                        is NetworkResult.Failure -> {
                            Utils.createDialog(
                                requireContext(),
                                resources.getString(R.string.attenzione),
                                it.exception.error
                            )
                        }
                        is NetworkResult.Loading -> {
                        }
                    }
                    loading.dismiss()
                }
            }
    //            UserPreferences.logout(requireContext())
    //            signOutFromGoogle()
    //            startActivity(Intent(requireContext(), MainActivity::class.java))
    //            } else
    //            {
    //                resultLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
    //            }
        }
    }

    private fun signOutFromGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1000542308482-lh6j1o4de9heu3it4uvraudt4f0uh2k9.apps.googleusercontent.com").build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
    }

    override fun prepareStage() {
        setResultLauncher()
        setLayout()
    }

    private fun setImageProfile(){
        Glide.with(requireContext())
            .load(UserPreferences.getUserPhoto(requireContext()))
            .placeholder(R.drawable.profile)
            .into(binding.photo)
    }

    private fun setResultLauncher() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data

                    //data has extra
                }
            }
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun setLayout() {
        if (UserPreferences.getUser(requireContext()) != null) {
            binding.apply {
                layoutAutUser.visibility = View.VISIBLE
                buttonExitLogin.text = resources.getString(R.string.esci)
                buttonExitLogin.contentDescription = resources.getString(R.string.esci)
                setExitButtonListener()
                photo.setImageResource(R.drawable.profile)
                isLogged()
            }
            setImageProfile()
        } else {
            binding.apply {
                layoutAutUser.visibility = View.GONE
                buttonExitLogin.text = resources.getString(R.string.accedi)
                buttonExitLogin.contentDescription = resources.getString(R.string.accedi)
                setAccediButtonListener()
                photo.setImageResource(R.drawable.no_photo_profile)
            }
        }
    }

    private fun setAccediButtonListener() {
        binding.buttonExitLogin.setOnClickListener {
            UserPreferences.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }


    private fun isLogged() {
        val userInfo = UserPreferences.getUser(requireContext())
        binding.name.text = userInfo?.nome
        binding.surname.text = userInfo?.cognome
        binding.email.text = userInfo?.email
        binding.corso.text = userInfo?.corsoStudi
    }
}