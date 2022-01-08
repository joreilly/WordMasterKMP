package dev.johnoreilly.wordmaster.androidApp

import android.app.Application
import java.io.File

class WordMasterApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val copiedFile = File(filesDir, "words.txt")
        assets.open("words.txt").use { input ->
            copiedFile.outputStream().use { output ->
                input.copyTo(output, 1024)
            }
        }
    }

}