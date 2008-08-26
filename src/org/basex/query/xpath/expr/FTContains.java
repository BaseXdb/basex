package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
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
  /** FullText options (currently not used). */
  private FTOpt option;
  /** Result item. */
  private Item v1 = null;
  /** Flag for initial run. */
  private boolean f = false;
  /** Temporary result node.*/
  private FTNode ftn = null;
  /** Flag for sequential evaluation. */
  boolean s;
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

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);

    if (expr1 instanceof LocPathAbs) {
      s = true;
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
      Literal lit = (Literal) expr1;
      ctx.ftitem = new FTTokenizer();
      ctx.ftitem.init(lit.str());
      return expr2.eval(ctx);
    }
    
    if (s) return evalSeq(ctx);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
    
    v1 = ctx.eval(expr2);
    if (v1.bool()) {
      FTArrayExpr ftae = (FTArrayExpr) expr2;
      IntList il = new IntList();
      while (ftae.more()) {
        ftn = ftae.next(ctx);
        if (ftn.size == 0) break;
        il.add(ftn.getPre());
      }
      return new NodeSet(il.finish(), ctx);
    }
    
    /*
    if(v1.size() == 0) return Bool.FALSE;

    boolean res = false;
    if(v1 instanceof NodeSet) {
      NodeSet ns = (NodeSet) v1;
      for(int i = 0; i < ns.size; i++) {
        ft.init(ctx.local.data.atom(ns.nodes[i]));
        res = ctx.eval(expr2).bool();
        if(res) break;
      }
    } else {
      ft.init(v1.str());
      res = ctx.eval(expr2).bool();
    }
    */
    ctx.ftitem = tmp;
    /*return Bool.get(res);
    
    for(int i = 0; i < size; i++) {
      if(Token.ftcontains(data.atom(nodes[i]), qu)) return true;
    }
    
    // don't evaluate empty node sets
    return Bool.get(v1.size() != 0 && v2.size() != 0 && type.eval(v1, v2));
    */
    return Bool.FALSE;
  }

  /**
   * Sequential evaluation - used for not expression. 
   * @param ctx XPContext
   * @return resulting result
   * @throws QueryException Exception
   */
  public Bool evalSeq(final XPContext ctx) throws QueryException {
    if (!iu) return evalWithoutIndex(ctx);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;

   if (!f) {
      v1 = ctx.eval(expr2);
      f = true;
    }
    
   if (v1.bool()) {
     FTArrayExpr ftae = (FTArrayExpr) expr2;
     ftn = (ftn == null && ftae.more()) ? ftae.next(ctx) : ftn;
     
     if (ftn != null) {
        while (ftn.getPre() < ctx.local.nodes[0] + 1) {
          if (ftae.more()) ftn = ftae.next(ctx);
          else break;
        }
        if (ftn.size > 0) {
          final boolean not = ftn.not;
          if (ftn.getPre() == ctx.local.nodes[0] + 1) {
            ftn = null;
            return Bool.get(!not); // false
          } else {
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
  public Bool evalWithoutIndex(final XPContext ctx) throws QueryException {
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
  public Item evalNS(final XPContext ctx, final Item it) throws QueryException {
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

    s = seq;
    final LocPath path = (LocPath) expr1;
    ctx.compInfo(OPTFTINDEX);
    
    // all FTWords are recursively converted to FTIndex requests
    //final Expr expr = expr2.indexEquivalent(ctx, curr);
    final FTArrayExpr ae = (FTArrayExpr) 
      (iu ? expr2.indexEquivalent(ctx, curr, seq) : expr2);
    Expr expr;
    if (!seq) {
      expr = new FTContains(expr1, ae, option);
      return new Path(expr, path.invertPath(curr));
    } else {
      if (!iu) expr = new FTContains(expr1, expr2, option);
      else expr = new FTContains(expr1, ae, option);
      return new Path(expr, path);
    }
  } 
  
   @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {

    // check if first expression is a location path and if fulltext index exists
    final MetaData meta = ctx.local.data.meta;
    if(!(expr1 instanceof LocPathRel && meta.ftxindex))
      return Integer.MAX_VALUE;
    
    // check if index can be applied
    final LocPath path = (LocPathRel) expr1;
    final Step step = path.steps.last();
    final boolean text = step.test == TestNode.TEXT && step.preds.size() == 0;
    if(!text || !path.checkAxes()) return Integer.MAX_VALUE;

    //if(expr2 instanceof FTUnaryNot) return Integer.MAX_VALUE;

    // check all ftcontains options recursively if they comply
    // to the index options..
    iu = ((FTArrayExpr) expr2).indexOptions(meta);
    if(!iu) return Integer.MAX_VALUE;
 
    // TODO... check number of index results
    //final byte[] token = expr2.str();
    //final int nrIDs = ctx.local.data.nrFTIDs(token);
    
    // Integer.MAX_VALUE is returned if every textnode is a result
    final int nrIDs = expr2.indexSizes(ctx, curr, min);
    if (nrIDs == Integer.MAX_VALUE) {
      // <SG> every text node is a hit
      
    }
    return nrIDs == -1 ? Integer.MAX_VALUE : nrIDs;
  }
  
  @Override
  public String toString() {
    return expr1 + " ftcontainsIndex " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement(this);
  }
}
