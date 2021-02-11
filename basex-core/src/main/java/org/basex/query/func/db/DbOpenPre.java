package org.basex.query.func.db;

import org.basex.data.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbOpenPre extends DbOpenId {
  @Override
  protected int pre(final int id, final Data data) {
    return id;
  }
}
