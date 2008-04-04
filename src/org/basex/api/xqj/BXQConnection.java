package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import org.basex.api.xqj.javax.XQConnection;
import org.basex.api.xqj.javax.XQException;
import org.basex.api.xqj.javax.XQExpression;
import org.basex.api.xqj.javax.XQItem;
import org.basex.api.xqj.javax.XQItemType;
import org.basex.api.xqj.javax.XQMetaData;
import org.basex.api.xqj.javax.XQPreparedExpression;
import org.basex.api.xqj.javax.XQSequence;
import org.basex.api.xqj.javax.XQSequenceType;
import org.basex.api.xqj.javax.XQStaticContext;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryProcessor;
import org.basex.query.xquery.XQueryProcessor;

import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

public class BXQConnection implements XQConnection {

  public void close() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void commit() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public XQItemType createAtomicType(int arg0, QName arg1, URI arg2)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAtomicType(int arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAttributeType(QName arg0, int arg1, QName arg2,
      URI arg3) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createAttributeType(QName arg0, int arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createCommentType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentElementType(XQItemType arg0)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentSchemaElementType(XQItemType arg0)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createDocumentType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createElementType(QName arg0, int arg1, QName arg2,
      URI arg3, boolean arg4) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createElementType(QName arg0, int arg1) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQExpression createExpression() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQExpression createExpression(XQStaticContext arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItem(XQItem arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromAtomicValue(String arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromBoolean(boolean arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromByte(byte arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(InputStream arg0, String arg1,
      XQItemType arg2) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(Reader arg0, String arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(Source arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(String arg0, String arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(XMLReader arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDocument(XMLStreamReader arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromDouble(double arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromFloat(float arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromInt(int arg0, XQItemType arg1) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromLong(long arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromNode(Node arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromObject(Object arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromShort(short arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItem createItemFromString(String arg0, XQItemType arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createItemType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createNodeType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createProcessingInstructionType(String arg0)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createSchemaAttributeType(QName arg0, int arg1, URI arg2)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createSchemaElementType(QName arg0, int arg1, URI arg2)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequence createSequence(Iterator arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequence createSequence(XQSequence arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType createSequenceType(XQItemType arg0, int arg1)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType createTextType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean getAutoCommit() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public XQMetaData getMetaData() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public BXQStaticContext getStaticContext() throws XQException {
    // TODO Auto-generated method stub
    return new BXQStaticContext();
  }

  public boolean isClosed() {
    // TODO Auto-generated method stub
    return false;
  }

  public XQPreparedExpression prepareExpression(InputStream arg0,
      XQStaticContext arg1) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(InputStream arg0)
      throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(Reader arg0,
      XQStaticContext arg1) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQPreparedExpression prepareExpression(Reader arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public BXQPreparedExpression prepareExpression(String arg0,
      XQStaticContext arg1) throws XQException {
    // TODO Auto-generated method stub
    return new BXQPreparedExpression();
  }
  
  public BXQPreparedExpression prepareExpression(String arg0, Data data) throws XQException {
    // create query instance
    QueryProcessor xquery = new XQueryProcessor(arg0);
    // create context set, referring to the root node (0)
    Nodes nodes = new Nodes(0, data);
    return new BXQPreparedExpression(xquery, nodes);
  }

  public XQPreparedExpression prepareExpression(String arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public void rollback() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setAutoCommit(boolean arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void setStaticContext(XQStaticContext arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }
}
