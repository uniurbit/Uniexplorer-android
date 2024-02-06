package com.appload.einkapp.view.base

import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.utils.UserPreferences

abstract class CheckLoginFragment : BaseFragment(){

    fun checkLogged(){
        if(!UserPreferences.isGuest(requireContext()))
            if(UserPreferences.isUniurb(requireContext()))
                loggedUI(UserPreferences.getUser(requireContext()))
            else
                gmailUI(UserPreferences.getUser(requireContext()))
        else
            guestUI()
    }

    override fun prepareStage() {
        checkLogged()
    }

    abstract fun loggedUI(user: UserResponse?)

    abstract fun guestUI()

    abstract fun gmailUI(user: UserResponse?)
}