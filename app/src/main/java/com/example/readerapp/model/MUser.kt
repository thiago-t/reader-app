package com.example.readerapp.model

class MUser(
    val id: String?,
    val userId: String,
    val displayName: String,
    val avatarUrl: String,
    val quote: String,
    val profession: String
) {

    fun toMap(): MutableMap<String, String?> {
        return mutableMapOf(
            "user_id" to this.userId,
            "display_name" to this.displayName,
            "quote" to this.quote,
            "profession" to this.profession,
            "avatar_url" to this.avatarUrl
        )
    }

}