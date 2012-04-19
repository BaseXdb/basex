package org.basex.query.item;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;
import org.w3c.dom.Text;

/**
 * XQuery node types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public enum NodeType implements Type {
  /** Node type. */
  NOD("node", AtomType.ITEM, 8),

  /** Text type. */
  TXT("text", NOD, 9) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) {
      return o instanceof BXText ? ((BXText) o).getNod() : new FTxt((Text) o);
    }
  },

  /** PI type. */
  PI("processing-instruction", NOD, 10) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) {
      return o instanceof BXPI ? ((BXPI) o).getNod() :
        new FPI((ProcessingInstruction) o);
    }
  },

  /** Element type. */
  ELM("element", NOD, 11) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) {
      return o instanceof BXElem ? ((BXElem) o).getNod() :
        new FElem((Element) o, null, new TokenMap());
    }
  },

  /** Document type. */
  DOC("document-node", NOD, 12) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) throws QueryException {
      if(o instanceof BXDoc) return ((BXDoc) o).getNod();
      if(o instanceof Document) {
        try {
          final DOMWrapper p = new DOMWrapper((Document) o, "", new Prop());
          return new DBNode(MemBuilder.build(p));
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
  DEL("document-node(element())", NOD, 13),

  /** Attribute type. */
  ATT("attribute", NOD, 14) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) {
      return o instanceof BXAttr ? ((BXAttr) o).getNod() : new FAttr((Attr) o);
    }
  },

  /** Comment type. */
  COM("comment", NOD, 15) {
    @Override
    public ANode cast(final Object o, final InputInfo ii) {
      return o instanceof BXComm ? ((BXComm) o).getNod() :
        new FComm((Comment) o);
    }
  },

  /** Namespace type. */
  NSP("namespace-node", NOD, 16),

  /** Schema-element. */
  SCE("schema-element", NOD, 17),

  /** Schema-attribute. */
  SCA("schema-attribute", NOD, 18);

  /** String representation. */
  private final byte[] string;
  /** Parent type. */
  private final Type par;
  /** Type id . */
  private final int id;
  /** Sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param nm string representation
   * @param pr parent type
   * @param i type id
   */
  NodeType(final String nm, final Type pr, final int i) {
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
  public Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return it.type != this ? error(it, ii) : it;
  }

  @Override
  public Item cast(final Object o, final InputInfo ii) throws QueryException {
    Util.notexpected(o);
    return null;
  }

  @Override
  public SeqType seqType() {
    // cannot be statically instantiated due to circular dependency
    if(seq == null) seq = new SeqType(this);
    return seq;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t || par != null && par.instanceOf(t);
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public byte[] string() {
    return string;
  }

  @Override
  public String toString() {
    return new TokenBuilder().add(string).add("()").toString();
  }

  /**
   * Throws a casting exception.
   * @param it item to be included in the error message
   * @param ii input info
   * @return dummy item
   * @throws QueryException query exception
   */
  Item error(final Item it, final InputInfo ii) throws QueryException {
    Err.cast(ii, this, it);
    return null;
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
}
