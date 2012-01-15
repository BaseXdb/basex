package org.basex.gui.layout;

import java.awt.Color;
import org.basex.gui.GUIConstants;

/**
 * This class defines syntax highlighting for text files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextSyntax extends BaseXSyntax {
  /** Quoted flag. */
  private boolean quoted;

  @Override
  public void init() {
    quoted = false;
  }

  @Override
  public Color getColor(final BaseXTextTokens text) {
    final boolean quote = text.curr() == '"';
    final Color color = quoted || quote ? GUIConstants.BLUE : Color.black;
    if(quote) quoted ^= true;
    return color;
  }
}
