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
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NOD("node", AtomType.ITEM, ID.NOD),

  /** Text type. */
  TXT("text", NOD, ID.TXT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) {
      if(value instanceof BXText) return ((BXNode) value).getNode();
      if(value instanceof Text) return new FTxt((Text) value);
      return new FTxt(value.toString());
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, ID.PI) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
      if(value instanceof BXPI) return ((BXNode) value).getNode();
      if(value instanceof ProcessingInstruction) return new FPI((ProcessingInstruction) value);
      final Matcher m = Pattern.compile("<\\?(.*?) (.*)\\?>").matcher(value.toString());
      if(m.find()) return new FPI(m.group(1), m.group(2));
      throw NODEERR_X_X.get(info, this, normalize(value, info));
    }
  },

  /** Element type. */
  ELM("element", NOD, ID.ELM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
      if(value instanceof BXElem)  return ((BXNode) value).getNode();
      if(value instanceof Element) return new FElem((Element) value, null, new TokenMap());
      try {
        return new DBNode(new IOContent(value.toString())).children().next();
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(info, this, ex);
      }
    }
  },

  /** Document type. */
  DOC("document-node", NOD, ID.DOC) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
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
        if(string.startsWith("<")) return new DBNode(new IOContent(string));
        return new FDoc().add(new FTxt(string));
      } catch(final IOException ex) {
        throw NODEERR_X_X.get(info, this, ex);
      }
    }
  },

  /** Document element type. */
  DEL("document-node(element())", DOC, ID.DEL) {
    @Override
    public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
      return DOC.cast(value, qc, sc, info);
    }
  },

  /** Attribute type. */
  ATT("attribute", NOD, ID.ATT) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
      if(value instanceof BXAttr) return ((BXNode) value).getNode();
      if(value instanceof Attr) return new FAttr((Attr) value);
      final Matcher m = Pattern.compile(" (.*?)=\"(.*)\"").matcher(value.toString());
      if(m.find()) return new FAttr(m.group(1), m.group(2));
      throw NODEERR_X_X.get(info, this, normalize(value, info));
    }
  },

  /** Comment type. */
  COM("comment", NOD, ID.COM) {
    @Override
    public ANode cast(final Object value, final QueryContext qc, final StaticContext sc,
        final InputInfo info) throws QueryException {
      if(value instanceof BXComm) return ((BXNode) value).getNode();
      if(value instanceof Comment) return new FComm((Comment) value);
      final Matcher m = Pattern.compile("<!--(.*?)-->").matcher(value.toString());
      if(m.find()) return new FComm(m.group(1));
      throw NODEERR_X_X.get(info, this, normalize(value, info));
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

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

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
      final InputInfo info) throws QueryException {
    return item.type == this ? item : error(item, info);
  }

  @Override
  public Item cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {
    throw Util.notExpected(value);
  }

  @Override
  public final Item castString(final String value, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return cast(value, qc, sc, info);
  }

  @Override
  public final SeqType seqType(final Occ occ) {
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public final boolean eq(final Type type) {
    return this == type;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    return this == type || parent.instanceOf(type);
  }

  @Override
  public final Type union(final Type type) {
    return type instanceof NodeType ? this == type ? this : NOD : AtomType.ITEM;
  }

  @Override
  public final NodeType intersect(final Type type) {
    if(!(type instanceof NodeType)) return instanceOf(type) ? this : null;
    return this == type ? this : this == NOD ? (NodeType) type : type == NOD ? this : null;
  }

  @Override
  public final byte[] string() {
    return name;
  }

  @Override
  public final AtomType atomic() {
    return this == NodeType.PI || this == NodeType.COM ? AtomType.STR : AtomType.ATM;
  }

  @Override
  public final ID id() {
    return id;
  }

  @Override
  public final String toString() {
    return Strings.concat(name, "()");
  }

  /**
   * Throws a casting exception.
   * @param item item to be included in the error message
   * @param info input info
   * @return dummy item
   * @throws QueryException query exception
   */
  final Item error(final Item item, final InputInfo info) throws QueryException {
    throw typeError(item, this, info);
  }

  @Override
  public boolean nsSensitive() {
    return false;
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
