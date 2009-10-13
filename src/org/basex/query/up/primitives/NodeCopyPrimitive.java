package org.basex.query.up.primitives;

import java.util.Iterator;
import java.util.LinkedList;

import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.UpdateFunctions;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted i.e..
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class NodeCopyPrimitive extends UpdatePrimitive {
  /** Copy of nodes to be inserted. */
  LinkedList<Iter> c;

  /**
   * Constructor.
   * @param n target node
   * @param copy node copy
   */
  protected NodeCopyPrimitive(final Nod n, final Iter copy) {
    super(n);
    c = new LinkedList<Iter>();
    c.add(copy);
  }
  
  /**
   * Builds MemData instance from iterator.
   * @return data instance
   * @throws QueryException query exception
   */
  public MemData buildDB() throws QueryException {
    final SeqIter seq = new SeqIter();
    if(c.size() == 0) return null;
    final Iterator<Iter> it = c.iterator();
    while(it.hasNext()) {
      final Iter ni = it.next();
      Item i = ni.next();
      while(i != null) {
        seq.add(i);
        i = ni.next();
      }
    }
    return UpdateFunctions.buildDB(seq, ((DBNode) node).data);
  }
}
