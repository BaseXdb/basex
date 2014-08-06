package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.regex.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * String pattern functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNPat extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<Pattern> patterns = new TokenObjMap<>();

  /** Slash pattern. */
  private static final Pattern SLASH = Pattern.compile("\\$");
  /** Slash pattern. */
  private static final Pattern BSLASH = Pattern.compile("\\\\");

  /** Module prefix. */
  private static final String PREFIX = "fn";
  /** QName. */
  private static final QNm Q_ANALYZE = QNm.get(PREFIX, "analyze-string-result", FNURI);
  /** QName. */
  private static final QNm Q_MATCH = QNm.get(PREFIX, "match", FNURI);
  /** QName. */
  private static final QNm Q_NONMATCH = QNm.get(PREFIX, "non-match", FNURI);
  /** QName. */
  private static final QNm Q_MGROUP = QNm.get(PREFIX, "group", FNURI);

  /** Attribute for the analyze-string-result function. */
  private static final String NR = "nr";

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNPat(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case TOKENIZE: return tokenize(qc).iter();
      default:       return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case TOKENIZE: return tokenize(qc);
      default:       return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case MATCHES:        return matches(checkEStr(exprs[0], qc), qc);
      case REPLACE:        return replace(checkEStr(exprs[0], qc), qc);
      case ANALYZE_STRING: return analyzeString(checkEStr(exprs[0], qc), qc);
      default:             return super.item(qc, ii);
    }
  }

  /**
   * Evaluates the match function.
   * @param value input value
   * @param qc query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item matches(final byte[] value, final QueryContext qc) throws QueryException {
    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc);
    return Bln.get(p.matcher(string(value)).find());
  }

  /**
   * Evaluates the analyze-string function.
   * @param value input value
   * @param qc query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item analyzeString(final byte[] value, final QueryContext qc) throws QueryException {
    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc);
    if(p.matcher("").matches()) throw REGROUP.get(info);
    final String str = string(value);
    final Matcher m = p.matcher(str);

    final FElem root = new FElem(Q_ANALYZE).declareNS();
    int s = 0;
    while(m.find()) {
      if(s != m.start()) nonmatch(str.substring(s, m.start()), root);
      match(m, str, root, 0);
      s = m.end();
    }
    if(s != str.length()) nonmatch(str.substring(s), root);
    return root;
  }

  /**
   * Processes a match.
   * @param matcher matcher
   * @param string string
   * @param parent parent
   * @param group group number
   * @return next group number and position in string
   */
  private static int[] match(final Matcher matcher, final String string, final FElem parent,
      final int group) {

    final FElem nd = new FElem(group == 0 ? Q_MATCH : Q_MGROUP);
    if(group > 0) nd.add(NR, token(group));

    final int start = matcher.start(group), end = matcher.end(group), gc = matcher.groupCount();
    int[] pos = { group + 1, start }; // group and position in string
    while(pos[0] <= gc && matcher.end(pos[0]) <= end) {
      final int st = matcher.start(pos[0]);
      if(st >= 0) { // group matched
        if(pos[1] < st) nd.add(string.substring(pos[1], st));
        pos = match(matcher, string, nd, pos[0]);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      nd.add(string.substring(pos[1], end));
      pos[1] = end;
    }
    parent.add(nd);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param par root node
   */
  private static void nonmatch(final String text, final FElem par) {
    par.add(new FElem(Q_NONMATCH).add(text));
  }

  /**
   * Evaluates the replace function.
   * @param value input value
   * @param qc query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item replace(final byte[] value, final QueryContext qc) throws QueryException {
    final byte[] rep = checkStr(exprs[2], qc);
    for(int i = 0; i < rep.length; ++i) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          throw FUNREPBS.get(info, rep);
        ++i;
      }
      if(rep[i] == '$' && (i == 0 || rep[i - 1] != '\\') &&
        (i + 1 == rep.length || !digit(rep[i + 1]))) throw FUNREPDOL.get(info, rep);
    }

    final Pattern p = pattern(exprs[1], exprs.length == 4 ? exprs[3] : null, qc);
    if(p.pattern().isEmpty()) throw REGROUP.get(info);

    String r = string(rep);
    if((p.flags() & Pattern.LITERAL) != 0) {
      r = SLASH.matcher(BSLASH.matcher(r).replaceAll("\\\\\\\\")).replaceAll("\\\\\\$");
    }

    try {
      return Str.get(p.matcher(string(value)).replaceAll(r));
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) throw REGROUP.get(info);
      throw REGPAT.get(info, ex);
    }
  }

  /**
   * Evaluates the tokenize function.
   * @param qc query context
   * @return function result
   * @throws QueryException query exception
   */
  private Value tokenize(final QueryContext qc) throws QueryException {
    final byte[] val = checkEStr(exprs[0], qc);
    if(exprs.length < 2) return StrSeq.get(split(norm(val), ' '));

    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc);
    if(p.matcher("").matches()) throw REGROUP.get(info);

    final TokenList tl = new TokenList();
    final String str = string(val);
    if(!str.isEmpty()) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        tl.add(str.substring(s, m.start()));
        s = m.end();
      }
      tl.add(str.substring(s, str.length()));
    }
    return StrSeq.get(tl);
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern input pattern
   * @param modifier modifier item
   * @param qc query context
   * @return pattern modifier
   * @throws QueryException query exception
   */
  private Pattern pattern(final Expr pattern, final Expr modifier, final QueryContext qc)
      throws QueryException {

    final byte[] pat = checkStr(pattern, qc);
    final byte[] mod = modifier != null ? checkStr(modifier, qc) : null;
    final TokenBuilder tb = new TokenBuilder(pat);
    if(mod != null) tb.add(0).add(mod);
    final byte[] key = tb.finish();
    Pattern p = patterns.get(key);
    if(p == null) {
      p = RegExParser.parse(pat, mod, sc.xquery3(), info);
      patterns.put(key, p);
    }
    return p;
  }
}
