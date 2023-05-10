package org.basex.gui.text;

import static java.awt.Font.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.basex.gui.layout.*;
import org.basex.util.hash.*;

/**
 * Current font with different fallbacks and styles.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class TextFont {
  /** Fallback fonts for visited codepoints. */
  private static final IntObjMap<String> FALLBACK = new IntObjMap<>();
  /** Cached fonts for looking up codepoints. */
  private static final Font[] ALLFONTS = new Font[BaseXLayout.FONTS.length];

  /** Fallback fonts. */
  private final Map<String, FontBox> fallbacks = new LinkedHashMap<>();
  /** Component. */
  private final JComponent comp;
  /** Font container. */
  private final FontBox font;

  /** Current style. */
  private int style;

  /**
   * Constructor.
   * @param font font
   * @param comp component
   */
  TextFont(final Font font, final JComponent comp) {
    this.comp = comp;
    this.font = new FontBox(font);
    new FontBox(BaseXLayout.isMono(this.font.plainMetrics) ? MONOSPACED : SANS_SERIF);
  }

  /**
   * Assigns a style and returns the font.
   * @param s style
   */
  void assign(final int s) {
    style = s;
  }

  /**
   * Returns the font size.
   * @return size
   */
  int size() {
    return font.plain.getSize();
  }

  /**
   * Returns the pixel width of the specified string.
   * @param string string
   * @return width
   */
  int stringWidth(final String string) {
    final int i = font().canDisplayUpTo(string);
    final FontBox box = i == -1 ? font : fallback(string.charAt(i));
    return (style == 0 ? box.plainMetrics : box.boldMetrics).stringWidth(string);
  }

  /**
   * Returns the pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  int charWidth(final int cp) {
    final FontBox box = font().canDisplay(cp) ? font : fallback(cp);
    return (style == 0 ? box.plainMetrics : box.boldMetrics).charWidth(cp);
  }

  /**
   * Returns an appropriate font for the specified string.
   * @param string string
   * @return font
   */
  Font font(final String string) {
    final int i = font().canDisplayUpTo(string);
    final FontBox box = i == -1 ? font : fallback(string.charAt(i));
    return style == 0 ? box.plain : box.bold;
  }

  /**
   * Returns a fallback font for the specified codepoint.
   * @param cp codepoint
   * @return font
   */
  private FontBox fallback(final int cp) {
    // check if a fallback is known for the specified codepoint
    final String fb = FALLBACK.get(cp);
    if(fb != null) {
      final FontBox box = fallbacks.get(fb);
      return box != null ? box : new FontBox(fb);
    }

    // check existing fallback fonts
    for(final Map.Entry<String, FontBox> entry : fallbacks.entrySet()) {
      final FontBox box = entry.getValue();
      if(box.plain.canDisplay(cp)) {
        FALLBACK.put(cp, entry.getKey());
        return box;
      }
    }

    // find new font
    final int fl = BaseXLayout.FONTS.length;
    for(int f = 0; f < fl; f++) {
      final String nm = BaseXLayout.FONTS[f];
      Font fn = ALLFONTS[f];
      if(fn == null) {
        fn = newFont(nm);
        ALLFONTS[f] = fn;
      }
      if(fn.canDisplay(cp)) {
        FALLBACK.put(cp, nm);
        return new FontBox(nm);
      }
    }

    // no font found: use standard font
    FALLBACK.put(cp, font.plain.getName());
    return font;
  }

  /**
   * Returns the current font.
   * @return font
   */
  private Font font() {
    return style == 0 ? font.plain : font.bold;
  }

  /**
   * Creates a new font.
   * @param nm name of font
   * @return font
   */
  private Font newFont(final String nm) {
    return new Font(nm, PLAIN, size());
  }

  /**
   * Fonts and font metrics.
   */
  private final class FontBox {
    /** Plain font. */
    private final Font plain;
    /** Bold font. */
    private final Font bold;
    /** Plain font metrics. */
    private final FontMetrics plainMetrics;
    /** Bold font metrics. */
    private final FontMetrics boldMetrics;

    /**
     * Constructor.
     * @param name name of font
     */
    private FontBox(final String name) {
      this(newFont(name));
    }

    /**
     * Constructor.
     * @param plain plain font
     */
    private FontBox(final Font plain) {
      this.plain = plain;
      bold = plain.deriveFont(BOLD);
      plainMetrics = comp.getFontMetrics(plain);
      boldMetrics = comp.getFontMetrics(bold);
      fallbacks.put(plain.getName(), this);
    }
  }
}
