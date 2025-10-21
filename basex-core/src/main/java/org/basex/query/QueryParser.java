package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.QueryText.FALSE;
import static org.basex.query.QueryText.TRUE;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpN.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
import org.basex.query.up.expr.*;
import org.basex.query.up.expr.Insert.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.format.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Parser for XQuery expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** Pattern for detecting library modules. */
  private static final Pattern LIBMOD_PATTERN = Pattern.compile(
      "^(xquery( version ['\"].*?['\"])?( encoding ['\"].*?['\"])? ?; ?)?module .*");
  /** QName check: skip namespace check. */
  private static final byte[] SKIPCHECK = {};
  /** Reserved keywords. */
  private static final TokenSet KEYWORDS = new TokenSet(
      ATTRIBUTE, COMMENT, DOCUMENT_NODE, ELEMENT, NAMESPACE_NODE, NODE, SCHEMA_ATTRIBUTE,
      SCHEMA_ELEMENT, PROCESSING_INSTRUCTION, TEXT, ARRAY, ENUM, FN, FUNCTION, GET, IF,
      ITEM, MAP, RECORD, SWITCH, TYPE, TYPESWITCH);

  /** URIs of modules loaded by the current file. */
  public final TokenSet moduleURIs = new TokenSet();
  /** Query context. */
  public final QueryContext qc;
  /** Static context. */
  public final StaticContext sc;

  /** List of modules to be parsed. */
  private final ArrayList<ModInfo> modules = new ArrayList<>();
  /** Namespaces. */
  private final TokenObjectMap<byte[]> namespaces = new TokenObjectMap<>();

  /** Parsed variables. */
  private final ArrayList<StaticVar> vars = new ArrayList<>();
  /** Parsed functions. */
  private final ArrayList<StaticFunc> funcs = new ArrayList<>();
  /** Function references. */
  private final ArrayList<FuncRef> funcRefs = new ArrayList<>();
  /** Types. */
  private final QNmMap<SeqType> declaredTypes = new QNmMap<>();
  /** Public types. */
  private final QNmMap<SeqType> publicTypes = new QNmMap<>();
  /** Named record types. */
  private final QNmMap<RecordType> namedRecordTypes = new QNmMap<>();
  /** Named record type references. */
  private final QNmMap<RecordType> recordTypeRefs = new QNmMap<>();
  /** Options. */
  private final QNmMap<String> options = new QNmMap<>();

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<>();
  /** Output declarations. */
  private final HashMap<String, Object> sparams = new HashMap<>();
  /** QName cache. */
  private final QNmCache qnames = new QNmCache();
  /** Local variable. */
  private final LocalVars localVars = new LocalVars(this);

  /** Temporary token cache. */
  private final TokenBuilder token = new TokenBuilder();
  /** Current XQDoc string. */
  private final TokenBuilder docBuilder = new TokenBuilder();

  /** XQDoc string of module. */
  private String moduleDoc = "";
  /** Alternative error. */
  private QueryError alter;
  /** Alternative position. */
  private int alterPos;

  /**
   * Constructor.
   * @param query query string
   * @param uri base URI (can be {@code null}; only passed on if not bound to static context yet)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   */
  QueryParser(final String query, final String uri, final QueryContext qctx,
      final StaticContext sctx) {

    super(query);
    qc = qctx;
    sc = sctx != null ? sctx : new StaticContext(qctx);
    if(uri != null) sc.baseURI(uri);
  }

  /**
   * Parses a main module.
   * Parses the "MainModule" rule.
   * Parses the "Setter" rule.
   * Parses the "QueryBody (= Expr)" rule.
   * @return module
   * @throws QueryException query exception
   */
  final MainModule parseMain() throws QueryException {
    init();
    try {
      versionDecl();

      final int p = pos;
      if(wsConsumeWs(MODULE, null, NAMESPACE)) throw error(MAINMOD);
      pos = p;

      prolog1();
      importModules();
      prolog2();

      localVars.pushContext(false);
      final Expr expr = expr();
      if(expr == null) throw alterError(EXPREMPTY);

      final VarScope vs = localVars.popContext();
      final MainModule mm = new MainModule(expr, vs, sc);
      mm.set(funcs, vars, publicTypes, moduleURIs, namespaces, options, moduleDoc);
      finish(mm);
      check(mm);
      return mm;
    } catch(final QueryException expr) {
      mark();
      expr.pos(this);
      throw expr;
    }
  }

  /**
   * Parses a library module.
   * Parses the "ModuleDecl" rule.
   * @param root indicates if this library is or is not imported by another module
   * @return module
   * @throws QueryException query exception
   */
  final LibraryModule parseLibrary(final boolean root) throws QueryException {
    init();
    try {
      versionDecl();

      wsCheck(MODULE);
      wsCheck(NAMESPACE);
      skipWs();
      final byte[] prefix = ncName(NONAME_X);
      wsCheck("=");
      final byte[] uri = stringLiteral();
      if(uri.length == 0) throw error(NSMODURI);

      sc.module = new QNm(prefix, uri);
      sc.ns.add(prefix, uri, info());
      namespaces.put(prefix, uri);
      wsCheck(";");

      // get absolute path
      final IO baseO = sc.baseIO();
      final byte[] pth = token(baseO == null ? "" : baseO.path());
      qc.modParsed.put(pth, uri);
      qc.modStack.push(pth);

      prolog1();
      importModules();
      prolog2();
      finish(null);
      if(root) check(null);

      qc.modStack.pop();
      final LibraryModule lm = new LibraryModule(sc);
      lm.set(funcs, vars, publicTypes, moduleURIs, namespaces, options, moduleDoc);
      return lm;
    } catch(final QueryException expr) {
      mark();
      expr.pos(this);
      throw expr;
    }
  }

  /**
   * Parses a sequence type.
   * @return sequence type
   * @throws QueryException query exception
   */
  final SeqType parseSeqType() throws QueryException {
    try {
      return sequenceType();
    } catch(final QueryException expr) {
      Util.debug(expr);
      throw error(CASTTYPE_X, null, expr.getLocalizedMessage());
    }
  }

  /**
   * Initializes the parsing process.
   * @throws QueryException query exception
   */
  private void init() throws QueryException {
    final IO baseIO = sc.baseIO();
    path = baseIO == null ? null : baseIO.path();
    if(!more()) throw error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    for(int i = 0; i < length; i++) {
      final int cp = input[i];
      if(!XMLToken.valid(cp)) {
        pos = i;
        throw error(MODLEINV_X, currentAsString());
      }
    }
  }

  /**
   * Finishes the parsing step.
   * @param mm main module; {@code null} for library modules
   * @throws QueryException query exception
   */
  private void finish(final MainModule mm) throws QueryException {
    if(more()) {
      if(alter != null) throw alterError(null);
      final String rest = remaining();
      pos++;
      if(mm == null) throw error(MODEXPR, rest);
      throw error(QUERYEND_X, rest);
    }

    // completes the parsing step
    qnames.assignURI(this, 0);
    if(sc.elemNS != null) sc.ns.add(EMPTY, sc.elemNS, null);
    RecordType.resolveRefs(recordTypeRefs, namedRecordTypes);

    for(final FuncRef fr : funcRefs) fr.resolve(qc);

    if(qc.contextValue != null) {
      final Expr ctx = qc.contextValue.expr;
      if(!sc.mixUpdates && ctx.has(Flag.UPD)) throw error(UPCTX, ctx);
    }
  }

  /**
   * Checks function calls, variable references and updating semantics.
   * @param main main module; {@code null} for library modules
   * @throws QueryException query exception
   */
  private void check(final MainModule main) throws QueryException {
    // check function calls and variable references
    qc.functions.check(qc);
    qc.vars.check();

    if(qc.updating) {
      // check updating semantics if updating expressions exist
      if(!sc.mixUpdates) {
        qc.functions.checkUp();
        qc.vars.checkUp();
        if(main != null) main.expr.checkUp();
      }
      // check if main expression is updating
      qc.updating = main != null && main.expr.has(Flag.UPD);
    }
  }

  /**
   * Parses the "VersionDecl" rule.
   * @throws QueryException query exception
   */
  private void versionDecl() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(XQUERY)) return;

    final boolean version = wsConsumeWs(VERSION);
    if(version) {
      // parse xquery version
      final String string = string(stringLiteral()).replaceAll("0+(\\d)", "$1");
      if(!QueryContext.isSupported(string)) throw error(XQUERYVER_X, string);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if(wsConsumeWs(ENCODING)) {
      final String encoding = string(stringLiteral());
      if(!Strings.encodingSupported(encoding)) throw error(XQUERYENC2_X, encoding);
    } else if(!version) {
      pos = p;
      return;
    }
    wsCheck(";");
  }

  /**
   * Parses the "Prolog" rule.
   * Parses the "Setter" rule.
   * @throws QueryException query exception
   */
  private void prolog1() throws QueryException {
    while(true) {
      final int p = pos;
      if(wsConsumeWs(DECLARE)) {
        if(wsConsumeWs(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl() && !emptyOrderDecl() &&
             !decimalFormatDecl(true)) throw error(DECLINCOMPLETE);
        } else if(wsConsumeWs(BOUNDARY_SPACE)) {
          boundarySpaceDecl();
        } else if(wsConsumeWs(BASE_URI)) {
          baseURIDecl();
        } else if(wsConsumeWs(CONSTRUCTION)) {
          constructionDecl();
        } else if(wsConsumeWs(ORDERING)) {
          orderingModeDecl();
        } else if(wsConsumeWs(REVALIDATION)) {
          revalidationDecl();
        } else if(wsConsumeWs(COPY_NAMESPACES)) {
          copyNamespacesDecl();
        } else if(wsConsumeWs(DECIMAL_FORMAT)) {
          decimalFormatDecl(false);
        } else if(wsConsumeWs(NAMESPACE)) {
          namespaceDecl();
        } else if(wsConsumeWs(FT_OPTION)) {
          // subsequent assignment required to enable duplicate checks
          final FTOpt fto = new FTOpt();
          while(ftMatchOption(fto));
          qc.ftOpt().assign(fto);
        } else {
          pos = p;
          return;
        }
      } else if(wsConsumeWs(IMPORT)) {
        if(wsConsumeWs(SCHEMA)) {
          schemaImport();
        } else if(wsConsumeWs(MODULE)) {
          moduleImport();
        } else {
          pos = p;
          return;
        }
      } else {
        return;
      }
      docBuilder.reset();
      skipWs();
      check(';');
    }
  }

  /**
   * Parses the "Prolog" rule.
   * @throws QueryException query exception
   */
  private void prolog2() throws QueryException {
    while(true) {
      final int p = pos;
      if(!wsConsumeWs(DECLARE)) break;

      if(wsConsumeWs(CONTEXT)) {
        contextValueDecl();
      } else if(wsConsumeWs(OPTION)) {
        optionDecl();
      } else if(wsConsumeWs(DEFAULT)) {
        throw error(PROLOGORDER);
      } else {
        final AnnList anns = annotations(true);
        if(wsConsumeWs(VARIABLE)) {
          // variables cannot be updating
          if(anns.contains(Annotation.UPDATING)) throw error(UPDATINGVAR);
          varDecl(anns.check(true, true));
        } else if(wsConsumeWs(FUNCTION)) {
          functionDecl(anns.check(false, true));
        } else if(wsConsumeWs(TYPE)) {
          // types cannot be updating
          if(anns.contains(Annotation.UPDATING)) throw error(UPDATINGTYPE);
          typeDecl(anns.check(false, true));
        } else if(wsConsumeWs(RECORD)) {
          // types cannot be updating
          if(anns.contains(Annotation.UPDATING)) throw error(UPDATINGTYPE);
          namedRecordTypeDecl(anns.check(false, true));
        } else if(!anns.isEmpty()) {
          throw error(VARFUNC);
        } else {
          pos = p;
          break;
        }
      }
      docBuilder.reset();
      skipWs();
      check(';');
    }

    // parse serialization parameters
    if(!sparams.isEmpty()) {
      final QueryBiConsumer<String, Object[]> parse = (k, v) ->
        qc.parameters().parse(k, (String) v[0], (InputInfo) v[1]);
      final String key = SerializerOptions.PARAMETER_DOCUMENT.name();
      final Object value = sparams.remove(key);
      if(value != null) parse.accept(key, (Object[]) value);
      for(final Map.Entry<String, Object> entry : sparams.entrySet()) {
        parse.accept(entry.getKey(), (Object[]) entry.getValue());
      }
    }
  }

  /**
   * Parses the "Annotation" rule.
   * @param updating also check for updating keyword
   * @return annotations
   * @throws QueryException query exception
   */
  private AnnList annotations(final boolean updating) throws QueryException {
    AnnList anns = AnnList.EMPTY;
    while(true) {
      final Ann ann;
      if(updating && wsConsumeWs(UPDATING)) {
        ann = new Ann(info(), Annotation.UPDATING, Empty.VALUE);
      } else if(wsConsumeWs("%")) {
        final InputInfo ii = info();
        final QNm name = eQName(XQ_URI, QNAME_X);

        final ItemList items = new ItemList();
        if(wsConsume("(")) {
          do {
            final Expr expr;
            final boolean truee = wsConsume(TRUE);
            if(truee || consume(FALSE)) {
              wsCheck("(");
              wsCheck(")");
              expr = Bln.get(truee);
            } else {
              if(quote(current())) expr = Str.get(stringLiteral());
              else if(!consume('#')) expr = numericLiteral(0, true);
              else {
                skipWs();
                expr = eQName(null, QNAME_X);
              }
              if(!(expr instanceof Item)) {
                if(Function.ERROR.is(expr)) expr.item(qc, ii);
                throw error(ANNVALUE_X, currentAsString());
              }
            }
            items.add((Item) expr);
          } while(wsConsume(","));
          wsCheck(")");
        }

        // check if annotation is a pre-defined one
        final Annotation def = Annotation.get(name);
        if(def == null) {
          // reject unknown annotations with pre-defined namespaces, ignore others
          final byte[] uri = name.uri();
          if(NSGlobal.prefix(uri).length != 0 && !eq(uri, LOCAL_URI, ERROR_URI)) {
            throw error(NSGlobal.reserved(uri) ? ANNWHICH_X_X : BASEX_ANNOTATION1_X_X,
                ii, "%", name.string());
          }
          ann = new Ann(ii, name, items.value());
        } else {
          if(def.single && anns.contains(def)) throw error(BASEX_ANN3_X_X, ii, "%", def.name());
          final int is = items.size();
          final IntList arities = Functions.checkArity(is, def.minMax[0], def.minMax[1]);
          if(arities != null) throw error(BASEX_ANN2_X_X, ii, def, arity(arguments(is), arities));

          final int al = def.params.length;
          for(int i = 0; i < is; i++) {
            final AtomType type = def.params[Math.min(al - 1, i)];
            final Item item = items.get(i);
            if(!item.type.instanceOf(type)) {
              throw error(BASEX_ANN_X_X_X, ii, def, type, item.seqType());
            }
          }
          ann = new Ann(ii, def, items.value());
        }
      } else {
        break;
      }

      anns = anns.attach(ann);
      if(ann.definition == Annotation.UPDATING) qc.updating();
    }
    return anns;
  }

  /**
   * Parses the "NamespaceDecl" rule.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final byte[] prefix = ncName(NONAME_X);
    wsCheck("=");
    final byte[] uri = stringLiteral();
    if(sc.ns.staticURI(prefix) != null) throw error(DUPLNSDECL_X, prefix);
    sc.ns.add(prefix, uri, info());
    namespaces.put(prefix, uri);
  }

  /**
   * Parses the "RevalidationDecl" rule.
   * @throws QueryException query exception
   */
  private void revalidationDecl() throws QueryException {
    if(!decl.add(REVALIDATION)) throw error(DUPLREVAL);
    if(wsConsumeWs(STRICT) || wsConsumeWs(LAX)) throw error(NOREVAL);
    wsCheck(SKIP);
  }

  /**
   * Parses the "BoundarySpaceDecl" rule.
   * @throws QueryException query exception
   */
  private void boundarySpaceDecl() throws QueryException {
    if(!decl.add(BOUNDARY_SPACE)) throw error(DUPLBOUND);
    final boolean spaces = wsConsumeWs(PRESERVE);
    if(!spaces) wsCheck(STRIP);
    sc.spaces = spaces;
  }

  /**
   * Parses the "DefaultNamespaceDecl" rule.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean defaultNamespaceDecl() throws QueryException {
    final boolean elem = wsConsumeWs(ELEMENT);
    if(!elem && !wsConsumeWs(FUNCTION)) return false;
    wsCheck(NAMESPACE);
    final byte[] uri = stringLiteral();
    if(eq(XML_URI, uri)) throw error(BINDXMLURI_X_X, uri, XML);
    if(eq(XMLNS_URI, uri)) throw error(BINDXMLURI_X_X, uri, XMLNS);

    if(elem) {
      if(!decl.add(ELEMENT)) throw error(DUPLNS);
      sc.elemNS = uri.length == 0 ? null : uri;
    } else {
      if(!decl.add(FUNCTION)) throw error(DUPLNS);
      sc.funcNS = uri;
    }
    return true;
  }

  /**
   * Parses the "OptionDecl" rule.
   * @throws QueryException query exception
   */
  private void optionDecl() throws QueryException {
    skipWs();
    final QNm qname = eQName(XQ_URI, QNAME_X);
    final String name = string(qname.local()), value = string(stringLiteral());
    final byte[] uri = qname.uri();

    if(eq(uri, OUTPUT_URI)) {
      // output declaration
      if(sc.module != null) throw error(OUTPUTLIB_X, name);
      if(sparams.put(name, new Object[] { value, info() }) != null) throw error(OUTDUPL_X, name);
    } else if(eq(uri, DB_URI)) {
      // project-specific declaration
      if(sc.module != null) throw error(BASEX_OPTIONSLIB_X, name);
      qc.options.add(name, value, this);
    } else if(eq(uri, BASEX_URI)) {
      // query-specific options
      if(!name.equals(LOCK)) throw error(BASEX_OPTIONSINV_X, name);
      for(final String lock : Locking.queryLocks(token(value))) qc.locks.add(lock);
    }
    // ignore unknown options
    options.put(qname, value);
  }

  /**
   * Parses the "OrderingModeDecl" rule.
   * @throws QueryException query exception
   */
  private void orderingModeDecl() throws QueryException {
    if(!decl.add(ORDERING)) throw error(DUPLORD);
    sc.ordered = wsConsumeWs(ORDERED);
    if(!sc.ordered) wsCheck(UNORDERED);
  }

  /**
   * Parses the "emptyOrderDecl" rule.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean emptyOrderDecl() throws QueryException {
    if(!wsConsumeWs(ORDER)) return false;
    wsCheck(EMPTYY);
    if(!decl.add(EMPTYY)) throw error(DUPLORDEMP);
    sc.orderGreatest = wsConsumeWs(GREATEST);
    if(!sc.orderGreatest) wsCheck(LEAST);
    return true;
  }

  /**
   * Parses the "copyNamespacesDecl" rule.
   * Parses the "PreserveMode" rule.
   * Parses the "InheritMode" rule.
   * @throws QueryException query exception
   */
  private void copyNamespacesDecl() throws QueryException {
    if(!decl.add(COPY_NAMESPACES)) throw error(DUPLCOPYNS);
    sc.preserveNS = wsConsumeWs(PRESERVE);
    if(!sc.preserveNS) wsCheck(NO_PRESERVE);
    wsCheck(",");
    sc.inheritNS = wsConsumeWs(INHERIT);
    if(!sc.inheritNS) wsCheck(NO_INHERIT);
  }

  /**
   * Parses the "DecimalFormatDecl" rule.
   * @param def default flag
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean decimalFormatDecl(final boolean def) throws QueryException {
    if(def && !wsConsumeWs(DECIMAL_FORMAT)) return false;

    // use empty name for default declaration
    final byte[] name = (def ? QNm.EMPTY : eQName(null, QNAME_X)).unique();

    // check if format has already been declared
    if(sc.decFormats.get(name) != null) throw error(DECDUPL);

    // create new format
    final DecFormatOptions dfo = new DecFormatOptions();
    // collect all property declarations
    while(true) {
      skipWs();
      final String prop = string(ncName(null));
      if(prop.isEmpty()) break;
      wsCheck("=");
      if(dfo.get(prop) != null) throw error(DECDUPLPROP_X, prop);
      try {
        dfo.assign(prop, string(stringLiteral()));
      } catch(final BaseXException ex) {
        throw error(FORMPROP_X, ex);
      }
    }
    sc.decFormats.put(name, new DecFormatter(dfo, info()));
    return true;
  }

  /**
   * Parses the "DefaultCollationDecl" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private boolean defaultCollationDecl() throws QueryException {
    if(!wsConsumeWs(COLLATION)) return false;
    if(!decl.add(COLLATION)) throw error(DUPLCOLL);
    sc.collation = Collation.get(stringLiteral(), qc, info(), WHICHDEFCOLL_X);
    return true;
  }

  /**
   * Parses the "BaseURIDecl" rule.
   * @throws QueryException query exception
   */
  private void baseURIDecl() throws QueryException {
    if(!decl.add(BASE_URI)) throw error(DUPLBASE);
    sc.baseURI(string(stringLiteral()));
  }

  /**
   * Parses the "SchemaImport" rule.
   * Parses the "SchemaPrefix" rule.
   * @throws QueryException query exception
   */
  private void schemaImport() throws QueryException {
    byte[] prefix = null;
    if(wsConsumeWs(NAMESPACE)) {
      prefix = ncName(NONAME_X);
      if(eq(prefix, XML, XMLNS)) throw error(BINDXML_X, prefix);
      wsCheck("=");
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NAMESPACE);
    }
    final byte[] uri = stringLiteral();
    if(prefix != null && uri.length == 0) throw error(NSEMPTY);
    if(!Uri.get(uri).isValid()) throw error(INVURI_X, uri);
    addLocations(new TokenList());
    throw error(IMPLSCHEMA);
  }

  /**
   * Parses the "ModuleImport" rule.
   * @throws QueryException query exception
   */
  private void moduleImport() throws QueryException {
    byte[] prefix = EMPTY;
    if(wsConsumeWs(NAMESPACE)) {
      prefix = ncName(NONAME_X);
      wsCheck("=");
    }

    final byte[] uri = trim(stringLiteral());
    if(uri.length == 0) throw error(NSMODURI);
    if(!Uri.get(uri).isValid()) throw error(INVURI_X, uri);
    if(moduleURIs.contains(token(uri))) throw error(DUPLMODULE_X, uri);
    moduleURIs.add(uri);

    // add non-default namespace
    if(prefix != EMPTY) {
      if(sc.ns.staticURI(prefix) != null) throw error(DUPLNSDECL_X, prefix);
      sc.ns.add(prefix, uri, info());
      namespaces.put(prefix, uri);
    }

    // check modules at specified locations
    final ModInfo mi = new ModInfo();
    if(!addLocations(mi.paths)) {
      // check module files that have been pre-declared by a test API
      final byte[] pth = qc.modDeclared.get(uri);
      if(pth != null) mi.paths.add(pth);
    }
    mi.uri = uri;
    mi.info = info();
    modules.add(mi);
  }

  /**
   * Adds locations.
   * @param list list of locations
   * @return if locations were added
   * @throws QueryException query exception
   */
  private boolean addLocations(final TokenList list) throws QueryException {
    final boolean add = wsConsume(AT);
    if(add) {
      do {
        final byte[] uri = stringLiteral();
        if(!Uri.get(uri).isValid() || IO.get(string(uri)) instanceof IOContent)
          throw error(INVURI_X, uri);
        list.add(uri);
      } while(wsConsume(","));
    }
    return add;
  }

  /**
   * Imports all modules parsed in the prolog.
   * @throws QueryException query exception
   */
  private void importModules() throws QueryException {
    for(final ModInfo mi : modules) importModule(mi);
  }

  /**
   * Imports a single module.
   * @param mi module import
   * @throws QueryException query exception
   */
  private void importModule(final ModInfo mi) throws QueryException {
    final byte[] uri = mi.uri;
    if(mi.paths.isEmpty()) {
      // no paths specified: skip statically available modules; try to resolve module URI
      if(Functions.staticURI(uri) || qc.resources.modules().addImport(string(uri), this, mi.info))
        return;
      // module not found
      throw error(WHICHMOD_X, mi.info, uri);
    }
    // parse supplied paths
    for(final byte[] pth : mi.paths) module(string(pth), string(uri), mi.info);
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param pth input path
   * @param uri base URI of module
   * @param info input info
   * @throws QueryException query exception
   */
  public final void module(final String pth, final String uri, final InputInfo info)
      throws QueryException {

    // get absolute path
    final IO io = sc.resolve(pth, uri);
    final byte[] tPath = token(io.path());

    // check if module has already been parsed
    final byte[] tUri = token(uri), pUri = qc.modParsed.get(tPath);
    if(pUri != null) {
      if(!eq(tUri, pUri)) throw error(WRONGMODULE_X_X_X, info, io.name(), uri, pUri);
    }
    else {
      qc.modParsed.put(tPath, tUri);

      // read module
      final String query;
      try {
        query = io.readString();
      } catch(final IOException expr) {
        Util.debug(expr);
        throw error(WHICHMODFILE_X, info, io);
      }

      qc.modStack.push(tPath);
      final QueryParser qp = new QueryParser(query, io.path(), qc, null);
      qp.sc.resolver = sc.resolver;

      // check if import and declaration URI match
      final LibraryModule lib = qp.parseLibrary(false);
      qc.libs.put(tPath, lib);
      final byte[] muri = lib.sc.module.uri();
      if(!uri.equals(string(muri))) throw error(WRONGMODULE_X_X_X, info, io.name(), uri, muri);

      // check if context value declaration types are compatible to each other
      final StaticContext sctx = qp.sc;
      if(sctx.contextType != null) {
        if(sc.contextType == null) {
          sc.contextType = sctx.contextType;
        } else if(!sctx.contextType.eq(sc.contextType)) {
          throw error(VALUETYPES_X_X, sctx.contextType, sc.contextType);
        }
      }
      qc.modStack.pop();
    }

    // import the module's public types
    final LibraryModule lib = qc.libs.get(tPath);
    if(lib != null) {
      for(final QNm qn : lib.types) {
        if(declaredTypes.contains(qn)) throw error(DUPLTYPE_X, qn.string());
        declaredTypes.put(qn, lib.types.get(qn));
      }
    }
  }

  /**
   * Parses the "ContextValueDecl" rule.
   * @throws QueryException query exception
   */
  private void contextValueDecl() throws QueryException {
    final boolean item = wsConsume(ITEM);
    if(!item) wsCheck(VALUEE);
    if(!decl.add(VALUEE)) throw error(DUPLVALUE);

    final SeqType cst = sc.contextType;
    SeqType st = cst != null ? cst : item ? SeqType.ITEM_O : SeqType.ITEM_ZM;
    if(wsConsumeWs(AS)) {
      st = item ? itemType() : sequenceType();
      sc.contextType = st;
      qc.contextType = st;
      if(cst != null && !cst.eq(st)) throw error(VALUETYPES_X_X, cst, st);
    }

    final boolean external = wsConsumeWs(EXTERNAL);
    if(!consume(":=")) {
      if(external) return;
      throw error(WRONGCHAR_X_X, ":=", found());
    }
    if(!external) qc.finalContext = true;

    localVars.pushContext(false);
    final Expr expr = check(single(), NOEXPR);
    final VarScope vs = localVars.popContext();
    qc.contextValue = new ContextScope(expr, st, vs, sc, info(), docBuilder.toString());

    if(sc.module != null) throw error(DECITEM);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void varDecl(final AnnList anns) throws QueryException {
    final Var var = newVar();
    if(sc.module != null && !eq(var.name.uri(), sc.module.uri())) throw error(MODULENS_X, var);

    localVars.pushContext(false);
    final boolean external = wsConsumeWs(EXTERNAL);
    Expr expr = null;
    if(wsConsume(":=")) {
      expr = check(single(), NOVARDECL);
    } else if(!external) {
      throw error(WRONGCHAR_X_X, ":=", found());
    }
    final VarScope vs = localVars.popContext();
    final String doc = docBuilder.toString();
    final StaticVar sv = qc.vars.declare(var, expr, anns, external, vs, doc);
    vars.add(sv);
  }

  /**
   * Parses an optional SeqType declaration.
   * @return type if preceded by {@code as} or {@code null}
   * @throws QueryException query exception
   */
  private SeqType optAsType() throws QueryException {
    return wsConsumeWs(AS) ? sequenceType() : null;
  }

  /**
   * Parses the "ConstructionDecl" rule.
   * @throws QueryException query exception
   */
  private void constructionDecl() throws QueryException {
    if(!decl.add(CONSTRUCTION)) throw error(DUPLCONS);
    sc.strip = wsConsumeWs(STRIP);
    if(!sc.strip) wsCheck(PRESERVE);
  }

  /**
   * Parses the "FunctionDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void functionDecl(final AnnList anns) throws QueryException {
    final InputInfo ii = info();
    final QNm name = eQName(sc.funcNS, FUNCNAME);
    if(reserved(name)) throw error(RESERVED_X, name.local());

    wsCheck("(");
    if(sc.module != null && !eq(name.uri(), sc.module.uri())) throw error(MODULENS_X, name);

    localVars.pushContext(false);
    final Params params = paramList(true);
    final Expr expr = wsConsumeWs(EXTERNAL) ? null : enclosedExpr();
    final String doc = docBuilder.toString();
    final VarScope vs = localVars.popContext();
    final byte[] uri = name.uri();
    if(NSGlobal.reserved(uri) || Functions.builtIn(name) != null)
      throw FNRESERVED_X.get(ii, name.string());
    final StaticFunc func = qc.functions.declare(name, params, expr, anns, doc, vs, ii);
    funcs.add(func);
  }

  /**
   * Checks if the specified name equals a reserved keyword.
   * @param name name
   * @return result of check
   */
  private static boolean reserved(final QNm name) {
    return !name.hasPrefix() && KEYWORDS.contains(name.string());
  }

  /**
   * Parses the "ItemTypeDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void typeDecl(final AnnList anns) throws QueryException {
    final QNm qn = eQName(sc.elemNS, TYPENAME);
    if(declaredTypes.contains(qn)) throw error(DUPLTYPE_X, qn.string());
    if(NSGlobal.reserved(qn.uri())) throw error(TYPERESERVED_X, qn.string());
    wsCheck(AS);
    final SeqType st = itemType();
    if(!anns.contains(Annotation.PRIVATE)) {
      if(sc.module != null && !eq(qn.uri(), sc.module.uri())) throw error(MODULENS_X, qn);
      publicTypes.put(qn, st);
    }
    declaredTypes.put(qn, st);
  }

  /**
   * Parses the "NamedRecordTypeDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void namedRecordTypeDecl(final AnnList anns) throws QueryException {
    final InputInfo ii = info();
    final QNm qn = eQName(sc.elemNS, TYPENAME);
    if(declaredTypes.contains(qn)) throw error(DUPLTYPE_X, qn.string());
    if(NSGlobal.reserved(qn.uri())) throw error(TYPERESERVED_X, qn.string());
    wsCheck("(");
    final TokenObjectMap<RecordField> fields = new TokenObjectMap<>();
    boolean extensible = false;
    if(!wsConsume(")")) {
      boolean exprRequired = false;
      do {
        skipWs();
        if(!fields.isEmpty() && consume("*")) {
          extensible = true;
          break;
        }
        final byte[] name = ncName(NONCNAME_X);
        final boolean optional = wsConsume("?");
        final SeqType seqType = wsConsume(AS) ? sequenceType() : null;
        if(fields.contains(name)) throw error(DUPFIELD_X, name);
        skipWs();
        Expr expr = null;
        if(exprRequired && !optional || current() == ':') {
          consume(":=");
          localVars.pushContext(false);
          expr = check(single(), NOEXPR);
          localVars.popContext();
          exprRequired = true;
        }
        fields.put(name, new RecordField(optional, seqType, expr));
      } while(wsConsume(","));
      wsCheck(")");
    }
    final RecordType rt = new RecordType(extensible, fields, qn, anns);
    declaredTypes.put(qn, rt.seqType());
    namedRecordTypes.put(qn, rt);
    if(!anns.contains(Annotation.PRIVATE)) {
      if(sc.module != null && !eq(qn.uri(), sc.module.uri())) throw error(MODULENS_X, qn);
      publicTypes.put(qn, rt.seqType());
    }
    if(qn.uri().length != 0) declareRecordConstructor(rt, ii);
  }

  /**
   * Declares a record constructor function for the specified record type.
   * @param rt record type
   * @param ii input info
   * @throws QueryException query exception
   */
  private void declareRecordConstructor(final RecordType rt, final InputInfo ii)
      throws QueryException {

    final QNm name = rt.name();
    localVars.pushContext(false);
    final Params params = new Params();
    boolean defaults = false;
    final TokenObjectMap<RecordField> fields = rt.fields();
    for(final byte[] key : fields) {
      final RecordField rf = fields.get(key);
      final boolean optional = rf.isOptional();
      final Expr initExpr = rf.expr();
      if(optional || initExpr != null) {
        defaults = true;
      } else if(defaults) {
        throw error(PARAMOPTIONAL_X, key);
      }
      final SeqType fst = rf.seqType();
      final SeqType pst = optional ? fst.union(Occ.ZERO) : fst;
      final Expr init = initExpr == null && optional ? Empty.VALUE : initExpr;
      params.add(new QNm(key), pst, init, null);
    }
    if(rt.isExtensible()) {
      byte[] key;
      int i = -1;
      do {
        final String paramName = ++i == 0 ? "options" : "options" + i;
        key = Token.token(paramName);
      } while(fields.contains(key));
      params.add(new QNm(key), SeqType.MAP_O, XQMap.empty(), null);
    }
    params.seqType(rt.seqType()).finish(qc, localVars);

    final Var[] pv = params.vars();
    final Expr[] args = new Expr[pv.length];
    for(int i = 0; i < pv.length; ++i) {
      args[i] = new VarRef(null, pv[i]);
    }
    final Expr expr = new CRecord(ii, rt, args);
    final String doc = docBuilder.toString();
    final VarScope vs = localVars.popContext();
    final StaticFunc func = qc.functions.declare(name, params, expr, rt.anns(), doc, vs, ii);
    funcs.add(func);
  }

  /**
   * Parses a ParamList.
   * @param dflt allow default values
   * @return declared variables
   * @throws QueryException query exception
   */
  private Params paramList(final boolean dflt) throws QueryException {
    final Params params = new Params();
    boolean defaults = false;
    do {
      skipWs();
      if(current() != '$' && params.size() == 0) break;
      final InputInfo ii = info();
      final QNm name = varName();
      final SeqType type = optAsType();
      Expr expr = null;
      if(dflt && wsConsume(":=")) {
        defaults = true;
        expr = single();
      } else if(defaults) {
        throw error(PARAMOPTIONAL_X, name);
      }
      params.add(name, type, expr, ii);
    } while(consume(','));

    wsCheck(")");
    return params.seqType(optAsType()).finish(qc, localVars);
  }

  /**
   * Parses the "EnclosedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr enclosedExpr() throws QueryException {
    wsCheck("{");
    final Expr expr = expr();
    wsCheck("}");
    return expr == null ? Empty.VALUE : expr;
  }

  /**
   * Parses the "Expr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr expr() throws QueryException {
    final Expr expr = single();
    if(expr == null) {
      if(more()) return null;
      throw alterError(NOEXPR);
    }

    if(!wsConsume(",")) return expr;
    final ExprList el = new ExprList().add(expr);
    do add(el, single()); while(wsConsume(","));
    return new List(info(), el.finish());
  }

  /**
   * Parses the "ExprSingle" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr single() throws QueryException {
    alter = null;
    Expr expr = flwor();
    if(expr == null) expr = quantified();
    if(expr == null) expr = switchh();
    if(expr == null) expr = typeswitch();
    if(expr == null) expr = iff();
    if(expr == null) expr = tryCatch();
    if(expr == null) expr = insert();
    if(expr == null) expr = delete();
    if(expr == null) expr = rename();
    if(expr == null) expr = replace();
    if(expr == null) expr = updatingFunctionCall();
    if(expr == null) expr = copyModify();
    if(expr == null) expr = or();
    return expr;
  }

  /**
   * Parses the "FLWORExpr" rule.
   * Parses the "WhereClause" rule.
   * Parses the "WhileClause" rule.
   * Parses the "OrderByClause" rule.
   * Parses the "OrderSpecList" rule.
   * Parses the "GroupByClause" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr flwor() throws QueryException {
    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = initialClause(null);
    if(clauses == null) return null;

    final TokenObjectMap<Var> curr = new TokenObjectMap<>();
    for(final Clause fl : clauses)
      for(final Var var : fl.vars()) curr.put(var.name.unique(), var);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(final Clause clause : clauses) {
          for(final Var var : clause.vars()) curr.put(var.name.unique(), var);
        }
      } while(size < clauses.size());

      if(wsConsumeWs(WHERE)) {
        alterPos = pos;
        clauses.add(new Where(check(single(), NOWHERE), info()));
      }

      if(wsConsumeWs(WHILE)) {
        alterPos = pos;
        clauses.add(new While(check(single(), NOWHILE), info()));
      }

      if(wsConsumeWs(GROUP)) {
        wsCheck(BY);
        skipWs();
        alterPos = pos;
        final GroupSpec[] specs = groupSpecs(clauses);

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<>();
        for(final GroupSpec spec : specs) curr.put(spec.var.name.unique(), spec.var);
        VARS:
        for(final Var var : curr.values()) {
          for(final GroupSpec spec : specs) {
            if(spec.var == var) continue VARS;
          }
          ng.add(new VarRef(specs[0].info(), var));
        }

        // add new copies for all non-grouping variables
        final int ns = ng.size();
        final Var[] ngrp = new Var[ns];
        for(int i = ns; --i >= 0;) {
          final VarRef ref = ng.get(i);
          // if one groups variables such as $x as xs:integer, then the resulting
          // sequence isn't compatible with the type and can't be assigned
          final Var nv = localVars.add(new Var(ref.var.name, null, qc, ref.var.info));
          ngrp[i] = nv;
          curr.put(nv.name.unique(), nv);
        }
        clauses.add(new GroupBy(specs, ng.toArray(VarRef[]::new), ngrp, specs[0].info()));
      }

      final boolean stable = wsConsumeWs(STABLE);
      if(stable) wsCheck(ORDER);
      if(stable || wsConsumeWs(ORDER)) {
        wsCheck(BY);
        alterPos = pos;
        OrderKey[] keys = null;
        do {
          final OrderKey key = orderSpec();
          keys = keys == null ? new OrderKey[] { key } : Array.add(keys, key);
        } while(wsConsume(","));

        final VarRef[] vs = new VarRef[curr.size()];
        int i = 0;
        for(final Var var : curr.values()) vs[i++] = new VarRef(keys[0].info(), var);
        clauses.add(new OrderBy(vs, keys, keys[0].info()));
      }

      if(wsConsumeWs(COUNT, NOCOUNT, "$")) {
        final Var var = localVars.add(newVar(SeqType.INTEGER_O));
        curr.put(var.name.unique(), var);
        clauses.add(new Count(var));
      }
    } while(size < clauses.size());

    if(!wsConsumeWs(RETURN)) throw alterError(FLWORRETURN);

    final Expr rtrn = check(single(), NORETURN);
    localVars.closeScope(s);

    return new GFLWOR(clauses.peek().info(), clauses, rtrn);
  }

  /**
   * Parses the "InitialClause" rule.
   * @param clauses list of clauses (can be {@code null})
   * @return clauses
   * @throws QueryException query exception
   */
  private LinkedList<Clause> initialClause(final LinkedList<Clause> clauses) throws QueryException {
    LinkedList<Clause> cls = clauses;
    // WindowClause
    if(wsConsumeWs(FOR, NOWINDOW, SLIDING, TUMBLING)) {
      if(cls == null) cls = new LinkedList<>();
      cls.add(windowClause());
    } else {
      // ForClause / LetClause
      final boolean lt = wsConsumeWs(LET, NOLET, "$", SCORE);
      final boolean fr = !lt && wsConsumeWs(FOR, NOFOR, "$", MEMBER, KEY, VALUEE);
      if(lt || fr) {
        if(cls == null) cls = new LinkedList<>();
        if(lt) letClause(cls);
        else   forClause(cls);
      }
    }
    return cls;
  }

  /**
   * Parses the "ForClause" rule.
   * Parses the "PositionalVar" rule.
   * @param clauses list of clauses
   * @throws QueryException parse exception
   */
  private void forClause(final LinkedList<Clause> clauses) throws QueryException {
    do {
      final Var member = wsConsumeWs(MEMBER) ? newVar() : null;
      final Var key = member == null && wsConsumeWs(KEY) ? newVar() : null;
      final Var value = member == null && wsConsumeWs(VALUEE) ? newVar() : null;
      final Var fr = member == null && key == null && value == null ? newVar() : null;
      final boolean empty = fr != null && wsConsume(ALLOWING);
      if(empty) wsCheck(EMPTYY);
      final Var at = wsConsumeWs(AT) ? newVar(SeqType.INTEGER_O) : null;
      final Var score = wsConsumeWs(SCORE) ? newVar(SeqType.DOUBLE_O) : null;
      wsCheck(IN);
      final InputInfo ii = info();
      final Expr expr = check(single(), NOVARDECL);

      // declare variables after the expression, check for duplicates
      final QNmSet names = new QNmSet();
      for(final Var var  : new Var[] { member, key, value, fr, at, score }) {
        if(var != null && !names.add(var.name)) throw error(DUPLVAR_X, var);
      }

      localVars.add(fr, at, score);
      if(fr != null) {
        clauses.add(new For(fr, at, score, expr, empty));
      } else if(member != null) {
        // for member $m in EXPR  ->  for $m in array:split(EXPR) let $m := array:items($a)
        final Var split = new Var(member.name, null, qc, ii);
        localVars.add(split, member);
        clauses.add(new For(split, at, score, Function._ARRAY_SPLIT.get(ii, expr), false));
        clauses.add(new Let(member, Function._ARRAY_ITEMS.get(ii, new VarRef(ii, split))));
      } else if(value == null) {
        // for key $k in EXPR
        // ->  for $k in map:keys(EXPR)
        localVars.add(key);
        clauses.add(new For(key, at, score, Function._MAP_KEYS.get(ii, expr), false));
      } else if(key == null) {
        // for value in EXPR
        // ->  for $v in map:entries(EXPR) let $v := map:items($v)
        final Var entries = new Var(value.name, null, qc, ii);
        localVars.add(entries, value);
        clauses.add(new For(entries, at, score, Function._MAP_ENTRIES.get(ii, expr), false));
        clauses.add(new Let(value, Function._MAP_ITEMS.get(ii, new VarRef(ii, entries))));
      } else {
        // for key $k value $v in EXPR
        // ->  for $v in map:entries(EXPR) let $k := map:keys($v) let $v := map:items($v)
        final Var entries = localVars.add(new Var(value.name, null, qc, ii));
        localVars.add(entries, key, value);
        clauses.add(new For(entries, at, score, Function._MAP_ENTRIES.get(ii, expr), false));
        clauses.add(new Let(key, Function._MAP_KEYS.get(ii, new VarRef(ii, entries))));
        clauses.add(new Let(value, Function._MAP_ITEMS.get(ii, new VarRef(ii, entries))));
      }
    } while(wsConsumeWs(","));
  }

  /**
   * Parses the "LetClause" rule.
   * Parses the "FTScoreVar" rule.
   * @param clauses list of clauses
   * @throws QueryException parse exception
   */
  private void letClause(final LinkedList<Clause> clauses) throws QueryException {
    do {
      if(wsConsumeWs(SCORE)) {
        letValueBinding(newVar(SeqType.DOUBLE_O), true, clauses);
      } else {
        final int p = pos;
        final InputInfo ii = info();
        if(wsConsumeWs("$") && (current('(') || current('[') || current('{'))) {
          letStructBinding(ii, clauses);
        } else {
          pos = p;
          letValueBinding(newVar(), false, clauses);
        }
      }
    } while(wsConsume(","));
  }

  /**
   * Parses the "LetValueBinding" rule.
   * @param var variable to bind
   * @param score score flag
   * @param clauses list of clauses
   * @throws QueryException query exception
   */
  private void letValueBinding(final Var var, final boolean score, final LinkedList<Clause> clauses)
      throws QueryException {

    wsCheck(":=");
    final Expr expr = check(single(), NOVARDECL);
    clauses.add(new Let(localVars.add(var), expr, score));
  }

  /**
   * Parses the "LetSequenceBinding" rule.
   * Parses the "LetArrayBinding" rule.
   * Parses the "LetMapBinding" rule.
   * @param ii input info
   * @param clauses list of clauses
   * @throws QueryException query exception
   */
  private void letStructBinding(final InputInfo ii, final LinkedList<Clause> clauses)
      throws QueryException {

    final LetStructBinding binding = switch(consume()) {
      case '[' -> new LetStructBinding(']', SeqType.ARRAY_O);
      case '{' -> new LetStructBinding('}', SeqType.MAP_O);
      default -> new LetStructBinding(')', SeqType.ITEM_ZM);
    };

    skipWs();
    final LinkedList<Var> vrs = new LinkedList<>();
    do {
      vrs.add(newVar());
    } while(wsConsumeWs(","));
    check(binding.endCp);
    final SeqType asType = Objects.requireNonNullElse(optAsType(), SeqType.ITEM_ZM);
    final SeqType st = asType.intersect(binding.type);
    if(st == null) throw error(NOSUB_X_X, binding.type, asType);
    final Var struct = new Var(vrs.getLast().name, st, qc, ii);

    wsCheck(":=");
    clauses.add(new Let(localVars.add(struct), check(single(), NOVARDECL)));
    final VarRef seqRef = new VarRef(ii, struct);
    int i = 0;
    for(final Var var : vrs) {
      final Expr expr = switch(binding.endCp) {
        case ']' -> Function._ARRAY_GET.get(var.info, seqRef, Itr.get(++i));
        case '}' -> Function._MAP_GET.get(var.info, seqRef, Str.get(var.name.local()));
        default -> (++i < vrs.size() ? Function.ITEMS_AT : Function.SUBSEQUENCE).
          get(var.info, seqRef, Itr.get(i));
      };
      clauses.add(new Let(localVars.add(var), expr));
    }
  }

  /**
   * Represents the parameters of a structure binding in a let clause.
   * @param endCp end character of binding (')', ']', '}')
   * @param type type of binding structure (array(*), map(*), item()*)
   */
  private record LetStructBinding(char endCp, SeqType type) { }

  /**
   * Parses the "TumblingWindowClause" rule.
   * Parses the "SlidingWindowClause" rule.
   * @return the window clause
   * @throws QueryException parse exception
   */
  private Window windowClause() throws QueryException {
    final boolean sliding = !wsConsume(TUMBLING) && wsConsume(SLIDING);
    wsCheck(WINDOW);
    skipWs();

    final Var var = newVar();
    wsCheck(IN);
    final Expr expr = check(single(), NOVARDECL);

    // WindowStartCondition
    final Condition start = wsConsume(START) ? windowCond(true) :
      new Condition(true, null, null, null, null, Bln.TRUE, info());
    // WindowEndCondition
    Condition end = null;
    final boolean only = wsConsume(ONLY), check = sliding || only;
    if(check || wsConsume(END)) {
      if(check) wsCheck(END);
      end = windowCond(false);
    }
    return new Window(sliding, localVars.add(var), expr, start, only, end);
  }

  /**
   * Parses the "WindowVars" rule.
   * @param start start condition flag
   * @return an array containing the current, positional, previous and next variable name
   * @throws QueryException parse exception
   */
  private Condition windowCond(final boolean start) throws QueryException {
    skipWs();
    final InputInfo ii = info();
    final Var var = current('$')          ? localVars.add(newVar(SeqType.ITEM_O))    : null;
    final Var at  = wsConsumeWs(AT)       ? localVars.add(newVar(SeqType.INTEGER_O)) : null;
    final Var prv = wsConsumeWs(PREVIOUS) ? localVars.add(newVar(SeqType.ITEM_ZO))   : null;
    final Var nxt = wsConsumeWs(NEXT)     ? localVars.add(newVar(SeqType.ITEM_ZO))   : null;
    final Expr expr = wsConsume(WHEN)     ? check(single(), NOEXPR) : Bln.TRUE;
    return new Condition(start, var, at, prv, nxt, expr, ii);
  }

  /**
   * Parses the "OrderSpec" rule.
   * Parses the "OrderModifier" rule.
   * Empty order specs are ignored, {@code order} is then returned unchanged.
   * @return new order key
   * @throws QueryException query exception
   */
  private OrderKey orderSpec() throws QueryException {
    final Expr expr = check(single(), ORDERBY);

    boolean desc = false;
    if(!wsConsumeWs(ASCENDING)) desc = wsConsumeWs(DESCENDING);
    boolean least = !sc.orderGreatest;
    if(wsConsumeWs(EMPTYY)) {
      least = !wsConsumeWs(GREATEST);
      if(least) wsCheck(LEAST);
    }
    final Collation coll = wsConsumeWs(COLLATION) ?
      Collation.get(stringLiteral(), qc, info(), FLWORCOLL_X) : sc.collation;
    return new OrderKey(info(), expr, desc, least, coll);
  }

  /**
   * Parses the "GroupingSpec" rule.
   * @param cl preceding clauses
   * @return new group specification
   * @throws QueryException query exception
   */
  private GroupSpec[] groupSpecs(final LinkedList<Clause> cl) throws QueryException {
    GroupSpec[] specs = null;
    do {
      final Var var = newVar();
      final Expr by;
      final boolean checksType = var.declType != null;
      if(checksType || wsConsume(":=")) {
        if(checksType) wsCheck(":=");
        by = check(single(), NOVARDECL);
      } else {
        final VarRef ref = localVars.resolveLocal(var.name, var.info);
        // the grouping variable has to be declared by the same FLWOR expression
        boolean dec = false;
        if(ref != null) {
          // check preceding clauses
          for(final Clause f : cl) {
            if(f.declares(ref.var)) {
              dec = true;
              break;
            }
          }

          // check other grouping variables
          if(!dec && specs != null) {
            for(final GroupSpec spec : specs) {
              if(spec.var == ref.var) {
                dec = true;
                break;
              }
            }
          }
        }
        if(!dec) throw error(GVARNOTDEFINED_X, var);
        by = ref;
      }

      final Collation coll = wsConsumeWs(COLLATION) ? Collation.get(stringLiteral(),
          qc, info(), FLWORCOLL_X) : sc.collation;
      final GroupSpec spec = new GroupSpec(var.info, localVars.add(var), by, coll);
      if(specs == null) {
        specs = new GroupSpec[] { spec };
      } else {
        for(int i = specs.length; --i >= 0;) {
          if(specs[i].var.name.eq(spec.var.name)) {
            specs[i].occluded = true;
            break;
          }
        }
        specs = Array.add(specs, spec);
      }
    } while(wsConsumeWs(","));
    return specs;
  }

  /**
   * Parses the "QuantifiedExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = wsConsumeWs(SOME, NOSOME, "$");
    final boolean every = !some && wsConsumeWs(EVERY, NOSOME, "$");
    if(!some && !every) return null;

    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = new LinkedList<>();
    do {
      final Var var = newVar();
      wsCheck(IN);
      final Expr expr = check(single(), NOSOME);
      clauses.add(new For(localVars.add(var), expr));
    } while(wsConsumeWs(","));

    wsCheck(SATISFIES);
    final Expr rtrn = Function.BOOLEAN.get(info(), check(single(), NOSOME));
    localVars.closeScope(s);

    final InputInfo ii = clauses.peek().info();
    final GFLWOR flwor = new GFLWOR(ii, clauses, rtrn);
    final CmpG cmp = new CmpG(ii, flwor, Bln.get(some), OpG.EQ);
    return some ? cmp : Function.NOT.get(ii, cmp);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!wsConsumeWs(SWITCH, NOSWITCH, "(", "{", CASE)) return null;

    final InputInfo ii = info();
    check('(');
    final Expr cond = expr();
    wsCheck(")");
    final boolean brace = wsConsume("{");

    // collect all cases
    final ArrayList<SwitchGroup> groups = new ArrayList<>();
    ExprList exprs;
    do {
      exprs = new ExprList().add((Expr) null);
      while(wsConsumeWs(CASE)) add(exprs, check(expr(), NOSWITCH));
      if(exprs.size() == 1) {
        // add default case
        if(groups.isEmpty()) throw error(WRONGCHAR_X_X, CASE, found());
        wsCheck(DEFAULT);
      }
      wsCheck(RETURN);
      exprs.set(0, check(single(), NOSWITCH));
      groups.add(new SwitchGroup(info(), exprs.finish()));
    } while(exprs.size() != 1);

    if(brace) wsCheck("}");
    return new Switch(ii, cond != null ? cond : Bln.TRUE, groups.toArray(SwitchGroup[]::new));
  }

  /**
   * Parses the "TypeswitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!wsConsumeWs(TYPESWITCH, NOTYPESWITCH, "(")) return null;
    final InputInfo ii = info();
    check('(');
    final Expr ts = check(expr(), NOTYPESWITCH);
    wsCheck(")");
    final boolean brace = wsConsume("{");

    TypeswitchGroup[] cases = { };
    final ArrayList<SeqType> types = new ArrayList<>();
    final int s = localVars.openScope();
    boolean cs;
    do {
      cs = wsConsumeWs(CASE);
      if(!cs) {
        wsCheck(DEFAULT);
        skipWs();
      }
      Var var = null;
      if(current('$')) {
        var = localVars.add(newVar(SeqType.ITEM_ZM));
        if(cs) wsCheck(AS);
      }
      if(cs) {
        do {
          types.add(sequenceType());
        } while(wsConsume("|"));
      }
      wsCheck(RETURN);
      final Expr rtrn = check(single(), NOTYPESWITCH);
      final SeqType[] st = types.toArray(SeqType[]::new);
      cases = Array.add(cases, new TypeswitchGroup(info(), var, st, rtrn));
      localVars.closeScope(s);
      types.clear();
    } while(cs);
    if(brace) wsCheck("}");

    if(cases.length == 1) throw error(NOTYPESWITCH);
    return new Typeswitch(ii, ts, cases);
  }

  /**
   * Parses the "IfExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr iff() throws QueryException {
    if(!wsConsumeWs(IF, IFPAR, "(")) return null;

    final LinkedList<InputInfo> infos = new LinkedList<>();
    infos.add(info());
    final ExprList list = new ExprList(3).add(ifCond());
    if(wsConsumeWs(THEN)) {
      list.add(check(single(), NOIF));
      if(wsConsumeWs(ELSE)) list.add(check(single(), NOIF));
    } else {
      list.add(enclosedExpr());
      while(wsConsume(ELSE)) {
        if(!wsConsume(IF)) {
          list.add(enclosedExpr());
          break;
        }
        infos.add(info());
        list.add(ifCond()).add(enclosedExpr());
      }
    }
    Expr expr = (list.size() & 1) == 0 ? Empty.VALUE : list.pop();
    while(!list.isEmpty()) {
      final Expr thn = list.pop(), cond = list.pop();
      expr = new If(infos.removeLast(), cond, thn, expr);
    }
    return expr;
  }

  /**
   * Parses the if condition.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ifCond() throws QueryException {
    wsCheck("(");
    final Expr expr = check(expr(), NOIF);
    wsCheck(")");
    return expr;
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr expr = and();
    if(!wsConsumeWs(OR)) return expr;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(expr);
    do add(el, and()); while(wsConsumeWs(OR));
    return new Or(ii, el.finish());
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr expr = comparison();
    if(!wsConsumeWs(AND)) return expr;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(expr);
    do add(el, comparison()); while(wsConsumeWs(AND));
    return new And(ii, el.finish());
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr expr = ftContains();
    if(expr != null) {
      for(final OpV c : OpV.values()) {
        if(wsConsumeWs(c.name)) return new CmpV(info(), expr, check(ftContains(), CMPEXPR), c);
      }
      for(final OpN c : OpN.values()) {
        for(final String name : c.names) {
          if(wsConsumeWs(name)) return new CmpN(info(), expr, check(ftContains(), CMPEXPR), c);
        }
      }
      for(final OpG c : OpG.values()) {
        if(wsConsume(c.name)) {
          if(c == OpG.LT && current('?')) throw error(CMPEXPR); // longest token rule asks for "<?"
          skipWs();
          return new CmpG(info(), expr, check(ftContains(), CMPEXPR), c);
        }
      }
    }
    return expr;
  }

  /**
   * Parses the "FTContainsExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ftContains() throws QueryException {
    final Expr expr = otherwise();
    final int p = pos;
    if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      pos = p;
      return expr;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      throw error(FTIGNORE);
    }
    return new FTContains(expr, select, info());
  }

  /**
   * Parses the "OtherwiseExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr otherwise() throws QueryException {
    final Expr expr = stringConcat();
    if(expr == null || !wsConsumeWs(OTHERWISE)) return expr;
    final ExprList el = new ExprList().add(expr);
    do add(el, stringConcat()); while(wsConsume(OTHERWISE));
    return new Otherwise(info(), el.finish());
  }

  /**
   * Parses the "StringConcatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr stringConcat() throws QueryException {
    final Expr expr = range();
    if(expr == null || !consume("||")) return expr;

    final ExprList el = new ExprList().add(expr);
    do add(el, range()); while(wsConsume("||"));
    return new Concat(info(), el.finish());
  }

  /**
   * Parses the "RangeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr expr = additive();
    if(!wsConsumeWs(TO)) return expr;
    return new Range(info(), expr, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr additive() throws QueryException {
    Expr expr = multiplicative();
    while(expr != null) {
      final Calc c = consume('+') ? Calc.ADD : next() != '>' && consume('-') ? Calc.SUBTRACT : null;
      if(c == null) break;
      expr = new Arith(info(), expr, check(multiplicative(), CALCEXPR), c);
    }
    return expr;
  }

  /**
   * Parses the "MultiplicativeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr multiplicative() throws QueryException {
    Expr expr = union();
    while(expr != null) {
      final Calc c = consume('*') || consume('×') ? Calc.MULTIPLY :
        consume('÷') || wsConsumeWs(DIV) ? Calc.DIVIDE :
        wsConsumeWs(IDIV) ? Calc.DIVIDEINT :
        wsConsumeWs(MOD) ? Calc.MODULO : null;
      if(c == null) break;
      expr = new Arith(info(), expr, check(union(), CALCEXPR), c);
    }
    return expr;
  }

  /**
   * Parses the "UnionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr union() throws QueryException {
    final Expr expr = intersect();
    if(expr == null || !isUnion()) return expr;
    final ExprList el = new ExprList().add(expr);
    do add(el, intersect()); while(isUnion());
    return new Union(info(), el.finish());
  }

  /**
   * Checks if a union operator is found.
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean isUnion() throws QueryException {
    if(wsConsumeWs(UNION)) return true;
    final int p = pos;
    if(consume("|") && !consume("|")) return true;
    pos = p;
    return false;
  }

  /**
   * Parses the "IntersectExceptExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr intersect() throws QueryException {
    Expr expr = instanceOf();
    boolean lastIs = false;
    ExprList el = null;
    while(true) {
      final boolean is = wsConsumeWs(INTERSECT);
      if(!is && !wsConsumeWs(EXCEPT)) break;
      if(is != lastIs && el != null) {
        expr = intersectExcept(lastIs, el);
        el = null;
      }
      lastIs = is;
      if(el == null) el = new ExprList().add(expr);
      add(el, instanceOf());
    }
    return el != null ? intersectExcept(lastIs, el) : expr;
  }

  /**
   * Parses the "IntersectExceptExpr" rule.
   * @param intersect intersect flag
   * @param el expression list
   * @return expression
   */
  private Expr intersectExcept(final boolean intersect, final ExprList el) {
    return intersect ? new Intersect(info(), el.finish()) : new Except(info(), el.finish());
  }

  /**
   * Parses the "InstanceofExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr instanceOf() throws QueryException {
    final Expr expr = treat();
    if(!wsConsumeWs(INSTANCE)) return expr;
    wsCheck(OF);
    return new Instance(info(), expr, sequenceType());
  }

  /**
   * Parses the "TreatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr treat() throws QueryException {
    final Expr expr = coerce();
    if(!wsConsumeWs(TREAT)) return expr;
    wsCheck(AS);
    return new Treat(info(), expr, sequenceType());
  }

  /**
   * Parses the "CoerceExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr coerce() throws QueryException {
    final Expr expr = castable();
    if(!wsConsumeWs(COERCE)) return expr;
    wsCheck(TO);
    return new TypeCheck(info(), expr, sequenceType());
  }

  /**
   * Parses the "CastableExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr castable() throws QueryException {
    final Expr expr = cast();
    if(!wsConsumeWs(CASTABLE)) return expr;
    wsCheck(AS);
    return new Castable(info(), expr, castTarget());
  }

  /**
   * Parses the "CastExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr expr = transformWith();
    if(!wsConsumeWs(CAST)) return expr;
    wsCheck(AS);
    return new Cast(info(), expr, castTarget());
  }

  /**
   * Parses the "TransformWithExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr transformWith() throws QueryException {
    Expr expr = pipeline();
    while(expr != null) {
      if(wsConsume(TRANSFORM)) {
        wsCheck(WITH);
      } else if(!wsConsume(UPDATE)) {
        break;
      }
      qc.updating();
      expr = new TransformWith(info(), expr, enclosedExpr());
    }
    return expr;
  }

  /**
   * Parses the "PipelineExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr pipeline() throws QueryException {
    final Expr expr = arrow();
    if(expr != null) {
      if(wsConsumeWs("->")) {
        final ExprList el = new ExprList(expr);
        do add(el, arrow()); while(wsConsumeWs("->"));
        return new Pipeline(info(), el.finish());
      }
    }
    return expr;
  }

  /**
   * Parses the "ArrowExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr arrow() throws QueryException {
    Expr expr = unary();
    if(expr != null) {
      while(true) {
        final boolean mapping = wsConsume("=!>");
        if(!mapping && !consume("=>")) break;

        QNm name = null;
        Expr ex;
        skipWs();
        if(current('$')) {
          ex = varRef();
        } else if(current('(')) {
          ex = parenthesized();
        } else {
          ex = mapConstructor();
          if(ex == null) ex = arrayConstructor();
          if(ex == null) ex = functionItem();
          if(ex == null) {
            name = eQName(null, ARROWSPEC_X);
            if(reserved(name)) throw error(RESERVED_X, name.local());
          }
        }
        final InputInfo ii = info();
        final Expr arg;
        For fr = null;
        int s = 0;
        if(mapping) {
          s = localVars.openScope();
          fr = new For(localVars.add(new Var(new QNm("item"), null, qc, ii)), expr);
          arg = new VarRef(ii, fr.var);
        } else {
          arg = expr;
        }
        final FuncBuilder fb = argumentList(ex == null, arg);
        expr = ex != null ? Functions.dynamic(ex, fb) :
          Functions.get(name, fb, qc, moduleURIs.contains(name.uri()));
        if(mapping) {
          expr = new GFLWOR(ii, fr, expr);
          localVars.closeScope(s);
        }
      }
    }
    return expr;
  }

  /**
   * Parses the "UnaryExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr unary() throws QueryException {
    boolean minus = false, found = false;
    while(true) {
      skipWs();
      if(next() != '>' && consume('-')) {
        minus ^= true;
      } else if(consume('+')) {
      } else {
        final Expr expr = value();
        return found ? new Unary(info(), check(expr, EVALUNARY), minus) : expr;
      }
      found = true;
    }
  }

  /**
   * Parses the "ValueExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr value() throws QueryException {
    validate();
    final Expr expr = extension();
    return expr == null ? itemMap() : expr;
  }

  /**
   * Parses the "ValidateExpr" rule.
   * @throws QueryException query exception
   */
  private void validate() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(VALIDATE)) return;

    if(consume(TYPE)) {
      final InputInfo ii = info();
      qnames.add(eQName(SKIPCHECK, QNAME_X), ii);
    }
    consume(STRICT);
    consume(LAX);
    skipWs();
    if(current('{')) {
      enclosedExpr();
      throw error(IMPLVAL);
    }
    pos = p;
  }

  /**
   * Parses the "ExtensionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr extension() throws QueryException {
    final Pragma[] pragmas = pragma();
    if(pragmas == null) return null;
    wsCheck("{");
    Expr expr = check(expr(), NOPRAGMA);
    wsCheck("}");
    for(int p = pragmas.length - 1; p >= 0; p--) {
      expr = new Extension(info(), pragmas[p], expr);
    }
    return expr;
  }

  /**
   * Parses the "Pragma" rule.
   * @return array of pragmas or {@code null}
   * @throws QueryException query exception
   */
  private Pragma[] pragma() throws QueryException {
    final int p = pos;
    if(!wsConsume("(#") || !consumeWS()) {
      pos = p;
      return null;
    }

    final ArrayList<Pragma> el = new ArrayList<>();
    do {
      final QNm name = eQName(null, QNAME_X);
      int cp = current();
      if(cp != '#' && !ws(cp)) throw error(PRAGMAINV);
      token.reset();
      while(cp != '#' || next() != ')') {
        if(cp == 0) throw error(PRAGMAINV);
        token.add(consume());
        cp = current();
      }

      final byte[] value = token.trim().toArray();
      if(eq(name.prefix(), DB_PREFIX)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final MainOptions mopts = qc.context.options;
        final Option<?> option = mopts.option(key);
        if(option == null) throw error(BASEX_OPTIONSINV_X, mopts.similar(key));
        el.add(new DBPragma(name, option, value));
      } else if(eq(name.prefix(), BASEX_PREFIX)) {
        // project-specific declaration
        el.add(new BaseXPragma(name, value));
      }
      pos += 2;
    } while(wsConsumeWs("(#"));
    return el.toArray(Pragma[]::new);
  }

  /**
   * Parses the "ItemMapExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr itemMap() throws QueryException {
    final Expr expr = path();
    if(expr != null) {
      final int next = next();
      if(next != '=' && next != '!' && wsConsumeWs("!")) {
        final ExprList el = new ExprList().add(expr);
        do add(el, path()); while(next() != '=' && wsConsumeWs("!"));
        return new CachedMap(info(), el.finish());
      }
    }
    return expr;
  }

  /**
   * Parses the "PathExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr path() throws QueryException {
    checkInit();

    final ExprList el;
    Expr root = null;
    if(consume('/')) {
      final InputInfo ii = info();
      root = Function._UTIL_ROOT.get(ii, new ContextValue(ii));
      el = new ExprList();
      final Expr expr;
      if(consume('/')) {
        // two slashes: absolute descendant path
        checkAxis(Axis.DESCENDANT);
        add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, NodeTest.NODE));
        mark();
        expr = step(true);
      } else {
        // one slash: absolute child path
        checkAxis(Axis.CHILD);
        mark();
        expr = step(false);
        // no more steps: return root expression
        if(expr == null) return root;
      }
      add(el, expr);
    } else {
      // relative path (no preceding slash)
      mark();
      final Expr expr = step(false);
      if(expr == null) return null;
      // return expression if no slash follows
      if(current() != '/' && !(expr instanceof Step)) return expr;
      el = new ExprList();
      if(expr instanceof Step) add(el, expr);
      else root = expr;
    }
    relativePath(el);
    return Path.get(info(), root, el.finish());
  }

  /**
   * Parses the "RelativePathExpr" rule.
   * @param el expression list
   * @throws QueryException query exception
   */
  private void relativePath(final ExprList el) throws QueryException {
    while(true) {
      if(consume('/')) {
        if(consume('/')) {
          add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, NodeTest.NODE));
          checkAxis(Axis.DESCENDANT);
        } else {
          checkAxis(Axis.CHILD);
        }
      } else {
        return;
      }
      mark();
      add(el, step(true));
    }
  }

  // methods for query suggestions

  /**
   * Performs an optional check init.
   */
  void checkInit() { }

  /**
   * Performs an optional axis check.
   * @param axis axis
   */
  @SuppressWarnings("unused")
  void checkAxis(final Axis axis) { }

  /**
   * Performs an optional test check.
   * @param ei expression info
   * @param element element flag
   */
  @SuppressWarnings("unused")
  void checkTest(final ExprInfo ei, final boolean element) { }

  /**
   * Checks a predicate.
   * @param open open flag
   */
  @SuppressWarnings("unused")
  void checkPred(final boolean open) { }

  /**
   * Parses the "StepExpr" rule.
   * @param error show error if nothing is found
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr step(final boolean error) throws QueryException {
    final Expr expr = postfix();
    return expr != null ? expr : axisStep(error);
  }

  /**
   * Parses the "AxisStep" rule.
   * @param error show error if nothing is found
   * @return step or {@code null}
   * @throws QueryException query exception
   */
  private Expr axisStep(final boolean error) throws QueryException {
    Axis axis = null;
    ArrayList<ExprInfo> list = new ArrayList<>();
    if(wsConsume("..")) {
      axis = Axis.PARENT;
      list.add(NodeTest.NODE);
      checkTest(list.get(0), true);
    } else if(consume('@')) {
      axis = Axis.ATTRIBUTE;
      list = nodeTest(axis);
      if(list == null) {
        --pos;
        throw error(NOATTNAME);
      }
    } else {
      for(final Axis ax : Axis.values()) {
        final int p = pos;
        if(wsConsume(ax.name)) {
          if(wsConsumeWs("::")) {
            alterPos = pos;
            axis = ax;
            list = nodeTest(axis);
            if(list == null) throw error(AXISMISS_X, axis);
            break;
          }
          pos = p;
        }
      }

      if(axis == null) {
        axis = Axis.CHILD;
        final ExprInfo ei = simpleNodeTest(NodeType.ELEMENT, true);
        if(ei == NodeTest.NAMESPACE_NODE) throw error(NSAXIS);
        if(ei instanceof final Test tst) {
          if(tst.type == NodeType.ATTRIBUTE) axis = Axis.ATTRIBUTE;
          checkTest(tst, axis != Axis.ATTRIBUTE);
        }
        if(ei != null) list.add(ei);
      }
    }
    if(list.isEmpty()) {
      if(error) throw error(STEPMISS_X, found());
      return null;
    }

    final ExprList preds = new ExprList();
    while(wsConsume("[")) {
      checkPred(true);
      add(preds, expr());
      wsCheck("]");
      checkPred(false);
    }

    final int ls = list.size();
    if(ls == 1 && list.get(0) instanceof final Test test) {
      return new CachedStep(info(), axis, test, preds.finish());
    }

    final InputInfo ii = info();
    final ExprList exprs = new ExprList(ls);
    for(int l = 0; l < ls; l++) {
      final ExprInfo ei = list.get(l);
      if(ei instanceof final Test test) {
        exprs.add(new CachedStep(info(), axis, test));
      } else {
        final int s = localVars.openScope();
        final Let lt = new Let(localVars.add(new Var(new QNm("names"), null, qc, ii)), (Expr) ei);
        exprs.add(new GFLWOR(ii, lt, new CachedStep(info(), axis, NodeTest.ELEMENT,
            Function._UTIL_SELECT.get(ii, new VarRef(ii, lt.var)))));
        localVars.closeScope(s);
      }
    }
    final Expr root = exprs.size() == 1 ? exprs.get(0) : new Union(ii, exprs.finish());
    return new CachedFilter(ii, root, preds.finish());
  }

  /**
   * Parses the NodeTest rule.
   * @param axis axis
   * @return tests and gets, or {@code null}
   * @throws QueryException query exception
   */
  private ArrayList<ExprInfo> nodeTest(final Axis axis) throws QueryException {
    final ArrayList<ExprInfo> exprs = new ArrayList<>(1);
    final ArrayList<Test> tests = new ArrayList<>(1);
    final QueryPredicate<Expr> add = e -> {
      final boolean element = axis != Axis.ATTRIBUTE;
      final ExprInfo ei = simpleNodeTest(element ? NodeType.ELEMENT : NodeType.ATTRIBUTE, true);
      if(ei == null) return false;
      if(ei instanceof final Test test) {
        checkTest(test, element);
        if(!tests.contains(test)) tests.add(test);
      } else {
        exprs.add(ei);
      }
      return true;
    };
    if(consume("(")) {
      do {
        skipWs();
        if(!add.test(null)) return null;
      } while(wsConsume("|"));
      if(!consume(')')) throw error(WRONGCHAR_X_X, ')', found());
    } else {
      if(!add.test(null)) return null;
    }
    if(!tests.isEmpty()) exprs.add(0, Test.get(tests));
    return exprs;
  }

  /**
   * Parses the "SimpleNodeTest" rule.
   * Parses the "NameTest" rule.
   * Parses the "KindTest" rule.
   * @param type node type (either {@link NodeType#ELEMENT} or {@link NodeType#ATTRIBUTE})
   * @param all check all tests, or only names
   * @return node test, get() expression (if {@code all} is true), or {@code null}
   * @throws QueryException query exception
   */
  private ExprInfo simpleNodeTest(final NodeType type, final boolean all) throws QueryException {
    int p = pos;
    if(consume('*')) {
      p = pos;
      if(consume(':') && !consume('*')) {
        // name test: *:name
        return new NameTest(new QNm(ncName(QNAME_X)), NamePart.LOCAL, type, sc.elemNS);
      }
      // name test: *
      pos = p;
      return NodeTest.get(type);
    }
    if(consume("Q{")) {
      // name test: Q{uri}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        return new NameTest(new QNm(cpToken(':'), uri), NamePart.URI, type, sc.elemNS);
      }
    }
    pos = p;

    final InputInfo ii = info();
    QNm name = eQName(SKIPCHECK, null);
    if(name != null) {
      p = pos;
      if(all && wsConsumeWs("(")) {
        NodeType nt = NodeType.find(name);
        if(nt != null) {
          // kind test
          final Test test = kindTest(nt);
          return test == null ? NodeTest.get(nt) : test;
        }
        if(name.eq(new QNm(GET))) {
          // dynamic name test
          final Expr expr = single();
          wsCheck(")");
          return expr;
        } else if(name.eq(new QNm(TYPE))) {
          // type test
          final SeqType st = sequenceType();
          wsCheck(")");
          nt = st.zero() ? null : st.type == AtomType.ITEM ? NodeType.NODE :
            st.type instanceof final NodeType n ? n : null;
          return nt == null ? NodeTest.FALSE : NodeTest.get(nt);
        }
      } else {
        pos = p;
        NamePart part = NamePart.FULL;
        if(!name.hasPrefix() && consume(":*")) {
          // name test: prefix:*
          name = new QNm(concat(name.string(), cpToken(':')));
          part = NamePart.URI;
        }
        // name test: prefix:name, name, Q{uri}name
        qnames.add(name, type == NodeType.ELEMENT, ii);
        return new NameTest(name, part, type, sc.elemNS);
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "PostfixExpr" rule.
   * @return postfix expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr postfix() throws QueryException {
    Expr expr = primary();
    if(expr != null) {
      while(true) {
        if(wsConsume("[")) {
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck("]");
          } while(wsConsume("["));
          expr = new CachedFilter(info(), expr, el.finish());
        } else if(consume("?[")) {
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck("]");
          } while(wsConsume("?["));
          expr = new StructFilter(info(), expr, el.finish());
        } else if(consume("=?>")) {
          expr = methodCall(expr);
        } else if(current('(')) {
          expr = Functions.dynamic(expr, argumentList(false, null));
        } else if(current('?')) {
          expr = lookup(expr);
          if(expr == null) break;
        } else {
          break;
        }
      }
    }
    return expr;
  }

  /**
   * Parses the "Lookup" rule.
   * @param expr expression (can be {@code null})
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr lookup(final Expr expr) throws QueryException {
    final int p = pos;
    if(consume('?') && !wsConsume(",") && !consume(")")) {
      final InputInfo info = info();
      final Expr ctx = expr != null ? expr : new ContextValue(info);
      return new Lookup(info, ctx, keySpecifier());
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "MethodCall" rule.
   * @param expr expression
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr methodCall(final Expr expr) throws QueryException {
    skipWs();
    final InputInfo info = info();
    final Str key = Str.get(ncName(NONCNAME_X));
    final int s = localVars.openScope();
    final For fr = new For(localVars.add(new Var(new QNm("method"), null, qc, info)), expr);
    final VarRef arg = new VarRef(info, fr.var);
    final FuncBuilder fb = argumentList(false, arg);
    if(fb.placeholders != 0) throw error(INVPLACEHOLDER_X, key);
    final Lookup func = new Lookup(info, arg, key);
    final Expr call = Functions.dynamic(func, fb);
    final GFLWOR gflwor = new GFLWOR(info, fr, call);
    localVars.closeScope(s);
    return gflwor;
  }

  /**
   * Parses the "PrimaryExpr" rule.
   * Parses the "VarRef" rule.
   * Parses the "ContextItem" rule.
   * Parses the "Literal" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr primary() throws QueryException {
    skipWs();

    final int cp = current();
    if(cp == '<') return dirConstructor(true);
    if(cp == '`') return stringConstructor();
    if(cp == '(') return parenthesized();
    if(cp == '$') return varRef();
    if(cp == '.' && !digit(next())) return contextValue();
    if(wsConsumeWs(ORDERED, null, "{") || wsConsumeWs(UNORDERED, null, "{")) return enclosedExpr();

    Expr expr = functionItem();
    if(expr == null) expr = functionCall();
    if(expr == null) expr = compConstructor();
    if(expr == null) expr = mapConstructor();
    if(expr == null) expr = arrayConstructor();
    if(expr == null) expr = lookup(null);
    if(expr == null) expr = literal();
    return expr;
  }

  /**
   * Parses the "KeySpecifier" rule.
   * @return specifier expression ({@code null} means wildcard)
   * @throws QueryException query exception
   */
  private Expr keySpecifier() throws QueryException {
    if(wsConsume("*")) return Lookup.WILDCARD;

    final int cp = current();
    if(cp == '(') return parenthesized();
    if(cp == '$') return varRef();
    if(cp == '.' && !digit(next())) return contextValue();

    final Expr expr = literal();
    return expr != null ? expr : Str.get(ncName(KEYSPEC_X));
  }

  /**
   * Parses the "ContextValue" rule.
   * @return expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr contextValue() throws QueryException {
    if(next() == '.') return null;
    check('.');
    return new ContextValue(info());
  }

  /**
   * Returns a map constructor.
   * @return map constructor or {@code null}
   * @throws QueryException query exception
   */
  private CMap mapConstructor() throws QueryException {
    if(wsConsumeWs(MAP, MAPCONSTR, "{") || current('{')) {
      check('{');
      final InputInfo info = info();
      final ExprList el = new ExprList();
      if(!wsConsume("}")) {
        final ItemSet set = new HashItemSet(ItemSet.Mode.ATOMIC, info);
        do {
          final Expr first = single();
          add(el, check(first, INVMAPKEY));
          if(wsConsume(":")) {
            if(first instanceof final Item item && !set.add(item)) throw error(MAPDUPLKEY_X, first);
            add(el, check(single(), INVMAPVAL));
          } else {
            add(el, Empty.UNDEFINED);
          }
        } while(wsConsume(","));
        wsCheck("}");
      }
      return new CMap(info, el.finish());
    }
    return null;
  }

  /**
   * Returns an array constructor.
   * @return array constructor or {@code null}
   * @throws QueryException query exception
   */
  private Expr arrayConstructor() throws QueryException {
    if(wsConsumeWs(ARRAY, ARRAYCONSTR, "{")) {
      check('{');
      final Expr expr = expr();
      wsCheck("}");
      return expr == null ? XQArray.empty() : new CItemArray(info(), expr);
    }
    if(consume('[')) {
      final InputInfo info = info();
      final ExprList el = new ExprList();
      if(!wsConsume("]")) {
        do {
          add(el, check(single(), INVMAPVAL));
        } while(wsConsume(","));
        wsCheck("]");
      }
      return el.isEmpty() ? XQArray.empty() : new CArray(info, el.finish());
    }
    return null;
  }

  /**
   * Parses the "FunctionItemExpr" rule.
   * Parses the "NamedFunctionRef" rule.
   * Parses the "LiteralFunctionItem" rule.
   * Parses the "InlineFunction" rule.
   * @return function item or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionItem() throws QueryException {
    skipWs();

    // inline function
    final int p = pos;
    final AnnList anns = annotations(false).check(false, true);
    if(wsConsume(FUNCTION) || wsConsume(FN)) {
      final boolean args = wsConsume("("), focus = !args && current('{');
      if(args || focus) {
        final HashMap<Var, Expr> global = localVars.pushContext(true);
        Params params;
        Expr expr;
        if(args) {
          params = paramList(false);
          expr = enclosedExpr();
        } else {
          // focus function
          final InputInfo ii = info();
          final QNm name = new QNm("arg");
          params = new Params().add(name, SeqType.ITEM_ZM, null, ii).finish(qc, localVars);
          expr = new Pipeline(ii, localVars.resolve(name, ii), enclosedExpr());
        }
        final VarScope vs = localVars.popContext();
        if(anns.contains(Annotation.PRIVATE) || anns.contains(Annotation.PUBLIC))
          throw error(NOVISALLOWED);
        return new Closure(info(), expr, params, anns, vs, global);
      }
    }
    pos = p;

    // annotations not allowed here
    if(!anns.isEmpty()) throw error(NOANN);

    // named function reference
    final QNm name = eQName(null, null);
    if(name != null && wsConsumeWs("#")) {
      final Expr num = numericLiteral(Integer.MAX_VALUE, false);
      if(reserved(name)) {
        if(num != null) throw error(RESERVED_X, name.local());
      } else {
        if(Function.ERROR.is(num)) return num;
        if(num instanceof final Itr itr) {
          final FuncRef fr = new FuncRef(name, (int) itr.itr(), info(),
              moduleURIs.contains(name.uri()));
          funcRefs.add(fr);
          return fr;
        }
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr literal() throws QueryException {
    if(quote(current())) return Str.get(stringLiteral());
    if(!consume('#')) return numericLiteral(0, false);
    skipWs();
    return eQName(null, QNAME_X);
  }

  /**
   * Parses the "NumericLiteral" rule.
   * Parses the "DecimalLiteral" rule.
   * Parses the "IntegerLiteral" rule.
   * @param max maximum value for integers (if 0, parse all numeric types)
   * @param mns parse minus character
   * @return numeric literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr numericLiteral(final long max, final boolean mns) throws QueryException {
    final boolean negate = mns && consume('-');
    if(negate) skipWs();

    final int cp = current();
    if(!digit(cp) && cp != '.') return null;
    token.reset();

    int base = 10;
    if(cp == '0') {
      final int n = next();
      if(max == 0) {
        if(n == 'x' || n == 'X') base = 16;
        else if(n == 'b' || n == 'B') base = 2;
        if(base != 10) {
          consume();
          consume();
          if(current('_')) throw error(NUMBER_X, token.add('_'));
        }
      }
    }

    BigInteger l = BigInteger.ZERO;
    boolean us = false;
    for(int c; (c = current()) != 0;) {
      if(consume('_')) {
        us = true;
      } else {
        final int n = c <= '9' ? c - 0x30 : (c & 0xDF) - 0x37;
        if(n >= base || !(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
          break;
        }
        l = l.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(n));
        token.add(consume());
        us = false;
      }
    }
    if(us) throw error(NUMBER_X, token.add('_'));

    if(base == 10 && max == 0) {
      // fractional digits?
      final boolean dec = consume('.');
      if(dec) {
        token.add('.');
        if(digit(current())) digits();
        else if(token.size() == 1) throw error(NUMBER_X, token);
      }
      // double value
      if(XMLToken.isNCStartChar(current())) {
        if(!consume('e') && !consume('E')) throw error(NUMBER_X, token);
        token.add('e');
        if(current('+') || current('-')) token.add(consume());
        if(digit(current())) digits();
        else throw error(NUMBER_X, token);

        if(XMLToken.isNCStartChar(current())) throw error(NUMBER_X, token);
        final double d = Dbl.parse(token.toArray(), info());
        return Dbl.get(negate ? -d : d);
      }
      // decimal value
      if(dec) {
        final BigDecimal d = new BigDecimal(string(token.toArray()));
        return Dec.get(negate ? d.negate() : d);
      }
    }

    // integer value
    if(token.isEmpty()) throw error(NUMBER_X, token);
    // out of range
    if(l.compareTo(BigInteger.valueOf(max != 0 ? max : Long.MAX_VALUE)) > 0)
      return FnError.get(RANGE_X.get(info(), token), Itr.ZERO);

    return Itr.get(negate ? -l.longValue() : l.longValue());
  }

  /**
   * Parses the "Digits" rule.
   * @throws QueryException query exception
   */
  private void digits() throws QueryException {
    boolean us;
    do {
      us = false;
      token.add(consume());
      while(consume('_')) us = true;
    } while(digit(current()));
    if(us) throw error(NUMBER_X, token.add('_'));
  }

  /**
   * Parses the "StringLiteral" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] stringLiteral() throws QueryException {
    skipWs();
    final int quote = current();
    if(!quote(quote)) throw error(NOQUOTE_X, found());
    consume();
    token.reset();
    while(true) {
      while(!consume(quote)) {
        if(!more()) throw error(NOQUOTE_X, found());
        entity(token);
      }
      if(!consume(quote)) break;
      token.add(quote);
    }
    return token.toArray();
  }

  /**
   * Parses the "BracedURILiteral" rule without the "Q{" prefix.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] bracedURILiteral() throws QueryException {
    final int p = pos;
    token.reset();
    while(!consume('}')) {
      if(!more() || current() == '{') throw error(WRONGCHAR_X_X, "}", found());
      entity(token);
    }
    final byte[] ns = normalize(token.toArray());
    if(eq(ns, XMLNS_URI)) {
      pos = p;
      throw error(ILLEGALEQNAME_X, ns);
    }
    return ns;
  }

  /**
   * Parses the "VarName" rule.
   * @return variable name
   * @throws QueryException query exception
   */
  private QNm varName() throws QueryException {
    check('$');
    skipWs();
    return eQName(null, NOVARNAME);
  }

  /**
   * Parses a variable with an optional type declaration.
   * @return variable
   * @throws QueryException query exception
   */
  private Var newVar() throws QueryException {
    return newVar(null);
  }

  /**
   * Parses a variable.
   * @param type type (if {@code null}, optional type will be parsed)
   * @return variable
   * @throws QueryException query exception
   */
  private Var newVar(final SeqType type) throws QueryException {
    final InputInfo ii = info();
    return new Var(varName(), type != null ? type : optAsType(), qc, ii);
  }

  /**
   * Parses a variable reference.
   * @return variable reference
   * @throws QueryException query exception
   */
  private ParseExpr varRef() throws QueryException {
    final InputInfo ii = info();
    return localVars.resolve(varName(), ii);
  }

  /**
   * Parses the "ParenthesizedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr parenthesized() throws QueryException {
    check('(');
    final Expr expr = expr();
    wsCheck(")");
    return expr == null ? Empty.VALUE : expr;
  }

  /**
   * Parses the "FunctionCall" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int p = pos;
    final QNm name = eQName(null, null);
    if(name != null && !reserved(name)) {
      skipWs();
      if(current('(')) {
        final FuncRef fr = new FuncRef(name, argumentList(true, null),
            moduleURIs.contains(name.uri()));
        funcRefs.add(fr);
        return fr;
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "ArgumentList" rule.
   * @param keywords allow keyword arguments
   * @param expr first argument (can be {@code null})
   * @return function arguments
   * @throws QueryException query exception
   */
  private FuncBuilder argumentList(final boolean keywords, final Expr expr) throws QueryException {
    final FuncBuilder fb  = new FuncBuilder(info());
    if(expr != null) fb.add(expr, null);
    wsCheck("(");
    if(!wsConsumeWs(")")) {
      boolean keywordFound = false;
      do {
        final int p = pos;
        QNm name = null;
        if(keywords) {
          final QNm qnm = eQName(null, null);
          if(wsConsume(":=")) {
            name = qnm;
            keywordFound = true;
          } else {
            pos = p;
          }
        }
        Expr arg = null;
        if(!keywordFound || name != null) {
          arg = single();
          if(arg == null && wsConsume("?")) arg = Empty.UNDEFINED;
        }
        if(arg == null) throw error(FUNCARG_X, found());
        if(fb.add(arg, name)) throw error(KEYWORDTWICE_X, name);
      } while(wsConsumeWs(","));
      if(!consume(")")) throw error(FUNCARG_X, found());
    }
    return fb;
  }

  /**
   * Parses the "StringConstructor" and "StringTemplate" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr stringConstructor() throws QueryException {
    check('`');
    final boolean constr = consume("`[");
    final ExprList el = new ExprList();
    final TokenBuilder tb = new TokenBuilder();
    while(more()) {
      // check for end
      final int p = pos;
      if(constr ? consume(']') && consume('`') && consume('`') : consume('`') && !consume('`')) {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        return el.size() == 1 ? el.get(0) : new Concat(info(), el.finish());
      }
      pos = p;
      // check for variable part
      if(constr ? consume('`') && consume('{') : consume('{') && !consume('{')) {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        final Expr expr = expr();
        if(expr != null) el.add(Function.STRING_JOIN.get(info(), expr, Str.get(' ')));
        skipWs();
        check('}');
        if(constr) check('`');
      } else {
        // fixed part
        pos = p;
        final int cp = consume();
        if(!constr && (cp == '{' || cp == '}' || cp == '`')) check((char) cp);
        tb.add(cp);
      }
    }
    throw error(INCOMPLETE);
  }

  /**
   * Parses the "DirectConstructor" rule.
   * @param root root call
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirConstructor(final boolean root) throws QueryException {
    final int p = pos;
    check('<');
    final Expr expr = consume('!') ? dirComment() : consume('?') ? dirPI() : dirElement(root);
    if(expr != null) return expr;
    pos = p;
    return null;
  }

  /**
   * Parses the "DirElemConstructor" rule.
   * Parses the "DirAttributeList" rules.
   * @param root root call
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr dirElement(final boolean root) throws QueryException {
    final InputInfo ii = info();
    final byte[] qnm = qName(root ? null : ELEMNAME_X);
    consumeWS();
    int cp = current();
    if(qnm.length == 0 || root && cp != '/' && cp != '>' && !XMLToken.isNCStartChar(cp)) {
      return null;
    }

    // cache namespace information
    final int size = sc.ns.size();
    final byte[] nse = sc.elemNS;
    final int npos = qnames.size();

    final QNm name = new QNm(qnm);
    qnames.add(name, ii);

    final Atts ns = new Atts();
    final ExprList cont = new ExprList();

    // parse attributes
    boolean xmlDecl = false; // xml prefix explicitly declared?
    ArrayList<QNm> atts = null;
    while(true) {
      final byte[] atn = qName(null);
      if(atn.length == 0) break;

      final ExprList attv = new ExprList();
      consumeWS();
      if(root) {
        if(!consume('=')) return null;
      } else {
        check('=');
      }
      consumeWS();
      final int delim = consume();
      if(!quote(delim)) throw error(NOQUOTE_X, found());
      final TokenBuilder tb = new TokenBuilder();

      boolean simple = true;
      while(true) {
        while(!consume(delim)) {
          cp = current();
          switch(cp) {
            case '{':
              if(next() == '{') {
                tb.add(consume());
                consume();
              } else {
                final byte[] text = tb.next();
                if(text.length == 0) {
                  add(attv, enclosedExpr());
                  simple = false;
                } else {
                  add(attv, Str.get(text));
                }
              }
              break;
            case '}':
              consume();
              check('}');
              tb.add('}');
              break;
            case '<':
            case 0:
              throw error(NOQUOTE_X, found());
            case '\n':
            case '\t':
              tb.add(' ');
              consume();
              break;
            case '\r':
              if(next() != '\n') tb.add(' ');
              consume();
              break;
            default:
              entity(tb);
              break;
          }
        }
        if(!consume(delim)) break;
        tb.add(delim);
      }

      if(!tb.isEmpty()) add(attv, Str.get(tb.finish()));

      // parse namespace declarations
      final boolean pr = startsWith(atn, XMLNS_COLON);
      if(pr || eq(atn, XMLNS)) {
        if(!simple) throw error(NSCONS);
        final byte[] prefix = pr ? local(atn) : EMPTY;
        final byte[] uri = attv.isEmpty() ? EMPTY : ((Str) attv.get(0)).string();
        if(eq(prefix, XML) && eq(uri, XML_URI)) {
          if(xmlDecl) throw error(DUPLNSDEF_X, XML);
          xmlDecl = true;
        } else {
          if(!Uri.get(uri).isValid()) throw error(INVURI_X, uri);
          if(pr) {
            if(uri.length == 0) throw error(NSEMPTYURI);
            if(eq(prefix, XML, XMLNS)) throw error(BINDXML_X, prefix);
            if(eq(uri, XML_URI)) throw error(BINDXMLURI_X_X, uri, XML);
            if(eq(uri, XMLNS_URI)) throw error(BINDXMLURI_X_X, uri, XMLNS);
            sc.ns.add(prefix, uri);
          } else {
            if(eq(uri, XML_URI)) throw error(XMLNSDEF_X, uri);
            sc.elemNS = uri;
          }
          if(ns.contains(prefix)) throw error(DUPLNSDEF_X, prefix);
          ns.add(prefix, uri);
        }
      } else {
        final QNm attn = new QNm(atn);
        if(atts == null) atts = new ArrayList<>(1);
        atts.add(attn);
        qnames.add(attn, false, info());
        add(cont, new CAttr(info(), false, attn, attv.finish()));
      }
      if(!consumeWS()) break;
    }

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(current() != '<' || next() != '/') {
        final Expr expr = dirElemContent(name.string());
        if(expr != null) add(cont, expr);
      }
      pos += 2;

      final byte[] close = qName(ELEMNAME_X);
      consumeWS();
      check('>');
      if(!eq(name.string(), close)) throw error(TAGWRONG_X_X, name.string(), close);
    }

    qnames.assignURI(this, npos);

    // check for duplicate attribute names
    if(atts != null) {
      final int as = atts.size();
      for(int a = 0; a < as - 1; a++) {
        for(int b = a + 1; b < as; b++) {
          if(atts.get(a).eq(atts.get(b))) throw error(ATTDUPL_X, atts.get(a));
        }
      }
    }

    sc.ns.size(size);
    sc.elemNS = nse;
    return new CElem(info(), false, name, ns, cont.finish());
  }

  /**
   * Parses the "DirElemContent" rule.
   * @param name name of opening element
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr dirElemContent(final byte[] name) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    boolean strip = true;
    while(true) {
      final int cp = current();
      if(cp == '<') {
        if(wsConsume("<![CDATA[")) {
          tb.add(cDataSection());
          strip = false;
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : next() == '/' ? null : dirConstructor(false);
        }
      } else if(cp == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : enclosedExpr();
        }
      } else if(cp == '}') {
        consume();
        check('}');
        tb.add('}');
      } else if(cp != 0) {
        strip &= !entity(tb);
      } else {
        throw error(NOCLOSING_X, name);
      }
    }
  }

  /**
   * Returns a string item.
   * @param tb token builder
   * @param strip strip flag
   * @return string item or {@code null}
   */
  private Str text(final TokenBuilder tb, final boolean strip) {
    final byte[] text = tb.toArray();
    return text.length == 0 || strip && !sc.spaces && ws(text) ? null : Str.get(text);
  }

  /**
   * Parses the "DirCommentConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirComment() throws QueryException {
    check('-');
    check('-');
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int cp = consume();
      if(cp == 0) throw error(NOCOMMENT);
      if(cp == '-' && consume('-')) {
        check('>');
        return new CComm(info(), false, Str.get(tb.finish()));
      }
      tb.add(cp);
    }
  }

  /**
   * Parses the "DirPIConstructor" rule.
   * Parses the "DirPIContents" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirPI() throws QueryException {
    final byte[] name = ncName(NOPINAME);
    if(eq(lc(name), XML)) throw error(PIXML_X, name);

    final boolean space = skipWs();
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int cp = consume();
      if(cp == 0) throw error(NOPI);
      if(cp == '?' && consume('>')) {
        return new CPI(info(), false, Str.get(name), Str.get(tb.finish()));
      }
      if(!space) throw error(NOPI);
      tb.add(cp);
    }
  }

  /**
   * Parses the "CDataSection" rule.
   * @return CData
   * @throws QueryException query exception
   */
  private byte[] cDataSection() throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int cp = consume();
      if(cp == 0) throw error(NOCDATA);
      if(cp == ']' && current(']') && next() == '>') {
        pos += 2;
        return tb.finish();
      }
      tb.add(cp);
    }
  }

  /**
   * Parses the "ComputedConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compConstructor() throws QueryException {
    final int p = pos;
    final Expr expr = wsConsumeWs(DOCUMENT) ? compDoc() :
      wsConsumeWs(ELEMENT) ? compElement() :
      wsConsumeWs(ATTRIBUTE) ? compAttribute() :
      wsConsumeWs(NAMESPACE) ? compNamespace() :
      wsConsumeWs(TEXT) ? compText() :
      wsConsumeWs(COMMENT) ? compComment() :
      wsConsumeWs(PROCESSING_INSTRUCTION) ? compPI() : null;
    if(expr == null) pos = p;
    return expr;
  }

  /**
   * Parses the "CompElemConstructor" rule.
   * Parses the "ContextExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compElement() throws QueryException {
    final Expr name = compName(NOELEMNAME, true);
    if(name == null) return null;
    if(name instanceof final QNm qnm) qnames.add(qnm, info());
    skipWs();
    return current('{') ? new CElem(info(), true, name, new Atts(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompAttrConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    final Expr name = compName(NOATTNAME, true);
    if(name == null) return null;
    if(name instanceof final QNm qnm) qnames.add(qnm, false, info());
    skipWs();
    return current('{') ? new CAttr(info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompNamespaceConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compNamespace() throws QueryException {
    final Expr name = compName(NONSNAME, false);
    if(name == null) return null;
    skipWs();
    return current('{') ? new CNSpace(info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompPIConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compPI() throws QueryException {
    final Expr name = compName(NOPINAME, false);
    if(name == null) return null;
    skipWs();
    return current('{') ? new CPI(info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses a computed name.
   * @param error error message
   * @param qname QName or NCName
   * @return name or {@code null}
   * @throws QueryException query exception
   */
  private Expr compName(final QueryError error, final boolean qname) throws QueryException {
    // parse name enclosed in curly braces
    if(consume("{")) {
      final Expr name = check(expr(), error);
      wsCheck("}");
      return name;
    }
    // parse literal name
    consume("#");
    skipWs();
    if(qname) return eQName(SKIPCHECK, null);

    // parse name enclosed in quotes
    final byte[] string = ncName(null);
    return string.length != 0 ? Str.get(string) : null;
  }

  /**
   * Parses the "CompDocConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compDoc() throws QueryException {
    return current('{') ? new CDoc(info(), false, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompTextConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compText() throws QueryException {
    return current('{') ? new CTxt(info(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompCommentConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compComment() throws QueryException {
    return current('{') ? new CComm(info(), true, enclosedExpr()) : null;
  }

  /**
   * Parses the "CastTarget" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType castTarget() throws QueryException {
    Type type;
    if(wsConsume("(")) {
      type = choiceItemType().type;
    } else {
      final QNm name = eQName(sc.elemNS, TYPEINVALID);
      if(!name.hasURI() && eq(name.local(), token(ENUM))) {
        if(!wsConsume("(")) throw error(WHICHCAST_X, AtomType.similar(name));
        type = enumerationType();
      } else {
        type = ListType.find(name);
        if(type == null) {
          type = AtomType.find(name, false);
          if(consume("(")) throw error(SIMPLETYPE_X, name.prefixId(XML));
          if(type == null ? name.eq(AtomType.ANY_SIMPLE_TYPE.qname()) :
            type.oneOf(AtomType.ANY_ATOMIC_TYPE, AtomType.NOTATION))
            throw error(INVALIDCAST_X, name.prefixId(XML));
          if(type == null) {
            final SeqType st = declaredTypes.get(name);
            if(st == null) throw error(WHICHCAST_X, AtomType.similar(name));
            type = st.type;
          }
        }
      }
    }
    if(type.atomic() == null) throw error(INVALIDCAST_X, type);
    skipWs();
    return SeqType.get(type, consume('?') ? Occ.ZERO_OR_ONE : Occ.EXACTLY_ONE);
  }

  /**
   * Parses the "SequenceType" rule.
   * Parses the "OccurrenceIndicator" rule.
   * Parses the "KindTest" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType sequenceType() throws QueryException {
    // empty sequence
    if(wsConsumeWs(EMPTY_SEQUENCE, INCOMPLETE, "(")) {
      wsCheck("(");
      wsCheck(")");
      return SeqType.EMPTY_SEQUENCE_Z;
    }

    // parse item type and occurrence indicator
    final SeqType st = itemType();
    skipWs();
    final Occ occ = consume('?') ? Occ.ZERO_OR_ONE : consume('+') ? Occ.ONE_OR_MORE :
      consume('*') ? Occ.ZERO_OR_MORE : Occ.EXACTLY_ONE;
    skipWs();
    return st.with(occ);
  }

  /**
   * Parses the "ItemType" rule.
   * Parses the "ParenthesizedItemType" rule.
   * @return item type
   * @throws QueryException query exception
   */
  private SeqType itemType() throws QueryException {
    // choice item type
    if(wsConsume("(")) return choiceItemType();

    // parse annotations and type name
    final AnnList anns = annotations(false).check(false, false);
    skipWs();
    SeqType st = null;
    Type type;
    final QNm name = eQName(null, TYPEINVALID);
    if(!name.hasURI() && eq(name.local(), token(ENUM))) {
      // enumeration
      if(!wsConsume("(")) throw error(WHICHCAST_X, AtomType.similar(name));
      type = enumerationType();
    } else if(wsConsume("(")) {
      // function type
      type = FuncType.find(name);
      if(type != null) return functionTest(anns, type).seqType();
      // node type
      type = NodeType.find(name);
      if(type != null) {
        // extended node type
        if(!wsConsume(")")) st = SeqType.get(type, Occ.EXACTLY_ONE, kindTest((NodeType) type));
      } else if(name.eq(AtomType.ITEM.qname())) {
        // item type
        type = AtomType.ITEM;
        wsCheck(")");
      }
      // no type found
      if(type == null) throw error(WHICHTYPE_X, Type.similar(name));
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(sc.elemNS);
      // atomic type
      type = AtomType.find(name, false);
      // declared type
      if(type == null) {
        st = declaredTypes.get(name);
        if(st == null) {
          RecordType ref  = recordTypeRefs.get(name);
          if(ref == null) {
            ref = new RecordType(name, info());
            recordTypeRefs.put(name, ref);
          }
          type = ref;
        }
      }
    }
    // annotations are not allowed for remaining types
    if(!anns.isEmpty()) throw error(NOANN);

    return st != null ? st : type.seqType();
  }

  /**
   * Parses the "FunctionTest" rule.
   * @param anns annotations
   * @param type function type
   * @return resulting type
   * @throws QueryException query exception
   */
  private Type functionTest(final AnnList anns, final Type type) throws QueryException {
    // wildcard
    if(wsConsume("*")) {
      wsCheck(")");
      return type;
    }

    // record
    if(type instanceof RecordType) {
      final TokenObjectMap<RecordField> fields = new TokenObjectMap<>();
      boolean extensible = !consume(')');
      if(extensible) {
        do {
          extensible = wsConsume("*");
          if(extensible) break;
          final byte[] name = quote(current()) ? stringLiteral() : ncName(NOSTRNCN_X);
          final boolean optional = wsConsume("?");
          final SeqType seqType = wsConsume(AS) ? sequenceType() : null;
          if(fields.contains(name)) throw error(DUPFIELD_X, name);
          fields.put(name, new RecordField(optional, seqType));
        } while(wsConsume(","));
        wsCheck(")");
      }
      return qc.shared.record(extensible, fields);
    }
    // map
    if(type instanceof MapType) {
      Type key = itemType().type;
      if(key instanceof final RecordType rt) key = rt.getDeclaration(namedRecordTypes);
      if(!key.instanceOf(AtomType.ANY_ATOMIC_TYPE)) throw error(MAPTAAT_X, key);
      wsCheck(",");
      final MapType tp = MapType.get(key, sequenceType());
      wsCheck(")");
      return tp;
    }
    // array
    if(type instanceof ArrayType) {
      final ArrayType tp = ArrayType.get(sequenceType());
      wsCheck(")");
      return tp;
    }
    // function type
    SeqType[] args = { };
    if(!wsConsume(")")) {
      // function has got arguments
      final QNmSet names = new QNmSet();
      do {
        skipWs();
        if(current('$')) {
          final QNm qnm = varName();
          if(!names.add(qnm)) throw FUNCDUPL_X.get(info(), qnm);
          wsCheck(AS);
        }
        args = Array.add(args, sequenceType());
      } while(wsConsume(","));
      wsCheck(")");
    }
    wsCheck(AS);
    return FuncType.get(anns, sequenceType(), args);
  }

  /**
   * Parses the "KindTest" rule without the type name and the opening bracket.
   * @param type type
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test kindTest(final NodeType type) throws QueryException {
    final Test tp = switch(type) {
      case DOCUMENT_NODE -> documentTest();
      case ELEMENT, ATTRIBUTE -> elemAttrTest(type);
      case PROCESSING_INSTRUCTION -> piTest();
      case SCHEMA_ELEMENT, SCHEMA_ATTRIBUTE -> schemaTest();
      default -> null;
    };
    wsCheck(")");
    return tp;
  }

  /**
   * Parses the "DocumentTest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test documentTest() throws QueryException {
    Test test;
    final boolean element = consume(ELEMENT), schema = !element && consume(SCHEMA_ELEMENT);
    if(element || schema) {
      wsCheck("(");
      skipWs();
      test = element ? elemAttrTest(NodeType.ELEMENT) : schemaTest();
      wsCheck(")");
    } else {
      test = Test.get(nameTestUnion(NodeType.ELEMENT));
      if(test == null) return null;
    }
    return new DocTest(test != null ? test : NodeTest.ELEMENT);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return error (not supported)
   * @throws QueryException query exception
   */
  private Test schemaTest() throws QueryException {
    throw error(SCHEMAINV_X, eQName(sc.elemNS, QNAME_X));
  }

  /**
   * Parses the "ElementTest" and "AttributeTest" rule without the leading keyword and brackets.
   * Parses the "TypeName" rule.
   * @param type node type
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test elemAttrTest(final NodeType type) throws QueryException {
    final ArrayList<Test> tests = nameTestUnion(type);
    if(tests.isEmpty()) return null;

    if(wsConsumeWs(",")) {
      final QNm name = eQName(sc.elemNS, QNAME_X);
      Type ann = ListType.find(name);
      if(ann == null) ann = AtomType.find(name, true);
      if(ann == null) throw error(TYPEUNDEF_X, AtomType.similar(name));
      // parse (and ignore) optional question mark
      if(type == NodeType.ELEMENT) wsConsume("?");
      if(!ann.oneOf(AtomType.ANY_TYPE, AtomType.UNTYPED) && (type == NodeType.ELEMENT ||
         !ann.oneOf(AtomType.ANY_SIMPLE_TYPE, AtomType.ANY_ATOMIC_TYPE, AtomType.UNTYPED_ATOMIC))) {
        throw error(STATIC_X, ann);
      }
    }
    return Test.get(tests);
  }

  /**
   * Parses the "PITest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test piTest() throws QueryException {
    token.reset();
    final byte[] name;
    if(quote(current())) {
      name = trim(stringLiteral());
      if(!XMLToken.isNCName(name)) throw error(INVNCNAME_X, name);
    } else if(ncName()) {
      name = token.toArray();
    } else {
      return null;
    }
    return NameTest.get(NodeType.PROCESSING_INSTRUCTION, new QNm(name), null);
  }

  /**
   * Parses the "EnumerationType" rule without the leading keyword and the opening bracket.
   * @return enum values
   * @throws QueryException query exception
   */
  private EnumType enumerationType() throws QueryException {
    final TokenSet values = new TokenSet();
    do {
      values.add(stringLiteral());
    } while(wsConsume(","));
    check(')');
    return new EnumType(values);
  }

  /**
   * Parses the "ChoiceItemType" rule without the leading parenthesis.
   * @return item type
   * @throws QueryException query exception
   */
  private SeqType choiceItemType() throws QueryException {
    final ArrayList<SeqType> types = new ArrayList<>() {
      @Override
      public boolean add(final SeqType st) {
        // collect alternative item type, combining consecutive EnumTypes into a single instance
        if(!(st.type instanceof EnumType) || isEmpty()) return super.add(st);
        final int i = size() - 1;
        final Type tp = get(i).type;
        if(!(tp instanceof EnumType)) return super.add(st);
        set(i, tp.union(st.type).seqType());
        return true;
      }
    };

    do {
      final SeqType st = itemType();
      // collect alternative item type, combining nested ChoiceItemTypes into a single instance
      if(st.type instanceof ChoiceItemType) {
        types.addAll(((ChoiceItemType) st.type).types);
      } else {
        types.add(st);
      }
    } while(wsConsume("|"));
    check(')');
    return types.size() == 1 ? types.get(0) : new ChoiceItemType(types).seqType();
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!wsConsumeWs(TRY)) return null;

    final Expr expr = enclosedExpr();

    Catch[] catches = { };
    while(wsConsume(CATCH)) {
      final ArrayList<Test> tests = nameTestUnion(NodeType.ELEMENT);
      if(tests.isEmpty()) throw error(NOCATCH);

      final int s = localVars.openScope();
      final InputInfo ii = info();
      final Var[] vrs = QueryException.variables(qc, ii);
      for(final Var var : vrs) localVars.add(var);
      final Catch c = new Catch(ii, enclosedExpr(), vrs, tests);
      localVars.closeScope(s);

      catches = Array.add(catches, c);
    }
    Expr fnlly = null;
    if(wsConsume(FINALLY)) fnlly = enclosedExpr();

    if(catches.length == 0 && fnlly == null) throw error(NOCATCH);
    return new Try(info(), expr, fnlly != null ? fnlly : Empty.VALUE, catches);
  }

  /**
   * Parses the "NameTestUnion" rule.
   * @param type node type
   * @return name tests or {@code null}
   * @throws QueryException query exception
   */
  private ArrayList<Test> nameTestUnion(final NodeType type) throws QueryException {
    final ArrayList<Test> tests = new ArrayList<>();
    Test test;
    final int p = pos;
    do {
      skipWs();
      test = (Test) simpleNodeTest(type, false);
      if(test == null) break;
      tests.add(test);
    } while(wsConsume("|"));
    if(test == null) pos = p;
    return tests;
  }

  /**
   * Parses the "FTSelection" rules.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftSelection(final boolean prg) throws QueryException {
    FTExpr expr = ftOr(prg), first = null, old;
    boolean ordered = false;
    do {
      old = expr;
      if(wsConsumeWs(ORDERED)) {
        ordered = true;
        old = null;
      } else if(wsConsumeWs(WINDOW)) {
        expr = new FTWindow(info(), expr, additive(), ftUnit());
      } else if(wsConsumeWs(DISTANCE)) {
        final Expr[] rng = ftRange(false);
        if(rng == null) throw error(FTRANGE);
        expr = new FTDistance(info(), expr, rng[0], rng[1], ftUnit());
      } else if(wsConsumeWs(AT)) {
        final FTContents cont = wsConsumeWs(START) ? FTContents.START : wsConsumeWs(END) ?
          FTContents.END : null;
        if(cont == null) throw error(INCOMPLETE);
        expr = new FTContent(info(), expr, cont);
      } else if(wsConsumeWs(ENTIRE)) {
        wsCheck(CONTENT);
        expr = new FTContent(info(), expr, FTContents.ENTIRE);
      } else {
        final boolean same = wsConsumeWs(SAME);
        final boolean diff = !same && wsConsumeWs(DIFFERENT);
        if(same || diff) {
          final FTUnit unit;
          if(wsConsumeWs(SENTENCE)) unit = FTUnit.SENTENCES;
          else if(wsConsumeWs(PARAGRAPH)) unit = FTUnit.PARAGRAPHS;
          else throw error(INCOMPLETE);
          expr = new FTScope(info(), expr, same, unit);
        }
      }
      if(first == null && old != null && old != expr) first = expr;
    } while(old != expr);

    if(ordered) {
      if(first == null) return new FTOrder(info(), expr);
      first.exprs[0] = new FTOrder(info(), first.exprs[0]);
    }
    return expr;
  }

  /**
   * Parses the "FTOr" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftOr(final boolean prg) throws QueryException {
    final FTExpr expr = ftAnd(prg);
    if(!wsConsumeWs(FTOR)) return expr;

    FTExpr[] list = { expr };
    do list = Array.add(list, ftAnd(prg)); while(wsConsumeWs(FTOR));
    return new FTOr(info(), list);
  }

  /**
   * Parses the "FTAnd" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftAnd(final boolean prg) throws QueryException {
    final FTExpr expr = ftMildNot(prg);
    if(!wsConsumeWs(FTAND)) return expr;

    FTExpr[] list = { expr };
    do list = Array.add(list, ftMildNot(prg)); while(wsConsumeWs(FTAND));
    return new FTAnd(info(), list);
  }

  /**
   * Parses the "FTMildNot" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftMildNot(final boolean prg) throws QueryException {
    final FTExpr expr = ftUnaryNot(prg);
    if(!wsConsumeWs(NOT)) return expr;

    FTExpr[] list = { };
    do {
      wsCheck(IN);
      list = Array.add(list, ftUnaryNot(prg));
    } while(wsConsumeWs(NOT));

    // convert "A not in B not in ..." to "A not in (B or ...)"
    final InputInfo ii = info();
    final FTExpr not = list.length == 1 ? list[0] : new FTOr(ii, list);
    if(expr.usesExclude() || not.usesExclude()) throw error(FTMILD, ii);
    return new FTMildNot(ii, expr, not);
  }

  /**
   * Parses the "FTUnaryNot" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftUnaryNot(final boolean prg) throws QueryException {
    final boolean not = wsConsumeWs(FTNOT);
    final FTExpr expr = ftPrimaryWithOptions(prg);
    return not ? new FTNot(info(), expr) : expr;
  }

  /**
   * Parses the "FTPrimaryWithOptions" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimaryWithOptions(final boolean prg) throws QueryException {
    FTExpr expr = ftPrimary(prg);

    final FTOpt fto = new FTOpt();
    boolean found = false;
    while(ftMatchOption(fto)) found = true;

    // check if specified language is not available
    if(found) {
      if(fto.ln == null) fto.ln = Language.def();
      if(!Tokenizer.supportFor(fto.ln)) throw error(FTNOTOK_X, fto.ln);
      if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln))
        throw error(FTNOSTEM_X, fto.ln);
    }

    // consume weight option
    if(wsConsumeWs(WEIGHT)) expr = new FTWeight(info(), expr, enclosedExpr());

    // skip options if none were specified...
    return found ? new FTOptions(info(), expr, fto) : expr;
  }

  /**
   * Parses the "FTPrimary" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimary(final boolean prg) throws QueryException {
    final Pragma[] pragmas = pragma();
    if(pragmas != null) {
      wsCheck("{");
      FTExpr expr = ftSelection(true);
      wsCheck("}");
      for(int p = pragmas.length - 1; p >= 0; p--) expr = new FTExtension(info(), pragmas[p], expr);
      return expr;
    }

    if(wsConsume("(")) {
      final FTExpr expr = ftSelection(false);
      wsCheck(")");
      return expr;
    }

    skipWs();
    final Expr e;
    if(quote(current())) {
      e = Str.get(stringLiteral());
    } else if(current('{')) {
      e = enclosedExpr();
    } else {
      throw error(prg ? NOPRAGMA : NOFTSELECT_X, found());
    }

    // FTAnyAllOption
    FTMode mode = FTMode.ANY;
    if(wsConsumeWs(ALL)) {
      mode = wsConsumeWs(WORDS) ? FTMode.ALL_WORDS : FTMode.ALL;
    } else if(wsConsumeWs(ANY)) {
      mode = wsConsumeWs(WORD) ? FTMode.ANY_WORD : FTMode.ANY;
    } else if(wsConsumeWs(PHRASE)) {
      mode = FTMode.PHRASE;
    }

    // FTTimes
    Expr[] occ = null;
    if(wsConsumeWs(OCCURS)) {
      occ = ftRange(false);
      if(occ == null) throw error(FTRANGE);
      wsCheck(TIMES);
    }
    return new FTWords(info(), e, mode, occ);
  }

  /**
   * Parses the "FTRange" rule.
   * @param i accept only integers ("FTLiteralRange")
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr[] ftRange(final boolean i) throws QueryException {
    final Expr[] occ = { Itr.ZERO, Itr.MAX };
    if(wsConsumeWs(EXACTLY)) {
      occ[0] = ftAdditive(i);
      occ[1] = occ[0];
    } else if(wsConsumeWs(AT)) {
      if(wsConsumeWs(LEAST)) {
        occ[0] = ftAdditive(i);
      } else if(wsConsumeWs(MOST)) {
        occ[1] = ftAdditive(i);
      } else {
        return null;
      }
    } else if(wsConsumeWs(FROM)) {
      occ[0] = ftAdditive(i);
      wsCheck(TO);
      occ[1] = ftAdditive(i);
    } else {
      return null;
    }
    return occ;
  }

  /**
   * Returns an argument of the "FTRange" rule.
   * @param i accept only integers
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr ftAdditive(final boolean i) throws QueryException {
    if(!i) return additive();
    skipWs();
    token.reset();
    while(digit(current())) token.add(consume());
    if(token.isEmpty()) throw error(INTEXP);
    return Itr.get(toLong(token.toArray()));
  }

  /**
   * Parses the "FTUnit" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private FTUnit ftUnit() throws QueryException {
    if(wsConsumeWs(WORDS)) return FTUnit.WORDS;
    if(wsConsumeWs(SENTENCES)) return FTUnit.SENTENCES;
    if(wsConsumeWs(PARAGRAPHS)) return FTUnit.PARAGRAPHS;
    throw error(INCOMPLETE);
  }

  /**
   * Parses the "FTMatchOption" rule.
   * @param opt options instance
   * @return false if no options were found
   * @throws QueryException query exception
   */
  private boolean ftMatchOption(final FTOpt opt) throws QueryException {
    if(!wsConsumeWs(USING)) return false;

    if(wsConsumeWs(LOWERCASE)) {
      if(opt.cs != null) throw error(FTDUP_X, CASE);
      opt.cs = FTCase.LOWER;
    } else if(wsConsumeWs(UPPERCASE)) {
      if(opt.cs != null) throw error(FTDUP_X, CASE);
      opt.cs = FTCase.UPPER;
    } else if(wsConsumeWs(CASE)) {
      if(opt.cs != null) throw error(FTDUP_X, CASE);
      if(wsConsumeWs(SENSITIVE)) {
        opt.cs = FTCase.SENSITIVE;
      } else {
        opt.cs = FTCase.INSENSITIVE;
        wsCheck(INSENSITIVE);
      }
    } else if(wsConsumeWs(DIACRITICS)) {
      if(opt.isSet(DC)) throw error(FTDUP_X, DIACRITICS);
      opt.set(DC, wsConsumeWs(SENSITIVE));
      if(!opt.is(DC)) wsCheck(INSENSITIVE);
    } else if(wsConsumeWs(LANGUAGE)) {
      if(opt.ln != null) throw error(FTDUP_X, LANGUAGE);
      final byte[] lan = stringLiteral();
      opt.ln = Language.get(string(lan));
      if(opt.ln == null) throw error(FTNOTOK_X, lan);
    } else if(wsConsumeWs(OPTION)) {
      optionDecl();
    } else {
      final boolean using = !wsConsumeWs(NO);

      if(wsConsumeWs(STEMMING)) {
        if(opt.isSet(ST)) throw error(FTDUP_X, STEMMING);
        opt.set(ST, using);
      } else if(wsConsumeWs(THESAURUS)) {
        if(opt.th != null) throw error(FTDUP_X, THESAURUS);
        opt.th = new ThesList();
        if(using) {
          final boolean par = wsConsume("(");
          if(!wsConsumeWs(DEFAULT)) ftThesaurusID(opt.th);
          while(par && wsConsume(",")) ftThesaurusID(opt.th);
          if(par) wsCheck(")");
        }
      } else if(wsConsumeWs(STOP)) {
        // add union/except
        wsCheck(WORDS);

        if(opt.sw != null) throw error(FTDUP_X, STOP + ' ' + WORDS);
        final StopWords sw = new StopWords();
        opt.sw = sw;
        if(wsConsumeWs(DEFAULT)) {
          if(!using) throw error(FTSTOP);
        } else if(using) {
          boolean union = false, except = false;
          do {
            if(wsConsume("(")) {
              do {
                final byte[] sl = stringLiteral();
                if(except) sw.remove(sl);
                else sw.add(sl);
              } while(wsConsume(","));
              wsCheck(")");
            } else if(wsConsumeWs(AT)) {
              // optional: resolve URI reference
              final IO fl = qc.resources.stopWords(string(stringLiteral()), sc);
              try {
                opt.sw.read(fl, except);
              } catch(final IOException expr) {
                Util.debug(expr);
                throw error(NOSTOPFILE_X, fl);
              }
            } else if(!union && !except) {
              throw error(FTSTOP);
            }
            union = wsConsumeWs(UNION);
            except = !union && wsConsumeWs(EXCEPT);
          } while(union || except);
        }
      } else if(wsConsumeWs(WILDCARDS)) {
        if(opt.isSet(WC)) throw error(FTDUP_X, WILDCARDS);
        if(opt.is(FZ)) throw error(FT_OPTIONS);
        opt.set(WC, using);
      } else if(wsConsumeWs(FUZZY)) {
        // extension to the official extension: "using fuzzy"
        if(opt.isSet(FZ)) throw error(FTDUP_X, FUZZY);
        if(opt.is(WC)) throw error(FT_OPTIONS);
        opt.set(FZ, using);
        if(digit(current())) {
          opt.errors = (int) ((ANum) ftAdditive(true)).itr();
          wsCheck(ERRORS);
        }
      } else {
        throw error(FTMATCH_X, currentAsString());
      }
    }
    return true;
  }

  /**
   * Parses the "FTThesaurusID" rule.
   * @param queries thesaurus queries
   * @throws QueryException query exception
   */
  private void ftThesaurusID(final ThesList queries) throws QueryException {
    wsCheck(AT);

    // optional: resolve URI reference
    final IO fl = qc.resources.thesaurus(string(stringLiteral()), sc);
    final byte[] rel = wsConsumeWs(RELATIONSHIP) ? stringLiteral() : EMPTY;
    final Expr[] range = ftRange(true);
    long min = 0, max = Long.MAX_VALUE;
    if(range != null) {
      wsCheck(LEVELS);
      // values will always be integer instances
      min = ((ANum) range[0]).itr();
      max = ((ANum) range[1]).itr();
    }
    queries.add(new ThesAccessor(fl, rel, min, max, info()));
  }

  /**
   * Parses the "InsertExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr insert() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(INSERT) || !wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      pos = p;
      return null;
    }

    final Expr s = check(single(), INCOMPLETE);
    Mode mode = Mode.INTO;
    if(wsConsumeWs(AS)) {
      if(wsConsumeWs(FIRST)) {
        mode = Mode.FIRST;
      } else {
        wsCheck(LAST);
        mode = Mode.LAST;
      }
      wsCheck(INTO);
    } else if(!wsConsumeWs(INTO)) {
      if(wsConsumeWs(AFTER)) {
        mode = Mode.AFTER;
      } else if(wsConsumeWs(BEFORE)) {
        mode = Mode.BEFORE;
      } else {
        throw error(INCOMPLETE);
      }
    }
    final Expr trg = check(single(), INCOMPLETE);
    qc.updating();
    return new Insert(info(), s, mode, trg);
  }

  /**
   * Parses the "DeleteExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr delete() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(DELETE) || !wsConsumeWs(NODES) && !wsConsumeWs(NODE)) {
      pos = p;
      return null;
    }
    qc.updating();
    return new Delete(info(), check(single(), INCOMPLETE));
  }

  /**
   * Parses the "RenameExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr rename() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(RENAME) || !wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      pos = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(AS);
    final Expr n = check(single(), INCOMPLETE);
    qc.updating();
    return new Rename(info(), trg, n);
  }

  /**
   * Parses the "ReplaceExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr replace() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(REPLACE)) return null;

    final boolean value = wsConsumeWs(VALUEE);
    if(value) {
      wsCheck(OF);
      if(!wsConsumeWs(NODES)) wsCheck(NODE);
    } else if(!wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      pos = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(WITH);
    final Expr src = check(single(), INCOMPLETE);
    qc.updating();
    return new Replace(info(), trg, src, value);
  }

  /**
   * Parses the "CopyModifyExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr copyModify() throws QueryException {
    if(!wsConsumeWs(COPY, INCOMPLETE, "$")) return null;

    final int s = localVars.openScope();
    Let[] fl = { };
    do {
      final Var var = newVar(SeqType.NODE_O);
      wsCheck(":=");
      final Expr expr = check(single(), INCOMPLETE);
      fl = Array.add(fl, new Let(localVars.add(var), expr));
    } while(wsConsumeWs(","));

    wsCheck(MODIFY);
    final InputInfo ii = info();
    final Expr m = check(single(), INCOMPLETE);
    wsCheck(RETURN);
    final Expr r = check(single(), INCOMPLETE);

    localVars.closeScope(s);
    qc.updating();
    return new Transform(ii, fl, m, r);
  }

  /**
   * Parses the "UpdatingFunctionCall" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr updatingFunctionCall() throws QueryException {
    final int p = pos;
    wsConsume(INVOKE);
    final boolean upd = wsConsumeWs(UPDATING), ndt = wsConsumeWs(NONDETERMINISTIC);
    if(upd || ndt) {
      final Expr func = primary();
      if(wsConsume("(")) {
        final InputInfo ii = info();
        final ExprList argList = new ExprList();

        if(!wsConsume(")")) {
          do {
            final Expr expr = single();
            if(expr == null) throw error(FUNCARG_X, found());
            argList.add(expr);
          } while(wsConsume(","));
          if(!wsConsume(")")) throw error(FUNCARG_X, found());
        }
        // skip if primary expression cannot be a function
        if(upd) qc.updating();
        return new DynFuncCall(ii, upd, ndt, func, argList.finish());
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "NCName" rule.
   * @param error optional error message
   * @return name (empty if no token was found)
   * @throws QueryException query exception
   */
  private byte[] ncName(final QueryError error) throws QueryException {
    token.reset();
    if(ncName()) return token.toArray();
    if(error != null) throw error(error, currentAsString());
    return EMPTY;
  }

  /**
   * Parses the "EQName" rule.
   * @param ns default namespace (can be {@code null}), or {@link #SKIPCHECK} to skip checks
   * @param error optional error message. If not {@code null}, will be raised if no EQName is found
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  private QNm eQName(final byte[] ns, final QueryError error) throws QueryException {
    // parse URIQualifiedName
    final int p = pos;
    if(consume("Q{")) {
      final byte[] uri = bracedURILiteral(), name1 = ncName(null);
      if(name1.length != 0) {
        if(!consume(':')) return new QNm(name1, uri);
        final byte[] name2 = ncName(null);
        if(name2.length != 0) {
          if(uri.length == 0) throw error(PREFIXNOURI_X, name2);
          return new QNm(name1, name2, uri);
        }
      }
      pos = p;
    }

    // parse QName (null will only be returned if no error was raised)
    final byte[] nm = qName(error);
    if(nm.length == 0) return null;
    if(ns == SKIPCHECK) return new QNm(nm);

    // create new EQName and set namespace
    final QNm name = new QNm(nm, sc);
    if(!name.hasURI()) {
      if(name.hasPrefix()) {
        pos = p;
        throw error(NOURI_X, name.prefix());
      }
      name.uri(ns);
    }
    return name;
  }

  /**
   * Parses the "QName" rule.
   * @param error optional error message. If not {@code null}, will be raised if no QName is found
   * @return QName string
   * @throws QueryException query exception
   */
  private byte[] qName(final QueryError error) throws QueryException {
    token.reset();
    if(!ncName()) {
      if(error != null) throw error(error, currentAsString());
    } else if(consume(':')) {
      if(XMLToken.isNCStartChar(current())) {
        token.add(':');
        do {
          token.add(consume());
        } while(XMLToken.isNCChar(current()));
      } else {
        --pos;
      }
    }
    return token.toArray();
  }

  /**
   * Helper method for parsing NCNames.
   * @return true for success
   */
  private boolean ncName() {
    if(!XMLToken.isNCStartChar(current())) return false;
    do token.add(consume()); while(XMLToken.isNCChar(current()));
    return true;
  }

  /**
   * Parses and converts entities.
   * @param tb token builder
   * @return true if an entity was found
   * @throws QueryException query exception
   */
  private boolean entity(final TokenBuilder tb) throws QueryException {
    final int p = pos;
    final boolean entity = consume('&');
    if(entity) {
      if(consume('#')) {
        final int b = consume('x') ? 0x10 : 10;
        boolean ok = true;
        int n = 0;
        do {
          final int cp = current();
          final boolean m = digit(cp);
          final boolean h = b == 0x10 && (cp >= 'a' && cp <= 'f' || cp >= 'A' && cp <= 'F');
          if(!m && !h) entityError(p, INVENTITY_X);
          final long nn = n;
          n = n * b + (consume() & 0xF);
          if(n < nn) ok = false;
          if(!m) n += 9;
        } while(!consume(';'));
        if(!ok) entityError(p, INVCHARREF_X);
        if(!XMLToken.valid(n)) entityError(p, INVCHARREF_X);
        tb.add(n);
      } else {
        if(consume("lt")) {
          tb.add('<');
        } else if(consume("gt")) {
          tb.add('>');
        } else if(consume("amp")) {
          tb.add('&');
        } else if(consume("quot")) {
          tb.add('"');
        } else if(consume("apos")) {
          tb.add('\'');
        } else {
          entityError(p, INVENTITY_X);
        }
        if(!consume(';')) entityError(p, INVENTITY_X);
      }
    } else {
      tb.add(consume());
    }
    return entity;
  }

  /**
   * Raises an entity error.
   * @param start start position
   * @param code error code
   * @throws QueryException query exception
   */
  private void entityError(final int start, final QueryError code) throws QueryException {
    final String sub = substring(start, Math.min(start + 20, length)).toString();
    final int semi = sub.indexOf(';');
    throw error(code, semi == -1 ? sub + DOTS : sub.substring(0, semi + 1));
  }

  /**
   * Raises an error if the specified expression is {@code null}.
   * @param <E> expression type
   * @param expr expression
   * @param error error message
   * @return expression
   * @throws QueryException query exception
   */
  private <E extends Expr> E check(final E expr, final QueryError error) throws QueryException {
    if(expr == null) throw error(error);
    return expr;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param ch expected character
   * @throws QueryException query exception
   */
  private void check(final char ch) throws QueryException {
    if(!consume(ch)) throw error(WRONGCHAR_X_X, ch, found());
  }

  /**
   * Skips whitespace, raises an error if the specified string cannot be consumed.
   * @param string expected string
   * @throws QueryException query exception
   */
  private void wsCheck(final String string) throws QueryException {
    if(!wsConsume(string)) throw error(WRONGCHAR_X_X, string, found());
  }

  /**
   * Consumes the specified string and surrounding whitespace.
   * @param string string to consume (words must not be followed by letters)
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String string) throws QueryException {
    final int p = pos;
    if(wsConsume(string)) {
      if(skipWs() || !XMLToken.isNCStartChar(string.charAt(0)) || !XMLToken.isNCChar(current()))
        return true;
      pos = p;
    }
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position. If the strings are
   * found, the cursor is placed after the first token.
   * @param string string to consume (words must not be followed by letters)
   * @param expr alternative error message (can be {@code null})
   * @param strings subsequent strings
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String string, final QueryError expr, final String... strings)
      throws QueryException {

    final int p1 = pos;
    if(!wsConsumeWs(string)) return false;
    final int p2 = pos;
    alter = expr;
    alterPos = p2;
    for(final String s : strings) {
      if(wsConsume(s)) {
        pos = p2;
        return true;
      }
    }
    pos = p1;
    return false;
  }

  /**
   * Skips whitespace, consumes the specified string and ignores trailing characters.
   * @param string string to consume
   * @return true if string was found
   * @throws QueryException query exception
   */
  private boolean wsConsume(final String string) throws QueryException {
    skipWs();
    return consume(string);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespace was found
   * @throws QueryException query exception
   */
  private boolean skipWs() throws QueryException {
    final int i = pos;
    while(more()) {
      final int cp = current();
      if(cp == '(' && next() == ':') {
        comment();
      } else {
        if(cp == 0 || cp > ' ') return i != pos;
        ++pos;
      }
    }
    return i != pos;
  }

  /**
   * Consumes a comment.
   * @throws QueryException query exception
   */
  private void comment() throws QueryException {
    ++pos;
    final boolean xqdoc = next() == '~';
    if(xqdoc) {
      docBuilder.reset();
      ++pos;
    }
    comment(false, xqdoc);
  }

  /**
   * Consumes a comment.
   * @param nested nested flag
   * @param xqdoc xqdoc flag
   * @throws QueryException query exception
   */
  private void comment(final boolean nested, final boolean xqdoc) throws QueryException {
    while(++pos < length) {
      int curr = current();
      if(curr == '(' && next() == ':') {
        ++pos;
        comment(true, xqdoc);
        curr = current();
      }
      if(curr == ':' && next() == ')') {
        pos += 2;
        if(!nested && moduleDoc.isEmpty()) {
          moduleDoc = docBuilder.toString().trim();
          docBuilder.reset();
        }
        return;
      }
      if(xqdoc) docBuilder.add(curr);
    }
    throw error(COMCLOSE);
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespace was found
   */
  private boolean consumeWS() {
    final int i = pos;
    while(more()) {
      final int cp = current();
      if(cp == 0 || cp > ' ') return i != pos;
      ++pos;
    }
    return true;
  }

  /**
   * Returns an alternative error, or the supplied error if no alternative error is registered.
   * @param error query error (can be {@code null})
   * @return error
   */
  private QueryException alterError(final QueryError error) {
    if(alter == null) return error(error);
    pos = alterPos;
    return error(alter);
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param expr new expression
   * @throws QueryException query exception
   */
  private void add(final ExprList ar, final Expr expr) throws QueryException {
    if(expr == null) throw error(INCOMPLETE);
    ar.add(expr);
  }

  /**
   * Creates the specified error.
   * @param error error to be thrown
   * @param arg error arguments
   * @return error
   */
  private QueryException error(final QueryError error, final Object... arg) {
    return error(error, info(), arg);
  }

  /**
   * Creates the specified error.
   * @param error error to be thrown
   * @param info input info
   * @param arg error arguments
   * @return error
   */
  public QueryException error(final QueryError error, final InputInfo info, final Object... arg) {
    return error.get(info, arg);
  }

  /**
   * Checks if the specified XQuery string is a library module.
   * @param query query string
   * @return result of check
   */
  public static boolean isLibrary(final String query) {
    return LIBMOD_PATTERN.matcher(removeComments(query, 80)).matches();
  }

  /**
   * Removes comments from the specified string and returns the first characters of a query.
   * @param query query string
   * @param max maximum length of string to return
   * @return result
   */
  public static String removeComments(final String query, final int max) {
    final StringBuilder sb = new StringBuilder();
    boolean s = false;
    final int ql = query.length();
    for(int m = 0, c = 0; c < ql && sb.length() < max; ++c) {
      final char ch = query.charAt(c);
      if(ch == 0x0d) continue;
      if(ch == '(' && c + 1 < ql && query.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        ++m;
        ++c;
      } else if(m != 0 && ch == ':' && c + 1 < ql && query.charAt(c + 1) == ')') {
        --m;
        ++c;
      } else if(m == 0) {
        if(ch > ' ') sb.append(ch);
        else if(!s) sb.append(' ');
        s = ch <= ' ';
      }
    }
    if(sb.length() >= max) sb.append(Text.DOTS);
    return sb.toString().trim();
  }

  @Override
  public final InputInfo info() {
    return new InputInfo(this, sc);
  }
}
