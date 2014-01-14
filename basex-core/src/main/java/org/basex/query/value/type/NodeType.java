package org.basex.query.value.type;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.regex.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * XQuery node types.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NOD("node", AtomType.ITEM, ID.NOD),

  /** Text type. */
  TXT("text", NOD, ID.TXT) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) {
      if(o instanceof BXText) return ((BXNode) o).getNode();
      if(o instanceof Text) return new FTxt((Text) o);
      return new FTxt(o.toString());
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, ID.PI) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(o instanceof BXPI) return ((BXNode) o).getNode();
      if(o instanceof ProcessingInstruction) return new FPI((ProcessingInstruction) o);
      final Matcher m = Pattern.compile("<\\?(.*?) (.*)\\?>").matcher(o.toString());
      if(m.find()) return new FPI(m.group(1), m.group(2));
      throw NODEERR.get(ii, this, chop(o));
    }
  },

  /** Element type. */
  ELM("element", NOD, ID.ELM) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(o instanceof BXElem)  return ((BXNode) o).getNode();
      if(o instanceof Element) return new FElem((Element) o, null, new TokenMap());
      try {
        return new DBNode(new IOContent(o.toString()), new MainOptions()).children().next();
      } catch(final IOException ex) {
        throw NODEERR.get(ii, this, ex);
      }
    }
  },

  /** Document type. */
  DOC("document-node", NOD, ID.DOC) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(o instanceof BXDoc) return ((BXNode) o).getNode();
      try {
        if(o instanceof Document) {
          final DOMWrapper p = new DOMWrapper((Document) o, "", new MainOptions());
          return new DBNode(MemBuilder.build(p));
        }
        if(o instanceof DocumentFragment) {
          // document fragment
          final DocumentFragment df = (DocumentFragment) o;
          final String bu = df.getBaseURI();
          return new FDoc(df, bu != null ? Token.token(bu) : Token.EMPTY);
        }
        final String c = o.toString();
        if(c.startsWith("<")) return new DBNode(new IOContent(c), new MainOptions());
        return new FDoc().add(new FTxt(c));
      } catch(final IOException ex) {
        throw NODEERR.get(ii, this, ex);
      }
    }
  },

  /** Document element type. */
  DEL("document-node(element())", NOD, ID.DEL) {
    @Override
    public Item cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return DOC.cast(o, ctx, sc, ii);
    }
  },

  /** Attribute type. */
  ATT("attribute", NOD, ID.ATT) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(o instanceof BXAttr) return ((BXNode) o).getNode();
      if(o instanceof Attr) return new FAttr((Attr) o);
      final Matcher m = Pattern.compile(" (.*?)=\"(.*)\"").matcher(o.toString());
      if(m.find()) return new FAttr(m.group(1), m.group(2));
      throw NODEERR.get(ii, this, chop(o));
    }
  },

  /** Comment type. */
  COM("comment", NOD, ID.COM) {
    @Override
    public ANode cast(final Object o, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(o instanceof BXComm) return ((BXNode) o).getNode();
      if(o instanceof Comment) return new FComm((Comment) o);
      final Matcher m = Pattern.compile("<!--(.*?)-->").matcher(o.toString());
      if(m.find()) return new FComm(m.group(1));
      throw NODEERR.get(ii, this, chop(o));
    }
  },

  /** Namespace type. */
  NSP("namespace-node", NOD, ID.NSP),

  /** Schema-element. */
  SCE("schema-element", NOD, ID.SCE),

  /** Schema-attribute. */
  SCA("schema-attribute", NOD, ID.SCA);

  /** Cached enums (faster). */
  private static final NodeType[] VALUES = values();
  /** String representation. */
  private final byte[] string;
  /** Parent type. */
  private final Type par;
  /** Type id . */
  private final ID id;
  /** Sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   * @param i type id
   */
  NodeType(final String nm, final Type pr, final ID i) {
    string = Token.token(nm);
    par = pr;
    id = i;
  }

  @Override
  public final boolean isNode() {
    return true;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  @Override
  public boolean isNumberOrUntyped() {
    return true;
  }

  @Override
  public boolean isStringOrUntyped() {
    return true;
  }

  @Override
  public Item cast(final Item it, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it.type == this ? it : error(it, ii);
  }

  @Override
  public Item cast(final Object o, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    throw Util.notExpected(o);
  }

  @Override
  public Item castString(final String o, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(o, ctx, sc, ii);
  }

  @Override
  public SeqType seqType() {
    // cannot be statically instantiated due to circular dependency
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public boolean eq(final Type t) {
    return this == t;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t || par.instanceOf(t);
  }

  @Override
  public Type union(final Type t) {
    return t.isNode() ? this == t ? this : NOD : AtomType.ITEM;
  }

  @Override
  public NodeType intersect(final Type t) {
    if(!(t instanceof NodeType)) return instanceOf(t) ? this : null;
    return this == t ? this : this == NOD ? (NodeType) t : t == NOD ? this : null;
  }

  @Override
  public ID id() {
    return id;
  }

  @Override
  public byte[] string() {
    return string;
  }

  @Override
  public String toString() {
    return new TokenBuilder(string).add("()").toString();
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  Item error(final Item it, final InputInfo ii) throws QueryException {
    throw Err.castError(ii, this, it);
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static NodeType find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      for(final NodeType t : VALUES) {
        if(Token.eq(ln, t.string)) return t;
      }
    }
    return null;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  /**
   * Gets the type instance for the given ID.
   * @param id type ID
   * @return corresponding type if found, {@code null} otherwise
   */
  static Type getType(final Type.ID id) {
    for(final NodeType t : VALUES) if(t.id == id) return t;
    return null;
  }
}
