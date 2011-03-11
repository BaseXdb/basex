package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.PathNode;
import org.basex.data.StatsKey;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CDoc;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Filter;
import org.basex.query.expr.Pos;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Name;
import static org.basex.query.util.Err.*;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;

/**
 * Axis Path expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public class AxisPath extends Path {
  /** Expression list. */
  public AxisStep[] step;
  /** Flag for result caching. */
  private boolean cache;
  /** Cached result. */
  private NodeCache citer;
  /** Last visited item. */
  private Value lvalue;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s location steps; will at least have one entry
   */
  protected AxisPath(final InputInfo ii, final Expr r, final AxisStep... s) {
    super(ii, r);
    step = s;
  }

  /**
   * Returns a new class instance.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param st location steps; will at least have one entry
   * @return class instance
   */
  public static final AxisPath get(final InputInfo ii, final Expr r,
      final AxisStep... st) {
    return new AxisPath(ii, r, st).finish(null);
  }

  /**
   * If possible, converts this path expression to a path iterator.
   * @param ctx context reference
   * @return resulting operator
   */
  private AxisPath finish(final QueryContext ctx) {
    // evaluate number of results
    size = size(ctx);
    // set type with number of results or occurrence from last step
    type = size != -1 ? SeqType.get(Type.NOD, size) :
      step[step.length - 1].type();

    return iterable() ? new IterPath(input, root, step, type, size) : this;
  }

  /**
   * Checks if the path is iterable.
   * @return resulting operator
   */
  private boolean iterable() {
    if(root == null || root.uses(Use.VAR) || root.duplicates()) return false;

    final int sl = step.length;
    for(int s = 0; s < sl; ++s) {
      switch(step[s].axis) {
        // reverse axes - don't iterate
        case ANC: case ANCORSELF: case PREC: case PRECSIBL:
          return false;
        // multiple, unsorted results - only iterate at last step,
        // or if last step uses attribute axis
        case DESC: case DESCORSELF: case FOLL: case FOLLSIBL:
          return s + 1 == sl || s + 2 == sl && step[s + 1].axis == Axis.ATTR;
        // allow iteration for CHILD, ATTR, PARENT and SELF
        default:
      }
    }
    return true;
  }

  /**
   * Computes the number of results.
   * @param ctx query context
   * @return number of results
   */
  private long size(final QueryContext ctx) {
    final Value rt = root(ctx);
    final Data data = rt != null && rt.type == Type.DOC &&
      rt instanceof DBNode ? ((DBNode) rt).data : null;

    if(data == null || !data.meta.pathindex || !data.meta.uptodate ||
        !data.single()) return -1;

    ArrayList<PathNode> nodes = data.pthindex.root();
    for(final AxisStep as : step) {
      nodes = as.size(nodes, data);
      if(nodes == null) return -1;
    }

    long sz = 0;
    for(final PathNode pn : nodes) sz += pn.size;
    return sz;
  }

  @Override
  protected Expr compPath(final QueryContext ctx) throws QueryException {
    for(final AxisStep s : step) checkUp(s, ctx);

    // merge two axis paths
    if(root instanceof AxisPath) {
      AxisStep[] st = ((AxisPath) root).step;
      root = ((AxisPath) root).root;
      for(final AxisStep s : step) st = Array.add(st, s);
      step = st;
      // refresh root context
      ctx.compInfo(OPTPATH);
      ctx.value = root(ctx);
    }
    final AxisStep s = emptyStep();
    if(s != null) COMPSELF.thrw(input, s);

    for(int i = 0; i != step.length; ++i) {
      final Expr e = step[i].comp(ctx);
      if(!(e instanceof AxisStep)) return e;
      step[i] = (AxisStep) e;
    }
    optSteps(ctx);

    // check if all context nodes reference document nodes
    final Data data = ctx.resource.data();
    if(data != null) {
      boolean doc = ctx.resource.docNodes();
      if(!doc) {
        final Iter iter = ctx.value.iter(ctx);
        Item it;
        while((it = iter.next()) != null) {
          doc = it.type == Type.DOC;
          if(!doc) break;
        }
      }
      if(doc && data.meta.uptodate) {
        Expr e = this;
        // check index access
        if(root != null && !uses(Use.POS)) e = index(ctx, data);
        // check children path rewriting
        if(e == this) e = children(ctx, data);
        // return optimized expression
        if(e != this) return e.comp(ctx);
      }
    }

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && !uses(Use.VAR);

    // if applicable, return iterator
    final AxisPath path = finish(ctx);

    // heuristics: use filter expression if one result is expected
    return size() != 1 ? path :
      new Filter(input, this, Pos.get(1, size(), input)).comp2(ctx);
  }

  /**
   * Converts descendant to child steps.
   * @param ctx query context
   * @param data data reference
   * @return path
   */
  private AxisPath children(final QueryContext ctx, final Data data) {
    for(int s = 0; s < step.length; ++s) {
      // don't allow predicates in preceding location steps
      if(s > 0 && step[s - 1].pred.length != 0) break;

      // ignore axes other than descendant, or position predicates
      if(step[s].axis != Axis.DESC || step[s].uses(Use.POS)) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, s);
      if(nodes == null) continue;

      ctx.compInfo(OPTCHILD, step[s]);

      // cache child steps
      final TokenList tl = new TokenList();
      while(nodes.get(0).par != null) {
        byte[] tag = data.tags.key(nodes.get(0).name);
        for(int j = 0; j < nodes.size(); ++j) {
          if(nodes.get(0).name != nodes.get(j).name) tag = null;
        }
        tl.add(tag);
        nodes = data.pthindex.parent(nodes);
      }

      // build new steps
      int ts = tl.size();
      final AxisStep[] steps = new AxisStep[ts + step.length - s - 1];
      for(int t = 0; t < ts; ++t) {
        final Expr[] preds = t == ts - 1 ? step[s].pred : new Expr[0];
        final byte[] n = tl.get(ts - t - 1);
        final NameTest nt = n == null ? new NameTest(false, input) :
          new NameTest(new QNm(n), Name.NAME, false, input);
        steps[t] = AxisStep.get(input, Axis.CHILD, nt, preds);
      }
      while(++s < step.length) steps[ts++] = step[s];

      return get(input, root, steps).children(ctx, data);
    }
    return this;
  }

  /**
   * Returns all summary path nodes for the specified location step or
   * {@code null} if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param l last step to be checked
   * @return path nodes
   */
  private ArrayList<PathNode> pathNodes(final Data data, final int l) {
    // convert single descendant step to child steps
    if(!data.meta.pathindex || !data.meta.uptodate) return null;

    ArrayList<PathNode> in = data.pthindex.root();
    for(int s = 0; s <= l; ++s) {
      final boolean desc = step[s].axis == Axis.DESC;
      if(!desc && step[s].axis != Axis.CHILD || step[s].test.test != Name.NAME)
        return null;

      final int name = data.tags.id(step[s].test.name.ln());

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
   * If possible, returns an expression which accesses the index.
   * Otherwise, returns the original expression.
   * @param ctx query context
   * @param data data reference
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr index(final QueryContext ctx, final Data data)
      throws QueryException {

    // cache index access costs
    IndexContext ics = null;
    // cheapest predicate and step
    int pmin = 0;
    int smin = 0;

    // check if path can be converted to an index access
    for(int s = 0; s < step.length; ++s) {
      // find cheapest index access
      final AxisStep stp = step[s];
      if(!stp.axis.down) break;

      // check if resulting index path will be duplicate free
      final boolean d = pathNodes(data, s) == null;

      // choose cheapest index access
      for(int p = 0; p < stp.pred.length; ++p) {
        final IndexContext ic = new IndexContext(ctx, data, stp, d);
        if(!stp.pred[p].indexAccessible(ic)) continue;

        if(ic.costs == 0) {
          if(ic.not) {
            // not operator... accept all results
            stp.pred[p] = Bln.TRUE;
            continue;
          }
          // no results...
          ctx.compInfo(OPTNOINDEX, this);
          return Empty.SEQ;
        }
        if(ics == null || ics.costs > ic.costs) {
          ics = ic;
          pmin = p;
          smin = s;
        }
      }
    }

    // no index access possible...
    if(ics == null) return this;

    // replace expressions for index access
    final AxisStep stp = step[smin];
    final Expr ie = stp.pred[pmin].indexEquivalent(ics);

    if(ics.seq) {
      // do not invert path
      stp.pred[pmin] = ie;
    } else {
      AxisStep[] inv = {};

      // collect remaining predicates
      final Expr[] newPreds = new Expr[stp.pred.length - 1];
      int c = 0;
      for(int p = 0; p != stp.pred.length; ++p) {
        if(p != pmin) newPreds[c++] = stp.pred[p];
      }

      // invert path before index step
      for(int j = smin; j >= 0; j--) {
        final Axis ax = step[j].axis.invert();
        if(ax == null) break;

        if(j != 0) {
          final AxisStep prev = step[j - 1];
          inv = Array.add(inv, AxisStep.get(input, ax, prev.test, prev.pred));
        } else {
          final Test test = DocTest.get(ctx, data);
          // add document test for collections and axes other than ancestors
          if(test != Test.DOC || ax != Axis.ANC && ax != Axis.ANCORSELF)
            inv = Array.add(inv, AxisStep.get(input, ax, test));
        }
      }
      final boolean simple = inv.length == 0 && newPreds.length == 0;

      // create resulting expression
      AxisPath result = null;
      if(ie instanceof AxisPath) {
        result = (AxisPath) ie;
      } else if(smin + 1 < step.length || !simple) {
        result = simple ? new AxisPath(input, ie) :
          new AxisPath(input, ie, AxisStep.get(input, Axis.SELF, Test.NOD));
      } else {
        return ie;
      }

      // add remaining predicates to last step
      final int sl = result.step.length - 1;
      result.step[sl] = result.step[sl].addPreds(newPreds);
      // add inverted path as predicate to last step
      if(inv.length != 0) result.step[sl] = result.step[sl].addPreds(
          AxisPath.get(input, null, inv));

      // add remaining steps
      for(int s = smin + 1; s < step.length; ++s) {
        result.step = Array.add(result.step, step[s]);
      }
      return result;
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value c = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    Value r = root != null ? root.value(ctx) : c;

    if(!cache || citer == null || lvalue.type != Type.DOC ||
        r.type != Type.DOC || !((ANode) lvalue).is((ANode) r)) {

      lvalue = r;
      citer = new NodeCache().random();
      if(r != null) {
        final Iter ir = ctx.iter(r);
        while((r = ir.next()) != null) {
          ctx.value = r;
          iter(0, citer, ctx);
        }
      } else {
        ctx.value = null;
        iter(0, citer, ctx);
      }
      citer.sort();
    } else {
      citer.reset();
    }

    ctx.value = c;
    ctx.size = cs;
    ctx.pos = cp;
    return citer;
  }

  /**
   * Recursive step iterator.
   * @param l current step
   * @param nc node builder
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void iter(final int l, final NodeCache nc, final QueryContext ctx)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodIter} instance
    final NodeIter ni = (NodeIter) ctx.iter(step[l]);
    final boolean more = l + 1 != step.length;
    ANode node;
    while((node = ni.next()) != null) {
      if(more) {
        ctx.value = node;
        iter(l + 1, nc, ctx);
      } else {
        ctx.checkStop();
        nc.add(node);
      }
    }
  }

  /**
   * Optimizes descendant-or-self steps and static types.
   * @param ctx query context
   */
  private void optSteps(final QueryContext ctx) {
    boolean opt = false;
    for(int l = 1; l < step.length; ++l) {
      if(!step[l - 1].simple(DESCORSELF, false)) continue;

      final AxisStep next = step[l];
      if(next.axis == CHILD && !next.uses(Use.POS)) {
        // descendant-or-self::node()/child::X -> descendant::X
        Array.move(step, l, -1, step.length - l);
        step = Arrays.copyOf(step, step.length - 1);
        next.axis = DESC;
        opt = true;
      } else if(next.axis == ATTR && !next.uses(Use.POS)) {
        // descendant-or-self::node()/@X -> descendant-or-self::*/@X
        step[l - 1].test = new NameTest(false, step[l - 1].input);
        opt = true;
      }
    }
    if(opt) ctx.compInfo(OPTDESC);

    // set atomic type for single attribute steps to speedup predicate tests
    if(root == null && step.length == 1 && step[0].axis == ATTR &&
        step[0].test.test == Name.STD) step[0].type = SeqType.NOD_ZO;
  }

  /**
   * Checks if the location path will never yield results.
   * @return empty step, or {@code null}
   */
  private AxisStep emptyStep() {
    for(int l = 0; l < step.length; ++l) {
      final AxisStep s = step[l];
      final Axis sa = s.axis;
      if(l == 0) {
        if(root instanceof CAttr) {
          if(sa == CHILD || sa == DESC) return s;
        } else if(root instanceof DBNode && ((DBNode) root).type == Type.DOC ||
            root instanceof CDoc) {
          if(sa != CHILD && sa != DESC && sa != DESCORSELF &&
            (sa != SELF && sa != ANCORSELF ||
             s.test != Test.NOD && s.test != Test.DOC)) return s;
        }
      } else {
        final AxisStep ls = step[l - 1];
        final Axis lsa = ls.axis;
        if(sa == SELF || sa == DESCORSELF) {
          // .../self:: / .../descendant-or-self::
          if(s.test == Test.NOD) continue;
          // @.../...
          if(lsa == ATTR && s.test.type != Type.ATT) return s;
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
   * Inverts a location path.
   * @param r new root node
   * @param curr current location step
   * @return inverted path
   */
  public final AxisPath invertPath(final Expr r, final AxisStep curr) {
    // hold the steps to the end of the inverted path
    int s = step.length;
    final AxisStep[] e = new AxisStep[s--];
    // add predicates of last step to new root node
    final Expr rt = step[s].pred.length != 0 ?
        new Filter(input, r, step[s].pred) : r;

    // add inverted steps in a backward manner
    int c = 0;
    while(--s >= 0) {
      e[c++] = AxisStep.get(input, step[s + 1].axis.invert(),
          step[s].test, step[s].pred);
    }
    e[c] = AxisStep.get(input, step[s + 1].axis.invert(), curr.test);
    return new AxisPath(input, rt, e);
  }

  /**
   * Adds a predicate to the last step.
   * @param pred predicate to be added
   * @return resulting path instance
   */
  public final AxisPath addPreds(final Expr... pred) {
    step[step.length - 1] = step[step.length - 1].addPreds(pred);
    return get(input, root, step);
  }

  @Override
  public final AxisPath copy() {
    final AxisStep[] steps = new AxisStep[step.length];
    for(int s = 0; s < step.length; ++s) steps[s] = AxisStep.get(step[s]);
    return get(input, root, steps);
  }

  @Override
  public final Expr addText(final QueryContext ctx) throws QueryException {
    final AxisStep s = step[step.length - 1];

    if(s.pred.length != 0 || !s.axis.down || s.test.type == Type.ATT ||
        s.test.test != Name.NAME && s.test.test != Name.STD) return this;

    final Data data = ctx.resource.data();
    if(data == null || !data.meta.uptodate) return this;

    final StatsKey stats = data.tags.stat(data.tags.id(s.test.name.ln()));
    if(stats != null && stats.leaf) {
      step = Array.add(step, AxisStep.get(input, Axis.CHILD, Test.TXT));
      ctx.compInfo(OPTTEXT, this);
    }
    return this;
  }

  @Override
  public final boolean uses(final Use u) {
    return uses(step, u);
  }

  @Override
  public final int count(final Var v) {
    int c = 0;
    for(final AxisStep s : step) c += s.count(v);
    return c + super.count(v);
  }

  @Override
  public final boolean removable(final Var v) {
    for(final AxisStep s : step) if(!s.removable(v)) return false;
    return super.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    for(int s = 0; s != step.length; ++s) step[s].remove(v);
    return super.remove(v);
  }

  @Override
  public boolean duplicates() {
    return false;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisPath)) return false;
    final AxisPath ap = (AxisPath) cmp;
    if((root == null || ap.root == null) && root != ap.root ||
        step.length != ap.step.length ||
        root != null && !root.sameAs(ap.root)) return false;

    for(int s = 0; s < step.length; ++s) {
      if(!step[s].sameAs(ap.step[s])) return false;
    }
    return true;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    super.plan(ser, step);
  }

  @Override
  public final String toString() {
    return toString(step);
  }
}
