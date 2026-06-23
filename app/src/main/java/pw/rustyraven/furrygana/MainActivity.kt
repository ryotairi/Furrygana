package pw.rustyraven.furrygana

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pw.rustyraven.furrygana.services.FurryganaAccessibilityService
import pw.rustyraven.furrygana.ui.DashboardScreen
import pw.rustyraven.furrygana.ui.GreetingScreen
import pw.rustyraven.furrygana.ui.theme.FurryganaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FurryganaTheme {
                var currentScreen by remember { mutableStateOf("intro") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "intro" -> GreetingScreen(
                            onContinue = {
                                if (this@MainActivity.onIntroContinueClick()) {
                                    currentScreen = "dashboard"
                                }
                            }, modifier = Modifier.padding(innerPadding)
                        )

                        "dashboard" -> DashboardScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }

    fun onIntroContinueClick(): Boolean {
        if (isAccessibilityServiceGranted()) return true;
        requestAccessibilityService();
        return false;
    }

    fun isAccessibilityServiceGranted(): Boolean {
        val services =
            Settings.Secure.getString(contentResolver, "enabled_accessibility_services").split(':')
        return services.contains("$packageName/.services.FurryganaAccessibilityService") || services.contains(
            "$packageName/$packageName.services.FurryganaAccessibilityService"
        )
    }

    fun requestAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val componentName =
                ComponentName(packageName, FurryganaAccessibilityService::class.java.name)
            putExtra(Intent.EXTRA_COMPONENT_NAME, componentName.flattenToString())
        }
        startActivity(intent)
    }
}