package org.basex.http.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.rest.RESTText.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class RESTCode {
  /**
   * Performs the REST operation.
   * @param http HTTP context
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  abstract void run(final HTTPContext http) throws HTTPException, IOException;

  /**
   * Opens the addressed database.
   * @param http HTTP context
   * @throws HTTPException REST exception
   */
  static void open(final HTTPContext http) throws HTTPException {
    if(http.db() == null) return;
    try {
      http.session().execute(new Open(http.path()));
    } catch(final IOException ex) {
      throw new HTTPException(SC_NOT_FOUND, ex.getMessage());
    }
  }

  /**
   * Sets the wrapping flag.
   * @param val value
   * @param http HTTP context
   * @throws HTTPException REST exception
   */
  static void wrap(final String val, final HTTPContext http) throws HTTPException {
    http.wrapping = Util.yes(val);
    if(!http.wrapping && !Util.no(val)) {
      try {
        SerializerProp.error(WRAP, val, Text.YES, Text.NO);
      } catch(final SerializerException ex) {
        throw new HTTPException(SC_BAD_REQUEST, ex.getMessage());
      }
    }
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param http HTTP context
   * @return number of documents
   * @throws IOException I/O exception
   */
  protected static boolean exists(final HTTPContext http) throws IOException {
    final Query q = http.session().query(_DB_EXISTS.args("$d", "$p"));
    q.bind("d", http.db());
    q.bind("p", http.dbpath());
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Checks if the specified path points to a binary resource.
   * @param http HTTP context
   * @return result of check
   * @throws IOException I/O exception
   */
  protected static boolean isRaw(final HTTPContext http) throws IOException {
    final Query q = http.session().query(_DB_IS_RAW.args("$d", "$p"));
    q.bind("d", http.db());
    q.bind("p", http.dbpath());
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Returns the content type of a database resource.
   * @param http HTTP context
   * @return content type
   * @throws IOException I/O exception
   */
  protected static String contentType(final HTTPContext http) throws IOException {
    final Query q = http.session().query(_DB_CONTENT_TYPE.args("$d", "$p"));
    q.bind("d", http.db());
    q.bind("p", http.dbpath());
    return q.execute();
  }

  /**
   * Parses and sets database options.
   * Throws an exception if an option is unknown.
   * @param http HTTP context
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  static void parseOptions(final HTTPContext http) throws HTTPException, IOException {
    for(final Entry<String, String[]> param : http.params().entrySet()) {
     if(!parseOption(http, param)) {
       throw new HTTPException(SC_BAD_REQUEST, ERR_PARAM, param.getKey());
     }
    }
  }

  /**
   * Parses and sets a single database option.
   * @param http HTTP context
   * @param param current parameter
   * @return success flag
   * @throws IOException I/O exception
   */
  static boolean parseOption(final HTTPContext http, final Entry<String, String[]> param)
      throws IOException {

    final String key = param.getKey().toUpperCase(Locale.ENGLISH);
    final boolean found = http.context().prop.get(key) != null;
    if(found) http.session().execute(new Set(key, param.getValue()[0]));
    return found;
  }
}
