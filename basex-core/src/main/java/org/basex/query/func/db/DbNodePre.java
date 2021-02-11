package org.basex.query.func.db;

import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbNodePre extends DbNodeId {
  @Override
  protected int id(final DBNode node) {
    return node.pre();
  }
}
