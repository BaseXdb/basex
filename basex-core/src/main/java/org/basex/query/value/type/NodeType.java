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
import org.basex.io.in.DataInput;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NODE("node", AtomType.ITEM, ID.NOD),

  /** Text type. */
  TEXT("text", NODE, ID.TXT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info) {
      if(value instanceof BXText) return ((BXNode) value).getNode();
      if(value instanceof Text) return new FTxt((Text) value);
      return new FTxt(Token.token(value));
    }
  },

  /** PI type. */
  PROCESSING_INSTRUCTION("processing-instruction", NODE, ID.PI) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof BXPI) return ((BXNode) value).getNode();
      if(value instanceof ProcessingInstruction) return new FPI((ProcessingInstruction) value);
      final Matcher m = matcher("<\\?(.*?) (.*)\\?>", value, info);
      return new FPI(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Element type. */
  ELEMENT("element", NODE, ID.ELM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof BXElem)  return ((BXNode) value).getNode();
      if(value instanceof Element) return FElem.build((Element) value, new TokenMap()).finish();
      try {
        return new DBNode(new IOContent(Token.token(value))).childIter().next();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(info, this, ex);
      }
    }
  },

  /** Document type. */
  DOCUMENT_NODE("document-node", NODE, ID.DOC) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof BXDoc) return ((BXNode) value).getNode();
      try {
        if(value instanceof Document) {
          final DOMWrapper dom = new DOMWrapper((Document) value, "", new MainOptions());
          return new DBNode(MemBuilder.build(dom));
        }
        if(value instanceof DocumentFragment) {
          // document fragment
          final DocumentFragment df = (DocumentFragment) value;
          final String bu = df.getBaseURI();
          return FDoc.build(bu != null ? bu : "", df).finish();
        }
        final byte[] token = Token.token(value);
        return Token.startsWith(token, '<') ? new DBNode(new IOContent(token)) :
          FDoc.build().add(token).finish();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(info, this, ex);
      }
    }
  },

  /** Document element type. */
  DOCUMENT_NODE_ELEMENT("document-node(element())", DOCUMENT_NODE, ID.DEL) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      return DOCUMENT_NODE.cast(value, qc, info);
    }
  },

  /** Attribute type. */
  ATTRIBUTE("attribute", NODE, ID.ATT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof BXAttr) return ((BXNode) value).getNode();
      if(value instanceof Attr) return new FAttr((Attr) value);
      final Matcher m = matcher(" ?(.*?)=\"(.*)\"", value, info);
      return new FAttr(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Comment type. */
  COMMENT("comment", NODE, ID.COM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof BXComm) return ((BXNode) value).getNode();
      if(value instanceof Comment) return new FComm((Comment) value);
      final Matcher m = matcher("<!--(.*?)-->", value, info);
      return new FComm(Token.token(m.group(1)));
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
    return this != PROCESSING_INSTRUCTION && this != COMMENT && this != NODE;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return this != PROCESSING_INSTRUCTION && this != COMMENT && this != NODE;
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
  public final ANode cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {
    if(item.type == this) return (ANode) item;
    throw typeError(item, this, info);
  }

  @Override
  public ANode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
  }

  @Override
  public ANode read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    return cast(in.readToken(), qc, null);
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
  public final NodeType intersect(final Type type) {
    return instanceOf(type) ? this : type.instanceOf(this) ? (NodeType) type : null;
  }

  @Override
  public final AtomType atomic() {
    return oneOf(PROCESSING_INSTRUCTION, COMMENT) ? AtomType.STRING :
      this == NODE ? AtomType.ANY_ATOMIC_TYPE : AtomType.UNTYPED_ATOMIC;
  }

  @Override
  public final ID id() {
    return id;
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
   * Creates a matcher for the specified pattern or raises an error.
   * @param pattern pattern
   * @param value value
   * @param info input info (can be {@code null})
   * @return matcher
   * @throws QueryException query exception
   */
  final Matcher matcher(final String pattern, final Object value, final InputInfo info)
      throws QueryException {
    final Matcher m = Pattern.compile(pattern).matcher(Token.string(Token.token(value)));
    if(m.find()) return m;
    throw NODEERR_X_X.get(info, this, value);
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
}
