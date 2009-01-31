package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;

/**
 * Node iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class NodeIter extends Iter {
  /** Empty node iterator. */
  public static final NodeIter NONE = new NodeIter() {
    @Override
    public Nod next() { return null; }
    @Override
    public int size() { return 0; }
    @Override
    public Item get(final long i) { return null; }
    @Override
    public String toString() { return "()"; }
  };

  @Override
  public abstract Nod next() throws QueryException;
}
