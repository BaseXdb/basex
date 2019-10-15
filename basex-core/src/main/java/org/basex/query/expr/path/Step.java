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
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract axis step expression.
 *
 * @author BaseX Team 2005-19, BSD License
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
        test.part() == NamePart.FULL ? Occ.ZERO_ONE : Occ.ZERO_MORE), exprs);

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
    final Value value = cc.qc.focus.value;
    if(value != null) {
      // check if test will never yield results
      if(!test.optimize(value)) {
        cc.info(OPTNAME_X, test);
        return Empty.VALUE;
      }
    }

    // optimize predicate, choose best implementation
    final Expr expr = optimize(cc, this);
    return expr != this ? expr : copyType(get(info, axis, test, exprs));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = false;
    cc.pushFocus(this);
    try {
      changed = inlineAll(var, ex, exprs, cc);
    } finally {
      cc.removeFocus();
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
   * Checks if this step uses the specified axis test and has no predicates.
   * @param ax axis to be checked
   * @param name name/node test
   * @return result of check
   */
  public final boolean simple(final Axis ax, final boolean name) {
    return axis == ax && (name ? test.part() == NamePart.LOCAL : test == KindTest.NOD) &&
        exprs.length == 0;
  }

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param dt data reference
   * @return resulting path nodes or {@code null} if nodes cannot be evaluated
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final Data dt) {
    // skip steps with predicates or different namespaces
    if(exprs.length != 0 || dt.nspaces.globalUri() == null) return null;

    // check restrictions on node type
    int kind = -1, name = 0;
    if(test.type != null) {
      kind = ANode.kind(test.type);
      // no index available for processing instructions
      if(kind == Data.PI) return null;

      final NamePart part = test.part();
      if(part == NamePart.LOCAL) {
        // element/attribute test (*:ln)
        final Names names = kind == Data.ATTR ? dt.attrNames : dt.elemNames;
        name = names.id(((NameTest) test).local);
      } else if(part != null) {
        // skip namespace and standard tests
        return null;
      }
    }

    // skip axes other than descendant, child, and attribute
    if(axis != Axis.ATTRIBUTE && axis != Axis.CHILD && axis != Axis.DESCENDANT &&
       axis != Axis.DESCENDANT_OR_SELF && axis != Axis.SELF) return null;

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
      if(axis == Axis.ATTRIBUTE && test instanceof NameTest) sb.append('@');
      else if(axis != Axis.CHILD) sb.append(axis).append("::");
      sb.append(test);
    }
    return sb.append(super.toString()).toString();
  }
}
