package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultItem;
import org.basex.BaseX;
import org.basex.data.SAXSerializer;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.SeqIter;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Java XQuery API - Item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BXQItem extends BXQAbstract implements XQResultItem {
  /** Connection. */
  private final XQConnection conn;
  /** Query context. */
  private final QueryContext ctx;
  /** Item. */
  Item it;

  /**
   * Constructor.
   * @param item item
   */
  public BXQItem(final Item item) {
    this(item, null, new QueryContext(), null);
  }

  /**
   * Constructor.
   * @param item item
   * @param c close reference
   * @param context query context
   * @param connection connection reference
   */
  public BXQItem(final Item item, final BXQAbstract c,
      final QueryContext context, final BXQConnection connection) {
    super(c);
    conn = connection;
    ctx = context;
    it = item;
  }

  public String getAtomicValue() throws XQException {
    opened();
    if(it.node()) throw new BXQException(ATOM);
    return Token.string(it.str());
  }

  public boolean getBoolean() throws XQException {
    return ((Bln) check(Type.BLN)).bool();
  }

  public byte getByte() throws XQException {
    return (byte) castItr(Type.BYT);
  }

  public double getDouble() throws XQException {
    return ((Dbl) check(Type.DBL)).dbl();
  }

  public float getFloat() throws XQException {
    return ((Flt) check(Type.FLT)).flt();
  }

  public int getInt() throws XQException {
    return (int) castItr(Type.INT);
  }

  public XMLStreamReader getItemAsStream() {
    return new IterStreamReader(new SeqIter(it));
  }

  public String getItemAsString(final Properties props) throws XQException {
    return serialize();
  }

  public XQItemType getItemType() throws XQException {
    opened();
    return new BXQItemType(it.type);
  }

  public long getLong() throws XQException {
    return castItr(Type.LNG);
  }

  public Node getNode() throws XQException {
    opened();
    if(!it.node()) throw new BXQException(WRONG, Type.NOD, it.type);
    return ((Nod) it).java();
  }

  public URI getNodeUri() throws XQException {
    opened();
    if(!it.node()) throw new BXQException(NODE);
    final Nod node = (Nod) it;
    try {
      return new URI(Token.string(node.base()));
    } catch(final URISyntaxException ex) {
      throw new BXQException(ex.toString());
    }
  }

  public Object getObject() throws XQException {
    opened();
    return it.java();
  }

  public short getShort() throws XQException {
    return (short) castItr(Type.SHR);
  }

  public boolean instanceOf(final XQItemType type) throws XQException {
    opened();
    return it.type.instance(((BXQItemType) type).getType());
  }

  public void writeItem(final OutputStream os, final Properties props)
      throws XQException {
    valid(os, OutputStream.class);
    serialize(os);
  }

  /**
   * Serializes the item to the specified output stream.
   * @param os output stream
   * @throws XQException exception
   */
  private void serialize(final OutputStream os) throws XQException {
    try {
      final XMLSerializer ser = new XMLSerializer(new PrintOutput(os));
      serialize(it, ctx, ser);
      ser.close();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public void writeItem(final Writer ow, final Properties props)
      throws XQException {
    valid(ow, Writer.class);
    try {
      ow.write(serialize());
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Returns the serialized output.
   * @return cached output
   * @throws XQException exception
   */
  private String serialize() throws XQException {
    opened();
    final CachedOutput co = new CachedOutput();
    try {
      final XMLSerializer ser = new XMLSerializer(co);
      serialize(it, ctx, ser);
      ser.close();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
    return co.toString();
  }

  public void writeItemToResult(final Result result) throws XQException {
    opened();
    valid(result, Result.class);

    // evaluate different Result types...
    if(result instanceof StreamResult) {
      // StreamResult.. directly write result as string
      writeItem(((StreamResult) result).getWriter(), null);
    } else if(result instanceof SAXResult) {
      try {
        // SAXResult.. serialize result to underlying parser
        final SAXSerializer ser = new SAXSerializer(null);
        ser.setContentHandler(((SAXResult) result).getHandler());
        serialize(it, ctx, ser);
        ser.close();
      } catch(final IOException ex) {
        throw new BXQException(ex);
      }
    } else {
      BaseX.notimplemented();
    }
  }

  public void writeItemToSAX(final ContentHandler sax) throws XQException {
    valid(sax, ContentHandler.class);
    writeItemToResult(new SAXResult(sax));
  }

  public XQConnection getConnection() throws XQException {
    opened();
    return conn;
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param type expected type
   * @return item
   * @throws XQException xquery exception
   */
  private Item check(final Type type) throws XQException {
    opened();
    if(it.type != type) throw new BXQException(WRONG, it.type, type);
    return it;
  }

  /**
   * Casts the current item.
   * @param type expected type
   * @return cast item
   * @throws XQException xquery exception
   */
  private long castItr(final Type type) throws XQException {
    opened();
    try {
      final double d = it.dbl();
      if(!it.n() || d != (long) d) throw new BXQException(NUM, d);
      return type.e(it, null).itr();
    } catch(final QueryException ex) {
      throw new BXQException(ex);
    }
  }
}
