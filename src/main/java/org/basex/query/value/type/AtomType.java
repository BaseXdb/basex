package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.regex.*;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum AtomType implements Type {
  /** Item type. */
  ITEM("item", null, EMPTY, false, false, false, 32),

  /** Any simple type. */
  UTY("untyped", null, XSURI, false, false, false, 33),

  /** Any simple type. */
  ATY("anyType", null, XSURI, false, false, false, 34),

  /** Any simple type. */
  AST("anySimpleType", null, XSURI, false, false, false, 35),

  /** Any atomic type. */
  AAT("anyAtomicType", ITEM, XSURI, false, false, false, 36) {
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
  ATM("untypedAtomic", AAT, XSURI, false, true, false, 37) {
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
  STR("string", AAT, XSURI, false, false, true, 38) {
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
  NST("normalizedString", STR, XSURI, false, false, true, 39) {
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
  TOK("token", NST, XSURI, false, false, true, 40) {
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
  LAN("language", TOK, XSURI, false, false, true, 41) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!LANGPATTERN.matcher(Token.string(v)).matches()) invValue(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** NMTOKEN type. */
  NMT("NMTOKEN", TOK, XSURI, false, false, true, 42) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!XMLToken.isNMToken(v)) invValue(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Name type. */
  NAM("Name", TOK, XSURI, false, false, true, 43) {
    @Override
    public Str cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      final byte[] v = norm(it.string(ii));
      if(!XMLToken.isName(v)) invValue(it, ii);
      return new Str(v, this);
    }
    @Override
    public Str cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** NCName type. */
  NCN("NCName", NAM, XSURI, false, false, true, 44) {
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
  ID("ID", NCN, XSURI, false, false, true, 45) {
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
  IDR("IDREF", NCN, XSURI, false, false, true, 46) {
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
  ENT("ENTITY", NCN, XSURI, false, false, true, 47) {
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
  FLT("float", AAT, XSURI, true, false, false, 48) {
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
  DBL("double", AAT, XSURI, true, false, false, 49) {
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
  DEC("decimal", AAT, XSURI, true, false, false, 50) {
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
  PDC("precisionDecimal", null, EMPTY, false, false, false, 51),

  /** Integer type. */
  ITR("integer", DEC, XSURI, true, false, false, 52) {
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
  NPI("nonPositiveInteger", ITR, XSURI, true, false, false, 53) {
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
  NIN("negativeInteger", NPI, XSURI, true, false, false, 54) {
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
  LNG("long", ITR, XSURI, true, false, false, 55) {
    @Override
    public Int cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Int cast(final Object o, final InputInfo ii) throws QueryException {
      return new Int(checkLong(o, 0, 0, ii), this);
    }
  },

  /** Int type. */
  INT("int", LNG, XSURI, true, false, false, 56) {
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
  SHR("short", INT, XSURI, true, false, false, 57) {
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
  BYT("byte", SHR, XSURI, true, false, false, 58) {
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
  NNI("nonNegativeInteger", ITR, XSURI, true, false, false, 59) {
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
  ULN("unsignedLong", NNI, XSURI, true, false, false, 60) {
    @Override
    public Dec cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return cast(it, ii);
    }
    @Override
    public Dec cast(final Object o, final InputInfo ii) throws QueryException {
      final Item it = o instanceof Item ? (Item) o : Str.get(o.toString());
      final BigDecimal v = checkNum(it, ii).dec(ii);
      final BigDecimal i = v.setScale(0, BigDecimal.ROUND_DOWN);
      if(v.signum() < 0 || v.compareTo(Dec.MAXULNG) > 0 ||
        it.type.isStringOrUntyped() && !v.equals(i)) FUNCAST.thrw(ii, this, it);
      return new Dec(i, this);
    }
  },

  /** Short type. */
  UIN("unsignedInt", ULN, XSURI, true, false, false, 61) {
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
  USH("unsignedShort", UIN, XSURI, true, false, false, 62) {
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
  UBY("unsignedByte", USH, XSURI, true, false, false, 63) {
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
  PIN("positiveInteger", NNI, XSURI, true, false, false, 64) {
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
  DUR("duration", AAT, XSURI, false, false, false, 65) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Dur ? new Dur((Dur) it) : str(it) ?
          new Dur(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year month duration type. */
  YMD("yearMonthDuration", DUR, XSURI, false, false, false, 66) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Dur ? new YMDur((Dur) it) : str(it) ?
          new YMDur(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Day time duration type. */
  DTD("dayTimeDuration", DUR, XSURI, false, false, false, 67) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Dur ? new DTDur((Dur) it) : str(it) ?
          new DTDur(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** DateTime type. */
  DTM("dateTime", AAT, XSURI, false, false, false, 68) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DAT ? new Dtm((Dat) it) : str(it) ?
        new Dtm(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** DateTimeStamp type. */
  DTS("dateTimeStamp", null, EMPTY, false, false, false, 69),

  /** Date type. */
  DAT("date", AAT, XSURI, false, false, false, 70) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM ? new Dat((Dtm) it) : str(it) ?
          new Dat(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Time type. */
  TIM("time", AAT, XSURI, false, false, false, 71) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM ? new Tim((Dtm) it) : str(it) ?
          new Tim(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year month type. */
  YMO("gYearMonth", AAT, XSURI, false, false, false, 72) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new GDt((ADate) it, this) : str(it) ?
          new GDt(it.string(ii), this, ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Year type. */
  YEA("gYear", AAT, XSURI, false, false, false, 73) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new GDt((ADate) it, this) : str(it) ?
          new GDt(it.string(ii), this, ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Month day type. */
  MDA("gMonthDay", AAT, XSURI, false, false, false, 74) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new GDt((ADate) it, this) : str(it) ?
          new GDt(it.string(ii), this, ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Day type. */
  DAY("gDay", AAT, XSURI, false, false, false, 75) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new GDt((ADate) it, this) : str(it) ?
          new GDt(it.string(ii), this, ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Month type. */
  MON("gMonth", AAT, XSURI, false, false, false, 76) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it.type == DTM || it.type == DAT ?
          new GDt((ADate) it, this) : str(it) ?
          new GDt(it.string(ii), this, ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return cast(Str.get(o), null, ii);
    }
  },

  /** Boolean type. */
  BLN("boolean", AAT, XSURI, false, false, false, 77) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof ANum ? Bln.get(it.bool(ii)) : str(it) ?
          Bln.get(Bln.parse(it.string(ii), ii)) : invCast(it, ii);
    }
    @Override
    public Bln cast(final Object o, final InputInfo ii) {
      return o instanceof Boolean ? Bln.get((Boolean) o) :
        Bln.get(Boolean.parseBoolean(o.toString()));
    }
  },

  /** Implementation specific: binary type. */
  BIN("binary", AAT, BASEXURI, false, false, false, 78),

  /** Base64 binary type. */
  B64("base64Binary", BIN, XSURI, false, false, false, 79) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Bin ? new B64((Bin) it, ii) : str(it) ?
          new B64(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return new B64(o instanceof byte[] ? (byte[]) o : Token.token(o.toString()), ii);
    }
  },

  /** Hex binary type. */
  HEX("hexBinary", BIN, XSURI, false, false, false, 80) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return it instanceof Bin ? new Hex((Bin) it, ii) : str(it) ?
          new Hex(it.string(ii), ii) : invCast(it, ii);
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) throws QueryException {
      return new Hex(o instanceof byte[] ? (byte[]) o : Token.token(o.toString()), ii);
    }
  },

  /** Any URI type. */
  URI("anyURI", AAT, XSURI, false, false, true, 81) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {

      if(!it.type.isStringOrUntyped()) invCast(it, ii);
      final Uri u = Uri.uri(it.string(ii));
      if(!u.isValid()) FUNCAST.thrw(ii, this, it);
      return u;
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return Uri.uri(o.toString());
    }
  },

  /** QName Type. */
  QNM("QName", AAT, XSURI, false, false, false, 82) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {

      // argument must be of type string and a valid QName
      if(it.type != STR) invCast(it, ii);
      final byte[] nm = it.string(ii);
      if(nm.length == 0 || !XMLToken.isQName(nm)) FUNCAST.thrw(ii, this, it);
      final QNm qn = new QNm(nm, ctx);
      if(!qn.hasURI() && qn.hasPrefix()) NSDECL.thrw(ii, qn.prefix());
      return qn;
    }
    @Override
    public Item cast(final Object o, final InputInfo ii) {
      return o instanceof QName ? new QNm((QName) o) : new QNm(o.toString());
    }
  },

  /** NOTATION Type. */
  NOT("NOTATION", AAT, XSURI, false, false, false, 83),

  /** Java type. */
  JAVA("java", null, EMPTY, true, true, true, 86) {
    @Override
    public Item cast(final Item it, final QueryContext ctx, final InputInfo ii) {
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
  private final byte id;

  /** Number flag. */
  private final boolean num;
  /** Untyped flag. */
  private final boolean unt;
  /** String flag. */
  private final boolean str;

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
   * @param i type id
   */
  AtomType(final String nm, final Type pr, final byte[] ur, final boolean n,
      final boolean u, final boolean s, final int i) {
    name = new QNm(nm, ur);
    par = pr;
    num = n;
    unt = u;
    str = s;
    id = (byte) i;
  }

  @Override
  public boolean isNumber() {
    return num;
  }

  @Override
  public boolean isUntyped() {
    return unt;
  }

  @Override
  public boolean isNumberOrUntyped() {
    return num || unt;
  }

  @Override
  public boolean isStringOrUntyped() {
    return str || unt;
  }

  @Override
  public byte[] string() {
    return name.string();
  }

  @Override
  public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return it.type != this ? invCast(it, ii) : it;
  }

  @Override
  public Item cast(final Object o, final InputInfo ii) throws QueryException {
    Util.notexpected(o);
    return null;
  }

  @Override
  public Item castString(final String o, final InputInfo ii) throws QueryException {
    return cast(o, ii);
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
   * Throws an exception if the specified item cannot be converted to a number.
   * @param it item
   * @param ii input info
   * @return item argument
   * @throws QueryException query exception
   */
  Item checkNum(final Item it, final InputInfo ii) throws QueryException {
    final Type ip = it.type;
    return it instanceof ANum || ip.isStringOrUntyped() && ip != URI || ip == BLN ?
      it : invCast(it, ii);
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
    return ip.isStringOrUntyped() && ip != URI;
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
    if(!XMLToken.isNCName(v)) invValue(it, ii);
    return v;
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  Item invCast(final Item it, final InputInfo ii) throws QueryException {
    throw Err.cast(ii, this, it);
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  Item invValue(final Item it, final InputInfo ii) throws QueryException {
    throw INVCAST.thrw(ii, it.type(), this, it);
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
