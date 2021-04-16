package com.ashakhov.app.jbproducts.model

enum class Status(val version: String) {
    EAP("eap"),
    RELEASE("release"),
    DOWNLOADING("downloading"),
    COMPLETE("complete"),
    PENDING("pending")
}
