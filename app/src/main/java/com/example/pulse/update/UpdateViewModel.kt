package com.example.pulse.update

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UpdateRepository(application)
    private val _events = Channel<UpdateEvent>(Channel.BUFFERED)
    val events: Flow<UpdateEvent> = _events.receiveAsFlow()
    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()
    var awaitingInstallPermission: File? = null

    init {
        viewModelScope.launch { checkForUpdate() }
    }

    suspend fun checkForUpdate() {
        val info = try {
            repository.fetchLatest()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return
        }

        if (info.versionCode <= repository.currentVersionCode()) {
            _events.send(UpdateEvent.Message("Установлена актуальная версия \uD83D\uDC85"))
            return
        }

        _events.send(UpdateEvent.Message("Найдена версия ${info.versionName}, скачиваю в фоне"))
        try {
            val file = try {
                repository.download(info) { progress -> _downloadProgress.value = progress }
            } finally {
                _downloadProgress.value = null
            }
            _events.send(UpdateEvent.ReadyToInstall(file))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _events.send(UpdateEvent.Message("Не удалось скачать обновление"))
        }
    }
    private companion object {
        const val TAG = "UpdateViewModel"
    }
}