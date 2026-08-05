package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnTransform.TransformOptions.*;
import static org.basex.util.Token.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.xml.transform.*;

import net.sf.saxon.lib.*;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.fn.FnTransform.*;
import org.basex.query.func.xslt.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * XSLT transformations via the Saxon s9api interface.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SaxonTransform {
  /** Cached stylesheets. */
  private static final ConcurrentHashMap<String, XsltExecutable> EXECS = new ConcurrentHashMap<>();

  /** Saxon processor, created on first access. */
  private static final class Saxon {
    /** Processor ({@code null} if Saxon is available but cannot be initialized). */
    private static final Processor PROC = processor();
    /** Factory for atomic item types. */
    private static final ItemTypeFactory ITEM_TYPES =
        PROC != null ? new ItemTypeFactory(PROC) : null;

    /** Private constructor. */
    private Saxon() { }

    /**
     * Creates the Saxon processor.
     * @return processor, or {@code null} if the processor cannot be created
     */
    private static Processor processor() {
      try {
        final Processor proc = new Processor();
        proc.setConfigurationProperty(Feature.RESULT_DOCUMENT_THREADS, 1);
        return proc;
      } catch(final Throwable th) {
        Util.debug(th);
        return null;
      }
    }
  }

  /** Function reference. */
  private final FnTransform func;
  /** Transformation options. */
  private final TransformOptions options;
  /** Allow access to external resources. */
  private final boolean trusted;
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;
  /** Delivery format. */
  private final DeliveryFormat format;
  /** Resource resolver ({@code null} if the default resolver is to be used). */
  private final ResourceResolver resolver;
  /** Serialization parameters of the principal result document. */
  private final HashMap<String, String> properties;
  /** Principal and secondary result documents. */
  private final ArrayList<ResultDoc> documents = new ArrayList<>();
  /** Dynamic errors reported by the XSLT processor. */
  private final ArrayList<XmlProcessingError> errors = new ArrayList<>();

  /**
   * Constructor.
   * @param func function reference
   * @param options transformation options
   * @param trusted allow access to external resources
   * @param qc query context
   * @throws QueryException query exception
   */
  SaxonTransform(final FnTransform func, final TransformOptions options, final boolean trusted,
      final QueryContext qc) throws QueryException {

    this.func = func;
    this.options = options;
    this.trusted = trusted;
    this.qc = qc;
    info = func.info();
    format = options.get(DELIVERY_FORMAT);
    properties = func.outputProperties(options, qc);

    // untrusted stylesheets must not access external resources
    final URIResolver ur = trusted ? qc.context.options.resolver().uriResolver() : null;
    resolver = trusted ? ur != null ? new ResourceResolverWrappingURIResolver(ur) : null :
      request -> { throw new XPathException(Util.info(Text.EXTACCESS_BLOCKED_X, request.uri)); };
  }

  /**
   * Indicates if the Saxon processor could be created.
   * @return result of check
   */
  public static boolean available() {
    return Saxon.PROC != null;
  }

  /**
   * Discards the cached stylesheets.
   */
  public static void init() {
    EXECS.clear();
  }

  /**
   * Performs the transformation.
   * @return map with the principal and the secondary result documents
   * @throws QueryException query exception
   */
  XQMap transform() throws QueryException {
    final Xslt30Transformer transformer = transformer();
    documents.add(new ResultDoc(options.get(BASE_OUTPUT_URI), true));

    try {
      invoke(transformer, documents.get(0).dest);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    } catch(final SaxonApiUncheckedException ex) {
      throw error(new SaxonApiException(ex.getCause()));
    } catch(final SaxonApiException ex) {
      throw error(ex);
    }

    final MapBuilder map = new MapBuilder();
    for(final ResultDoc doc : documents) {
      final Str key = Str.get(doc.key);
      map.put(key, func.postProcess(key, doc.value(), options, qc));
    }
    return map.map();
  }

  /**
   * Invokes the requested entry point of the stylesheet.
   * @param transformer transformer
   * @param destination destination of the principal result
   * @throws QueryException query exception
   * @throws SaxonApiException Saxon exception
   */
  private void invoke(final Xslt30Transformer transformer, final Destination destination)
      throws QueryException, SaxonApiException {

    final Value function = options.get(INITIAL_FUNCTION), template = options.get(INITIAL_TEMPLATE);
    final String location = options.get(SOURCE_LOCATION);
    final XNode node = FnTransform.node(options.get(SOURCE_NODE));
    final Value selection = options.get(INITIAL_MATCH_SELECTION);
    func.exclusive(function, template, options.get(INITIAL_MODE));
    func.exclusive(location, node, selection);

    if(function != null) {
      final Value params = options.get(FUNCTION_PARAMS);
      if(params == null)
        throw TRANSFORM_OPTIONS_X.get(info, "No function parameters were supplied.");
      final ArrayList<XdmValue> args = new ArrayList<>();
      for(final Value param : ((XQArray) params).members()) args.add(value(param));
      transformer.callFunction(qname(function), args.toArray(XdmValue[]::new), destination);
    } else {
      final XdmNode source = source(location, node);
      final XdmValue match = source != null ? source :
        selection != null ? value(selection) : null;
      if(options.get(GLOBAL_CONTEXT_ITEM) == null && match != null && match.size() == 1 &&
          match.itemAt(0) instanceof final XdmNode nd)
        transformer.setGlobalContextItem(nd.getRoot());

      if(template != null) transformer.callTemplate(qname(template), destination);
      else if(match != null) transformer.applyTemplates(match, destination);
      else transformer.callTemplate(null, destination);
    }
  }

  /**
   * Creates and prepares a transformer.
   * @return transformer
   * @throws QueryException query exception
   */
  private Xslt30Transformer transformer() throws QueryException {
    final Xslt30Transformer transformer = executable().load30();
    transformer.setErrorReporter(errors::add);
    if(resolver != null) transformer.setResourceResolver(resolver);
    if(!trusted) transformer.setUnparsedTextResolver((uri, encoding, config, unused) -> {
      throw new XPathException(Util.info(Text.EXTACCESS_BLOCKED_X, uri));
    });
    if(!options.get(ENABLE_MESSAGES)) transformer.setMessageHandler(message -> { });

    try {
      final String uri = options.get(BASE_OUTPUT_URI);
      if(uri != null) transformer.setBaseOutputURI(func.sc().resolve(uri).url());
      final Value context = options.get(GLOBAL_CONTEXT_ITEM);
      if(context != null && !context.isEmpty())
        transformer.setGlobalContextItem(item((Item) context));
      final Value mode = options.get(INITIAL_MODE);
      if(mode != null) transformer.setInitialMode(qname(mode));
      final Value sparams = options.get(STYLESHEET_PARAMS);
      if(sparams != null) transformer.setStylesheetParameters(params(sparams));
      final Value tparams = options.get(TEMPLATE_PARAMS);
      if(tparams != null) transformer.setInitialTemplateParameters(params(tparams), false);
      final Value uparams = options.get(TUNNEL_PARAMS);
      if(uparams != null) transformer.setInitialTemplateParameters(params(uparams), true);
    } catch(final SaxonApiException ex) {
      throw error(ex);
    }

    // collect the secondary result documents
    transformer.setResultDocumentHandler(uri -> {
      try {
        final ResultDoc doc = new ResultDoc(uri.toString(), false);
        documents.add(doc);
        return doc.dest;
      } catch(final QueryException ex) {
        throw new QueryRTException(ex);
      }
    });
    return transformer;
  }

  /**
   * Compiles the stylesheet or the top-level stylesheet package.
   * @return compiled stylesheet
   * @throws QueryException query exception
   */
  private XsltExecutable executable() throws QueryException {
    final String location = options.get(PACKAGE_LOCATION), text = options.get(PACKAGE_TEXT),
        name = options.get(PACKAGE_NAME);
    final XNode node = FnTransform.node(options.get(PACKAGE_NODE));
    func.exclusive(options.get(STYLESHEET_LOCATION), options.get(STYLESHEET_TEXT),
        FnTransform.node(options.get(STYLESHEET_NODE)), location, text, name, node);

    final IO pkg = name != null ? null : func.input(location, text, node, options);
    final boolean packaged = name != null || pkg != null;
    final IO io = packaged ? pkg : func.stylesheet(options);
    final String version = version();
    final String pversion = name != null ? options.get(PACKAGE_VERSION) : null;
    final Value sparams = options.get(STATIC_PARAMS);

    // only stylesheets that are supplied by location are cached: string and node
    // representations may share their base URI with other stylesheets
    final String key = options.get(CACHE) && sparams == null && !(io instanceof IOContent) ?
      (io != null ? io.url() : name) + '|' + pversion + '|' + trusted + '|' +
      options.get(ENABLE_ASSERTIONS) + '|' + version : null;

    XsltExecutable executable = key != null ? EXECS.get(key) : null;
    if(executable == null) {
      final ArrayList<XmlProcessingError> statics = new ArrayList<>();
      final XsltCompiler compiler = Saxon.PROC.newXsltCompiler();
      compiler.setErrorReporter(statics::add);
      if(resolver != null) compiler.setResourceResolver(resolver);
      compiler.setAssertionsEnabled(options.get(ENABLE_ASSERTIONS));
      if(version != null) compiler.setXsltLanguageVersion(version);
      try {
        if(sparams != null) {
          for(final XQMap.Entry entry : ((XQMap) sparams).entries()) {
            compiler.setParameter(qname(entry.key()), value(entry.value()));
          }
        }
        executable = name != null ?
          compiler.obtainPackage(name, pversion != null ? pversion : "*").link() :
          packaged ? compiler.compilePackage(io.streamSource()).link() :
            compiler.compile(io.streamSource());
      } catch(final SaxonApiException ex) {
        throw error(statics, ex);
      }
      if(key != null) EXECS.put(key, executable);
    }
    return executable;
  }

  /**
   * Returns the requested XSLT language version.
   * @return version, or {@code null} if the version of the stylesheet is to be used
   * @throws QueryException query exception
   */
  private String version() throws QueryException {
    final Value version = options.get(XSLT_VERSION);
    if(version == null) return null;
    // XSLT 1.0 and 2.0 are processed in backwards compatibility mode
    final double requested = ((ANum) version).dbl();
    if(requested == 1 || requested == 2) return null;
    if(requested == 3) return "3.0";
    if(requested == 4) return "4.0";
    throw TRANSFORM_PROCESSOR_X.get(info, "XSLT version " + version + " (max 4.0)");
  }

  /**
   * Returns the source document.
   * @param location location of the source document (can be {@code null})
   * @param node source node (can be {@code null})
   * @return source document, or {@code null} if no source was supplied
   * @throws QueryException query exception
   */
  private XdmNode source(final String location, final XNode node) throws QueryException {
    if(node != null) return node(node);
    if(location == null) return null;
    final Source source = func.input(location, null, null, options).streamSource();
    try {
      return Saxon.PROC.newDocumentBuilder().build(source);
    } catch(final SaxonApiException ex) {
      throw error(ex);
    }
  }

  /**
   * Converts stylesheet or template parameters.
   * @param value map with QName keys
   * @return parameters
   * @throws QueryException query exception
   */
  private Map<QName, XdmValue> params(final Value value) throws QueryException {
    final Map<QName, XdmValue> params = new HashMap<>();
    for(final XQMap.Entry entry : ((XQMap) value).entries()) {
      params.put(qname(entry.key()), value(entry.value()));
    }
    return params;
  }

  /**
   * Converts a QName.
   * @param value QName item
   * @return converted QName
   */
  private static QName qname(final Value value) {
    final QNm qnm = (QNm) value;
    return new QName(string(qnm.prefix()), string(qnm.uri()), string(qnm.local()));
  }

  /**
   * Converts an XQuery value.
   * @param value value
   * @return converted value
   * @throws QueryException query exception
   */
  private XdmValue value(final Value value) throws QueryException {
    final ArrayList<XdmItem> items = new ArrayList<>();
    for(final Item item : value) items.add(item(item));
    return new XdmValue(items);
  }

  /**
   * Converts an XQuery item.
   * @param item item
   * @return converted item
   * @throws QueryException query exception
   */
  private XdmItem item(final Item item) throws QueryException {
    if(item instanceof final XNode node) return node(node);
    if(item instanceof final QNm qnm) return new XdmAtomicValue(qname(qnm));
    if(item instanceof FItem) throw TRANSFORM_PROCESSOR_X.get(info, item.seqType() + " values");
    try {
      final ItemType type = item.type instanceof final BasicType bt ?
        Saxon.ITEM_TYPES.getAtomicType(qname(bt.qname())) : ItemType.UNTYPED_ATOMIC;
      return new XdmAtomicValue(string(item.string(info)), type);
    } catch(final SaxonApiException ex) {
      throw error(ex);
    }
  }

  /**
   * Converts a node. Nodes are serialized and rebuilt, i.e. their identity is not preserved.
   * @param node node
   * @return converted node
   * @throws QueryException query exception
   */
  private XdmNode node(final XNode node) throws QueryException {
    final Source source = Xslt.source(node, func.sc().baseURI(), info);
    try {
      final XdmNode doc = Saxon.PROC.newDocumentBuilder().build(source);
      return node.kind() == Kind.DOCUMENT ? doc : doc.getOutermostElement();
    } catch(final SaxonApiException ex) {
      throw error(ex);
    }
  }

  /**
   * Converts a raw transformation result.
   * @param xdm value
   * @return converted value
   * @throws QueryException query exception
   */
  private Value value(final XdmValue xdm) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final XdmItem item : xdm) {
      if(item instanceof final XdmNode node) vb.add(node(node));
      else if(item instanceof final XdmAtomicValue atomic) vb.add(atomic(atomic));
      else throw TRANSFORM_PROCESSOR_X.get(info, "raw results of type " + item.getClass());
    }
    return vb.value();
  }

  /**
   * Converts an atomic value of a raw transformation result.
   * @param atomic atomic value
   * @return converted item
   * @throws QueryException query exception
   */
  private Item atomic(final XdmAtomicValue atomic) throws QueryException {
    final QName name = atomic.getTypeName();
    final BasicType type = BasicType.get(new QNm(name.getLocalName(), name.getNamespace()), true);
    if(type == BasicType.QNAME) return qnm(atomic.getQNameValue());
    return (type != null ? type : BasicType.UNTYPED_ATOMIC).cast(atomic.getStringValue(), qc, info);
  }

  /**
   * Converts a node of a transformation result to a BaseX node via the public s9api API.
   * @param node Saxon node
   * @return BaseX node
   */
  private FNode node(final XdmNode node) {
    return node(node, Map.of());
  }

  /**
   * Converts a node, declaring only the namespaces that differ from the enclosing scope.
   * @param node Saxon node
   * @param inscope in-scope namespaces (prefix to URI)
   * @return BaseX node
   */
  private FNode node(final XdmNode node, final Map<String, String> inscope) {
    return switch(node.getNodeKind()) {
      case DOCUMENT -> {
        final URI uri = node.getBaseURI();
        final FBuilder doc = FDoc.build(uri != null ? token(uri.toString()) : EMPTY);
        for(final XdmNode child : node.children()) doc.node(node(child, inscope));
        yield doc.finish();
      }
      case ELEMENT -> {
        final FBuilder elem = FElem.build(qnm(node.getNodeName()));
        // declare only namespaces that are new or rebound compared to the parent
        final Map<String, String> scope = new HashMap<>();
        for(final XdmSequenceIterator<XdmNode> ns = node.axisIterator(Axis.NAMESPACE);
            ns.hasNext();) {
          final XdmNode n = ns.next();
          final QName name = n.getNodeName();
          final String prefix = name != null ? name.getLocalName() : "";
          if(prefix.equals("xml")) continue;
          final String uri = n.getStringValue();
          scope.put(prefix, uri);
          if(!uri.equals(inscope.get(prefix))) elem.ns(token(prefix), token(uri));
        }
        // undeclared default namespace
        if(inscope.containsKey("") && !scope.containsKey("")) elem.ns(EMPTY, EMPTY);
        for(final XdmSequenceIterator<XdmNode> at = node.axisIterator(Axis.ATTRIBUTE);
            at.hasNext();) {
          final XdmNode a = at.next();
          elem.attr(qnm(a.getNodeName()), token(a.getStringValue()));
        }
        for(final XdmNode child : node.children()) elem.node(node(child, scope));
        yield elem.finish();
      }
      case TEXT -> new FTxt(token(node.getStringValue()));
      case COMMENT -> new FComm(token(node.getStringValue()));
      case PROCESSING_INSTRUCTION ->
        new FPI(qnm(node.getNodeName()), token(node.getStringValue()));
      case ATTRIBUTE -> new FAttr(qnm(node.getNodeName()), token(node.getStringValue()));
      default -> throw Util.notExpected(node.getNodeKind());
    };
  }

  /**
   * Converts a QName.
   * @param name QName
   * @return converted QName
   */
  private static QNm qnm(final QName name) {
    return new QNm(token(name.getPrefix()), name.getLocalName(), token(name.getNamespace()));
  }

  /**
   * Assembles an error for a failed transformation.
   * @param ex Saxon exception
   * @return query exception
   */
  private QueryException error(final SaxonApiException ex) {
    return error(errors, ex);
  }

  /**
   * Assembles an error for a failed transformation.
   * @param list reported errors
   * @param ex Saxon exception
   * @return query exception
   */
  private QueryException error(final List<XmlProcessingError> list, final SaxonApiException ex) {
    Util.debug(ex);
    QName code = ex.getErrorCode();
    final StringList sl = new StringList();
    for(final XmlProcessingError error : list) {
      if(error.isWarning()) continue;
      if(code == null) code = error.getErrorCode();
      Xslt.message(sl, error.getMessage());
    }
    if(sl.isEmpty()) Xslt.message(sl, Util.message(ex));
    final String message = String.join("; ", sl.finish());
    return code == null ? TRANSFORM_ERROR_X.get(info, message) :
      new QueryException(info, qnm(code), message);
  }

  /** Single result document. */
  private final class ResultDoc {
    /** Key of the result in the returned map. */
    private final String key;
    /** Destination of the transformation. */
    private final Destination dest;
    /** Destination for the assembled document node (delivery format: document). */
    private XdmDestination document;
    /** Serialized result (delivery format: serialized). */
    private ArrayOutput output;

    /**
     * Constructor.
     * @param uri output URI of the result ({@code null} for a principal result without base URI)
     * @param main main result document
     * @throws QueryException query exception
     */
    private ResultDoc(final String uri, final boolean main) throws QueryException {
      key = uri != null ? uri : "output";
      switch(format) {
        case DOCUMENT -> {
          document = new XdmDestination();
          dest = document;
        }
        case SERIALIZED -> {
          output = new ArrayOutput();
          dest = serializer(Saxon.PROC.newSerializer(output), main);
        }
        case FILE -> dest = serializer(Saxon.PROC.newSerializer(func.file(uri, qc).file()), main);
        default -> dest = new RawDestination();
      }
    }

    /**
     * Returns the delivered result.
     * @return result
     * @throws QueryException query exception
     */
    private Value value() throws QueryException {
      // finalize the destination (flush serializer, complete the document tree)
      try {
        dest.close();
      } catch(final SaxonApiException ex) {
        throw error(ex);
      }
      return switch(format) {
        case DOCUMENT   -> {
          final XdmNode nd = document.getXdmNode();
          yield nd != null ? node(nd) : FDoc.build().finish();
        }
        case SERIALIZED -> Str.get(output.finish());
        case FILE       -> Empty.VALUE;
        default         -> SaxonTransform.this.value(((RawDestination) dest).getXdmValue());
      };
    }

    /**
     * Assigns the serialization parameters, which only apply to the principal result document.
     * @param serializer serializer
     * @param principal principal result document
     * @return serializer
     */
    private Serializer serializer(final Serializer serializer, final boolean principal) {
      if(principal) properties.forEach((name, value) -> {
        try {
          serializer.setOutputProperty(new QName("", name), value);
        } catch(final IllegalArgumentException ex) {
          Util.debug(ex);
        }
      });
      return serializer;
    }
  }
}
