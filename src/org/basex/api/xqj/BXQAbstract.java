package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQItemType;
import org.basex.BaseX;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.func.FunJava;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.XQException;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Java XQuery API - Abstract objects.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class BXQAbstract {
  /** Closed flag. */
  protected boolean closed;
  /** Parent closer. */
  protected BXQAbstract par;

  /**
   * Constructor.
   * @param p parent reference
   */
  public BXQAbstract(final BXQAbstract p) {
    par = p;
  }

  /**
   * Closes the class.
   */
  public final void close() {
    closed = true;
  }

  /**
   * Returns the closed flag.
   * @return flag
   */
  public final boolean isClosed() {
    return closed || par != null && par.isClosed();
  }

  /**
   * Checks if the object is open.
   * @throws BXQException exception
   */
  protected final void opened() throws BXQException {
    if(isClosed()) throw new BXQException(getClass().getSimpleName() + CLOSED);
  }

  /**
   * Checks if the specified object is null.
   * @param obj object to be checked
   * @param type data type
   * @return object
   * @throws BXQException exception
   */
  protected static final Object valid(final Object obj, final Class type)
      throws BXQException {
    if(obj == null) throw new BXQException(NULL, type.getSimpleName());
    return obj;
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param e expected type
   * @param tar target type
   * @return target type
   * @throws BXQException xquery exception
   */
  protected Type check(final Type e, final XQItemType tar) throws BXQException {
    opened();
    if(tar == null) return e;

    final Type t = ((BXQItemType) tar).getType();
    if(e == t) return e;

    boolean valid = false;
    switch(e) {
      case BYT: case INT: case LNG: case SHR: case DEC: case ITR:
        valid = t.num && t != Type.DBL && t != Type.FLT; break;
      case STR:
        valid = t.str; break;
      default:
        break;
    }
    if(!valid) throw new BXQException(WRONG, e, t);
    return t;
  }

  /**
   * Creates an XQuery item from the specified Java object.
   * @param v input object
   * @param t target type
   * @return resulting item
   * @throws BXQException exception
   */
  protected final Item create(final Object v, final XQItemType t)
      throws BXQException {
    
    // check if object exists
    valid(v, Object.class);

    // return xquery items
    if(t == null && v instanceof BXQItem) return ((BXQItem) v).it;

    // get XQuery mapping
    final Type e = FunJava.type(v);
    if(e == null) throw new BXQException(CONV, v.getClass().getSimpleName());

    try {
      // return item with correct type 
      return check(e, t).e(v);
    } catch(final XQException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Return the contents of the specified reader as a string.
   * @param r reader
   * @return string
   * @throws BXQException exception
   */
  protected final byte[] content(final Reader r) throws BXQException {
    valid(r, XMLReader.class);
    try {
      final TokenBuilder tb = new TokenBuilder();
      final BufferedReader br = new BufferedReader(r);
      int i = 0;
      while((i = br.read()) != -1) tb.add((char) i);
      br.close();
      return tb.finish();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Return the contents of the specified input stream as a string.
   * @param is input stream
   * @return string
   * @throws BXQException exception
   */
  protected final byte[] content(final InputStream is) throws BXQException {
    valid(is, InputStream.class);
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final BufferedInputStream bis = new BufferedInputStream(is);
      int i = 0;
      while((i = bis.read()) != -1) bos.write(i);
      bis.close();
      return bos.toByteArray();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Creates a database instance from the specified byte array.
   * @param s input source
   * @param it item type
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final Source s, final XQItemType it)
      throws BXQException {

    valid(s, Source.class);
    check(Type.DOC, it);
    if(s instanceof StreamSource) {
      final StreamSource ss = (StreamSource) s;
      final InputStream is = ss.getInputStream();
      if(is != null) return createDB(is);
      final Reader r = ss.getReader();
      if(r != null) return createDB(r);
      return createDB(new IOContent(Token.token(ss.getSystemId())));
    }
    BaseX.notimplemented();
    return null;
  }

  /**
   * Creates a database instance from the specified byte array.
   * @param is input stream
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final InputStream is) throws BXQException {
    opened();
    valid(is, InputStream.class);
    try {
      return checkDB(CreateDB.xml(new SAXSource(new InputSource(is))));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Creates a database instance from the specified byte array.
   * @param r reader
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final Reader r) throws BXQException {
    opened();
    valid(r, Reader.class);
    try {
      return checkDB(CreateDB.xml(new SAXSource(new InputSource(r))));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Creates a database instance from the specified xml reader.
   * @param r xml reader
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final XMLReader r) throws BXQException {
    opened();
    valid(r, XMLReader.class);
    try {
      return checkDB(CreateDB.xml(new SAXSource(r, null)));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Creates a database instance from the specified xml reader.
   * @param sr xml stream reader
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final XMLStreamReader sr)
      throws BXQException {
    opened();
    valid(sr, XMLStreamReader.class);
    try {
      return checkDB(CreateDB.xml(new XMLStreamWrapper(sr), "tmp"));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Creates a database instance from the specified byte array.
   * @param io io reference
   * @return document node
   * @throws BXQException exception
   */
  protected final DNode createDB(final IO io) throws BXQException {
    try {
      return checkDB(CreateDB.xml(io, TMP));
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Checks the specified database instance and returns a document node.
   * @param d database instance
   * @return document node
   * @throws BXQException exception
   */
  private DNode checkDB(final Data d) throws BXQException {
    valid(d, Data.class);
    return new DNode(d, 0);
  }

  /**
   * Serializes an item to the specified serializer.
   * @param it item
   * @param ctx context
   * @param ser serializer
   * @throws BXQException exception
   */
  protected void serialize(final Item it, final XQContext ctx,
      final Serializer ser) throws BXQException {
    opened();
    try {
      if(it.type == Type.ATT) throw new BXQException(ATTR);
      ctx.serialize(ser, it);
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }
}

