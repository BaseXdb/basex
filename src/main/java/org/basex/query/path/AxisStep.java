package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.index.Names;
import org.basex.index.Stats;
import org.basex.index.path.PathNode;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Preds;
import org.basex.query.item.ANode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Name;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.list.ObjList;

/**
 * Location Step expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class AxisStep extends Preds {
  /** Axis. */
  Axis axis;
  /** Node test. */
  public Test test;

  /**
   * This method creates a copy from the specified step.
   * @param s step to be copied
   * @return step
   */
  public static AxisStep get(final AxisStep s) {
    return get(s.input, s.axis, s.test, s.preds);
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
    for(final Expr pr : p) num |= pr.type().mayBeNum() || pr.uses(Use.POS);
    return num ? new AxisStep(ii, a, t, p) : new IterStep(ii, a, t, p);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  AxisStep(final InputInfo ii, final Axis a, final Test t,
           final Expr... p) {
    super(ii, p);
    axis = a;
    test = t;
    type = SeqType.NOD_ZM;
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    if(!test.comp(ctx)) return Empty.SEQ;

    // leaf flag indicates that a context node can be replaced by a text() step
    final Data data = ctx.data();
    ctx.leaf = data != null &&
      test.test == Name.NAME && test.type != NodeType.ATT && axis.down &&
      data.meta.uptodate && data.nspaces.size() == 0;
    if(ctx.leaf) {
      final Stats s =
        data.tagindex.stat(data.tagindex.id(((NameTest) test).ln));
      ctx.leaf = s != null && s.leaf;
    }

    // as predicates will not necessarily start from the document node,
    // the context item type is temporarily generalized
    final Type ct = ctx.value != null ? ctx.value.type : null;
    if(ct == NodeType.DOC) ctx.value.type = NodeType.NOD;
    final Expr e = super.comp(ctx);
    if(ct == NodeType.DOC) ctx.value.type = ct;
    ctx.leaf = false;

    // return optimized step / don't re-optimize step
    if(e != this || e instanceof IterStep) return e;

    // no numeric predicates.. use simple iterator
    if(!uses(Use.POS)) return new IterStep(input, axis, test, preds);

    // don't re-optimize step
    if(this instanceof IterPosStep) return this;

    // use iterator for simple numeric predicate
    return useIterator() ? new IterPosStep(this) : this;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final Value v = checkCtx(ctx);
    if(!v.type.isNode()) NODESPATH.thrw(input, this, v.type);
    final AxisIter ai = axis.iter((ANode) v);

    final NodeCache nc = new NodeCache();
    for(ANode n; (n = ai.next()) != null;) if(test.eval(n)) nc.add(n.finish());

    // evaluate predicates
    for(final Expr p : preds) {
      ctx.size = nc.size();
      ctx.pos = 1;
      int c = 0;
      for(int n = 0; n < nc.size(); ++n) {
        ctx.value = nc.get(n);
        final Item i = p.test(ctx, input);
        if(i != null) {
          // assign score value
          nc.get(n).score(i.score());
          nc.item[c++] = nc.get(n);
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
      (name ? test.test == Name.NAME : test == Test.NOD);
  }

  /**
   * Returns the path nodes that are the result of this step.
   * @param nodes initial path nodes
   * @param data data reference
   * @return resulting path nodes, or {@code null} if nodes cannot be evaluated
   */
  final ObjList<PathNode> nodes(final ObjList<PathNode> nodes,
      final Data data) {

    // skip steps with predicates or different namespaces
    if(preds.length != 0 || data.nspaces.globalNS() == null) return null;

    // check restrictions on node type
    int kind = -1, name = 0;
    if(test.type != null) {
      kind = ANode.kind(test.type);
      if(kind == Data.PI) return null;

      if(test.test == Name.NAME) {
        // element/attribute test (*:ln)
        final Names names = kind == Data.ATTR ? data.atnindex : data.tagindex;
        name = names.id(((NameTest) test).ln);
      } else if(test.test != null && test.test != Name.ALL) {
        // skip namespace and standard tests
        return null;
      }
    }

    // skip axes other than descendant, child, and attribute
    if(axis != Axis.ATTR && axis != Axis.CHILD && axis != Axis.DESC &&
       axis != Axis.DESCORSELF && axis != Axis.SELF) return null;

    final ObjList<PathNode> tmp = new ObjList<PathNode>();
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
  private void add(final PathNode node, final ObjList<PathNode> nodes,
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
    return get(input, axis, test, preds);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisStep)) return false;
    final AxisStep st = (AxisStep) cmp;
    if(preds.length != st.preds.length || axis != st.axis ||
        !test.sameAs(st.test)) return false;
    for(int p = 0; p < preds.length; ++p) {
      if(!preds[p].sameAs(st.preds[p])) return false;
    }
    return true;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(AXIS, Token.token(axis.name));
    ser.attribute(TEST, Token.token(test.toString()));
    super.plan(ser);
    ser.closeElement();
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
}
