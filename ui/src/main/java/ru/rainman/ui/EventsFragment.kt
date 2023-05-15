package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.FragmentEventsBinding
import ru.rainman.ui.helperutils.getNavController

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private val binding: FragmentEventsBinding by viewBinding (FragmentEventsBinding::bind)
    lateinit var navController: NavController


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        binding.newEvent.setOnClickListener {
            navController.navigate(MainFragmentDirections.actionMainFragmentToEventEditorFragment())
        }
    }
}