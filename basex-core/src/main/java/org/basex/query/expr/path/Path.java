package org.basex.query.expr.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.expr.path.Axis.*;

import java.util.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.Test.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
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
 * @author BaseX Team 2005-18, BSD License
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
    super(info, SeqType.ITEM_ZM);
    this.root = root;
    this.steps = steps;
  }

  /**
   * Returns a new path instance.
   * A path implementation is chosen that works fastest for the given steps.
   * @param info input info
   * @param root root expression (can be {@code null})
   * @param steps steps
   * @return path instance
   */
  public static ParseExpr get(final InputInfo info, final Expr root, final Expr... steps) {
    // new list with steps
    int sl = steps.length;
    final ExprList list = new ExprList(sl);

    // merge nested paths
    Expr rt = root;
    if(rt instanceof Path) {
      final Path path = (Path) rt;
      list.add(path.steps);
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
        if(p.root != null && !(p.root instanceof ContextValue)) list.add(p.root);
        final int pl = p.steps.length - 1;
        for(int i = 0; i < pl; i++) list.add(p.steps[i]);
        step = p.steps[pl];
      }
      list.add(step);
    }

    // check if all steps are axis steps
    int axes = 0;
    final Expr[] st = list.toArray();
    for(final Expr step : st) {
      if(step instanceof Step) axes++;
    }
    sl = st.length;
    if(axes == sl) return iterative(rt, st) ? new IterPath(info, rt, st) :
      new CachedPath(info, rt, st);

    // if last expression yields no nodes, rewrite mixed path to simple map
    // example: $a/b/string -> $a/b ! string()
    final Expr last = st[sl - 1];
    if(sl > 1 && last.seqType().type.instanceOf(AtomType.AAT)) {
      list.remove(sl - 1);
      return (SimpleMap) SimpleMap.get(info, Path.get(info, rt, list.finish()), last);
    }
    return new MixedPath(info, rt, st);
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
    if(root != null) {
      root = root.compile(cc);
      if(root.seqType().zero() || steps.length == 0) return root;
      cc.pushFocus(root);
    } else {
      if(steps.length == 0) return new ContextValue(info).optimize(cc);
      cc.pushFocus(cc.qc.focus.value);
    }

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      Expr step = steps[s];
      try {
        step = step.compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        step = cc.error(ex, this);
      }
      cc.updateFocus(step);
      steps[s] = step;
    }
    cc.removeFocus();

    // optimize path
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // root returns no results...
    if(root != null && root.seqType().zero()) return cc.replaceWith(this, root);

    // simplify path with empty root expression or empty step
    final Value rt = rootValue(cc);
    if(emptyPath(rt)) return cc.emptySeq(this);

    seqType(cc, rt);

    // merge descendant steps
    Expr expr = mergeSteps(cc);
    // check index access
    if(expr == this) expr = index(cc, rt);
    /* rewrite descendant to child steps. this optimization is called after the index rewritings,
     * as it is cheaper to invert a descendant step. examples:
     * - //B [. = '...']  ->  IA('...', B)
     * - /A/B[. = '...']  ->  IA('...', B)/parent::A *[parent::document-node()] */
    if(expr == this) expr = children(cc, rt);
    if(expr != this) return expr.optimize(cc);

    // choose best path implementation and set type information
    return copyType(get(info, root, steps));
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) throws QueryException {
    final Expr last = steps[steps.length - 1];
    if(last instanceof Step) {
      final Step step = (Step) last;
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
  public final boolean has(final Flag... flags) {
    /* Context dependency: check if no root exists, or if it depends on context.
     * Examples: text(); ./abc */
    if(Flag.CTX.in(flags) && (root == null || root.has(Flag.CTX))) return true;
    /* Positional access: only check root node (steps will refer to result of root node).
     * Example: position()/a */
    if(Flag.POS.in(flags) && (root != null && root.has(Flag.POS))) return true;
    // check remaining flags
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    if(flgs.length != 0) {
      for(final Expr step : steps) if(step.has(flgs)) return true;
      return root != null && root.has(flgs);
    }
    return false;
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
    // skip computation if path does not start with document nodes
    final Value rt = rootValue(cc);
    if(rt == null || rt.type != NodeType.DOC || cc.nestedFocus() && cc.qc.focus.value == null)
      return null;

    final Data data = rt.data();
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
   * Returns a root value for this path.
   * @param cc compilation context
   * @return context value, dummy item or {@code null}
   */
  private Value rootValue(final CompileContext cc) {
    // no root expression: return context value (possibly empty)
    if(root == null) return cc.qc.focus.value;
    // root is value: return root
    if(root instanceof Value) return (Value) root;
    // otherwise, create dummy item
    return cc.dummyItem(root);
  }

  /**
   * Estimates the cost to evaluate this path. This is used to determine if the path
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
    final SeqType st = root.seqType();
    boolean atMostOne = size == 0 || size == 1 || st.zeroOrOne();
    boolean sameDepth = atMostOne || st.type == NodeType.DOC || st.type == NodeType.DEL;

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
   * Assigns a sequence type and (if statically known) result size.
   * @param rt root value (can be {@code null})
   * @param cc compilation context
   */
  private void seqType(final CompileContext cc, final Value rt) {
    // assign data reference
    final int sl = steps.length;
    final long size = size(cc, rt);
    final Expr last = steps[sl - 1];
    final SeqType st = last.seqType();
    final Type type = st.type;
    Occ occ = Occ.ZERO_MORE;

    // unknown result size: single attribute with exact name test will return at most one result
    if(size < 0 && root == null && sl == 1 && last instanceof Step && cc.nestedFocus()) {
      final Step step = (Step) last;
      if(step.axis == ATTR && step.test.one) {
        occ = Occ.ZERO_ONE;
        step.exprType.assign(occ);
      }
    }
    exprType.assign(type, occ, size);
  }

  /**
   * Computes the number of results.
   * @param rt root value (can be {@code null})
   * @param cc compilation context
   * @return number of results
   */
  private long size(final CompileContext cc, final Value rt) {
    if(root != null && root.size() == 0) return 0;
    for(final Expr step : steps) {
      if(step.size() == 0) return 0;
    }

    // skip computation if path does not start with document nodes
    if(rt == null || rt.type != NodeType.DOC || cc.nestedFocus() && cc.qc.focus.value == null)
      return -1;

    // skip computation if no database instance is available, is outdated, or
    // if context does not contain all database nodes
    final Data data = rt.data();
    if(data == null || !data.meta.uptodate || data.meta.ndocs != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths.root();
    long lastSize = 1;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      final Step curr = axisStep(s);
      if(curr != null) {
        nodes = curr.nodes(nodes, data);
        if(nodes == null) return -1;
      } else if(s + 1 == sl) {
        lastSize = steps[s].size();
      } else {
        // stop if a non-axis step is not placed last
        return -1;
      }
    }

    long size = 0;
    for(final PathNode pn : nodes) size += pn.stats.count;
    return size * lastSize;
  }

  /**
   * Returns all summary path nodes for the specified location step.
   * @param data data reference (can be {@code null})
   * @param last last step to be checked
   * @return path nodes, or {@code null} if nodes cannot be retrieved
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
   * @param rt root value (can be {@code null})
   * @return {@code true} if steps will never yield results
   */
  private boolean emptyPath(final Value rt) {
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      if(emptyStep(rt, s)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified step will never yield results.
   * @param rt root value (can be {@code null})
   * @param s index of step
   * @return {@code true} if steps will never yield results
   */
  private boolean emptyStep(final Value rt, final int s) {
    if(steps[s] == Empty.SEQ) return true;

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
        switch(axis) {
          case SELF:
          case ANCORSELF:
            if(step.test != KindTest.NOD && step.test != KindTest.DOC) return true;
            break;
          case CHILD:
          case DESC:
            if(step.test == KindTest.DOC || step.test == KindTest.ATT) return true;
            break;
          case DESCORSELF:
            if(step.test == KindTest.ATT) return true;
            break;
          default:
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
   * @param rt root value (can be {@code null})
   * @return original or new expression
   */
  private Expr children(final CompileContext cc, final Value rt) {
    // only rewrite on document level
    if(rt == null || rt.type != NodeType.DOC || cc.nestedFocus() && cc.qc.focus.value == null)
      return this;

    // skip if index does not exist or is out-dated, or if several namespaces occur in the input
    final Data data = rt.data();
    if(data == null || !data.meta.uptodate || data.nspaces.globalUri() == null) return this;

    Expr path = this;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.exprs.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESC || curr.positional()) continue;

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
   * 1. A[text() = '...']    : IA('...', A)
   * 2. A[. = '...']         : IA('...', A)
   * 3. text()[. = '...']    : IA('...')
   * 4. A[B = '...']         : IA('...', B)/parent::A
   * 5. A[B/text() = '...']  : IA('...')/parent::B/parent::A
   * 6. A[B/C = '...']       : IA('...', C)/parent::B/parent::A
   * 7. A[@a = '...']        : IA('...', @a)/parent::A
   * 8. @a[. = '...']        : IA('...', @a)</pre>
   *
   * Queries of type 1, 3, 5 will not yield any results if the string to be compared is empty.
   *
   * @param cc compilation context
   * @param rt root value (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr index(final CompileContext cc, final Value rt) throws QueryException {
    /* skip optimization...
     * - if root is known, but does not point to document node, or
     * - if focus is nested, and if value is bound to focus */
    if(rt != null && rt.type != NodeType.DOC || cc.nestedFocus() && cc.qc.focus.value != null)
      return this;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int indexPred = 0, indexStep = 0;

    // check if path can be converted to an index access
    final Data data = rt != null ? rt.data() : null;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      // Example for position predicate: child:x[1] != parent::x[1]
      final Step step = axisStep(s);
      if(step == null || !step.axis.down || step.positional()) break;

      final int el = step.exprs.length;
      if(el > 0) {
        // check if path is iterable (i.e., will be duplicate-free)
        final boolean iter = pathNodes(data, s) != null;
        final IndexDb db = data != null ? new IndexStaticDb(data, iter, info) :
          new IndexDynDb(info, iter, root == null ? new ContextValue(info) : root);

        // choose cheapest index access
        for(int e = 0; e < el; e++) {
          final IndexInfo ii = new IndexInfo(db, cc.qc, step);
          if(!step.exprs[e].indexAccessible(ii)) continue;

          if(ii.costs.results() == 0) {
            // no results...
            cc.info(OPTNORESULTS_X, ii.step);
            return Empty.SEQ;
          }

          if(index == null || index.costs.compareTo(ii.costs) > 0) {
            index = ii;
            indexPred = e;
            indexStep = s;
          }
        }
      }
    }

    // skip rewriting if no index access is possible, or if it is too expensive
    if(index == null || data != null && index.costs.tooExpensive(data)) return this;
    // skip optimization if it is not enforced
    if(rt instanceof Dummy && !index.enforce()) return this;

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
      final Path path = (Path) index.expr;
      resultRoot = path.root;
      resultSteps.add(path.steps);
    } else {
      resultRoot = index.expr;
    }

    // only one hit: update sequence type
    if(index.costs.results() == 1) {
      final Occ occ = resultRoot instanceof IndexAccess ? Occ.ONE : Occ.ZERO_ONE;
      ((ParseExpr) resultRoot).exprType.assign(occ);
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
   * @param i index step
   * @return result of check
   */
  private boolean predSteps(final Data data, final int i) {
    for(int s = i; s >= 0; s--) {
      final Step step = axisStep(s);
      // ensure that the index step does not use wildcard
      if(step.test.kind == Kind.WILDCARD && s != i) continue;
      // consider child steps with name test and without predicates
      if(step.test.kind != Kind.NAME || step.axis != CHILD ||
          s != i && step.exprs.length > 0) return true;

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
          Expr expr = mergeList(next);
          if(expr != null) {
            steps[s + 1] = expr;
            opt = true;
            continue;
          }
          // //(X, Y)[text()] -> (/descendant::X | /descendant::Y)[text()]
          if(next instanceof Filter && !((Filter) next).positional()) {
            final Filter f = (Filter) next;
            expr = mergeList(f.root);
            if(expr != null) {
              f.root = expr;
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
   * Tries to rewrite union or list expressions.
   * @param expr input expression
   * @return rewriting flag or {@code null}
   */
  private Expr mergeList(final Expr expr) {
    if(expr instanceof Union || expr instanceof List) {
      final Arr array = (Arr) expr;
      if(childSteps(array)) {
        for(final Expr ex : array.exprs) ((Step) ((Path) ex).steps[0]).axis = DESC;
        return new Union(array.info, array.exprs);
      }
    }
    return null;
  }

  /**
   * Checks if the expressions in the specified array start with child steps.
   * @param array array expression to be checked
   * @return result of check
   */
  private static boolean childSteps(final Arr array) {
    for(final Expr expr : array.exprs) {
      if(!(expr instanceof Path)) return false;
      final Path path = (Path) expr;
      if(path.root != null || !simpleChild(path.steps[0])) return false;
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
      if(step.axis == CHILD && !step.positional()) return true;
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
    boolean changed = false;
    if(root != null) {
      final Expr rt = root.inline(var, ex, cc);
      if(rt != null) {
        root = rt;
        changed = true;
      }
    }

    cc.pushFocus(root != null ? root : cc.qc.focus.value);
    try {
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr exp = steps[s].inline(var, ex, cc);
        if(exp != null) {
          steps[s] = exp;
          changed = true;
        }
        cc.updateFocus(steps[s]);
      }
      return changed ? optimize(cc) : null;
    } finally {
      cc.removeFocus();
    }
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
    int size = 1;
    for(final Expr step : steps) size += step.exprSize();
    return root == null ? size : size + root.exprSize();
  }

  @Override
  public final boolean equals(final Object obj) {
    if(!(obj instanceof Path)) return false;
    final Path path = (Path) obj;
    return Objects.equals(root, path.root) && Array.equals(steps, path.steps);
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), root, steps);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr step : steps) {
      if(sb.length() != 0) sb.append('/');
      final String s = step.toString();
      final boolean par = !s.contains("[") && s.contains(" ");
      if(par) sb.append('(');
      sb.append(step);
      if(par) sb.append(')');
    }
    return sb.toString();
  }
}
