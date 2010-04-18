package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;
import org.basex.core.Main;
import org.basex.data.SAXSerializer;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Java XQuery API - Result Sequence.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class BXQSequence extends BXQAbstract implements XQResultSequence {
  /** Result iterator. */
  final Iter result;
  /** Current result. */
  private BXQItem it;
  /** Iterator position. */
  private int pos;
  /** BXQConnection. */
  private final BXQConnection conn;
  /** Next flag. */
  private boolean next;
  /** Forward flag. */
  private final boolean scrollable;

  /**
   * Constructor.
   * @param item result item
   * @param c closer
   * @throws XQException xquery exception
   */
  BXQSequence(final Iter item, final BXQDataFactory c) throws XQException {
    this(item, c, null);
  }

  /**
   * Constructor.
   * @param item result item
   * @param c closer
   * @param cn connection
   * @throws XQException xquery exception
   */
  BXQSequence(final Iter item, final BXQAbstract c,
      final BXQConnection cn) throws XQException {
    super(c);
    result = item;
    conn = cn;
    scrollable = cn == null || cn.getStaticContext().
      getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE;
  }

  public XQConnection getConnection() throws XQException {
    opened();
    return conn;
  }

  public boolean absolute(final int p) throws XQException {
    final SeqIter seq = sequence();
    cursor(seq, p >= 0 ? p - 1 : seq.size() + p);
    return pos > 0;
  }

  public void afterLast() throws XQException {
    cursor(sequence(), Integer.MAX_VALUE);
  }

  public void beforeFirst() throws XQException {
    cursor(sequence(), -1);
  }

  public int count() throws XQException {
    return sequence().size();
  }

  public boolean first() throws XQException {
    return cursor(sequence(), 0);
  }

  public String getAtomicValue() throws XQException {
    return item().getAtomicValue();
  }

  public boolean getBoolean() throws XQException {
    return item().getBoolean();
  }

  public byte getByte() throws XQException {
    return item().getByte();
  }

  public double getDouble() throws XQException {
    return item().getDouble();
  }

  public float getFloat() throws XQException {
    return item().getFloat();
  }

  public int getInt() throws XQException {
    return item().getInt();
  }

  public XQItem getItem() throws XQException {
    return item();
  }

  public XMLStreamReader getItemAsStream() throws XQException {
    opened();
    return item().getItemAsStream();
  }

  public String getItemAsString(final Properties p) throws XQException {
    return item().getItemAsString(p);
  }

  public XQItemType getItemType() throws XQException {
    pos();
    return it.getItemType();
  }

  public long getLong() throws XQException {
    return item().getLong();
  }

  public Node getNode() throws XQException {
    return item().getNode();
  }

  public URI getNodeUri() throws XQException {
    pos();
    return it.getNodeUri();
  }

  public Object getObject() throws XQException {
    return item().getObject();
  }

  public int getPosition() throws XQException {
    final SeqIter iter = sequence();
    return pos != -1 ? pos : iter.size() + 1;
  }

  public XMLStreamReader getSequenceAsStream() throws XQException {
    opened();
    if(it != null && !next) throw new BXQException(TWICE);
    return new IterStreamReader(result);
  }

  public String getSequenceAsString(final Properties p) throws XQException {
    opened();
    if(it != null && !next) throw new BXQException(TWICE);
    if(!next && !next()) return "";

    final CachedOutput co = new CachedOutput();
    try {
      final XMLSerializer xml = new XMLSerializer(co);
      do {
        final BXQItem item = item();
        item.serialize(item.it, xml);
      } while(next());
      xml.close();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
    return co.toString();
  }

  public short getShort() throws XQException {
    return item().getShort();
  }

  public boolean instanceOf(final XQItemType type) throws XQException {
    pos();
    return it.instanceOf(type);
  }

  public boolean isAfterLast() throws XQException {
    sequence();
    return pos == -1;
   }

  public boolean isBeforeFirst() throws XQException {
    sequence();
    return pos == 0 && pos < sequence().size();
  }

  public boolean isFirst() throws XQException {
    sequence();
    return pos == 1;
  }

  public boolean isLast() throws XQException {
    return pos == sequence().size();
  }

  public boolean isOnItem() throws XQException {
    opened();
    return pos > 0;
  }

  public boolean isScrollable() throws XQException {
    opened();
    return scrollable;
  }

  public boolean last() throws XQException {
    final SeqIter seq = sequence();
    return cursor(seq, seq.size() - 1);
  }

  public boolean next() throws XQException {
    opened();
    if(pos < 0) return false;

    try {
      final Item i = result.next();
      next = i != null;
      pos++;
      it = new BXQItem(i, this, conn);
      if(!next) pos = -1;
      return next;
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  public boolean previous() throws XQException {
    return relative(-1);
  }

  public boolean relative(final int p) throws XQException {
    return cursor(sequence(), getPosition() + p - 1);
  }

  public void writeItem(final OutputStream os, final Properties p)
      throws XQException {
    item().writeItem(os, p);
  }

  public void writeItem(final Writer ow, final Properties p)
      throws XQException {
    item().writeItem(ow, p);
  }

  public void writeItemToResult(final Result r) throws XQException {
    item().writeItemToResult(r);
  }

  public void writeItemToSAX(final ContentHandler sax) throws XQException {
    item().writeItemToSAX(sax);
  }

  public void writeSequence(final OutputStream os, final Properties p)
      throws XQException {
    if(it != null && !next) throw new BXQException(TWICE);
    while(next()) item().writeItem(os, p);
  }

  public void writeSequence(final Writer ow, final Properties p)
      throws XQException {
    if(it != null && !next) throw new BXQException(TWICE);
    while(next()) item().writeItem(ow, p);
  }

  public void writeSequenceToResult(final Result res) throws XQException {
    valid(res, Result.class);
    if(it != null && !next) throw new BXQException(TWICE);

    // evaluate different result types...
    if(res instanceof StreamResult) {
      // StreamResult.. directly write result as string
      final StreamResult sr = (StreamResult) res;
      if(sr.getWriter() != null) writeSequence(sr.getWriter(), null);
      else writeSequence(sr.getOutputStream(), null);
    } else if(res instanceof SAXResult) {
      // SAXResult.. serialize result to underlying parser
      final SAXSerializer ser = new SAXSerializer(null);
      final SAXResult sax = (SAXResult) res;
      ser.setContentHandler(sax.getHandler());
      ser.setLexicalHandler(sax.getLexicalHandler());
      while(next()) serialize(item().it, ser);
    } else {
      Main.notimplemented();
    }
  }

  public void writeSequenceToSAX(final ContentHandler sax) throws XQException {
    valid(sax, ContentHandler.class);
    writeSequenceToResult(new SAXResult(sax));
  }

  /**
   * Checks the specified cursor position.
   * @return item
   * @throws XQException xquery exception
   */
  private BXQItem item() throws XQException {
    pos();
    if(!next) throw new BXQException(TWICE);
    next = scrollable;
    return it;
  }

  /**
   * Checks the specified cursor position.
   * @throws XQException xquery exception
   */
  private void pos() throws XQException {
    if(!isOnItem()) throw new BXQException(CURSOR);
  }

  /**
   * Checks the forward flag and returns the result.
   * @return sequence iterator
   * @throws XQException xquery exception
   */
  private SeqIter sequence() throws XQException {
    opened();
    if(!scrollable) throw new BXQException(FORWARD);
    return (SeqIter) result;
  }

  /**
   * Sets the cursor to the specified position.
   * @param seq iterator sequence
   * @param p cursor position
   * @return result of check
   * @throws XQException xquery exception
   */
  private boolean cursor(final SeqIter seq, final int p) throws XQException {
    pos = p < 0 ? 0 : p >= seq.size() ? -1 : p;
    seq.pos(pos - 1);
    return p < 0 ? false : next();
  }
}
