package org.basex.query.func.xslt;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XSLT transformations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Xslt {
  /** Templates cache. */
  private static final ConcurrentHashMap<String, Templates> CACHE = new ConcurrentHashMap<>();

  /** Saxon implementations. */
  private static final StringList SAXONS = new StringList(
    "com.saxonica.config.EnterpriseTransformerFactory",
    "com.saxonica.config.ProfessionalTransformerFactory",
    "net.sf.saxon.TransformerFactoryImpl"
  );

  /** Processor. */
  public static final String PROCESSOR;
  /** Version. */
  public static final String VERSION;

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

  /** Private constructor. */
  private Xslt() { }

  /**
   * Discards the cached stylesheets.
   */
  public static void init() {
    CACHE.clear();
  }

  /**
   * Returns an input reference for a node.
   * @param node node
   * @param base base URI of the query
   * @param info input info (can be {@code null})
   * @return input reference
   * @throws QueryException query exception
   */
  public static IO io(final XNode node, final Uri base, final InputInfo info)
      throws QueryException {
    try {
      final Uri uri = node.baseURI(base, true, info);
      return new IOContent(node.serialize().finish(), string(uri.string()));
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Returns an input source for a node. Documents that cannot be serialized as well-formed XML
   * (multiple root elements, text children) are passed on as DOM.
   * @param node node
   * @param base base URI of the query
   * @param info input info (can be {@code null})
   * @return source
   * @throws QueryException query exception
   */
  public static Source source(final XNode node, final Uri base, final InputInfo info)
      throws QueryException {
    if(node.kind() != Kind.DOCUMENT || Types.DOCUMENT_ELEMENT.test.matches(node))
      return io(node, base, info).streamSource();

    final DOMSource source = new DOMSource(node.toJava());
    source.setSystemId(string(node.baseURI(base, true, info).string()));
    return source;
  }

  /**
   * Compiles a stylesheet and performs a transformation.
   * @param stylesheet stylesheet
   * @param source input source
   * @param result transformation result
   * @param cache cache compiled stylesheet
   * @param trusted allow access to external resources
   * @param qc query context
   * @param prepare callback for assigning parameters and output properties
   * @return error message, or {@code null} if the transformation was successful
   */
  public static String transform(final IO stylesheet, final Source source,
      final Result result, final boolean cache, final boolean trusted, final QueryContext qc,
      final Consumer<Transformer> prepare) {

    final PrintStream errPS = System.err;
    final ArrayOutput err = new ArrayOutput();
    try {
      // redirect errors
      System.setErr(new PrintStream(err));
      final Transformer tr = transformer(stylesheet, cache, trusted, qc);
      prepare.accept(tr);
      tr.transform(source, result);
      return null;
    } catch(final IllegalArgumentException | TransformerException |
        TransformerFactoryConfigurationError ex) {
      // Saxon raises runtime exceptions for illegal parameters
      Util.debug(ex);
      // collect transformation errors, most specific one last
      final StringList list = new StringList();
      final Consumer<String> add = string -> {
        final String normalized = string != null ? string.replaceAll("\\s+", " ").trim() : "";
        if(!normalized.isEmpty()) list.addUnique(normalized);
      };
      for(Throwable th = ex; th != null; th = th.getCause()) add.accept(th.getMessage());
      try {
        add.accept(new String(err.toArray(), Prop.CHARSET));
      } catch(final Exception e) {
        Util.debug(e);
        add.accept(e.getMessage());
      }
      return String.join("; ", list.reverse().finish());
    } finally {
      System.setErr(errPS);
    }
  }

  /**
   * Returns a transformer for the specified stylesheet.
   * @param stylesheet stylesheet
   * @param cache cache compiled stylesheet
   * @param trusted allow access to external resources
   * @param qc query context
   * @return transformer
   * @throws TransformerException transformer exception
   */
  private static Transformer transformer(final IO stylesheet, final boolean cache,
      final boolean trusted, final QueryContext qc) throws TransformerException {

    // only stylesheets that are supplied by location are cached: string and node
    // representations may share their base URI with other stylesheets
    final String key = cache && !(stylesheet instanceof IOContent) ?
      (trusted ? "" : "!") + stylesheet.url() : null;
    final URIResolver ur = qc.context.options.resolver().uriResolver();

    // retrieve new or cached templates object
    Templates templates = key != null ? CACHE.get(key) : null;
    if(templates == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      // untrusted stylesheets must not include or import external modules
      if(!trusted) {
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      }
      // assign catalog resolver (if defined)
      if(ur != null) tf.setURIResolver(ur);
      templates = tf.newTemplates(stylesheet.streamSource());
      if(key != null) CACHE.put(key, templates);
    }

    // create transformer, assign catalog resolver (if defined)
    final Transformer tr = templates.newTransformer();
    if(trusted) {
      if(ur != null) tr.setURIResolver(ur);
    } else {
      // untrusted stylesheets must not retrieve external documents
      tr.setURIResolver((href, base) -> {
        throw new TransformerException(Util.info(Text.EXTACCESS_BLOCKED_X, href));
      });
    }
    return tr;
  }
}
