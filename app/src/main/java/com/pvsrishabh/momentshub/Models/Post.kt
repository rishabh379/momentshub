package com.pvsrishabh.momentshub.models

class Post {
    var postUrl:String=""
    var caption:String=""
    var uid: String=""
    var docId: String? = ""
    var time: String=""
    var likes: Long = 0

    constructor()

    constructor(postUrl: String, caption: String) {
        this.postUrl = postUrl
        this.caption = caption
    }

    constructor(postUrl: String, caption: String, uid: String, time: String) {
        this.postUrl = postUrl
        this.caption = caption
        this.uid = uid
        this.time = time
    }


}