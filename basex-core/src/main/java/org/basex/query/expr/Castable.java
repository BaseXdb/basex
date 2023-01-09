package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Castable expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Castable extends Convert {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param seqType sequence type to check
   */
  public Castable(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType) {
    super(sc, info, expr, seqType, SeqType.BOOLEAN_O);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final SeqType castType = castType();
    final Boolean castable = cast(castType);
    if(castable != null) return cc.replaceWith(this, Bln.get(castable));

    final Expr arg = simplify(castType, cc);
    if(arg != null) return new Castable(sc, info, arg, seqType).optimize(cc);

    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(seqType.cast(expr.atomValue(qc, info), false, qc, sc, info) != null);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Castable(sc, info, expr.copy(cc, vm), seqType));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Castable && seqType.eq(((Castable) obj).seqType) &&
        super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("(").token(expr).token(CASTABLE).token(AS).token(seqType).token(')');
  }
}
