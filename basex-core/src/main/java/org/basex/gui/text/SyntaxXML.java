package org.basex.gui.text;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for XML files: markup without embedded code.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends SyntaxMarkup {
  @Override
  public byte[] commentOpen() {
    return XMLToken.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.COMM_C;
  }
}
