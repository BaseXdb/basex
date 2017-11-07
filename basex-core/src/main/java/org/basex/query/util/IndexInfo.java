package org.basex.query.util;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class IndexInfo {
  /** Query context. */
  public final QueryContext qc;
  /** Index database. */
  public final IndexDb db;
  /** Step with predicate that can be rewritten for index access. */
  public final Step step;

  /** Optimization info. */
  public String optInfo;
  /** Name test of parent element. */
  public NameTest test;
  /** Index expression. */
  public Expr expr;
  /** Costs of index access ({@code null}) if no index access is possible). */
  public IndexCosts costs;

  /** Predicate expression. */
  private Expr pred;
  /** Indicates if the last step refers to a text step. */
  private boolean text;

  /**
   * Constructor.
   * @param db index database
   * @param qc query context
   * @param step step containing the rewritable predicate
   */
  public IndexInfo(final IndexDb db, final QueryContext qc, final Step step) {
    this.qc = qc;
    this.db = db;
    this.step = step;
  }

  /**
   * Checks if the specified expression can be rewritten for index access, and returns
   * the applicable index type.
   * @param input input (if {@code null}, no optimization will be possible)
   * @param type proposed index type ({@link IndexType#TOKEN}, {@link IndexType#FULLTEXT},
   * or {@code null})
   * @return type of applicable index, or {@code null}
   */
  public IndexType type(final Expr input, final IndexType type) {
    pred = input;

    // find last step that will be evaluated before doing a comparison
    final Step last = lastStep();
    if(last == null) return null;

    final Data data = db.data();
    final boolean elem = last.test.type == NodeType.ELM;
    if(elem) {
      // stop if database is unknown/out-dated, if namespaces occur, or if name test is not simple
      if(data == null || !(data.meta.uptodate && data.nspaces.isEmpty() &&
          last.test.kind == Kind.NAME)) return null;

      test = (NameTest) last.test;
      final Stats stats = data.elemNames.stats(data.elemNames.id(test.name.local()));
      if(stats == null || !stats.isLeaf()) return null;
    }
    text = elem || last.test.type == NodeType.TXT;

    // check if the index contains result for the specified elements or attributes
    final IndexType it = type != null ? type : text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    return data == null ||
        new IndexNames(it, data).contains(qname()) && check(it, last) ? it : null;
  }

  /**
   * Tries to rewrite the specified input for index access.
   * @param search expression to find (can be {@code null})
   * @param type index type (can be {@code null})
   * @param info input info (can be {@code null})
   * @param trim normalize second string
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean create(final Expr search, final IndexType type, final InputInfo info,
      final boolean trim) throws QueryException {

    // no index or no search value: no optimization
    if(type == null || search == null) return false;

    final Data data = db.data();
    if(data == null && !enforce()) return false;

    final ParseExpr root;
    if(search.isValue()) {
      // loop through all items
      final Iter iter = qc.iter(search);
      final ArrayList<ValueAccess> tmp = new ArrayList<>();
      final TokenSet strings = new TokenSet();
      for(Item it; (it = iter.next()) != null;) {
        // only strings and untyped items are supported
        if(!it.type.isStringOrUntyped()) return false;
        // do not use text/attribute index if string is empty or too long
        byte[] string = it.string(info);
        if(trim) string = Token.trim(string);
        final int sl = string.length;
        if(type != IndexType.TOKEN && (sl == 0 || data != null && sl > data.meta.maxlen))
          return false;

        // add only expressions that yield results and that have not been requested before
        if(!strings.contains(string)) {
          strings.put(string);
          final IndexCosts c = costs(data, new StringToken(type, string));
          if(c == null) return false;
          final int r = c.results();
          if(r != 0) {
            final ValueAccess va = new ValueAccess(info, it, type, test, db).trim(trim);
            tmp.add(va);
            if(r == 1) va.seqType = va.seqType().withOcc(Occ.ZERO_ONE);
          }
          costs = IndexCosts.add(costs, c);
        }
      }
      // more than one string: merge index results
      final int vs = tmp.size();
      root = vs == 1 ? tmp.get(0) : new Union(info, tmp.toArray(new ValueAccess[vs]));
    } else {
      /* index access is not possible if returned type is not a string or untyped; if
         expression depends on context; or if it is non-deterministic. examples:
         for $x in ('a', 1) return //*[text() = $x]
         //*[text() = .]
         //*[text() = (if(random:double() < .5) then 'X' else 'Y')]
       */
      if(!search.seqType().type.isStringOrUntyped() || search.has(Flag.CTX, Flag.NDT, Flag.UPD))
        return false;

      // estimate costs (tend to worst case)
      if(data != null) costs = IndexCosts.get(Math.max(1, data.meta.size / 10));
      root = new ValueAccess(info, search, type, test, db);
    }

    create(root, false, info, Util.info(OPTINDEX_X_X, type, search));
    return true;
  }

  /**
   * Creates an index expression with an inverted axis path.
   * @param root new root expression
   * @param parent add parent step
   * @param ii input info
   * @param opt optimization info
   */
  public void create(final ParseExpr root, final boolean parent, final InputInfo ii,
      final String opt) {

    expr = invert(test == null || !parent ? root :
      Path.get(ii, root, Step.get(ii, Axis.PARENT, test)));
    optInfo = opt;
  }

  /**
   * Computes costs if the specified data reference exists.
   * @param data data reference
   * @param token index token
   * @return costs costs, or {@code null} if index access is not possible
   */
  public IndexCosts costs(final Data data, final IndexToken token) {
    return enforce() ? IndexCosts.ENFORCE : data.costs(token);
  }

  /**
   * Indicates if the index rewriting should be enforced.
   * @return result of check
   */
  public boolean enforce() {
    return qc.context.options.get(MainOptions.ENFORCEINDEX);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Checks if the specified expression can be rewritten for index access.
   * @param type index type
   * @param last last step
   * @return type of index that can be used; {@code null} otherwise
   */
  private boolean check(final IndexType type, final Step last) {
    return db.data().meta.index(type) && (
      type == IndexType.FULLTEXT ? text :
      type == IndexType.TOKEN ? !text :
      type == IndexType.TEXT ? text :
      !text && last.test.type == NodeType.ATT
    );
  }

  /**
   * Returns the local name and namespace uri of the last name test.
   * If the returned name or uri is {@code null}, it represents a wildcard.
   * <ul>
   *   <li> //*[x = 'TEXT']         -> x </li>
   *   <li> //*[x /text() = 'TEXT'] -> x </li>
   *   <li> //x[. = 'TEXT']         -> x </li>
   *   <li> //x[text() = 'TEXT']    -> x </li>
   *   <li> //*[* /@x = 'TEXT']     -> x </li>
   *   <li> //*[@x = 'TEXT']        -> x </li>
   *   <li> //@x[. = 'TEXT']        -> x </lI>
   * </ul>
   * @return local name and namespace uri. Either result, name, and uri can be {@code null}.
   *         {@code null} will be returned if the test is not a name test
   */
  private byte[][] qname() {
    Step s = step;
    if(text) {
      if(pred instanceof AxisPath) {
        // predicate is context value: return global step
        final AxisPath path = (AxisPath) pred;
        final int pl = path.steps.length;
        s = path.step(pl - 1);
        if(s.axis == Axis.CHILD && s.test == KindTest.TXT) {
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
