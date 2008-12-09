package org.basex.query.xpath.path;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.Filter;
import org.basex.query.xpath.expr.InterSect;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.internal.IndexMatch;
import org.basex.query.xpath.item.Comp;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.item.Str;
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
  public abstract Nod eval(final XPContext ctx) throws QueryException;

  @Override
  public final Expr comp(final XPContext ctx) throws QueryException {
    // at this point, a data reference must be available
    if(ctx.item == null) throw new QueryException(NODATA);

    // replacing . with text() for possible index integration
    if(ctx.leaf && steps.size() == 1 && steps.get(0).simple(Axis.SELF)) {
      steps.set(0, Axis.create(Axis.CHILD, TestNode.TEXT));
      ctx.compInfo(OPTTEXT);
    }

    // check if all steps yield results
    if(!steps.compile(ctx) || steps.emptyPath(ctx)) return new Nod(ctx);

    // remove superfluous self axes
    steps.mergeSelf(ctx);

    // merge descendant and child steps
    steps.mergeDescendant(ctx);

    // check if the available indexes can be applied
    Expr result = this;

    MAIN:
    for(int i = 0; i < steps.size(); i++) {
      final Step step = steps.get(i);
      final Preds preds = step.preds;

      // don't optimize non-invertible axes
      if(invertAxis(step.axis) == null) continue;

      // find predicate with lowest number of occurrences
      boolean pos = false;
      int min = Integer.MAX_VALUE;
      int minP = -1;
      for(int p = 0; p < preds.size(); p++) {
        final Pred pred = preds.get(p);
        final int nrIDs = pred.indexSizes(ctx, step, min);

        // zero results - predicates will always yield false
        if(nrIDs == 0) {
          ctx.compInfo(OPTLOC);
          return new Nod(ctx);
        }

        // remember cheapest index access
        if(min > nrIDs) {
          // skip step if position predicate was found before
          if(pos) continue MAIN;
          min = nrIDs;
          minP = p;
        }
        
        // check if position predicate is found
        pos |= pred.usesPos() || pred.usesSize();
      }

      // check if query has to be processed sequentially
      final boolean seq = min == Integer.MAX_VALUE;

      // ..skip index evaluation for too large results and relative paths
      // [SG] conflicts with the FTContains.iu flag
      //if(!seq && this instanceof LocPathRel && min > ctx.item.data.size / 10)
      //  continue;

      // predicates that are optimized to use index and results of index queries
      final IntList oldPreds = new IntList();
      Expr indexArg = null;

      for(int p = 0; p < preds.size(); p++) {
        final Pred pred = preds.get(p);

        if(seq || p == minP && indexArg == null) {
          oldPreds.add(p);
          indexArg = pred.indexEquivalent(ctx, step, seq);
        }
      }

      if (seq) continue;

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
        if(!oldPreds.contains(p)) {
          Pred pred = step.preds.get(p);
          if (pred instanceof PredSimple) {
            Expr e = ((PredSimple) pred).expr;
            if (e instanceof FTContains) {
              ((PredSimple) pred).expr = e.indexEquivalent(ctx, null, true);
            }
            newPreds.add(pred);
          } else if (pred instanceof PredPos) {
            invPath.steps.get(invPath.steps.size() - 1).
              addPosPred((PredPos) pred);
          } else {
            newPreds.add(pred);
          }
          
        }
      }
      result = new InterSect(new Expr[] { indexArg }).comp(ctx);

      // add rest of predicates
      if(newPreds.size() != 0) result =
        new Filter(result, newPreds).comp(ctx);

      // add match with initial nodes
      if(indexMatch) result = new IndexMatch(this, result, invPath);

      // add rest of location path
      if(oldPath.steps.size() != 0) result = new Path(result, oldPath);
    }
    return result;
  }

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
   * Limit evaluation of predicates to first hit
   * if only existence of path has to be checked.
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
  public final IndexToken indexable(final XPContext ctx, final Expr exp,
      final Comp cmp) {

    if(!(this instanceof LocPathRel && exp instanceof Str)) return null;

    final Data data = ctx.item.data;
    final boolean txt = data.meta.txtindex && cmp == Comp.EQ;
    final boolean atv = data.meta.atvindex && cmp == Comp.EQ;

    final Step step = steps.last();
    final boolean text = txt && step.test == TestNode.TEXT &&
      step.preds.size() == 0;
    final boolean attr = !text && atv && step.simpleName(Axis.ATTR, false) !=
      Integer.MIN_VALUE;

    return text || attr && checkAxes() ?
        new ValuesToken(text, ((Str) exp).str()) : null;
  }

  /**
   * Checks if the specified location step has suitable index axes.
   * @return true result of check
   */
  public final boolean checkAxes() {
    for(int s = 0; s < steps.size() - 1; s++) {
      final Step curr = steps.get(s);
      // not the last text step
      if(curr.preds.size() != 0 || curr.axis != Axis.ANC &&
         curr.axis != Axis.ANCORSELF && curr.axis != Axis.DESC &&
         curr.axis != Axis.SELF && curr.axis != Axis.DESCORSELF &&
         curr.axis != Axis.CHILD && curr.axis != Axis.PARENT) return false;
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
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    steps.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String color() {
    return "FFCC33";
  }

  @Override
  public String toString() {
    return steps.toString();
  }
}
