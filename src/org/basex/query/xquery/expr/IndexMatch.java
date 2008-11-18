package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.path.Path;
import org.basex.util.TokenBuilder;

/**
 * This class is only internally used for index optimizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
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
  public Expr comp(final XQContext ctx) throws XQException {
    return ctx.comp(this);
    //return this;
  };
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    // evaluate standard path
    final Iter pathI = ctx.iter(path);
    if(pathI.size() == 0) return pathI;
    
    // evaluate optimized expression
    final NodIter result = new NodIter();
    //final NodeBuilder result = new NodeBuilder();

    // match rest of path
    final Nod old = (Nod) ctx.item;
    Iter exprI = ctx.iter(expr);
    Item exprIm = exprI.next();
    while(exprIm != null) {
      ctx.item = exprIm;
      if (found(pathI, ctx.iter(match))) result.add((Nod) exprIm);
    }

    ctx.item = old;
    return result;
    //, ctx.local.ftidpos, ctx.local.ftpointer);
  }

  /**
   * Checks if i1 is found in i2.
   * @param i1 Iter1
   * @param i2 Iter2
   * @return boolean
   */
  private boolean found(final Iter i1, final Iter i2) {
    System.out.println(i1.toString() + i2.toString());
    //<SG> fill me
    return true;
  }
  
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
    if(path.expr.length != 0) sb.add(path + ", ");
    sb.add(expr + "[");
    sb.add(match.toString() + "])");
    return sb.toString();
  }
}
