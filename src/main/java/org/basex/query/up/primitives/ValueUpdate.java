package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.util.InputInfo;

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
   * @param info input info
   */
  ValueUpdate(final PrimitiveType t, final int p, final Data d,
              final InputInfo info) {
    super(t, p, d, info);
  }
}
