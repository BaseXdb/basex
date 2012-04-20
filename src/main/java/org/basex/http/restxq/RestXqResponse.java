package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.path.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** HTTP Response test. */
  private static final ExtTest HTTP_RESPONSE = new ExtTest(NodeType.ELM,
      new QNm(RESPONSE, QueryText.HTTPURI));
  /** REST Response test. */
  private static final ExtTest REST_RESPONSE = new ExtTest(NodeType.ELM,
      new QNm(RESPONSE, QueryText.RESTXQURI));
  /** HTTP Response test. */
  private static final ExtTest HTTP_HEADER = new ExtTest(NodeType.ELM,
      new QNm(HEADER, QueryText.HTTPURI));

  /** Function to be evaluated. */
  private final RestXqFunction function;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP context. */
  private final HTTPContext http;

  /**
   * Constructor.
   * @param rxf function to be evaluated
   * @param ctx query context
   * @param hc HTTP context
   */
  RestXqResponse(final RestXqFunction rxf, final QueryContext ctx, final HTTPContext hc) {
    function = rxf;
    qc = ctx;
    http = hc;
  }

  /**
   * Evaluates the specified function and creates a response.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void create() throws QueryException, IOException {
    // wrap function with a function call
    final UserFunc uf = function.function;
    final BaseFuncCall bfc = new BaseFuncCall(null, uf.name, uf.args);
    bfc.init(uf);

    // bind variables
    function.bind(http);

    // compile and evaluate function
    try {
      final Value result = bfc.comp(qc).value(qc);
      final int rs = (int) result.size();
      // execute updates
      if(qc.updating()) qc.updates.apply();

      final Item item = rs > 0 ? result.itemAt(0) : null;
      final SeqType st = SeqType.get(REST_RESPONSE.type, Occ.ONE, REST_RESPONSE);
      final ANode response = item != null && st.instance(item) ? (ANode) item : null;

      // HEAD method may only return a single response element
      if(function.methods.size() == 1 && function.methods.contains(HTTPMethod.HEAD)) {
        if(rs != 1 || response == null) function.error(HEAD_METHOD);
      }

      // process rest:response element and set serializer
      SerializerProp sp = response != null ? process(response) : null;
      if(sp == null) sp = function.output;

      // initialize response and serialize result
      http.initResponse(sp);
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sp);
      for(int v = response != null ? 1 : 0; v < rs; v++) {
        result.itemAt(v).serialize(ser);
      }
      ser.close();
    } finally {
      qc.close();
    }
  }

  /**
   * Processes the response element and creates the serialization parameters.
   * @param response response element
   * @return serialization properties
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private SerializerProp process(final ANode response)
      throws QueryException, IOException {

    SerializerProp sp = null;
    String cType = null;
    for(final ANode n : response.children()) {
      // process http:response element
      if(HTTP_RESPONSE.eq(n)) {
        final byte[] sta = n.attribute(new QNm(STATUS));
        if(sta != null) {
          final byte[] msg = n.attribute(new QNm(REASON));
          http.status(toInt(sta), msg != null ? string(msg) : null);
        }
        for(final ANode c : n.children()) {
          // process http:header element
          if(HTTP_HEADER.eq(c)) {
            final byte[] nam = c.attribute(new QNm(NAME));
            final byte[] val = c.attribute(new QNm(VALUE));
            if(nam != null && val != null) {
              final String key = string(nam);
              if(key.equals(MimeTypes.CONTENT_TYPE)) {
                cType = string(val);
              } else {
                http.res.setHeader(key, string(val));
              }
            }
          }
        }
      }
      // process output:serialization-parameters
      if(FNGen.OUTPUT_SERIAL.eq(n)) {
        sp = new SerializerProp(FNGen.parameters(n, null));
      }
    }

    // set content type
    if(cType != null) {
      if(sp == null) sp = new SerializerProp(function.output.toString());
      sp.set(SerializerProp.S_MEDIA_TYPE, cType);
    }

    return sp;
  }
}
