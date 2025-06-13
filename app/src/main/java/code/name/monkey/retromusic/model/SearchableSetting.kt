package code.name.monkey.retromusic.model

data class SearchableSetting(
    val id: Int, // R.id of the parent SettingListItemView (category)
    val title: String,
    val summary: String?,
    val isCategory: Boolean = false, // True if this represents a main category header
    val navigationAction: Int? = null // New: R.id of the fragment to navigate to
)
