package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * BaseX XQuery result sequence.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXQResultSequence implements XQResultSequence {
  /** Forward-Only error message. */
  private static final String FORWARD = "Sequence is forwards-only";
  /** Query context. */
  private final XQContext context;
  /** Result iterator. */
  private final Iter result;
  /** Current result. */
  Item item;
  /** Iterator position. */
  private int pos;
  /** Closed flag. */
  private boolean closed;
  /** BXQConnection. */
  private BXQConnection connection;
  
  /**
   * Constructor.
   * @param item result item
   * @param ctx query context
   */
  public BXQResultSequence(XQContext ctx, Iter item) {
    context = ctx;
    result = item;
  }

  public boolean absolute(int arg0) throws BXQException {
    throw new BXQException(FORWARD);
  }

  public void afterLast() throws BXQException {
    checkIfClosed();
    pos = -1;
  }

  public void beforeFirst() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public void close() {
    closed = true;
  }

  public int count() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean first() throws BXQException {
    throw new BXQException(FORWARD);
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
    return connection;
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

  public String getItemAsString(Properties arg0) throws BXQException {
    checkIfClosed();
    try {
      final CachedOutput co = new CachedOutput();
      final XMLSerializer ps = new XMLSerializer(co);
      item.serialize(ps, context, 0);
      return co.toString();
    } catch(final Exception ex) {
      throw new BXQException(ex.getMessage());
    }
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

  public URI getNodeUri() throws BXQException {
    if(item.node()) {
      Node node = (Node) item;
      try {
        return new URI(node.getBaseURI());
      } catch(URISyntaxException ex) {
        throw new BXQException(ex.toString());
      }
    }
    return null;
  }

  public Object getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getPosition() {
    return pos;
  }

  public XMLStreamReader getSequenceAsStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSequenceAsString(Properties arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public short getShort() throws BXQException {
    try {
      return (short) Type.SHR.e(item, context).itr();
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean instanceOf(XQItemType arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isAfterLast() throws BXQException {
    throw new BXQException(FORWARD);
   }

  public boolean isBeforeFirst() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isFirst() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean isLast() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean isOnItem() throws BXQException {
    checkIfClosed();
    return pos > 0;
  }

  public boolean isScrollable() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean last() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean next() throws BXQException {
    checkIfClosed();
    if(pos < 0) return false;

    try {
      item = result.next();
      if(item == null) {
        pos = -1;
        return false;
      } else {
        pos++;
        return true;
      }
    } catch(XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean previous() throws BXQException {
    throw new BXQException(FORWARD);
  }

  public boolean relative(int arg0) throws BXQException {
    throw new BXQException(FORWARD);
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
  
  private void checkIfClosed() throws BXQException {
    if(closed) throw new BXQException("The XQSequence has been closed");
  }
}
