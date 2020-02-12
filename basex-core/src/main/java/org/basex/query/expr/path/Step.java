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
   * This method returns the most efficient step implementation.
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
      } else if(pred.seqType().mayBeNumber() || pred.has(Flag.POS)) {
        // choose cached evaluation if check *may* be positional
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
   * @param exprs predicates
   */
  Step(final InputInfo info, final Axis axis, final Test test, final Expr... exprs) {
    super(info, SeqType.get(
      axis == Axis.ATTRIBUTE ? NodeType.ATT : test.type,
      axis == Axis.SELF && test == KindTest.NOD && exprs.length == 0 ? Occ.ONE :
      axis == Axis.SELF || axis == Axis.PARENT || axis == Axis.ATTRIBUTE &&
        test instanceof NameTest && ((NameTest) test).part == NamePart.FULL ?
        Occ.ZERO_ONE : Occ.ZERO_MORE), exprs);

    this.axis = axis;
    this.test = test;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // self step: assign type of input
    final Value value = cc.qc.focus.value;
    if(value != null && axis == Axis.SELF && test == KindTest.NOD) {
      exprType.assign(value.seqType().type);
    }

    cc.pushFocus(this);
    try {
      super.compile(cc);
    } finally {
      cc.removeFocus();
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // check if step or test will never yield results
    if(emptyStep() || !test.optimize(cc.qc.focus.value)) {
      cc.info(OPTSTEP_X, this);
      return cc.emptySeq(this);
    }
    // optimize predicate, choose best implementation
    return optimize(cc, this) ? copyType(get(info, axis, test, exprs)) : cc.emptySeq(this);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = false;
    if(var != null) {
      cc.pushFocus(this);
      try {
        changed = inlineAll(var, ex, exprs, cc);
      } finally {
        cc.removeFocus();
      }
    }
    return changed ? optimize(cc) : null;
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
   * @param dt data reference
   * @return resulting path nodes or {@code null} if nodes cannot be collected
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final Data dt) {
    // skip steps with predicates or different namespaces
    if(exprs.length != 0 || dt.nspaces.globalUri() == null) return null;

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
        final Names names = test.type == NodeType.ATT ? dt.attrNames : dt.elemNames;
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
  private boolean emptyStep() {
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
  boolean emptyStep(final NodeType prevType) {
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
   * @param add predicates to be added
   * @return resulting step instance
   */
  final Step addPreds(final Expr... add) {
    return copyType(get(info, axis, test, ExprList.concat(exprs, add)));
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
    plan.add(plan.create(this, AXIS, axis.name, TEST, test), exprs);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(test == KindTest.NOD) {
      if(axis == Axis.PARENT) sb.append("..");
      if(axis == Axis.SELF) sb.append('.');
    }
    if(sb.length() == 0) {
      final java.util.function.Function<Test, StringBuilder> add = t -> {
        if(axis == Axis.ATTRIBUTE && t instanceof NameTest) sb.append('@');
        else if(axis != Axis.CHILD) sb.append(axis).append("::");
        return sb.append(t);
      };

      if(test instanceof UnionTest) {
        sb.append('(');
        for(final Test t : ((UnionTest) test).tests) add.apply(t).append(" | ");
        sb.delete(sb.length() - 3, sb.length()).append(')');
      } else {
        add.apply(test);
      }
    }
    return sb.append(super.toString()).toString();
  }
}
