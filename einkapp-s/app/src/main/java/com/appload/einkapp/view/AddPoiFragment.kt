package com.appload.einkapp.view

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.appload.einkapp.R
import com.appload.einkapp.databinding.FragmentAddPoiBinding
import com.appload.einkapp.model.request.LuogoRequest
import com.appload.einkapp.model.response.LuoghiResponse
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.base.CheckLoginFragment
import com.appload.einkapp.view.common.LoadingDialog
import com.bumptech.glide.Glide


class AddPoiFragment : CheckLoginFragment() {

    private lateinit var binding: FragmentAddPoiBinding
    private var edificioSelezionato: LuoghiResponse.LuoghiResponseItem? = null
    private var aulaSelezionata: LuoghiResponse.LuoghiResponseItem.Luogo? = null


    override fun loggedUI(user: UserResponse?) {
        binding.profile.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.profile
            )
        )

    }

    override fun prepareStage() {
        super.prepareStage()
        setObservers()
        Glide.with(requireContext())
            .load(UserPreferences.getUserPhoto(requireContext()))
            .placeholder(R.drawable.profile)
            .into(binding.profile)
    }

    private fun setObservers() {
        val loadingDialog = LoadingDialog(requireContext())
        viewModelApi.addLuogo.observe(this) {
            it.getContentIfNotHandled()?.let { res ->
                loadingDialog.show()
                when (res) {
                    is NetworkResult.Success -> {
                        if (res.container.status.equals("success", true)) {
                            val builder = AlertDialog.Builder(requireContext())
                            if(!res.container.data.isNullOrBlank()){
                                builder.setMessage(res.container.data?.replace("\"", ""))
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                        setNoEnableBotton()
                                    }
                                    .show()
                            }else{
                                builder.setMessage("Aula aggiunta ai tuoi preferiti")
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                        setNoEnableBotton()
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
                            res.exception.error
                        )
                    }

                    is NetworkResult.Loading -> {}
                }
                loadingDialog.dismiss()
            }
        }
    }

    override fun guestUI() {
        binding.profile.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.settings
            )
        )
    }

    override fun gmailUI(user: UserResponse?) {

    }

    override fun setListeners() {
        getAllLuoghi()
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.profile.setOnClickListener {
            val action = AddPoiFragmentDirections.actionAddPoiFragmentToProfileNavigation()
            findNavController().navigate(action)
        }
        binding.buttonAddPoi.setOnClickListener {
            if (aulaSelezionata != null) {
                val list = UserPreferences.getFavourites(requireContext())
                Log.e("aulaSelezionata", aulaSelezionata!!.idStanza.toString())
                if (!list.isNullOrEmpty()) {
                    if (list.map {
                            Utils.isSameBeacon(
                                it.uuid,
                                it.majorNumber.toString(),
                                it.minorNumber.toString(),
                                aulaSelezionata!!.uuid,
                                aulaSelezionata!!.majorNumber.toString(),
                                aulaSelezionata!!.minorNumber.toString()
                            )
                        }.first()) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("Aula già presente nell'elenco")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    } else {
                        addPoi(
                            aulaSelezionata!!.uuid,
                            aulaSelezionata!!.majorNumber,
                            aulaSelezionata!!.minorNumber,
                        )
                    }
                } else {
                    addPoi(
                        aulaSelezionata!!.uuid,
                        aulaSelezionata!!.majorNumber,
                        aulaSelezionata!!.minorNumber
                    )
                }
            }
        }
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentAddPoiBinding.inflate(inflater)
        return binding.root
    }

    private fun addPoi(uuid: String, majorNumber: Int, minorNumber: Int) {
        if (context != null) {
            val idUtente = UserPreferences.getUser(requireContext())!!.id
            viewModelApi.addLuogo(idUtente, LuogoRequest(uuid = uuid,major = majorNumber,minor = minorNumber) )
        }
    }

    private fun getAllLuoghi() {
//        val idUtente = UserPreferences.getUser(requireContext())!!.id
        viewModelApi.getAllLuoghi()
        viewModelApi.allLuoghiResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    it.container.data?.let { data -> setDialogs(data) }
                }

                is NetworkResult.Failure -> {
                    if (it.exception.errorCode == "404") {
                        Log.e("getAllLuoghi", it.exception.errorCode + " " + it.exception.error)
                    } else {
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            it.exception.error
                        )
                    }
                }

                is NetworkResult.Loading -> {

                }
            }
        }
    }

    private fun setDialogs(data: LuoghiResponse) {
        binding.edificioChoice.setOnClickListener {
            val edifici = arrayListOf<CharSequence>()
            edificioSelezionato = null
            aulaSelezionata = null
            setNoEnableBotton()
            data.forEach() { edifici.add(it.nomeEdificio as CharSequence) }
            dialogEdificio(edifici, data)
        }
    }

    private fun setNoEnableBotton() {
        binding.buttonAddPoi.isEnabled = false
        binding.nomeEdificio.text = requireContext().getString(R.string.nome_edificio)
        binding.nomeAula.text = requireContext().getString(R.string.nome_aula)
        binding.aulaChoice.setOnClickListener {}
    }

    private fun dialogEdificio(edifici: ArrayList<CharSequence>, data: LuoghiResponse) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setItems(edifici.toTypedArray()) { dialog, item ->
            edificioSelezionato = data[item]
            binding.nomeEdificio.text = edificioSelezionato!!.nomeEdificio
            if (edificioSelezionato != null) {
                binding.aulaChoice.setOnClickListener {
                    val aule = arrayListOf<CharSequence>()
                    edificioSelezionato!!.luoghi.forEach {
                        aule.add(it.nomeStanza)
                    }
                    dialogNomeAula(aule)
                }
            }
        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun dialogNomeAula(aule: ArrayList<CharSequence>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setItems(
            aule.toTypedArray()
        ) { dialog, item ->
            aulaSelezionata = edificioSelezionato!!.luoghi[item]
            binding.nomeAula.text = aulaSelezionata!!.nomeStanza
            checkAddBotton()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkAddBotton() {
        if (edificioSelezionato != null && aulaSelezionata != null) {
            binding.buttonAddPoi.isEnabled = true
        }
    }

}