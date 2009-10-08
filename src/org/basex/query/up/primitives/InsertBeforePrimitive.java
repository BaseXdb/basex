package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;

import java.util.Iterator;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;

/**
 * Insert before primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertBeforePrimitive extends NodeCopyPrimitive {
  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param attr copied nodes are attributes
   */
  public InsertBeforePrimitive(final Nod n, final Iter copy, 
      final boolean attr) {
    super(n, copy, attr);
  }
  
  @Override
  public void check() throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public void apply() throws QueryException {
 // create db containing insertion nodes
    if(!(node instanceof DBNode)) return;
    final SeqIter seq = new SeqIter();
    final Iterator<Iter> it = c.iterator();
    while(it.hasNext()) {
      seq.add(it.next());
    }
//    m = buildDB(seq, ((DBNode) node).data);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public Type type() {
    return Type.INSERTBEFORE;
  }
}
