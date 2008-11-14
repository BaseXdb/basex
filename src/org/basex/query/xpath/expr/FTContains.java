package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathAbs;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestName;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.util.IntList;

/**
 * FTContains Expression; used for fulltext operations.
 * Used for sequential processing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContains extends Arr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();
  /** FullText options. */
  private FTOpt opt;
  /** Result item. */
  private Item v2 = null;
  /** Flag for initial run. */
  private boolean f = false;
  /** Temporary result node.*/
  private FTNode ftn = null;
  /** Flag for index use. */
  private boolean iu;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public FTContains(final Expr e1, final FTArrayExpr e2) {
    super(e1, e2);
  }

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param o fulltext options
   * @param indexuse flag for index use
   */
  public FTContains(final Expr e1, final Expr e2, final FTOpt o,
      final boolean indexuse) {
    super(e1, e2);
    opt = o;
    iu = indexuse;
  }
  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);

    if (expr[0] instanceof LocPathAbs) {
      iu = false;
      return this;
    } 

    final Item i1 = expr[0] instanceof Item ? (Item) expr[0] : null;
    final Item i2 = expr[1] instanceof Item ? (Item) expr[1] : null;
    if(i1 != null && i1.size() == 0 || i2 != null && i2.size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bln.FALSE;
    }

    XPOptimizer.addText(expr[0], ctx);
    return this;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    return ctx.iu ? evalSeq(ctx) : evalWithoutIndex(ctx);
  }

  /**
   * Sequential evaluation - used for. 
   *  - for not expression
   *  - more then one predicate
   *  
   * @param ctx XPContext
   * @return resulting result
   * @throws QueryException Exception
   */
  private Bln evalSeq(final XPContext ctx) throws QueryException {
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;

    final Nod ns = (Nod) expr[0].eval(ctx); 
    if (!ns.bool()) return Bln.FALSE;
    
    if (!f) {
      v2 = ctx.eval(expr[1]);
      f = true;
    }

    if(v2.bool()) {
      if(expr[1] instanceof FTArrayExpr) {
        final FTArrayExpr ftae = (FTArrayExpr) expr[1];
        ftn = (ftn == null && ftae.more()) ? ftae.next(ctx) : ftn;
        for(int z = 0; z < ns.size(); z++) {
          while(ftn != null && ns.nodes[z] > ftn.getPre()) {
            ftn = ftae.more() ? ftae.next(ctx) : null;
          }
          if(ftn != null) {
            final boolean not = ftn.not;
            if(ftn.getPre() == ns.nodes[z]) {
              return Bln.get(!not);
            }
          }
        }
      } else if(expr[1] instanceof Bln) {
        if(expr[0].eval(ctx).bool()) return (Bln) expr[1];
      }
      return Bln.TRUE;
    }

    ctx.ftitem = tmp;
    return Bln.FALSE;
  }

  /**
   * Performs evaluation without index access.
   * @param ctx XPContext
   * @return resulting item
   * @throws QueryException Exception
   */
  private Bln evalWithoutIndex(final XPContext ctx) throws QueryException {
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;

    final Nod c = ctx.item;
    final Item it = expr[0].eval(ctx);
    
    if (!it.bool()) return Bln.FALSE;
    Item res;
    if(it instanceof Nod) {
      ctx.item = (Nod) it;
      res = evalNS(ctx, it);
    } else if (it instanceof Bln) {
      res = expr[1].eval(ctx);
    } else {
      ctx.ftitem = new FTTokenizer(it.str());
      res = expr[1].eval(ctx);
    }
    
    ctx.ftitem = tmp;
    ctx.item = c;  
    return Bln.get(res.bool());
  }

  /**
   * Performs evaluation without index access for the specified node set.
   * @param ctx XPContext
   * @param it item
   * @return resulting item
   * @throws QueryException Exception
   */
  private Item evalNS(final XPContext ctx, final Item it)
      throws QueryException {

    final Nod nodes = (Nod) it;
    IntList res = new IntList();
    final Data data = nodes.data;
    for(int n = 0; n < nodes.size; n++) {
      ft.init(data.atom(nodes.nodes[n]));
      ctx.ftitem = ft;
      if(expr[1].eval(ctx).bool()) res.add(nodes.nodes[n]);
    }
    return new Nod(res.finish(), ctx);
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {
    
    if(!(expr[0] instanceof LocPathRel)) return this;
    final LocPath path = (LocPath) expr[0];
    
    // all FTArrayExpr are recursively converted to for index access
    final FTArrayExpr ae = (FTArrayExpr)
      (iu ? expr[1].indexEquivalent(ctx, curr, seq) : expr[1]);
      
    Expr ex;
    if (!seq) {
      // standard index evaluation
      ctx.compInfo(OPTFTINDEX);
      ex = new FTContainsNS(expr[0], ae);
      if (curr != null) return new Path(ex, path.invertPath(curr));
      else return ex;
    } else {
      // sequential evaluation
      if (!iu) {
        // without index access
        ex = new FTContains(expr[0], expr[1], opt, iu);
      } else {
        // with index access
        ctx.compInfo(OPTFTINDEX);
        ex = new FTContains(expr[0], ae, opt, iu);
      }
  
      if (curr == null) return ex;
      return new Path(ex, path);
    }
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.item.data.meta;
    if(!(expr[0] instanceof LocPathRel && meta.ftxindex))
      return Integer.MAX_VALUE;

    // check if index can be applied
    final LocPath path = (LocPathRel) expr[0];
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // with the index options..
    iu = ((FTArrayExpr) expr[1]).indexOptions(meta);
    ctx.iu = iu;
    if(!iu) {
      // sequential processing necessary - no index use
      return Integer.MAX_VALUE;
    }

    // Integer.MAX_VALUE is return if an ftnot does not occur
    // after an ftand
    final int nrIDs = expr[1].indexSizes(ctx, curr, min);
    if (nrIDs == -1) expr[1] = Bln.TRUE;
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }

  /**
   * Sum up two FTContains Expr to one.
   * If Summing up isn't possible, return null.
   * 
   * @param ftc1 FTContains 1
   * @param ftc2 FTContains 2
   * @param and Flag for FTAnd or FTOr
   * @return FTContains or null
   */
  public static Expr sumUp(final FTContains ftc1, final FTContains ftc2, 
      final boolean and) {
    final LocPathRel l1 = (LocPathRel) ftc1.expr[0];
    final LocPathRel l2 = (LocPathRel) ftc2.expr[0];
    
    if ((l1.steps.get(0).test instanceof TestName ||
        l1.steps.get(0).test == TestNode.TEXT)
        && l1.steps.get(0).test.sameAs(l2.steps.get(0).test) 
        && l1.steps.size() == 1) {
      // sum 
      if (check(ftc1, ftc2)) {
        final FTSelect fts1 = (FTSelect) ftc1.expr[1];
        final FTSelect fts2 = (FTSelect) ftc2.expr[1];
        if (and) {
          if (fts2.getExpr() instanceof FTAnd) {
            FTAnd ftand = (FTAnd) fts2.getExpr();
            ftand.add(fts1.getExpr());
          } else {
            final FTAnd fta = new FTAnd(new FTArrayExpr[]{
              fts1.getExpr(), fts2.getExpr()});
            fts2.setExpr(fta);
          }
          return ftc2;
        } else {
          if (fts2.getExpr() instanceof FTOr) {
            FTOr ftor = (FTOr) fts2.getExpr();
            ftor.add(fts1.getExpr());
          } else {
            final FTOr ftor = new FTOr(new FTArrayExpr[]{
              fts1.getExpr(), fts2.getExpr()});
            fts2.setExpr(ftor);
          } 
        }
        //return ftc2;
      } else {
        FTSelect fts;
        if (and) {
          FTAnd fta = new FTAnd(new FTArrayExpr[]{
              (FTArrayExpr) ftc2.expr[1], (FTArrayExpr) ftc1.expr[1]});
          fts = new FTSelect(fta, new FTPositionFilter(new FTPos()));          
        } else {
          FTAnd fta = new FTAnd(new FTArrayExpr[]{
              (FTArrayExpr) ftc2.expr[1], (FTArrayExpr) ftc1.expr[1]});
          fts = new FTSelect(fta, new FTPositionFilter(new FTPos()));
        }
        ftc2.expr[1] = fts;
      }
      return ftc2;
    }
    return null;
  }
  
  /**
   * Check if two FTContains expressions could be summed up.
   * @param ftc1 FTContains expression1 
   * @param ftc2 FTContains expression2
   * @return boolean result
   */
  private static boolean check(final FTContains ftc1, final FTContains ftc2) {
    if (ftc1.expr[1] instanceof FTSelect && ftc2.expr[1] instanceof FTSelect) {
      final FTSelect fts1 = (FTSelect) ftc1.expr[1];
      final FTSelect fts2 = (FTSelect) ftc2.expr[1];
      return fts1.checkSumUp(fts2.ftpos);
    }
    return false;
  }

  
  @Override
  public String toString() {
    return expr[0] + " ftcontainsBool" + (iu ? "_I " : " ")  + expr[1];
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
