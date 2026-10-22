package com.swmansion.enriched.markdown.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.swmansion.enriched.markdown.renderer.BlockStyle
import com.swmansion.enriched.markdown.renderer.SpanStyleCache

/** Monospace span whose foreground comes from the extension, not the built-in code color. */
class CustomCodeSpan(
  private val styleCache: SpanStyleCache,
  private val blockStyle: BlockStyle,
  private val color: Int,
) : MetricAffectingSpan() {
  override fun updateDrawState(tp: TextPaint) {
    applyMonospacedFont(tp)
    tp.color = color
  }

  override fun updateMeasureState(tp: TextPaint) {
    applyMonospacedFont(tp)
  }

  private fun applyMonospacedFont(paint: TextPaint) {
    paint.textSize = if (styleCache.codeFontSize > 0) styleCache.codeFontSize else blockStyle.fontSize
    val preservedStyle = (paint.typeface?.style ?: 0) and (Typeface.BOLD or Typeface.ITALIC)
    paint.typeface =
      if (styleCache.codeFontFamily.isNotEmpty()) {
        styleCache.getTypeface(styleCache.codeFontFamily, preservedStyle)
      } else {
        SpanStyleCache.getMonospaceTypeface(preservedStyle)
      }
  }
}
