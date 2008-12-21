package org.basex.query.xquery.path;

import static org.basex.query.xquery.path.Axis.*;
import static org.basex.query.xquery.path.Test.NODE;
import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.FTIndexAcsbl;
import org.basex.query.xquery.FTIndexEq;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CAttr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.Root;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;
import org.basex.util.Array;

/**
 * Axis Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public class AxisPath extends Path {
  /** Expression list. */
  public Step[] step;
  /** Flag for result caching. */
  private boolean cache;
  /** Cached result. */
  private Item citem;
  /** Last visited item. */
  private Item litem;

  /**
   * Constructor.
   * @param r root expression; can be null
   * @param s location steps; will at least have one entry
   */
  public AxisPath(final Expr r, final Step[] s) {
    super(r);
    step = s;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    // step optimizations will always return step instances
    for(int i = 0; i != step.length; i++) step[i] = (Step) ctx.comp(step[i]);
    super.comp(ctx);
    
    mergeDesc(ctx);
    checkEmpty();

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && !root.uses(Using.VAR);
      
    // check if steps have predicates
    boolean noPreds = true;
    for(final Step s : step) {
      // check if we have a predicate
      if(s.pred.length != 0) {
        noPreds = false;
        // check if we also find a variable
        if(s.uses(Using.VAR)) {
          cache = false;
          break;
        }
      }
    }
    
    // no caching - leave compilation
    if(!cache) return this;
    
    // no predicates, one downward step... choose iterative evaluation
    final Axis axis = step[0].axis;
    if(noPreds && step.length == 1 && axis.down)
      return new SimpleIterPath(root, step);

    // check if the root expression yields an absolute document node
    DBNode dbnode = null;
    if(root != null) {
      if(root instanceof DBNode) dbnode = (DBNode) root;
      // root expressions make only sense if an initial context item was set.
      // currently, this is always a database node, so the cast is safe.
      if(root instanceof Root) dbnode = (DBNode) ctx.iter(root).next();
    }
    cache &= dbnode != null;
    
    // no root node, which could allow index access
    if(!cache) return this;

    // skip position predicates and horizontal axes
    for(final Step s : step) if(s.uses(Using.POS) || !s.axis.vert) return this;

    // loop through all steps
    Expr result = this;
    for(int i = 0; i < step.length; i++) {
      // find cheapest index access
      final Step stp = step[i];
      FTIndexAcsbl iacs = null;
      int minp = 0;
      
      for(int p = 0; p < stp.pred.length; p++) {
        final FTIndexAcsbl ia = new FTIndexAcsbl(dbnode.data);
        stp.pred[p].indexAccessible(ctx, ia);
        if(ia.io && ia.iu) {
          if(iacs == null || iacs.is > ia.is) {
            iacs = ia;
            minp = p;
          }
        }
      }

      // no index access possible; skip remaining tests
      if(iacs == null || !iacs.io || !iacs.iu) continue;

      if(iacs.is == 0 && iacs.ftnot) {
        // no result, not operator... accept all results
        stp.pred[minp] = Bln.TRUE;
        continue;
      }

      // replace expressions for index access
      final FTIndexEq ieq = new FTIndexEq(iacs, stp);
      final Expr ie = stp.pred[minp].indexEquivalent(ctx, ieq);
      
      if(iacs.seq) {
        // do not invert path
        stp.pred[minp] = ie;
      } else {
        Step[] inv = {};
        
        // add remaining predicates
        final Expr[] newPreds = new Expr[stp.pred.length - 1];
        int c = 0;
        for(int p = 0; p != stp.pred.length; p++) {
          if(p != minp) newPreds[c++] = stp.pred[p];
        }
        
        // invert path before index step
        for(int j = i; j >= 0; j--) {
          final Axis a = step[j].axis.invert();
          if(a == null) break;
          
          if(j == 0) {
            if(a == Axis.PARENT) inv = Array.add(inv, Step.get(a, Test.DOC));
          } else {
            final Step prev = step[j - 1];
            if(prev.pred.length != 0) break;
            inv = Array.add(inv, Step.get(a, prev.test));
          }
        }

        // add predicates to check remaining path
        // invert Path - can be safely cast
        final AxisPath res = (AxisPath) ie; 
        
        if(inv.length != 0) {
          res.step[res.step.length - 1].addPred(new AxisPath(null, inv));
        }
        
        // add remaining steps
        for(int j = i + 1; j < step.length; j++) {
          res.step = Array.add(res.step, step[j]);
        }
        result = res;
        break;
      }
    }
    return result;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = root != null ? ctx.iter(root).finish() : ctx.item;

    if(cache && citem != null && litem == it && it.type == Type.DOC) {
      return citem.iter();
    }

    litem = it;
    final Item c = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;
    ctx.item = it;
    citem = eval(ctx);
    ctx.item = c;
    ctx.size = cs;
    ctx.pos = cp;
    return citem.iter();
  }
  
  /**
   * Evaluates the location path.
   * @param ctx query context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  protected Item eval(final XQContext ctx) throws XQException {
    // simple location step traversal...
    final NodIter ir = new NodIter();
    iter(0, ir, ctx);

    final NodeBuilder nb = new NodeBuilder(false);
    Nod it;
    while((it = ir.next()) != null) nb.add(it);
    return nb.finish();
  }

  /**
   * Recursive step iterator.
   * @param l current step
   * @param ni node builder
   * @param ctx query context
   * @throws XQException query exception
   */
  private void iter(final int l, final NodIter ni, final XQContext ctx)
      throws XQException {

    // cast is ok as all steps are axis steps here (see calling method)
    final NodeIter ir = (NodeIter) ctx.iter(step[l]);
    final boolean more = l + 1 != step.length;
    Nod it;
    while((it = ir.next()) != null) {
      if(more) {
        ctx.item = it;
        iter(l + 1, ni, ctx);
      } else {
        ctx.checkStop();
        ni.add(it);
      }
    }
  }

  /**
   * Merges superfluous descendant-or-self steps.
   * This method implies that all expressions are location steps.
   * @param ctx query context
   */
  private void mergeDesc(final XQContext ctx) {
    int ll = step.length;
    for(int l = 1; l < ll; l++) {
      if(!step[l - 1].simple(DESCORSELF)) continue;
      final Step next = step[l];
      if(next.axis == CHILD && !next.uses(Using.POS)) {
        Array.move(step, l, -1, ll-- - l);
        next.axis = DESC;
      }
    }
    if(ll != step.length) {
      ctx.compInfo(OPTDESC);
      step = Array.finish(step, ll);
    }
  }

  /**
   * Checks if any of the location steps will never yield results.
   * @throws XQException evaluation exception
   */
  private void checkEmpty() throws XQException {
    final int ll = step.length;
    if(ll > 0) {
      final Step s = step[0];
      if(root instanceof DBNode && (s.axis == ATTR || s.axis == PARENT ||
          s.axis == SELF && s.test != NODE) || root instanceof CAttr &&
          s.axis == CHILD) warning(s);
    }

    for(int l = 1; l < ll; l++) {
      final Step s1 = step[l];
      final Step s0 = step[l - 1];

      if(s1.axis == SELF) {
        if(s1.test == NODE) continue;

        if(s0.axis == ATTR) warning(s1);
        if(s0.test.type == Type.TXT && s1.test.type != Type.TXT)
          warning(s1);

        final QNm n1 = s1.test.name;
        final QNm n0 = s0.test.name;
        if(n0 == null || n1 == null) continue;
        if(!n1.eq(n0)) warning(s1);

      } else if(s1.axis == DESCORSELF) {
        if(s1.test == NODE) continue;
        if(s0.axis == ATTR) warning(s1);

        if(s0.test.type == Type.TXT && s1.test.type != Type.TXT)
          warning(s1);
      } else if(s1.axis == DESC || s1.axis == CHILD) {
        if(s0.axis == ATTR || s0.test.type == Type.TXT)
          warning(s1);
      }
    }
  }

  /**
   * Throws a static warning.
   * @param s step
   * @throws XQException evaluation exception
   */
  private void warning(final Step s) throws XQException {
    Err.or(COMPSELF, s);
  }
  
  
  /**
   * Inverts a location path.
   * @param r new root node
   * @param curr current location step
   * @return inverted path
   */
  public final AxisPath invertPath(final Expr r, final Step curr) {
    // hold the steps to the end of the inverted path
    final Step[] e = new Step[step.length];
    int c = 0;    
    
    // add inverted pretext steps
    Axis lastAxis = step[step.length - 1].axis.invert();
    for(int k = step.length - 2; k >= 0; k--) {
      final Step inv = Step.get(lastAxis, step[k].test, step[k].pred);
      lastAxis = inv.axis.invert();
      e[c++] = inv;
    } 
    e[c] = Step.get(lastAxis, curr.test);
    return new AxisPath(r, e);
  }

  /*
   * Checks if there is anything to sum up.
   * @param d Data
   * @return boolean sum up
  public boolean sumUp(final Data d) {
    if (step.length == 1 && root instanceof SimpleIterStep) {
      final SimpleIterStep sis = (SimpleIterStep) root;
      return sis.sumUp() && sis.test.kind == Test.Kind.NAME &&
        d.skel.desc(sis.test.name.str(), false, false).size == 0;
    }
    return false;
  }
   */
  
  @Override
  public boolean uses(final Using u) {
    return super.uses(u, step);
  }

  @Override
  public Type returned() {
    return Type.NOD;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    super.plan(ser, step);
  }

  @Override
  public String toString() {
    return toString(step);
  }
}
