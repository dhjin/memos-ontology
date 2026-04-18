package com.llicorp.memosontology

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.llicorp.memosontology.ui.MemoApp
import com.llicorp.memosontology.ui.theme.MemosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemosTheme {
                MemoApp()
            }
        }
    }
}
