package org.basex.gui.layout;

import static org.basex.util.Token.*;
import java.awt.Color;

/**
 * This abstract class defines a framework for a simple syntax
 * highlighting in text panels.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXSyntax {
  /** Simple syntax. */
  static final BaseXSyntax SIMPLE = new BaseXSyntax() {
    @Override
    public void init() { }
    @Override
    public Color getColor(final BaseXTextTokens tokens) { return Color.black; }
  };

  /**
   * Initializes the highlighter.
   */
  public abstract void init();

  /**
   * Returns the color for the current token.
   * @param tokens tokenizer
   * @return color
   */
  public abstract Color getColor(final BaseXTextTokens tokens);

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
}
