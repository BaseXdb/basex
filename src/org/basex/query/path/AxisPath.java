package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;
import static org.basex.query.path.Test.NODE;
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
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Pred;
import org.basex.query.expr.Return;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Kind;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.TokenList;

/**
 * Axis Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public class AxisPath extends Path {
  /** Expression list. */
  public Step[] step;
  /** Flag for result caching. */
  private boolean cache;
  /** Cached result. */
  private NodIter citem;
  /** Last visited item. */
  private Item litem;

  /**
   * Constructor.
   * @param r root expression; can be null
   * @param s location steps; will at least have one entry
   */
  protected AxisPath(final Expr r, final Step... s) {
    super(r);
    step = s;
  }

  /**
   * Constructor.
   * @param r root expression; can be null
   * @param st location steps; will at least have one entry
   * @return class instance
   */
  public static final AxisPath get(final Expr r, final Step... st) {
    // check if steps have predicates
    boolean pred = false;
    for(final Step s : st) pred |= s.pred.length != 0;
    return new AxisPath(r, st).iterator(null);
  }

  /**
   * If possible, converts this path expression to a path iterator.
   * @param ctx context reference
   * @return resulting operator
   */
  private AxisPath iterator(final QueryContext ctx) {
    return iterable(ctx) ? new IterPath(root, step) : this;
  }

  /**
   * Checks if the path is iterable.
   * To check if the result of a path is in document order so that no special
   * ordering functions are needed and we can use the iterative evaluation,
   * we are geared to an automaton that is provided in the paper
   * "Avoiding Unnecessary Ordering Operations in XPath" by
   * Jan Hidders and Philippe Michiels (Page 6 - Figure 5).
   * Since the implementation has a slightly different implementation,
   * it is possible to have alternating child and parent steps or more parent
   * steps - different than described in the paper.
   * @param ctx context reference
   * @return resulting operator
   */
  private boolean iterable(final QueryContext ctx) {
    // skip paths with variables and roots with duplicates
    if(root == null || root.uses(Use.VAR, ctx) || root.duplicates(ctx)) {
      return false;
    }

    int i = 0;
    while(true) {
      final Step s = step[i];

      // first steps can be attribute, parent or self steps.
      if(s.axis == Axis.ATTR || s.axis == Axis.PARENT || s.axis == Axis.SELF) {
        if(++i == step.length) return true;
        continue;
      }

      // if after only-parent steps or without any parent steps the next
      // and only step is an descendant, descendant-or-self, preceding
      // or following step, the result of the path is still in document order.
      if(s.axis == Axis.DESC || s.axis == Axis.DESCORSELF ||
          s.axis == Axis.FOLL || s.axis == Axis.PREC) {
        return ++i == step.length;
      }

      // if after only-parent steps or without any parent steps the next
      // step is an attribute, child, self, preceding-sibling or
      // following-sibling step, the result of the path is still in
      // document order.
      if(s.axis == Axis.CHILD || s.axis == Axis.FOLLSIBL ||
          s.axis == Axis.PRECSIBL) {
        if(++i == step.length) return true;

        // in this state finite attribute, child, parent or self steps
        // are possible.
        while(step[i].axis == Axis.ATTR || step[i].axis == Axis.CHILD ||
            step[i].axis == Axis.PARENT || step[i].axis == Axis.SELF) {
          if(++i == step.length) return true;
        }

        // the last step can be a attribute, child, parent or self step,
        // and also a descendant or descendant-or-self step.
        return i + 1 == step.length && (step[i].axis == Axis.DESC ||
          step[i].axis == Axis.DESCORSELF);
      }
      return false;
    }
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(final Step s : step) checkUp(s, ctx);
    if(root instanceof Context) root = null;

    // merge two axis paths
    if(root instanceof AxisPath) {
      Step[] st = ((AxisPath) root).step;
      root = ((AxisPath) root).root;
      for(final Step s : step) st = Array.add(st, s);
      step = st;
    }
    checkEmpty();

    final Item ci = ctx.item;
    ctx.item = root(ctx);

    final Expr e = c(ctx);
    ctx.item = ci;
    return e;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException query exception
   */
  private Expr c(final QueryContext ctx) throws QueryException {
    // step optimizations will always return step instances
    for(int i = 0; i != step.length; i++) {
      final Expr e = step[i].comp(ctx);
      if(!(e instanceof Step)) return e;
      step[i] = (Step) e;
    }
    mergeDesc(ctx);

    // check if steps have predicates
    boolean pos = false;
    for(final Step s : step) {
      // check if we have a predicate
      if(s.pred.length != 0) {
        if(s.uses(Use.POS, ctx)) {
          // variable and position test found - skip optimizations
          if(s.uses(Use.VAR, ctx)) return this;
          pos = true;
        }
      }
    }

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && !root.uses(Use.VAR, ctx);

    // check if context is set to document nodes
    final Data data = ctx.data();
    if(data != null) {
      boolean doc = true;
      final Item item = ctx.item;
      if(item.size(ctx) != ctx.docs || !(item instanceof Seq) ||
          ((Seq) item).val != ctx.doc) {
        final Iter iter = item.iter();
        Item it;
        while((it = iter.next()) != null) doc &= it.type == Type.DOC;
      }

      if(doc) {
        // check if no position is used
        if(!pos) {
          // check index access
          final Expr e = index(ctx, data);
          if(e != this) return e;
        }

        // check children path rewriting
        final Expr e = children(ctx, data);
        if(e != this) return e;
      }
    }

    // if applicable, return iterator
    return iterator(ctx);
  }

  /**
   * Converts descendant to child steps.
   * @param ctx query context
   * @param data data reference
   * @return path
   */
  private AxisPath children(final QueryContext ctx, final Data data) {
    for(int i = 0; i < step.length; i++) {
      if(step[i].axis != Axis.DESC) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, i);
      if(nodes == null) continue;

      ctx.compInfo(OPTCHILD, step[i]);

      // cache child steps
      final TokenList tl = new TokenList();
      while(nodes.get(0).par != null) {
        byte[] tag = data.tags.key(nodes.get(0).name);
        for(int j = 0; j < nodes.size(); j++) {
          if(nodes.get(0).name != nodes.get(j).name) tag = null;
        }
        tl.add(tag);
        nodes = data.path.parent(nodes);
      }

      // build new steps
      int ts = tl.size();
      final Step[] steps = new Step[ts + step.length - i - 1];
      for(int t = 0; t <= ts - 1; t++) {
        final Expr[] preds = t == ts - 1 ? step[i].pred : new Expr[] {};
        final byte[] n = tl.get(ts - t - 1);
        final NameTest nt = n == null ? new NameTest(false) :
          new NameTest(new QNm(n), Kind.NAME, false);
        steps[t] = Step.get(Axis.CHILD, nt, preds);
      }
      while(++i < step.length) steps[ts++] = step[i];

      return get(root, steps).children(ctx, data);
    }
    return this;
  }

  /**
   * Returns all summary path nodes for the specified location step or null
   * if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param l last step to be checked
   * @return path nodes
   */
  private ArrayList<PathNode> pathNodes(final Data data, final int l) {
    // convert single descendant step to child steps
    if(!data.meta.pathindex || !data.meta.uptodate || data.ns.size() != 0)
      return null;

    ArrayList<PathNode> in = data.path.root();
    for(int s = 0; s <= l; s++) {
      final boolean desc = step[s].axis == Axis.DESC;
      if(!desc && step[s].axis != Axis.CHILD || step[s].test.kind != Kind.NAME)
        return null;

      final int name = data.tagID(step[s].test.name.ln());

      final ArrayList<PathNode> out = new ArrayList<PathNode>();
      for(final PathNode sn : data.path.desc(in, desc)) {
        if(sn.kind == Data.ELEM && name == sn.name) {
          // skip test if a tag is found on different levels
          if(out.size() != 0 && out.get(0).level() != sn.level()) return null;
          out.add(sn);
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

    // skip position predicates and horizontal axes
    for(final Step s : step) if(!s.axis.down) return this;

    // cache index access costs
    IndexContext ics = null;
    int ips = 0;
    int ms = 0;

    // check if path can be converted to an index access
    for(int s = 0; s < step.length; s++) {
      // find cheapest index access
      final Step stp = step[s];
      // check if resulting index path will be duplicate free
      final boolean d = pathNodes(data, s) == null;

      // choose cheapest index access
      for(int p = 0; p < stp.pred.length; p++) {
        final IndexContext ic = new IndexContext(ctx, data, stp, d);
        if(!stp.pred[p].indexAccessible(ic)) continue;
        if(ic.is == 0) {
          if(ic.not) {
            // not operator... accept all results
            stp.pred[p] = Bln.TRUE;
            continue;
          }
          // no results...
          ctx.compInfo(OPTNOINDEX, this);
          return Seq.EMPTY;
        }
        if(ics == null || ics.is > ic.is) {
          ics = ic;
          ips = p;
          ms = s;
        }
      }
    }

    // no index access possible...
    if(ics == null) return this;

    // replace expressions for index access
    final Step stp = step[ms];
    final Expr ie = stp.pred[ips].indexEquivalent(ics);

    if(ics.seq) {
      // do not invert path
      stp.pred[ips] = ie;
    } else {
      Step[] inv = {};

      // collect remaining predicates
      final Expr[] newPreds = new Expr[stp.pred.length - 1];
      int c = 0;
      for(int p = 0; p != stp.pred.length; p++) {
        if(p != ips) newPreds[c++] = stp.pred[p];
      }

      // invert path before index step
      for(int j = ms; j >= 0; j--) {
        final Axis a = step[j].axis.invert();
        if(a == null) break;

        if(j == 0) {
          if(a != Axis.ANC && a != Axis.ANCORSELF)
            inv = Array.add(inv, Step.get(a, new KindTest(Type.DOC)));
        } else {
          final Step prev = step[j - 1];
          inv = Array.add(inv, Step.get(a, prev.test, prev.pred));
        }
      }
      final boolean add = inv.length != 0 || newPreds.length != 0;

      // create resulting expression
      AxisPath result = null;
      if(ie instanceof AxisPath) {
        result = (AxisPath) ie;
      } else if(add || ms + 1 < step.length) {
        result = add ? new AxisPath(ie, Step.get(Axis.SELF, Test.NODE)) :
          new AxisPath(ie);
      } else {
        return ie;
      }

      // add remaining predicates to last step
      final int sl = result.step.length - 1;
      for(final Expr np : newPreds) {
        result.step[sl] = result.step[sl].addPred(np);
      }
      // add inverted path as predicate to last step
      if(inv.length != 0) {
        result.step[sl] = result.step[sl].addPred(AxisPath.get(null, inv));
      }
      // add remaining steps
      for(int s = ms + 1; s < step.length; s++) {
        result.step = Array.add(result.step, step[s]);
      }
      return result.comp(ctx);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item c = ctx.item;
    final long cs = ctx.size;
    final long cp = ctx.pos;

    final Item it = root != null ? ctx.iter(root).finish() : ctx.item;

    if(!cache || citem == null || litem != it || it.type != Type.DOC) {
      litem = it;
      ctx.item = it;
      citem = new NodIter();
      iter(0, citem, ctx);
      citem.sort(true);
    } else {
      citem.reset();
    }

    ctx.item = c;
    ctx.size = cs;
    ctx.pos = cp;
    return citem;
  }

  /**
   * Recursive step iterator.
   * @param l current step
   * @param ni node builder
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void iter(final int l, final NodIter ni, final QueryContext ctx)
      throws QueryException {

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

  @Override
  public final long size(final QueryContext ctx) {
    long res = -1;

    final Item rt = root(ctx);
    final Data data = rt != null && rt.type == Type.DOC &&
      rt instanceof DBNode ? ((DBNode) rt).data : null;

    if(data != null && !data.meta.pathindex && data.meta.uptodate &&
        data.ns.size() == 0) {

      ArrayList<PathNode> nodes = data.path.root();
      for(final Step s : step) {
        res = -1;
        nodes = s.count(nodes, data);
        if(nodes == null) break;
        res = 0;
        for(final PathNode sn : nodes) res += sn.count;
      }
    }
    return res;
  }

  /**
   * Merges superfluous descendant-or-self steps.
   * This method implies that all expressions are location steps.
   * @param ctx query context
   */
  private void mergeDesc(final QueryContext ctx) {
    int ll = step.length;
    for(int l = 1; l < ll; l++) {
      if(!step[l - 1].simple(DESCORSELF, false)) continue;
      final Step next = step[l];
      if(next.axis == CHILD && !next.uses(Use.POS, ctx)) {
        Array.move(step, l, -1, ll-- - l);
        next.axis = DESC;
      }
    }
    if(ll != step.length) {
      ctx.compInfo(OPTDESC);
      step = Arrays.copyOf(step, ll);
    }
  }

  /**
   * Checks if any of the location steps will never yield results.
   * @throws QueryException query exception
   */
  private void checkEmpty() throws QueryException {
    final int ll = step.length;
    if(ll > 0) {
      final Step s = step[0];
      if(root instanceof DBNode && ((DBNode) root).type == Type.DOC &&
          (s.axis == ATTR || s.axis == PARENT || s.axis == SELF
              && s.test != NODE)
          || root instanceof CAttr && s.axis == CHILD) warning(s);
    }

    for(int l = 1; l < ll; l++) {
      final Step s1 = step[l];
      final Step s0 = step[l - 1];

      if(s1.axis == SELF || s1.axis == DESCORSELF) {
        if(s1.test == NODE) continue;
        if(s0.axis == ATTR) warning(s1);
        if(s0.test.type == Type.TXT && s1.test.type != Type.TXT) warning(s1);

        if(s1.axis == DESCORSELF) continue;
        final QNm n1 = s1.test.name;
        final QNm n0 = s0.test.name;
        if(n0 == null || n1 == null) continue;
        if(!n1.eq(n0)) warning(s1);
      } else if(s1.axis == FOLLSIBL || s1.axis == PRECSIBL) {
        if(s0.axis == ATTR) warning(s1);
      } else if(s1.axis == DESC || s1.axis == CHILD) {
        if(s0.axis == ATTR || s0.test.type == Type.TXT) warning(s1);
      }
    }
  }

  /**
   * Throws a static warning.
   * @param s step
   * @throws QueryException query exception
   */
  private void warning(final Step s) throws QueryException {
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
    int s = step.length;
    final Step[] e = new Step[s--];
    // add predicates of last step to new root node
    final Expr rt = step[s].pred.length != 0 ? new Pred(r, step[s].pred) : r;

    // add inverted steps in a backward manner
    int c = 0;
    while(--s >= 0) {
      e[c++] = Step.get(step[s + 1].axis.invert(), step[s].test, step[s].pred);
    }
    e[c] = Step.get(step[s + 1].axis.invert(), curr.test);
    return new AxisPath(rt, e);
  }

  /**
   * Adds a predicate to the last step.
   * @param pred predicate to be added
   * @return resulting path instance
   */
  public final AxisPath addPred(final Expr pred) {
    step[step.length - 1] = step[step.length - 1].addPred(pred);
    return get(root, step);
  }

  @Override
  public final Expr addText(final QueryContext ctx) throws QueryException {
    final Step s = step[step.length - 1];
    if(s.pred.length > 0 || !s.axis.down || s.test.kind != Test.Kind.NAME ||
        s.test.type == Type.ATT) return this;

    final Data data = ctx.data();
    if(data == null) return this;
    final StatsKey stats = data.tags.stat(data.tags.id(s.test.name.ln()));

    if(data.meta.uptodate && stats != null && stats.leaf) {
      step = Array.add(step, Step.get(Axis.CHILD, new KindTest(Type.TXT)));
      ctx.compInfo(OPTTEXT, this);
    }
    return this;
  }

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return uses(step, u, ctx);
  }

  @Override
  public final boolean removable(final Var v, final QueryContext ctx) {
    for(final Step s : step) if(!s.removable(v, ctx)) return false;
    return true;
  }

  @Override
  public final Expr remove(final Var v) {
    for(int s = 0; s != step.length; s++) step[s].remove(v);
    return super.remove(v);
  }

  @Override
  public final Return returned(final QueryContext ctx) {
    return size(ctx) == 1 ? Return.NOD : Return.NODSEQ;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisPath)) return false;
    final AxisPath ap = (AxisPath) cmp;
    if((root == null || ap.root == null) && root != ap.root ||
        step.length != ap.step.length ||
        root != null && !root.sameAs(ap.root)) return false;

    for(int s = 0; s < step.length; s++) {
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
