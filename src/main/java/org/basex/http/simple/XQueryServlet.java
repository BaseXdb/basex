package org.basex.http.simple;

import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.server.*;

/**
 * This servlet receives and processes XQuery requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XQueryServlet extends SimpleServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    final IOFile io = file(http);

    // authenticate user
    final LocalSession session = session(http);
    // set base path to correctly resolve local references
    session.execute(new Set(Prop.QUERYPATH, io.path()));

    // create query instance
    final Query qu = session.query(io.string());
    // bind variables
    for(final Entry<String, String[]> param : http.params().entrySet()) {
      final String[] val = param.getValue();
      qu.bind(param.getKey(), val[0], val.length == 1 ? "" : val[1]);
    }
    // initializes the response with query serialization options
    http.initResponse(new SerializerProp(qu.options()));
    // run query
    qu.execute();
  }
}
