package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.func.FunJava;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Jav;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.xml.sax.XMLReader;

/**
 * Java XQuery API - Closable objects.
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
   * @param parent
   */
  public BXQAbstract(final BXQAbstract parent) {
    par = parent;
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
   * @throws XQException exception
   */
  protected final void check() throws XQException {
    if(isClosed()) throw new BXQException(getClass().getSimpleName() + CLOSED);
  }
  
  /**
   * Checks if the specified object is null.
   * @param obj object to be checked
   * @param type data type
   * @throws XQException exception
   */
  protected static final void check(final Object obj, final Class type)
      throws XQException {
    if(obj == null) throw new BXQException(NULL, type.getSimpleName());
  }

  /**
   * Checks the specified data type; throws an error if the type is wrong.
   * @param it item type
   * @param t expected type
   * @return target type
   * @throws XQException xquery exception
   */
  protected Type check(final XQItemType it, final Type t) throws XQException {
    check();
    final BXQItemType bit = (BXQItemType) it;
    
    //if(it != null && !bit.type.instance(t))
    if(it != null && !bit.type.instance(t) && !t.instance(bit.type)) {
    //if(it != null && !t.instance(bit.type))
      throw new BXQException(WRONG, t, bit.type);
    }
    return it != null ? bit.type : t;
  }

  /**
   * Creates n item from the specified object.
   * @param v input object
   * @return resulting item
   * @throws XQException exception
   */
  protected static final Item createItem(Object v) throws XQException {
    try {
      final Type t = FunJava.jType(v.getClass());
      if(t != Type.JAVA) return t.e(new Jav(v), null);
      
      final String s = v instanceof BXQItem ?
          ((BXQItem) v).getAtomicValue() : v.toString();
      return Str.get(Token.token(s));
    } catch(org.basex.query.xquery.XQException ex) {
      throw new BXQException(ex);
    }
  }

  /**
   * Return the contents of the specified reader as a string.
   * @param r reader 
   * @return string
   * @throws XQException exception
   */
  protected static final byte[] content(final Reader r) throws XQException {
    check(r, XMLReader.class);
    try {
      final TokenBuilder sb = new TokenBuilder();
      final BufferedReader br = new BufferedReader(r);
      int i = 0;
      while((i = br.read()) != -1) sb.add((char) i);
      br.close();
      return sb.finish();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }
  
  /**
   * Return the contents of the specified input stream as a string.
   * @param is input stream
   * @return string
   * @throws XQException exception
   */
  protected static final byte[] content(final InputStream is) throws XQException {
    check(is, InputStream.class);
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
   * @param val XML contents
   * @return document node
   * @throws XQException exception
   */
  protected static final DNode createDB(final byte[] val) throws XQException {
    return checkDB(CreateDB.xml(new IO(val, TMP), TMP));
  }
  
  /**
   * Creates a database instance from the specified xml reader.
   * @param r xml reader
   * @return document node
   * @throws XQException exception
   */
  protected static final DNode createDB(final XMLReader r) throws XQException {
    check(r, XMLReader.class);
    final IO tmp = new IO(Token.EMPTY, TMP);
    return checkDB(CreateDB.xml(new SAXWrapper(tmp, r), TMP));
  }
  
  /**
   * Creates a database instance from the specified xml reader.
   * @param sr xml stream reader
   * @return document node
   * @throws XQException exception
   */
  protected static final DNode createDB(final XMLStreamReader sr)
      throws XQException {
    check(sr, XMLStreamReader.class);
    return checkDB(CreateDB.xml(new XMLStreamWrapper(sr), TMP));
  }
  
  /**
   * Checks the specified database instance and returns a document node.
   * @param d database instance
   * @return document node
   * @throws XQException exception
   */
  private static DNode checkDB(final Data d) throws XQException {
    check(d, Data.class);
    return new DNode(d, 0, null, Type.DOC);
  }

  /**
   * Serializes an item to the specified serializer.
   * @param it item
   * @param ctx context
   * @param ser serializer
   * @throws XQException exception
   */
  protected void serialize(final Item it, final XQContext ctx,
      final Serializer ser) throws XQException {
    check();
    try {
      if(it.type == Type.ATT) throw new BXQException(ATTR);
      it.serialize(ser, ctx, 0);
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }
}

