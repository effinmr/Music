package code.name.monkey.retromusic.api

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    val id: Long,
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val name: String?,
    val prerelease: Boolean,
    val draft: Boolean,
    val body: String?
)
