package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.Test.*;
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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public abstract class Step extends Preds {
  /** Kind test. */
  public final Test test;
  /** Axis. */
  public Axis axis;

  /**
   * This method returns the most efficient step implementation.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   * @return step
   */
  public static Step get(final InputInfo info, final Axis axis, final Test test,
      final Expr... preds) {

    // optimize single last() functions
    Step step = null;
    if(preds.length == 1 && preds[0].isFunction(Function.LAST)) {
      step = new IterLastStep(info, axis, test, preds);
    } else {
      // check for simple positional predicates
      boolean pos = false;
      for(final Expr pred : preds) {
        if(pred instanceof ItrPos || numeric(pred)) {
          pos = true;
        } else if(pred.seqType().mayBeNumber() || pred.has(Flag.POS)) {
          // positional checks may be nested or non-deterministic: choose full evaluation
          step = new CachedStep(info, axis, test, preds);
          break;
        }
      }
      if(step == null) {
        step = pos ? new IterPosStep(info, axis, test, preds) :
          new IterStep(info, axis, test, preds);
      }
    }
    return step;
  }

  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param exprs predicates
   */
  Step(final InputInfo info, final Axis axis, final Test test, final Expr... exprs) {
    super(info, SeqType.get(test.type, Occ.ZERO_MORE), exprs);
    this.axis = axis;
    this.test = test;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
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
        return Empty.SEQ;
      }
    }

    // simplifies the predicates
    simplify(cc, this);

    // optimize predicates
    cc.pushFocus(this);
    try {
      final Expr expr = super.optimize(cc);
      if(expr != this) return expr;
    } finally {
      cc.removeFocus();
    }

    // compute result size
    if(!exprType(seqType(), size())) return cc.emptySeq(this);

    // choose best implementation
    return copyType(get(info, axis, test, exprs));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return inlineAll(exprs, var, ex, cc) ? optimize(cc) : null;
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
    return axis == ax && (name ? test.kind == Kind.NAME : test == KindTest.NOD) &&
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

      if(test.kind == Kind.NAME) {
        // element/attribute test (*:ln)
        final Names names = kind == Data.ATTR ? dt.attrNames : dt.elemNames;
        name = names.id(((NameTest) test).local);
      } else if(test.kind != null && test.kind != Kind.WILDCARD) {
        // skip namespace and standard tests
        return null;
      }
    }

    // skip axes other than descendant, child, and attribute
    if(axis != Axis.ATTR && axis != Axis.CHILD && axis != Axis.DESC &&
       axis != Axis.DESCORSELF && axis != Axis.SELF) return null;

    final ArrayList<PathNode> tmp = new ArrayList<>();
    for(final PathNode pn : nodes) {
      if(axis == Axis.SELF || axis == Axis.DESCORSELF) {
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

    for(final PathNode n : node.children) {
      if(axis == Axis.DESC || axis == Axis.DESCORSELF) {
        add(n, nodes, name, kind);
      }
      if(kind == -1 && n.kind != Data.ATTR ^ axis == Axis.ATTR ||
         kind == n.kind && (name == 0 || name == n.name)) {
        if(!nodes.contains(n)) nodes.add(n);
      }
    }
  }

  /**
   * Adds predicates to the step.
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Step)) return false;
    final Step s = (Step) obj;
    return axis == s.axis && test.equals(s.test) && super.equals(obj);
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
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(AXIS, axis.name, TEST, test), exprs);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(test == KindTest.NOD) {
      if(axis == Axis.PARENT) sb.append("..");
      if(axis == Axis.SELF) sb.append('.');
    }
    if(sb.length() == 0) {
      if(axis == Axis.ATTR && test instanceof NameTest) sb.append('@');
      else if(axis != Axis.CHILD) sb.append(axis).append("::");
      sb.append(test);
    }
    return sb.append(super.toString()).toString();
  }
}
