package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SelectableUser
import javax.inject.Inject

@HiltViewModel
class SelectUsersDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _filterState = MutableLiveData(UsersFilter(false, null, null))
    val filterState: LiveData<UsersFilter> get() = _filterState

    private val _users = userRepository.flowableUsers.asLiveData(viewModelScope.coroutineContext)

    private val _selectedIds = MutableLiveData(setOf<Long>())
    val selectedIds: List<Long> get() = _selectedIds.value?.toList() ?: listOf()

    private val _selectableUsers = MediatorLiveData<List<SelectableUser>>()
    val selectableUsers: LiveData<List<SelectableUser>> get() = _selectableUsers

    init {
        _selectableUsers.addSource(_users) {
            _selectableUsers.postValue(combineSelectable(it, _selectedIds.value ?: emptySet()))
        }

        _selectableUsers.addSource(_selectedIds) {
            _selectableUsers.postValue(combineSelectable(_users.value ?: emptyList(), it))
        }
    }

    fun setFavorite() {
        val old = _filterState.value!!
        val new = old.copy(favorite = !old.favorite)
        _filterState.postValue(new)
    }

    fun setSelected(value: Boolean?) {
        val old = _filterState.value!!
        val new = old.copy(selected = value)
        _filterState.postValue(new)
    }

    fun setName(value: String?) {
        val old = _filterState.value!!
        val new = old.copy(name = value)
        _filterState.postValue(new)
    }

    fun selectUser(userId: Long) {
        val ids = _selectedIds.value!!.toMutableSet()
        if (!ids.add(userId)) ids.remove(userId)
        _selectedIds.postValue(ids)
    }

    fun selectUsers(userIds: Set<Long>) {
        _selectedIds.postValue(userIds)
    }

    private fun combineSelectable(users: List<User>, ids: Set<Long>): List<SelectableUser> {
        return users.map {
            SelectableUser(it, ids.contains(it.id))
        }
    }

    fun resetSelections() {
        _selectedIds.postValue(emptySet())
    }
}
