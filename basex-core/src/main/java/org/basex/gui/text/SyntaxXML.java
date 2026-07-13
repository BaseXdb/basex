package org.basex.gui.text;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for XML files: markup without embedded code.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends SyntaxMarkup {
  /** Attribute that preserves whitespace. */
  private static final byte[] SPACE = token("xml:space");

  @Override
  public byte[] commentOpen() {
    return XMLToken.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.COMM_C;
  }

  @Override
  boolean boundarySpace(final byte[] text) {
    // 'xml:space' turns the whitespace between tags into significant text
    return indexOf(text, SPACE) == -1;
  }
}
