package com.appload.einkapp.view

import android.app.AlertDialog
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.appload.einkapp.R
import com.appload.einkapp.databinding.FragmentAulaBinding
import com.appload.einkapp.model.request.DeleteLuogoRequest
import com.appload.einkapp.model.request.LuogoRequest
import com.appload.einkapp.model.response.BeaconsResponse
import com.appload.einkapp.model.response.FavouritesRespose
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.base.CheckLoginFragment
import com.appload.einkapp.view.common.ErrorDialog
import com.appload.einkapp.view.common.LoadingDialog
import com.bumptech.glide.Glide

class AulaFragment : CheckLoginFragment() {
    private lateinit var binding: FragmentAulaBinding
    private lateinit var uuid: String
    private lateinit var idStanza: String
    private lateinit var aula: Aula
    private var listFavourites: FavouritesRespose? = null
    private var user: UserResponse? = null
    private var isFav: Boolean = false
    private val navArgs: AulaFragmentArgs by navArgs()

    private data class Aula(
        val uuid: String,
        val major: Int,
        val minor: Int,
    )

    override fun setListeners() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.profile.setOnClickListener {
            val action = AulaFragmentDirections.actionAulaFragmentToProfileNavigation()
            findNavController().navigate(action)
        }

    }

    override fun onPause() {
        super.onPause()
        viewModelApi.luogoResponse.value = null
    }

    override fun prepareStage() {
        user = UserPreferences.getUser(requireContext())
        if (arguments != null) {
//            idStanza =navArgs
            aula = Aula(navArgs.uuid, navArgs.major.toInt(), navArgs.minor.toInt())
            viewModelApi.getLuogo(navArgs.uuid, navArgs.major, navArgs.minor)
        }
        setObservers()
        super.prepareStage()

    }

    private fun setFakeData() {
        binding.place.text = "Test"
        val text = (Html.fromHtml(
            "<h2>Title</h2><br><p>Description here</p>",
            Html.FROM_HTML_MODE_COMPACT
        ))
        binding.webview.text = text
        binding.webview.announceForAccessibility(text)
        binding.webview.requestFocus()
    }

    private fun setImageProfile() {
        Glide.with(requireContext())
            .load(UserPreferences.getUserPhoto(requireContext()))
            .placeholder(R.drawable.profile)
            .into(binding.profile)
    }

    override fun loggedUI(user: UserResponse?) {
        setImageProfile()
        binding.removePoi.visibility = View.VISIBLE
        addRemoveFavListeners()
        getFavourites()

    }

    override fun guestUI() {
        binding.removePoi.visibility = View.GONE
        binding.buttonAddPoi.visibility = View.GONE
        binding.profile.setImageResource(R.drawable.settings)
    }

    override fun gmailUI(user: UserResponse?) {
        setImageProfile()
        binding.removePoi.visibility = View.VISIBLE
        addRemoveFavListeners()
        getFavourites()
    }

    private fun addRemoveFavListeners() {
        binding.removePoi.setOnClickListener {
            if (user != null /*&& this::idStanza.isInitialized */ && ::aula.isInitialized) {
                val idUtente = user!!.id
                val deleteRequest = DeleteLuogoRequest(aula.uuid, aula.major, aula.minor)
                viewModelApi.deleteLuogo(idUtente, deleteRequest)
                UserPreferences.deleteFavourite(requireContext(), deleteRequest)
            }
        }
        binding.buttonAddPoi.setOnClickListener {
            if (context != null) {
                val idUtente = UserPreferences.getUser(requireContext())!!.id
                viewModelApi.addLuogo(
                    idUtente,
                    LuogoRequest(uuid = aula.uuid, major = aula.major, minor = aula.minor)
                )
            }
        }
    }

    private fun setObservers() {
        viewModelApi.error.observe(this) {
            if (it.error.isNullOrBlank().not())
                ErrorDialog.showError(requireContext(), it)
        }
        viewModelApi.luogoResponse.observe(this) {
            val loadingDialog = LoadingDialog(requireContext())
            loadingDialog.show()
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        setDetails(it.container.data)
                    }
                }

                is NetworkResult.Failure -> {
//                    Utils.createDialog(
//                        requireContext(),
//                        resources.getString(R.string.attenzione),
//                        getString(R.string.errore_caricamento)
//                    )
                    setFakeData()
                }

                is NetworkResult.Loading -> {}
            }
            loadingDialog.dismiss()

        }
        viewModelApi.favouritesResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        listFavourites = it.container.data
                        isFav = checkFav()
                        if (isFav) {
                            binding.removePoi.visibility = View.VISIBLE
                            binding.buttonAddPoi.visibility = View.GONE
                        } else {
                            binding.removePoi.visibility = View.GONE
                            binding.buttonAddPoi.visibility = View.VISIBLE
                        }
                        Log.e("isFav", isFav.toString())
                        binding.removePoi.visibility = if (isFav) View.VISIBLE else View.GONE
                    }
                }

                is NetworkResult.Failure -> {
                    if (it.exception.errorCode == "404") {
                        Log.e("getFav", it.exception.errorCode + " " + it.exception.error)
                        binding.removePoi.visibility = View.GONE
                        binding.removePoi.visibility = View.GONE
                        binding.buttonAddPoi.visibility = View.VISIBLE
                    } else {
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            getString(R.string.errore_caricamento)
                        )
                        binding.removePoi.visibility = View.GONE
                    }
                }

                is NetworkResult.Loading -> {}
            }
        }
        viewModelApi.deletedLuogoEvent.observe(this) {
            it.getContentIfNotHandled()?.let { res ->
                when (res) {
                    is NetworkResult.Success -> {
                        if (res.container.data != null) {
                        }
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            /*res.container.data*/
                            "Aula eliminata dai preferiti"
                        )
                        getFavourites()
                        enableRemovePoi()
                    }

                    is NetworkResult.Failure -> {
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            getString(R.string.errore_caricamento)
                        )
                        enableRemovePoi()
                    }

                    is NetworkResult.Loading -> {
                        disableRemovePoi()
                    }
                }
            }
        }
        viewModelApi.addLuogo.observe(this) {
            val loadingDialog = LoadingDialog(requireContext())
            it.getContentIfNotHandled()?.let { res ->
                loadingDialog.show()
                when (res) {
                    is NetworkResult.Success -> {
                        if (res.container.status.equals("success", true)) {
                            val builder = AlertDialog.Builder(requireContext())
                            enableAddPoi()
                            getFavourites()
                            if (!res.container.data.isNullOrBlank()) {
                                builder.setMessage(res.container.data?.replace("\"", ""))
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()

                            } else {
                                builder.setMessage("Aula aggiunta ai tuoi preferiti")
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }

                        } else {
                            Utils.createDialog(
                                requireContext(),
                                resources.getString(R.string.attenzione),
                                res.container.message
                            )
                        }
                    }

                    is NetworkResult.Failure -> {
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            getString(R.string.errore_caricamento)
                        )
                    }

                    is NetworkResult.Loading -> {
                        disableAddPoi()
                    }
                }
                loadingDialog.dismiss()
            }
        }

    }

    private fun disableRemovePoi() {
        binding.removePoi.isEnabled = false
        binding.removePoi.alpha = 0.5F
    }

    private fun disableAddPoi() {
        binding.buttonAddPoi.isEnabled = false
        binding.buttonAddPoi.alpha = 0.5F
    }

    private fun enableAddPoi() {
        binding.buttonAddPoi.isEnabled = true
        binding.buttonAddPoi.alpha = 1F
    }

    private fun enableRemovePoi() {
        binding.removePoi.isEnabled = true
        binding.removePoi.alpha = 1F
    }

    private fun checkFav(): Boolean {
        if (this::aula.isInitialized) {
            return listFavourites!!.find {
                it.uuid.equals(
                    aula.uuid,
                    true
                ) && it.majorNumber == aula.major && it.minorNumber == aula.minor
            } != null
        }
        return false
    }

    private fun getFavourites() {
        if (user != null) {
            viewModelApi.getFav(user!!.id)
        }
    }

    private fun setDetails(luogo: BeaconsResponse) {
        val text = (Html.fromHtml(
            luogo.testo,
            Html.FROM_HTML_MODE_COMPACT
        ))
        binding.place.text = luogo.nomeStanza
        binding.webview.text = text
        binding.webview.announceForAccessibility(text)
        binding.webview.requestFocus()
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentAulaBinding.inflate(inflater, container, false)
        return binding.root
    }


}