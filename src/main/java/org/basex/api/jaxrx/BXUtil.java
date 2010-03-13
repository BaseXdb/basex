package org.basex.api.jaxrx;

import static org.basex.util.Token.*;
import static org.jaxrx.constants.URLConstants.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;
import org.basex.core.Prop;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.data.SerializeProp;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;
import org.basex.util.Table;
import org.basex.util.TokenList;
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
      return new ClientSession("localhost",
          Integer.parseInt(System.getProperty("org.jaxrx.port")),
          System.getProperty("org.jaxrx.user"),
          System.getProperty("org.jaxrx.password"));
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
   * This method runs a query or command on the database.
   * @param db An optional database reference
   * @param p parameters
   * @return streaming output reference
   */
  static StreamingOutput query(final String db,
      final Map<EURLParameter, String> p) {

    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        final ClientSession cs = session();
        run(cs, new Code() {
          @Override
          public void run() throws IOException {
            // open database
            if(db != null && !cs.execute(new Open(db))) notFound(cs.info());

            // set serialization parameters
            final String par = params(p);
            if(!cs.execute(new Set(Prop.SERIALIZER, par)))
              badRequest(cs.info());

            // run command, query or show databases
            if(p.get(EURLParameter.COMMAND) != null) {
              command(cs, out, p);
            } else if(p.get(EURLParameter.QUERY) != null || db != null) {
              query(cs, out, p);
            } else {
              list(cs, out, p);
            }
          }
        });
      }
    };
  }

  /**
   * Runs a query.
   * @param cs client session
   * @param out output stream
   * @param par query parameters
   * @throws IOException I/O exception
   */
  static void query(final ClientSession cs, final OutputStream out,
      final Map<EURLParameter, String> par) throws IOException {

    // wrap start and counter around query expression
    final String query = par.get(EURLParameter.QUERY);
    final String start = par.get(EURLParameter.START);
    final String count = par.get(EURLParameter.COUNT);
    final int s = start != null ? Integer.valueOf(start) : 1;
    final int m = count != null ? Integer.valueOf(count) :
      Integer.MAX_VALUE - s;
    final String xquery = "(" + (query != null ? query : ".") +
      ")[position() = " + s + " to " + (s + m - 1) + "]";
    if(!cs.execute(new XQuery(xquery), out)) badRequest(cs.info());
  }

  /**
   * Lists all databases.
   * @param cs client session
   * @param out output stream
   * @param par serialization parameters
   * @throws IOException I/O exception
   */
  static void list(final ClientSession cs, final OutputStream out,
      final Map<EURLParameter, String> par) throws IOException {

    // retrieve list of databases
    final CachedOutput co = new CachedOutput();
    if(!cs.execute(new List(), co)) badRequest(cs.info());
    final Table table = new Table(co.toString());

    final XMLSerializer xml =
      new XMLSerializer(out, new SerializeProp(params(par)));
    for(final TokenList l : table.contents) {
      xml.emptyElement(token(JAXRX + ":" + "resource"),
          token("name"), l.get(0), token("documents"), l.get(1),
          token("size"), l.get(2));
    }
    xml.close();
  }

  /**
   * Runs a database command.
   * @param cs client session
   * @param out output stream
   * @param par query parameters
   * @throws IOException I/O exception
   */
  static void command(final ClientSession cs, final OutputStream out,
      final Map<EURLParameter, String> par) throws IOException {

    // perform command and serialize output
    final CachedOutput co = new CachedOutput();
    final String cmd = par.get(EURLParameter.COMMAND);
    if(!cs.execute(cmd, co)) badRequest(cs.info());

    final XMLSerializer xml =
      new XMLSerializer(out, new SerializeProp(params(par)));
    xml.text(delete(co.finish(), '\r'));
    xml.close();
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
   * Returns serialization parameters, specified in the map, in a string.
   * @param p map with parameters
   * @return string with serialization parameters
   */
  static String params(final Map<EURLParameter, String> p) {
    String ser = p.get(EURLParameter.OUTPUT);
    if(ser == null) ser = "";
    
    final String wrap = p.get(EURLParameter.WRAP);
    // wrap results by default
    if(wrap == null || wrap.equals(SerializeProp.YES)) {
      ser += "," + SerializeProp.S_WRAP_PRE[0] + "=" + JAXRX +
             "," + SerializeProp.S_WRAP_URI[0] + "=" + URL;
    } else if(!wrap.equals(SerializeProp.NO)) {
      badRequest(SerializeProp.error(EURLParameter.WRAP.toString(),
          SerializeProp.YES, SerializeProp.NO));
    }
    return ser;
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
