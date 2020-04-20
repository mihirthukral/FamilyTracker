package com.mihir.familytracker.model

data class Tracking( var email:String?,
                     var uid:String?,
                     var lat:String?,
                     var lng:String?){
    constructor():this("","","","")
}