package org.basex.query.up.primitives;

import org.basex.query.up.*;

/**
 * Types of update operations. Do not change the order as this affects
 * {@link NodeUpdateComparator} and will most likely lead to weird results.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public enum UpdateType {

  /* DO NOT CHANGE ORDER OF PRIMITIVES.
   *
   * Update types help to create an order on update primitives.
   *
   * If more than one update primitive is targeted at a node T:
   *  - INSERTAFTER has to be carried out first, as it accesses
   *    the highest PRE value (T+size(T)). Hence it's ranked highest.
   *  - INSERTBEFORE v.v. (as it accesses the lowest PRE value equal T).
   */

  // Backup operation

  /** Create backup.        */ BACKUPCREATE,

  // Operations on nodes of existing databases

  /** Dummy type, indicating start of node updates. */ _NODE_UPDATES_,

  /** Insert before.        */ INSERTBEFORE,
  /** Delete.               */ DELETENODE,
  /** Replace node.         */ REPLACENODE,
  /** Rename.               */ RENAMENODE,
  /** Replace value.        */ REPLACEVALUE,
  /** Insert attribute.     */ INSERTATTR,
  /** Insert into as first. */ INSERTINTOFIRST,
  /** Insert into.          */ INSERTINTO,
  /** Insert into as last.  */ INSERTINTOLAST,
  /** Insert after.         */ INSERTAFTER,
  /** FnPut.                */ FNPUT,

  // Operations on resources of existing databases

  /** Add document.         */ DBADD,
  /** Add binary resource.  */ DBSTORE,
  /** Rename resource.      */ DBRENAME,
  /** Delete resource.      */ DBDELETE,
  /** Optimize database.    */ DBOPTIMIZE,
  /** Flush database.       */ DBFLUSH,
  /** Create user.          */ DBCREATEUSER,
  /** Grant user.           */ DBGRANTUSER,
  /** Drop user.            */ DBDROPUSER,

  // Database operations

  /** Copy database.        */ DBCOPY,
  /** Alter database.       */ DBALTER,
  /** Drop database.        */ DBDROP,
  /** Create database.      */ DBCREATE,
  /** Restore database.     */ DBRESTORE,
  /** Drop backup.          */ BACKUPDROP,
}
