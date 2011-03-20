package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.Iterator;
import java.util.List;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.NodeMore;
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
    final ANode payload = parseHdrs(request.children(), r.headers);
    if(payload != null) {
      if(eq(payload.nname(), BODY)) {
        parseBody(payload, r.payloadAttrs, r.bodyContent);
        r.isMultipart = false;
      } else if(eq(payload.nname(), MULTIPART)) {
        parseMultipart(payload, r.payloadAttrs, r.parts);
        r.isMultipart = true;
      } else ELMINV.thrw(ii);
    }

    check(r, ii);
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
        if(eq(attr.nname(), HDR_NAME)) name = attr.atom();
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
   */
  private static void parseBody(final ANode body, final TokenMap attrs,
      final ItemCache bodyContent) {
    parseAttrs(body, attrs);
    ANode n;
    final NodeMore i = body.children();
    while((n = i.next()) != null) {
      bodyContent.add(n);
    }
  }

  /**
   * Parses <http:multipart/> element.
   * @param multipart multipart element
   * @param attrs map for multipart attributes
   * @param parts list for multipart parts
   */
  private static void parseMultipart(final ANode multipart,
      final TokenMap attrs, final List<Part> parts) {
    parseAttrs(multipart, attrs);
    ANode n;
    final NodeMore i = multipart.children();
    while((n = i.next()) != null) {
      parts.add(parsePart(n));
    }
  }

  /**
   * Parses a part from a <http:multipart/> element.
   * @param part part element
   * @return structure representing the part
   */
  private static Part parsePart(final ANode part) {
    final Part p = new Part();
    final ANode partBody = parseHdrs(part.children(), p.headers);
    parseBody(partBody, p.bodyAttrs, p.bodyContent);
    return p;
  }

  /**
   * Checks some mandatory attributes in <http:request/> and <http:body/>
   * elements.
   * @param r request representation
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void check(final Request r, final InputInfo ii)
      throws QueryException {
    if(r.attrs.get(METHOD) == null) {
      MANDATTR.thrw(ii, string(METHOD));
    }
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    if(sendAuth != null && Boolean.parseBoolean(string(sendAuth))) {
      final byte[] usrname = r.attrs.get(USRNAME);
      final byte[] passwd = r.attrs.get(PASSWD);

      if(usrname == null && passwd != null || usrname != null && passwd == null)
        CREDSERR.thrw(ii);
    }

    if(r.isMultipart) {
      // Check body of each part
      final Iterator<Part> i = r.parts.iterator();
      Part p = null;
      while((p = i.next()) != null) {
        if(p.bodyContent.size() != 0 && p.bodyAttrs.get(MEDIATYPE) == null)
          MANDATTR.thrw(ii, string(MEDIATYPE));
      }
    } else {
      if(r.bodyContent.size() != 0 && r.payloadAttrs.get(MEDIATYPE) == null)
        MANDATTR.thrw(ii, string(MEDIATYPE));
    }
  }
}
