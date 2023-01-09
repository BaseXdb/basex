package org.basex.http.rest;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.http.*;
import org.basex.index.resource.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Retrieve resources via REST.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class RESTRetrieve extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  private RESTRetrieve(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    // open addressed database
    for(final Command cmd : session) run(cmd);

    final HTTPConnection conn = session.conn;
    final SerializerOptions sopts = conn.sopts();
    final OutputStream os = conn.response.getOutputStream();

    final String database = conn.db(), path = conn.dbpath();
    final boolean db = database.isEmpty(), contents = !db && run(_DB_EXISTS).equals(Text.FALSE);
    if(db || contents) {
      // list databases or its contents
      final FElem root;
      final Command cmd;
      if(db) {
        root = new FElem(RESTText.Q_DATABASES).declareNS();
        cmd = new List();
      } else {
        root = new FElem(RESTText.Q_DATABASE).declareNS().add(RESTText.NAME, database);
        cmd = new Dir(path);
      }
      final Table table = new Table(run(cmd));
      for(final TokenList list : table.contents) {
        final boolean dir = !db && eq(RESTText.DIR, list.get(1));
        final FElem elem = new FElem(db ? RESTText.Q_DATABASE : dir ? RESTText.Q_DIR :
          RESTText.Q_RESOURCE);
        if(!dir) {
          final int ll = list.size() - (db ? 1 : 0);
          for(int l = 1; l < ll; l++) elem.add(lc(table.header.get(l)), list.get(l));
        }
        root.add(elem.add(list.get(0)));
      }

      conn.initResponse();
      try(Serializer ser = Serializer.get(os, sopts)) {
        ser.serialize(root);
      }
    } else {
      // return database resource
      final ResourceType type = ResourceType.valueOf(run(_DB_TYPE).toUpperCase(Locale.ENGLISH));
      final Function function;
      if(type == ResourceType.XML) {
        function = _DB_GET;
      } else {
        sopts.set(SerializerOptions.MEDIA_TYPE, run(_DB_CONTENT_TYPE));
        context.options.set(MainOptions.SERIALIZER, sopts);
        function = type == ResourceType.BINARY ? _DB_GET_BINARY : _DB_GET_VALUE;
      }
      conn.initResponse();
      run(query(function), os);
    }
  }

  /**
   * Runs a database query.
   * @param function function
   * @return query
   * @throws HTTPException HTTP exception
   */
  private String run(final Function function) throws HTTPException {
    return run(query(function));
  }

  /**
   * Creates a database query.
   * @param function function
   * @return query
   */
  private XQuery query(final Function function) {
    final HTTPConnection conn = session.conn;
    final String query = "declare variable $db external;" +
        "declare variable $path external;" + function.args(" $db", " $path");
    return new XQuery(query).bind("db", conn.db()).bind("path", conn.dbpath());
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @return command
   */
  static RESTRetrieve get(final RESTSession session) {
    return new RESTRetrieve(session);
  }
}
