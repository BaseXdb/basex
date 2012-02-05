package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.text.Normalizer;
import java.util.Arrays;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.IntSeq;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * String functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNStr extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNStr(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];

    switch(sig) {
      case STRING_TO_CODEPOINTS:
        return str2cp(e.item(ctx, input));
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case STRING_TO_CODEPOINTS:
        final int[] tmp = cps(checkEStr(expr[0], ctx));
        final long[] vals = new long[tmp.length];
        for(int i = 0; i < tmp.length; i++) vals[i] = tmp[i];
        return IntSeq.get(vals, AtomType.ITR);
      default:
        return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Expr e = expr[0];

    switch(sig) {
      case CODEPOINTS_TO_STRING:
        return cp2str(ctx.iter(e));
      case COMPARE:
        if(expr.length == 3) checkColl(expr[2], ctx);
        Item it1 = e.item(ctx, input);
        Item it2 = expr[1].item(ctx, input);
        if(it1 == null || it2 == null) return null;
        final int d = diff(checkEStr(it1), checkEStr(it2));
        return Int.get(Math.max(-1, Math.min(1, d)));
      case CODEPOINT_EQUAL:
        it1 = e.item(ctx, input);
        it2 = expr[1].item(ctx, input);
        if(it1 == null || it2 == null) return null;
        return Bln.get(eq(checkEStr(it1), checkEStr(it2)));
      case STRING_JOIN:
        return strjoin(ctx);
      case SUBSTRING:
        return substr(ctx);
      case NORMALIZE_UNICODE:
        return normuni(ctx);
      case UPPER_CASE:
        return Str.get(uc(checkEStr(e, ctx)));
      case LOWER_CASE:
        return Str.get(lc(checkEStr(e, ctx)));
      case TRANSLATE:
        return trans(ctx);
      case ENCODE_FOR_URI:
        return Str.get(uri(checkEStr(e, ctx), false));
      case IRI_TO_URI:
        return Str.get(uri(checkEStr(e, ctx), true));
      case ESCAPE_HTML_URI:
        return Str.get(escape(checkEStr(e, ctx)));
      case CONCAT:
        return concat(ctx);
      case CONTAINS:
        if(expr.length == 3) checkColl(expr[2], ctx);
        Item it = expr[1].item(ctx, input);
        if(it == null) return Bln.TRUE;
        return Bln.get(contains(checkEStr(e, ctx), checkEStr(it)));
      case STARTS_WITH:
        if(expr.length == 3) checkColl(expr[2], ctx);
        it = expr[1].item(ctx, input);
        if(it == null) return Bln.TRUE;
        return Bln.get(startsWith(checkEStr(e, ctx), checkEStr(it)));
      case ENDS_WITH:
        if(expr.length == 3) checkColl(expr[2], ctx);
        it = expr[1].item(ctx, input);
        if(it == null) return Bln.TRUE;
        return Bln.get(endsWith(checkEStr(e, ctx), checkEStr(it)));
      case SUBSTRING_AFTER:
        if(expr.length == 3) checkColl(expr[2], ctx);
        final byte[] str = checkEStr(e, ctx);
        final byte[] sa = checkEStr(expr[1], ctx);
        final int pa = indexOf(str, sa);
        return pa != -1 ? Str.get(substring(str, pa + sa.length)) :
          Str.ZERO;
      case SUBSTRING_BEFORE:
        if(expr.length == 3) checkColl(expr[2], ctx);
        final byte[] sb = checkEStr(e, ctx);
        final int pb = indexOf(sb, checkEStr(expr[1], ctx));
        return pb > 0 ? Str.get(substring(sb, 0, pb)) : Str.ZERO;
      default:
        return super.item(ctx, ii);
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
    for(Item i; (i = iter.next()) != null;) {
      final long n = checkItr(i);
      // check int boundaries before casting
      if(n < Integer.MIN_VALUE || n > Integer.MAX_VALUE
          || !XMLToken.valid((int) n)) INVCODE.thrw(input, i);
      tb.add((int) n);
    }
    return Str.get(tb.finish());
  }

  /**
   * Converts a string to code points, lazily.
   * @param it item
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter str2cp(final Item it) throws QueryException {
    if(it == null) return Empty.ITER;
    final byte[] s = checkEStr(it);

    return new Iter() {
      int l;
      @Override
      public Item next() {
        if(l == s.length) return null;
        final int i = cp(s, l);
        l += cl(s, l);
        return Int.get(i);
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
    final byte[] str = checkEStr(expr[0], ctx);
    if(Double.isNaN(ds)) return Str.ZERO;

    final boolean end = expr.length == 3;
    int l = len(str);
    int s = subPos(ds);
    int e = end ? subPos(checkDbl(expr[2], ctx) + 1) : l;
    if(s < 0) {
      e += s;
      s = 0;
    }
    e = Math.min(l, end ? s + e : Integer.MAX_VALUE);
    if(s >= e) return Str.ZERO;
    if(ascii(str)) return Str.get(substring(str, s, e));

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
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Item trans(final QueryContext ctx) throws QueryException {
    final int[] tok =  cps(checkEStr(expr[0], ctx));
    final int[] srch = cps(checkStr(expr[1], ctx));
    final int[] rep =  cps(checkStr(expr[2], ctx));

    final TokenBuilder tmp = new TokenBuilder(tok.length);
    for(final int t : tok) {
      int j = -1;
      while(++j < srch.length && t != srch[j]) ;
      if(j < srch.length) {
        if(j >= rep.length) continue;
        tmp.add(rep[j]);
      } else {
        tmp.add(t);
      }
    }
    return Str.get(tmp.finish());
  }

  /**
   * Returns a joined string.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item strjoin(final QueryContext ctx) throws QueryException {
    final byte[] sep = expr.length == 2 ? checkStr(expr[1], ctx) : EMPTY;
    final TokenBuilder tb = new TokenBuilder();
    final Iter iter = ctx.iter(expr[0]);
    int c = 0;
    for(Item i; (i = iter.next()) != null;) {
      tb.add(checkEStr(i));
      tb.add(sep);
      ++c;
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
    final byte[] str = checkEStr(expr[0], ctx);

    Normalizer.Form form = Normalizer.Form.NFC;
    if(expr.length == 2) {
      final byte[] n = uc(trim(checkStr(expr[1], ctx)));
      if(n.length == 0) return Str.get(str);

      try {
        form = Normalizer.Form.valueOf(string(n));
      } catch(final IllegalArgumentException ex) {
        NORMUNI.thrw(input, n);
      }
    }
    return ascii(str) ? Str.get(str) :
      Str.get(Normalizer.normalize(string(str), form));
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
      final Item it = a.item(ctx, input);
      if(it != null) tb.add(it.string(input));
    }
    return Str.get(tb.finish());
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && sig == Function.STRING_JOIN && expr.length == 1 ||
      super.uses(u);
  }
}
