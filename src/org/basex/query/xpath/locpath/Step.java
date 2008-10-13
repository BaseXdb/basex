package org.basex.query.xpath.locpath;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * Single Location Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public abstract class Step extends ExprInfo {
  /** Axis. */
  public Axis axis;
  /** Node test. */
  public Test test;
  /** Predicate array. */
  public Preds preds;

  /** Result nodes. */
  protected final NodeBuilder result = new NodeBuilder();
  /** Temporary nodes. */
  private final NodeBuilder tmp = new NodeBuilder();
  /** Position predicate. */
  protected int posPred;
  /** Early evaluation. */
  private boolean early;
  /** No predicates. */
  private boolean simple;

  /**
   * Evaluates the location step.
   * @param ctx query context
   * @return the resulting node set
   * @throws QueryException evaluation exception
   */
  final NodeSet eval(final XPContext ctx) throws QueryException {
    // return if nodeset is empty
    if(ctx.item.size == 0) return ctx.item;

    result.reset();
    final Data data = ctx.item.data;
    final int[] nodes = ctx.item.nodes;

    // choose the evaluation plan
    if(simple) {
      for(int n : nodes) eval(data, n, result);
    } else if(posPred != 0) {
      for(int n : nodes) pos(ctx, data, n);
    } else if(early) {
      for(int n : nodes) early(ctx, data, n);
    } else {
      tmp.reset();
      for(int n : nodes) {
        eval(data, n, tmp);
        preds.eval(ctx, tmp, result);
      }
    }
    return new NodeSet(result.finish(), ctx);
  }

  /**
   * Standard evaluation which does not skips predicate evaluation.
   * @param d data reference
   * @param p pre value
   * @param t node set
   */
  protected abstract void eval(final Data d, final int p, final NodeBuilder t);

  /**
   * Evaluation which is applied if the first predicate is a position predicate.
   * node and stops as soon as {@link #posPred} is found.
   * @param ctx query context
   * @param d data reference
   * @param pre node
   * @throws QueryException evaluation exception
   */
  protected abstract void pos(XPContext ctx, Data d, int pre)
      throws QueryException;

  /**
   * Early evaluation which is applied if no [last()] predicate is specified.
   * @param ctx query context
   * @param d data reference
   * @param pre node
   * @throws QueryException evaluation exception
   */
  protected abstract void early(XPContext ctx, Data d, int pre)
      throws QueryException;

  /**
   * Optimizes the location step.
   * @param ctx query context
   * @return false if location steps yields an empty nodeset
   * @throws QueryException evaluation exception
   */
  final boolean compile(final XPContext ctx) throws QueryException {
    // set leaf flag if tag in location step has no further leaf tags
    final Data data = ctx.item.data;
    test.compile(data);
    if(test instanceof TestName) {
      final TestName t = (TestName) test;
      ctx.leaf = axis != Axis.ATTR && axis != Axis.SELF && t.id >= 0 &&
        !data.tags.noLeaf(t.id);
    }
    if(!preds.compile(ctx)) return false;
    ctx.leaf = false;

    // set flags for axis optimizations
    simple = preds.size() == 0;
    if(!simple) {
      posPred = preds.get(0).posPred();
      if(posPred == -1) return false;
    }
    early = true;
    for(int p = 0; p < preds.size(); p++)
      early = early && !preds.get(p).usesSize();

    return true;
  }

  /**
   * Limit evaluation of predicates to first hit when only existence
   * of path has to be checked...
   * @return true if position predicate was added
   */
  final boolean addPosPred() {
    // skip optimization if position predicate does already exist
    if(preds.size() != 0 && preds.get(0) instanceof PredPos ||
        axis == Axis.PARENT || axis == Axis.SELF) return false;
    preds.add(1, 1);
    return true;
  }

  /**
   * Checks if this is a simple axis (node test, no predicates).
   * @param ax axis to be checked
   * @return result of check
   */
  public final boolean simple(final Axis ax) {
    return axis == ax && test == TestNode.NODE && preds.size() == 0;
  }

  /**
   * Checks if this is a simple name axis (no predicates).
   * @param ax axis to be checked
   * @param name name reference needed
   * @return name id or {@link Integer#MIN_VALUE} if test was negative
   */
  public final int simpleName(final Axis ax, final boolean name) {
    return axis == ax && test instanceof TestName && preds.size() == 0
        && (name ? ((TestName) test).id > 0 : true) ? ((TestName) test).id
        : Integer.MIN_VALUE;
  }

  /**
   * Checks whether this LocationStep has any position predicates.
   * @return false if no Position predicate
   */
  final boolean hasPosPreds() {
    for(int p = 0; p < preds.size(); p++) {
      final Pred pred = preds.get(p);
      if(pred.posPred() > 0 || pred.usesPos()) return true;
    }
    return false;
  }

  /**
   * Checks location steps for equality.
   * @param step location step
   * @return false if no Position predicate
   */
  public final boolean sameAs(final Step step) {
    if(axis != Axis.CHILD && axis != Axis.SELF && axis != Axis.ATTR)
      return false;
    if(axis != step.axis || !step.test.sameAs(test)) return false;
    return preds.sameAs(step.preds);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(this instanceof StepAttr && test != TestNode.NODE) {
      sb.append("@");
      sb.append(test);
    } else if(this instanceof StepParent && test == TestNode.NODE) {
      sb.append("..");
    } else if(this instanceof StepSelf && test == TestNode.NODE) {
      sb.append(".");
    } else {
      if(!(this instanceof StepChild)) sb.append(axis.name + "::");
      sb.append(test);
    }
    for(int p = 0; p < preds.size(); p++)
      sb.append(preds.get(p));
    return sb.toString();
  }

  /**
   * Returns the position predicate of the specified predicate array
   * or 0 if no position predicate is specified.
   * @return position predicate
   */
  protected final int posPred() {
    return preds.size() != 0 ? preds.get(0).posPred() : 0;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.startElement(this);
    ser.attribute(Token.token("test"), Token.token(test.toString()));
    if(simple) ser.attribute(Token.token("simple"), Token.TRUE);
    else if(early) ser.attribute(Token.token("early"), Token.TRUE);
    else if(posPred != 0) ser.attribute(Token.token("pos"), Token.TRUE);
    if(preds.size() != 0) {
      ser.finishElement();
      preds.plan(ser);
      ser.closeElement();
    } else {
      ser.emptyElement();
    }
  }

  @Override
  public final String color() {
    return "FFFF66";
  }
}
