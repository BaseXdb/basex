package org.basex.test.io;

import javax.xml.bind.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;

import junit.framework.Assert;

import org.basex.core.cmd.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.test.*;
import org.junit.Test;

/**
 * Tests the SAXSerializer.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Hedenus
 */
public class SAXSerializerTest extends SandboxTest {
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
    new CreateDB(NAME, marshalled.toString()).execute(context);

    // get object from DB
    final QueryProcessor queryProcessor = new QueryProcessor(
        "//domain-object[@name='Object1']", context);
    final Item item = queryProcessor.iter().next();

    final SAXSerializer saxSerializer = new SAXSerializer(item);
    final SAXSource saxSource = new SAXSource(saxSerializer, null);

    final SAXSerializerObject dom = jaxbContext.createUnmarshaller().unmarshal(saxSource,
        SAXSerializerObject.class).getValue();

    queryProcessor.close();

    Assert.assertEquals(42, dom.getValue());
  }

  /**
   * Tests the handling of namespaces.
   * @throws Exception exception
   */
  @Test
  public void namespaces() throws Exception {
    final QueryProcessor queryProcessor = new QueryProcessor("<a xmlns='x'/>", context);
    final Item item = queryProcessor.iter().next();

    final SAXSerializer saxSerializer = new SAXSerializer(item);
    final SAXSource saxSource = new SAXSource(saxSerializer, null);

    final DOMResult result = new DOMResult();
    TransformerFactory.newInstance().newTransformer().transform(saxSource, result);
    Assert.assertEquals("x", result.getNode().getFirstChild().getNamespaceURI());

    queryProcessor.close();
  }
}
