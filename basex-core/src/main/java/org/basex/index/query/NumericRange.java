package org.basex.index.query;

import org.basex.index.*;
import org.basex.util.*;

/**
 * This class stores a numeric range for index access.
 * @param type index type
 * @param min minimum value
 * @param max maximum value
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public record NumericRange(IndexType type, double min, double max) implements IndexSearch {
  @Override
  public byte[] token() {
    return Token.EMPTY;
  }
}
