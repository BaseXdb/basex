package org.basex.query.util.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;

/**
 * Index predicate: path expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
class IndexPath extends IndexPred {
  /** Path expression. */
  private final AxisPath path;
  /** Index of the last relevant step. */
  private int last;

  /**
   * Constructor.
   * @param path path expression
   * @param info index info
   */
  IndexPath(final AxisPath path, final IndexInfo info) {
    super(info);
    this.path = path;
    last = path.steps.length - 1;
  }

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[b = 'A']}        → {@code b}</li>
   *   <li>{@code /xml/a[b/text() = 'A']} → {@code text()}</li>
   *   <li>{@code /xml/a[text() = 'A']}   → {@code text()}</li>
   * </ul>
   * @return step or {@code null}
   */
  @Override
  Step step() {
    if(path.root != null) return null;
    // give up if one of the steps contains positional predicates
    final int sl = path.steps.length;
    for(int s = 0; s < sl; s++) {
      if(path.step(s).mayBePositional()) return null;
    }
    // return last step
    return path.step(last);
  }

  /**
   * Returns the local name and namespace URI of the last name test. Examples:
   * <ul>
   *   <li> //*[x = 'TEXT']         → x </li>
   *   <li> //*[x /text() = 'TEXT'] → x </li>
   *   <li> //x[text() = 'TEXT']    → x </li>
   *   <li> //*[* /@x = 'TEXT']     → x </li>
   *   <li> //*[@x = 'TEXT']        → x </li>
   * </ul>
   * @return step with name
   */
  @Override
  Step qname() {
    final Step st = step(last);
    return info.text && st.axis == Axis.CHILD && st.test == NodeTest.TEXT ? step(last - 1) : st;
  }

  @Override
  Step dropText() {
    return step(--last);
  }

  @Override
  Expr invert(final Expr root) throws QueryException {
    // the text step was dropped: the addressed element is the step with the predicate
    if(last < 0) return root;

    final CompileContext cc = info.cc;
    final ExprList steps = new ExprList();

    // choose new root expression: add predicates of last step to root
    int s = last;
    final Step lastStep = step(s);
    final Expr rt = lastStep.exprs.length > 0 ?
      Filter.get(cc, path.info(), root, lastStep.exprs) : root;

    // attribute index request: start inverted path with attribute step
    if(!info.text && (lastStep.test instanceof NameTest || lastStep.test instanceof UnionTest)) {
      steps.add(Step.self(cc, rt, lastStep.info(), lastStep.test));
    }
    // add inverted steps in reverse order
    while(--s >= 0) {
      final Step st = step(s);
      steps.add(Step.get(cc, rt, st.info(), step(s + 1).axis.invert(), st.test, st.exprs));
    }
    // add root step without predicates
    final Step st = step(s);
    steps.add(Step.get(cc, rt, st.info(), step(s + 1).axis.invert(), st.test));

    return Path.get(cc, path.info(), rt, steps.finish());
  }

  /**
   * Returns the specified step or the parent step.
   * @param index step index (if negative, parent step will be returned)
   * @return step
   */
  private Step step(final int index) {
    return index < 0 ? info.step : path.step(index);
  }
}
