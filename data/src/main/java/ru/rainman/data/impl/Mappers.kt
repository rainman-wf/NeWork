package ru.rainman.data.impl

import com.example.common_utils.toDateTime
import ru.rainman.data.local.entity.*
import ru.rainman.data.local.entity.crossref.*
import ru.rainman.data.remote.request.EventCreateRequest
import ru.rainman.data.remote.request.PostCreateRequest
import ru.rainman.data.remote.response.*
import ru.rainman.data.remote.response.Coordinates
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.*
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates as CoordinatesModel
import ru.rainman.data.remote.response.Attachment as AttachmentRequestBody

fun UserWithJob.toModel() = User(
    id = userEntity.userId,
    login = userEntity.login,
    name = userEntity.name,
    avatar = userEntity.avatar,
    favorite = userIdEntity?.userId == userEntity.userId,
    currentJob = job?.toModel()
)

fun JobEntity.toModel() = Job(
    id = id,
    name = name,
    position = position,
    start = start.toDateTime(),
    finish = finish?.toDateTime(),
    link = link
)

fun JobResponse.toEntity(userId: Long) = JobEntity(
    id = id,
    employeeId = userId,
    name = name,
    position = position,
    start = start,
    finish = finish,
    link = link
)

fun UserResponse.toEntity() = UserEntity(
    userId = id,
    login = login,
    name = name,
    avatar = avatar,
    jobId = null
)

fun EventResponse.toEntity() = EventEntity(
    eventId = id,
    authorId = authorId,
    content = content,
    datetime = datetime,
    published = published,
    coordinates = coordinates?.toModel(),
    type = type,
    likedByMe = likedByMe,
    participatedByMe = participatedByMe,
    ownedByMe = ownedByMe,
    link = link
)

fun EventWithUsers.toModel() = Event(
    id = eventEntity.eventId,
    author = author.toModel(),
    content = eventEntity.content,
    datetime = eventEntity.datetime.toDateTime(),
    published = eventEntity.published.toDateTime(),
    coordinates = eventEntity.coordinates,
    type = EventType.valueOf(eventEntity.type),
    likeOwnerIds = likeOwners.map { it.toModel() },
    likedByMe = eventEntity.likedByMe,
    speakerIds = speakers.map { it.toModel() },
    participantsIds = participants.map { it.toModel() },
    participatedByMe = eventEntity.participatedByMe,
    attachment = attachment?.toModel(),
    link = linkPreview?.toModel(),
    ownedByMe = eventEntity.ownedByMe,
)

fun Coordinates.toModel() = CoordinatesModel(
    latitude = latitude.toDouble(),
    longitude = longitude.toDouble()
)

fun AttachmentEntity.toModel(): Attachment {
    return when (this.type) {
        AttachmentType.IMAGE -> Attachment.Image(url, 1.7f)
        AttachmentType.VIDEO -> Attachment.Video(url, duration!!, ratio!!)
        AttachmentType.AUDIO -> Attachment.Audio(url, duration!!, artist!!, title!!)
    }
}

fun PostWithUsers.toModel() = Post(
    id = postEntity.postId,
    author = author.toModel(),
    content = postEntity.content,
    published = postEntity.published.toDateTime(),
    coordinates = postEntity.coordinates,
    link = linkPreview?.toModel(),
    likeOwnerIds = likeOwners.map { it.toModel() },
    mentionIds = mentioned.map { it.toModel() },
    mentionedMe = postEntity.mentionedMe,
    likedByMe = postEntity.likedByMe,
    attachment = attachment?.toModel(),
    ownedByMe = postEntity.ownedByMe,
)

fun PostResponse.toEntity() = PostEntity(
    postId = id,
    authorId = authorId,
    content = content,
    published = published,
    coordinates = coordinates?.toModel(),
    mentionedMe = mentionedMe,
    likedByMe = likedByMe,
    ownedByMe = ownedByMe,
    link = link
)

fun List<PostResponse>.fetchPostLikeOwners() =
    map { post ->
        post.likeOwnerIds.map { PostsLikeOwnersCrossRef(post.id, it) }
    }.flatten()

fun List<PostResponse>.fetchMentioned() =
    map { post ->
        post.mentionIds.map { PostsMentionedUsersCrossRef(post.id, it) }
    }.flatten()

fun List<EventResponse>.fetchEventLikeOwners() =
    map { event ->
        event.likeOwnerIds.map { EventsLikeOwnersCrossRef(event.id, it) }
    }.flatten()

fun List<EventResponse>.fetchSpeakers() =
    map { event ->
        event.speakerIds.map { EventsSpeakersCrossRef(event.id, it) }
    }.flatten()

fun List<EventResponse>.fetchParticipants() =
    map { event ->
        event.participantsIds.map { EventsParticipantsCrossRef(event.id, it) }
    }.flatten()

fun JobEntity.compareWith(jobEntity: JobEntity?): Boolean {
    return jobEntity != null && id == jobEntity.id && finish == jobEntity.finish && link == jobEntity.link
}

fun EventEntity.compareWith(eventEntity: EventEntity): Boolean {
    return content == eventEntity.content &&
            datetime == eventEntity.datetime &&
            coordinates == eventEntity.coordinates &&
            type == eventEntity.type &&
            link == eventEntity.link &&
            participatedByMe == eventEntity.participatedByMe &&
            likedByMe == eventEntity.likedByMe
}

fun PostEntity.compareWith(postEntity: PostEntity): Boolean {
    return content == postEntity.content &&
            coordinates == postEntity.coordinates &&
            link == postEntity.link &&
            likedByMe == postEntity.likedByMe &&
            mentionedMe == postEntity.mentionedMe
}

fun LinkPreview.toEventLinkEntity(eventId: Long) = EventLinkPreviewEntity(
    publicationId = eventId,
    linkPreview = LinkPreview(url, title, description, image, siteName)
)

fun PublicationLinkPreviewEntity.toModel() = LinkPreview(
    url = linkPreview.url,
    title = linkPreview.title,
    description = linkPreview.description,
    image = linkPreview.image,
    siteName = linkPreview.siteName,
)

fun NewEventDto.toRequestBody(attachment: AttachmentRequestBody? = null) = EventCreateRequest(
    id = 0,
    content = content,
    dateTime = dateTime.toString(),
    coordinates = coordinates?.let {
        Coordinates(
            it.latitude.toString().take(8),
            it.longitude.toString().take(8)
        )
    },
    type = type!!.name,
    attachment = attachment,
    link = link,
    speakerIds = speakerIds
)

fun NewPostDto.toRequestBody(attachment: AttachmentRequestBody? = null) = PostCreateRequest(
    id = 0,
    content = content,
    coordinates = coordinates?.let {
        Coordinates(
            it.latitude.toString().take(8),
            it.longitude.toString().take(8)
        )
    },
    attachment = attachment,
    link = link,
    mentionIds = mentionIds
)


