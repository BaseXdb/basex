package org.basex.io.serial;

import static org.basex.core.Text.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.flwor.*;
import org.basex.query.ft.*;
import org.basex.query.func.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * This class contains formatting information for the DOT output.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DOTData {
  /** Font. */
  private static final String FONT = "Tahoma"; //"Charter BT";

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
    { "9933FF", CFrag.class },
    { "9933CC", And.class },
    { "9933CC", Or.class },
    { "9933CC", Union.class },
    { "9933CC", InterSect.class },
    { "9933CC", Except.class },
    { "9933CC", "Then" },
    { "9933CC", "Else" },
    // pink
    { "CC3399", If.class },
    { "CC3399", Quantifier.class },
    { "CC3399", "Where" },
    { "CC3399", Order.class },
    { "CC6699", OrderBy.class },
    // red
    { "FF3333", Arith.class },
    { "FF3333", CmpG.class },
    { "FF3333", CmpN.class },
    { "FF3333", CmpV.class },
    { "FF3333", CmpR.class },
    { "FF3333", Pos.class },
    { "FF3333", FTContains.class },
    { "FF6666", FTExpr.class },
    { "FF6666", Try.class },
    { "FF6666", Catch.class },
    // orange
    { "AA9988", UserFunc.class },
    { "776655", UserFuncs.class },
    { "CC6600", Path.class },
    { "FF9900", Preds.class },
    // green
    { "009900", GFLWOR.class },
    { "339933", VarStack.class },
    { "33CC33", ForLet.class },
    { "33CC33", List.class },
    { "33CC33", Range.class },
    { "33CC33", Context.class },
    { "33CC33", "Return" },
    { "66CC66", Var.class },
    { "66CC66", Cast.class },
    // cyan
    { "009999", UserFuncCall.class },
    { "00BBBB", StandardFunc.class },
    { "00BBBB", Root.class },
    { "00BBBB", VarRef.class },
    { "00BBBB", ValueAccess.class },
    { "00BBBB", RangeAccess.class },
    { "00BBBB", StringRangeAccess.class },
    { "00BBBB", FTIndexAccess.class },
  };

  /** Hash map, caching expression names. */
  private static final Object[][] NAMES = {
    { "Calculation", Arith.class },
    { "Comparison", CmpG.class },
    { "Comparison", CmpN.class },
    { "Comparison", CmpV.class },
    { "Comparison", CmpR.class },
    { "FLWOR", FLWR.class },
    { "Declaration", UserFunc.class },
    { "Function", StandardFunc.class },
    { null, Path.class },
    { "Step", AxisStep.class },
    { "Variables", VarStack.class },
    { "OrderBy", OrderByExpr.class },
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
