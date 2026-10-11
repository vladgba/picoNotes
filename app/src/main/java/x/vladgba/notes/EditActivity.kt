package x.vladgba.notes

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText

class EditActivity : Activity() {

    private lateinit var editor: EditText
    private var noteId = 0L
    private var deleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        applySystemInsets(findViewById(R.id.root))

        noteId = savedInstanceState?.getLong(EXTRA_ID) ?: intent.getLongExtra(EXTRA_ID, 0L)
        editor = findViewById(R.id.editor)
        if (savedInstanceState == null) {
            NoteStore.get(this, noteId)?.let {
                editor.setText(it.text)
                editor.setSelection(it.text.length)
            }
        }

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.delete).setOnClickListener { confirmDelete() }
    }

    override fun onPause() {
        super.onPause()
        if (!deleted) save()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(EXTRA_ID, noteId)
    }

    private fun save() {
        val text = editor.text.toString()
        val exists = NoteStore.get(this, noteId) != null
        if (!exists && text.isBlank()) return // don't create empty notes
        noteId = NoteStore.save(this, noteId, text).id
        NoteWidget.updateAll(this)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_note_q)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleted = true
                NoteStore.delete(this, noteId)
                NoteWidget.updateAll(this)
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_ID = "note_id"

        /** [id] = 0 creates a new note. */
        fun intent(ctx: Context, id: Long): Intent =
            Intent(ctx, EditActivity::class.java).putExtra(EXTRA_ID, id)
    }
}
