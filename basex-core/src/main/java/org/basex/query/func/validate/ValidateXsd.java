package org.basex.query.func.validate;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.build.xml.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ValidateXsd extends ValidateFn {
  /** Schema factory. */
  private static final String FACTORY = "http://www.w3.org/2001/XMLSchema";
  /** Saxon version URI. */
  private static final String SAXON_VERSION_URI = "http://saxon.sf.net/feature/xsd-version";

  /** XSD implementations. */
  static final String[] IMPL = {
    "com.saxonica.ee.jaxp.SchemaFactoryImpl", "Saxon EE", "1.1",
    "org.apache.xerces.jaxp.validation.XMLSchema11Factory", "Xerces", "1.1",
    "org.apache.xerces.jaxp.validation.XMLSchemaFactory", "Xerces", "1.0",
    "", "Java", "1.0",
  };

  /** Implementation offset. */
  static final int OFFSET;
  /** Saxon flag. */
  static final boolean SAXON;
  /** Java flag. */
  static final boolean JAVA;

  static {
    int i = 0;
    while(i + 3 < IMPL.length && Reflect.find(IMPL[i]) == null) i += 3;
    OFFSET = i;
    SAXON = i == 0;
    JAVA = i == 9;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return check(qc);
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    return process(new Validation() {
      @Override
      void process(final ValidationHandler handler) throws IOException, SAXException,
          QueryException {

        final IO in = read(toNodeOrAtomItem(0, qc), null);
        final Item schema = exprs.length > 1 ? toNodeOrAtomItem(1, qc) : null;
        final HashMap<String, String> options = toOptions(2, new Options(), qc).free();

        // create schema factory and set version
        final SchemaFactory sf = JAVA ? SchemaFactory.newInstance(FACTORY) :
          (SchemaFactory) Reflect.get(Reflect.find(IMPL[OFFSET]));
        // Saxon: use version 1.1
        if(SAXON) sf.setProperty(SAXON_VERSION_URI, IMPL[OFFSET + 2]);

        final LSResourceResolver ls = CatalogWrapper.getLSResourceResolver(qc.context.options);
        if(ls != null) sf.setResourceResolver(ls);

        // assign parser features
        for(final Entry<String, String> entry : options.entrySet()) {
          sf.setFeature(entry.getKey(), Strings.toBoolean(entry.getValue()));
        }

        // schema declaration is included in document, or specified as string
        final Schema s = schema == null ? sf.newSchema() :
          sf.newSchema(new URL(prepare(read(schema, null), handler).url()));

        final Validator v = s.newValidator();
        v.setErrorHandler(handler);
        v.validate(in instanceof IOContent || in instanceof IOStream ?
            new StreamSource(in.inputStream()) : new StreamSource(in.url()));
      }
    });
  }
}
