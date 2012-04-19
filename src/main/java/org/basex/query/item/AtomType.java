package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * XQuery data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum AtomType implements Type {
  /** Item type. */
  ITEM("item", null, EMPTY, false, false, false, false, false, 32),

  /** Any simple type. */
  UTY("untyped", null, XSURI, false, false, false, false, false, 33),

  /** Any simple type. */
  ATY("anyType", null, XSURI, false, false, false, false, false, 34),

  /** Any simple type. */
  AST("anySimpleType", null, XSURI, false, false, false, false, false, 35),

  /** Any atomic type. */
  AAT("anyAtomicType", ITEM, XSURI, false, false, false, false, false, 36) {
    @Override
    public Atm cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Atm(it.string(ii));
    }
    @Override
    public Atm cast(final Object o, final InputInfo ii) {
      return new Atm(o.toString());
    }
  },

  /** Untyped Atomic type. */
  ATM("untypedAtomic", AAT, XSURI, false, true, true, false, false, 37) {
    @Override
    public Atm cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Atm(it.string(ii));
    }
    @Override
    public Atm cast(final Object o, final InputInfo ii) {
      return new Atm(o.toString());
    }
  },

  /** String type. */
  STR("string", AAT, XSURI, false, false, true, false, false, 38) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return Str.get(it.string(ii));
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) {
      return Str.get(o);
    }
  },

  /** Normalized String type. */
  NST("normalizedString", STR, XSURI, false, false, true, false, false, 39) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(it.string(ii), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Token type. */
  TOK("token", NST, XSURI, false, false, true, false, false, 40) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(norm(it.string(ii)), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Language type. */
  LAN("language", TOK, XSURI, false, false, true, false, false, 41) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!LANGPATTERN.matcher(Token.string(v)).matches()) error(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** NMTOKEN type. */
  NMT("NMTOKEN", TOK, XSURI, false, false, true, false, false, 42) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!XMLToken.isNMToken(v)) error(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Name type. */
  NAM("Name", TOK, XSURI, false, false, true, false, false, 43) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!XMLToken.isName(v)) error(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** NCName type. */
  NCN("NCName", NAM, XSURI, false, false, true, false, false, 44) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(checkName(it, ii), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** ID type. */
  ID("ID", NCN, XSURI, false, false, true, false, false, 45) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(checkName(it, ii), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** IDREF type. */
  IDR("IDREF", NCN, XSURI, false, false, true, false, false, 46) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(checkName(it, ii), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Entity type. */
  ENT("ENTITY", NCN, XSURI, false, false, true, false, false, 47) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return new Str(checkName(it, ii), this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Float type. */
  FLT("float", AAT, XSURI, true, false, false, false, false, 48) {
    @Override
    public Flt cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return Flt.get(checkNum(it, ii).flt(ii));
    }
    @Override
    public Flt cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Double type. */
  DBL("double", AAT, XSURI, true, false, false, false, false, 49) {
    @Override
    public Dbl cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return Dbl.get(checkNum(it, ii).dbl(ii));
    }
    @Override
    public Dbl cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Decimal type. */
  DEC("decimal", AAT, XSURI, true, false, false, false, false, 50) {
    @Override
    public Dec cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return Dec.get(checkNum(it, ii).dec(ii));
    }
    @Override
    public Dec cast(final Object o, final InputInfo ii) {
      return Dec.get(new BigDecimal(o.toString()));
    }
  },

  /** Precision decimal type. */
  PDC("precisionDecimal", null, EMPTY, false, false, false, false, false, 51),

  /** Integer type. */
  ITR("integer", DEC, XSURI, true, false, false, false, false, 52) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return Int.get(checkLong(o, 0, 0, ii));
    }
  },

  /** Non-positive integer type. */
  NPI("nonPositiveInteger", ITR, XSURI, true, false, false, false, false, 53) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, Long.MIN_VALUE, 0, ii), this);
    }
  },

  /** Negative integer type. */
  NIN("negativeInteger", NPI, XSURI, true, false, false, false, false, 54) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, Long.MIN_VALUE, -1, ii), this);
    }
  },

  /** Long type. */
  LNG("long", ITR, XSURI, true, false, false, false, false, 55) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type.isDate() ? new Int((Date) it) : cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, 0, ii), this);
    }
  },

  /** Int type. */
  INT("int", LNG, XSURI, true, false, false, false, false, 56) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, -0x80000000, 0x7FFFFFFF, ii), this);
    }
  },

  /** Short type. */
  SHR("short", INT, XSURI, true, false, false, false, false, 57) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, -0x8000, 0x7FFF, ii), this);
    }
  },

  /** Byte type. */
  BYT("byte", SHR, XSURI, true, false, false, false, false, 58) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, -0x80, 0x7F, ii), this);
    }
  },

  /** Non-negative integer type. */
  NNI("nonNegativeInteger", ITR, XSURI, true, false, false, false, false, 59) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, Long.MAX_VALUE, ii), this);
    }
  },

  /** Unsigned long type. */
  ULN("unsignedLong", NNI, XSURI, true, false, false, false, false, 60) {
    /** Maximum value. */
    final BigDecimal max = new BigDecimal(Long.MAX_VALUE).multiply(
        BigDecimal.valueOf(2)).add(BigDecimal.ONE);

    @Override
    public Dec cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final BigDecimal v = checkNum(it, ii).dec(ii);
      if(v.signum() < 0 || v.compareTo(max) > 0 || it.type.isString() &&
          contains(it.string(ii), '.')) FUNCAST.thrw(ii, this, it);
      return new Dec(v, this);
    }
    @Override
    public Dec cast(final Object o, final InputInfo ii) {
      return new Dec(token(o.toString()));
    }
  },

  /** Short type. */
  UIN("unsignedInt", ULN, XSURI, true, false, false, false, false, 61) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, 0xFFFFFFFFL, ii), this);
    }
  },

  /** Unsigned Short type. */
  USH("unsignedShort", UIN, XSURI, true, false, false, false, false, 62) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, 0xFFFF, ii), this);
    }
  },

  /** Unsigned byte type. */
  UBY("unsignedByte", USH, XSURI, true, false, false, false, false, 63) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, 0xFF, ii), this);
    }
  },

  /** Positive integer type. */
  PIN("positiveInteger", NNI, XSURI, true, false, false, false, false, 64) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 1, Long.MAX_VALUE, ii), this);
    }
  },

  /** Duration type. */
  DUR("duration", AAT, XSURI, false, false, false, true, false, 65) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type.isDuration() ? new Dur((Dur) it) : str(it) ?
          new Dur(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year month duration type. */
  YMD("yearMonthDuration", DUR, XSURI, false, false, false, true, false, 66) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type.isDuration() ? new YMd((Dur) it) : str(it) ?
          new YMd(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Day time duration type. */
  DTD("dayTimeDuration", DUR, XSURI, false, false, false, true, false, 67) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type.isDuration() ? new DTd((Dur) it) : str(it) ?
          new DTd(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** DateTime type. */
  DTM("dateTime", AAT, XSURI, false, false, false, false, true, 68) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == LNG ? new Dtm((Int) it, ii) : it.type == DAT ?
          new Dtm((Date) it) : str(it) ? new Dtm(it.string(ii), ii) :
            error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** DateTimeStamp type. */
  DTS("dateTimeStamp", null, EMPTY, false, false, false, false, true, 69),

  /** Date type. */
  DAT("date", AAT, XSURI, false, false, false, false, true, 70) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM ? new Dat((Date) it) : str(it) ?
          new Dat(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Time type. */
  TIM("time", AAT, XSURI, false, false, false, false, true, 71) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM ? new Tim((Date) it) : str(it) ?
          new Tim(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year month type. */
  YMO("gYearMonth", AAT, XSURI, false, false, false, false, true, 72) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new DSim((Date) it, this) : str(it) ?
          new DSim(it.string(ii), this, ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year type. */
  YEA("gYear", AAT, XSURI, false, false, false, false, true, 73) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new DSim((Date) it, this) : str(it) ?
          new DSim(it.string(ii), this, ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Month day type. */
  MDA("gMonthDay", AAT, XSURI, false, false, false, false, true, 74) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new DSim((Date) it, this) : str(it) ?
          new DSim(it.string(ii), this, ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Day type. */
  DAY("gDay", AAT, XSURI, false, false, false, false, true, 75) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new DSim((Date) it, this) : str(it) ?
          new DSim(it.string(ii), this, ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Month type. */
  MON("gMonth", AAT, XSURI, false, false, false, false, true, 76) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new DSim((Date) it, this) : str(it) ?
          new DSim(it.string(ii), this, ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Boolean type. */
  BLN("boolean", AAT, XSURI, false, false, false, false, false, 77) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type.isNumber() ? Bln.get(it.bool(ii)) : str(it) ?
          Bln.get(Bln.parse(it.string(ii), ii)) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return Bln.get((Boolean) o);
    }
  },

  /** Implementation specific: binary type. */
  BIN("binary", AAT, BASEXURI, false, false, false, false, false, 78),

  /** Base64 binary type. */
  B64("base64Binary", BIN, XSURI, false, false, false, false, false, 79) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Bin ? new B64((Bin) it, ii) : str(it) ?
          new B64(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return new B64((byte[]) o, ii);
    }
  },

  /** Hex binary type. */
  HEX("hexBinary", BIN, XSURI, false, false, false, false, false, 80) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Bin ? new Hex((Bin) it, ii) : str(it) ?
          new Hex(it.string(ii), ii) : error(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return new Hex((byte[]) o, ii);
    }
  },

  /** Any URI type. */
  URI("anyURI", AAT, XSURI, false, false, true, false, false, 81) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {

      if(!it.type.isString()) error(it, ii);
      final Uri u = Uri.uri(it.string(ii));
      if(!u.isValid()) FUNCAST.thrw(ii, this, it);
      return u;
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return Uri.uri(token(o.toString()));
    }
  },

  /** QName Type. */
  QNM("QName", AAT, XSURI, false, false, false, false, false, 82) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {

      // argument must be of type string and a valid QName
      if(it.type != STR) error(it, ii);
      final byte[] nm = it.string(ii);
      if(nm.length == 0) FUNCAST.thrw(ii, this, it);
      final QNm qn = new QNm(nm, ctx);
      if(!qn.hasURI() && qn.hasPrefix()) NSDECL.thrw(ii, qn.prefix());
      return qn;
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return new QNm((QName) o);
    }
  },

  /** NOTATION Type. */
  NOT("NOTATION", AAT, XSURI, false, false, false, false, false, 83),

  /** Empty sequence type. */
  EMP("empty-sequence", null, EMPTY, false, false, false, false, false, 84),

  /** Sequence type. */
  SEQ("sequence", null, EMPTY, false, false, false, false, false, 85),

  /** Java type. */
  JAVA("java", null, EMPTY, true, true, true, false, false, 86) {
    @Override
    public Item cast(final Item it, final QueryContext ctx,
        final InputInfo ii) {
      return new Jav(it);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return new Jav(o);
    }
  };

  /** Language pattern. */
  static final Pattern LANGPATTERN = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z0-9]{1,8})*");

  /** Name. */
  public final QNm name;
  /** Parent type. */
  public final Type par;
  /** Type id . */
  private final int id;

  /** Number flag. */
  private final boolean num;
  /** Untyped flag. */
  private final boolean unt;
  /** String flag. */
  private final boolean str;
  /** Duration flag. */
  private final boolean dur;
  /** Date flag. */
  private final boolean dat;

  /** Sequence type (lazy). */
  private SeqType seq;

  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   * @param ur uri
   * @param n number flag
   * @param u untyped flag
   * @param s string flag
   * @param d duration flag
   * @param t date flag
   * @param i type id
   */
  AtomType(final String nm, final Type pr, final byte[] ur, final boolean n,
      final boolean u, final boolean s, final boolean d, final boolean t, final int i) {
    name = new QNm(nm, ur);
    par = pr;
    num = n;
    unt = u;
    str = s;
    dur = d;
    dat = t;
    id = i;
  }

  @Override
  public boolean isNumber() {
    return num;
  }

  @Override
  public boolean isString() {
    return str;
  }

  @Override
  public boolean isUntyped() {
    return unt;
  }

  @Override
  public boolean isDuration() {
    return dur;
  }

  @Override
  public boolean isDate() {
    return dat;
  }

  @Override
  public boolean isFunction() {
    return false;
  }

  @Override
  public boolean isMap() {
    return false;
  }

  @Override
  public byte[] string() {
    return name.string();
  }

  @Override
  public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return it.type != this ? error(it, ii) : it;
  }

  @Override
  public Item cast(final Object o, final InputInfo ii) throws QueryException {
    Util.notexpected(o);
    return null;
  }

  @Override
  public SeqType seqType() {
    // cannot be statically instantiated due to circular dependencies
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t || par != null && par.instanceOf(t);
  }

  @Override
  public final boolean isNode() {
    return false;
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public String toString() {
    final boolean xs = eq(XSURI, name.uri());
    final TokenBuilder tb = new TokenBuilder();
    if(xs) tb.add(NSGlobal.prefix(name.uri())).add(':');
    tb.add(name.string());
    if(!xs) tb.add("()");
    return tb.toString();
  }

  /**
   * Throws an exception if the specified item can't be converted to a number.
   * @param it item
   * @param ii input info
   * @return item argument
   * @throws QueryException query exception
   */
  Item checkNum(final Item it, final InputInfo ii) throws QueryException {
    final Type ip = it.type;
    return ip == URI || !ip.isString() && !ip.isNumber() &&
        !ip.isUntyped() && ip != BLN ? error(it, ii) : it;
  }

  /**
   * Checks the validity of the specified object and returns its long value.
   * @param o value to be checked
   * @param min minimum value
   * @param max maximum value
   * @param ii input info
   * @return integer value
   * @throws QueryException query exception
   */
  long checkLong(final Object o, final long min, final long max, final InputInfo ii)
      throws QueryException {

    final Item it = o instanceof Item ? (Item) o : Str.get(o.toString());
    checkNum(it, ii);

    final Type ip = it.type;
    if(ip == DBL || ip == FLT) {
      final double d = it.dbl(ii);
      if(Double.isNaN(d) || d == 1 / 0d || d == -1 / 0d) value(ii, this, it);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) INTRANGE.thrw(ii, d);
      if(min != max && (d < min || d > max)) FUNCAST.thrw(ii, this, it);
      return (long) d;
    }
    final long l = it.itr(ii);
    if(min == max) {
      final double d = it.dbl(ii);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) FUNCAST.thrw(ii, this, it);
    }
    if(min != max && (l < min || l > max)) FUNCAST.thrw(ii, this, it);
    return l;
  }

  /**
   * Checks if the specified item is a string.
   * @param it item
   * @return item argument
   */
  static boolean str(final Item it) {
    final Type ip = it.type;
    return (ip.isString() || ip.isUntyped()) && ip != URI;
  }

  /**
   * Checks the validity of the specified name.
   * @param it value to be checked
   * @param ii input info
   * @throws QueryException query exception
   * @return name
   */
  byte[] checkName(final Item it, final InputInfo ii) throws QueryException {
    final byte[] v = norm(it.string(ii));
    if(!XMLToken.isNCName(v)) error(it, ii);
    return v;
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  Item error(final Item it, final InputInfo ii) throws QueryException {
    throw Err.cast(ii, this, it);
  }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @param all accept all types (include those without parent type)
   * @return type or {@code null}
   */
  public static AtomType find(final QNm type, final boolean all) {
    for(final AtomType t : values()) {
      if(t.name.eq(type) && (all || t.par != null)) return t;
    }
    return null;
  }
}
