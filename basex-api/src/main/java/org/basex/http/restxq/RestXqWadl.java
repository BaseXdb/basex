package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.func.inspect.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class returns a Web Application Description Language (WADL) file,
 * listing all available RESTXQ services.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqWadl {
  /** HTTP request. */
  private final HttpServletRequest request;

  /**
   * Constructor.
   * @param request HTTP request
   */
  public RestXqWadl(final HttpServletRequest request) {
    this.request = request;
  }

  /**
   * Returns a WADL description for all available URIs.
   * @param modules available modules
   * @return WADL description
   */
  public synchronized FElem create(final HashMap<String, WebModule> modules) {
    // create root nodes
    final FElem application = new FElem(WADL + "application", WADL_URI).declareNS();
    final String base = request.getRequestURL().toString().replace(request.getRequestURI(),
        request.getContextPath());
    final FElem resources = elem("resources", application).add("base", base);

    // create children
    final TreeMap<String, FElem> map = new TreeMap<>();
    for(final WebModule module : modules.values()) {
      for(final RestXqFunction func : module.functions()) {
        if(func.path == null) continue;

        final TokenObjMap<TokenList> xqdoc = func.function.doc();
        final String path = func.path.toString();
        final String methods = func.methods.toString().replaceAll("[^A-Z ]", "");

        // create resource
        final FElem resource = new FElem(WADL + "resource", WADL_URI).add("path", path);
        map.put(path + '?' + methods, resource);

        // add documentation for path variables
        final Matcher var = Pattern.compile("\\$[^}]*").matcher(path);
        while(var.find()) {
          addParam(var.group().substring(1), "template", resource, xqdoc, func);
        }

        // create method, add function documentation
        final FElem method = elem("method", resource).add("name", methods);
        final TokenList descs = xqdoc != null ? xqdoc.get(Inspect.DOC_DESCRIPTION) : null;
        if(descs != null) for(final byte[] desc : descs) addDoc(desc, method);

        // create request
        final FElem rqst = elem("request", method);
        for(final WebParam rxp : func.queryParams)
          addParam(rxp.name, "query",  rqst, xqdoc, func);
        for(final WebParam rxp : func.formParams)
          addParam(rxp.name, "query",  rqst, xqdoc, func);
        for(final WebParam rxp : func.headerParams)
          addParam(rxp.name, "header",  rqst, xqdoc, func);

        // create response
        final FElem response = elem("response", method);
        final FElem representation = elem("representation", response);
        representation.add("mediaType", HTTPConnection.mediaType(func.output).toString());
      }
    }
    // add resources in sorted order
    for(final FElem elem : map.values()) resources.add(elem);
    return application;
  }

  /**
   * Adds a parameter and its documentation to the specified element.
   * @param name name of parameter
   * @param style style
   * @param root root element
   * @param xqdoc documentation
   * @param func function
   */
  private static void addParam(final String name, final String style, final FElem root,
                               final TokenObjMap<TokenList> xqdoc, final RestXqFunction func) {

    final FElem param = elem("param", root);
    param.add("name", name).add("style", style);
    final QNm qn = new QNm(name);
    for(final Var var : func.function.params) {
      if(var.name.eq(qn) && var.declType != null) {
        param.add("type", var.declType.toString());
      }
    }
    addDoc(Inspect.doc(xqdoc, token(name)), param);
  }

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent node
   * @return element node
   */
  private static FElem elem(final String name, final FElem parent) {
    final FElem elem = new FElem(WADL + name, WADL_URI);
    if(parent != null) parent.add(elem);
    return elem;
  }

  /**
   * Adds a documentation element to the specified element.
   * @param xqdoc documentation (may be {@code null})
   * @param parent parent node
   */
  private static void addDoc(final byte[] xqdoc, final FElem parent) {
    if(xqdoc == null) return;
    final FElem doc = elem("doc", parent);
    doc.namespaces().add(EMPTY, token(XHTML_URL));
    Inspect.add(xqdoc, doc);
  }
}
