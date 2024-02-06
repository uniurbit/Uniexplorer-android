package com.appload.einkapp.view.explore

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.appload.einkapp.R
import com.appload.einkapp.databinding.FragmentExploreHomeBinding
import com.appload.einkapp.databinding.ListItemBinding
import com.appload.einkapp.model.response.LuoghiResponse
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.RANGE_HIGH
import com.appload.einkapp.utils.RANGE_HIGHEST
import com.appload.einkapp.utils.RANGE_MID
import com.appload.einkapp.utils.SEC_TIC
import com.appload.einkapp.utils.TIME_TO_LIVE
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.MainViewModel
import com.appload.einkapp.view.base.CheckLoginFragment
import com.bumptech.glide.Glide
import org.altbeacon.beacon.Beacon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ExploreHome : CheckLoginFragment() {
    private var isSet: Boolean = false
    private val savedList: ArrayList<Beacon> = arrayListOf()
    private lateinit var binding: FragmentExploreHomeBinding
    private var listLuoghi = arrayListOf<LuoghiResponse.LuoghiResponseItem.Luogo>()
    private val vm: MainViewModel by activityViewModels { MainViewModel.Factory() }
    private var lastUpdate = 0L
    private var cachedBeacons =
        ArrayList<BeaconWithTimer>()//Beacon rilevati negli ultimi TIME_TO_LIVE millisecondi

    override fun setListeners() {
        binding.profile.setOnClickListener {
            val action = ExploreHomeDirections.actionExploreHomeToProfileNavigation()
            findNavController().navigate(action)
        }
        binding.buttonAddPoi.setOnClickListener {
            val action = ExploreHomeDirections.actionExploreHomeToAddPoiFragment()
            findNavController().navigate(action)
        }
    }

    override fun prepareStage() {
        super.prepareStage()
        observeBeacons()
        setObservers()
        getAllLuoghi()
        getCalendar()
        getFavorites()
        //checkLogged()
    }

    private fun getFavorites() {
        if (UserPreferences.getUser(requireContext())?.id != null)
            viewModelApi.getFav(UserPreferences.getUser(requireContext())?.id!!)

    }

    private fun getCalendar() {
        if (UserPreferences.getUser(requireContext())?.id != null)
            viewModelApi.getCalendar(UserPreferences.getUser(requireContext())?.id!!)
    }

    private fun getAllLuoghi() {
        viewModelApi.getAllLuoghi()
    }

    private fun setObservers() {
        viewModelApi.allLuoghiResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    listLuoghi.clear()
                    it.container.data?.forEach { list ->
                        list.luoghi.forEach {
                            Log.e("idStanza", it.idStanza.toString())
                        }
                        listLuoghi.addAll(list.luoghi)
                        listLuoghi.addAll(UserPreferences.testItems()) //todo: remove test item
                    }
                    UserPreferences.setAllLuoghi(requireContext(), listLuoghi)
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
        viewModelApi.calendarioResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        UserPreferences.setCalendario(requireContext(), it.container.data)
                    }
                }

                is NetworkResult.Failure -> {
                    UserPreferences.deleteCalendario(requireContext())
                    if (it.exception.errorCode == "404") {
                        Log.e("getCalendar", it.exception.errorCode + " " + it.exception.error)
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
        viewModelApi.favouritesResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        UserPreferences.setFavourites(requireContext(), it.container.data)
                    }
                }

                is NetworkResult.Failure -> {
                    if (it.exception.errorCode == "404") {
                        Log.e("getFav", it.exception.errorCode + " " + it.exception.error)
                    } else {
                        Utils.createDialog(
                            requireContext(),
                            resources.getString(R.string.attenzione),
                            it.exception.error
                        )
                    }
                }

                is NetworkResult.Loading -> {}
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun loggedUI(user: UserResponse?) {
        Glide.with(requireContext())
            .load(UserPreferences.getUserPhoto(requireContext()))
            .placeholder(R.drawable.profile)
            .into(binding.profile)
        binding.nome.text =
            (getString(R.string.ciao)) + " " + (user?.nome ?: user?.cognome ?: "Guest")
    }

    @SuppressLint("SetTextI18n")
    override fun guestUI() {
        binding.profile.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.settings
            )
        )
        binding.nome.text = getString(R.string.ciao) + " " + getString(R.string.guest)
        binding.buttonAddPoi.visibility = View.GONE
    }

    override fun gmailUI(user: UserResponse?) {
        loggedUI(user)
    }

    private fun observeBeacons() {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        vm.rangedBeacons.observe(this) {
            Log.d("Beacons", "$it ${formatter.format(Date())}")
            val now = System.currentTimeMillis()
            val list = it.filter { findNomeAula(it) != null && it.distance < 12 }
            savedList.clear()
            savedList.addAll(list)
            if (lastUpdate == 0L || !isSet) {
                lastUpdate = now
                setBeacons(savedList)
            } else if ((now - lastUpdate) > 1000 * SEC_TIC) {
                lastUpdate = now
                Log.d("Beacons", "$it ${formatter.format(Date())}")
                setBeacons(savedList)
            }
        }
        vm.monitoredBeacons.observe(this) {
            Log.d("Beacons State", it.toString())
        }
    }

    data class LuogoContainer(
        val luogo: LuoghiResponse.LuoghiResponseItem.Luogo,
        val avgDistance: Double,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LuogoContainer

            if (luogo != other.luogo) return false
            if (avgDistance != other.avgDistance) return false

            return true
        }

        override fun hashCode(): Int {
            var result = luogo.hashCode()
            result = 31 * result + avgDistance.hashCode()
            return result
        }

        override fun toString(): String {
            return luogo.toString()
        }
    }

    private fun setBeacons(beacons: Collection<Beacon>) {
        isSet = beacons.isNotEmpty()
        updateCachedBeacons(beacons)
        val inflater = LayoutInflater.from(requireContext())
        binding.container.removeAllViews()
        Log.e("Cached beacons", cachedBeacons.toString())
        val result = arrayListOf<LuogoContainer>()
        cachedBeacons.distinct().forEachIndexed { index, beacon ->
            if (listLuoghi.isNotEmpty()) {
                val luogo = listLuoghi.find {
                    Utils.isSameBeacon(
                        beacon.beacon,
                        it.uuid,
                        it.majorNumber.toString(),
                        it.minorNumber.toString()
                    )
                }

                if (luogo != null) {
                    result.add(LuogoContainer(luogo, beacon.avgDistances()))
                }
            }

        }

        if (result.isNotEmpty()) binding.progress.visibility = View.GONE
        else binding.progress.visibility = View.VISIBLE
        result.distinct().sortedBy { it.luogo.nomeStanza }.forEach { luogo ->
            val b = ListItemBinding.inflate(inflater, binding.container, true)
            val item = setSignal(b, luogo.avgDistance)
            item.separator.visibility = View.GONE
            item.name.text = "${luogo.luogo.nomeStanza}"
            item.root.contentDescription = luogo.luogo.nomeStanza
            item.root.setOnClickListener {
                val action = ExploreHomeDirections.actionExploreHomeToAulaFragment(
                    luogo.luogo.uuid,
                    luogo.luogo.majorNumber.toString(),
                    luogo.luogo.minorNumber.toString()
                )
                findNavController().navigate(action)
            }
        }
    }


    private fun setSignal(item: ListItemBinding, distance: Double): ListItemBinding {
        when (distance) {
            in RANGE_HIGHEST -> {
                item.signal0.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal1.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal2.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal3.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
            }

            in RANGE_HIGH -> {
                item.signal0.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal1.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal2.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal3.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
            }

            in RANGE_MID -> {
                item.signal0.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal1.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal2.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
                item.signal3.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
            }

            else -> {
                item.signal0.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal
                ))
                item.signal1.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
                item.signal2.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
                item.signal3.background = (AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.background_radius_signal_grey
                ))
            }
        }

        return item
    }

    private fun updateCachedBeacons(beacons: Collection<Beacon>) {
        //aggiorna last seen per ogni beacon
        beacons.forEach { foundBeacon ->
            val beacon = cachedBeacons.find {
                Utils.isSameBeacon(
                    foundBeacon,
                    it.beacon.id1.toString(),
                    it.beacon.id2.toString(),
                    it.beacon.id3.toString()
                )
            }

            beacon?.lastSeen = System.currentTimeMillis()
            beacon?.beacon = foundBeacon
            beacon?.updateLatestDistances(foundBeacon.distance)
        }
        //Rimuovi i nuovi beacon già presenti
        val currentMillis = System.currentTimeMillis()
        val beaconsWithLastSeen = ArrayList<BeaconWithTimer>()
        val beaconsList = ArrayList(beacons)
        cachedBeacons.distinct().forEach { cachedBeacon ->
            val beacon = beaconsList.find {
                Utils.isSameBeacon(
                    cachedBeacon.beacon,
                    it.id1.toString(),
                    it.id2.toString(),
                    it.id3.toString()
                )
            }
            if (beacon != null) beaconsList.remove(beacon)
        }
        beaconsList.forEach {
            val beaconWithTTL = BeaconWithTimer(it, currentMillis, currentMillis)
            beaconsWithLastSeen.add(beaconWithTTL)
        }
        //Aggiungo i nuovi beacon
        cachedBeacons.addAll(beaconsWithLastSeen)
        //Rimuovi tutti i beacon con last seen troppo vecchia
        cachedBeacons.removeIf {
            (it.lastSeen + TIME_TO_LIVE) < currentMillis
        }
        //metti tutti i nuovi beacon
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentExploreHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun findNomeAula(beacon: Beacon) =
        UserPreferences.getAllLuoghi(requireContext())?.find {
            Utils.isSameBeacon(
                beacon,
                it.uuid,
                it.majorNumber.toString(),
                it.minorNumber.toString()
            )
        }?.nomeStanza

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelApi.allLuoghiResponse.removeObservers(this)
        viewModelApi.calendarioResponse.removeObservers(this)
        viewModelApi.favouritesResponse.removeObservers(this)
        vm.rangedBeacons.removeObservers(this)
        vm.monitoredBeacons.removeObservers(this)
    }
}