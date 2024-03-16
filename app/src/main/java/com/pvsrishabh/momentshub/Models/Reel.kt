package com.pvsrishabh.momentshub.models

class Reel {
    var videoUrl:String=""
    var caption:String=""
    var profileLink:String?=null

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