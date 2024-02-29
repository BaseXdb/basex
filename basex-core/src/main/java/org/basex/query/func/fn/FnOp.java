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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnOp extends StandardFunc {
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
      params[p] = vs.addNew(p == 0 ? Q_X : Q_Y, null, true, qc, info);
      args[p] = new VarRef(info, params[p]);
    }
    final Expr arg1 = args[0], arg2 = args[1], body;
    switch(operator) {
      case ","        : body = new List(info, arg1, arg2); break;
      case "and"      : body = new And(info, arg1, arg2); break;
      case "or"       : body = new Or(info, arg1, arg2); break;
      case "+"        : body = new Arith(info, arg1, arg2, Calc.ADD); break;
      case "-"        : body = new Arith(info, arg1, arg2, Calc.SUBTRACT); break;
      case "*"        : body = new Arith(info, arg1, arg2, Calc.MULTIPLY); break;
      case "div"      : body = new Arith(info, arg1, arg2, Calc.DIVIDE); break;
      case "idiv"     : body = new Arith(info, arg1, arg2, Calc.DIVIDEINT); break;
      case "mod"      : body = new Arith(info, arg1, arg2, Calc.MODULO); break;
      case "="        : body = new CmpG(info, arg1, arg2, OpG.EQ); break;
      case "<"        : body = new CmpG(info, arg1, arg2, OpG.LT); break;
      case "<="       : body = new CmpG(info, arg1, arg2, OpG.LE); break;
      case ">"        : body = new CmpG(info, arg1, arg2, OpG.GT); break;
      case ">="       : body = new CmpG(info, arg1, arg2, OpG.GE); break;
      case "!="       : body = new CmpG(info, arg1, arg2, OpG.NE); break;
      case "eq"       : body = new CmpV(info, arg1, arg2, OpV.EQ); break;
      case "lt"       : body = new CmpV(info, arg1, arg2, OpV.LT); break;
      case "le"       : body = new CmpV(info, arg1, arg2, OpV.LE); break;
      case "gt"       : body = new CmpV(info, arg1, arg2, OpV.GT); break;
      case "ge"       : body = new CmpV(info, arg1, arg2, OpV.GE); break;
      case "ne"       : body = new CmpV(info, arg1, arg2, OpV.NE); break;
      case "<<"       : body = new CmpN(info, arg1, arg2, OpN.ET); break;
      case ">>"       : body = new CmpN(info, arg1, arg2, OpN.GT); break;
      case "is"       : body = new CmpN(info, arg1, arg2, OpN.EQ); break;
      case "||"       : body = new Concat(info, arg1, arg2); break;
      case "|":
      case "union"    : body = new Union(info, arg1, arg2); break;
      case "except"   : body = new Except(info, arg1, arg2); break;
      case "intersect": body = new Intersect(info, arg1, arg2); break;
      case "to"       : body = new Range(info, arg1, arg2); break;
      case "otherwise": body = new Otherwise(info, arg1, arg2); break;
      default         : throw UNKNOWNOP_X.get(info, operator);
    }
    final FuncType ft = FuncType.get(body.seqType(), SeqType.ITEM_ZM, SeqType.ITEM_ZM);
    return new FuncItem(info, body, params, AnnList.EMPTY, ft, pl, null);
  }
}
