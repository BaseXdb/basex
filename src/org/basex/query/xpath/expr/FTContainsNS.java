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
import org.basex.util.IntArrayList;
import org.basex.util.IntList;

/**
 * FTContains Expression; used for fulltext operations.
 * Called when path could be inverted and index accessed.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTContainsNS extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();
  /** FullText options. */
  /** Temporary result node.*/
  private FTNode ftn;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContainsNS(final Expr e1, final FTArrayExpr e2) {
    super(e1, e2);
  }
  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    XPOptimizer.addText(expr[0], ctx);
    return this;
  }

  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    boolean iu = ctx.iu;
    ctx.iu = true;

    final Item res = ctx.eval(expr[1]);
    if(!res.bool()) return new Nod(ctx);

    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
    
    final FTArrayExpr ftae = (FTArrayExpr) expr[1];
    final IntList il = new IntList();
    final IntArrayList poi = new IntArrayList();
    final IntArrayList pos = new IntArrayList();
    while (ftae.more()) {
      ftn = ftae.next(ctx);
      if (ftn.size == 0) break;
      il.add(ftn.getPre());
      pos.add(ftn.getPos());
      poi.add(ftn.getPoi());
    }
    ctx.item = new Nod(il.finish(), pos.finish(), poi.finish(), ctx);
    ctx.ftitem = tmp;
    ctx.iu = iu;
    return ctx.item;
  }

  @Override
  public String color() {
    return "33CC33";
  }

  @Override
  public String toString() {
    return toString(" ftcontainsNS ");
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
