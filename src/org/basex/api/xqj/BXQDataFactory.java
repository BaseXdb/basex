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
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * BaseX XQuery data factory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BXQDataFactory implements XQDataFactory {

  public XQItemType createAtomicType(final int it, final QName qn,
      final URI uri) {
    return new BXQItemType(it);
  }

  public XQItemType createAtomicType(final int it) {
    return new BXQItemType(it);
  }

  public XQItemType createAttributeType(final QName qn, final int arg1,
      final QName qn2, final URI uri) {
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createAttributeType(final QName qn, final int arg1) {
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createCommentType() {
    return new BXQItemType(Type.COM);
  }

  public XQItemType createDocumentElementType(final XQItemType it) {
    return new BXQItemType(Type.ELM);
  }

  public XQItemType createDocumentSchemaElementType(final XQItemType it) {
    return new BXQItemType(Type.ELM);
  }

  public XQItemType createDocumentType() {
    return new BXQItemType(Type.DOC);
  }

  public XQItemType createElementType(final QName qn, final int arg1,
      final QName qn2, final URI uri, final boolean arg4) {
    return null;
  }

  public XQItemType createElementType(final QName qn, final int arg1) {
    return null;
  }

  public XQItem createItem(final XQItem v) {
    return null;
  }

  public XQItem createItemFromAtomicValue(final String v,
      final XQItemType it) {
    return null;
  }

  public XQItem createItemFromBoolean(final boolean v, final XQItemType it)
      throws XQException {
    check(it, Type.BLN);
    return new BXQItem(Bln.get(v));
  }

  public XQItem createItemFromByte(final byte v, final XQItemType it)
      throws XQException {
    check(it, Type.BYT);
    return new BXQItem(new Itr(v, Type.BYT));
  }

  public XQItem createItemFromDocument(final InputStream is,
      final String arg1, final XQItemType arg2) {
    return null;
  }

  public XQItem createItemFromDocument(final Reader r, final String arg1,
      final XQItemType arg2) {
    return null;
  }

  public XQItem createItemFromDocument(final Source arg0, final XQItemType it) {
    return null;
  }

  public XQItem createItemFromDocument(final String arg0, final String arg1,
      final XQItemType arg2) {
    return null;
  }

  public XQItem createItemFromDocument(final XMLReader r,
      final XQItemType it) {
    return null;
  }

  public XQItem createItemFromDocument(final XMLStreamReader sr,
      final XQItemType it) {
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
    check(it, Type.INT);
    return new BXQItem(new Itr(v, Type.INT));
  }

  public XQItem createItemFromLong(final long v, final XQItemType it)
      throws XQException {
    check(it, Type.LNG);
    return new BXQItem(new Itr(v, Type.LNG));
  }

  public XQItem createItemFromNode(final Node v, final XQItemType it) {
    return null;
  }

  public XQItem createItemFromObject(final Object v, final XQItemType it) {
    return null;
  }

  public XQItem createItemFromShort(final short v, final XQItemType it)
      throws XQException {
    check(it, Type.SHR);
    return new BXQItem(new Itr(v, Type.SHR));
  }

  public XQItem createItemFromString(final String v, final XQItemType it)
      throws XQException {
    check(it, Type.STR);
    return new BXQItem(Str.get(Token.token(v)));
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param it item type
   * @param type expected type
   * @throws XQException xquery exception
   */
  private void check(final XQItemType it, final Type type) throws XQException {
    final BXQItemType bit = (BXQItemType) it;
    if(!type.instance(bit.type)) throw new BXQException(
        BaseX.info("Wrong data type; % expected, % found", bit.type, type));
  }

  public XQItemType createItemType() {
    return new BXQItemType(Type.ITEM);
  }

  public XQItemType createNodeType() {
    return new BXQItemType(Type.NOD);
  }

  public XQItemType createProcessingInstructionType(final String arg0) {
    return new BXQItemType(Type.PI);
  }

  public XQItemType createSchemaAttributeType(final QName qn, final int arg1,
      final URI uri) {
    return new BXQItemType(Type.ATT);
  }

  public XQItemType createSchemaElementType(final QName qn, final int arg1,
      final URI uri) {
    return new BXQItemType(Type.ELM);
  }

  public XQSequence createSequence(final Iterator arg0) {
    return null;
  }

  public XQSequence createSequence(final XQSequence arg0) {
    return null;
  }

  public XQSequenceType createSequenceType(final XQItemType it, final int arg1) {
    return null;
  }

  public XQItemType createTextType() {
    return new BXQItemType(Type.TXT);
  }
}
