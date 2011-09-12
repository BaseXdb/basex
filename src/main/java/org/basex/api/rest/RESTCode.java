package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.data.DataText.*;

import java.io.IOException;

import org.basex.core.cmd.Open;
import org.basex.data.DataText;
import org.basex.io.serial.SerializerProp;

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
  void initOutput(final SerializerProp sprop, final RESTContext ctx) {
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
      } else if(method.equals(M_JSON) || method.equals(M_JSONML)) {
        type = APP_JSON;
      } else if(method.equals(M_XHTML) || method.equals(M_HTML)) {
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
    } catch(IOException ex) {
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
    ctx.wrapping = DataText.YES.equals(val);
    if(!ctx.wrapping && !DataText.NO.equals(val)) {
      throw new RESTException(SC_BAD_REQUEST, SerializerProp.error(WRAP,
          val, DataText.YES, DataText.NO).getMessage());
    }
  }
}
