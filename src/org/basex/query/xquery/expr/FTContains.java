package org.basex.query.xquery.expr;

import org.basex.data.Data;
import org.basex.index.FTIndexAcsbl;
import org.basex.index.FTTokenizer;
import org.basex.query.xpath.XPText;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQOptimizer;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;
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
  public final FTTokenizer ft = new FTTokenizer();
  /** Flag for index use. */
  private boolean iu;
  /** NodeIter for Path Expression. */
  public NodeIter v1;
  /** Current FTNodeItem. */
  public FTNodeItem ftn;

  
  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTContains(final Expr... ex) {
    super(ex);
  }

  /**
   * Constructor.
   * @param indexuse flag for index use
   * @param ex contains, select and optional ignore expression
   */
  private FTContains(final boolean indexuse, final Expr... ex) {
    super(ex);
    iu = indexuse;
  }

  /**
   * Check if theres anything to sum up.
   * @param d data
   * @return boolean sum up
   */
  public boolean sumUp(final Data d) {
    return expr[0] instanceof Path && ((Path) expr[0]).sumUp(d);
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    expr[0] = XQOptimizer.addText(expr[0], ctx);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {    
    if (!iu) return iterWithOutIndex(ctx);
    return iterIndex(ctx);
   }


  
  /**
   * Processing without using the index.
   * @param ctx context
   * @return iterator with results
   * @throws XQException Exception
   */
  public Iter iterIndex(final XQContext ctx) throws XQException {
    v1 = (NodeIter) ctx.iter(expr[0]);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
        
    final FTNodeIter fti = (FTNodeIter) ctx.iter(expr[1]);
    if (ftn == null) 
      ftn = fti.next();
    
      DNode n;
      while((n = (DNode) v1.next()) != null) {
        while (ftn != null && ftn.ftn.size > 0 && n.pre > ftn.ftn.getPre()) {
          ftn = fti.next();
        }
        if(ftn != null) {
          final boolean not = ftn.ftn.not;
          if(ftn.ftn.getPre() == n.pre) {
            ftn = null;
            ctx.ftitem = tmp;
            return new Bln(!not, n.score()).iter();
          }
          if(not) {
            ctx.ftitem = tmp;
            return new Bln(true, n.score()).iter();
          }
        }
      }
      ctx.ftitem = tmp;
      return new Bln(false, 0).iter();
    }
  
  /**
   * Processing without using the index.
   * @param ctx context
   * @return iterator with results
   * @throws XQException Exception
   */
  public Iter iterWithOutIndex(final XQContext ctx) throws XQException {
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
  public void indexAccessible(final XQContext ctx, 
      final FTIndexAcsbl ia) throws XQException {
    //ia.set(false, Integer.MAX_VALUE, false);
    ia.init();
    
    if (!(ctx.item instanceof DNode)) return;
    // check if index exists
    final Data data = ((DNode) ctx.item).data;
    if(!(data.meta.ftxindex && !data.meta.ftst && expr[1] instanceof FTExpr)) 
      return;
    
    // check if index can be applied
    Step s = null;
    if (expr[0] instanceof Path) {
      final Path path = (Path) expr[0];
      if(path.expr != null && path.expr.length == 1 
          && path.root instanceof Step 
          && !(path.expr[0] instanceof Str)) s = (Step) path.expr[0];
    } else if(expr[0] instanceof Step) {
      s = (Step) expr[0];
    } else return;
    
    if (s == null) return;
    final boolean text = s.test.type == Type.TXT && 
      (s.expr == null || s.expr.length == 0);
    if(!text) return;
    ia.data = data;
    expr[1].indexAccessible(ctx, ia);    
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq) {
    Step s = null;
    final boolean p = expr[0] instanceof Path;
    if (p) {
      s = (Step) ((Path) expr[0]).expr[0];
      
    } else if (expr[0] instanceof SimpleIterStep) {
      s = (Step) expr[0];
    } else return this;
    
    final Expr ae = expr[1].indexEquivalent(ctx, ieq);
      
    ctx.compInfo(XPText.OPTFTINDEX);
    Expr ex;
    if (!ieq.seq) {
      // standard index evaluation
      ex = new FTContainsIndex(ft, expr[0], ae);
      if (p) {
        final Path pa = (Path) expr[0];
        return pa.invertPathNew(ieq.curr, ex);
      } else return Path.invertStep(s, ieq.curr, ex);
    } else {
      // sequential evaluation with index access
      ex = new FTContains(true, expr[0], ae);
      return ex;
    }
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String color() {
    return "33CC33";
  }

  @Override
  public String toString() {
    return toString(" ftcontainsS" + (iu ? "I " : " "));
  }
}
