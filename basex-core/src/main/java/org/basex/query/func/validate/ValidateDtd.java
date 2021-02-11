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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ValidateDtd extends ValidateFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return process(new Validation() {
      @Override
      void process(final ValidationHandler handler)
          throws IOException, ParserConfigurationException, SAXException, QueryException {

        final Item input = toNodeOrAtomItem(0, qc);
        IO schema = null;
        if(exprs.length > 1) {
          final byte[] path = toTokenOrNull(exprs[1], qc);
          if(path != null) schema = checkPath(path);
        }

        // integrate doctype declaration via serialization parameters
        SerializerOptions sp = null;
        if(schema != null) {
          sp = new SerializerOptions();
          sp.set(SerializerOptions.DOCTYPE_SYSTEM, prepare(schema, handler).url());
        }

        final IO in = read(input, sp);
        final SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setValidating(true);
        sf.newSAXParser().parse(in.inputSource(), handler);
      }
    });
  }
}
