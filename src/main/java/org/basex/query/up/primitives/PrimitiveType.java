package org.basex.query.up.primitives;

/**
 * Update primitive type enumeration.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public enum PrimitiveType {
  // Order is essential - don't change..

  /* [LK] XQuery/Update: why does it differ from the specification?
   * - INSERTATTR, REPLACEVALUE, RENAMENODE
   * - INSERTBEFORE, INSERTAFTER, INSERTINTOFIRST, INSERTINTO
   * - REPLACENODE
   * - REPLACEELEMCONT
   * - DELETE
   * - PUT
   */

  /** Insert attribute.        */ INSERTATTR,
  /** Replace value.           */ REPLACEVALUE,
  /** Rename.                  */ RENAMENODE,
  /** Insert after.            */ INSERTAFTER,
  /** Insert into as first.    */ INSERTINTOFIRST,
  /** Insert into (as last).   */ INSERTINTO,
  /** Replace element content. */ REPLACEELEMCONT,
  /** Put.                     */ PUT,
  /** Insert before.           */ INSERTBEFORE,
  /** Replace node.            */ REPLACENODE,
  /** Delete.                  */ DELETENODE
}
