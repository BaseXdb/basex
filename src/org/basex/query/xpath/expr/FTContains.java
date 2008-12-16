package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.query.FTOpt;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.path.LocPath;
import org.basex.query.xpath.path.LocPathAbs;
import org.basex.query.xpath.path.LocPathRel;
import org.basex.query.xpath.path.Step;
import org.basex.query.xpath.path.TestNode;

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
  /** Temporary result node.*/
  private FTNode ftn = null;
  /** Flag for index use. */
  private boolean iu;
  /** Flag for index use. */
  private boolean iut;
  

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

    if(expr[0] instanceof LocPathAbs) return this;

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
    final boolean tmp  = ctx.iu;
    ctx.iu = iu;
    final Item r = iu ? evalIndex(ctx) : evalNoIndex(ctx);  
    ctx.iu = tmp;
    return r;
  }

  /**
   * Sequential evaluation - used for. 
   *  - not expression
   *  - more then one predicate
   *  
   * @param ctx XPContext
   * @return resulting result
   * @throws QueryException Exception
   */
  private Bln evalIndex(final XPContext ctx) throws QueryException {
    final FTTokenizer fti = ctx.ftitem;
    ctx.ftitem = ft;
    
    // evaluate index requests only once
    if(v2 == null) v2 = ctx.eval(expr[1]);

    if(v2.bool()) {
      final Nod ns = (Nod) expr[0].eval(ctx);
      if (expr[1] instanceof FTArrayExpr) {
        final FTArrayExpr ftae = (FTArrayExpr) expr[1];
        ftn = (ftn == null && ftae.more()) ? ftae.next(ctx) : ftn;
        for(int z = 0; z < ns.size(); z++) {
          while(ftn != null && ns.nodes[z] > ftn.getPre() && ftn.size > 0) {
            ftn = ftae.more() ? ftae.next(ctx) : null;
          }
          if(ftn != null) {
            final boolean not = ftn.not;
            if(ftn.getPre() == ns.nodes[z]) {
              ftn = null;
              ctx.ftitem = fti;
              return Bln.get(!not);
            }
            if(not) {
              ctx.ftitem = fti;
              return Bln.TRUE;
            }
          }
        }
      } else {
        final Bln b = Bln.get(ns.bool() && ((Bln) expr[1]).bool());
        ctx.ftitem = fti;
        // expression can only be FTArrayExpr or boolean
        return b; 
      }
    }
    ctx.ftitem = fti;
    return Bln.FALSE;
  }

  /**
   * Performs evaluation without index access.
   * @param ctx XPContext
   * @return resulting item
   * @throws QueryException Exception
   */
  private Bln evalNoIndex(final XPContext ctx) throws QueryException {
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;

    final Nod c = ctx.item;
    final Item it = expr[0].eval(ctx);
    
    boolean found = false;
    if(it instanceof Nod) {
      ctx.item = (Nod) it;
      
      final Nod ns = (Nod) it;
      for(int n = 0; n < ns.size; n++) {
        ft.init(ns.data.atom(ns.nodes[n]));
        found = expr[1].eval(ctx).bool();
        if(found) break;
      }
    } else {
      ft.init(it.str());
      found = expr[1].eval(ctx).bool();
    }
    
    ctx.ftitem = tmp;
    ctx.item = c;
    return Bln.get(found);
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {

    if(!(expr[0] instanceof LocPathRel)) return this;
    final LocPath path = (LocPath) expr[0];
    iu = iut;
    // all expressions are recursively converted for index access
    final FTArrayExpr ae = (FTArrayExpr)
      (iu ? expr[1].indexEquivalent(ctx, curr, seq) : expr[1]);
      
    if(!seq) {
      // standard index evaluation
      ctx.compInfo(OPTFTINDEX);
      final Expr ex = new FTContainsNS(expr[0], ae);
      return curr == null ? ex : new Path(ex, path.invertPath(curr));
    }

    // sequential evaluation
    Expr ex = null;
    if(!iu) {
      // without index access
      ex = new FTContains(expr[0], expr[1], opt, iu);
    } else {
      // with index access
      ctx.compInfo(OPTFTINDEX);
      ex = new FTContains(expr[0], ae, opt, iu);
    }
    return curr == null ? ex : new Path(ex, path);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.item.data.meta;
    if(!(expr[0] instanceof LocPathRel && meta.ftxindex && !meta.ftst))
      return Integer.MAX_VALUE;

    // check if index can be applied
    final LocPath path = (LocPathRel) expr[0];
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // with the index options..
    //iu = ((FTArrayExpr) expr[1]).indexOptions(meta);
    iut = ((FTArrayExpr) expr[1]).indexOptions(meta);
    final boolean tmp = ctx.iu;
//    ctx.iu = iu;
    ctx.iu = iut;

    // sequential processing necessary - no index use
    //if(!iu) return Integer.MAX_VALUE;
    if(!iut) return Integer.MAX_VALUE;

    // Integer.MAX_VALUE is returned if an ftnot does not occur after an ftand
    final int nrIDs = expr[1].indexSizes(ctx, curr, min);
    if (!meta.ftoptpreds && nrIDs > min) {
        // only one ftcontains predicate should use the index
        iu = false;
        ctx.iu = false;
        return min;
    }
/*    if (nrIDs > min) {
      // comment this, if every ftcontains pred should use the index
      iu = false;
      ctx.iu = false;
      return min;
    }
  */  
    //iu = ctx.iu;
    iut = ctx.iu;
    // sequential processing necessary - no index use
    //if(!iu) return Integer.MAX_VALUE;
    if(!iut) return Integer.MAX_VALUE;

    ctx.iu = tmp;
    if (nrIDs == -1) expr[1] = Bln.TRUE;
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }

  @Override
  public String color() {
    return "33CC33";
  }
  
  @Override
  public String toString() {
    return expr[0] + " ftcontains" + (iu ? "_I " : " ")  + expr[1];
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
