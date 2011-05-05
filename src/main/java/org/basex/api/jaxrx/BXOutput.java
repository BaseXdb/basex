package org.basex.api.jaxrx;

import java.io.OutputStream;
import java.util.Scanner;
import javax.ws.rs.core.StreamingOutput;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.server.ClientQuery;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * Wrapper class for running JAX-RX code which creates output.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class BXOutput extends BXCode implements StreamingOutput {
  /** Optional path info. */
  final ResourcePath path;
  /** Output stream. */
  OutputStream out;

  /**
   * Constructor.
   * @param pt optional path info
   */
  BXOutput(final ResourcePath pt) {
    super(pt);
    path = pt;
  }

  @Override
  public void write(final OutputStream os) {
    out = os;

    if(path != null) {
      try {
        // open database if a resource path was specified
        if(path.getDepth() != 0) cs.execute(new Open(path.getResourcePath()));
      } catch(final BaseXException ex) {
        throw new JaxRxException(status(ex), ex.getMessage());
      }
      try {
        // set serialization parameters
        cs.execute(new Set(Prop.SERIALIZER, serial(path)));
      } catch(final BaseXException ex) {
        throw new JaxRxException(400, ex.getMessage());
      }
    }
    run();
  }

  /**
   * Executes the specified command. If command execution fails, an exception is
   * thrown.
   * @param command command to be executed
   * @param os output stream, or {@code null}
   * @return result, or {@code null} if output stream was specified
   */
  final String exec(final Object command, final OutputStream os) {
    cs.setOutputStream(os);
    try {
      return cs.execute(command.toString());
    } catch(final BaseXException ex) {
      throw new JaxRxException(400, ex.getMessage());
    }
  }

  /**
   * Runs the specified query.
   * @param query query string
   * @return query results
   */
  final String query(final String query) {
    // evaluate first result and number of results
    final int s = num(path, QueryParameter.START, 1);
    final int m = num(path, QueryParameter.COUNT, Integer.MAX_VALUE - s);

    ClientQuery cq = null;
    try {
      cs.execute(new Set(Prop.SERIALIZER, serial(path)));
      cs.setOutputStream(out);

      // create query instance
      cq = cs.query(query.isEmpty() ? "." : query);
      final String var = path.getValue(QueryParameter.VAR);
      if(var != null) {
        // bind external variables
        final Scanner sc = new Scanner(var);
        sc.useDelimiter("\1");
        while(sc.hasNext()) {
          final String v = sc.next();
          String[] sp = v.split("\2", 3);
          if(sp.length < 2) sp = v.split("=", 2);
          cq.bind(sp[0], sp.length > 1 ? sp[1] : "",
              sp.length > 2 ? sp[2] : "");
        }
      }
      // loop through all results
      int c = 0;
      cq.init();
      while(++c < s + m && cq.more())
        if(c >= s) cq.next();
      return null;
    } catch(final BaseXException ex) {
      throw new JaxRxException(status(ex), ex.getMessage());
    } finally {
      // close query instance
      if(cq != null) try {
        cq.close();
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Returns status code when an exception occurs.
   * @param ex Exception occurred.
   * @return status code.
   */
  private int status(final Exception ex) {
    int status = 404;
    String message = ex.getMessage();
    if(message != null && (message.contains(Text.PERMNO.substring(2)) ||
        message.contains(Text.PERMINV))) status = 403;
    return status;
  }
}
