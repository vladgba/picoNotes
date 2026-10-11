package x.vladgba.notes

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var adapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemInsets(findViewById(R.id.root))

        adapter = NotesAdapter()
        val list = findViewById<ListView>(R.id.list)
        list.adapter = adapter
        list.emptyView = findViewById(R.id.empty)

        list.setOnItemClickListener { _, _, pos, _ ->
            startActivity(EditActivity.intent(this, adapter.getItem(pos).id))
        }
        list.setOnItemLongClickListener { _, _, pos, _ ->
            confirmDelete(adapter.getItem(pos))
            true
        }
        findViewById<View>(R.id.add).setOnClickListener {
            startActivity(EditActivity.intent(this, 0L))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.reload()
    }

    private fun confirmDelete(note: Note) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_note_q)
            .setMessage(note.title.ifEmpty { getString(R.string.untitled) })
            .setPositiveButton(R.string.delete) { _, _ ->
                NoteStore.delete(this, note.id)
                NoteWidget.updateAll(this)
                adapter.reload()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private inner class NotesAdapter : BaseAdapter() {
        private var items: List<Note> = emptyList()

        fun reload() {
            items = NoteStore.all(this@MainActivity)
            notifyDataSetChanged()
        }

        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = items[position].id

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: layoutInflater.inflate(R.layout.item_note, parent, false)
            val note = items[position]
            v.findViewById<TextView>(R.id.title).text = note.title.ifEmpty { getString(R.string.untitled) }
            v.findViewById<TextView>(R.id.preview).apply {
                text = note.preview
                visibility = if (note.preview.isEmpty()) View.GONE else View.VISIBLE
            }
            v.findViewById<TextView>(R.id.date).text = DateUtils.getRelativeTimeSpanString(
                note.updated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
            )
            return v
        }
    }

}
