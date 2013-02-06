package org.basex.test.io;

import javax.xml.bind.*;
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
    JAXBContext jaxbContext = JAXBContext.newInstance(SAXSerializerObject.class);

    // create XML
    final ArrayOutput marshalled = new ArrayOutput();
    jaxbContext.createMarshaller().marshal(
        new SAXSerializerObject("Object1", 42), marshalled);
    new CreateDB("test1", marshalled.toString()).execute(context);

    // get object from DB
    QueryProcessor queryProcessor = new QueryProcessor(
        "//domain-object[@name='Object1']", context);
    Item item1 = queryProcessor.iter().next();

    SAXSerializer saxSerializer = new SAXSerializer(item1);
    SAXSource saxSource = new SAXSource(saxSerializer, null);

    SAXSerializerObject dom = jaxbContext.createUnmarshaller().unmarshal(saxSource,
        SAXSerializerObject.class).getValue();

    queryProcessor.close();

    Assert.assertEquals(42, dom.getValue());
  }
}
