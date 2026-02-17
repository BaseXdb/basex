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
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Basic XDM types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum BasicType implements Type {
  /** Item type. */
  ITEM("item", null, Type.ID.ITEM, false, false),

  /** Untyped type. */
  UNTYPED("untyped", null, Type.ID.UTY, false, false),

  /** Any type. */
  ANY_TYPE("anyType", null, Type.ID.ATY, false, false),

  /** Any simple type. */
  ANY_SIMPLE_TYPE("anySimpleType", null, Type.ID.AST, false, false),

  /** Any atomic type. */
  ANY_ATOMIC_TYPE("anyAtomicType", ITEM, Type.ID.AAT, false, false),

  /** Error type. */
  ERROR("error", ANY_ATOMIC_TYPE, Type.ID.ERR, false, false),

  /** Untyped Atomic type. */
  UNTYPED_ATOMIC("untypedAtomic", ANY_ATOMIC_TYPE, Type.ID.ATM, false, false) {
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
  STRING("string", ANY_ATOMIC_TYPE, Type.ID.STR, false, true) {
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
  NORMALIZED_STRING("normalizedString", STRING, Type.ID.NST, false, true) {
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
  TOKEN("token", NORMALIZED_STRING, Type.ID.TOK, false, true) {
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
  LANGUAGE("language", TOKEN, Type.ID.LAN, false, true) {
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
  NMTOKEN("NMTOKEN", TOKEN, Type.ID.NMT, false, true) {
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
  NAME("Name", TOKEN, Type.ID.NAM, false, true) {
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
  NCNAME("NCName", NAME, Type.ID.NCN, false, true) {
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
  ID("ID", NCNAME, Type.ID.ID, false, true) {
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
  IDREF("IDREF", NCNAME, Type.ID.IDR, false, true) {
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
  ENTITY("ENTITY", NCNAME, Type.ID.ENT, false, true) {
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
  NUMERIC("numeric", ANY_ATOMIC_TYPE, Type.ID.NUM, true, false) {
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
  FLOAT("float", NUMERIC, Type.ID.FLT, true, false) {
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
      return Flt.get(Flt.parse(in.readToken(), null));
    }
  },

  /** Double type. */
  DOUBLE("double", NUMERIC, Type.ID.DBL, true, false) {
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
      return Dbl.get(Dbl.parse(in.readToken(), null));
    }
  },

  /** Decimal type. */
  DECIMAL("decimal", NUMERIC, Type.ID.DEC, true, false) {
    @Override
    public Dec cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Dec.get(checkNum(item, info).dec(info));
    }
    @Override
    public Dec cast(final Object value, final QueryContext qc, final InputInfo info) {
      return Dec.get(value instanceof final BigDecimal bd ? bd :
        new BigDecimal(string(token(value))));
    }
    @Override
    public Dec read(final DataInput in, final QueryContext qc) throws IOException {
      return Dec.get(new BigDecimal(string(in.readToken())));
    }
  },

  /** Precision decimal type. */
  PRECISION_DECIMAL("precisionDecimal", null, Type.ID.PDC, true, false),

  /** Integer type. */
  INTEGER("integer", DECIMAL, Type.ID.ITR, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return Itr.get(checkLong(value, 0, 0, info));
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong());
    }
  },

  /** Non-positive integer type. */
  NON_POSITIVE_INTEGER("nonPositiveInteger", INTEGER, Type.ID.NPI, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, Long.MIN_VALUE, 0, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Negative integer type. */
  NEGATIVE_INTEGER("negativeInteger", NON_POSITIVE_INTEGER, Type.ID.NIN, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, Long.MIN_VALUE, -1, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Long type. */
  LONG("long", INTEGER, Type.ID.LNG, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 0, 0, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Int type. */
  INT("int", LONG, Type.ID.INT, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, -0x80000000, 0x7FFFFFFF, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Short type. */
  SHORT("short", INT, Type.ID.SHR, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, -0x8000, 0x7FFF, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Byte type. */
  BYTE("byte", SHORT, Type.ID.BYT, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, -0x80, 0x7F, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Non-negative integer type. */
  NON_NEGATIVE_INTEGER("nonNegativeInteger", INTEGER, Type.ID.NNI, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 0, Long.MAX_VALUE, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Unsigned long type. */
  UNSIGNED_LONG("unsignedLong", NON_NEGATIVE_INTEGER, Type.ID.ULN, true, false) {
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
  UNSIGNED_INT("unsignedInt", UNSIGNED_LONG, Type.ID.UIN, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 0, 0xFFFFFFFFL, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Unsigned Short type. */
  UNSIGNED_SHORT("unsignedShort", UNSIGNED_INT, Type.ID.USH, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 0, 0xFFFF, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Unsigned byte type. */
  UNSIGNED_BYTE("unsignedByte", UNSIGNED_SHORT, Type.ID.UBY, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 0, 0xFF, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Positive integer type. */
  POSITIVE_INTEGER("positiveInteger", NON_NEGATIVE_INTEGER, Type.ID.PIN, true, false) {
    @Override
    public Itr cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast((Object) item, qc, info);
    }
    @Override
    public Itr cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Itr(checkLong(value, 1, Long.MAX_VALUE, info), this);
    }
    @Override
    public Itr read(final DataInput in, final QueryContext qc) throws IOException {
      return Itr.get(in.readLong(), this);
    }
  },

  /** Duration type. */
  DURATION("duration", ANY_ATOMIC_TYPE, Type.ID.DUR, false, false) {
    @Override
    public Dur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof final Dur dur) return new Dur(dur);
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
  YEAR_MONTH_DURATION("yearMonthDuration", DURATION, Type.ID.YMD, false, false) {
    @Override
    public YMDur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof final Dur dur) return new YMDur(dur);
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
  DAY_TIME_DURATION("dayTimeDuration", DURATION, Type.ID.DTD, false, false) {
    @Override
    public DTDur cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof final Dur dur) return new DTDur(dur);
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
  DATE_TIME("dateTime", ANY_ATOMIC_TYPE, Type.ID.DTM, false, false) {
    @Override
    public Dtm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE, DATE_TIME_STAMP)) return new Dtm((ADate) item, DATE_TIME, info);
      if(isString(item)) return new Dtm(item.string(info), DATE_TIME, info);
      throw typeError(item, this, info);
    }
    @Override
    public Dtm cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dtm read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Dtm(in.readToken(), DATE_TIME, null);
    }
  },

  /** DateTimeStamp type. */
  DATE_TIME_STAMP("dateTimeStamp", DATE_TIME, Type.ID.DTS, false, false) {
    @Override
    public Dtm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE, DATE_TIME)) return new Dtm((ADate) item, DATE_TIME_STAMP, info);
      if(isString(item)) return new Dtm(item.string(info), DATE_TIME_STAMP, info);
      throw typeError(item, this, info);
    }
    @Override
    public Dtm cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Dtm read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
      return new Dtm(in.readToken(), DATE_TIME_STAMP, null);
    }
  },

  /** Date type. */
  DATE("date", ANY_ATOMIC_TYPE, Type.ID.DAT, false, false) {
    @Override
    public Dat cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.instanceOf(DATE_TIME)) return new Dat((ADate) item);
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
  TIME("time", ANY_ATOMIC_TYPE, Type.ID.TIM, false, false) {
    @Override
    public Tim cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.instanceOf(DATE_TIME)) return new Tim((ADate) item);
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
  G_YEAR_MONTH("gYearMonth", ANY_ATOMIC_TYPE, Type.ID.YMO, false, false) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME_STAMP, DATE_TIME, DATE)) return new GDt((ADate) item, this);
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
  G_YEAR("gYear", ANY_ATOMIC_TYPE, Type.ID.YEA, false, false) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME_STAMP, DATE_TIME, DATE)) return new GDt((ADate) item, this);
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
  G_MONTH_DAY("gMonthDay", ANY_ATOMIC_TYPE, Type.ID.MDA, false, false) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME_STAMP, DATE_TIME, DATE)) return new GDt((ADate) item, this);
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
  G_DAY("gDay", ANY_ATOMIC_TYPE, Type.ID.DAY, false, false) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME_STAMP, DATE_TIME, DATE)) return new GDt((ADate) item, this);
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
  G_MONTH("gMonth", ANY_ATOMIC_TYPE, Type.ID.MON, false, false) {
    @Override
    public GDt cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item.type.oneOf(DATE_TIME_STAMP, DATE_TIME, DATE)) return new GDt((ADate) item, this);
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
  BOOLEAN("boolean", ANY_ATOMIC_TYPE, Type.ID.BLN, false, false) {
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
      return value instanceof final Boolean bln ? Bln.get(bln) :
        cast(Str.get(value, qc, info), qc, info);
    }
    @Override
    public Bln read(final DataInput in, final QueryContext qc) throws IOException {
      return Bln.get(in.readBool());
    }
  },

  /** Implementation specific: binary type. */
  BINARY("binary", ANY_ATOMIC_TYPE, Type.ID.BIN, false, false),

  /** Base64 binary type. */
  BASE64_BINARY("base64Binary", BINARY, Type.ID.B64, false, false) {
    @Override
    public B64 cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof final Bin bin) return B64.get(bin, info);
      if(isString(item)) return B64.get(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public B64 cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return value instanceof final byte[] bytes ? B64.get(bytes) : B64.get(token(value), info);
    }
    @Override
    public B64 read(final DataInput in, final QueryContext qc) throws IOException {
      return B64.get(in.readToken());
    }
  },

  /** Hex binary type. */
  HEX_BINARY("hexBinary", BINARY, Type.ID.HEX, false, false) {
    @Override
    public Hex cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(item instanceof final Bin bin) return new Hex(bin, info);
      if(isString(item)) return new Hex(item.string(info), info);
      throw typeError(item, this, info);
    }
    @Override
    public Hex cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      return new Hex(value instanceof final byte[] bytes ? bytes : token(value), info);
    }
    @Override
    public Hex read(final DataInput in, final QueryContext qc) throws IOException {
      return new Hex(in.readToken());
    }
  },

  /** Any URI type. */
  ANY_URI("anyURI", ANY_ATOMIC_TYPE, Type.ID.URI, false, true) {
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
  QNAME("QName", ANY_ATOMIC_TYPE, Type.ID.QNM, false, false) {
    @Override
    public QNm cast(final Item item, final QueryContext qc, final InputInfo info)
        throws QueryException {
      final Type type = item.type;
      if(type != STRING && !type.isUntyped()) throw typeError(item, this, info);
      final byte[] name = trim(item.string(info));
      if(XMLToken.isQName(name)) {
        final QNm qnm = qc.shared.qName(name, qc.ns.resolve(prefix(name), info.sc()));
        if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(info, qnm.prefix());
        return qnm;
      }
      throw castError(item, info);
    }
    @Override
    public QNm cast(final Object value, final QueryContext qc, final InputInfo info) {
      return value instanceof final QName name ? new QNm(name) :
        new QNm(value.toString().replaceAll("^#", ""));
    }
    @Override
    public QNm read(final DataInput in, final QueryContext qc) throws IOException {
      return new QNm(in.readToken(), in.readBool() ? in.readToken() : null);
    }
  },

  /** NOTATION Type. */
  NOTATION("NOTATION", ANY_ATOMIC_TYPE, Type.ID.NOT, false, false);

  /** Language pattern. */
  private static final Pattern LANGPATTERN = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z\\d]{1,8})*");
  /** Cached types. */
  private static final TokenObjectMap<BasicType> TYPES = new TokenObjectMap<>();

  /** Name. */
  private final byte[] name;
  /** Parent type (can be {@code null}). */
  private final BasicType parent;

  /** Type ID. */
  private final ID id;
  /** Number flag. */
  private final boolean numeric;
  /** String flag. */
  private final boolean string;

  /** Pre/post values (pre, post << 8). */
  private short prePost;
  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;
  /** QName (lazy instantiation). */
  private QNm qnm;

  /**
   * Constructor.
   * @param name string representation
   * @param parent parent type (can be {@code null})
   * @param id type ID
   * @param numeric numeric flag
   * @param string string flag
   */
  BasicType(final String name, final BasicType parent, final ID id, final boolean numeric,
      final boolean string) {
    this.name = token(name);
    this.parent = parent;
    this.numeric = numeric;
    this.string = string;
    this.id = id;
  }

  // map hierarchy to pre/post values
  static {
    final EnumMap<BasicType, List<BasicType>> types = new EnumMap<>(BasicType.class);
    for(final BasicType type : values()) {
      if(type.parent != null) types.computeIfAbsent(type.parent, k -> new ArrayList<>()).add(type);
      if(type != ITEM) TYPES.put(type.name, type);
    }
    ITEM.assign(types, new byte[2]);
  }

  /**
   * Finds and returns the specified type.
   * @param qname name of type
   * @param all accept all types (including those without parent type)
   * @return type or {@code null}
   */
  public static BasicType get(final QNm qname, final boolean all) {
    if(Token.eq(qname.uri(), XS_URI)) {
      final BasicType type = TYPES.get(qname.local());
      if(type != null && (all || type.parent != null)) return type;
    }
    return null;
  }

  /**
   * Assign pre/post values to types.
   * @param types child types
   * @param pp pre/post array
   */
  private void assign(final EnumMap<BasicType, List<BasicType>> types, final byte[] pp) {
    prePost = pp[0]++;
    for(final BasicType type : types.getOrDefault(this, List.of())) type.assign(types, pp);
    prePost |= pp[1]++ << 8;
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
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  /**
   * Returns the name of a type.
   * @return name
   */
  public final QNm qname() {
    if(qnm == null) qnm = new QNm(name, this == ITEM ? EMPTY : XS_URI);
    return qnm;
  }

  /**
   * Returns the parent type.
   * @return parent (can be {@code null})
   */
  public final BasicType parent() {
    return parent;
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    if(type == this || type == ITEM) return true;
    if(type instanceof final BasicType bt) {
      return (prePost & 0xFF) >= (bt.prePost & 0xFF) && (prePost & 0xFF00) <= (bt.prePost & 0xFF00);
    }
    return type instanceof final ChoiceItemType cit && cit.hasInstance(this);
  }

  @Override
  public final Type union(final Type type) {
    if(this == ERROR) return type;
    if(type == ERROR) return this;
    if(type instanceof ChoiceItemType || type instanceof EnumType) return type.union(this);
    if(type.instanceOf(this)) return this;
    if(instanceOf(type)) return type;
    if(type instanceof final BasicType bt) {
      final List<BasicType> ancestors = new ArrayList<>(8);
      for(BasicType p = bt; p != null; p = p.parent) ancestors.add(p);
      for(BasicType p = this; p != null; p = p.parent) {
        if(ancestors.contains(p)) return p;
      }
    }
    return ITEM;
  }

  @Override
  public final Type intersect(final Type type) {
    if(this == ERROR) return type;
    if(type == ERROR) return this;
    return type instanceof ChoiceItemType ? type.intersect(this) :
      instanceOf(type) ? this : type.instanceOf(this) ? type : null;
  }

  @Override
  public final boolean isNumber() {
    return numeric;
  }

  @Override
  public final boolean isUntyped() {
    return this == UNTYPED_ATOMIC;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return numeric || isUntyped();
  }

  @Override
  public final boolean isStringOrUntyped() {
    return string || isUntyped();
  }

  @Override
  public final boolean isSortable() {
    return instanceOf(ANY_ATOMIC_TYPE);
  }

  @Override
  public final BasicType atomic() {
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
    if(item.type.oneOf(DOUBLE, FLOAT)) {
      final double d = item.dbl(info);
      if(!Double.isFinite(d)) throw valueError(this, item.string(info), info);
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
    if(value instanceof final Value val) {
      if(val.size() != 1) throw typeError(val, this, info);
      item = (Item) val;
    } else if(value instanceof Double || value instanceof Float) {
      item = Dbl.get(((Number) value).doubleValue());
    } else if(value instanceof Number) {
      item = Itr.get(((Number) value).longValue());
    } else if(value instanceof Character) {
      item = Itr.get((char) value);
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
    return eq(QNAME) || eq(NOTATION);
  }

  @Override
  public final boolean refinable() {
    return !((Checks<BasicType>) at -> at.eq(this) || !at.instanceOf(this)).all(values());
  }

  /**
   * Returns an info message for a similar function.
   * @param qname name of type
   * @return info string
   */
  public static String similar(final QNm qname) {
    final byte[] ln = lc(qname.local());

    final Function<BasicType, byte[]> local = tp -> tp.parent != null ? tp.qname().local() : null;
    Object similar = Levenshtein.similar(ln, values(), value -> local.apply((BasicType) value));
    if(similar == null) {
      for(final BasicType value : values()) {
        final byte[] lc = local.apply(value);
        if(lc != null && startsWith(lc, ln)) {
          similar = value;
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

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(this == ITEM) {
      tb.add(name).add("()");
    } else {
      tb.add(XS_PREFIX).add(':').add(name);
    }
    return tb.toString();
  }
}