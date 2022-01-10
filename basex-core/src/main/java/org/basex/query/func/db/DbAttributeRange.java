package org.basex.query.func.db;

import org.basex.index.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbAttributeRange extends DbTextRange {
  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
