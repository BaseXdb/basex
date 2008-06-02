package org.basex.query.xquery.expr;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.TokenBuilder;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTCont extends Arr {
  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTCont(final Expr... ex) {
    super(ex);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr[0]);
    final TokenBuilder tb = new TokenBuilder();
    Item i;

    boolean more = false;
    while((i = iter.next()) != null) {
      if(more) tb.add(' ');
      norm(tb, i.str());
      more = true;
    }
    ctx.ftitem = tb.finish();
    if(ctx.ftitem.length == 0) return Bln.FALSE.iter();

    final Item it = ctx.iter(expr[1]).next();
    return new Bln(it.bool(), it.dbl()).iter();
  }

  /**
   * Normalizes the token by removing multiple whitespaces.
   * Sentences and Paragraphs are preserved.
   * @param tb token builder
   * @param tok token
   * @return token builder
   */
  static TokenBuilder norm(final TokenBuilder tb, final byte[] tok) {
    final int l = tok.length;
    boolean ws1 = true;
    for(int i = 0; i < l; i += cl(tok[i])) {
      final int t = cp(tok, i);
      final boolean ws2 = !Character.isLetterOrDigit(t) && t != '.' &&
        t != '!' && t != '?';
      final boolean nl = t == '\n' || t == '\r';
      if(ws2 && ws1) {
        if(nl) {
          if(i == 0) tb.addUTF(t);
          else if(tb.size != 0) tb.replace((byte) t, tb.size - 1);
        }
      } else {
        tb.addUTF(ws2 && !nl ? (byte) ' ' : t);
        ws1 = ws2;
      }
    }
    tb.trim();
    return tb;
  }

  @Override
  public String toString() {
    return toString(" ftcontains ");
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }
}
