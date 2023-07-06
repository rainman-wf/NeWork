package ru.rainman.ui.fragments.publications

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import ru.rainman.ui.helperutils.CustomPagerAdapter

class PagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    fragmentList: List<Fragment>
) : CustomPagerAdapter(fragmentManager, lifecycle, fragmentList)