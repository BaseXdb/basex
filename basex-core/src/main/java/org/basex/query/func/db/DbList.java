package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class DbList extends DbFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs.length == 0 ? list(qc).iter() : resources(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs.length == 0 ? list(qc) : resources(qc).value(qc);
  }

  /**
   * Returns a list of all databases.
   * @param qc query context
   * @return databases
   */
  private static Value list(final QueryContext qc) {
    final Context ctx = qc.context;
    final StringList dbs = ctx.listDBs();
    final TokenList list = new TokenList(dbs.size());
    for(final String name : dbs) list.add(name);
    return StrSeq.get(list);
  }

  /**
   * Returns an iterator over all resources in a databases.
   * @param qc query context
   * @return resource iterator
   * @throws QueryException query exception
   */
  private Iter resources(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? EMPTY : toToken(exprs[1], qc));
    final IntList docs = data.resources.docs(path);
    final TokenList bins = data.resources.binaries(path);
    final int ds = docs.size(), size = ds + bins.size();

    return new BasicIter<Str>(size) {
      @Override
      public Str get(final long i) {
        return i < size ? Str.get(tokenAt((int) i)) : null;
      }
      @Override
      public Value value(final QueryContext q) {
        final TokenList tl = new TokenList(size);
        for(int i = 0; i < size; i++) tl.add(tokenAt(i));
        return StrSeq.get(tl);
      }
      private byte[] tokenAt(final int i) {
        return i < ds ? data.text(docs.get(i), true) : bins.get(i - ds);
      }
    };
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(exprs.length == 0) {
      if(!visitor.lock(null)) return false;
    } else {
      if(!dataLock(visitor, 0)) return false;
    }
    return super.accept(visitor);
  }
}
