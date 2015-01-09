package org.basex.query.func.validate;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.xml.sax.*;

/**
 * Validates a document against a DTD.
 * The following two variants exist:
 *
 * <ul>{@code validate:dtd($doc)}
 *  <li>Looks for the document type declaration in {@code $doc} and
 *    uses it for validation.</li>
 *  <li>{@code $doc} must contain a DTD for this to work.</li>
 *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
 *    xs:string} pointing to an URL or a local file that will then be parsed
 *    and validated.</li>
 *  </ul>
 *  <ul>{@code validate:dtd($doc, $dtd)}
 *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
 *    xs:string} pointing to an URL or a local file</li>
 *  <li>{@code $dtd as xs:string} is expected to point to an URL or a local
 *  file containing the document type definitions. </li>
 *  </ul>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ValidateDtdInfo extends ValidateFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return process(new Validate() {
      @Override
      void process(final ErrorHandler handler)
          throws IOException, ParserConfigurationException, SAXException, QueryException {

        final Item it = toItem(exprs[0], qc);
        SerializerOptions sp = null;

        // integrate doctype declaration via serialization parameters
        if(exprs.length > 1) {
          sp = new SerializerOptions();
          IO dtd = checkPath(exprs[1], qc);
          tmp = createTmp(dtd);
          if(tmp != null) dtd = tmp;
          sp.set(SerializerOptions.DOCTYPE_SYSTEM, dtd.url());
        }

        final IO in = read(it, qc, sp);
        final SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setValidating(true);
        sf.newSAXParser().parse(in.inputSource(), handler);
      }
    });
  }
}
