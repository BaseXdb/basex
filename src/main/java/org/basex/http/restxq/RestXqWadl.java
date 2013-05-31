package org.basex.http.restxq;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;

/**
 * This class returns a Web Application Description Language (WADL) file,
 * listing all available RESTXQ services.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqWadl {
  /** WADL namespace. */
  private static final String WADL = "http://wadl.dev.java.net/2009/02";

  /** Private constructor. */
  RestXqWadl() { }

  /**
   * Lists all available URIs.
   * @param http HTTP context
   * @param modules available modules
   * @throws IOException I/O exception
   */
  synchronized void create(final HTTPContext http, final HashMap<String, RestXqModule>
      modules) throws IOException {

    // create root nodes
    final FElem appl = new FElem("application", WADL).declareNS();
    final FElem ress = new FElem("resources", WADL);
    final String base = http.req.getRequestURL().toString().replaceAll(HTTPText.WADL, "");
    appl.add(ress.add("base", base));

    // create children
    final TreeMap<String, FElem> map = new TreeMap<String, FElem>();
    for(final RestXqModule mod : modules.values()) {
      for(final RestXqFunction func : mod.functions()) {
        final FElem res = new FElem("resource", WADL);
        final String path = func.path.toString();
        res.add("path", path);
        map.put(path, res);
        final FElem method = new FElem("method", WADL);
        res.add(method.add("name", func.methods.toString().replaceAll("[^A-Z ]", "")));
        final FElem request = new FElem("request", WADL);
        method.add(request);
        addParams(func.queryParams,  "query",  request);
        addParams(func.formParams,   "query",  request);
        addParams(func.headerParams, "header", request);
        final FElem response = new FElem("response", WADL);
        method.add(response);
        final FElem representation = new FElem("representation", WADL);
        response.add(representation.add("mediaType", HTTPContext.mediaType(func.output)));
      }
    }
    for(final FElem elem : map.values()) ress.add(elem);

    // serialize node
    final Serializer ser = Serializer.get(http.res.getOutputStream());
    ser.serialize(appl);
    ser.close();
  }

  /**
   * Adds parameters from the passed on request body.
   * @param params parameters
   * @param style style
   * @param root root element
   */
  private static void addParams(final ArrayList<RestXqParam> params,
      final String style, final FElem root) {

    for(final RestXqParam rxp : params) {
      root.add(new FElem("param", WADL).add("name", rxp.key).add("style", style));
    }
  }
}
