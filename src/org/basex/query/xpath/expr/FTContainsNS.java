package org.basex.query.xpath.expr;


import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.IntList;

/**
 * FTContains Expression; used for fulltext operations.
 * Called when path could be inverted and index accessed.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsNS extends DualExpr {
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
    
    expr1 = expr1.comp(ctx);
    expr2 = expr2.comp(ctx);

    XPOptimizer.addText(expr1, ctx);
    return this;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
      final FTTokenizer tmp = ctx.ftitem;
      ctx.ftitem = ft;
      
      Item res = ctx.eval(expr2);
      if (res.bool()) {
        final FTArrayExpr ftae = (FTArrayExpr) expr2;
        final IntList il = new IntList();
        while (ftae.more()) {
          ftn = ftae.next(ctx);
          if (ftn.size == 0) break;
          il.add(ftn.getPre());
        }
        res = new NodeSet(il.finish(), ctx);
        ctx.item = (NodeSet) res;
      }
      ctx.ftitem = tmp;
    
      return res;
  }

  @Override
  public String toString() {
    return expr1 + " ftcontainsNS " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement();
  }
}
