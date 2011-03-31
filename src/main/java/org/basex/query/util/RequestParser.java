package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.List;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.NodeMore;
import org.basex.query.util.Request.Part;
import org.basex.util.InputInfo;
import org.basex.util.TokenMap;

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

  /**
   * Constructor.
   */
  private RequestParser() {

  }

  /**
   * Parses an <http:request/> element.
   * @param request request element
   * @param ii input info
   * @return parsed request
   * @throws QueryException query exception
   */
  public static Request parse(final ANode request, final InputInfo ii)
      throws QueryException {
    final Request r = new Request();
    parseAttrs(request, r.attrs);
    checkRequest(r, ii);
    final ANode payload = parseHdrs(request.children(), r.headers);
    if(payload != null) {
      if(eq(payload.nname(), BODY)) {
        parseBody(payload, r.payloadAttrs, r.bodyContent, ii);
        r.isMultipart = false;
      } else if(eq(payload.nname(), MULTIPART)) {
        parseMultipart(payload, r.payloadAttrs, r.parts, ii);
        r.isMultipart = true;
      } else ELMINV.thrw(ii);
    }

    // check(r, ii);
    return r;
  }

  /**
   * Parses the attributes of an element.
   * @param element element
   * @param attrs map for parsed attributes
   */
  private static void parseAttrs(final ANode element, final TokenMap attrs) {
    final AxisIter elAttrs = element.atts();
    ANode attr = null;
    while((attr = elAttrs.next()) != null) {
      attrs.add(attr.nname(), attr.atom());
    }
  }

  /**
   * Parses <http:header/> children of requests and parts.
   * @param i iterator on request/part children
   * @param hdrs map for parsed headers
   * @return body or multipart
   */
  private static ANode parseHdrs(final NodeMore i, final TokenMap hdrs) {
    ANode n = null;
    while((n = i.next()) != null && eq(n.nname(), HDR)) {
      final AxisIter hdrAttrs = n.atts();
      ANode attr = null;
      byte[] name = null;
      byte[] value = null;

      while((attr = hdrAttrs.next()) != null) {
        if(eq(attr.nname(), HDR_NAME)) name = lc(attr.atom());
        if(eq(attr.nname(), HDR_VALUE)) value = attr.atom();

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
   * @param attrs map for parsed body attributes
   * @param bodyContent item cache for parsed body content
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void parseBody(final ANode body, final TokenMap attrs,
      final ItemCache bodyContent, final InputInfo ii) throws QueryException {
    parseAttrs(body, attrs);
    checkBody(body, attrs, ii);
    if(attrs.get(SRC) == null) {
      ANode n;
      final NodeMore i = body.children();
      while((n = i.next()) != null) {
        bodyContent.add(n);
      }
    }
  }

  /**
   * Parses <http:multipart/> element.
   * @param multipart multipart element
   * @param attrs map for multipart attributes
   * @param parts list for multipart parts
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void parseMultipart(final ANode multipart,
      final TokenMap attrs, final List<Part> parts, final InputInfo ii)
      throws QueryException {
    parseAttrs(multipart, attrs);
    ANode n;
    final NodeMore i = multipart.children();
    while((n = i.next()) != null) {
      parts.add(parsePart(n, ii));
    }
  }

  /**
   * Parses a part from a <http:multipart/> element.
   * @param part part element
   * @param ii input info
   * @return structure representing the part
   * @throws QueryException query exception
   */
  private static Part parsePart(final ANode part, final InputInfo ii)
      throws QueryException {
    final Part p = new Part();
    final ANode partBody = parseHdrs(part.children(), p.headers);
    parseBody(partBody, p.bodyAttrs, p.bodyContent, ii);
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
    // Check if HTTP method is provided
    if(r.attrs.get(METHOD) == null) {
      MANDATTR.thrw(ii, string(METHOD));
    }

    // Check parameters needed in case of authorization
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = r.attrs.get(USRNAME);
      final byte[] passwd = r.attrs.get(PASSWD);

      if(usrname == null && passwd != null || usrname != null && passwd == null)
        CREDSERR.thrw(ii);
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

    // First check if media-type is specified
    if(bodyAttrs.get(MEDIATYPE) == null) MANDATTR.thrw(ii);

    // If src attribute is used to set the content of the body, no
    // other attributes must be specified and no content must be present
    if(bodyAttrs.get(SRC) != null
        && (bodyAttrs.size() > 2 || body.children().more())) SRCATTR.thrw(ii);

  }
}
