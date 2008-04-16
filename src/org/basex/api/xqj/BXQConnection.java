package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import javax.xml.xquery.XQException;
import org.basex.core.Context;
import org.basex.query.xquery.XQueryProcessor;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * BaseX connection.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQConnection implements XQConnection {
  /** BaseX context. */
  Context ctx;
  /** Boolean Value if connection is closed. */
  private boolean closed;
  /** Boolean Value if autoCommit is enabled. Default disabled */
  private boolean autoCommit = false;

  /**
   * Constructor.
   * @param c context instance
   */
  public BXQConnection(final Context c) {
    ctx = c;
  }

  public void close() {
    closed = true;
  }

  public void commit() {
    // TODO Auto-generated method stub  
  }

  public XQItemType createAtomicType(int arg0, QName arg1, URI arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAtomicType(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAttributeType(QName arg0, int arg1, QName arg2,
      URI arg3) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAttributeType(QName arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createCommentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentElementType(XQItemType arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentSchemaElementType(XQItemType arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createElementType(QName arg0, int arg1, QName arg2,
      URI arg3, boolean arg4) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createElementType(QName arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQExpression createExpression() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQExpression createExpression(XQStaticContext arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItem(XQItem arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromAtomicValue(String arg0, XQItemType arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromBoolean(boolean arg0, XQItemType arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromByte(byte arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(InputStream arg0, String arg1,
      XQItemType arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(Reader arg0, String arg1, XQItemType arg2)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(Source arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(String arg0, String arg1, XQItemType arg2)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(XMLReader arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(XMLStreamReader arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDouble(double arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromFloat(float arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromInt(int arg0, XQItemType arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromLong(long arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromNode(Node arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromObject(Object arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromShort(short arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromString(String arg0, XQItemType arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createItemType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createNodeType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createProcessingInstructionType(String arg0)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createSchemaAttributeType(QName arg0, int arg1, URI arg2)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createSchemaElementType(QName arg0, int arg1, URI arg2)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequence createSequence(Iterator arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequence createSequence(XQSequence arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType createSequenceType(XQItemType arg0, int arg1)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createTextType() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean getAutoCommit() {
    return autoCommit;
  }

  public XQMetaData getMetaData() {
    // TODO Auto-generated method stub
    return null;
  }

  public BXQStaticContext getStaticContext() {
    // TODO Auto-generated method stub
    return new BXQStaticContext(ctx);
  }

  public boolean isClosed() {
    return closed;
  }

  public XQPreparedExpression prepareExpression(InputStream arg0,
      XQStaticContext arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(InputStream arg0)
      {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(Reader arg0,
      XQStaticContext arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(Reader arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public BXQPreparedExpression prepareExpression(String query,
      XQStaticContext sc) {

    // create query instance
    XQueryProcessor xquery = new XQueryProcessor(query);
    // create context set, referring to the root node (0)
    return new BXQPreparedExpression(xquery, ctx.current());
  }
  
  public XQPreparedExpression prepareExpression(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public void rollback() throws XQException {
    if (closed) {
      throw new XQException("Connection has been closed");
      }
  }

  public void setAutoCommit(boolean arg0) {
    autoCommit = arg0;
  }

  public void setStaticContext(XQStaticContext arg0) {
    // TODO Auto-generated method stub
  }
}
