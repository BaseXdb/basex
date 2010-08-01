package org.basex.query.up.primitives;

import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;

/**
 * Abstract update primitive which holds an additional 'name' attribute to for
 * updating values, names, etc..
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
abstract class NewValue extends UpdatePrimitive {
  /** New name. */
  final QNm name;

  /**
   * Constructor.
   * @param u updating expression
   * @param n target node
   * @param newName new name
   */
  protected NewValue(final ParseExpr u, final Nod n, final QNm newName) {
    super(u, n);
    name = newName;
  }
}
