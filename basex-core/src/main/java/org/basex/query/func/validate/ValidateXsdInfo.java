package org.basex.query.func.validate;

import java.io.*;
import java.net.*;

import javax.xml.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.xml.sax.*;

/**
 * Validates a document against an XML Schema.
 * The following two variants exist:
 *
 * <div>{@code validate:xsd($doc)}:</div>
 * <ul>
 *  <li>Looks for {@code xsi:(noNamespace)schemaLocation} in {@code $doc} and
 *    uses this schema for validation.</li>
 *  <li>{@code $doc} must contain a schemaLocation declaration for validation
 *  to work.</li>
 *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
 *    xs:string} pointing to an URL or a local file that will then be parsed
 *    and validated.</li>
 * </ul>
 * <div>{@code validate:xsd($doc, $schema)}:</div>
 * <ul>
 *  <li>if {@code $doc} contains an {@code xsi:(noNamespace)schemaLocation} it
 *  will be ignored.</li>
 *  <li>{@code $doc} is allowed to be either a {@code XML node} or a {@code
 *    xs:string} pointing to an URL or a local file</li>
 *  <li>{@code $schema as xs:string} is expected to point to an URL or a local
 *  file containing the schema definitions. </li>
 * </ul>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ValidateXsdInfo extends ValidateFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return process(new Validate() {
      @Override
      void process(final ErrorHandler handler) throws IOException, SAXException, QueryException {
        final IO in = read(toItem(exprs[0], qc), qc, null);
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema;
        if(exprs.length < 2) {
          // assume that schema declaration is included in document
          schema = sf.newSchema();
        } else {
          final Item it = toItem(exprs[1], qc);
          // schema specified as string
          IO scio = read(it, qc, null);
          tmp = createTmp(scio);
          if(tmp != null) scio = tmp;
          schema = sf.newSchema(new URL(scio.url()));
        }

        final Validator v = schema.newValidator();
        v.setErrorHandler(handler);
        v.validate(new StreamSource(in.inputStream()));
      }
    });
  }
}
