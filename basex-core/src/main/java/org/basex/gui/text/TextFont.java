package org.basex.gui.text;

import static java.awt.Font.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Current font with different fallbacks and styles.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TextFont {
  /** Default variable fonts. */
  private static final StringList VARS = new StringList();
  /** Default monospaced fonts. */
  private static final StringList MONO = new StringList();
  /** Names of fallback fonts for already visited codepoints. */
  private static final IntObjectMap<String> FALLBACK = new IntObjectMap<>();
  /** Fonts sorted in descending order by number of glyphs. */
  private static List<Font> cachedFonts;

  /** Cached fallback fonts. */
  private final Map<String, FontFamily> fallbacks = new LinkedHashMap<>();
  /** Cached widths of ASCII characters (plain, bold). */
  private final int[][] widths = { new int[128], new int[128] };
  /** Cached unsnapped widths of ASCII characters (plain, bold). */
  private final int[][] raws = { new int[128], new int[128] };
  /** Cached font families of ASCII characters (plain, bold). */
  private final FontFamily[][] families = { new FontFamily[128], new FontFamily[128] };
  /** Component. */
  private final JComponent comp;
  /** Font family. */
  private final FontFamily family;
  /** Tab indentation. */
  private final int indent;
  /** Font size. */
  private final int size;
  /** Width of a character cell (base for monospaced grid alignment). */
  private final int cell;
  /** Monospaced flag: snap glyphs to the character grid. */
  private final boolean mono;

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
    // character cell width for grid alignment (0 for proportional fonts)
    final FontMetrics fm = family.metrics(PLAIN);
    cell = GUIConstants.monoWidth(fm);
    mono = cell > 0;
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
    // single character (e.g. tab): use its cell width
    final int sl = string.length();
    if(sl == 1) return charWidth(string.codePointAt(0));
    // proportional: native width
    if(!mono) return family(string).metrics(style).stringWidth(string);
    // monospaced: sum per-glyph cells (matches grid drawing)
    int width = 0;
    for(int s = 0; s < sl;) {
      final int cp = string.codePointAt(s);
      width += charWidth(cp);
      s += Character.charCount(cp);
    }
    return width;
  }

  /**
   * Returns the pixel width of the specified text range.
   * @param text text
   * @param start start position
   * @param end end position
   * @return width
   */
  int stringWidth(final byte[] text, final int start, final int end) {
    // proportional: the width of a string is not the sum of its glyph widths
    if(!mono) return stringWidth(Token.string(text, start, end - start));
    int width = 0;
    for(int p = start; p < end;) {
      width += charWidth(Token.cp(text, p));
      p += Token.cl(text, p);
    }
    return width;
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
   * Returns a font family for the specified string.
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
    if(cp >= 128) return width(cp);
    final int[] cache = widths[style];
    int width = cache[cp];
    if(width == 0) {
      width = width(cp);
      cache[cp] = width;
    }
    return width;
  }

  /**
   * Computes the pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  private int width(final int cp) {
    if(cp >= TokenBuilder.PRIVATE_START && cp <= TokenBuilder.PRIVATE_END) return 0;
    if(cp == '\t') return charWidth(' ') * indent;
    // combining marks attach to the preceding glyph and take no space of their own
    if(nonspacing(cp)) return 0;

    // snap glyphs to the character grid to keep columns aligned in monospaced fonts
    final int width = rawWidth(cp);
    return mono && width != 0 ? Math.max(1, (width + cell / 2) / cell) * cell : width;
  }

  /**
   * Checks if a codepoint is a non-spacing or enclosing combining mark.
   * @param cp codepoint
   * @return result of check
   */
  private static boolean nonspacing(final int cp) {
    final int type = Character.getType(cp);
    return type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK;
  }

  /**
   * Returns the unsnapped pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  private int rawWidth(final int cp) {
    if(cp >= 128) return family(cp).metrics(style).charWidth(cp);
    final int[] cache = raws[style];
    int width = cache[cp];
    if(width == 0) {
      width = family(cp).metrics(style).charWidth(cp);
      cache[cp] = width;
    }
    return width;
  }

  /**
   * Draws a string, aligning each character to the grid for monospaced fonts.
   * @param g graphics reference
   * @param string string to draw
   * @param x x position
   * @param y y position
   */
  void draw(final Graphics g, final String string, final int x, final int y) {
    if(!mono) {
      g.setFont(font(string));
      g.drawString(string, x, y);
      return;
    }
    // draw as many characters as possible with a single call: start of the pending run and its
    // x position ({@code -1}: no characters pending)
    final char[] chars = string.toCharArray();
    final int len = chars.length;
    int cx = x, start = -1, sx = 0;
    Font last = null;
    for(int i = 0; i < len;) {
      final int cp = string.codePointAt(i), w = charWidth(cp);
      // group the base glyph with trailing combining marks, so the font can compose them
      int end = i + Character.charCount(cp);
      while(end < len) {
        final int mcp = string.codePointAt(end);
        if(!nonspacing(mcp)) break;
        end += Character.charCount(mcp);
      }
      if(w == 0) {
        // invisible glyph: it is skipped, so the pending characters must be drawn
        if(start != -1) {
          g.drawChars(chars, start, i - start, sx, y);
          start = -1;
        }
      } else {
        final Font fnt = family(cp).font(style);
        if(fnt != last) {
          if(start != -1) {
            g.drawChars(chars, start, i - start, sx, y);
            start = -1;
          }
          g.setFont(fnt);
          last = fnt;
        }
        if(start == -1) {
          start = i;
          sx = cx;
        }
        // the glyph was snapped to the grid: the next one must be positioned explicitly
        if(w != rawWidth(cp)) {
          g.drawChars(chars, start, end - start, sx, y);
          start = -1;
        }
      }
      cx += w;
      i = end;
    }
    if(start != -1) g.drawChars(chars, start, len - start, sx, y);
  }

  /**
   * Returns a font family for the specified codepoint.
   * @param cp codepoint
   * @return font family
   */
  private FontFamily family(final int cp) {
    if(cp >= 128) return family.font(style).canDisplay(cp) ? family : fallback(cp);
    final FontFamily[] cache = families[style];
    FontFamily ff = cache[cp];
    if(ff == null) {
      ff = family.font(style).canDisplay(cp) ? family : fallback(cp);
      cache[cp] = ff;
    }
    return ff;
  }

  /**
   * Returns a fallback font family for the specified codepoint.
   * @param cp codepoint
   * @return font family
   */
  private FontFamily fallback(final int cp) {
    if(fallbacks.isEmpty()) {
      final StringList fonts = mono ? MONO : VARS;
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
          map(Map.Entry::getKey).toList();
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
