package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HTTPText.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Request parser.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class RequestParser {
  /** Input information (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public RequestParser(final InputInfo info) {
    this.info = info;
  }

  /**
   * Parses an http:request element.
   * @param node request element (can be {@code null})
   * @param bodies request bodies
   * @return parsed request
   * @throws QueryException query exception
   */
  public Request parse(final GNode node, final Value bodies) throws QueryException {
    final Request request = new Request();

    if(node != null) {
      for(final GNode attr : node.attributeIter()) {
        final String key = string(attr.name());
        final RequestAttribute attribute = Enums.get(RequestAttribute.class, key);
        if(attribute == null) throw HC_REQ_X.get(info, "Unknown attribute: " + key);
        assign(request, attribute, string(attr.string()));
      }
      checkRequest(request);

      // it is an error if content is set for HTTP methods that do not allow bodies
      final GNode body = parseHeaders(node.childIter(), request.headers);
      final String error = request.invalid();
      if(error != null) throw HC_REQ_X.get(info, error);
      if(body != null) {
        final QNm pl = body.qname();
        // single part request
        if(pl.eq(Q_HTTP_BODY)) {
          parseBody(body, bodies, request.payloadAtts, request.payload);
        // multipart request
        } else if(pl.eq(Q_HTTP_MULTIPART)) {
          parseMultipart(body, bodies.iter(), request.payloadAtts, request.parts);
          request.isMultipart = true;
        } else {
          throw HC_REQ_X.get(info, "Unknown payload element: " + body.qname());
        }
      }
    }
    return request;
  }

  /**
   * Parses the attributes of an element.
   * @param element element
   * @param atts map for parsed attributes
   */
  private static void parseAtts(final GNode element, final Map<String, String> atts) {
    for(final GNode attr : element.attributeIter()) {
      atts.put(string(attr.name()), string(attr.string()));
    }
  }

  /**
   * Parses <http:header/> children of requests and parts.
   * @param iter iterator on request/part children
   * @param headers map for parsed headers
   * @return next non-header element (can be {@code null})
   */
  private static GNode parseHeaders(final BasicNodeIter iter, final Map<String, String> headers) {
    for(final GNode node : iter) {
      final QNm nm = node.qname();
      if(nm == null) continue;
      if(!nm.eq(Q_HTTP_HEADER)) return node;

      String name = "", value = "";
      for(final GNode attr : node.attributeIter()) {
        final QNm qn = attr.qname();
        if(qn.equals(Q_NAME)) name = string(attr.string());
        else if(qn.equals(Q_VALUE)) value = string(attr.string());
      }
      // an empty value is legal HTTP; repeated names are merged (RFC 9110, RFC 6265)
      final String sep = name.equalsIgnoreCase(COOKIE) ? "; " : ", ";
      if(!name.isEmpty()) headers.merge(name, value, (a, b) -> a + sep + b);
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
  private void parseBody(final GNode body, final Value items, final Map<String, String> atts,
      final ItemList payload) throws QueryException {

    parseAtts(body, atts);
    checkBody(body, atts);

    if(atts.get(SRC) == null) {
      // no linked resource for setting request content
      if(items.isEmpty()) {
        // payload is taken from children of <http:body/> element
        for(final GNode node : body.childIter()) payload.add(node);
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
  private void parseMultipart(final GNode multipart, final BasicIter<Item> bodies,
      final HashMap<String, String> atts, final ArrayList<Part> parts) throws QueryException {

    parseAtts(multipart, atts);
    if(atts.get(SerializerOptions.MEDIA_TYPE.name()) == null)
      throw HC_REQ_X.get(info, "Attribute media-type of http:multipart is mandatory");

    final BasicNodeIter iter = multipart.childIter();
    while(true) {
      final Part part = new Part();
      final GNode payload = parseHeaders(iter, part.headers);
      if(payload == null) break;
      // a part payload must be an <http:body/> element
      if(!payload.qname().eq(Q_HTTP_BODY))
        throw HC_REQ_X.get(info, "Unknown payload element: " + payload.qname());
      // a part loading its content from 'src' takes no item from the $bodies sequence;
      // otherwise the content is set from <http:body/> children or from $bodies
      boolean src = false;
      for(final GNode attr : payload.attributeIter()) {
        if(string(attr.name()).equals(SRC)) { src = true; break; }
      }
      final Item body = src ? null : bodies.next();
      parseBody(payload, body == null ? Empty.VALUE : body, part.attributes, part.contents);
      parts.add(part);
    }
  }

  /**
   * Assigns the value of a request attribute.
   * @param request request
   * @param attribute attribute
   * @param value attribute value
   * @throws QueryException query exception
   */
  private void assign(final Request request, final RequestAttribute attribute, final String value)
      throws QueryException {
    switch(attribute) {
      case HREF -> request.href = value;
      case METHOD -> request.method = value.toUpperCase(Locale.ENGLISH);
      case USERNAME -> request.username = value;
      case PASSWORD -> request.password = value;
      case OVERRIDE_MEDIA_TYPE -> request.overrideMediaType = value;
      case CSV -> request.csv = value;
      case XML -> request.xml = value;
      case PROXY -> request.proxy(value, info);
      case VERIFY -> request.verify = bool(attribute, value);
      case JSON -> request.json = value;
      case HTML -> request.html = value;
      case COOKIES -> request.cookies = bool(attribute, value);
      case STATUS_ONLY ->
        request.bodyMode = bool(attribute, value) ? BodyMode.NONE : BodyMode.PARSE;
      case FOLLOW_REDIRECT -> request.redirects(value, info);
      case SEND_AUTHORIZATION -> request.sendAuthorization = bool(attribute, value);
      case TIMEOUT -> request.timeout(value, info);
      // the authentication method also applies to credentials in the URI
      case AUTH_METHOD -> request.authMethod(value, info);
    }
  }

  /**
   * Returns the boolean value of a request attribute.
   * @param attribute attribute
   * @param value attribute value
   * @return boolean value
   * @throws QueryException query exception
   */
  private boolean bool(final RequestAttribute attribute, final String value)
      throws QueryException {
    if(Strings.isTrue(value)) return true;
    if(Strings.isFalse(value)) return false;
    throw HC_REQ_X.get(info, "Value of '" + attribute + "' attribute is no boolean: " + value);
  }

  /**
   * Checks consistency of attributes for <http:request/>.
   * @param request request
   * @throws QueryException query exception
   */
  private void checkRequest(final Request request) throws QueryException {
    // method denotes the HTTP verb and is mandatory
    if(request.method == null)
      throw HC_REQ_X.get(info, "Missing attribute: " + RequestAttribute.METHOD);
    // a password is required if a username is supplied
    if(request.username != null && request.password == null)
      throw HC_REQ_X.get(info, "Missing attribute: " + RequestAttribute.PASSWORD);
  }

  /**
   * Checks consistency of attributes for <http:body/>.
   * @param body body element
   * @param bodyAtts body attributes
   * @throws QueryException query exception
   */
  private void checkBody(final GNode body, final Map<String, String> bodyAtts)
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
