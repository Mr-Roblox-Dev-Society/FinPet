package ru.finny.pet.util

import ru.finny.pet.domain.model.PetSpecies
import kotlin.random.Random

object ChildCodeGenerator {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(prefix: String = "FIN"): String {
        val body = buildString {
            repeat(3) { append(ALPHABET[Random.nextInt(ALPHABET.length)]) }
        }
        return prefix + body
    }
}

object PetFactory {
    fun allSpecies(): List<PetSpecies> = PetSpecies.entries
}
