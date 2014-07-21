package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions for converting data to other formats.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNConvert extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNConvert(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _CONVERT_BINARY_TO_BYTES: return binaryToBytes(qc).iter();
      default:                       return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _CONVERT_BINARY_TO_BYTES: return binaryToBytes(qc);
      default:                       return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _CONVERT_INTEGER_FROM_BASE:   return integerFromBase(qc, ii);
      case _CONVERT_INTEGER_TO_BASE:     return integerToBase(qc, ii);
      case _CONVERT_BINARY_TO_STRING:    return toString(qc);
      case _CONVERT_STRING_TO_BASE64:    return new B64(stringToBinary(qc));
      case _CONVERT_BYTES_TO_BASE64:     return new B64(bytesToBinary(qc));
      case _CONVERT_STRING_TO_HEX:       return new Hex(stringToBinary(qc));
      case _CONVERT_BYTES_TO_HEX:        return new Hex(bytesToBinary(qc));
      case _CONVERT_DATETIME_TO_INTEGER: return dateTimeToInteger(qc);
      case _CONVERT_INTEGER_TO_DATETIME: return integerToDateTime(qc);
      case _CONVERT_DAYTIME_TO_INTEGER:  return dayTimeToInteger(qc);
      case _CONVERT_INTEGER_TO_DAYTIME:  return integerToDayTime(qc);
      default:                           return super.item(qc, ii);
    }
  }

  /** Digits used in base conversion. */
  private static final byte[] DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  /**
   * Converts the given number to a string, using base
   * 2<sup>shift</sup>.
   * @param num number item
   * @param shift number of bits to use for one digit
   * @return string representation of the given number
   */
  private static Str toBaseFast(final long num, final int shift) {
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
   * @param qc query context
   * @param ii input info
   * @return string representation of the given number
   * @throws QueryException query exception
   */
  private Str integerToBase(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long num = checkItr(exprs[0], qc), base = checkItr(exprs[1], qc);
    if(base < 2 || base > 36) throw INVBASE.get(ii, base);

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
    while(n != 0) {
      tb.add(DIGITS[(int) (n % base)]);
      n /= base;
    }

    final byte[] res = tb.finish();
    Array.reverse(res);
    return Str.get(res);
  }

  /**
   * Converts the given string to a number, interpreting it as an xs:integer
   * encoded in the given base.
   * @param qc query context
   * @param ii input info
   * @return read integer
   * @throws QueryException exception
   */
  private Int integerFromBase(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] str = checkStr(exprs[0], qc);
    final long base = checkItr(exprs[1], qc);
    if(base < 2 || base > 36) throw INVBASE.get(ii, base);

    long res = 0;
    for(final byte b : str) {
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' ||
          b >= 'A' && b <= 'Z') || num >= base)
        throw INVDIG.get(ii, base, (char) (b & 0xff));

      res = res * base + num;
    }

    return Int.get(res);
  }

  /**
   * Extracts the bytes from a given item.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value binaryToBytes(final QueryContext qc) throws QueryException {
    try {
      return BytSeq.get(checkItem(exprs[0], qc).input(info).content());
    } catch(final IOException ex) {
      throw BXCO_STRING.get(info, ex);
    }
  }

  /**
   * Converts the specified integer to a dateTime item.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Dtm integerToDateTime(final QueryContext qc) throws QueryException {
    return new Dtm(checkItr(exprs[0], qc), info);
  }

  /**
   * Converts the specified dateTime to milliseconds.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Int dateTimeToInteger(final QueryContext qc) throws QueryException {
    return Int.get(dateTimeToMs(exprs[0], qc));
  }

  /**
   * Converts the specified integer to a dayTimeDuration item.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private DTDur integerToDayTime(final QueryContext qc) throws QueryException {
    return new DTDur(checkItr(exprs[0], qc));
  }

  /**
   * Converts the specified dayTimeDuration to milliseconds.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Int dayTimeToInteger(final QueryContext qc) throws QueryException {
    final DTDur dur = (DTDur) checkType(checkItem(exprs[0], qc), AtomType.DTD);
    final BigDecimal ms = dur.sec.multiply(BigDecimal.valueOf(1000));
    if(ms.compareTo(ADateDur.BDMAXLONG) > 0) throw INTRANGE.get(info, ms);
    return Int.get(ms.longValue());
  }

  /**
   * Converts the specified data to a string.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Str toString(final QueryContext qc) throws QueryException {
    final Bin bin = checkBin(exprs[0], qc);
    final String enc = checkEncoding(1, BXCO_ENCODING, qc);
    try {
      return Str.get(toString(bin.input(info), enc, qc));
    } catch(final IOException ex) {
      throw BXCO_STRING.get(info, ex);
    }
  }

  /**
   * Converts the specified input to a string in the specified encoding.
   * @param is input stream
   * @param enc encoding
   * @param qc query context
   * @return resulting value
   * @throws IOException I/O exception
   */
  public static byte[] toString(final InputStream is, final String enc, final QueryContext qc)
      throws IOException {
    return toString(is, enc, qc.context.options.get(MainOptions.CHECKSTRINGS));
  }

  /**
   * Converts the specified input to a string in the specified encoding.
   * @param is input stream
   * @param enc encoding
   * @param val validate string
   * @return resulting value
   * @throws IOException I/O exception
   */
  public static byte[] toString(final InputStream is, final String enc, final boolean val)
      throws IOException {
    try {
      return new TextInput(is).encoding(enc).validate(val).content();
    } finally {
      is.close();
    }
  }

  /**
   * Converts the first argument from a string to a byte array.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private byte[] stringToBinary(final QueryContext qc) throws QueryException {
    final byte[] in = checkStr(exprs[0], qc);
    final String enc = checkEncoding(1, BXCO_ENCODING, qc);
    if(enc == null || enc == UTF8) return in;
    try {
      return toBinary(in, enc);
    } catch(final CharacterCodingException ex) {
      throw BXCO_BASE64.get(info, chop(in, info), enc);
    }
  }

  /**
   * Converts the first argument from a string to a byte array.
   * @param in input string
   * @param enc encoding
   * @return resulting value
   * @throws CharacterCodingException character coding exception
   */
  public static byte[] toBinary(final byte[] in, final String enc) throws CharacterCodingException {
    if(enc == UTF8) return in;
    final ByteBuffer bb = Charset.forName(enc).newEncoder().encode(CharBuffer.wrap(string(in)));
    final int il = bb.limit();
    final byte[] tmp = bb.array();
    return tmp.length == il  ? tmp : Arrays.copyOf(tmp, il);
  }

  /**
   * Converts the first argument from a byte sequence to a byte array.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private byte[] bytesToBinary(final QueryContext qc) throws QueryException {
    final Value v = exprs[0].value(qc);
    // directly pass on byte array
    if(v instanceof BytSeq) return ((BytSeq) v).toJava();

    // check if all arguments are bytes
    final Iter ir = v.iter();
    final ByteList bl = new ByteList(Math.max(Array.CAPACITY, (int) v.size()));
    for(Item it; (it = ir.next()) != null;) {
      bl.add((int) ((ANum) checkType(it, AtomType.BYT)).itr());
    }
    return bl.finish();
  }
}
