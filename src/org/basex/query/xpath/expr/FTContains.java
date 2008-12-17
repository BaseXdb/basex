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
public final class FTContains extends Expr {
  /** Fulltext parser. */
  private final FTTokenizer ft = new FTTokenizer();
  /** Content expression. */
  public Expr cont;
  /** FullText query. */
  public FTArrayExpr query;
  /** FullText options. */
  private FTOpt opt;
  /** Cached index results. */
  private Item cache;
  /** Temporary result node.*/
  private FTNode ftn;
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
    cont = e1;
    query = e2;
  }

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param o fulltext options
   * @param indexuse flag for index use
   */
  public FTContains(final Expr e1, final FTArrayExpr e2, final FTOpt o,
      final boolean indexuse) {
    this(e1, e2);
    opt = o;
    iu = indexuse;
  }
  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    cont = cont.comp(ctx);
    query = query.comp(ctx);

    if(cont instanceof LocPathAbs) return this;

    final Item i1 = cont instanceof Item ? (Item) cont : null;
    if(i1 != null && i1.size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bln.FALSE;
    }

    XPOptimizer.addText(cont, ctx);
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
    if(cache == null) cache = ctx.eval(query);

    if(cache.bool()) {
      final Nod ns = (Nod) cont.eval(ctx);
      // treat special boolean case
      if (query instanceof FTBool) {
        final Bln b = Bln.get(ns.bool() && query.eval(ctx).bool());
        ctx.ftitem = fti;
        return b; 
      } else {
        ftn = (ftn == null && query.more()) ? query.next(ctx) : ftn;
        for(int z = 0; z < ns.size(); z++) {
          while(ftn != null && ns.nodes[z] > ftn.getPre() && ftn.size > 0) {
            ftn = query.more() ? query.next(ctx) : null;
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
    final Item it = cont.eval(ctx);
    
    boolean found = false;
    if(it instanceof Nod) {
      ctx.item = (Nod) it;
      
      final Nod ns = (Nod) it;
      for(int n = 0; n < ns.size; n++) {
        ft.init(ns.data.atom(ns.nodes[n]));
        found = query.eval(ctx).bool();
        if(found) break;
      }
    } else {
      ft.init(it.str());
      found = query.eval(ctx).bool();
    }
    
    ctx.ftitem = tmp;
    ctx.item = c;
    return Bln.get(found);
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {

    if(!(cont instanceof LocPathRel)) return this;
    final LocPath path = (LocPath) cont;
    iu = iut;
    // all expressions are recursively converted for index access
    final FTArrayExpr ae = iu ? query.indexEquivalent(ctx, curr, seq) : query;
      
    if(!seq) {
      // standard index evaluation
      ctx.compInfo(OPTFTINDEX);
      final Expr ex = new FTContainsNS(cont, ae);
      return curr == null ? ex : new Path(ex, path.invertPath(curr));
    }

    // sequential evaluation
    Expr ex = null;
    if(!iu) {
      // without index access
      ex = new FTContains(cont, query, opt, iu);
    } else {
      // with index access
      ctx.compInfo(OPTFTINDEX);
      ex = new FTContains(cont, ae, opt, iu);
    }
    return curr == null ? ex : new Path(ex, path);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.item.data.meta;
    if(!(cont instanceof LocPathRel && meta.ftxindex && !meta.ftst))
      return Integer.MAX_VALUE;

    // check if index can be applied
    final LocPath path = (LocPathRel) cont;
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // with the index options..
    iut = query.indexOptions(meta);
    final boolean tmp = ctx.iu;
    ctx.iu = iut;

    // sequential processing necessary - no index use
    if(!iut) return Integer.MAX_VALUE;

    // Integer.MAX_VALUE is returned if an ftnot does not occur after an ftand
    final int nrIDs = query.indexSizes(ctx, curr, min);
    /*
    final double avgT = path.avgTextLength(ctx);
    final double ld = ctx.item.data.skel.tl(null);
    System.out.println("avgT=" + avgT + " ld=" + ld);
    */
    if(!meta.ftoptpreds && nrIDs > min) {
        // only one ftcontains predicate should use the index
        iu = false;
        ctx.iu = false;
        return min;
    }

    iut = ctx.iu;
    // sequential processing necessary - no index use
    if(!iut) return Integer.MAX_VALUE;

    ctx.iu = tmp;
    if (nrIDs == -1) query = new FTBool(true);
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }

  @Override
  public boolean usesSize() {
    return cont.usesSize();
  }

  @Override
  public boolean usesPos() {
    return cont.usesPos();
  }

  @Override
  public String color() {
    return "33CC33";
  }
  
  @Override
  public String toString() {
    return cont + " ftcontains" + (iu ? "_I " : " ")  + query;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    cont.plan(ser);
    query.plan(ser);
    ser.closeElement();
  }
}
