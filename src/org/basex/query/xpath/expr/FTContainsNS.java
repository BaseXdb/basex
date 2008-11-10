package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.util.IntList;

/**
 * FTContains Expression; used for fulltext operations.
 * Called when path could be inverted and index accessed.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsNS extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();
  /** FullText options. */
  /** Temporary result node.*/
  private FTNode ftn = null;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContainsNS(final Expr e1, final FTArrayExpr e2) {
    super(e1, e2);
  }

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContainsNS(final Expr e1, final Expr e2) {
    super(e1, e2);
  }

  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);

    XPOptimizer.addText(expr[0], ctx);
    return this;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
      final FTTokenizer tmp = ctx.ftitem;
      ctx.ftitem = ft;
      
      Item res = ctx.eval(expr[1]);
      if (res.bool()) {
        final FTArrayExpr ftae = (FTArrayExpr) expr[1];
        final IntList il = new IntList();
        while (ftae.more()) {
          ftn = ftae.next(ctx);
          if (ftn.size == 0) break;
          il.add(ftn.getPre());
        }
        res = new Nod(il.finish(), ctx);
        ctx.item = (Nod) res;
      } else {
        res = new Nod(ctx);
      }
      ctx.ftitem = tmp;
    
      return res;
  }

  @Override
  public String toString() {
    return expr[0] + " ftcontainsNS " + expr[1];
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
