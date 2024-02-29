package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * XQuery atomic types.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum AtomType implements Type {
  /** Item type. */
  ITEM("item", null, EMPTY, false, false, false, false, Type.ID.ITEM),

  /** Untyped type. */
  UNTYPED("untyped", null, XS_URI, false, false, false, false, Type.ID.UTY),

  /** Any type. */
  ANY_TYPE("anyType", null, XS_URI, false, false, false, false, Type.ID.ATY),

  /** Any simple type. */
  ANY_SIMPLE_TYPE("anySimpleType", null, XS_URI, false, false, false, false, Type.ID.AST),

  /** Any atomic type. */
  ANY_ATOMIC_TYPE("anyAtomicType", ITEM, XS_URI, false, false, false, false, Type.ID.AAT) {
    @Override
    public Atm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Atm.get(item.string(info));
    }
    @Override
    public Atm cast(final Object value, final QueryContext qc, final InputInfo info) {
      return Atm.get(token(value));
    }
  },

  /** Untyped Atomic type. */
  UNTYPED_ATOMIC("untypedAtomic", ANY_ATOMIC_TYPE, XS_URI, false, true, false, true, Type.ID.ATM) {
    @Override
    public Atm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Atm.get(item.string(info));
    }
    @Override
    public Atm cast(final Object value, final QueryContext qc, final InputInfo info) {
      return Atm.get(token(value));
    }
    @Override
    public Atm read(final DataInput in, final QueryContext qc) throws IOException {
      return Atm.get(in.readToken());
    }
  },

  /** String type. */
  STRING("string", ANY_ATOMIC_TYPE, XS_URI, false, false, true, true, Type.ID.STR) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(item.string(info));
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(value, qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken());
    }
  },

  /** Normalized String type. */
  NORMALIZED_STRING("normalizedString", STRING, XS_URI, false, false, true, true, Type.ID.NST) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final byte[] token = item.string(info);
      final ByteList bl = new ByteList(token.length);
      for(final byte b : token) bl.add(b == '\t' || b == '\r' || b == '\n' ? ' ' : b);
      return Str.get(bl.finish(), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** Token type. */
  TOKEN("token", NORMALIZED_STRING, XS_URI, false, false, true, true, Type.ID.TOK) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(normalize(item.string(info)), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** Language type. */
  LANGUAGE("language", TOKEN, XS_URI, false, false, true, true, Type.ID.LAN) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final byte[] v = normalize(item.string(info));
      if(!LANGPATTERN.matcher(string(v)).matches()) throw castError(item, info);
      return Str.get(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** NMTOKEN type. */
  NMTOKEN("NMTOKEN", TOKEN, XS_URI, false, false, true, true, Type.ID.NMT) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final byte[] v = normalize(item.string(info));
      if(!XMLToken.isNMToken(v)) throw castError(item, info);
      return Str.get(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** Name type. */
  NAME("Name", TOKEN, XS_URI, false, false, true, true, Type.ID.NAM) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final byte[] v = normalize(item.string(info));
      if(!XMLToken.isName(v)) throw castError(item, info);
      return Str.get(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** NCName type. */
  NCNAME("NCName", NAME, XS_URI, false, false, true, true, Type.ID.NCN) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(checkName(item, info), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** ID type. */
  ID("ID", NCNAME, XS_URI, false, false, true, true, Type.ID.ID) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(checkName(item, info), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** IDREF type. */
  IDREF("IDREF", NCNAME, XS_URI, false, false, true, true, Type.ID.IDR) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(checkName(item, info), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** Entity type. */
  ENTITY("ENTITY", NCNAME, XS_URI, false, false, true, true, Type.ID.ENT) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Str.get(checkName(item, info), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Str read(final DataInput in, final QueryContext qc) throws IOException {
      return Str.get(in.readToken(), this);
    }
  },

  /** Numeric type. */
  NUMERIC("numeric", ANY_ATOMIC_TYPE, XS_URI, true, false, false, true, Type.ID.NUM) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      // return double
      return item.type.isNumber() ? item : Dbl.get(checkNum(item, info).dbl(info));
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      // return double
      return cast(Str.get(value, qc, info), qc, info);
    }
  },

  /** Float type. */
  FLOAT("float", NUMERIC, XS_URI, true, false, false, true, Type.ID.FLT) {
    @Override
    public Flt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Flt.get(checkNum(item, info).flt(info));
    }
    @Override
    public Flt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Flt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return Flt.get(in.readToken(), null);
    }
  },

  /** Double type. */
  DOUBLE("double", NUMERIC, XS_URI, true, false, false, true, Type.ID.DBL) {
    @Override
    public Dbl cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Dbl.get(checkNum(item, info).dbl(info));
    }
    @Override
    public Dbl cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dbl read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return Dbl.get(in.readToken(), null);
    }
  },

  /** Decimal type. */
  DECIMAL("decimal", NUMERIC, XS_URI, true, false, false, true, Type.ID.DEC) {
    @Override
    public Dec cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Dec.get(checkNum(item, info).dec(info));
    }
    @Override
    public Dec cast(final Object value, final QueryContext qc, final InputInfo info) {
      return Dec.get(value instanceof BigDecimal ? (BigDecimal) value :
        new BigDecimal(string(token(value))));
    }
    @Override
    public Dec read(final DataInput in, final QueryContext qc) throws IOException {
      return Dec.get(new BigDecimal(string(in.readToken())));
    }
  },

  /** Precision decimal type. */
  PRECISION_DECIMAL("precisionDecimal", null, XS_URI, true, false, false, true, Type.ID.PDC),

  /** Integer type. */
  INTEGER("integer", DECIMAL, XS_URI, true, false, false, true, Type.ID.ITR) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Int.get(checkLong(value, 0, 0, info));
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong());
    }
  },

  /** Non-positive integer type. */
  NON_POSITIVE_INTEGER("nonPositiveInteger", INTEGER, XS_URI, true, false, false, true,
      Type.ID.NPI) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, Long.MIN_VALUE, 0, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Negative integer type. */
  NEGATIVE_INTEGER("negativeInteger", NON_POSITIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.NIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, Long.MIN_VALUE, -1, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Long type. */
  LONG("long", INTEGER, XS_URI, true, false, false, true, Type.ID.LNG) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 0, 0, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Int type. */
  INT("int", LONG, XS_URI, true, false, false, true, Type.ID.INT) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, -0x80000000, 0x7FFFFFFF, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Short type. */
  SHORT("short", INT, XS_URI, true, false, false, true, Type.ID.SHR) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, -0x8000, 0x7FFF, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Byte type. */
  BYTE("byte", SHORT, XS_URI, true, false, false, true, Type.ID.BYT) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, -0x80, 0x7F, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Non-negative integer type. */
  NON_NEGATIVE_INTEGER("nonNegativeInteger", INTEGER, XS_URI, true, false, false, true,
      Type.ID.NNI) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 0, Long.MAX_VALUE, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Unsigned long type. */
  UNSIGNED_LONG("unsignedLong", NON_NEGATIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.ULN) {
    @Override
    public Uln cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Uln cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {

      final Item item = checkNum(value, info);
      final BigDecimal v = item.dec(info), i = v.setScale(0, RoundingMode.DOWN);
      // equals() used to also test fractional digits
      if(v.signum() < 0 || v.compareTo(Uln.MAXULN) > 0 ||
        item.type.isStringOrUntyped() && !v.equals(i)) throw castError(item, info);
      return new Uln(i.toBigInteger());
    }
    @Override
    public Uln read(final DataInput in, final QueryContext qc) throws IOException {
      return new Uln(new BigInteger(string(in.readToken())));
    }
  },

  /** Short type. */
  UNSIGNED_INT("unsignedInt", UNSIGNED_LONG, XS_URI, true, false, false, true, Type.ID.UIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 0, 0xFFFFFFFFL, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Unsigned Short type. */
  UNSIGNED_SHORT("unsignedShort", UNSIGNED_INT, XS_URI, true, false, false, true, Type.ID.USH) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 0, 0xFFFF, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Unsigned byte type. */
  UNSIGNED_BYTE("unsignedByte", UNSIGNED_SHORT, XS_URI, true, false, false, true, Type.ID.UBY) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 0, 0xFF, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Positive integer type. */
  POSITIVE_INTEGER("positiveInteger", NON_NEGATIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.PIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Int(checkLong(value, 1, Long.MAX_VALUE, info), this);
    }
    @Override
    public Int read(final DataInput in, final QueryContext qc) throws IOException {
      return Int.get(in.readLong(), this);
    }
  },

  /** Duration type. */
  DURATION("duration", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.DUR) {
    @Override
    public Dur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof Dur) return new Dur((Dur) item);
      if(isString(item)) return new Dur(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Dur cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dur read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Dur(in.readToken(), null);
    }
  },

  /** Year month duration type. */
  YEAR_MONTH_DURATION("yearMonthDuration", DURATION, XS_URI, false, false, false, true,
      Type.ID.YMD) {
    @Override
    public YMDur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof Dur) return new YMDur((Dur) item);
      if(isString(item)) return new YMDur(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public YMDur cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public YMDur read(final DataInput in, final QueryContext qc)
        throws IOException, QueryException {
      return new YMDur(in.readToken(), null);
    }
  },

  /** Day time duration type. */
  DAY_TIME_DURATION("dayTimeDuration", DURATION, XS_URI, false, false, false, true, Type.ID.DTD) {
    @Override
    public DTDur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof Dur) return new DTDur((Dur) item);
      if(isString(item)) return new DTDur(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public DTDur cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public DTDur read(final DataInput in, final QueryContext qc)
        throws IOException, QueryException {
      return new DTDur(in.readToken(), null);
    }
  },

  /** DateTime type. */
  DATE_TIME("dateTime", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.DTM) {
    @Override
    public Dtm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type == DATE) return new Dtm((ADate) item);
      if(isString(item)) return new Dtm(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Dtm cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dtm read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Dtm(in.readToken(), null);
    }
  },

  /** DateTimeStamp type. */
  DATE_TIME_STAMP("dateTimeStamp", null, XS_URI, false, false, false, true, Type.ID.DTS),

  /** Date type. */
  DATE("date", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.DAT) {
    @Override
    public Dat cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type == DATE_TIME) return new Dat((ADate) item);
      if(isString(item)) return new Dat(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Dat cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dat read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Dat(in.readToken(), null);
    }
  },

  /** Time type. */
  TIME("time", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.TIM) {
    @Override
    public Tim cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type == DATE_TIME) return new Tim((ADate) item);
      if(isString(item)) return new Tim(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Tim cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Tim read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Tim(in.readToken(), null);
    }
  },

  /** Year month type. */
  G_YEAR_MONTH("gYearMonth", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.YMO) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME, DATE)) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(info), this, info);
      throw typeError(item, this, info);
    }
    @Override
    public GDt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public GDt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new GDt(in.readToken(), this, null);
    }
  },

  /** Year type. */
  G_YEAR("gYear", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.YEA) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME, DATE)) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(info), this, info);
      throw typeError(item, this, info);
    }
    @Override
    public GDt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public GDt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new GDt(in.readToken(), this, null);
    }
  },

  /** Month day type. */
  G_MONTH_DAY("gMonthDay", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.MDA) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME, DATE)) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(info), this, info);
      throw typeError(item, this, info);
    }
    @Override
    public GDt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public GDt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new GDt(in.readToken(), this, null);
    }
  },

  /** Day type. */
  G_DAY("gDay", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.DAY) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME, DATE)) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(info), this, info);
      throw typeError(item, this, info);
    }
    @Override
    public GDt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public GDt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new GDt(in.readToken(), this, null);
    }
  },

  /** Month type. */
  G_MONTH("gMonth", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.MON) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME, DATE)) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(info), this, info);
      throw typeError(item, this, info);
    }
    @Override
    public GDt cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public GDt read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new GDt(in.readToken(), this, null);
    }
  },

  /** Boolean type. */
  BOOLEAN("boolean", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.BLN) {
    @Override
    public Bln cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof ANum) return Bln.get(item.bool(info));
      if(isString(item)) return Bln.get(Bln.parse(item, info));
      throw typeError(item, this, info);
    }
    @Override
    public Bln cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return value instanceof Boolean ? Bln.get((Boolean) value) :
        cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Bln read(final DataInput in, final QueryContext qc) throws IOException {
      return Bln.get(in.readBool());
    }
  },

  /** Implementation specific: binary type. */
  BINARY("binary", ANY_ATOMIC_TYPE, BASEX_URI, false, false, false, true, Type.ID.BIN),

  /** Base64 binary type. */
  BASE64_BINARY("base64Binary", BINARY, XS_URI, false, false, false, true, Type.ID.B64) {
    @Override
    public B64 cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof Bin) return B64.get((Bin) item, info);
      if(isString(item)) return B64.get(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public B64 cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return value instanceof byte[] ? B64.get((byte[]) value) : B64.get(token(value), info);
    }
    @Override
    public B64 read(final DataInput in, final QueryContext qc) throws IOException {
      return B64.get(in.readToken());
    }
  },

  /** Hex binary type. */
  HEX_BINARY("hexBinary", BINARY, XS_URI, false, false, false, true, Type.ID.HEX) {
    @Override
    public Hex cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof Bin) return new Hex((Bin) item, info);
      if(isString(item)) return new Hex(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Hex cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Hex(value instanceof byte[] ? (byte[]) value : token(value), info);
    }
    @Override
    public Hex read(final DataInput in, final QueryContext qc) throws IOException {
      return new Hex(in.readToken());
    }
  },

  /** Any URI type. */
  ANY_URI("anyURI", ANY_ATOMIC_TYPE, XS_URI, false, false, true, true, Type.ID.URI) {
    @Override
    public Uri cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(!item.type.isStringOrUntyped()) throw typeError(item, this, info);
      final Uri u = Uri.get(item.string(info));
      if(!u.isValid()) throw castError(item, info);
      return u;
    }
    @Override
    public Uri cast(final Object value, final QueryContext qc, final InputInfo info) {
      return Uri.get(token(value));
    }
    @Override
    public Uri read(final DataInput in, final QueryContext qc) throws IOException {
      return Uri.get(in.readToken());
    }
  },

  /** QName Type. */
  QNAME("QName", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.QNM) {
    @Override
    public QNm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final Type type = item.type;
      if(type != STRING && !type.isUntyped()) throw typeError(item, this, info);
      final byte[] name = trim(item.string(info));
      if(XMLToken.isQName(name)) {
        final QNm qnm = qc.shared.qName(name, info.sc().ns.uri(prefix(name)));
        if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(info, qnm.prefix());
        return qnm;
      }
      throw castError(item, info);
    }
    @Override
    public QNm cast(final Object value, final QueryContext qc, final InputInfo info) {
      return value instanceof QName ? new QNm((QName) value) : new QNm(value.toString());
    }
    @Override
    public QNm read(final DataInput in, final QueryContext qc) throws IOException {
      return new QNm(in.readToken(), in.readBool() ? in.readToken() : null);
    }
  },

  /** NOTATION Type. */
  NOTATION("NOTATION", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.NOT);

  /** Language pattern. */
  private static final Pattern LANGPATTERN = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z\\d]{1,8})*");

  /** Cached enums (faster). */
  private static final AtomType[] VALUES = values();

  /** Name. */
  private final byte[] name;
  /** Parent type. */
  private final AtomType parent;
  /** URI. */
  private final byte[] uri;

  /** Type ID. */
  private final ID id;
  /** Number flag. */
  private final boolean numeric;
  /** Untyped flag. */
  private final boolean untyped;
  /** String flag. */
  private final boolean string;
  /** Sortable flag. */
  private final boolean sortable;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;
  /** QName (lazy instantiation). */
  private QNm qnm;

  /**
   * Constructor.
   * @param name string representation
   * @param parent parent type
   * @param uri uri
   * @param numeric numeric flag
   * @param untyped untyped flag
   * @param string string flag
   * @param sortable sortable flag
   * @param id type ID
   */
  AtomType(final String name, final AtomType parent, final byte[] uri, final boolean numeric,
      final boolean untyped, final boolean string, final boolean sortable, final ID id) {
    this.name = token(name);
    this.parent = parent;
    this.uri = uri;
    this.numeric = numeric;
    this.untyped = untyped;
    this.string = string;
    this.sortable = sortable;
    this.id = id;
  }

  @Override
  public Item cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item.type == this) return item;
    throw typeError(item, this, info);
  }

  @Override
  public Item cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    throw Util.notExpected();
  }

  @Override
  public final SeqType seqType(final Occ occ) {
    // cannot statically be instantiated due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  /**
   * Returns the name of a node type.
   * @return name
   */
  public final QNm qname() {
    if(qnm == null) qnm = new QNm(name, uri);
    return qnm;
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    return this == type || type == AtomType.ITEM ||
        type instanceof AtomType && parent != null && parent.instanceOf(type);
  }

  @Override
  public final AtomType union(final Type type) {
    if(type.instanceOf(this)) return this;

    if(type instanceof AtomType) {
      AtomType at = (AtomType) type;
      if(instanceOf(at)) return at;
      final List<AtomType> arr = new ArrayList<>();
      while((at = at.parent) != null) arr.add(at);
      for(AtomType p = this; (p = p.parent) != null;)
        if(arr.contains(p)) return p;
    }
    return ITEM;
  }

  @Override
  public final Type intersect(final Type type) {
    return instanceOf(type) ? this : type.instanceOf(this) ? type : null;
  }

  @Override
  public final boolean isNumber() {
    return numeric;
  }

  @Override
  public final boolean isUntyped() {
    return untyped;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return numeric || untyped;
  }

  @Override
  public final boolean isStringOrUntyped() {
    return string || untyped;
  }

  @Override
  public final boolean isSortable() {
    return sortable;
  }

  @Override
  public final AtomType atomic() {
    return instanceOf(ANY_ATOMIC_TYPE) ? this : null;
  }

  @Override
  public final ID id() {
    return id;
  }

  /**
   * Throws an exception if the specified item cannot be converted to a number.
   * @param item item
   * @param info input info (can be {@code null})
   * @return item argument
   * @throws QueryException query exception
   */
  final Item checkNum(final Item item, final InputInfo info) throws QueryException {
    final Type type = item.type;
    if(item instanceof ANum || type.isStringOrUntyped() && type != ANY_URI || type == BOOLEAN)
      return item;
    throw typeError(item, this, info);
  }

  /**
   * Checks the validity of the specified object and returns its long value.
   * @param value value to be checked
   * @param min minimum value
   * @param max maximum value (no limit if identical to min)
   * @param info input info (can be {@code null})
   * @return integer value
   * @throws QueryException query exception
   */
  final long checkLong(final Object value, final long min, final long max, final InputInfo info)
      throws QueryException {

    final Item item = checkNum(value, info);
    final Type type = item.type;
    if(type.oneOf(DOUBLE, FLOAT)) {
      final double d = item.dbl(info);
      if(Double.isNaN(d) || Double.isInfinite(d)) throw valueError(this, item.string(info), info);
      if(min != max && (d < min || d > max)) throw castError(item, info);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) throw INTRANGE_X.get(info, d);
      return (long) d;
    }

    final long l = item.itr(info);
    if(min != max && (l < min || l > max)) throw castError(item, info);
    return l;
  }

  /**
   * Checks the validity of the specified object and returns it as item.
   * @param value value to be checked
   * @param info input info (can be {@code null})
   * @return integer value
   * @throws QueryException query exception
   */
  final Item checkNum(final Object value, final InputInfo info) throws QueryException {
    final Item item;
    if(value instanceof Value) {
      final Value val = (Value) value;
      if(val.size() != 1) throw typeError(val, this, info);
      item = (Item) val;
    } else if(value instanceof Double || value instanceof Float) {
      item = Dbl.get(((Number) value).doubleValue());
    } else if(value instanceof Number) {
      item = Int.get(((Number) value).longValue());
    } else if(value instanceof Character) {
      item = Int.get((char) value);
    } else {
      item = Str.get(token(value));
    }
    return checkNum(item, info);
  }

  /**
   * Checks the validity of the specified name.
   * @param item value to be checked
   * @param info input info (can be {@code null})
   * @return name
   * @throws QueryException query exception
   */
  final byte[] checkName(final Item item, final InputInfo info) throws QueryException {
    final byte[] v = normalize(item.string(info));
    if(!XMLToken.isNCName(v)) throw castError(item, info);
    return v;
  }

  /**
   * Returns a cast exception.
   * @param item item to be converted
   * @param info input info (can be {@code null})
   * @return query exception
   */
  public final QueryException castError(final Item item, final InputInfo info)  {
    return FUNCCAST_X_X_X.get(info, item.type, this, item);
  }

  /**
   * Returns a cast exception.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return query exception
   */
  public final QueryException castError(final byte[] value, final InputInfo info)  {
    return FUNCCAST_X_X.get(info, this, value);
  }

  @Override
  public final boolean nsSensitive() {
    return instanceOf(QNAME) || instanceOf(NOTATION);
  }

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(Token.eq(XS_URI, uri)) {
      tb.add(XS_PREFIX).add(':').add(name);
    } else {
      tb.add(name).add("()");
    }
    return tb.toString();
  }

  /**
   * Finds and returns the specified type.
   * @param qname name of type
   * @param all accept all types (including those without parent type)
   * @return type or {@code null}
   */
  public static AtomType find(final QNm qname, final boolean all) {
    if(!Token.eq(qname.uri(), BASEX_URI)) {
      for(final AtomType tp : VALUES) {
        if(qname.eq(tp.qname()) && (all || tp.parent != null)) return tp;
      }
    }
    return null;
  }

  /**
   * Returns an info message for a similar function.
   * @param qname name of type
   * @return info string
   */
  public static byte[] similar(final QNm qname) {
    final byte[] ln = lc(qname.local());

    final Function<AtomType, byte[]> local = tp -> {
      final QNm qnm = tp.qname();
      return Token.eq(qnm.uri(), XS_URI) && tp.parent != null ? qnm.local() : null;
    };
    Object similar = Levenshtein.similar(ln, VALUES, o -> local.apply((AtomType) o));
    if(similar == null) {
      for(final AtomType tp : VALUES) {
        final byte[] lc = local.apply(tp);
        if(lc != null && startsWith(lc, ln)) {
          similar = tp;
          break;
        }
      }
    }
    return QueryError.similar(qname.prefixId(XML), similar);
  }

  /**
   * Checks if the specified item is a string.
   * @param item item
   * @return item argument
   */
  private static boolean isString(final Item item) {
    final Type type = item.type;
    return type.isStringOrUntyped() && type != ANY_URI;
  }
}
