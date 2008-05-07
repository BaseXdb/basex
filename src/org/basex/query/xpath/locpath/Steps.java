package org.basex.query.xpath.locpath;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
import static org.basex.query.xpath.XPText.*;

/**
 * Location Steps.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Steps {
  /** Steps array. */
  private Step[] steps = new Step[4];
  /** Number of steps. */
  private int size;
  
  /**
   * Returns the number of location steps.
   * @return number of steps
   */
  public int size() {
    return size;
  }
  
  /**
   * Returns the specified step.
   * @param s step index
   * @return location step
   */
  public Step get(final int s) {
    return steps[s];
  }
  
  /**
   * Returns the last location step.
   * @return location step
   */
  public Step last() {
    return steps[size - 1];
  }
  
  /**
   * Sets the specified step.
   * @param s step index
   * @param step step to be set
   */
  public void set(final int s, final Step step) {
    steps[s] = step;
  }
  
  /**
   * Adds the specified step at the specified position.
   * @param s step index
   * @param step step to be added
   */
  public void add(final int s, final Step step) {
    if(size == steps.length) steps = Array.extend(steps);
    Array.move(steps, s, 1, size++ - s);
    steps[s] = step;
  }
  
  /**
   * Adds the specified step.
   * @param step step to be added
   */
  public void add(final Step step) {
    if(size == steps.length) steps = Array.extend(steps);
    steps[size++] = step;
  }
  
  /**
   * Removes the specified step.
   * @param s step index
   */
  public void remove(final int s) {
    Array.move(steps, s + 1, -1, --size - s);
  }
  
  /**
   * Evaluates the location steps.
   * @param ctx query context
   * @return result of check
   * @throws QueryException evaluation exception
   */
  public NodeSet eval(final XPContext ctx) throws QueryException {
    final NodeSet tmp = ctx.local;
    for(int s = 0; s < size; s++) {
      ctx.local = steps[s].eval(ctx);
      ctx.checkStop();
    }
    final NodeSet res = ctx.local;
    ctx.local = tmp;
    return res;
  }

  /**
   * Optimizes the location steps.
   * @param ctx query context
   * @return false if location step yields no results
   * @throws QueryException evaluation exception
   */
  public boolean compile(final XPContext ctx) throws QueryException {
    for(int s = 0; s < size; s++) if(!steps[s].compile(ctx)) return false;
    return true;
  }

  /**
   * Returns the location steps for equality.
   * @param stps location steps to be compared
   * @return result of check
   */
  public boolean sameAs(final Steps stps) {
    if(size != stps.size) return false;
    for(int s = 0; s < size; s++) {
      if(!steps[s].sameAs(stps.steps[s])) return false;
    }
    return true;
  }

  /**
   * Removes superfluous self axes.
   * @param ctx query context
   */
  void mergeSelf(final XPContext ctx) {
    for(int s = 0; s < size; s++) {
      if(size <= 1) return;
      if(steps[s].simple(Axis.SELF)) {
        remove(s--);
        ctx.compInfo(OPTSELF);
      }
    }
  }

  /**
   * Merges descendant and child nodes.
   * @param ctx query context
   */
  void mergeDescendant(final XPContext ctx) {
    for(int i = 0; i < size - 1; i++) {
      final Step step = steps[i];
      final Step next = steps[i + 1];

      if(step.simple(Axis.DESCORSELF) && next.axis == Axis.CHILD &&
          !next.hasPosPreds()) {
        remove(i);
        steps[i] = Axis.create(Axis.DESC, next.test, next.preds);
        ctx.compInfo(OPTMERGE);
      }
    }
  }
  
  /**
   * Checks if the location path will yield no results.
   * @param ctx query context
   * @return true result of check
   */
  boolean emptyPath(final XPContext ctx) {
    for(int s = 0; s < size; s++) {
      final Step step = steps[s];
      if(step.test instanceof TestName
          && ((TestName) step.test).id == TestName.UNKNOWN) {
        ctx.compInfo(OPTNAME, ((TestName) step.test).name);
        return true;
      }
      final Preds pred = step.preds;
      for(int p = 0; p < pred.size(); p++) {
        if(pred.get(p).posPred() == -1) {
          ctx.compInfo(OPTPOSPRED2);
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int s = 0; s < size; s++) sb.append('/' + steps[s].toString());
    return sb.toString();
  }

  /**
   * Serializes the abstract syntax tree.
   * @param ser serializer
   * @throws Exception exception
   */
  public void plan(final Serializer ser) throws Exception {
    for(int s = 0; s < size; s++) steps[s].plan(ser);
  }
}
