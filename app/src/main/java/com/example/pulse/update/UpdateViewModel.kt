package com.example.pulse.update

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UpdateRepository(application)

    private val _events = Channel<UpdateEvent>(Channel.BUFFERED)
    val events: Flow<UpdateEvent> = _events.receiveAsFlow()

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
            Log.w(TAG, "Не удалось проверить обновление", e)
            return
        }

        if (info.versionCode <= repository.currentVersionCode()) {
            _events.send(UpdateEvent.Message("Установлена актуальная версия \uD83D\uDC85"))
            return
        }

        _events.send(UpdateEvent.Message("Найдена версия ${info.versionName}, скачиваю в фоне"))
        try {
            val file = repository.download(info)
            _events.send(UpdateEvent.ReadyToInstall(file))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось скачать обновление", e)
            _events.send(UpdateEvent.Message("Не удалось скачать обновление"))
        }
    }

    private companion object {
        const val TAG = "UpdateViewModel"
    }
}