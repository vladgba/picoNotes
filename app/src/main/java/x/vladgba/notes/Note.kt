package x.vladgba.notes

data class Note(
    val id: Long,
    val text: String,
    val updated: Long,
) {
    /** First non-blank line, used as the title. */
    val title: String
        get() = text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: ""

    /** Everything after the title line, collapsed to one line. */
    val preview: String
        get() {
            val lines = text.lines()
            val idx = lines.indexOfFirst { it.isNotBlank() }
            if (idx < 0) return ""
            return lines.drop(idx + 1).joinToString(" ") { it.trim() }.trim()
        }
}
