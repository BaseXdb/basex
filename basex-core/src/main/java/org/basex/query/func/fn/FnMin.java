package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;

import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.index.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnMin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return minmax(true, qc);
  }

  /**
   * Returns a minimum or maximum item.
   * @param min compute minimum or maximum
   * @param qc query context
   * @return resulting item or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item minmax(final boolean min, final QueryContext qc) throws QueryException {
    final Expr values = arg(0);
    final Collation collation = toCollation(arg(1), qc);

    if(values instanceof Range) {
      final Value value = values.value(qc);
      return value.isEmpty() ? Empty.VALUE : value.itemAt(min ? 0 : value.size() - 1);
    }

    final Iter iter = values.atomIter(qc, info);
    Item item = iter.next();
    if(item == null) return Empty.VALUE;

    final Type type = item.type;
    final boolean string = item instanceof AStr;
    final boolean dateTime = item instanceof Dtm, duration = item instanceof Dur;
    final boolean numeric = !(string || dateTime || duration || type == BOOLEAN || type == QNAME ||
        item instanceof ADate || item instanceof Bin);
    if(numeric) {
      if(type.isUntyped()) item = DOUBLE.cast(item, qc, info);
      if(item == Dbl.NAN || item == Flt.NAN) return item;
    }
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type type2 = it.type;
      if(numeric) {
        if(type2.isUntyped()) it = DOUBLE.cast(it, qc, info);
        if(it == Dbl.NAN || it == Flt.NAN) return it;
      }
      if(!(numeric ? it instanceof ANum : string ? it instanceof AStr :
         dateTime ? it instanceof Dtm : duration ? it instanceof Dur : type == type2)) {
        throw ARGTYPE_X_X_X.get(info, type, type2, it);
      }
      if(min ^ item.compare(it, collation, true, qc, info) < 0) item = it;
    }
    return item;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    super.simplifyArgs(cc);
    if(arg(0).seqType().type.isNumberOrUntyped()) {
      arg(0, arg -> arg.simplifyFor(Simplify.NUMBER, cc));
    }
    // min(distinct-values($values)) → min($values), max(sort($values)) → max($values)
    arg(0, arg -> arg.simplifyFor(Simplify.SET, cc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(true, cc);
  }

  /**
   * Optimizes a minimum or maximum item.
   * @param min compute minimum or maximum
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final boolean min, final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    final boolean noColl = !defined(1);
    final Expr values = arg(0);
    if(noColl && values instanceof final SimpleMap map) {
      final Expr monotonic = monotonic(min, map, cc);
      if(monotonic != null) return monotonic;
    }
    final SeqType st = values.seqType();
    Type type = st.type;
    if(type.isSortable()) {
      if(type.isUntyped()) {
        type = DOUBLE;
      } else if(st.one() && noColl) {
        // min($item) → $item, max($item) → $item
        return values;
      }
      exprType.assign(type);
      if(values instanceof final RangeSeq rs && noColl) {
        // min(1 to 10) → 1, max(1 to 10) → 10
        final Item item = rs.itemAt(min ^ rs.ascending() ? rs.size() - 1 : 0);
        type = item.seqType().type;
        if(type.isNumber() || type.instanceOf(STRING)) return item;
      }
    }

    // max(db:get('db')//price) → 99e0
    if(noColl && values instanceof final Path path) {
      final ArrayList<Stats> list = PathNodes.stats(path);
      if(list != null) {
        double v = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        for(final Stats stats : list) {
          if(!StatsType.isNumeric(stats.type)) return this;
          final TokenIntMap map = stats.values;
          if(map == null) {
            v = min ? Math.min(v, stats.min) : Math.max(v, stats.max);
          } else {
            for(final byte[] value : map) {
              if(value.length == 0) return this;
              final double d = Token.toDouble(value);
              v = min ? Math.min(v, d) : Math.max(v, d);
            }
          }
        }
        return Dbl.get(v);
      }
    }
    return this;
  }

  /**
   * Applies a monotonic arithmetic operation to the minimum or maximum of its input.
   * @param min compute minimum or maximum
   * @param map simple map
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr monotonic(final boolean min, final SimpleMap map, final CompileContext cc)
      throws QueryException {
    final Expr[] ops = map.args();
    final int ol = ops.length;
    final Expr last = ops[ol - 1];
    final Expr[] operands;
    final Calc calc;
    final boolean antitone;
    int pos = 0;
    if(last instanceof final Unary unary && unary.minus) {
      // max(E ! -X) → -min(E ! X)
      operands = unary.args();
      calc = null;
      antitone = true;
    } else if(last instanceof final Arith arith &&
        !arith.calc.oneOf(Calc.DIVIDEINT, Calc.MODULO)) {
      // max(E ! (X + 1)) → max(E ! X) + 1, max(E ! (X * -2)) → min(E ! X) * -2
      // max(E ! (1 - X)) → 1 - min(E ! X)
      calc = arith.calc;
      if(calc == Calc.SUBTRACT && arith.arg(0) instanceof ANum) pos = 1;
      if(!(arith.arg(1 - pos) instanceof final ANum num)) return null;
      final double d = num.dbl();
      if(Double.isNaN(d) || d == 0 && calc.oneOf(Calc.MULTIPLY, Calc.DIVIDE)) return null;
      operands = arith.args().clone();
      antitone = pos == 1 || d < 0 && calc.oneOf(Calc.MULTIPLY, Calc.DIVIDE);
    } else {
      return null;
    }

    // homogeneous input: no promotion of different numeric types
    final Expr arg = operands[pos];
    final SeqType st = arg.seqType();
    final Type type = st.type;
    if(!st.zeroOrOne() || !(type.isUntyped() || type.instanceOf(DECIMAL) ||
        type.oneOf(DOUBLE, FLOAT)) || arg.has(Flag.NDT)) return null;

    // trailing context values are removed: E ! . → E
    final Expr[] args = ops.clone();
    args[ol - 1] = arg;
    operands[pos] = cc.function(min ^ antitone ? Function.MIN : Function.MAX, info,
        SimpleMap.get(cc, info, args));
    return calc == null ? new Unary(info, operands[0], true).optimize(cc) :
      new Arith(info, operands[0], operands[1], calc).optimize(cc);
  }
}
