package org.basex.query.up.primitives;

/**
 * Update primitive type enumeration.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public enum PrimitiveType {

  /*
   * XQuery/Update
   *
   * The type order corresponds to the order updates are carried out node-wise.
   * The XQuery Update Facility specification proposes the following order:
   *
   * - INSERTINTO, INSERTATTR, REPLACEVALUE, RENAMENODE
   * - INSERTBEFORE, INSERTAFTER, INSERTINTOFIRST, INSERTINTOLAST
   * - REPLACENODE
   * - REPLACEELEMCONT
   * - DELETE
   * - PUT
   *
   * Why does it differ from the specification?
   *
   * We apply the order to each single target node instead of a document (like
   * stated in the specification). The result stays the same. Several
   * adjustments to the proposed order simplify the implementation. In general,
   * updates that shift pre values on the descendant/following axis are moved
   * to the back to avoid further calculation expenses to determine target
   * pre values.
   */

  /** Insert attribute.        */ INSERTATTR,
  /** Replace value.           */ REPLACEVALUE,
  /** Rename.                  */ RENAMENODE,
  /** Insert after.            */ INSERTAFTER,
  /** Insert into as first.    */ INSERTINTOFIRST,
  /** Insert into.             */ INSERTINTO,
  /** Insert into as last.     */ INSERTINTOLAST,
  /** Replace element content. */ REPLACEELEMCONT,
  /** Put.                     */ PUT,
  /** Insert before.           */ INSERTBEFORE,
  /** Replace node.            */ REPLACENODE,
  /** Delete.                  */ DELETENODE
}
