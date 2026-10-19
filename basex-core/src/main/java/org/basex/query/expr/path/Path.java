package org.basex.query.expr.path;

import static org.basex.query.expr.path.Axis.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.path.NameTest.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
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
    super(info, type.seqType(Occ.ZERO_OR_MORE));
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
        expr = Step.get(expr.info(info), SELF, NodeTest.NODE);
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

    // an atomic step result may only replace the path if the context is an XML node
    // examples: 'text', (a union b)
    if(step != null && (step.seqType().instanceOf(Types.ANY_ATOMIC_TYPE_ZM) ?
        input != null && input.seqType().type.instanceOf(NodeType.XNODE) : step.ddo())) {
      return step;
    }
    // example: (1 to 10)/<xml/>
    return new MixedPath(info, root, stps);
  }

  @Override
  public final boolean navigational() {
    return (root == null || root.navigational()) && Checks.all(steps, Expr::navigational);
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
    updateSteps(focus(cc), cc, s -> cc.compileOrError(steps[s], root == null && s == 0));
    return optimize(cc);
  }

  /**
   * Updates the steps within the focus of their predecessors.
   * @param focus focus of the first step (can be {@code null})
   * @param cc compilation context
   * @param update function that returns a new step for an index, or {@code null}
   * @return {@code true} if a step has been changed
   * @throws QueryException query exception
   */
  private boolean updateSteps(final Expr focus, final CompileContext cc,
      final QueryFunction<Integer, Expr> update) throws QueryException {
    return cc.get(focus, true, () -> {
      boolean changed = false;
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr step = update.apply(s);
        if(step != null && step != steps[s]) {
          steps[s] = step;
          changed = true;
        }
        cc.updateFocus(steps[s], true);
      }
      return changed;
    });
  }

  /**
   * Returns the focus of the first step: maps and arrays are converted to JNodes.
   * @param cc compilation context
   * @return focus expression (can be {@code null})
   * @throws QueryException query exception
   */
  private Expr focus(final CompileContext cc) throws QueryException {
    // function items other than maps and arrays will be rejected at runtime
    final Expr rt = cc.focusOr(root);
    return rt != null && rt.seqType().type.instanceOf(Types.FUNCTION) ?
      cc.function(Function.JTREE, info, rt) : rt;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // no root, no nesting: assign context value (can be null)
    if(root == null && !cc.nestedFocus()) root = cc.qc.focus.value;

    // remove redundant steps, find empty steps
    Expr expr = cleanSteps(cc);
    if(expr != this) return expr;

    // flatten nested path expressions
    expr = flatten(cc);
    // rewrite list to union expressions
    if(expr == this) expr = toUnion(cc);
    // merge adjacent steps
    if(expr == this) expr = mergeSteps(cc);
    // move predicates downward
    if(expr == this) expr = movePredicates();
    // return optimized expression
    if(expr != this) return expr.optimize(cc);

    // assign sequence type, compute result size
    final Expr rt = cc.focusOr(root);
    seqType(rt);

    // remove paths that will yield no result
    expr = removeEmpty(cc, rt);
    // rewrite to simple map
    if(expr == this) expr = toMap(cc);
    // check index access
    if(expr == this) expr = IndexRewrite.get(this, cc, rt);
    /* rewrite descendant to child steps. this optimization is called after the index rewritings,
     * as it is cheaper to invert a descendant step. examples:
     * - //B [. = '...'] → IA('...', B)
     * - /A/B[. = '...'] → IA('...', B)/parent::A *[parent::document-node()] */
    if(expr == this) expr = children(cc, rt);
    // split predicates with outer variables: R/a[$v] → (R/a)[$v]
    if(expr == this) expr = splitPredicates(cc);
    // return optimized expression
    if(expr != this) return expr;

    // choose the best path implementation (dummy will be used for type checking)
    final Expr input = root == null && rt instanceof Dummy ? rt : root;
    final Expr path = copyType(get(info, input, steps));
    if(path instanceof final AxisPath ap) {
      ap.optimize();
    } else if(path instanceof final MixedPath mp) {
      // a/(if (@x) then * else .): not checked when parsed, as steps may be incomplete
      mp.iterative = iterative(input, mp.steps);
    }
    return path;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // drop irrelevant predicate: if(a[1]) → if(a)
      expr = dropPredicate(cc);
      // merge nested predicates: if(a[b]) → if(a/b), if(a[b][c]) → if(a[b]/c)
      if(expr == this) expr = flattenEbv(true, cc);
    }
    if(expr == this && this instanceof final CachedPath cp && mode.oneOf(Simplify.EBV,
        Simplify.PREDICATE, Simplify.SET, Simplify.COUNT, Simplify.EXISTENCE) && unordered(cc)) {
      // skip sorting if order is irrelevant: count(preceding-sibling::a)
      expr = cp.copyCache(new UnorderedPath(info, root, steps));
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * Checks if the path yields distinct nodes when evaluated in axis order.
   * @param cc compilation context
   * @return result of check
   */
  private boolean unordered(final CompileContext cc) {
    final Expr rt = cc.focusOr(root);
    if(rt == null || !rt.seqType().zeroOrOne()) return false;
    final int sl = steps.length;
    for(int s = 1; s < sl; s++) {
      if(!((Step) steps[s]).axis.oneOf(CHILD, ATTRIBUTE, SELF)) return false;
    }
    return true;
  }

  /**
   * Removes the last predicate from the last step and returns the optimized expression.
   * An error will be raised if the last step is no XPath step.
   * @param cc compilation context
   * @return new path
   * @throws QueryException query exception
   */
  public final Expr removePredicate(final CompileContext cc) throws QueryException {
    // the type is not adopted: the path may yield more results without the predicate
    return withLastStep(((Step) steps[steps.length - 1]).removePredicate(), cc);
  }

  /**
   * Flattens the predicates of the last step for boolean evaluation.
   * @param ebv EBV check
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public final Expr flattenEbv(final boolean ebv, final CompileContext cc) throws QueryException {
    if(!(steps[steps.length - 1] instanceof final Step step)) return this;
    final Expr expr = step.flattenEbv(this, ebv, cc);
    return expr != step ? expr : this;
  }

  /**
   * Replaces the last step and returns the optimized path.
   * @param step new last step
   * @param cc compilation context
   * @return new path
   * @throws QueryException query exception
   */
  private Expr withLastStep(final Step step, final CompileContext cc) throws QueryException {
    final int sl = steps.length;
    // context of the last step: previous step or root
    final Expr ctx = sl == 1 ? root : steps[sl - 2];
    final Expr[] st = steps.clone();
    st[sl - 1] = cc.get(ctx, true, () -> step.optimize(cc));
    return get(cc, info, root, st);
  }

  /**
   * Splits predicates with free variables from the last step to make the remaining path cacheable.
   * @param cc compilation context
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr splitPredicates(final CompileContext cc) throws QueryException {
    // for $o in O return db:get('db')/a/b[@id > $o/@r] → (db:get('db')/a/b)[@id > $o/@r]
    final int last = steps.length - 1;
    if(!(root instanceof Value) || root instanceof Dummy ||
        !(steps[last] instanceof final Step step) || step.mayBePositional()) return this;

    final Expr[] preds = step.exprs;
    final int p = Checks.index(preds, Expr::hasFreeVars);
    if(p == -1) return this;
    for(int s = 0; s < last; s++) {
      if(steps[s].hasFreeVars()) return this;
    }

    final Expr path = withLastStep(step.rebuild(Arrays.copyOf(preds, p)), cc);
    if(!(path instanceof final AxisPath ap) || !ap.isSimple()) return this;
    return cc.replaceWith(this,
      Filter.split(cc, info, ap, Arrays.copyOfRange(preds, p, preds.length)));
  }

  /**
   * Removes a trailing predicate that does not influence the existence of results.
   * @param cc compilation context
   * @return original or optimized expression
   * @throws QueryException query exception
   */
  public final Expr dropPredicate(final CompileContext cc) throws QueryException {
    return steps[steps.length - 1] instanceof final Preds preds && preds.keepsResults() ?
      removePredicate(cc) : this;
  }

  @Override
  public final boolean has(final Flag... flags) {
    // Context dependency, positional access: only check root expression.
    // Examples: text(); ./abc; position()/a
    if(Flag.CTX.oneOf(flags) && (root == null || root.has(Flag.CTX)) ||
       Flag.POS.oneOf(flags) && root != null && root.has(Flag.POS)) return true;
    if(Flag.CNS.oneOf(flags) && seqType().type.intersect(NodeType.JNODE) != null) return true;
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
   * @return axis step, or {@code null}
   */
  public final Step axisStep(final int index) {
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

    // ($a/b)/c → $a/b/c
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
        // flatten nested path; keep non-node roots nested to preserve map/array coercion
        // $a/(b/c) → $a/b/c
        final Expr proot = path.root;
        final boolean rootStep = proot != null && !(proot instanceof ContextValue);
        if(!rootStep || proot.seqType().type instanceof NodeType) {
          if(rootStep) tmp.add(proot);
          final int pl = path.steps.length - 1;
          for(int i = 0; i < pl; i++) tmp.add(path.steps[i]);
          expr = path.steps[pl];
          cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) path::description, path);
          changed = true;
        }
      }
      tmp.add(expr);
    }
    return changed ? get(info, rt, tmp.finish()) : this;
  }

  /**
   * Removes empty results and redundant steps.
   * @param cc compilation context
   * @return original or optimized expression
   * @throws QueryException query exception
   */
  private Expr cleanSteps(final CompileContext cc) throws QueryException {
    // ()/a → ()
    if(root != null && root.seqType().zero()) return cc.emptySeq(this);

    // find empty results, remove redundant steps
    final int sl = steps.length;
    boolean removed = false;
    final ExprList list = new ExprList(sl);
    for(int s = 0; s < sl; s++) {
      Expr step = steps[s];
      final Expr prev = list.isEmpty() ? cc.focusOr(root) : list.peek();
      if(prev != null) {
        final SeqType seqType = prev.seqType();
        final Type pt = seqType.type;
        // $node/./a → $node/a, a/self::node()/b → a/b
        if(pt instanceof NodeType && (step instanceof ContextValue ||
            step instanceof final Step stp && stp.remove(seqType))) {
          removed = true;
          continue;
        }
        // $doc/following-or-self::x → $doc/self::x, $array/descendant::* → $array/child::*
        if(step instanceof final Step stp) {
          final Axis axis = stp.axis(seqType);
          if(axis != null && axis != stp.axis) {
            step = Step.get(cc, prev, stp.info(info), axis, stp.test, stp.selector, stp.exprs);
            cc.info(QueryText.OPTREWRITE_X_X, stp.axis, step);
          }
        }
        // $map/'X' → $map/child::{ 'X' }; skip context-dependent steps (e.g. $node/jkey())
        if(!(step instanceof Step) && step.seqType().instanceOf(Types.ANY_ATOMIC_TYPE_ZM) &&
            !step.has(Flag.CTX) &&
            (pt.instanceOf(NodeType.JNODE) || pt.instanceOf(Types.MAP_OR_ARRAY))) {
          step = new SelectorStep(step.info(info), CHILD, step).optimize(prev, cc);
        }
      }

      // $doc/() → ()
      if(step == Empty.VALUE) return cc.emptySeq(this);

      // add step to list
      list.add(step);

      // ignore remaining steps if step yields no results: A/void(.)/B → A/void(.)
      if(step.seqType().zero() && s + 1 < sl) {
        cc.info(QueryText.OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
        break;
      }
    }

    // self step was removed: ensure that result will be in distinct document order
    // $nodes/. → distinct-ordered-nodes($nodes)
    if(removed && (list.isEmpty() || !(list.get(0).seqType().type instanceof NodeType))) {
      if(root == null) root = ContextValue.get(cc, info);
      if(!root.ddo() && root.seqType().type instanceof NodeType) {
        root = cc.replaceWith(root, cc.function(Function.DISTINCT_ORDERED_NODES, info, root));
      }
    }

    // no steps left: return root
    steps = list.finish();
    return cc.replaceWith(this, steps.length == 0 ? root : this);
  }

  @Override
  public final boolean noMatches(final ArrayList<PathNode> nodes, final Data data)
      throws QueryException {
    // a[b]: relative path will yield no results
    if(root != null) return false;
    final ArrayList<PathNode> pn = PathNodes.get(this, nodes, false);
    return pn != null && pn.isEmpty();
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
        if(step instanceof final Step stp && stp.empty(prev.seqType())) return true;
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
  public static boolean iterative(final Expr root, final Expr... steps) {
    if(root == null || !root.ddo()) return false;

    final SeqType st = root.seqType();
    boolean atMostOne = st.zeroOrOne();
    boolean sameDepth = atMostOne || st.type.instanceOf(NodeType.DOCUMENT);
    // path nodes of the path summary (null: unknown)
    final Data data = root.data();
    ArrayList<PathNode> nodes = PathNodes.root(root, data, false);

    for(final Expr step : steps) {
      final Axis axis = axis(step);
      if(axis == null) return false;
      if(nodes != null) nodes = step instanceof final Step stp ? stp.nodes(nodes, false) : null;
      // results on the same level are non-overlapping and in document order
      final boolean level = nodes != null && PathNodes.sameLevel(nodes);
      switch(axis) {
        case ATTRIBUTE, SELF -> {
          // nothing changes
        }
        case PARENT, FOLLOWING_SIBLING, FOLLOWING_SIBLING_OR_SELF -> {
          // can overlap, preserves level
          if(!atMostOne) return false;
        }
        case CHILD, ITEM -> {
          // order is only ensured if all nodes are on the same level
          if(!sameDepth && !level) return false;
          sameDepth = true;
        }
        case DESCENDANT, DESCENDANT_OR_SELF -> {
          // non-overlapping if all nodes are on the same level
          if(!sameDepth) return false;
          sameDepth = level;
        }
        case ANCESTOR, ANCESTOR_OR_SELF, PRECEDING, PRECEDING_OR_SELF, PRECEDING_SIBLING,
             PRECEDING_SIBLING_OR_SELF -> {
          // backwards axes must be reordered
          return false;
        }
        case FOLLOWING, FOLLOWING_OR_SELF -> {
          // can overlap
          if(!atMostOne) return false;
          sameDepth = false;
        }
        default -> throw Util.notExpected();
      }
      atMostOne &= step.seqType().zeroOrOne();
      // results are distinct: at most one instance of the resulting path nodes may exist
      if(nodes != null && data.meta.counts) {
        long count = 0;
        for(final PathNode pn : nodes) count += pn.stats.count;
        if(count <= 1) atMostOne = true;
      }
    }
    return true;
  }

  /**
   * Returns an iterator that evaluates the steps lazily.
   * @param qc query context
   * @return iterator
   */
  final Iter lazyIter(final QueryContext qc) {
    return new Iter() {
      final int offset = root != null ? 1 : 0;
      final int sz = steps.length - 1 + offset;
      final Iter[] iter = new Iter[sz + 1];
      int pos;

      @Override
      public Item next() throws QueryException {
        if(iter[0] == null) iter[0] = (root != null ? root : steps[0]).iter(qc);

        final QueryFocus qf = qc.focus;
        final Value qv = qf.value;
        try {
          while(true) {
            final Item item = qc.next(iter[pos]);
            if(item == null) {
              if(--pos == -1) return null;
            } else if(pos < sz) {
              qf.value = item;
              pos++;
              iter[pos] = steps[pos - offset].iter(qc);
            } else {
              return item;
            }
          }
        } finally {
          qf.value = qv;
        }
      }
    };
  }

  /**
   * Returns the axis of a step, or the combined axis of a conditional step whose branches are
   * self, child and attribute steps.
   * @param step step
   * @return axis or {@code null}
   */
  private static Axis axis(final Expr step) {
    if(step instanceof final Step stp) return stp.axis;
    final Expr[] branches = switch(step) {
      case final If iff -> iff.exprs;
      case final Otherwise otherwise -> otherwise.exprs;
      case final Switch swtch -> swtch.branches();
      case final Typeswitch typeswitch -> typeswitch.branches();
      default -> null;
    };
    if(branches == null || step.has(Flag.POS)) return null;

    // examples: (if (@x) then * else .), (@x otherwise @y), (switch (@t) case 'a' return * ...)
    Axis axis = SELF;
    for(final Expr branch : branches) {
      if(branch == Empty.VALUE || branch instanceof ContextValue) continue;
      final Expr br = branch instanceof final Path path && path.root == null &&
        path.steps.length == 1 ? path.steps[0] : branch;
      final Axis ax = axis(br);
      if(ax == null || !ax.oneOf(SELF, CHILD, ATTRIBUTE)) return null;
      if(ax != SELF) axis = axis.oneOf(SELF, ax) ? ax : CHILD;
    }
    return axis;
  }

  /**
   * Assigns a sequence type and (if statically known) result size.
   * @param rt root at compile time (can be {@code null})
   */
  private void seqType(final Expr rt) {
    final Expr last = steps[steps.length - 1];
    final Data data = last.data();
    SeqType st = last.seqType();
    // consider JNodes
    final Expr prev = steps.length > 1 ? steps[steps.length - 2] : rt;
    if(st.type.instanceOf(BasicType.ANY_ATOMIC_TYPE) &&
        (prev == null || !prev.seqType().type.instanceOf(NodeType.XNODE))) {
      st = NodeType.JNODE.seqType();
    }
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
      if(size == -1 && steps.length == 1 && steps[0] instanceof final Step step) {
        size = step.size(rt);
      }
    }
    exprType.assign(st, occ, size).data(data);
  }

  /**
   * Computes the result size via database statistics.
   * @param rt root at compile time (can be {@code null})
   * @param data data reference (can be {@code null})
   * @return number of results (or {@code -1})
   */
  private long size(final Expr rt, final Data data) {
    // check if a step will yield no results: A/void(.)
    for(final Expr step : steps) {
      if(step.size() == 0) return 0;
    }

    // skip computation if:
    // - path does not start with document nodes,
    // - no database instance is available, outdated, or
    // - if context does not contain all database nodes
    if(rt == null || !rt.seqType().type.instanceOf(NodeType.DOCUMENT) ||
        data == null || !data.meta.counts || data.meta.ndocs != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths().root();
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
   * Returns an empty sequence if the path will yield no results.
   * @param cc compilation context
   * @param rt root at compile time (can be {@code null})
   * @return original or new expression
   * @throws QueryException query exception
   */
  private Expr removeEmpty(final CompileContext cc, final Expr rt) throws QueryException {
    final ArrayList<PathNode> rn = PathNodes.root(rt, data(), false);
    final ArrayList<PathNode> nodes = rn != null ? PathNodes.get(this, rn, false) : null;
    // $doc/NON-EXISTING/PATH → ()
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
    // skip optimization if the path summary is not available for the root nodes,
    // or if the root nodes are located on different levels
    final Data data = data();
    ArrayList<PathNode> nodes = PathNodes.root(rt, data, false);
    if(nodes == null || nodes.isEmpty() || !PathNodes.sameLevel(nodes)) return this;
    final int rootLevel = nodes.getFirst().level();

    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.exprs.length != 0) break;

      // follow child and descendant steps with name tests
      final Step curr = axisStep(s);
      if(curr == null || !curr.axis.oneOf(CHILD, DESCENDANT) || !(curr.test instanceof NameTest))
        break;
      nodes = curr.nodes(nodes, false);
      // stop if no elements are found, or if they occur on different levels
      if(nodes == null || nodes.isEmpty() || !PathNodes.sameLevel(nodes)) break;

      // ignore axes other than descendant, or numeric predicates
      if(curr.axis != DESCENDANT || curr.mayBePositional()) continue;

      // collect the tests of the child steps
      final ArrayList<Test> tests = new ArrayList<>();
      for(ArrayList<PathNode> pn = nodes; pn.getFirst().level() > rootLevel;
          pn = PathIndex.parent(pn)) {
        final Test test = test(pn, data);
        if(test == null) return this;
        tests.add(test);
      }
      // $doc//c → $doc/a/b/c  (path summary)
      cc.info(QueryText.OPTCHILD_X, steps[s]);

      // build new steps
      int ts = tests.size();
      final Expr[] stps = new Expr[ts + sl - s - 1];
      for(int t = 0; t < ts; t++) {
        final Expr[] preds = t == ts - 1 ? ((Preds) steps[s]).exprs : new Expr[0];
        stps[t] = Step.get(cc, root, curr.info(), CHILD, tests.get(ts - t - 1), preds);
      }
      while(++s < sl) stps[ts++] = steps[s];

      return get(cc, info, root, stps);
    }
    return this;
  }

  /**
   * Returns a test for the elements of the specified path nodes.
   * @param nodes path nodes
   * @param data data reference
   * @return test, or {@code null} if the namespace of the name cannot be resolved
   */
  private static Test test(final ArrayList<PathNode> nodes, final Data data) {
    // different names: wildcard test
    final int name = nodes.getFirst().name;
    if(!Checks.all(nodes, node -> node.name == name)) return NodeTest.ELEMENT;
    final byte[] key = data.elemNames.key(name), uri = data.nsUri(key, true);
    return uri == null ? null :
      Test.get(Kind.ELEMENT, new QNm(Token.local(key), uri), Scope.FULL, null);
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
     * - previous expression yields XML nodes (otherwise, an error must be raised at runtime)
     * - last expression is no step, and yields a single result or no node */
    if(!type1.instanceOf(NodeType.XNODE) || s2 instanceof Step || size() != 1 &&
       !type2.instanceOf(BasicType.ANY_ATOMIC_TYPE) && !type2.instanceOf(Types.FUNCTION))
      return this;

    /* remove last step from new root expression. examples:
     * - (<a/>, <b/>)/map { name(): . } → (<a/>, <b/>) ! map { name(): . }
     * - <a/>/<b/> → <a/> ! <b/>
     * - $a/b/string → $a/b ! string() */
    if(sl > 1) s1 = get(cc, info, root, Arrays.copyOfRange(steps, 0, sl - 1));
    return cc.replaceWith(this, SimpleMap.get(cc, info, s1, s2));
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
      // (a, b)/c → (a | b)/c
      if(step instanceof final List lst) return lst.toUnion(cc);
      // (a, b)[c] → (a | b)[c]
      if(step instanceof final Filter fltr && !fltr.mayBePositional() &&
          fltr.root instanceof final List lst) {
        final Expr st = lst.toUnion(cc);
        if(st != fltr.root) return Filter.get(cc, fltr.info(), st, fltr.exprs);
      }
      // replicate(a, 2)/b → a/b
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

    final int sl = steps.length;
    changed |= updateSteps(root, cc,
      s -> rewrite.apply(steps[s], s + 1 < sl ? steps[s + 1] : null));

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
    return cc.get(root, true, () -> {
      boolean chngd = false;
      for(int s = 0; s < sl; s++) {
        Expr curr = steps[s];
        if(curr instanceof final Step crr && s < sl - 1) {
          final Expr[] merged = mergeStep(crr, steps[s + 1], cc);
          if(merged != null) {
            if(merged.length == 1) {
              // both steps have been merged: //* → /descendant::*
              cc.info(QueryText.OPTMERGE_X, merged[0]);
              curr = merged[0];
              s++;
            } else {
              // current step has been narrowed: node()/@* → */@*
              curr = cc.replaceWith(curr, merged[0]);
            }
            chngd = true;
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
   * @return original or new expression
   */
  private Expr movePredicates() {
    // examples:
    // a[b]/b     → a/b
    // a[b]/b/c   → a/b/c
    // a[b/c]/b/c → a/b/c
    final int sl = steps.length;
    for(int s = 0; s < sl; s++) {
      if(steps[s] instanceof Step) {
        final Expr ex = movePredicates(s);
        if(ex != null) return ex;
      }
    }
    return this;
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
    exprs[s] = step.copyType(step.rebuild());
    return get(info, root, exprs);
  }

  /**
   * Merges adjacent steps.
   * @param curr current step
   * @param next next expression
   * @param cc compilation context
   * @return single merged step, current step followed by narrowed next step, or {@code null}
   * @throws QueryException query exception
   */
  private static Expr[] mergeStep(final Step curr, final Expr next, final CompileContext cc)
      throws QueryException {

    // do not merge if current step contains positional predicates
    if(curr.mayBePositional()) return null;

    final Step nxt = next instanceof final Step stp ? stp : null;

    // narrow node test before attribute step:  node()/@* → */@*
    if(nxt != null && nxt.axis == ATTRIBUTE && curr.selector == null &&
        curr.test == NodeTest.XNODE) {
      return new Expr[] {
        Step.get(cc, null, curr.info(), curr.axis, NodeTest.ELEMENT, curr.exprs), next };
    }

    // merge self steps:  child::*/self::a → child::a
    if(nxt != null && nxt.axis == SELF && !nxt.mayBePositional()) {
      final Test test = curr.test.intersect(nxt.test);
      // an intersection may be less specific than the merged tests (e.g., for records)
      if(test == null || !test.instanceOf(curr.test) || !test.instanceOf(nxt.test)) return null;
      final Expr cs = curr.selector, ns = nxt.selector;
      // two selectors cannot be merged into a single step
      if(cs != null && ns != null) return null;
      return new Expr[] { Step.get(cc, null, curr.info(), curr.axis, test, cs != null ? cs : ns,
          ExprList.concat(curr.exprs, nxt.exprs)) };
    }

    // merge descendant-or-self step
    if(curr.axis != DESCENDANT_OR_SELF || curr.exprs.length > 0 || curr.selector != null ||
        curr.test != NodeTest.XNODE && curr.test != NodeTest.JNODE &&
        curr.test != NodeTest.NODE) return null;

    // examples:
    // - descendant-or-self::node()/* → descendant::*
    // - descendant-or-self::node()/descendant::* → descendant::*
    // - descendant-or-self::node()/descendant-or-self::* → descendant-or-self::*
    final Axis merged = mergedAxis(nxt);
    if(merged != null) return new Expr[] {
      Step.get(cc, null, nxt.info(), merged, nxt.test, nxt.selector, nxt.exprs) };

    // function for distributing the step over the operands of a union expression
    final QueryFunction<Expr, Expr> rewrite = expr -> {
      if(!(expr instanceof final Union union)) return null;
      final Expr[] args = union.args();
      final int al = args.length;
      final Expr[] branches = new Expr[al];
      for(int a = 0; a < al; a++) {
        // reject operands that cannot absorb or narrow the step
        if(!(args[a] instanceof final Path path) || path.root != null ||
            !(path.steps[0] instanceof final Step stp)) return null;
        final Expr[] mrgd = mergeStep(curr, stp, cc);
        if(mrgd == null) return null;
        final Expr[] stps = path.steps;
        final ExprList list = new ExprList(stps.length + mrgd.length - 1).add(mrgd);
        for(int t = 1; t < stps.length; t++) list.add(stps[t]);
        branches[a] = Path.get(cc, path.info(), null, list.finish());
      }
      return new Union(union.info(), branches).optimize(cc);
    };
    // descendant-or-self::node()/(* | text() | @*)
    //   → (descendant::* | descendant::text() | descendant-or-self::*/@*)
    if(next instanceof Union) {
      final Expr expr = rewrite.apply(next);
      if(expr != null) return new Expr[] { expr };
    }

    // descendant-or-self::node()/(text()|*)[..] → (descendant::text() | descendant::*)[..]
    if(next instanceof final Filter filter && !filter.mayBePositional()) {
      final Expr expr = rewrite.apply(filter.root);
      if(expr != null) return new Expr[] { Filter.get(cc, filter.info(), expr, filter.exprs) };
    }
    return null;
  }

  /**
   * Returns a merged axis for a step preceded by a descendant-or-self step.
   * @param expr step to test
   * @return axis or {@code null}
   */
  private static Axis mergedAxis(final Expr expr) {
    if(expr instanceof final Step step) {
      final Axis axis = step.axis;
      if(!step.mayBePositional()) {
        if(axis.oneOf(CHILD, DESCENDANT)) return DESCENDANT;
        if(axis == DESCENDANT_OR_SELF) return DESCENDANT_OR_SELF;
      }
    }
    return null;
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    // steps are only inlined into if a variable is replaced (see #inline)
    if(ic.var != null) {
      final boolean ctx = ic.expr.has(Flag.CTX);
      for(final Expr step : steps) {
        // do not replace $v with .: E/$v
        if(ctx && step.uses(ic.var) || !step.inlineable(ic)) return false;
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
    if(!changed && ic.var == null) return null;
    final CompileContext cc = ic.cc;
    final Expr focus = focus(cc);
    if(changed) updateSteps(focus, cc, s -> steps[s].optimize(cc));
    changed |= ic.var != null && updateSteps(focus, cc, s -> steps[s].inline(ic));

    return changed ? optimize(cc) : null;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(root == null) {
      if(!visitor.lock(Locking.CONTEXT, false)) return false;
    } else if(!root.accept(visitor)) {
      return false;
    }
    visitor.enterFocus();
    final boolean more = visitAll(visitor, steps);
    visitor.exitFocus();
    return more;
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
        Arrays.equals(steps, path.steps);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), root, steps);
  }

  @Override
  public void toString(final QueryString qs) {
    if(root != null) qs.token(root).token('/');
    qs.tokens(steps, "/");
  }
}
