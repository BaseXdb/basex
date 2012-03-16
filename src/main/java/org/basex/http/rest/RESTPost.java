package org.basex.http.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.rest.RESTSchema.*;
import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * REST-based evaluation of POST operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTPost extends RESTCode {
  /** Validator for POST schemas. */
  private static final Validator VALIDATOR;

  static {
    Validator v = null;
    try {
      v = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
          newSchema(new StreamSource(new ArrayInput(POST))).newValidator();
    } catch(final SAXException ex) {
      Util.notexpected(ex);
    }
    VALIDATOR = v;
  }

  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    parseOptions(http);

    String enc = http.req.getCharacterEncoding();
    if(enc == null) enc = Token.UTF8;

    // perform queries
    final byte[] in = new NewlineInput(http.in, enc).content();
    validate(in);

    final Context ctx = http.context();
    DBNode doc;
    try {
      doc = new DBNode(new IOContent(in), ctx.prop);
    } catch(final IOException ex) {
      throw new HTTPException(SC_BAD_REQUEST, ex.getMessage());
    }

    final SerializerProp sp = new SerializerProp();
    QueryProcessor qp;
    RESTCode code;

    try {
      // handle serialization parameters
      final TokenBuilder ser = new TokenBuilder();
      qp = new QueryProcessor("*/*:parameter", ctx).context(doc);
      Iter ir = qp.iter();
      for(Item param; (param = ir.next()) != null;) {
        final String name = value("data(@name)", param, ctx);
        final String value = value("data(@value)", param, ctx);
        if(sp.get(name) != null) {
          ser.add(name).add('=').add(value).add(',');
        } else if(name.equals(WRAP)) {
          wrap(value, http);
        } else {
          throw new HTTPException(SC_BAD_REQUEST, ERR_PARAM, name);
        }
      }
      http.serialization = ser.toString();

      // handle database options
      qp = new QueryProcessor("*/*:option", ctx).context(doc);
      ir = qp.iter();
      for(Item opt; (opt = ir.next()) != null;) {
        final String name = value("data(@name)", opt, ctx);
        final String value = value("data(@value)", opt, ctx);
        http.session().execute(new Set(name, value));
      }

      // handle variables
      final Map<String, String[]> vars = new HashMap<String, String[]>();
      qp = new QueryProcessor("*/*:variable", ctx).context(doc);
      ir = qp.iter();
      for(Item var; (var = ir.next()) != null;) {
        final String name = value("data(@name)", var, ctx);
        final String value = value("data(@value)", var, ctx);
        final String type = value("data(@type)", var, ctx);
        vars.put(name, new String[] { value, type });
      }

      // handle input
      byte[] item = null;
      qp = new QueryProcessor("*/*:context/node()", ctx).context(doc);
      ir = qp.iter();
      for(Item n; (n = ir.next()) != null;) {
        if(item != null) throw new HTTPException(SC_BAD_REQUEST, ERR_CTXITEM);
        // create main memory instance of the specified node
        n = DataBuilder.stripNS((ANode) n, RESTURI, ctx);
        final ArrayOutput ao = new ArrayOutput();
        n.serialize(Serializer.get(ao));
        item = ao.toArray();
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
      throw new HTTPException(SC_BAD_REQUEST, ex.getLocalizedMessage());
    }
  }

  /**
   * Returns the atomized item for the specified query.
   * @param query query
   * @param item context item
   * @param context database context
   * @return atomized item
   * @throws QueryException query exception
   */
  private static String value(final String query, final Item item, final Context context)
      throws QueryException {

    final QueryProcessor qp = new QueryProcessor(query, context).context(item);
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
      VALIDATOR.validate(new DOMSource(db.parse(new ArrayInput(input))));
    } catch(final Exception ex) {
      // validation fails
      throw new HTTPException(SC_BAD_REQUEST, ex.getMessage());
    }
  }
}
