package org.basex.http.rest;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Lists REST resources.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class RESTList extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  RESTList(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    String result = "";
    for(final Command cmd : session.commands) result = run(cmd);

    final Table table = new Table(result);
    final FElem el = new FElem(RESTText.Q_DATABASES).declareNS();
    el.add(RESTText.RESOURCES, token(table.contents.size()));
    list(table, el, RESTText.Q_DATABASE, 1);

    final HTTPContext http = session.http;
    http.initResponse();
    try(Serializer ser = Serializer.get(http.res.getOutputStream(), http.sopts())) {
      ser.serialize(el);
    }
  }
}
