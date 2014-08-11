package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.text.Normalizer.Form;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * String functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNStr extends BuiltinFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNStr(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case STRING_TO_CODEPOINTS: return stringToCodepointsIter(qc);
      default:                   return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case STRING_TO_CODEPOINTS: return stringToCodepoints(qc);
      default:                   return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case CODEPOINTS_TO_STRING: return codepointsToString(qc);
      case COMPARE:              return compare(qc);
      case CODEPOINT_EQUAL:      return codepointEqual(qc);
      case STRING_JOIN:          return stringJoin(qc);
      case SUBSTRING:            return substr(qc);
      case NORMALIZE_UNICODE:    return normalizeUnicode(qc);
      case UPPER_CASE:           return Str.get(uc(toToken(exprs[0], qc, true)));
      case LOWER_CASE:           return Str.get(lc(toToken(exprs[0], qc, true)));
      case TRANSLATE:            return translate(qc);
      case ENCODE_FOR_URI:       return Str.get(uri(toToken(exprs[0], qc, true), false));
      case IRI_TO_URI:           return Str.get(uri(toToken(exprs[0], qc, true), true));
      case ESCAPE_HTML_URI:      return Str.get(escape(toToken(exprs[0], qc, true)));
      case CONCAT:               return concat(qc);
      case CONTAINS:             return contains(qc);
      case CONTAINS_TOKEN:       return containsToken(qc);
      case STARTS_WITH:          return startsWith(qc);
      case ENDS_WITH:            return endsWith(qc);
      case SUBSTRING_AFTER:      return substringAfter(qc);
      case SUBSTRING_BEFORE:     return substringBefore(qc);
      default:                   return super.item(qc, ii);
    }
  }

  /**
   * Converts a string to codepoints.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Value stringToCodepoints(final QueryContext qc) throws QueryException {
    final int[] tmp = cps(toToken(exprs[0], qc, true));
    final long[] vals = new long[tmp.length];
    for(int i = 0; i < tmp.length; i++) vals[i] = tmp[i];
    return IntSeq.get(vals, AtomType.ITR);
  }

  /**
   * Compares two strings.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Int compare(final QueryContext qc) throws QueryException {
    final Item it1 = exprs[0].atomItem(qc, info), it2 = exprs[1].atomItem(qc, info);
    final Collation coll = toCollation(2, qc);
    if(it1 == null || it2 == null) return null;
    return Int.get(Math.max(-1, Math.min(1, coll == null ? diff(toToken(it1), toToken(it2)) :
      coll.compare(toToken(it1), toToken(it2)))));
  }

  /**
   * Compares codepoints of two strings.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln codepointEqual(final QueryContext qc) throws QueryException {
    final Item it1 = exprs[0].atomItem(qc, info), it2 = exprs[1].atomItem(qc, info);
    return it1 == null || it2 == null ? null : Bln.get(eq(toToken(it1), toToken(it2)));
  }

  /**
   * Converts codepoints to a string.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str codepointsToString(final QueryContext qc) throws QueryException {
    final Iter ir = exprs[0].atomIter(qc, info);
    final TokenBuilder tb = new TokenBuilder(Math.max(8, (int) ir.size()));
    for(Item it; (it = ir.next()) != null;) {
      final long n = toLong(it);
      final int i = (int) n;
      // check int boundaries before casting
      if(n < Integer.MIN_VALUE || n > Integer.MAX_VALUE || !XMLToken.valid(i))
        throw INVCODE_X.get(info, Long.toHexString(n));
      tb.add(i);
    }
    return Str.get(tb.finish());
  }

  /**
   * Converts a string to code points, lazily.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter stringToCodepointsIter(final QueryContext qc) throws QueryException {
    final byte[] s = toToken(exprs[0], qc, true);
    if(s == null) return Empty.ITER;

    return new Iter() {
      int l;
      @Override
      public Item next() {
        if(l >= s.length) return null;
        final int i = cp(s, l);
        l += cl(s, l);
        return Int.get(i);
      }
    };
  }

  /**
   * Returns a substring.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str substr(final QueryContext qc) throws QueryException {
    // normalize positions
    final byte[] str = toToken(exprs[0], qc, true);

    final Item is = toItem(exprs[1], qc);
    int s;
    if(is instanceof Int) {
      s = (int) is.itr(info) - 1;
    } else {
      final double ds = is.dbl(info);
      if(Double.isNaN(ds)) return Str.ZERO;
      s = subPos(ds);
    }

    final boolean end = exprs.length == 3, ascii = ascii(str);
    int l = ascii ? str.length : length(str);
    int e = l;
    if(end) {
      final Item ie = toItem(exprs[2], qc);
      e = ie instanceof Int ? (int) ie.itr(info) : subPos(ie.dbl(info) + 1);
    }
    if(s < 0) {
      e += s;
      s = 0;
    }
    e = Math.min(l, end ? s + e : Integer.MAX_VALUE);
    if(s >= e) return Str.ZERO;
    if(ascii) return Str.get(substring(str, s, e));

    int ss = s;
    int ee = e;
    int p = 0;
    for(l = 0; l < str.length; l += cl(str, l), ++p) {
      if(p == s) ss = l;
      if(p == e) ee = l;
    }
    if(p == e) ee = l;
    return Str.get(Arrays.copyOfRange(str, ss, ee));
  }

  /**
   * Returns the specified substring position.
   * @param d double value
   * @return substring position
   */
  private static int subPos(final double d) {
    final int i = (int) d;
    return d == i ? i - 1 : (int) StrictMath.floor(d - .5);
  }

  /**
   * Returns a translated string.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  private Str translate(final QueryContext qc) throws QueryException {
    final int[] tok =  cps(toToken(exprs[0], qc, true));
    final int[] srch = cps(toToken(exprs[1], qc));
    final int[] rep =  cps(toToken(exprs[2], qc));

    final TokenBuilder tb = new TokenBuilder(tok.length);
    for(final int t : tok) {
      int j = -1;
      while(++j < srch.length && t != srch[j]) ;
      if(j < srch.length) {
        if(j >= rep.length) continue;
        tb.add(rep[j]);
      } else {
        tb.add(t);
      }
    }
    return Str.get(tb.finish());
  }

  /**
   * Returns a joined string.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str stringJoin(final QueryContext qc) throws QueryException {
    final byte[] sep = exprs.length == 2 ? toToken(exprs[1], qc) : EMPTY;
    final TokenBuilder tb = new TokenBuilder();
    final Iter iter = exprs[0].atomIter(qc, info);
    int c = 0;
    for(Item it; (it = iter.next()) != null;) {
      if(c++ != 0) tb.add(sep);
      tb.add(toToken(it));
    }
    return Str.get(tb.finish());
  }

  /**
   * Returns normalized unicode.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  private Str normalizeUnicode(final QueryContext qc) throws QueryException {
    final byte[] str = toToken(exprs[0], qc, true);

    Form form = Form.NFC;
    if(exprs.length == 2) {
      final byte[] n = uc(trim(toToken(exprs[1], qc)));
      if(n.length == 0) return Str.get(str);
      try {
        form = Form.valueOf(string(n));
      } catch(final IllegalArgumentException ex) {
        throw NORMUNI_X.get(info, n);
      }
    }
    return ascii(str) ? Str.get(str) : Str.get(Normalizer.normalize(string(str), form));
  }

  /**
   * Concatenates strings.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str concat(final QueryContext qc) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr a : exprs) {
      final Item it = a.atomItem(qc, info);
      if(it != null) tb.add(it.string(info));
    }
    return Str.get(tb.finish());
  }

  /**
   * Checks if a string contains another.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln contains(final QueryContext qc) throws QueryException {
    final byte[] ss = toToken(exprs[0], qc, true), sb = toToken(exprs[1], qc, true);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.contains(ss, sb) : coll.contains(ss, sb, info));
  }

  /**
   * Checks if a string contains a token.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln containsToken(final QueryContext qc) throws QueryException {
    final byte[] token = trim(toToken(exprs[1], qc));
    final Collation coll = toCollation(2, qc);
    if(token.length != 0) {
      final Iter ir = qc.iter(exprs[0]);
      for(Item it; (it = ir.next()) != null;) {
        for(final byte[] tok : split(normalize(toToken(it)), ' ')) {
          if(coll == null ? eq(token, tok) : coll.compare(token, tok) == 0) return Bln.TRUE;
        }
      }
    }
    return Bln.FALSE;
  }

  /**
   * Checks if a string starts with another.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln startsWith(final QueryContext qc) throws QueryException {
    final byte[] ss = toToken(exprs[0], qc, true), sb = toToken(exprs[1], qc, true);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.startsWith(ss, sb) : coll.startsWith(ss, sb, info));
  }

  /**
   * Checks if a string ends with another.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln endsWith(final QueryContext qc) throws QueryException {
    final byte[] ss = toToken(exprs[0], qc, true), sb = toToken(exprs[1], qc, true);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.endsWith(ss, sb) : coll.endsWith(ss, sb, info));
  }

  /**
   * Returns the string after another string.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str substringAfter(final QueryContext qc) throws QueryException {
    final byte[] ss = toToken(exprs[0], qc, true), sb = toToken(exprs[1], qc, true);
    final Collation coll = toCollation(2, qc);
    if(coll == null) {
      final int p = indexOf(ss, sb);
      return p == -1 ? Str.ZERO : Str.get(substring(ss, p + sb.length));
    }
    return Str.get(coll.after(ss, sb, info));
  }

  /**
   * Returns the string before another string.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str substringBefore(final QueryContext qc) throws QueryException {
    final byte[] ss = toToken(exprs[0], qc, true), sb = toToken(exprs[1], qc, true);
    final Collation coll = toCollation(2, qc);
    if(coll == null) {
      final int p = indexOf(ss, sb);
      return p == -1 ? Str.ZERO : Str.get(substring(ss, 0, p));
    }
    return Str.get(coll.before(ss, sb, info));
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.X30 && func == Function.STRING_JOIN && exprs.length == 1 ||
        super.has(flag);
  }
}
