package org.basex.api.jaxrx;

import static org.jaxrx.constants.URLConstants.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.gui.SerializeProp;
import org.basex.server.ClientSession;
import org.jaxrx.constants.EURLParameter;

/**
 * This class contains utility methods which are used by several JAX-RX
 * implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXUtil {
  /** Private constructor. */
  private BXUtil() { }

  /** User login. */
  static String login = System.getProperty("org.jaxrx.user");
  /** User password. */
  static String password = System.getProperty("org.jaxrx.password");
  /** Server port. */
  static int port = Integer.parseInt(System.getProperty("org.jaxrx.port"));

  /** Container for a runnable method. */
  abstract static class Code {
    /**
     * Runnable method.
     * @throws IOException I/O exception
     */
    abstract void run() throws IOException;
  }

  /**
   * Creates a client session or throws a server exception.
   * @return client session
   */
  static ClientSession session() {
    try {
      return new ClientSession("localhost", port, login, password);
    } catch(final IOException ex) {
      error(ex);
      return null;
    }
  }

  /**
   * Runs the specified code and closes the specified session.
   * A server exception is thrown if I/O errors occur.
   * @param cs client session.
   * @param code code to be run
   */
  static void run(final ClientSession cs, final Code code) {
    try {
      code.run();
    } catch(final IOException ex) {
      error(ex);
    } finally {
      try { if(cs != null) cs.close(); } catch(final Exception ex) { }
    }
  }
  
  /**
   * This method executes the XQuery expression within BaseX.
   * @param resource resource
   * @param query The XQuery expression as {@link String}
   * @param out The {@link OutputStream} that writes the result of the query
   * @param wrap The optional surrounding result element (needed for a
   *          XML fragment)
   * @param serialize serialization parameters
   * @param start The start value
   * @param count The maximum value of results
   * @throws WebApplicationException The exception occurred
   */
  static void query(final String resource, final OutputStream out,
      final String query, final String wrap, final String serialize,
      final String start, final String count) {

    // wrap start and counter around query expression
    final int s = start != null ? Integer.valueOf(start) : 1;
    final int m = count != null ? Integer.valueOf(count) :
      Integer.MAX_VALUE - s;
    final String xquery = "(" + (query != null ? query : ".") +
      ")[position() = " + s + " to " + (s + m - 1) + "]";
    
    final ClientSession cs = session();
    run(cs, new Code() {
      @Override
      public void run() throws IOException {
        // compose serialization parameters
        String ser = serialize;
        if(wrap != null) {
          if(wrap.equals(SerializeProp.YES)) {
            ser += "," + SerializeProp.WRAP_PRE[0] + "=" + JAXRX +
                   "," + SerializeProp.WRAP_URI[0] + "=" + URL;
          } else if(!wrap.equals(SerializeProp.NO)) {
            badRequest(Main.info(Text.SETVAL + Text.NL,
                EURLParameter.WRAP, wrap));
          }
        }
        // open database
        if(!cs.execute(new Open(resource))) notFound(cs.info());
        // set serialization parameters
        if(!cs.execute(new Set(Prop.SERIALIZER, ser))) badRequest(cs.info());
        // run query
        if(!cs.execute(new XQuery(xquery), out)) badRequest(cs.info());
      }
    });
  }

  /**
   * Caches the input stream contents to a temporary file on disk.
   * @param in input stream
   * @return file reference
   * @throws IOException I/O exception
   */
  static File cache(final InputStream in) throws IOException {
    // use current number of milliseconds as filename
    final File file = new File(Prop.TMP + System.currentTimeMillis());
    final BufferedInputStream bis = new BufferedInputStream(in);
    final BufferedOutputStream bos = new BufferedOutputStream(
        new FileOutputStream(file));

    try {
      while(true) {
        final int b = bis.read();
        if(b == -1) break;
        bos.write(b);
      }
      bos.close();
      return file;
    } catch(final IOException ex) {
      // try to delete temporary file before returning the exception
      try { bos.close(); } catch(final Exception e) { }
      file.delete();
      throw ex;
    }
  }
  
  /**
   * Returns a {@link Status#NOT_FOUND} exception.
   * @param info info string
   */
  static void notFound(final String info) {
    throw new WebApplicationException(
        Response.status(Status.NOT_FOUND).entity(info).build());
  }

  /**
   * Returns a {@link Status#BAD_REQUEST} exception.
   * @param info info string
   */
  static void badRequest(final String info) {
    throw new WebApplicationException(
        Response.status(Status.BAD_REQUEST).entity(info).build());
  }

  /**
   * Returns an {@link Status#INTERNAL_SERVER_ERROR} exception.
   * @param ex exception
   */
  static void error(final Exception ex) {
    throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
  }
}
