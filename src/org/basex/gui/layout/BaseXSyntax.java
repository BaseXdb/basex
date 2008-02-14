package org.basex.gui.layout;

import java.awt.Color;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BaseXSyntax {
  /** 
   * Initializes the method.
   */
  public void init() { }

  /** 
   * Returns the color for the current token.
   * @param text text reference
   * @return color
   */
  @SuppressWarnings("unused")
  public Color getColor(final BaseXTextTokens text) {
    return Color.black;
  }
}
