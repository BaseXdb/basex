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
public final class FNStr extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNStr(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];

    switch(sig) {
      case STRING_TO_CODEPOINTS:
        return str2cp(e.item(ctx, info));
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Expr e = expr[0];

    switch(sig) {
      case CODEPOINTS_TO_STRING:
        return cp2str(ctx.iter(e));
      case COMPARE:
        Collation coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        Item it1 = e.item(ctx, info);
        Item it2 = expr[1].item(ctx, info);
        if(it1 == null || it2 == null) return null;
        return Int.get(Math.max(-1, Math.min(1,
            coll == null ? diff(checkEStr(it1), checkEStr(it2)) :
            coll.compare(checkStr(it1), checkStr(it2)))));
      case CODEPOINT_EQUAL:
        it1 = e.item(ctx, info);
        it2 = expr[1].item(ctx, info);
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
        coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        byte[] ss = checkEStr(e, ctx);
        byte[] sb = checkEStr(expr[1], ctx);
        return Bln.get(coll == null ? contains(ss, sb) : coll.contains(ss, sb, info));
      case STARTS_WITH:
        coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        ss = checkEStr(e, ctx);
        sb = checkEStr(expr[1], ctx);
        return Bln.get(coll == null ? startsWith(ss, sb) : coll.startsWith(ss, sb, info));
      case ENDS_WITH:
        coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        ss = checkEStr(e, ctx);
        sb = checkEStr(expr[1], ctx);
        return Bln.get(coll == null ? endsWith(ss, sb) : coll.endsWith(ss, sb, info));
      case SUBSTRING_AFTER:
        coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        ss = checkEStr(e, ctx);
        sb = checkEStr(expr[1], ctx);
        if(coll == null) {
          final int p = indexOf(ss, sb);
          return p == -1 ? Str.ZERO : Str.get(substring(ss, p + sb.length));
        }
        return Str.get(coll.after(ss, sb, info));
      case SUBSTRING_BEFORE:
        coll = checkColl(expr.length == 3 ? expr[2] : null, ctx, sc);
        ss = checkEStr(e, ctx);
        sb = checkEStr(expr[1], ctx);
        if(coll == null) {
          final int p = indexOf(ss, sb);
          return p == -1 ? Str.ZERO : Str.get(substring(ss, 0, p));
        }
        return Str.get(coll.before(ss, sb, info));
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Converts codepoints to a string.
   * @param ir iterator
   * @return iterator
   * @throws QueryException query exception
   */
  private Str cp2str(final Iter ir) throws QueryException {
    final TokenBuilder tb = new TokenBuilder(Math.max(8, (int) ir.size()));
    for(Item it; (it = ir.next()) != null;) {
      final long n = checkItr(it);
      final int i = (int) n;
      // check int boundaries before casting
      if(n < Integer.MIN_VALUE || n > Integer.MAX_VALUE || !XMLToken.valid(i))
        throw INVCODE.get(info, Long.toHexString(n));
      tb.add(i);
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
        if(l >= s.length) return null;
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
  private Str substr(final QueryContext ctx) throws QueryException {
    // normalize positions
    final byte[] str = checkEStr(expr[0], ctx);

    final Item is = checkItem(expr[1], ctx);
    int s;
    if(is instanceof Int) {
      s = (int) is.itr(info) - 1;
    } else {
      final double ds = is.dbl(info);
      if(Double.isNaN(ds)) return Str.ZERO;
      s = subPos(ds);
    }

    final boolean end = expr.length == 3, ascii = ascii(str);
    int l = ascii ? str.length : len(str);
    int e = l;
    if(end) {
      final Item ie = checkItem(expr[2], ctx);
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
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str trans(final QueryContext ctx) throws QueryException {
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
  private Str strjoin(final QueryContext ctx) throws QueryException {
    final byte[] sep = expr.length == 2 ? checkStr(expr[1], ctx) : EMPTY;
    final TokenBuilder tb = new TokenBuilder();
    final Iter iter = ctx.iter(expr[0]);
    int c = 0;
    for(Item i; (i = iter.next()) != null;) {
      tb.add(checkEStr(i)).add(sep);
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
  private Str normuni(final QueryContext ctx) throws QueryException {
    final byte[] str = checkEStr(expr[0], ctx);

    Form form = Form.NFC;
    if(expr.length == 2) {
      final byte[] n = uc(trim(checkStr(expr[1], ctx)));
      if(n.length == 0) return Str.get(str);
      try {
        form = Form.valueOf(string(n));
      } catch(final IllegalArgumentException ex) {
        throw NORMUNI.get(info, n);
      }
    }
    return ascii(str) ? Str.get(str) : Str.get(Normalizer.normalize(string(str), form));
  }

  /**
   * Concatenates strings.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str concat(final QueryContext ctx) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr a : expr) {
      final Item it = a.item(ctx, info);
      if(it != null) tb.add(it.string(info));
    }
    return Str.get(tb.finish());
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.X30 && sig == Function.STRING_JOIN && expr.length == 1 ||
        super.has(flag);
  }
}
