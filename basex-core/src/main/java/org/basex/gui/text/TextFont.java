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
  /** Fonts sorted in descending order by number of glyphs (can be {@code null}). */
  private static List<Font> cachedFonts;

  /** Cached fallback fonts. */
  private final Map<String, FontFamily> fallbacks = new LinkedHashMap<>();
  /** Cached character widths and font families (plain, bold). */
  private final Cache[] caches = { new Cache(), new Cache() };
  /** Buffer for the characters to be drawn. */
  private char[] chars = new char[16];
  /** Component. */
  private final JComponent comp;
  /** Font family. */
  private final FontFamily family;
  /** Tab indentation. */
  private final int indent;
  /** Font size. */
  private final int size;
  /** Width of a character cell ({@code 0} if the font is not monospaced). */
  private final int cell;

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
    cell = GUIConstants.monoWidth(family.metrics(PLAIN));
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
    int width = 0;
    for(int s = 0, sl = string.length(); s < sl;) {
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
    int width = 0;
    for(int p = start; p < end;) {
      width += charWidth(Token.cp(text, p));
      p += Token.cl(text, p);
    }
    return width;
  }

  /**
   * Returns the pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  int charWidth(final int cp) {
    final Cache cache = caches[style];
    int width = cache.width(cp);
    if(width == Integer.MIN_VALUE) {
      width = width(cp);
      cache.width(cp, width);
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
    // control characters are rendered as control pictures
    if(control(cp)) return charWidth(picture(cp));
    // combining marks attach to the preceding glyph and take no space of their own
    if(nonspacing(cp)) return 0;

    // snap glyphs to the character grid to keep columns aligned in monospaced fonts
    final int width = family(cp).metrics(style).charWidth(cp);
    return cell != 0 && width != 0 ? Math.max(1, (width + cell / 2) / cell) * cell : width;
  }

  /**
   * Checks if a codepoint is a control character that is rendered as a picture.
   * @param cp codepoint
   * @return result of check
   */
  static boolean control(final int cp) {
    return cp >= 0 && (cp < ' ' && cp != '\t' && cp != '\n' && cp != '\r' ||
      cp >= 0x7F && cp <= 0x9F);
  }

  /**
   * Returns the picture for a control character.
   * @param cp codepoint
   * @return codepoint of the picture
   */
  static int picture(final int cp) {
    // C0 controls have their own pictures, DEL has a symbol, C1 controls have no representation
    return cp < ' ' ? 0x2400 + cp : cp == 0x7F ? 0x2421 : 0xFFFD;
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
   * Draws a string.
   * @param g graphics reference
   * @param string string to draw
   * @param x x position
   * @param y y position
   */
  void draw(final Graphics g, final String string, final int x, final int y) {
    final int len = string.length();
    string.getChars(0, len, chars(len), 0);
    draw(g, len, x, y);
  }

  /**
   * Draws a text range.
   * @param g graphics reference
   * @param text text
   * @param start start position
   * @param end end position
   * @param x x position
   * @param y y position
   */
  void draw(final Graphics g, final byte[] text, final int start, final int end,
      final int x, final int y) {
    int len = 0;
    for(int p = start; p < end; p += Token.cl(text, p)) {
      len += Character.toChars(Token.cp(text, p), chars(len + 2), len);
    }
    draw(g, len, x, y);
  }

  /**
   * Draws the buffered characters, positioning each of them at its computed width.
   * @param g graphics reference
   * @param len number of characters
   * @param x x position
   * @param y y position
   */
  private void draw(final Graphics g, final int len, final int x, final int y) {
    // the characters cannot be drawn with a single call: it would advance them by the fractional
    // glyph widths of the font, which drift from the rounded and snapped widths used everywhere
    final char[] chrs = chars;
    int cx = x;
    Font last = null;
    for(int c = 0; c < len;) {
      final int cp = Character.codePointAt(chrs, c, len), w = charWidth(cp);
      // group the base glyph with trailing combining marks, so the font can compose them
      int end = c + Character.charCount(cp);
      while(end < len) {
        final int mcp = Character.codePointAt(chrs, end, len);
        if(!nonspacing(mcp)) break;
        end += Character.charCount(mcp);
      }
      // invisible glyphs are skipped
      if(w != 0) {
        final Font fnt = family(cp).font(style);
        if(fnt != last) {
          g.setFont(fnt);
          last = fnt;
        }
        g.drawChars(chrs, c, end - c, cx, y);
      }
      cx += w;
      c = end;
    }
  }

  /**
   * Returns the character buffer, resized if required.
   * @param sz required size
   * @return buffer
   */
  private char[] chars(final int sz) {
    if(chars.length < sz) chars = Arrays.copyOf(chars, Array.newCapacity(sz));
    return chars;
  }

  /**
   * Returns a font family for the specified codepoint.
   * @param cp codepoint
   * @return font family
   */
  private FontFamily family(final int cp) {
    final Cache cache = caches[style];
    FontFamily ff = cache.family(cp);
    if(ff == null) {
      ff = family.font(style).canDisplay(cp) ? family : fallback(cp);
      cache.family(cp, ff);
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
      final StringList fonts = cell != 0 ? MONO : VARS;
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

  /** Cached character widths and font families. */
  private static final class Cache {
    /** Widths of ASCII characters ({@link Integer#MIN_VALUE}: not cached yet). */
    private final int[] widths = new int[128];
    /** Font families of ASCII characters. */
    private final FontFamily[] families = new FontFamily[128];
    /** Widths of other characters. */
    private final IntMap wideWidths = new IntMap();
    /** Font families of other characters. */
    private final IntObjectMap<FontFamily> wideFamilies = new IntObjectMap<>();

    /** Constructor. */
    private Cache() {
      Arrays.fill(widths, Integer.MIN_VALUE);
    }

    /**
     * Returns the cached width of a character.
     * @param cp codepoint
     * @return width, or {@link Integer#MIN_VALUE} if it has not been cached yet
     */
    private int width(final int cp) {
      return cp < 128 ? widths[cp] : wideWidths.get(cp);
    }

    /**
     * Caches the width of a character.
     * @param cp codepoint
     * @param width width
     */
    private void width(final int cp, final int width) {
      if(cp < 128) widths[cp] = width;
      else wideWidths.put(cp, width);
    }

    /**
     * Returns the cached font family of a character.
     * @param cp codepoint
     * @return font family, or {@code null} if it has not been cached yet
     */
    private FontFamily family(final int cp) {
      return cp < 128 ? families[cp] : wideFamilies.get(cp);
    }

    /**
     * Caches the font family of a character.
     * @param cp codepoint
     * @param ff font family
     */
    private void family(final int cp, final FontFamily ff) {
      if(cp < 128) families[cp] = ff;
      else wideFamilies.put(cp, ff);
    }
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
