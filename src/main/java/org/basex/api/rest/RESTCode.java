package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
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
   * @param ctx rest context
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  abstract void run(final HTTPContext ctx) throws HTTPException, IOException;

  /**
   * Opens the addressed database.
   * @param ctx rest context
   * @throws HTTPException REST exception
   */
  static void open(final HTTPContext ctx) throws HTTPException {
    if(ctx.db() == null) return;
    try {
      ctx.session.execute(new Open(ctx.path()));
    } catch(final IOException ex) {
      throw new HTTPException(SC_NOT_FOUND, ex.getMessage());
    }
  }

  /**
   * Sets the wrapping flag.
   * @param val value
   * @param ctx rest context
   * @throws HTTPException REST exception
   */
  static void wrap(final String val, final HTTPContext ctx)
      throws HTTPException {

    ctx.wrapping = Util.yes(val);
    if(!ctx.wrapping && !Util.no(val)) {
      try {
        SerializerProp.error(WRAP, val, Text.YES, Text.NO);
      } catch(final SerializerException ex) {
        throw new HTTPException(SC_BAD_REQUEST, ex.getMessage());
      }
    }
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param ctx REST context
   * @return number of documents
   * @throws IOException I/O exception
   */
  protected static boolean exists(final HTTPContext ctx) throws IOException {
    final Query q = ctx.session.query(_DB_EXISTS.args("$d", "$p"));
    q.bind("d", ctx.db());
    q.bind("p", ctx.dbpath());
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Checks if the specified path points to a binary resource.
   * @param ctx REST context
   * @return result of check
   * @throws IOException I/O exception
   */
  protected static boolean isRaw(final HTTPContext ctx) throws IOException {
    final Query q = ctx.session.query(_DB_IS_RAW.args("$d", "$p"));
    q.bind("d", ctx.db());
    q.bind("p", ctx.dbpath());
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Returns the content type of a database resource.
   * @param ctx REST context
   * @return content type
   * @throws IOException I/O exception
   */
  protected static String contentType(final HTTPContext ctx)
      throws IOException {

    final Query q = ctx.session.query(_DB_CONTENT_TYPE.args("$d", "$p"));
    q.bind("d", ctx.db());
    q.bind("p", ctx.dbpath());
    return q.execute();
  }

  /**
   * Returns all query parameters.
   * @param ctx rest context
   * @return parameters
   */
  static Map<String, String[]> params(final HTTPContext ctx) {
    final Map<String, String[]> params = new HashMap<String, String[]>();
    final Map<?, ?> map = ctx.req.getParameterMap();
    for(final Entry<?, ?> s : map.entrySet()) {
      final String key = s.getKey().toString();
      final String[] vals = s.getValue() instanceof String[] ?
          (String[]) s.getValue() : new String[] { s.getValue().toString() };
      params.put(key, vals);
    }
    return params;
  }

  /**
   * Parses and sets database options.
   * Throws an exception if an option is unknown.
   * @param ctx rest context
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  static void parseOptions(final HTTPContext ctx)
      throws HTTPException, IOException {

    for(final Entry<String, String[]> param : params(ctx).entrySet()) {
     if(!parseOption(ctx, param)) {
       throw new HTTPException(SC_BAD_REQUEST, ERR_PARAM, param.getKey());
     }
    }
  }

  /**
   * Parses and sets a single database option.
   * @param ctx rest context
   * @param param current parameter
   * @return success flag
   * @throws IOException I/O exception
   */
  static boolean parseOption(final HTTPContext ctx,
      final Entry<String, String[]> param) throws IOException {

    final String key = param.getKey().toUpperCase(Locale.ENGLISH);
    final boolean found = HTTPSession.context().prop.get(key) != null;
    if(found) ctx.session.execute(new Set(key, param.getValue()[0]));
    return found;
  }
}
