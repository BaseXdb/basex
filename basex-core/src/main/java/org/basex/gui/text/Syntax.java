package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.awt.*;

/**
 * This abstract class defines a framework for a simple syntax
 * highlighting in text panels.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Syntax {
  /** Empty highlighter state. */
  private static final int[] NO_STATE = {};

  /** Standard color. */
  Color plain;
  /** Highlighter state; allows resuming mid-document (see {@link TextLineCache}). */
  int[] state = NO_STATE;

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
    state = NO_STATE;
  }

  /**
   * Returns the color for the current token.
   * @param iter iterator
   * @return color
   */
  public abstract Color getColor(TextIterator iter);

  /**
   * Returns a snapshot of the current highlighting state.
   * @return state (empty if the highlighter is stateless)
   */
  public final int[] state() {
    return state.clone();
  }

  /**
   * Restores a highlighting state previously returned by {@link #state()}.
   * @param st state to restore
   */
  public final void state(final int[] st) {
    state = st.clone();
  }

  /**
   * Checks if the current token is a number.
   * @param iter iterator
   * @return result of check
   */
  static boolean number(final TextIterator iter) {
    return digit(iter.curr()) && !Double.isNaN(toDouble(token(iter.currString())));
  }

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
