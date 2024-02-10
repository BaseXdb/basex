package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;
import java.util.AbstractMap.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * REST-based evaluation of POST operations.
 *
 * @author BaseX Team 2005-24, BSD License
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
      throw HTTPStatus.BAD_REQUEST_X.get(ex);
    }

    try {
      final Context ctx = conn.context;
      // handle request
      final String cmd = value("local-name(*)", doc, ctx);
      if(cmd.equals(COMMANDS)) {
        final String script = DataBuilder.stripNamespace(doc, REST_URI, ctx).serialize().toString();
        return RESTCommands.get(session, script, false);
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
            throw HTTPStatus.UNKNOWN_PARAM_X.get(name);
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
      final Map<String, Map.Entry<Object, String>> bindings = new HashMap<>();
      try(QueryProcessor qp = new QueryProcessor("*/*:variable", ctx).context(doc)) {
        for(final Item item : qp.value()) {
          final String name = value("@name", item, ctx);
          final String value = value("@value", item, ctx);
          final String type = value("@type", item, ctx);
          bindings.compute(name, (k, v) -> {
            final StringList list = new StringList();
            if(v != null) {
              if(v.getKey() instanceof String[]) {
                for(final String obj : (String[]) v.getKey()) list.add(obj);
              } else if(v.getKey() instanceof String) {
                list.add(k);
              }
            }
            return new SimpleEntry<>(list.add(value).finish(), type);
          });
        }
      }

      // handle input
      String value = null;
      try(QueryProcessor qp = new QueryProcessor("*/*:context/(*|text()[normalize-space()])", ctx).
          context(doc)) {
        for(final Item item : qp.value()) {
          if(value != null) throw HTTPStatus.MULTIPLE_CONTEXTS.get();
          // create main memory instance of the specified node
          value = DataBuilder.stripNamespace((ANode) item, REST_URI, ctx).serialize().toString();
        }
      }
      if(value != null) {
        bindings.put(null, new SimpleEntry<>(value, NodeType.DOCUMENT_NODE.toString()));
      }

      // command body
      final String text = value("*/*:text/text()", doc, ctx);

      // choose evaluation
      if(cmd.equals(COMMAND)) return RESTCommands.get(session, text, true);
      if(cmd.equals(RUN)) return RESTRun.get(session, text, bindings);
      if(cmd.equals(QUERY)) return RESTQuery.get(session, text, bindings);
      throw HTTPStatus.BAD_REQUEST_X.get("Invalid POST command: " + cmd);

    } catch(final QueryException ex) {
      throw HTTPStatus.BAD_REQUEST_X.get(ex);
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
