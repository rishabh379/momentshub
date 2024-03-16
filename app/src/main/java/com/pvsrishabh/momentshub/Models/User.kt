package com.pvsrishabh.momentshub.Models

class User{
    var image:String? = null
    var name:String? = null
    var email:String? = null
    var password:String? = null
    var userId: String? = null
    var lastMessage: String? = null

    constructor()



    constructor(image: String?, name: String?, email: String?, password: String?) {
        this.image = image
        this.name = name
        this.email = email
        this.password = password
    }

    constructor(name: String?, email: String?, password: String?) {
        this.name = name
        this.email = email
        this.password = password
    }

    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

    constructor(image: String?, name: String?, email: String?, password: String?, userId: String?) {
        this.image = image
        this.name = name
        this.email = email
        this.password = password
        this.userId = userId
    }


}