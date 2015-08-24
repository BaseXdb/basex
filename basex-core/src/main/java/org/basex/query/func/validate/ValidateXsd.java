package org.basex.query.func.validate;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ValidateXsd extends ValidateFn {
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

        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema s;
        if(exprs.length < 2) {
          // schema declaration is included in document
          s = sf.newSchema();
        } else {
          // schema is specified as string
          final IO schema = prepare(read(toNodeOrAtomItem(exprs[1], qc), qc, null), handler);
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
