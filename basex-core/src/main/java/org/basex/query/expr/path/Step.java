package org.basex.query.expr.path;

import static org.basex.query.expr.path.Axis.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.Function;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract axis step expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Step extends Preds {
  /** Kind test. */
  public Test test;
  /** Axis. */
  public Axis axis;
  /** Selector ({@code null} for a static step). */
  public Expr selector;

  /**
   * Returns a new optimized self::node() step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param info input info (can be {@code null})
   * @param preds predicates
   * @return step or empty sequence
   * @throws QueryException query exception
   */
  public static Expr self(final CompileContext cc, final Expr root, final InputInfo info,
      final Expr... preds) throws QueryException {
    return self(cc, root, info, NodeTest.NODE, preds);
  }

  /**
   * Returns a new optimized self step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param info input info (can be {@code null})
   * @param test test
   * @param preds predicates
   * @return step or empty sequence
   * @throws QueryException query exception
   */
  public static Expr self(final CompileContext cc, final Expr root, final InputInfo info,
      final Test test, final Expr... preds) throws QueryException {
    return get(cc, root, info, SELF, test, preds);
  }

  /**
   * Returns a new optimized step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test test
   * @param preds predicates
   * @return step or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo info,
      final Axis axis, final Test test, final Expr... preds) throws QueryException {
    return new CachedStep(info, axis, test, preds).optimize(root, cc);
  }

  /**
   * Returns a new optimized step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test test
   * @param selector selector (can be {@code null})
   * @param preds predicates
   * @return step or empty sequence
   * @throws QueryException query exception
   */
  static Expr get(final CompileContext cc, final Expr root, final InputInfo info, final Axis axis,
      final Test test, final Expr selector, final Expr[] preds) throws QueryException {
    return (selector != null ? new SelectorStep(info, axis, test, selector, preds) :
      new CachedStep(info, axis, test, preds)).optimize(root, cc);
  }

  /**
   * Returns a new step.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test test
   * @param preds predicates
   * @return step
   */
  public static Step get(final InputInfo info, final Axis axis, final Test test,
      final Expr... preds) {

    // optimize single last() functions
    if(preds.length == 1 && preds[0] instanceof final Pos pos && Function.LAST.is(pos.expr))
      return new IterLastStep(info, axis, test, preds);

    // check for simple positional predicates
    boolean pos = false;
    for(final Expr pred : preds) {
      if(pred instanceof CmpPos) {
        // predicate is known to be a positional check; can be optimized
        pos = true;
      } else if(mayBePositional(pred)) {
        // predicate **may** be positional: choose cached evaluation
        return new CachedStep(info, axis, test, preds);
      }
    }
    return pos ?
      new IterPosStep(info, axis, test, preds) :
      new IterStep(info, axis, test, preds);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test test
   * @param preds predicates
   */
  Step(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, seqType(axis, test, preds), preds);
    this.axis = axis;
    this.test = test;
  }

  /**
   * Returns an axis iterator.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  final BasicNodeIter iterator(final QueryContext qc) throws QueryException {
    return axis.iter(toContextNode(qc.focus.value), test, info);
  }

  /**
   * Filters the cached result nodes with the predicates.
   * @param list result nodes (will be modified)
   * @param qc query context
   * @return iterator over the resulting nodes
   * @throws QueryException query exception
   */
  final BasicNodeIter preds(final GNodeList list, final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      for(final Expr expr : exprs) {
        final long ns = list.size();
        qf.size = ns;
        int c = 0;
        for(int p = 1; p <= ns; p++) {
          final GNode node = list.get(p - 1);
          qf.value = node;
          qf.pos = p;
          if(expr.predicate(qc, info, p)) list.set(c++, node);
        }
        list.size(c);
      }
    } finally {
      qc.focus = focus;
    }
    return list.clean().iter();
  }

  @Override
  public final boolean navigational() {
    return true;
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    if(selector != null) selector = cc.get(null, false, () -> selector.compile(cc));
    return super.compile(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(null, cc);
  }

  /**
   * Optimizes the step for the given root expression.
   * @param root root context expression; if {@code null}, the current context will be used
   * @param cc compilation context
   * @return optimized step or empty sequence
   * @throws QueryException query exception
   */
  Expr optimize(final Expr root, final CompileContext cc) throws QueryException {
    // updates the static type
    final Expr rt = assignType(cc.focusOr(root));
    final NodeType rnt = rt != null ? nodeType(rt.seqType().type) : NodeType.NODE;
    final Type rtype = rnt != null ? rnt : rt.seqType().type;

    // choose stricter axis
    final Axis old = axis;
    final Type type = seqType().type;
    if(type instanceof final NodeType tnt &&
        (old == DESCENDANT_OR_SELF && tnt.root() ||
         old == ANCESTOR_OR_SELF && tnt.kind().leaf())) {
      // descendant-or-self::document-node() → self::document-node()
      // ancestor-or-self::text() → self::text()
      axis = SELF;
    } else if(rnt != null && rnt.intersect(type) == null) {
      // root()/descendant-or-self::x → root()/descendant::x
      if(old == DESCENDANT_OR_SELF) axis = DESCENDANT;
      // $text/ancestor-or-self::x → $text/ancestor::x
      else if(old == ANCESTOR_OR_SELF) axis = ANCESTOR;
    }
    if(axis != old) cc.info(QueryText.OPTREWRITE_X_X, old, this);

    // optimize test; check if it will never accept results
    final Test t = test.optimize(rtype.kind(), data());
    if(t != null && t != test) {
      test = t;
      exprType.assign(NodeType.get(t));
    }
    // $jnode/attribute::x: type error at runtime
    final boolean typeError = axis == ATTRIBUTE && rtype.seqType().mayBeJNode();
    // attribute::element() → (), parent::comment() → ()
    if(!typeError && !selectorMayFail() && (t == null || noMatches() || seqType().zero())) {
      cc.info(QueryText.OPTSTEP_X, this);
      return cc.emptySeq(this);
    }

    // a[false()] → ()
    if(optimize(cc, this)) return cc.emptySeq(this);

    // performed after predicate optimization: adopt final sequence type if it is more specific
    if(seqType().type instanceof final NodeType nt && nt.test == null) {
      final NodeType tt = NodeType.get(test);
      if(nt.instanceOf(tt)) test = NodeTest.get(nt.kind());
    }
    // choose best implementation
    return copyType(rebuild(exprs));
  }

  @Override
  public final Expr assignType(final Expr expr) {
    final Type type = expr != null ? expr.seqType().type : NodeType.NODE;
    test = test.optimize(type.kind(), null);

    SeqType st = seqType(axis, test, exprs);
    final NodeType ct = nodeType(type);
    if(axis == CHILD && ct != null && ct.test instanceof final JNodeTest pt) {
      // child JNodes: adopt the value type of the map or array: jtree({ 'a': 1 })/a
      final SeqType mvt = pt.childType();
      if(mvt != null) {
        final Test it = test.intersect(JNodeTest.get(null, mvt));
        if(it instanceof final JNodeTest jt) {
          if(jt.key != Empty.VALUE) st = NodeType.get(jt).seqType(st.occ);
        } else if(it == null && test instanceof JNodeTest) {
          // no results: { 'a': 1 }/jnode(a, xs:string)
          st = Types.EMPTY_SEQUENCE_Z;
        }
      }
    }
    if(expr != null && axis == SELF && ct != null) {
      // node test: adopt type of context expression: <a/>/self::node()
      if(test == NodeTest.NODE) st = ct.seqType(st.occ);
      // no predicates: step will yield single result: $elements/self::element()
      if(exprs.length == 0 && selector == null && test.subsumes(ct) == Boolean.TRUE)
        st = st.with(Occ.EXACTLY_ONE);
    }
    exprType.assign(st).data(expr);
    return expr;
  }

  /**
   * Determines the sequence type of the step.
   * @param axis axis
   * @param test test
   * @param preds predicates
   * @return sequence type
   */
  private static SeqType seqType(final Axis axis, final Test test, final Expr... preds) {
    final Occ occ = axis == ATTRIBUTE && test.subsumes(NodeType.ATTRIBUTE) == Boolean.FALSE
      // no results: attribute::element()
      ? Occ.ZERO :
        axis.oneOf(SELF, PARENT) ||
        axis == ATTRIBUTE && test instanceof final NameTest nt && nt.scope == NameTest.Scope.FULL ||
        preds.length == 1 && preds[0] instanceof final CmpPos cp && cp.exact()
      // zero or one result: self::X, parent::X, attribute::Q{uri}local, ...[position() = n]
      ? Occ.ZERO_OR_ONE
      : Occ.ZERO_OR_MORE;
    return SeqType.get(NodeType.get(test), occ);
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    // do not inline context value
    if(ic.var == null) return null;

    final Expr s = selector == null ? null :
      ic.cc.get(null, false, () -> ic.inlineOrNull(selector));
    if(s != null) selector = s;
    // predicates are guarded by the existence of a node
    final boolean preds = ic.cc.get(this, true, () -> ic.inline(exprs, true));
    return s != null || preds ? optimize(ic.cc) : null;
  }

  @Override
  public abstract Step copy(CompileContext cc, IntObjectMap<Var> vm);

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param stats assess database statistics; if {@code true}, return early if the step has
   *   predicates or if its axis does not yield all instances of the resulting path nodes
   * @return path nodes, or {@code null} if nodes cannot be collected
   */
  public final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final boolean stats) {
    // skip steps with selectors and predicates
    final Data data = data();
    if(data == null || selector != null || stats && exprs.length != 0) return null;

    // skip axes that cannot be traversed in the path summary
    if(!axis.down && (stats || !axis.oneOf(PARENT, ANCESTOR, ANCESTOR_OR_SELF, FOLLOWING_SIBLING,
        FOLLOWING_SIBLING_OR_SELF, PRECEDING_SIBLING, PRECEDING_SIBLING_OR_SELF))) return null;

    // skip processing instructions
    final Kind kind = test.kind;
    if(kind.oneOf(Kind.NODE, Kind.PROCESSING_INSTRUCTION)) return null;

    final Names names = kind == Kind.ATTRIBUTE ? data.attrNames : data.elemNames;
    final int kn = XNode.dbKind(kind);
    final ArrayList<PathNode> tmp = new ArrayList<>();
    final Predicate<Test> addNodes = t -> {
      int name = 0;
      if(t instanceof final NameTest nt) {
        final byte[] nm = nt.dbName();
        name = nm != null ? names.index(nm) : 0;
        if(name == 0) return false;
      }
      for(final PathNode pn : nodes) {
        switch(axis) {
          case SELF -> add(pn, tmp, name, kn, true);
          case PARENT -> add(pn.parent, tmp, name, kn, false);
          case ANCESTOR, ANCESTOR_OR_SELF -> {
            for(PathNode p = axis == ANCESTOR ? pn.parent : pn; p != null; p = p.parent) {
              add(p, tmp, name, kn, p == pn);
            }
          }
          case FOLLOWING_SIBLING, FOLLOWING_SIBLING_OR_SELF,
               PRECEDING_SIBLING, PRECEDING_SIBLING_OR_SELF -> {
            if(axis.oneOf(FOLLOWING_SIBLING_OR_SELF, PRECEDING_SIBLING_OR_SELF)) {
              add(pn, tmp, name, kn, true);
            }
            if(pn.parent != null && pn.kind != Data.ATTR) {
              for(final PathNode p : pn.parent.children) add(p, tmp, name, kn, false);
            }
          }
          default -> {
            if(axis == DESCENDANT_OR_SELF) add(pn, tmp, name, kn, true);
            desc(pn, tmp, name, kn);
          }
        }
      }
      return true;
    };

    // add nodes
    if(test instanceof final UnionTest ut) {
      for(final Test t : ut.tests) {
        if(!addNodes.test(t)) return null;
      }
    } else if(!addNodes.test(test)) {
      return null;
    }
    return tmp;
  }

  /**
   * Adds the children or descendants of a path node to the list if they comply with the given
   * test conditions.
   * @param node root node
   * @param nodes output nodes
   * @param name name ID, or {@code 0} as wildcard
   * @param kind node kind, or {@code -1} for all types
   */
  private void desc(final PathNode node, final ArrayList<PathNode> nodes, final int name,
      final int kind) {

    for(final PathNode pn : node.children) {
      if(axis.oneOf(DESCENDANT, DESCENDANT_OR_SELF)) desc(pn, nodes, name, kind);
      add(pn, nodes, name, kind, false);
    }
  }

  /**
   * Adds a path node to the list if it complies with the given test conditions.
   * @param node path node (can be {@code null})
   * @param nodes output nodes
   * @param name name ID, or {@code 0} as wildcard
   * @param kind node kind, or {@code -1} for all types
   * @param self node is the context node
   */
  private void add(final PathNode node, final ArrayList<PathNode> nodes, final int name,
      final int kind, final boolean self) {

    if(node == null) return;
    final boolean ok = kind == -1 ? self || (node.kind == Data.ATTR) == (axis == ATTRIBUTE) :
      kind == node.kind && (name == 0 || name == node.name);
    if(ok && !nodes.contains(node)) nodes.add(node);
  }

  /**
   * Checks if the step will never yield results.
   * @return {@code true} if steps will never yield results
   */
  private boolean noMatches() {
    final Kind kind = test.kind;
    if(kind.oneOf(Kind.NODE, Kind.XNODE)) return false;

    return switch(axis) {
      // attribute::element()
      case ATTRIBUTE ->
        kind != Kind.ATTRIBUTE;
      // parent::comment()
      case ANCESTOR, PARENT ->
        kind.leaf();
      // child::attribute()
      case CHILD, DESCENDANT, FOLLOWING, FOLLOWING_SIBLING, PRECEDING, PRECEDING_SIBLING ->
        kind.oneOf(Kind.ATTRIBUTE, Kind.DOCUMENT, Kind.NAMESPACE);
      default ->
        false;
    };
  }

  /**
   * Checks if the step will never yield results.
   * @param seqType type of input nodes
   * @return {@code true} if steps will never yield results
   */
  final boolean empty(final SeqType seqType) {
    final NodeType nt = nodeType(seqType.type);
    if(nt == null || selectorMayFail()) return false;

    // $doc/.. → (), $array/following::* → (), $text/child::x → (), [ 1 ]/*/* → ()
    final Axis ax = axis(nt);
    if(ax == null) return true;

    final Kind kind = test.kind, nk = nt.kind();
    return switch(ax) {
      // $element/self::text(), $text/descendant-or-self::comment(), ...
      case SELF ->
        test.subsumes(nt) == Boolean.FALSE;
      // $doc/child::document-node(), $doc/descendant::attribute()
      case CHILD, DESCENDANT ->
        nk == Kind.DOCUMENT && kind.oneOf(Kind.DOCUMENT, Kind.ATTRIBUTE);
      // $doc/descendant-or-self::attribute()
      case DESCENDANT_OR_SELF ->
        nk == Kind.DOCUMENT && kind == Kind.ATTRIBUTE;
      // $doc/attribute::, $comment/attribute:: ($jnode/attribute::: type error at runtime)
      case ATTRIBUTE ->
        nk == Kind.DOCUMENT || nk.leaf();
      // $attribute/following-sibling::, $attribute/preceding-sibling::
      case FOLLOWING_SIBLING, PRECEDING_SIBLING ->
        nk == Kind.ATTRIBUTE;
      // $attribute/parent::document-node()
      case PARENT ->
        nk == Kind.ATTRIBUTE && kind == Kind.DOCUMENT;
      default ->
        false;
    };
  }

  /**
   * Checks if the step has a selector that may raise an error.
   * @return result of check
   */
  private boolean selectorMayFail() {
    return selector != null && !(selector instanceof Value);
  }

  /**
   * Checks if the step is redundant.
   * @param seqType type of incoming nodes
   * @return {@code true} if the step can be removed
   */
  final boolean remove(final SeqType seqType) {
    // <xml/>/. → <xml/>
    // <xml/>/self::node() → <xml/>
    // $text/descendant-or-self::text() → $text
    // $doc/ancestor-or-self::node() → $doc
    return exprs.length == 0 && selector == null && axis(seqType) == SELF &&
      test.subsumes(seqType.type) == Boolean.TRUE;
  }

  /**
   * Returns the result size of the step for the specified input.
   * @param input input expression
   * @return result size, or {@code -1} if it is unknown
   */
  final long size(final Expr input) {
    final long ss = input.structSize();
    if(ss == -1 || selector != null) return -1;
    long count = -1;
    if(axis == CHILD) {
      // array { 1 to 3 }/* → 3
      if(test == NodeTest.JNODE) {
        count = ss;
      } else if(test instanceof final JNodeTest jt && jt.key instanceof final Itr key &&
          jt.valueType.eq(Types.ITEM_ZM) && input.seqType().instanceOf(Types.ARRAY_O)) {
        // array { 1 to 3 }/2 → 1
        count = key.itr() >= 1 && key.itr() <= ss ? 1 : 0;
      }
    } else if(axis == DESCENDANT_OR_SELF && test == NodeTest.JNODE &&
        nodeType(input.seqType().type) instanceof final NodeType nt && nt.leafChildren()) {
      // array { 1 to 3 }/descendant-or-self::* → 4
      count = ss + 1;
    }
    // array { 1 to 3 }/*[1] → 1
    return count == -1 ? -1 : size(count);
  }

  /**
   * Returns the node type of the input; maps and arrays are converted to root JNodes.
   * @param type input type
   * @return node type, or {@code null} if the input yields no nodes
   */
  private static NodeType nodeType(final Type type) {
    return type.instanceOf(Types.MAP_OR_ARRAY) ? NodeType.get(Empty.VALUE, type.seqType()) :
      type instanceof final NodeType nt ? nt : null;
  }

  /**
   * Returns the axis that is equivalent to the step axis for the specified input.
   * @param nt input type (can be {@code null})
   * @return equivalent axis, or {@code null} if no nodes will be returned
   */
  private Axis axis(final NodeType nt) {
    if(nt == null) return axis;
    Axis ax = nt.root() ? axis.onRoot() : axis;
    if(ax != null && nt.leaf()) ax = ax.onLeaf();
    // array { 1 to 3 }/descendant::* → array { 1 to 3 }/child::*
    return ax == DESCENDANT && nt.leafChildren() ? CHILD : ax;
  }

  /**
   * Returns the axis that is equivalent to the step axis for the specified input.
   * @param seqType input type
   * @return equivalent axis, or {@code null} if no nodes will be returned
   */
  final Axis axis(final SeqType seqType) {
    return axis(nodeType(seqType.type));
  }

  /**
   * Creates a new step of the same kind with the given predicates.
   * @param preds predicates
   * @return step
   */
  final Step rebuild(final Expr... preds) {
    return selector != null ? new SelectorStep(info, axis, test, selector, preds) :
      get(info, axis, test, preds);
  }

  /**
   * Adds predicates to the step.
   * After the call, a new instance of the resulting path must be created.
   * @param preds predicates to be added
   * @return resulting step instance
   */
  public final Step addPredicates(final Expr... preds) {
    exprType.assign(seqType().union(Occ.ZERO));
    return copyType(rebuild(ExprList.concat(exprs, preds)));
  }

  /**
   * Removes the last predicate from the step.
   * After the call, a new instance of the resulting path must be created.
   * @return resulting step instance
   */
  final Step removePredicate() {
    return rebuild(Arrays.copyOfRange(exprs, 0, exprs.length - 1));
  }

  @Override
  public final boolean has(final Flag... flags) {
    return selector != null && selector.has(flags) || super.has(flags);
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    return (selector == null || selector.inlineable(ic)) && super.inlineable(ic);
  }

  @Override
  public final VarUsage count(final Var var) {
    final VarUsage vu = super.count(var);
    return selector != null ? selector.count(var).plus(vu) : vu;
  }

  @Override
  public final int exprSize() {
    return (selector != null ? selector.exprSize() : 0) + super.exprSize();
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(selector != null && !selector.accept(visitor)) return false;
    for(final Expr pred : exprs) {
      visitor.enterFocus();
      final boolean more = pred.accept(visitor);
      visitor.exitFocus();
      if(!more) return false;
    }
    return true;
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final Step st && axis == st.axis && test.equals(st.test) &&
        Objects.equals(selector, st.selector) && super.equals(obj);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.AXIS, axis.name, QueryText.TEST,
        test.toString(false)), selector, exprs);
  }

  @Override
  protected final void rootToString(final QueryString qs) {
    if(selector != null) {
      qs.token(axis.name + "::").token("{").token(selector).token("}");
      return;
    }
    final TokenBuilder tb = new TokenBuilder();
    if(test == NodeTest.XNODE) {
      if(axis == PARENT) tb.add("..");
      if(axis == SELF) tb.add('.');
    }
    if(tb.isEmpty()) {
      final java.util.function.Function<Test, TokenBuilder> add = t -> {
        if(axis == ATTRIBUTE) {
          if(t instanceof NodeTest) return tb.add('@').add('*');
          if(t instanceof final NameTest nt) return tb.add('@').add(nt.nameString());
        }
        if(axis != CHILD) tb.add(axis).add("::");
        return tb.add(t.toString(false));
      };
      if(test instanceof final UnionTest ut) {
        tb.add('(');
        for(final Test t : ut.tests) add.apply(t).add(" | ");
        tb.delete(tb.size() - 3, tb.size()).add(')');
      } else {
        add.apply(test);
      }
    }
    qs.token(tb.finish());
  }
}
