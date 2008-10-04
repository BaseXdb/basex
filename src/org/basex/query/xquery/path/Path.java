package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.CAttr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.VarCall;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
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
import org.basex.query.xquery.util.Var;
import org.basex.util.Array;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Path extends Arr {
  /** Top expression. */
  private Expr root;
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
    root = root.comp(ctx);
    Expr e = root;
    if(e instanceof VarCall) {
      final Var v = ctx.vars.get(((VarCall) e).var);
      if(v != null) e = v.expr;
    }
    if(e instanceof Fun && ((Fun) e).func == FunDef.DOC) {
      if(expr[0] instanceof Step) {
        final Step s = (Step) expr[0];
        if(s.axis == Axis.ATTR || s.axis == Axis.PARENT || s.axis == Axis.SELF
            && s.test != Test.NODE) Err.or(COMPSELF, expr[0]);
      }
    }
    if(e instanceof CAttr) {
      if(expr[0] instanceof Step) {
        if(((Step) expr[0]).axis == Axis.CHILD) Err.or(COMPSELF, expr[0]);
      }
    }

    for(int i = 0; i != expr.length; i++) {
      expr[i] = expr[i].comp(ctx);
      steps &= expr[i] instanceof Step;
    }

    if(steps) {
      mergeDesc(ctx);
      checkEmpty();
      // analyze if result set can be cached
      cache = true;
      for(final Expr ex : expr) {
        if(((Step) ex).expr.length != 0 && (ex.uses(Using.VAR))) {
          cache = false;
          break;
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

    final NodeIter ir = ((Step) expr[l]).iter(ctx);
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
      if(!((Step) expr[l - 1]).simple(Axis.DESCORSELF)) continue;
      final Step next = (Step) expr[l];
      if(next.axis == Axis.CHILD && !next.uses(Using.POS)) {
        Array.move(expr, l, -1, ll-- - l);
        next.axis = Axis.DESC;
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

      if(step.axis == Axis.SELF) {
        if(step.test == Test.NODE) continue;

        if(step0.axis == Axis.ATTR) warning(step);
        if(step0.test.type == Type.TXT && step.test.type != Type.TXT)
          warning(step);

        final QNm name = step.test.name;
        final QNm name0 = step0.test.name;
        if(name0 == null || name == null) continue;
        if(!name.eq(name0)) warning(step);

      } else if(step.axis == Axis.DESCORSELF) {
        if(step.test == Test.NODE) continue;
        if(step0.axis == Axis.ATTR) warning(step);

        if(step0.test.type == Type.TXT && step.test.type != Type.TXT)
          warning(step);
      } else if(step.axis == Axis.DESC || step.axis == Axis.CHILD) {
        if(step0.axis == Axis.ATTR || step0.test.type == Type.TXT)
          warning(step);
      }
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
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FFCC00";
  }
}
