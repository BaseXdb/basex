package org.basex.gui.text;

import static org.basex.util.Token.*;

/**
 * Maps the char offsets of a decoded text to the byte offsets of its UTF-8 encoding.
 * The offsets must be requested in ascending order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TextCursor {
  /** Text. */
  private final byte[] text;
  /** Char offset. */
  private int chr;
  /** Byte offset. */
  private int pos;

  /**
   * Constructor.
   * @param text text
   */
  TextCursor(final byte[] text) {
    this.text = text;
  }

  /**
   * Advances the cursor to the specified char offset.
   * @param chars char offset
   * @return byte offset
   */
  int advance(final int chars) {
    while(chr < chars) {
      // a 4-byte UTF-8 sequence is a supplementary code point, i.e. two UTF-16 chars
      final int bl = cl(text, pos);
      pos += bl;
      chr += bl == 4 ? 2 : 1;
    }
    return pos;
  }
}
