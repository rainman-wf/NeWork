package ru.rainman.ui.fragments.publications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType
import javax.inject.Inject

@HiltViewModel
class PublicationsPagerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val isLoggedIn = userRepository.authToken.map { it != null }.asLiveData(viewModelScope.coroutineContext)

}

