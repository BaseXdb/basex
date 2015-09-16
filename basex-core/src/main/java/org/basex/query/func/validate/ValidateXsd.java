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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ValidateXsd extends ValidateFn {
  /** Classpath for XML Schema 1.1. */
  private static final String XSD11_PATH = "javax.xml.validation.SchemaFactory";
  /** URI for XML Schema 1.1. */
  private static final String XSD11_URI = "http://www.w3.org/XML/XMLSchema/v1.1";
  /** XML Schema 1.1 implementations. */
  private static final String[] XSD11_FACTORIES = {
    "com.saxonica.ee.jaxp.SchemaFactoryImpl",
    "org.apache.xerces.jaxp.validation.XMLSchema11Factory"
  };
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
        final IO in = read(toNodeOrAtomItem(exprs[0], qc), qc, null);
        final Item sch = exprs.length < 2 ? null : toNodeOrAtomItem(exprs[1].item(qc, info));
        String ns = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        String version = VERSION_10;
        if(exprs.length > 2) {
          version = Token.string(toToken(exprs[2], qc));
          if(version.equals(VERSION_11)) {
            ns = XSD11_URI;
            String factory = null;
            for(final String f : XSD11_FACTORIES) if(Reflect.find(f) != null) factory = f;
            if(factory == null) throw BXVA_XSDVERSION_X.get(info, VERSION_11);
            System.setProperty(XSD11_PATH + ':' + ns, factory);
          } else if(!version.equals(VERSION_10)) {
            throw BXVA_XSDVERSION_X.get(info, VERSION_11);
          }
        }

        final SchemaFactory sf = SchemaFactory.newInstance(ns);
        //sf.setProperty("http://saxon.sf.net/feature/xsd-version", version);

        final Schema s;
        if(sch == null) {
          // schema declaration is included in document
          s = sf.newSchema();
        } else {
          // schema is specified as string
          final IO schema = prepare(read(sch, qc, null), handler);
          s = sf.newSchema(new URL(schema.url()));
        }

        final Validator v = s.newValidator();
        v.setErrorHandler(handler);
        final String url = in.url();
        v.validate(url.isEmpty() ? new StreamSource(in.inputStream()) : new StreamSource(url));
      }
    });
  }
}
