package org.basex.query.func.xslt;

import java.util.concurrent.*;

import javax.xml.transform.*;

import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
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
      if(Reflect.find(impl) == null) continue;

      if(SAXONS.contains(impl)) {
        // Saxon: assign to system property, retrieve edition and XSL version
        processor = "Saxon";
        if(!impl.equals(property)) System.setProperty(clazz, impl);
        final Class<?> vrsn = Reflect.find("net.sf.saxon.Version");
        final Object se = Reflect.get(Reflect.field(vrsn, "softwareEdition"), null);
        if(se != null) processor += " " + se;
        final Object xsl = Reflect.invoke(Reflect.method(vrsn, "getXSLVersionString"), null);
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
