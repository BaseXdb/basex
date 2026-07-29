package org.basex.gui.text;

import java.util.*;

import org.basex.gui.*;
import org.basex.util.options.*;

/**
 * Options of a text editor, resolved against the GUI options or their default values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class EditorOptions {
  /** Options with default values. */
  static final EditorOptions DEFAULTS = new EditorOptions(null);

  /** GUI options (can be {@code null}). */
  private final GUIOptions gopts;

  /**
   * Constructor.
   * @param gopts GUI options (can be {@code null})
   */
  EditorOptions(final GUIOptions gopts) {
    this.gopts = gopts;
  }

  /**
   * Returns the value of a boolean option.
   * @param option option
   * @return value
   */
  boolean get(final BooleanOption option) {
    return gopts != null ? gopts.get(option) : option.value();
  }

  /**
   * Returns the value of a number option.
   * @param option option
   * @return value
   */
  int get(final NumberOption option) {
    return gopts != null ? gopts.get(option) : option.value();
  }

  /**
   * Returns the value of a string option.
   * @param option option
   * @return value
   */
  String get(final StringOption option) {
    return gopts != null ? gopts.get(option) : option.value();
  }

  /**
   * Returns the current indentation.
   * @return indentation
   */
  int indent() {
    return Math.max(1, get(GUIOptions.INDENT));
  }

  /**
   * Returns the characters used for indenting text.
   * @return tab or spaces
   */
  byte[] spaces() {
    if(!get(GUIOptions.TABSPACES)) return new byte[] { '\t' };
    final byte[] spaces = new byte[indent()];
    Arrays.fill(spaces, (byte) ' ');
    return spaces;
  }

  /**
   * Returns the line margin.
   * @return margin, or {@code 0} if no margin is shown
   */
  int margin() {
    return get(GUIOptions.SHOWMARGIN) ? Math.max(1, get(GUIOptions.MARGIN)) : 0;
  }
}
