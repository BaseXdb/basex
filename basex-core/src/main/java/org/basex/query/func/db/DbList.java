package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.users.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class DbList extends DbFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    if(el == 0) {
      final Context ctx = qc.context;
      final StringList dbs = ctx.filter(Perm.READ, ctx.databases.listDBs());
      final TokenList tl = new TokenList(dbs.size());
      for(final String name : dbs) tl.add(name);
      return StrSeq.get(tl).iter();
    }

    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? EMPTY : toToken(exprs[1], qc));
    final IntList docs = data.resources.docs(path);
    final TokenList bins = data.resources.binaries(path);
    final int ds = docs.size(), sz = ds + bins.size();

    return new BasicIter<Str>(sz) {
      @Override
      public Str get(final long i) {
        return i < sz ? Str.get(tokenAt((int) i)) : null;
      }
      @Override
      public boolean hasValue() {
        return true;
      }
      @Override
      public Value value(final QueryContext q) {
        final TokenList tl = new TokenList(sz);
        for(int i = 0; i < sz; i++) tl.add(tokenAt(i));
        return StrSeq.get(tl);
      }
      /**
       * Returns the specified token.
       * @param i token index
       * @return token
       */
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
