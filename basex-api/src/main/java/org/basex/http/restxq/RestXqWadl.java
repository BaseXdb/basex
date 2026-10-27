package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.util.*;

import jakarta.servlet.http.*;

import org.basex.http.web.*;
import org.basex.http.web.WebFunction.*;
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
 * @author BaseX Team, BSD License
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

  /** Private constructor. */
  private RestXqWadl() { }

  /**
   * Returns a WADL description for all available URIs.
   * @param request HTTP request
   * @param modules available modules
   * @return WADL description
   * @throws QueryException query exception
   */
  public static FNode create(final HttpServletRequest request,
      final HashMap<String, WebModule> modules) throws QueryException {
    // create root nodes
    final FBuilder application = FElem.build(Q_APPLICATION).ns();
    final String url = request.getRequestURL().toString();
    final String base = url.substring(0, url.length() - request.getRequestURI().length()) +
        request.getContextPath() + request.getServletPath();
    final FBuilder resources = FElem.build(Q_RESOURCES).attr(Q_BASE, base);

    // create children
    final TreeMap<String, FBuilder> map = new TreeMap<>();
    for(final WebModule module : modules.values()) {
      for(final RestXqFunction func : module.functions()) {
        if(func.path == null || func.error != null) continue;

        final TokenObjectMap<TokenList> xqdoc = func.function.doc();
        final String path = func.path.toString();
        final String methods = func.methods.toString().replaceAll("[^A-Z ]", "");

        // create resource
        final FBuilder resource = FElem.build(Q_RESOURCE).attr(Q_PATH, path);
        map.put(path.replaceAll("^/*", "/") + ' ' + methods, resource);

        // add documentation for path variables
        for(final QNm var : func.path.varNames()) {
          addParam(string(var.string()), var, "template", resource, xqdoc, func);
        }

        // create method, add function documentation
        final FBuilder method = FElem.build(Q_METHOD).attr(Q_NAME, methods);
        final TokenList descs = xqdoc != null ? xqdoc.get(Inspect.DOC_DESCRIPTION) : null;
        if(descs != null) {
          for(final byte[] desc : descs) addDoc(desc, method);
        }

        // create request
        final FBuilder rqst = FElem.build(Q_REQUEST);
        for(final WebParam wp : func.queryParams) {
          addParam(wp.name(), wp.var(), "query", rqst, xqdoc, func);
        }
        for(final WebParam wp : func.formParams) {
          addParam(wp.name(), wp.var(), "query", rqst, xqdoc, func);
        }
        for(final WebParam wp : func.headerParams) {
          addParam(wp.name(), wp.var(), "header", rqst, xqdoc, func);
        }
        method.node(rqst);

        // create response
        final FBuilder response = FElem.build(Q_RESPONSE);
        final FBuilder representation = FElem.build(Q_REPRESENTATION);
        representation.attr(Q_MEDIA_TYPE, func.sopts.mediaType(func.function.seqType()));
        response.node(representation);
        method.node(response);

        resource.node(method);
      }
    }

    // add resources in sorted order
    for(final FBuilder elem : map.values()) resources.node(elem);
    return application.node(resources).finish();
  }

  /**
   * Adds a parameter and its documentation to the specified element.
   * @param name name of parameter
   * @param var name of bound variable
   * @param style style
   * @param root root element
   * @param xqdoc documentation
   * @param func function
   * @throws QueryException query exception
   */
  private static void addParam(final String name, final QNm var, final String style,
      final FBuilder root, final TokenObjectMap<TokenList> xqdoc, final RestXqFunction func)
      throws QueryException {

    final FBuilder param = FElem.build(Q_PARAM).attr(Q_NAME, name).attr(Q_STYLE, style);
    for(final Var v : func.function.params) {
      if(v.name.eq(var) && v.declType != null) param.attr(Q_TYPE, v.declType);
    }
    addDoc(Inspect.doc(xqdoc, var.string()), param);
    root.node(param);
  }

  /**
   * Adds a documentation element to the specified element.
   * @param xqdoc documentation (can be {@code null})
   * @param parent parent node
   * @throws QueryException query exception
   */
  private static void addDoc(final byte[] xqdoc, final FBuilder parent) throws QueryException {
    if(xqdoc == null) return;
    final FBuilder doc = FElem.build(Q_DOC).ns(EMPTY, token(XHTML_URL));
    Inspect.add(xqdoc, doc);
    parent.node(doc);
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
