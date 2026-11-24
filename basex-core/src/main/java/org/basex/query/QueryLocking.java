package org.basex.query;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.query.scope.*;

/**
 * Computes the locks of a query.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class QueryLocking {
  /** Private constructor. */
  private QueryLocking() { }

  /**
   * Adds the locks of a query.
   * @param qc query context
   * @param jobLocks locks to be extended
   */
  static void add(final QueryContext qc, final Locks jobLocks) {
    final LockVisitor visitor = new LockVisitor(jobLocks, qc.functions, qc.updating,
        qc.contextValue == null);

    // locks in main module (can be null if parsing failed)
    boolean local = qc.main == null || qc.main.visit(visitor);
    // thesaurus databases of the full-text options in the prologs
    if(local && qc.main != null) local = qc.main.sc.acceptFTOpt(visitor);
    for(final LibraryModule lib : qc.modules.libraries()) {
      if(local) local = lib.sc.acceptFTOpt(visitor);
    }
    // locks in context expression
    if(local && qc.contextValue != null) {
      // check if scope may still be overwritten by dynamic context
      if(!qc.finalContext) jobLocks.reads.add(Locking.CONTEXT);
      visitor.resetFocus();
      local = qc.contextValue.visit(visitor);
    }
    // custom locks of the query
    final LockList list = qc.updating ? jobLocks.writes : jobLocks.reads;
    if(local) {
      list.add(qc.locks);
    } else {
      // global locking, referenced databases cannot be determined statically
      list.addGlobal();
    }
    visitor.finish();
  }

  /**
   * Checks if a query locks the specified database.
   * @param qc query context
   * @param data database
   * @return result of check
   */
  static boolean locked(final QueryContext qc, final Data data) {
    final Locks locks = new Locks();
    add(qc, locks);
    final String name = data.meta.name;
    final LockList reads = locks.finish(qc.context).reads, writes = locks.writes;
    return reads.global() || writes.global() || reads.contains(name) || writes.contains(name);
  }
}
