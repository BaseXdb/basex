package org.basex.query.item;

import static java.lang.Double.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DOCWrapper;
import org.basex.core.Prop;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenMap;
import org.basex.util.Util;
import org.basex.util.XMLToken;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * XQuery node types.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {

  /** Node type. */
  NOD("node", SimpleType.ITEM, EMPTY, false, true, false, false, false),

  /** Text type. */
  TXT("text", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) {
      return o instanceof BXText ? ((BXText) o).getNod() :
        new FTxt((Text) o, null);
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) {
      return o instanceof BXPI ? ((BXPI) o).getNod() :
        new FPI((ProcessingInstruction) o, null);
    }
  },

  /** Element type. */
  ELM("element", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) {
      return o instanceof BXElem ? ((BXElem) o).getNod() :
        new FElem((Element) o, null, new TokenMap());
    }
  },

  /** Document type. */
  DOC("document-node", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) throws QueryException {
      if(o instanceof BXDoc) return ((BXDoc) o).getNod();

      if(o instanceof Document) {
        try {
          final DOCWrapper p = new DOCWrapper((Document) o, "");
          return new DBNode(MemBuilder.build(p, new Prop(false)), 0);
        } catch(final IOException ex) {
          UNDOC.thrw(ii, ex);
        }
      }
      // document fragment
      final DocumentFragment df = (DocumentFragment) o;
      final String bu = df.getBaseURI();
      return new FDoc(df, bu != null ? token(bu) : EMPTY);
    }
  },

  /** Document element type (required by XQJ API). */
  DEL("document-node(...)", NOD, EMPTY, false, true, false, false, false),

  /** Attribute type. */
  ATT("attribute", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) {
      return o instanceof BXAttr ? ((BXAttr) o).getNod() :
        new FAttr((Attr) o, null);
    }
  },

  /** Comment type. */
  COM("comment", NOD, EMPTY, false, true, false, false, false) {
    @Override
    public Nod e(final Object o, final InputInfo ii) {
      return o instanceof BXComm ? ((BXComm) o).getNod() :
        new FComm((Comment) o, null);
    }
  };

  /** String representation. */
  public final byte[] nam;
  /** URI representation. */
  private final byte[] uri;
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
  /** Date flag. */
  public final boolean dat;
  /** Sequence type. */
  private SeqType seq;

  @Override
  public boolean dat() {
    return dat;
  }

  @Override
  public boolean dur() {
    return dur;
  }

  @Override
  public byte[] nam() {
    return nam;
  }

  @Override
  public boolean num() {
    return num;
  }

  @Override
  public Type par() {
    return par;
  }

  @Override
  public boolean str() {
    return str;
  }

  @Override
  public boolean unt() {
    return unt;
  }

  @Override
  public byte[] uri() {
    return uri;
  }

  /**
   * Constructs a new item from the specified item.
   * @param it item to be converted
   * @param ctx query context
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  public Item e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return it.type != this ? error(it, ii) : it;
  }

  /**
   * Constructs a new item from the specified Java object.
   * The Java object is supposed to have a correct mapping type.
   * @param o Java object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item e(final Object o, final InputInfo ii) throws QueryException {
    Util.notexpected(o);
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
   * @param t date flag
   */
  private NodeType(final String nm, final Type pr, final byte[] ur,
      final boolean n, final boolean u, final boolean s, final boolean d,
      final boolean t) {
    nam = token(nm);
    par = pr;
    uri = ur;
    num = n;
    unt = u;
    str = s;
    dur = d;
    dat = t;
  }

  /**
   * Returns the sequence type of this type.
   * @return sequence type
   */
  public SeqType seq() {
    // cannot be statically instantiated due to circular dependency
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  /**
   * Throws an exception if the specified item can't be converted to a number.
   * @param it item
   * @param ii input info
   * @return item argument
   * @throws QueryException query exception
   */
  Item checkNum(final Item it, final InputInfo ii)
      throws QueryException {
    return it.type == SimpleType.URI || !it.str() && !it.num() && !it.unt() &&
      it.type != SimpleType.BLN ? error(it, ii) : it;
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
  long checkLong(final Object o, final long min,
      final long max, final InputInfo ii) throws QueryException {

    final Item it = o instanceof Item ? (Item) o : Str.get(o.toString());
    checkNum(it, ii);

    if(it.type == SimpleType.DBL || it.type == SimpleType.FLT) {
      final double d = it.dbl(ii);
      if(isNaN(d) || d == 1 / 0d || d == -1 / 0d) Err.value(ii, this, it);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) INTRANGE.thrw(ii, d);
      if(min != max && (d < min || d > max)) FUNCAST.thrw(ii, this, it);
      return (long) d;
    }
    final long l = it.itr(ii);
    if(min == max) {
      final double d = it.dbl(ii);
      if(d < Long.MIN_VALUE || d > Long.MAX_VALUE)
        FUNCAST.thrw(ii, this, it);
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
    return (it.str() || it.unt()) && it.type != SimpleType.URI;
  }

  /**
   * Checks the validity of the specified name.
   * @param it value to be checked
   * @param ii input info
   * @throws QueryException query exception
   * @return name
   */
  byte[] checkName(final Item it, final InputInfo ii)
      throws QueryException {
    final byte[] v = norm(it.atom());
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
  Item error(final Item it, final InputInfo ii)
      throws QueryException {
    Err.cast(ii, this, it);
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
    return false;
  }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @param atom atomic type
   * @return type or {@code null}
   */
  public static NodeType find(final QNm type, final boolean atom) {
    // type must be atomic, or must not have a namespace
    if(atom ^ !type.ns()) {
      final byte[] ln = type.ln();
      final byte[] uri = type.uri().atom();
      for(final NodeType t : values()) {
        // skip non-standard types
        if(eq(ln, t.nam) && eq(uri, t.uri)) return t;
      }
    }
    return null;
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static NodeType node(final QNm type) {
    final byte[] ln = type.ln();
    final byte[] uri = type.uri().atom();
    for(final NodeType t : values()) {
      if(t.node() && eq(ln, t.nam) && eq(uri, t.uri)) return t;
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
    tb.add(nam);
    if(uri != XSURI) tb.add("()");
    return tb.toString();
  }

}
