package org.basex.query.expr.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.expr.path.Axis.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.path.Test.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** XPath axes that are expected to be expensive when at the start of a path. */
  private static final EnumSet<Axis> EXPENSIVE =
      EnumSet.of(DESC, DESCORSELF, PREC, PRECSIBL, FOLL, FOLLSIBL);

  /** Root expression (can be {@code null}). */
  public Expr root;
  /** Path steps. */
  public final Expr[] steps;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression (can be {@code null})
   * @param steps steps
   */
  protected Path(final InputInfo info, final Expr root, final Expr[] steps) {
    super(info);
    this.root = root;
    this.steps = steps;
  }

  /**
   * Returns a new path instance.
   * A path implementation is chosen that works fastest for the given steps.
   * @param info input info
   * @param root root expression, may be {@code null}
   * @param steps steps
   * @return path instance
   */
  public static Path get(final InputInfo info, final Expr root, final Expr... steps) {
    // new list with steps
    final int sl = steps.length;
    final ExprList stps = new ExprList(sl);

    // merge nested paths
    Expr rt = root;
    if(rt instanceof Path) {
      final Path path = (Path) rt;
      stps.add(path.steps);
      rt = path.root;
    }
    // remove redundant context reference
    if(rt instanceof ContextValue) rt = null;

    // add steps of input array
    for(final Expr expr : steps) {
      Expr step = expr;
      if(step instanceof ContextValue) {
        // remove redundant context references
        if(sl > 1) continue;
        // single step: rewrite to axis step (required to sort results of path)
        step = Step.get(((ContextValue) step).info, SELF, KindTest.NOD);
      } else if(step instanceof Filter) {
        // rewrite filter to axis step
        final Filter f = (Filter) step;
        if(f.root instanceof ContextValue) {
          step = Step.get(f.info, SELF, KindTest.NOD, f.exprs);
        }
      } else if(step instanceof Path) {
        // rewrite path to axis steps
        final Path p = (Path) step;
        if(p.root != null && !(p.root instanceof ContextValue)) stps.add(p.root);
        final int pl = p.steps.length - 1;
        for(int i = 0; i < pl; i++) stps.add(p.steps[i]);
        step = p.steps[pl];
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

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    final int ss = steps.length;
    for(int s = 0; s < ss - 1; s++) checkNoUp(steps[s]);
    steps[ss - 1].checkUp();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    if(root != null) root = root.compile(cc);
    // no steps
    if(steps.length == 0) return root == null ? new ContextValue(info) : root;

    final QueryFocus focus = cc.qc.focus;
    final Value init = focus.value, cv = initial(cc);
    focus.value = cv;
    final boolean doc = cv != null && cv.type == NodeType.DOC;
    try {
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr step = steps[s];
        // axis step: if input is a document, its type is temporarily generalized
        final boolean as = step instanceof Step;
        if(as && s == 0 && doc) cv.type = NodeType.NOD;
        try {
          steps[s] = step.compile(cc);
        } catch(final QueryException ex) {
          // replace original expression with error
          steps[s] = cc.error(ex, this);
        }
        // no axis step: invalidate context value
        if(!as) focus.value = null;
      }
    } finally {
      if(doc) cv.type = NodeType.DOC;
      focus.value = init;
    }
    // optimize path
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // simplify path with empty root expression or empty step
    final Value rt = initial(cc);
    if(rt != null && rt.isEmpty() || emptyPath(rt)) return cc.emptySeq(this);

    // merge descendant steps
    Expr e = mergeSteps(cc);
    if(e != this) return e.optimize(cc);

    // check index access
    e = index(cc, rt);
    // recompile path
    if(e != this) return e.optimize(cc);

    /* rewrite descendant to child steps. this optimization is located after the index rewriting,
     * as it is cheaper to invert a descendant step. examples:
     * - //C[. = '...']     ->  IA('...', C)
     * - /A/B/C[. = '...']  ->  IA('...', C)/parent::B/parent::A
     */
    e = children(cc, rt);
    if(e != this) return e.optimize(cc);

    // choose best path implementation and set type information
    final Path path = get(info, root, steps);
    final int sl = path.steps.length;
    final Expr lastExpr = path.steps[sl - 1];

    // try to compute result size and derive result type from last step
    path.size = path.size(cc);
    path.seqType = lastExpr.seqType().withSize(path.size);

    // single attribute with exact name test will return at most one result
    if(path.root == null && sl == 1 && lastExpr instanceof Step) {
      final Step lastStep = (Step) lastExpr;
      if(lastStep.axis == ATTR && lastStep.test.one) {
        lastStep.seqType = lastStep.seqType.withOcc(Occ.ZERO_ONE);
        path.seqType = lastStep.seqType;
      }
    }
    return path;
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) throws QueryException {
    final int sl = steps.length;
    if(steps[sl - 1] instanceof Step) {
      final Step step = (Step) steps[sl - 1];
      if(step.exprs.length == 1 && step.seqType().type instanceof NodeType &&
          !step.exprs[0].seqType().mayBeNumber()) {
        // merge nested predicates. example: if(a[b]) ->  if(a/b)
        final Expr s = step.optimizeEbv(this, cc);
        if(s != step) {
          step.exprs = new Expr[0];
          return cc.replaceEbv(this, s);
        }
      }
    }
    return super.optimizeEbv(cc);
  }

  @Override
  public Data data() {
    if(root != null) {
      // data reference
      final Data data = root.data();
      if(data != null) {
        final int sl = steps.length;
        for(int s = 0; s < sl; s++) {
          if(axisStep(s) == null) return null;
        }
        return data;
      }
    }
    return null;
  }

  @Override
  public final boolean has(final Flag flag) {
    // context dependency. no root exists, or if it depends on context. Example: text(); ./abc
    if(flag == Flag.CTX) return root == null || root.has(Flag.CTX);
    // positional test. may only occur in root node, not in first step. Example: position()/a
    if(flag != Flag.POS) {
      for(final Expr step : steps) if(step.has(flag)) return true;
    }
    return root != null && root.has(flag);
  }

  /**
   * Tries to cast the specified step into an axis step.
   * @param index index
   * @return axis step, or {@code null})
   */
  private Step axisStep(final int index) {
    return steps[index] instanceof Step ? (Step) steps[index] : null;
  }

  /**
   * Returns the path nodes that will result from this path.
   * @param cc compilation context
   * @return path nodes or {@code null} if nodes cannot be evaluated
   */
  public final ArrayList<PathNode> pathNodes(final CompileContext cc) {
    final Value init = initial(cc);
    final Data data = init != null && init.type == NodeType.DOC ? init.data() : null;
    if(data == null || !data.meta.uptodate) return null;

    ArrayList<PathNode> nodes = data.paths.root();
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      final Step curr = axisStep(s);
      if(curr == null) return null;
      nodes = curr.nodes(nodes, data);
      if(nodes == null) return null;
    }
    return nodes;
  }

  /**
   * Guesses if the evaluation of this axis path is cheap. This is used to determine if it
   * can be inlined into a loop to enable index rewritings.
   * @return guess
   */
  public final boolean cheap() {
    if(!(root instanceof ANode) || ((Value) root).type != NodeType.DOC) return false;
    final int sl = steps.length;
    for(int i = 0; i < sl; i++) {
      final Step s = axisStep(i);
      if(s == null || i < 2 && EXPENSIVE.contains(s.axis)) return false;
      final Expr[] ps = s.exprs;
      if(!(ps.length == 0 || ps.length == 1 && ps[0] instanceof ItrPos)) return false;
    }
    return true;
  }

  /**
   * Checks if the path can be rewritten for iterative evaluation.
   * @param root root expression; can be a {@code null} reference
   * @param steps path steps
   * @return result of check
   */
  private static boolean iterative(final Expr root, final Expr... steps) {
    if(root == null || !root.iterable()) return false;

    final long size = root.size();
    final SeqType tp = root.seqType();
    boolean atMostOne = size == 0 || size == 1 || tp.zeroOrOne();
    boolean sameDepth = atMostOne || tp.type == NodeType.DOC || tp.type == NodeType.DEL;

    for(final Expr expr : steps) {
      final Step step = (Step) expr;
      switch(step.axis) {
        case ANC:
        case ANCORSELF:
        case PREC:
        case PRECSIBL:
          // backwards axes must be reordered
          return false;
        case FOLL:
          // can overlap
          if(!atMostOne) return false;
          atMostOne = false;
          sameDepth = false;
          break;
        case FOLLSIBL:
          // can overlap, preserves level
          if(!atMostOne) return false;
          atMostOne = false;
          break;
        case ATTR:
          // only unique for exact QName matching
          atMostOne &= step.test.kind == Kind.URI_NAME;
          break;
        case CHILD:
          // order is only ensured if all nodes are on the same level
          if(!sameDepth) return false;
          atMostOne = false;
          break;
        case DESC:
        case DESCORSELF:
          // non-overlapping if all nodes are on the same level
          if(!sameDepth) return false;
          atMostOne = false;
          sameDepth = false;
          break;
        case PARENT:
          // overlaps
          if(!atMostOne) return false;
          break;
        case SELF:
          // nothing changes
          break;
        default:
          throw Util.notExpected();
      }
    }
    return true;
  }

  /**
   * Returns the initial context value of a path or {@code null}.
   * @param cc compilation context (may be @code null)
   * @return root
   */
  private Value initial(final CompileContext cc) {
    return initial(cc, root);
  }

  /**
   * Returns a context value for the given root, or {@code null}.
   * @param cc compilation context (may be @code null)
   * @param root root expression
   * @return root
   */
  public static Value initial(final CompileContext cc, final Expr root) {
    // current context value
    final Value v = cc != null ? cc.qc.focus.value : null;
    // no root or context expression: return context
    if(root == null || root instanceof ContextValue) return v;
    // root reference
    if(root instanceof Root) return v instanceof ANode ? ((ANode) v).root() : v;
    // root is value: return root
    if(root.isValue()) return (Value) root;
    // data reference
    final Data d = root.data();
    if(d != null) return new DBNode(d, 0, Data.ELEM);
    // otherwise, return null
    return null;
  }

  /**
   * Computes the number of results.
   * @param cc compilation context (may be @code null)
   * @return number of results
   */
  private long size(final CompileContext cc) {
    final Value rt = initial(cc);
    // skip computation if value is not a document node
    if(rt == null || rt.type != NodeType.DOC) return -1;
    final Data data = rt.data();
    // skip computation if no database instance is available, is out-of-date or
    // if context does not contain all database nodes
    if(data == null || !data.meta.uptodate || data.meta.ndocs != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths.root();
    long m = 1;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      final Step curr = axisStep(s);
      if(curr != null) {
        nodes = curr.nodes(nodes, data);
        if(nodes == null) return -1;
      } else if(s + 1 == sl) {
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
   * @param data data reference (can be {@code null})
   * @param last last step to be checked
   * @return path nodes
   */
  private ArrayList<PathNode> pathNodes(final Data data, final int last) {
    // skip request if no path index exists or might be out-of-date
    if(data == null || !data.meta.uptodate) return null;

    ArrayList<PathNode> nodes = data.paths.root();
    for(int s = 0; s <= last; s++) {
      // only follow axis steps
      final Step curr = axisStep(s);
      if(curr == null) return null;

      final boolean desc = curr.axis == DESC;
      if(!desc && curr.axis != CHILD || curr.test.kind != Kind.NAME) return null;

      final int name = data.elemNames.id(curr.test.name.local());

      final ArrayList<PathNode> tmp = new ArrayList<>();
      for(final PathNode node : PathIndex.desc(nodes, desc)) {
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
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) if(emptyStep(rt, s)) return true;
    return false;
  }

  /**
   * Checks if the specified step will never yield results.
   * @param rt root value
   * @param s index of step
   * @return {@code true} if steps will never yield results
   */
  private boolean emptyStep(final Value rt, final int s) {
    if(steps[s].isEmpty()) return true;

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
          if(step.test != KindTest.NOD && step.test != KindTest.DOC) return true;
        } else if(axis == CHILD || axis == DESC) {
          if(step.test == KindTest.DOC || step.test == KindTest.ATT) return true;
        } else if(axis == DESCORSELF) {
          if(step.test == KindTest.ATT) return true;
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
        if(step.test == KindTest.NOD) return false;
        // @.../..., text()/...
        if(last.axis == ATTR && step.test.type != NodeType.ATT ||
           last.test == KindTest.TXT && step.test != KindTest.TXT) return true;
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
        return last.axis == ATTR || last.test == KindTest.TXT || last.test == KindTest.COM ||
           last.test == KindTest.PI || axis == ATTR && step.test == KindTest.NSP;
      // .../parent:: / .../ancestor::
      if(axis == PARENT || axis == ANC) return last.test == KindTest.DOC;
    }
    return false;
  }

  /**
   * Converts descendant to child steps.
   * @param cc compilation context
   * @param rt root value
   * @return original or new expression
   */
  private Expr children(final CompileContext cc, final Value rt) {
    // only rewrite on document level
    if(rt == null || rt.type != NodeType.DOC) return this;

    // skip if index does not exist or is out-dated, or if several namespaces occur in the input
    final Data data = rt.data();
    if(data == null || !data.meta.uptodate || data.nspaces.globalUri() == null) return this;

    Path path = this;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.exprs.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESC || curr.has(Flag.POS)) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, s);
      if(nodes == null) continue;

      // cache child steps
      final ArrayList<QNm> qnm = new ArrayList<>();
      while(nodes.get(0).parent != null) {
        QNm nm = new QNm(data.elemNames.key(nodes.get(0).name));
        // skip children with prefixes
        if(nm.hasPrefix()) return this;
        for(final PathNode p : nodes) {
          if(nodes.get(0).name != p.name) nm = null;
        }
        qnm.add(nm);
        nodes = PathIndex.parent(nodes);
      }
      cc.info(OPTCHILD_X, steps[s]);

      // build new steps
      int ts = qnm.size();
      final Expr[] stps = new Expr[ts + sl - s - 1];
      for(int t = 0; t < ts; t++) {
        final Expr[] preds = t == ts - 1 ? ((Preds) steps[s]).exprs : new Expr[0];
        final QNm nm = qnm.get(ts - t - 1);
        final NameTest nt = nm == null ? new NameTest(false) :
          new NameTest(nm, Kind.NAME, false, null);
        stps[t] = Step.get(info, CHILD, nt, preds);
      }
      while(++s < sl) stps[ts++] = steps[s];
      path = get(info, root, stps);
      break;
    }

    // check if all steps yield results; if not, return empty sequence
    final ArrayList<PathNode> nodes = pathNodes(cc);
    if(nodes != null && nodes.isEmpty()) {
      cc.info(OPTPATH_X, path);
      return Empty.SEQ;
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
   * 1. A[text() = '...']    -> IA('...', A)
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
   * @param cc compilation context
   * @param rt root value (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  public Expr index(final CompileContext cc, final Value rt) throws QueryException {
    final boolean enforce = cc.qc.context.options.get(MainOptions.ENFORCEINDEX);

    // only rewrite if data reference exists and if root points to documents
    final Data data = rt == null ? null : rt.data();
    if(!enforce && (data == null || rt.type != NodeType.DOC)) return this;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int indexPred = 0, indexStep = 0;

    // check if path can be converted to an index access
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      // Example for position predicate: child:x[1] != parent::x[1]
      final Step step = axisStep(s);
      if(step == null || !step.axis.down || step.has(Flag.POS)) break;

      // check if path is iterable (i.e., will be duplicate-free)
      final boolean iter = pathNodes(data, s) != null;
      final IndexContext ictx = data != null ? new IndexContext(data, iter) :
        new IndexContext(root == null ? new ContextValue(info) : root, iter);

      // choose cheapest index access
      final int pl = step.exprs.length;
      for(int p = 0; p < pl; p++) {
        final IndexInfo ii = new IndexInfo(ictx, cc.qc, step);
        if(!step.exprs[p].indexAccessible(ii)) continue;

        if(ii.costs == 0) {
          // no results...
          cc.info(OPTNORESULTS_X, ii.step);
          return Empty.SEQ;
        }

        if(index == null || index.costs > ii.costs) {
          index = ii;
          indexPred = p;
          indexStep = s;
        }
      }
    }

    // skip rewriting if no index access is possible, or if it is too expensive
    if(index == null || data != null && index.costs > data.meta.size) return this;

    // rewrite for index access
    cc.info(index.optInfo);

    // invert steps that occur before index step and add them as predicate
    final ExprList newPreds = new ExprList();
    final Test rootTest = InvDocTest.get(rt);
    final ExprList invSteps = new ExprList();
    if(rootTest != KindTest.DOC || data == null || !data.meta.uptodate ||
        predSteps(data, indexStep)) {
      for(int s = indexStep; s >= 0; s--) {
        final Axis invAxis = axisStep(s).axis.invert();
        if(s == 0) {
          // add document test for collections and axes other than ancestors
          if(rootTest != KindTest.DOC || invAxis != ANC && invAxis != ANCORSELF)
            invSteps.add(Step.get(info, invAxis, rootTest));
        } else {
          final Step prevStep = axisStep(s - 1);
          final Axis newAxis = prevStep.axis == ATTR ? ATTR : invAxis;
          invSteps.add(Step.get(info, newAxis, prevStep.test, prevStep.exprs));
        }
      }
    }
    if(!invSteps.isEmpty()) newPreds.add(get(info, null, invSteps.finish()));

    // add remaining predicates
    final Expr[] preds = index.step.exprs;
    final int pl = preds.length;
    for(int p = 0; p < pl; p++) {
      if(p != indexPred) newPreds.add(preds[p]);
    }

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

    // only one hit:
    if(index.costs == 1) {
      // set sequence type
      if(resultRoot instanceof IndexAccess) ((IndexAccess) resultRoot).size(1);
      else ((ParseExpr) resultRoot).seqType = resultRoot.seqType().withOcc(Occ.ZERO_ONE);
    }

    if(!newPreds.isEmpty()) {
      int ls = resultSteps.size() - 1;
      final Step step;
      if(ls < 0 || !(resultSteps.get(ls) instanceof Step)) {
        // add at least one self axis step
        step = Step.get(info, SELF, KindTest.NOD);
        ls++;
      } else {
        step = (Step) resultSteps.get(ls);
      }
      // add remaining predicates to last step
      resultSteps.set(ls, step.addPreds(newPreds.finish()));
    }

    // add remaining steps
    for(int s = indexStep + 1; s < sl; s++) resultSteps.add(steps[s]);
    return resultSteps.isEmpty() ? resultRoot : get(info, resultRoot, resultSteps.finish());
  }

  /**
   * Checks if steps before index step need to be inverted and traversed.
   * @param data data reference
   * @param iStep index step
   * @return result of check
   */
  private boolean predSteps(final Data data, final int iStep) {
    for(int s = iStep; s >= 0; s--) {
      final Step step = axisStep(s);
      // ensure that the index step does not use wildcard
      if(step.test.kind == Kind.WILDCARD && s != iStep) continue;
      // consider child steps with name test and without predicates
      if(step.test.kind != Kind.NAME || step.axis != CHILD ||
          s != iStep && step.exprs.length > 0) return true;

      // support only unique paths with nodes on the correct level
      final ArrayList<PathNode> pn = data.paths.desc(step.test.name.local());
      if(pn.size() != 1 || pn.get(0).level() != s + 1) return true;
    }
    return false;
  }

  /**
   * Merges expensive descendant-or-self::node() steps.
   * @param cc compilation context
   * @return original or new expression
   */
  private Expr mergeSteps(final CompileContext cc) {
    boolean opt = false;
    final int sl = steps.length;
    final ExprList stps = new ExprList(sl);
    for(int s = 0; s < sl; s++) {
      final Expr step = steps[s];
      // check for simple descendants-or-self step with succeeding step
      if(s < sl - 1 && step instanceof Step) {
        final Step curr = (Step) step;
        if(curr.simple(DESCORSELF, false)) {
          // check succeeding step
          final Expr next = steps[s + 1];
          // descendant-or-self::node()/child::X -> descendant::X
          if(simpleChild(next)) {
            ((Step) next).axis = DESC;
            opt = true;
            continue;
          }
          // descendant-or-self::node()/(X, Y) -> (descendant::X | descendant::Y)
          Expr e = mergeList(next);
          if(e != null) {
            steps[s + 1] = e;
            opt = true;
            continue;
          }
          // //(X, Y)[text()] -> (/descendant::X | /descendant::Y)[text()]
          if(next instanceof Filter && !next.has(Flag.POS)) {
            final Filter f = (Filter) next;
            e = mergeList(f.root);
            if(e != null) {
              f.root = e;
              opt = true;
              continue;
            }
          }

        }
      }
      stps.add(step);
    }

    if(opt) {
      cc.info(OPTDESC);
      return stps.isEmpty() ? root : get(info, root, stps.finish());
    }
    return this;
  }

  /**
   * Trie to rewrite union or list expressions.
   * @param expr input expression
   * @return rewriting flag
   */
  private static Expr mergeList(final Expr expr) {
    if(expr instanceof Union || expr instanceof List) {
      final Arr next = (Arr) expr;
      if(childSteps(next)) {
        for(final Expr e : next.exprs) ((Step) ((Path) e).steps[0]).axis = DESC;
        return new Union(next.info, next.exprs);
      }
    }
    return null;
  }

  /**
   * Checks if the expressions in the specified array start with child steps.
   * @param list array to be checked
   * @return result of check
   */
  private static boolean childSteps(final Arr list) {
    for(final Expr e : list.exprs) {
      if(!(e instanceof Path)) return false;
      final Path p = (Path) e;
      if(p.root != null || !simpleChild(p.steps[0])) return false;
    }
    return true;
  }

  /**
   * Checks if the expressions is a simple child step.
   * @param expr expression to be checked
   * @return result of check
   */
  private static boolean simpleChild(final Expr expr) {
    if(expr instanceof Step) {
      final Step step = (Step) expr;
      if(step.axis == CHILD && !step.has(Flag.POS)) return true;
    }
    return false;
  }

  @Override
  public final boolean removable(final Var var) {
    for(final Expr step : steps) if(step.uses(var)) return false;
    return root == null || root.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    final VarUsage inRoot = root == null ? VarUsage.NEVER : root.count(var);
    return VarUsage.sum(var, steps) == VarUsage.NEVER ? inRoot : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {

    // #1202: during inlining, expressions will be optimized, which are based on the context value
    boolean changed;
    final QueryFocus focus = cc.qc.focus;
    final Value init = focus.value;
    focus.value = null;
    try {
      changed = inlineAll(steps, var, ex, cc);
    } finally {
      focus.value = init;
    }

    if(root != null) {
      final Expr rt = root.inline(var, ex, cc);
      if(rt != null) {
        root = rt;
        changed = true;
      }
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(root == null) {
      visitor.lock(Locking.CONTEXT);
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
  public final boolean equals(final Object obj) {
    if(!(obj instanceof Path)) return false;
    final Path p = (Path) obj;
    return Objects.equals(root, p.root) && Array.equals(steps, p.steps);
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
      if(sb.length() != 0) sb.append('/');
      sb.append(s);
    }
    return sb.toString();
  }
}
