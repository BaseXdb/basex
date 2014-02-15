package org.basex.io.serial.dot;

import static org.basex.core.Text.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.ft.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.*;
import org.basex.query.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This class contains formatting information for the DOT output.
 *
 * @author BaseX Team 2005-14, BSD License
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
    { "6666FF", Item.class, Seq.class, Str.class, QNm.class, Uri.class, Int.class },
    // violet
    { "9933FF", CAttr.class, CComm.class, CDoc.class, CElem.class,
                CNSpace.class, CPI.class, CTxt.class },
    { "9933CC", And.class, Or.class, Union.class, InterSect.class, Except.class },
    // pink
    { "CC3399", If.class, Quantifier.class, QueryText.WHR },
    { "CC6699", OrderBy.class },
    // red
    { "FF3333", Arith.class, CmpG.class, CmpN.class, CmpV.class, CmpR.class,
                Pos.class, FTContains.class },
    { "FF6666", FTExpr.class, Try.class, Catch.class },
    // orange
    { "AA9988", StaticFunc.class },
    { "776655", StaticFuncs.class },
    { "CC6600", Path.class },
    { "FF9900", Preds.class },
    // green
    { "009900", GFLWOR.class },
    { "339933", VarStack.class },
    { "33CC33", For.class, Let.class, List.class, Range.class, Context.class,
                QueryText.RET },
    { "66CC66", Var.class, Cast.class },
    // cyan
    { "009999", StaticFuncCall.class, StandardFunc.class, Root.class, VarRef.class,
                StaticVar.class, ValueAccess.class, RangeAccess.class,
                StringRangeAccess.class, FTIndexAccess.class },
  };

  /**
   * Returns the color for the specified string, or {@code null}.
   * @param s string string
   * @return color
   */
  static String color(final byte[] s) {
    for(final Object[] color : COLORS) {
      for(int c = 1; c < color.length; c++) {
        final Object o = color[c];
        final byte[] cl = o instanceof byte[] ? (byte[]) o :
          Token.token(o instanceof Class ? Util.className((Class<?>) o) : o.toString());
        if(Token.eq(cl, s)) return color[0].toString();
      }
    }
    return null;
  }
}
