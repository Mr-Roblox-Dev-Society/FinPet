package ru.finny.pet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.finny.pet.ui.navigation.FinnyNavHost
import ru.finny.pet.ui.theme.FinnyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinnyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinnyNavHost()
                }
            }
        }
    }
}
