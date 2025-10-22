package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpN.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnOp extends StandardFunc {
  /** QName: X. */
  private static final QNm Q_X = new QNm("x");
  /** QName: Y. */
  private static final QNm Q_Y = new QNm("y");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String operator = toString(arg(0), qc);

    final int pl =  2;
    final VarScope vs = new VarScope();
    final Var[] params = new Var[pl];
    final Expr[] args = new Expr[pl];
    for(int p = 0; p < pl; p++) {
      params[p] = vs.addNew(p == 0 ? Q_X : Q_Y, null, qc, info);
      args[p] = new VarRef(info, params[p]);
    }
    final Expr arg1 = args[0], arg2 = args[1];
    final Expr body = switch(operator) {
      case "," -> new List(info, arg1, arg2);
      case "and" -> new And(info, arg1, arg2);
      case "or" -> new Or(info, arg1, arg2);
      case "+" -> new Arith(info, arg1, arg2, Calc.ADD);
      case "-" -> new Arith(info, arg1, arg2, Calc.SUBTRACT);
      case "*" -> new Arith(info, arg1, arg2, Calc.MULTIPLY);
      case "div" -> new Arith(info, arg1, arg2, Calc.DIVIDE);
      case "idiv" -> new Arith(info, arg1, arg2, Calc.DIVIDEINT);
      case "mod" -> new Arith(info, arg1, arg2, Calc.MODULO);
      case "=" -> new CmpG(info, arg1, arg2, OpG.EQ);
      case "<" -> new CmpG(info, arg1, arg2, OpG.LT);
      case "<=" -> new CmpG(info, arg1, arg2, OpG.LE);
      case ">" -> new CmpG(info, arg1, arg2, OpG.GT);
      case ">=" -> new CmpG(info, arg1, arg2, OpG.GE);
      case "!=" -> new CmpG(info, arg1, arg2, OpG.NE);
      case "eq" -> new CmpV(info, arg1, arg2, OpV.EQ);
      case "lt" -> new CmpV(info, arg1, arg2, OpV.LT);
      case "le" -> new CmpV(info, arg1, arg2, OpV.LE);
      case "gt" -> new CmpV(info, arg1, arg2, OpV.GT);
      case "ge" -> new CmpV(info, arg1, arg2, OpV.GE);
      case "ne" -> new CmpV(info, arg1, arg2, OpV.NE);
      case "<<", "precedes" -> new CmpN(info, arg1, arg2, OpN.LT);
      case ">>", "follows" -> new CmpN(info, arg1, arg2, OpN.GT);
      case "is" -> new CmpN(info, arg1, arg2, OpN.EQ);
      case "is-not" -> new CmpN(info, arg1, arg2, OpN.NE);
      case "precedes-or-is" -> new CmpN(info, arg1, arg2, OpN.LE);
      case "follows-or-is" -> new CmpN(info, arg1, arg2, OpN.GE);
      case "||" -> new Concat(info, arg1, arg2);
      case "|", "union" -> new Union(info, arg1, arg2);
      case "except" -> new Except(info, arg1, arg2);
      case "intersect" -> new Intersect(info, arg1, arg2);
      case "to" -> new Range(info, arg1, arg2);
      case "otherwise" -> new Otherwise(info, arg1, arg2);
      default -> throw UNKNOWNOP_X.get(info, operator);
    };
    final FuncType ft = FuncType.get(body.seqType(), Types.ITEM_ZM, Types.ITEM_ZM);
    return new FuncItem(info, body, params, AnnList.EMPTY, ft, pl, null);
  }
}
