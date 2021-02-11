package org.basex.query.util.index;

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
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexInfo {
  /** Compilation context. */
  public final CompileContext cc;
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
  /** Indicates if the last step addresses a text node. */
  boolean text;

  /** Predicate expression. */
  private IndexPred pred;

  /**
   * Constructor.
   * @param db index database
   * @param cc compilation context
   * @param step step containing the rewritable predicate
   */
  public IndexInfo(final IndexDb db, final CompileContext cc, final Step step) {
    this.cc = cc;
    this.db = db;
    this.step = step;
  }

  /**
   * Checks if the specified expression can be rewritten for index access, and returns
   * the applicable index type.
   * @param input input (if {@code null}, no optimization will be possible)
   * @param type index type, predefined by the called expression (can be {@code null})
   * @return supplied type, {@link IndexType#TEXT}, {@link IndexType#ATTRIBUTE}, or
   *   {@code null} if index access is not possible
   */
  public IndexType type(final Expr input, final IndexType type) {
    pred = IndexPred.get(input, this);
    if(pred == null) return null;

    // find last step that will be evaluated before doing a comparison
    final Step last = pred.step();
    if(last == null) return null;

    final Data data = db.data();
    if(last.test.type == NodeType.TEXT) {
      text = true;
    } else if(last.test.type == NodeType.ELEMENT) {
      // ensure that addressed elements only have text nodes as children
      // stop if database is unknown/out-dated, if namespaces occur, or if name test is not simple
      if(data == null || !(data.meta.uptodate && data.nspaces.isEmpty()) ||
          !(last.test instanceof NameTest)) return null;

      test = (NameTest) last.test;
      if(test.part() != NamePart.LOCAL) return null;

      final Stats stats = data.elemNames.stats(data.elemNames.id(test.qname.local()));
      if(stats == null || !stats.isLeaf()) return null;
      text = true;
    } else if(last.test.type != NodeType.ATTRIBUTE) {
      // other tests cannot be rewritten for index access
      return null;
    }

    // check if the index contains result for the specified elements or attributes
    final IndexType it = type != null ? type : text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    if(text ? (it != IndexType.TEXT && it != IndexType.FULLTEXT) :
      (it != IndexType.TOKEN && it != IndexType.ATTRIBUTE)) return null;

    // database is known at compile time: perform additional checks
    if(data != null) {
      // check if required index exists
      if(!data.meta.index(it)) return null;
      // check if targeted name is contained in the index
      final Step st = pred.qname();
      byte[][] qname = null;
      if(st.test instanceof NameTest) {
        final NameTest nt = (NameTest) st.test;
        qname = new byte[][] { nt.local, nt.qname == null ? null : nt.qname.uri() };
      }
      if(!new IndexNames(it, data).contains(qname)) return null;
    }
    return it;
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
      final Iter iter = search.iter(cc.qc);
      final TokenIntMap cache = new TokenIntMap();
      for(Item item; (item = cc.qc.next(iter)) != null;) {
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
      va.size(counts);
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
   * @throws QueryException query exception
   */
  public void create(final ParseExpr root, final boolean parent, final String opt,
      final InputInfo ii) throws QueryException {

    final Expr rt;
    if(test == null || !parent) {
      rt = root;
    } else {
      final Expr st = Step.get(cc, root, ii, Axis.PARENT, test);
      rt = Path.get(cc, ii, root, st);
    }
    expr = pred.invert(rt);
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
    return cc.qc.context.options.get(MainOptions.ENFORCEINDEX);
  }
}
