package com.pvsrishabh.momentshub.Models

class Comment {

    var text:String=""
    var uid: String=""
    var docId: String?=""
    var userName: String=""
    var time: String=""
    var userProfile: String? = ""
    var isReel: Boolean = false

    constructor()

    constructor(text: String, userName: String, time: String) {
        this.text = text
        this.userName = userName
        this.time = time
    }

    constructor(text: String, time: String) {
        this.text = text
        this.time = time
    }
}