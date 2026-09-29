package ru.finny.pet

import android.app.Application
import ru.finny.pet.di.AppContainer

class FinnyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
