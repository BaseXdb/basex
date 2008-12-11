package org.basex.query.xquery.path;

import static org.basex.query.xquery.path.Axis.*;
import static org.basex.query.xquery.path.Test.NODE;
import static org.basex.query.xquery.XQText.*;
import java.io.IOException;

import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTIndexAcsbl;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.CAttr;
import org.basex.query.xquery.expr.CmpG;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FTContains;
import org.basex.query.xquery.expr.Filter;
import org.basex.query.xquery.expr.Root;
import org.basex.query.xquery.expr.FTIndexEq;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Array;



/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
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
      
      // analyze if result set can be cached - no predicates/variables...
      cache = !root.uses(Using.VAR);
        
      boolean noPreds = true;
      for(final Expr step : expr) {
        // check if we have a predicate
        if(((Step) step).expr.length != 0) {
          noPreds = false;
          // check if we also find a variable
          if(step.uses(Using.VAR)) {
            cache = false;
            break;
          }
        }
      }
      // no predicates, one child or descendant step...
      final Axis axis = ((Step) expr[0]).axis;
      // if we've found a variable, cache will be false
      if(cache && noPreds && expr.length == 1 && (axis == Axis.DESC ||
          axis == Axis.DESCORSELF || axis == Axis.CHILD)) {
        return new SimpleIterPath(root, expr);
      }
      
      if (expr.length == 1 && checkAxes() && isAbsPath()) {
      //if (checkAxes() && isAbsPath()) {
        for (int i = 0; i < expr.length; i++) {
          final Step s = (Step) expr[i];
          FTIndexAcsbl iacs = null;
          int minp = 0;
          int min = Integer.MAX_VALUE;
          
          for (int j = 0; j < s.expr.length; j++) {
              FTIndexAcsbl ia = new FTIndexAcsbl();
              s.expr[j].indexAccessible(ctx, ia);
              if (ia.io && ia.iu) {
                if (min >= ia.indexSize) {
                  min = ia.indexSize;
                  iacs = ia;
                  minp = j;
                }
              }
          }
          
          
          if (iacs != null && iacs.io && iacs.iu) {
            // replace expressions for index access
            final FTIndexEq ieq = new FTIndexEq(s, iacs.seq);
            Expr ie = s.expr[minp].indexEquivalent(ctx, ieq);
            if (iacs.indexSize == 0) {
              if (iacs.ftnot) {
                // all nodes are results 
                s.expr[0] = ((FTContains) s.expr[minp]).expr[0];
                return this;                
              } else {
                // any node is a result
                return Seq.EMPTY;                
              }
            }
            if (iacs.seq) {
              // do not invert path
              s.expr[0] = ie;
            } else {
              // invert Path
              Path result = (Path) ie; 
              
              // add rest of predicates
              final Expr[] newPreds = new Expr[s.expr.length - 1];
              int c = 0;
              for(int p = 0; p != s.expr.length; p++)  
                if (p != minp) newPreds[c++] = s.expr[p];
              
              // invert path before index step
              for (int j = i; j > 0; j--) {
                final Step curr = (Step) expr[j];
                final Axis a = invertAxis(curr.axis);
                if(a == null) break;
                if(j == 0) {
                  if(this.isAbsPath() || a == Axis.PARENT) {
                    result.addStep(new Step(a, Test.NODE, new Expr[]{}));
                  } 
                } else {
                  final Step prev = (Step) expr[j - 1];
                  if(prev.expr.length != 0) break;
                  final Step step = (Step) result.expr[result.expr.length - 1];
                  
                  final Expr[] tmp = new Expr[step.expr.length + 1];
                  System.arraycopy(step.expr, 0, tmp, 0, tmp.length - 1);
                  tmp[tmp.length - 1] = new Step(a, prev.test, new Expr[]{}); 
                  step.expr = tmp;
                }
                //remove(j);
              }
              
              if(newPreds.length > 0 && ieq.addFilter) ie =
                new Filter(result, newPreds).comp(ctx);
              else ie = result;
              return ie;
            }
          }
        }
      }
    }
    return this;  
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
   * Add Step.
   * @param s Step to add
   */
  public void addStep(final Step s) {
    Expr[] e = new Expr[expr.length + 1];
    System.arraycopy(expr, 0, e, 0, expr.length);
    e[expr.length] = s;
    expr = e;
  }
  
  /**
   * Remove expression.
   * @param j point on expression
   */
  public void remove(final int j) {
    Expr[] ex = new Expr[expr.length - 1];
    if (j > 0) System.arraycopy(expr, 0, ex, 0, j - 1);
    if (j < expr.length - 1) 
      System.arraycopy(expr, j + 1, ex, j, expr.length - j);
    expr = ex;
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
    int k = expr.length - 1;
    
    while(k > -1 && !(expr[k] instanceof Step)) k--;
    if (k > -1) {
      lastAxis = invertAxis(((Step) expr[k]).axis);
      while (k > -1) {
        if (expr[k] instanceof Step) {
          final Step step = (Step) expr[k];
          final Step inv = new Step(lastAxis, step.test, step.expr);
          lastAxis = invertAxis(step.axis);
          el[c++] = inv;
        }
        k--;
      }
      el[c++] = new Step(lastAxis, curr.test, new Expr[]{});
    }
    return c > 0 ? new Path(root, Array.finish(el, c)) : this;
  }

  
  /**
   * Inverts a location path.
   * @param curr current location step
   * @param r new root node
   * @return inverted path
   */
  public final Path invertPathNew(final Step curr, final Expr r) {
    // hold the steps to the end of the inverted path
    final Expr[] e = new Expr[expr.length + 1];
    int c = 0;
    
    
    // add inverted pretext steps
    Axis lastAxis = invertAxis(((Step) expr[expr.length - 1]).axis);
    for(int k = expr.length - 1; k >= 0; k--) {
      final Step step = (Step) expr[k];
      Step inv;
      if (k == 0) {
        inv = (Step) root;
        inv = new Step(lastAxis, inv.test, new Expr[]{});
      } else inv = new Step(lastAxis, step.test, step.expr);
      lastAxis = invertAxis(step.axis);
      e[c++] = inv;
    } 
    e[c++] = new Step(lastAxis, curr.test, new Expr[]{});
    
    return new Path(r, e);
  }

  /**
   * Inverts a step.
   * @param sis Step
   * @param curr current location step
   * @param expr Expression
   * @return inverted path
   */
  public static Path invertStep(final Step sis, final Step curr,
      final Expr expr) {
    // add inverted pretext steps
    Axis lastAxis = invertAxis(sis.axis);
    Expr  e = new Step(lastAxis, curr.test, new Expr[]{});
    return new Path(expr, new Expr[]{e});
  }

  
  /**
   * Checks if the specified location step has suitable index axes.
   * @return true result of check
   */
  public final boolean checkAxes() {
    for(int s = 0; s < expr.length - 1; s++) {
      final Step curr = (Step) expr[s];
      // not the last text step
      if(curr.expr != null && curr.expr.length != 0 || curr.axis != Axis.ANC &&
         curr.axis != Axis.ANCORSELF && curr.axis != Axis.DESC &&
         curr.axis != Axis.SELF && curr.axis != Axis.DESCORSELF &&
         curr.axis != Axis.CHILD && curr.axis != Axis.PARENT) return false;
    }
    return true;
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

  /**
   * Check if theres anything to sum up.
   * @param d Data
   * @return boolean sum up
   */
  public boolean sumUp(final Data d) {
    if (expr.length == 1 && expr[0] instanceof Step 
        && root instanceof SimpleIterStep) {
      final SimpleIterStep sis = (SimpleIterStep) root;
      return sis.sumUp() && sis.test.kind == Test.Kind.NAME &&
        d.skel.desc(sis.test.name.str(), false, false).size == 0;
    }
    return false;
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
   * Checks if the path is indexable.
   * @param ctx query context
   * @param exp expression which must be a literal
   * @param cmp comparator
   * @return result of check
   */
  public final IndexToken indexable(final XQContext ctx, final Expr exp,
      final CmpG.Comp cmp) {
    
    if(!(isAbsPath() && exp instanceof Str)) return null;

    if (ctx.item instanceof DNode) {
      final Data data = ((DNode) ctx.item).data;
      final boolean txt = data.meta.txtindex && cmp == CmpG.Comp.EQ;
      final boolean atv = data.meta.atvindex && cmp == CmpG.Comp.EQ;
      final Step step = (Step) expr[expr.length - 1];
      final boolean text = txt && step.test.type == Type.TXT && 
        (step.expr == null || step.expr.length == 0);
      final boolean attr = !text && atv && step.simple(Axis.ATTR);
      return text || attr && checkAxes() ?
          new ValuesToken(text, ((Str) exp).str()) : null;
    }
    return null;
  }


  /**
   * Check if current path is absolut.
   * 
   * @return boolean abspath
   */
  public boolean isAbsPath() {
    return root instanceof Root || root instanceof DNode;
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
  public final String color() {
    return "FFCC33";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root + "/");
    return sb.append(path()).toString();
  }
}
