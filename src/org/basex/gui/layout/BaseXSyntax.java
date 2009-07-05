package org.basex.gui.layout;

import java.awt.Color;

/**
 * This abstract class defines a framework for a simple syntax
 * highlighting in text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class BaseXSyntax {
  /** Simple Syntax. */
  public static final BaseXSyntax SIMPLE = new BaseXSyntax() {
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
}
