package com.swmansion.enriched.markdown.renderer

import android.graphics.Color
import android.text.SpannableStringBuilder
import com.swmansion.enriched.markdown.parser.MarkdownASTNode
import com.swmansion.enriched.markdown.spans.CustomCodeBackgroundSpan
import com.swmansion.enriched.markdown.spans.CustomCodeSpan
import com.swmansion.enriched.markdown.utils.text.span.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class CustomCodeRenderer(
  private val config: RendererConfig,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    onLinkLongPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    if (node.children.isEmpty() || node.children.all { it.content.isEmpty() }) return

    val textColor = node.getAttribute("color").toColorOrNull()
    val backgroundColor = node.getAttribute("backgroundColor").toColorOrNull()

    factory.renderWithSpan(builder, { node.children.forEach { builder.append(it.content) } }) { start, end, blockStyle ->
      if (textColor != null) {
        builder.setSpan(
          CustomCodeSpan(factory.styleCache, blockStyle, textColor),
          start,
          end,
          SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
        )
      }
      builder.setSpan(
        CustomCodeBackgroundSpan(config.style, backgroundColor),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}

private fun String?.toColorOrNull(): Int? {
  if (this.isNullOrEmpty()) return null
  return try {
    Color.parseColor(this)
  } catch (_: IllegalArgumentException) {
    null
  }
}
