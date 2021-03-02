package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryError.normalize;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import java.math.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * XQuery atomic types.
 *
 * @author BaseX Team 2005-21, BSD License
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
    public Atm cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Atm(item.string(ii));
    }
    @Override
    public Atm cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return new Atm(value.toString());
    }
  },

  /** Untyped Atomic type. */
  UNTYPED_ATOMIC("untypedAtomic", ANY_ATOMIC_TYPE, XS_URI, false, true, false, true, Type.ID.ATM) {
    @Override
    public Atm cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Atm(item.string(ii));
    }
    @Override
    public Atm cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return new Atm(value.toString());
    }
  },

  /** String type. */
  STRING("string", ANY_ATOMIC_TYPE, XS_URI, false, false, true, true, Type.ID.STR) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Str.get(item.string(ii));
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Str.get(value, qc, ii);
    }
  },

  /** Normalized String type. */
  NORMALIZED_STRING("normalizedString", STRING, XS_URI, false, false, true, true, Type.ID.NST) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {

      final byte[] str = item.string(ii);
      final int sl = str.length;
      for(int s = 0; s < sl; s++) {
        final byte b = str[s];
        if(b == '\t' || b == '\r' || b == '\n') str[s] = ' ';
      }
      return new Str(str, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Token type. */
  TOKEN("token", NORMALIZED_STRING, XS_URI, false, false, true, true, Type.ID.TOK) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Str(normalize(item.string(ii)), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Language type. */
  LANGUAGE("language", TOKEN, XS_URI, false, false, true, true, Type.ID.LAN) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      final byte[] v = normalize(item.string(ii));
      if(!LANGPATTERN.matcher(Token.string(v)).matches()) throw castError(item, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** NMTOKEN type. */
  NMTOKEN("NMTOKEN", TOKEN, XS_URI, false, false, true, true, Type.ID.NMT) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      final byte[] v = normalize(item.string(ii));
      if(!XMLToken.isNMToken(v)) throw castError(item, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Name type. */
  NAME("Name", TOKEN, XS_URI, false, false, true, true, Type.ID.NAM) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      final byte[] v = normalize(item.string(ii));
      if(!XMLToken.isName(v)) throw castError(item, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** NCName type. */
  NCNAME("NCName", NAME, XS_URI, false, false, true, true, Type.ID.NCN) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Str(checkName(item, ii), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** ID type. */
  ID("ID", NCNAME, XS_URI, false, false, true, true, Type.ID.ID) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Str(checkName(item, ii), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** IDREF type. */
  IDREF("IDREF", NCNAME, XS_URI, false, false, true, true, Type.ID.IDR) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Str(checkName(item, ii), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Entity type. */
  ENTITY("ENTITY", NCNAME, XS_URI, false, false, true, true, Type.ID.ENT) {
    @Override
    public Str cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Str(checkName(item, ii), this);
    }
    @Override
    public Str cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Numeric type. */
  NUMERIC("numeric", ANY_ATOMIC_TYPE, XS_URI, true, false, false, true, Type.ID.NUM) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      // return double
      return item.type.isNumber() ? item : Dbl.get(checkNum(item, ii).dbl(ii));
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      // return double
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Float type. */
  FLOAT("float", NUMERIC, XS_URI, true, false, false, true, Type.ID.FLT) {
    @Override
    public Flt cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Flt.get(checkNum(item, ii).flt(ii));
    }
    @Override
    public Flt cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Double type. */
  DOUBLE("double", NUMERIC, XS_URI, true, false, false, true, Type.ID.DBL) {
    @Override
    public Dbl cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Dbl.get(checkNum(item, ii).dbl(ii));
    }
    @Override
    public Dbl cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Decimal type. */
  DECIMAL("decimal", NUMERIC, XS_URI, true, false, false, true, Type.ID.DEC) {
    @Override
    public Dec cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Dec.get(checkNum(item, ii).dec(ii));
    }
    @Override
    public Dec cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return Dec.get(new BigDecimal(value.toString()));
    }
  },

  /** Precision decimal type. */
  PRECISION_DECIMAL("precisionDecimal", null, XS_URI, true, false, false, true, Type.ID.PDC),

  /** Integer type. */
  INTEGER("integer", DECIMAL, XS_URI, true, false, false, true, Type.ID.ITR) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return Int.get(checkLong(value, 0, 0, ii));
    }
  },

  /** Non-positive integer type. */
  NON_POSITIVE_INTEGER("nonPositiveInteger", INTEGER, XS_URI, true, false, false, true,
      Type.ID.NPI) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, Long.MIN_VALUE, 0, ii), this);
    }
  },

  /** Negative integer type. */
  NEGATIVE_INTEGER("negativeInteger", NON_POSITIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.NIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, Long.MIN_VALUE, -1, ii), this);
    }
  },

  /** Long type. */
  LONG("long", INTEGER, XS_URI, true, false, false, true, Type.ID.LNG) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 0, 0, ii), this);
    }
  },

  /** Int type. */
  INT("int", LONG, XS_URI, true, false, false, true, Type.ID.INT) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, -0x80000000, 0x7FFFFFFF, ii), this);
    }
  },

  /** Short type. */
  SHORT("short", INT, XS_URI, true, false, false, true, Type.ID.SHR) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, -0x8000, 0x7FFF, ii), this);
    }
  },

  /** Byte type. */
  BYTE("byte", SHORT, XS_URI, true, false, false, true, Type.ID.BYT) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, -0x80, 0x7F, ii), this);
    }
  },

  /** Non-negative integer type. */
  NON_NEGATIVE_INTEGER("nonNegativeInteger", INTEGER, XS_URI, true, false, false, true,
      Type.ID.NNI) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 0, Long.MAX_VALUE, ii), this);
    }
  },

  /** Unsigned long type. */
  UNSIGNED_LONG("unsignedLong", NON_NEGATIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.ULN) {
    @Override
    public Uln cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Uln cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {

      final Item item = checkNum(value, ii);
      final BigDecimal v = item.dec(ii), i = v.setScale(0, RoundingMode.DOWN);
      // equals() used to also test fractional digits
      if(v.signum() < 0 || v.compareTo(Uln.MAXULN) > 0 ||
        item.type.isStringOrUntyped() && !v.equals(i)) throw castError(item, ii);
      return Uln.get(i.toBigInteger());
    }
  },

  /** Short type. */
  UNSIGNED_INT("unsignedInt", UNSIGNED_LONG, XS_URI, true, false, false, true, Type.ID.UIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 0, 0xFFFFFFFFL, ii), this);
    }
  },

  /** Unsigned Short type. */
  UNSIGNED_SHORT("unsignedShort", UNSIGNED_INT, XS_URI, true, false, false, true, Type.ID.USH) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 0, 0xFFFF, ii), this);
    }
  },

  /** Unsigned byte type. */
  UNSIGNED_BYTE("unsignedByte", UNSIGNED_SHORT, XS_URI, true, false, false, true, Type.ID.UBY) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 0, 0xFF, ii), this);
    }
  },

  /** Positive integer type. */
  POSITIVE_INTEGER("positiveInteger", NON_NEGATIVE_INTEGER, XS_URI, true, false, false, true,
      Type.ID.PIN) {
    @Override
    public Int cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast((Object) item, qc, sc, ii);
    }
    @Override
    public Int cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Int(checkLong(value, 1, Long.MAX_VALUE, ii), this);
    }
  },

  /** Duration type. */
  DURATION("duration", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.DUR) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof Dur) return new Dur((Dur) item);
      if(isString(item)) return new Dur(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Year month duration type. */
  YEAR_MONTH_DURATION("yearMonthDuration", DURATION, XS_URI, false, false, false, true,
      Type.ID.YMD) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof Dur) return new YMDur((Dur) item);
      if(isString(item)) return new YMDur(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Day time duration type. */
  DAY_TIME_DURATION("dayTimeDuration", DURATION, XS_URI, false, false, false, true, Type.ID.DTD) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof Dur) return new DTDur((Dur) item);
      if(isString(item)) return new DTDur(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** DateTime type. */
  DATE_TIME("dateTime", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.DTM) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE) return new Dtm((ADate) item);
      if(isString(item)) return new Dtm(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** DateTimeStamp type. */
  DATE_TIME_STAMP("dateTimeStamp", null, XS_URI, false, false, false, true, Type.ID.DTS),

  /** Date type. */
  DATE("date", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.DAT) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME) return new Dat((ADate) item);
      if(isString(item)) return new Dat(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Time type. */
  TIME("time", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.TIM) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME) return new Tim((ADate) item);
      if(isString(item)) return new Tim(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Year month type. */
  G_YEAR_MONTH("gYearMonth", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.YMO) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME || item.type == DATE) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(ii), this, ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Year type. */
  G_YEAR("gYear", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.YEA) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME || item.type == DATE) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(ii), this, ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Month day type. */
  G_MONTH_DAY("gMonthDay", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.MDA) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME || item.type == DATE) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(ii), this, ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Day type. */
  G_DAY("gDay", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.DAY) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME || item.type == DATE) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(ii), this, ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Month type. */
  G_MONTH("gMonth", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.MON) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item.type == DATE_TIME || item.type == DATE) return new GDt((ADate) item, this);
      if(isString(item)) return new GDt(item.string(ii), this, ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return cast(Str.get(value, qc, ii), qc, sc, ii);
    }
  },

  /** Boolean type. */
  BOOLEAN("boolean", ANY_ATOMIC_TYPE, XS_URI, false, false, false, true, Type.ID.BLN) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof ANum) return Bln.get(item.bool(ii));
      if(isString(item)) return Bln.get(Bln.parse(item, ii));
      throw typeError(item, this, ii);
    }
    @Override
    public Bln cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return Bln.get(value instanceof Boolean ? (Boolean) value :
        Boolean.parseBoolean(value.toString()));
    }
  },

  /** Implementation specific: binary type. */
  BINARY("binary", ANY_ATOMIC_TYPE, BASEX_URI, false, false, false, true, Type.ID.BIN),

  /** Base64 binary type. */
  BASE64_BINARY("base64Binary", BINARY, XS_URI, false, false, false, true, Type.ID.B64) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof Bin) return org.basex.query.value.item.B64.get((Bin) item, ii);
      if(isString(item)) return org.basex.query.value.item.B64.get(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return value instanceof byte[] ? org.basex.query.value.item.B64.get((byte[]) value) :
        org.basex.query.value.item.B64.get(token(value.toString()), ii);
    }
  },

  /** Hex binary type. */
  HEX_BINARY("hexBinary", BINARY, XS_URI, false, false, false, true, Type.ID.HEX) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(item instanceof Bin) return new Hex((Bin) item, ii);
      if(isString(item)) return new Hex(item.string(ii), ii);
      throw typeError(item, this, ii);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return new Hex(value instanceof byte[] ? (byte[]) value : token(value.toString()), ii);
    }
  },

  /** Any URI type. */
  ANY_URI("anyURI", ANY_ATOMIC_TYPE, XS_URI, false, false, true, true, Type.ID.URI) {
    @Override
    public Uri cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {

      if(!item.type.isStringOrUntyped()) throw typeError(item, this, ii);
      final Uri u = Uri.uri(item.string(ii));
      if(!u.isValid()) throw castError(item, ii);
      return u;
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return Uri.uri(value.toString());
    }
  },

  /** QName Type. */
  QNAME("QName", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.QNM) {
    @Override
    public QNm cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {

      if(item.type != STRING && !item.type.isUntyped()) throw typeError(item, this, ii);
      final byte[] nm = trim(item.string(ii));
      if(!XMLToken.isQName(nm)) throw castError(item, ii);
      final QNm qn = new QNm(nm, sc);
      if(!qn.hasURI() && qn.hasPrefix()) throw NSDECL_X.get(ii, qn.prefix());
      return qn;
    }
    @Override
    public QNm cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return value instanceof QName ? new QNm((QName) value) : new QNm(value.toString());
    }
  },

  /** NOTATION Type. */
  NOTATION("NOTATION", ANY_ATOMIC_TYPE, XS_URI, false, false, false, false, Type.ID.NOT),

  /** Java type. */
  JAVA("java", ITEM, BASEX_URI, true, true, true, false, Type.ID.JAVA) {
    @Override
    public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return new Jav(item, qc);
    }
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      return new Jav(value, qc);
    }
  };

  /** Language pattern. */
  private static final Pattern LANGPATTERN = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z0-9]{1,8})*");

  /** Cached enums (faster). */
  public static final AtomType[] VALUES = values();

  /** Name. */
  private final byte[] name;
  /** Parent type. */
  private final AtomType parent;
  /** URI. */
  private final byte[] uri;

  /** Type id . */
  private final Type.ID id;
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
   * @param id type id
   */
  AtomType(final String name, final AtomType parent, final byte[] uri, final boolean numeric,
      final boolean untyped, final boolean string, final boolean sortable, final Type.ID id) {
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
  public Item cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    if(item.type == this) return item;
    throw typeError(item, this, ii);
  }

  @Override
  public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    throw Util.notExpected(value);
  }

  @Override
  public final Item castString(final String value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(value, qc, sc, ii);
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
    return this == type || type == ITEM ||
        type instanceof AtomType && parent != null && parent.instanceOf(type);
  }

  @Override
  public final Type union(final Type type) {
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof AtomType) {
      final List<AtomType> arr = new ArrayList<>();
      for(AtomType at = (AtomType) type; (at = at.parent) != null;) arr.add(at);
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
  public final Type.ID id() {
    return id;
  }

  /**
   * Throws an exception if the specified item cannot be converted to a number.
   * @param item item
   * @param ii input info
   * @return item argument
   * @throws QueryException query exception
   */
  final Item checkNum(final Item item, final InputInfo ii) throws QueryException {
    final Type type = item.type;
    if(item instanceof ANum || type.isStringOrUntyped() && type != ANY_URI || type == BOOLEAN)
      return item;
    throw typeError(item, this, ii);
  }

  /**
   * Checks the validity of the specified object and returns its long value.
   * @param value value to be checked
   * @param min minimum value
   * @param max maximum value (no limit if identical to min)
   * @param ii input info
   * @return integer value
   * @throws QueryException query exception
   */
  final long checkLong(final Object value, final long min, final long max, final InputInfo ii)
      throws QueryException {

    final Item item = checkNum(value, ii);
    final Type type = item.type;
    if(type == DOUBLE || type == FLOAT) {
      final double d = item.dbl(ii);
      if(Double.isNaN(d) || Double.isInfinite(d)) throw valueError(this, item.string(ii), ii);
      if(min != max && (d < min || d > max)) throw castError(item, ii);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) throw INTRANGE_X.get(ii, d);
      return (long) d;
    }

    final long l = item.itr(ii);
    if(min != max && (l < min || l > max)) throw castError(item, ii);
    return l;
  }

  /**
   * Checks the validity of the specified object and returns it as item.
   * @param value value to be checked
   * @param ii input info
   * @return integer value
   * @throws QueryException query exception
   */
  final Item checkNum(final Object value, final InputInfo ii) throws QueryException {
    final Item item;
    if(value instanceof Value) {
      final Value val = (Value) value;
      if(val.size() != 1) throw typeError(val, this, ii);
      item = (Item) val;
    } else {
      item = Str.get(value.toString());
    }
    return checkNum(item, ii);
  }

  /**
   * Checks the validity of the specified name.
   * @param item value to be checked
   * @param ii input info
   * @return name
   * @throws QueryException query exception
   */
  final byte[] checkName(final Item item, final InputInfo ii) throws QueryException {
    final byte[] v = normalize(item.string(ii));
    if(!XMLToken.isNCName(v)) throw castError(item, ii);
    return v;
  }

  /**
   * Returns a cast exception.
   * @param item item to be converted
   * @param ii input info
   * @return query exception
   */
  public final QueryException castError(final Item item, final InputInfo ii)  {
    return FUNCCAST_X_X_X.get(ii, item.type, this, item);
  }

  /**
   * Returns a cast exception.
   * @param value value to be converted
   * @param ii input info
   * @return query exception
   */
  public final QueryException castError(final byte[] value, final InputInfo ii)  {
    return FUNCCAST_X_X.get(ii, this, normalize(value, ii));
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
   * Gets the type instance for the given ID.
   * @param id type ID
   * @return corresponding type if found, {@code null} otherwise
   */
  static Type getType(final Type.ID id) {
    for(final AtomType type : VALUES) {
      if(type.id == id) return type;
    }
    return null;
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
