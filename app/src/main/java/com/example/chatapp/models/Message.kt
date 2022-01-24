package com.example.chatapp.models

class Message {
    var uid: String? = null
    var from: String? = null
    var to: String? = null
    var text: String? = null
    var time: String? = null

    constructor()
    constructor(uid: String?, from: String?, to: String?, text: String?, time: String?) {
        this.uid = uid
        this.from = from
        this.to = to
        this.text = text
        this.time = time
    }
}