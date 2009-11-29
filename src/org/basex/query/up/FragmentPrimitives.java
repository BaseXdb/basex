package org.basex.query.up;

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
  public void apply() { };
}
