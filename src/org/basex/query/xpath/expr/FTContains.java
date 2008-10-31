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
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathAbs;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.IntList;

/**
 * FTContains Expression; used for fulltext operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContains extends DualExpr {
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
  /** Flag for sequential evaluation. */
  boolean s = true;
  /** Flag for index use. */
  private boolean iu;
  /** Flag for indexSizes call. */
  private boolean isc = false;
  /** Flag for indexEquivalent call. */
  private boolean iec = false;

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
   * @param seq flag for sequential processing
   * @param indexuse flag for index use
   */
  public FTContains(final Expr e1, final Expr e2, final FTOpt opt,
      final boolean seq, final boolean indexuse) {
    super(e1, e2);
    option = opt;
    s = seq;
    iu = indexuse;
  }

  
  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);

    //
    if (expr1 instanceof LocPathAbs) {
    //if (expr1 instanceof LocPathAbs || expr1 instanceof LocPathRel) {
      s = true;
      //s = false;
      iu = false;
      return this;
    }

    final Item i1 = expr1 instanceof Item ? (Item) expr1 : null;
    final Item i2 = expr2 instanceof Item ? (Item) expr2 : null;
    if(i1 != null && i1.size() == 0 || i2 != null && i2.size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bool.FALSE;
    }

    XPOptimizer.addText(expr1, ctx);
    return this;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    if (expr1 instanceof Literal) {
      final Literal lit = (Literal) expr1;
      ctx.ftitem = new FTTokenizer();
      ctx.ftitem.init(lit.str());
      return expr2.eval(ctx);
    } 

    // [SG] Cause for troubles.. resulting item can have different type
    // (Bool/NodeSet). In the other XPath expressions, return type is always
    // the same (moreover, in indexEquivalent(), this class is specified as
    // argument for the Path expression; this expression expects the first
    // argument to return a node set..) A possible solution might be to split
    // this class into two in the compilation step.
    
    Item res;
    if (s) res = evalSeq(ctx);
    else if (!s && isc && !iec) res = evalWithoutIndex(ctx);
    else {
      final FTTokenizer tmp = ctx.ftitem;
      ctx.ftitem = ft;
  
      res = ctx.eval(expr2);
      if (res.bool()) {
        final FTArrayExpr ftae = (FTArrayExpr) expr2;
        final IntList il = new IntList();
        while (ftae.more()) {
          ftn = ftae.next(ctx);
          if (ftn.size == 0) break;
          //ctx.local.set(ftn.getPre());
          //ctx.local.size = 1;
          //if (expr1.eval(ctx).bool())
          il.add(ftn.getPre());
        }
        //return new NodeSet(il.finish(), ctx);
        res = new NodeSet(il.finish(), ctx);
      }
      
      
      
      ctx.ftitem = tmp;
    }
    
  /*  if (v1 instanceof Bool) {
      if (expr1.eval(ctx).bool()) return v1;
      else return Bool.FALSE;
    } else {
      NodeSet n = (NodeSet) v1;
      ctx.local = n;
      if(expr1.eval(ctx).bool()) return n;
      else return new NodeSet(ctx);
    }*/
    
    return res;
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
  private Bool evalSeq(final XPContext ctx) throws QueryException {
    if (!iu) return evalWithoutIndex(ctx);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;

    final NodeSet ns = (NodeSet) expr1.eval(ctx); 
    if (!ns.bool()) return Bool.FALSE;
    
    if (!f) {
      v2 = ctx.eval(expr2);
      f = true;
    }

    if (v2.bool()) {
      if (expr2 instanceof FTArrayExpr) {
        final FTArrayExpr ftae = (FTArrayExpr) expr2;
        ftn = (ftn == null && ftae.more()) ? ftae.next(ctx) : ftn;
  
        if (ftn != null) {
          while (ftn.getPre() < ns.nodes[0]) {
            if (ftae.more()) ftn = ftae.next(ctx);
            else break;
          }
        
          if (ftn.size > 0) {
            final boolean not = ftn.not;
            if (ftn.getPre() == ns.nodes[0]) {
              ftn = null;
              return Bool.get(!not); // false
            }
            return Bool.get(not); //Bool.TRUE;
          }
        }
      }
     return Bool.TRUE;
   }

   ctx.ftitem = tmp;
   return Bool.FALSE;
  }

  /**
   * Performs evaluation without index access.
   * @param ctx XPContext
   * @return resulting item
   * @throws QueryException Exception
   */
  private Bool evalWithoutIndex(final XPContext ctx) throws QueryException {
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
    Item it = expr1.eval(ctx);
    if(it instanceof NodeSet) {
      it = evalNS(ctx, it);
    } else {
      it = expr2.eval(ctx);
    }
    ctx.ftitem = tmp;
    return Bool.get(it.bool());
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

    final NodeSet nodes = (NodeSet) it;
    final Data data = nodes.data;
    for(int n = 0; n < nodes.size; n++) {
      ft.init(data.atom(nodes.nodes[n]));
      if(expr2.eval(ctx).bool()) return Bool.TRUE;
    }
    return Bool.FALSE;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {
    
    if(!(expr1 instanceof LocPathRel)) return this;

    //s = seq;
    final LocPath path = (LocPath) expr1;
    ctx.compInfo(OPTFTINDEX);

    // all FTArrayExpr are recursively converted to for indexaccess
    final FTArrayExpr ae = (FTArrayExpr)
      (iu ? expr2.indexEquivalent(ctx, curr, seq) : expr2);
      
    Expr expr;
    iec = true;
    if (!seq) {
      // standard index evaluation
      expr = new FTContains(expr1, ae, option, seq, iu);
      if (curr != null) return new Path(expr, path.invertPath(curr));
      else return expr;
    } else {
      // sequential evaluation
      if (!iu) {
        // without index access
        expr = new FTContains(expr1, expr2, option, seq, iu);
      } else {
        // with index access
        expr = new FTContains(expr1, ae, option, seq, iu);
      }
      if (curr == null) return expr;
      return new Path(expr, path);
    }
  }

   @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
     isc = true;
     s = true;
    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.item.data.meta;
    if(!(expr1 instanceof LocPathRel && meta.ftxindex))
      return Integer.MAX_VALUE;

    // check if index can be applied
    final LocPath path = (LocPathRel) expr1;
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // to the index options..
    iu = ((FTArrayExpr) expr2).indexOptions(meta);
    ctx.iu = iu;
    if(!iu) {
      // sequential processing necessary - no index use
      return Integer.MAX_VALUE;
    }

    // Integer.MAX_VALUE is return if an ftnot does not occure
    // after an ftand
    final int nrIDs = expr2.indexSizes(ctx, curr, min);
    if (nrIDs < Integer.MAX_VALUE && nrIDs > -1) {
      s = false;
    }
    if (nrIDs == -1) expr2 = Bool.TRUE;
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }

  @Override
  public String toString() {
    return expr1 + " ftcontains " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement();
  }
}
