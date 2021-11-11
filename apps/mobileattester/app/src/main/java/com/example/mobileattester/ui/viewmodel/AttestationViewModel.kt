package com.example.mobileattester.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.mobileattester.data.model.Element
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.repository.AttestationRepository
import kotlinx.coroutines.Dispatchers

interface AttestationViewModel {
    var baseUrl : MutableLiveData<String>
    fun isDefaultBaseUrl() : Boolean

    fun getElementIds(): LiveData<Response<List<String>>>
    suspend fun getElement(itemid: String): Response<Element>

    fun getPolicyIds(): LiveData<Response<List<String>>>
}

// --------- Implementation ---------

class AttestationViewModelImpl(
    private val repo: AttestationRepository,
) : AttestationViewModel, ViewModel() {

    private val elements = mutableListOf<Element>()


    private val listOfElementIds = emitResponse(repo::getElementIds)
    private val listOfPolicyIds = emitResponse(repo::getPolicyIds)
    override var baseUrl = repo.baseUrl
    override fun isDefaultBaseUrl(): Boolean = repo.isDefaultBaseUrl()

    override fun getElementIds() = listOfElementIds

    override suspend fun getElement(itemid: String): Response<Element> {
        return try {
            elements.find { it.itemid == itemid }?.let {
                return Response.success(it)
            }

            val element = repo.getElement(itemid)
            elements.add(element)
            Response.success(element)
        } catch (e: Exception) {
            Response.error(null, e.message.toString())
        }
    }

    override fun getPolicyIds() = listOfPolicyIds


    private fun <T> emitResponse(func: suspend () -> T): LiveData<Response<T>> {
        return liveData(Dispatchers.IO) {
            emit(Response.loading(null))
            try {
                emit(Response.success(data = func()))
                Log.d("TEST", "data fetched successfully")
            } catch (exception: Exception) {
                emit(Response.error(data = null, message = exception.message ?: "Error Occurred!"))
                Log.d("TEST", "Error getting data: ${exception.message}")
            }
        }
    }
}


class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo) as T
    }
}
