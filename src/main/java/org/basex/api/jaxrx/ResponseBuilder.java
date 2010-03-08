package org.basex.api.jaxrx;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This utility class builds the XML response for listing existing available
 * resources or collections to a given URL path.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 */
public final class ResponseBuilder {

  /**
   * The private empty constructor.
   */
  private ResponseBuilder() {
  // i do nothing
  }

  /**
   * This method creates a new {@link Document} instance for the surrounding XML
   * element for the client response.
   * 
   * @return The created {@link Document} instance.
   * @throws ParserConfigurationException The exception occurred.
   */
  public static Document createSurroundingXMLResp()
      throws ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().
      newDocumentBuilder().newDocument();
  }

  /**
   * This method creates the response XML element.
   * 
   * @param document The {@link Document} instance for the response.
   * @return The created XML {@link Element}.
   */
  public static Element createResultElement(final Document document) {
    final Element response = document.createElementNS("http://jaxrx.org/",
        "result");
    response.setPrefix("jaxrx");
    return response;
  }

  /**
   * This method creates a list of Elements of existing resources to a given URL
   * path.
   * 
   * @param pathResource The available resources/collections to a given URL
   *          path.
   * @param document The document to create an Element.
   * @return The list of available resources/collections packed in a list of
   *         elements.
   */
  public static List<Element> createCollectionOrResourceEl(
      final Map<String, String> pathResource, final Document document) {

    final List<Element> collections = new ArrayList<Element>();
    if(pathResource != null) {
      for(final Map.Entry<String, String> entry : pathResource.entrySet()) {
        final String pathString = entry.getKey();
        final String type = entry.getValue();
        if(type.equals("resource")) {
          final Element resource = document.createElement(type);
          resource.setAttribute("name", pathString);
          collections.add(resource);
        } else {
          final Element collection = document.createElement(type);
          collection.setAttribute("name", pathString);
          collections.add(collection);
        }
      }
    }
    return collections;
  }

  /**
   * This method creates an XML resource list of available resources
   * corresponding to a given URL path.
   * 
   * @param availableResources The existing resources to a given URL path.
   * @return The list of available resources as an XML response packed in a
   *         {@link StreamingOutput}.
   */
  public static StreamingOutput buildResponse(
      final Map<String, String> availableResources) {

    final StreamingOutput sOutput = new StreamingOutput() {
      @Override
      public void write(final OutputStream output) {
        Document document;
        try {
          document = createSurroundingXMLResp();
          final Element resElement =
            ResponseBuilder.createResultElement(document);

          final List<Element> collOrRes =
            ResponseBuilder.createCollectionOrResourceEl(
                availableResources, document);
          for(final Element resource : collOrRes) {
            resElement.appendChild(resource);
          }
          document.appendChild(resElement);
          final DOMSource domSource = new DOMSource(document);
          final StreamResult streamResult = new StreamResult(output);
          final Transformer transformer =
            TransformerFactory.newInstance().newTransformer();
          transformer.transform(domSource, streamResult);
        } catch(final Exception exce) {
          // catch all kind of exceptions to get sure an exception is returned 
          throw new WebApplicationException(exce);
        }
      }
    };

    return sOutput;
  }
}
