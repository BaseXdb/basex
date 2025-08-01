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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccessFn {
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
    arg(1, arg -> arg.simplifyFor(Simplify.DATA, cc).simplifyFor(Simplify.DISTINCT, cc));

    // count number of results
    final Data data = data();
    final IndexType type = type();
    if(type != IndexType.TOKEN && data != null && arg(1) instanceof Value) {
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

  @Override
  public final boolean ddo() {
    return true;
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
    final TokenSet token = new TokenSet();
    for(final Item item : arg(1).atomValue(qc, info)) {
      token.put(toToken(item));
    }
    return token;
  }
}
