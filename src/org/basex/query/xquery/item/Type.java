package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.util.Err;
import org.basex.util.XMLToken;
import static org.basex.util.Token.*;

/**
 * XQuery Data Types.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Type {
  /** Untyped type. */
  ITEM("item", null, EMPTY, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Untyped type. */
  AAT("anyAtomicType", ITEM, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Untyped Atomic type. */
  ATM("untypedAtomic", AAT, XSURI, false, true, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return new Atm(it.str());
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** String type. */
  STR("string", AAT, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return Str.get(it.str());
    }
    
    @Override
    public Object j(final Item it) {
      return string(it.str());
    }
  },
  
  /** Normalized String type. */
  NST("normalizedString", STR, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return new Str(it.str(), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Token type. */
  TOK("token", NST, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return new Str(norm(it.str()), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Language type. */
  LAN("language", TOK, XSURI, false, false, true, false, false) {
    final Pattern pat = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z0-9]{1,8})*");

    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      final byte[] v = norm(it.str());
      if(!pat.matcher(string(v)).matches()) error(it);
      return new Str(v, this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** NMTOKEN type. */
  NMT("NMTOKEN", TOK, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      final byte[] v = norm(it.str());
      if(!XMLToken.isNMToken(v)) error(it);
      return new Str(v, this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Name type. */
  NAM("Name", TOK, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      final byte[] v = norm(it.str());
      if(!XMLToken.isName(v)) error(it);
      return new Str(v, this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Name type. */
  NCN("NCName", NAM, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Str(checkName(it), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** ID type. */
  ID("ID", NCN, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Str(checkName(it), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** IDREF type. */
  IDR("IDREF", NCN, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Str(checkName(it), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Name type. */
  ENT("ENTITY", NCN, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Str(checkName(it), this);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Float type. */
  FLT("float", AAT, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return Flt.get(checkNum(it).flt());
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.flt();
    }
  },
  
  /** Double type. */
  DBL("double", AAT, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return Dbl.get(checkNum(it).dbl());
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.dbl();
    }
  },
  
  /** Decimal type. */
  DEC("decimal", AAT, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return Dec.get(checkNum(it).dec());
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.dbl();
    }
  },
  
  /** Integer type. */
  ITR("integer", DEC, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return Itr.get(checkItr(it, Long.MIN_VALUE, Long.MAX_VALUE));
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Positive integer type. */
  NPI("nonPositiveInteger", ITR, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, Long.MIN_VALUE, 0), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Negative integer type. */
  NIN("negativeInteger", NPI, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, Long.MIN_VALUE, -1), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Long type. */
  LNG("long", ITR, XSURI, true, false, false, false, false) {
    final BigDecimal min = new BigDecimal(Long.MIN_VALUE);
    final BigDecimal max = new BigDecimal(Long.MAX_VALUE);

    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      final BigDecimal v = checkNum(it).dec();
      if(v.compareTo(min) < 0 || v.compareTo(max) > 0) 
        Err.or(FUNCAST, this, it);
      return new Dec(v, this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Int type. */
  INT("int", LNG, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, -0x80000000, 0x7FFFFFFF), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (int) it.itr();
    }
  },
  
  /** Short type. */
  SHR("short", INT, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, -0x8000, 0x7FFF), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (short) it.itr();
    }
  },
  
  /** Byte type. */
  BYT("byte", SHR, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, -0x80, 0x7F), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (byte) it.itr();
    }
  },
  
  /** Negative integer type. */
  NNI("nonNegativeInteger", ITR, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, 0, Long.MAX_VALUE), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Short type. */
  ULN("unsignedLong", NNI, XSURI, true, false, false, false, false) {
    /** Maximum value. */
    final BigDecimal max = new BigDecimal(Long.MAX_VALUE).multiply(
        BigDecimal.valueOf(2)).add(BigDecimal.ONE);

    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      final BigDecimal v = checkNum(it).dec();
      if(v.signum() < 0 || v.compareTo(max) > 0) Err.or(FUNCAST, this, it);
      return new Dec(v, this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Short type. */
  UIN("unsignedInt", ULN, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, 0, 0xFFFFFFFFL), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (int) it.itr();
    }
  },
  
  /** Short type. */
  USH("unsignedShort", UIN, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, 0, 0xFFFF), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (short) it.itr();
    }
  },
  
  /** Short type. */
  UBY("unsignedByte", USH, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, 0, 0xFF), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return (byte) it.itr();
    }
  },

  /** Positive integer type. */
  PIN("positiveInteger", NNI, XSURI, true, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return new Itr(checkItr(it, 1, Long.MAX_VALUE), this);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return it.itr();
    }
  },
  
  /** Date type. */
  DUR("duration", AAT, XSURI, false, false, false, true, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.d() ? new Dur((Dur) it) : checkStr(it) ?
          new Dur(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** Date type. */
  YMD("yearMonthDuration", DUR, XSURI, false, false, false, true, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.d() ? new YMd((Dur) it) : checkStr(it) ?
          new YMd(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** Date type. */
  DTD("dayTimeDuration", DUR, XSURI, false, false, false, true, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.d() ? new DTd((Dur) it) : checkStr(it) ?
          new DTd(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** DateTime type. */
  DTM("dateTime", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == LNG ? new Dtm((Dec) it) : it.type == DAT ?
          new Dtm((Date) it) : checkStr(it) ? new Dtm(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Date type. */
  DAT("date", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM ? new Dat((Date) it) : checkStr(it) ?
          new Dat(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Time type. */
  TIM("time", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM ? new Tim((Date) it) : checkStr(it) ?
          new Tim(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** YearMonth type. */
  YMO("gYearMonth", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM || it.type == DAT ? new YMo((Date) it) :
        checkStr(it) ? new YMo(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Year type. */
  YEA("gYear", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM || it.type == DAT ? new Yea((Date) it) :
        checkStr(it) ? new Yea(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** YearMonth type. */
  MDA("gMonthDay", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM || it.type == DAT ? new MDa((Date) it) :
        checkStr(it) ? new MDa(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Day type. */
  DAY("gDay", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM || it.type == DAT ? new Day((Date) it) :
        checkStr(it) ? new Day(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Month type. */
  MON("gMonth", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == DTM || it.type == DAT ? new Mon((Date) it) :
        checkStr(it) ? new Mon(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Boolean type. */
  BLN("boolean", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.n() ? Bln.get(it.bool()) : checkStr(it) ?
          Bln.get(Bln.check(it.str())) : error(it);
    }
    
    @Override
    public Object j(final Item it) throws XQException {
      return Bln.check(it.str());
    }
  },
  
  /** Base64Binary type. */
  B6B("base64Binary", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == HEX ? new B64((Hex) it) : checkStr(it) ?
          new B64(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** HexBinary type. */
  HEX("hexBinary", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.type == B6B ? new Hex((B64) it) : checkStr(it) ?
          new Hex(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** URI type. */
  URI("anyURI", AAT, XSURI, false, false, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      return it.s() ? Uri.uri(it.str()) : error(it);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** QName. */
  QNM("QName", AAT, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) throws XQException {
      if(it.type != STR) error(it);
      final byte[] s = trim(it.str());
      if(s.length == 0) Err.or(QNMINV, s);
      return new QNm(s, ctx);
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** NOTATION. */
  NOT("NOTATION", null, XSURI, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return null;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** Node type. */
  NOD("node", AAT, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** Text type. */
  TXT("text", NOD, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** PI type. */
  PI("processing-instruction", NOD, EMPTY, false, true, false, false,
      true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Element type. */
  ELM("element", NOD, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Document type. */
  DOC("document-node", NOD, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Attribute type. */
  ATT("attribute", NOD, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Comment type. */
  COM("comment", NOD, EMPTY, false, true, false, false, true) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return it;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },

  /** Sequence type. */
  SEQ("(Sequence)", null, EMPTY, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      BaseX.notexpected();
      return null;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Empty sequence type. */
  EMP("empty-sequence", null, EMPTY, false, false, false, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      return Seq.EMPTY;
    }
    
    @Override
    public Object j(final Item it) {
      return null;
    }
  },
  
  /** Empty sequence type. */
  JAVA("java", null, EMPTY, true, true, true, false, false) {
    @Override
    public Item e(final Item it, final XQContext ctx) {
      BaseX.notexpected();
      return null;
    }
    
    @Override
    public Object j(final Item it) {
      return ((Jav) it).val;
    }
  };
  
  /** String representation. */
  public final byte[] name;
  /** URI representation. */
  public final byte[] uri;
  /** Number flag. */
  public final boolean num;
  /** Parent type. */
  public final Type par;
  /** Untyped flag. */
  public final boolean unt;
  /** String flag. */
  public final boolean str;
  /** Duration flag. */
  public final boolean dur;
  /** Node flag. */
  public final boolean node;

  /**
   * Constructs a new item from the specified item.
   * @param it item to be converted
   * @param ctx xquery context
   * @return new item
   * @throws XQException evaluation exception
   */
  public abstract Item e(Item it, final XQContext ctx) throws XQException;

  /**
   * Converts the specified item to a Java object.
   * @param it item to be evaluated
   * @return new item
   * @throws XQException evaluation exception
   */
  public abstract Object j(final Item it) throws XQException;
  
  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   * @param ur uri
   * @param n number flag
   * @param u untyped flag
   * @param s string flag
   * @param d duration flag
   * @param nd node flag
   */
  private Type(final String nm, final Type pr, final byte[] ur, final boolean n,
      final boolean u, final boolean s, final boolean d, final boolean nd) {
    name = token(nm);
    uri = ur;
    par = pr;
    num = n;
    unt = u;
    str = s;
    dur = d;
    node = nd;
  }
  
  /**
   * Throws an exception if the specified item can't be converted to a number.
   * @param it item
   * @return item argument
   * @throws XQException evaluation exception
   */
  protected Item checkNum(final Item it) throws XQException {
    return it.type == URI || !it.s() && !it.n() && !it.u() &&
      it.type != BLN ? error(it) : it;
  }
  
  /**
   * Checks the validity of the specified item and returns its long value.
   * @param it value to be checked
   * @param min minimum value
   * @param max maximum value
   * @return integer value
   * @throws XQException possible converting exception
   */
  protected long checkItr(final Item it, final long min, final long max)
      throws XQException {

    checkNum(it);
    if(it.type == Type.DBL || it.type == Type.FLT) {
      final double d = it.dbl();
      if(d != d || d == 1 / 0d || d == -1 / 0d) Err.value(this, it);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) Err.or(INTRANGE, d);
      if(d < min || d > max) Err.or(FUNCAST, this, it);
      return (long) d;
    }
    final long l = it.itr();
    if(l < min || l > max) Err.or(FUNCAST, this, it);
    return l;
  }
  
  
  /**
   * Checks if the specified item is a string.
   * @param it item
   * @return item argument
   */
  protected static boolean checkStr(final Item it) {
    return (it.s() || it.u()) && it.type != URI;
  }

  /**
   * Checks the validity of the specified name.
   * @param it value to be checked
   * @throws XQException if name is invalid
   * @return name
   */
  protected byte[] checkName(final Item it) throws XQException {
    final byte[] v = norm(it.str());
    if(!XMLToken.isNCName(v)) error(it);
    return v;
  }
  
  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @return dummy item
   * @throws XQException xquery exception
   */
  protected Item error(final Item it) throws XQException {
    Err.cast(this, it);
    return null;
  }

  // PUBLIC AND STATIC METHODS ================================================

  /**
   * Checks if the specified type is an instance of the current type.
   * @param t type to be checked
   * @return result of check
   */
  public boolean instance(final Type t) {
    return this == t ? true : par == null ? false : par.instance(t);
  }

  /**
   * Checks if the type refers to a node.
   * @return result of check
   */
  public boolean node() {
    return this == NOD || par == NOD;
  }
  
  /**
   * Finds and returns the specified data type. 
   * @param type type as string
   * @param nodes flag for including node types
   * @return type or null
   */
  public static Type find(final QNm type, final boolean nodes) {
    final byte[] ln = type.ln();
    final byte[] uri = type.uri.str();
    
    for(final Type t : Type.values()) {
      if(eq(t.name, ln) && eq(uri, t.uri) &&
          (nodes || t.par != null && t != AAT)) return t;
    }
    return null;
  }

  @Override
  public String toString() {
    return string(name);
  }
};
