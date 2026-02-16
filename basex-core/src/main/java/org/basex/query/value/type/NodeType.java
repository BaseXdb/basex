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
 * XDM node types.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NODE("node", ID.NOD),

  /** Text type. */
  TEXT("text", ID.TXT) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info) {
      if(value instanceof final BXText text) return text.getNode();
      if(value instanceof final Text text) return new FTxt(text);
      return new FTxt(Token.token(value));
    }
  },

  /** PI type. */
  PROCESSING_INSTRUCTION("processing-instruction", ID.PI) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof final BXPI pi) return pi.getNode();
      if(value instanceof final ProcessingInstruction pi) return new FPI(pi);
      final Matcher m = matcher("<\\?(.*?) (.*)\\?>", value, info);
      return new FPI(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Element type. */
  ELEMENT("element", ID.ELM) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
        throws QueryException {
      if(value instanceof final BXElem elem)
        return elem.getNode();
      if(value instanceof final Element elem)
        return FElem.build(elem, new TokenObjectMap<>()).finish();
      try {
        return new DBNode(new IOContent(Token.token(value))).childIter().next();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(info, this, ex);
      }
    }
  },

  /** Document type. */
  DOCUMENT("document-node", ID.DOC) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof final BXDoc doc) return doc.getNode();
      try {
        if(value instanceof final Document doc) {
          return new DBNode(MemBuilder.build(new DOMWrapper(doc, "", new MainOptions())));
        }
        if(value instanceof final DocumentFragment df) {
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

  /** Attribute type. */
  ATTRIBUTE("attribute", ID.ATT) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof final BXAttr attr) return attr.getNode();
      if(value instanceof final Attr attr) return new FAttr(attr);
      final Matcher m = matcher(" ?(.*?)=\"(.*)\"", value, info);
      return new FAttr(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Comment type. */
  COMMENT("comment", ID.COM) {
    @Override
    public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
      if(value instanceof final BXComm comm) return comm.getNode();
      if(value instanceof final Comment comm) return new FComm(comm);
      final Matcher m = matcher("<!--(.*?)-->", value, info);
      return new FComm(Token.token(m.group(1)));
    }
  },

  /** Namespace type. */
  NAMESPACE("namespace-node", ID.NSP),

  /** Schema-element. */
  SCHEMA_ELEMENT("schema-element", ID.SCE),

  /** Schema-attribute. */
  SCHEMA_ATTRIBUTE("schema-attribute", ID.SCA);

  /** Cached types. */
  private static final TokenObjectMap<NodeType> TYPES = new TokenObjectMap<>();

  /** Name. */
  private final byte[] name;
  /** Type ID . */
  private final ID id;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param name name
   * @param id type ID
   */
  NodeType(final String name, final ID id) {
    this.name = Token.token(name);
    this.id = id;
  }

  static {
    for(final NodeType type : values()) TYPES.put(type.name, type);
  }

  /**
   * Finds and returns the specified node type.
   * @param qname name of type
   * @return type or {@code null}
   */
  public static NodeType get(final QNm qname) {
    return qname.uri().length == 0 ? TYPES.get(qname.local()) : null;
  }

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return !oneOf(PROCESSING_INSTRUCTION, COMMENT, NODE);
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return isUntyped();
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
  public final XNode cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    if(item.type == this) return (XNode) item;
    throw typeError(item, this, info);
  }

  @Override
  public XNode cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
  }

  @Override
  public XNode read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    return cast(in.readToken(), qc, null);
  }

  @Override
  public final SeqType seqType(final Occ occ) {
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  /**
   * Returns the node kind.
   * @return kind
   */
  public final String description() {
    return Token.string(name).replace("-node", "");
  }

  /**
   * Returns the name of the node type.
   * @return type
   */
  public final byte[] test() {
    return name;
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    return type.oneOf(this, NODE, BasicType.ITEM) ||
      type instanceof final ChoiceItemType cit && cit.hasInstance(this);
  }

  @Override
  public final Type union(final Type type) {
    return type == this ? this : type instanceof NodeType ? NODE :
      type instanceof ChoiceItemType ? type.union(this) : BasicType.ITEM;
  }

  @Override
  public final Type intersect(final Type type) {
    return type instanceof ChoiceItemType ? type.intersect(this) :
      instanceOf(type) ? this : type.instanceOf(this) ? (NodeType) type : null;
  }

  @Override
  public final BasicType atomic() {
    return oneOf(PROCESSING_INSTRUCTION, COMMENT) ? BasicType.STRING :
      this == NODE ? BasicType.ANY_ATOMIC_TYPE : BasicType.UNTYPED_ATOMIC;
  }

  @Override
  public final ID id() {
    return id;
  }

  @Override
  public final boolean refinable() {
    return this == NODE;
  }

  @Override
  public boolean nsSensitive() {
    return false;
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

  @Override
  public final String toString() {
    return toString("");
  }
}
