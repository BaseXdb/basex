package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Flt;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.SeqIter;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Java XQuery API - Data Factory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class BXQDataFactory extends BXQAbstract implements XQDataFactory {
  /** Database connection. */
  protected BXQStaticContext ctx;

  /**
   * Constructor.
   */
  public BXQDataFactory() {
    super(null);
    ctx = new BXQStaticContext();
  }

  public BXQItemType createAtomicType(final int b, final QName qn,
      final URI uri) throws XQException {
    return createAtomicType(b);
  }

  public BXQItemType createAtomicType(final int b) throws XQException {
    opened();
    return new BXQItemType(b);
  }

  public BXQItemType createAttributeType(final QName qn, final int it,
      final QName qn2, final URI uri) throws XQException {
    return createAttributeType(qn, it);
  }

  public BXQItemType createAttributeType(final QName qn, final int it)
      throws XQException {
    opened();
    checkAttr(it);
    return new BXQItemType(Type.ATT, qn, it);
  }
  
  /**
   * Performs a type check for attribute operations.
   * @param it input type
   * @throws XQException exception
   */
  private void checkAttr(final int it) throws XQException {
    if(it != XQItemType.XQBASETYPE_UNTYPED &&
        it != XQItemType.XQBASETYPE_ANYTYPE) return;
    throw new BXQException(ATT);
  }

  public BXQItemType createCommentType() throws XQException {
    opened();
    return new BXQItemType(Type.COM);
  }

  public BXQItemType createDocumentElementType(final XQItemType it)
      throws XQException {
    opened();
    valid(it, XQItemType.class);
    if(it.getItemKind() != XQItemType.XQITEMKIND_ELEMENT)
      throw new BXQException(ELM);
    
    return new BXQItemType(Type.DEL, it.getNodeName(), it.getBaseType());
  }

  public BXQItemType createDocumentSchemaElementType(final XQItemType it)
      throws XQException {
    opened();
    valid(it, XQItemType.class);
    if(it.getItemKind() != XQItemType.XQITEMKIND_SCHEMA_ELEMENT)
      throw new BXQException(ELM);
    
    return new BXQItemType(Type.DEL, it.getNodeName(), it.getBaseType());
  }
  
  public BXQItemType createDocumentType() throws XQException {
    opened();
    return new BXQItemType(Type.DOC);
  }

  public BXQItemType createElementType(final QName qn, final int it,
      final QName qn2, final URI uri, final boolean n) throws XQException {
    opened();
    return new BXQItemType(Type.ELM, qn, it);
  }

  public BXQItemType createElementType(final QName qn, final int it)
      throws XQException {
    return createElementType(qn, it, null, null, false);
  }

  public BXQItem createItem(final XQItem v) throws XQException {
    opened();
    valid(v, XQItem.class);
    try {
      final Type type = ((BXQItemType) v.getItemType()).getType();
      return new BXQItem(type.e(((BXQItem) v).it, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public BXQItem createItemFromAtomicValue(final String v, final XQItemType it)
      throws XQException {
    valid(it, XQItemType.class);
    try {
      final Str val = Str.get(valid(v, String.class));
      return new BXQItem(check(Type.STR, it).e(val, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public BXQItem createItemFromBoolean(final boolean v, final XQItemType it)
      throws XQException {
    check(Type.BLN, it);
    return new BXQItem(Bln.get(v));
  }

  public BXQItem createItemFromByte(final byte v, final XQItemType it)
      throws XQException {
    return itr(v, Type.BYT, it);
  }

  public BXQItem createItemFromDocument(final InputStream is,
      final String base, final XQItemType it) throws XQException {
    check(Type.DOC, it);
    return new BXQItem(createDB(is));
  }

  public BXQItem createItemFromDocument(final Reader r, final String base,
      final XQItemType it) throws XQException {
    check(Type.DOC, it);
    return new BXQItem(createDB(r));
  }

  public BXQItem createItemFromDocument(final Source s, final XQItemType it)
      throws XQException {
    return new BXQItem(createDB(s, it));
  }

  public BXQItem createItemFromDocument(final String v, final String base,
      final XQItemType it) throws XQException {
    valid(v, String.class);
    check(Type.DOC, it);
    return new BXQItem(createDB(new IOContent(Token.token(v))));
  }

  public BXQItem createItemFromDocument(final XMLReader r, final XQItemType it)
      throws XQException {
    check(Type.DOC, it);
    return new BXQItem(createDB(r));
  }

  public BXQItem createItemFromDocument(final XMLStreamReader sr,
      final XQItemType it) throws XQException {
    check(Type.DOC, it);
    return new BXQItem(createDB(sr));
  }

  public BXQItem createItemFromDouble(final double v, final XQItemType it)
      throws XQException {
    check(Type.DBL, it);
    return new BXQItem(Dbl.get(v));
  }

  public BXQItem createItemFromFloat(final float v, final XQItemType it)
      throws XQException {
    check(Type.FLT, it);
    return new BXQItem(Flt.get(v));
  }

  public BXQItem createItemFromInt(final int v, final XQItemType it)
      throws XQException {
    return itr(v, Type.INT, it);
  }

  public BXQItem createItemFromLong(final long v, final XQItemType it)
      throws XQException {
    return itr(v, Type.LNG, it);
  }

  public BXQItem createItemFromNode(final Node v, final XQItemType it)
      throws XQException {
    opened();
    check(Type.DOC, it);
    valid(v, Node.class);

    final ByteArrayOutputStream ba = new ByteArrayOutputStream();
    try {
      new XMLSerializer(ba, null).serialize(v);
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
    return new BXQItem(createDB(new IOContent(ba.toByteArray())));
  }

  public BXQItem createItemFromObject(final Object v, final XQItemType t)
      throws XQException {
    return new BXQItem(create(v, t));
  }
  
  public BXQItem createItemFromShort(final short v, final XQItemType it)
      throws XQException {
    return itr(v, Type.SHR, it);
  }

  public BXQItem createItemFromString(final String v, final XQItemType it)
      throws XQException {
    try {
      final Str val = Str.get(valid(v, String.class));
      return new BXQItem(check(Type.STR, it).e(val, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public BXQItemType createItemType() throws XQException {
    opened();
    return new BXQItemType(Type.ITEM);
  }

  public BXQItemType createNodeType() throws XQException {
    opened();
    return new BXQItemType(Type.NOD);
  }

  public BXQItemType createProcessingInstructionType(final String nm)
      throws XQException {
    opened();
    final QName name = nm == null ? null : new QName(nm);
    return new BXQItemType(Type.PI, name, -1);
  }

  public BXQItemType createSchemaAttributeType(final QName qn, final int it,
      final URI uri) throws XQException {
    opened();
    checkAttr(it);
    return new BXQItemType(Type.ATT, qn, it);
  }

  public BXQItemType createSchemaElementType(final QName qn, final int it,
      final URI uri) throws XQException {
    opened();
    return new BXQItemType(Type.ELM, qn, it);
  }

  public BXQSequence createSequence(final Iterator it) throws XQException {
    opened();
    valid(it, Iterator.class);
    final SeqIter iter = new SeqIter();
    while(it.hasNext()) iter.add(create(it.next(), null));
    return new BXQSequence(iter, this);
  }

  public BXQSequence createSequence(final XQSequence seq) throws XQException {
    opened();
    valid(seq, XQSequence.class);
    try {
      return new BXQSequence(SeqIter.get(((BXQSequence) seq).result), this);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public BXQItemType createSequenceType(final XQItemType it, final int occ)
      throws XQException {
    opened();
    valid(it, XQItemType.class);
    
    if(occ < 1 || occ > 5) throw new BXQException(OCCINV);
    if(occ == XQSequenceType.OCC_EMPTY && it != null ||
        occ == XQSequenceType.OCC_EXACTLY_ONE && it == null)
      throw new BXQException(OCC);

    final Type type = ((BXQItemType) it).getType();
    final QName name = type.node() ? it.getNodeName() : null;
    return new BXQItemType(type, name, it.getBaseType(), occ);
  }

  public BXQItemType createTextType() throws XQException {
    opened();
    return new BXQItemType(Type.TXT);
  }

  /**
   * Returns an integer item.
   * @param v input value
   * @param e expected type
   * @param t target type
   * @return resulting item
   * @throws XQException exception
   */
  private BXQItem itr(final long v, final Type e, final XQItemType t)
      throws XQException {
    try {
      return new BXQItem(check(e, t).e(Itr.get(v), null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }
}
