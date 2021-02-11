package org.basex.query.iter;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Database node iterator.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class DBNodeIter extends BasicNodeIter {
  /** Data reference. */
  protected final Data data;

  /**
   * Constructor.
   * @param data data reference
   */
  protected DBNodeIter(final Data data) {
    this.data = data;
  }

  @Override
  public abstract DBNode next();

  @Override
  public Value value(final QueryContext qc, final Expr expr) {
    final IntList il = new IntList();
    for(DBNode node; (node = next()) != null;) {
      qc.checkStop();
      il.add(node.pre());
    }
    return DBNodeSeq.get(il.finish(), data, expr);
  }
}
