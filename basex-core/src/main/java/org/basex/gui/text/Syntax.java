package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.awt.*;

/**
 * This abstract class defines a framework for a simple syntax
 * highlighting in text panels.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class Syntax {
  /** Standard color. */
  Color plain;

  /** Simple syntax. */
  static final Syntax SIMPLE = new Syntax() {
    @Override
    public Color getColor(final TextIterator iter) { return plain; }
  };

  /**
   * Initializes the highlighter.
   * @param color default color
   */
  public void init(final Color color) {
    plain = color;
  }

  /**
   * Returns the color for the current token.
   * @param iter iterator
   * @return color
   */
  public abstract Color getColor(TextIterator iter);

  /**
   * Returns the start of a comment.
   * @return comment start
   */
  public byte[] commentOpen() {
    return EMPTY;
  }

  /**
   * Returns the end of a comment.
   * @return comment end
   */
  public byte[] commentEnd() {
    return EMPTY;
  }

  /**
   * Returns a formatted version of a string.
   * @param string string to be formatted
   * @param spaces spaces
   * @return formatted string
   */
  @SuppressWarnings("unused")
  public byte[] format(final byte[] string, final byte[] spaces) {
    return string;
  }
}
