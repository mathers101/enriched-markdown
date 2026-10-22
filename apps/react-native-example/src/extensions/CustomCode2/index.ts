import type { MarkdownExtension } from 'react-native-enriched-markdown';

/**
 * Inline mark wrapped in `%`, drawn by CustomCodeRenderer in `src/android`.
 * Backtick code is unchanged.
 */
export const CustomCode2: MarkdownExtension = {
  name: 'customCode2',
  delimiter: '%%',
  color: '#285704',
  backgroundColor: '#85d14b',
  androidClassName:
    'com.swmansion.enriched.markdown.renderer.CustomCode2Renderer',
};
