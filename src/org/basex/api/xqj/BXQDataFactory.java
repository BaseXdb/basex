package org.basex.api.xqj;

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
import org.basex.BaseX;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * Java XQuery API - Data Factory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXQDataFactory extends BXQClose implements XQDataFactory {
  /** Database connection. */
  protected BXQStaticContext ctx;

  /**
   * Constructor.
   * @param c project context
   */
  public BXQDataFactory(final Context c) {
    super(null);
    ctx = new BXQStaticContext(c);
  }

  public XQItemType createAtomicType(final int it, final QName qn,
      final URI uri) throws XQException {
    check();
    return new BXQItemType(it);
  }

  public XQItemType createAtomicType(final int it) throws XQException {
    check();
    return new BXQItemType(it);
  }

  public XQItemType createAttributeType(final QName qn, final int arg1,
      final QName qn2, final URI uri) throws XQException {
    check();
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createAttributeType(final QName qn, final int arg1)
      throws XQException {
    check();
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createCommentType() throws XQException {
    check();
    return new BXQItemType(Type.COM);
  }

  public XQItemType createDocumentElementType(final XQItemType it)
      throws XQException {
    check();
    return new BXQItemType(Type.ELM);
  }

  public XQItemType createDocumentSchemaElementType(final XQItemType it)
      throws XQException {
    check();
    return new BXQItemType(Type.ELM);
  }

  public XQItemType createDocumentType() throws XQException {
    check();
    return new BXQItemType(Type.DOC);
  }

  public XQItemType createElementType(final QName qn, final int arg1,
      final QName qn2, final URI uri, final boolean arg4) throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQItemType createElementType(final QName qn, final int arg1)
      throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQItem createItem(final XQItem v) throws XQException {
    check();
    check(v);
    try {
      final Type type = ((BXQItemType) v.getItemType()).type;
      return new BXQItem(type.e(((BXQItem) v).it, null));
    } catch(org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XQItem createItemFromAtomicValue(final String v,
      final XQItemType it) throws XQException {
    check(v);
    try {
      return new BXQItem(check(it, Type.STR).e(Str.get(Token.token(v)), null));
    } catch(org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XQItem createItemFromBoolean(final boolean v, final XQItemType it)
      throws XQException {
    check(it, Type.BLN);
    return new BXQItem(Bln.get(v));
  }

  public XQItem createItemFromByte(final byte v, final XQItemType it)
      throws XQException {
    return new BXQItem(new Itr(v, check(it, Type.BYT)));
  }

  public XQItem createItemFromDocument(final InputStream is,
      final String base, final XQItemType it) throws XQException {
    return createItemFromDocument(content(is), base, it);
  }

  public XQItem createItemFromDocument(final Reader r, final String base,
      final XQItemType it) throws XQException {
    return createItemFromDocument(content(r), base, it);
  }

  public XQItem createItemFromDocument(final Source s, final XQItemType it)
      throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQItem createItemFromDocument(final String v, final String base,
      final XQItemType it) throws XQException {
    check(it, Type.DOC);
    check(v);
    final IO tmp = new IO(Token.token(v), "tmp");
    final Data data = CreateDB.xml(tmp, "tmp");
    check(data);
    return new BXQItem(new DNode(data, 0, null, Type.DOC));
  }

  public XQItem createItemFromDocument(final XMLReader r, final XQItemType it)
      throws XQException {
    check(r);
    final IO tmp = new IO(Token.EMPTY, "x");
    final Data data = CreateDB.xml(new SAXWrapper(tmp, r), "x");
    check(data);
    return new BXQItem(new DNode(data, 0, null, Type.DOC));
  }

  public XQItem createItemFromDocument(final XMLStreamReader sr,
      final XQItemType it) throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQItem createItemFromDouble(final double v, final XQItemType it)
      throws XQException {
    check(it, Type.DBL);
    return new BXQItem(Dbl.get(v));
  }

  public XQItem createItemFromFloat(final float v, final XQItemType it)
      throws XQException {
    check(it, Type.FLT);
    return new BXQItem(Flt.get(v));
  }

  public XQItem createItemFromInt(final int v, final XQItemType it)
      throws XQException {
    return new BXQItem(new Itr(v, check(it, Type.INT)));
  }

  public XQItem createItemFromLong(final long v, final XQItemType it)
      throws XQException {
    return new BXQItem(new Itr(v, check(it, Type.LNG)));
  }

  public XQItem createItemFromNode(final Node v, final XQItemType it)
      throws XQException {
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQItem createItemFromObject(final Object v, final XQItemType it)
      throws XQException {
    check(v);
    return createItemFromAtomicValue(v.toString(), it);
  }

  public XQItem createItemFromShort(final short v, final XQItemType it)
      throws XQException {
    return new BXQItem(new Itr(v, check(it, Type.SHR)));
  }

  public XQItem createItemFromString(final String v, final XQItemType it)
      throws XQException {
    return createItemFromAtomicValue(v, it);
  }

  public XQItemType createItemType() throws XQException {
    check();
    return new BXQItemType(Type.ITEM);
  }

  public XQItemType createNodeType() throws XQException {
    check();
    return new BXQItemType(Type.NOD);
  }

  public XQItemType createProcessingInstructionType(final String nm)
      throws XQException {
    check();
    return new BXQItemType(Type.PI, nm);
  }

  public XQItemType createSchemaAttributeType(final QName qn, final int arg1,
      final URI uri) throws XQException {
    check();
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createSchemaElementType(final QName qn, final int arg1,
      final URI uri) throws XQException {
    check();
    return new BXQItemType(Type.ELM);
  }

  public XQSequence createSequence(final Iterator it) throws XQException {
    check(it);
    check();
    BaseX.notimplemented();
    return null;
  }

  public XQSequence createSequence(final XQSequence seq) throws XQException {
    check();
    return seq;
  }

  public XQSequenceType createSequenceType(final XQItemType it, final int occ)
      throws XQException {
    check();
    return new BXQItemType(Type.SEQ, occ);
  }

  public XQItemType createTextType() throws XQException {
    check();
    return new BXQItemType(Type.TXT);
  }
}
