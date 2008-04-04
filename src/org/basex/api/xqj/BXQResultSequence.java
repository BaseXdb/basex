package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import org.basex.api.xqj.javax.XQConnection;
import org.basex.api.xqj.javax.XQException;
import org.basex.api.xqj.javax.XQItem;
import org.basex.api.xqj.javax.XQItemType;
import org.basex.api.xqj.javax.XQResultSequence;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public class BXQResultSequence implements XQResultSequence {

  public boolean absolute(int arg0) throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public void afterLast() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void beforeFirst() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void close() throws XQException {
    // TODO Auto-generated method stub
    
  }

  public int count() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean first() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public String getAtomicValue() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean getBoolean() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public byte getByte() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public XQConnection getConnection() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public double getDouble() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public float getFloat() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getInt() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public XQItem getItem() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XMLStreamReader getItemAsStream() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getItemAsString(Properties arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType getItemType() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public long getLong() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public Node getNode() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public URI getNodeUri() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getObject() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public int getPosition() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public XMLStreamReader getSequenceAsStream() throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSequenceAsString(Properties arg0) throws XQException {
    // TODO Auto-generated method stub
    return null;
  }

  public short getShort() throws XQException {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean instanceOf(XQItemType arg0) throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isAfterLast() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isBeforeFirst() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isClosed() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isFirst() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isLast() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isOnItem() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isScrollable() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean last() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean next() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean previous() throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean relative(int arg0) throws XQException {
    // TODO Auto-generated method stub
    return false;
  }

  public void writeItem(OutputStream arg0, Properties arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeItem(Writer arg0, Properties arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeItemToResult(Result arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeItemToSAX(ContentHandler arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeSequence(OutputStream arg0, Properties arg1)
      throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeSequence(Writer arg0, Properties arg1) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeSequenceToResult(Result arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }

  public void writeSequenceToSAX(ContentHandler arg0) throws XQException {
    // TODO Auto-generated method stub
    
  }
}
