package org.basex.query.item;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DOMWrapper;
import org.basex.core.Prop;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NOD("node", AtomType.ITEM),

  /** Text type. */
  TXT("text", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) {
      return o instanceof BXText ? ((BXText) o).getNod() : new FTxt((Text) o);
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) {
      return o instanceof BXPI ? ((BXPI) o).getNod() :
        new FPI((ProcessingInstruction) o);
    }
  },

  /** Element type. */
  ELM("element", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) {
      return o instanceof BXElem ? ((BXElem) o).getNod() :
        new FElem((Element) o, null, new TokenMap());
    }
  },

  /** Document type. */
  DOC("document-node", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) throws QueryException {
      if(o instanceof BXDoc) return ((BXDoc) o).getNod();

      if(o instanceof Document) {
        try {
          final DOMWrapper p = new DOMWrapper((Document) o, "");
          return new DBNode(MemBuilder.build(p, new Prop()), 0);
        } catch(final IOException ex) {
          UNDOC.thrw(ii, ex);
        }
      }
      // document fragment
      final DocumentFragment df = (DocumentFragment) o;
      final String bu = df.getBaseURI();
      return new FDoc(df, bu != null ? Token.token(bu) : Token.EMPTY);
    }
  },

  /** Document element type. */
  DEL("document-node(element())", NOD),

  /** Attribute type. */
  ATT("attribute", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) {
      return o instanceof BXAttr ? ((BXAttr) o).getNod() : new FAttr((Attr) o);
    }
  },

  /** Comment type. */
  COM("comment", NOD) {
    @Override
    public ANode e(final Object o, final InputInfo ii) {
      return o instanceof BXComm ? ((BXComm) o).getNod() :
        new FComm((Comment) o);
    }
  },

  /** Namespace type. */
  NSP("namespace-node", NOD);

  /** String representation. */
  private final byte[] string;
  /** Parent type. */
  private final Type par;
  /** Sequence type. */
  private SeqType seq;

  @Override
  public final boolean isNode() {
    return true;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isString() {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  @Override
  public boolean isDuration() {
    return false;
  }

  @Override
  public boolean isDate() {
    return false;
  }

  @Override
  public final boolean isFunction() {
    return false;
  }

  @Override
  public final boolean isMap() {
    return false;
  }

  @Override
  public Item e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return it.type != this ? error(it, ii) : it;
  }

  @Override
  public Item e(final Object o, final InputInfo ii) throws QueryException {
    Util.notexpected(o);
    return null;
  }

  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   */
  private NodeType(final String nm, final Type pr) {
    string = Token.token(nm);
    par = pr;
  }

  @Override
  public SeqType seq() {
    // cannot be statically instantiated due to circular dependency
    if(seq == null) seq = new SeqType(this);
    return seq;
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

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t || par != null && par.instanceOf(t);
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static NodeType find(final QNm type) {
    if(type.uri().length == 0) {
      final byte[] ln = type.local();
      for(final NodeType t : values()) {
        if(Token.eq(ln, t.string)) return t;
      }
    }
    return null;
  }

  @Override
  public int id() {
    return ordinal() + 8;
  }

  @Override
  public String toString() {
    return new TokenBuilder(string).add("()").toString();
  }

  @Override
  public byte[] string() {
    return string;
  }
}
