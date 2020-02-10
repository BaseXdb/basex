package org.basex.query.util.index;

import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;

/**
 * Index predicate: path expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class IndexPath extends IndexPred {
  /** Path expression. */
  private AxisPath path;

  /**
   * Constructor.
   * @param path path expression
   */
  IndexPath(final AxisPath path) {
    this.path = path;
  }

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[b = 'A']}        -> {@code b}</li>
   *   <li>{@code /xml/a[b/text() = 'A']} -> {@code text()}</li>
   *   <li>{@code /xml/a[text() = 'A']}   -> {@code text()}</li>
   * </ul>
   * @param ii index info
   * @return step or {@code null}
   */
  @Override
  Step step(final IndexInfo ii) {
    if(path.root != null) return null;
    // give up if one of the steps contains positional predicates
    final int sl = path.steps.length;
    for(int s = 0; s < sl; s++) {
      if(path.step(s).positional()) return null;
    }
    // return last step
    return path.step(sl - 1);
  }

  /**
   * Returns the local name and namespace uri of the last name test. Examples:
   * <ul>
   *   <li> //*[x = 'TEXT']         -> x </li>
   *   <li> //*[x /text() = 'TEXT'] -> x </li>
   *   <li> //x[text() = 'TEXT']    -> x </li>
   *   <li> //*[* /@x = 'TEXT']     -> x </li>
   *   <li> //*[@x = 'TEXT']        -> x </li>
   * </ul>
   * @param ii index info
   * @return step with name
   */
  @Override
  Step qname(final IndexInfo ii) {
    final int pl = path.steps.length;
    Step st = path.step(pl - 1);
    // if last step matches text nodes: use previous step
    if(ii.text && st.axis == Axis.CHILD && st.test == KindTest.TXT) {
      st = pl > 1 ? path.step(pl - 2) : ii.step;
    }
    return st;
  }

  @Override
  Path invert(final ParseExpr root, final IndexInfo ii) {
    Path invPath = path.invertPath(root, ii.step);
    if(!ii.text) {
      // attribute index request: start inverted path with attribute step
      final Step st = path.step(path.steps.length - 1);
      if(st.test instanceof NameTest || st.test instanceof UnionTest) {
        final ExprList steps = new ExprList(invPath.steps.length + 1);
        steps.add(Step.get(st.info, Axis.SELF, st.test)).add(invPath.steps);
        invPath = Path.get(invPath.info, invPath.root, steps.finish());
      }
    }
    return invPath;
  }
}
