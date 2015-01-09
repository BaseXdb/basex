package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class DbList extends DbFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    if(el == 0) {
      final TokenList tl = new TokenList();
      for(final String s : qc.context.databases.listDBs()) tl.add(s);
      return StrSeq.get(tl).iter();
    }

    final Data data = checkData(qc);
    final String path = string(exprs.length == 1 ? Token.EMPTY : toToken(exprs[1], qc));
    final IntList il = data.resources.docs(path);
    final TokenList tl = data.resources.binaries(path);
    return new Iter() {
      final int is = il.size(), ts = tl.size();
      int ip, tp;
      @Override
      public Str get(final long i) {
        return i < is ? Str.get(data.text(il.get((int) i), true)) :
          i < is + ts ? Str.get(tl.get((int) i - is)) : null;
      }
      @Override
      public Str next() {
        return ip < is ? get(ip++) : tp < ts ? get(ip + tp++) : null;
      }
      @Override
      public long size() { return is + ts; }
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
