package org.basex.query.path;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.PathNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Preds;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Name;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Location Step expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
    return get(s.input, s.axis, s.test, s.pred);
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
  protected AxisStep(final InputInfo ii, final Axis a, final Test t,
      final Expr... p) {
    super(ii, p);
    axis = a;
    test = t;
    type = SeqType.NOD_ZM;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(!test.comp(ctx)) return Empty.SEQ;

    // if possible, add text() step to predicates
    final Data data = ctx.resource.data();
    ctx.leaf = false;
    if(data != null && test.test == Name.NAME && test.type != NodeType.ATT) {
      final byte[] ln = ((NameTest) test).ln;
      ctx.leaf = axis.down && data.meta.uptodate && data.ns.size() == 0 &&
        data.tags.stat(data.tags.id(ln)).leaf;
    }

    // as predicates will not necessarily start from the document node,
    // a document context item is temporarily set to element
    final Type ct = ctx.value != null ? ctx.value.type : null;
    if(ct == NodeType.DOC) ctx.value.type = NodeType.ELM;

    final Expr e = super.comp(ctx);
    if(ct != null) ctx.value.type = ct;
    ctx.leaf = false;

    // return optimized step / don't re-optimize step
    if(e != this || e instanceof IterStep) return e;

    // no positional predicates.. use simple iterator
    if(!uses(Use.POS)) return new IterStep(input, axis, test, pred);

    // don't re-optimize step
    if(this instanceof IterPosStep) return this;

    // use iterator for simple positional predicate
    return iterable() ? new IterPosStep(this) : this;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final Value v = checkCtx(ctx);
    if(!v.node()) NODESPATH.thrw(input, AxisStep.this, v.type);
    final AxisIter ai = axis.iter((ANode) v);

    final NodeCache nb = new NodeCache();
    ANode node;
    while((node = ai.next()) != null) {
      if(test.eval(node)) nb.add(node.finish());
    }

    // evaluate predicates
    for(final Expr p : pred) {
      ctx.size = nb.size();
      ctx.pos = 1;
      int c = 0;
      for(int n = 0; n < nb.size(); ++n) {
        ctx.value = nb.get(n);
        final Item i = p.test(ctx, input);
        if(i != null) {
          // assign score value
          nb.get(n).score(i.score());
          nb.item[c++] = nb.get(n);
        }
        ctx.pos++;
      }
      nb.size(c);
    }
    return nb;
  }

  /**
   * Checks if this is a simple axis without predicates.
   * @param ax axis to be checked
   * @param name name/node test
   * @return result of check
   */
  public final boolean simple(final Axis ax, final boolean name) {
    return axis == ax && pred.length == 0 &&
      (name ? test.test == Test.Name.NAME : test == Test.NOD);
  }

  /**
   * Returns all path nodes that yield results for this step.
   * @param nodes input nodes
   * @param data data reference
   * @return path nodes, or {@code null} if size cannot be evaluated
   */
  final ArrayList<PathNode> size(final ArrayList<PathNode> nodes,
      final Data data) {

    // skip steps with predicates or different namespaces
    if(pred.length != 0 || data.ns.globalNS() == null) return null;

    // check restrictions on node type
    int kind = -1, name = 0;
    if(test.type != null) {
      kind = ANode.kind(test.type);
      // skip processing instructions and attributes
      if(kind == Data.PI || kind == Data.ATTR) return null;

      if(test.test == Name.NAME) {
        // element test (*:ln)
        name = data.tags.id(((NameTest) test).ln);
      } else if(test.test != null && test.test != Name.ALL) {
        // skip namespace and standard tests
        return null;
      }
    }

    // skip axes other than descendant and child
    final boolean desc = axis == Axis.DESC;
    if(!desc && axis != Axis.CHILD) return null;

    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode pn : nodes) {
      data.pthindex.desc(pn, out, name, kind, desc);
    }
    return out;
  }

  /**
   * Adds predicates to the step.
   * @param preds predicates to be added
   * @return resulting step instance
   */
  final AxisStep addPreds(final Expr... preds) {
    for(final Expr p : preds) pred = Array.add(pred, p);
    return get(input, axis, test, pred);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisStep)) return false;
    final AxisStep st = (AxisStep) cmp;
    if(pred.length != st.pred.length || axis != st.axis ||
        !test.sameAs(st.test)) return false;
    for(int p = 0; p < pred.length; ++p) {
      if(!pred[p].sameAs(st.pred[p])) return false;
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
      if(axis == Axis.SELF) sb.append(".");
    }
    if(sb.length() == 0) {
      if(axis == Axis.ATTR) sb.append("@");
      else if(axis != Axis.CHILD) sb.append(axis + "::");
      sb.append(test);
    }
    return sb.append(super.toString()).toString();
  }
}
