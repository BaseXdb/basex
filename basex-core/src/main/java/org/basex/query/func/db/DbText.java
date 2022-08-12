package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    return valueAccess(data, qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    return valueAccess(data, qc).value(qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    compileData(cc);
    exprs[1] = exprs[1].simplifyFor(Simplify.DATA, cc).simplifyFor(Simplify.DISTINCT, cc);

    // count number of results
    final Data data = data();
    final IndexType type = type();
    if(type != IndexType.TOKEN && data != null && exprs[1] instanceof Value) {
      type.check(data, info);
      long size = 0;
      for(final byte[] token : tokens(cc.qc)) {
        final int tl = token.length;
        if(tl == 0 || tl > data.meta.maxlen) return this;
        size += data.costs(new StringToken(type, token)).results();
      }
      exprType.assign(seqType(), size);
    }
    return this;
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.TEXT;
  }

  /**
   * Returns an index accessor.
   * @param data data reference
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final Data data, final QueryContext qc) throws QueryException {
    return new ValueAccess(info, tokens(qc), type(), null, new IndexStaticDb(data, info));
  }

  /**
   * Returns tokens to be looked up.
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  private TokenSet tokens(final QueryContext qc) throws QueryException {
    final TokenSet set = new TokenSet();
    final Iter iter = exprs[1].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      set.put(toToken(item));
    }
    return set;
  }
}
