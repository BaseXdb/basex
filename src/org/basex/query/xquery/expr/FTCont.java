package org.basex.query.xquery.expr;

import static org.basex.util.Token.*;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.FTTokenizer;
import org.basex.util.TokenBuilder;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTCont extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();

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
    final FTTokenizer tmp = ctx.ftitem;

    double d = 0;
    Item i;
    ctx.ftitem = ft;
    while((i = iter.next()) != null) {
      ft.init(i.str());
      final Item it = ctx.iter(expr[1]).next();
      d = Scoring.and(d, it.dbl());
    }
    ctx.ftitem = tmp;
    return new Bln(d != 0, d).iter();
  }

  /**
   * Normalizes the token by removing multiple whitespaces.
   * Sentences and Paragraphs are preserved.
   * @param tok token
   * @return token builder
   */
  static byte[] norm(final byte[] tok) {
    final TokenBuilder tb = new TokenBuilder();
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
    return tb.finish();
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
