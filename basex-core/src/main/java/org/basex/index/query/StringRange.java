package org.basex.index.query;

import org.basex.index.*;
import org.basex.util.*;

/**
 * This class stores a string range for index access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param type index type
 * @param min minimum value
 * @param mni include minimum value
 * @param max maximum value
 * @param mxi include maximum value
 */
public record StringRange(IndexType type, byte[] min, boolean mni, byte[] max,
    boolean mxi) implements IndexSearch {
  @Override
  public byte[] token() {
    return Token.EMPTY;
  }
}
