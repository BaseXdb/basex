package org.basex.query.util.http;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.http.Request.Part;
import org.basex.util.InputInfo;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.ObjList;

/**
 * Request parser.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RequestParser {
  /** http:header element. */
  private static final byte[] HDR = token("http:header");
  /** Header attribute: name. */
  private static final byte[] HDR_NAME = token("name");
  /** Header attribute: value. */
  private static final byte[] HDR_VALUE = token("value");
  /** http:multipart element. */
  private static final byte[] MULTIPART = token("http:multipart");
  /** http:body element. */
  private static final byte[] BODY = token("http:body");

  /** Request attribute: HTTP method. */
  private static final byte[] METHOD = token("method");
  /** Request attribute: username. */
  private static final byte[] USRNAME = token("username");
  /** Request attribute: password. */
  private static final byte[] PASSWD = token("password");
  /** Request attribute: send-authorization. */
  private static final byte[] SENDAUTH = token("send-authorization");
  /** Body attribute: media-type. */
  private static final byte[] MEDIATYPE = token("media-type");
  /** Body attribute: media-type. */
  private static final byte[] SRC = token("src");
  /** HTTP method TRACE. */
  private static final byte[] TRACE = token("trace");
  /** HTTP method DELETE. */
  private static final byte[] DELETE = token("delete");

  /** Input information. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param ii input info
   */
  public RequestParser(final InputInfo ii) {
    input = ii;
  }

  /**
   * Parses an <http:request/> element.
   * @param request request element
   * @param bodies content items
   * @return parsed request
   * @throws QueryException query exception
   */
  public Request parse(final ANode request, final ItemCache bodies)
      throws QueryException {

    final Request r = new Request();
    parseAttrs(request, r.attrs);
    checkRequest(r);

    final ANode payload = parseHdrs(request.children(), r.headers);
    final byte[] httpMethod = lc(r.attrs.get(METHOD));
    // it is an error if content is set for HTTP verbs which must be empty
    if((eq(TRACE, httpMethod) || eq(DELETE, httpMethod)) &&
        (payload != null || bodies != null))
      REQINV.thrw(input, "Body not expected for method " + string(httpMethod));

    if(payload != null) {
      // single part request
      if(eq(payload.name(), BODY)) {
        Item it = null;
        if(bodies != null) {
          // $bodies must contain exactly one item
          if(bodies.size() != 1) REQINV.thrw(input,
              "Number of items with request body content differs "
                  + "from number of body descriptors.");
          it = bodies.next();
        }
        parseBody(payload, it, r.payloadAttrs, r.bodyContent);
        r.isMultipart = false;
        // multipart request
      } else if(eq(payload.name(), MULTIPART)) {
        int i = 0;
        final AxisMoreIter ch = payload.children();
        while(ch.next() != null)
          i++;
        // number of items in $bodies must be equal to number of body
        // descriptors
        if(bodies != null && bodies.size() != i) REQINV.thrw(input,
            "Number of items with request body content differs "
                + "from number of body descriptors.");
        parseMultipart(payload, bodies, r.payloadAttrs, r.parts);
        r.isMultipart = true;
      } else {
        REQINV.thrw(input);
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
    ANode n = null;
    while(true) {
      n = i.next();
      if(n == null) break;
      final byte[] nm = n.name();
      if(nm == null) continue;
      if(!eq(nm, HDR)) break;

      final AxisIter hdrAttrs = n.attributes();
      byte[] name = null;
      byte[] value = null;

      for(ANode attr; (attr = hdrAttrs.next()) != null;) {
        if(eq(attr.name(), HDR_NAME)) name = attr.string();
        if(eq(attr.name(), HDR_VALUE)) value = attr.string();

        if(name != null && name.length != 0 && value != null
            && value.length != 0) {
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
  private void parseBody(final ANode body, final Item contItem,
      final TokenMap attrs, final ItemCache bodyContent) throws QueryException {

    parseAttrs(body, attrs);
    checkBody(body, attrs);

    if(attrs.get(SRC) == null) {
      // no linked resource for setting request content
      if(contItem == null) {
        // content is set from <http:body/> children
        final AxisMoreIter i = body.children();
        for(ANode n; (n = i.next()) != null;) bodyContent.add(n);
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
  private void parseMultipart(final ANode multipart,
      final ItemCache contItems, final TokenMap attrs,
      final ObjList<Part> parts) throws QueryException {

    parseAttrs(multipart, attrs);
    if(attrs.get(MEDIATYPE) == null) REQINV.thrw(input,
        "Attribute media-type of http:multipart is mandatory");
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
  private Part parsePart(final ANode part, final Item contItem)
      throws QueryException {

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
  private void checkRequest(final Request r) throws QueryException {
    // @method denotes the HTTP verb and is mandatory
    if(r.attrs.get(METHOD) == null)
      REQINV.thrw(input, "Attribute method is mandatory");

    // check parameters needed in case of authorization
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = r.attrs.get(USRNAME);
      final byte[] passwd = r.attrs.get(PASSWD);

      if(usrname == null && passwd != null || usrname != null && passwd == null
          || usrname == null && passwd == null)
        REQINV.thrw(input, "Provided credentials are invalid");
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
    if(bodyAttrs.get(MEDIATYPE) == null)
      REQINV.thrw(input, "Attribute media-type of http:body is mandatory");

    // if src attribute is used to set the content of the body, no
    // other attributes must be specified and no content must be present
    if(bodyAttrs.get(SRC) != null &&
        (bodyAttrs.size() > 2 || body.children().more())) SRCATTR.thrw(input);

  }
}
