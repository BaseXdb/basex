package org.basex.query.func.index;

import org.basex.index.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexAttributeNames extends IndexElementNames {
  @Override
  IndexType type() {
    return IndexType.ATTRNAME;
  }
}
