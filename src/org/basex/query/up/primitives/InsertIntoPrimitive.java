package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import java.util.Iterator;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;

/**
 * Represents an insert into primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertIntoPrimitive extends InsertPrimitive {

  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l actual pre location where nodes are inserted
   */
  public InsertIntoPrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }
  
  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void apply() throws QueryException {
    if(!(node instanceof DBNode)) return;

    final SeqIter seq = new SeqIter();
    final Iterator<Iter> it = c.iterator();
    while(it.hasNext()) {
      final Iter ni = it.next();
      Item i = ni.next();
      while(i != null) {
        seq.add(i);
        i = ni.next();
      }
    }
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    // source nodes may be empty, thus insert has no effect at all
    if(seq.size() == 0) return;
    d.insertSeq(loc, n.pre, buildDB(seq, ((DBNode) node).data));
  }

  @SuppressWarnings("unused")
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTINTO;
  }
}
