package com.pvsrishabh.momentshub.models

class Message {

    var msgId: String = ""
    var uId: String = ""
    var text: String = ""
    var time: String = ""
    var isImage: Boolean = false

    constructor()

    constructor(msgId: String, text: String, time: String) {
        this.msgId = msgId
        this.text = text
        this.time = time
    }

    constructor(msgId: String, text: String) {
        this.msgId = msgId
        this.text = text
    }

    constructor(msgId: String, text: String, time: String, isImage: Boolean) {
        this.msgId = msgId
        this.text = text
        this.time = time
        this.isImage = isImage
    }


}