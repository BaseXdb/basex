package org.basex.query.func.validate;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ValidateDtd extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    return validate(new Validation() {
      @Override
      void validate()
          throws IOException, ParserConfigurationException, SAXException, QueryException {

        final Item input = toNodeOrAtomItem(arg(0), false, qc);
        final String schema = toStringOrNull(arg(1), qc);
        final IO schm = schema != null ? toIO(schema) : null;

        // integrate doctype declaration via serialization parameters
        SerializerOptions sp = null;
        if(schm != null) {
          sp = new SerializerOptions();
          sp.set(SerializerOptions.DOCTYPE_SYSTEM, prepare(schm).url());
        }

        final IO in = read(input, sp);
        final SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setValidating(true);
        sf.newSAXParser().parse(in.inputSource(), this);
      }
    });
  }
}
