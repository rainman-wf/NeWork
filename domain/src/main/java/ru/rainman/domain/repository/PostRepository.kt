package ru.rainman.domain.repository

import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Post

interface PostRepository: PublicationsRepository<Post, NewPostDto>