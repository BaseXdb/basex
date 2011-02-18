package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Hex;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class FNUtil extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNUtil(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case EVAL:
        return eval(ctx);
      case RUN:
        return run(ctx);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case MB: return mb(ctx);
      case MS: return ms(ctx);
      case FRM_BASE: return fromBase(ctx, ii);
      case TO_BASE: return toBase(ctx, ii);
      case MD5: return hash(ctx, "MD5");
      case SHA1: return hash(ctx, "SHA");
      case CRC32: return crc32(ctx);
      default: return super.item(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Performs the query function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter run(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    try {
      return eval(ctx, io.content());
    } catch(final IOException ex) {
      NODOC.thrw(input, ex.toString());
      return null;
    }
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(qu));
    qt.compile();
    return ItemIter.get(qt.iter());
  }

  /**
   * Measures the memory consumption for the specified expression in MB.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Dbl mb(final QueryContext ctx) throws QueryException {
    // check caching flag
    final boolean c = expr.length == 2
        && checkType(expr[1].item(ctx, input), Type.BLN).bool(input);

    // measure initial memory consumption
    Performance.gc(3);
    final long l = Performance.mem();

    // create (and, optionally, cache) result value
    Iter ir = expr[0].iter(ctx);
    final Value v = (c ? ItemIter.get(ir) : ir).finish();

    // measure resulting memory consumption
    Performance.gc(2);
    final double d = Performance.mem() - l;

    // loop through all results to avoid premature result disposal
    ir = v.iter();
    while(ir.next() != null)
      ;

    // return memory consumption in megabytes
    return Dbl.get(Math.max(0, d) / 1024 / 1024d);
  }

  /**
   * Measures the execution time for the specified expression in milliseconds.
   * @param ctx query context
   * @return time in milliseconds
   * @throws QueryException query exception
   */
  private Dbl ms(final QueryContext ctx) throws QueryException {
    // check caching flag
    final boolean c = expr.length == 2
        && checkType(expr[1].item(ctx, input), Type.BLN).bool(input);

    // create timer
    final Performance p = new Performance();

    // iterate (and, optionally, cache) results
    final Iter ir = expr[0].iter(ctx);
    if(c) {
      ItemIter.get(ir);
    } else {
      while(ir.next() != null)
        ;
    }

    // return measured time in milliseconds
    return Dbl.get(p.getTime() / 10000 / 100d);
  }

  /** Digits used in base conversion. */
  private static final byte[] DIGITS = {
    '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'a' , 'b' ,
    'c' , 'd' , 'e' , 'f' , 'g' , 'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
    'o' , 'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' , 'w' , 'x' , 'y' , 'z'
  };

  /**
   * Converts the given number to a string, using base
   * 2<sup>shift</sup>.
   * @param num number item
   * @param shift number of bits to use for one digit
   * @return string representation of the given number
   */
  private Str toBaseFast(final long num, final int shift) {
    final byte[] bytes = new byte[(64 + shift - 1) / shift];
    final int mask = (1 << shift) - 1;
    long n = num;
    int pos = bytes.length;
    do {
      bytes[--pos] = DIGITS[(int) (n & mask)];
      n >>>= shift;
    } while(n != 0);
    return Str.get(substring(bytes, pos));
  }

  /** BigInteger representing 2 * ({@link Long#MAX_VALUE} + 1). */
  private static final BigInteger MAX_ULONG = BigInteger.ONE.shiftLeft(64);

  /**
   * Converts the given number to a string, using the given base.
   * @param ctx query context
   * @param ii input info
   * @return string representation of the given number
   * @throws QueryException query exception
   */
  private Str toBase(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final long num = checkItr(expr[0], ctx),
               base = checkItr(expr[1], ctx);
    if(base < 2 || base > 36) INVBASE.thrw(ii, base);

    // use fast variant for powers of two
    for(int i = 1, p = 2; i < 6; i++, p <<= 1)
      if(base == p) return toBaseFast(num, i);

    final TokenBuilder tb = new TokenBuilder();
    long n = num;
    if(n < 0) {
      // unsigned value doesn't fit in any native type...
      final BigInteger[] dr = BigInteger.valueOf(n).add(
          MAX_ULONG).divideAndRemainder(BigInteger.valueOf(base));
      n = dr[0].longValue();
      tb.addByte(DIGITS[dr[1].intValue()]);
    } else {
      tb.addByte(DIGITS[(int) (n % base)]);
      n /= base;
    }

    while (n != 0) {
      tb.addByte(DIGITS[(int) (n % base)]);
      n /= base;
    }

    final byte[] res = tb.finish();
    Array.reverse(res);
    return Str.get(res);
  }

  /**
   * Converts the given string to a number, interpreting it as an xs:integer
   * encoded in the given base.
   * @param ctx query context
   * @param ii input info
   * @return read integer
   * @throws QueryException exception
   */
  private Itr fromBase(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final byte[] str = checkStr(expr[0], ctx);
    final long base = checkItr(expr[1], ctx);
    if(base < 2 || base > 36) INVBASE.thrw(ii, base);

    long res = 0;
    for(final byte b : str) {
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' ||
          b >= 'A' && b <= 'Z') || num >= base)
        INVDIG.thrw(ii, base, (char) (b & 0xff));

      res = res * base + num;
    }

    return Itr.get(res);
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param ctx query context
   * @param algo hashing algorithm
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex hash(final QueryContext ctx, final String algo)
      throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    try {
      final byte[] hash = MessageDigest.getInstance(algo).digest(str);
      return new Hex(hash);
    } catch(final NoSuchAlgorithmException ex) {
      Util.notexpected(ex);
      return null;
    }
  }

  /**
   * Creates the CRC32 hash of the given xs:string.
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex crc32(final QueryContext ctx) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    final CRC32 crc = new CRC32();
    crc.update(str);
    final byte[] res = new byte[4];
    for(int i = res.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8)
      res[i] = (byte) (c & 0xFFL);
    return new Hex(res);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && (def == FunDef.MB || def == FunDef.MB
        || def == FunDef.EVAL || def == FunDef.RUN) || super.uses(u);
  }
}
