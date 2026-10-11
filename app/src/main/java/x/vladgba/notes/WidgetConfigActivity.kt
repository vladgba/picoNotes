package x.vladgba.notes

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

/** Shown when a widget is placed: pick which note it shows (or make a new one). */
class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContentView(R.layout.activity_widget_config)
        applySystemInsets(findViewById(R.id.root))

        val notes = NoteStore.all(this)
        val labels = listOf(getString(R.string.new_note)) +
            notes.map { it.title.ifEmpty { getString(R.string.untitled) } }

        val list = findViewById<ListView>(R.id.list)
        list.adapter = ArrayAdapter(this, R.layout.item_simple, labels)
        list.setOnItemClickListener { _, _, pos, _ ->
            if (pos == 0) {
                val note = NoteStore.save(this, 0L, "")
                finishWith(note.id, openEditor = true)
            } else {
                finishWith(notes[pos - 1].id, openEditor = false)
            }
        }
    }

    private fun finishWith(noteId: Long, openEditor: Boolean) {
        NoteWidget.bind(this, widgetId, noteId)
        NoteWidget.update(this, AppWidgetManager.getInstance(this), widgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (openEditor) startActivity(EditActivity.intent(this, noteId))
        finish()
    }
}
