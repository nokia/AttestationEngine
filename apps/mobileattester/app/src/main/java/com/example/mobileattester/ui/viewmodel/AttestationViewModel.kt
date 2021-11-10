package com.example.mobileattester.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.mobileattester.data.network.Response
import com.example.mobileattester.data.repository.AttestationRepository
import kotlinx.coroutines.Dispatchers

interface AttestationViewModel {
    fun getElementIds(): LiveData<Response<List<String>>>
}

// --------- Implementation ---------

class AttestationViewModelImpl(
    private val repo: AttestationRepository,
) : AttestationViewModel, ViewModel() {

    private val listOfElementIds = liveData(Dispatchers.IO) {
        emit(Response.loading(data = null))
        try {
            emit(Response.success(data = repo.getElementIds()))
        } catch (exception: Exception) {
            emit(Response.error(data = null, message = exception.message ?: "Error Occurred!"))
            Log.d("TEST", "Error getting element ids: ${exception.message}");
        }
    }

    override fun getElementIds(): LiveData<Response<List<String>>> = listOfElementIds
}

class AttestationViewModelImplFactory(
    private val repo: AttestationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttestationViewModelImpl(repo) as T
    }
}
