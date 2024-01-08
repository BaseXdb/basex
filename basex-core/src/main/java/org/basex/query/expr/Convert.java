package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Cast expressions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class Convert extends Single {
  /** Static context. */
  final StaticContext sc;
  /** Sequence type to cast to (zero or one items). */
  final SeqType seqType;

  /**
   * Function constructor.
   * @param sc static context
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param seqType sequence type to cast to (zero or one item)
   * @param targetType target type
   */
  Convert(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType, final SeqType targetType) {
    super(info, expr, targetType);
    this.sc = sc;
    this.seqType = seqType;
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.STRING, cc);
    return this;
  }

  /**
   * Returns a stricter cast type.
   * @return sequence type
   */
  final SeqType castType() {
    final SeqType est = expr.seqType();
    Type type = seqType.type;
    Occ occ = seqType.occ;
    if(type instanceof ListType) {
      type = type.atomic();
      occ = Occ.ZERO_OR_MORE;
    } else if(occ == Occ.ZERO_OR_ONE && est.oneOrMore() && !est.mayBeArray()) {
      occ = Occ.EXACTLY_ONE;
    }
    return SeqType.get(type, occ);
  }

  /**
   * Checks if the expression can be cast to the specified type.
   * @param castType type to cast to
   * @return result of check or {@code null}
   */
  final Boolean cast(final SeqType castType) {
    final SeqType est = expr.seqType();
    if(!est.mayBeArray()) {
      final long es = expr.size();
      if(es != -1 && (es < castType.occ.min || es > castType.occ.max)) return false;

      final Type et = est.type;
      if(et.instanceOf(castType.type) && est.occ.instanceOf(castType.occ) &&
          (et.eq(castType.type) || castType.type == AtomType.NUMERIC)) return true;
    }
    return null;
  }

  /**
   * Checks if the argument can be simplified.
   * @param castType type to cast to
   * @param cc compilation context
   * @return simplified argument or {@code null}
   */
  final Expr simplify(final SeqType castType, final CompileContext cc) {
    final SeqType est = expr.seqType();
    Expr arg = null;
    if(est.one() && !est.mayBeArray() && castType.type.instanceOf(AtomType.NUMERIC)) {
      // xs:int(string(I))
      // xs:int(xs:double(I))  ->  xs:int(I)
      arg = FnNumber.simplify(expr, cc);
      if(arg == null && expr instanceof Cast && (
        castType.type.instanceOf(est.type) ||
        castType.type.instanceOf(AtomType.INT) && est.type == AtomType.DOUBLE ||
        castType.type.instanceOf(AtomType.SHORT) && est.type == AtomType.FLOAT
      )) {
        arg = ((Cast) expr).expr;
      }
    }
    return arg;
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, AS, seqType), expr);
  }
}
