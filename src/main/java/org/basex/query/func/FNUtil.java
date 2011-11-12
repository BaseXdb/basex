package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.CRC32;

import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.in.TextInput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Dbl;
import org.basex.query.item.Hex;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.ItrSeq;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ValueIter;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.basex.util.list.ByteList;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class FNUtil extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNUtil(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case _UTIL_EVAL:     return eval(ctx).iter();
      case _UTIL_RUN:      return run(ctx).iter();
      case _UTIL_TO_BYTES: return toBytes(ctx);
      default:             return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case _UTIL_EVAL: return eval(ctx);
      case _UTIL_RUN:  return run(ctx);
      default:         return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case _UTIL_FORMAT:            return format(ctx);
      case _UTIL_MB:                return mb(ctx);
      case _UTIL_MS:                return ms(ctx);
      case _UTIL_INTEGER_FROM_BASE: return fromBase(ctx, ii);
      case _UTIL_INTEGER_TO_BASE:   return toBase(ctx, ii);
      case _UTIL_MD5:               return hash(ctx, "MD5");
      case _UTIL_SHA1:              return hash(ctx, "SHA");
      case _UTIL_CRC32:             return crc32(ctx);
      case _UTIL_UUID:              return uuid();
      case _UTIL_TO_STRING:         return toString(ctx);
      default:                      return super.item(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {

    final QueryContext qc = new QueryContext(ctx.context);
    qc.parse(string(qu));
    qc.compile();
    return qc.value();
  }

  /**
   * Performs the run function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value run(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    try {
      return eval(ctx, io.read());
    } catch(final IOException ex) {
      throw IOERR.thrw(input, ex);
    }
  }

  /**
   * Formats a string according to the specified format.
   * @param ctx query context
   * @return formatted string
   * @throws QueryException query exception
   */
  private Str format(final QueryContext ctx) throws QueryException {
    final String form = string(checkStr(expr[0], ctx));
    final Object[] args = new Object[expr.length - 1];
    for(int e = 1; e < expr.length; e++) {
      args[e - 1] = expr[e].item(ctx, input).toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORM.thrw(input, Util.name(ex), ex.getMessage());
    }
  }

  /**
   * Measures the memory consumption for the specified expression in MB.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Dbl mb(final QueryContext ctx) throws QueryException {
    // check caching flag
    final boolean c = expr.length == 2 &&
      checkType(expr[1].item(ctx, input), AtomType.BLN).bool(input);

    // measure initial memory consumption
    Performance.gc(3);
    final long l = Performance.mem();

    // create (and, optionally, cache) result value
    Value val = ctx.value(expr[0]);
    if(c) val = val.cache().value();

    // measure resulting memory consumption
    Performance.gc(2);
    final double d = Performance.mem() - l;

    // loop through all results to avoid premature result disposal
    final Iter ir = val.iter();
    while(ir.next() != null);

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
    final boolean c = expr.length == 2 &&
      checkType(expr[1].item(ctx, input), AtomType.BLN).bool(input);

    // create timer
    final Performance p = new Performance();

    // iterate (and, optionally, cache) results
    if(c) {
      ctx.value(expr[0]).cache();
    } else {
      final Iter ir = ctx.iter(expr[0]);
      while(ir.next() != null);
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

    final long num = checkItr(expr[0], ctx), base = checkItr(expr[1], ctx);
    if(base < 2 || base > 36) INVBASE.thrw(ii, base);

    // use fast variant for powers of two
    for(int i = 1, p = 2; i < 6; i++, p <<= 1)
      if(base == p) return toBaseFast(num, i);

    final ByteList tb = new ByteList();
    long n = num;
    if(n < 0) {
      // unsigned value doesn't fit in any native type...
      final BigInteger[] dr = BigInteger.valueOf(n).add(
          MAX_ULONG).divideAndRemainder(BigInteger.valueOf(base));
      n = dr[0].longValue();
      tb.add(DIGITS[dr[1].intValue()]);
    } else {
      tb.add(DIGITS[(int) (n % base)]);
      n /= base;
    }
    while (n != 0) {
      tb.add(DIGITS[(int) (n % base)]);
      n /= base;
    }

    final byte[] res = tb.toArray();
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
      return new Hex(MessageDigest.getInstance(algo).digest(str));
    } catch(final NoSuchAlgorithmException ex) {
      throw Util.notexpected(ex);
    }
  }

  /**
   * Creates the CRC32 hash of the given xs:string.
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex crc32(final QueryContext ctx) throws QueryException {
    final CRC32 crc = new CRC32();
    crc.update(checkStr(expr[0], ctx));
    final byte[] res = new byte[4];
    for(int i = res.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8)
      res[i] = (byte) (c & 0xFF);
    return new Hex(res);
  }

  /**
   * Extracts the bytes from the given item.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Iter toBytes(final QueryContext ctx) throws QueryException {
    final byte[] bytes = checkBin(expr[0], ctx);
    return new ValueIter() {
      final int bl = bytes.length;
      int pos;

      @Override
      public Value value() {
        final long[] tmp = new long[bl - pos];
        for(int i = 0; i < tmp.length; i++) tmp[i] = bytes[pos + i];
        return ItrSeq.get(tmp, AtomType.BYT);
      }
      @Override
      public Item get(final long i) {
        return Itr.get(bytes[(int) i], AtomType.BYT);
      }
      @Override
      public Item next() { return pos < size() ? get(pos++) : null; }
      @Override
      public boolean reset() { pos = 0; return true; }
      @Override
      public long size() { return bl; }
    };
  }

  /**
   * Converts the specified data to a string.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Str toString(final QueryContext ctx) throws QueryException {
    final byte[] val = checkBin(expr[0], ctx);
    final String enc = expr.length == 2 ? string(checkStr(expr[1], ctx)) : UTF8;
    try {
      return Str.get(TextInput.content(new IOContent(val), enc).finish());
    } catch(final IOException ex) {
      throw CONVERT.thrw(input, ex);
    }
  }

  /**
   * Creates a random UUID.
   * @return random UUID
   */
  private Str uuid() {
    return Str.get(UUID.randomUUID());
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT && (def == Function._UTIL_EVAL ||
        def == Function._UTIL_RUN || def == Function._UTIL_MB ||
        def == Function._UTIL_MS || def == Function._UTIL_UUID) ||
      super.uses(u);
  }
}
