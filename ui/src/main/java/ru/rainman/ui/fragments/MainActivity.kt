package ru.rainman.ui.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.helperutils.PlayerHolder

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerHolder.initPlayer(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerHolder.instance.release()
    }
}