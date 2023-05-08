package ru.rainman.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.rainman.ui.databinding.ActivityMainBinding
import ru.rainman.ui.helperutils.getNavController

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

    }
}