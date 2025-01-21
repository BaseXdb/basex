package org.basex.query.func.index;

import org.basex.index.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexAttributes extends IndexTexts {
  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
