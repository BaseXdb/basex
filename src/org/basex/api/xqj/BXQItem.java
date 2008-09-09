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
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultItem;
import org.basex.BaseX;
import org.basex.api.jaxp.IterStreamReader;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
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
    return getItemCache().toString();
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
    BaseX.notimplemented();
    final SeqIter seq = new SeqIter();
    seq.add(it);
    return new IterStreamReader(seq);
  }

  public String getItemAsString(Properties props) throws XQException {
    return getItemCache().toString();
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
    return Token.string(it.str());
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
    try {
      os.write(getItemCache().finish());
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public void writeItem(Writer ow, Properties props) throws XQException {
    check(ow, Writer.class);
    try {
      ow.write(getItemCache().toString());
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Returns the current item in an output cache
   * @return cached output
   * @throws XQException exception
   */
  CachedOutput getItemCache() throws XQException {
    check();
    try {
      final CachedOutput co = new CachedOutput();
      final XMLSerializer ser = new XMLSerializer(co);
      if(it.type == Type.ATT) throw new BXQException(ATTR);
      it.serialize(ser, ctx, 0);
      return co;
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  public void writeItemToResult(Result result) throws XQException {
    check();
    BaseX.notimplemented();
  }

  public void writeItemToSAX(ContentHandler saxhdlr) throws XQException {
    check();
    BaseX.notimplemented();
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
