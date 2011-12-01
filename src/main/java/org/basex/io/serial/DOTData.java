package org.basex.io.serial;

import static org.basex.core.Text.*;
import org.basex.data.ExprInfo;
import org.basex.util.Util;

/**
 * This class contains formatting information for the DOT output.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class DOTData {
  /** Font. */
  static final String FONT = "Tahoma"; //"Charter BT";

  /** Node entry. */
  static final String HEADER =
    "digraph BaseXAlgebra {" + NL +
    "node [shape=box style=bold width=0 height=0];" + NL +
    "node [fontsize=12 fontname=\"" + FONT + "\"];";
  /** Node entry. */
  static final String FOOTER = "}";

  /** Node entry. */
  static final String DOTNODE = "node% [label=\"%\" color=\"#%\"];";
  /** Link entry. */
  static final String DOTLINK = "node% -> node%;";
  /** Node entry. */
  static final String DOTATTR = "\\n%: %";

  /** Link entry. */
  static final String ELEM1 = "303030";
  /** Link entry. */
  static final String ELEM2 = "909090";
  /** Link entry. */
  static final String ITEM = "3366CC";
  /** Link entry. */
  static final String TEXT = "6666FF";
  /** Link entry. */
  static final String COMM = "3366FF";
  /** Link entry. */
  static final String PI = "3399FF";

  /** Private constructors. */
  private DOTData() { }

  /** Hash map, caching colors for expressions. */
  private static final Object[][] COLORS = {
    // blue
    { "6666FF", "Item" },
    { "6666FF", "sequence" },
    // violet
    { "9933FF", org.basex.query.expr.CFrag.class },
    { "9933CC", org.basex.query.expr.And.class },
    { "9933CC", org.basex.query.expr.Or.class },
    { "9933CC", org.basex.query.expr.Union.class },
    { "9933CC", org.basex.query.expr.InterSect.class },
    { "9933CC", org.basex.query.expr.Except.class },
    { "9933CC", "Then" },
    { "9933CC", "Else" },
    // pink
    { "CC3399", org.basex.query.expr.If.class },
    { "CC3399", org.basex.query.expr.Quantifier.class },
    { "CC3399", "Where" },
    { "CC3399", org.basex.query.expr.Order.class },
    { "CC6699", org.basex.query.expr.OrderBy.class },
    // red
    { "FF3333", org.basex.query.expr.Arith.class },
    { "FF3333", org.basex.query.expr.CmpG.class },
    { "FF3333", org.basex.query.expr.CmpN.class },
    { "FF3333", org.basex.query.expr.CmpV.class },
    { "FF3333", org.basex.query.expr.CmpR.class },
    { "FF3333", org.basex.query.expr.Pos.class },
    { "FF3333", org.basex.query.ft.FTContains.class },
    { "FF6666", org.basex.query.ft.FTExpr.class },
    { "FF6666", org.basex.query.expr.Try.class },
    { "FF6666", org.basex.query.expr.Catch.class },
    // orange
    { "AA9988", org.basex.query.expr.UserFunc.class },
    { "776655", org.basex.query.util.UserFuncs.class },
    { "CC6600", org.basex.query.path.Path.class },
    { "FF9900", org.basex.query.expr.Preds.class },
    // green
    { "009900", org.basex.query.expr.GFLWOR.class },
    { "339933", org.basex.query.util.VarList.class },
    { "33CC33", org.basex.query.expr.ForLet.class },
    { "33CC33", org.basex.query.expr.List.class },
    { "33CC33", org.basex.query.expr.Range.class },
    { "33CC33", org.basex.query.expr.Context.class },
    { "33CC33", "Return" },
    { "66CC66", org.basex.query.util.Var.class },
    { "66CC66", org.basex.query.expr.Cast.class },
    // cyan
    { "009999", org.basex.query.expr.UserFuncCall.class },
    { "00BBBB", org.basex.query.func.FuncCall.class },
    { "00BBBB", org.basex.query.expr.Root.class },
    { "00BBBB", org.basex.query.expr.VarRef.class },
    { "00BBBB", org.basex.query.expr.IndexAccess.class },
    { "00BBBB", org.basex.query.expr.RangeAccess.class },
    { "00BBBB", org.basex.query.ft.FTIndexAccess.class },
  };

  /** Hash map, caching expression names. */
  private static final Object[][] NAMES = {
    { "Calculation", org.basex.query.expr.Arith.class },
    { "Comparison", org.basex.query.expr.CmpG.class },
    { "Comparison", org.basex.query.expr.CmpN.class },
    { "Comparison", org.basex.query.expr.CmpV.class },
    { "Comparison", org.basex.query.expr.CmpR.class },
    { "FLWOR", org.basex.query.expr.FLWR.class },
    { "Declaration", org.basex.query.expr.UserFunc.class },
    { "Function", org.basex.query.func.FuncCall.class },
    { null, org.basex.query.path.Path.class },
    { "Step", org.basex.query.path.AxisStep.class },
    { "Variables", org.basex.query.util.VarList.class },
    { "OrderBy", org.basex.query.expr.OrderByExpr.class },
    { "operator", "op" },
  };

  /**
   * Returns the color for the specified expression, or {@code null}.
   * @param e expression
   * @return color
   */
  static String color(final ExprInfo e) {
    for(final Object[] o : COLORS) {
      if(o[1] instanceof Class<?> && ((Class<?>) o[1]).isInstance(e))
        return o[0].toString();
    }
    return null;
  }

  /**
   * Returns the color for the specified string, or {@code null}.
   * @param s string string
   * @return color
   */
  static String color(final String s) {
    for(final Object[] o : COLORS) {
      if(o[1] instanceof String && s.equals(o[1].toString())) {
        return o[0].toString();
      }
    }
    return null;
  }

  /**
   * Returns the node name for the specified expression.
   * @param e expression
   * @return name
   */
  static String name(final ExprInfo e) {
    final String name = e.info();
    for(final Object[] o : NAMES) {
      if(!(o[1] instanceof Class<?>)) continue;
      final Class<?> c = (Class<?>) o[1];
      if(!c.isInstance(e)) continue;
      return o[0] != null ? o[0].toString() : Util.name(c);
    }
    return name;
  }

  /**
   * Returns the node name for the specified string.
   * @param s string
   * @return name
   */
  static String name(final String s) {
    for(final Object[] o : NAMES) {
      if(!(o[1] instanceof String)) continue;
      final String c = (String) o[1];
      if(c.equals(s)) return o[0].toString();
    }
    return s;
  }
}
