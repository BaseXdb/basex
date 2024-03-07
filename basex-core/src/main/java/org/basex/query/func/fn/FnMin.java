package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnMin extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
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

    // ensure that item is sortable
    final Type type = item.type;
    if(!type.isSortable()) throw COMPARE_X_X.get(info, type, item);

    final boolean string = item instanceof AStr, numeric = !string && !(
        type == BOOLEAN || item instanceof ADate || item instanceof Dur || item instanceof Bin);
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
      if(!(numeric ? it instanceof ANum : string ? it instanceof AStr : type == type2)) {
        throw ARGTYPE_X_X_X.get(info, type, type2, it);
      }
      if(min ^ item.compare(it, collation, true, info) < 0) item = it;
    }
    return item;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    final Type type = arg(0).seqType().type;
    if(type.isNumberOrUntyped()) arg(0, arg -> arg.simplifyFor(Simplify.NUMBER, cc));

    if(defined(1)) arg(1, arg -> arg.simplifyFor(Simplify.STRING, cc));
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
    arg(0, arg -> arg.simplifyFor(Simplify.DATA, cc).simplifyFor(Simplify.DISTINCT, cc));

    final Expr expr = optFirst();
    if(expr != this) return expr;

    final boolean noColl = !defined(1);
    final Expr values = arg(0);
    final SeqType st = values.seqType();
    Type type = st.type;
    if(type.isSortable()) {
      if(type.isUntyped()) {
        type = DOUBLE;
      } else if(st.one() && noColl) {
        return values;
      }
      exprType.assign(type);
      if(values instanceof Value && noColl) {
        Item item = null;
        final Value value = (Value) values;
        final long size = value.size();
        if(value instanceof RangeSeq) {
          final RangeSeq rs = (RangeSeq) value;
          item = rs.itemAt(min ^ rs.ascending() ? size - 1 : 0);
        } else if(value instanceof SingletonSeq && ((SingletonSeq) value).singleItem()) {
          item = value.itemAt(0);
        } else if(value.isItem()) {
          item = (Item) value;
        }
        if(item != null) {
          type = item.seqType().type;
          if(type.isNumber() || type.instanceOf(STRING)) return item;
        }
      }
    }

    if(noColl && values instanceof Path) {
      final ArrayList<Stats> list = ((Path) values).pathStats();
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
}
