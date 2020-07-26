package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-20, BSD License
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
   * @param root root expression
   * @param ii input info
   * @param preds predicates
   * @return step
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo ii,
      final Expr... preds) throws QueryException {
    return get(cc, root, ii, KindTest.NOD, preds);
  }

  /**
   * Returns a new optimized self step.
   * @param cc compilation context
   * @param root root expression
   * @param ii input info
   * @param test test
   * @param preds predicates
   * @return step
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final Expr root, final InputInfo ii,
      final Test test, final Expr... preds) throws QueryException {
    return get(cc, root, ii, Axis.SELF, test, preds);
  }

  /**
   * Returns a new optimized step.
   * @param cc compilation context
   * @param root root expression
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
      if(pred instanceof ItrPos || numeric(pred)) {
        // predicate is known to be positional check; can be optimized
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
    super(info, SeqType.get(
      // type
      axis == Axis.ATTRIBUTE ? NodeType.ATT : test.type,
      // occurrence
      axis == Axis.SELF && test == KindTest.NOD && preds.length == 0 ? Occ.ONE :
      axis == Axis.SELF || axis == Axis.PARENT || axis == Axis.ATTRIBUTE &&
        test instanceof NameTest && ((NameTest) test).part == NamePart.FULL ?
        Occ.ZERO_ONE : Occ.ZERO_MORE
    ), preds);

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
   * @param expr context expression; if {@code null}, the current context will be used
   * @return optimized step
   * @throws QueryException query exception
   */
  final Expr optimize(final Expr expr, final CompileContext cc) throws QueryException {
    // updates the static type
    final Expr ex = expr != null ? expr : cc.qc.focus.value;
    type(ex);

    // check if step or test will never yield results
    if(noMatches() || ex != null && test.noMatches(ex.data())) {
      cc.info(OPTSTEP_X, this);
      return cc.emptySeq(this);
    }
    // simplify predicates, choose best implementation
    return simplify(cc, this) ? cc.emptySeq(this) : copyType(get(info, axis, test, exprs));
  }

  @Override
  protected final void type(final Expr expr) {
    if(expr != null && axis == Axis.SELF && test instanceof KindTest) {
      final Type type = expr.seqType().type;
      if(test == KindTest.NOD) {
        // node test: adopt type of context expression
        // <a/>/self::node()
        exprType.assign(type);
      } else if(type == test.type && exprs.length == 0) {
        // other kind tests, no predicates: step will yield one result
        // $elements/self::element()
        exprType.assign(Occ.ONE);
      }
    }
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    // do not inline context value
    return ic.var != null && ic.cc.ok(this, () -> ic.inline(exprs)) ? optimize(ic.cc) : null;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public abstract Step copy(CompileContext cc, IntObjMap<Var> vm);

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param data data reference
   * @return resulting path nodes or {@code null} if nodes cannot be collected
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final Data data) {
    // skip steps with predicates or different namespaces
    if(exprs.length != 0 || data.defaultNs() == null) return null;

    // skip axes other than descendant, child, and attribute
    if(axis != Axis.ATTRIBUTE && axis != Axis.CHILD && axis != Axis.DESCENDANT &&
       axis != Axis.DESCENDANT_OR_SELF && axis != Axis.SELF) return null;
    // skip tests other than processing instructions, skip union tests
    if(test.type == NodeType.PI || test instanceof UnionTest) return null;

    // check node type
    int name = 0;
    if(test instanceof NameTest) {
      final NamePart part = ((NameTest) test).part;
      if(part == NamePart.LOCAL) {
        // element/attribute test (*:ln)
        final Names names = test.type == NodeType.ATT ? data.attrNames : data.elemNames;
        name = names.id(((NameTest) test).local);
      } else if(part != null) {
        // skip namespace and standard tests
        return null;
      }
    }

    final int kind = ANode.kind(test.type);
    final ArrayList<PathNode> tmp = new ArrayList<>();
    for(final PathNode pn : nodes) {
      if(axis == Axis.SELF || axis == Axis.DESCENDANT_OR_SELF) {
        if(kind == -1 || kind == pn.kind && (name == 0 || name == pn.name)) {
          if(!tmp.contains(pn)) tmp.add(pn);
        }
      }
      if(axis != Axis.SELF) add(pn, tmp, name, kind);
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
      if(axis == Axis.DESCENDANT || axis == Axis.DESCENDANT_OR_SELF) {
        add(pn, nodes, name, kind);
      }
      if(kind == -1 && pn.kind != Data.ATTR ^ axis == Axis.ATTRIBUTE ||
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
    if(type.oneOf(NodeType.NOD, NodeType.SCA, NodeType.SCE)) return false;

    switch(axis) {
      // attribute::element()
      case ATTRIBUTE:
        return type != NodeType.ATT;
      case ANCESTOR:
      // parent::comment()
      case PARENT:
        return type.oneOf(NodeType.ATT, NodeType.COM, NodeType.NSP, NodeType.PI, NodeType.TXT);
      // child::attribute()
      case CHILD:
      case DESCENDANT:
      case FOLLOWING:
      case FOLLOWING_SIBLING:
      case PRECEDING:
      case PRECEDING_SIBLING:
        return type.oneOf(NodeType.ATT, NodeType.DEL, NodeType.DOC, NodeType.NSP);
      default:
        return false;
    }
  }

  /**
   * Checks if the step will never yield results.
   * @param prevType type of incoming nodes
   * @return {@code true} if steps will never yield results
   */
  final boolean emptyStep(final NodeType prevType) {
    // checks steps on document nodes
    final NodeType type = test.type;
    if(prevType.instanceOf(NodeType.DOC) && ((Check) () -> {
      switch(axis) {
        case SELF:
        case ANCESTOR_OR_SELF:
          return !type.oneOf(NodeType.NOD, NodeType.DOC);
        case CHILD:
        case DESCENDANT:
          return type.oneOf(NodeType.DOC, NodeType.ATT);
        case DESCENDANT_OR_SELF:
          return type == NodeType.ATT;
        // document {}/parent::, ...
        default:
          return true;
      }
    }).ok()) return true;

    // check step after any other expression
    switch(axis) {
      // type of current step will not accept any nodes of previous step
      // example: <a/>/self::text()
      case SELF:
        return type != NodeType.NOD && !type.instanceOf(prevType);
      // .../descendant::, .../child::, .../attribute::
      case DESCENDANT:
      case CHILD:
      case ATTRIBUTE:
        return prevType.oneOf(NodeType.ATT, NodeType.TXT, NodeType.COM, NodeType.PI, NodeType.NSP);
      // .../following-sibling::, .../preceding-sibling::
      case FOLLOWING_SIBLING:
      case PRECEDING_SIBLING:
        return prevType == NodeType.ATT;
      default:
        return false;
    }
  }

  /**
   * Adds predicates to the step.
   * After the call, a new instance of the resulting path must be created.
   * @param preds predicates to be added
   * @return resulting step instance
   */
  final Step addPredicates(final Expr... preds) {
    exprType.assign(seqType().with(Occ.ZERO_MORE));
    return copyType(get(info, axis, test, ExprList.concat(exprs, preds)));
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
    throw value == null ? NOCTX_X.get(info, this) :
      STEPNODE_X_X_X.get(info, this, value.type, value);
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
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, AXIS, axis.name, TEST, test.toString(false)), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    if(test == KindTest.NOD) {
      if(axis == Axis.PARENT) tb.add("..");
      if(axis == Axis.SELF) tb.add('.');
    }
    if(tb.isEmpty()) {
      final java.util.function.Function<Test, TokenBuilder> add = t -> {
        if(axis == Axis.ATTRIBUTE && t instanceof NameTest)
          return tb.add('@').add(t.toString(false));
        if(axis != Axis.CHILD) tb.add(axis).add(COLS);
        return tb.add(t.toString(test.type == NodeType.ATT));
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
    super.plan(qs);
  }
}
