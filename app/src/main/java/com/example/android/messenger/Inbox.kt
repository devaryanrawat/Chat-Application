package com.example.android.messenger

import java.util.*

data class Inbox (
    val msg:String,
    var from:String,
    var image:String,
    var count:Int =0,
    var name:String,
    val time:Date = Date()
){
    constructor() :this("","","",0,"",Date())

    constructor(msg: String, friendId: String?, image: String?, count: Int,name: String) :
            this(msg,friendId!!,image!!,count,name,Date())
}