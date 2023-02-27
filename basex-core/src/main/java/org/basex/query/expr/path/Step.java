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
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract axis step expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Step extends Preds {
  /** Kind test. */
  public Test test;
  /** Axis. */
  public Axis axis;

  /**
   * Returns a new optimized self::node() step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param ii input info
   * @param preds predicates
   * @return step
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo ii,
      final Expr... preds) throws QueryException {
    return get(cc, root, ii, KindTest.NODE, preds);
  }

  /**
   * Returns a new optimized self step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param ii input info
   * @param test test
   * @param preds predicates
   * @return step
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo ii,
      final Test test, final Expr... preds) throws QueryException {
    return get(cc, root, ii, SELF, test, preds);
  }

  /**
   * Returns a new optimized step.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @param ii input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   * @return step
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo ii,
      final Axis axis, final Test test, final Expr... preds) throws QueryException {
    return new CachedStep(ii, axis, test, preds).optimize(root, cc);
  }

  /**
   * Returns a new step.
   * @param ii input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   * @return step
   */
  public static Step get(final InputInfo ii, final Axis axis, final Test test,
      final Expr... preds) {

    // optimize single last() functions
    if(preds.length == 1 && Function.LAST.is(preds[0]))
      return new IterLastStep(ii, axis, test, preds);

    // check for simple positional predicates
    boolean pos = false;
    for(final Expr pred : preds) {
      if(pred instanceof CmpPos || numeric(pred)) {
        // predicate is known to be a positional check; can be optimized
        pos = true;
      } else if(mayBePositional(pred)) {
        // predicate **may** be positional: choose cached evaluation
        return new CachedStep(ii, axis, test, preds);
      }
    }
    return pos ?
      new IterPosStep(ii, axis, test, preds) :
      new IterStep(ii, axis, test, preds);
  }

  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  Step(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, seqType(axis, test, preds), preds);
    this.axis = axis;
    this.test = test;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(null, cc);
  }

  /**
   * Optimizes the step for the given root expression.
   * @param cc compilation context
   * @param root root context expression; if {@code null}, the current context will be used
   * @return optimized step
   * @throws QueryException query exception
   */
  final Expr optimize(final Expr root, final CompileContext cc) throws QueryException {
    // updates the static type
    final Expr rt = type(root != null ? root : cc.qc.focus.value);

    // choose stricter axis
    final Axis old = axis;
    final Type type = seqType().type;
    if(axis == DESCENDANT_OR_SELF && type.instanceOf(NodeType.DOCUMENT_NODE) ||
       axis == ANCESTOR_OR_SELF && type.oneOf(NodeType.LEAF_TYPES)) {
      // descendant-or-self::document-node()  ->  self::document-node()
      // ancestor-or-self::text()  ->  self::text()
      axis = SELF;
    } else if(rt != null && rt.seqType().type.intersect(type) == null) {
      // root()/descendant-or-self::x  ->  root()/descendant::x
      if(axis == DESCENDANT_OR_SELF) axis = DESCENDANT;
      // $text/ancestor-or-self::x  ->  $text/ancestor::x
      else if(axis == ANCESTOR_OR_SELF) axis = ANCESTOR;
    }
    if(axis != old) cc.info(QueryText.OPTREWRITE_X_X, old, this);

    // check if the test or step will never yield results
    final Test t = test.optimize(data());
    if(t == null || noMatches()) {
      cc.info(QueryText.OPTSTEP_X, this);
      return cc.emptySeq(this);
    }
    test = t;

    // simplify predicates, choose best implementation
    return simplify(cc, this) ? cc.emptySeq(this) : copyType(get(info, axis, test, exprs));
  }

  @Override
  protected final Expr type(final Expr expr) {
    exprType.data(expr);
    if(expr != null && axis == SELF) {
      final SeqType seqType = expr.seqType();
      // node test: adopt type of context expression: <a/>/self::node()
      if(test == KindTest.NODE) exprType.assign(seqType.type);
      // no predicates: step will yield single result: $elements/self::element()
      if(exprs.length == 0 && test.matches(seqType) == Boolean.TRUE)
        exprType.assign(Occ.EXACTLY_ONE);
    }
    return expr;
  }

  /**
   * Determines the sequence type of the step.
   * @param axis axis
   * @param test test
   * @param preds predicates
   * @return sequence type
   */
  public static SeqType seqType(final Axis axis, final Test test, final Expr... preds) {
    final Type type = axis == ATTRIBUTE ? NodeType.ATTRIBUTE : test.type;
    final Occ occ = axis == SELF && test == KindTest.NODE && preds.length == 0
      // one result: self::node()
      ? Occ.EXACTLY_ONE :
        axis == SELF || axis == PARENT ||
        axis == ATTRIBUTE && test instanceof NameTest && ((NameTest) test).part == NamePart.FULL ||
        preds.length == 1 && preds[0] instanceof CmpPos && ((CmpPos) preds[0]).exact()
      // zero or one result: self::X, parent::X, attribute::Q{uri}local, ...[position() = n]
      ? Occ.ZERO_OR_ONE
      : Occ.ZERO_OR_MORE;
    final Test t = test instanceof KindTest ? null : test;
    return SeqType.get(type, occ, t);
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    // do not inline context value
    return ic.var != null && ic.cc.ok(this, () -> ic.inline(exprs)) ? optimize(ic.cc) : null;
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public abstract Step copy(CompileContext cc, IntObjMap<Var> vm);

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
    if(axis != ATTRIBUTE && axis != CHILD  && axis != SELF && axis != DESCENDANT &&
       axis != DESCENDANT_OR_SELF) return null;

    // skip processing instructions
    final NodeType type = test.type;
    if(type == NodeType.PROCESSING_INSTRUCTION) return null;

    final Names names = type == NodeType.ATTRIBUTE ? data.attrNames : data.elemNames;
    final int kind = ANode.kind(test.type);
    final ArrayList<PathNode> tmp = new ArrayList<>();
    final Predicate<Test> addNodes = t -> {
      int name = 0;
      if(t instanceof NameTest) {
        final NameTest nt = (NameTest) t;
        if(nt.part() != NamePart.LOCAL) return false;
        name = names.id(nt.local);
      }
      for(final PathNode pn : nodes) {
        if(axis == SELF || axis == DESCENDANT_OR_SELF) {
          if(kind == -1 || kind == pn.kind && (name == 0 || name == pn.name)) {
            if(!tmp.contains(pn)) tmp.add(pn);
          }
        }
        if(axis != SELF) add(pn, tmp, name, kind);
      }
      return true;
    };

    // add nodes
    if(test instanceof UnionTest) {
      for(final Test t : ((UnionTest) test).tests) {
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
   * @param name name id, or {@code 0} as wildcard
   * @param kind node kind, or {@code -1} for all types
   */
  private void add(final PathNode node, final ArrayList<PathNode> nodes, final int name,
      final int kind) {

    for(final PathNode pn : node.children) {
      if(axis == DESCENDANT || axis == DESCENDANT_OR_SELF) {
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
    final NodeType type = test.type;
    if(type.oneOf(NodeType.NODE, NodeType.SCHEMA_ATTRIBUTE, NodeType.SCHEMA_ELEMENT)) return false;

    switch(axis) {
      // attribute::element()
      case ATTRIBUTE:
        return type != NodeType.ATTRIBUTE;
      case ANCESTOR:
      // parent::comment()
      case PARENT:
        return type.oneOf(NodeType.LEAF_TYPES);
      // child::attribute()
      case CHILD:
      case DESCENDANT:
      case FOLLOWING:
      case FOLLOWING_SIBLING:
      case PRECEDING:
      case PRECEDING_SIBLING:
        return type.oneOf(NodeType.ATTRIBUTE, NodeType.DOCUMENT_NODE_ELEMENT,
            NodeType.DOCUMENT_NODE, NodeType.NAMESPACE_NODE);
      default:
        return false;
    }
  }

  /**
   * Checks if the step will never yield results.
   * @param seqType type of incoming nodes
   * @return {@code true} if steps will never yield results
   */
  final boolean empty(final SeqType seqType) {
    // checks steps on document nodes
    final Type inputType = seqType.type;
    final NodeType type = test.type;
    if(inputType.instanceOf(NodeType.DOCUMENT_NODE) && ((Check) () -> {
      switch(axis) {
        case SELF:
        case ANCESTOR_OR_SELF:
          return !type.oneOf(NodeType.NODE, NodeType.DOCUMENT_NODE);
        case CHILD:
        case DESCENDANT:
          return type.oneOf(NodeType.DOCUMENT_NODE, NodeType.ATTRIBUTE);
        case DESCENDANT_OR_SELF:
          return type == NodeType.ATTRIBUTE;
        default:
          return true;
      }
    }).ok()) return true;

    // check step after any other expression
    switch(axis) {
      // $element/self::text(), ...
      case SELF:
        return test.matches(seqType) == Boolean.FALSE;
      // $attribute/descendant::, $text/child::, $comment/attribute::, ...
      case DESCENDANT:
      case CHILD:
      case ATTRIBUTE:
        return inputType.oneOf(NodeType.LEAF_TYPES);
      // $text/descendant-or-self::text(), ...
      case DESCENDANT_OR_SELF:
        return inputType.oneOf(NodeType.LEAF_TYPES) && type != NodeType.NODE &&
          !type.instanceOf(inputType);
      // $attribute/following-sibling::, $attribute/preceding-sibling::
      case FOLLOWING_SIBLING:
      case PRECEDING_SIBLING:
        return inputType == NodeType.ATTRIBUTE;
      // $attribute/parent::document-node()
      case PARENT:
        return inputType == NodeType.ATTRIBUTE && type == NodeType.DOCUMENT_NODE;
      default:
        return false;
    }
  }

  /**
   * Checks if the step is redundant.
   * @param seqType type of incoming nodes
   * @return {@code true} if the step can be ignored
   */
  final boolean redundant(final SeqType seqType) {
    // <xml/>/.  ->  <xml/>
    // <xml/>/self::node()  ->  <xml/>
    // $text/descendant-or-self::text()  ->  $text
    // $doc/ancestor-or-self::text()  ->  $doc
    final Type prevType = seqType.type;
    return exprs.length == 0 && (
      axis == SELF ||
      axis == DESCENDANT_OR_SELF && prevType.oneOf(NodeType.LEAF_TYPES) ||
      axis == ANCESTOR_OR_SELF && prevType.instanceOf(NodeType.DOCUMENT_NODE)
    ) && test.matches(seqType) == Boolean.TRUE;
  }

  /**
   * Adds predicates to the step.
   * After the call, a new instance of the resulting path must be created.
   * @param preds predicates to be added
   * @return resulting step instance
   */
  final Step addPredicates(final Expr... preds) {
    exprType.assign(seqType().union(Occ.ZERO));
    return copyType(get(info, axis, test, ExprList.concat(exprs, preds)));
  }

  /**
   * Removes the last predicate from the step.
   * After the call, a new instance of the resulting path must be created.
   * @return resulting step instance
   */
  final Step removePredicate() {
    return copyType(get(info, axis, test, Arrays.copyOfRange(exprs, 0, exprs.length - 1)));
  }

  /**
   * Throws an exception if the context value is not a node.
   * @param qc query context
   * @return context
   * @throws QueryException query exception
   */
  final ANode checkNode(final QueryContext qc) throws QueryException {
    final Value value = qc.focus.value;
    if(value instanceof ANode) return (ANode) value;
    throw value == null ? QueryError.NOCTX_X.get(info, this) :
      QueryError.PATHNODE_X_X_X.get(info, this, value.type, value);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Expr pred : exprs) {
      visitor.enterFocus();
      if(!pred.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return true;
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr pred : exprs) size += pred.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Step)) return false;
    final Step st = (Step) obj;
    return axis == st.axis && test.equals(st.test) && super.equals(obj);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.AXIS, axis.name, QueryText.TEST,
        test.toString(false)), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    if(test == KindTest.NODE) {
      if(axis == PARENT) tb.add("..");
      if(axis == SELF) tb.add('.');
    }
    if(tb.isEmpty()) {
      final java.util.function.Function<Test, TokenBuilder> add = type -> {
        if(axis == ATTRIBUTE && type instanceof NameTest)
          return tb.add('@').add(type.toString(false));
        if(axis != CHILD) tb.add(axis).add("::");
        return tb.add(type.toString(test.type == NodeType.ATTRIBUTE));
      };
      if(test instanceof UnionTest) {
        tb.add('(');
        for(final Test t : ((UnionTest) test).tests) add.apply(t).add(" | ");
        tb.delete(tb.size() - 3, tb.size()).add(')');
      } else {
        add.apply(test);
      }
    }
    qs.token(tb.finish());
    super.toString(qs);
  }
}
