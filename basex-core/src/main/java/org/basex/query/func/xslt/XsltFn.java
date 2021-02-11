package org.basex.query.func.xslt;

import static org.basex.util.Reflect.*;

import java.util.concurrent.*;

import javax.xml.transform.*;

import org.basex.query.func.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class XsltFn extends StandardFunc {
  /** Templates cache. */
  static final ConcurrentHashMap<String, Templates> MAP = new ConcurrentHashMap<>();

  /** Saxon implementations. */
  static final String[] SAXON = {
    "com.saxonica.config.EnterpriseTransformerFactory",
    "com.saxonica.config.ProfessionalTransformerFactory",
    "net.sf.saxon.TransformerFactoryImpl"
  };

  /** Processor. */
  static final String PROCESSOR;
  /** Version. */
  static final String VERSION;

  static {
    String processor = "Java", version = "1.0";

    // check if system property has been assigned by the user
    final String fac = TransformerFactory.class.getName();
    final String impl = System.getProperty(fac);
    if(impl != null) {
      processor = "unknown";
      version = "unknown";
    } else {
      // search classpath for Saxon processors, retrieve edition and XSL version
      for(final String saxon : SAXON) {
        if(find(saxon) != null) {
          processor = "Saxon";
          System.setProperty(fac, saxon);
          final Class<?> vrsn = find("net.sf.saxon.Version");
          final Object se = get(field(vrsn, "softwareEdition"), null);
          if(se != null) processor += " " + se;
          final Object xsl = invoke(method(vrsn, "getXSLVersionString"), null);
          version = xsl != null ? xsl.toString() : "3.0";
          break;
        }
      }
    }
    PROCESSOR = processor;
    VERSION = version;
  }
}
