package org.basex.http.rest;

import javax.xml.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * XML Schemas for REST requests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
enum RESTSchema {
  /** Single instance. */
  INSTANCE;

  /** Validation schema. */
  private final Schema schema;

  /** Constructor. */
  RESTSchema() {
    Schema s = null;
    try {
      s = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
          newSchema(new StreamSource(new ArrayInput(Token.token(SCHEMA_CONTENT))));
    } catch(final SAXException ex) {
      throw Util.notExpected(ex);
    }
    schema = s;
  }

  /**
   * Create a new validator against the schema.
   * @return a new validator
   */
  static Validator newValidator() {
    return INSTANCE.schema.newValidator();
  }

  /** Post Schema. */
  private static final String SCHEMA_CONTENT =
    "<?xml version='1.0' encoding='UTF-8'?>" +
    "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
    " xmlns='http://basex.org/rest'" +
    " targetNamespace='http://basex.org/rest'>" +
    "<xs:element name='query'>" +
    "<xs:complexType>" +
    "<xs:sequence>" +
    "<xs:element ref='text' minOccurs='1' maxOccurs='1'/>" +
    "<xs:element ref='parameter' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='option' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='variable' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='context' minOccurs='0' maxOccurs='1'/>" +
    "</xs:sequence>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='run'>" +
    "<xs:complexType>" +
    "<xs:sequence>" +
    "<xs:element ref='text' minOccurs='1' maxOccurs='1'/>" +
    "<xs:element ref='parameter' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='option' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='variable' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='context' minOccurs='0' maxOccurs='1'/>" +
    "</xs:sequence>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='command'>" +
    "<xs:complexType>" +
    "<xs:sequence>" +
    "<xs:element ref='text' minOccurs='1' maxOccurs='1'/>" +
    "<xs:element ref='parameter' minOccurs='0' maxOccurs='unbounded'/>" +
    "<xs:element ref='option' minOccurs='0' maxOccurs='unbounded'/>" +
    "</xs:sequence>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='text' type='xs:string'/>" +
    "<xs:element name='option'>" +
    "<xs:complexType>" +
    "<xs:attribute name='name' type='xs:string' use='required'/>" +
    "<xs:attribute name='value' type='xs:string' use='required'/>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='parameter'>" +
    "<xs:complexType>" +
    "<xs:attribute name='name' type='xs:string' use='required'/>" +
    "<xs:attribute name='value' type='xs:string' use='required'/>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='variable'>" +
    "<xs:complexType>" +
    "<xs:attribute name='name' type='xs:string' use='required'/>" +
    "<xs:attribute name='value' type='xs:string' use='required'/>" +
    "<xs:attribute name='type' type='xs:string' use='optional'/>" +
    "</xs:complexType>" +
    "</xs:element>" +
    "<xs:element name='context' type='xs:anyType'/>" +
    "</xs:schema>";
}
