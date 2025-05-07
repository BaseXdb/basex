package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbNodePre extends DbNodeId {
  @Override
  protected void addIds(final Value nodes, final LongList ids) throws QueryException {
    if(nodes instanceof final DBNodeSeq seq) {
      for(final int pre : seq.pres()) ids.add(pre);
    } else {
      super.addIds(nodes, ids);
    }
  }

  @Override
  protected int id(final DBNode node) {
    return node.pre();
  }
}
