package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class ValidateXsd extends ValidateFn {
  /** XML Schema 1.1 Classpath. */
  private static final String SCHEMA_FACTORY_CP = "javax.xml.validation.SchemaFactory";
  /** XML Schema 1.0 URI. */
  private static final String XSD10_URI = "http://www.w3.org/XML/XMLSchema/v1.0";
  /** XML Schema 1.1 URI. */
  private static final String XSD11_URI = "http://www.w3.org/XML/XMLSchema/v1.1";
  /** Saxon schema factory. */
  private static final String SAXON_CP = "com.saxonica.ee.jaxp.SchemaFactoryImpl";
  /** Xerces schema factory. */
  private static final String XERCES_CP = "org.apache.xerces.jaxp.validation.XMLSchemaFactory";
  /** Xerces schema factory. */
  private static final String XERCES11_CP = "org.apache.xerces.jaxp.validation.XMLSchema11Factory";
  /** Saxon version URI. */
  private static final String SAXON_VERSION_URI = "http://saxon.sf.net/feature/xsd-version";
  /** Version 1.0. */
  private static final String VERSION_10 = "1.0";
  /** Version 1.1. */
  private static final String VERSION_11 = "1.1";

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    return process(new Validation() {
      @Override
      void process(final ErrorHandler handler) throws IOException, SAXException, QueryException {
        final IO in = read(toNodeOrAtomItem(exprs[0], qc), null);
        final Item schema = exprs.length > 1 ? toNodeOrAtomItem(exprs[1].item(qc, info)) : null;
        final String version = exprs.length > 2 ? Token.string(toToken(exprs[2], qc)) : null;
        final boolean xsd11 = VERSION_11.equals(version);

        // find alternative validator (null: use default validator)
        String cp = null;
        if(Reflect.find(XERCES11_CP) != null) {
          cp = xsd11 ? XERCES11_CP : XERCES_CP;
        } else if(Reflect.find(SAXON_CP) != null) {
          cp = SAXON_CP;
        }

        // find implementation URI
        final String uri;
        if(version == null || version.equals(VERSION_10)) {
          uri = cp == null ? XMLConstants.W3C_XML_SCHEMA_NS_URI : XSD10_URI;
        } else if(xsd11 && cp != null) {
          uri = XSD11_URI;
        } else {
          throw BXVA_XSDVERSION_X.get(info, version);
        }

        // create schema factory and set version
        if(cp != null) System.setProperty(SCHEMA_FACTORY_CP + ':' + uri, cp);
        final SchemaFactory sf = SchemaFactory.newInstance(uri);
        if(SAXON_CP.equals(cp)) sf.setProperty(SAXON_VERSION_URI, xsd11 ? VERSION_11 : VERSION_10);

        final Schema s;
        if(schema == null) {
          // schema declaration is included in document
          s = sf.newSchema();
        } else {
          // schema is specified as string
          s = sf.newSchema(new URL(prepare(read(schema, null), handler).url()));
        }

        final Validator v = s.newValidator();
        v.setErrorHandler(handler);
        final String url = in.url();
        v.validate(url.isEmpty() ? new StreamSource(in.inputStream()) : new StreamSource(url));
      }
    });
  }
}
