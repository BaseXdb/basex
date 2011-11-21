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
 * @author BaseX Team 2005-11, BSD License
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

  /**
   * Constructor.
   */
  private RequestParser() {

  }

  /**
   * Parses an <http:request/> element.
   * @param request request element
   * @param bodies content items
   * @param ii input info
   * @return parsed request
   * @throws QueryException query exception
   */
  public static Request parse(final ANode request, final ItemCache bodies,
      final InputInfo ii) throws QueryException {
    final Request r = new Request();
    parseAttrs(request, r.attrs);
    checkRequest(r, ii);
    final ANode payload = parseHdrs(request.children(), r.headers);
    final byte[] httpMethod = lc(r.attrs.get(METHOD));
    // it is an error if content is set for HTTP verbs which must be empty
    if((eq(TRACE, httpMethod) || eq(DELETE, httpMethod))
        && (payload != null || bodies != null)) REQINV.thrw(ii,
        "Body not expected for method " + string(httpMethod));
    if(payload != null) {
      // single part request
      if(eq(payload.nname(), BODY)) {
        Item it = null;
        if(bodies != null) {
          // $bodies must contain exactly one item
          if(bodies.size() != 1) REQINV.thrw(ii,
              "Number of items with request body content differs "
                  + "from number of body descriptors.");
          it = bodies.next();
        }
        parseBody(payload, it, r.payloadAttrs, r.bodyContent, ii);
        r.isMultipart = false;
        // multipart request
      } else if(eq(payload.nname(), MULTIPART)) {
        int i = 0;
        final AxisMoreIter ch = payload.children();
        while(ch.next() != null)
          i++;
        // number of items in $bodies must be equal to number of body
        // descriptors
        if(bodies != null && bodies.size() != i) REQINV.thrw(ii,
            "Number of items with request body content differs "
                + "from number of body descriptors.");
        parseMultipart(payload, bodies, r.payloadAttrs, r.parts, ii);
        r.isMultipart = true;
      } else REQINV.thrw(ii);
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
      attrs.add(attr.nname(), attr.string());
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
    while((n = i.next()) != null && eq(n.nname(), HDR)) {
      final AxisIter hdrAttrs = n.attributes();
      byte[] name = null;
      byte[] value = null;

      for(ANode attr; (attr = hdrAttrs.next()) != null;) {
        if(eq(attr.nname(), HDR_NAME)) name = attr.string();
        if(eq(attr.nname(), HDR_VALUE)) value = attr.string();

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
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void parseBody(final ANode body, final Item contItem,
      final TokenMap attrs, final ItemCache bodyContent, final InputInfo ii)
      throws QueryException {
    parseAttrs(body, attrs);
    checkBody(body, attrs, ii);
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
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void parseMultipart(final ANode multipart,
      final ItemCache contItems, final TokenMap attrs,
      final ObjList<Part> parts, final InputInfo ii) throws QueryException {
    parseAttrs(multipart, attrs);
    if(attrs.get(MEDIATYPE) == null) REQINV.thrw(ii,
        "Attribute media-type of http:multipart is mandatory");
    final AxisMoreIter i = multipart.children();
    if(contItems == null) {
      // content is set from <http:body/> children of <http:part/> elements
      for(ANode n; (n = i.next()) != null;)
        parts.add(parsePart(n, null, ii));
    } else {
      // content is set from $bodies parameter
      for(ANode n; (n = i.next()) != null;)
        parts.add(parsePart(n, contItems.next(), ii));
    }
  }

  /**
   * Parses a part from a <http:multipart/> element.
   * @param part part element
   * @param contItem content item
   * @param ii input info
   * @return structure representing the part
   * @throws QueryException query exception
   */
  private static Part parsePart(final ANode part, final Item contItem,
      final InputInfo ii) throws QueryException {
    final Part p = new Part();
    final ANode partBody = parseHdrs(part.children(), p.headers);
    parseBody(partBody, contItem, p.bodyAttrs, p.bodyContent, ii);
    return p;
  }

  /**
   * Checks consistency of attributes for <http:request/>.
   * @param r request
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkRequest(final Request r, final InputInfo ii)
      throws QueryException {
    // @method denotes the HTTP verb and is mandatory
    if(r.attrs.get(METHOD) == null) REQINV.thrw(ii,
        "Attribute method is mandatory");
    // check parameters needed in case of authorization
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = r.attrs.get(USRNAME);
      final byte[] passwd = r.attrs.get(PASSWD);

      if(usrname == null && passwd != null || usrname != null && passwd == null
          || usrname == null && passwd == null) REQINV.thrw(ii,
          "Provided credentials are invalid");
    }
  }

  /**
   * Checks consistency of attributes for <http:body/>.
   * @param body body
   * @param bodyAttrs body attributes
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkBody(final ANode body, final TokenMap bodyAttrs,
      final InputInfo ii) throws QueryException {

    // @media-type is mandatory
    if(bodyAttrs.get(MEDIATYPE) == null) REQINV.thrw(ii,
        "Attribute media-type of http:body is mandatory");

    // if src attribute is used to set the content of the body, no
    // other attributes must be specified and no content must be present
    if(bodyAttrs.get(SRC) != null
        && (bodyAttrs.size() > 2 || body.children().more())) SRCATTR.thrw(ii);

  }
}
