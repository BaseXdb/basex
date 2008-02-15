package org.basex.gui.layout;

import java.awt.Color;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class BaseXSyntax {
  /** Static Syntax reference. */
  public static final BaseXSyntax SIMPLE = new BaseXSyntax() {
    @Override
    public void init() { }
    @Override
    public Color getColor(final BaseXTextTokens text) { return Color.black; }
  };
  
  /** 
   * Initializes the method.
   */
  public abstract void init();

  /** 
   * Returns the color for the current token.
   * @param text text reference
   * @return color
   */
  public abstract Color getColor(final BaseXTextTokens text);
}
