package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTPost {
  /** Private constructor. */
  private RESTPost() { }

  /**
   * Creates and returns a REST command.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTCmd get(final RESTSession session) throws IOException {
    final HTTPConnection conn = session.conn;
    String encoding = conn.request.getCharacterEncoding();
    if(encoding == null) encoding = Strings.UTF8;

    // perform queries
    final DBNode doc;
    try(NewlineInput ni = new NewlineInput(conn.request.getInputStream())) {
      doc = new DBNode(new IOContent(ni.encoding(encoding).content()));
    } catch(final IOException ex) {
      throw HTTPCode.BAD_REQUEST_X.get(ex);
    }

    try {
      final Context ctx = conn.context;
      // handle request
      final String cmd = value("local-name(*)", doc, ctx);
      if(cmd.equals(COMMANDS)) {
        final String script = DataBuilder.stripNS(doc, REST_URI, ctx).serialize().toString();
        return RESTScript.get(session, script);
      }

      // handle serialization parameters
      final SerializerOptions sopts = conn.sopts();
      try(QueryProcessor qp = new QueryProcessor("*/*:parameter", ctx).context(doc)) {
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
      try(QueryProcessor qp = new QueryProcessor("*/*:option", ctx).context(doc)) {
        for(final Item item : qp.value()) {
          final String name = value("@name", item, ctx).toUpperCase(Locale.ENGLISH);
          final String value = value("@value", item, ctx);
          ctx.options.assign(name, value);
        }
      }

      // handle variables
      final Map<String, String[]> vars = new HashMap<>();
      try(QueryProcessor qp = new QueryProcessor("*/*:variable", ctx).context(doc)) {
        for(final Item item : qp.value()) {
          final String name = value("@name", item, ctx);
          final String value = value("@value", item, ctx);
          final String type = value("@type", item, ctx);
          vars.put(name, new String[] { value, type });
        }
      }

      // handle input
      String val = null;
      try(QueryProcessor qp = new QueryProcessor("*/*:context/(*|text()[normalize-space()])", ctx).
          context(doc)) {
        for(final Item item : qp.value()) {
          if(val != null) throw HTTPCode.MULTIPLE_CONTEXTS.get();
          // create main memory instance of the specified node
          val = DataBuilder.stripNS((ANode) item, REST_URI, ctx).serialize().toString();
        }
      }

      // command body
      final String text = value("*/*:text/text()", doc, ctx);

      // choose evaluation
      if(cmd.equals(COMMAND)) return RESTCommand.get(session, text);
      if(cmd.equals(RUN)) return RESTRun.get(session, text, vars, val);
      if(cmd.equals(QUERY)) return RESTQuery.get(session, text, vars, val);
      throw HTTPCode.BAD_REQUEST_X.get("Invalid POST command: " + cmd);

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

    try(QueryProcessor qp = new QueryProcessor(query, ctx).context(value)) {
      final Item item = qp.iter().next();
      return item == null ? "" : Token.string(item.string(null));
    }
  }
}
