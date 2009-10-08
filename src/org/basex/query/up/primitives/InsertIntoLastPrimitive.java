package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;  

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Insert into as last primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertIntoLastPrimitive extends NodeCopyPrimitive {
  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param attr copied nodes are attributes
   */
  protected InsertIntoLastPrimitive(final Nod n, final Iter copy, 
      final boolean attr) {
    super(n, copy, attr);
  }
  
  @Override
  public void check() throws QueryException {
    super.check();
    Err.or(UPDATE, this);
  }

  @Override
  public void apply() {
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public Type type() {
    return Type.INSERTINTOLA;
  }
}
