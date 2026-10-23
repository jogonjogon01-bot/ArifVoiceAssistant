package com.arif.voiceassistant

object VoiceAssistant {
    fun processCommand(command: String): String {
        val text = command.trim().lowercase()

        return when {
            text.contains("hello") ||
            text.contains("হ্যালো") -> "Hello! How can I help you?"

            text.contains("time") ||
            text.contains("সময়") -> "Please check your phone clock."

            text.contains("নাম") ||
            text.contains("your name") -> "I am your voice assistant."

            else -> "Sorry, I don't understand that command yet."
        }
    }
}
