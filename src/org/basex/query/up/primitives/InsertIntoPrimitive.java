package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import java.util.Iterator;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.UpdateFunctions;

/**
 * Represents an insert into primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertIntoPrimitive extends NodeCopyPrimitive {

  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param attr copied nodes are attributes
   */
  public InsertIntoPrimitive(final Nod n, final Iter copy, 
      final boolean attr) {
    super(n, copy, attr);
  }
  
  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void apply() throws QueryException {
    if(!(node instanceof DBNode)) return;

    // create db containing insertion nodes
    // attribute nodes are treated seperately
    final SeqIter aSeq = new SeqIter();
    final SeqIter seq = new SeqIter();
    final Iterator<Iter> it = c.iterator();
    while(it.hasNext()) {
      final Iter ni = it.next();
      // sort nodes into attribute sequence and others
      Item i = ni.next();
      while(i != null) {
        if(Nod.kind(i.type) == Data.ATTR) aSeq.add(i);
        else seq.add(i);
        i = ni.next();
      }
    }
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    MemData m = null;
    // source nodes may be empty, thus insert has no effect at all
    if(seq.size() == 0 && aSeq.size() == 0) return;
    
    // insert non-attribute nodes
    if(seq.size() > 0) {
      m = buildDB(seq, ((DBNode) node).data);
      d.insertSeq(n.pre + d.attSize(n.pre, Nod.kind(n.type)), n.pre, m);
    }
    
    // insert attributes
    if(aSeq.size() > 0) {
      m = buildDB(aSeq, ((DBNode) node).data);
      UpdateFunctions.insertAttributes(n.pre + d.attSize(n.pre, d.kind(n.pre)), 
          n.pre, d, m);
    }
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
