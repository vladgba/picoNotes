package x.vladgba.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

/** Home-screen widget showing one note. Tap → open that note in the editor. */
class NoteWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(ctx, mgr, it) }
    }

    override fun onDeleted(ctx: Context, ids: IntArray) {
        val e = prefs(ctx).edit()
        ids.forEach { e.remove(key(it)) }
        e.apply()
    }

    companion object {
        private const val PREFS = "widgets"
        private fun key(widgetId: Int) = "w$widgetId"
        private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        fun bind(ctx: Context, widgetId: Int, noteId: Long) {
            prefs(ctx).edit().putLong(key(widgetId), noteId).apply()
        }

        fun updateAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            mgr.getAppWidgetIds(ComponentName(ctx, NoteWidget::class.java))
                .forEach { update(ctx, mgr, it) }
        }

        fun update(ctx: Context, mgr: AppWidgetManager, widgetId: Int) {
            val noteId = prefs(ctx).getLong(key(widgetId), 0L)
            if (noteId == 0L) return // not configured yet
            val note = NoteStore.get(ctx, noteId)
            val views = RemoteViews(ctx.packageName, R.layout.widget_note)

            val click: Intent
            if (note != null) {
                views.setTextViewText(R.id.w_title, note.title.ifEmpty { ctx.getString(R.string.untitled) })
                val body = note.text.lines().dropWhile { it.isBlank() }.drop(1).joinToString("\n").trim()
                views.setTextViewText(R.id.w_body, body)
                views.setViewVisibility(R.id.w_body, if (body.isEmpty()) View.GONE else View.VISIBLE)
                click = EditActivity.intent(ctx, note.id)
            } else {
                views.setTextViewText(R.id.w_title, ctx.getString(R.string.widget_note_gone))
                views.setViewVisibility(R.id.w_body, View.GONE)
                click = Intent(ctx, MainActivity::class.java)
            }
            click.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            val pi = PendingIntent.getActivity(
                ctx, widgetId, click,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.w_root, pi)
            mgr.updateAppWidget(widgetId, views)
        }
    }
}
