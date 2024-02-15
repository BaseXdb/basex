package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import jakarta.servlet.http.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.func.inspect.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class returns a Web Application Description Language (WADL) document
 * with available RESTXQ services.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RestXqWadl {
  /** QName. */
  private static final QNm Q_APPLICATION = qnm("application");
  /** QName. */
  private static final QNm Q_RESOURCES = qnm("resources");
  /** QName. */
  private static final QNm Q_RESOURCE = qnm("resource");
  /** QName. */
  private static final QNm Q_DOC = qnm("doc");
  /** QName. */
  private static final QNm Q_METHOD = qnm("method");
  /** QName. */
  private static final QNm Q_REQUEST = qnm("request");
  /** QName. */
  private static final QNm Q_RESPONSE = qnm("response");
  /** QName. */
  private static final QNm Q_REPRESENTATION = qnm("representation");
  /** QName. */
  private static final QNm Q_PARAM = qnm("param");

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
   * @throws QueryException query exception
   */
  public synchronized FNode create(final HashMap<String, WebModule> modules) throws QueryException {
    // create root nodes
    final FBuilder application = FElem.build(Q_APPLICATION).declareNS();
    final String base = request.getRequestURL().toString().replace(request.getRequestURI(),
        request.getContextPath());
    final FBuilder resources = FElem.build(Q_RESOURCES).add(Q_BASE, base);

    // create children
    final TreeMap<String, FBuilder> map = new TreeMap<>();
    for(final WebModule module : modules.values()) {
      for(final RestXqFunction func : module.functions()) {
        if(func.path == null) continue;

        final TokenObjMap<TokenList> xqdoc = func.function.doc();
        final String path = func.path.toString();
        final String methods = func.methods.toString().replaceAll("[^A-Z ]", "");

        // create resource
        final FBuilder resource = FElem.build(Q_RESOURCE).add(Q_PATH, path);
        map.put(path.replaceAll("^/*", "/") + ' ' + methods, resource);

        // add documentation for path variables
        final Matcher var = Pattern.compile("\\$[^}]*").matcher(path);
        while(var.find()) {
          addParam(var.group().substring(1), "template", resource, xqdoc, func);
        }

        // create method, add function documentation
        final FBuilder method = FElem.build(Q_METHOD).add(Q_NAME, methods);
        final TokenList descs = xqdoc != null ? xqdoc.get(Inspect.DOC_DESCRIPTION) : null;
        if(descs != null) {
          for(final byte[] desc : descs) addDoc(desc, method);
        }

        // create request
        final FBuilder rqst = FElem.build(Q_REQUEST);
        for(final WebParam wp : func.queryParams) addParam(wp.name, "query", rqst, xqdoc, func);
        for(final WebParam wp : func.formParams) addParam(wp.name, "query", rqst, xqdoc, func);
        for(final WebParam wp : func.headerParams) addParam(wp.name, "header", rqst, xqdoc, func);
        method.add(rqst);

        // create response
        final FBuilder response = FElem.build(Q_RESPONSE);
        final FBuilder representation = FElem.build(Q_REPRESENTATION);
        representation.add(Q_MEDIA_TYPE, HTTPConnection.mediaType(func.sopts));
        response.add(representation);
        method.add(response);

        resource.add(method);
      }
    }

    // add resources in sorted order
    for(final FBuilder elem : map.values()) resources.add(elem);
    return application.add(resources).finish();
  }

  /**
   * Adds a parameter and its documentation to the specified element.
   * @param name name of parameter
   * @param style style
   * @param root root element
   * @param xqdoc documentation
   * @param func function
   * @throws QueryException query exception
   */
  private static void addParam(final String name, final String style, final FBuilder root,
      final TokenObjMap<TokenList> xqdoc, final RestXqFunction func) throws QueryException {

    final FBuilder param = FElem.build(Q_PARAM).add(Q_NAME, name).add(Q_STYLE, style);
    final QNm qnm = new QNm(name);
    for(final Var var : func.function.params) {
      if(var.name.eq(qnm) && var.declType != null) param.add(Q_TYPE, var.declType);
    }
    addDoc(Inspect.doc(xqdoc, token(name)), param);
    root.add(param);
  }

  /**
   * Adds a documentation element to the specified element.
   * @param xqdoc documentation (may be {@code null})
   * @param parent parent node
   * @throws QueryException query exception
   */
  private static void addDoc(final byte[] xqdoc, final FBuilder parent) throws QueryException {
    if(xqdoc == null) return;
    final FBuilder doc = FElem.build(Q_DOC).addNS(EMPTY, token(XHTML_URL));
    Inspect.add(xqdoc, doc);
    parent.add(doc);
  }

  /**
   * Creates a QName.
   * @param name name
   * @return QName
   */
  private static QNm qnm(final String name) {
    return new QNm(WADL_PREFIX, name, WADL_URI);
  }
}
