package com.example.fashionshoppingadmin

class ProductModel {
    var productName: String= ""
    var price: Double= -1.0
    var desc: String= ""
    var imageUrl: String= ""

    constructor(productName: String, price: Double, desc: String, imageUrl: String) {
        this.productName = productName
        this.price = price
        this.desc = desc
        this.imageUrl = imageUrl
    }

    constructor()
}