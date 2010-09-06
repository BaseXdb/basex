package org.basex.api.jaxrx;

import static org.jaxrx.core.URLConstants.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.basex.core.Prop;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
      cs = new ClientSession("localhost",
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
   * @throws IOException I/O exception
   */
  abstract void code() throws IOException;

  /**
   * Runs the class code and closes the client session.
   * A server exception is thrown if I/O errors occur.
   */
  final void run() {
    try {
      code();
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
  final String root(final ResourcePath path) {
    if(path.getDepth() == 1) return path.getResourcePath();
    throw new JaxRxException(404, "Resource '" + path + "' was not found.");
  }

  /**
   * Caches the input stream contents to a temporary file on disk.
   * @param in input stream
   * @return file reference
   * @throws IOException I/O exception
   */
  final File cache(final InputStream in) throws IOException {
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

      if(file.length() == 0)
        throw new JaxRxException(400, "XML input was missing.");

      return file;
    } catch(final IOException ex) {
      // try to delete temporary file before returning the exception
      try { bos.close(); } catch(final Exception exx) { /**/ }
      file.delete();
      throw ex;
    }
  }

  /**
   * Returns serialization parameters, specified in the path, as a string.
   * @param path JAX-RX path
   * @return string with serialization parameters
   */
  final String params(final ResourcePath path) {
    String ser = path.getValue(QueryParameter.OUTPUT);
    if(ser == null) ser = "";

    final String wrap = path.getValue(QueryParameter.WRAP);
    // wrap results by default
    if(wrap == null || wrap.equals(DataText.YES)) {
      ser += "," + SerializerProp.S_WRAP_PRE[0] + "=" + JAXRX +
             "," + SerializerProp.S_WRAP_URI[0] + "=" + URL;
    } else if(!wrap.equals(DataText.NO)) {
      throw new JaxRxException(400, SerializerProp.error(
          QueryParameter.WRAP.toString(), DataText.YES, DataText.NO));
    }
    return ser;
  }
}
