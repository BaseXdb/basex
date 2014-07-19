package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Context;
import org.basex.query.path.Test.Kind;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Root expression. */
  public Expr root;
  /** Path steps. */
  public final Expr[] steps;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps steps
   */
  Path(final InputInfo info, final Expr root, final Expr[] steps) {
    super(info);
    this.root = root;
    this.steps = steps;
  }

  /**
   * Returns a new path instance. A path implementation is chosen that works fastest for the
   * given steps.
   * @param info input info
   * @param root root expression; can be {@code null}
   * @param steps steps
   * @return path instance
   */
  public static Path get(final InputInfo info, final Expr root, final Expr... steps) {
    // new list with steps
    final ExprList stps = new ExprList(steps.length);

    // merge nested paths
    Expr rt = root;
    if(rt instanceof Path) {
      final Path path = (Path) rt;
      stps.add(path.steps);
      rt = path.root;
    }
    // remove redundant context reference
    if(rt instanceof Context) rt = null;

    // add steps of input array
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      Expr step = steps[s];
      if(step instanceof Context) {
        // remove redundant context references
        if(sl > 1) continue;
        // single step: rewrite to axis step (required to sort results of path)
        step = Step.get(((Context) step).info, SELF, Test.NOD);
      } else if(step instanceof Filter) {
        // rewrite to axis step (can be evaluated faster than filter expression)
        final Filter f = (Filter) step;
        if(f.root instanceof Context) step = Step.get(f.info, SELF, Test.NOD, f.preds);
      }
      stps.add(step);
    }

    // check if all steps are axis steps
    boolean axes = true;
    final Expr[] st = stps.finish();
    for(final Expr step : st) axes &= step instanceof Step;

    // choose best implementation
    return axes ? iterative(rt, st) ? new IterPath(info, rt, st) :
      new CachedPath(info, rt, st) : new MixedPath(info, rt, st);
  }

  /**
   * Checks if the path can be rewritten for iterative evaluation.
   * @param root root expression; can be a {@code null} reference
   * @param steps path steps
   * @return result of check
   */
  private static boolean iterative(final Expr root, final Expr... steps) {
    if(root == null || !root.iterable()) return false;

    final int sl = steps.length;
    for(int s = 0; s < sl; ++s) {
      switch(((Step) steps[s]).axis) {
        // reverse axes - don't iterate
        case ANC: case ANCORSELF: case PREC: case PRECSIBL:
          return false;
        // multiple, unsorted results - only iterate at last step,
        // or if last step uses attribute axis
        case DESC: case DESCORSELF: case FOLL: case FOLLSIBL:
          return s + 1 == sl || s + 2 == sl && ((Step) steps[s + 1]).axis == Axis.ATTR;
        // allow iteration for CHILD, ATTR, PARENT and SELF axes
        default:
      }
    }
    return true;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    final int ss = steps.length;
    for(int s = 0; s < ss - 1; s++) checkNoUp(steps[s]);
    steps[ss - 1].checkUp();
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    if(root != null) root = root.compile(qc, scp);

    final Value init = qc.value, cv = initial(qc);
    final boolean doc = cv != null && cv.type == NodeType.DOC;

    qc.value = cv;
    try {
      for(int s = 0; s < steps.length; s++) {
        Expr e = steps[s];

        // axis step: if input is a document, its type is temporarily generalized
        final boolean as = e instanceof Step;
        if(as && s == 0 && doc) cv.type = NodeType.NOD;

        e = e.compile(qc, scp);
        if(e.isEmpty()) return optPre(null, qc);
        steps[s] = e;

        // no axis step: invalidate context value
        if(!as) qc.value = null;
      }
    } finally {
      if(doc) cv.type = NodeType.DOC;
      qc.value = init;
    }

    // no steps left
    if(steps.length == 0) return root == null ? new Context(info) : root;
    // optimize path
    return optimize(qc, scp);
  }

  @Override
  public final Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final Value v = initial(qc);
    if(emptyPath(v)) return optPre(null, qc);

    // merge descendant steps
    Expr e = mergeSteps(qc);
    if(e == this && v != null && v.type == NodeType.DOC) {
      // check index access
      e = index(qc, v);
      // rewrite descendant to child steps
      if(e == this) e = children(qc, v);
    }
    // recompile path if it has changed
    if(e != this) return e.compile(qc, scp);

    // set atomic type for single attribute steps to speed up predicate tests
    if(root == null && steps.length == 1 && steps[0] instanceof Step) {
      final Step curr = (Step) steps[0];
      if(curr.axis == ATTR && curr.test.kind == Kind.URI_NAME) curr.seqType = SeqType.NOD_ZO;
    }

    // choose best path implementation and set type information
    final Path path = get(info, root, steps);
    path.size = path.size(qc);
    path.seqType = SeqType.get(steps[steps.length - 1].seqType().type, size);
    return path;
  }

  /**
   * Returns the initial context value of a path or {@code null}.
   * @param qc query context (may be @code null)
   * @return root
   */
  protected final Value initial(final QueryContext qc) {
    // current context value
    final Value value = qc != null ? qc.value : null;
    // no root or context expression: return context
    if(root == null || root instanceof Context) return value;
    // root is value: return root
    if(root.isValue()) return (Value) root;
    // root reference:
    if(root instanceof Root) return value != null && value.size() == 1 ? Root.root(value) : value;
    // otherwise, return null
    return null;
  }

  @Override
  public final boolean has(final Flag flag) {
    // first step or root expression will be used as context
    if(flag == Flag.CTX) return root == null || root.has(flag);
    for(final Expr s : steps) if(s.has(flag)) return true;
    return root != null && root.has(flag);
  }

  /**
   * Casts the specified step into an axis step, or returns a {@code null} reference.
   * @param i index
   * @return step
   */
  public final Step axisStep(final int i) {
    return steps[i] instanceof Step ? (Step) steps[i] : null;
  }

  /**
   * Adds a predicate to the last step.
   * @param qc query context
   * @param scp variable scope
   * @param pred predicate to be added
   * @return resulting path instance
   * @throws QueryException query exception
   */
  public final Expr addPreds(final QueryContext qc, final VarScope scp, final Expr... pred)
      throws QueryException {

    steps[steps.length - 1] = axisStep(steps.length - 1).addPreds(pred);
    return get(info, root, steps).optimize(qc, scp);
  }

  /**
   * Computes the number of results.
   * @param qc query context (may be @code null)
   * @return number of results
   */
  private long size(final QueryContext qc) {
    final Value rt = initial(qc);
    // skip computation if value contains document nodes
    if(rt == null || rt.type != NodeType.DOC) return -1;
    final Data data = rt.data();
    // skip computation if no database instance is available, is out-of-date or
    // if context does not contain all database nodes
    if(data == null || !data.meta.uptodate || data.resources.docs().size() != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths.root();
    long m = 1;
    for(int s = 0; s < steps.length; s++) {
      final Step curr = axisStep(s);
      if(curr != null) {
        nodes = curr.nodes(nodes, data);
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
   * Returns all summary path nodes for the specified location step or
   * {@code null} if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param last last step to be checked
   * @return path nodes
   */
  private ArrayList<PathNode> pathNodes(final Data data, final int last) {
    // skip request if no path index exists or might be out-of-date
    if(!data.meta.uptodate) return null;

    ArrayList<PathNode> nodes = data.paths.root();
    for(int s = 0; s <= last; s++) {
      // only follow axis steps
      final Step curr = axisStep(s);
      if(curr == null) return null;

      final boolean desc = curr.axis == DESC;
      if(!desc && curr.axis != CHILD || curr.test.kind != Kind.NAME) return null;

      final int name = data.tagindex.id(curr.test.name.local());

      final ArrayList<PathNode> tmp = new ArrayList<>();
      for(final PathNode node : PathSummary.desc(nodes, desc)) {
        if(node.kind == Data.ELEM && name == node.name) {
          // skip test if an element name occurs on different levels
          if(!tmp.isEmpty() && tmp.get(0).level() != node.level()) return null;
          tmp.add(node);
        }
      }
      if(tmp.isEmpty()) return null;
      nodes = tmp;
    }
    return nodes;
  }

  /**
   * Checks if the path will never yield results.
   * @param rt root
   * @return {@code true} if steps will never yield results
   */
  private boolean emptyPath(final Value rt) {
    for(int s = 0; s < steps.length; s++) {
      if(emptyPath(rt, s)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified step will never yield results.
   * @param rt root value
   * @param s index of step
   * @return {@code true} if steps will never yield results
   */
  private boolean emptyPath(final Value rt, final int s) {
    final Step step = axisStep(s);
    if(step == null) return false;

    final Axis axis = step.axis;
    if(s == 0) {
      // first location step:
      if(root instanceof CAttr) {
        // @.../child:: / @.../descendant::
        if(axis == CHILD || axis == DESC) return true;
      } else if(root instanceof Root || root instanceof CDoc ||
          rt != null && rt.type == NodeType.DOC) {
        if(axis == SELF || axis == ANCORSELF) {
          if(step.test != Test.NOD && step.test != Test.DOC) return true;
        } else if(axis == CHILD || axis == DESC) {
          if(step.test == Test.DOC || step.test == Test.ATT) return true;
        } else if(axis == DESCORSELF) {
          if(step.test == Test.ATT) return true;
        } else {
          return true;
        }
      }
    } else {
      // remaining steps:
      final Step last = axisStep(s - 1);
      if(last == null) return false;

      // .../self:: / .../descendant-or-self::
      if(axis == SELF || axis == DESCORSELF) {
        if(step.test == Test.NOD) return false;
        // @.../..., text()/...
        if(last.axis == ATTR && step.test.type != NodeType.ATT ||
           last.test == Test.TXT && step.test != Test.TXT) return true;
        if(axis == DESCORSELF) return false;

        // .../self::
        final QNm name = step.test.name, lastName = last.test.name;
        if(lastName == null || name == null || lastName.local().length == 0 ||
            name.local().length == 0) return false;
        // ...X/...Y
        return !name.eq(lastName);
      }
      // .../following-sibling:: / .../preceding-sibling::
      if(axis == FOLLSIBL || axis == PRECSIBL) return last.axis == ATTR;
      // .../descendant:: / .../child:: / .../attribute::
      if(axis == DESC || axis == CHILD || axis == ATTR)
        return last.axis == ATTR || last.test == Test.TXT || last.test == Test.COM ||
           last.test == Test.PI || axis == ATTR && step.test == Test.NSP;
      // .../parent:: / .../ancestor::
      if(axis == PARENT || axis == ANC) return last.test == Test.DOC;
    }
    return false;
  }

  /**
   * Converts descendant to child steps.
   * @param qc query context
   * @param rt root value
   * @return original or new expression
   */
  private Expr children(final QueryContext qc, final Value rt) {
    // skip if index does not exist or is out-dated, or if several namespaces occur in the input
    final Data data = rt.data();
    if(data == null || !data.meta.uptodate || data.nspaces.globalNS() == null) return this;

    Path path = this;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.preds.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESC || curr.has(Flag.FCS)) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, s);
      if(nodes == null) continue;

      // cache child steps
      final ArrayList<QNm> qnm = new ArrayList<>();
      while(nodes.get(0).parent != null) {
        QNm nm = new QNm(data.tagindex.key(nodes.get(0).name));
        // skip children with prefixes
        if(nm.hasPrefix()) return this;
        for(final PathNode p : nodes) {
          if(nodes.get(0).name != p.name) nm = null;
        }
        qnm.add(nm);
        nodes = PathSummary.parent(nodes);
      }
      qc.compInfo(OPTCHILD, steps[s]);

      // build new steps
      int ts = qnm.size();
      final Expr[] stps = new Expr[ts + sl - s - 1];
      for(int t = 0; t < ts; t++) {
        final Expr[] preds = t == ts - 1 ? ((Preds) steps[s]).preds : new Expr[0];
        final QNm nm = qnm.get(ts - t - 1);
        final NameTest nt = nm == null ? new NameTest(false) :
          new NameTest(nm, Kind.NAME, false, null);
        stps[t] = Step.get(info, CHILD, nt, preds);
      }
      while(++s < sl) stps[ts++] = steps[s];
      path = get(info, root, stps);
      break;
    }

    // check if the all children in the path exist; don't test with namespaces
    if(data.nspaces.size() == 0) {
      LOOP:
      for(int s = 0; s < path.steps.length; s++) {
        // only verify child steps; ignore namespaces
        final Step st = path.axisStep(s);
        if(st == null || st.axis != CHILD) break;
        if(st.test.kind == Kind.WILDCARD || st.test.kind == null) continue;
        if(st.test.kind != Kind.NAME) break;

        // check if one of the addressed nodes is on the correct level
        final int name = data.tagindex.id(st.test.name.local());
        for(final PathNode pn : data.paths.desc(name, Data.ELEM)) {
          if(pn.level() == s + 1) continue LOOP;
        }
        qc.compInfo(OPTPATH, path);
        return Empty.SEQ;
      }
    }

    return path;
  }

  /**
   * Returns an equivalent expression which accesses an index.
   * If the expression cannot be rewritten, the original expression is returned.
   *
   * The following types of queries can be rewritten (in the examples, the equality comparison
   * is used, which will be rewritten to {@link ValueAccess} instances):
   *
   * <pre>
   * 1. A[text() = '...']    -> IA('...')
   * 2. A[. = '...']         -> IA('...', A)
   * 3. text()[. = '...']    -> IA('...')
   * 4. A[B = '...']         -> IA('...', B)/parent::A
   * 1. A[B/text() = '...']  -> IA('...')/parent::B/parent::A
   * 2. A[B/C = '...']       -> IA('...', C)/parent::B/parent::A
   * 7. A[@a = '...']        -> IA('...', @a)/parent::A
   * 8. @a[. = '...']        -> IA('...', @a)</pre>
   *
   * Queries of type 1, 3, 5 will not yield any results if the string to be compared is empty.
   *
   * @param qc query context
   * @param rt root value
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr index(final QueryContext qc, final Value rt) throws QueryException {
    // only rewrite paths with data reference
    final Data data = rt.data();
    if(data == null) return this;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int iPred = 0, iStep = 0;

    // check if path can be converted to an index access
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      final Step step = axisStep(s);
      if(step == null || !step.axis.down || step.has(Flag.FCS)) break;

      // check if path is iterable (i.e., will be duplicate-free)
      final boolean iter = pathNodes(data, s) != null;
      final IndexContext ictx = new IndexContext(data, iter);

      // choose cheapest index access
      final int pl = step.preds.length;
      for(int p = 0; p < pl; p++) {
        final IndexInfo ii = new IndexInfo(ictx, qc, step);
        if(!step.preds[p].indexAccessible(ii)) continue;

        if(ii.costs == 0) {
          // no results...
          qc.compInfo(OPTNOINDEX, this);
          return Empty.SEQ;
        }
        if(index == null || index.costs > ii.costs) {
          index = ii;
          iPred = p;
          iStep = s;
        }
      }
    }

    // skip rewriting if no index access is possible, or if it is too expensive
    if(index == null || index.costs > data.meta.size) return this;

    // rewrite for index access
    qc.compInfo(index.info);

    // replace expressions for index access
    final Step indexStep = index.step;

    // collect remaining predicates
    final int pl = indexStep.preds.length;
    final ExprList newPreds = new ExprList(pl - 1);
    for(int p = 0; p < pl; p++) {
      if(p != iPred) newPreds.add(indexStep.preds[p]);
    }

    // check if steps before index step need to be inverted and traversed
    final Test test = InvDocTest.get(rt);
    boolean inv = true;
    if(test == Test.DOC && data.meta.uptodate) {
      int s = 0;
      for(; s <= iStep; s++) {
        // step must use child axis and name test, and have no predicates
        final Step step = axisStep(s);
        if(step.test.kind != Kind.NAME || step.axis != Axis.CHILD ||
            s != iStep && step.preds.length > 0) break;

        // support only unique paths with nodes on the correct level
        final int name = data.tagindex.id(step.test.name.local());
        final ArrayList<PathNode> pn = data.paths.desc(name, Data.ELEM);
        if(pn.size() != 1 || pn.get(0).level() != s + 1) break;
      }
      inv = s <= iStep;
    }

    // invert steps that occur before index step and add them as predicate
    final ExprList invSteps = new ExprList();
    if(inv) {
      for(int s = iStep; s >= 0; s--) {
        final Axis ax = axisStep(s).axis.invert();
        if(s == 0) {
          // add document test for collections and axes other than ancestors
          if(test != Test.DOC || ax != Axis.ANC && ax != Axis.ANCORSELF)
            invSteps.add(Step.get(info, ax, test));
        } else {
          final Step prev = axisStep(s - 1);
          invSteps.add(Step.get(info, ax, prev.test, prev.preds));
        }
      }
    }
    if(!invSteps.isEmpty()) newPreds.add(get(info, null, invSteps.finish()));

    // create resulting expression
    final ExprList resultSteps = new ExprList();
    final Expr resultRoot;
    if(index.expr instanceof Path) {
      final Path p = (Path) index.expr;
      resultRoot = p.root;
      resultSteps.add(p.steps);
    } else {
      resultRoot = index.expr;
    }

    if(!newPreds.isEmpty()) {
      int ls = resultSteps.size() - 1;
      Step step;
      if(ls < 0 || !(resultSteps.get(ls) instanceof Step)) {
        // add at least one self axis step
        step = Step.get(info, Axis.SELF, Test.NOD);
        ls++;
      } else {
        step = (Step) resultSteps.get(ls);
      }
      // add remaining predicates to last step
      resultSteps.set(ls, step.addPreds(newPreds.finish()));
    }

    // add remaining steps
    for(int s = iStep + 1; s < sl; s++) resultSteps.add(steps[s]);
    return get(info, resultRoot, resultSteps.finish());
  }

  /**
   * Merge steps.
   * @param qc query context
   * @return original or new expression
   */
  private Expr mergeSteps(final QueryContext qc) {
    // merge descendant steps
    boolean opt = false;
    final int sl = steps.length;
    final ExprList stps = new ExprList(sl);
    for(int s = 0; s < sl; s++) {
      final Expr step = steps[s];
      if(s < sl - 1 && step instanceof Step && steps[s + 1] instanceof Step) {
        final Step curr = (Step) step, next = (Step) steps[s + 1];
        if(curr.simple(DESCORSELF, false)) {
          // descendant-or-self::node()/child::X -> descendant::X
          if(next.axis == CHILD && !next.has(Flag.FCS)) {
            next.axis = DESC;
            opt = true;
            continue;
          }
          // descendant-or-self::node()/@X -> descendant-or-self::*/@X
          if(next.axis == ATTR && !next.has(Flag.FCS)) {
            curr.test = new NameTest(false);
            opt = true;
          }
        }
      }
      stps.add(step);
    }

    if(opt) {
      qc.compInfo(OPTDESC);
      return get(info, root, stps.finish());
    }
    return this;
  }

  @Override
  public final boolean removable(final Var var) {
    for(final Expr step : steps) if(step.uses(var)) return false;
    return root == null || root.removable(var);
  }

  @Override
  public VarUsage count(final Var v) {
    final VarUsage inRoot = root == null ? VarUsage.NEVER : root.count(v);
    return VarUsage.sum(v, steps) == VarUsage.NEVER ? inRoot : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final QueryContext qc, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    boolean changed = false;
    if(root != null) {
      final Expr rt = root.inline(qc, scp, v, e);
      if(rt != null) {
        root = rt;
        changed = true;
      }
    }

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      final Expr nw = steps[s].inline(qc, scp, v, e);
      if(nw != null) {
        steps[s] = nw;
        changed = true;
      }
    }
    return changed ? optimize(qc, scp) : null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(root == null) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(!root.accept(visitor)) {
      return false;
    }
    visitor.enterFocus();
    if(!visitAll(visitor, steps)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr e : steps) sz += e.exprSize();
    return root == null ? sz : sz + root.exprSize();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), root, steps);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : steps) {
      if(sb.length() != 0) sb.append(s instanceof Bang ? " ! " : "/");
      if(s instanceof Step) sb.append(s);
      else sb.append(s);
    }
    return sb.toString();
  }
}
