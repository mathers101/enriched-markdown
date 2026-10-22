package com.swmansion.enriched.markdown.renderer

import android.text.SpannableStringBuilder
import com.swmansion.enriched.markdown.parser.MarkdownASTNode

/** Dispatches an [MarkdownASTNode.NodeType.Extension] node to the app renderer registered under its name. */
internal class ExtensionMarkRenderer(
  private val config: RendererConfig,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    onLinkLongPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val name = node.getAttribute("name")
    val renderer = name?.let { RendererRegistry.create(it, config) }
    if (renderer == null) {
      factory.renderChildren(node, builder, onLinkPress, onLinkLongPress)
      return
    }
    renderer.render(node, builder, onLinkPress, onLinkLongPress, factory)
  }
}
