package org.basex.api.xqj;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.xquery.XQContext;
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
public final class BXQResultSequence implements XQResultSequence {
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
  public BXQResultSequence(final XQContext ctx, final Iter item) {
    context = ctx;
    result = item;
  }

  public boolean absolute(final int arg0) throws XQException {
    throw new BXQException(FORWARD);
  }

  public void afterLast() throws XQException {
    checkIfClosed();
    pos = -1;
  }

  public void beforeFirst() throws XQException {
    throw new BXQException(FORWARD);
  }

  public void close() {
    closed = true;
  }

  public int count() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean first() throws XQException {
    throw new BXQException(FORWARD);
  }

  public String getAtomicValue() throws XQException {
    if(item.node()) throw new BXQException("Current item is a node.");
    return Token.string(item.str());
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
      return (byte) Type.BYT.e(item, context).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public XQConnection getConnection() {
    return connection;
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

  public XQItem getItem() {
    return null;
  }

  public XMLStreamReader getItemAsStream() {
    return null;
  }

  public String getItemAsString(final Properties arg0) throws XQException {
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

  public URI getNodeUri() throws XQException {
    if(item.node()) {
      final Node node = (Node) item;
      try {
        return new URI(node.getBaseURI());
      } catch(final URISyntaxException ex) {
        throw new BXQException(ex.toString());
      }
    }
    return null;
  }

  public Object getObject() {
    return null;
  }

  public int getPosition() {
    return pos;
  }

  public XMLStreamReader getSequenceAsStream() {
    return null;
  }

  public String getSequenceAsString(final Properties arg0) {
    return null;
  }

  public short getShort() throws XQException {
    try {
      return (short) Type.SHR.e(item, context).itr();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean instanceOf(final XQItemType it) {
    return item.type.instance(((BXQItemType) it).type);
  }

  public boolean isAfterLast() throws XQException {
    throw new BXQException(FORWARD);
   }

  public boolean isBeforeFirst() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isFirst() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean isLast() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean isOnItem() throws XQException {
    checkIfClosed();
    return pos > 0;
  }

  public boolean isScrollable() {
    return false;
  }

  public boolean last() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean next() throws XQException {
    checkIfClosed();
    if(pos < 0) return false;

    try {
      item = result.next();
      pos++;
      boolean more = item != null;
      if(!more) pos = -1;
      return more;
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean previous() throws XQException {
    throw new BXQException(FORWARD);
  }

  public boolean relative(final int arg0) throws XQException {
    throw new BXQException(FORWARD);
  }

  public void writeItem(final OutputStream arg0, final Properties arg1) {
  }

  public void writeItem(final Writer arg0, final Properties arg1) {
  }

  public void writeItemToResult(final Result arg0) {
  }

  public void writeItemToSAX(final ContentHandler arg0) {
  }

  public void writeSequence(final OutputStream arg0, final Properties arg1) {
  }

  public void writeSequence(final Writer arg0, final Properties arg1) {
  }

  public void writeSequenceToResult(final Result arg0) {
  }

  public void writeSequenceToSAX(final ContentHandler arg0) {
  }

  private void checkIfClosed() throws XQException {
    if(closed) throw new BXQException("The XQSequence has been closed");
  }
}
