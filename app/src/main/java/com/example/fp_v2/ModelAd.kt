package com.example.fp_v2

class ModelAd {
    var id: String = ""
    var vid: String= ""
    var brand: String= ""
    var category: String= ""
    var condition: String= ""
    var address: String= ""
    var price: String= ""
    var title: String= ""
    var description: String= ""
    var status: String= ""
    var timestamp: Long=0
    var latitude = 0.0
    var longitude = 0.0
    var favourite=false


    constructor()


    constructor(
        id: String,
        vid: String,
        brand: String,
        category: String,
        condition: String,
        address: String,
        price: String,
        title: String,
        description: String,
        status: String,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        favourite: Boolean
    ) {
        this.id = id
        this.vid = vid
        this.brand = brand
        this.category = category
        this.condition = condition
        this.address = address
        this.price = price
        this.title = title
        this.description = description
        this.status = status
        this.timestamp = timestamp
        this.latitude = latitude
        this.longitude = longitude
        this.favourite = favourite
    }


}