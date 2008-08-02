package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import org.basex.BaseX;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * BaseX XQuery item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXQItem implements XQItem {
  /** Item. */
  private final Item it;
  
  /**
   * Constructor.
   * @param item item
   */
  public BXQItem(final Item item) {
    it = item;
  }
  
  public void close() {
  }

  public boolean isClosed() {
    return false;
  }

  public String getAtomicValue() {
    return null;
  }

  public boolean getBoolean() throws XQException {
    try {
      return check(Type.BLN).bool();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public byte getByte() throws XQException {
    try {
      return (byte) check(Type.BYT).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public double getDouble() throws XQException {
    try {
      return check(Type.DBL).dbl();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public float getFloat() throws XQException {
    try {
      return check(Type.FLT).flt();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param type expected type
   * @return item
   * @throws XQException xquery exception
   */
  private Item check(final Type type) throws XQException {
    if(!it.type.instance(type)) throw new BXQException(
        BaseX.info("Wrong data type; % expected, % found", it.type, type));
    return it;
  }

  public int getInt() throws XQException {
    try {
      return (int) check(Type.ITR).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XMLStreamReader getItemAsStream() {
   return null;
  }

  public String getItemAsString(Properties props) {
   return null;
  }

  public XQItemType getItemType() {
   return null;
  }

  public long getLong() throws XQException {
    try {
      return check(Type.LNG).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public Node getNode() {
   return null;
  }

  public URI getNodeUri() {
   return null;
  }

  public Object getObject() {
   return null;
  }

  public short getShort() throws XQException {
    try {
      return (short) check(Type.SHR).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean instanceOf(XQItemType type) {
    return false;
  }

  public void writeItem(OutputStream os, Properties props) {
  }

  public void writeItem(Writer ow, Properties props) {
  }

  public void writeItemToResult(Result result) {
  }

  public void writeItemToSAX(ContentHandler saxhdlr) {
  }
}
