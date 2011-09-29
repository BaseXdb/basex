package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.data.DataText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.func.Function.*;

import java.io.IOException;

import org.basex.core.Text;
import org.basex.core.cmd.Open;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Query;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class RESTCode {
  /**
   * Performs the REST operation.
   * @param ctx rest context
   * @throws RESTException REST exception
   * @throws IOException I/O exception
   */
  abstract void run(final RESTContext ctx) throws RESTException, IOException;

  /**
   * Initializes the output. Sets the expected encoding and content type.
   * @param ctx rest context
   * @param sprop serialization properties
   */
  void initResponse(final SerializerProp sprop, final RESTContext ctx) {
    // set encoding
    ctx.res.setCharacterEncoding(sprop.get(SerializerProp.S_ENCODING));

    // set content type
    String type = sprop.get(SerializerProp.S_MEDIA_TYPE);
    if(type.isEmpty()) {
      // determine content type dependent on output method
      final String method = sprop.get(SerializerProp.S_METHOD);
      if(method.equals(M_RAW)) {
        type = APP_OCTET;
      } else if(method.equals(M_XML)) {
        type = APP_XML;
      } else if(Token.eq(method, M_JSON, M_JSONML)) {
        type = APP_JSON;
      } else if(Token.eq(method, M_XHTML, M_HTML)) {
        type = TEXT_HTML;
      } else {
        type = TEXT_PLAIN;
      }
    }
    ctx.res.setContentType(type);
  }

  /**
   * Opens the addressed database.
   * @param ctx rest context
   * @throws RESTException REST exception
   */
  void open(final RESTContext ctx) throws RESTException {
    if(ctx.db() == null) return;
    try {
      ctx.session.execute(new Open(ctx.all()));
    } catch(final IOException ex) {
      throw new RESTException(SC_NOT_FOUND, ex.getMessage());
    }
  }

  /**
   * Sets the wrapping flag.
   * @param val value
   * @param ctx rest context
   * @throws RESTException REST exception
   */
  void wrap(final String val, final RESTContext ctx) throws RESTException {
    ctx.wrapping = Util.yes(val);
    if(!ctx.wrapping && !Util.no(val)) {
      throw new RESTException(SC_BAD_REQUEST, SerializerProp.error(WRAP,
          val, Text.YES, Text.NO).getMessage());
    }
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param ctx REST context
   * @return number of documents
   * @throws IOException I/O exception
   */
  protected static boolean exists(final RESTContext ctx) throws IOException {
    final Query q = ctx.session.query(DBEXISTS.args("$d", "$p"));
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
  protected static boolean isRaw(final RESTContext ctx) throws IOException {
    final Query q = ctx.session.query(DBISRAW.args("$d", "$p"));
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
  protected static String contentType(final RESTContext ctx)
      throws IOException {

    final Query q = ctx.session.query(DBCTYPE.args("$d", "$p"));
    q.bind("d", ctx.db());
    q.bind("p", ctx.dbpath());
    return q.execute();
  }
}
