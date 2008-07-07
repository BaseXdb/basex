package org.basex.query.xquery.expr;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.util.Token;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Pred extends Arr {
  /** Expression. */
  Expr root;

  /**
   * Constructor.
   * @param r expression
   * @param e predicates
   */
  public Pred(final Expr r, final Expr[] e) {
    super(e);
    root = r;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    root = root.comp(ctx);
    super.comp(ctx);
    // if in Using.POS is a predicate that has the last() or
    // position() function, it will be processed in PredIter
    return uses(Using.POS) ? new PredIter(root, expr) : this; 
  }  

  /*
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {

      Iter iter = ctx.iter(root);

      final Item ci = ctx.item;
      final int cp = ctx.pos;
      
      int predCount = 0;
      int predCountTemp = predCount - 1;
      
      Item i;
      
      @Override
      public Item next() throws XQException {

        // evaluates predicates
        while (predCount < expr.length) {
          
          // looks if it's a new predicate
          if ((predCount - 1) == predCountTemp) {
            predCountTemp++;
            ctx.pos = 1;
          }

          while ((i = iter.next()) != null) {
            ctx.item = i;
            i = ctx.iter(expr[predCount]).ebv();
            if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
              ctx.pos++;
              return i;
            }
            ctx.pos++;
          }
          predCount++;
        }

        ctx.item = ci;
        ctx.pos = cp;
        return null;
      }
    };
  } */
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    Iter iter = ctx.iter(root);

    final Item ci = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;
    
    // cache results to support last() function
    final SeqIter sb = new SeqIter();
    Item i;
    while((i = iter.next()) != null) sb.add(i);

    // evaluates predicates
    for(final Expr p : expr) {
      ctx.size = sb.size;
      ctx.pos = 1;
      int c = 0;
      for(int s = 0; s < sb.size; s++) {
        ctx.item = sb.item[s];
        i = ctx.iter(p).ebv();
        if(i.n() ? i.dbl() == ctx.pos : i.bool()) sb.item[c++] = sb.item[s];
        ctx.pos++;
      }
      sb.size = c;
    }

    ctx.item = ci;
    ctx.size = cs;
    ctx.pos = cp;
    return sb;
  } 
  
  @Override
  public boolean uses(final Using u) {
    switch(u) {
      case POS:
        for(final Expr e : expr) {
          final Type t = e.returned();
          
          // I need to find a way to get access to e.func.desc or 
          // e.func.name because there is the last() or position() 
          // function described and I didn't find it anywhere else.

          if(t == null || t.num || e.uses(u)) return false;
        }
        return super.uses(u);
      default:
        return super.uses(u);
    }
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + root + ", " + toString(", ") + ")";
  }

  @Override
  public String info() {
    return "Predicate";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF6666";
  }
}
