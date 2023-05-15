package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.Args.*
import ru.rainman.ui.databinding.FragmentPagerBinding

@AndroidEntryPoint
class PagerFragment : Fragment(R.layout.fragment_pager) {

    private val binding: FragmentPagerBinding by viewBinding(FragmentPagerBinding::bind)
    private val args: PagerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val tabs = binding.tabs
        val pager = binding.pager

        val fragments =
            when (args.intention) {
                PUBLICATIONS -> listOf(
                    PostsFragment(),
                    EventsFragment()
                )
                USERS -> listOf(
                    UsersFragment(),
                    UsersFragment()
                )
            }

        pager.adapter = PagerAdapter(childFragmentManager, lifecycle, fragments)

        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = when (pos) {
                0 -> when (args.intention) {
                    PUBLICATIONS -> "Posts"
                    USERS -> "All"
                }
                1 -> when (args.intention) {
                    PUBLICATIONS -> "Events"
                    USERS -> "Favorite"
                }
                else -> null
            }
        }.attach()
    }
}
