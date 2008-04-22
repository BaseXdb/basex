package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import org.basex.query.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultItem;
import org.basex.api.xqj.BXQException;
import org.basex.query.xquery.item.Item;
//import org.basex.query.xquery.item.Type;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * BaseX XQuery result item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQResultItem implements XQResultItem {
  
  /** Item item. */
  Item item;
  /** BXQConnection connection. */
  BXQConnection connection;
  
  /**
   * Sets the connection.
   * @param connection
   */
  void setConnection(BXQConnection connection) {
    this.connection = connection;
  }

  public XQConnection getConnection() {
    return connection;
  }

  public void close() {
   item = null;
  }

  public boolean isClosed() {
    return item == null;
  }

  public String getAtomicValue() {
      return item.toString();
  }

  public boolean getBoolean() throws BXQException {
    try {
      return item.bool();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public byte getByte() {
    return 0;
    }

  public double getDouble() throws BXQException {
    try {
      return item.dbl();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public float getFloat() throws BXQException {
    try {
      return item.flt();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public int getInt() throws BXQException {
    try {
      return (int) item.itr();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XMLStreamReader getItemAsStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getItemAsString(Properties props) {
    // TODO Auto-generated method stub
    return null;
  }

  public XQItemType getItemType() {
    // TODO Auto-generated method stub
    return null;
  }

  public long getLong() throws BXQException {
    try {
      return item.itr();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public Node getNode() {
    if(item.node()) {
      return (Node) item;
    }
    return null;
  }

  public URI getNodeUri() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public short getShort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean instanceOf(XQItemType type) {
    // TODO Auto-generated method stub
    return false;
  }

  public void writeItem(OutputStream os, Properties props) {
  // TODO Auto-generated method stub

  }

  public void writeItem(Writer ow, Properties props) {
  // TODO Auto-generated method stub

  }

  public void writeItemToResult(Result result) {
  // TODO Auto-generated method stub
    
  }

  public void writeItemToSAX(ContentHandler saxhdlr) {
  // TODO Auto-generated method stub

  }

}
