package org.basex.query.util;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param type index type, predefined by the called expression (can be {@code null})
   * @return resulting index type, or {@code null} if index access is not possible
   */
  public IndexType type(final Expr input, final IndexType type) {
    pred = input;

    // find last step that will be evaluated before doing a comparison
    final Step last = lastStep();
    if(last == null) return null;

    final Data data = db.data();
    if(last.test.type == NodeType.TXT) {
      text = true;
    } else if(last.test.type == NodeType.ELM) {
      // ensure that addressed elements only have text nodes as children
      // stop if database is unknown/out-dated, if namespaces occur, or if name test is not simple
      if(data == null || !(data.meta.uptodate && data.nspaces.isEmpty()) ||
          !(last.test instanceof NameTest)) return null;

      test = (NameTest) last.test;
      if(test.part != NamePart.LOCAL) return null;

      final Stats stats = data.elemNames.stats(data.elemNames.id(test.qname.local()));
      if(stats == null || !stats.isLeaf()) return null;
      text = true;
    } else if(last.test.type != NodeType.ATT) {
      // other tests cannot be rewritten for index access
      return null;
    }

    // check if the index contains result for the specified elements or attributes
    final IndexType it = type != null ? type : text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    if(text ? (it != IndexType.TEXT && it != IndexType.FULLTEXT) :
      (it != IndexType.TOKEN && it != IndexType.ATTRIBUTE)) return null;

    // reject index access if database is known at runtime, and if it does not match all criteria
    return data == null || new IndexNames(it, data).contains(qname()) && data.meta.index(it) ?
      it : null;
  }

  /**
   * Tries to rewrite the specified input for index access.
   * @param search expression to find (can be {@code null})
   * @param type index type (can be {@code null})
   * @param trim normalize second string
   * @param ii input info (can be {@code null})
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean create(final Expr search, final IndexType type, final boolean trim,
      final InputInfo ii) throws QueryException {

    // no index or no search value: no optimization
    if(type == null || search == null) return false;

    final Data data = db.data();
    if(data == null && !enforce()) return false;

    final ParseExpr root;
    if(search instanceof Value) {
      // loop through all items
      final Iter iter = search.iter(qc);
      final TokenIntMap cache = new TokenIntMap();
      for(Item item; (item = qc.next(iter)) != null;) {
        // only strings and untyped items are supported
        if(!item.type.isStringOrUntyped()) return false;
        // do not use text/attribute index if string is empty or too long
        byte[] token = item.string(ii);
        if(trim) token = Token.trim(token);
        final int sl = token.length;
        if(type != IndexType.TOKEN && (sl == 0 || data != null && sl > data.meta.maxlen))
          return false;

        // only cache distinct tokens that have not been requested before
        if(!cache.contains(token)) {
          final IndexCosts ic = costs(data, new StringToken(type, token));
          if(ic == null) return false;
          cache.put(token, ic.results());
          costs = IndexCosts.add(costs, ic);
        }
      }

      // ignore expressions that yield no results
      final TokenSet tokens = new TokenSet();
      int counts = 0;
      for(final byte[] token : cache) {
        final int count = cache.get(token);
        if(count != 0) tokens.add(token);
        if(counts >= 0) counts = count >= 0 ? counts + count : -1;
      }

      // create expression for index access
      final ValueAccess va = new ValueAccess(ii, tokens, type, test, db);
      if(counts == 1) va.exprType.assign(Occ.ZERO_ONE);
      root = va;

    } else {
      /* index access is not possible if returned type is not a string or untyped; if
       * expression depends on context; or if it is non-deterministic. examples:
       * - for $x in ('a', 1) return //*[text() = $x]
       * - //*[text() = .]
       * - //*[text() = (if(random:double() < .5) then 'X' else 'Y')] */
      if(!search.seqType().type.isStringOrUntyped() || search.has(Flag.CTX, Flag.NDT))
        return false;

      // estimate costs for dynamic query terms
      costs = enforce() ? IndexCosts.ENFORCE_DYNAMIC :
        IndexCosts.get(Math.max(1, data.meta.size / 10));
      root = new ValueAccess(ii, search, type, test, db);
    }

    create(root, false, Util.info(OPTINDEX_X_X, type, search), ii);
    return true;
  }

  /**
   * Creates an index expression with an inverted axis path.
   * @param root new root expression
   * @param parent add parent step
   * @param opt optimization info
   * @param ii input info
   */
  public void create(final ParseExpr root, final boolean parent, final String opt,
      final InputInfo ii) {

    expr = invert(test == null || !parent ? root :
      Path.get(ii, root, Step.get(ii, Axis.PARENT, test)));
    optInfo = opt;
  }

  /**
   * Computes costs if the specified data reference exists.
   * @param data data reference
   * @param search index search definition
   * @return costs costs, or {@code null} if index access is not possible
   */
  public IndexCosts costs(final Data data, final IndexSearch search) {
    return data != null ? data.costs(search) : IndexCosts.ENFORCE_STATIC;
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
    Step st = step;

    // if predicate is axis path, extract last step
    if(pred instanceof AxisPath) {
      final AxisPath path = (AxisPath) pred;
      final int pl = path.steps.length;
      st = path.step(pl - 1);
      // if last step matches text nodes: use previous step
      if(text && st.axis == Axis.CHILD && st.test == KindTest.TXT) {
        st = pl > 1 ? path.step(pl - 2) : step;
      }
    }

    // give up if test is not a name test
    if(!(st.test instanceof NameTest)) return null;

    // return local name and namespace uri (null represents wildcards)
    final NameTest nt = (NameTest) st.test;
    return new byte[][] { nt.local, nt.qname == null ? null : nt.qname.uri() };
  }

  /**
   * Rewrites the expression for index access.
   * @param root new root expression
   * @return index access
   */
  private ParseExpr invert(final ParseExpr root) {
    // rewrite context node
    if(pred instanceof ContextValue) {
      if(text || !(step.test instanceof NameTest || step.test instanceof UnionTest)) return root;
      // attribute index request: add attribute step
      return Path.get(root.info, root, Step.get(step.info, Axis.SELF, step.test));
    }

    // rewrite axis path
    final AxisPath path = (AxisPath) pred;
    Path invPath = path.invertPath(root, step);
    if(!text) {
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

  /**
   * Returns the last step pointing to the requested nodes. Examples:
   * <ul>
   *   <li>{@code /xml/a[b = 'A']} -> {@code b}</li>
   *   <li>{@code /xml/a[b/text() = 'A']} -> {@code text()}</li>
   *   <li>{@code /xml/a[. = 'A']} -> {@code a}</li>
   *   <li>{@code /xml/a[text() = 'A']} -> {@code text()}</li>
   *   <li>{@code /xml/a/text()[. = 'A']} -> {@code text()}</li>
   * </ul>
   * @return step or {@code null}
   */
  private Step lastStep() {
    // expression in predicate is context value: return global step
    if(pred instanceof ContextValue) return step;
    // give up if expression is not an axis path
    if(!(pred instanceof AxisPath)) return null;
    // give up if path is not relative
    final AxisPath path = (AxisPath) pred;
    if(path.root != null) return null;
    // give up if one of the steps contains positional predicates
    final int sl = path.steps.length;
    for(int s = 0; s < sl; s++) {
      if(path.step(s).positional()) return null;
    }
    // return last step
    return path.step(sl - 1);
  }
}
