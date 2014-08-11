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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NOD("node", AtomType.ITEM, ID.NOD),

  /** Text type. */
  TXT("text", NOD, ID.TXT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      if(value instanceof BXText) return ((BXNode) value).getNode();
      if(value instanceof Text) return new FTxt((Text) value);
      return new FTxt(value.toString());
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, ID.PI) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXPI) return ((BXNode) value).getNode();
      if(value instanceof ProcessingInstruction) return new FPI((ProcessingInstruction) value);
      final Matcher m = Pattern.compile("<\\?(.*?) (.*)\\?>").matcher(value.toString());
      if(m.find()) return new FPI(m.group(1), m.group(2));
      throw NODEERR_X_X.get(ii, this, chop(value, ii));
    }
  },

  /** Element type. */
  ELM("element", NOD, ID.ELM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXElem)  return ((BXNode) value).getNode();
      if(value instanceof Element) return new FElem((Element) value, null, new TokenMap());
      try {
        return new DBNode(new IOContent(value.toString())).children().next();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(ii, this, ex);
      }
    }
  },

  /** Document type. */
  DOC("document-node", NOD, ID.DOC) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXDoc) return ((BXNode) value).getNode();
      try {
        if(value instanceof Document) {
          final MainOptions opts = new MainOptions();
          opts.set(MainOptions.CHOP, false);
          final DOMWrapper p = new DOMWrapper((Document) value, "", opts);
          return new DBNode(MemBuilder.build(p));
        }
        if(value instanceof DocumentFragment) {
          // document fragment
          final DocumentFragment df = (DocumentFragment) value;
          final String bu = df.getBaseURI();
          return new FDoc(df, bu != null ? Token.token(bu) : Token.EMPTY);
        }
        final String string = value.toString();
        if(string.startsWith("<")) return new DBNode(new IOContent(string));
        return new FDoc().add(new FTxt(string));
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(ii, this, ex);
      }
    }
  },

  /** Document element type. */
  DEL("document-node(element())", NOD, ID.DEL) {
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return DOC.cast(value, qc, sc, ii);
    }
  },

  /** Attribute type. */
  ATT("attribute", NOD, ID.ATT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXAttr) return ((BXNode) value).getNode();
      if(value instanceof Attr) return new FAttr((Attr) value);
      final Matcher m = Pattern.compile(" (.*?)=\"(.*)\"").matcher(value.toString());
      if(m.find()) return new FAttr(m.group(1), m.group(2));
      throw NODEERR_X_X.get(ii, this, chop(value, ii));
    }
  },

  /** Comment type. */
  COM("comment", NOD, ID.COM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXComm) return ((BXNode) value).getNode();
      if(value instanceof Comment) return new FComm((Comment) value);
      final Matcher m = Pattern.compile("<!--(.*?)-->").matcher(value.toString());
      if(m.find()) return new FComm(m.group(1));
      throw NODEERR_X_X.get(ii, this, chop(value, ii));
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
  /** Name. */
  private final byte[] name;
  /** Parent type. */
  private final Type parent;
  /** Type id . */
  private final ID id;

  /** Sequence type (lazy instantiation). */
  private SeqType seq;

  /**
   * Constructor.
   * @param name name
   * @param parent parent type
   * @param id type id
   */
  NodeType(final String name, final Type parent, final ID id) {
    this.name = Token.token(name);
    this.parent = parent;
    this.id = id;
  }

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return true;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return true;
  }

  @Override
  public boolean isStringOrUntyped() {
    return true;
  }

  @Override
  public final Item cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item.type == this ? item : error(item, ii);
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
  public final SeqType seqType() {
    // cannot be statically instantiated due to circular dependency
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public final boolean eq(final Type t) {
    return this == t;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t || parent.instanceOf(t);
  }

  @Override
  public final Type union(final Type t) {
    return t instanceof NodeType ? this == t ? this : NOD : AtomType.ITEM;
  }

  @Override
  public final NodeType intersect(final Type t) {
    if(!(t instanceof NodeType)) return instanceOf(t) ? this : null;
    return this == t ? this : this == NOD ? (NodeType) t : t == NOD ? this : null;
  }

  @Override
  public final ID id() {
    return id;
  }

  @Override
  public final byte[] string() {
    return name;
  }

  @Override
  public final String toString() {
    return new TokenBuilder(name).add("()").toString();
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  final Item error(final Item it, final InputInfo ii) throws QueryException {
    throw Err.castError(ii, it, this);
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  /**
   * Finds and returns the specified node type.
   * @param type type
   * @return type or {@code null}
   */
  public static NodeType find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      for(final NodeType t : VALUES) {
        if(Token.eq(ln, t.name)) return t;
      }
    }
    return null;
  }

  /**
   * Gets the type instance for the given ID.
   * @param id type ID
   * @return corresponding type if found, {@code null} otherwise
   */
  static Type getType(final ID id) {
    for(final NodeType t : VALUES) if(t.id == id) return t;
    return null;
  }
}
