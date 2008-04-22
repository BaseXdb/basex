package org.basex.api.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import javax.xml.xquery.XQStaticContext;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.iter.Iter;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

/**
 * BaseX XQuery prepared expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQPreparedExpression implements XQPreparedExpression  {
  private XQueryProcessor query;
  private Nodes nodes;
  private boolean closed;
  
  /**
   * Constructor.
   * @param xquery query instance
   * @param xnodes node reference
   */
  public BXQPreparedExpression(XQueryProcessor xquery, Nodes xnodes) {
    this.query = xquery;
    this.nodes = xnodes;
  }
  
  public void bindAtomicValue(QName arg0, String arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindBoolean(QName arg0, boolean arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindByte(QName arg0, byte arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, InputStream arg1, String arg2,
      XQItemType arg3) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, Reader arg1, String arg2,
      XQItemType arg3) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, Source arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, String arg1, String arg2,
      XQItemType arg3) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, XMLReader arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindDocument(QName arg0, XMLStreamReader arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindDouble(QName arg0, double arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindFloat(QName arg0, float arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindInt(QName arg0, int arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindItem(QName arg0, XQItem arg1) {
    // TODO Auto-generated method stub
    
  }

  public void bindLong(QName arg0, long arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindNode(QName arg0, Node arg1, XQItemType arg2){
    // TODO Auto-generated method stub
    
  }

  public void bindObject(QName arg0, Object arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindSequence(QName arg0, XQSequence arg1) {
    // TODO Auto-generated method stub
    
  }

  public void bindShort(QName arg0, short arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void bindString(QName arg0, String arg1, XQItemType arg2) {
    // TODO Auto-generated method stub
    
  }

  public void cancel() {
    // TODO Auto-generated method stub
    
  }

  public void close() {
    closed = true;
  }

  public BXQResultSequence executeQuery() throws XQException {
    try {
      query.create();
      XQContext ctx = query.ctx;
      Iter iter = ctx.compile(nodes).iter();
      return new BXQResultSequence(ctx, iter);
    } catch(QueryException ex) {
      throw new XQQueryException(ex.getMessage());
    }
  } 

  public QName[] getAllExternalVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  public QName[] getAllUnboundExternalVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  public TimeZone getImplicitTimeZone() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQStaticContext getStaticContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType getStaticResultType() {
    // TODO Auto-generated method stub
    return null;
  }

  public XQSequenceType getStaticVariableType(QName arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setImplicitTimeZone(TimeZone arg0) {
    // TODO Auto-generated method stub
    
  }
}
