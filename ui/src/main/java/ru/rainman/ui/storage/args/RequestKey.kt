package ru.rainman.ui.storage.args

enum class RequestKey {
    EVENT_REQUEST_KEY_ATTACHMENT,
    POST_REQUEST_KEY_ATTACHMENT,
    EVENT_REQUEST_KEY_SPEAKERS,
    POST_REQUEST_KEY_MENTIONED,
    EVENT_REQUEST_KEY_LOCATION
}

enum class ArgKeys {
    ATTACHMENT,
    USERS,
    LOCATION;

    override fun toString(): String {
        return this.name
    }
}