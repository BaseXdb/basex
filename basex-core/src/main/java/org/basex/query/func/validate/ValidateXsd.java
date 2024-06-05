package org.basex.query.func.validate;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
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
  private static final boolean SAXON;
  /** Java flag. */
  private static final boolean JAVA;

  static {
    int i = 0;
    final int il = IMPL.length;
    while(i + 3 < il && Reflect.find(IMPL[i]) == null) i += 3;
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
    return validate(new Validation() {
      @Override
      void validate() throws IOException, SAXException, QueryException {
        final IO input = read(toNodeOrAtomItem(arg(0), false, qc), null);
        final Item schema = toNodeOrAtomItem(arg(1), true, qc);
        final HashMap<String, String> options = toOptions(arg(2), qc);

        final String url = schema == null ? "" : prepare(read(schema, null)).url();
        final String caching = options.remove("cache");
        final boolean cache = caching != null && Strings.toBoolean(caching);

        Schema s = cache ? MAP.get(url) : null;
        if(s == null) {
          // create schema factory and set version
          final SchemaFactory sf;
          if(JAVA) {
            sf = SchemaFactory.newInstance(FACTORY);
          } else {
            final Class<?> clz = Reflect.find(IMPL[OFFSET]);
            // catch Saxon errors (e.g. NoClassDefFoundError: org/xmlresolver/Resolver)
            try {
              sf = (SchemaFactory) clz.getDeclaredConstructor().newInstance();
            } catch(final Exception ex) {
              throw new BaseXException(ex);
            }
            // Saxon: use version 1.1
            if(SAXON) sf.setProperty(SAXON_VERSION_URI, IMPL[OFFSET + 2]);
          }
          sf.setErrorHandler(this);

          final LSResourceResolver ls = Resolver.resources(qc.context.options);
          if(ls != null) sf.setResourceResolver(ls);

          // assign parser features
          for(final Entry<String, String> entry : options.entrySet()) {
            sf.setFeature(entry.getKey(), Strings.toBoolean(entry.getValue()));
          }
          // schema declaration is included in document, or specified as string
          s = url.isEmpty() ? sf.newSchema() : sf.newSchema(new URL(url));
          if(cache) MAP.put(url, s);
        }

        final Validator v = s.newValidator();
        v.setErrorHandler(this);
        v.validate(input instanceof IOContent || input instanceof IOStream ?
            new StreamSource(input.inputStream()) : new StreamSource(input.url()));
      }
    });
  }
}
