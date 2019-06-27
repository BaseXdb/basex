package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnError extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        throw error(qc);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    throw error(qc);
  }

  @Override
  public boolean isVacuous() {
    return true;
  }

  @Override
  protected Expr typeCheck(final TypeCheck tc, final CompileContext cc) {
    return this;
  }

  /**
   * Creates an error function instance.
   * @param ex query exception
   * @param st type of the expression
   * @param sc static context
   * @return function
   */
  public static StandardFunc get(final QueryException ex, final SeqType st,
      final StaticContext sc) {
    Util.debug(ex);
    final Str msg = Str.get(ex.getLocalizedMessage());
    final StandardFunc sf = ERROR.get(sc, ex.info(), ex.qname(), msg);
    sf.exprType.assign(st);
    return sf;
  }

  /**
   * Raises an error.
   * @param qc query context
   * @return query exception
   * @throws QueryException query exception
   */
  public QueryException error(final QueryContext qc) throws QueryException {
    final int al = exprs.length;
    if(al == 0) return FUNERR1.get(info);

    QNm name = toQNm(exprs[0], qc, true);
    if(name == null) name = FUNERR1.qname();

    final String msg = al > 1 ? Token.string(toToken(exprs[1], qc)) : FUNERR1.desc;
    final Value value = al > 2 ? exprs[2].value(qc) : null;
    return new QueryException(info, name, msg).value(value);
  }
}
