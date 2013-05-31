package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.dom.*;

import org.basex.core.*;
import org.basex.core.cmd.Set;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTPost extends RESTCode {
  @Override
  void run(final HTTPContext http) throws IOException {
    parseOptions(http);

    String enc = http.req.getCharacterEncoding();
    if(enc == null) enc = Token.UTF8;

    // perform queries
    final byte[] in = new NewlineInput(http.req.getInputStream()).encoding(enc).content();
    validate(in);

    final Context ctx = http.context();
    DBNode doc;
    try {
      doc = new DBNode(new IOContent(in), ctx.prop);
    } catch(final IOException ex) {
      throw HTTPErr.BAD_REQUEST_X.thrw(ex);
    }

    final SerializerProp sp = new SerializerProp();
    QueryProcessor qp;
    RESTCode code;

    try {
      // handle serialization parameters
      final TokenBuilder ser = new TokenBuilder();
      qp = new QueryProcessor("*/*:parameter", ctx).context(doc);
      for(final Item param : qp.value()) {
        final String name = value("data(@name)", param, ctx);
        final String value = value("data(@value)", param, ctx);
        if(sp.get(name) != null) {
          ser.add(name).add('=').add(value).add(',');
        } else if(name.equals(WRAP)) {
          wrap(value, http);
        } else {
          HTTPErr.UNKNOWN_PARAM_X.thrw(name);
        }
      }
      http.serialization = ser.toString();

      // handle database options
      qp = new QueryProcessor("*/*:option", ctx).context(doc);
      for(final Item it : qp.value()) {
        final String name = value("data(@name)", it, ctx);
        final String value = value("data(@value)", it, ctx);
        http.session().execute(new Set(name, value));
      }

      // handle variables
      final Map<String, String[]> vars = new HashMap<String, String[]>();
      qp = new QueryProcessor("*/*:variable", ctx).context(doc);
      for(final Item it : qp.value()) {
        final String name = value("data(@name)", it, ctx);
        final String value = value("data(@value)", it, ctx);
        final String type = value("data(@type)", it, ctx);
        vars.put(name, new String[] { value, type });
      }

      // handle input
      byte[] item = null;
      qp = new QueryProcessor("*/*:context/node()", ctx).context(doc);
      for(final Item it : qp.value()) {
        if(item != null) HTTPErr.MULTIPLE_CONTEXT_X.thrw();
        // create main memory instance of the specified node
        item = DataBuilder.stripNS((ANode) it, Token.token(RESTURI), ctx).
            serialize().toArray();
      }

      // handle request
      final String request = value("local-name(*)", doc, ctx);
      final String text = value("*/*:text/text()", doc, ctx);
      if(request.equals(COMMAND)) {
        code = new RESTCommand(text);
      } else if(request.equals(RUN)) {
        code = new RESTRun(text, vars, item);
      } else {
        code = new RESTQuery(text, vars, item);
      }
      code.run(http);
    } catch(final QueryException ex) {
      HTTPErr.BAD_REQUEST_X.thrw(ex);
    }
  }

  /**
   * Returns the atomized item for the specified query.
   * @param query query
   * @param item context item
   * @param ctx database context
   * @return atomized item
   * @throws QueryException query exception
   */
  private static String value(final String query, final Item item, final Context ctx)
      throws QueryException {

    final QueryProcessor qp = new QueryProcessor(query, ctx).context(item);
    final Item it = qp.iter().next();
    return it == null ? null : Token.string(it.string(null));
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
      HTTPErr.BAD_REQUEST_X.thrw(ex);
    }
  }
}
