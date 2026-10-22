package com.swmansion.enriched.markdown.parser

/**
 * An inline mark supplied by the `extensions` prop. [delimiter] wraps the mark
 * on both sides (`%text%` when the app sets delimiter to `%`). The library does
 * not define any concrete mark; the app chooses the name, delimiter, and colors.
 *
 * Backtick code is left as [MarkdownASTNode.NodeType.Code].
 */
data class InlineExtension(
  val name: String,
  val delimiter: String = "",
  val color: String = "",
  val backgroundColor: String = "",
  /** Fully qualified Android class compiled into the app. Empty when the app registers the renderer itself. */
  val androidClassName: String = "",
)

fun MarkdownASTNode.withInlineExtensions(extensions: List<InlineExtension>): MarkdownASTNode {
  val active = extensions.filter { it.name.isNotEmpty() && it.delimiter.isNotEmpty() }
  if (active.isEmpty()) return this
  return rewriteInlineExtensions(active)
}

private fun MarkdownASTNode.rewriteInlineExtensions(extensions: List<InlineExtension>): MarkdownASTNode {
  if (type == MarkdownASTNode.NodeType.Code || type == MarkdownASTNode.NodeType.CodeBlock) return this
  if (children.isEmpty()) return this

  val rewritten =
    children.flatMap { child ->
      if (child.type == MarkdownASTNode.NodeType.Text) {
        splitTextNode(child, extensions)
      } else {
        listOf(child.rewriteInlineExtensions(extensions))
      }
    }
  return if (rewritten == children) this else copy(children = rewritten)
}

private fun splitTextNode(
  node: MarkdownASTNode,
  extensions: List<InlineExtension>,
): List<MarkdownASTNode> {
  val text = node.content
  if (text.isEmpty()) return listOf(node)

  val pieces = mutableListOf<MarkdownASTNode>()
  var index = 0
  while (index < text.length) {
    val match = earliestMatch(text, index, extensions)
    if (match == null) {
      pieces.add(node.copy(content = text.substring(index)))
      break
    }
    if (match.start > index) {
      pieces.add(node.copy(content = text.substring(index, match.start)))
    }
    val attributes = linkedMapOf("name" to match.extension.name, "delimiter" to match.extension.delimiter)
    if (match.extension.color.isNotEmpty()) attributes["color"] = match.extension.color
    if (match.extension.backgroundColor.isNotEmpty()) attributes["backgroundColor"] = match.extension.backgroundColor
    pieces.add(
      MarkdownASTNode(
        type = MarkdownASTNode.NodeType.Extension,
        attributes = attributes,
        children = listOf(MarkdownASTNode(MarkdownASTNode.NodeType.Text, content = match.inner)),
      ),
    )
    index = match.end
  }
  return pieces
}

private data class DelimiterMatch(
  val extension: InlineExtension,
  val start: Int,
  val end: Int,
  val inner: String,
)

private fun earliestMatch(
  text: String,
  from: Int,
  extensions: List<InlineExtension>,
): DelimiterMatch? {
  var best: DelimiterMatch? = null
  for (extension in extensions) {
    val match = findMatch(text, from, extension) ?: continue
    val current = best
    if (
      current == null ||
      match.start < current.start ||
      (match.start == current.start && extension.delimiter.length > current.extension.delimiter.length)
    ) {
      best = match
    }
  }
  return best
}

private fun findMatch(
  text: String,
  from: Int,
  extension: InlineExtension,
): DelimiterMatch? {
  val delimiter = extension.delimiter
  var search = from
  while (search < text.length) {
    val open = text.indexOf(delimiter, search)
    if (open < 0) return null
    if (!isBoundaryBefore(text, open)) {
      search = open + 1
      continue
    }
    val contentStart = open + delimiter.length
    var closeSearch = contentStart
    var found: DelimiterMatch? = null
    while (closeSearch < text.length) {
      val close = text.indexOf(delimiter, closeSearch)
      if (close < 0) break
      val end = close + delimiter.length
      val newline = text.indexOf('\n', contentStart)
      if (close == contentStart || (newline in contentStart until close) || !isBoundaryAfter(text, end)) {
        closeSearch = close + 1
        continue
      }
      found = DelimiterMatch(extension, open, end, text.substring(contentStart, close))
      break
    }
    if (found != null) return found
    search = open + 1
  }
  return null
}

private fun isBoundaryBefore(
  text: String,
  index: Int,
): Boolean = index == 0 || !text[index - 1].isLetterOrDigit()

private fun isBoundaryAfter(
  text: String,
  index: Int,
): Boolean = index >= text.length || !text[index].isLetterOrDigit()
