package com.example.chatapp.notification

class Data {
    var user: String? =null
    var icon: Int? = null
    var body: String? = null
    var title: String? = null
    var type: String? = null
    var sented: String? = null
    var sentedName: String? = null

    constructor()
    constructor(
        user: String?,
        icon: Int?,
        body: String?,
        title: String?,
        type: String?,
        sented: String?,
        sentedName: String?
    ) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.type = type
        this.sented = sented
        this.sentedName = sentedName
    }


}