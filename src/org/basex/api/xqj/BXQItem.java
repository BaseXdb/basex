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
import org.basex.api.jaxp.IterStreamReader;
import org.basex.data.SAXSerializer;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.util.Token;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Java XQuery API - Item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXQItem extends BXQAbstract implements XQResultItem {
  /** Connection. */
  private final XQConnection conn;
  /** Query context. */
  private final XQContext ctx;
  /** Item. */
  Item it;
  
  /**
   * Constructor.
   * @param item item
   */
  public BXQItem(final Item item) {
    this(item, null, null, null);
  }

  /**
   * Constructor.
   * @param item item
   * @param c close reference
   * @param context query context
   * @param connection connection reference
   */
  public BXQItem(final Item item, final BXQAbstract c,
      final XQContext context, final BXQConnection connection) {
    super(c);
    conn = connection;
    ctx = context;
    it = item;
  }
  
  public String getAtomicValue() throws XQException {
    check();
    if(it.node()) throw new BXQException(ATOM, it.type);
    return Token.string(it.str());
  }

  public boolean getBoolean() throws XQException {
    try {
      return check(Type.BLN).bool();
    } catch(final org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  public byte getByte() throws XQException {
    return (byte) castItr(Type.BYT);
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

  public int getInt() throws XQException {
    return (int) castItr(Type.INT);
  }

  public XMLStreamReader getItemAsStream() {
    final SeqIter seq = new SeqIter();
    seq.add(it);
    return new IterStreamReader(seq);
  }

  public String getItemAsString(Properties props) throws XQException {
    return serialize();
  }

  public XQItemType getItemType() throws XQException {
    check();
    return new BXQItemType(it.type);
  }

  public long getLong() throws XQException {
    return castItr(Type.LNG);
  }

  public Node getNode() throws XQException {
    check();
    BaseX.notimplemented();
    return it.node() ? (Node) it : null;
  }

  public URI getNodeUri() throws XQException {
    check();
    if(!it.node()) throw new BXQException(NODE);
    final org.basex.query.xquery.item.Node node =
      (org.basex.query.xquery.item.Node) it;
    try {
      return new URI(Token.string(node.base()));
    } catch(final URISyntaxException ex) {
      throw new BXQException(ex.toString());
    }
  }

  public Object getObject() throws XQException {
    check();
    return it.java();
  }

  public short getShort() throws XQException {
    return (short) castItr(Type.SHR);
  }

  public boolean instanceOf(XQItemType type) throws XQException {
    check();
    return it.type.instance(((BXQItemType) type).type);
  }

  public void writeItem(OutputStream os, Properties props) throws XQException {
    check(os, OutputStream.class);
    serialize(os);
  }

  /**
   * Returns the current item in an output cache
   * @param os output stream
   * @throws XQException exception
   */
  private void serialize(final OutputStream os) throws XQException {
    serialize(it, ctx, new XMLSerializer(new PrintOutput(os)));
  }

  public void writeItem(Writer ow, Properties props) throws XQException {
    check(ow, Writer.class);
    try {
      ow.write(serialize());
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Returns the current item in an output cache
   * @return cached output
   * @throws XQException exception
   */
  private String serialize() throws XQException {
    check();
    final CachedOutput co = new CachedOutput();
    serialize(it, ctx, new XMLSerializer(co));
    return co.toString();
  }

  public void writeItemToResult(Result result) throws XQException {
    check();
    check(result, Result.class);
    
    // evaluate different Result types...
    if(result instanceof StreamResult) {
      // StreamResult.. directly write result as string
      writeItem(((StreamResult) result).getWriter(), null);
    } else if(result instanceof SAXResult) {
      // SAXResult.. serialize result to underlying parser
      final SAXSerializer ser = new SAXSerializer(null);
      ContentHandler h = ((SAXResult) result).getHandler();
      ser.setContentHandler(h);
      serialize(it, ctx, ser);
      
      /*
      final ContentHandler handler = ((SAXResult) result).getHandler();
      try {
        final SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setValidating(false);
        final XMLReader r = f.newSAXParser().getXMLReader();
        r.setContentHandler(handler);
        r.parse(new InputSource(new StringReader(serialize())));
      } catch(final Exception ex) {
        throw new BXQException(ex);
      }*/
    } else {
      BaseX.notimplemented();
    }
  }

  public void writeItemToSAX(ContentHandler sax) throws XQException {
    check(sax, ContentHandler.class);
    writeItemToResult(new SAXResult(sax));
  }

  public XQConnection getConnection() throws XQException {
    check();
    return conn;
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param type expected type
   * @return item
   * @throws XQException xquery exception
   */
  private Item check(final Type type) throws XQException {
    check();
    if(!it.type.instance(type)) throw new BXQException(WRONG, it.type, type);
    return it;
  }

  /**
   * Casts the current item.
   * @param type expected type
   * @return cast item
   * @throws XQException xquery exception
   */
  private Item cast(final Type type) throws XQException {
    check();
    try {
      return type.e(it, null);
    } catch(org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Casts the current item.
   * @param type expected type
   * @return cast item
   * @throws XQException xquery exception
   */
  private long castItr(final Type type) throws XQException {
    check();
    try {
      final double d = it.dbl();
      if(!it.n() || d != (long) d) throw new BXQException(NUM);
      return cast(type).itr();
    } catch(org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }
}
