package org.basex.query.expr.path;

import static org.basex.query.expr.path.Axis.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.index.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Path expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Root expression (can be {@code null}). */
  public Expr root;
  /** Path steps. */
  public Expr[] steps;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param type type
   * @param root root expression (can be {@code null})
   * @param steps steps
   */
  protected Path(final InputInfo info, final Type type, final Expr root, final Expr... steps) {
    super(info, SeqType.get(type, Occ.ZERO_OR_MORE));
    this.root = root;
    this.steps = steps;
  }

  /**
   * Creates a new, optimized path expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param root root expression (can be temporary {@link Dummy} node or {@code null})
   * @param steps steps
   * @return path instance
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr root,
      final Expr... steps) throws QueryException {
    return get(info, root, steps).optimize(cc);
  }

  /**
   * Returns a new path instance.
   * A path implementation is chosen that works fastest for the given steps.
   * @param info input info (can be {@code null})
   * @param input input expression (can be temporary {@link Dummy} node or {@code null})
   * @param steps steps
   * @return path instance
   */
  public static Expr get(final InputInfo info, final Expr input, final Expr... steps) {
    // add steps of input array
    boolean axes = true;
    final ExprList tmp = new ExprList(steps.length);
    for(final Expr step : steps) {
      Expr expr = step;
      if(expr instanceof ContextValue) {
        // rewrite context value reference to self step
        expr = Step.get(expr.info(info), SELF, KindTest.NODE);
      } else if(expr instanceof final Filter filter) {
        // rewrite filter expression to self step with predicates
        if(filter.root instanceof ContextValue) {
          expr = Step.get(filter.info(), SELF, KindTest.NODE, filter.exprs);
        }
      }
      tmp.add(expr);
      axes = axes && expr instanceof Step;
    }
    final Expr root = input instanceof ContextValue && input.size() == 1 ||
        input instanceof Dummy ? null : input;
    final Expr[] stps = tmp.finish();
    final Expr step = root == null && stps.length == 1 ? stps[0] : null;

    // choose best implementation
    if(axes) {
      if(iterative(input, stps)) {
        // example: a
        if(step != null && !step.has(Flag.POS)) return new SingleIterPath(info, step);
        // example: a/b
        return new IterPath(info, root, stps);
      }
      // example: a/b/..
      return new CachedPath(info, root, stps);
    }

    // examples: 'text', (a union b)
    if(step != null && (step.seqType().instanceOf(SeqType.ANY_ATOMIC_TYPE_ZM) || step.ddo())) {
      return stps[0];
    }
    // example: (1 to 10)/<xml/>
    return new MixedPath(info, root, stps);
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

    cc.get(rt, true, () -> {
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr step = cc.compileOrError(steps[s], root == null && s == 0);
        steps[s] = step;
        cc.updateFocus(step, true);
      }
      return null;
    });

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
    // merge adjacent steps
    if(expr == this) expr = mergeSteps(cc);
    // move predicates downward
    if(expr == this) expr = movePredicates(cc);
    // return optimized expression
    if(expr != this) return expr.optimize(cc);

    // assign sequence type, compute result size
    final Expr rt = root != null ? root : cc.qc.focus.value;
    seqType(rt);

    // remove paths that will yield no result
    expr = removeEmpty(cc, rt);
    // rewrite to simple map
    if(expr == this) expr = toMap(cc);
    // check index access
    if(expr == this) expr = index(cc, rt);
    /* rewrite descendant to child steps. this optimization is called after the index rewritings,
     * as it is cheaper to invert a descendant step. examples:
     * - //B [. = '...']  ->  IA('...', B)
     * - /A/B[. = '...']  ->  IA('...', B)/parent::A *[parent::document-node()] */
    if(expr == this) expr = children(cc, rt);
    // return optimized expression
    if(expr != this) return expr;

    // choose the best path implementation (dummy will be used for type checking)
    return copyType(get(info, root == null && rt instanceof Dummy ? rt : root, steps));
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // merge nested predicates. example: if(a[b])  ->  if(a/b)
      final Expr last = steps[steps.length - 1];
      if(last instanceof final Step step && step.exprs.length == 1 &&
          step.seqType().type instanceof NodeType && !step.exprs[0].seqType().mayBeNumber()) {
        final Expr ex = step.flattenEbv(this, true, cc);
        if(ex != step) expr = ex;
      }
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * Removes the last predicate from the last step and returns the optimized expression.
   * An error will be raised if the last step is no XPath step.
   * @param cc compilation context
   * @return new path
   * @throws QueryException query exception
   */
  public final Expr removePredicate(final CompileContext cc) throws QueryException {
    final ExprList list = new ExprList(steps.length).add(steps);
    final Step step = ((Step) list.pop()).removePredicate();
    list.add(cc.get(root, true, () -> step.optimize(cc)));
    return copyType(get(cc, info, root, list.finish()));
  }

  @Override
  public final boolean has(final Flag... flags) {
    // Context dependency, positional access: only check root expression.
    // Examples: text(); ./abc; position()/a
    if(Flag.FCS.oneOf(flags) ||
       Flag.CTX.oneOf(flags) && (root == null || root.has(Flag.CTX)) ||
       Flag.POS.oneOf(flags) && root != null && root.has(Flag.POS)) return true;
    // check remaining flags
    final Flag[] flgs = Flag.remove(flags, Flag.POS, Flag.CTX);
    if(flgs.length == 0) return false;
    for(final Expr step : steps) {
      if(step.has(flgs)) return true;
    }
    return root != null && root.has(flgs);
  }

  /**
   * Tries to cast the specified step into an axis step.
   * @param index index
   * @return axis step, or {@code null})
   */
  private Step axisStep(final int index) {
    return steps[index] instanceof final Step step ? step : null;
  }

  /**
   * Checks if this path exclusively contains self, child and attribute steps.
   * @return result of check
   */
  public boolean simple() {
    for(final Expr step : steps) {
      if(!(step instanceof final Step stp) || !stp.axis.oneOf(SELF, CHILD, ATTRIBUTE)) return false;
    }
    return true;
  }

  /**
   * Flattens nested path expressions.
   * @param cc compilation context
   * @return original or optimized expression
   */
  private Expr flatten(final CompileContext cc) {
    // new list with steps
    boolean changed = false;
    final ExprList tmp = new ExprList(steps.length);

    // flatten nested path
    Expr rt = root;
    if(rt instanceof final Path path) {
      tmp.add(path.steps);
      rt = path.root;
      cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) path::description, path);
      changed = true;
    }

    // add steps of input array
    for(final Expr step : steps) {
      Expr expr = step;
      if(expr instanceof final Path path) {
        // rewrite nested path to axis steps
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
  private Expr simplify(final CompileContext cc) throws QueryException {
    // root yields no result
    if(root != null && root.seqType().zero()) return cc.emptySeq(this);

    // find empty results, remove redundant steps
    final int sl = steps.length;
    boolean removed = false;
    final ExprList list = new ExprList(sl);
    for(int s = 0; s < sl; s++) {
      final Expr step = steps[s];
      final Expr prev = list.isEmpty() ? root != null ? root : cc.qc.focus.value : list.peek();
      if(prev != null) {
        final SeqType seqType = prev.seqType();
        if(seqType.type instanceof NodeType && (step instanceof ContextValue ||
            step instanceof final Step stp && stp.remove(seqType))) {
          removed = true;
          continue;
        }
      }

      // step is empty sequence. example: $doc/NON-EXISTING-STEP  ->  $doc/()  ->  ()
      final Expr expr = steps[s];
      if(expr == Empty.VALUE) return cc.emptySeq(this);

      // add step to list
      list.add(expr);

      // ignore remaining steps if step yields no results
      // example: A/void(.)/B  ->  A/void(.)
      if(expr.seqType().zero() && s + 1 < sl) {
        cc.info(QueryText.OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
        break;
      }
    }

    // self step was removed: ensure that result will be in distinct document order
    if(removed && (list.isEmpty() || !(list.get(0).seqType().type instanceof NodeType))) {
      if(root == null) root = ContextValue.get(cc, info);
      if(!root.ddo()) {
        root = cc.replaceWith(root, cc.function(Function.DISTINCT_ORDERED_NODES, info, root));
      }
    }

    // no steps left: return root
    steps = list.finish();
    return cc.replaceWith(this, steps.length == 0 ? root : this);
  }

  /**
   * Returns the path nodes that will result from this path.
   * @param rt root at compile time (can be {@code null})
   * @param stats assess database statistics
   * @return path nodes, or {@code null} if nodes cannot be evaluated
   */
  private ArrayList<PathNode> pathNodes(final Expr rt, final boolean stats) {
    // ensure that path starts with document nodes
    final Data data = data();
    return rt != null && rt.seqType().type.instanceOf(NodeType.DOCUMENT_NODE) && data != null &&
        data.meta.uptodate ? pathNodes(data.paths.root(), stats) : null;
  }

  /**
   * Returns the path nodes that will result from this path.
   * @param nodes current path nodes
   * @param stats assess database statistics
   * @return path nodes, or {@code null} if nodes cannot be collected
   */
  final ArrayList<PathNode> pathNodes(final ArrayList<PathNode> nodes, final boolean stats) {
    ArrayList<PathNode> pn = nodes;
    for(final Expr expr : steps) {
      if(expr instanceof UtilRoot) {
        pn = UtilRoot.nodes(pn);
      } else if(expr instanceof final Step step) {
        pn = step.nodes(pn, stats);
        // check if paths within predicates are correct
        if(!stats && pn != null) {
          for(final Expr ex : step.exprs) {
            if(ex instanceof final AxisPath path && path.root == null) {
              final ArrayList<PathNode> tmp = path.pathNodes(pn, false);
              if(tmp != null && tmp.isEmpty()) return tmp;
            }
          }
        }
      } else {
        pn = null;
      }
      if(pn == null) break;
    }
    return pn;
  }

  /**
   * Returns database statistics for the path nodes that will result from this path.
   * @return statistics or {@code null}
   */
  public ArrayList<Stats> pathStats() {
    final ArrayList<PathNode> nodes = pathNodes(root, true);
    if(nodes == null) return null;

    // loop through all nodes
    final ArrayList<Stats> stats = new ArrayList<>();
    for(PathNode node : nodes) {
      // retrieve text child if addressed node is an element
      if(node.kind == Data.ELEM) {
        if(!node.stats.isLeaf()) return null;
        for(final PathNode nd : node.children) {
          if(nd.kind == Data.TEXT) node = nd;
        }
      }
      // skip nodes others than texts and attributes
      // check if distinct values are available
      final int kind = node.kind;
      if(kind != Data.TEXT && kind != Data.ATTR) return null;
      stats.add(node.stats);
    }
    return stats;
  }

  /**
   * Checks if the path contains empty steps.
   * @param rt root at compile time (can be {@code null})
   * @return result of check
   */
  private boolean emptySteps(final Expr rt) {
    if(rt != null) {
      Expr prev = rt;
      for(final Expr step : steps) {
        final SeqType seqType = prev.seqType();
        if(seqType.type instanceof NodeType && step instanceof final Step stp &&
            stp.empty(seqType)) return true;
        prev = step;
      }
    }
    return false;
  }

  /**
   * Checks if a path can be evaluated iteratively (i.e., if all results will be in distinct
   * document order without final sorting).
   * @param root root expression (can be {@code null})
   * @param steps path steps
   * @return result of check
   */
  private static boolean iterative(final Expr root, final Expr... steps) {
    if(root == null || !root.ddo()) return false;

    final SeqType st = root.seqType();
    boolean atMostOne = st.zeroOrOne();
    boolean sameDepth = atMostOne || st.type.instanceOf(NodeType.DOCUMENT_NODE);

    for(final Expr expr : steps) {
      final Step step = (Step) expr;
      switch(step.axis) {
        case ATTRIBUTE:
        case SELF:
          // nothing changes
          break;
        case PARENT:
        case FOLLOWING_SIBLING:
        case FOLLOWING_SIBLING_OR_SELF:
          // can overlap, preserves level
          if(!atMostOne) return false;
          break;
        case CHILD:
          // order is only ensured if all nodes are on the same level
          if(!sameDepth) return false;
          break;
        case DESCENDANT:
        case DESCENDANT_OR_SELF:
          // non-overlapping if all nodes are on the same level
          if(!sameDepth) return false;
          sameDepth = false;
          break;
        case ANCESTOR:
        case ANCESTOR_OR_SELF:
        case PRECEDING:
        case PRECEDING_OR_SELF:
        case PRECEDING_SIBLING:
        case PRECEDING_SIBLING_OR_SELF:
          // backwards axes must be reordered
          return false;
        case FOLLOWING:
        case FOLLOWING_OR_SELF:
          // can overlap
          if(!atMostOne) return false;
          sameDepth = false;
          break;
        default:
          throw Util.notExpected();
      }
      atMostOne &= step.seqType().zeroOrOne();
    }
    return true;
  }

  /**
   * Assigns a sequence type and (if statically known) result size.
   * @param rt root at compile time (can be {@code null})
   */
  private void seqType(final Expr rt) {
    final Expr last = steps[steps.length - 1];
    final Data data = last.data();
    final SeqType st = last.seqType();
    Occ occ = Occ.ZERO_OR_MORE;
    long size = size(rt, data);

    if(size == -1 && rt != null) {
      occ = rt.seqType().occ;
      size = rt.size();

      for(final Expr step : steps) {
        occ = occ.union(step.seqType().occ);
        final long sz = step.size();
        size = size != -1 && sz != -1 ? size * sz : -1;
      }
      // more than one result: final size is unknown due to DDO
      if(size > 1) size = -1;
    }
    exprType.assign(st, occ, size).data(data);
  }

  /**
   * Computes the result size via database statistics.
   * @param rt root at compile time (can be {@code null})
   * @param data data reference
   * @return number of results (or {@code -1})
   */
  private long size(final Expr rt, final Data data) {
    // check if path will yield any results
    if(root != null && root.size() == 0) return 0;
    for(final Expr step : steps) {
      if(step.size() == 0) return 0;
    }

    // skip computation if:
    // - path does not start with document nodes,
    // - no database instance is available, outdated, or
    // - if context does not contain all database nodes
    if(rt == null || !rt.seqType().type.instanceOf(NodeType.DOCUMENT_NODE) ||
        data == null || !data.meta.uptodate || data.meta.ndocs != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths.root();
    long lastSize = 1;
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      final Step curr = axisStep(s);
      if(curr != null) {
        nodes = curr.nodes(nodes, true);
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
   * @param last last step to be checked
   * @return path nodes, or {@code null} if nodes cannot be retrieved
   */
  private ArrayList<PathNode> pathNodes(final int last) {
    // skip request if no path index exists or might be out-of-date
    final Data data = data();
    if(data == null || !data.meta.uptodate) return null;

    ArrayList<PathNode> nodes = data.paths.root();
    for(int s = 0; s <= last; s++) {
      // only follow axis steps
      final Step curr = axisStep(s);
      if(curr == null) return null;

      final boolean desc = curr.axis == DESCENDANT;
      if(!desc && curr.axis != CHILD || !(curr.test instanceof final NameTest test)) return null;
      if(test.part() != NamePart.LOCAL) return null;

      final int name = data.elemNames.index(test.qname.local());
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
   * @param rt root at compile time (can be {@code null})
   * @return original or new expression
   */
  private Expr removeEmpty(final CompileContext cc, final Expr rt) {
    final ArrayList<PathNode> nodes = pathNodes(rt, false);
    if(nodes != null ? nodes.isEmpty() : emptySteps(rt)) {
      cc.info(QueryText.OPTPATH_X, this);
      return Empty.VALUE;
    }
    return this;
  }

  /**
   * Converts descendant to child steps.
   * @param cc compilation context
   * @param rt root at compile time (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr children(final CompileContext cc, final Expr rt) throws QueryException {
    // skip optimization...
    // - if path does not start with document nodes
    // - if index does not exist or is out-dated
    // - if several namespaces occur in the input
    final Data data = data();
    if(rt == null || !rt.seqType().type.instanceOf(NodeType.DOCUMENT_NODE) ||
        data == null || !data.meta.uptodate || data.defaultNs() == null) return this;

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.exprs.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESCENDANT || curr.mayBePositional()) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> nodes = pathNodes(s);
      if(nodes == null) continue;

      // cache child steps
      final ArrayList<QNm> qNames = new ArrayList<>();
      while(nodes.get(0).parent != null) {
        QNm qName = new QNm(data.elemNames.key(nodes.get(0).name));
        // skip children with prefixes
        if(qName.hasPrefix()) return this;
        for(final PathNode node : nodes) {
          if(nodes.get(0).name != node.name) {
            qName = null;
            break;
          }
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
        final Test test = qName == null ? KindTest.ELEMENT :
          new NameTest(qName, NamePart.LOCAL, NodeType.ELEMENT, null);
        stps[t] = Step.get(cc, root, curr.info(), CHILD, test, preds);
      }
      while(++s < sl) stps[ts++] = steps[s];

      return get(cc, info, root, stps);
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
    if(!(type1 instanceof NodeType) || s2 instanceof Step || size() != 1 &&
       !type2.instanceOf(AtomType.ANY_ATOMIC_TYPE) && !type2.instanceOf(SeqType.FUNCTION))
      return this;

    /* remove last step from new root expression. examples:
     * - (<a/>, <b/>)/map { name(): . }  ->  (<a/>, <b/>) ! map { name(): . }
     * - <a/>/<b/>  ->  <a/> ! <b/>
     * - $a/b/string  ->  $a/b ! string() */
    if(sl > 1) s1 = get(cc, info, root, Arrays.copyOfRange(steps, 0, sl - 1));
    if(s1 != null) s2 = SimpleMap.get(cc, info, s1, s2);
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
   * @param rt root at compile time (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr index(final CompileContext cc, final Expr rt) throws QueryException {
    // skip optimization if path does not start with document nodes
    if(rt == null || !rt.seqType().type.instanceOf(NodeType.DOCUMENT_NODE)) return this;

    // cache index access costs
    IndexInfo index = null;
    // cheapest predicate and step
    int predIndex = 0, stepIndex = 0;

    // check if path can be converted to an index access
    final Data data = data();
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // only accept descendant steps without positional predicates
      // Example for position predicate: child:x[1] != parent::x[1]
      final Step step = axisStep(s);
      if(step == null || !step.axis.down || step.mayBePositional()) break;

      final int el = step.exprs.length;
      if(el > 0) {
        // static vs dynamic access
        final IndexDb db = data != null ?
          new IndexStaticDb(data, info) :
          new IndexDynDb(root == null ? new ContextValue(info) : root, info);

        // choose the cheapest index access
        for(int e = 0; e < el; e++) {
          final IndexInfo ii = new IndexInfo(db, cc, step);
          if(!step.exprs[e].indexAccessible(ii)) continue;

          if(ii.costs.results() == 0) {
            // no results...
            cc.info(QueryText.OPTNORESULTS_X, step);
            return Empty.VALUE;
          }

          if(index == null || index.costs.compareTo(ii.costs) > 0) {
            index = ii;
            predIndex = e;
            stepIndex = s;
          }
        }
      }
    }

    // skip rewriting if no index access is possible, if it is too expensive or if root is no value
    if(index == null || data != null && index.costs.tooExpensive(data)) return this;
    // skip optimization if it is not enforced
    if((!(rt instanceof Value) || rt instanceof Dummy) && !index.enforce()) return this;

    // rewrite for index access
    cc.info(index.optInfo);

    // create new root expression
    final ExprList indexSteps = new ExprList();
    final Expr indexRoot;
    if(index.expr instanceof final Path path) {
      indexRoot = path.root;
      indexSteps.add(path.steps);
    } else {
      indexRoot = index.expr;
    }
    // only one hit: update sequence type
    if(index.costs.results() == 1 && indexRoot instanceof final ParseExpr expr) {
      expr.exprType.assign(expr instanceof IndexAccess ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE);
    }

    // invert steps that occur before index step, rewrite them to predicates
    final Expr indexStep = indexSteps.isEmpty() ? null : indexSteps.peek();
    final ExprList invSteps = new ExprList(), lastPreds = new ExprList();
    final Test rootTest = InvDocTest.get(rt);
    if(rootTest != KindTest.DOCUMENT_NODE || data == null || !data.meta.uptodate ||
        invertSteps(stepIndex)) {
      for(int s = stepIndex; s >= 0; s--) {
        final Axis axis = axisStep(s).axis.invert();
        InputInfo ii;
        Axis newAxis;
        Test newTest;
        Expr[] newPreds;
        if(s == 0) {
          ii = info;
          newAxis = axis;
          newTest = rootTest;
          newPreds = new Expr[0];
        } else {
          final Step step = axisStep(s - 1);
          ii = step.info();
          newAxis = step.axis == ATTRIBUTE ? ATTRIBUTE : axis;
          newTest = step.test;
          newPreds = step.exprs;
        }
        // skip step if it is always successful
        if(newAxis != ANCESTOR && newAxis != ANCESTOR_OR_SELF ||
            newTest != KindTest.NODE && newTest != KindTest.DOCUMENT_NODE ||
            newPreds.length > 0) {
          final Expr expr = invSteps.isEmpty() ?
            indexStep != null ? indexStep : indexRoot : invSteps.peek();
          invSteps.add(Step.get(cc, expr, ii, newAxis, newTest, newPreds));
        }
      }
    }
    // add created steps, followed by remaining predicates
    if(!invSteps.isEmpty()) {
      lastPreds.add(cc.get(indexStep != null ? indexStep : indexRoot, true,
        () -> get(cc, info, null, invSteps.finish())));
    }
    lastPreds.add(Array.remove(index.step.exprs, predIndex));

    // attach predicates to last step or new self::node() step
    if(!lastPreds.isEmpty()) {
      indexSteps.add(indexStep instanceof Step
          ? ((Step) indexSteps.pop()).addPredicates(lastPreds.finish())
          : Step.self(cc, indexRoot, info, lastPreds.finish()));
    }

    // add remaining steps
    for(int s = stepIndex + 1; s < sl; s++) indexSteps.add(steps[s]);

    return indexSteps.isEmpty() ? indexRoot : get(cc, info, indexRoot, indexSteps.finish());
  }

  /**
   * Checks if steps before index step need to be inverted and traversed.
   * @param i index step
   * @return result of check
   */
  private boolean invertSteps(final int i) {
    for(int s = i; s >= 0; s--) {
      final Step step = axisStep(s);
      // ensure that the index step does not use wildcard
      if(step.test instanceof KindTest && s != i) continue;
      // consider child steps with name test and without predicates
      if(step.axis != CHILD || s != i && step.exprs.length > 0 ||
          !(step.test instanceof final NameTest test)) return true;
      // only consider local name tests
      if(test.part() != NamePart.LOCAL) return true;
      // only support unique paths with nodes on the correct level
      final ArrayList<PathNode> pn = data().paths.desc(test.qname.local());
      if(pn.size() != 1 || pn.get(0).level() != s + 1) return true;
    }
    return false;
  }

  /**
   * Tries to rewrite steps to union expressions.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr toUnion(final CompileContext cc) throws QueryException {
    // function for rewriting a list to a union expression
    final QueryBiFunction<Expr, Expr, Expr> rewrite = (step, next) -> {
      // do not rewrite (a, b)/<c/>
      if(step == null || next != null && next.has(Flag.CNS)) return step;
      // (a, b)/c  ->  (a | b)/a
      if(step instanceof final List lst) return lst.toUnion(cc);
      // a[b, c]  ->  a[b | c]
      if(step instanceof final Filter fltr && !fltr.mayBePositional() &&
          fltr.root instanceof final List lst) {
        final Expr st = lst.toUnion(cc);
        if(st != fltr.root) return Filter.get(cc, fltr.info(), st, fltr.exprs);
      }
      // replicate(a, 2)/b  ->  a/b
      if(step.seqType().type instanceof NodeType) {
        if(REPLICATE.is(step) && ((FnReplicate) step).singleEval(false)) return step.arg(0);
        if(step instanceof final SingletonSeq ss) return ss.itemAt(0);
      }
      return step;
    };

    // only rewrite root expression if subsequent step yields nodes
    boolean changed = false;
    if(steps[0].seqType().type instanceof NodeType) {
      final Expr rt = rewrite.apply(root, steps[0]);
      if(rt != root) {
        root = rt;
        changed = true;
      }
    }

    changed |= cc.ok(root, true, () -> {
      boolean chngd = false;
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr step = rewrite.apply(steps[s], s + 1 < sl ? steps[s + 1] : null);
        if(step != steps[s]) {
          steps[s] = step;
          chngd = true;
        }
        cc.updateFocus(step, true);
      }
      return chngd;
    });

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
    return cc.ok(root, true, () -> {
      boolean chngd = false;
      for(int s = 0; s < sl; s++) {
        Expr curr = steps[s];
        if(curr instanceof final Step crr) {
          Expr next = s < sl - 1 ? steps[s + 1] : null;
          if(crr.test == KindTest.NODE && next instanceof final Step stp && stp.axis == ATTRIBUTE) {
            // rewrite node test before attribute step: node()/@*  ->  */@*
            next = Step.get(cc, null, crr.info(), crr.axis, KindTest.ELEMENT, crr.exprs);
            curr = cc.replaceWith(curr, next);
            chngd = true;
          } else if(next != null) {
            // merge steps: //*  ->  /descendant::*
            next = mergeStep(crr, next, s > 0 ? steps[s - 1] : root, cc);
            if(next != null) {
              cc.info(QueryText.OPTMERGE_X, next);
              curr = next;
              chngd = true;
              s++;
            }
          }
        }
        stps.add(curr);
        cc.updateFocus(curr, true);
      }
      return chngd;
    }) ? get(info, root, stps.finish()) : this;
  }

  /**
   * Moves predicates downward.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr movePredicates(final CompileContext cc) throws QueryException {
    // examples:
    // a[b]/b      ->  a/b
    // a[b]/b/c    ->  a/b/c
    // a[b/c]/b/c  ->  a/b/c
    return cc.get(root, true, () -> {
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr curr = steps[s];
        if(curr instanceof Step) {
          final Expr ex = movePredicates(s);
          if(ex != null) return ex;
        }
        cc.updateFocus(curr, true);
      }
      return this;
    });
  }

  /**
   * Moves a predicate downwards.
   * @param s current step
   * @return new expression or {@code null}
   */
  private Expr movePredicates(final int s) {
    final Step step = (Step) steps[s];
    if(step.exprs.length != 1 || step.mayBePositional()) return null;

    final Expr pred = step.exprs[0];
    if(!(pred instanceof final Path path) || path.root != null) return null;

    final Expr[] predSteps = path.steps;
    final int sl = steps.length, pl = predSteps.length;
    int p = 0;
    for(int i = s + 1; i < sl && p < pl; p++, i++) {
      if(!steps[i].equals(predSteps[p])) break;
    }
    if(p < pl) return null;

    // compose new path, adopt analyzed step without predicates
    final Expr[] exprs = steps.clone();
    exprs[s] = step.copyType(Step.get(step.info(), step.axis, step.test));
    return get(info, root, exprs);
  }

  /**
   * Merges adjacent steps.
   * @param curr current step
   * @param next next step
   * @param prev previous step (can be {@code null})
   * @param cc compilation context
   * @return merged expression or {@code null}
   * @throws QueryException query exception
   */
  private static Expr mergeStep(final Step curr, final Expr next, final Expr prev,
      final CompileContext cc) throws QueryException {

    // do not merge if current step contains positional predicates
    if(curr.mayBePositional()) return null;

    // merge self steps:  child::*/self::a  ->  child::a
    final Step nxt = next instanceof final Step stp ? stp : null;
    if(nxt != null && nxt.axis == SELF && !nxt.mayBePositional()) {
      final Test test = curr.test.intersect(nxt.test);
      return test == null ? null :
        Step.get(cc, prev, curr.info(), curr.axis, test, ExprList.concat(curr.exprs, nxt.exprs));
    }

    // merge descendant-or-self::node()
    if(curr.axis != DESCENDANT_OR_SELF || curr.test != KindTest.NODE || curr.exprs.length > 0)
      return null;

    // examples:
    // - descendant-or-self::node()/*  ->  descendant::*
    // - descendant-or-self::node()/descendant::*  ->  descendant::*
    // - descendant-or-self::node()/descendant-or-self::*  ->  descendant-or-self::*
    final Axis merged = mergedAxis(nxt);
    if(merged != null) return Step.get(cc, prev, nxt.info(), merged, nxt.test, nxt.exprs);

    // function for merging steps inside union expressions
    final QueryFunction<Expr, Expr> rewrite = expr -> {
      if(expr instanceof Union) {
        final Axis axis = commonAxis(expr.args());
        if(axis != null) {
          for(final Expr path : expr.args()) {
            final Path p = (Path) path;
            final Step s = (Step) p.steps[0];
            p.steps[0] = Step.get(cc, prev, s.info(), axis, s.test, s.exprs);
          }
          return expr.optimize(cc);
        }
      }
      return null;
    };
    // descendant-or-self::node()/(* | text())  ->  (descendant::text() | (descendant::*)
    if(next instanceof Union) return rewrite.apply(next);

    // descendant-or-self::node()/(text()|*)[..]  ->  (descendant::text() | descendant::*)[..]
    if(next instanceof final Filter filter && !filter.mayBePositional()) {
      final Expr expr = rewrite.apply(filter.root);
      if(expr != null) return Filter.get(cc, filter.info(), expr, filter.exprs);
    }
    return null;
  }

  /**
   * Returns a merged axis for a step preceded by descendant-or-self::node().
   * @param expr step to test
   * @return axis or {@code null}
   */
  private static Axis mergedAxis(final Expr expr) {
    if(expr instanceof final Step step) {
      final Axis axis = step.axis;
      if(!step.mayBePositional()) {
        if(axis == CHILD || axis == DESCENDANT) return DESCENDANT;
        if(axis == DESCENDANT_OR_SELF) return DESCENDANT_OR_SELF;
      }
    }
    return null;
  }

  /**
   * Returns the common merged axis of multiple expressions.
   * @param exprs expressions
   * @return common axis or {@code null}
   */
  private static Axis commonAxis(final Expr... exprs) {
    Axis common = null;
    for(final Expr ex : exprs) {
      if(!(ex instanceof final Path path) || path.root != null) return null;
      final Axis merged = mergedAxis(path.steps[0]);
      common = common == merged || common == null ? merged : null;
      if(common == null) return null;
    }
    return common;
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    // do not replace $v with .:  EXPR/$v
    if(ic.var != null && ic.expr.has(Flag.CTX)) {
      for(final Expr step : steps) {
        if(step.uses(ic.var)) return false;
      }
    }
    return root == null || root.inlineable(ic);
  }

  @Override
  public final VarUsage count(final Var var) {
    // context reference check: only consider root
    if(var == null) return root == null ? VarUsage.ONCE : root.count(null);

    final VarUsage inRoot = root == null ? VarUsage.NEVER : root.count(var);
    return VarUsage.sum(var, steps) == VarUsage.NEVER ? inRoot : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    if(root != null) {
      final Expr inlined = root.inline(ic);
      if(inlined != null) {
        root = inlined;
        changed = true;
      }
    } else if(ic.var == null) {
      // relative path: assign new root
      root = ic.copy();
      changed = true;
    }

    // optimize steps with new root context
    final CompileContext cc = ic.cc;
    final int sl = steps.length;
    final Expr rt = root != null ? root : cc.qc.focus.value;
    if(changed) cc.get(rt, true, () -> {
      for(int s = 0; s < sl; s++) {
        steps[s] = steps[s].optimize(cc);
        cc.updateFocus(steps[s], true);
      }
      return null;
    });

    changed |= ic.var != null && cc.ok(rt, true, () -> {
      boolean chngd = false;
      for(int s = 0; s < sl; s++) {
        final Expr step = steps[s].inline(ic);
        if(step != null) {
          steps[s] = step;
          chngd = true;
        }
        cc.updateFocus(steps[s], true);
      }
      return chngd;
    });

    return changed ? optimize(cc) : null;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
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
    return this == obj || obj instanceof final Path path && Objects.equals(root, path.root) &&
        Array.equals(steps, path.steps);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), root, steps);
  }

  @Override
  public void toString(final QueryString qs) {
    if(root != null) qs.token(root).token('/');
    qs.tokens(steps, "/");
  }
}
