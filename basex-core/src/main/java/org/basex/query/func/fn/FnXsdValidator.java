package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.basex.build.*;
import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.parse.xml.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.validate.*;
import org.basex.query.func.validate.ErrorInfo.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnXsdValidator extends StandardFunc {
  /** Options for fn:xsd-validator. */
  public static final class XsdValidatorOptions extends Options {
    /** Whether external resources may be fetched. */
    public static final BooleanOption TRUSTED = new BooleanOption(CommonOptions.TRUSTED);
    /** Use schema components of the static context (ignored: no schema imports are supported). */
    public static final BooleanOption USE_IMPORTED_SCHEMA =
        new BooleanOption("use-imported-schema", true);
    /** Schema documents. */
    public static final ValueOption SCHEMA = new ValueOption("schema", Types.ELEMENT_ZM);
    /** Target namespaces of schema components. */
    public static final ValueOption TARGET_NAMESPACE =
        new ValueOption("target-namespace", Types.ANY_URI_ZM);
    /** Locations of schema documents. */
    public static final ValueOption SCHEMA_LOCATION =
        new ValueOption("schema-location", Types.ANY_URI_ZM);
    /** Flag for using xsi:schemaLocation. */
    public static final BooleanOption USE_XSI_SCHEMA_LOCATION =
        new BooleanOption(CommonOptions.USE_XSI_SCHEMA_LOCATION, false);
    /** XSD version. */
    public static final ValueOption XSD_VERSION = new ValueOption("xsd-version", Types.DECIMAL_O);
    /** Validation mode. */
    public static final EnumOption<ValidationMode> VALIDATION_MODE =
        new EnumOption<>("validation-mode", ValidationMode.STRICT);
    /** Governing type. */
    public static final ValueOption TYPE = new ValueOption("type", Types.QNAME_ZO);
    /** Return the validated node. */
    public static final BooleanOption RETURN_TYPED_NODE =
        new BooleanOption("return-typed-node", true);
    /** Return details on invalidities. */
    public static final BooleanOption RETURN_ERROR_DETAILS =
        new BooleanOption("return-error-details", false);
  }

  /** SAX property for comments and CDATA sections, which validators do not forward. */
  private static final String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

  /** Type of the nodes to be validated. */
  public static final SeqType TARGET_TYPE = ChoiceItemType.get(Types.DOCUMENT_ELEMENT,
      NodeType.ELEMENT, NodeType.ATTRIBUTE).seqType(Occ.ZERO_OR_ONE);
  /** Type of the validation result. */
  private static final SeqType RESULT_TYPE =
      Records.VALIDATION_RESULT.get().seqType(Occ.ZERO_OR_ONE);
  /** Type of the returned validation function. */
  public static final FuncType VALIDATOR_TYPE = FuncType.get(RESULT_TYPE, TARGET_TYPE);

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XsdValidatorOptions options = toOptions(arg(0), new XsdValidatorOptions(), qc);
    final boolean trusted = trusted(options, qc);
    final boolean xsi = options.get(XsdValidatorOptions.USE_XSI_SCHEMA_LOCATION);
    if(xsi && !trusted) throw EXTERNALRESOURCE_X.get(info, "'use-xsi-schema-location'");
    // as in fn:doc, external resources are only retrieved with additional permissions
    if(xsi || !options.get(XsdValidatorOptions.SCHEMA_LOCATION).isEmpty())
      checkPerm(qc, Perm.CREATE);
    checkSupported(options);

    final Var var = new VarScope().addNew(new QNm("target"), TARGET_TYPE, qc, info);
    final Var[] params = { var };
    final Expr validate = new Validate(info, schema(options, trusted, qc),
        options.get(XsdValidatorOptions.RETURN_TYPED_NODE),
        options.get(XsdValidatorOptions.RETURN_ERROR_DETAILS), xsi, new VarRef(info, var));
    return new FuncItem(info, validate, params, AnnList.EMPTY, VALIDATOR_TYPE,
        params.length, null);
  }

  /**
   * Rejects options that require a schema-aware processor.
   * @param options validation options
   * @throws QueryException query exception
   */
  private void checkSupported(final XsdValidatorOptions options) throws QueryException {
    final ValidationMode mode = options.get(XsdValidatorOptions.VALIDATION_MODE);
    if(mode != ValidationMode.STRICT) throw NOSCHEMAAWARENESS_X.get(info, "'" + mode + "' mode");
    if(!options.get(XsdValidatorOptions.TYPE).isEmpty())
      throw NOSCHEMAAWARENESS_X.get(info, "'type' option");
    if(!options.get(XsdValidatorOptions.TARGET_NAMESPACE).isEmpty())
      throw NOSCHEMAAWARENESS_X.get(info, "'target-namespace' option");
    final Value version = options.get(XsdValidatorOptions.XSD_VERSION);
    if(!version.isEmpty() && !ValidateXsd.supports(((Item) version).dec(info)))
      throw NOSCHEMAAWARENESS_X.get(info, "XSD " + version);
  }

  /**
   * Assembles a schema from the schema documents referenced by the options.
   * @param options validation options
   * @param trusted allow access to external resources
   * @param qc query context
   * @return schema
   * @throws QueryException query exception
   */
  private Schema schema(final XsdValidatorOptions options, final boolean trusted,
      final QueryContext qc) throws QueryException {

    final ArrayList<Source> sources = new ArrayList<>();
    try {
      // schema documents supplied as nodes
      for(final Item item : options.get(XsdValidatorOptions.SCHEMA)) {
        sources.add(source((XNode) item).streamSource());
      }
      // schema documents supplied as locations
      for(final Item location : options.get(XsdValidatorOptions.SCHEMA_LOCATION)) {
        final IO io = toIO(toString(location), false);
        sources.add(new StreamSource(io.inputStream(), io.url()));
      }

      final SchemaFactory sf = ValidateXsd.factory(qc.context.options);
      // indirectly referenced schema documents are only retrieved in trusted mode
      if(!trusted) ValidateXsd.restrict(sf);
      return sources.isEmpty() ? sf.newSchema() : sf.newSchema(sources.toArray(Source[]::new));
    } catch(final QueryIOException ex) {
      throw ex.getCause();
    } catch(final IOException | SAXException ex) {
      throw SCHEMAASSEMBLY_X.get(info, ex);
    }
  }

  /**
   * Returns the serialized node.
   * @param node node
   * @return input
   * @throws QueryIOException serialization exception
   */
  private static IOContent source(final XNode node) throws QueryIOException {
    return new IOContent(node.serialize().finish(), Token.string(node.baseURI()));
  }

  /**
   * Result function of fn:xsd-validator: validate a node against an assembled schema.
   */
  private static final class Validate extends Arr {
    /** Assembled schema. */
    private final Schema schema;
    /** Return typed node. */
    private final boolean typedNode;
    /** Return error details. */
    private final boolean errorDetails;
    /** Resolve xsi:schemaLocation attributes of the validated node. */
    private final boolean xsiLocation;
    /** XML readers (cached per thread; readers are not thread-safe). */
    private final ThreadLocal<XMLReader> readers = new ThreadLocal<>();

    /**
     * Constructor.
     * @param info input info (can be {@code null})
     * @param schema assembled schema
     * @param typedNode return typed node
     * @param errorDetails return error details
     * @param xsiLocation resolve xsi:schemaLocation attributes
     * @param args function arguments
     */
    private Validate(final InputInfo info, final Schema schema, final boolean typedNode,
        final boolean errorDetails, final boolean xsiLocation, final Expr... args) {
      super(info, RESULT_TYPE, args);
      this.schema = schema;
      this.typedNode = typedNode;
      this.errorDetails = errorDetails;
      this.xsiLocation = xsiLocation;
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final Item item = arg(0).item(qc, info);
      if(item.isEmpty()) return Empty.VALUE;

      final XNode node = (XNode) item;
      if(node.type.instanceOf(NodeType.ATTRIBUTE))
        throw NOSCHEMAAWARENESS_X.get(info, "validation of attributes");

      final ArrayList<ErrorInfo> errors = new ArrayList<>();
      Value typed = Empty.VALUE;
      try {
        final IOContent input = source(node);
        final XMLReader reader = reader();
        final ValidatorHandler handler = handler(errors);
        reader.setContentHandler(handler);
        if(typedNode) {
          // the validated input is passed on to a database builder
          typed = new DBNode(MemBuilder.build(new ValidatingParser(input, reader, handler,
              node.type.instanceOf(NodeType.DOCUMENT), qc.context.options)));
        } else {
          handler.setContentHandler(null);
          reader.setProperty(LEXICAL_HANDLER, null);
          reader.parse(input.inputSource());
        }
      } catch(final QueryIOException ex) {
        throw ex.getCause();
      } catch(final IOException | SAXException | ParserConfigurationException ex) {
        // the validation is aborted as soon as an invalidity is found
        if(errors.isEmpty()) throw XSDVALIDATIONERR_X.get(info, ex);
      }

      final boolean valid = errors.isEmpty();
      if(!valid) typed = Empty.VALUE;
      final Value details = !valid && errorDetails ? details(errors, qc) : Empty.VALUE;
      return new XQRecordMap(Records.VALIDATION_RESULT.get(), Bln.get(valid), typed, details);
    }

    /**
     * Returns a cached XML reader for the current thread.
     * @return reader
     * @throws SAXException SAX exception
     * @throws ParserConfigurationException parser configuration exception
     */
    private XMLReader reader() throws SAXException, ParserConfigurationException {
      XMLReader reader = readers.get();
      if(reader == null) {
        reader = XmlParser.reader(new MainOptions());
        readers.set(reader);
      }
      return reader;
    }

    /**
     * Creates a validating handler that records invalidities. A new handler is created for each
     * validation: some processors stop reporting invalidities if a handler is reused.
     * @param errors error list
     * @return handler
     */
    private ValidatorHandler handler(final ArrayList<ErrorInfo> errors) {
      final ValidatorHandler handler = schema.newValidatorHandler();
      handler.setErrorHandler(new ErrorHandler() {
        @Override
        public void warning(final SAXParseException ex) { }
        @Override
        public void error(final SAXParseException ex) throws SAXException {
          errors.add(new ErrorInfo(ex, Level.ERROR, null));
          // validation can be stopped if no details are requested
          if(!errorDetails) throw ex;
          // invalid input yields no typed node: stop building the result
          handler.setContentHandler(null);
        }
        @Override
        public void fatalError(final SAXParseException ex) throws SAXException {
          errors.add(new ErrorInfo(ex, Level.FATAL, null));
          throw ex;
        }
      });
      // xsi:schemaLocation attributes are only resolved on demand
      if(!xsiLocation) ValidateXsd.restrict(handler);
      return handler;
    }

    /**
     * Returns details on the invalidities that were found.
     * @param errors errors
     * @param qc query context
     * @return maps with error details
     * @throws QueryException query exception
     */
    private static Value details(final ArrayList<ErrorInfo> errors, final QueryContext qc)
        throws QueryException {

      final ValueBuilder vb = new ValueBuilder(qc, errors.size());
      for(final ErrorInfo error : errors) {
        final MapBuilder map = new MapBuilder().put("message", error.message);
        if(error.url != null) map.put("error-uri", error.url);
        if(error.line > 0) map.put("line-number", Itr.get(error.line));
        if(error.column > 0) map.put("column-number", Itr.get(error.column));
        vb.add(map.map());
      }
      return vb.value(Types.MAP);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return copyType(new Validate(info, schema, typedNode, errorDetails, xsiLocation,
          copyAll(cc, vm, args())));
    }

    @Override
    public boolean equals(final Object obj) {
      return this == obj || obj instanceof final Validate vld && schema == vld.schema &&
          typedNode == vld.typedNode && errorDetails == vld.errorDetails &&
          xsiLocation == vld.xsiLocation && super.equals(obj);
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("xsd-validate").params(exprs);
    }
  }

  /**
   * Parser that feeds the validated input of a validating pipeline into a database builder.
   */
  private static final class ValidatingParser extends Parser {
    /** XML reader. */
    private final XMLReader reader;
    /** Validating handler. */
    private final ValidatorHandler handler;
    /** Create a document node. */
    private final boolean document;

    /**
     * Constructor.
     * @param source serialized node
     * @param reader XML reader
     * @param handler validating handler
     * @param document create a document node
     * @param options main options
     */
    private ValidatingParser(final IO source, final XMLReader reader,
        final ValidatorHandler handler, final boolean document, final MainOptions options) {
      super(source, options);
      this.reader = reader;
      this.handler = handler;
      this.document = document;
    }

    @Override
    public void parse(final Builder builder) throws IOException {
      final SAXHandler saxh = new SAXHandler(builder);
      handler.setContentHandler(saxh);
      try {
        // comments and CDATA sections are supplied by the reader, all other events by the validator
        reader.setProperty(LEXICAL_HANDLER, saxh);
        // an element target yields a parentless element: no document node is created
        if(document) builder.openDoc(Token.token(source.name()));
        reader.parse(source.inputSource());
        if(document) builder.closeDoc();
      } catch(final SAXException ex) {
        throw new IOException(ex);
      }
    }
  }

  /** Validation modes. */
  public enum ValidationMode {
    /** Strict.  */ STRICT,
    /** Lax.     */ LAX,
    /** By type. */ BY_TYPE;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
