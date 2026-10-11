package x.vladgba.notes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Tiny JSON-file storage. All notes are kept in memory; the file is
 * rewritten atomically on every change. Good for hundreds of notes.
 */
object NoteStore {
    private const val FILE = "notes.json"
    private var cache: MutableList<Note>? = null

    @Synchronized
    private fun notes(ctx: Context): MutableList<Note> {
        cache?.let { return it }
        val file = File(ctx.filesDir, FILE)
        val list = mutableListOf<Note>()
        if (file.exists()) {
            runCatching {
                val arr = JSONArray(file.readText())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list += Note(o.getLong("id"), o.getString("text"), o.getLong("updated"))
                }
            }
        }
        cache = list
        return list
    }

    @Synchronized
    private fun persist(ctx: Context) {
        val arr = JSONArray()
        notes(ctx).forEach {
            arr.put(JSONObject().put("id", it.id).put("text", it.text).put("updated", it.updated))
        }
        val tmp = File(ctx.filesDir, "$FILE.tmp")
        tmp.writeText(arr.toString())
        tmp.renameTo(File(ctx.filesDir, FILE))
    }

    /** Newest first. */
    @Synchronized
    fun all(ctx: Context): List<Note> = notes(ctx).sortedByDescending { it.updated }

    @Synchronized
    fun get(ctx: Context, id: Long): Note? = notes(ctx).firstOrNull { it.id == id }

    /** Inserts or updates. Returns the saved note. */
    @Synchronized
    fun save(ctx: Context, id: Long, text: String): Note {
        val list = notes(ctx)
        val now = System.currentTimeMillis()
        val idx = list.indexOfFirst { it.id == id }
        val note: Note
        if (idx >= 0) {
            if (list[idx].text == text) return list[idx]
            note = list[idx].copy(text = text, updated = now)
            list[idx] = note
        } else {
            val newId = if (id > 0) id else (list.maxOfOrNull { it.id } ?: 0L) + 1
            note = Note(newId, text, now)
            list += note
        }
        persist(ctx)
        return note
    }

    @Synchronized
    fun delete(ctx: Context, id: Long) {
        if (notes(ctx).removeAll { it.id == id }) persist(ctx)
    }
}
