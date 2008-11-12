package org.basex.query.xquery.expr;

import static org.basex.query.xpath.XPText.*;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Path;
import org.basex.query.xquery.path.SimpleIterStep;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.util.Scoring;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContains extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();

  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTContains(final Expr... ex) {
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

  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexInfo ii, 
      final Step curr) {
    if(!(expr[0] instanceof SimpleIterStep)) return this;
    final SimpleIterStep sis = (SimpleIterStep) expr[0];
    final Expr ae = expr[1]; //expr[1].indexEquivalent(ctx, ii, curr);
      
    Expr ex;
    if (!ii.seq) {
      // standard index evaluation
      ctx.compInfo(OPTFTINDEX);
      //ex = new FTContainsNS(expr[0], ae);
      ex = new FTContains(expr[0], ae);
      if (curr != null) return Path.invertSIStep(sis, curr, this);
      else return ex;
    } else {
      /*
      // sequential evaluation
      if (!iu) {
        // without index access
        ex = new FTContains(expr[0], expr[1], option, iu);
      } else {
        // with index access
        ctx.compInfo(OPTFTINDEX);
        ex = new FTContains(expr[0], ae, option, iu);
      }
  
      if (curr == null) return ex;
      return new Path(ex, path);
      */
    }
    return null;
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
