package org.basex.query.item;

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
import org.basex.build.xml.DOMWrapper;
import org.basex.core.Prop;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
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
 * @author BaseX Team 2005-11, BSD License
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
      return new FDoc(df, bu != null ? token(bu) : EMPTY);
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
  };

  /** String representation. */
  private final byte[] nam;
  /** Parent type. */
  private final Type par;
  /** Sequence type. */
  private SeqType seq;

  @Override
  public final boolean node() {
    return true;
  }

  @Override
  public boolean dat() {
    return false;
  }

  @Override
  public boolean dur() {
    return false;
  }

  @Override
  public boolean num() {
    return false;
  }

  @Override
  public boolean str() {
    return false;
  }

  @Override
  public boolean unt() {
    return true;
  }

  @Override
  public final boolean func() {
    return false;
  }

  @Override
  public final boolean map() {
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
    nam = token(nm);
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
  public final boolean instance(final Type t) {
    return this == t || par != null && par.instance(t);
  }

  /**
   * Finds and returns the specified node type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static NodeType find(final QNm type) {
    final byte[] ln = type.ln();
    final byte[] uri = type.uri().atom();
    for(final NodeType t : values()) {
      if(eq(ln, t.nam) && eq(uri, EMPTY)) return t;
    }
    return null;
  }

  @Override
  public int id() {
    return ordinal() + 8;
  }

  @Override
  public String toString() {
    return string(nam) + "()";
  }

  @Override
  public byte[] nam() {
    return nam;
  }
}
