package org.basex.query.util;

import org.basex.data.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.Kind;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexInfo {
  /** Query context. */
  public final QueryContext qc;
  /** Index context. */
  public final IndexContext ic;
  /** Step with predicate that can be rewritten for index access. */
  public final Step step;

  /** Name of parent element. */
  public byte[] name;

  /** Flag for text index access. */
  public boolean text;
  /** Flag for attribute index access. */
  private boolean attr;

  /** Optimization info. */
  public String info;
  /** Index expression. */
  public Expr expr;
  /** Costs of index access: smaller is better, 0 means no results. */
  public int costs;

  /** Original expression. */
  private Expr orig;

  /**
   * Constructor.
   * @param ic index context
   * @param qc query context
   * @param step index step
   */
  public IndexInfo(final IndexContext ic, final QueryContext qc, final Step step) {
    this.qc = qc;
    this.ic = ic;
    this.step = step;
  }

  /**
   * Checks if the specified expression can be rewritten for index access.
   * @param ex expression (must be {@link Context} or {@link AxisPath})
   * @param ft full-text flag
   * @return location step or {@code null}
   */
  public boolean check(final Expr ex, final boolean ft) {
    orig = ex;

    // context reference: work with index step
    Step s = step;
    if(!(ex instanceof Context)) {
      // check if index can be applied
      if(!(ex instanceof AxisPath)) return false;
      // accept only single axis steps as first expression
      final AxisPath path = (AxisPath) ex;
      // path must contain no root node
      if(path.root != null) return false;
      // return last step
      s = path.step(path.steps.length - 1);
    }

    // check if step points to leaf element
    final Data data = ic.data;
    final boolean elem = s.test.type == NodeType.ELM;
    if(elem) {
      // only do check if database is up-to-date if no namespaces occur and if name test is used
      if(!data.meta.uptodate || data.nspaces.size() != 0 || s.test.kind != Kind.NAME) return false;
      name = s.test.name.local();
      final Stats stats = data.elemNames.stat(data.elemNames.id(name));
      if(stats == null || !stats.isLeaf()) return false;
    }

    // check for full-text index access
    if(ft) return (elem || s.test.type == NodeType.TXT) && data.meta.ftxtindex;

    // check for text or attribute index access
    text = (elem || s.test.type == NodeType.TXT) && data.meta.textindex;
    attr = !text && s.test.type == NodeType.ATT && data.meta.attrindex;
    return text || attr;
  }

  /**
   * Creates an index expression with an inverted axis path.
   * @param root new root expression
   * @param parent add parent step
   * @param ii input info
   * @param opt optimization info
   */
  public void create(final ParseExpr root, final InputInfo ii, final String opt,
      final boolean parent) {

    ParseExpr rt = root;
    if(parent && name != null) {
      final NameTest test = new NameTest(new QNm(name), Kind.NAME, false, null);
      final Step s = Step.get(ii, Axis.PARENT, test);
      rt = Path.get(ii, rt, s);
    }
    expr = invert(rt);
    info = opt;
  }

  /**
   * Rewrites the expression for index access.
   * @param root new root expression
   * @return index access
   */
  private ParseExpr invert(final ParseExpr root) {
    // handle context node
    if(orig instanceof Context) {
      // add attribute step
      if(!attr || step.test.name == null) return root;
      final Step as = Step.get(step.info, Axis.SELF, step.test);
      return Path.get(root.info, root, as);
    }

    final AxisPath origPath = (AxisPath) orig;
    final Path invPath = origPath.invertPath(root, step);

    if(attr) {
      // add attribute test as first step
      final Step at = origPath.step(origPath.steps.length - 1);
      if(at.test.name != null) {
        final ExprList steps = new ExprList(invPath.steps.length + 1);
        steps.add(Step.get(at.info, Axis.SELF, at.test)).add(invPath.steps);
        return Path.get(invPath.info, invPath.root, steps.finish());
      }
    }
    return invPath;
  }
}
