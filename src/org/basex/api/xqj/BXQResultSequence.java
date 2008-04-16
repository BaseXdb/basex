package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * BaseX result sequence.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQResultSequence implements XQResultSequence {
  /** Query context. */
  XQContext context;
  /** Result iterator. */
  Iter result;
  /** Current result. */
  Item item;
  /** Position of the iterator. */
  private int position;
  /** Boolean value if closed. */
  private boolean closed;
  
  /**
   * Constructor.
   * @param item result item
   * @param ctx query context
   */
  public BXQResultSequence(XQContext ctx, Iter item) {
    context = ctx;
    result = item;
  }

  public boolean absolute(int arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public void afterLast() {
    position = -1;
  }

  public void beforeFirst() {
    position = 0;
  }

  public void close() {
    closed = true;
  }

  public int count() {
    return 0;
  }

  public boolean first() {
    return false;
  }

  public String getAtomicValue() throws BXQException {
    if(item.node()) throw new BXQException("Current item is a node.");
    return Token.string(item.str());
  }

  public boolean getBoolean() throws BXQException {
    try {
      return item.bool();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public byte getByte() throws BXQException {
    try {
      return (byte) Type.BYT.e(item, context).itr();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XQConnection getConnection() {
    // TODO Auto-generated method stub
    return null;
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

  public XQItem getItem() {
    // TODO Auto-generated method stub
    return null;
  }

  public XMLStreamReader getItemAsStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getItemAsString(Properties arg0) {
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
    // TODO Auto-generated method stub
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

  public int getPosition() {
    return position;
  }

  public XMLStreamReader getSequenceAsStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSequenceAsString(Properties arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public short getShort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean instanceOf(XQItemType arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isAfterLast() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isBeforeFirst() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isFirst() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isLast() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isOnItem() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isScrollable() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean last() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean next() throws BXQException {
    try {
      item = result.next();
      return item != null;
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean previous() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean relative(int arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public void writeItem(OutputStream arg0, Properties arg1) {
    // TODO Auto-generated method stub
    
  }

  public void writeItem(Writer arg0, Properties arg1) {
    // TODO Auto-generated method stub
    
  }

  public void writeItemToResult(Result arg0) {
    // TODO Auto-generated method stub
    
  }

  public void writeItemToSAX(ContentHandler arg0) {
    // TODO Auto-generated method stub
    
  }

  public void writeSequence(OutputStream arg0, Properties arg1) {
    // TODO Auto-generated method stub
    
  }

  public void writeSequence(Writer arg0, Properties arg1) {
    // TODO Auto-generated method stub
    
  }

  public void writeSequenceToResult(Result arg0) {
    // TODO Auto-generated method stub
    
  }

  public void writeSequenceToSAX(ContentHandler arg0) {
    // TODO Auto-generated method stub
    
  }
}
