package org.basex.query.path;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract axis path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AxisPath extends Path {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  AxisPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  /**
   * Inverts a location path.
   * @param rt new root node
   * @param curr current location step
   * @return inverted path
   */
  public final Path invertPath(final Expr rt, final Step curr) {
    // add predicates of last step to new root node
    int s = steps.length - 1;
    final Expr r = step(s).preds.length == 0 ? rt : Filter.get(info, rt, step(s).preds);

    // add inverted steps in a backward manner
    final ExprList stps = new ExprList();
    while(--s >= 0) {
      stps.add(Step.get(info, step(s + 1).axis.invert(), step(s).test, step(s).preds));
    }
    stps.add(Step.get(info, step(s + 1).axis.invert(), curr.test));
    return Path.get(info, r, stps.array());
  }

  /**
   * Returns the specified axis step.
   * @param i index
   * @return step
   */
  public final Step step(final int i) {
    return (Step) steps[i];
  }

  /**
   * Returns the path nodes that will result from this path.
   * @param qc query context
   * @return path nodes, or {@code null} if nodes cannot be evaluated
   */
  public ArrayList<PathNode> nodes(final QueryContext qc) {
    final Value init = initial(qc);
    final Data data = init != null && init.type == NodeType.DOC ? init.data() : null;
    if(data == null || !data.meta.uptodate) return null;

    ArrayList<PathNode> nodes = data.paths.root();
    for(int s = 0; s < steps.length; s++) {
      final Step curr = axisStep(s);
      if(curr == null) return null;
      nodes = curr.nodes(nodes, data);
      if(nodes == null) return null;
    }
    return nodes;
  }

  @Override
  public final boolean iterable() {
    return true;
  }

  /**
   * Guesses if the evaluation of this axis path is cheap. This is used to determine if it
   * can be inlined into a loop to enable index rewritings.
   * @return guess
   */
  public boolean cheap() {
    if(!(root instanceof ANode) || ((Value) root).type != NodeType.DOC) return false;
    final Axis[] expensive = { Axis.DESC, Axis.DESCORSELF, Axis.PREC, Axis.PRECSIBL,
        Axis.FOLL, Axis.FOLLSIBL };
    for(int i = 0; i < steps.length; i++) {
      final Step s = step(i);
      if(i < 2) for(final Axis a : expensive) if(s.axis == a) return false;
      final Expr[] ps = s.preds;
      if(!(ps.length == 0 || ps.length == 1 && ps[0] instanceof Pos)) return false;
    }
    return true;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof AxisPath)) return false;
    final AxisPath ap = (AxisPath) cmp;
    if((root == null || ap.root == null) && root != ap.root ||
        steps.length != ap.steps.length || root != null && !root.sameAs(ap.root)) return false;

    for(int s = 0; s < steps.length; ++s) {
      if(!steps[s].sameAs(ap.steps[s])) return false;
    }
    return true;
  }
}
