package ru.mpei.domain_news.dto

data class NewsItem(
        var id: String = "",
        var name: String = "",
        var chislo: String = "",
        var month: String = "",
        var hour: String = "",
        var content: String = "",
        // @SerializedName("image_src")
        var imageUrl: String = ""
)