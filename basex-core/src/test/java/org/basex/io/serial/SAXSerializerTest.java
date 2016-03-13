package org.basex.io.serial;

import static org.junit.Assert.*;

import javax.xml.bind.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.junit.Test;

/**
 * Tests the SAXSerializer.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Michael Hedenus
 */
public final class SAXSerializerTest extends SandboxTest {
  /**
   * Tests the (un)marshalling of objects.
   * @throws Exception exception
   */
  @Test
  public void unmarshallJAXBObjectWithSAXSerializer() throws Exception {
    final JAXBContext jaxbContext = JAXBContext.newInstance(SAXSerializerObject.class);

    // create XML
    final ArrayOutput marshalled = new ArrayOutput();
    jaxbContext.createMarshaller().marshal(
        new SAXSerializerObject("Object1", 42), marshalled);
    execute(new CreateDB(NAME, marshalled.toString()));

    // get object from DB
    try(final QueryProcessor queryProcessor = new QueryProcessor(
        "//domain-object[@name='Object1']", context)) {
      final Item item = queryProcessor.iter().next();

      final SAXSerializer saxSerializer = new SAXSerializer(item);
      final SAXSource saxSource = new SAXSource(saxSerializer, null);

      final SAXSerializerObject dom = jaxbContext.createUnmarshaller().unmarshal(saxSource,
          SAXSerializerObject.class).getValue();
      assertEquals(42, dom.getValue());
    }
  }

  /**
   * Tests the handling of namespaces.
   * @throws Exception exception
   */
  @Test
  public void namespaces() throws Exception {
    try(final QueryProcessor queryProcessor = new QueryProcessor("<a xmlns='x'/>", context)) {
      final Item item = queryProcessor.iter().next();

      final SAXSerializer saxSerializer = new SAXSerializer(item);
      final SAXSource saxSource = new SAXSource(saxSerializer, null);

      final DOMResult result = new DOMResult();
      TransformerFactory.newInstance().newTransformer().transform(saxSource, result);
      assertEquals("x", result.getNode().getFirstChild().getNamespaceURI());
    }
  }
}
