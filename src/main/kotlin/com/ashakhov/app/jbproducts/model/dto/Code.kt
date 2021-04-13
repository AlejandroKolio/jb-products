package com.ashakhov.app.jbproducts.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Code(
        @JsonProperty("type") val type: String,
        @JsonProperty("build") val build: String,
        @JsonProperty("version") val version: String,
        @JsonProperty("downloads") val downloads: Downloads)

data class Downloads(@JsonProperty("linux") val linux: Linux?)

data class Linux(@JsonProperty("link") val link: String,
                 @JsonProperty("size") val size: Long,
                 @JsonProperty("checksumLink") val checksumLink: String)
