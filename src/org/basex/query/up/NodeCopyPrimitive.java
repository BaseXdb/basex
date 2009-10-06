package org.basex.query.up;

import java.util.LinkedList;

import static org.basex.query.up.UpdateFunctions.*;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

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
    // [LK] correct that for multiple iterators after merging
    m = buildDB(c.getFirst(), node instanceof DBNode ? ((DBNode) node).data :
      null);
  }
}
