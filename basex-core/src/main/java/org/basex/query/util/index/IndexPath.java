package org.basex.query.util.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;

/**
 * Index predicate: path expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class IndexPath extends IndexPred {
  /** Path expression. */
  private final AxisPath path;

  /**
   * Constructor.
   * @param path path expression
   * @param ii index info
   */
  IndexPath(final AxisPath path, final IndexInfo ii) {
    super(ii);
    this.path = path;
  }

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[b = 'A']}         ->  {@code b}</li>
   *   <li>{@code /xml/a[b/text() = 'A']}  ->  {@code text()}</li>
   *   <li>{@code /xml/a[text() = 'A']}    ->  {@code text()}</li>
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
    return path.step(sl - 1);
  }

  /**
   * Returns the local name and namespace uri of the last name test. Examples:
   * <ul>
   *   <li> //*[x = 'TEXT']          ->  x </li>
   *   <li> //*[x /text() = 'TEXT']  ->  x </li>
   *   <li> //x[text() = 'TEXT']     ->  x </li>
   *   <li> //*[* /@x = 'TEXT']      ->  x </li>
   *   <li> //*[@x = 'TEXT']         ->  x </li>
   * </ul>
   * @return step with name
   */
  @Override
  Step qname() {
    final int s = path.steps.length - 1;
    final Step st = step(s);
    return ii.text && st.axis == Axis.CHILD && st.test == KindTest.TXT ? step(s - 1) : st;
  }

  @Override
  Expr invert(final Expr root) throws QueryException {
    final CompileContext cc = ii.cc;
    final ExprList steps = new ExprList();

    // choose new root expression: add predicates of last step to root
    int s = path.steps.length - 1;
    final Step last = step(s);
    final Expr rt = last.exprs.length == 0 ? root : Filter.get(cc, path.info, root, last.exprs);

    // attribute index request: start inverted path with attribute step
    if(!ii.text && (last.test instanceof NameTest || last.test instanceof UnionTest)) {
      steps.add(Step.get(cc, rt, last.info, last.test));
    }
    // add inverted steps in reverse order
    while(--s >= 0) {
      final Step st = step(s);
      steps.add(Step.get(cc, rt, st.info, step(s + 1).axis.invert(), st.test, st.exprs));
    }
    // add root step without predicates
    final Step st = step(s);
    steps.add(Step.get(cc, rt, st.info, step(s + 1).axis.invert(), st.test));

    return Path.get(cc, path.info, rt, steps.finish());
  }

  /**
   * Returns the specified step or the parent step.
   * @param index step index (if negative, parent step will be returned)
   * @return step
   */
  private Step step(final int index) {
    return index < 0 ? ii.step : path.step(index);
  }
}
