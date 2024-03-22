package com.pvsrishabh.momentshub.Models

class Story {

    var videoUrl:String=""
    var time: String=""
    var uid: String? = ""

    constructor()

    constructor(videoUrl: String, time: String, uid: String?) {
        this.videoUrl = videoUrl
        this.time = time
        this.uid = uid
    }

}