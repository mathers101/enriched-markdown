package com.swmansion.enriched.markdown.renderer

import android.util.Log
import com.swmansion.enriched.markdown.parser.InlineExtension

/**
 * App-registered node renderers. A view uses one only when its `extensions`
 * prop names it. The class must already be compiled into the app. Pass
 * [InlineExtension.androidClassName] and the view loads it; [register] remains
 * for an app that wants to install the renderer itself.
 *
 * A name equal to a node type replaces that renderer. Any other name is an
 * extra inline mark, created when the extension's delimiter matches.
 */
object RendererRegistry {
  private val factories = mutableMapOf<String, (RendererConfig) -> NodeRenderer>()
  private val loadedClasses = mutableMapOf<String, String>()

  fun register(
    name: String,
    factory: (RendererConfig) -> NodeRenderer,
  ) {
    factories[name] = factory
  }

  /** Loads renderers named by [InlineExtension.androidClassName] through the app class loader. */
  fun load(
    extensions: List<InlineExtension>,
    classLoader: ClassLoader,
  ) {
    for (extension in extensions) {
      val className = extension.androidClassName
      if (className.isEmpty() || loadedClasses[extension.name] == className) continue
      try {
        val constructor =
          classLoader.loadClass(className).getConstructor(RendererConfig::class.java)
        register(extension.name) { config -> constructor.newInstance(config) as NodeRenderer }
        loadedClasses[extension.name] = className
      } catch (error: Exception) {
        Log.e(TAG, "Failed to load extension renderer $className", error)
      }
    }
  }

  fun create(
    name: String,
    config: RendererConfig,
  ): NodeRenderer? =
    factories.entries
      .firstOrNull { it.key.equals(name, ignoreCase = true) }
      ?.value
      ?.invoke(config)

  private const val TAG = "RendererRegistry"
}
