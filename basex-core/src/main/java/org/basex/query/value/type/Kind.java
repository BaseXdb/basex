package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.regex.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.Type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * Node kinds.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Kind {
  /** Node. */
  NODE("node", ID.NOD),

  /** Text. */
  TEXT("text", ID.TXT) {
    @Override
    public XNode cast(final Object value, final InputInfo info) {
      if(value instanceof final BXText text) return text.getNode();
      if(value instanceof final Text text) return new FTxt(text);
      return new FTxt(Token.token(value));
    }
  },

  /** Processing instruction. */
  PROCESSING_INSTRUCTION("processing-instruction", ID.PI) {
    @Override
    public XNode cast(final Object value, final InputInfo info) throws QueryException {
      if(value instanceof final BXPI pi) return pi.getNode();
      if(value instanceof final ProcessingInstruction pi) return new FPI(pi);
      final Matcher m = matcher("<\\?(.*?) (.*)\\?>", value, info);
      return new FPI(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Element. */
  ELEMENT("element", ID.ELM) {
    @Override
    public XNode cast(final Object value, final InputInfo info) throws QueryException {
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

  /** Document. */
  DOCUMENT("document-node", ID.DOC) {
    @Override
    public XNode cast(final Object value, final InputInfo info) throws QueryException {
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

  /** Attribute. */
  ATTRIBUTE("attribute", ID.ATT) {
    @Override
    public XNode cast(final Object value, final InputInfo info) throws QueryException {
      if(value instanceof final BXAttr attr) return attr.getNode();
      if(value instanceof final Attr attr) return new FAttr(attr);
      final Matcher m = matcher(" ?(.*?)=\"(.*)\"", value, info);
      return new FAttr(new QNm(m.group(1)), Token.token(m.group(2)));
    }
  },

  /** Comment. */
  COMMENT("comment", ID.COM) {
    @Override
    public XNode cast(final Object value, final InputInfo info) throws QueryException {
      if(value instanceof final BXComm comm) return comm.getNode();
      if(value instanceof final Comment comm) return new FComm(comm);
      final Matcher m = matcher("<!--(.*?)-->", value, info);
      return new FComm(Token.token(m.group(1)));
    }
  },

  /** Namespace. */
  NAMESPACE("namespace-node", ID.NSP),

  /** Schema-element. */
  SCHEMA_ELEMENT("schema-element", ID.SCE),

  /** Schema-attribute. */
  SCHEMA_ATTRIBUTE("schema-attribute", ID.SCA);

  /** Name. */
  final byte[] name;
  /** Type ID . */
  final ID id;

  /**
   * Constructor.
   * @param name name
   * @param id type ID
   */
  Kind(final String name, final ID id) {
    this.name = Token.token(name);
    this.id = id;
  }

  /** Cached kinds. */
  static final TokenObjectMap<Kind> KINDS = new TokenObjectMap<>();

  static {
    for(final Kind kind : Kind.values()) KINDS.put(kind.name, kind);
  }

  /**
   * Finds and returns the specified node type.
   * @param qname name of type
   * @return kind or {@code null}
   */
  public static Kind get(final QNm qname) {
    if(qname.uri().length == 0) {
      return Kind.KINDS.get(qname.local());
    }
    return null;
  }

  /**
   * Casts the specified Java value to a node of this kind.
   * @param value Java value
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  XNode cast(final Object value, final InputInfo info) throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
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
   * Checks if this is one of the specified kinds.
   * @param kinds kinds
   * @return result of check
   */
  public boolean oneOf(final Kind... kinds) {
    for(final Kind kind : kinds) {
      if(this == kind) return true;
    }
    return false;
  }

  /**
   * Returns a node type description.
   * @return kind
   */
  public String description() {
    return Token.string(name).replace("-node", "");
  }

  /**
   * Returns a string representation with the specified argument.
   * @param arg argument
   * @return string representation
   */
  public String toString(final String arg) {
    return new TokenBuilder().add(name).add('(').add(arg).add(')').toString();
  }
}
