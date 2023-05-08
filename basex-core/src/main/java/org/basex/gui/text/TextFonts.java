package org.basex.gui.text;

import java.awt.*;
import static java.awt.Font.*;
import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * Fonts of the current text renderer.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class TextFonts {
  /** Fonts (default, bold). */
  private final Font[] fonts = new Font[2];
  /** Fallback fonts. */
  private final Font[] fallbackFonts = new Font[2];
  /** Font metrics. */
  private final FontMetrics[] metrics = new FontMetrics[2];
  /** Fallback font metrics. */
  private final FontMetrics[] fallbackMetrics = new FontMetrics[2];

  /** Monospace flag. */
  private boolean monospace;
  /** Font style. */
  private int style;

  /**
   * Constructor.
   * @param font font
   */
  TextFonts(final Font font) {
    fonts[PLAIN] = font;
    fonts[BOLD] = font.deriveFont(BOLD);
  }

  /**
   * Initializes the font metrics and fallback fonts.
   * @param comp component
   */
  void init(final JComponent comp) {
    if(metrics[PLAIN] != null) return;

    metrics[PLAIN] = comp.getFontMetrics(fonts[PLAIN]);
    metrics[BOLD] = comp.getFontMetrics(fonts[BOLD]);
    monospace = BaseXLayout.isMono(metrics[PLAIN]);

    final String name = monospace ? MONOSPACED : SANS_SERIF;
    fallbackFonts[PLAIN] = new Font(name, PLAIN, fonts[PLAIN].getSize());
    fallbackFonts[BOLD] = fallbackFonts[PLAIN].deriveFont(BOLD);
    fallbackMetrics[PLAIN] = comp.getFontMetrics(fallbackFonts[PLAIN]);
    fallbackMetrics[BOLD] = comp.getFontMetrics(fallbackFonts[BOLD]);
  }

  /**
   * Assigns a style and returns the font.
   * @param s style
   */
  void assign(final int s) {
    style = s;
  }

  /**
   * Returns the current font.
   * @return font
   */
  Font font() {
    return fonts[style];
  }

  /**
   * Returns the monospace flag.
   * @return monospace flag
   */
  boolean monospace() {
    return monospace;
  }

  /**
   * Returns the pixel width of the specified string.
   * @param string string
   * @return width
   */
  int stringWidth(final String string) {
    return (canDisplay(string) ? metrics : fallbackMetrics)[style].stringWidth(string);
  }

  /**
   * Returns the pixel width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  int charWidth(final int cp) {
    return (font().canDisplay(cp) ? metrics : fallbackMetrics)[style].charWidth(cp);
  }

  /**
   * Returns an appropriate font for the specified string.
   * @param string string
   * @return font
   */
  Font font(final String string) {
    return (canDisplay(string) ? fonts : fallbackFonts)[style];
  }

  /**
   * Checks if the specified font can be displayed with the current string.
   * @param string string
   * @return result of check
   */
  private boolean canDisplay(final String string) {
    return font().canDisplayUpTo(string) == -1;
  }
}
