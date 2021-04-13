package com.ashakhov.app.jbproducts.model.dto

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "products")
data class ProductWrapper(
    @JacksonXmlProperty(localName = "product")
    @JacksonXmlElementWrapper(useWrapping = false)
    val products: List<Product>
)

data class Product(
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    val name: String,
    @JacksonXmlProperty(localName = "code")
    @JacksonXmlElementWrapper(useWrapping = false)
    val codes: List<String>,
    @JacksonXmlProperty(isAttribute = true, localName = "channel")
    @JacksonXmlElementWrapper(useWrapping = false)
    val channels: List<Channel>
)

data class Channel(
    val id: String,
    val name: String,
    val status: String,
    val url: String,
    val feedback: String,
    val majorVersion: String,
    val licensing: String,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(isAttribute = true, localName = "build")
    val builds: List<Build>
)

data class Build(
    val number: Double,
    val version: String,
    val fullNumber: String?,
    val releaseDate: String?,
)