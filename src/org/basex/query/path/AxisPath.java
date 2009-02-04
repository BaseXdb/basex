package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;
import static org.basex.query.path.Test.NODE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.SkelNode;
import org.basex.data.StatsKey;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.expr.For;
import org.basex.query.expr.Pred;
import org.basex.query.expr.Return;
import org.basex.query.expr.Root;
import org.basex.query.expr.VarCall;
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
    return new AxisPath(r, st).iterator(pred);
  }

  /**
   * If possible, converts this path expression to a path iterator.
   * @param pred predicate flag
   * @return resulting operator
   */
  public final AxisPath iterator(final boolean pred) {
    // variables in root expression or predicates not supported yet..
    if(pred || root != null && root.countVar(null) != 0 || pred) return this;

    // Simple iterator: one downward location step
    if(step.length == 1 && step[0].axis.down)
      return new SimpleIterPath(root, step);

    // check if all steps are child steps
    boolean children = true;
    for(final Step s : step) children &= s.axis == Axis.CHILD;
    if(children) 
      return new ChildIterPath(root, step);
    
 /*   boolean parents = true;
    for(final Step s : step) parents &= s.axis == Axis.PARENT;
    if(parents) 
      return new ParentIterPath(root, step);
*/
    // return null if no iterator could be created
    return this;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
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
    setRoot(ctx);
    final Expr e = c(ctx);
    ctx.item = ci;
    return e;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException exception
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
    boolean preds = false;
    for(final Step s : step) {
      // check if we have a predicate
      if(s.pred.length != 0) {
        preds = true;
        if(s.countVar(null) != 0) {
          // no caching possible - skip other steps
          cache = false;
          return this;
        }
      }
    }

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && root.countVar(null) == 0;

    // check if the context item is set to a document node
    final Data data = ctx.data();
    if(data != null && ctx.item.type == Type.DOC) {
      // check index access
      Expr e = index(ctx, data);
      if(e != this) return e;
  
      // check children path rewriting
      if(!preds) {
        e = children(ctx, data);
        if(e != this) return e;
      }
    }

    // if applicable, return iterator
    return iterator(preds);
  }

  /**
   * If possible, converts a descendant step to several child steps.
   * @param ctx query context
   * @param data data reference
   * @return path
   */
  private AxisPath children(final QueryContext ctx, final Data data) {
    // convert single descendant step to child steps
    if(!data.meta.uptodate || data.ns.size() != 0 || step.length != 1 ||
        step[0].axis != Axis.DESC || step[0].test.kind != Kind.NAME)
      return this;

    final ArrayList<SkelNode> n = new ArrayList<SkelNode>();
    n.add(data.skel.root);
    final int name = data.tagID(step[0].test.name.ln());
    
    int c = 0;
    SkelNode node = null;
    for(final SkelNode sn : data.skel.desc(n, 0, Data.DOC, true)) {
      if(sn.kind == Data.ELEM && name == sn.name) {
        node = sn;
        c++;
      }
    }
    
    if(c == 1) {
      final TokenList tl = new TokenList();
      while(node.par != null) {
        tl.add(data.tags.key(node.name));
        node = node.par;
      }
      final Step[] steps = new Step[tl.size];
      for(int t = 0; t < tl.size; t++) steps[t] = Step.get(Axis.CHILD,
          new NameTest(new QNm(tl.list[tl.size - t - 1]), Kind.NAME, false));

      final AxisPath path = get(root, steps);
      ctx.compInfo(OPTCHILD, this);
      return path;
    }
    return this;
  }

  /**
   * Check if the path expression can be replaced with an index access.
   * @param ctx query context
   * @param data data reference
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr index(final QueryContext ctx, final Data data)
      throws QueryException {
    
    // skip position predicates and horizontal axes
    for(final Step s : step) if(s.usesPos(ctx) || !s.axis.vert) return this;

    // check if path can be converted to an index access
    for(int i = 0; i < step.length; i++) {
      // find cheapest index access
      final Step stp = step[i];
      IndexContext ictx = null;
      int minp = 0;

      for(int p = 0; p < stp.pred.length; p++) {
        final IndexContext ic = new IndexContext(data, stp);
        stp.pred[p].indexAccessible(ctx, ic);
        if(ic.io && ic.iu) {
          if(ictx == null || ictx.is > ic.is) {
            ictx = ic;
            minp = p;
          }
        }
      }

      // no index access possible; skip remaining tests
      if(ictx == null || !ictx.io || !ictx.iu) continue;

      // no results...
      if(ictx.is == 0) {
        if(ictx.ftnot) {
          // not operator... accept all results
          stp.pred[minp] = Bln.TRUE;
          continue;
        }
        ctx.compInfo(OPTNOINDEX, this);
        return Seq.EMPTY;
      }

      // replace expressions for index access
      final Expr ie = stp.pred[minp].indexEquivalent(ctx, ictx);

      if(ictx.seq) {
        // do not invert path
        stp.pred[minp] = ie;
      } else {
        Step[] inv = {};

        // collect remaining predicates
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
            if(a == Axis.PARENT) inv = Array.add(inv, Step.get(a,
                new KindTest(Type.DOC)));
          } else {
            final Step prev = step[j - 1];
            if(prev.pred.length != 0) break;
            inv = Array.add(inv, Step.get(a, prev.test));
          }
        }
        final boolean add = inv.length != 0 || newPreds.length != 0;

        // create resulting expression
        AxisPath result = null;
        if(ie instanceof AxisPath) {
          result = (AxisPath) ie;
        } else if(add || i + 1 < step.length) {
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
          result.step[sl] = result.step[sl].addPred(new AxisPath(null, inv));
        }
        // add remaining steps
        for(int j = i + 1; j < step.length; j++) {
          result.step = Array.add(result.step, step[j]);
        }
        return result;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = root != null ? ctx.iter(root).finish() : ctx.item;

    if(!cache || citem == null || litem != it || it.type != Type.DOC) {
      litem = it;
      final Item c = ctx.item;
      final long cs = ctx.size;
      final long cp = ctx.pos;
      ctx.item = it;

      citem = new NodIter();
      iter(0, citem, ctx);
      citem.sort(true);

      ctx.item = c;
      ctx.size = cs;
      ctx.pos = cp;
    } else {
      citem.reset();
    }
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

  /**
   * Sets the root node as context item.
   * @param ctx query context
   */
  public void setRoot(final QueryContext ctx) {
    if(root != null) {
      if(root instanceof Root) {
        if(ctx.item != null) ctx.item = ((Root) root).root(ctx.item);
      } else {
        ctx.item = null;
        if(root.i()) ctx.item = (Item) root;
      }
    }
  }

  @Override
  public long size(final QueryContext ctx) {
    final Item ci = ctx.item;
    long res = -1;
    setRoot(ctx);

    final Data data = ctx.data();
    if(data != null) {
      if(data.meta.uptodate && data.ns.size() == 0) {
        HashSet<SkelNode> nodes = new HashSet<SkelNode>();
        nodes.add(data.skel.root);

        for(final Step s : step) {
          res = -1;
          nodes = s.count(nodes, data);
          if(nodes == null) break;
          res = 0;
          for(final SkelNode sn : nodes) res += sn.count;
        }
      }
    }
    ctx.item = ci;
    return res;
  }

  /**
   * Converts each step into a For-Loops.
   *
   * @param var variable
   * @param pos position variable
   * @param score score variable
   * @return array with for expression
   */
  public For[] convSteps(final Var var, final Var pos, final Var score) {
    final For[] f = new For[step.length];
    final VarCall vc = new VarCall(var);
    for (int i = 0; i < step.length; i++) {
      f[i] = new For(new AxisPath(vc, new Step[]{step[i]}), var, pos, score);
    }
    return f;
  }

  /**
   * Merges superfluous descendant-or-self steps.
   * This method implies that all expressions are location steps.
   * @param ctx query context
   */
  private void mergeDesc(final QueryContext ctx) {
    int ll = step.length;
    for(int l = 1; l < ll; l++) {
      if(!step[l - 1].simple(DESCORSELF)) continue;
      final Step next = step[l];
      if(next.axis == CHILD && !next.usesPos(ctx)) {
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
   * @throws QueryException evaluation exception
   */
  private void checkEmpty() throws QueryException {
    final int ll = step.length;
    if(ll > 0) {
      final Step s = step[0];
      if(root instanceof DBNode && ((DBNode) root).type == Type.DOC &&
        (s.axis == ATTR || s.axis == PARENT || s.axis == SELF && s.test != NODE)
        || root instanceof CAttr && s.axis == CHILD) warning(s);
    }

    for(int l = 1; l < ll; l++) {
      final Step s1 = step[l];
      final Step s0 = step[l - 1];

      if(s1.axis == SELF) {
        if(s1.test == NODE) continue;

        if(s0.axis == ATTR) warning(s1);
        if(s0.test.type == Type.TXT && s1.test.type != Type.TXT) warning(s1);

        final QNm n1 = s1.test.name;
        final QNm n0 = s0.test.name;
        if(n0 == null || n1 == null) continue;
        if(!n1.eq(n0)) warning(s1);

      } else if(s1.axis == DESCORSELF) {
        if(s1.test == NODE) continue;
        if(s0.axis == ATTR) warning(s1);

        if(s0.test.type == Type.TXT && s1.test.type != Type.TXT) warning(s1);
      } else if(s1.axis == DESC || s1.axis == CHILD) {
        if(s0.axis == ATTR || s0.test.type == Type.TXT) warning(s1);
      }
    }
  }

  /**
   * Throws a static warning.
   * @param s step
   * @throws QueryException evaluation exception
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

  @Override
  public Expr addText(final QueryContext ctx) {
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

  /**
   * Adds a position predicate to the last step.
   * @param ctx query context
   * @return resulting path instance
   */
  public final AxisPath addPos(final QueryContext ctx) {
    step[step.length - 1] = step[step.length - 1].addPos(ctx);
    return get(root, step);
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
  public boolean usesPos(final QueryContext ctx) {
    return usesPos(step, ctx);
  }

  @Override
  public int countVar(final Var v) {
    return usesVar(v, step);
  }

  @Override
  public Expr removeVar(final Var v) {
    for(int s = 0; s != step.length; s++) step[s].removeVar(v);
    return super.removeVar(v);
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return size(ctx) == 1 ? Return.NOD : Return.NODSEQ;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisPath)) return false;
    final AxisPath ap = (AxisPath) cmp;
    if(step.length != ap.step.length) return false;
    for(int s = 0; s < step.length; s++) {
      if(!step[s].sameAs(ap.step[s])) return false;
    }
    return true;
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
