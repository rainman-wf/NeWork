package ru.rainman.domain.repository

import ru.rainman.domain.dto.NewObjectDto
import ru.rainman.domain.model.BaseModel

interface PostRepository<M: BaseModel, NO: NewObjectDto> : PublicationsRepository<M, NO>