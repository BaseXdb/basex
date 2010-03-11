package org.basex.api.jaxrx.local;

import static org.jaxrx.constants.URLConstants.*;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Text;
import org.basex.core.proc.Open;
import org.basex.data.SerializeProp;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
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

    // parse parameters
    final String xquery = query != null ? query : ".";
    final int s = start != null ? Integer.valueOf(start) : 0;
    final int m = count != null ? Integer.valueOf(count) :
      Integer.MAX_VALUE - s;

    SerializeProp props = null;
    try {
      props = new SerializeProp(serialize);
      if(wrap != null) {
        if(wrap.equals(SerializeProp.YES)) {
          props.set(SerializeProp.S_WRAP_PRE, JAXRX);
          props.set(SerializeProp.S_WRAP_URI, URL);
        } else if(!wrap.equals(SerializeProp.NO)) {
          badRequest(Main.info(
              Text.SETVAL + Text.NL, EURLParameter.WRAP, wrap));
        }
      }
    } catch(final IOException ex) {
      badRequest(ex.toString());
    }

    // open database
    final Context ctx = new Context();
    try {
      new Open(resource).execute(ctx);
    } catch(BaseXException ex) {
      notFound(ex.getMessage());
    }

    // perform query and return result
    QueryProcessor qp = null;
    try {
      final XMLSerializer xml = new XMLSerializer(out, props);
      qp = new QueryProcessor(xquery, ctx);

      final Iter iter = qp.iter();
      for(int c = 0; c < s + m; c++) {
        final Item it = iter.next();
        if(it == null) break;
        if(c < s) continue;
        xml.openResult();
        it.serialize(xml);
        xml.closeResult();
      }
      xml.close();
    } catch(final QueryException ex) {
      badRequest(ex.getMessage());
    } catch(final IOException ex) {
      error(ex);
    } finally {
      // close open references
      try { if(qp != null) qp.close(); } catch(final Exception ex) { }
      ctx.close();
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
