package ru.rainman.ui.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentMainBinding
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.menuItemHandle

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding(FragmentMainBinding::bind)
    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = childFragmentManager.getNavController(R.id.main_nav_host)
        val parentNav =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)


        binding.bottomNavBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.mainToolBar.title = destination.label
        }

        viewModel.me.observe(viewLifecycleOwner) { me ->
            val authenticated = me?.token != null

            binding.mainToolBar.menu.clear()
            binding.mainToolBar.inflateMenu(if (authenticated) R.menu.auth_authenticated_menu else R.menu.auth_unaithicated_menu)

            me?.user?.avatar?.let { avatar ->
                Glide.with(requireContext())
                    .asDrawable()
                    .load(avatar)
                    .override(40, 40)
                    .error(R.drawable.avatar_error)
                    .circleCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            binding.mainToolBar.overflowIcon = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            binding.mainToolBar.overflowIcon = errorDrawable
                        }
                    })

            } ?: Glide.with(requireContext())
                .asDrawable()
                .override(40, 40)
                .load(R.drawable.avatar_stub)
                .circleCrop()
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding.mainToolBar.overflowIcon = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        binding.mainToolBar.overflowIcon = errorDrawable
                    }
                })
        }

        binding.mainToolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reg -> {
                    parentNav.navigate(MainFragmentDirections.actionMainFragmentToRegistrationFragment())
                    true
                }

                R.id.login -> {
                    parentNav.navigate(MainFragmentDirections.actionMainFragmentToLoginDialogFragment())
                    true
                }

                R.id.logout -> menuItemHandle { viewModel.logOut() }
                R.id.settings -> {
                    viewModel.me.value?.token?.id?.let { id ->
                        parentNav.navigate(
                            MainFragmentDirections.actionMainFragmentToUserInfoFragment(id, true)
                        )
                    }
                    true
                }

                else -> false
            }
        }
    }


}




