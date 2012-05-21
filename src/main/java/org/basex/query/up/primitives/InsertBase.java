package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Base class for all insert primitives.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
abstract class InsertBase extends NodeCopy {
  /**
   * Constructor.
   * @param t type
   * @param p pre
   * @param d data
   * @param i input info
   * @param nc node copy
   */
  InsertBase(final PrimitiveType t, final int p, final Data d,
             final InputInfo i, final NodeSeqBuilder nc) {
    super(t, p, d, i, nc);
  }

  @Override
  public void apply() {
    shifts = md.meta.size;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    insert.add(((NodeCopy) p).insert.get(0));
  }
}
