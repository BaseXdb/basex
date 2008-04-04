package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import org.basex.api.xqj.javax.XQException;
import org.basex.api.xqj.javax.XQItem;
import org.basex.api.xqj.javax.XQItemType;
import org.basex.api.xqj.javax.XQPreparedExpression;
import org.basex.api.xqj.javax.XQResultSequence;
import org.basex.api.xqj.javax.XQSequence;
import org.basex.api.xqj.javax.XQSequenceType;
import org.basex.api.xqj.javax.XQStaticContext;
import org.basex.query.QueryProcessor;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.SAXSerializer;
import org.basex.api.xqj.BXQStaticContext;
import java.io.IOException;
import org.basex.query.QueryException;

import org.w3c.dom.Node;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

public class BXQPreparedExpression implements XQPreparedExpression  {
  
  QueryProcessor query;
  Nodes nodes;
  
  public BXQPreparedExpression(QueryProcessor xquery, Nodes xnodes) {
    this.query = xquery;
    this.nodes = xnodes;
  }
  
  public BXQPreparedExpression() {
    
  }

  public void bindAtomicValue(QName arg0, String arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindBoolean(QName arg0, boolean arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindByte(QName arg0, byte arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, InputStream arg1, String arg2,
      XQItemType arg3) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, Reader arg1, String arg2, XQItemType arg3)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, Source arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, String arg1, String arg2, XQItemType arg3)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, XMLReader arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, XMLStreamReader arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindDouble(QName arg0, double arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindFloat(QName arg0, float arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindInt(QName arg0, int arg1, XQItemType arg2) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindItem(QName arg0, XQItem arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindLong(QName arg0, long arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindNode(QName arg0, Node arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindObject(QName arg0, Object arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindSequence(QName arg0, XQSequence arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindShort(QName arg0, short arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void bindString(QName arg0, String arg1, XQItemType arg2)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void cancel() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void close() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public BXQResultSequence executeQuery(BXQStaticContext sc) throws XQException, IOException, SAXException, QueryException {
    // TODO Auto-generated method stub
    BXQResultSequence resultn = new BXQResultSequence();
 // execute query
    Result result = this.query.query(this.nodes);
 // create XML reader
    final XMLReader reader = new SAXSerializer(result);
    // set this class as content handler
    reader.setContentHandler(new BXQStaticContext());
    // start parser
    reader.parse("");
    return resultn;
  }
  
  public BXQResultSequence executeQuery() throws XQException {
    // TODO Auto-generated method stub
    BXQResultSequence result = new BXQResultSequence();
    return result;
  } 

  public QName[] getAllExternalVariables() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public QName[] getAllUnboundExternalVariables() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public TimeZone getImplicitTimeZone() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQStaticContext getStaticContext() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType getStaticResultType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType getStaticVariableType(QName arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isClosed() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setImplicitTimeZone(TimeZone arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }
}
