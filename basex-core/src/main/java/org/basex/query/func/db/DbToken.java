package org.basex.query.func.db;

import org.basex.index.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbToken extends DbAttribute {
  @Override
  IndexType type() {
    return IndexType.TOKEN;
  }
}
