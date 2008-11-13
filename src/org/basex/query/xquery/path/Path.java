package org.basex.query.xquery.path;

import static org.basex.query.xpath.XPText.*;
import static org.basex.query.xquery.path.Axis.*;
import static org.basex.query.xquery.path.Test.NODE;
import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.CAttr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FTIndexInfo;
import org.basex.query.xquery.expr.IndexMatch;
import org.basex.query.xquery.expr.InterSect;
import org.basex.query.xquery.expr.Pred;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Path extends Arr {
  /** Top expression. */
  public Expr root;
  /** Steps flag. */
  private boolean steps = true;
  /** Flag for result caching. */
  private boolean cache;
  /** Cached result. */
  private Item res;
  /** Cached item. */
  private Item item;

  /**
   * Constructor.
   * @param r root expression
   * @param p expression list
   */
  public Path(final Expr r, final Expr[] p) {
    super(p);
    root = r;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    root = ctx.comp(root);
    Expr e = root;

    if(expr[0] instanceof Step) {
      final Step s = (Step) expr[0];
      if(e instanceof DNode && (s.axis == ATTR || s.axis == PARENT ||
          s.axis == SELF && s.test != NODE) || e instanceof CAttr &&
          s.axis == CHILD) Err.or(COMPSELF, s);
    }

    for(int i = 0; i != expr.length; i++) {
      expr[i] = ctx.comp(expr[i]);
      steps &= expr[i] instanceof Step;
    }

    if(steps) {
      mergeDesc(ctx);
      checkEmpty();
      // analyze if result set can be cached - no predicates or no variables...
      cache = true;
      boolean noPreds = true;
      for(final Expr ex : expr) {
        // check if we have a predicate
        if(((Step) ex).expr.length != 0) {
          noPreds = false;
          // check if we also find a variable
          if(ex.uses(Using.VAR)) {
            cache = false;
            break;
          }
        }
      }
      // no predicates, one child or descendant step...
      final Axis axis = ((Step) expr[0]).axis;
      // if we've found a variable, cache will be true. But we can't 
      // handle variables in SimpleIterPath yet.
      if(!cache && noPreds && expr.length == 1 && (axis == Axis.DESC || 
          axis == Axis.DESCORSELF || axis == Axis.CHILD)) {
        return new SimpleIterPath(root, expr);
      }      
    }
    Expr result = this;
    if (FTIndexInfo.optimize && steps) {
      // check if the available indexes can be applied
      
      MAIN:
      for (int i = 0; i < expr.length; i++) {
        final Step step = (Step) expr[i];
        final Expr[] preds = step.expr;

        // don't optimize non-invertible axes
        if(invertAxis(step.axis) == null) continue;

        // find predicate with lowest number of occurrences
        boolean pos = false;
        int min = Integer.MAX_VALUE;
        int minP = -1;
        final Expr[] ie = new Expr[preds.length];
        final FTIndexInfo[] ftii = new FTIndexInfo[preds.length];
        for(int p = 0; p < preds.length; p++) {
          final Expr pred = preds[p];
          ftii[p] = new FTIndexInfo(); 
          ie[p] = pred.indexEquivalent(ctx, ftii[p], step);
          final int nrIDs = ftii[p].indexSize;

          // zero results - predicates will always yield false
          if(ftii[p].indexSize == 0) {
            ctx.compInfo(OPTLOC);
            return Bln.FALSE; //Nod(ctx);
          }

          // remember cheapest index access
          if(min > nrIDs) {
            // skip step if position predicate was found before
            if(pos) continue MAIN;
            min = nrIDs;
            minP = p;
          }
          
          // check if position predicate is found
          pos |= pred.uses(Using.POS) || pred.uses(Using.VAR);
        }

        // ..skip index evaluation for too large results and relative paths
        if(minP == -1 || ftii[minP].seq) 
          //&& this instanceof LocPathRel && min > ctx.item.data.size / 10)
          continue;

        //predicates that are optimized to use index
        final IntList oldPreds = new IntList();
        Expr indexArg = null;

        for(int p = 0; p < preds.length; p++) {
          if(ftii[minP].seq || p == minP && indexArg == null) {
            oldPreds.add(p);
            indexArg = ie[p];
          }
        }

        if (ftii[minP].seq) continue;

        // hold all steps following this step
        final Expr[] oldPath = new Expr[expr.length - i - 1];
        if (i + 1 < expr.length) {
          System.arraycopy(expr, i + 1, oldPath, 0, oldPath.length);
          Expr[] exprN = new Expr[i];
          System.arraycopy(expr, 0, exprN, 0, exprN.length);
          expr = exprN;
        }
        // hold the part of the path we want to invert.
        // suggestion: all forward steps without predicates 
        //             in front of the index
        final Path invPath = new Path(root, new Expr[]{});

        boolean indexMatch = true;
        for(int j = i; j >= 0; j--) {
          final Step curr = (Step) expr[j];
          final Axis axis = invertAxis(curr.axis);
          if(axis == null) break;

          if(j == 0) {
            if(axis == Axis.PARENT) {
              Expr[] ex = new Expr[invPath.expr.length + 1];
              System.arraycopy(invPath.expr, 0, ex, 0, invPath.expr.length);
              ex[ex.length - 1] = new Step(axis, Test.NODE, null);
             } else {
              indexMatch = false;
            }
          } else {
            final Step prev = (Step) expr[j - 1];
            if(prev.expr.length != 0) break;
            Expr[] ex = new Expr[invPath.expr.length + 1];
            System.arraycopy(invPath.expr, 0, ex, 0, invPath.expr.length);
            ex[ex.length - 1] = new Step(axis, prev.test, null);            
          }
          if (invPath.expr.length > 0) {
            Expr[] ex = new Expr[invPath.expr.length - 1];
            System.arraycopy(expr, 0, ex, 0, j - 1);
            System.arraycopy(expr, j + 1, ex, j, expr.length - j - 1);
          }
        }

        int predlength = preds.length - oldPreds.size;
        if(indexMatch || invPath.expr.length != 0) predlength += 1;

        Expr[] newPreds = new Expr[step.expr.length];
        int c = 0;
        if(!indexMatch && invPath.expr.length != 0) newPreds[c++] = invPath;
        for(int p = 0; p != step.expr.length; p++) {
          if(!oldPreds.contains(p)) {
            if (ie[p] != null) newPreds[c++] = ie[p]; 
            /*if (pred instanceof PredSimple) {
              Expr e = ((PredSimple) pred).expr;
              if (e instanceof FTContains) {
                ((PredSimple) pred).expr = e.indexEquivalent(ctx, null, true);
              }
            }*/
            //newPreds.add(pred);
          }
        }
        if (c == 0) newPreds = new Expr[]{}; 
        else Array.finish(newPreds, c);
        result = (new InterSect(new Expr[] {indexArg})).comp(ctx);
        //result = new InterSect(new Expr[] { indexArg }).comp(ctx);

        // add rest of predicates
        if(newPreds.length != 0) result =
          //new Filter(result, newPreds).comp(ctx);
          ctx.comp(new Pred(result, newPreds));
        
        // add match with initial nodes
        //if(indexMatch && checkMatch(invPath)) {
        if(indexMatch) result = new IndexMatch(this, result, invPath);

        // add rest of location path
        if(oldPath.length != 0) result = new Path(result, oldPath);
      }

      //}
      //final Expr p = expr[0].indexEquivalent(ctx, new FTIndexInfo(), this);
    }
    
    return result;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = ctx.iter(root).finish();

    if(cache && res != null && item == it && it.type == Type.DOC)
      return res.iter();

    item = it;
    final Item c = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;
    ctx.item = it;
    res = eval(ctx);

    ctx.item = c;
    ctx.size = cs;
    ctx.pos = cp;
    return res.iter();
  }

  /**
   * Evaluates the location path.
   * @param ctx query context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  protected Item eval(final XQContext ctx) throws XQException {
    // simple location step traversal...
    if(steps) {
      final NodIter ir = new NodIter();
      iter(0, ir, ctx);

      if(ir.size == 0) return Seq.EMPTY;
      if(ir.size == 1) return ir.list[0];

      final NodeBuilder nb = new NodeBuilder(false);
      Nod it;
      while((it = ir.next()) != null) nb.add(it);
      return nb.finish();
    }

    Item it = ctx.item;
    for(final Expr e : expr) {
      if(e instanceof Step) {
        ctx.item = it;
        it = ctx.iter(e).finish();
      } else {
        final SeqBuilder sb = new SeqBuilder();
        final Iter ir = it.iter();
        ctx.size = it.size();
        ctx.pos = 1;
        Item i;
        while((i = ir.next()) != null) {
          if(!i.node()) Err.or(NODESPATH, this, i.type);
          ctx.item = i;
          sb.add(ctx.iter(e));
          ctx.pos++;
        }
        it = sb.finish();
      }
    }

    // either nodes or atomic items are allowed in a result set, but not both
    final Iter ir = it.iter();
    Item i = ir.next();
    if(i != null) {
      if(i.node()) {
        // [CG] XQuery/evaluate path: verify when results might be ordered
        final NodeBuilder nb = new NodeBuilder(false);
        nb.add((Nod) i);
        while((i = ir.next()) != null) {
          if(!i.node()) Err.or(EVALNODESVALS);
          nb.add((Nod) i);
        }
        return nb.finish();
      }
      while((i = ir.next()) != null) if(i.node()) Err.or(EVALNODESVALS);
    }
    return it;
  }

  /**
   * Path Iterator.
   * @param l current step
   * @param ni node builder
   * @param ctx query context
   * @throws XQException query exception
   */
  private void iter(final int l, final NodIter ni, final XQContext ctx)
      throws XQException {

    final NodeIter ir = (NodeIter) (ctx.iter(expr[l]));
    final boolean more = l + 1 != expr.length;
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
    int ll = expr.length;
    for(int l = 1; l < ll; l++) {
      if(!((Step) expr[l - 1]).simple(DESCORSELF)) continue;
      final Step next = (Step) expr[l];
      if(next.axis == CHILD && !next.uses(Using.POS)) {
        Array.move(expr, l, -1, ll-- - l);
        next.axis = DESC;
      }
    }
    if(ll != expr.length) {
      ctx.compInfo(OPTDESC);
      final Expr[] tmp = new Expr[ll];
      System.arraycopy(expr, 0, tmp, 0, ll);
      expr = tmp;
    }
  }

  /**
   * Check if any of the steps will always yield no results.
   * This method implies that all expressions are location steps.
   * @throws XQException evaluation exception
   */
  private void checkEmpty() throws XQException {
    final int ll = expr.length;

    for(int l = 1; l < ll; l++) {
      final Step step = (Step) expr[l];
      final Step step0 = (Step) expr[l - 1];

      if(step.axis == SELF) {
        if(step.test == NODE) continue;

        if(step0.axis == ATTR) warning(step);
        if(step0.test.type == Type.TXT && step.test.type != Type.TXT)
          warning(step);

        final QNm name = step.test.name;
        final QNm name0 = step0.test.name;
        if(name0 == null || name == null) continue;
        if(!name.eq(name0)) warning(step);

      } else if(step.axis == DESCORSELF) {
        if(step.test == NODE) continue;
        if(step0.axis == ATTR) warning(step);

        if(step0.test.type == Type.TXT && step.test.type != Type.TXT)
          warning(step);
      } else if(step.axis == DESC || step.axis == CHILD) {
        if(step0.axis == ATTR || step0.test.type == Type.TXT)
          warning(step);
      }
    }
  }

  
  /**
   * Inverts a path.
   * @param curr current location step
   * @return inverted path
   */
  public Path invertPath(final Step curr) {
    final Expr[] el = new Expr[expr.length + 1];
    int c = 0;
    
    if (!steps) {
      return this;      
    }
    
    // add inverted pretext steps
    Axis lastAxis;
    int k = expr.length;
    
    while(k > -1 && !(expr[k] instanceof Step)) k--;
    if (k > -1) {
      lastAxis = invertAxis(((Step) expr[k]).axis);
      while (k > -1) {
        if (expr[k] instanceof Step) {
          final Step step = (Step) expr[k];
          final Step inv = new Step(lastAxis, step.test, step.expr);
          //final Step inv = Axis.create(lastAxis, step.test, step.expr);
          lastAxis = invertAxis(step.axis);
          el[c++] = inv;
        }
      }
      el[c++] = new Step(lastAxis, curr.test, new Expr[]{});
    }
    //el[c++] = Axis.create(lastAxis, curr.test);
    
    return c > 0 ? new Path(root, Array.finish(el, c)) : this;
  }

  /**
   * Inverts a path.
   * @param sis SimpleIterStep
   * @param curr current location step
   * @param expr Expression
   * @return inverted path
   */
  public static Path invertSIStep(final SimpleIterStep sis, final Step curr,
      final Expr expr) {
    // add inverted pretext steps
    Axis lastAxis = invertAxis(sis.axis);
    Expr  e = new Step(lastAxis, curr.test, new Expr[]{});
    
    return new Path(expr, new Expr[]{e});
    //return new Path(null, new Expr[]{expr, e});
  }

  
  
  /**
   * Inverts an XPath axis.
   * @param axis axis to be inverted.
   * @return inverted axis
   */
  private static Axis invertAxis(final Axis axis) {
    switch(axis) {
      case ANC:        return Axis.DESC;
      case ANCORSELF:  return Axis.DESCORSELF;
      case ATTR:
      case CHILD:      return Axis.PARENT;
      case DESC:       return Axis.ANC;
      case DESCORSELF: return Axis.ANCORSELF;
      case PARENT:     return Axis.CHILD;
      case SELF:       return Axis.SELF;
      default:         return null;
    }
  }

  
  @Override
  public boolean uses(final Using u) {
    return super.uses(u) || root.uses(u);
  }

  @Override
  public Type returned() {
    return Type.NOD;
  }

  /**
   * Throws a static warning.
   * @param s step
   * @throws XQException evaluation exception
   */
  protected void warning(final Expr s) throws XQException {
    Err.or(COMPSELF, s);
  }

  /**
   * Returns a string representation of the path.
   * @return path as string
   */
  public String path() {
    final StringBuilder sb = new StringBuilder();
    for(int p = 0; p < expr.length; p++) {
      if(p != 0) sb.append("/");
      sb.append(expr[p]);
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root + "/");
    return sb.append(path()).toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NS, timer());
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }
}
