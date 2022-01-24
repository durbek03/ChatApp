package com.example.chatapp.models

import java.io.Serializable

class User : Serializable {
    var uid: String? = null
    var nickName: String? = null
    var profileImg: String? = null
    var bio: String? = null
    var number: String? = null
    var gmail: String? = null
    var online: Boolean = false
    var token: String? = null

    constructor()
    constructor(
        uid: String?,
        nickName: String?,
        profileImg: String?,
        bio: String?,
        number: String?,
        gmail: String?,
        online: Boolean,
        token: String?
    ) {
        this.uid = uid
        this.nickName = nickName
        this.profileImg = profileImg
        this.bio = bio
        this.number = number
        this.gmail = gmail
        this.online = online
        this.token = token
    }


    override fun equals(other: Any?): Boolean {
        val user = other as User

        return user.online == this.online && user.nickName == this.nickName
    }
}