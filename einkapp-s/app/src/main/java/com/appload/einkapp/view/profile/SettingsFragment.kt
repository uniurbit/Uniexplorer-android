package com.appload.einkapp.view.profile

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import com.appload.einkapp.R
import com.appload.einkapp.databinding.FragmentSettingsBinding
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.utils.UserModel
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.UserPreferences.CALENDARIO
import com.appload.einkapp.utils.UserPreferences.PREFERITI
import com.appload.einkapp.utils.UserPreferences.TUTTI
import com.appload.einkapp.utils.UserPreferences.setForegroundEnabled
import com.appload.einkapp.view.MainActivity
import com.appload.einkapp.view.base.BaseFragment
import com.appload.einkapp.view.base.CheckLoginFragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class SettingsFragment : CheckLoginFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var setNotifications = mutableSetOf<String>()

    override fun setListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
//        binding.switchNotifiche.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                binding.itemsSwitch.visibility = View.VISIBLE
//                Log.e("switch", setNotifications.toString())
//            } else {
//                binding.itemsSwitch.visibility = View.GONE
//                binding.apply {
//                    calendario.isChecked=false
//                    preferiti.isChecked=false
//                    tutti.isChecked=false
//                    setNotifications.clear()
//                    UserPreferences.cleanNotifications(requireContext())
//                }
//                Log.e("switch", UserPreferences.getNotifications(requireContext()).toString())
//            }
//        })
        getTypeNotifications()
        binding.switchBackground.setOnCheckedChangeListener { _, boolean ->
            Log.e("botton",UserPreferences.isForegroundEnabled(requireContext()).toString())
            UserPreferences.setForegroundEnabled(requireContext(),boolean)
        }
        binding.checkBattery.setOnClickListener{
            Log.d(TAG, "${Build.BRAND} -- ${Build.MODEL} -- ${Build.MANUFACTURER}")
            try {
                val intent = Intent()
                intent.component = ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
                intent.putExtra("package_name", "com.appload.einkapp")
                intent.putExtra("package_label", getString(R.string.app_name))
                startActivity(intent)
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
    }

    private fun getTypeNotifications() {
        binding.apply {
            calendario.setOnClickListener {
                (it as CheckBox).putOnSet()
                uncheckTutti()
            }
            preferiti.setOnClickListener {
                (it as CheckBox).putOnSet()
                uncheckTutti()
            }
            tutti.setOnClickListener {
                (it as CheckBox).putOnSet()
                checkAllPrevious()
                hideAllPrevious(it.isChecked)
            }
        }
    }

    private fun uncheckTutti() {
        binding.tutti.isChecked = false
    }

    private fun hideAllPrevious(checked: Boolean) {
        binding.calendario.isEnabled = !checked
        binding.preferiti.isEnabled = !checked
        binding.calendario.alpha = if (!checked) 1f else .5f
        binding.preferiti.alpha = if (!checked) 1f else .5f
    }

    private fun checkAllPrevious() {
        binding.calendario.isChecked = true
        setNotifications.add(binding.calendario.tag.toString().uppercase())
        binding.preferiti.isChecked = true
        setNotifications.add(binding.preferiti.tag.toString().uppercase())
        UserPreferences.setNotifications(requireContext(), setNotifications)
    }

    private fun CheckBox.putOnSet() {
        if (this.isChecked) {
            setNotifications.add(this.tag.toString().uppercase())
        } else if (!this.isChecked && setNotifications.contains(this.tag.toString().uppercase())) {
            setNotifications.remove(this.tag.toString().uppercase())
        }
        UserPreferences.setNotifications(requireContext(), setNotifications)
        Log.e("check", UserPreferences.getNotifications(requireContext()).toString())
    }

    override fun prepareStage() {
        //checkSwitchesFromPreferences()
        //checkLogged()
        setForegroundEnabled()
        preCheckBoxes()
        checkLogged()
        checkDeviceBrand()
    }
    private fun setImageProfile(){
        Glide.with(requireContext())
            .load(UserPreferences.getUserPhoto(requireContext()))
            .placeholder(R.drawable.profile)
            .into(binding.photo)
    }

    private fun checkDeviceBrand() {
        if(Build.MANUFACTURER.equals("xiaomi",true).not())
            binding.batteryOptimization.visibility = View.GONE
    }

    private fun setForegroundEnabled() {
        if (context != null) {
            binding.switchBackground.isChecked = UserPreferences.isForegroundEnabled(requireContext())
        }
    }

    private fun preCheckBoxes() {
        UserPreferences.getNotifications(requireContext())?.forEach {
            if (it.equals(CALENDARIO, true)) {
                binding.calendario.isChecked = true
                setNotifications.add(binding.calendario.tag.toString().uppercase())
            }
            if (it.equals(PREFERITI, true)) {
                binding.preferiti.isChecked = true
                setNotifications.add(binding.preferiti.tag.toString().uppercase())
            }
            if (it.equals(TUTTI, true)) {
                binding.tutti.isChecked = true
                setNotifications.add(binding.tutti.tag.toString().uppercase())
            }
        }
        if (binding.tutti.isChecked)
            hideAllPrevious(binding.tutti.isChecked)
    }

    override fun loggedUI(user: UserResponse?) {
        binding.calendario.visibility = View.VISIBLE
        binding.preferiti.visibility = View.VISIBLE
        setImageProfile()
    }

    override fun guestUI() {
        binding.preferiti.visibility = View.GONE
        binding.calendario.visibility = View.GONE
        binding.apply {
           // buttonExitLogin.text = resources.getString(R.string.accedi)
            photo.setImageResource(R.drawable.no_photo_profile)
           /* buttonExitLogin.setOnClickListener {
                UserPreferences.logout(requireContext())
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }*/
        }
    }

    override fun gmailUI(user: UserResponse?) {
        binding.preferiti.visibility = View.VISIBLE
        binding.calendario.visibility = View.GONE
        setImageProfile()

    }

    private fun logout() {
        UserPreferences.logout(requireContext())
        signOutFromGoogle()
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    private fun signOutFromGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1000542308482-lh6j1o4de9heu3it4uvraudt4f0uh2k9.apps.googleusercontent.com")
            .build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
    }


    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}