import type { MarkdownExtension } from 'react-native-enriched-markdown';

/**
 * Inline mark wrapped in `%`, drawn by CustomCodeRenderer in `src/android`.
 * Backtick code is unchanged.
 */
export const CustomCode: MarkdownExtension = {
  name: 'customCode',
  delimiter: '%',
  color: '#c2410c',
  backgroundColor: '#ffedd5',
  androidClassName:
    'com.swmansion.enriched.markdown.renderer.CustomCodeRenderer',
};
