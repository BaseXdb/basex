package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Insert into as first primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertIntoFirstPrimitive extends InsertPrimitive {
  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l actual pre location where nodes are inserted
   */
  public InsertIntoFirstPrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }
  
  @Override
  public void check() throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public void apply() throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPDATE, this);
  }

  @Override
  public Type type() {
    return Type.INSERTINTOFI;
  }
}
