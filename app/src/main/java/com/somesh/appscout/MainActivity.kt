package com.somesh.appscout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.somesh.appscout.ui.theme.PaddockTheme
import com.somesh.appscout.view.PaddockApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Paddock is a dark-only experience: force light system icons on the near-black canvas
        // and make the bars transparent so hero imagery can run edge to edge.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { PaddockTheme { PaddockApp() } }
    }
}
