package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.Index;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.Filter;
import org.basex.query.xpath.expr.InterSect;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.internal.IndexMatch;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.IntList;
import static org.basex.query.xpath.XPText.*;

/**
 * LocationPath (absolute or relative).
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public abstract class LocPath extends Expr {
  /** List of location steps. */
  public Steps steps = new Steps();

  @Override
  public abstract NodeSet eval(final XPContext ctx) throws QueryException;

  @Override
  public final Expr compile(final XPContext ctx) throws QueryException {
    // At this point a data reference has to be available;
    if(ctx.local == null) throw new QueryException(NODATA);

    // replacing self::node() with child::text() for possible index integration
    if(ctx.leaf && steps.size() == 1 && steps.get(0).simple(Axis.SELF)) {
      steps.set(0, Axis.create(Axis.CHILD, TestNode.TEXT));
      ctx.compInfo(OPTTEXT);
    }

    // check if all steps yield results
    if(!steps.compile(ctx) || steps.emptyPath(ctx)) return new NodeSet(ctx);

    // remove superfluous self axes
    steps.mergeSelf(ctx);

    // merge descendant and child steps
    steps.mergeDescendant(ctx);

    // check with paths if the available indexes can be applied
    Expr result = this;

    for(int i = 0; i < steps.size(); i++) {
      final Step step = steps.get(i);
      final Preds preds = step.preds;

      // skip optimization if no preds are specified or if a pos.pred is found
      // [CG] XPath/Path optimization: enough to check first predicate?
      if(preds.size() == 0 || step.posPred() > 0) continue;
      
      // don't optimize non-forward axes
      if(invertAxis(step.axis) == null) continue;

      // find predicate with lowest number of occurrences
      int min = Integer.MAX_VALUE;
      int minP = -1; 
      for(int p = 0; p < preds.size(); p++) {
        final Pred pred = preds.get(p);
        final int nrIDs = pred.indexSizes(ctx, step, min);

        if(min > nrIDs) {
          if(nrIDs == 0) {
            ctx.compInfo(OPTLOC);
            return new NodeSet(ctx);
          }
          min = nrIDs;
          minP = p;
        }
      }
      if(min == Integer.MAX_VALUE) continue;
      // ..what is the optimal maximum for index access?
      if(this instanceof LocPathRel && min > 100) continue;
      
      // predicates that are optimized to use index and results of index queries
      final IntList oldPreds = new IntList();
      Expr indexArg = null;

      for(int p = 0; p < preds.size(); p++) {
        final Pred pred = preds.get(p);

        if(p == minP && indexArg == null) {
          oldPreds.add(p);
          indexArg = pred.indexEquivalent(ctx, step);
        }
      }

      // hold all steps following this step
      final LocPath oldPath = new LocPathRel();
      for(final int j = i + 1; j < steps.size();) {
        final Step oldStep = steps.get(j);
        steps.remove(j);
        oldPath.steps.add(oldStep);
      }

      // hold the part of the path we want to invert.
      // suggestion: all forward steps without predicates in front of the index
      final LocPath invPath = new LocPathRel();

      boolean indexMatch = true;
      for(int j = i; j >= 0; j--) {
        final Step curr = steps.get(j);
        final Axis axis = invertAxis(curr.axis);
        if(axis == null) break;

        if(j == 0) {
          if(this instanceof LocPathRel || axis == Axis.PARENT) {
            invPath.steps.add(Axis.create(axis, TestNode.NODE));
            //invPath.steps.add(Axis.get(axis, curr.test));
          } else {
            indexMatch = false;
          }
        } else {
          final Step prev = steps.get(j - 1);
          if(prev.preds.size() != 0) break;
          invPath.steps.add(Axis.create(axis, prev.test));
        }
        steps.remove(j);
      }

      int predlength = preds.size() - oldPreds.size;
      if(indexMatch || invPath.steps.size() != 0) predlength += 1;

      final Preds newPreds = new Preds();
      if(!indexMatch && invPath.steps.size() != 0) newPreds.add(invPath);

      for(int p = 0; p != step.preds.size(); p++) {
        if(!oldPreds.contains(p)) newPreds.add(step.preds.get(p));
      }
      result = new InterSect(new Expr[] { indexArg }).compile(ctx);

      // add rest of predicates
      if(newPreds.size() != 0) result =
        new Filter(result, newPreds).compile(ctx);

      // add match with initial nodes
      //if(indexMatch && checkMatch(invPath)) {
      if(indexMatch) result = new IndexMatch(this, result, invPath);
      
      // add rest of location path
      if(oldPath.steps.size() != 0) result = new Path(result, oldPath);
    }
    return result;
  }
  
  /*
   * Check if the inverted path needs to be matched.
   * @param path location path
   * @return result of check
  private boolean checkMatch(final LocPath path) {
    if(path.steps.size() != 1) return true;
    final Step step = path.steps.get(0);
    return !step.simple(Axis.ANC) && !step.simple(Axis.ANCORSELF);
  }
   */

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof LocPath)) return false;
    return steps.sameAs(((LocPath) cmp).steps);
  }

  @Override
  public final boolean usesSize() {
    return false;
  }

  @Override
  public final boolean usesPos() {
    return false;
  }

  /**
   * Limit evaluation of predicates to first hit when only existence of path has
   * to be checked...
   * @param ctx query context
   */
  public final void addPosPred(final XPContext ctx) {
    if(steps.size() != 0 && steps.last().addPosPred()) {
      ctx.compInfo(OPTPOSPRED1);
    }
  }
  
  /**
   * Checks if the path is indexable.
   * @param ctx query context
   * @param exp expression which must be a literal
   * @param cmp comparator
   * @return result of check
   */
  public final Index.TYPE indexable(final XPContext ctx, final Expr exp,
      final Comp cmp) {

    if(!(this instanceof LocPathRel && exp instanceof Literal)) return null;

    final Data data = ctx.local.data;
    final boolean txt = data.meta.txtindex && cmp == Comp.EQ;
    final boolean atv = data.meta.atvindex && cmp == Comp.EQ;
    
    final Step step = steps.last();
    final boolean text = txt && step.test == TestNode.TEXT &&
      step.preds.size() == 0;
    final boolean attr = atv && step.simpleName(Axis.ATTR, false) !=
      Integer.MIN_VALUE;
    if(!text && !attr || !checkAxes()) return null;

    return attr ? Index.TYPE.ATV : Index.TYPE.TXT;
  }

  /**
   * Checks if the specified location step has suitable index axes.
   * @return true result of check
   */
  public final boolean checkAxes() {
    for(int s = 0; s < steps.size() - 1; s++) {
      final Step curr = steps.get(s);
      // not the last text step
      if(curr.preds.size() != 0 || curr.axis != Axis.ANC
          && curr.axis != Axis.ANCORSELF && curr.axis != Axis.DESC
          && curr.axis != Axis.SELF && curr.axis != Axis.DESCORSELF
          && curr.axis != Axis.CHILD && curr.axis != Axis.PARENT) return false;
    }
    return true;
  }

  /**
   * Inverts a location path.
   * @param curr current location step
   * @return inverted path
   */
  public final LocPath invertPath(final Step curr) {
    // hold the steps to the end of the inverted path
    final LocPath path = new LocPathRel();
    final Steps stps = steps;

    // add inverted pretext steps
    Axis lastAxis = invertAxis(stps.last().axis);
    for(int k = stps.size() - 2; k >= 0; k--) {
      final Step step = stps.get(k);
      final Step inv = Axis.create(lastAxis, step.test, step.preds);
      lastAxis = invertAxis(step.axis);
      path.steps.add(inv);
    }
    path.steps.add(Axis.create(lastAxis, curr.test));
    return path;
  }

  /**
   * Inverts an XPath axis.
   * @param axis axis to be inverted.
   * @return inverted axis
   */
  private static Axis invertAxis(final Axis axis) {
    switch(axis) {
      case ANC:        return Axis.DESC;
      case ANCORSELF:  return Axis.DESCORSELF;
      case ATTR:
      case CHILD:      return Axis.PARENT;
      case DESC:       return Axis.ANC;
      case DESCORSELF: return Axis.ANCORSELF;
      case PARENT:     return Axis.CHILD;
      case SELF:       return Axis.SELF;
      default:         return null;
    }
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    steps.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FFCC33";
  }
}

