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

    // skip relative location paths
    if(this instanceof LocPathRel) return this;
    
    // skip paths with position predicates
    for(int i = 0; i < steps.size(); i++) {
      final Preds preds = steps.get(i).preds;
      for(int p = 0; p < preds.size(); p++) {
        if(preds.get(p) instanceof PredPos) return this;
      }
    }
    
    // check if the available indexes can be applied
    Expr result = this;

    MAIN:
    for(int i = 0; i < steps.size(); i++) {
      final Step step = steps.get(i);
      final Preds preds = step.preds;

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
      //if(this instanceof LocPathRel && min > ctx.item.data.size / 10)
      //  continue;

      // predicates that are optimized to use index and results of index queries
      final IntList oldPreds = new IntList();
      Expr indexArg = null;

      for(int p = 0; p < preds.size(); p++) {
        if(seq || p == minP && indexArg == null) {
          oldPreds.add(p);
          indexArg = preds.get(p).indexEquivalent(ctx, step, seq);
        }
      }
      if(seq) continue;

      // hold all steps following this step
      final LocPath oldPath = new LocPathRel();
      for(final int j = i + 1; j < steps.size();) {
        oldPath.steps.add(steps.remove(j));
      }

      // hold the part of the path we want to invert.
      // suggestion: all forward steps without predicates in front of the index
      final LocPath inv = new LocPathRel();

      for(int j = i; j >= 0; j--) {
        final Axis a = steps.get(j).axis.invert();
        if(a == null) break;

        if(j == 0) {
          if(a == Axis.PARENT) inv.steps.add(Axis.create(a, TestNode.DOC));
        } else {
          final Step prev = steps.get(j - 1);
          if(prev.preds.size() != 0) break;
          inv.steps.add(Axis.create(a, prev.test));
        }
        steps.remove(j);
      }

      final Preds newPreds = new Preds();
      if(inv.steps.size() != 0) newPreds.add(inv);

      for(int p = 0; p != step.preds.size(); p++) {
        if(!oldPreds.contains(p)) {
          final Pred pred = step.preds.get(p);
          if(pred instanceof PredSimple) {
            final Expr e = ((PredSimple) pred).expr;
            if(e instanceof FTContains) {
              // curr = null indicates that there should be no path inverting
              ((PredSimple) pred).expr = e.indexEquivalent(ctx, null, true);
            }
          }
          newPreds.add(pred);
        }
      }
      result = new InterSect(indexArg).comp(ctx);

      // add rest of predicates
      if(newPreds.size() != 0) result = new Filter(result, newPreds).comp(ctx);

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
    Axis lastAxis = stps.last().axis.invert();
    for(int k = stps.size() - 2; k >= 0; k--) {
      final Step step = stps.get(k);
      final Step inv = Axis.create(lastAxis, step.test, step.preds);
      lastAxis = step.axis.invert();
      path.steps.add(inv);
    }
    path.steps.add(Axis.create(lastAxis, curr.test));
    return path;
  }

  /**
   * Checks if the path has only single results.
   * @param ctx current context
   * @return boolean 
   */
  public boolean singlePath(final XPContext ctx) {
    final Data data = ctx.item.data;
    if(!data.meta.uptodate) return false;
    
    boolean f = false;
    for(int i = 0; i < steps.size(); i++) {
      final Step s = steps.get(i);
      if(s instanceof StepChild) {
        if(s.test instanceof TestName) {
          final TestName tn = (TestName) s.test;
          if(data.skel.desc(tn.name, false, false).size != 0) return false;
          else f = true;
        } else if(s.test == TestNode.TEXT) {
          return f;
        }
      } else return false;
    }
    return false;
  }
  
  /**
   * Get average text length for this path.
   * @param ctx current context
   * @return avg text legnth
   */
  public double avgTextLength(final XPContext ctx) {
    if (steps.size() == 1 && steps.get(0).test == TestNode.TEXT)
      return ctx.item.data.skel.tl(null);
    else if (steps.size() == 2) {
      Step s = steps.get(0);
      double avg = -1;
      if (s instanceof StepChild && s.test instanceof TestName) {
          TestName tn = (TestName) s.test;
          avg = ctx.item.data.skel.tl(tn.name); 
      } 
      s = steps.get(1);
      if (s.test == TestNode.TEXT) return avg;      
    }
    return -1;
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
