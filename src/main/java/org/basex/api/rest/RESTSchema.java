package org.basex.api.rest;

import org.basex.util.*;

/**
 * XML Schemas for REST requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
interface RESTSchema {
  /** Post Schema. */
  byte[] POST = Token.token(
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
    "</xs:schema>");
}
