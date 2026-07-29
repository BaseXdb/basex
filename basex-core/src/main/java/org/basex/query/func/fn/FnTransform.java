package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnTransform.TransformOptions.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.xslt.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnTransform extends StandardFunc {
  /** Options for fn:transform. */
  public static final class TransformOptions extends Options {
    /** Map with QName keys and arbitrary values. */
    private static final SeqType QNAME_MAP = MapType.get(BasicType.QNAME, Types.ITEM_ZM).seqType();

    /** URI of the principal result document. */
    public static final StringOption BASE_OUTPUT_URI = new StringOption("base-output-uri");
    /** Cache the compiled stylesheet. */
    public static final BooleanOption CACHE = new BooleanOption("cache", true);
    /** Format of the delivered results. */
    public static final EnumOption<DeliveryFormat> DELIVERY_FORMAT =
        new EnumOption<>("delivery-format", DeliveryFormat.DOCUMENT);
    /** Evaluate xsl:assert instructions (ignored). */
    public static final BooleanOption ENABLE_ASSERTIONS =
        new BooleanOption("enable-assertions", false);
    /** Evaluate xsl:message instructions (ignored). */
    public static final BooleanOption ENABLE_MESSAGES = new BooleanOption("enable-messages", false);
    /** Generate trace messages (ignored). */
    public static final BooleanOption ENABLE_TRACE = new BooleanOption("enable-trace", false);
    /** Arguments of the initial function call. */
    public static final ValueOption FUNCTION_PARAMS =
        new ValueOption("function-params", Types.ARRAY_O, null);
    /** Global context item. */
    public static final ValueOption GLOBAL_CONTEXT_ITEM =
        new ValueOption("global-context-item", Types.ITEM_ZO, null);
    /** Name of the initial function. */
    public static final ValueOption INITIAL_FUNCTION =
        new ValueOption("initial-function", Types.QNAME_ZO, null);
    /** Initial match selection. */
    public static final ValueOption INITIAL_MATCH_SELECTION =
        new ValueOption("initial-match-selection", Types.ITEM_ZM, null);
    /** Name of the initial processing mode. */
    public static final ValueOption INITIAL_MODE =
        new ValueOption("initial-mode", Types.QNAME_ZO, null);
    /** Name of the initial template. */
    public static final ValueOption INITIAL_TEMPLATE =
        new ValueOption("initial-template", Types.QNAME_ZO, null);
    /** Location of the top-level stylesheet package. */
    public static final StringOption PACKAGE_LOCATION = new StringOption("package-location");
    /** Name of the top-level stylesheet package. */
    public static final StringOption PACKAGE_NAME = new StringOption("package-name");
    /** Node with the top-level stylesheet package. */
    public static final ValueOption PACKAGE_NODE =
        new ValueOption("package-node", Types.NODE_ZO, null);
    /** Text of the top-level stylesheet package. */
    public static final StringOption PACKAGE_TEXT = new StringOption("package-text");
    /** Version of the top-level stylesheet package. */
    public static final StringOption PACKAGE_VERSION = new StringOption("package-version");
    /** Function for post-processing the results. */
    public static final ValueOption POST_PROCESS = new ValueOption("post-process",
        FuncType.get(Types.ITEM_ZM, Types.STRING_O, Types.ITEM_ZM).seqType(), null);
    /** Requested properties of the XSLT processor. */
    public static final ValueOption REQUESTED_PROPERTIES = new ValueOption(
        "requested-properties", MapType.get(BasicType.QNAME, Types.ANY_ATOMIC_TYPE_O).seqType(),
        null);
    /** Serialization parameters of the principal result document. */
    public static final ValueOption SERIALIZATION_PARAMS =
        new ValueOption("serialization-params", Types.MAP_ZO, null);
    /** Location of the source document. */
    public static final StringOption SOURCE_LOCATION = new StringOption("source-location");
    /** Source node. */
    public static final ValueOption SOURCE_NODE =
        new ValueOption("source-node", Types.NODE_ZO, null);
    /** Values of static stylesheet parameters. */
    public static final ValueOption STATIC_PARAMS =
        new ValueOption("static-params", QNAME_MAP, null);
    /** Static base URI of the stylesheet. */
    public static final StringOption STYLESHEET_BASE_URI =
        new StringOption("stylesheet-base-uri");
    /** Location of the stylesheet. */
    public static final StringOption STYLESHEET_LOCATION =
        new StringOption("stylesheet-location");
    /** Node with the stylesheet. */
    public static final ValueOption STYLESHEET_NODE =
        new ValueOption("stylesheet-node", Types.NODE_ZO, null);
    /** Values of stylesheet parameters. */
    public static final ValueOption STYLESHEET_PARAMS =
        new ValueOption("stylesheet-params", QNAME_MAP, null);
    /** Text of the stylesheet. */
    public static final StringOption STYLESHEET_TEXT = new StringOption("stylesheet-text");
    /** Values of the parameters of the initial template. */
    public static final ValueOption TEMPLATE_PARAMS =
        new ValueOption("template-params", QNAME_MAP, null);
    /** Allow access to external resources. */
    public static final BooleanOption TRUSTED = new BooleanOption(CommonOptions.TRUSTED);
    /** Values of the tunnel parameters of the initial template. */
    public static final ValueOption TUNNEL_PARAMS =
        new ValueOption("tunnel-params", QNAME_MAP, null);
    /** Vendor-defined configuration options (ignored). */
    public static final ValueOption VENDOR_OPTIONS =
        new ValueOption("vendor-options", QNAME_MAP, null);
    /** Requested XSLT version. */
    public static final ValueOption XSLT_VERSION =
        new ValueOption("xslt-version", Types.DECIMAL_O, null);
  }

  /** Options that cannot be assigned via the JAXP interface. */
  private static final Option<?>[] UNSUPPORTED = { FUNCTION_PARAMS, GLOBAL_CONTEXT_ITEM,
    INITIAL_FUNCTION, INITIAL_MATCH_SELECTION, INITIAL_MODE, INITIAL_TEMPLATE, PACKAGE_LOCATION,
    PACKAGE_NAME, PACKAGE_NODE, PACKAGE_TEXT, PACKAGE_VERSION, STATIC_PARAMS, TEMPLATE_PARAMS,
    TUNNEL_PARAMS };
  /** Serialization parameters that are passed on to the XSLT processor. */
  private static final Set<String> OUTPUT_KEYS = Set.of(OutputKeys.CDATA_SECTION_ELEMENTS,
    OutputKeys.DOCTYPE_PUBLIC, OutputKeys.DOCTYPE_SYSTEM, OutputKeys.ENCODING, OutputKeys.INDENT,
    OutputKeys.MEDIA_TYPE, OutputKeys.METHOD, OutputKeys.OMIT_XML_DECLARATION,
    OutputKeys.STANDALONE, OutputKeys.VERSION);
  /** The only processor property that can be requested (it has no effect). */
  private static final QNm XSL_VERSION =
      new QNm("version", "http://www.w3.org/1999/XSL/Transform");

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TransformOptions options = options(qc);

    // reject requests that cannot be served by the JAXP interface
    for(final Option<?> option : UNSUPPORTED) {
      if(options.contains(option))
        throw TRANSFORM_PROCESSOR_X.get(info, "'" + option.name() + "' option");
    }
    final DeliveryFormat format = options.get(DELIVERY_FORMAT);
    if(format == DeliveryFormat.RAW)
      throw TRANSFORM_PROCESSOR_X.get(info, "'" + format + "' delivery format");
    version(options);
    requestedProperties(options);

    // resolve stylesheet, source document and target file
    final IO stylesheet = stylesheet(options);
    final Source source = source(options);
    final String uri = options.get(BASE_OUTPUT_URI);
    final IOFile file = format == DeliveryFormat.FILE ? file(uri, qc) : null;

    // assemble stylesheet parameters and output properties
    final HashMap<String, Object> params = stylesheetParams(options);
    final HashMap<String, String> properties = outputProperties(options, qc);

    // perform transformation: build a document node, or serialize the result
    final boolean tree = format == DeliveryFormat.DOCUMENT;
    final ArrayOutput output = tree ? null : new ArrayOutput();
    final XsltResult document = tree ?
      new XsltResult(uri != null ? token(uri) : EMPTY, qc.context.options) : null;

    final String error = Xslt.transform(stylesheet, source,
        tree ? document : new StreamResult(output),
        options.get(CACHE), trusted(options, qc), qc, tr -> {
          params.forEach(tr::setParameter);
          properties.forEach(tr::setOutputProperty);
        });
    if(error != null) throw TRANSFORM_ERROR_X.get(info, error);

    // deliver result, optionally post-processed by a user-defined function
    final Str key = Str.get(uri != null ? uri : "output");
    Value value = tree ? document.node() : deliver(output.finish(), file);
    final Value pp = options.get(POST_PROCESS);
    if(pp != null) {
      value = invoke(toFunction(pp, 2, qc), new HofArgs(2).set(0, key).set(1, value), qc);
    }
    return XQMap.get(key, value);
  }

  @Override
  public int hofOffsets() {
    return functionOption(0) ? Integer.MAX_VALUE : 0;
  }

  /**
   * Returns the transformation options. Values that are not permitted for an option are
   * rejected with a function-specific error.
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  private TransformOptions options(final QueryContext qc) throws QueryException {
    try {
      return toOptions(arg(0), new TransformOptions(), qc);
    } catch(final QueryException ex) {
      throw error(ex, ex.error() == INVALIDOPTIONVALUE_X ? TRANSFORM_OPTIONS_X : null);
    }
  }

  /**
   * Checks if the requested XSLT version is supported.
   * @param options transformation options
   * @throws QueryException query exception
   */
  private void version(final TransformOptions options) throws QueryException {
    final Value version = options.get(XSLT_VERSION);
    if(version == null || Xslt.VERSION.isEmpty()) return;
    final double requested = ((ANum) version).dbl();
    if(requested > Double.parseDouble(Xslt.VERSION))
      throw TRANSFORM_PROCESSOR_X.get(info, "XSLT " + version);
  }

  /**
   * Checks if the requested processor properties are supported. As the XSLT processor cannot be
   * chosen at runtime, only {@code xsl:version} is accepted (which has no effect).
   * @param options transformation options
   * @throws QueryException query exception
   */
  private void requestedProperties(final TransformOptions options) throws QueryException {
    final Value properties = options.get(REQUESTED_PROPERTIES);
    if(properties == null) return;
    for(final Item key : ((XQMap) properties).keys()) {
      if(!((QNm) key).eq(XSL_VERSION))
        throw TRANSFORM_PROCESSOR_X.get(info, "'" + key + "' property");
    }
  }

  /**
   * Returns the serialization parameters that will be passed on to the XSLT processor.
   * @param options transformation options
   * @param qc query context
   * @return output properties
   * @throws QueryException query exception
   */
  private HashMap<String, String> outputProperties(final TransformOptions options,
      final QueryContext qc) throws QueryException {

    final HashMap<String, String> properties = new HashMap<>();
    final Value sp = options.get(SERIALIZATION_PARAMS);
    if(sp != null) {
      final XQMap map = (XQMap) sp;
      // validate parameters, and pass on those that are known to XSLT processors
      new SerializerOptions().assign(map, qc, info);
      for(final XQMap.Entry entry : map.entries()) {
        final String name = string(entry.key().string(info));
        final Value value = entry.value();
        if(!OUTPUT_KEYS.contains(name) || value.isEmpty()) continue;
        final TokenBuilder tb = new TokenBuilder();
        for(final Item item : value) {
          if(!tb.isEmpty()) tb.add(' ');
          if(item instanceof final Bln bln) tb.add(bln.bool(info) ? Text.YES : Text.NO);
          else tb.add(item.string(info));
        }
        properties.put(name, tb.toString());
      }
    }
    return properties;
  }

  /**
   * Returns the values of the stylesheet parameters, with names in Clark notation.
   * @param options transformation options
   * @return parameters
   * @throws QueryException query exception
   */
  private static HashMap<String, Object> stylesheetParams(final TransformOptions options)
      throws QueryException {

    final HashMap<String, Object> params = new HashMap<>();
    final Value value = options.get(STYLESHEET_PARAMS);
    if(value != null) {
      for(final XQMap.Entry entry : ((XQMap) value).entries()) {
        params.put(((QNm) entry.key()).toJava().toString(), entry.value().toJava());
      }
    }
    return params;
  }

  /**
   * Returns a reference to the stylesheet.
   * @param options transformation options
   * @return input reference
   * @throws QueryException query exception
   */
  private IO stylesheet(final TransformOptions options) throws QueryException {
    final String location = options.get(STYLESHEET_LOCATION), text = options.get(STYLESHEET_TEXT);
    final XNode node = node(options.get(STYLESHEET_NODE));
    exclusive(location, text, node);

    if(location != null) return toIO(location, false);
    final String base = options.get(STYLESHEET_BASE_URI);
    final Uri uri = base != null ? Uri.get(sc().resolve(base).url()) : sc().baseURI();
    if(text != null) return new IOContent(text, string(uri.string()));
    if(node != null) return Xslt.io(node, uri, info);
    throw TRANSFORM_OPTIONS_X.get(info, "No stylesheet supplied.");
  }

  /**
   * Returns an input source for the source document.
   * @param options transformation options
   * @return source
   * @throws QueryException query exception
   */
  private Source source(final TransformOptions options) throws QueryException {
    final String location = options.get(SOURCE_LOCATION);
    final XNode node = node(options.get(SOURCE_NODE));
    exclusive(location, node);

    if(location != null) return toIO(location, false).streamSource();
    if(node != null) return Xslt.source(node, sc().baseURI(), info);
    throw TRANSFORM_OPTIONS_X.get(info, "No source node supplied.");
  }

  /**
   * Returns the file to which the result will be written.
   * @param uri base output URI (can be {@code null})
   * @param qc query context
   * @return file
   * @throws QueryException query exception
   */
  private IOFile file(final String uri, final QueryContext qc) throws QueryException {
    checkPerm(qc, Perm.ADMIN);
    if(uri == null) throw TRANSFORM_OPTIONS_X.get(info, "No base output URI supplied.");
    if(sc().resolve(uri) instanceof final IOFile file) return file;
    throw TRANSFORM_OPTIONS_X.get(info, "Base output URI is not writable: " + uri + '.');
  }

  /**
   * Rejects mutually exclusive options.
   * @param values supplied option values (can be {@code null})
   * @throws QueryException query exception
   */
  private void exclusive(final Object... values) throws QueryException {
    int count = 0;
    for(final Object value : values) {
      if(value != null) ++count;
    }
    if(count > 1) throw TRANSFORM_OPTIONS_X.get(info, "Mutually exclusive options were supplied.");
  }

  /**
   * Delivers a serialized transformation result.
   * @param result serialized result
   * @param file target file, or {@code null} if the result is returned as string
   * @return delivered result
   * @throws QueryException query exception
   */
  private Value deliver(final byte[] result, final IOFile file) throws QueryException {
    if(file == null) return Str.get(result);
    try {
      file.write(result);
    } catch(final IOException ex) {
      throw TRANSFORM_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }

  /**
   * Returns a node reference.
   * @param value option value (can be {@code null})
   * @return node, or {@code null} if no node was supplied
   */
  private static XNode node(final Value value) {
    return value instanceof final XNode node ? node : null;
  }

  /** Delivery formats. */
  public enum DeliveryFormat {
    /** Document.   */ DOCUMENT,
    /** Serialized. */ SERIALIZED,
    /** Raw.        */ RAW,
    /** File.       */ FILE;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
