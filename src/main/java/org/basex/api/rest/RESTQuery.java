package org.basex.api.rest;

import static org.basex.api.rest.RESTText.*;
import static org.basex.data.DataText.*;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.TokenBuilder;

/**
 * REST-based evaluation of XQuery expressions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCode {
  /** External variables. */
  protected Map<String, String[]> variables;
  /** Query input. */
  protected final String input;

  /**
   * Constructor.
   * @param in query to be executed
   * @param vars external variables
   */
  RESTQuery(final String in, final Map<String, String[]> vars) {
    input = in;
    variables = vars;
  }

  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    query(input, ctx);
  }

  /**
   * Evaluates the specified query.
   * @param in query input
   * @param ctx REST context
   * @throws RESTException REST exception
   * @throws IOException I/O exception
   */
  protected void query(final String in, final RESTContext ctx)
      throws RESTException, IOException {

    // try to open addressed database
    open(ctx);

    // set specified serialization options
    final Session session = ctx.session;
    session.execute(new Set(Prop.SERIALIZER, serial(ctx)));

    // check if path points to a raw file
    String query = in.isEmpty() ? "." : in;
    if(query.equals(".") && ctx.depth() > 1) {
      // retrieve binary contents if no query is specified
      final String args = "'" + ctx.db() + "','" + ctx.dbpath() + "'";
      final String raw = "db:is-raw(" + args + ")";
      if(session.query(raw).execute().equals("true"))
        query = "declare option output:method '" + M_RAW + "';" +
            "db:get(" + args + ")";
    }

    // redirect output stream
    session.setOutputStream(ctx.out);

    // create query instance
    final Query qu = session.query(query);
    // bind external variables
    for(final Entry<String, String[]> e : variables.entrySet()) {
      final String[] val = e.getValue();
      if(val.length == 2) qu.bind(e.getKey(), val[0], val[1]);
      if(val.length == 1) qu.bind(e.getKey(), val[0]);
    }

    // initializes the output
    initOutput(new SerializerProp(qu.options()), ctx);

    // run query
    qu.execute();
  }

  /**
   * Returns the serialization options.
   * @param ctx REST context
   * @return serialization options
   */
  String serial(final RESTContext ctx) {
    final TokenBuilder ser = new TokenBuilder(ctx.serialization);
    if(ctx.wrapping) {
      if(ser.size() != 0) ser.add(',');
      ser.addExt(SerializerProp.S_WRAP_PREFIX[0]).add('=').add(REST).add(',');
      ser.addExt(SerializerProp.S_WRAP_URI[0]).add('=').add(RESTURI);
    }
    return ser.toString();
  }
}
