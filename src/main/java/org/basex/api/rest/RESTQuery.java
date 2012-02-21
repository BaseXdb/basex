package org.basex.api.rest;

import static org.basex.api.rest.RESTText.*;
import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * REST-based evaluation of XQuery expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCode {
  /** External variables. */
  protected final Map<String, String[]> variables;
  /** Query input. */
  protected final String input;
  /** Optional context item. */
  protected final byte[] item;

  /**
   * Constructor.
   * @param in query to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTQuery(final String in, final Map<String, String[]> vars,
      final byte[] it) {
    input = in;
    variables = vars;
    item = it;
  }

  @Override
  void run(final HTTPContext ctx) throws HTTPException, IOException {
    query(input, ctx);
  }

  /**
   * Evaluates the specified query.
   * @param in query input
   * @param ctx REST context
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  protected void query(final String in, final HTTPContext ctx)
      throws HTTPException, IOException {

    if(item != null) {
      // create main memory instance of the document specified as context node
      final boolean mm = ctx.session.execute(
          new Get(Prop.MAINMEM)).split(COLS)[1].equals(TRUE);
      ctx.session.execute(new Set(Prop.MAINMEM, true));
      ctx.session.create(Util.name(RESTQuery.class), new ArrayInput(item));
      if(!mm) ctx.session.execute(new Set(Prop.MAINMEM, false));
    } else {
      // open addressed database
      open(ctx);
    }

    // send serialization options to the server
    final Session session = ctx.session;
    session.execute(new Set(Prop.SERIALIZER, serial(ctx)));
    session.setOutputStream(ctx.out);

    // set query path to http path
    final Context context = HTTPSession.context();
    final String path = context.mprop.get(MainProp.HTTPPATH);
    session.execute(new Set(Prop.QUERYPATH, new IOFile(path).path()));

    try {
      // create query instance
      final Query qu = session.query(in);
      // bind external variables
      for(final Entry<String, String[]> e : variables.entrySet()) {
        final String[] val = e.getValue();
        if(val.length == 2) qu.bind(e.getKey(), val[0], val[1]);
        if(val.length == 1) qu.bind(e.getKey(), val[0]);
      }
      // initializes the response with query serialization options
      initResponse(new SerializerProp(qu.options()), ctx);
      // run query
      qu.execute();
    } catch(final IOException ex) {
      // suppress information on queried file
      final String m1 = Util.message(ex);
      final String m2 = m1.replaceAll(STOPPED_AT + ".*" + NL, "");
      throw m1.equals(m2) ? ex : new IOException(m2, ex.getCause());
    }
  }

  /**
   * Returns the serialization options.
   * @param ctx REST context
   * @return serialization options
   */
  static String serial(final HTTPContext ctx) {
    final TokenBuilder ser = new TokenBuilder(ctx.serialization);
    if(ctx.wrapping) {
      if(ser.size() != 0) ser.add(',');
      ser.addExt(SerializerProp.S_WRAP_PREFIX[0]).add('=').add(REST).add(',');
      ser.addExt(SerializerProp.S_WRAP_URI[0]).add('=').add(RESTURI);
    }
    return ser.toString();
  }
}
