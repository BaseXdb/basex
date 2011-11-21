package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.Context;
import org.basex.data.Result;
import org.basex.io.IOContent;
import org.basex.io.in.BufferInput;
import org.basex.io.in.TextInput;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.DataBuilder;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ByteList;

/**
 * REST-based evaluation of POST operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class RESTPost extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    final Map<?, ?> map = ctx.req.getParameterMap();
    if(map.size() != 0) throw new RESTException(SC_BAD_REQUEST, ERR_NOPARAM);

    String enc = ctx.req.getCharacterEncoding();
    if(enc == null) enc = Token.UTF8;

    // perform queries
    final BufferInput bis = new BufferInput(ctx.in);
    final ByteList bl = new ByteList();
    for(int i = 0; (i = bis.read()) != -1;) bl.add(i);
    final IOContent cont = new IOContent(bl.toArray());
    final String input = TextInput.content(cont, enc).toString();

    final Context context = HTTPSession.context();
    Result node = null;
    try {
      node = new QueryProcessor(CHECK, input, context).execute();
    } catch(final QueryException ex) {
      throw new RESTException(SC_BAD_REQUEST, ex.getMessage().
          replaceAll("\\r?\\n+", " ").replaceAll(".*\\[\\w+\\d*\\] ", ""));
    }

    final SerializerProp sp = new SerializerProp();
    QueryProcessor qp;
    RESTCode code = null;

    try {
      // handle serialization parameters
      final TokenBuilder ser = new TokenBuilder();
      qp = new QueryProcessor("*:parameter", node, context);
      Iter ir = qp.iter();
      for(Item param; (param = ir.next()) != null;) {
        final String name = value("data(@name)", param, context);
        final String value = value("data(@value)", param, context);
        if(sp.get(name) != null) {
          ser.add(name).add('=').add(value).add(',');
        } else if(name.equals(WRAP)) {
          wrap(value, ctx);
        } else {
          throw new RESTException(SC_BAD_REQUEST, ERR_PARAM + name);
        }
      }
      ctx.serialization = ser.toString();

      // handle variables
      final Map<String, String[]> vars = new HashMap<String, String[]>();
      qp = new QueryProcessor("*:variable", node, context);
      ir = qp.iter();
      for(Item var; (var = ir.next()) != null;) {
        final String name = value("data(@name)", var, context);
        final String value = value("data(@value)", var, context);
        final String type = value("data(@type)", var, context);
        vars.put(name, new String[] { value, type });
      }

      // handle input
      byte[] item = null;
      qp = new QueryProcessor("*:context/node()", node, context);
      ir = qp.iter();
      for(Item n; (n = ir.next()) != null;) {
        if(item != null) throw new RESTException(SC_BAD_REQUEST, ERR_CTXITEM);
        // create main memory instance of the specified node
        n = DataBuilder.stripNS((ANode) n, RESTURI, qp.ctx);
        final ArrayOutput ao = new ArrayOutput();
        n.serialize(Serializer.get(ao));
        item = ao.toArray();
      }

      // handle request
      final String request = value("local-name(.)", node, context);
      final String text = value("*:text/text()", node, context);
      if(request.equals(COMMAND)) {
        code = new RESTCommand(text);
      } else if(request.equals(RUN)) {
        code = new RESTRun(text, vars, item);
      } else {
        code = new RESTQuery(text, vars, item);
      }
      code.run(ctx);
    } catch(final QueryException ex) {
      throw new RESTException(SC_BAD_REQUEST, ex.getMessage().
          replaceAll("\\r?\\n+", " ").replaceAll(".*\\[\\w+\\d*\\] ", ""));
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
  private String value(final String query, final Object item,
      final Context context) throws QueryException {
    final QueryProcessor qp = new QueryProcessor(query, item, context);
    final Item it = qp.iter().next();
    return it == null ? null : Token.string(it.string(null));
  }

  /** XQuery for checking the syntax of the POST request. */
  private static final String CHECK = new TokenBuilder().
    add("declare namespace n = \"").add(RESTURI).add("\"; ").
    add("declare variable $n := \"").add(RESTURI).add("\"; ").
    add("declare function n:c($test as item()*, $err as xs:string) { ").
    add("if($test) then () else fn:error(xs:QName(\"SYNTAX\"), $err) }; ").
    add("let $input := parse-xml(.)/* return ( ").
    add("n:c(every $x in $input/self::* satisfies namespace-uri($x) eq $n, ").
    add("\"Nodes must belong to \"\"\" || $n || \"\"\" namespace.\"), ").
    add("n:c(local-name($input) = ('query','command','run'), ").
    add("\"Invalid request: <\" || name($input) || \"/>.\"), ").
    add("n:c($input/*:text, 'Missing <text/> element.'), ").
    add("n:c($input/*:text/text(), '<text/> element has no content.'), ").
    add("for $ch in $input/* return ( ").
    add("n:c(local-name($ch) = ('text', 'parameter', ").
    add("if(local-name($input) = 'command') then () ").
    add("else ('context','variable')), ").
    add("\"Invalid child: <\" || name($ch) || \"/>.\"), ").
    add("for $p in $ch/(self::*:parameter|self::*:variable) ").
    add("let $atts := ('name','value', if(local-name($p) = 'parameter') ").
    add("then () else 'type') return ( ").
    add("n:c($p/@name, '''name'' attribute missing.'), ").
    add("n:c($p/@value, '''value'' attribute missing.'), ").
    add("for $a in $p/@* return ").
    add("n:c(local-name($a) = $atts, ").
    add("\"Invalid attribute: @\" || name($a) || \".\") ").
    add(")),$input)").toString();
}
