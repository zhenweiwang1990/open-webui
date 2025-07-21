package ai.gbox.chatdroid.ui.utils

import android.content.Context
import android.widget.TextView
import io.noties.markwon.Markwon

object MarkdownRenderer {
    
    private var markwon: Markwon? = null
    
    fun getInstance(context: Context): Markwon {
        if (markwon == null) {
            markwon = Markwon.create(context)
        }
        return markwon!!
    }
    
    /**
     * Render markdown text into a TextView
     */
    fun renderMarkdown(textView: TextView, markdown: String) {
        val markwon = getInstance(textView.context)
        markwon.setMarkdown(textView, markdown)
    }
    
    /**
     * Parse markdown to styled text without setting it to a TextView
     */
    fun parseMarkdown(context: Context, markdown: String): CharSequence {
        val markwon = getInstance(context)
        return markwon.toMarkdown(markdown)
    }
}