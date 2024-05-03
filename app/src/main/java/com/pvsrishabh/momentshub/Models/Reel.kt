package com.pvsrishabh.momentshub.models

class Reel {
    var videoUrl:String=""
    var caption:String=""
    var time: String=""
    var profileLink:String?=null
    var uid: String? = ""
    var docId: String? = ""
    var likes: Long = 0
    var comments: Long? = 0
    var name: String? = ""

    constructor()

    constructor(videoUrl: String, caption: String) {
        this.videoUrl = videoUrl
        this.caption = caption
    }

    constructor(videoUrl: String, caption: String, profileLink: String) {
        this.videoUrl = videoUrl
        this.caption = caption
        this.profileLink = profileLink
    }
}