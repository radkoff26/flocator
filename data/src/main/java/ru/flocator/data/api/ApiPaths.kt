package ru.flocator.data.api

object ApiPaths {
    /* AUTH */
    /**
     * POST
     *
     * Body: (firstName, lastName, email, login, password)
     *
     * Response: -
     * */
    const val AUTH_REGISTER = "auth/register"

    /**
     * GET
     *
     * Params: refresh_token
     *
     * Response: token_pair (refreshToken, accessToken)
     * */
    const val AUTH_REFRESH = "auth/refresh"

    /**
     * GET
     *
     * Params: username, password
     *
     * Response: refreshToken
     * */
    const val AUTH_LOGIN = "auth/login"

    /**
     * GET
     *
     * Params: login
     *
     * Response: boolean
     * */
    const val AUTH_IS_LOGIN_AVAILABLE = "auth/is_login_available"

    /**
     * GET
     *
     * Params: email
     *
     * Response: boolean
     * */
    const val AUTH_IS_EMAIL_AVAILABLE = "auth/is_email_available"

    /* USER */
    /**
     * POST
     *
     * Params: blockedId
     *
     * Response: -
     * */
    const val USER_UNBLOCK = "api/user/unblock"

    /**
     * POST
     *
     * Params: -
     *
     * Response: -
     * */
    const val USER_ONLINE = "api/user/online"

    /**
     * POST
     *
     * Params: -
     *
     * Response: -
     * */
    const val USER_OFFLINE = "api/user/offline"

    /**
     * POST
     *
     * Params: firstName, lastName
     *
     * Response: boolean
     * */
    const val USER_NAME = "api/user/name"

    /**
     * POST
     *
     * Body: (longitude, latitude)
     *
     * Response: boolean
     * */
    const val USER_LOCATION = "api/user/location"

    /**
     * POST
     *
     * Params: newPassword
     *
     * Response: boolean
     * */
    const val USER_CHANGE_PASSWORD = "api/user/changePassword"

    /**
     * POST
     *
     * Params: blockedId
     *
     * Response: -
     * */
    const val USER_BLOCK = "api/user/block"

    /**
     * POST
     *
     * Params: birthDate (Timestamp)
     *
     * Response: boolean
     * */
    const val USER_BIRTH_DATE = "api/user/birthdate"

    /**
     * POST
     *
     * Params: photoUri
     *
     * Response: boolean
     * */
    const val USER_AVATAR = "api/user/avatar"

    /**
     * GET
     *
     * Params: name, surname
     *
     * Response: list of users
     * */
    const val USER_GET_USERS_BY_NAME = "api/user/getUsersByName"

    /**
     * GET
     *
     * Params: userId
     *
     * Response: (firstName, lastName)
     * */
    const val USER_GET_USERNAME = "api/user/getUsername"

    /**
     * GET
     *
     * Params: userId (optional - target user will be returned)
     *
     * Response: object of user
     * */
    const val USER_GET_USER = "api/user/getUser"

    /**
     * GET
     *
     * Params: userId
     *
     * Response: list of users
     * */
    const val USER_GET_FRIENDS = "api/user/getFriends"

    /**
     * GET
     *
     * Params: userId
     *
     * Response: list of users for map
     * */
    const val USER_GET_FRIENDS_FOR_MAP = "api/user/getFriends/map"

    /**
     * GET
     *
     * Params: -
     *
     * Response: list of users
     * */
    const val USER_GET_FRIEND_REQUESTS = "api/user/getFriendRequests"

    /**
     * GET
     *
     * Params: -
     *
     * Response: list of users
     * */
    const val USER_GET_BLOCKED = "api/user/getBlocked"

    /**
     * GET
     *
     * Params: -
     *
     * Response: list of users
     * */
    const val USER_BLOCKED_BY = "api/user/blockedBy"

    /**
     * DELETE
     *
     * Params: -
     *
     * Response: -
     * */
    const val USER_DELETE = "api/user"

    /* PHOTO */
    /**
     * GET
     *
     * Params: uri
     *
     * Response: bytearray of photo
     * */
    const val PHOTO_GET = "api/photo"

    /**
     * POST
     *
     * Body: list of bytearrays
     *
     * Response: list of uris of saved photos
     * */
    const val PHOTO_POST = "api/photo"

    /**
     * GET
     *
     * Params: uri, compressionFactor
     *
     * Response: bytearray of photo
     * */
    const val PHOTO_GET_COMPRESSED = "api/photo/compressed"

    /* MARK */
    /**
     * POST
     *
     * Body: mark object
     *
     * Response: boolean
     * */
    const val MARK_POST = "api/mark"

    /**
     * POST
     *
     * Params: markId
     *
     * Response: -
     * */
    const val MARK_UNLIKE = "api/mark/unlike"

    /**
     * POST
     *
     * Params: markId
     *
     * Response: -
     * */
    const val MARK_LIKE = "api/mark/like"

    /**
     * GET
     *
     * Params: markId
     *
     * Response: object of mark
     * */
    const val MARK_GET = "api/mark/like"

    /**
     * GET
     *
     * Params: markId
     *
     * Response: list of users
     * */
    const val MARK_WHO_LIKED = "api/mark/whoLiked"

    /**
     * GET
     *
     * Params: -
     *
     * Response: list of marks
     * */
    const val MARK_FRIENDS = "api/mark/friends"

    /* FRIENDSHIP */
    /**
     * POST
     *
     * Params: friendId
     *
     * Response: -
     * */
    const val FRIENDSHIP_REJECT = "api/friendship/reject"

    /**
     * POST
     *
     * Params: friendId
     *
     * Response: -
     * */
    const val FRIENDSHIP_ACCEPT = "api/friendship/accept"

    /**
     * POST
     *
     * Params: friendId, status (PRECISE | APPROXIMATE | FIXED)
     *
     * Response: -
     * */
    const val FRIENDSHIP_CHANGE_PRIVACY = "api/friendship/privacy/change"

    /**
     * POST
     *
     * Params: login
     *
     * Response: boolean
     * */
    const val FRIENDSHIP_ADD_BY_LOGIN = "api/friendship/privacy/add_by_login"

    /**
     * POST
     *
     * Params: friendId
     *
     * Response: boolean
     * */
    const val FRIENDSHIP_ADD = "api/friendship/add"

    /**
     * GET
     *
     * Params: -
     *
     * Response: list of friend ids and statuses (PRECISE | APPROXIMATE | FIXED)
     * */
    const val FRIENDSHIP_PRIVACY = "api/friendship/privacy"

    /**
     * DELETE
     *
     * Params: friendId
     *
     * Response: -
     * */
    const val FRIENDSHIP_DELETE_FRIEND = "api/friendship"
}