package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Cache for path results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathCache {
  /** Minimum value of {@link #threshold}. */
  private static final int MIN = 2;
  /** Maximum value of {@link #threshold}. */
  private static final int MAX = 64;

  /** Checks of the context value. */
  private enum Focus {
    /** Root of the context node. */ ROOT,
    /** Ignored context value.    */ STATIC,
    /** Identical context value.  */ IDENTITY
  }

  /** Check of the context value ({@code null} if results cannot be cached). */
  private Focus focus;
  /** Free variables of the path. */
  private final Var[] vars;
  /** Values of the free variables (compared by identity). */
  private Value[] values;
  /** Key of the context value (can be {@code null}). */
  private Value key;
  /** Number of consecutive evaluations with the current key ({@code 0}: no key). */
  private int count;
  /** Number of evaluations with the same key after which a result is cached. */
  private int threshold = MIN;
  /** Cached result (can be {@code null}). */
  private Value result;
  /** Indicates if the cached result has been returned. */
  private boolean hit;

  /**
   * Constructor.
   * @param path path
   * @param value context value of the first evaluation
   */
  PathCache(final AxisPath path, final Value value) {
    vars = path.freeVars();
    if(vars == null) return;

    final Expr root = path.root;
    if(root instanceof UtilRoot && root.arg(0) instanceof ContextValue && value instanceof XNode) {
      focus = Focus.ROOT;
    } else if(path.staticRoot) {
      focus = Focus.STATIC;
    } else if(!(value instanceof DBNode)) {
      focus = Focus.IDENTITY;
    } else {
      return;
    }
    values = new Value[vars.length];
  }

  /**
   * Returns the cached result for the current evaluation.
   * @param value context value
   * @param qc query context
   * @return cached result, or {@code null} if the path needs to be evaluated
   */
  Value get(final Value value, final QueryContext qc) {
    if(focus == null) return null;
    if(same(value, qc)) {
      if(result != null) {
        hit = true;
        return result;
      }
      count++;
    } else {
      // key has changed: back off if the cached result has never been returned
      if(result != null) threshold = hit ? MIN : Math.min(threshold << 1, MAX);
      assign(value, null, qc);
    }
    return null;
  }

  /**
   * Checks if the result of the current evaluation is to be cached.
   * @return result of check
   */
  boolean ready() {
    return focus != null && count >= threshold;
  }

  /**
   * Caches the result of the current evaluation.
   * @param value context value
   * @param res result
   * @param qc query context
   */
  void cache(final Value value, final Value res, final QueryContext qc) {
    if(shareable(res)) {
      // assign key again: the evaluation of the path may have changed it
      assign(value, res, qc);
    } else {
      focus = null;
      key = null;
      values = null;
    }
  }

  /**
   * Checks if the key of the current evaluation equals the cached key.
   * @param value context value
   * @param qc query context
   * @return result of check
   */
  private boolean same(final Value value, final QueryContext qc) {
    if(count == 0 || !switch(focus) {
      case ROOT -> value instanceof final XNode node && key instanceof final XNode root &&
        node.root().is(root);
      case STATIC -> true;
      case IDENTITY -> value == key;
    }) return false;

    final int vl = vars.length;
    for(int v = 0; v < vl; v++) {
      if(qc.get(vars[v]) != values[v]) return false;
    }
    return true;
  }

  /**
   * Assigns the key of the current evaluation and a result.
   * @param value context value
   * @param res result (can be {@code null})
   * @param qc query context
   */
  private void assign(final Value value, final Value res, final QueryContext qc) {
    key = switch(focus) {
      case ROOT -> value instanceof final XNode node ? node.root() : null;
      case STATIC -> null;
      case IDENTITY -> value;
    };
    final int vl = vars.length;
    for(int v = 0; v < vl; v++) values[v] = qc.get(vars[v]);
    result = res;
    hit = false;
    count = 1;
  }

  /**
   * Checks if the nodes of a result may be returned more than once.
   * @param res result
   * @return result of check
   */
  private static boolean shareable(final Value res) {
    // JNodes get a new identity whenever they are created
    return res.type.instanceOf(NodeType.XNODE) || !Checks.any(res, JNode.class::isInstance);
  }
}
