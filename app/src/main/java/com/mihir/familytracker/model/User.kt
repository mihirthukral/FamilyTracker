package com.mihir.familytracker.model

data class User(var email:String?,var status:String?){
    constructor():this("","")
}