package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Path;
import org.basex.util.TokenBuilder;

/**
 * This class is only internally used for index optimizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public final class IndexMatch extends Expr {
  /** LocationPath that contains the index reference nodes. */
  private final Path path;
  /** Expression that finds nodes (using the index). */
  private final Expr expr;
  /** Expression that matches the result set. */
  private final Path match;

  /**
   * Constructor.
   * @param p input path
   * @param exp expression that uses the index
   * @param m matching path
   */
  public IndexMatch(final Path p, final Expr exp, final Path m) {
    path = p;
    expr = exp;
    match = m;
  }

  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    // evaluate standard path
    final Iter i = path.iter(ctx); 
    if (i == Iter.EMPTY) return i;
    //final Item old = ctx.item;
    Item it;
    
    while((it = i.next())  != null) {
      ctx.item = it;
      //final Iter i2 = expr.iter(ctx);
        
    }
    return null;
  }
    
/*    return new Iter{
      Iter i0 = null;
      boolean f;
      
      public Iter next() {
        if (i0 == null) {
          i0 = path.iter(ctx); 
          if (i0 == Iter.EMPTY) return null;
          final Item o = ctx.item;
          
        }
      }
    }
//    // evaluate optimized expression
//    final NodeBuilder result = new NodeBuilder();
//    final NodeIter ni = new NodeIter();
//
//    // match rest of path
//    final Nod old = ctx.item;
//    final Nod tmp = new Nod(ctx);
//    for(final int res : ((Nod) ctx.eval(expr)).nodes) {
//      tmp.set(res);
//      ctx.item = tmp;
//      if(found(loc.nodes, match.eval(ctx).nodes)) result.add(res);
//    }
//
//    ctx.item = old;
//    return new Nod(result.finish(), ctx);
    //, ctx.local.ftidpos, ctx.local.ftpointer);
  }
*/

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    path.plan(ser);
    expr.plan(ser);
    match.plan(ser);
    ser.closeElement();
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder();
    sb.add(name());
    sb.add("(");
   // if(path.steps.size() != 0) sb.add(path + ", ");
    sb.add(expr + "[");
    sb.add(match.toString() + "])");
    return sb.toString();
  }
}
