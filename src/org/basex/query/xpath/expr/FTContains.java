package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.Data;
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
import org.basex.query.xpath.item.Str;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathAbs;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
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
  private FTOpt option;
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
   * @param opt fulltext options
   */
  public FTContains(final Expr e1, final Expr e2, final FTOpt opt) {
    super(e1, e2);
    option = opt;
  }

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param opt fulltext options
   * @param indexuse flag for index use
   */
  public FTContains(final Expr e1, final Expr e2, final FTOpt opt,
      final boolean indexuse) {
    super(e1, e2);
    option = opt;
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
    if (expr[0] instanceof Str) {
      final Str lit = (Str) expr[0];
      ctx.ftitem = new FTTokenizer();
      ctx.ftitem.init(lit.str());
      return expr[1].eval(ctx);
    } 

    return iu ? evalSeq(ctx) : evalWithoutIndex(ctx);
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
    Item it = expr[0].eval(ctx);
    if (!it.bool()) return Bln.FALSE;
    if(it instanceof Nod) {
      it = evalNS(ctx, it);
    } else {
      it = expr[1].eval(ctx);
    }
    ctx.ftitem = tmp;
    return Bln.get(it.bool());
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
      if(expr[1].eval(ctx).bool()) res.add(nodes.nodes[n]);
    }
    if (res.size > 0) {
      ctx.item = new Nod(res.finish(), ctx);
      return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {
    
    if(!(expr[0] instanceof LocPathRel)) return this;

    //s = seq;
    final LocPath path = (LocPath) expr[0];
    
    // all FTArrayExpr are recursively converted to for index access
    final FTArrayExpr ae = (FTArrayExpr)
      (iu ? expr[1].indexEquivalent(ctx, curr, seq) : expr[1]);
      
    Expr ex;
    if (!seq) {
      // standard index evaluation
      //expr = new FTContains(expr[0], ae, option, seq, iu);
      ctx.compInfo(OPTFTINDEX);
      ex = new FTContainsNS(expr[0], ae);
      if (curr != null) return new Path(ex, path.invertPath(curr));
      else return ex;
    } else {
      // sequential evaluation
      if (!iu) {
        // without index access
        ex = new FTContains(expr[0], expr[1], option, iu);
      } else {
        // with index access
        ctx.compInfo(OPTFTINDEX);
        ex = new FTContains(expr[0], ae, option, iu);
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
  /*  if (nrIDs < Integer.MAX_VALUE && nrIDs > -1) {
      s = false;
    }*/
    if (nrIDs == -1) expr[1] = Bln.TRUE;
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
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
