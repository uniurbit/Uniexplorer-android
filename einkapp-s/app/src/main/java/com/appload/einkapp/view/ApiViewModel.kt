package com.appload.einkapp.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appload.einkapp.model.request.DeleteLuogoRequest
import com.appload.einkapp.model.request.LoginRequest
import com.appload.einkapp.model.request.LogoutRequest
import com.appload.einkapp.model.request.LuogoRequest
import com.appload.einkapp.model.response.BeaconsResponse
import com.appload.einkapp.model.response.CalendarioResponse
import com.appload.einkapp.model.response.FavouritesRespose
import com.appload.einkapp.model.response.LuoghiResponse
import com.appload.einkapp.model.response.LuogoResponse
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.repository.ApiRepository
import com.appload.einkapp.repository.network.ApiContainer
import com.appload.einkapp.repository.network.Event
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.view.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.UUID

class ApiViewModel(private val rep: ApiRepository) : BaseViewModel() {
//    val luogoResponse = MutableLiveData<NetworkResult<ApiContainer<LuogoResponse>>>()
    val luogoResponse = MutableLiveData<NetworkResult<ApiContainer<BeaconsResponse>>>()
    val allLuoghiResponse = MutableLiveData<NetworkResult<ApiContainer<LuoghiResponse>>>()
    val favouritesResponse = MutableLiveData<NetworkResult<ApiContainer<FavouritesRespose>>>()
    val calendarioResponse = MutableLiveData<NetworkResult<ApiContainer<CalendarioResponse>>>()
    val addLuogo = MutableLiveData<Event<NetworkResult<ApiContainer<String>>>>()
    val user = MutableLiveData<NetworkResult<ApiContainer<UserResponse>>>()
    val userLogout = MutableLiveData<NetworkResult<ApiContainer<String>>>()

    //val deleteLuogo= MutableLiveData<NetworkResult<String?>>()
    val deletedLuogoEvent = MutableLiveData<Event<NetworkResult<ApiContainer<String>>>>()


    class Factory(private val rep: ApiRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ApiViewModel(this.rep) as T
        }
    }

    fun getLuogo(uuid: String,major:String,minor:String) {
        viewModelScope.launch {
            luogoResponse.postValue(NetworkResult.Loading())
            luogoResponse.postValue(rep.getLuogo(uuid,major,minor))
        }
    }

    fun getAllLuoghi() {
        viewModelScope.launch {
            allLuoghiResponse.postValue(NetworkResult.Loading())
            val all = rep.getAllLuoghi()
//            when (all) {
//                is NetworkResult.Success -> {
//                    all.data.add(fakeApploadLuoghi())
//                }
//            }
            allLuoghiResponse.postValue(all)
        }
    }

    private fun fakeApploadLuoghi() = LuoghiResponse.LuoghiResponseItem(
        idEdificio = "6",
        nomeEdificio = "Appload",
        luoghi = arrayListOf(
            LuoghiResponse.LuoghiResponseItem.Luogo(
                idStanza = "11",
                uuid = "e5774e38-2a30-4856-93ff-850ba902ff0e",
                majorNumber = 200,
                minorNumber = 2,
                nomeStanza = "Iphone XS",
                testo = "blabla",
            ),
            LuoghiResponse.LuoghiResponseItem.Luogo(
                idStanza = "12",
                uuid = "e5774e38-2a30-4856-93ff-850ba902ff0e",
                majorNumber = 200,
                minorNumber = 1,
                nomeStanza = "Iphone 7",
                testo = "blabla",
            ),
            LuoghiResponse.LuoghiResponseItem.Luogo(
                idStanza = "13",
                uuid = "e5774e38-2a30-4856-93ff-850ba902ff0e",
                majorNumber = 100,
                minorNumber = 1,
                nomeStanza = "Samsung salmone",
                testo = "blabla"
            ),
            LuoghiResponse.LuoghiResponseItem.Luogo(
                idStanza = "14",
                uuid = "e5774e38-2a30-4856-93ff-850ba902ff0e",
                majorNumber = 100,
                minorNumber = 2,
                nomeStanza = "Samsung altro",
                testo = "blabla"
            )
        )
    )

    fun getFav(idUtente: Int) {
        viewModelScope.launch {
            favouritesResponse.postValue(NetworkResult.Loading())
            favouritesResponse.postValue(rep.getFavourites(idUtente))
        }
    }

    fun getCalendar(idUtente: Int) {
        viewModelScope.launch {
            calendarioResponse.postValue(NetworkResult.Loading())
            calendarioResponse.postValue(rep.getCalendar(idUtente))
        }
    }

    fun addLuogo(idUtente: Int,luogo: LuogoRequest) {
        viewModelScope.launch {
            addLuogo.postValue(Event(NetworkResult.Loading()))
            addLuogo.postValue(Event(rep.addLuogo(idUtente,luogo)))
        }
    }

    fun logout(utente: LogoutRequest) {
        viewModelScope.launch {
            userLogout.postValue(NetworkResult.Loading())
            userLogout.postValue(rep.logout(utente))
        }
    }

    fun login(utente: LoginRequest) {
        viewModelScope.launch {
            user.postValue(NetworkResult.Loading())
            user.postValue(rep.login(utente))
        }
    }

    fun deleteLuogo(idUtente : Int ,delete: DeleteLuogoRequest) {
        viewModelScope.launch {
            deletedLuogoEvent.postValue(Event(NetworkResult.Loading()))
            deletedLuogoEvent.postValue(Event(rep.deleteLuogo(idUtente.toString(),delete)))
        }
    }

    private suspend fun awaitAll(vararg jobs: Job) {
        jobs.asList().joinAll()
    }

}