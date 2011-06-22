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
   *
   * Why does it differ from the specification?
   *
   * We apply the order to each single target node instead of a document (like
   * stated in the specification). The result stays the same.
   *
   * Put is executed before replace, delete and insert before as the node
   * to be serialized has already been updated and applying replace, delete
   * or insert before would change its pre value or kill its identity.
   *
   * For all other operations, concerning the order the following rule applies:
   * Updates are applied from bottom to top regarding the data table. This
   * means the update primitive which affects the highest pre value comes
   * first, etc.
   *
   * 'Insert into as last' is carried out after 'insert into' as we have to
   * make sure that location modifiers are correctly applied.
   *
   * 'Replace element content' must be executed after 'insert into as first'
   * to replace evtl. nodes inserted by 'insert into as first'
   *
   * The following list shows the affected pre value for each primitive type
   * relative to its target node pre value P. An 'S' in the first column
   * means that the corresponding primitive could lead to structural changes
   * of the table -> a shift of pre values.
   *
   * P: target pre value
   * S: may lead to structural change
   * size(), attSize() -> see {@link Data}
   *
   *
   * Primitive                Affected Pre
   * ----------------------------------------
   * S  insert after             P + size(P)
   * S  insert into              P + size(P)
   * S  insert into as last      P + size(P)
   * S  insert attribute         P + attSize(P)
   * S  insert into as first     P + attSize(P)
   * S  replace elm content      P + attSize(P) //attributes not affected
   * S  replace value            P              //inserting empty txt node -> S
   *    rename                   P
   *    put                      P
   * S  replace                  P
   * S  delete                   P
   * S  insert before            P
   *
   *
   * *****************************************************************
   * TREAT ORDER OF PRIMITIVES WITH CARE AS CHANGES WILL MOST PROBABLY
   * AFFECT OTHER PARTS OF THE XQUP MODULE (EG TEXT MERGING).
   * *****************************************************************
   *
   */

  /** Insert after.            */ INSERTAFTER,
  /** Insert into.             */ INSERTINTO,
  /** Insert into as last.     */ INSERTINTOLAST,
  /** Insert attribute.        */ INSERTATTR,
  /** Insert into as first.    */ INSERTINTOFIRST,
  /** Replace element content. */ REPLACEELEMCONT,
  /** Replace value.           */ REPLACEVALUE,
  /** Rename.                  */ RENAMENODE,
  /** Put.                     */ PUT,
  /** Replace node.            */ REPLACENODE,
  /** Delete.                  */ DELETENODE,
  /** Insert before.           */ INSERTBEFORE
}
