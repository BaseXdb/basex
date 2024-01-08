package org.basex.query.func.xslt;

import static org.basex.util.Reflect.*;

import java.util.concurrent.*;

import javax.xml.transform.*;

import org.basex.query.func.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class XsltFn extends StandardFunc {
  /** Templates cache. */
  static final ConcurrentHashMap<String, Templates> MAP = new ConcurrentHashMap<>();

  /** Saxon implementations. */
  private static final StringList SAXONS = new StringList(
    "com.saxonica.config.EnterpriseTransformerFactory",
    "com.saxonica.config.ProfessionalTransformerFactory",
    "net.sf.saxon.TransformerFactoryImpl"
  );

  /** Processor. */
  static final String PROCESSOR;
  /** Version. */
  static final String VERSION;

  static {
    // check for system property, create list of implementations to check
    final String clazz = TransformerFactory.class.getName();
    final String property = System.getProperty(clazz);
    final StringList impls = new StringList();
    if(property != null) impls.add(property);
    impls.add(SAXONS);

    // search for implementation (custom, predefined)
    String processor = "Java", version = "1.0";
    for(final String impl : impls) {
      if(find(impl) == null) continue;

      if(SAXONS.contains(impl)) {
        // Saxon: assign to system property, retrieve edition and XSL version
        if(!impl.equals(property)) System.setProperty(clazz, impl);
        final Class<?> vrsn = find("net.sf.saxon.Version");
        final Object se = get(field(vrsn, "softwareEdition"), null);
        if(se != null) processor += " " + se;
        final Object xsl = invoke(method(vrsn, "getXSLVersionString"), null);
        processor = "Saxon";
        version = xsl != null ? xsl.toString() : "3.0";
      } else {
        // unknown: assign classpath
        processor = impl;
        version = "";
      }
      break;
    }
    PROCESSOR = processor;
    VERSION = version;
  }
}
