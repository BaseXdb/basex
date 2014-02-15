package org.basex.query.up.primitives;

import org.basex.query.up.*;

/**
 * {@link UpdatePrimitive} types. Do not change the order as this affects
 * {@link UpdatePrimitiveComparator} and will most likely lead to weird results.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public enum PrimitiveType {
  /* DO NOT CHANGE ORDER OF PRIMITIVES.
   *
   * Primitive types help to create an order on update primitives.
   *
   * If more than one update primitive is targeted at a node T:
   *  - INSERTAFTER has to be carried out first, as it accesses
   *    the highest PRE value (T+size(T)). Hence it's ranked highest.
   *  - INSERTBEFORE v.v. (as it accesses the lowest PRE value equal T).
   */
  /** Insert before.           */ INSERTBEFORE,
  /** Delete.                  */ DELETENODE,
  /** Replace node.            */ REPLACENODE,
  /** Rename.                  */ RENAMENODE,
  /** Replace value.           */ REPLACEVALUE,
  /** Insert attribute.        */ INSERTATTR,
  /** Insert into as first.    */ INSERTINTOFIRST,
  /** Insert into.             */ INSERTINTO,
  /** Insert into as last.     */ INSERTINTOLAST,
  /** Insert after.            */ INSERTAFTER,
}
