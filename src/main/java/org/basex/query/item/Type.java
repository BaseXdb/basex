package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DOCWrapper;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * XQuery data types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Type {
  /** Item type. */
  ITEM("item", null, EMPTY, false, false, false, false),

  /** Any atomic type. */
  AAT("anyAtomicType", ITEM, XSURI, false, false, false, false),

  /** Untyped Atomic type. */
  ATM("untypedAtomic", AAT, XSURI, false, true, true, false) {
    @Override
    public Atm e(final Item it, final QueryContext ctx) {
      return new Atm(it.atom());
    }
    @Override
    public Atm e(final Object o) {
      return new Atm(token(o.toString()));
    }
  },

  /** String type. */
  STR("string", AAT, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) {
      return Str.get(it.atom());
    }
    @Override
    public Str e(final Object o) {
      return Str.get(o);
    }
  },

  /** Normalized String type. */
  NST("normalizedString", STR, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) {
      return new Str(it.atom(), this);
    }
    @Override
    public Str e(final Object o) {
      return e(Str.get(o), null);
    }
  },

  /** Token type. */
  TOK("token", NST, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) {
      return new Str(norm(it.atom()), this);
    }
    @Override
    public Str e(final Object o) {
      return e(Str.get(o), null);
    }
  },

  /** Language type. */
  LAN("language", TOK, XSURI, false, false, true, false) {
    final Pattern pat = Pattern.compile("[A-Za-z]{1,8}(-[A-Za-z0-9]{1,8})*");

    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      final byte[] v = norm(it.atom());
      if(!pat.matcher(string(v)).matches()) error(it);
      return new Str(v, this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** NMTOKEN type. */
  NMT("NMTOKEN", TOK, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      final byte[] v = norm(it.atom());
      if(!XMLToken.isNMToken(v)) error(it);
      return new Str(v, this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Name type. */
  NAM("Name", TOK, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      final byte[] v = norm(it.atom());
      if(!XMLToken.isName(v)) error(it);
      return new Str(v, this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** NCName type. */
  NCN("NCName", NAM, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      return new Str(checkName(it), this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** ID type. */
  ID("ID", NCN, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      return new Str(checkName(it), this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** IDREF type. */
  IDR("IDREF", NCN, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      return new Str(checkName(it), this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Entity type. */
  ENT("ENTITY", NCN, XSURI, false, false, true, false) {
    @Override
    public Str e(final Item it, final QueryContext ctx) throws QueryException {
      return new Str(checkName(it), this);
    }
    @Override
    public Str e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Float type. */
  FLT("float", AAT, XSURI, true, false, false, false) {
    @Override
    public Flt e(final Item it, final QueryContext ctx) throws QueryException {
      return Flt.get(checkNum(it).flt());
    }
    @Override
    public Flt e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Double type. */
  DBL("double", AAT, XSURI, true, false, false, false) {
    @Override
    public Dbl e(final Item it, final QueryContext ctx) throws QueryException {
      return Dbl.get(checkNum(it).dbl());
    }
    @Override
    public Dbl e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Decimal type. */
  DEC("decimal", AAT, XSURI, true, false, false, false) {
    @Override
    public Dec e(final Item it, final QueryContext ctx) throws QueryException {
      return Dec.get(checkNum(it).dec());
    }
    @Override
    public Dec e(final Object o) {
      return Dec.get(new BigDecimal(o.toString()));
    }
  },

  /** Integer type. */
  ITR("integer", DEC, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return Itr.get(check(o, 0, 0));
    }
  },

  /** Non-positive integer type. */
  NPI("nonPositiveInteger", ITR, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, Long.MIN_VALUE, 0), this);
    }
  },

  /** Negative integer type. */
  NIN("negativeInteger", NPI, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, Long.MIN_VALUE, -1), this);
    }
  },

  /** Long type. */
  LNG("long", ITR, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return it instanceof Date ? new Itr((Date) it) : e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 0, 0), this);
    }
  },

  /** Int type. */
  INT("int", LNG, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, -0x80000000, 0x7FFFFFFF), this);
    }
  },

  /** Short type. */
  SHR("short", INT, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, -0x8000, 0x7FFF), this);
    }
  },

  /** Byte type. */
  BYT("byte", SHR, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, -0x80, 0x7F), this);
    }
  },

  /** Non-negative integer type. */
  NNI("nonNegativeInteger", ITR, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 0, Long.MAX_VALUE), this);
    }
  },

  /** Unsigned long type. */
  ULN("unsignedLong", NNI, XSURI, true, false, false, false) {
    /** Maximum value. */
    final BigDecimal max = new BigDecimal(Long.MAX_VALUE).multiply(
        BigDecimal.valueOf(2)).add(BigDecimal.ONE);

    @Override
    public Dec e(final Item it, final QueryContext ctx) throws QueryException {
      final BigDecimal v = checkNum(it).dec();
      if(v.signum() < 0 || v.compareTo(max) > 0 ||
          it.str() && contains(it.atom(), '.')) Err.or(FUNCAST, this, it);
      return new Dec(v, this);
    }
    @Override
    public Dec e(final Object o) {
      return new Dec(token(o.toString()));
    }
  },

  /** Short type. */
  UIN("unsignedInt", ULN, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 0, 0xFFFFFFFFL), this);
    }
  },

  /** Unsigned Short type. */
  USH("unsignedShort", UIN, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 0, 0xFFFF), this);
    }
  },

  /** Unsigned byte type. */
  UBY("unsignedByte", USH, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 0, 0xFF), this);
    }
  },

  /** Positive integer type. */
  PIN("positiveInteger", NNI, XSURI, true, false, false, false) {
    @Override
    public Itr e(final Item it, final QueryContext ctx) throws QueryException {
      return e(it);
    }
    @Override
    public Itr e(final Object o) throws QueryException {
      return new Itr(check(o, 1, Long.MAX_VALUE), this);
    }
  },

  /** Duration type. */
  DUR("duration", AAT, XSURI, false, false, false, true) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.dur() ? new Dur((Dur) it) : str(it) ?
          new Dur(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Year month duration type. */
  YMD("yearMonthDuration", DUR, XSURI, false, false, false, true) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.dur() ? new YMd((Dur) it) : str(it) ?
          new YMd(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Day time duration type. */
  DTD("dayTimeDuration", DUR, XSURI, false, false, false, true) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.dur() ? new DTd((Dur) it) : str(it) ?
          new DTd(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** DateTime type. */
  DTM("dateTime", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == LNG ? new Dtm((Itr) it) : it.type == DAT ?
          new Dtm((Date) it) : str(it) ? new Dtm(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Date type. */
  DAT("date", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM ? new Dat((Date) it) : str(it) ?
          new Dat(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Time type. */
  TIM("time", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM ? new Tim((Date) it) : str(it) ?
          new Tim(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Year month type. */
  YMO("gYearMonth", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM || it.type == DAT ? new DSim((Date) it, this) :
        str(it) ? new DSim(it.atom(), this) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Year type. */
  YEA("gYear", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM || it.type == DAT ? new DSim((Date) it, this) :
        str(it) ? new DSim(it.atom(), this) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Month day type. */
  MDA("gMonthDay", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM || it.type == DAT ? new DSim((Date) it, this) :
        str(it) ? new DSim(it.atom(), this) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Day type. */
  DAY("gDay", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM || it.type == DAT ? new DSim((Date) it, this) :
        str(it) ? new DSim(it.atom(), this) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Month type. */
  MON("gMonth", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == DTM || it.type == DAT ? new DSim((Date) it, this) :
        str(it) ? new DSim(it.atom(), this) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return e(Str.get(o), null);
    }
  },

  /** Boolean type. */
  BLN("boolean", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.num() ? Bln.get(it.bool()) : str(it) ?
          Bln.get(Bln.check(it.atom())) : error(it);
    }
    @Override
    public Item e(final Object o) {
      return Bln.get((Boolean) o);
    }
  },

  /** Base64 binary type. */
  B6B("base64Binary", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == HEX ? new B64((Hex) it) : str(it) ?
          new B64(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return new B64((byte[]) o);
    }
  },

  /** Hex binary type. */
  HEX("hexBinary", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      return it.type == B6B ? new Hex((B64) it) : str(it) ?
          new Hex(it.atom()) : error(it);
    }
    @Override
    public Item e(final Object o) throws QueryException {
      return new Hex((byte[]) o);
    }
  },

  /** Any URI type. */
  URI("anyURI", AAT, XSURI, false, false, true, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      if(!it.str()) error(it);
      final Uri u = Uri.uri(it.atom());
      if(!u.valid()) Err.or(FUNCAST, this, it);
      return u;
    }
    @Override
    public Item e(final Object o) {
      return Uri.uri(token(o.toString()));
    }
  },

  /** QName Type. */
  QNM("QName", AAT, XSURI, false, false, false, false) {
    @Override
    public Item e(final Item it, final QueryContext ctx) throws QueryException {
      if(it.type != STR) error(it);
      final byte[] s = trim(it.atom());
      if(s.length == 0) Err.or(QNMINV, s);
      return new QNm(s, ctx);
    }
    @Override
    public Item e(final Object o) {
      return new QNm((QName) o);
    }
  },

  /** NOTATION Type. */
  NOT("NOTATION", null, XSURI, false, false, false, false),

  /** Node type. */
  NOD("node", AAT, EMPTY, false, true, false, false),

  /** Text type. */
  TXT("text", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) {
      return o instanceof BXText ? ((BXText) o).getNod() :
        new FTxt((Text) o, null);
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) {
      return o instanceof BXPI ? ((BXPI) o).getNod() :
        new FPI((ProcessingInstruction) o, null);
    }
  },

  /** Element type. */
  ELM("element", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) {
      return o instanceof BXElem ? ((BXElem) o).getNod() :
        new FElem((Element) o, null);
    }
  },

  /** Document type. */
  DOC("document-node", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) throws QueryException {
      if(o instanceof BXDoc) return ((BXDoc) o).getNod();

      if(o instanceof Document) {
        try {
          final DOCWrapper p = new DOCWrapper((Document) o, "");
          final Data data = MemBuilder.build(p, new Prop(false));
          return new DBNode(data, 0);
        } catch(final IOException ex) {
          Err.or(UNDOC, ex.getMessage());
        }
      }
      // document fragment
      final DocumentFragment df = (DocumentFragment) o;
      final String bu = df.getBaseURI();
      return new FDoc(df, bu != null ? token(bu) : EMPTY);
    }
  },

  /** Document element type (required by XQJ API). */
  DEL("document-node(...)", NOD, EMPTY, false, true, false, false),

  /** Attribute type. */
  ATT("attribute", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) {
      return o instanceof BXAttr ? ((BXAttr) o).getNod() :
        new FAttr((Attr) o, null);
    }
  },

  /** Comment type. */
  COM("comment", NOD, EMPTY, false, true, false, false) {
    @Override
    public Nod e(final Object o) {
      return o instanceof BXComm ? ((BXComm) o).getNod() :
        new FComm((Comment) o, null);
    }
  },

  /** Sequence type. */
  SEQ("sequence", null, EMPTY, false, false, false, false),

  /** Empty sequence type. */
  EMP("empty-sequence", null, EMPTY, false, false, false, false),

  /** Java type. */
  JAVA("java", null, EMPTY, true, true, true, false);

  /** String representation. */
  public final String name;
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

  /**
   * Constructs a new item from the specified item.
   * @param it item to be converted
   * @param ctx query context
   * @return new item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item e(final Item it, final QueryContext ctx) throws QueryException {
    return it.type != this ? error(it) : it;
  }

  /**
   * Constructs a new item from the specified Java object.
   * The Java object is supposed to have a correct mapping type.
   * @param o Java object
   * @return new item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item e(final Object o) throws QueryException {
    Main.notexpected(o);
    return null;
  }

  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   * @param ur uri
   * @param n number flag
   * @param u untyped flag
   * @param s string flag
   * @param d duration flag
   */
  private Type(final String nm, final Type pr, final byte[] ur, final boolean n,
      final boolean u, final boolean s, final boolean d) {
    name = nm;
    par = pr;
    uri = ur;
    num = n;
    unt = u;
    str = s;
    dur = d;
  }

  /**
   * Throws an exception if the specified item can't be converted to a number.
   * @param it item
   * @return item argument
   * @throws QueryException query exception
   */
  protected final Item checkNum(final Item it) throws QueryException {
    return it.type == URI || !it.str() && !it.num() && !it.unt() &&
      it.type != BLN ? error(it) : it;
  }

  /**
   * Checks the validity of the specified object and returns its long value.
   * @param o value to be checked
   * @param min minimum value
   * @param max maximum value
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long check(final Object o, final long min,
      final long max) throws QueryException {

    final Item it = o instanceof Item ? (Item) o : Str.get(o.toString());
    checkNum(it);

    if(it.type == Type.DBL || it.type == Type.FLT) {
      final double d = it.dbl();
      if(d != d || d == 1 / 0d || d == -1 / 0d) Err.value(this, it);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) Err.or(INTRANGE, d);
      if(min != max && (d < min || d > max)) Err.or(FUNCAST, this, it);
      return (long) d;
    }
    final long l = it.itr();
    if(min == max) {
      final double d = it.dbl();
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) Err.or(FUNCAST, this, it);
    }
    if(min != max && (l < min || l > max)) Err.or(FUNCAST, this, it);
    return l;
  }

  /**
   * Checks if the specified item is a string.
   * @param it item
   * @return item argument
   */
  protected static boolean str(final Item it) {
    return (it.str() || it.unt()) && it.type != URI;
  }

  /**
   * Checks the validity of the specified name.
   * @param it value to be checked
   * @throws QueryException query exception
   * @return name
   */
  protected final byte[] checkName(final Item it) throws QueryException {
    final byte[] v = norm(it.atom());
    if(!XMLToken.isNCName(v)) error(it);
    return v;
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @return dummy item
   * @throws QueryException query exception
   */
  protected final Item error(final Item it) throws QueryException {
    Err.cast(this, it);
    return null;
  }

  // PUBLIC AND STATIC METHODS ================================================

  /**
   * Checks if the specified type is an instance of the current type.
   * @param t type to be checked
   * @return result of check
   */
  public final boolean instance(final Type t) {
    return this == t || par != null && par.instance(t);
  }

  /**
   * Checks if the type refers to a node.
   * @return result of check
   */
  public final boolean node() {
    return this == NOD || par == NOD;
  }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @param root include root types
   * @return type or null
   */
  public static Type find(final QNm type, final boolean root) {
    final String ln = string(type.ln());
    final byte[] uri = type.uri.atom();

    for(final Type t : values()) {
      if((root || t.par != null && t != AAT) && ln.equals(t.name) &&
          eq(uri, t.uri) && t != Type.SEQ && t != Type.JAVA) return t;
    }
    return null;
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or null
   */
  public static Type node(final QNm type) {
    final String ln = string(type.ln());
    final byte[] uri = type.uri.atom();
    for(final Type t : Type.values()) {
      if(t.node() && ln.equals(t.name) && eq(uri, t.uri)) return t;
    }
    return null;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(uri == XSURI) {
      tb.add(XS);
      tb.add(':');
    }
    tb.add(name);
    if(uri != XSURI) tb.add("()");
    return tb.toString();
  }
};
