package org.basex.query.up;

import static org.basex.query.QueryText.*;
import java.util.HashSet;
import java.util.Set;

import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class FragmentPrimitives extends Primitives {
  /**
   * Constructor.
   */
  public FragmentPrimitives() {
    super();
  }
  
  @Override
  public void finish() {
    // get keys (pre values) and sort ascending
    final IntList il = new IntList(op.size());
    for(final int i : op.keySet()) il.add(i);
    nodes = il.finish();
    finished = true;
  }
  
  @Override
  public void check() throws QueryException {
    super.check();
    
    // [LK] revise data structures ... lots of useless access
    // check fn:put constraints ... duplicate uri
    final Set<String> uris = new HashSet<String>();
    for(final UpdatePrimitive[] ups : op.values()) {
      final Put put = (Put) ups[PrimitiveType.PUT.ordinal()];
      if(put == null) continue;
      if(!uris.add(put.path())) Err.or(UPURIDUP, put.path());
    }
  }
  
  @Override
  public void apply() {
    for(final UpdatePrimitive[] ups : op.values()) {
      final Put put = (Put) ups[PrimitiveType.PUT.ordinal()];
      if(put == null) continue;
      put.apply(0);
    }
  }
}
