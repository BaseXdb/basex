package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.data.Data;
import org.basex.index.path.PathNode;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CDoc;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.expr.Root;
import org.basex.query.item.Empty;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.path.Test.Name;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.list.ObjList;

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
  public Expr[] steps;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s axis steps
   */
  protected Path(final InputInfo ii, final Expr r, final Expr[] s) {
    super(ii);
    root = r;
    steps = s;
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
    try {
      ctx.value = root(ctx);
      return compPath(ctx);
    } finally {
      ctx.value = v;
    }
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
    if(root.isValue()) return (Value) root;
    // no root reference, no context: return null
    if(!(root instanceof Root) || v == null) return null;
    // return context sequence or root of current context
    return v.size() != 1 ? v : ((Root) root).root(v);
  }

  @Override
  public final boolean uses(final Use use) {
    // first step or root expression will be used as context
    if(use == Use.CTX) return root == null || root.uses(use);
    for(final Expr s : steps) if(s.uses(use)) return true;
    return root != null && root.uses(use);
  }

  /**
   * Optimizes descendant-or-self steps and static types.
   * @param ctx query context
   */
  protected void optSteps(final QueryContext ctx) {
    boolean opt = false;
    Expr[] st = steps;
    for(int l = 1; l < st.length; ++l) {
      if(!(st[l - 1] instanceof AxisStep &&
           st[l] instanceof AxisStep)) continue;

      final AxisStep prev = (AxisStep) st[l - 1];
      final AxisStep curr = (AxisStep) st[l];
      if(!prev.simple(DESCORSELF, false)) continue;

      if(curr.axis == CHILD && !curr.uses(Use.POS)) {
        // descendant-or-self::node()/child::X -> descendant::X
        Array.move(st, l, -1, st.length - l);
        st = Arrays.copyOf(st, st.length - 1);
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
    if(root == null && st.length == 1 && st[0] instanceof AxisStep) {
      final AxisStep curr = (AxisStep) st[0];
      if(curr.axis == ATTR && curr.test.test == Name.STD)
        curr.type = SeqType.NOD_ZO;
    }
    steps = st;
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

    ObjList<PathNode> nodes = data.pthindex.root();
    long m = 1;
    for(int s = 0; s < steps.length; s++) {
      final AxisStep curr = axisStep(s);
      if(curr != null) {
        nodes = curr.size(nodes, data);
        if(nodes == null) return -1;
      } else if(s + 1 == steps.length) {
        m = steps[s].size();
      } else {
        // stop if a non-axis step is not placed last
        return -1;
      }
    }

    long sz = 0;
    for(final PathNode pn : nodes) sz += pn.stats.count;
    return sz * m;
  }

  /**
   * Checks if the location path contains steps that will never yield results.
   * @param stps step array
   * @return empty step, or {@code null}
   */
  protected AxisStep voidStep(final Expr[] stps) {
    for(int l = 0; l < stps.length; ++l) {
      final AxisStep s = axisStep(l);
      if(s == null) continue;
      final Axis sa = s.axis;
      if(l == 0) {
        if(root instanceof CAttr) {
          if(sa == CHILD || sa == DESC) return s;
        } else if(root instanceof Root || root instanceof Value &&
            ((Value) root).type == NodeType.DOC || root instanceof CDoc) {
          if(sa != CHILD && sa != DESC && sa != DESCORSELF &&
            (sa != SELF && sa != ANCORSELF ||
             s.test != Test.NOD && s.test != Test.DOC)) return s;
        }
      } else {
        final AxisStep ls = axisStep(l - 1);
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
  protected Expr children(final QueryContext ctx, final Data data) {
    // skip path check if no path index exists, or if it is out-of-dated
    if(!data.meta.pathindex || !data.meta.uptodate ||
        data.nspaces.globalNS() == null) return this;

    Path path = this;
    for(int s = 0; s < steps.length; ++s) {
      // don't allow predicates in preceding location steps
      final AxisStep prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.preds.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final AxisStep curr = axisStep(s);
      if(curr == null || curr.axis != Axis.DESC || curr.uses(Use.POS)) continue;

      // check if child steps can be retrieved for current step
      ObjList<PathNode> pn = pathNodes(data, s);
      if(pn == null) continue;

      // cache child steps
      final ObjList<QNm> qnm = new ObjList<QNm>();
      while(pn.get(0).par != null) {
        QNm nm = new QNm(data.tagindex.key(pn.get(0).name));
        // skip children with prefixes
        if(nm.hasPrefix()) return this;
        for(int j = 0; j < pn.size(); ++j) {
          if(pn.get(0).name != pn.get(j).name) nm = null;
        }
        qnm.add(nm);
        pn = data.pthindex.parent(pn);
      }
      ctx.compInfo(OPTCHILD, steps[s]);

      // build new steps
      int ts = qnm.size();
      final Expr[] stps = new Expr[ts + steps.length - s - 1];
      for(int t = 0; t < ts; ++t) {
        final Expr[] preds = t == ts - 1 ?
            ((AxisStep) steps[s]).preds : new Expr[0];
        final QNm nm = qnm.get(ts - t - 1);
        final NameTest nt = nm == null ? new NameTest(false, input) :
          new NameTest(nm, Name.NAME, false, input);
        stps[t] = AxisStep.get(input, Axis.CHILD, nt, preds);
      }
      while(++s < steps.length) stps[ts++] = steps[s];
      path = get(input, root, stps);
      break;
    }

    // check if the all children in the path exist; don't test with namespaces
    if(data.nspaces.size() == 0) {
      LOOP:
      for(int s = 0; s < path.steps.length; ++s) {
        // only verify child steps; ignore namespaces
        final AxisStep st = path.axisStep(s);
        if(st == null || st.axis != Axis.CHILD) break;
        if(st.test.test == Name.ALL || st.test.test == null) continue;
        if(st.test.test != Name.NAME) break;

        // check if one of the addressed nodes is on the correct level
        final int name = data.tagindex.id(st.test.name.local());
        for(final PathNode pn : data.pthindex.desc(name, Data.ELEM)) {
          if(pn.level() == s + 1) continue LOOP;
        }
        ctx.compInfo(OPTPATH, path);
        return Empty.SEQ;
      }
    }
    return path;
  }

  /**
   * Casts the specified step into an axis step, or returns a {@code null}
   * reference.
   * @param i index
   * @return step
   */
  public AxisStep axisStep(final int i) {
    return steps[i] instanceof AxisStep ? (AxisStep) steps[i] : null;
  }

  /**
   * Returns all summary path nodes for the specified location step or
   * {@code null} if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param l last step to be checked
   * @return path nodes
   */
  protected ObjList<PathNode> pathNodes(final Data data, final int l) {
    // skip request if no path index exists or might be out-of-date
    if(!data.meta.pathindex || !data.meta.uptodate) return null;

    ObjList<PathNode> in = data.pthindex.root();
    for(int s = 0; s <= l; ++s) {
      final AxisStep curr = axisStep(s);
      if(curr == null) return null;
      final boolean desc = curr.axis == Axis.DESC;
      if(!desc && curr.axis != Axis.CHILD || curr.test.test != Name.NAME)
        return null;

      final int name = data.tagindex.id(curr.test.name.local());

      final ObjList<PathNode> al = new ObjList<PathNode>();
      for(final PathNode pn : data.pthindex.desc(in, desc)) {
        if(pn.kind == Data.ELEM && name == pn.name) {
          // skip test if a tag is found on different levels
          if(al.size() != 0 && al.get(0).level() != pn.level()) return null;
          al.add(pn);
        }
      }
      if(al.size() == 0) return null;
      in = al;
    }
    return in;
  }

  /**
   * Adds a predicate to the last step.
   * @param pred predicate to be added
   * @return resulting path instance
   */
  public final Path addPreds(final Expr... pred) {
    steps[steps.length - 1] = axisStep(steps.length - 1).addPreds(pred);
    return get(input, root, steps);
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
    for(final Expr s : steps) s.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : steps) sb.append((sb.length() != 0 ? "/" : "") + s);
    return sb.toString();
  }
}
