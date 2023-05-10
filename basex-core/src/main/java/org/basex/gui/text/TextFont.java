package org.basex.gui.text;

import static java.awt.Font.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Current font with different fallbacks and styles.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class TextFont {
  /** Default variable fonts. */
  private static final StringList VARS = new StringList();
  /** Default monospaced fonts. */
  private static final StringList MONO = new StringList();
  /** Names of fallback fonts for already visited codepoints. */
  private static final IntObjMap<String> FALLBACK = new IntObjMap<>();
  /** Fonts sorted in descending order by number of glyphs. */
  private static List<Font> cachedFonts;

  /** Cached fallback fonts. */
  private final Map<String, FontBox> fallbacks = new LinkedHashMap<>();
  /** Component. */
  private final JComponent comp;
  /** Font container. */
  private final FontBox font;

  /** Current style. */
  private int style;

  static {
    final Set<String> set = new HashSet<>(Arrays.asList(GUIConstants.FONTS));
    final BiConsumer<StringList, String[]> add = (list, fonts) -> {
      for(final String font : fonts) {
        if(set.contains(font)) list.add(font);
      }
    };
    add.accept(VARS, new String[] { "Noto Sans", "DejaVu Sans", "Arial Unicode MS", SANS_SERIF });
    add.accept(MONO, new String[] { "Noto Mono", "DejaVu Sans Mono", MONOSPACED });
    MONO.add(VARS);
  }

  /**
   * Constructor.
   * @param font font
   * @param comp component
   */
  TextFont(final Font font, final JComponent comp) {
    this.comp = comp;
    this.font = new FontBox(font);
    final StringList fonts = GUIConstants.isMono(this.font.plainMetrics) ? MONO : VARS;
    for(final String name : fonts) new FontBox(name);
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
    final FontBox box = i == -1 ? font : fallback(string.codePointAt(i));
    return style == 0 ? box.plain : box.bold;
  }

  /**
   * Returns a fallback font for the specified codepoint.
   * @param cp codepoint
   * @return font
   */
  private FontBox fallback(final int cp) {
    // check if a fallback has already been registered
    final String fb = FALLBACK.get(cp);
    if(fb != null) {
      final FontBox box = fallbacks.get(fb);
      return box != null ? box : new FontBox(fb);
    }

    // check for codepoint in existing fallback fonts
    for(final Map.Entry<String, FontBox> entry : fallbacks.entrySet()) {
      final FontBox box = entry.getValue();
      if(box.plain.canDisplay(cp)) {
        FALLBACK.put(cp, entry.getKey());
        return box;
      }
    }

    // find new font (first call: sort fonts by number of glyphs)
    if(cachedFonts == null) {
      final Map<Font, Integer> map = new HashMap<>(GUIConstants.FONTS.length);
      for(final String name : GUIConstants.FONTS) {
        final Font f = newFont(name);
        map.put(f, f.getNumGlyphs());
      }
      cachedFonts = map.entrySet().stream().
          sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).
          map(Map.Entry::getKey).
          collect(Collectors.toUnmodifiableList());
    }
    for(final Font f : cachedFonts) {
      if(f.canDisplay(cp)) {
        final String nm = f.getName();
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
