package org.basex.api.jaxrx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Open;
import org.basex.data.MemData;
import org.jaxrx.interfaces.IPost;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class offers an implementation of the JAX-RX 'post' operation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXPost implements IPost {

  // ...next method could be split up in the interface?
  
  @Override
  public Response postResource(final String resource, final Object input,
      final boolean isQuery) {

    StreamingOutput so = null;
    if(isQuery) {
      so = postResource(resource, (Document) input);
    } else {
      postResource(resource, (InputStream) input);
    }
    return Response.ok(so).build();
  }

  /**
   * Inserts a new document to a database.
   * @param resource the resource to be requested
   * @param in input stream
   */
  public void postResource(final String resource, final InputStream in) {
    final Context ctx = new Context();
    try {
      new Open(resource).execute(ctx);
    } catch(BaseXException ex) {
      BXUtil.notFound(ex.getMessage());
    }

    try {
      final SAXSource saxSource = new SAXSource(new InputSource(in));
      final Parser parser = new SAXWrapper(saxSource, ctx.prop);
      final MemData data = new MemBuilder(parser).build(new Date() + ".xml");
      ctx.data.insert(ctx.data.meta.size, -1, data);
    } catch(final IOException ex) {
      BXUtil.error(ex);
    } finally {
      ctx.close();
    }
  }

  /**
   * Returns a query result.
   * @param resource the resource to be requested
   * @param doc the query input
   * @return output stream
   */
  public StreamingOutput postResource(final String resource,
      final Document doc) {

    return new StreamingOutput() {
      @Override
      public void write(final OutputStream out) {
        final Map<String, String> params = getParams(doc);
        BXUtil.query(resource, out,
            params.get("query"),
            params.get("wrap"),
            params.get("output"),
            params.get("start"),
            params.get("count"));
      }
    };
  }
  
  /**
   * This method extracts and returns query parameters from a document.
   * @param doc The XML {@link Document} containing the XQuery XML post
   *          request.
   * @return The XQuery expression as {@link String} and optional properties.
   * @throws WebApplicationException The exception occurred.
   */
  protected Map<String, String> getParams(final Document doc) {
    final Map<String, String> params = new HashMap<String, String>();

    params.put("query",
        doc.getElementsByTagName("text").item(0).getTextContent());

    final NodeList props = doc.getElementsByTagName("property");
    for(int i = 0; i < props.getLength(); i++) {
      final Node prop = props.item(i);
      params.put(
          prop.getAttributes().getNamedItem("name").getTextContent(),
          prop.getAttributes().getNamedItem("value").getTextContent());
    }
    return params;
  }
}
