package com.example.pulse.update

import java.io.File

sealed interface UpdateEvent {
    data class Message(val text: String) : UpdateEvent
    data class ReadyToInstall(val file: File) : UpdateEvent
}