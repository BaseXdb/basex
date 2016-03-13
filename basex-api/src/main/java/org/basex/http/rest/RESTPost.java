package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.dom.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * REST-based evaluation of POST operations.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class RESTPost {
  /** Private constructor. */
  private RESTPost() { }

  /**
   * Creates REST code.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTCmd get(final RESTSession session) throws IOException {
    final HTTPContext http = session.http;
    String enc = http.req.getCharacterEncoding();
    if(enc == null) enc = Strings.UTF8;

    // perform queries
    final byte[] input = new NewlineInput(http.req.getInputStream()).encoding(enc).content();
    validate(input);

    final Context ctx = session.context;
    final DBNode doc;
    try {
      doc = new DBNode(new IOContent(input));
    } catch(final IOException ex) {
      throw HTTPCode.BAD_REQUEST_X.get(ex);
    }

    try {
      // handle serialization parameters
      final SerializerOptions sopts = http.sopts();
      try(final QueryProcessor qp = new QueryProcessor("*/*:parameter", ctx).context(doc)) {
        for(final Item param : qp.value()) {
          final String name = value("@name", param, ctx);
          final String value = value("@value", param, ctx);
          if(sopts.option(name) != null) {
            sopts.assign(name, value);
          } else {
            throw HTTPCode.UNKNOWN_PARAM_X.get(name);
          }
        }
      }

      // handle database options
      try(final QueryProcessor qp = new QueryProcessor("*/*:option", ctx).context(doc)) {
        for(final Item it : qp.value()) {
          final String name = value("@name", it, ctx).toUpperCase(Locale.ENGLISH);
          final String value = value("@value", it, ctx);
          ctx.options.assign(name, value);
        }
      }

      // handle variables
      final Map<String, String[]> vars = new HashMap<>();
      try(final QueryProcessor qp = new QueryProcessor("*/*:variable", ctx).context(doc)) {
        for(final Item it : qp.value()) {
          final String name = value("@name", it, ctx);
          final String value = value("@value", it, ctx);
          final String type = value("@type", it, ctx);
          vars.put(name, new String[] { value, type });
        }
      }

      // handle input
      String val = null;
      try(final QueryProcessor qp = new QueryProcessor(
          "*/*:context/(*, text()[normalize-space()])", ctx).context(doc)) {
        for(final Item it : qp.value()) {
          if(val != null) throw HTTPCode.MULTIPLE_CONTEXT_X.get();
          // create main memory instance of the specified node
          val = DataBuilder.stripNS((ANode) it, Token.token(REST_URI), ctx).serialize().toString();
        }
      }

      // handle request
      final String request = value("local-name(*)", doc, ctx);
      final String text = value("*/*:text/text()", doc, ctx);

      if(request.equals(COMMAND)) return RESTCommand.get(session, text);
      if(request.equals(RUN)) return RESTRun.get(session, text, vars, val);
      return RESTQuery.get(session, text, vars, val);

    } catch(final QueryException ex) {
      throw HTTPCode.BAD_REQUEST_X.get(ex);
    }
  }

  /**
   * Returns the atomized item for the specified query.
   * @param query query
   * @param value context value
   * @param ctx database context
   * @return atomized item
   * @throws QueryException query exception
   */
  private static String value(final String query, final Item value, final Context ctx)
      throws QueryException {

    try(final QueryProcessor qp = new QueryProcessor(query, ctx).context(value)) {
      final Item it = qp.iter().next();
      return it == null ? null : Token.string(it.string(null));
    }
  }

  /**
   * Validates the specified XML input against the POST schema.
   * @param input input document
   * @throws HTTPException exception
   */
  private static void validate(final byte[] input) throws HTTPException {
    try {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      final DocumentBuilder db = dbf.newDocumentBuilder();
      RESTSchema.newValidator().validate(new DOMSource(db.parse(new ArrayInput(input))));
    } catch(final Exception ex) {
      Util.debug("Error while validating \"" + Token.string(input) + '"');
      // validation fails
      throw HTTPCode.BAD_REQUEST_X.get(ex);
    }
  }
}
