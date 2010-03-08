package org.basex.api.jaxrx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.StringList;
import org.jaxrx.constants.EURLParameter;
import org.jaxrx.interfaces.IDelete;
import org.jaxrx.interfaces.IGet;
import org.jaxrx.interfaces.IPost;
import org.jaxrx.interfaces.IPut;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is responsible to interact between the REST layer implemented via
 * JAX-RX and the BaseX database. Hence, this class gets HTTP request, evaluates
 * it and send an appropriate response afterwards.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 */
public final class BaseXREST implements IGet, IPost, IDelete, IPut {

  // /**
  // * This field the begin result element of a XQuery or XPath expression.
  // */
  // private final transient String beginResult =
  // "<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">";
  //
  // /**
  // * This field the end result element of a XQuery or XPath expression.
  // */
  // private final transient String endResult = "</jaxrx:result>";

  @Override
  public Set<EURLParameter> getAvaliableParams() {
    final Set<EURLParameter> paramSet = new HashSet<EURLParameter>();
    paramSet.add(EURLParameter.COUNT);
    paramSet.add(EURLParameter.COMMAND);
    paramSet.add(EURLParameter.OUTPUT);
    paramSet.add(EURLParameter.QUERY);
    paramSet.add(EURLParameter.START);
    paramSet.add(EURLParameter.WRAP);
    return paramSet;
  }

  @Override
  public StreamingOutput getResource(final String resourceName,
      final Map<EURLParameter, String> parameters) {
    final StreamingOutput sOutput = new StreamingOutput() {
      @Override
      public void write(final OutputStream output) {
        final Context context = new Context();
        final String query = parameters.get(EURLParameter.QUERY);
        final String wrap = parameters.get(EURLParameter.WRAP);
        final String count = parameters.get(EURLParameter.COUNT);
        final String startCount = parameters.get(EURLParameter.START);
        final boolean sur = wrap != null && Boolean.parseBoolean(wrap);
        final String xQuery = query != null ? query : ".";
        final int max = count != null ? Integer.valueOf(count) :
          Integer.MAX_VALUE;
        final int start = startCount != null  ? Integer.valueOf(startCount) : 0;
        final boolean opened = new Open(resourceName).exec(context);
        if(opened) {
          doXQuery(xQuery, output, context, sur, start, max);
          new Close().exec(context);
        }
        context.close();
      }
    };

    return sOutput;
  }

  @Override
  public StreamingOutput getResourcesNames() {
    final Map<String, String> resources = new HashMap<String, String>();
    final Context context = new Context();
    final StringList stringList = org.basex.core.proc.List.list(context);
    context.close();
    for(final String res : stringList) {
      final Context dbContext = new Context();
      final boolean opened = new Open(res).exec(dbContext);
      if(opened) {
        final Nodes nodes = dbContext.current;
        if(nodes.size() > 1) {
          resources.put(res, "collection");
        } else {
          resources.put(res, "resource");
        }
        new Close().exec(dbContext);
        dbContext.close();
      }
    }
    return buildDBResponse(resources);
  }

  @Override
  public Response postResource(final String resourceName, final Object input,
      final boolean isQuery) {
    StreamingOutput sOutput = null;
    if(isQuery) {
      final Document pvDoc = (Document) input;
      sOutput = new StreamingOutput() {
        @Override
        public void write(final OutputStream output) {
          final OutputStream out = output;
          final Context context = new Context();
          final boolean opened = new Open(resourceName).exec(context);
          if(!opened)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

          final Map<String, String> postQuery = getQueryOutOfXML(pvDoc);
          final String xquery = postQuery.get("query");
          final String wrap = postQuery.get("wrap");
          final String count = postQuery.get("count");
          final String startCount = postQuery.get("start");
          final int start =
            startCount != null ? Integer.valueOf(startCount) : 0;
          final int max =
            count != null ? Integer.valueOf(count) : Integer.MAX_VALUE;
          final boolean sur = wrap != null && Boolean.parseBoolean(wrap);
          doXQuery(xquery, out, context, sur, start, max);
          context.close();
        }
      };
    } else {
      final InputStream inputStream = (InputStream) input;
      final Context dbContext = new Context();
      final boolean opened = new Open(resourceName).exec(dbContext);
      if(opened) {
        try {
          final SAXSource saxSource = new SAXSource(
              new InputSource(inputStream));
          final Parser parser = new SAXWrapper(saxSource, dbContext.prop);
          final Data data = dbContext.data;
          data.insert(data.meta.size, -1,
              new MemBuilder(parser).build(new Date() + ".xml"));
          data.flush();
          dbContext.update();
          new Close().exec(dbContext);
        } catch(final IOException exce) {
          throw new WebApplicationException(exce);
        }
      }
      dbContext.close();
    }
    return Response.ok(sOutput).build();

  }

  @Override
  public boolean deleteResource(final String resourceName) {
    final Context context = new Context();
    final boolean deleted = new DropDB(resourceName).exec(context);
    context.close();
    return deleted;
  }

  @Override
  public boolean createResource(final String resourceName,
      final InputStream input) {
    final Context context = new Context();
    boolean created = false;
    try {
      final SAXSource saxSource = new SAXSource(new InputSource(input));
      final Parser parser = new SAXWrapper(saxSource, context.prop);
      final Data data = CreateDB.xml(context, parser, resourceName);
      Close.close(context, data);
      context.close();
      created = true;
    } catch(final IOException exce) {
      throw new WebApplicationException(exce);
    }
    context.close();
    return created;
  }

  /**
   * This method creates the XML response containing the available XML resources
   * and collections under the URL path /jaxrx/db.
   * @param resources The resources that will be packed in an XML streaming
   *          output
   * @return The available resources as XML streaming output
   */
  private StreamingOutput buildDBResponse(final Map<String, String> resources) {
    return ResponseBuilder.buildResponse(resources);
  }

  /**
   * This method executes the XQuery expression within BaseX.
   * @param query The XQuery expression as {@link String}
   * @param out The {@link OutputStream} that writes the result of the query
   * @param ctx The context
   * @param sur The optional surrounding result element (needed for a
   *          XML fragment)
   * @param start The start value
   * @param count The maximum value of results
   * @throws WebApplicationException The exception occurred
   */
  protected void doXQuery(final String query, final OutputStream out,
      final Context ctx, final boolean sur, final int start, final int count) {

    XMLSerializer xml = null;
    QueryProcessor proc = null;
    try {
      xml = new XMLSerializer(out, sur, true);
      proc = new QueryProcessor(query, ctx);

      final Iter iter = proc.iter();
      for(int c = 0; c < count; c++) {
        final Item it = iter.next();
        if(it == null) break;
        if(c < start) continue;
        xml.openResult();
        it.serialize(xml);
        xml.closeResult();
      }
    } catch(final Exception ex) {
      // catch all kind of exceptions to get sure an exception is returned 
      throw new WebApplicationException(ex);
    } finally {
      try {
        if(xml != null) xml.close();
        if(proc != null) proc.close();
      } catch(final Exception ex) { }
    }
  }

  /**
   * This method gets a XQuery out of the sent XML file.
   * @param pvDoc The XML {@link Document} containing the XQuery XML post
   *          request.
   * @return The XQuery expression as {@link String} and optional properties.
   * @throws WebApplicationException The exception occurred.
   */
  protected Map<String, String> getQueryOutOfXML(final Document pvDoc) {
    String query = null;
    final Map<String, String> values = new HashMap<String, String>();

    final NodeList queryNodes = pvDoc.getElementsByTagName("text");
    query = queryNodes.item(0).getTextContent();
    values.put("query", query);
    final NodeList propertyNodes = pvDoc.getElementsByTagName("property");
    for(int i = 0; i < propertyNodes.getLength(); i++) {
      final Node property = propertyNodes.item(i);
      values.put(
          property.getAttributes().getNamedItem("name").getTextContent(),
          property.getAttributes().getNamedItem("value").getTextContent());
    }
    return values;
  }
}
