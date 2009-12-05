package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * String functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FNStr extends Fun {
  /** Normalization types. */
  private static final String[] NORMS = { "NFC", "NFD", "NFKC", "NFKD", "" };
  /** Hex codes. */
  private static final byte[] HEX = token("0123456789ABCDEF");
  /** Reserved characters. */
  private static final byte[] IRIRES = token("!#$%&*'()+,-./:;=?@[]~_");
  /** Reserved characters. */
  private static final byte[] RES = token("-._~");

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];

    switch(func) {
      case STCODE:
        return str2cp(e.atomic(ctx));
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];

    switch(func) {
      case CODESTR:
        return cp2str(e.iter(ctx));
      case COMPARE:
        if(expr.length == 3) checkColl(expr[2], ctx);
        Item it1 = e.atomic(ctx);
        Item it2 = expr[1].atomic(ctx);
        if(it1 == null || it2 == null) return null;
        final int d = diff(checkStr(it1), checkStr(it2));
        return Itr.get(Math.max(-1, Math.min(1, d)));
      case CODEPNT:
        it1 = e.atomic(ctx);
        it2 = expr[1].atomic(ctx);
        if(it1 == null || it2 == null) return null;
        return Bln.get(eq(checkStr(it1), checkStr(it2)));
      case STRJOIN:
        return strjoin(ctx);
      case SUBSTR:
        return substr(ctx);
      case NORMUNI:
        return normuni(ctx);
      case UPPER:
        return Str.get(uc(checkStr(e, ctx)));
      case LOWER:
        return Str.get(lc(checkStr(e, ctx)));
      case TRANS:
        return trans(ctx);
      case ENCURI:
        return Str.get(uri(checkStr(e, ctx), false));
      case IRIURI:
        return Str.get(uri(checkStr(e, ctx), true));
      case ESCURI:
        return Str.get(esc(checkStr(e, ctx)));
      case CONCAT:
        return concat(ctx);
      case CONTAINS:
        if(expr.length == 3) checkColl(expr[2], ctx);
        Item it = expr[1].atomic(ctx);
        if(it == null) return Bln.TRUE;
        return Bln.get(contains(checkStr(e, ctx), checkStr(it)));
      case STARTS:
        if(expr.length == 3) checkColl(expr[2], ctx);
        it = expr[1].atomic(ctx);
        if(it == null) return Bln.TRUE;
        return Bln.get(startsWith(checkStr(e, ctx), checkStr(it)));
      case ENDS:
        if(expr.length == 3) checkColl(expr[2], ctx);
        it = expr[1].atomic(ctx);
        if(it == null) return Bln.TRUE;
        return Bln.get(endsWith(checkStr(e, ctx), checkStr(it)));
      case SUBAFTER:
        if(expr.length == 3) checkColl(expr[2], ctx);
        final byte[] str = checkStr(e, ctx);
        final byte[] sa = checkStr(expr[1], ctx);
        final int pa = indexOf(str, sa);
        return pa != -1 ? Str.get(substring(str, pa + sa.length)) :
          Str.ZERO;
      case SUBBEFORE:
        if(expr.length == 3) checkColl(expr[2], ctx);
        final byte[] sb = checkStr(e, ctx);
        final int pb = indexOf(sb, checkStr(expr[1], ctx));
        return pb > 0 ? Str.get(substring(sb, 0, pb)) : Str.ZERO;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];

    // optimize frequently used functions
    switch(func) {
      case UPPER:
        return e.i() ? Str.get(uc(checkStr(e, ctx))) : this;
      case LOWER:
        return e.i() ? Str.get(lc(checkStr(e, ctx))) : this;
      case CONCAT:
        for(final Expr a : expr) if(!a.i()) return this;
        return concat(ctx);
      case CONTAINS:
        if(expr.length == 2) {
          final byte[] i = expr[1].i() ? checkStr((Item) expr[1]) : null;
          // empty query string: return true
          if(expr[1].e() || i != null && i.length == 0) return Bln.TRUE;
          // empty input string: return false
          if(e.e() && i != null && i.length != 0) return Bln.FALSE;
          // evaluate items
          if(e.i() && expr[1].i()) return Bln.get(contains(
              checkStr(e, ctx), checkStr((Item) expr[1])));
        }
        return this;
      default:
        return this;
    }
  }

  /**
   * Converts codepoints to a string.
   * @param iter iterator
   * @return iterator
   * @throws QueryException query exception
   */
  private Item cp2str(final Iter iter) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    Item i;
    while((i = iter.next()) != null) {
      final long n = checkItr(i);
      if(!XMLToken.valid(n)) Err.or(INVCODE, i);
      tb.addUTF((int) n);
    }
    return Str.get(tb.finish());
  }

  /**
   * Converts a string to codepoints.
   * @param it item
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter str2cp(final Item it) throws QueryException {
    if(it == null) return Iter.EMPTY;
    final byte[] s = checkStr(it);

    return new Iter() {
      int l;
      @Override
      public Item next() {
        if(l == s.length) return null;
        final int i = cp(s, l);
        l += cl(s[l]);
        return Itr.get(i);
      }
    };
  }

  /**
   * Returns a substring.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item substr(final QueryContext ctx) throws QueryException {
    // normalize positions
    final double ds = checkDbl(expr[1], ctx);
    final byte[] str = checkStr(expr[0], ctx);
    final boolean end = expr.length == 3;
    int l = len(str);
    if(ds != ds) return Str.ZERO;
    int s = (int) Math.floor(ds - .5);
    int e = end ? (int) Math.floor(checkDbl(expr[2], ctx) + .5) : l;
    if(s < 0) {
      e += s;
      s = 0;
    }
    e = Math.min(l, end ? s + e : Integer.MAX_VALUE);
    if(s >= e) return Str.ZERO;
    if(ascii(str)) return Str.get(substring(str, s, e));
    if(s == 0 && e == str.length) return Str.get(str);

    int ss = s;
    int ee = e;
    int p = 0;
    for(l = 0; l < str.length; l += cl(str[l]), p++) {
      if(p == s) ss = l;
      if(p == e) ee = l;
    }
    if(p == e) ee = l;
    return Str.get(Arrays.copyOfRange(str, ss, ee));
  }

  /**
   * Returns a translated string.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Item trans(final QueryContext ctx) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    final Item is = expr[1].atomic(ctx);
    if(is == null) Err.empty(this);
    final byte[] sea = checkStr(is);
    final Item ir = expr[2].atomic(ctx);
    if(ir == null) Err.empty(this);
    final byte[] rep = checkStr(ir);
    return Str.get(translate(string(str), string(sea), string(rep)));
  }

  /**
   * Performs a translation on the specified token.
   * @param tok token
   * @param srch characters to be found
   * @param rep characters to be replaced
   * @return translated token
   */
  private static byte[] translate(final String tok, final String srch,
      final String rep) {
    final int l = tok.length();
    final StringBuilder tmp = new StringBuilder(l);
    for(int i = 0; i < l; i++) {
      final char b = tok.charAt(i);
      int j = -1;
      while(++j < srch.length() && b != srch.charAt(j));
      if(j < srch.length()) {
        if(j >= rep.length()) continue;
        tmp.append(rep.charAt(j));
      } else {
        tmp.append(tok.charAt(i));
      }
    }
    return token(tmp.toString());
  }

  /**
   * Returns a string join.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item strjoin(final QueryContext ctx) throws QueryException {
    final Item is = expr[1].atomic(ctx);
    if(is == null) Err.empty(this);
    final byte[] sep = checkStr(is);

    final TokenBuilder tb = new TokenBuilder();
    final Iter iter = expr[0].iter(ctx);
    int c = 0;
    Item i;
    while((i = iter.next()) != null) {
      tb.add(checkStr(i));
      tb.add(sep);
      c++;
    }
    final byte[] v = tb.finish();
    return Str.get(c == 0 ? v : substring(v, 0, v.length - sep.length));
  }

  /**
   * Returns normalized unicode.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Item normuni(final QueryContext ctx) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    String nr = null;
    if(expr.length == 2) {
      final Item is = expr[1].atomic(ctx);
      if(is == null) Err.empty(this);
      final String n = string(uc(trim(checkStr(is))));
      for(final String nrm : NORMS) if(nrm.equals(n)) nr = nrm;
      if(nr == null) Err.or(NORMUNI, n);
    } else {
      nr = NORMS[0];
    }
    // [CG] XQuery/normalize-unicode
    return Str.get(str);
  }

  /**
   * Concatenates strings.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item concat(final QueryContext ctx) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr a : expr) {
      final Item it = a.atomic(ctx);
      if(it != null) tb.add(it.str());
    }
    return Str.get(tb.finish());
  }

  /**
   * Returns a URI encoded token.
   * @param tok token
   * @param iri input
   * @return encoded token
   */
  private static byte[] uri(final byte[] tok, final boolean iri) {
    final int tl = tok.length;
    final TokenBuilder tb = new TokenBuilder();
    for(int t = 0; t < tl; t++) {
      final byte b = tok[t];
      if(letterOrDigit(b) || contains(iri ? IRIRES : RES, b)) tb.add(b);
      else hex(tb, b);
    }
    return tb.finish();
  }

  /**
   * Escapes the specified token.
   * @param tok token
   * @return escaped token
   */
  private static byte[] esc(final byte[] tok) {
    final int tl = tok.length;
    final TokenBuilder tb = new TokenBuilder();
    for(int t = 0; t < tl; t++) {
      final byte b = tok[t];
      if(b >= 32 && b <= 126) tb.add(b);
      else hex(tb, b);
    }
    return tb.finish();
  }

  /**
   * Adds the specified byte in hex code.
   * @param tb token builder
   * @param b byte to be added
   */
  private static void hex(final TokenBuilder tb, final byte b) {
    tb.add('%');
    tb.add(HEX[(b & 0xFF) >> 4]);
    tb.add(HEX[b & 0xFF & 15]);
  }
}
