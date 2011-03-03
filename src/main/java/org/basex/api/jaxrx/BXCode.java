package org.basex.api.jaxrx;

import static org.jaxrx.core.JaxRxConstants.*;
import java.io.IOException;
import org.basex.core.Text;
import org.basex.data.DataText;
import org.basex.data.SerializerProp;
import org.basex.server.ClientSession;
import org.basex.util.Util;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * Wrapper class for running JAX-RX code.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class BXCode {
  /** Client session. */
  final ClientSession cs;

  /**
   * Default constructor, creating a new client session instance.
   */
  BXCode() {
    try {
      cs = new ClientSession(Text.LOCALHOST,
          Integer.parseInt(System.getProperty("org.basex.serverport")),
          System.getProperty("org.basex.user"),
          System.getProperty("org.basex.password"));
    } catch(final Exception ex) {
      Util.stack(ex);
      throw new JaxRxException(ex);
    }
  }

  /**
   * Code to be run.
   * @return string info message
   * @throws IOException I/O exception
   */
  abstract String code() throws IOException;

  /**
   * Runs the {@link #code()} method and closes the client session.
   * A server exception is thrown if I/O errors occur.
   * @return info message
   */
  final String run() {
    try {
      return code();
    } catch(final IOException ex) {
      throw new JaxRxException(ex);
    } finally {
      try { cs.close(); } catch(final Exception ex) { /**/ }
    }
  }

  /**
   * Returns the root resource of the specified path.
   * If the path contains more or less than a single resource,
   * an exception is thrown.
   * @param path path
   * @return root resource
   */
  final String db(final ResourcePath path) {
    return path.getResource(0);
  }

  /**
   * Converts the specified query parameter to a positive integer.
   * Throws an exception if the string is smaller than 1 or cannot be converted.
   * @param rp resource path
   * @param qp query parameter
   * @param def default value
   * @return integer
   */
  int num(final ResourcePath rp, final QueryParameter qp, final int def) {
    final String val = rp.getValue(qp);
    if(val == null) return def;

    try {
      final int i = Integer.parseInt(val);
      if(i > 0) return i;
    } catch(final NumberFormatException ex) {
      /* exception follows for both branches. */
    }
    throw new JaxRxException(400, "Parameter '" + qp +
        "' is no valid integer: " + val);
  }

  /**
   * Returns the collection path for a database.
   * @param path path
   * @return root resource
   */
  final String path(final ResourcePath path) {
    final StringBuilder sb = new StringBuilder();
    for(int i = 1; i < path.getDepth(); ++i) {
      sb.append('/').append(path.getResource(i));
    }
    return sb.substring(Math.min(1, sb.length()));
  }

  /**
   * Returns serialization parameters, specified in the path, as a string.
   * @param path JAX-RX path
   * @return string with serialization parameters
   */
  final String serial(final ResourcePath path) {
    String ser = path.getValue(QueryParameter.OUTPUT);
    if(ser == null) ser = "";

    final String wrap = path.getValue(QueryParameter.WRAP);
    // wrap results by default
    if(wrap == null || wrap.equals(DataText.YES)) {
      ser += "," + SerializerProp.S_WRAP_PREFIX[0] + "=" + JAXRX +
             "," + SerializerProp.S_WRAP_URI[0] + "=" + URL;
    } else if(!wrap.equals(DataText.NO)) {
      throw new JaxRxException(400, SerializerProp.error(QueryParameter.WRAP,
          wrap, DataText.YES, DataText.NO).getMessage());
    }
    return ser.replaceAll("^,", "");
  }
}
