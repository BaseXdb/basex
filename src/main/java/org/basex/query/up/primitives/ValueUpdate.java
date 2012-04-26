package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Base class for update primitives that only affect values of database nodes
 * and do not result in structural changes / pre value shifts of the table.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
abstract class ValueUpdate extends UpdatePrimitive {
  /**
   * Constructor.
   * @param t type
   * @param p pre
   * @param d data
   * @param ii input info
   */
  ValueUpdate(final PrimitiveType t, final int p, final Data d, final InputInfo ii) {
    super(t, p, d, ii);
  }
}
