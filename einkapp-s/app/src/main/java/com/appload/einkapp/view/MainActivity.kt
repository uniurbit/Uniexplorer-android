package com.appload.einkapp.view

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appload.einkapp.R
import com.appload.einkapp.databinding.ActivityMainBinding
import com.appload.einkapp.model.response.CalendarioResponseItem
import com.appload.einkapp.receivers.SnoozeBeaconReceiver
import com.appload.einkapp.receivers.SnoozeBeaconReceiver.Companion.BEACON_MAJOR
import com.appload.einkapp.receivers.SnoozeBeaconReceiver.Companion.BEACON_MINOR
import com.appload.einkapp.receivers.SnoozeBeaconReceiver.Companion.BEACON_UUID
import com.appload.einkapp.receivers.SnoozeBeaconReceiver.Companion.SNOOZE_BEACON
import com.appload.einkapp.utils.ACTION_STOP_FOREGROUND
import com.appload.einkapp.utils.DISTANCE_THRESHOLD
import com.appload.einkapp.utils.DateUtils
import com.appload.einkapp.utils.IBEACON_LAYOUT
import com.appload.einkapp.utils.TIME_BEFORE_NOTIFICATION
import com.appload.einkapp.utils.TIME_TO_LIVE
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.UserPreferences.CALENDARIO
import com.appload.einkapp.utils.UserPreferences.PREFERITI
import com.appload.einkapp.utils.UserPreferences.TUTTI
import com.appload.einkapp.utils.Utils
import com.appload.einkapp.view.base.BaseActivity
import com.appload.einkapp.view.common.InfoDialog
import com.appload.einkapp.view.explore.BeaconWithTimer
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region


class MainActivity : BaseActivity() {
    private lateinit var permissionGroups: Array<String>
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private var cachedBeacons = arrayListOf<BeaconWithTimer>()

    private val vm: MainViewModel by viewModels { MainViewModel.Factory() }
    private var stopBackgroundReceiver: BroadcastReceiver? = null

    val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                scanBeacons()
            } else
                showRationale(permissions)
        }

    fun checkLogged() {
//        if (UserPreferences.isGuest(this)) {
//            binding.poi.isEnabled = false
//            binding.poi.alpha = 0.3f
//        }
    }

    private fun showRationale(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        val rejected = permissions.filter { !it.value }.map { it.key }
        var showNotificationPopUp: Boolean = false
        var rationale = ""
        rejected.forEach {
            if (shouldShowRequestPermissionRationale(it).not())
                when (it) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        rationale =
                            "$rationale${getString(R.string.access_fine_location_rationale)}"
                    }

                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        rationale =
                            "$rationale${getString(R.string.access_background_location_rationale)}"
                    }

                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT -> {
                        rationale =
                            "$rationale${getString(R.string.access_bluetooth)}"
                    }
                }
        }
        if (rationale.isNotEmpty()) {
            InfoDialog(
                this, getString(R.string.attenzione), rationale
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.show()
        }
    }

    val rangedObserver = Observer<Collection<Beacon>> { beacons ->
        vm.rangedBeacons.postValue(beacons)
        beacons.forEach { beacon ->
            //aggiungere && beacon.distance < DISTANCE_THRESHOLD per specificare la distanza
            if (beacon.distance != null) {
                //Aggiungo il beacon alla cache se non è già presente
                //Aggiorno lastSeen se è già presente e last distances
                //mostro la notifica se non è già mostrata
                val isInAllAule = findNomeAula(beacon)
                if (!isInAllAule.isNullOrBlank()) {
                    sendNotification(beacon)
                    if (!cacheContainsBeacon(
                            beacon.id1.toString(),
                            beacon.id2.toString(),
                            beacon.id3.toString()
                        )
                    ) {
                        addToCache(beacon)
//                    Log.e("cached", cachedBeacons.toString())
                    } else {
                        updateLastSeen(beacon)
                    }
                }
                //rimuove tutti i beacon con lastSeen troppo vecchia dalla cache a dalle notifiche
                removeAllInactive()
            }
        }
    }

    private fun updateLastSeen(beacon: Beacon) {//Aggiorno last seen e le ultime distanze
        val found = cachedBeacons.find {
            Utils.isSameBeacon(
                beacon,
                it.beacon.id1.toString(),
                it.beacon.id2.toString(),
                it.beacon.id3.toString()
            )
        }
        found?.lastSeen = System.currentTimeMillis()
//        found?.updateLatestDistances(beacon.distance)
    }

    private fun addToCache(beacon: Beacon) {
        val beaconWithLastSeen = BeaconWithTimer(
            beacon = beacon,
            lastSeen = System.currentTimeMillis(),
            firstSeen = System.currentTimeMillis()
        )
        cachedBeacons.add(beaconWithLastSeen)
    }

    private fun removeAllInactive() {
        //Rimuovo dalle notifiche
        val currentMillis = System.currentTimeMillis()
        cachedBeacons.forEach {
            if ((it.lastSeen + TIME_TO_LIVE) < currentMillis)
                removeShownNotification(it.beacon)
        }
        //Rimuovo dalla cache
        cachedBeacons.removeIf {
            (it.lastSeen + TIME_TO_LIVE) < currentMillis
        }
    }

    private fun removeShownNotification(beacon: Beacon) {
        val nomeAula = findNomeAula(beacon)
        val calendarEvent = checkIfInCalendar(beacon)

        val builder = provideBeaconNotificationBuilder(beacon, nomeAula, calendarEvent)
        provideStackBuilder(builder)
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (alreadyShown(Utils.notificationIdFromBeacon(beacon), notificationManager))
            notificationManager.cancel(Utils.notificationIdFromBeacon(beacon))
    }


    private fun cacheContainsBeacon(id: String, major: String, minor: String): Boolean {
        var contains = false
        cachedBeacons.forEach {
            if (Utils.isSameBeacon(it.beacon, id, major, minor))
                contains = true
        }
        return contains
    }

    private val monitoredObserver = Observer<Int> { beacons ->
        Log.d("BeaconRange", beacons.toString())
        vm.monitoredBeacons.postValue(beacons)
    }

    private fun sendNotification(it: Beacon) {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val enoughTimeAndDistance = checkThresholdAndDistance(it)
        if (!alreadyShown(Utils.notificationIdFromBeacon(it), notificationManager)) {
            val nomeAula = findNomeAula(it)
            val calendarEvent = checkIfInCalendar(it)
            val showNotification =
                isAuthorizedNotification(it) && enoughTimeAndDistance.first && enoughTimeAndDistance.second && !isSnoozedBeacon(
                    it
                )
            if (showNotification) {
                val builder = provideBeaconNotificationBuilder(it, nomeAula, calendarEvent)
                provideStackBuilder(builder)

                if (!alreadyShown(Utils.notificationIdFromBeacon(it), notificationManager))
                    notificationManager.notify(Utils.notificationIdFromBeacon(it), builder.build())
            }
        }
    }

    private fun isSnoozedBeacon(it: Beacon): Boolean {
        return UserPreferences.isBeaconSnoozed(this, it)
    }

    private fun checkThresholdAndDistance(beacon: Beacon): Pair<Boolean, Boolean> {
        var passed = false
        var distanceOk = false
        val foundBeacon = cachedBeacons.find {
            Utils.isSameBeacon(
                it.beacon,
                beacon.id1.toString(),
                beacon.id2.toString(),
                beacon.id3.toString()
            )
        }
        if (foundBeacon != null) {
            passed = isTimeThresholdPassed(foundBeacon)
            distanceOk = isDistanceOk(foundBeacon)
        }
        Log.e("AVG", distanceOk.toString())
        return Pair(passed, distanceOk)
    }

    private fun isDistanceOk(it: BeaconWithTimer): Boolean {//Se la media delle ultime 10 distanze è maggiore di 0 e minore dei metri stabiiti allora mostra il beacon
        val avg = it.avgDistances()
//        Log.e(
//            "BEACON DISTANCE",
//            "${it.beacon.id1} | ${it.beacon.id2} | ${it.beacon.id3} AVERAGE DISTANCE $avg"
//        )
        Log.e("AVG", avg.toString())
        return (avg > 0 && avg < DISTANCE_THRESHOLD)
    }

    private fun isTimeThresholdPassed(beacon: BeaconWithTimer): Boolean {//Controlla che sono passati almeno TIME_BEFORE_NOTIFICATION secondi davanti al beacon
        return System.currentTimeMillis() - beacon.firstSeen > TIME_BEFORE_NOTIFICATION
    }

    private fun isAuthorizedNotification(beacon: Beacon): Boolean {
        var show = false
        val authorizedCategories = UserPreferences.getNotifications(this)
        authorizedCategories?.let {
            if (it.contains(TUTTI))//Notifica per tutti i beacon
                show = true
            if (it.contains(PREFERITI)) {//Notifica se è preferito
                val preferiti = UserPreferences.getFavourites(this)
                preferiti?.let { pref ->
                    val favorite = pref.find { fav ->
                        Utils.isSameBeacon(
                            beacon,
                            fav.uuid,
                            fav.majorNumber.toString(),
                            fav.minorNumber.toString()
                        )
                    }
                    if (favorite != null)
                        show = true
                }
            }
            if (it.contains(CALENDARIO)) {//Notifica se è in calendario
                UserPreferences.getCalendario(this)?.find { event ->
                    Utils.isSameBeacon(
                        beacon,
                        event.uuid,
                        event.majorNumber.toString(),
                        event.minorNumber.toString()
                    )//Stessa aula
                            // && DateUtils.isToday(event.inizio) // Stesso giorno
                            && DateUtils.isInTimeRange(
                        event.inizio,
                        event.fine
                    )// Nella fascia oraria
                }?.let {
                    show = true
                }
            }
        }
        return show
    }

    private fun checkIfInCalendar(beacon: Beacon): CalendarioResponseItem? {
        return UserPreferences.getCalendario(this)?.find {
            Utils.isSameBeacon(
                beacon,
                it.uuid,
                it.majorNumber.toString(),
                it.minorNumber.toString()
            )//Stessa aula
            //&& DateUtils.isToday(it.inizio) // Stesso giorno
            DateUtils.isInTimeRange(it.inizio, it.fine)
        }
    }

    private fun findNomeAula(beacon: Beacon) =
        UserPreferences.getAllLuoghi(this)?.find {
            Utils.isSameBeacon(
                beacon,
                it.uuid,
                it.majorNumber.toString(),
                it.minorNumber.toString()
            )
        }?.nomeStanza

    private fun provideStackBuilder(builder: NotificationCompat.Builder) {
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
    }

    private fun provideBeaconNotificationBuilder(
        it: Beacon,
        nomeAula: String?,
        calendarEvent: CalendarioResponseItem?
    ): NotificationCompat.Builder {
        val snoozeIntent = Intent(this, SnoozeBeaconReceiver::class.java).apply {
            action = SNOOZE_BEACON
            putExtra(BEACON_UUID, it.id1.toString())
            putExtra(BEACON_MAJOR, it.id2.toString())
            putExtra(BEACON_MINOR, it.id3.toString())
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                Utils.notificationIdFromBeacon(it),
                snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_UPDATE_CURRENT
            )
        return NotificationCompat.Builder(this, "beacon-ref-notification-id")
            .setContentTitle(getString(R.string.aula_nelle_vicinanze))
            .setContentText(
                if (calendarEvent == null) "${nomeAula ?: "${it.id1} | ${it.id2} | ${it.id3}"} nelle vicinanze."
                else "${nomeAula ?: "${it.id1} | ${it.id2} | ${it.id3}"} nelle vicinanze. ${calendarEvent.testo.trim()} alle ore ${
                    DateUtils.convertDateToHour(
                        calendarEvent.inizio
                    )
                }."
            )
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.poi)
            .setStyle(
                NotificationCompat.BigTextStyle()
            )
            .addAction(
                R.drawable.ic_clock,
                getString(R.string.silenzia_per_un_ora),
                snoozePendingIntent
            )
    }

    private fun alreadyShown(beaconId: Int, notificationManager: NotificationManager): Boolean {
        var alreadyShown = false
        notificationManager.activeNotifications.forEach {
            if (it.id == beaconId)
                alreadyShown = true
        }
        return alreadyShown
    }

    override fun prepareStage() {
        binding.explore.isEnabled = false
        binding.poi.isEnabled = true
        if (hasToLogin())
            showLogin()
        setNavController()
        checkEnabledFeatures()
        if (intent.action == ACTION_STOP_FOREGROUND) {
            stopForeground()
            finishAndRemoveTask()
        }
//        else if (intent.action == NOTIFICATION_CLICKED){
//            checkAlreadyRunning()
//        }
        checkLogged()
    }

    private fun checkAlreadyRunning() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE)
        for (taskInfo in runningTasks) {
            val componentName = taskInfo.topActivity
            if (componentName?.className == MainActivity::class.java.name) {
                // MyActivity is already running, finish this new instance and return to the existing one
                finish()
                return
            }
        }
    }

    private fun stopForeground() {
        Log.d(TAG, "Stopping Foreground Service")
        val region = Region("all-beacons", null, null, null)
        val bm = BeaconManager.getInstanceForApplication(this)
        try {
            bm.stopRangingBeacons(region)
            bm.stopMonitoring(region)
            bm.disableForegroundServiceScanning()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun checkEnabledFeatures() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
                ?: BluetoothAdapter.getDefaultAdapter()
        val bluetoothEnabled = bluetoothAdapter.isEnabled

        var infoText = ""

        if (!gpsEnabled) {
            infoText = getString(R.string.access_fine_location_rationale)
        }

        if (!bluetoothEnabled) {
            infoText = if (infoText.isNotEmpty()) {
                "$infoText${getString(R.string.also_access_bluetooth)}"
            } else {
                getString(R.string.access_bluetooth)
            }
        }

        if (infoText.isNotEmpty()) {
            InfoDialog(this, getString(R.string.attenzione), infoText) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)

            }.show()
        } else {
            checkPermissions()
        }

    }

    private fun showLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun checkPermissions() {
        permissionGroups = PermissionsHelper(this).beaconScanPermissionGroupsNeeded(true)

        if (!allPermissionsGranted(permissionGroups)) {
            requestPermissionsLauncher.launch(permissionGroups)
        } else {
            scanBeacons()
        }
    }

    private fun allPermissionsGranted(permissionsGroup: Array<String>): Boolean {
        val permissionsHelper = PermissionsHelper(this)
        for (permission in permissionsGroup) {
            if (!permissionsHelper.isPermissionGranted(permission)) {
                Log.e("Permission missing", permission)
                return false
            }
        }
        return true
    }

    private fun hasToLogin(): Boolean {
        return if (UserPreferences.getUser(this) == null) {
            val guest = UserPreferences.isGuest(this)
            !UserPreferences.isGuest(this)
        } else false
    }


    private fun scanBeacons() {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (UserPreferences.isForegroundEnabled(this)) {
            val (builder, channel) = setForegroundService()
            builder.setChannelId(channel.id)
            try {
                beaconManager.enableForegroundServiceScanning(builder.build(), 456)
            } catch (e: java.lang.IllegalStateException) {
                Log.d(TAG, "Service already running")
            }
        }
        BeaconManager.setDebug(true)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout(IBEACON_LAYOUT)
//            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
//            BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24")
        )

        val region = Region("all-beacons", null, null, null)
        beaconManager.backgroundBetweenScanPeriod = 1000L
        beaconManager.backgroundScanPeriod = 5000
        beaconManager.foregroundScanPeriod = 1000L
        // These two lines set up a Live Data observer so this Activity can get beacon data from the Application class
        val regionViewModel = beaconManager.getRegionViewModel(region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
//        regionViewModel.rangedBeacons.removeObservers(this)
        regionViewModel.rangedBeacons.observe(this, rangedObserver)
//        regionViewModel.rangedBeacons.removeObservers(this)
        regionViewModel.regionState.observe(this, monitoredObserver)
        beaconManager.startMonitoring(region)
        beaconManager.startRangingBeacons(region)

        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
//        if(!regionViewModel.rangedBeacons.hasActiveObservers())
//            regionViewModel.rangedBeacons.observeForever(rangedObserver)

    }

    private fun setForegroundService(): Pair<NotificationCompat.Builder, NotificationChannel> {
        val builder = NotificationCompat.Builder(this, "BeaconReferenceApp")
        builder.setSmallIcon(R.drawable.poi)
        builder.setContentTitle("Ricerca aule")
        builder.setOngoing(true)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.action = NOTIFICATION_CLICKED
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        val stopIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_STOP_FOREGROUND
        }

        val btPendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
        builder.addAction(
            R.drawable.baseline_stop_circle_24,
            getString(R.string.sospendi_rilevamento),
            btPendingIntent
        )

        val channel = NotificationChannel(
            "beacon-ref-notification-id",
            "Notifications beacon", NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        return Pair(builder, channel)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_STOP_FOREGROUND)
            stopForeground()
    }

    private fun setNavController() {
        navController = (binding.navHostFragment.getFragment() as NavHostFragment).navController
        navController.setGraph(
            R.navigation.explore_navigation
        )
        checkElevationBottom()
    }

    override fun setListeners() {
        binding.explore.setOnClickListener {
            navController.setGraph(R.navigation.explore_navigation)
            binding.explore.isEnabled = false
            binding.poi.isEnabled = true
            checkElevationBottom()
        }
        if (UserPreferences.isGuest(this)) {
            binding.poi.alpha = 0.3f
            binding.poi.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.per_poter_utilizzare_questa_funzione_devi_essere_loggato))
                    .setCancelable(true)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        } else {
            binding.poi.setOnClickListener {
                navController.setGraph(R.navigation.poi_navigation)
                binding.explore.isEnabled = true
                binding.poi.isEnabled = false
                checkElevationBottom()
            }
        }
    }

    private fun checkElevationBottom() {
        if (binding.explore.isEnabled) {
            binding.explore.elevation = 0f
            binding.poi.elevation = 4f
        } else {
            binding.explore.elevation = 4f
            binding.poi.elevation = 0f
        }
    }

    override fun getActivityView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }


    class PermissionsHelper(val context: Context) {
        fun isPermissionGranted(permissionString: String): Boolean {
            return (ContextCompat.checkSelfPermission(
                context,
                permissionString
            ) == PackageManager.PERMISSION_GRANTED)
        }

        fun beaconScanPermissionGroupsNeeded(backgroundAccessRequested: Boolean = false): Array<String> {
            val permissions = ArrayList<String>()
            // As of version M (6) we need FINE_LOCATION (or COARSE_LOCATION, but we ask for FINE)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // As of version Q (10) we need FINE_LOCATION and BACKGROUND_LOCATION
                if (backgroundAccessRequested) {
                    permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // As of version S (12) we need FINE_LOCATION, BLUETOOTH_SCAN and BACKGROUND_LOCATION
                // Manifest.permission.BLUETOOTH_CONNECT is not absolutely required to do just scanning,
                // but it is required if you want to access some info from the scans like the device name
                // and the aditional cost of requsting this access is minimal, so we just request it
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return permissions.toTypedArray()
        }

    }


    override fun onStart() {
        super.onStart()
        checkEnabledFeatures()
    }
}