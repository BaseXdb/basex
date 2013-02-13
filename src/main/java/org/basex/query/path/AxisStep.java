package org.basex.query.path;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.Test.Mode;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis step expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class AxisStep extends Preds {
  /** Axis. */
  Axis axis;
  /** Kind test. */
  public Test test;

  /**
   * This method creates a copy from the specified step.
   * @param s step to be copied
   * @return step
   */
  public static AxisStep get(final AxisStep s) {
    return get(s.info, s.axis, s.test, s.preds);
  }

  /**
   * This method creates a step instance.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   * @return step
   */
  public static AxisStep get(final InputInfo ii, final Axis a, final Test t,
      final Expr... p) {

    boolean num = false;
    for(final Expr pr : p) num |= pr.type().mayBeNumber() || pr.uses(Use.POS);
    return num ? new AxisStep(ii, a, t, p) : new IterStep(ii, a, t, p);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  AxisStep(final InputInfo ii, final Axis a, final Test t, final Expr... p) {
    super(ii, p);
    axis = a;
    test = t;
    type = SeqType.NOD_ZM;
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    if(!test.compile(ctx)) return Empty.SEQ;

    // leaf flag indicates that a context node can be replaced by a text() step
    final Type ct = ctx.value != null ? ctx.value.type : null;
    final boolean leaf = ctx.leaf;
    ctx.leaf = false;
    try {
      final Data data = ctx.data();
      if(data != null && test.mode == Mode.NAME && test.type != NodeType.ATT &&
          axis.down && data.meta.uptodate && data.nspaces.size() == 0) {
        final Stats s = data.tagindex.stat(data.tagindex.id(((NameTest) test).ln));
        ctx.leaf = s != null && s.isLeaf();
      }

      // as predicates will not necessarily start from the document node,
      // the context item type is temporarily generalized
      if(ct == NodeType.DOC) ctx.value.type = NodeType.NOD;
      final Expr e = super.compile(ctx, scp);

      // return optimized step / don't re-optimize step
      if(e != this || e instanceof IterStep) return e;

    } finally {
      if(ct == NodeType.DOC) ctx.value.type = ct;
      ctx.leaf = leaf;
    }

    // no numeric predicates.. use simple iterator
    if(!uses(Use.POS)) return new IterStep(info, axis, test, preds);

    // use iterator for simple numeric predicate
    return this instanceof IterPosStep || !useIterator() ? this : new IterPosStep(this);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    // evaluate step
    final AxisIter ai = axis.iter(checkNode(ctx));
    final NodeSeqBuilder nc = new NodeSeqBuilder();
    for(ANode n; (n = ai.next()) != null;) {
      if(test.eq(n)) nc.add(n.finish());
    }

    // evaluate predicates
    for(final Expr p : preds) {
      ctx.size = nc.size();
      ctx.pos = 1;
      int c = 0;
      for(int n = 0; n < nc.size(); ++n) {
        ctx.value = nc.get(n);
        final Item i = p.test(ctx, info);
        if(i != null) {
          // assign score value
          nc.get(n).score(i.score());
          nc.nodes[c++] = nc.get(n);
        }
        ctx.pos++;
      }
      nc.size(c);
    }
    return nc;
  }

  /**
   * Checks if this is a simple axis without predicates.
   * @param ax axis to be checked
   * @param name name/node test
   * @return result of check
   */
  public final boolean simple(final Axis ax, final boolean name) {
    return axis == ax && preds.length == 0 &&
      (name ? test.mode == Mode.NAME : test == Test.NOD);
  }

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param data data reference
   * @return resulting path nodes, or {@code null} if nodes cannot be evaluated
   */
  final ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes, final Data data) {
    // skip steps with predicates or different namespaces
    if(preds.length != 0 || data.nspaces.globalNS() == null) return null;

    // check restrictions on node type
    int kind = -1, name = 0;
    if(test.type != null) {
      kind = ANode.kind(test.type);
      if(kind == Data.PI) return null;

      if(test.mode == Mode.NAME) {
        // element/attribute test (*:ln)
        final Names names = kind == Data.ATTR ? data.atnindex : data.tagindex;
        name = names.id(((NameTest) test).ln);
      } else if(test.mode != null && test.mode != Mode.ALL) {
        // skip namespace and standard tests
        return null;
      }
    }

    // skip axes other than descendant, child, and attribute
    if(axis != Axis.ATTR && axis != Axis.CHILD && axis != Axis.DESC &&
       axis != Axis.DESCORSELF && axis != Axis.SELF) return null;

    final ArrayList<PathNode> tmp = new ArrayList<PathNode>();
    for(final PathNode n : nodes) {
      if(axis == Axis.SELF || axis == Axis.DESCORSELF) {
        if(kind == -1 || kind == n.kind && (name == 0 || name == n.name)) {
          if(!tmp.contains(n)) tmp.add(n);
        }
      }
      if(axis != Axis.SELF) add(n, tmp, name, kind);
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
  private void add(final PathNode node, final ArrayList<PathNode> nodes,
      final int name, final int kind) {

    for(final PathNode n : node.ch) {
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
  final AxisStep addPreds(final Expr... prds) {
    for(final Expr p : prds) preds = Array.add(preds, p);
    return get(info, axis, test, preds);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisStep)) return false;
    final AxisStep st = (AxisStep) cmp;
    if(preds.length != st.preds.length || axis != st.axis || !test.sameAs(st.test))
      return false;
    for(int p = 0; p < preds.length; ++p) {
      if(!preds[p].sameAs(st.preds[p])) return false;
    }
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return super.count(v);
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    // leaf flag indicates that a context node can be replaced by a text() step
    final Type ct = ctx.value != null ? ctx.value.type : null;
    final boolean leaf = ctx.leaf;
    ctx.leaf = false;
    try {
      // as predicates will not necessarily start from the document node,
      // the context item type is temporarily generalized
      if(ct == NodeType.DOC) ctx.value.type = NodeType.NOD;
      return super.inline(ctx, scp, v, e);
    } finally {
      if(ct == NodeType.DOC) ctx.value.type = ct;
      ctx.leaf = leaf;
    }
  }

  @Override
  public AxisStep copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    final Expr[] pred = new Expr[preds.length];
    for(int i = 0; i < pred.length; i++) pred[i] = preds[i].copy(ctx, scp, vs);
    return copy(new AxisStep(info, axis, test.copy(), pred));
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
      if(axis == Axis.ATTR && test != Test.NOD) sb.append('@');
      else if(axis != Axis.CHILD) sb.append(axis).append("::");
      sb.append(test);
    }
    return sb.append(super.toString()).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, preds);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : preds) sz += e.exprSize();
    return sz;
  }
}
