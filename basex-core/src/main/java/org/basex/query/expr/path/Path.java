package org.basex.query.expr.path;

import static org.basex.query.expr.path.Axis.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.index.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Root expression (can be {@code null}). */
  public Expr root;
  /** Path steps. */
  public Expr[] steps;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression (can be {@code null})
   * @param steps steps
   */
  protected Path(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, SeqType.ITEM_ZM);
    this.root = root;
    this.steps = steps;
  }

  /**
   * Returns a new path instance.
   * A path implementation is chosen that works fastest for the given steps.
   * @param ii input info
   * @param root root expression (can be temporary {@link Dummy} item or {@code null})
   * @param steps steps
   * @return path instance
   */
  public static Path get(final InputInfo ii, final Expr root, final Expr... steps) {
    // add steps of input array
    final ExprList tmp = new ExprList(steps.length);
    for(final Expr step : steps) {
      Expr expr = step;
      if(expr instanceof ContextValue) {
        // rewrite context item to self step
        expr = Step.get(((ContextValue) expr).info, SELF, KindTest.NOD);
      } else if(expr instanceof Filter) {
        // rewrite filter expression to self step with predicates
        final Filter f = (Filter) expr;
        if(f.root instanceof ContextValue) expr = Step.get(f.info, SELF, KindTest.NOD, f.exprs);
      }
      tmp.add(expr);
    }
    final Expr[] stps = tmp.finish();

    // check if path can be evaluated iteratively
    Expr rt = root;
    boolean axes = true;
    for(final Expr expr : stps) axes &= expr instanceof Step;
    final boolean iterative = axes && iterative(rt, stps);

    // normalize root context
    if(rt instanceof ContextValue || rt instanceof Dummy) rt = null;

    final boolean single = iterative && rt == null && stps.length == 1 && !stps[0].has(Flag.POS);

    return single ? new SingleIterPath(ii, stps[0]) :
      iterative ? new IterPath(ii, rt, stps) :
      axes ? new CachedPath(ii, rt, stps) :
      new MixedPath(ii, rt, stps);
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
    final Expr rt;
    if(root != null) {
      root = root.compile(cc);
      rt = root;
    } else {
      rt = cc.qc.focus.value;
    }

    cc.pushFocus(rt);
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      Expr step = steps[s];
      try {
        step = step.compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        step = cc.error(ex, this);
      }
      steps[s] = step;
      cc.updateFocus(step);
    }
    cc.removeFocus();

    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // no root, no nesting: assign context value (may be null)
    if(root == null && !cc.nestedFocus()) root = cc.qc.focus.value;

    // remove redundant steps, find empty steps
    Expr expr = simplify(cc);
    if(expr != this) return expr;

    // flatten nested path expressions
    expr = flatten(cc);
    // rewrite list to union expressions
    if(expr == this) expr = toUnion(cc);
    // merge descendant-or-self steps
    if(expr == this) expr = mergeSteps(cc);
    // return optimized expression
    if(expr != this) return expr.optimize(cc);

    // assign sequence type, compute result size
    Expr rt = root != null ? root : cc.qc.focus.value;
    seqType(rt);

    // remove paths that will yield no result
    expr = removeEmpty(cc, rt);
    // rewrite to simple map
    if(expr == this) expr = toMap(cc);
    // check index access
    if(expr == this) expr = index(cc, rt);
    /* rewrite descendant to child steps. this optimization is called after the index rewritings,
     * as it is cheaper to invert a descendant step. examples:
     * - //B [. = '...'] -> IA('...', B)
     * - /A/B[. = '...'] -> IA('...', B)/parent::A *[parent::document-node()] */
    if(expr == this) expr = children(cc, rt);
    // return optimized expression
    if(expr != this) return expr.optimize(cc);

    // choose best path implementation (dummy will be used for type checking)
    return copyType(get(info, rt = root == null && rt instanceof Dummy ? rt : root, steps));
  }

  @Override
  public final Expr optimizeEbv(final CompileContext cc) throws QueryException {
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
      for(final Expr step : steps) {
        if(step.has(flgs)) return true;
      }
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
   * Flattens nested path expressions.
   * @param cc compilation context
   * @return original or optimized expression
   */
  public final Expr flatten(final CompileContext cc) {
    // new list with steps
    boolean changed = false;
    final ExprList tmp = new ExprList(steps.length);

    // flatten nested path
    Expr rt = root;
    if(rt instanceof Path) {
      final Path path = (Path) rt;
      tmp.add(path.steps);
      rt = path.root;
      cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) path::description, path);
      changed = true;
    }

    // add steps of input array
    for(final Expr step : steps) {
      Expr expr = step;
      if(expr instanceof Path) {
        // rewrite nested path to axis steps
        final Path path = (Path) expr;
        if(path.root != null && !(path.root instanceof ContextValue)) tmp.add(path.root);
        final int pl = path.steps.length - 1;
        for(int i = 0; i < pl; i++) tmp.add(path.steps[i]);
        expr = path.steps[pl];
        cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) path::description, path);
        changed = true;
      }
      tmp.add(expr);
    }
    return changed ? get(info, rt, tmp.finish()) : this;
  }

  /**
   * Simplifies the path expression.
   * @param cc compilation context
   * @return original or optimized expression
   * @throws QueryException query exception
   */
  public final Expr simplify(final CompileContext cc) throws QueryException {
    // return root if it yields no result
    if(root != null && root.seqType().zero()) return cc.replaceWith(this, root);

    // find empty results, remove redundant steps
    final int sl = steps.length;
    Step self = null;
    final ExprList list = new ExprList(sl);
    for(int s = 0; s < sl; s++) {
      // remove redundant steps. example: <xml/>/self::node() -> <xml/>
      final Step st = axisStep(s);
      if(st != null && st.axis == SELF && st.exprs.length == 0 && st.test instanceof KindTest) {
        final Expr prev = list.isEmpty() ? root : list.peek();
        if(prev != null && prev.seqType().type.instanceOf(st.test.type)) {
          self = st;
          continue;
        }
      }

      // step is empty sequence. example: $doc/NON-EXISTING-STEP -> $doc/() -> ()
      final Expr expr = steps[s];
      if(expr == Empty.VALUE) return cc.emptySeq(this);

      // add step to list
      list.add(expr);

      // ignore remaining steps if step yields no results
      // example: A/prof:void(.)/B -> A/prof:void(.)
      if(expr.seqType().zero() && s + 1 < sl) {
        cc.info(QueryText.OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
        break;
      }
    }

    // self step was removed: ensure that result will be in distinct document order
    if(self != null && (list.isEmpty() || !list.get(0).seqType().type.instanceOf(NodeType.NOD))) {
      if(root == null) root = new ContextValue(info).optimize(cc);
      if(!root.seqType().zeroOrOne() || root instanceof DBNodeSeq) {
        root = cc.replaceWith(root, cc.function(Function._UTIL_DDO, info, root));
      }
    }

    // no steps left: return root
    steps = list.finish();
    return steps.length == 0 ? cc.replaceWith(this, root) : this;
  }

  /**
   * Returns the path nodes that will result from this path.
   * @param rt root at compile time (can be {@code null})
   * @return path nodes or {@code null} if nodes cannot be evaluated
   */
  public final ArrayList<PathNode> pathNodes(final Expr rt) {
    // ensure that path starts with document nodes
    if(rt == null || !rt.seqType().instanceOf(SeqType.DOC_ZM)) return null;

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
   * Checks if the specified axis steps can be evaluated iteratively.
   * @param root root expression (can be {@code null})
   * @param steps path steps
   * @return result of check
   */
  private static boolean iterative(final Expr root, final Expr... steps) {
    if(root == null || !root.iterable()) return false;

    final SeqType st = root.seqType();
    boolean atMostOne = st.zeroOrOne();
    boolean sameDepth = atMostOne || st.instanceOf(SeqType.DOC_ZM);

    for(final Expr expr : steps) {
      final Step step = (Step) expr;
      switch(step.axis) {
        case ANCESTOR:
        case ANCESTOR_OR_SELF:
        case PRECEDING:
        case PRECEDING_SIBLING:
          // backwards axes must be reordered
          return false;
        case FOLLOWING:
          // can overlap
          if(!atMostOne) return false;
          atMostOne = false;
          sameDepth = false;
          break;
        case FOLLOWING_SIBLING:
          // can overlap, preserves level
          if(!atMostOne) return false;
          atMostOne = false;
          break;
        case ATTRIBUTE:
          // only unique for exact QName matching
          atMostOne &= step.test instanceof NameTest &&
            ((NameTest) step.test).part == NamePart.FULL;
          break;
        case CHILD:
          // order is only ensured if all nodes are on the same level
          if(!sameDepth) return false;
          atMostOne = false;
          break;
        case DESCENDANT:
        case DESCENDANT_OR_SELF:
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
   * @param rt compile time root (can be {@code null})
   */
  private void seqType(final Expr rt) {
    final Type type = steps[steps.length - 1].seqType().type;
    Occ occ = Occ.ZERO_MORE;
    long size = size(rt);

    if(size == -1 && rt != null) {
      size = rt.size();
      occ = rt.seqType().occ;

      for(final Expr step : steps) {
        final long sz = step.size();
        size = size != -1 && sz != -1 ? size * sz : -1;
        occ = occ.union(step.seqType().occ);
      }
      // more than one result: final size is unknown due to DDO
      if(size > 1) size = -1;
    }
    exprType.assign(type, occ, size);
  }

  /**
   * Computes the result size via database statistics.
   * @param rt compile time root (can be {@code null})
   * @return number of results (or {@code -1})
   */
  private long size(final Expr rt) {
    // check if path will yield any results
    if(root != null && root.size() == 0) return 0;
    for(final Expr step : steps) {
      if(step.size() == 0) return 0;
    }

    // skip computation if path does not start with document nodes
    if(rt == null || !rt.seqType().instanceOf(SeqType.DOC_ZM)) return -1;

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
        if(lastSize == -1) return -1;
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

      final boolean desc = curr.axis == DESCENDANT;
      if(!desc && curr.axis != CHILD || !(curr.test instanceof NameTest)) return null;

      final NameTest test = (NameTest) curr.test;
      if(test.part != NamePart.LOCAL) return null;

      final int name = data.elemNames.id(test.name.local());
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
   * Returns an empty sequence if the path will yield no results.
   * @param cc compilation context
   * @param rt compile time root (can be {@code null})
   * @return original or new expression
   */
  private Expr removeEmpty(final CompileContext cc, final Expr rt) {
    final Check emptySteps = () -> {
      Expr prev = rt;
      for(final Expr step : steps) {
        if(step instanceof Step && prev != null) {
          final Type type = prev.seqType().type;
          if(type instanceof NodeType && ((Step) step).emptyStep((NodeType) type)) return true;
        }
        prev = step;
      }
      return false;
    };

    final ArrayList<PathNode> nodes = pathNodes(rt);
    if(nodes != null ? nodes.isEmpty() : emptySteps.ok()) {
      cc.info(QueryText.OPTPATH_X, this);
      return Empty.VALUE;
    }
    return this;
  }

  /**
   * Converts descendant to child steps.
   * @param cc compilation context
   * @param rt compile time root (can be {@code null})
   * @return original or new expression
   */
  private Expr children(final CompileContext cc, final Expr rt) {
    // skip optimization if path does not start with document nodes
    if(rt == null || !rt.seqType().instanceOf(SeqType.DOC_ZM)) return this;

    // skip if index does not exist or is out-dated, or if several namespaces occur in the input
    final Data data = rt.data();
    if(data == null || !data.meta.uptodate || data.nspaces.globalUri() == null) return this;

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.exprs.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESCENDANT || curr.positional()) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(data, s);
      if(nodes == null) continue;

      // cache child steps
      final ArrayList<QNm> qNames = new ArrayList<>();
      while(nodes.get(0).parent != null) {
        QNm qName = new QNm(data.elemNames.key(nodes.get(0).name));
        // skip children with prefixes
        if(qName.hasPrefix()) return this;
        for(final PathNode node : nodes) {
          if(nodes.get(0).name != node.name) qName = null;
        }
        qNames.add(qName);
        nodes = PathIndex.parent(nodes);
      }
      cc.info(QueryText.OPTCHILD_X, steps[s]);

      // build new steps
      int ts = qNames.size();
      final Expr[] stps = new Expr[ts + sl - s - 1];
      for(int t = 0; t < ts; t++) {
        final Expr[] preds = t == ts - 1 ? ((Preds) steps[s]).exprs : new Expr[0];
        final QNm qName = qNames.get(ts - t - 1);
        final Test test = qName == null ? KindTest.ELM :
          new NameTest(NodeType.ELM, qName, NamePart.LOCAL, null);
        stps[t] = Step.get(info, CHILD, test, preds);
      }
      while(++s < sl) stps[ts++] = steps[s];

      return get(info, root, stps);
    }
    return this;
  }

  /**
   * Tries to rewrite the path to a simple map expression.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr toMap(final CompileContext cc) throws QueryException {
    // do not rewrite relative paths with single step
    final int sl = steps.length;
    if(root == null && sl == 1) return this;

    Expr s1 = sl > 1 ? steps[sl - 2] : root, s2 = steps[sl - 1];
    final Type type1 = s1.seqType().type, type2 = s2.seqType().type;

    /* rewrite if:
     * - previous expression yields nodes (otherwise, an error must be raised at runtime)
     * - last expression is no step, and yields a single result or no node */
    if(!type1.instanceOf(NodeType.NOD) || s2 instanceof Step || size() != 1 &&
       !type2.instanceOf(AtomType.AAT) && !type2.instanceOf(SeqType.ANY_FUNC)) return this;

    /* remove last step from new root expression. examples:
     * - (<a/>, <b/>)/map { name(): . }  ->  (<a/>, <b/>) ! map { name(): . }
     * - <a/>/<b/>  ->  <a/> ! <b/>
     * - $a/b/string  ->  $a/b ! string() */
    if(sl > 1) s1 = get(info, root, Arrays.copyOfRange(steps, 0, sl - 1)).optimize(cc);
    if(s1 != null) s2 = SimpleMap.get(info, s1, s2);
    return cc.replaceWith(this, s2);
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
   * @param rt compile time root (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr index(final CompileContext cc, final Expr rt) throws QueryException {
    // skip optimization if path does not start with document nodes
    if(rt == null || !rt.seqType().instanceOf(SeqType.DOC_ZM)) return this;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int indexPred = 0, indexStep = 0;

    // check if path can be converted to an index access
    final Data data = rt.data();
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      // Example for position predicate: child:x[1] != parent::x[1]
      final Step step = axisStep(s);
      if(step == null || !step.axis.down || step.positional()) break;

      final int el = step.exprs.length;
      if(el > 0) {
        // check if path is iterable (i.e., will be duplicate-free)
        final IndexDb db = data != null ?
          new IndexStaticDb(data, pathNodes(data, s) != null, info) :
          new IndexDynDb(info, false, root == null ? new ContextValue(info) : root);

        // choose cheapest index access
        for(int e = 0; e < el; e++) {
          final IndexInfo ii = new IndexInfo(db, cc.qc, step);
          if(!step.exprs[e].indexAccessible(ii)) continue;

          if(ii.costs.results() == 0) {
            // no results...
            cc.info(QueryText.OPTNORESULTS_X, ii.step);
            return Empty.VALUE;
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
        invertSteps(data, indexStep)) {
      for(int s = indexStep; s >= 0; s--) {
        final Axis invAxis = axisStep(s).axis.invert();
        if(s == 0) {
          // add document test for collections and axes other than ancestors
          if(rootTest != KindTest.DOC || invAxis != ANCESTOR && invAxis != ANCESTOR_OR_SELF)
            invSteps.add(Step.get(info, invAxis, rootTest));
        } else {
          final Step prevStep = axisStep(s - 1);
          final Axis newAxis = prevStep.axis == ATTRIBUTE ? ATTRIBUTE : invAxis;
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
  private boolean invertSteps(final Data data, final int i) {
    for(int s = i; s >= 0; s--) {
      final Step step = axisStep(s);
      // ensure that the index step does not use wildcard
      if(step.test instanceof KindTest && s != i) continue;
      // consider child steps with name test and without predicates
      if(step.axis != CHILD || s != i && step.exprs.length > 0 ||
          !(step.test instanceof NameTest)) return true;
      // only consider local name tests
      final NameTest test = (NameTest) step.test;
      if(test.part != NamePart.LOCAL) return true;
      // only support unique paths with nodes on the correct level
      final ArrayList<PathNode> pn = data.paths.desc(test.name.local());
      if(pn.size() != 1 || pn.get(0).level() != s + 1) return true;
    }
    return false;
  }

  /**
   * Tries to rewrite lists to union expressions.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr toUnion(final CompileContext cc) throws QueryException {
    // function for rewriting a list to a union expression
    final QueryFunction<Expr, Expr> rewrite = step -> {
      if(step instanceof List) {
        final List list = (List) step;
        if(((Checks<Expr>) ex -> ex.seqType().instanceOf(SeqType.NOD_ZM)).all(list.exprs)) {
          return cc.replaceWith(list, new Union(list.info, list.exprs)).optimize(cc);
        }
      }
      return null;
    };

    boolean changed = false;
    final int sl = steps.length;

    cc.pushFocus(root);
    try {
      for(int s = 0; s < sl; s++) {
        Expr step = rewrite.apply(steps[s]);
        if(step == null && steps[s] instanceof Filter) {
          final Filter filter = (Filter) steps[s];
          if(!filter.positional()) {
            step = rewrite.apply(filter.root);
            if(step != null) step = Filter.get(filter.info, step, filter.exprs).optimize(cc);
          }
        }
        if(step != null) {
          changed = true;
          steps[s] = step;
        }
        cc.updateFocus(steps[s]);
      }
    } finally {
      cc.removeFocus();
    }
    return changed ? get(info, root, steps) : this;
  }

  /**
   * Merges adjacent steps.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr mergeSteps(final CompileContext cc) throws QueryException {
    final int sl = steps.length;
    final ExprList stps = new ExprList(sl);

    cc.pushFocus(root);
    try {
      for(int s = 0; s < sl; s++) {
        Expr curr = steps[s], next = s < sl - 1 ? mergeStep(curr, steps[s + 1], cc) : null;
        if(next != null) {
          cc.info(QueryText.OPTMERGE_X, next);
          curr = next;
          s++;
        }
        stps.add(curr);
        cc.updateFocus(curr);
      }
    } finally {
      cc.removeFocus();
    }

    return stps.size() != steps.length ? get(info, root, stps.finish()) : this;
  }

  /**
   * Merges adjacent steps.
   * @param curr current step
   * @param next next step
   * @param cc compilation context
   * @return merged expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr mergeStep(final Expr curr, final Expr next, final CompileContext cc)
      throws QueryException {

    if(!(curr instanceof Step)) return null;
    final Step crr = (Step) curr, nxt = next instanceof Step ? (Step) next : null;

    // merge self steps
    if(nxt != null && nxt.axis == SELF && !nxt.positional()) {
      final Test test = crr.test.intersect(nxt.test);
      if(test == null) return Empty.VALUE;
      crr.test = test;
      return crr.addPreds(nxt.exprs);
    }

    if(crr.axis != DESCENDANT_OR_SELF || crr.test != KindTest.NOD || crr.exprs.length > 0)
      return null;

    // function for merging steps inside union expressions
    final QueryFunction<Expr, Expr> rewrite = expr -> {
      final Checks<Expr> startWithChild = ex -> {
        if(!(ex instanceof Path)) return false;
        final Path path = (Path) ex;
        return path.root == null && simpleChild(path.steps[0]);
      };
      if(expr instanceof Union) {
        final Union union = (Union) expr;
        if(startWithChild.all(union.exprs)) {
          for(final Expr path : union.exprs) {
            ((Step) ((Path) path).steps[0]).axis = DESCENDANT;
          }
          return new Union(union.info, union.exprs).optimize(cc);
        }
      }
      return null;
    };

    // example: //child::* -> descendant::*
    if(simpleChild(nxt)) {
      nxt.axis = DESCENDANT;
      return nxt;
    }
    // example: //(text()|*) -> (descendant::text() | descendant::*)
    if(next instanceof Union) {
      return rewrite.apply(nxt);
    }
    // example: //(text()|*)[..] -> (/descendant::text() | /descendant::*)[..]
    if(next instanceof Filter) {
      final Filter filter = (Filter) next;
      if(!filter.positional()) {
        final Expr expr = rewrite.apply(filter.root);
        if(expr != null) return Filter.get(filter.info, expr, filter.exprs).optimize(cc);
      }
    }
    return null;
  }

  /**
   * Checks if the expressions is a simple child step.
   * @param expr expression to be checked
   * @return result of check
   */
  private static boolean simpleChild(final Expr expr) {
    if(expr instanceof Step) {
      final Step step = (Step) expr;
      return step.axis == CHILD && !step.positional();
    }
    return false;
  }

  @Override
  public final boolean inlineable(final Var var) {
    for(final Expr step : steps) {
      if(step.uses(var)) return false;
    }
    return root == null || root.inlineable(var);
  }

  @Override
  public final VarUsage count(final Var var) {
    final VarUsage inRoot = root == null ? VarUsage.NEVER : root.count(var);
    return VarUsage.sum(var, steps) == VarUsage.NEVER ? inRoot : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {

    boolean changed = false;
    if(root == null) {
      if(var == null) {
        root = ex;
        changed = true;
      }
    } else {
      final Expr rt = root.inline(var, ex, cc);
      if(rt != null) {
        root = rt;
        changed = true;
      }
    }

    // #1202: during inlining, expressions will be optimized, which are based on the context value
    if(var != null) {
      cc.pushFocus(root != null ? root : cc.qc.focus.value);
      try {
        final int sl = steps.length;
        for(int s = 0; s < sl; s++) {
          final Expr step = steps[s].inline(var, ex, cc);
          if(step != null) {
            steps[s] = step;
            changed = true;
          }
          cc.updateFocus(steps[s]);
        }
      } finally {
        cc.removeFocus();
      }
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(root == null) {
      visitor.lock(Locking.CONTEXT, false);
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
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this), root, steps);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr step : steps) {
      if(sb.length() != 0) sb.append('/');
      sb.append(step);
    }
    return sb.toString();
  }
}
