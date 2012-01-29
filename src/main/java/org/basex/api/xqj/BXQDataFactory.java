package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.ByteArrayOutputStream;
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
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Flt;
import org.basex.query.item.Int;
import org.basex.query.item.NodeType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.ItemCache;
import org.basex.util.Token;
import org.basex.util.Util;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;

/**
 * Java XQuery API - Data Factory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
class BXQDataFactory extends BXQAbstract implements XQDataFactory {

  /**
   * Constructor, specifying a user name and password.
   * @param name user name
   * @param pw password
   * @throws XQException if authentication fails
   */
  protected BXQDataFactory(final String name, final String pw)
      throws XQException {
    super(null);
    context = new BXQStaticContext(name, pw);
  }

  @Override
  public BXQItemType createAtomicType(final int b, final QName qn,
      final URI uri) throws XQException {
    return createAtomicType(b);
  }

  @Override
  public BXQItemType createAtomicType(final int b) throws XQException {
    opened();
    return new BXQItemType(b);
  }

  @Override
  public BXQItemType createAttributeType(final QName qn, final int it,
      final QName qn2, final URI uri) throws XQException {
    return createAttributeType(qn, it);
  }

  @Override
  public BXQItemType createAttributeType(final QName qn, final int it)
      throws XQException {
    opened();
    checkAttr(it);
    return new BXQItemType(NodeType.ATT, qn, it);
  }

  @Override
  public BXQItemType createCommentType() throws XQException {
    opened();
    return new BXQItemType(NodeType.COM);
  }

  @Override
  public BXQItemType createDocumentElementType(final XQItemType it)
      throws XQException {
    opened();
    valid(it, XQItemType.class);
    if(it.getItemKind() != XQItemType.XQITEMKIND_ELEMENT)
      throw new BXQException(ELM);

    return new BXQItemType(NodeType.DEL, it.getNodeName(), it.getBaseType());
  }

  @Override
  public BXQItemType createDocumentSchemaElementType(final XQItemType it)
      throws XQException {
    opened();
    valid(it, XQItemType.class);
    if(it.getItemKind() != XQItemType.XQITEMKIND_SCHEMA_ELEMENT)
      throw new BXQException(ELM);

    return new BXQItemType(NodeType.DEL, it.getNodeName(), it.getBaseType());
  }

  @Override
  public BXQItemType createDocumentType() throws XQException {
    opened();
    return new BXQItemType(NodeType.DOC);
  }

  @Override
  public BXQItemType createElementType(final QName qn, final int it,
      final QName qn2, final URI uri, final boolean n) throws XQException {
    opened();
    return new BXQItemType(NodeType.ELM, qn, it);
  }

  @Override
  public BXQItemType createElementType(final QName qn, final int it)
      throws XQException {
    return createElementType(qn, it, null, null, false);
  }

  @Override
  public BXQItem createItem(final XQItem v) throws XQException {
    opened();
    valid(v, XQItem.class);
    try {
      final Type type = ((BXQItemType) v.getItemType()).getType();
      return new BXQItem(type.cast(((BXQItem) v).it, null, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public BXQItem createItemFromAtomicValue(final String v, final XQItemType it)
      throws XQException {
    valid(it, XQItemType.class);
    try {
      final Str val = Str.get(valid(v, String.class));
      return new BXQItem(check(AtomType.STR, it).cast(val, null, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public BXQItem createItemFromBoolean(final boolean v, final XQItemType it)
      throws XQException {
    check(AtomType.BLN, it);
    return new BXQItem(Bln.get(v));
  }

  @Override
  public BXQItem createItemFromByte(final byte v, final XQItemType it)
      throws XQException {
    return itr(v, AtomType.BYT, it);
  }

  @Override
  public BXQItem createItemFromDocument(final InputStream is,
      final String base, final XQItemType it) throws XQException {
    check(NodeType.DOC, it);
    return new BXQItem(createNode(is));
  }

  @Override
  public BXQItem createItemFromDocument(final Reader r, final String base,
      final XQItemType it) throws XQException {
    check(NodeType.DOC, it);
    return new BXQItem(createNode(r));
  }

  @Override
  public BXQItem createItemFromDocument(final Source s, final XQItemType it)
      throws XQException {
    return new BXQItem(createNode(s, it));
  }

  @Override
  public BXQItem createItemFromDocument(final String v, final String base,
      final XQItemType it) throws XQException {
    valid(v, String.class);
    check(NodeType.DOC, it);
    return new BXQItem(createNode(new IOContent(Token.token(v))));
  }

  @Override
  public BXQItem createItemFromDocument(final XMLStreamReader sr,
      final XQItemType it) throws XQException {
    check(NodeType.DOC, it);
    return new BXQItem(createNode(sr));
  }

  @Override
  public BXQItem createItemFromDouble(final double v, final XQItemType it)
      throws XQException {
    check(AtomType.DBL, it);
    return new BXQItem(Dbl.get(v));
  }

  @Override
  public BXQItem createItemFromFloat(final float v, final XQItemType it)
      throws XQException {
    check(AtomType.FLT, it);
    return new BXQItem(Flt.get(v));
  }

  @Override
  public BXQItem createItemFromInt(final int v, final XQItemType it)
      throws XQException {
    return itr(v, AtomType.INT, it);
  }

  @Override
  public BXQItem createItemFromLong(final long v, final XQItemType it)
      throws XQException {
    return itr(v, AtomType.LNG, it);
  }

  @Override
  public BXQItem createItemFromNode(final Node v, final XQItemType it)
      throws XQException {
    opened();
    check(NodeType.DOC, it);
    valid(v, Node.class);

    final ByteArrayOutputStream ba = new ByteArrayOutputStream();
    try {
      final DOMImplementationRegistry registry =
        DOMImplementationRegistry.newInstance();
      final DOMImplementationLS impl =
          (DOMImplementationLS) registry.getDOMImplementation("LS");
      final LSOutput output = impl.createLSOutput();
      output.setByteStream(ba);
      impl.createLSSerializer().write(v, output);
    } catch(final Exception ex) {
      Util.stack(ex);
    }
    return new BXQItem(createNode(new IOContent(ba.toByteArray())));
  }

  @Override
  public BXQItem createItemFromObject(final Object v, final XQItemType t)
      throws XQException {
    return new BXQItem(create(v, t));
  }

  @Override
  public BXQItem createItemFromShort(final short v, final XQItemType it)
      throws XQException {
    return itr(v, AtomType.SHR, it);
  }

  @Override
  public BXQItem createItemFromString(final String v, final XQItemType it)
      throws XQException {
    try {
      final Str val = Str.get(valid(v, String.class));
      return new BXQItem(check(AtomType.STR, it).cast(val, null, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public BXQItemType createItemType() throws XQException {
    opened();
    return new BXQItemType(AtomType.ITEM);
  }

  @Override
  public BXQItemType createNodeType() throws XQException {
    opened();
    return new BXQItemType(NodeType.NOD);
  }

  @Override
  public BXQItemType createProcessingInstructionType(final String nm)
      throws XQException {
    opened();
    final QName name = nm == null ? null : new QName(nm);
    return new BXQItemType(NodeType.PI, name, -1);
  }

  @Override
  public BXQItemType createSchemaAttributeType(final QName qn, final int it,
      final URI uri) throws XQException {
    opened();
    checkAttr(it);
    return new BXQItemType(NodeType.ATT, qn, it);
  }

  @Override
  public BXQItemType createSchemaElementType(final QName qn, final int it,
      final URI uri) throws XQException {
    opened();
    return new BXQItemType(NodeType.ELM, qn, it);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public BXQSequence createSequence(final Iterator it) throws XQException {
    opened();
    valid(it, Iterator.class);
    final ItemCache ic = new ItemCache();
    while(it.hasNext()) ic.add(create(it.next(), null));
    return new BXQSequence(ic, this);
  }

  @Override
  public BXQSequence createSequence(final XQSequence seq) throws XQException {
    opened();
    valid(seq, XQSequence.class);
    final BXQSequence s = (BXQSequence) seq;
    s.opened();
    try {
      return new BXQSequence(s.result.value().cache(), this);
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public BXQItemType createSequenceType(final XQItemType it, final int occ)
      throws XQException {
    opened();
    valid(it, XQItemType.class);

    if(occ < 1 || occ > 5) throw new BXQException(OCCINV);
    if(occ == XQSequenceType.OCC_EMPTY && it != null ||
        occ == XQSequenceType.OCC_EXACTLY_ONE && it == null)
      throw new BXQException(OCC);

    final Type type = ((BXQItemType) it).getType();
    final QName name = type.isNode() ? it.getNodeName() : null;
    return new BXQItemType(type, name, it.getBaseType(), occ);
  }

  @Override
  public BXQItemType createTextType() throws XQException {
    opened();
    return new BXQItemType(NodeType.TXT);
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
      return new BXQItem(check(e, t).cast(Int.get(v), null, null));
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }
}
