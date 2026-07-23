package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbNodePre extends DbNodeId {
  @Override
  int id(final DBNode node) {
    return node.pre();
  }

  @Override
  void addIds(final Value nodes, final ValueBuilder vb) throws QueryException {
    if(nodes instanceof final DBNodeSeq seq) {
      for(final int pre : seq.pres()) vb.add(pre);
    } else {
      super.addIds(nodes, vb);
    }
  }
}
