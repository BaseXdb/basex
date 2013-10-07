package org.basex.http.rest;

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

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class RESTCode {
  /**
   * Performs the REST operation.
   * @param http HTTP context
   * @throws IOException I/O exception
   */
  abstract void run(final HTTPContext http) throws IOException;

  /**
   * Opens the addressed database.
   * @param http HTTP context
   * @throws HTTPException REST exception
   */
  static void open(final HTTPContext http) throws HTTPException {
    final String db = http.db();
    if(db == null) return;
    try {
      http.session().execute(new Open(db));
      final String path = http.dbpath();
      if(!path.isEmpty()) http.session().execute(new Cs(_DB_OPEN.args(db, path)));
    } catch(final IOException ex) {
      HTTPErr.NOT_FOUND_X.thrw(ex);
    }
  }

  /**
   * Sets the wrapping flag.
   * @param val value
   * @param http HTTP context
   * @throws HTTPException REST exception
   */
  static void wrap(final String val, final HTTPContext http) throws HTTPException {
    try {
      http.wrapping = SerializerOptions.yes(WRAP, val);
    } catch(final SerializerException ex) {
      HTTPErr.BAD_REQUEST_X.thrw(ex);
    }
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param http HTTP context
   * @return number of documents
   * @throws IOException I/O exception
   */
  protected static boolean exists(final HTTPContext http) throws IOException {
    final LocalQuery q = http.session().query(_DB_EXISTS.args("$d", "$p"));
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
    final LocalQuery q = http.session().query(_DB_IS_RAW.args("$d", "$p"));
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
    final LocalQuery q = http.session().query(_DB_CONTENT_TYPE.args("$d", "$p"));
    q.bind("d", http.db());
    q.bind("p", http.dbpath());
    return q.execute();
  }

  /**
   * Parses and sets database options.
   * Throws an exception if an option is unknown.
   * @param http HTTP context
   * @throws IOException I/O exception
   */
  static void parseOptions(final HTTPContext http) throws IOException {
    for(final Entry<String, String[]> param : http.params().entrySet()) {
      parseOption(http, param, true);
    }
  }

  /**
   * Parses and sets a single database option.
   * @param http HTTP context
   * @param param current parameter
   * @param force force execution
   * @return success flag, indicates if value was found
   * @throws IOException I/O exception
   */
  static boolean parseOption(final HTTPContext http, final Entry<String, String[]> param,
      final boolean force) throws IOException {

    final String key = param.getKey().toUpperCase(Locale.ENGLISH);
    final boolean found = http.context().options.option(key) != null;
    if(found || force) http.session().execute(new Set(key, param.getValue()[0]));
    return found;
  }
}
