package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultItem;
import org.basex.query.xquery.item.Item;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * BaseX XQuery result item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQResultItem implements XQResultItem {
  /** Item item. */
  Item item;
  /** BXQConnection connection. */
  BXQConnection conn;

  /**
   * Sets the connection.
   * @param connection
   */
  void setConnection(final BXQConnection connection) {
    conn = connection;
  }

  public XQConnection getConnection() {
    return conn;
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

  public boolean getBoolean() throws XQException {
    try {
      return item.bool();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public byte getByte() throws XQException {
    try {
      return (byte) item.itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public double getDouble() throws XQException {
    try {
      return item.dbl();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public float getFloat() throws XQException {
    try {
      return item.flt();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public int getInt() throws XQException {
    try {
      return (int) item.itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XMLStreamReader getItemAsStream() {
    return null;
  }

  public String getItemAsString(final Properties props) {
    return Token.string(item.str());
  }

  public XQItemType getItemType() {
    return null;
  }

  public long getLong() throws XQException {
    try {
      return item.itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public Node getNode() {
    return item.node() ? (Node) item : null;
  }

  public URI getNodeUri() {
    return null;
  }

  public Object getObject() {
    return null;
  }

  public short getShort() throws XQException {
    try {
      return (short) item.itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean instanceOf(final XQItemType it) {
    return item.type.instance(((BXQItemType) it).type);
  }

  public void writeItem(final OutputStream os, final Properties props) {
  }

  public void writeItem(final Writer w, final Properties props) {
  }

  public void writeItemToResult(final Result result) {
  }

  public void writeItemToSAX(final ContentHandler saxhdlr) {
  }
}
