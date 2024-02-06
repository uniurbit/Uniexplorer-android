package com.appload.einkapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.appload.einkapp.databinding.FragmentPoiHomeBinding
import com.appload.einkapp.databinding.ListItemBinding
import com.appload.einkapp.databinding.ListItemDarkerBinding
import com.appload.einkapp.model.response.CalendarioResponse
import com.appload.einkapp.model.response.FavouritesRespose
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
import com.appload.einkapp.view.explore.BeaconWithTimer
import com.bumptech.glide.Glide
import org.altbeacon.beacon.Beacon

class PoiHome : CheckLoginFragment() {
    private lateinit var savedList: Collection<Beacon>
    private lateinit var binding: FragmentPoiHomeBinding
    private var favoritesList: FavouritesRespose? = null
    private var beaconsList: Collection<Beacon>? = null
    private var calendarList: CalendarioResponse? = null
    private val bindings = arrayListOf<ListItemBinding>()
    private val darkBindings = arrayListOf<ListItemDarkerBinding>()
    private val vm: MainViewModel by activityViewModels { MainViewModel.Factory() }
    private var cachedBeacons = ArrayList<BeaconWithTimer>()
    private var lastUpdate = 0L

    override fun setListeners() {
        binding.buttonAddPoi.setOnClickListener {
            val action = PoiHomeDirections.actionPoiHomeToAddPoiFragment3()
            findNavController().navigate(action)
        }
        binding.profile.setOnClickListener {
            val action = PoiHomeDirections.actionPoiHomeToProfileNavigation()
            findNavController().navigate(action)
        }
    }

    private fun updateCachedBeacons(beacons: Collection<Beacon>?) {
        if (beacons == null) return
        val data = beacons.toMutableList()
        data.removeIf { it.distance < 10 }
        //aggiorna last seen per ogni beacon
        data.forEach { foundBeacon ->
            if (foundBeacon.distance < 10) {
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
                beacon?.updateLatestDistances(
                    beacon.calculateDistance(
                        foundBeacon.txPower,
                        foundBeacon.rssi
                    )
                )
            }
        }
        //Rimuovi i nuovi beacon già presenti
        val currentMillis = System.currentTimeMillis()
        val beaconsWithLastSeen = ArrayList<BeaconWithTimer>()
        val beaconsList = ArrayList(data)
        cachedBeacons.distinctBy { it.beacon }.forEach { cachedBeacon ->
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
        setCompleteList()
    }

    override fun prepareStage() {
        //checkLogged()
        super.prepareStage()
        setObservers()
        checkUser()
    }

    private fun checkUser() {
        if (UserPreferences.getUser(requireContext()) == null) {
            binding.buttonAddPoi.visibility = View.GONE
        } else {
            Glide.with(requireContext())
                .load(UserPreferences.getUserPhoto(requireContext()))
                .circleCrop()
                .placeholder(R.drawable.profile)
                .into(binding.profile)
        }
    }

    private fun setObservers() {
        viewModelApi.favouritesResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        Log.e("favourites", it.container.data.toString())
                        setFavorites(it.container.data)
                    }
                }

                is NetworkResult.Failure -> {
                    if (it.exception.errorCode == "404") {
                        Log.e("getFav", it.exception.errorCode + " " + it.exception.error)
                        setFavorites(null)
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

        vm.rangedBeacons.observe(this) {
//            Log.d("Beacons updated", it.toString())
            setBeacons(it)
        }
    }

    private fun setBeacons(beacons: Collection<Beacon>) {
        val now = System.currentTimeMillis()
        if (lastUpdate == 0L) {
            lastUpdate = now
            beaconsList = beacons
            updateCachedBeacons(beaconsList)
        } else if ((now - lastUpdate) > 1000 * SEC_TIC) {
            lastUpdate = now
            beaconsList = beacons
            updateCachedBeacons(beaconsList)
        }

    }

    private fun setFavorites(favouritesRespose: FavouritesRespose?) {
        favoritesList = favouritesRespose
    }

    private fun setCalendar(calendarioResponse: CalendarioResponse?) {
        calendarList = calendarioResponse
    }

    private fun isAlreadyDetected(favourite: FavouritesRespose.FavouritesResposeItem): Boolean {
        var beacon = false
        cachedBeacons.forEach {
            if (Utils.isSameBeacon(
                    it.beacon,
                    favourite.uuid,
                    favourite.majorNumber.toString(),
                    favourite.minorNumber.toString()
                )
            ) {
                beacon = true
            }
        }
        return beacon
    }

    private fun isAlreadyDetectedCalendar(favourite: FavouritesRespose.FavouritesResposeItem): Boolean {
        var beacon = false
        cachedBeacons.forEach {
            if (Utils.isSameBeacon(
                    it.beacon,
                    favourite.uuid,
                    favourite.majorNumber.toString(),
                    favourite.minorNumber.toString()
                )
            )
                beacon = true
        }
        favoritesList?.forEach {
            if (isAlreadyDetected(it)) {
                beacon = true
            }

        }
        return beacon
    }

    private fun setCompleteList() {
        binding.poiContainer.removeAllViews()
        binding.calendarContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (favoritesList.isNullOrEmpty())
            binding.poiLayout.visibility = View.GONE
        else
            binding.poiLayout.visibility = View.VISIBLE

        favoritesList?.sortedBy { it.nomeStanza }?.forEachIndexed { i, favouritesResposeItem ->
            setItemFavorite(favouritesResposeItem, inflater)
        }

        if (calendarList.isNullOrEmpty())
            binding.calendarLayout.visibility = View.GONE
        else
            binding.calendarLayout.visibility = View.VISIBLE

        calendarList?.sortedBy { it.nomeStanza }?.forEachIndexed() { i, calendarioResponseItem ->
            val calendarItem = FavouritesRespose.FavouritesResposeItem(
                nomeEdificio = calendarioResponseItem.nomeEdificio,
                nomeStanza = calendarioResponseItem.nomeStanza,
                uuid = calendarioResponseItem.uuid,
                idStanza = calendarioResponseItem.idStanza,
                majorNumber = calendarioResponseItem.majorNumber,
                minorNumber = calendarioResponseItem.minorNumber
            )
            setItemCalendar(calendarItem, inflater)
//            if (!isAlreadyDetectedCalendar(calendarItem)) {
//                beacons.add(calendarItem)
//            }
        }

    }

    private fun setCalendarItem(beacon: Beacon, name: String, inflater: LayoutInflater) {
        val item =
            setSignal(
                ListItemBinding.inflate(inflater, binding.poiContainer, true),
                beacon.distance
            )
        item.separator.visibility = View.GONE
        item.name.text = name
        item.root.setOnClickListener {
            val luogo = getLuogo(beacon)
            if (luogo != null) {
                val action =
                    PoiHomeDirections.actionPoiHomeToAulaFragment3(
                        luogo.uuid,
                        luogo.majorNumber.toString(),
                        luogo.minorNumber.toString()
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun setItemFavorite(
        favourite: FavouritesRespose.FavouritesResposeItem,
        inflater: LayoutInflater
    ) {
        val showedBinding = bindings.find { favourite == it.root.tag }
        val savedBinding = darkBindings.find { favourite == it.root.tag }
        val cached = isCached(favourite)
        if (showedBinding != null || savedBinding != null) {

        } else {
            val item = if (cached != null)
                setSignal(
                    ListItemBinding.inflate(inflater, binding.poiContainer, true),
                    cached.avgDistances()
                )
            else
                ListItemDarkerBinding.inflate(
                    inflater,
                    binding.poiContainer,
                    true
                )
            if (item is ListItemBinding) {
                item.name.text = favourite.nomeStanza
                item.separator.visibility = View.GONE
                item.root.contentDescription = favourite.nomeStanza
                item.root.setOnClickListener {
                    val action = PoiHomeDirections.actionPoiHomeToAulaFragment3(
                        favourite.uuid,
                        favourite.majorNumber.toString(),
                        favourite.minorNumber.toString()
                    )
                    findNavController().navigate(action)
                }
                item.root.tag = favourite
                bindings.add(item)
            } else if (item is ListItemDarkerBinding) {
                item.name.text = favourite.nomeStanza
                item.separator.visibility = View.GONE
                item.root.contentDescription = favourite.nomeStanza
                item.root.setOnClickListener {
                    val action = PoiHomeDirections.actionPoiHomeToAulaFragment3(
                        favourite.uuid,
                        favourite.majorNumber.toString(),
                        favourite.minorNumber.toString()
                    )
                    findNavController().navigate(action)
                }
                darkBindings.add(item)
            }
        }
    }

    private fun setItemCalendar(
        favourite: FavouritesRespose.FavouritesResposeItem,
        inflater: LayoutInflater
    ) {
        val cached = isCached(favourite)
        val item = if (cached != null)
            setSignal(
                ListItemBinding.inflate(inflater, binding.calendarContainer, true),
                cached.avgDistances()
            )
        else
            ListItemDarkerBinding.inflate(inflater, binding.calendarContainer, true)

        if (item is ListItemBinding) {
            item.name.text = favourite.nomeStanza
            item.separator.visibility = View.GONE
            item.root.contentDescription = favourite.nomeStanza
            item.root.setOnClickListener {
                val action = PoiHomeDirections.actionPoiHomeToAulaFragment3(
                    favourite.uuid,
                    favourite.majorNumber.toString(),
                    favourite.minorNumber.toString()
                )
                findNavController().navigate(action)
            }
        } else if (item is ListItemDarkerBinding) {
            item.name.text = favourite.nomeStanza
            item.separator.visibility = View.GONE
            item.root.contentDescription = favourite.nomeStanza
            item.root.setOnClickListener {
                val action = PoiHomeDirections.actionPoiHomeToAulaFragment3(
                    favourite.uuid,
                    favourite.majorNumber.toString(),
                    favourite.minorNumber.toString()
                )
                findNavController().navigate(action)
            }
        }
    }


    private fun isCached(favourite: FavouritesRespose.FavouritesResposeItem): BeaconWithTimer? {
        return cachedBeacons.find {
            Utils.isSameBeacon(
                it.beacon,
                favourite.uuid, favourite.majorNumber.toString(), favourite.minorNumber.toString()
            )
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

    private fun getLuogo(beacon: Beacon): LuoghiResponse.LuoghiResponseItem.Luogo? {
        var response: LuoghiResponse.LuoghiResponseItem.Luogo? = null
        if (context != null) {
            val luoghi = UserPreferences.getAllLuoghi(requireContext())
            luoghi?.forEach { luogo ->
                if (Utils.isSameBeacon(
                        beacon,
                        luogo.uuid,
                        luogo.majorNumber.toString(),
                        luogo.minorNumber.toString()
                    )
                ) {
                    response = luogo
                }
            }
        }
        return response
    }

    private fun getLuogoFavorite(beacon: FavouritesRespose.FavouritesResposeItem): LuoghiResponse.LuoghiResponseItem.Luogo? {
        var response: LuoghiResponse.LuoghiResponseItem.Luogo? = null
        if (context != null) {
            val luoghi = UserPreferences.getAllLuoghi(requireContext())
            luoghi?.forEach { luogo ->
                if (Utils.isSameBeaconFavorite(
                        beacon,
                        luogo.uuid,
                        luogo.majorNumber.toString(),
                        luogo.minorNumber.toString()
                    )
                ) {
                    response = luogo
                }
//                if(luogo.idStanza == beacon.idStanza){
//                    response = luogo
//                }
            }
        }
        return response
    }


    private fun checkIfFavoriteCalendar(beacon: Beacon): String? {
        var name: String? = null
        favoritesList?.forEach {
            if (beacon.id1.toString().equals(
                    it.uuid,
                    true
                ) && beacon.id2.toString() == it.majorNumber.toString() && beacon.id3.toString() == it.minorNumber.toString()
            ) {
                name = it.nomeStanza
            }
        }
        calendarList?.forEach {
            if (beacon.id1.toString().equals(
                    it.uuid,
                    true
                ) && beacon.id2.toString() == it.majorNumber.toString() && beacon.id3.toString() == it.minorNumber.toString()
            ) {
                name = it.nomeStanza
            }
        }
        return name
    }

    @SuppressLint("SetTextI18n")
    override fun loggedUI(user: UserResponse?) {
        viewModelApi.calendarioResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.container.data != null) {
                        setCalendar(it.container.data)
                    }
                }

                is NetworkResult.Failure -> {
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

                is NetworkResult.Loading -> {}
            }
        }

        binding.profile.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.profile
            )
        )
        binding.nome.text = getString(R.string.ciao) + " " + (user?.nome ?: "Guest")
        if (user?.id != null) {
            viewModelApi.getFav(user.id)
            viewModelApi.getCalendar(user.id)
        }

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
    }

    override fun gmailUI(user: UserResponse?) {
        binding.profile.setImageDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.profile
            )
        )
        binding.nome.text = user?.nome
        if (user?.id != null) {
            viewModelApi.getFav(user.id)
        }
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentPoiHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
}