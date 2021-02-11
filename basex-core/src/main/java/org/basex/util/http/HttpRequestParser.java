package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.http.HttpRequest.*;

/**
 * Request parser.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class HttpRequestParser {
  /** Input information. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   */
  public HttpRequestParser(final InputInfo info) {
    this.info = info;
  }

  /**
   * Parses an http:request element.
   * @param request request element (can be {@code null})
   * @param bodies request bodies
   * @return parsed request
   * @throws QueryException query exception
   */
  public HttpRequest parse(final ANode request, final Value bodies) throws QueryException {
    final HttpRequest hr = new HttpRequest();

    if(request != null) {
      for(final ANode attr : request.attributeIter()) {
        final String key = string(attr.name());
        final Request r = Request.get(key);
        if(r == null) throw HC_REQ_X.get(info, "Unknown attribute: " + key);
        hr.attributes.put(r, string(attr.string()));
      }
      checkRequest(hr);

      // it is an error if content is set for HTTP methods that do not allow bodies
      final ANode body = parseHeaders(request.childIter(), hr.headers);
      final String method = hr.attribute(Request.METHOD);
      if(Strings.eq(method, TRACE, DELETE) && (body != null || !bodies.isEmpty()))
        throw HC_REQ_X.get(info, "Body not expected for method " + method);

      if(body != null) {
        final QNm pl = body.qname();
        // single part request
        if(pl.eq(Q_BODY)) {
          parseBody(body, bodies, hr.payloadAtts, hr.payload);
          hr.isMultipart = false;
          // multipart request
        } else if(pl.eq(Q_MULTIPART)) {
          parseMultipart(body, bodies.iter(), hr.payloadAtts, hr.parts);
          hr.isMultipart = true;
        } else {
          throw HC_REQ_X.get(info, "Unknown payload element: " + body.qname());
        }
      }
    }
    return hr;
  }

  /**
   * Parses the attributes of an element.
   * @param element element
   * @param atts map for parsed attributes
   */
  private static void parseAtts(final ANode element, final Map<String, String> atts) {
    for(final ANode attr : element.attributeIter()) {
      atts.put(string(attr.name()), string(attr.string()));
    }
  }

  /**
   * Parses <http:header/> children of requests and parts.
   * @param iter iterator on request/part children
   * @param headers map for parsed headers
   * @return next non-header element (or {@code null})
   */
  private static ANode parseHeaders(final BasicNodeIter iter, final Map<String, String> headers) {
    for(final ANode node : iter) {
      final QNm nm = node.qname();
      if(nm == null) continue;
      if(!nm.eq(Q_HEADER)) return node;

      String name = "", value = "";
      for(final ANode attr : node.attributeIter()) {
        final String qn = string(attr.qname().local());
        if(qn.equals(NAME)) name = string(attr.string());
        else if(qn.equals(VALUE)) value = string(attr.string());
      }
      if(!name.isEmpty() && !value.isEmpty()) headers.put(name, value);
    }
    return null;
  }

  /**
   * Parses <http:body/> element.
   * @param body body element
   * @param items bodies
   * @param atts map for parsed body attributes
   * @param payload payload
   * @throws QueryException query exception
   */
  private void parseBody(final ANode body, final Value items, final Map<String, String> atts,
      final ItemList payload) throws QueryException {

    parseAtts(body, atts);
    checkBody(body, atts);

    if(atts.get(SRC) == null) {
      // no linked resource for setting request content
      if(items.isEmpty()) {
        // payload is taken from children of <http:body/> element
        for(final ANode node : body.childIter()) payload.add(node);
      } else {
        // payload is taken from $bodies parameter
        for(final Item item : items) payload.add(item);
      }
    }
  }

  /**
   * Parses a <http:multipart/> element.
   * @param multipart multipart element
   * @param bodies request bodies
   * @param atts map for multipart attributes
   * @param parts list for multipart parts
   * @throws QueryException query exception
   */
  private void parseMultipart(final ANode multipart, final BasicIter<Item> bodies,
      final HashMap<String, String> atts, final ArrayList<Part> parts) throws QueryException {

    parseAtts(multipart, atts);
    if(atts.get(SerializerOptions.MEDIA_TYPE.name()) == null)
      throw HC_REQ_X.get(info, "Attribute media-type of http:multipart is mandatory");

    final BasicNodeIter iter = multipart.childIter();
    while(true) {
      final Part part = new Part();
      final ANode payload = parseHeaders(iter, part.headers);
      if(payload == null) break;
      // content is set from <http:body/> children or from $bodies parameter
      final Item body = bodies.next();
      parseBody(payload, body == null ? Empty.VALUE : body, part.bodyAtts, part.bodyContents);
      parts.add(part);
    }
  }

  /**
   * Checks consistency of attributes for <http:request/>.
   * @param request request
   * @throws QueryException query exception
   */
  private void checkRequest(final HttpRequest request) throws QueryException {
    // method denotes the HTTP verb and is mandatory
    final String mth = request.attribute(Request.METHOD);
    if(mth == null) throw HC_REQ_X.get(info, "Missing attribute: " + Request.METHOD);
    request.attributes.put(Request.METHOD, mth.toUpperCase(Locale.ENGLISH));

    // check parameters needed in case of authorization
    final String us = request.attribute(Request.USERNAME);
    if(us != null) {
      // check if password is supplied
      final String pw = request.attribute(Request.PASSWORD);
      if(pw == null) throw HC_REQ_X.get(info, "Missing attribute: " + Request.PASSWORD);
      // check if authorization method is supplied (default is 'Basic')
      final String am = request.attribute(Request.AUTH_METHOD);
      if(am != null) {
        final AuthMethod method = StaticOptions.AUTHMETHOD.get(am);
        if(method == null) throw HC_REQ_X.get(info, "Invalid authentication method: " + am);
        request.authMethod = method;
      }
    }

    // check other parameters
    final String timeout = request.attribute(Request.TIMEOUT);
    if(timeout != null && Strings.toInt(timeout) < 0)
      throw HC_REQ_X.get(info, "Invalid timeout: " + timeout);

    for(final Request r : new Request[] {
      Request.FOLLOW_REDIRECT, Request.STATUS_ONLY, Request.SEND_AUTHORIZATION
    }) {
      final String s = request.attribute(r);
      if(s != null && !Strings.eq(s, Text.TRUE, Text.FALSE))
        throw HC_REQ_X.get(info, "Value of '" + r + "' attribute is no boolean: " + s);
    }
  }

  /**
   * Checks consistency of attributes for <http:body/>.
   * @param body body element
   * @param bodyAtts body attributes
   * @throws QueryException query exception
   */
  private void checkBody(final ANode body, final Map<String, String> bodyAtts)
      throws QueryException {

    // @media-type is mandatory
    if(bodyAtts.get(SerializerOptions.MEDIA_TYPE.name()) == null)
      throw HC_REQ_X.get(info, "Attribute media-type of http:body is mandatory");

    // if src attribute is used to set the content of the body, no
    // other attributes must be specified and no content must be present
    if(bodyAtts.get(SRC) != null && (bodyAtts.size() > 2 || body.childIter().next() != null))
      throw HC_ATTR.get(info);
  }
}
