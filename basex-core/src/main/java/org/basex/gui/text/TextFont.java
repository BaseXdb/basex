package org.basex.gui.text;

import static java.awt.Font.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.util.*;
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
  private final Map<String, FontFamily> fallbacks = new LinkedHashMap<>();
  /** Component. */
  private final JComponent comp;
  /** Font family. */
  private final FontFamily family;
  /** Tab indentation. */
  private final int indent;
  /** Font size. */
  private int size;

  /** Current style. */
  private int style;

  static {
    final Set<String> set = new HashSet<>(Arrays.asList(GUIConstants.fonts()));
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
   * @param indent indentation
   */
  TextFont(final Font font, final int indent, final JComponent comp) {
    this.comp = comp;
    this.indent = indent;
    family = new FontFamily(font, comp);
    size = font.getSize();
  }

  /**
   * Assigns a style.
   * @param s style
   */
  void style(final int s) {
    style = s;
  }

  /**
   * Returns the font size.
   * @return font size
   */
  int size() {
    return size;
  }

  /**
   * Returns the pixel width of the specified string.
   * @param string string
   * @return width
   */
  int stringWidth(final String string) {
    return string.length() == 1 ? charWidth(string.codePointAt(0)) :
      family(string).metrics(style).stringWidth(string);
  }

  /**
   * Returns an appropriate font for the specified string.
   * @param string string
   * @return font
   */
  Font font(final String string) {
    return family(string).font(style);
  }

  /**
   * Returns an font family for the specified string.
   * @param string string
   * @return font family
   */
  private FontFamily family(final String string) {
    final int i = family.font(style).canDisplayUpTo(string);
    return i == -1 ? family : fallback(string.codePointAt(i));
  }

  /**
   * Returns the pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  int charWidth(final int cp) {
    if(cp >= TokenBuilder.PRIVATE_START && cp <= TokenBuilder.PRIVATE_END) return 0;

    final FontFamily ff = family.font(style).canDisplay(cp) ? family : fallback(cp);
    final FontMetrics fm = ff.metrics(style);
    return cp == '\t' ? fm.charWidth(' ') * indent : fm.charWidth(cp);
  }

  /**
   * Returns a fallback font family for the specified codepoint.
   * @param cp codepoint
   * @return font family
   */
  private FontFamily fallback(final int cp) {
    if(fallbacks.isEmpty()) {
      final StringList fonts = GUIConstants.isMono(family.metrics(PLAIN)) ? MONO : VARS;
      for(final String name : fonts) fallback(name);
    }

    // check if a fallback has already been registered
    final String fb = FALLBACK.get(cp);
    if(fb != null) {
      final FontFamily ff = fallbacks.get(fb);
      return ff != null ? ff : fallback(fb);
    }

    // check for codepoint in existing fallback fonts
    for(final Map.Entry<String, FontFamily> entry : fallbacks.entrySet()) {
      final FontFamily ff = entry.getValue();
      if(ff.font(PLAIN).canDisplay(cp)) {
        FALLBACK.put(cp, entry.getKey());
        return ff;
      }
    }

    // find new font (first call: sort fonts by number of glyphs)
    if(cachedFonts == null) {
      final String[] names = GUIConstants.fonts();
      final Map<Font, Integer> map = new HashMap<>(names.length);
      for(final String name : names) {
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
        return fallback(nm);
      }
    }

    // no font found: use standard font
    FALLBACK.put(cp, family.font(PLAIN).getName());
    return family;
  }

  /**
   * Registers a fallback font family.
   * @param name name of font
   * @return font family
   */
  private FontFamily fallback(final String name) {
    final FontFamily ff = new FontFamily(newFont(name), comp);
    fallbacks.put(ff.font(PLAIN).getName(), ff);
    return ff;
  }

  /**
   * Creates a new font.
   * @param nm name of font
   * @return font
   */
  private Font newFont(final String nm) {
    return new Font(nm, PLAIN, size);
  }

  /** Fonts (plain and bold) and metrics. */
  private static final class FontFamily {
    /** Fonts (plain, bold). */
    private final Font[] fonts;
    /** Font metrics (plain, bold). */
    private final FontMetrics[] metrics;

    /**
     * Constructor.
     * @param font font
     * @param comp component
     */
    private FontFamily(final Font font, final JComponent comp) {
      final Font bold = font.deriveFont(BOLD);
      fonts = new Font[] { font, bold };
      metrics = new FontMetrics[] { comp.getFontMetrics(font), comp.getFontMetrics(bold) };
    }

    /**
     * Returns the font for the specified style.
     * @param style style
     * @return font
     */
    private Font font(final int style) {
      return fonts[style];
    }

    /**
     * Returns the font metrics for the specified style.
     * @param style style
     * @return font metrics
     */
    private FontMetrics metrics(final int style) {
      return metrics[style];
    }
  }
}
