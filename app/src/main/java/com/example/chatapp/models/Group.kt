package com.example.chatapp.models

import java.io.Serializable

class Group : Serializable {
    var uid: String? = null
    var groupName: String? = null
    var bio: String?= null
    var avatar: String? = null

    constructor()
    constructor(
        uid: String?,
        groupName: String?,
        bio: String?,
        avatar: String?,
    ) {
        this.uid = uid
        this.groupName = groupName
        this.bio = bio
        this.avatar = avatar
    }
}