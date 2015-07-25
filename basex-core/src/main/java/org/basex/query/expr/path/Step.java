package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.Test.Kind;
import org.basex.query.func.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Step extends Preds {
  /** Axis. */
  Axis axis;
  /** Kind test. */
  public final Test test;

  /**
   * This method returns the most appropriate step implementation.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   * @return step
   */
  public static Step get(final InputInfo info, final Axis axis, final Test test,
      final Expr... preds) {

    // optimize single last() functions
    if(preds.length == 1 && preds[0].isFunction(Function.LAST))
      return new IterLastStep(info, axis, test, preds);

    // check for simple positional predicates
    boolean pos = false;
    for(final Expr pred : preds) {
      if(pred instanceof Pos || num(pred)) {
        pos = true;
      } else if(pred.seqType().mayBeNumber() || pred.has(Flag.POS)) {
        // positional checks may be nested or non-deterministic: choose full evaluation
        return new CachedStep(info, axis, test, preds);
      }
    }
    return pos ? new IterPosStep(info, axis, test, preds) :
                 new IterStep(info, axis, test, preds);
  }

  /**
   * Constructor.
   * @param info input info
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  Step(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, preds);
    this.axis = axis;
    this.test = test;
    seqType = SeqType.NOD_ZM;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // check if test will never yield results
    if(!test.optimize(qc)) return Empty.SEQ;

    // optimize predicates
    final Expr e = super.optimize(qc, scp);
    if(e != this) return e;

    // compute result size
    seqType(seqType, Long.MAX_VALUE);
    if(size == 0) return optPre(qc);

    // choose best implementation
    return copyType(get(info, axis, test, preds));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    return inlineAll(qc, scp, preds, var, ex) ? optimize(qc, scp) : null;
  }

  @Override
  public abstract Step copy(QueryContext qc, VarScope scp, IntObjMap<Var> vs);

  /**
   * Checks if this step has no predicates and uses the specified axis text.
   * @param ax axis to be checked
   * @param name name/node test
   * @return result of check
   */
  public final boolean simple(final Axis ax, final boolean name) {
    return axis == ax && preds.length == 0 && (name ? test.kind == Kind.NAME : test == Test.NOD);
  }

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param data data reference
   * @return resulting path nodes or {@code null} if nodes cannot be evaluated
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final Data data) {
    // skip steps with predicates or different namespaces
    if(preds.length != 0 || data.nspaces.globalUri() == null) return null;

    // check restrictions on node type
    int kind = -1, name = 0;
    if(test.type != null) {
      kind = ANode.kind(test.type);
      // no index available for processing instructions
      if(kind == Data.PI) return null;

      if(test.kind == Kind.NAME) {
        // element/attribute test (*:ln)
        final Names names = kind == Data.ATTR ? data.attrNames : data.elemNames;
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
   * @param prds predicates to be added
   * @return resulting step instance
   */
  final Step addPreds(final Expr... prds) {
    for(final Expr pred : prds) preds = Array.add(preds, pred);
    return get(info, axis, test, preds);
  }

  /**
   * Throws an exception if the context value is not a node.
   * @param qc query context
   * @return context
   * @throws QueryException query exception
   */
  final ANode checkNode(final QueryContext qc) throws QueryException {
    final Value v = qc.value;
    if(v instanceof ANode) return (ANode) v;
    throw v == null ? NOCTX_X.get(info, this) : STEPNODE_X_X_X.get(info, this, v.type, v);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Step)) return false;
    final Step st = (Step) cmp;
    final int pl = preds.length;
    if(pl != st.preds.length || axis != st.axis || !test.sameAs(st.test)) return false;
    for(int p = 0; p < pl; p++) if(!preds[p].sameAs(st.preds[p])) return false;
    return true;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Expr pred : preds) {
      visitor.enterFocus();
      if(!pred.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return true;
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr pred : preds) sz += pred.exprSize();
    return sz;
  }

  @Override
  public final void plan(final FElem plan) {
    final FElem el = planElem(AXIS, axis.name, TEST, test);
    addPlan(plan, el);
    super.plan(el);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(test == Test.NOD) {
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
