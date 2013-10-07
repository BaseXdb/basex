package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This class holds information on a custom RESTXQ response.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqRespBuilder {
  /** Output cache. */
  final ArrayOutput cache = new ArrayOutput();;
  /** Show error. */
  boolean error;
  /** Status code. */
  int status;
  /** Status message. */
  String message;

  /**
   * Builds a response element and creates the serialization parameters.
   * @param response response element
   * @param func function
   * @param iter result iterator
   * @param http http context
   * @throws Exception exception (including unexpected ones)
   */
  void build(final ANode response, final RestXqFunction func,
      final Iter iter, final HTTPContext http) throws Exception {

    // don't allow attributes
    for(final ANode a : response.attributes()) func.error(UNEXP_NODE, a);

    // parse response and serialization parameters
    SerializerOptions sp = func.output;
    String cType = null;
    for(final ANode n : response.children()) {
      // process http:response element
      if(HTTP_RESPONSE.eq(n)) {
        // check status and reason
        byte[] sta = null, msg = null;
        for(final ANode a : n.attributes()) {
          final QNm qnm = a.qname();
          if(qnm.eq(Q_STATUS)) sta = a.string();
          else if(qnm.eq(Q_REASON) || qnm.eq(Q_MESSAGE)) msg = a.string();
          else func.error(UNEXP_NODE, a);
        }
        if(sta != null) {
          status = toInt(sta);
          message = msg != null ? string(msg) : null;
        }

        for(final ANode c : n.children()) {
          // process http:header elements
          if(HTTP_HEADER.eq(c)) {
            final byte[] nam = c.attribute(Q_NAME);
            final byte[] val = c.attribute(Q_VALUE);
            if(nam != null && val != null) {
              final String key = string(nam);
              final String value = string(val);
              if(key.equals(MimeTypes.CONTENT_TYPE)) {
                cType = value;
              } else {
                http.res.setHeader(key, value);
              }
            }
          } else {
            func.error(UNEXP_NODE, c);
          }
        }
      } else if(OUTPUT_SERIAL.eq(n)) {
        // parse output:serialization-parameters
        sp = FuncOptions.serializer(n, func.function.info);
        FuncOptions.parse(n, func.output, func.function.info);
      } else {
        func.error(UNEXP_NODE, n);
      }
    }

    // set content type
    if(cType != null) sp.string(SerializerOptions.S_MEDIA_TYPE, cType);

    // check next item
    Item item = iter.next();
    if(item == null) {
      error = true;
    } else if(func.methods.size() == 1 && func.methods.contains(HTTPMethod.HEAD)) {
      func.error(HEAD_METHOD);
    }

    // cache result
    http.initResponse(sp);
    final Serializer ser = Serializer.get(cache, sp);
    for(; item != null; item = iter.next()) ser.serialize(item);
    ser.close();
  }
}
