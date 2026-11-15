package kr.ac.dongguk.diet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kr.ac.dongguk.diet.ui.navigation.DietNavHost
import kr.ac.dongguk.diet.ui.theme.DietAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietAppTheme {
                DietNavHost()
            }
        }
    }
}
