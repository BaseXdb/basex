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

import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.SAXSerializer;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.ValueBuilder;
import org.basex.query.iter.Iter;
import org.basex.util.Util;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Java XQuery API - Result Sequence.
 *
 * @author BaseX Team 2005-12, BSD License
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

  @Override
  public XQConnection getConnection() throws XQException {
    opened();
    return conn;
  }

  @Override
  public boolean absolute(final int p) throws XQException {
    final ValueBuilder vb = sequence();
    cursor(vb, p >= 0 ? p - 1 : (int) vb.size() + p);
    return pos > 0;
  }

  @Override
  public void afterLast() throws XQException {
    cursor(sequence(), Integer.MAX_VALUE);
  }

  @Override
  public void beforeFirst() throws XQException {
    cursor(sequence(), -1);
  }

  @Override
  public int count() throws XQException {
    return (int) sequence().size();
  }

  @Override
  public boolean first() throws XQException {
    return cursor(sequence(), 0);
  }

  @Override
  public String getAtomicValue() throws XQException {
    return item().getAtomicValue();
  }

  @Override
  public boolean getBoolean() throws XQException {
    return item().getBoolean();
  }

  @Override
  public byte getByte() throws XQException {
    return item().getByte();
  }

  @Override
  public double getDouble() throws XQException {
    return item().getDouble();
  }

  @Override
  public float getFloat() throws XQException {
    return item().getFloat();
  }

  @Override
  public int getInt() throws XQException {
    return item().getInt();
  }

  @Override
  public XQItem getItem() throws XQException {
    return item();
  }

  @Override
  public XMLStreamReader getItemAsStream() throws XQException {
    opened();
    return item().getItemAsStream();
  }

  @Override
  public String getItemAsString(final Properties p) throws XQException {
    return item().getItemAsString(p);
  }

  @Override
  public XQItemType getItemType() throws XQException {
    pos();
    return it.getItemType();
  }

  @Override
  public long getLong() throws XQException {
    return item().getLong();
  }

  @Override
  public Node getNode() throws XQException {
    return item().getNode();
  }

  @Override
  public URI getNodeUri() throws XQException {
    pos();
    return it.getNodeUri();
  }

  @Override
  public Object getObject() throws XQException {
    return item().getObject();
  }

  @Override
  public int getPosition() throws XQException {
    final ValueBuilder vb = sequence();
    return pos != -1 ? pos : (int) vb.size() + 1;
  }

  @Override
  public XMLStreamReader getSequenceAsStream() throws XQException {
    opened();
    if(it != null && !next) throw new BXQException(TWICE);
    return new IterStreamReader(result);
  }

  @Override
  public String getSequenceAsString(final Properties p) throws XQException {
    opened();
    if(it != null && !next) throw new BXQException(TWICE);
    if(!next && !next()) return "";

    final ArrayOutput ao = new ArrayOutput();
    try {
      final Serializer ser = Serializer.get(ao);
      do {
        final BXQItem item = item();
        item.serialize(item.it, ser);
      } while(next());
      ser.close();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
    return ao.toString();
  }

  @Override
  public short getShort() throws XQException {
    return item().getShort();
  }

  @Override
  public boolean instanceOf(final XQItemType type) throws XQException {
    pos();
    return it.instanceOf(type);
  }

  @Override
  public boolean isAfterLast() throws XQException {
    sequence();
    return pos == -1;
   }

  @Override
  public boolean isBeforeFirst() throws XQException {
    sequence();
    return pos == 0 && pos < sequence().size();
  }

  @Override
  public boolean isFirst() throws XQException {
    sequence();
    return pos == 1;
  }

  @Override
  public boolean isLast() throws XQException {
    return pos == sequence().size();
  }

  @Override
  public boolean isOnItem() throws XQException {
    opened();
    return pos > 0;
  }

  @Override
  public boolean isScrollable() throws XQException {
    opened();
    return scrollable;
  }

  @Override
  public boolean last() throws XQException {
    final ValueBuilder vb = sequence();
    return cursor(vb, (int) vb.size() - 1);
  }

  @Override
  public boolean next() throws XQException {
    opened();
    if(pos < 0) return false;

    try {
      final Item i = result.next();
      next = i != null;
      ++pos;
      it = new BXQItem(i, this, conn);
      if(!next) pos = -1;
      return next;
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }

  @Override
  public boolean previous() throws XQException {
    return relative(-1);
  }

  @Override
  public boolean relative(final int p) throws XQException {
    return cursor(sequence(), getPosition() + p - 1);
  }

  @Override
  public void writeItem(final OutputStream os, final Properties p)
      throws XQException {
    item().writeItem(os, p);
  }

  @Override
  public void writeItem(final Writer ow, final Properties p)
      throws XQException {
    item().writeItem(ow, p);
  }

  @Override
  public void writeItemToResult(final Result r) throws XQException {
    item().writeItemToResult(r);
  }

  @Override
  public void writeItemToSAX(final ContentHandler sax) throws XQException {
    item().writeItemToSAX(sax);
  }

  @Override
  public void writeSequence(final OutputStream os, final Properties p)
      throws XQException {
    if(it != null && !next) throw new BXQException(TWICE);
    while(next()) item().writeItem(os, p);
  }

  @Override
  public void writeSequence(final Writer ow, final Properties p)
      throws XQException {
    if(it != null && !next) throw new BXQException(TWICE);
    while(next()) item().writeItem(ow, p);
  }

  @Override
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
      Util.notimplemented();
    }
  }

  @Override
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
  private ValueBuilder sequence() throws XQException {
    opened();
    if(!scrollable) throw new BXQException(FORWARD);
    return (ValueBuilder) result;
  }

  /**
   * Sets the cursor to the specified position.
   * @param seq iterator sequence
   * @param p cursor position
   * @return result of check
   * @throws XQException xquery exception
   */
  private boolean cursor(final ValueBuilder seq, final int p) throws XQException {
    pos = p < 0 ? 0 : p >= seq.size() ? -1 : p;
    seq.pos(pos - 1);
    return p >= 0 && next();
  }
}
