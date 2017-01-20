package org.basex.query.iter;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Database node iterator.
 *
 * @author BaseX Team 2005-16, BSD License
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
  public Value value() throws QueryException {
    final IntList il = new IntList();
    for(DBNode n; (n = next()) != null;) il.add(n.pre());
    return DBNodeSeq.get(il, data, false, false);
  }
}
