package com.example.chatapp.models

class GroupMessage {
    var uid: String? = null
    var senderUID: String? = null
    var senderName: String? = null
    var time: String? = null
    var text: String? = null

    constructor()
    constructor(
        uid: String?,
        senderUID: String?,
        senderName: String?,
        time: String?,
        text: String?
    ) {
        this.uid = uid
        this.senderUID = senderUID
        this.senderName = senderName
        this.time = time
        this.text = text
    }

}