package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import org.basex.query.xquery.item.Type;
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
    if(isClosed())
      throw new BXQException(getClass().getSimpleName() + CLOSED);
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
    if(it != null && !bit.type.instance(t) && !t.instance(bit.type))
    //if(it != null && !t.instance(bit.type))
      throw new BXQException(WRONG, t, bit.type);
    return it != null ? bit.type : t;
  }

  /**
   * Return the contents of the specified reader as a string.
   * @param r reader 
   * @return string
   * @throws XQException exception
   */
  public final String content(final Reader r) throws XQException {
    check(r, XMLReader.class);
    try {
      final StringBuilder sb = new StringBuilder();
      final BufferedReader br = new BufferedReader(r);
      int i = 0;
      while((i = br.read()) != -1) sb.append((char) i);
      br.close();
      return sb.toString();
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
  public final String content(final InputStream is) throws XQException {
    check(is, InputStream.class);
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final BufferedInputStream bis = new BufferedInputStream(is);
      int i = 0;
      while((i = bis.read()) != -1) bos.write(i);
      bis.close();
      return bos.toString();
    } catch(final IOException ex) {
      throw new BXQException(ex);
    }
  }
}
