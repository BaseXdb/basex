package org.basex.query.util;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.Kind;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IndexInfo {
  /** Query context. */
  public final QueryContext qc;
  /** Index context. */
  public final IndexContext ic;
  /** Step with predicate that can be rewritten for index access. */
  public final Step step;

  /** Name test of parent element. */
  public NameTest test;
  /** Indicates if the last step refers to a text step. */
  public boolean text;

  /** Optimization info. */
  public String info;
  /** Index expression. */
  public Expr expr;
  /** Costs of index access. 0 = no results; 1 = exactly one results;
   * all other values may be estimates (the smaller, the better). */
  public int costs;

  /** Predicate expression. */
  private Expr pred;

  /**
   * Constructor.
   * @param ic index context
   * @param qc query context
   * @param step step containing the rewritable predicate
   */
  public IndexInfo(final IndexContext ic, final QueryContext qc, final Step step) {
    this.qc = qc;
    this.ic = ic;
    this.step = step;
  }

  /**
   * Checks if the specified expression can be rewritten for index access.
   * @param pr predicate expression (must be {@link ContextValue} or {@link AxisPath})
   * @param ft full-text flag
   * @return result of check
   */
  public boolean check(final Expr pr, final boolean ft) {
    pred = pr;

    // check if step points to leaf element
    final Step last = lastStep();
    if(last == null) return false;

    final Data data = ic.data;
    final boolean elem = last.test.type == NodeType.ELM;
    if(elem) {
      // give up if database is out-dated, if namespaces occur, or if name test is not simple
      if(!(data.meta.uptodate && data.nspaces.isEmpty() && last.test.kind == Kind.NAME))
        return false;

      test = (NameTest) last.test;
      final Stats stats = data.elemNames.stat(data.elemNames.id(test.name.local()));
      if(stats == null || !stats.isLeaf()) return false;
    }
    text = elem || last.test.type == NodeType.TXT;

    // check if the index contains result for the specified elements or attributes
    final IndexNames in = new IndexNames(ft ? data.meta.ftinclude : text ? data.meta.textinclude :
      data.meta.attrinclude);
    if(!in.contains(qname())) return false;

    // full-text index
    if(ft) return text && data.meta.ftindex;
    // text index
    if(text) return data.meta.textindex;
    // attribute index
    return last.test.type == NodeType.ATT && data.meta.attrindex;
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

    expr = invert(test == null || !parent ? root :
      Path.get(ii, root, Step.get(ii, Axis.PARENT, test)));
    info = opt;
  }

  /**
   * Returns the local name and namespace uri of the last name test.
   * If the returned name or uri is null, it represents a wildcard.
   * <ul>
   *   <li> //*[x = 'TEXT']         -> x </li>
   *   <li> //*[x /text() = 'TEXT'] -> x </li>
   *   <li> //x[. = 'TEXT']         -> x </li>
   *   <li> //x[text() = 'TEXT']    -> x </li>
   *   <li> //*[* /@x = 'TEXT']     -> x </li>
   *   <li> //*[@x = 'TEXT']        -> x </li>
   *   <li> //@x[. = 'TEXT']        -> x </lI>
   * </ul>
   * @return local name and namespace uri (result, or name or uri, can be {@code null})
   */
  private byte[][] qname() {
    Step s = step;
    if(text) {
      if(pred instanceof AxisPath) {
        // predicate is context value: return global step
        final AxisPath path = (AxisPath) pred;
        final int pl = path.steps.length;
        s = path.step(pl - 1);
        if(s.axis == Axis.CHILD && s.test == Test.TXT) {
          s = pl > 1 ? path.step(pl - 2) : step;
        }
      }
    } else {
      // expression in predicate is context value: return global step
      if(pred instanceof AxisPath) {
        final AxisPath path = (AxisPath) pred;
        s = path.step(path.steps.length - 1);
      }
    }
    // give up if test is not a name test
    if(!(s.test instanceof NameTest)) return null;

    // return local name and namespace uri (null represents wildcards)
    final NameTest nt = (NameTest) s.test;
    return new byte[][] { nt.local, nt.name == null ? null : nt.name.uri() };
  }

  /**
   * Rewrites the expression for index access.
   * @param root new root expression
   * @return index access
   */
  private ParseExpr invert(final ParseExpr root) {
    // handle context node
    if(pred instanceof ContextValue) {
      // add attribute step
      if(text || step.test.name == null) return root;
      final Step as = Step.get(step.info, Axis.SELF, step.test);
      return Path.get(root.info, root, as);
    }

    final AxisPath origPath = (AxisPath) pred;
    final Path invPath = origPath.invertPath(root, step);

    if(!text) {
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

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[b = 'A']} -> {@code b}</li>
   *   <li>{@code /xml/a[b/text() = 'A']} -> {@code text()}</li>
   *   <li>{@code /xml/a[. = 'A']} -> {@code a}</li>
   *   <li>{@code /xml/a[text() = 'A']} -> {@code text()}</li>
   *   <li>{@code /xml/a/text()[. = 'A']} -> {@code text()}</li>
   * </ul>
   * @return step
   */
  private Step lastStep() {
    // expression in predicate is context value: return global step
    if(pred instanceof ContextValue) return step;
    // give up if expression is not an axis path
    if(!(pred instanceof AxisPath)) return null;
    // give up if path contains is not relative
    final AxisPath path = (AxisPath) pred;
    if(path.root != null) return null;
    // return last step
    final Step s = path.step(path.steps.length - 1);
    // give up if step contains numeric predicate
    if(s.has(Flag.POS)) return null;
    // success: return step
    return s;
  }
}
