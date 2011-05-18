package org.basex.query.path;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Filter;
import org.basex.query.expr.Pos;
import org.basex.query.item.Bln;
import org.basex.query.item.Empty;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Name;
import static org.basex.query.util.Err.*;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Axis path expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class AxisPath extends Path {
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
   * @param s axis steps
   */
  protected AxisPath(final InputInfo ii, final Expr r, final Expr... s) {
    super(ii, r, s);
  }

  /**
   * If possible, converts this path expression to a path iterator.
   * @param ctx context reference
   * @return resulting operator
   */
  protected AxisPath finish(final QueryContext ctx) {
    // evaluate number of results
    size = size(ctx);
    type = SeqType.get(step[step.length - 1].type().type, size);
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
      switch(step(s).axis) {
        // reverse axes - don't iterate
        case ANC: case ANCORSELF: case PREC: case PRECSIBL:
          return false;
        // multiple, unsorted results - only iterate at last step,
        // or if last step uses attribute axis
        case DESC: case DESCORSELF: case FOLL: case FOLLSIBL:
          return s + 1 == sl || s + 2 == sl && step(s + 1).axis == Axis.ATTR;
        // allow iteration for CHILD, ATTR, PARENT and SELF
        default:
      }
    }
    return true;
  }

  @Override
  protected Expr compPath(final QueryContext ctx) throws QueryException {
    for(final Expr s : step) checkUp(s, ctx);

    // merge two axis paths
    if(root instanceof AxisPath) {
      Expr[] st = ((AxisPath) root).step;
      root = ((AxisPath) root).root;
      for(final Expr s : step) st = Array.add(st, s);
      step = st;
      // refresh root context
      ctx.compInfo(OPTPATH);
      ctx.value = root(ctx);
    }
    final AxisStep s = voidStep(step);
    if(s != null) COMPSELF.thrw(input, s);

    for(int i = 0; i != step.length; ++i) {
      final Expr e = step[i].comp(ctx);
      if(!(e instanceof AxisStep)) return e;
      step[i] = e;
    }
    optSteps(ctx);

    // retrieve data reference
    final Data data = ctx.data();
    if(data != null && data.meta.uptodate && ctx.value.type == NodeType.DOC) {
      Expr e = this;
      // check index access
      if(root != null && !uses(Use.POS)) e = index(ctx, data);
      // check children path rewriting
      if(e == this) e = children(ctx, data);
      // return optimized expression
      if(e != this) return e.comp(ctx);
    }

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && !uses(Use.VAR);

    // if applicable, use iterative evaluation
    final Path path = finish(ctx);

    // heuristics: wrap with filter expression if only one result is expected
    //return path;
    return size() != 1 ? path :
      new Filter(input, this, Pos.get(1, size(), input)).comp2(ctx);
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
      final AxisStep stp = step(s);
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
    final AxisStep stp = step(smin);
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
        final Axis ax = step(j).axis.invert();
        if(ax == null) break;

        if(j != 0) {
          final AxisStep prev = step(j - 1);
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
      if(sl >= 0) {
        result.step[sl] = result.step(sl).addPreds(newPreds);
        // add inverted path as predicate to last step
        if(inv.length != 0) result.step[sl] = result.step(sl).addPreds(
            Path.get(input, null, inv));
      }

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

    if(!cache || citer == null || lvalue.type != NodeType.DOC ||
        r.type != NodeType.DOC || !((ANode) lvalue).is((ANode) r)) {
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
    for(ANode node; (node = ni.next()) != null;) {
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
   * Inverts a location path.
   * @param r new root node
   * @param curr current location step
   * @return inverted path
   */
  public final AxisPath invertPath(final Expr r, final AxisStep curr) {
    // hold the steps to the end of the inverted path
    int s = step.length;
    final Expr[] e = new Expr[s--];
    // add predicates of last step to new root node
    final Expr rt = step(s).pred.length != 0 ?
        new Filter(input, r, step(s).pred) : r;

    // add inverted steps in a backward manner
    int c = 0;
    while(--s >= 0) {
      e[c++] = AxisStep.get(input, step(s + 1).axis.invert(),
          step(s).test, step(s).pred);
    }
    e[c] = AxisStep.get(input, step(s + 1).axis.invert(), curr.test);
    return new AxisPath(input, rt, e);
  }

  @Override
  public final Expr addText(final QueryContext ctx) throws QueryException {
    final AxisStep s = step(step.length - 1);

    if(s.pred.length != 0 || !s.axis.down || s.test.type == NodeType.ATT ||
        s.test.test != Name.NAME && s.test.test != Name.STD) return this;

    final Data data = ctx.data();
    if(data == null || !data.meta.uptodate) return this;

    final StatsKey stats = data.tags.stat(data.tags.id(s.test.name.ln()));
    if(stats != null && stats.leaf) {
      step = Array.add(step, AxisStep.get(input, Axis.CHILD, Test.TXT));
      ctx.compInfo(OPTTEXT, this);
    }
    return this;
  }

  /**
   * Returns the specified axis step.
   * @param i index
   * @return step
   */
  public AxisStep step(final int i) {
    return (AxisStep) step[i];
  }

  @Override
  public final Path copy() {
    final Expr[] steps = new Expr[step.length];
    for(int s = 0; s < step.length; ++s) steps[s] = AxisStep.get(step(s));
    return get(input, root, steps);
  }

  @Override
  public final int count(final Var v) {
    int c = 0;
    for(final Expr s : step) c += s.count(v);
    return c + super.count(v);
  }

  @Override
  public final boolean removable(final Var v) {
    for(final Expr s : step) if(!s.removable(v)) return false;
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
}
