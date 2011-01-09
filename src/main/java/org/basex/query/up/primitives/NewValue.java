package org.basex.query.up.primitives;

import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.util.InputInfo;

/**
 * Abstract update primitive which holds an additional 'name' attribute to for
 * updating values, names, etc..
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Lukas Kircher
 */
abstract class NewValue extends UpdatePrimitive {
  /** New name. */
  final QNm name;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param newName new name
   */
  protected NewValue(final InputInfo ii, final Nod n, final QNm newName) {
    super(ii, n);
    name = newName;
  }
}
