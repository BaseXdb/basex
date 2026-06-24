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
import org.basex.query.value.node.*;
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
    super(info, seqType(axis, test, null, preds), preds);
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
    return new BasicNodeIter() {
      final BasicNodeIter iter = axis.iter(toContextNode(qc.focus.value), test);

      @Override
      public GNode next() {
        for(GNode node; (node = iter.next()) != null;) {
          qc.checkStop();
          if(test.matches(node)) return node;
        }
        return null;
      }
    };
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
          if(expr.test(qc, info, p)) list.set(c++, node);
        }
        list.size(c);
      }
    } finally {
      qc.focus = focus;
    }
    return list.clean().iter();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    if(selector != null) selector = selector.compile(cc);
    return super.compile(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(null, cc);
  }

  /**
   * Optimizes the step for the given root expression.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @return optimized step or empty sequence
   * @throws QueryException query exception
   */
  Expr optimize(final Expr root, final CompileContext cc) throws QueryException {
    // updates the static type
    final Expr rt = assignType(root != null ? root : cc.qc.focus.value);
    Type rtype = rt != null ? rt.seqType().type : NodeType.GNODE;
    if(rtype.instanceOf(Types.MAP_OR_ARRAY)) rtype = NodeType.JNODE;

    // choose stricter axis
    final Axis old = axis;
    final Type type = seqType().type;
    if(old == DESCENDANT_OR_SELF && type.instanceOf(NodeType.DOCUMENT) ||
       old == ANCESTOR_OR_SELF && isLeaf(type.kind())) {
      // descendant-or-self::document-node() → self::document-node()
      // ancestor-or-self::text() → self::text()
      axis = SELF;
    } else if(rtype instanceof NodeType && rtype.seqType().type.intersect(type) == null) {
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
    if(t == null || noMatches()) {
      cc.info(QueryText.OPTSTEP_X, this);
      return cc.emptySeq(this);
    }

    // optimize predicates
    if(optimize(cc, this)) return cc.emptySeq(this);

    // performed after predicate optimization: adopt final sequence type if it is more specific
    if(seqType().type instanceof final NodeType nt) {
      if(nt.test != null) {
        if(nt.test.instanceOf(test) && !nt.test.equals(test)) test = nt.test;
      } else {
        final NodeType tt = NodeType.get(test);
        if(nt.instanceOf(tt)) test = NodeTest.get(nt.kind());
      }
    }
    // choose best implementation
    return copyType(rebuild(exprs));
  }

  @Override
  public final Expr assignType(final Expr expr) {
    final Type type = expr != null ? expr.seqType().type : NodeType.GNODE;
    test = test.optimize(type.kind(), null);

    SeqType st = seqType(axis, test, selector, exprs);
    if(expr != null && axis == SELF) {
      // node test: adopt type of context expression: <a/>/self::gnode()
      if(test.kind == Kind.GNODE) st = type.seqType(st.occ);
      // no predicates: step will yield single result: $elements/self::element()
      if(exprs.length == 0 && selector == null && test.subsumes(type) == Boolean.TRUE)
        st = st.with(Occ.EXACTLY_ONE);
    }
    exprType.assign(st).data(expr);
    return expr;
  }

  /**
   * Determines the sequence type of the step.
   * @param axis axis
   * @param test test
   * @param selector selector
   * @param preds predicates
   * @return sequence type
   */
  private static SeqType seqType(final Axis axis, final Test test, final Expr selector,
      final Expr... preds) {
    final Occ occ = axis == ATTRIBUTE && test.subsumes(NodeType.ATTRIBUTE) == Boolean.FALSE
      // no results: attribute::element()
      ? Occ.ZERO :
        axis == SELF && test == NodeTest.NODE && selector == null && preds.length == 0
      // one result: self::node()
      ? Occ.EXACTLY_ONE :
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

    final Expr s = selector != null ? ic.inlineOrNull(selector) : null;
    if(s != null) selector = s;
    final boolean preds = ic.cc.ok(this, true, () -> ic.inline(exprs));
    return s != null || preds ? optimize(ic.cc) : null;
  }

  @Override
  public abstract Step copy(CompileContext cc, IntObjectMap<Var> vm);

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param stats assess database statistics; if {@code true}, return early if step has predicates
   * @return path nodes, or {@code null} if nodes cannot be collected
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final boolean stats) {
    // skip steps with predicates or different namespaces
    final Data data = data();
    if(stats && exprs.length != 0 || data == null || data.defaultNs() == null) return null;

    // skip axes other than descendant, child, and attribute
    if(!axis.oneOf(ATTRIBUTE, CHILD , SELF, DESCENDANT, DESCENDANT_OR_SELF)) return null;

    // skip processing instructions
    final Kind kind = test.kind;
    if(kind.oneOf(Kind.GNODE, Kind.PROCESSING_INSTRUCTION)) return null;

    final Names names = kind == Kind.ATTRIBUTE ? data.attrNames : data.elemNames;
    final int kn = XNode.dbKind(kind);
    final ArrayList<PathNode> tmp = new ArrayList<>();
    final Predicate<Test> addNodes = t -> {
      int name = 0;
      if(t instanceof final NameTest nt) {
        if(nt.name == null) return false;
        name = names.index(nt.name);
      }
      for(final PathNode pn : nodes) {
        if(axis.oneOf(SELF, DESCENDANT_OR_SELF)) {
          if(kn == -1 || kn == pn.kind && (name == 0 || name == pn.name)) {
            if(!tmp.contains(pn)) tmp.add(pn);
          }
        }
        if(axis != SELF) add(pn, tmp, name, kn);
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
   * Adds path nodes to the list if they comply with the given test conditions.
   * @param node root node
   * @param nodes output nodes
   * @param name name ID, or {@code 0} as wildcard
   * @param kind node kind, or {@code -1} for all types
   */
  private void add(final PathNode node, final ArrayList<PathNode> nodes, final int name,
      final int kind) {

    for(final PathNode pn : node.children) {
      if(axis.oneOf(DESCENDANT, DESCENDANT_OR_SELF)) {
        add(pn, nodes, name, kind);
      }
      if(kind == -1 && pn.kind != Data.ATTR ^ axis == ATTRIBUTE ||
         kind == pn.kind && (name == 0 || name == pn.name)) {
        if(!nodes.contains(pn)) nodes.add(pn);
      }
    }
  }

  /**
   * Checks if the step will never yield results.
   * @return {@code true} if steps will never yield results
   */
  private boolean noMatches() {
    final Kind kind = test.kind;
    if(kind.oneOf(Kind.GNODE, Kind.NODE)) return false;

    return switch(axis) {
      // attribute::element()
      case ATTRIBUTE ->
        kind != Kind.ATTRIBUTE;
      // parent::comment()
      case ANCESTOR, PARENT ->
        isLeaf(kind);
      // child::attribute()
      case CHILD, DESCENDANT, FOLLOWING, FOLLOWING_OR_SELF, FOLLOWING_SIBLING,
           FOLLOWING_SIBLING_OR_SELF, PRECEDING, PRECEDING_OR_SELF, PRECEDING_SIBLING,
           PRECEDING_SIBLING_OR_SELF ->
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
    if(!(seqType.type instanceof final NodeType nt)) return false;

    // checks steps on document nodes
    final Kind kind = test.kind, nk = nt.kind();
    if(nk == Kind.DOCUMENT && switch(axis) {
      case SELF, ANCESTOR_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF,
           PRECEDING_OR_SELF, PRECEDING_SIBLING_OR_SELF ->
        !kind.oneOf(Kind.GNODE, Kind.NODE, Kind.DOCUMENT);
      case CHILD, DESCENDANT ->
        kind.oneOf(Kind.DOCUMENT, Kind.ATTRIBUTE);
      case DESCENDANT_OR_SELF ->
        kind == Kind.ATTRIBUTE;
      default ->
        true;
    }) return true;

    // check step after any other expression
    return switch(axis) {
      // $element/self::text(), ...
      case SELF ->
        test.subsumes(nt) == Boolean.FALSE;
      // $attribute/descendant::, $text/child::, $comment/attribute::, ...
      case DESCENDANT, CHILD, ATTRIBUTE ->
        isLeaf(nk);
      // $text/descendant-or-self::text(), ...
      case DESCENDANT_OR_SELF ->
        isLeaf(nk) && !kind.oneOf(Kind.GNODE, Kind.NODE) && !kind.instanceOf(nk);
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
   * Checks if the step is redundant.
   * @param seqType type of incoming nodes
   * @return {@code true} if the step can be removed
   */
  boolean remove(final SeqType seqType) {
    // <xml/>/. → <xml/>
    // <xml/>/self::node() → <xml/>
    // $text/descendant-or-self::text() → $text
    // $doc/ancestor-or-self::text() → $doc
    final Type type = seqType.type;
    return exprs.length == 0 && selector == null && (
      axis == SELF ||
      axis == DESCENDANT_OR_SELF && isLeaf(type.kind()) ||
      axis == ANCESTOR_OR_SELF && type.instanceOf(NodeType.DOCUMENT)
    ) && test.subsumes(type) == Boolean.TRUE;
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
  final Step addPredicates(final Expr... preds) {
    exprType.assign(seqType().union(Occ.ZERO));
    return copyType(rebuild(ExprList.concat(exprs, preds)));
  }

  /**
   * Removes the last predicate from the step.
   * After the call, a new instance of the resulting path must be created.
   * @return resulting step instance
   */
  final Step removePredicate() {
    return copyType(rebuild(Arrays.copyOfRange(exprs, 0, exprs.length - 1)));
  }

  /**
   * Checks if the specified type refers to a leaf node.
   * @param kind kind (can be {@code null})
   * @return result of check
   */
  private boolean isLeaf(final Kind kind) {
    return kind != null && kind.oneOf(Kind.ATTRIBUTE, Kind.COMMENT,
      Kind.NAMESPACE, Kind.PROCESSING_INSTRUCTION, Kind.TEXT);
  }

  @Override
  public boolean has(final Flag... flags) {
    return selector != null && selector.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return (selector == null || selector.inlineable(ic)) && super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    final VarUsage vu = super.count(var);
    return selector != null ? selector.count(var).plus(vu) : vu;
  }

  @Override
  public int exprSize() {
    return (selector != null ? selector.exprSize() : 0) + super.exprSize();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(selector != null && !selector.accept(visitor)) return false;
    for(final Expr pred : exprs) {
      visitor.enterFocus();
      if(!pred.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Step st && axis == st.axis && test.equals(st.test) &&
        Objects.equals(selector, st.selector) && super.equals(obj);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.AXIS, axis.name, QueryText.TEST,
        test.toString(false)), selector, exprs);
  }

  @Override
  protected void rootToString(final QueryString qs) {
    if(selector != null) {
      qs.token(axis.name + "::").token("{").token(selector).token("}");
      return;
    }
    final TokenBuilder tb = new TokenBuilder();
    if(test == NodeTest.NODE) {
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
