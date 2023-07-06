package ru.rainman.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Post

interface PostRepository: PublicationsRepository<Post, NewPostDto> {

    fun wall (ownerId: Long): Flow<PagingData<Post>>

}