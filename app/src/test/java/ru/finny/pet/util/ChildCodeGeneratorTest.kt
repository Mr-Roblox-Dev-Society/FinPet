package ru.finny.pet.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChildCodeGeneratorTest {
    @Test
    fun generatesPrefixedAlphanumeric() {
        val code = ChildCodeGenerator.generate()
        assertThat(code).startsWith("FIN")
        assertThat(code.length).isEqualTo(6)
        assertThat(code.substring(3)).matches("[A-Z0-9]+")
    }
}
