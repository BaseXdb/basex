package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.basex.data.Data;
import org.basex.data.PathNode;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CDoc;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.expr.Root;
import org.basex.query.item.DBNode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.path.Test.Name;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Root expression. */
  public Expr root;
  /** Path steps. */
  public Expr[] step;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s axis steps
   */
  protected Path(final InputInfo ii, final Expr r, final Expr[] s) {
    super(ii);
    root = r;
    step = s;
  }

  /**
   * Returns a new path instance.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param path path steps
   * @return class instance
   */
  public static final Path get(final InputInfo ii, final Expr r,
      final Expr... path) {

    // check if all steps are axis steps
    boolean axes = true;
    for(final Expr p : path) axes &= p instanceof AxisStep;
    return axes ?
        new AxisPath(ii, r, path).finish(null) :
        new MixedPath(ii, r, path);
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    if(root != null) {
      root = checkUp(root, ctx).comp(ctx);
      if(root instanceof Context) root = null;
    }

    final Value v = ctx.value;
    ctx.value = root(ctx);
    final Expr e = compPath(ctx);
    ctx.value = v;
    return e;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @return optimized expression
   * @throws QueryException query exception
   */
  protected abstract Expr compPath(final QueryContext ctx)
    throws QueryException;

  /**
   * Returns the root of the current context or {@code null}.
   * @param ctx query context
   * @return root
   */
  protected final Value root(final QueryContext ctx) {
    final Value v = ctx != null ? ctx.value : null;
    // no root specified: return context, if it does not reference a document
    // as e.g. happens in //a(b|c)
    if(root == null) return v == null || v.type != NodeType.DOC ? v : null;
    // root is value: return root
    if(root.value()) return (Value) root;
    // no root reference, no context: return null
    if(!(root instanceof Root) || v == null) return null;
    // return context sequence or root of current context
    return v.size() != 1 ? v : ((Root) root).root(v);
  }

  @Override
  public final boolean uses(final Use use) {
    // initial context will be used as input
    if(use == Use.CTX) return root == null || root.uses(use);
    for(final Expr s : step) if(s.uses(use)) return true;
    return root != null && root.uses(use);
  }

  /**
   * Optimizes descendant-or-self steps and static types.
   * @param ctx query context
   */
  protected void optSteps(final QueryContext ctx) {
    boolean opt = false;
    Expr[] stp = step;
    for(int l = 1; l < stp.length; ++l) {
      if(!(stp[l - 1] instanceof AxisStep &&
           stp[l] instanceof AxisStep)) continue;

      final AxisStep prev = (AxisStep) stp[l - 1];
      final AxisStep curr = (AxisStep) stp[l];
      if(!prev.simple(DESCORSELF, false)) continue;

      if(curr.axis == CHILD && !curr.uses(Use.POS)) {
        // descendant-or-self::node()/child::X -> descendant::X
        Array.move(stp, l, -1, stp.length - l);
        stp = Arrays.copyOf(stp, stp.length - 1);
        curr.axis = DESC;
        opt = true;
      } else if(curr.axis == ATTR && !curr.uses(Use.POS)) {
        // descendant-or-self::node()/@X -> descendant-or-self::*/@X
        prev.test = new NameTest(false, prev.input);
        opt = true;
      }
    }
    if(opt) ctx.compInfo(OPTDESC);

    // set atomic type for single attribute steps to speedup predicate tests
    if(root == null && stp.length == 1 && stp[0] instanceof AxisPath) {
      final AxisStep curr = (AxisStep) stp[0];
      if(curr.axis == ATTR && curr.test.test == Name.STD)
        curr.type = SeqType.NOD_ZO;
    }
    step = stp;
  }

  /**
   * Computes the number of results.
   * @param ctx query context
   * @return number of results
   */
  protected long size(final QueryContext ctx) {
    final Value rt = root(ctx);
    final Data data = rt != null && rt.type == NodeType.DOC ? rt.data() : null;
    if(data == null || !data.meta.pathindex || !data.meta.uptodate ||
        !data.single()) return -1;

    ArrayList<PathNode> nodes = data.pthindex.root();
    long m = 1;
    for(int s = 0; s < step.length; s++) {
      final AxisStep curr = checkStep(s);
      if(curr != null) {
        nodes = curr.size(nodes, data);
        if(nodes == null) return -1;
      } else if(s + 1 == step.length) {
        m = step[s].size();
      } else {
        // stop if a non-axis step is not placed last
        return -1;
      }
    }

    long sz = 0;
    for(final PathNode pn : nodes) sz += pn.size;
    return sz * m;
  }

  /**
   * Checks if the location path contains steps that will never yield results.
   * @param steps step array
   * @return empty step, or {@code null}
   */
  protected AxisStep voidStep(final Expr[] steps) {
    for(int l = 0; l < steps.length; ++l) {
      final AxisStep s = checkStep(l);
      if(s == null) continue;
      final Axis sa = s.axis;
      if(l == 0) {
        if(root instanceof CAttr) {
          if(sa == CHILD || sa == DESC) return s;
        } else if(root instanceof DBNode &&
            ((Value) root).type == NodeType.DOC || root instanceof CDoc) {
          if(sa != CHILD && sa != DESC && sa != DESCORSELF &&
            (sa != SELF && sa != ANCORSELF ||
             s.test != Test.NOD && s.test != Test.DOC)) return s;
        }
      } else {
        final AxisStep ls = checkStep(l - 1);
        if(ls == null) continue;
        final Axis lsa = ls.axis;
        if(sa == SELF || sa == DESCORSELF) {
          // .../self:: / .../descendant-or-self::
          if(s.test == Test.NOD) continue;
          // @.../...
          if(lsa == ATTR && s.test.type != NodeType.ATT) return s;
          // text()/...
          if(ls.test == Test.TXT && s.test != Test.TXT) return s;
          if(sa == DESCORSELF) continue;
          // .../self::
          final QNm n1 = s.test.name;
          final QNm n0 = ls.test.name;
          if(n0 == null || n1 == null) continue;
          // ...X/...Y
          if(!n1.eq(n0)) return s;
        } else if(sa == FOLLSIBL || sa == PRECSIBL) {
          // .../following-sibling:: / .../preceding-sibling::
          if(lsa == ATTR) return s;
        } else if(sa == DESC || sa == CHILD || sa == ATTR) {
          // .../descendant:: / .../child:: / .../attribute::
          if(lsa == ATTR || ls.test == Test.TXT || ls.test == Test.COM ||
             ls.test == Test.PI) return s;
        } else if(sa == PARENT || sa == ANC) {
          // .../parent:: / .../ancestor::
          if(ls.test == Test.DOC) return s;
        }
      }
    }
    return null;
  }

  /**
   * Converts descendant to child steps.
   * @param ctx query context
   * @param data data reference
   * @return path
   */
  protected Path children(final QueryContext ctx, final Data data) {
    for(int s = 0; s < step.length; ++s) {
      // don't allow predicates in preceding location steps
      final AxisStep prev = s > 0 ? checkStep(s - 1) : null;
      if(prev != null && prev.pred.length != 0) break;

      // ignore axes other than descendant, or position predicates
      final AxisStep curr = checkStep(s);
      if(curr == null || curr.axis != Axis.DESC || curr.uses(Use.POS)) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, s);
      if(nodes == null) continue;

      ctx.compInfo(OPTCHILD, step[s]);

      // cache child steps
      final TokenList tl = new TokenList();
      while(nodes.get(0).par != null) {
        byte[] tag = data.tagindex.key(nodes.get(0).name);
        for(int j = 0; j < nodes.size(); ++j) {
          if(nodes.get(0).name != nodes.get(j).name) tag = null;
        }
        tl.add(tag);
        nodes = data.pthindex.parent(nodes);
      }

      // build new steps
      int ts = tl.size();
      final Expr[] steps = new Expr[ts + step.length - s - 1];
      for(int t = 0; t < ts; ++t) {
        final Expr[] preds = t == ts - 1 ?
            ((AxisStep) step[s]).pred : new Expr[0];
        final byte[] n = tl.get(ts - t - 1);
        final NameTest nt = n == null ? new NameTest(false, input) :
          new NameTest(new QNm(n), Name.NAME, false, input);
        steps[t] = AxisStep.get(input, Axis.CHILD, nt, preds);
      }
      while(++s < step.length) steps[ts++] = step[s];
      return get(input, root, steps);
    }
    return this;
  }

  /**
   * Casts the specified step into an axis step, or returns a {@code null}
   * reference.
   * @param i index
   * @return step
   */
  public AxisStep checkStep(final int i) {
    return step[i] instanceof AxisStep ? (AxisStep) step[i] : null;
  }

  /**
   * Returns all summary path nodes for the specified location step or
   * {@code null} if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param l last step to be checked
   * @return path nodes
   */
  protected ArrayList<PathNode> pathNodes(final Data data, final int l) {
    // convert single descendant step to child steps
    if(!data.meta.pathindex || !data.meta.uptodate) return null;

    ArrayList<PathNode> in = data.pthindex.root();
    for(int s = 0; s <= l; ++s) {
      final AxisStep curr = checkStep(s);
      if(curr == null) return null;
      final boolean desc = curr.axis == Axis.DESC;
      if(!desc && curr.axis != Axis.CHILD || curr.test.test != Name.NAME)
        return null;

      final int name = data.tagindex.id(curr.test.name.ln());

      final ArrayList<PathNode> out = new ArrayList<PathNode>();
      for(final PathNode pn : data.pthindex.desc(in, desc)) {
        if(pn.kind == Data.ELEM && name == pn.name) {
          // skip test if a tag is found on different levels
          if(out.size() != 0 && out.get(0).level() != pn.level()) return null;
          out.add(pn);
        }
      }
      if(out.size() == 0) return null;
      in = out;
    }
    return in;
  }

  /**
   * Adds a predicate to the last step.
   * @param pred predicate to be added
   * @return resulting path instance
   */
  public final Path addPreds(final Expr... pred) {
    step[step.length - 1] = checkStep(step.length - 1).addPreds(pred);
    return get(input, root, step);
  }

  @Override
  public int count(final Var v) {
    return root != null ? root.count(v) : 0;
  }

  @Override
  public boolean removable(final Var v) {
    return root == null || root.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    if(root != null) root = root.remove(v);
    if(root instanceof Context) root = null;
    return this;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    if(root != null) root.plan(ser);
    for(final Expr s : step) s.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : step) sb.append((sb.length() != 0 ? "/" : "") + s);
    return sb.toString();
  }
}
