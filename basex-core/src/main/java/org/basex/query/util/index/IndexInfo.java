package org.basex.query.util.index;

import static org.basex.query.QueryText.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class contains methods for storing information on new index expressions.
 *
 * @author BaseX Team, BSD License
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
  /** Costs of index access ({@code null} if no index access is possible). */
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
    final Kind kind = last.test.kind;
    if(kind == Kind.TEXT) {
      text = true;
    } else if(kind == Kind.ELEMENT) {
      // ensure that addressed elements only have text nodes as children
      // stop if database is unknown/out-dated or if name test is not simple
      if(data == null || !data.meta.uptodate ||
          !(last.test instanceof final NameTest nt)) return null;
      test = nt;

      // resolve local name for statistics lookup; sound only if its lexical name is unambiguous
      final byte[] local;
      if(data.nspaces.isEmpty()) {
        // no namespaces: one lexical name per local name
        if(test.name == null) return null;
        local = test.name;
      } else if(test.scope == NameTest.Scope.FULL && !test.qname.hasURI() &&
          !data.usesDefaultNs()) {
        // no default namespace: full no-namespace test maps to its no-prefix lexical name
        local = test.qname.local();
      } else {
        return null;
      }

      final Stats stats = data.elemNames.stats(data.elemNames.index(local));
      if(stats == null || !stats.isLeaf()) return null;
      text = true;
    } else if(kind == Kind.ATTRIBUTE) {
      text = false;
    } else {
      // other tests cannot be rewritten for index access
      return null;
    }

    // check if the index contains results for the specified elements or attributes
    final IndexType it = type != null ? type : text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    if(text ? it != IndexType.TEXT && it != IndexType.FULLTEXT :
      it != IndexType.TOKEN && it != IndexType.ATTRIBUTE) return null;

    // database is known at compile time: perform additional checks
    if(data != null) {
      // check if required index exists
      if(!data.meta.index(it)) return null;
      // check if values of targeted name are indexed
      final byte[][] qname = pred.qname().test instanceof final NameTest nt ?
        new byte[][] { nt.qname.local(), nt.qname.uri() } : null;
      if(!new IndexNames(it, data).contains(qname)) return null;
    }
    return it;
  }

  /**
   * Tries to rewrite the specified input for index access.
   * @param search expression to find (can be {@code null})
   * @param type index type (can be {@code null})
   * @param info input info (can be {@code null})
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean create(final Expr search, final IndexType type, final InputInfo info)
      throws QueryException {

    final Data data = db.data();
    if(type == null || search == null || data == null && !enforce()) return false;

    if(search instanceof final Value value) {
      // loop through all items; collect tokens, accumulate costs, and track the result size.
      // prerequisites for an exact size estimate:
      // - index access is not followed by a name test,
      // - no token index is used, or only one token is looked up
      final TokenSet tokens = new TokenSet();
      final AtomicInteger size = new AtomicInteger(test == null ? 0 : -1);
      final Predicate<byte[]> add = token -> {
        // do not use text/attribute index if string is empty or too long
        final int tl = token.length;
        return (type == IndexType.TOKEN || tl > 0 && (data == null || tl <= data.meta.maxlen)) &&
            addToken(token, type, data, tokens, size);
      };

      Stats stats = null;
      for(final Item item : value) {
        if(item.type.isStringOrUntyped()) {
          // string: exact search
          if(!add.test(item.string(info))) return false;
        } else if(item.type.instanceOf(BasicType.INTEGER)) {
          // integers: search indexed lexical forms (for non-canonical values like '+5')
          if(stats == null) {
            stats = intStats(data);
            if(stats == null) return false;
          }
          final long v = Token.toLong(item.string(info));
          for(final byte[] stored : stats.values) {
            if(Token.toLong(stored) == v && !add.test(stored)) return false;
          }
        } else {
          return false;
        }
      }
      valueAccess(tokens, size, type, info, search);
      return true;
    }

    /* index access is not possible if returned type is not a string or untyped; if
     * expression depends on context; or if it is nondeterministic. examples:
     * - for $x in ('a', 1) return //*[text() = $x]
     * - //*[text() = .]
     * - //*[text() = (if(random:double() < .5) then 'X' else 'Y')] */
    if(!search.seqType().type.isStringOrUntyped() || search.has(Flag.CTX, Flag.NDT))
      return false;

    // estimate costs for dynamic query terms
    costs = enforce() ? IndexCosts.ENFORCE_DYNAMIC :
      IndexCosts.get(Math.max(1, data.meta.size / 10));
    final ValueAccess va = new ValueAccess(info, search, type, test, db);
    return create(va, false, Util.info(OPTINDEX_X_X, type, search), info);
  }

  /**
   * Tries to rewrite an integer range comparison for index access.
   * The lookup is performed against the indexed lexical forms of an integer-category
   * value index, restricted to entries whose long-value lies in {@code [min..max]}.
   * @param min minimum value (inclusive)
   * @param max maximum value (inclusive)
   * @param info input info (can be {@code null})
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean create(final long min, final long max, final InputInfo info)
      throws QueryException {

    final Data data = db.data();
    final Stats stats = intStats(data);
    if(stats == null) return false;

    // collect indexed lexical forms whose long-value lies in [min..max]
    final IndexType type = text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    final TokenSet tokens = new TokenSet();
    final AtomicInteger size = new AtomicInteger(test == null ? 0 : -1);
    for(final byte[] token : stats.values) {
      final long v = Token.toLong(token);
      if(v < min || v > max) continue;
      if(!addToken(token, type, data, tokens, size)) return false;
    }
    final TokenBuilder tb = new TokenBuilder().add('[').addLong(min).add(',').addLong(max).add(']');
    return valueAccess(tokens, size, type, info, tb);
  }

  /**
   * Looks up the costs for the supplied token, adds it to the token set if not
   * already present, accumulates the costs, and updates the result-size estimate.
   * @param token token to look up
   * @param type index type
   * @param data data reference (can be {@code null})
   * @param tokens token set
   * @param size current size estimate
   * @return false if the lookup failed
   */
  private boolean addToken(final byte[] token, final IndexType type, final Data data,
      final TokenSet tokens, final AtomicInteger size) {
    if(!tokens.add(token)) return true;
    final IndexCosts ic = costs(data, new StringToken(type, token));
    if(ic == null) return false;
    costs = IndexCosts.add(costs, ic);
    final int r = ic.results(), s = size.get();
    if(s >= 0) size.set(r < 0 || s > 0 && type == IndexType.TOKEN && r > 0 ? -1 : s + r);
    return true;
  }

  /**
   * Builds a value-index access from already-collected lexical-form tokens and registers
   * the rewriting with this index info.
   * @param tokens distinct tokens to be looked up
   * @param size expected result size ({@code -1} if unknown)
   * @param type index type
   * @param info input info (can be {@code null})
   * @param opt optimization info displayed in the query plan
   * @return true
   * @throws QueryException query exception
   */
  private boolean valueAccess(final TokenSet tokens, final AtomicInteger size, final IndexType type,
      final InputInfo info, final Object opt) throws QueryException {

    // no matches: zero cost
    if(tokens.isEmpty()) costs = IndexCosts.add(costs, IndexCosts.ZERO);

    final ValueAccess va = new ValueAccess(info, tokens, type, test, db);
    va.exprType.assign(va.seqType(), size.get());
    return create(va, false, Util.info(OPTINDEX_X_X, type, opt), info);
  }

  /**
   * Creates an index expression with an inverted axis path.
   * @param root new root expression
   * @param parent add parent step
   * @param opt optimization info
   * @param info input info (can be {@code null})
   * @return true
   * @throws QueryException query exception
   */
  public boolean create(final ParseExpr root, final boolean parent, final String opt,
      final InputInfo info) throws QueryException {

    final Expr rt;
    if(test == null || !parent) {
      rt = root;
    } else {
      final Expr st = Step.get(cc, root, info, Axis.PARENT, test);
      rt = Path.get(cc, info, root, st);
    }
    expr = pred.invert(rt);
    optInfo = opt;
    return true;
  }

  /**
   * Computes costs if the specified data reference exists.
   * @param data data reference (can be {@code null})
   * @param search index search definition
   * @return costs, or {@code null} if index access is not possible
   */
  public static IndexCosts costs(final Data data, final IndexSearch search) {
    return data != null ? data.costs(search) : IndexCosts.ENFORCE_STATIC;
  }

  /**
   * Retrieves the statistics of the targeted element or attribute name.
   * @param data data reference (can be {@code null})
   * @return statistics, or {@code null} if not available
   */
  private Stats intStats(final Data data) {
    if(data == null || !data.meta.uptodate || !data.nspaces.isEmpty()) return null;
    if(!(pred.qname().test instanceof final NameTest nt) || nt.name == null) return null;
    final Names names = text ? data.elemNames : data.attrNames;
    final Stats stats = names.stats(names.index(nt.qname.local()));
    return stats == null || !StatsType.isCategory(stats.type) || !StatsType.isInteger(stats.type)
        ? null : stats;
  }

  /**
   * Indicates if the index rewriting should be enforced.
   * @return result of check
   */
  public boolean enforce() {
    return cc.qc.context.options.get(MainOptions.ENFORCEINDEX);
  }
}
