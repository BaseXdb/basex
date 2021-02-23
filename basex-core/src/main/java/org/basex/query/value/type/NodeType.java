package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * XQuery node types.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NODE("node", AtomType.ITEM, ID.NOD),

  /** Text type. */
  TEXT("text", NODE, ID.TXT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) {
      if(value instanceof BXText) return ((BXNode) value).getNode();
      if(value instanceof Text) return new FTxt((Text) value);
      return new FTxt(value.toString());
    }
  },

  /** PI type. */
  PROCESSING_INSTRUCTION("processing-instruction", NODE, ID.PI) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXPI) return ((BXNode) value).getNode();
      if(value instanceof ProcessingInstruction) return new FPI((ProcessingInstruction) value);
      final Matcher m = Pattern.compile("<\\?(.*?) (.*)\\?>").matcher(value.toString());
      if(m.find()) return new FPI(m.group(1), m.group(2));
      throw NODEERR_X_X.get(ii, this, normalize(value, ii));
    }
  },

  /** Element type. */
  ELEMENT("element", NODE, ID.ELM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXElem)  return ((BXNode) value).getNode();
      if(value instanceof Element) return new FElem((Element) value, null, new TokenMap());
      try {
        return new DBNode(new IOContent(value.toString())).childIter().next();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(ii, this, ex);
      }
    }
  },

  /** Document type. */
  DOCUMENT_NODE("document-node", NODE, ID.DOC) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXDoc) return ((BXNode) value).getNode();
      try {
        if(value instanceof Document) {
          final DOMWrapper dom = new DOMWrapper((Document) value, "", MainOptions.get());
          return new DBNode(MemBuilder.build(dom));
        }
        if(value instanceof DocumentFragment) {
          // document fragment
          final DocumentFragment df = (DocumentFragment) value;
          final String bu = df.getBaseURI();
          return new FDoc(df, bu != null ? Token.token(bu) : Token.EMPTY);
        }
        final String string = value.toString();
        if(Strings.startsWith(string, '<')) return new DBNode(new IOContent(string));
        return new FDoc().add(new FTxt(string));
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(ii, this, ex);
      }
    }
  },

  /** Document element type. */
  DOCUMENT_NODE_ELEMENT("document-node(element())", DOCUMENT_NODE, ID.DEL) {
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return DOCUMENT_NODE.cast(value, qc, sc, ii);
    }
  },

  /** Attribute type. */
  ATTRIBUTE("attribute", NODE, ID.ATT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXAttr) return ((BXNode) value).getNode();
      if(value instanceof Attr) return new FAttr((Attr) value);
      final Matcher m = Pattern.compile(" (.*?)=\"(.*)\"").matcher(value.toString());
      if(m.find()) return new FAttr(m.group(1), m.group(2));
      throw NODEERR_X_X.get(ii, this, normalize(value, ii));
    }
  },

  /** Comment type. */
  COMMENT("comment", NODE, ID.COM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      if(value instanceof BXComm) return ((BXNode) value).getNode();
      if(value instanceof Comment) return new FComm((Comment) value);
      final Matcher m = Pattern.compile("<!--(.*?)-->").matcher(value.toString());
      if(m.find()) return new FComm(m.group(1));
      throw NODEERR_X_X.get(ii, this, normalize(value, ii));
    }
  },

  /** Namespace type. */
  NAMESPACE_NODE("namespace-node", NODE, ID.NSP),

  /** Schema-element. */
  SCHEMA_ELEMENT("schema-element", NODE, ID.SCE),

  /** Schema-attribute. */
  SCHEMA_ATTRIBUTE("schema-attribute", NODE, ID.SCA);

  /** Cached enums (faster). */
  private static final NodeType[] VALUES = values();
  /** Leaf node types. */
  public static final NodeType[] LEAF_TYPES = {
    ATTRIBUTE, COMMENT, NAMESPACE_NODE, PROCESSING_INSTRUCTION, TEXT
  };

  /** Name. */
  private final byte[] name;
  /** Parent type. */
  private final Type parent;
  /** Type id . */
  private final ID id;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;
  /** QName (lazy instantiation). */
  private QNm qnm;

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
  public final boolean isSortable() {
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
    if(qnm == null) qnm = new QNm(name);
    return qnm;
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    return this == type || type == AtomType.ITEM ||
        type instanceof NodeType && parent.instanceOf(type);
  }

  @Override
  public final Type union(final Type type) {
    return this == type ? this : type instanceof NodeType ? NODE : AtomType.ITEM;
  }

  @Override
  public final Type intersect(final Type type) {
    return instanceOf(type) ? this : type.instanceOf(this) ? type : null;
  }

  @Override
  public final AtomType atomic() {
    return this == PROCESSING_INSTRUCTION || this == COMMENT ? AtomType.STRING :
      AtomType.UNTYPED_ATOMIC;
  }

  @Override
  public final ID id() {
    return id;
  }

  /**
   * Throws a casting exception.
   * @param item item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  final Item error(final Item item, final InputInfo ii) throws QueryException {
    throw typeError(item, this, ii);
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public final String toString() {
    return toString("");
  }

  /**
   * Returns a string representation with the specified argument.
   * @param arg argument
   * @return string representation
   */
  public final String toString(final String arg) {
    return new TokenBuilder().add(name).add('(').add(arg).add(')').toString();
  }

  /**
   * Finds and returns the specified node type.
   * @param name name of type
   * @return type or {@code null}
   */
  public static NodeType find(final QNm name) {
    if(name.uri().length == 0) {
      final byte[] ln = name.local();
      for(final NodeType type : VALUES) {
        if(Token.eq(ln, type.name)) return type;
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
    for(final NodeType type : VALUES) {
      if(type.id == id) return type;
    }
    return null;
  }
}
