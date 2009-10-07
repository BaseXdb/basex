package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import java.util.Iterator;
import java.util.LinkedList;

import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted i.e..
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class NodeCopyPrimitive extends UpdatePrimitive {
  /** Copy of nodes to be inserted. */
  LinkedList<Iter> c;
  /** {@link MemData} instance of node copies. Speeds up insert. */
  MemData m;

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
  
  @Override
  public void check() throws QueryException {
    createDB();
  }
  
  /**
   * Merges the given iterators by creating a {@link MemData} instance from
   * them.
   * @throws QueryException query exception 
   */
  private void createDB() throws QueryException {
    if(!(node instanceof DBNode)) return;
    final SeqIter seq = new SeqIter();
    final Iterator<Iter> it = c.iterator();
    while(it.hasNext()) {
      seq.add(it.next());
    }
    m = buildDB(seq,((DBNode) node).data);
  }
}
