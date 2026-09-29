package ru.finny.pet.di

import android.content.Context
import ru.finny.pet.content.ContentRepository
import ru.finny.pet.data.local.FinnyDatabase
import ru.finny.pet.data.repository.ProfileRepositoryImpl
import ru.finny.pet.domain.repository.ProfileRepository
import ru.finny.pet.domain.usecase.GameInteractor

object AppContainer {
    @Volatile private var created = false

    lateinit var database: FinnyDatabase
        private set
    lateinit var content: ContentRepository
        private set
    lateinit var profiles: ProfileRepository
        private set
    lateinit var game: GameInteractor
        private set

    fun init(context: Context) {
        if (created) return
        synchronized(this) {
            if (created) return
            database = FinnyDatabase.get(context)
            content = ContentRepository(context)
            profiles = ProfileRepositoryImpl(database)
            game = GameInteractor(profiles, content)
            created = true
        }
    }
}
