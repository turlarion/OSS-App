package ru.mpei.domain_profile.dto

import java.io.Serializable

data class ProfileItem(
        var id: Int = 0,
        var hashPass: String = "",
        var name: String = "Name",
        var surname: String = "Surname",
        var email: String = "",
        var capital: Int = 0
): Serializable