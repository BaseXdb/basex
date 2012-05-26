package org.basex.query.util.http;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.http.HTTPRequest.Part;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Request parser.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPRequestParser {
  /** Input information. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param ii input info
   */
  public HTTPRequestParser(final InputInfo ii) {
    info = ii;
  }

  /**
   * Parses an <http:request/> element.
   * @param request request element
   * @param bodies content items
   * @return parsed request
   * @throws QueryException query exception
   */
  public HTTPRequest parse(final ANode request, final ValueBuilder bodies)
      throws QueryException {

    final HTTPRequest r = new HTTPRequest();
    parseAttrs(request, r.attrs);
    checkRequest(r);

    final ANode payload = parseHdrs(request.children(), r.headers);
    final byte[] method = lc(r.attrs.get(METHOD));

    // it is an error if content is set for HTTP verbs which must be empty
    if(eq(method, TRACE, DELETE) && (payload != null || bodies != null))
      HC_REQ.thrw(info, "Body not expected for method " + string(method));

    if(payload != null) {
      final QNm pl = payload.qname();
      // single part request
      if(pl.eq(HTTP_BODY)) {
        Item it = null;
        if(bodies != null) {
          // $bodies must contain exactly one item
          if(bodies.size() != 1) HC_REQ.thrw(info,
              "Number of items with request body content differs " +
              "from number of body descriptors.");
          it = bodies.next();
        }
        parseBody(payload, it, r.payloadAttrs, r.bodyContent);
        r.isMultipart = false;
        // multipart request
      } else if(pl.eq(HTTP_MULTIPART)) {
        int i = 0;
        final AxisMoreIter ch = payload.children();
        while(ch.next() != null)
          i++;
        // number of items in $bodies must be equal to number of body
        // descriptors
        if(bodies != null && bodies.size() != i) HC_REQ.thrw(info,
            "Number of items with request body content differs " +
            "from number of body descriptors.");
        parseMultipart(payload, bodies, r.payloadAttrs, r.parts);
        r.isMultipart = true;
      } else {
        HC_REQ.thrw(info, "Unknown payload element " + pl);
      }
    }
    return r;
  }

  /**
   * Parses the attributes of an element.
   * @param element element
   * @param attrs map for parsed attributes
   */
  private static void parseAttrs(final ANode element, final TokenMap attrs) {
    final AxisIter elAttrs = element.attributes();
    for(ANode attr; (attr = elAttrs.next()) != null;) {
      attrs.add(attr.name(), attr.string());
    }
  }

  /**
   * Parses <http:header/> children of requests and parts.
   * @param i iterator on request/part children
   * @param hdrs map for parsed headers
   * @return body or multipart
   */
  private static ANode parseHdrs(final AxisMoreIter i, final TokenMap hdrs) {
    ANode n;
    while(true) {
      n = i.next();
      if(n == null) break;
      final QNm nm = n.qname();
      if(nm == null) continue;
      if(!nm.eq(HTTP_HEADER)) break;

      final AxisIter hdrAttrs = n.attributes();
      byte[] name = null;
      byte[] value = null;

      for(ANode attr; (attr = hdrAttrs.next()) != null;) {
        final QNm qn = attr.qname();
        if(qn.eq(Q_NAME)) name = attr.string();
        if(qn.eq(Q_VALUE)) value = attr.string();

        if(name != null && name.length != 0 && value != null && value.length != 0) {
          hdrs.add(name, value);
          break;
        }
      }
    }
    return n;
  }

  /**
   * Parses <http:body/> element.
   * @param body body element
   * @param contItem content item
   * @param attrs map for parsed body attributes
   * @param bodyContent item cache for parsed body content
   * @throws QueryException query exception
   */
  private void parseBody(final ANode body, final Item contItem, final TokenMap attrs,
      final ValueBuilder bodyContent) throws QueryException {

    parseAttrs(body, attrs);
    checkBody(body, attrs);

    if(attrs.get(SRC) == null) {
      // no linked resource for setting request content
      if(contItem == null) {
        // content is set from <http:body/> children
        for(final ANode n : body.children()) bodyContent.add(n);
      } else {
        // content is set from $bodies parameter
        bodyContent.add(contItem);
      }
    }
  }

  /**
   * Parses a <http:multipart/> element.
   * @param multipart multipart element
   * @param contItems content items
   * @param attrs map for multipart attributes
   * @param parts list for multipart parts
   * @throws QueryException query exception
   */
  private void parseMultipart(final ANode multipart, final ValueBuilder contItems,
      final TokenMap attrs, final ArrayList<Part> parts) throws QueryException {

    parseAttrs(multipart, attrs);
    if(attrs.get(MEDIA_TYPE) == null)
      HC_REQ.thrw(info, "Attribute media-type of http:multipart is mandatory");

    final AxisMoreIter i = multipart.children();
    if(contItems == null) {
      // content is set from <http:body/> children of <http:part/> elements
      for(ANode n; (n = i.next()) != null;)
        parts.add(parsePart(n, null));
    } else {
      // content is set from $bodies parameter
      for(ANode n; (n = i.next()) != null;)
        parts.add(parsePart(n, contItems.next()));
    }
  }

  /**
   * Parses a part from a <http:multipart/> element.
   * @param part part element
   * @param contItem content item
   * @return structure representing the part
   * @throws QueryException query exception
   */
  private Part parsePart(final ANode part, final Item contItem) throws QueryException {
    final Part p = new Part();
    final ANode partBody = parseHdrs(part.children(), p.headers);
    parseBody(partBody, contItem, p.bodyAttrs, p.bodyContent);
    return p;
  }

  /**
   * Checks consistency of attributes for <http:request/>.
   * @param r request
   * @throws QueryException query exception
   */
  private void checkRequest(final HTTPRequest r) throws QueryException {
    // @method denotes the HTTP verb and is mandatory
    if(r.attrs.get(METHOD) == null) HC_REQ.thrw(info, "Attribute method is mandatory");

    // check parameters needed in case of authorization
    final byte[] sendAuth = r.attrs.get(SEND_AUTHORIZATION);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] un = r.attrs.get(USERNAME);
      final byte[] pw = r.attrs.get(PASSWORD);
      if(un == null && pw != null || un != null && pw == null || un == null && pw == null)
        HC_REQ.thrw(info, "Provided credentials are invalid");
    }
  }

  /**
   * Checks consistency of attributes for <http:body/>.
   * @param body body
   * @param bodyAttrs body attributes
   * @throws QueryException query exception
   */
  private void checkBody(final ANode body, final TokenMap bodyAttrs)
      throws QueryException {

    // @media-type is mandatory
    if(bodyAttrs.get(MEDIA_TYPE) == null)
      HC_REQ.thrw(info, "Attribute media-type of http:body is mandatory");

    // if src attribute is used to set the content of the body, no
    // other attributes must be specified and no content must be present
    if(bodyAttrs.get(SRC) != null && (bodyAttrs.size() > 2 || body.children().more()))
      HC_ATTR.thrw(info);

  }
}
