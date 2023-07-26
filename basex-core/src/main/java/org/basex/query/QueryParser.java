package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
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
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.item.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** Pattern for detecting library modules. */
  private static final Pattern LIBMOD_PATTERN = Pattern.compile(
      "^(xquery( version ['\"].*?['\"])?( encoding ['\"].*?['\"])? ?; ?)?module .*");
  /** QName check: skip namespace check. */
  private static final byte[] SKIPCHECK = {};
  /** Reserved function names. */
  private static final TokenSet KEYWORDS;
  /** Decimal declarations. */
  private static final byte[][] DECFORMATS = tokens(
    DF_DEC, DF_DIG, DF_GRP, DF_EXP, DF_INF, DF_MIN, DF_NAN, DF_PAT, DF_PC, DF_PM, DF_ZD
  );

  // initialize keywords
  static {
    KEYWORDS = new TokenSet(
      NodeType.ATTRIBUTE.qname().string(), NodeType.COMMENT.qname().string(),
      NodeType.DOCUMENT_NODE.qname().string(), NodeType.ELEMENT.qname().string(),
      NodeType.NAMESPACE_NODE.qname().string(), NodeType.NODE.qname().string(),
      NodeType.PROCESSING_INSTRUCTION.qname().string(), NodeType.TEXT.qname().string(),
      NodeType.SCHEMA_ATTRIBUTE.qname().string(), NodeType.SCHEMA_ELEMENT.qname().string(),
      ArrayType.ARRAY, FuncType.FUNCTION, MapType.MAP, AtomType.ITEM.qname().string(),
      token(EMPTY_SEQUENCE), token(IF), token(SWITCH), token(TYPESWITCH));
  }

  /** URIs of modules loaded by the current file. */
  public final TokenSet moduleURIs = new TokenSet();
  /** Query context. */
  public final QueryContext qc;
  /** Static context. */
  public final StaticContext sc;

  /** List of modules to be parsed. */
  private final ArrayList<ModInfo> modules = new ArrayList<>();
  /** Namespaces. */
  private final TokenMap namespaces = new TokenMap();

  /** Parsed variables. */
  private final ArrayList<StaticVar> vars = new ArrayList<>();
  /** Parsed functions. */
  private final ArrayList<StaticFunc> funcs = new ArrayList<>();

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<>();
  /** QName cache. */
  private final QNmCache qnames = new QNmCache();
  /** Local variable. */
  private final LocalVars localVars = new LocalVars(this);

  /** Temporary token cache. */
  private final TokenBuilder token = new TokenBuilder();
  /** Current XQDoc string. */
  private final StringBuilder docBuilder = new StringBuilder();

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
      if(wsConsumeWs(MODULE, NAMESPACE, null)) throw error(MAINMOD);
      pos = p;

      prolog1();
      importModules();
      prolog2();

      localVars.pushContext(false);
      final Expr expr = expr();
      if(expr == null) throw alterError(EXPREMPTY);

      final VarScope vs = localVars.popContext();
      final MainModule mm = new MainModule(expr, vs);
      mm.set(funcs, vars, moduleURIs, namespaces, moduleDoc);
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
      final byte[] path = token(baseO == null ? "" : baseO.path());
      qc.modParsed.put(path, uri);
      qc.modStack.push(path);

      prolog1();
      importModules();
      prolog2();
      finish(null);
      if(root) check(null);

      qc.modStack.pop();
      final LibraryModule lm = new LibraryModule(sc);
      lm.set(funcs, vars, moduleURIs, namespaces, moduleDoc);
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
    file = baseIO == null ? null : baseIO.path();
    if(!more()) throw error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    for(int p = 0; p < length;) {
      // only retrieve code points for large character codes (faster)
      int cp = input.charAt(p);
      final boolean hs = cp >= Character.MIN_HIGH_SURROGATE;
      if(hs) cp = input.codePointAt(p);
      if(!XMLToken.valid(cp)) {
        pos = p;
        throw error(MODLEINV_X, cp);
      }
      p += hs ? Character.charCount(cp) : 1;
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
      final String ver = string(stringLiteral());
      if(!ver.equals("1.0") && !Strings.eq(ver, "1.1", "3.0", "3.1", "4.0"))
        throw error(XQUERYVER_X, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if(wsConsumeWs(ENCODING)) {
      final String encoding = string(stringLiteral());
      if(!Strings.supported(encoding)) throw error(XQUERYENC2_X, encoding);
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
      docBuilder.setLength(0);
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
        contextItemDecl();
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
        } else if(!anns.isEmpty()) {
          throw error(VARFUNC);
        } else {
          pos = p;
          break;
        }
      }
      docBuilder.setLength(0);
      skipWs();
      check(';');
    }
  }

  /**
   * Parses the "Annotation" rule.
   * @param updating also check for updating keyword
   * @return annotations
   * @throws QueryException query exception
   */
  private AnnList annotations(final boolean updating) throws QueryException {
    final AnnList anns = new AnnList();
    while(true) {
      final Ann ann;
      if(updating && wsConsumeWs(UPDATING)) {
        ann = new Ann(info(), Annotation.UPDATING, Empty.VALUE);
      } else if(wsConsumeWs("%")) {
        final InputInfo ii = info();
        final QNm name = eQName(XQ_URI, QNAME_X);

        final ItemList items = new ItemList();
        if(wsConsumeWs("(")) {
          do {
            final Expr expr = literal();
            if(!(expr instanceof Item)) {
              if(Function.ERROR.is(expr)) expr.item(qc, ii);
              throw error(ANNVALUE);
            }
            items.add((Item) expr);
          } while(wsConsumeWs(","));
          check(')');
        }

        // check if annotation is a pre-defined one
        final Annotation def = Annotation.get(name);
        if(def == null) {
          // reject unknown annotations with pre-defined namespaces, ignore others
          final byte[] uri = name.uri();
          if(NSGlobal.prefix(uri).length != 0 && !eq(uri, LOCAL_URI, ERROR_URI)) {
            throw error(NSGlobal.reserved(uri) ? ANNWHICH_X_X : BASEX_ANNOTATION1_X_X,
                ii, '%', name.string());
          }
          ann = new Ann(ii, name, items.value());
        } else {
          // check if annotation is specified more than once
          if(def.single && anns.contains(def)) throw error(BASEX_ANN3_X_X, ii, '%', def.id());

          final long arity = items.size();
          if(arity < def.minMax[0] || arity > def.minMax[1])
            throw error(BASEX_ANN2_X_X, ii, def, arguments(arity));
          final int al = def.params.length;
          for(int a = 0; a < arity; a++) {
            final SeqType st = def.params[Math.min(al - 1, a)];
            final Item item = items.get(a);
            if(!st.instance(item)) throw error(BASEX_ANN_X_X_X, ii, def, st, item.seqType());
          }
          ann = new Ann(ii, def, items.value());
        }
      } else {
        break;
      }

      anns.add(ann);
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
      sc.funcNS = uri.length == 0 ? null : uri;
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
    final byte[] value = stringLiteral();
    final String name = string(qname.local());

    if(eq(qname.uri(), OUTPUT_URI)) {
      // output declaration
      if(sc.module != null) throw error(OPTDECL_X, qname.string());

      final SerializerOptions sopts = qc.parameters();
      if(!decl.add("S " + name)) throw error(OUTDUPL_X, name);
      sopts.parse(name, value, sc, info());

    } else if(eq(qname.uri(), DB_URI)) {
      // project-specific declaration
      if(sc.module != null) throw error(BASEX_OPTIONS3_X, qname.local());
      qc.options.add(name, value, this);

    } else if(eq(qname.uri(), BASEX_URI)) {
      // query-specific options
      if(!name.equals(LOCK)) throw error(BASEX_OPTIONS1_X, name);
      for(final String lock : Locking.queryLocks(value)) qc.locks.add(lock);
    }
    // ignore unknown options
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
    final QNm name = def ? QNm.EMPTY : eQName(null, QNAME_X);

    // check if format has already been declared
    if(sc.decFormats.get(name.internal()) != null) throw error(DECDUPL);

    // create new format
    final TokenMap map = new TokenMap();
    // collect all property declarations
    int n;
    do {
      n = map.size();
      skipWs();
      final byte[] prop = ncName(null);
      for(final byte[] s : DECFORMATS) {
        if(!eq(prop, s)) continue;
        if(map.get(s) != null) throw error(DECDUPLPROP_X, s);
        wsCheck("=");
        map.put(s, stringLiteral());
        break;
      }
    } while(n != map.size());

    // completes the format declaration
    sc.decFormats.put(name.internal(), new DecFormatter(map, info()));
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
    sc.collation = Collation.get(stringLiteral(), qc, sc, info(), WHICHDEFCOLL_X);
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
      final byte[] path = qc.modDeclared.get(uri);
      if(path != null) mi.paths.add(path);
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
      // no paths specified: skip statically available modules; try to resolve module uri
      if(Functions.staticURI(uri) || qc.resources.modules().addImport(string(uri), this, mi.info))
        return;
      // module not found
      throw error(WHICHMOD_X, mi.info, uri);
    }
    // parse supplied paths
    for(final byte[] path : mi.paths) module(string(path), string(uri), mi.info);
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param path file path
   * @param uri base URI of module
   * @param ii input info
   * @throws QueryException query exception
   */
  public final void module(final String path, final String uri, final InputInfo ii)
      throws QueryException {

    // get absolute path
    final IO io = sc.resolve(path, uri);
    final byte[] tPath = token(io.path());

    // check if module has already been parsed
    final byte[] tUri = token(uri), pUri = qc.modParsed.get(tPath);
    if(pUri != null) {
      if(!eq(tUri, pUri)) throw error(WRONGMODULE_X_X_X, ii, io.name(), uri, pUri);
      return;
    }
    qc.modParsed.put(tPath, tUri);

    // read module
    final String query;
    try {
      query = io.string();
    } catch(final IOException expr) {
      Util.debug(expr);
      throw error(WHICHMODFILE_X, ii, io);
    }

    qc.modStack.push(tPath);
    final QueryParser qp = new QueryParser(query, io.path(), qc, null);

    // check if import and declaration uri match
    final LibraryModule lib = qp.parseLibrary(false);
    final byte[] muri = lib.sc.module.uri();
    if(!uri.equals(string(muri))) throw error(WRONGMODULE_X_X_X, ii, io.name(), uri, muri);

    // check if context value declaration types are compatible to each other
    final StaticContext sctx = qp.sc;
    if(sctx.contextType != null) {
      if(sc.contextType == null) {
        sc.contextType = sctx.contextType;
      } else if(!sctx.contextType.eq(sc.contextType)) {
        throw error(CITYPES_X_X, sctx.contextType, sc.contextType);
      }
    }
    qc.modStack.pop();
  }

  /**
   * Parses the "ContextItemDecl" rule.
   * @throws QueryException query exception
   */
  private void contextItemDecl() throws QueryException {
    wsCheck(ITEM);
    if(!decl.add(ITEM)) throw error(DUPLITEM);

    if(wsConsumeWs(AS)) {
      final SeqType st = itemType();
      if(sc.contextType == null) {
        sc.contextType = st;
      } else if(!sc.contextType.eq(st)) {
        throw error(CITYPES_X_X, sc.contextType, st);
      }
    }

    final boolean external = wsConsumeWs(EXTERNAL);
    if(!consume(":=")) {
      if(external) return;
      throw error(WRONGCHAR_X_X, ":=", found());
    }
    if(!external) qc.finalContext = true;

    localVars.pushContext(false);
    final Expr expr = check(single(), NOCIDECL);
    final VarScope vs = localVars.popContext();
    final SeqType st = sc.contextType;
    qc.contextScope = new ContextScope(expr, st != null ? st : SeqType.ITEM_O, vs);
    final StaticScope cs =  qc.contextScope;
    cs.info = info();
    cs.doc(docBuilder.toString());

    if(sc.module != null) throw error(DECITEM);
    if(!sc.mixUpdates && expr.has(Flag.UPD)) throw error(UPCTX, expr);
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
    final StaticVar sv = qc.vars.declare(var, expr, anns, doc, external, vs);
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
    final QNm name = checkReserved(eQName(sc.funcNS, FUNCNAME));
    wsCheck("(");
    if(sc.module != null && !eq(name.uri(), sc.module.uri())) throw error(MODULENS_X, name);

    localVars.pushContext(false);
    final Params params = paramList(true);
    final Expr expr = wsConsumeWs(EXTERNAL) ? null : enclosedExpr();
    final String doc = docBuilder.toString();
    final VarScope vs = localVars.popContext();
    final StaticFunc func = qc.functions.declare(name, params, expr, anns, doc, vs, ii);
    funcs.add(func);
  }

  /**
   * Checks if the specified name equals a reserved keyword.
   * @param name name
   * @return argument
   * @throws QueryException query exception
   */
  private QNm checkReserved(final QNm name) throws QueryException {
    if(reserved(name)) throw error(RESERVED_X, name.local());
    return name;
  }

  /**
   * Checks if the specified name equals reserved function names.
   * @param name name to be checked
   * @return result of check
   */
  private static boolean reserved(final QNm name) {
    return !name.hasPrefix() && KEYWORDS.contains(name.string());
  }

  /**
   * Parses a ParamList.
   * @param sttc static function
   * @return declared variables
   * @throws QueryException query exception
   */
  private Params paramList(final boolean sttc) throws QueryException {
    final Params params = new Params();
    do {
      skipWs();
      if(curr() != '$' && params.isEmpty()) break;
      final InputInfo ii = info();
      final QNm name = varName();
      final SeqType type = optAsType();
      final Expr dflt = sttc && wsConsume(":=") ? single() : null;
      params.add(name, type, dflt, ii);
    } while(consume(','));

    wsCheck(")");
    params.type = optAsType();
    params.finish(qc, sc, localVars);

    return params;
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
    final ExprList el = new ExprList(expr);
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
    if(expr == null) expr = ternaryIf();
    return expr;
  }

  /**
   * Parses the "FLWORExpr" rule.
   * Parses the "WhereClause" rule.
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

    final TokenObjMap<Var> curr = new TokenObjMap<>();
    for(final Clause fl : clauses)
      for(final Var var : fl.vars()) curr.put(var.name.internal(), var);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(final Clause clause : clauses) {
          for(final Var var : clause.vars()) curr.put(var.name.internal(), var);
        }
      } while(size < clauses.size());

      if(wsConsumeWs(WHERE)) {
        alterPos = pos;
        clauses.add(new Where(check(single(), NOWHERE), info()));
      }

      if(wsConsumeWs(GROUP)) {
        wsCheck(BY);
        skipWs();
        alterPos = pos;
        final GroupSpec[] specs = groupSpecs(clauses);

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<>();
        for(final GroupSpec spec : specs) curr.put(spec.var.name.internal(), spec.var);
        VARS:
        for(final Var var : curr.values()) {
          for(final GroupSpec spec : specs) {
            if(spec.var.is(var)) continue VARS;
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
          final Var nv = localVars.add(new Var(ref.var.name, null, qc, sc, ref.var.info));
          ngrp[i] = nv;
          curr.put(nv.name.internal(), nv);
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

      if(wsConsumeWs(COUNT, "$", NOCOUNT)) {
        final Var var = localVars.add(newVar(SeqType.INTEGER_O));
        curr.put(var.name.internal(), var);
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
   * @param clauses FLWOR clauses
   * @return query expression
   * @throws QueryException query exception
   */
  private LinkedList<Clause> initialClause(final LinkedList<Clause> clauses) throws QueryException {
    LinkedList<Clause> cls = clauses;
    // WindowClause
    final boolean slide = wsConsumeWs(FOR, SLIDING, NOWINDOW);
    if(slide || wsConsumeWs(FOR, TUMBLING, NOWINDOW)) {
      if(cls == null) cls = new LinkedList<>();
      cls.add(windowClause(slide));
    } else {
      // ForClause / LetClause
      final boolean let = wsConsumeWs(LET, SCORE, NOLET) || wsConsumeWs(LET, "$", NOLET);
      if(let || wsConsumeWs(FOR, "$", NOFOR)) {
        if(cls == null) cls = new LinkedList<>();
        if(let) letClause(cls);
        else    forClause(cls);
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
      final Var var = newVar();
      final boolean emp = wsConsume(ALLOWING);
      if(emp) wsCheck(EMPTYY);
      final Var at = wsConsumeWs(AT) ? newVar(SeqType.INTEGER_O) : null;
      final Var score = wsConsumeWs(SCORE) ? newVar(SeqType.DOUBLE_O) : null;
      // check for duplicate variable names
      if(at != null) {
        if(var.name.eq(at.name)) throw error(DUPLVAR_X, at);
        if(score != null && at.name.eq(score.name)) throw error(DUPLVAR_X, score);
      }
      if(score != null && var.name.eq(score.name)) throw error(DUPLVAR_X, score);
      wsCheck(IN);
      final Expr expr = check(single(), NOVARDECL);
      // declare late because otherwise it would shadow the wrong variables
      clauses.add(new For(localVars.add(var), localVars.add(at), localVars.add(score), expr, emp));
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
      final boolean score = wsConsumeWs(SCORE);
      final Var var = score ? newVar(SeqType.DOUBLE_O) : newVar();
      wsCheck(":=");
      final Expr expr = check(single(), NOVARDECL);
      clauses.add(new Let(localVars.add(var), expr, score));
    } while(wsConsume(","));
  }

  /**
   * Parses the "TumblingWindowClause" rule.
   * Parses the "SlidingWindowClause" rule.
   * @param sliding sliding window flag
   * @return the window clause
   * @throws QueryException parse exception
   */
  private Window windowClause(final boolean sliding) throws QueryException {
    wsCheck(sliding ? SLIDING : TUMBLING);
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
    final Var var = curr('$')             ? localVars.add(newVar(SeqType.ITEM_O))    : null;
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
      Collation.get(stringLiteral(), qc, sc, info(), FLWORCOLL_X) : sc.collation;
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
      if(var.declType != null || wsConsume(":=")) {
        if(var.declType != null) wsCheck(":=");
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
              if(spec.var.is(ref.var)) {
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
          qc, sc, info(), FLWORCOLL_X) : sc.collation;
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
    final boolean some = wsConsumeWs(SOME, "$", NOSOME);
    if(!some && !wsConsumeWs(EVERY, "$", NOSOME)) return null;

    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = new LinkedList<>();
    do {
      final Var var = newVar();
      wsCheck(IN);
      final Expr expr = check(single(), NOSOME);
      clauses.add(new For(localVars.add(var), expr));
    } while(wsConsumeWs(","));

    wsCheck(SATISFIES);
    final Expr rtrn = Function.BOOLEAN.get(sc, info(), check(single(), NOSOME));
    localVars.closeScope(s);

    final InputInfo info = clauses.peek().info();
    final GFLWOR flwor = new GFLWOR(info, clauses, rtrn);
    final CmpG cmp = new CmpG(info, flwor, Bln.get(some), OpG.EQ, null, sc);
    return some ? cmp : Function.NOT.get(sc, info, cmp);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!wsConsumeWs(SWITCH, "(", TYPEPAR)) return null;
    final InputInfo ii = info();
    wsCheck("(");
    final Expr cond = check(expr(), NOSWITCH);
    final ArrayList<SwitchGroup> groups = new ArrayList<>();
    wsCheck(")");
    final boolean brace = wsConsume("{");

    // collect all cases
    ExprList exprs;
    do {
      exprs = new ExprList((Expr) null);
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

    return new Switch(ii, cond, groups.toArray(SwitchGroup[]::new));
  }

  /**
   * Parses the "TypeswitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!wsConsumeWs(TYPESWITCH, "(", TYPEPAR)) return null;
    final InputInfo ii = info();
    wsCheck("(");
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
      if(curr('$')) {
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
    if(!wsConsumeWs(IF, "(", IFPAR)) return null;

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
   * Parses the "TernaryIfExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ternaryIf() throws QueryException {
    final Expr iff = elvis();
    if(!wsConsume("??")) return iff;

    final InputInfo ii = info();
    final Expr thn = check(single(), NOTERNARY);
    if(!wsConsume("!!")) throw error(NOTERNARY);
    final Expr els = check(single(), NOTERNARY);
    return new If(ii, iff, thn, els);
  }

  /**
   * Parses the "ElvisExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr elvis() throws QueryException {
    final Expr expr = or();
    return wsConsume("?:") ? new Otherwise(info(), expr, check(single(), NODEFAULT)) : expr;
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
      for(final OpV c : OpV.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpV(info(), expr, check(ftContains(), CMPEXPR), c, sc.collation, sc);
      }
      for(final OpN c : OpN.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpN(info(), expr, check(ftContains(), CMPEXPR), c);
      }
      for(final OpG c : OpG.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpG(info(), expr, check(ftContains(), CMPEXPR), c, sc.collation, sc);
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
    final Expr expr = stringConcat();
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
   * Parses the "StringConcatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr stringConcat() throws QueryException {
    final Expr expr = range();
    if(expr == null || !consume("||")) return expr;

    final ExprList el = new ExprList(expr);
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
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ? Calc.MINUS : null;
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
    Expr expr = otherwise();
    while(expr != null) {
      final Calc c = consume('*') || consume('×') ? Calc.MULT :
        consume('÷') || wsConsumeWs(DIV) ? Calc.DIV :
        wsConsumeWs(IDIV) ? Calc.IDIV :
        wsConsumeWs(MOD) ? Calc.MOD : null;
      if(c == null) break;
      expr = new Arith(info(), expr, check(otherwise(), CALCEXPR), c);
    }
    return expr;
  }

  /**
   * Parses the "OtherwiseExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr otherwise() throws QueryException {
    final Expr expr = union();
    if(expr == null || !wsConsumeWs(OTHERWISE)) return expr;
    final ExprList el = new ExprList(expr);
    do add(el, union()); while(wsConsume(OTHERWISE));
    return new Otherwise(info(), el.finish());
  }

  /**
   * Parses the "UnionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr union() throws QueryException {
    final Expr expr = intersect();
    if(expr == null || !isUnion()) return expr;
    final ExprList el = new ExprList(expr);
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
      if(el == null) el = new ExprList(expr);
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
    final Expr expr = promote();
    if(!wsConsumeWs(TREAT)) return expr;
    wsCheck(AS);
    return new Treat(sc, info(), expr, sequenceType());
  }

  /**
   * Parses the "TreatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr promote() throws QueryException {
    final Expr expr = castable();
    if(!wsConsumeWs(PROMOTE)) return expr;
    wsCheck(TO);
    return new TypeCheck(sc, info(), expr, sequenceType(), true);
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
    return new Castable(sc, info(), expr, simpleType());
  }

  /**
   * Parses the "CastExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr expr = arrow();
    if(!wsConsumeWs(CAST)) return expr;
    wsCheck(AS);
    return new Cast(sc, info(), expr, simpleType());
  }

  /**
   * Parses the "ArrowExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr arrow() throws QueryException {
    Expr expr = transformWith();
    if(expr != null) {
      for(boolean mapping; (mapping = wsConsume("=!>")) || consume("=>");) {
        skipWs();
        Expr ex = null;
        if(curr('$')) {
          ex = varRef();
        } else if(curr('(')) {
          ex = parenthesized();
        } else {
          ex = functionItem();
          if(ex == null) ex = checkReserved(eQName(sc.funcNS, ARROWSPEC));
        }
        final InputInfo ii = info();
        final Expr arg;
        For fr = null;
        int s = 0;
        if(mapping) {
          s = localVars.openScope();
          fr = new For(new Var(new QNm("item"), null, qc, sc, ii), expr);
          arg = new VarRef(ii, fr.var);
        } else {
          arg = expr;
        }
        final boolean qname = ex instanceof QNm;
        final FuncArgs args = argumentList(qname, arg);
        expr = qname ? funcCall((QNm) ex, ii, args) :
          dynFuncCall(ex, ii, args.exprs(), args.holes());
        if(mapping) {
          expr = new GFLWOR(ii, fr, expr);
          localVars.closeScope(s);
        }
      }
    }
    return expr;
  }

  /**
   * Parses the "TransformWithExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr transformWith() throws QueryException {
    Expr expr = unary();
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
    return expr == null ? map() : expr;
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
    if(curr('{')) {
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
    if(!wsConsumeWs("(#")) return null;

    final ArrayList<Pragma> el = new ArrayList<>();
    do {
      final QNm name = eQName(null, QNAME_X);
      char ch = curr();
      if(ch != '#' && !ws(ch)) throw error(PRAGMAINV);
      token.reset();
      while(ch != '#' || next() != ')') {
        if(ch == 0) throw error(PRAGMAINV);
        token.add(consume());
        ch = curr();
      }

      final byte[] value = token.trim().toArray();
      if(eq(name.prefix(), DB_PREFIX)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final Option<?> opt = qc.context.options.option(key);
        if(opt == null) throw error(BASEX_OPTIONS1_X, key);
        el.add(new DBPragma(name, opt, value));
      } else if(eq(name.prefix(), BASEX_PREFIX)) {
        // project-specific declaration
        el.add(new BaseXPragma(name, value));
      }
      pos += 2;
    } while(wsConsumeWs("(#"));
    return el.toArray(Pragma[]::new);
  }

  /**
   * Parses the "MapExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr map() throws QueryException {
    final Expr expr = path();
    if(expr != null) {
      final int next = next();
      if(next != '=' && next != '!' && wsConsumeWs("!")) {
        final ExprList el = new ExprList(expr);
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
      root = Function._UTIL_ROOT.get(sc, ii, new ContextValue(ii));
      el = new ExprList();
      final Expr expr;
      if(consume('/')) {
        // two slashes: absolute descendant path
        checkAxis(Axis.DESCENDANT);
        add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, KindTest.NODE));
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
      if(curr() != '/' && !(expr instanceof Step)) return expr;
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
          add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, KindTest.NODE));
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
   * @param test node test
   * @param element element flag
   */
  @SuppressWarnings("unused")
  void checkTest(final Test test, final boolean element) { }

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
  private Step axisStep(final boolean error) throws QueryException {
    Axis axis = null;
    Test test = null;
    if(wsConsume("..")) {
      axis = Axis.PARENT;
      test = KindTest.NODE;
      checkTest(test, true);
    } else if(consume('@')) {
      axis = Axis.ATTRIBUTE;
      test = nodeTest(NodeType.ATTRIBUTE, true);
      checkTest(test, false);
      if(test == null) {
        --pos;
        throw error(NOATTNAME);
      }
    } else {
      for(final Axis ax : Axis.VALUES) {
        final int p = pos;
        if(wsConsume(ax.name)) {
          if(wsConsumeWs("::")) {
            alterPos = pos;
            axis = ax;
            final boolean element = ax != Axis.ATTRIBUTE;
            test = nodeTest(element ? NodeType.ELEMENT : NodeType.ATTRIBUTE, true);
            checkTest(test, element);
            if(test == null) throw error(AXISMISS_X, axis);
            break;
          }
          pos = p;
        }
      }

      if(axis == null) {
        axis = Axis.CHILD;
        test = nodeTest(NodeType.ELEMENT, true);
        if(test == KindTest.NAMESPACE_NODE) throw error(NSAXIS);
        if(test != null && test.type == NodeType.ATTRIBUTE) axis = Axis.ATTRIBUTE;
        checkTest(test, axis != Axis.ATTRIBUTE);
      }
      if(test == null) {
        if(error) throw error(STEPMISS_X, found());
        return null;
      }
    }

    final ExprList el = new ExprList();
    while(wsConsume("[")) {
      checkPred(true);
      add(el, expr());
      wsCheck("]");
      checkPred(false);
    }
    return new CachedStep(info(), axis, test, el.finish());
  }

  /**
   * Parses the "NodeTest" rule.
   * Parses the "NameTest" rule.
   * Parses the "KindTest" rule.
   * @param type node type (either {@link NodeType#ELEMENT} or {@link NodeType#ATTRIBUTE})
   * @param all check all tests, or only names
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test nodeTest(final NodeType type, final boolean all) throws QueryException {
    int p = pos;
    if(consume('*')) {
      p = pos;
      if(consume(':') && !consume('*')) {
        // name test: *:name
        return new NameTest(new QNm(ncName(QNAME_X)), NamePart.LOCAL, type, sc.elemNS);
      }
      // name test: *
      pos = p;
      return KindTest.get(type);
    }
    if(consume("Q{")) {
      // name test: Q{uri}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) return new NameTest(new QNm(COLON, uri), NamePart.URI, type, sc.elemNS);
    }
    pos = p;

    final InputInfo ii = info();
    QNm name = eQName(SKIPCHECK, null);
    if(name != null) {
      p = pos;
      if(all && wsConsumeWs("(")) {
        final NodeType nt = NodeType.find(name);
        if(nt != null) {
          // kind test
          final Test test = kindTest(nt);
          return test == null ? KindTest.get(nt) : test;
        }
      } else {
        pos = p;
        NamePart part = NamePart.FULL;
        if(!name.hasPrefix() && consume(":*")) {
          // name test: prefix:*
          name = new QNm(concat(name.string(), COLON));
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
    Expr expr = primary(), old;
    if(expr != null) {
      do {
        old = expr;
        if(wsConsume("[")) {
          // parses the "Predicate" rule
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck("]");
          } while(wsConsume("["));
          expr = new CachedFilter(info(), expr, el.finish());
        } else if(curr('(')) {
          // parses the "ArgumentList" rule
          final InputInfo ii = info();
          final FuncArgs args = argumentList(true);
          expr = dynFuncCall(expr, ii, args.exprs(), args.holes());
        } else {
          final int p = pos;
          if(consume("?") && !consume("?") && !consume(':')) {
            // parses the "Lookup" rule
            expr = new Lookup(info(), expr, keySpecifier());
          } else {
            pos = p;
          }
        }
      } while(expr != old);
    }
    return expr;
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
    // function item
    Expr expr = functionItem();
    if(expr != null) return expr;

    final char ch = curr();
    // direct constructor
    if(ch == '<') return dirConstructor();
    // string constructor and template
    if(ch == '`') return stringConstructor();
    // variables
    if(ch == '$') return varRef();
    // parentheses
    if(ch == '(' && next() != '#') return parenthesized();
    // function call
    expr = functionCall();
    if(expr != null) return expr;
    // computed constructors
    expr = compConstructor();
    if(expr != null) return expr;
    // ordered expression
    int p = pos;
    if(wsConsumeWs(ORDERED) || wsConsumeWs(UNORDERED)) {
      if(curr('{')) return enclosedExpr();
      pos = p;
    }
    // map constructor
    if(wsConsumeWs(MAP, "{", INCOMPLETE)) return new CMap(info(), keyValues());
    // curly array constructor
    if(wsConsumeWs(ARRAY, "{", INCOMPLETE)) {
      wsCheck("{");
      final Expr exp = expr();
      wsCheck("}");
      return exp == null ? new CArray(info(), false) : new CArray(info(), false, exp);
    }
    // square array constructor
    if(wsConsume("[")) return new CArray(info(), true, values());
    // unary lookup
    p = pos;
    if(consume("?")) {
      if(!wsConsume(",") && !consume(")")) {
        final InputInfo info = info();
        return new Lookup(info, new ContextValue(info), keySpecifier());
      }
      pos = p;
    }
    // context value
    if(ch == '.') {
      if(next() == '.') return null;
      if(!digit(next())) {
        consume();
        return new ContextValue(info());
      }
    }
    // literals
    return literal();
  }

  /**
   * Parses the "KeySpecifier" rule.
   * @return specifier expression ({@code null} means wildcard)
   * @throws QueryException query exception
   */
  private Expr keySpecifier() throws QueryException {
    if(wsConsume("*")) return Lookup.WILDCARD;
    final char ch = curr();
    if(ch == '(') return parenthesized();
    if(ch == '$') return varRef();
    if(quote(ch)) return Str.get(stringLiteral());
    final Expr num = numericLiteral(ch, Long.MAX_VALUE);
    if(num != null) {
      if(Function.ERROR.is(num) || num instanceof Int) return num;
      throw error(NUMBERITR_X_X, num.seqType(), num);
    }
    return Str.get(ncName(KEYSPEC));
  }

  /**
   * Parses keys and values of maps.
   * @return map literals
   * @throws QueryException query exception
   */
  private Expr[] keyValues() throws QueryException {
    wsCheck("{");
    final ExprList el = new ExprList();
    if(!wsConsume("}")) {
      do {
        add(el, check(single(), INVMAPKEY));
        if(!wsConsume(":")) throw error(WRONGCHAR_X_X, ":", found());
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(","));
      wsCheck("}");
    }
    return el.finish();
  }

  /**
   * Parses values of arrays.
   * @return array literals
   * @throws QueryException query exception
   */
  private Expr[] values() throws QueryException {
    final ExprList el = new ExprList();
    if(!wsConsume("]")) {
      do {
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(","));
      wsCheck("]");
    }
    return el.finish();
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
      final boolean args = wsConsume("("), focus = !args && curr('{');
      if(args || focus) {
        final HashMap<Var, Expr> global = localVars.pushContext(true);
        Params params = null;
        Expr expr = null;
        if(args) {
          params = paramList(false);
          expr = enclosedExpr();
        } else if(focus) {
          // focus function
          final InputInfo ii = info();
          final QNm name = new QNm("arg");
          params = new Params().add(name, SeqType.ITEM_ZM, null, ii).finish(qc, sc, localVars);
          expr = new CachedMap(ii, localVars.resolve(name, ii), enclosedExpr());
        }
        final VarScope vs = localVars.popContext();
        if(anns.contains(Annotation.PRIVATE) || anns.contains(Annotation.PUBLIC))
          throw error(NOVISALLOWED);
        return new Closure(info(), params, expr, anns, global, vs);
      }
    }
    pos = p;

    // annotations not allowed here
    if(!anns.isEmpty()) throw error(NOANN);

    // named function reference
    final QNm name = eQName(sc.funcNS, null);
    if(name != null && wsConsumeWs("#")) {
      checkReserved(name);
      final char ch = curr();
      final Expr num = numericLiteral(ch, Integer.MAX_VALUE);
      if(Function.ERROR.is(num)) return num;
      if(!(num instanceof Int)) throw error(ARITY_X, ch == 0 ? "" : ch);
      final int arity = (int) ((Int) num).itr();
      final Expr expr = Functions.getLiteral(name, arity, qc, sc, info(), false);
      return expr != null ? expr : undeclaredLiteral(name, arity, info());
    }
    pos = p;
    return null;
  }

  /**
   * Creates and registers a function literal.
   * @param name function name
   * @param arity arity
   * @param ii input info
   * @return the literal
   * @throws QueryException query exception
   */
  private Closure undeclaredLiteral(final QNm name, final int arity, final InputInfo ii)
      throws QueryException {
    final Closure expr = Closure.undeclaredLiteral(name, arity, qc, sc, ii);
    qc.functions.registerFuncLiteral(expr);
    return expr;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr literal() throws QueryException {
    final char ch = curr();
    final Expr num = numericLiteral(ch, 0);
    return num != null ? num : quote(ch) ? Str.get(stringLiteral()) : null;
  }

  /**
   * Parses the "NumericLiteral" rule.
   * Parses the "DecimalLiteral" rule.
   * Parses the "IntegerLiteral" rule.
   * @param ch current character
   * @param max maximum value for integers (if 0, parse all numeric types)
   * @return numeric literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr numericLiteral(final char ch, final long max) throws QueryException {
    if(!digit(ch) && ch != '.') return null;
    token.reset();

    int base = 10;
    if(ch == '0') {
      final char n = next();
      if(max == 0) {
        if(n == 'x' || n == 'X') base = 16;
        else if(n == 'b' || n == 'B') base = 2;
        if(base != 10) {
          consume();
          consume();
          if(curr('_')) throw error(NUMBER_X, token.add('_'));
        }
      }
    }

    boolean range = false;
    long l = 0;
    boolean us = false;
    for(char c; (c = curr()) != 0;) {
      if(consume('_')) {
        us = true;
      } else {
        final int n = c <= '9' ? c - 0x30 : (c & 0xDF) - 0x37;
        if(n >= base || !(c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
          break;
        }
        l = l * base + n;
        if(l < 0 || l == 0 && c != '0') range = true;
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
        if(digit(curr())) digits();
        else if(token.size() == 1) throw error(NUMBER_X, token);
      }
      // double value
      if(XMLToken.isNCStartChar(curr())) {
        if(!consume('e') && !consume('E')) throw error(NUMBER_X, token);
        token.add('e');
        if(curr('+') || curr('-')) token.add(consume());
        if(digit(curr())) digits();
        else throw error(NUMBER_X, token);

        if(XMLToken.isNCStartChar(curr())) throw error(NUMBER_X, token);
        return Dbl.get(token.toArray(), info());
      }
      // decimal value
      if(dec) return Dec.get(new BigDecimal(string(token.toArray())));
    }

    // integer value
    if(token.isEmpty()) throw error(NUMBER_X, token);

    return !range && (max == 0 || l <= max) ? Int.get(l) :
      FnError.get(RANGE_X.get(info(), token), SeqType.INTEGER_O, sc);
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
    } while(digit(curr()));
    if(us) throw error(NUMBER_X, token.add('_'));
  }

  /**
   * Parses the "StringLiteral" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] stringLiteral() throws QueryException {
    skipWs();
    final char del = curr();
    if(!quote(del)) throw error(NOQUOTE_X, found());
    consume();
    token.reset();
    while(true) {
      while(!consume(del)) {
        if(!more()) throw error(NOQUOTE_X, found());
        entity(token);
      }
      if(!consume(del)) break;
      token.add(del);
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
      if(!more() || curr() == '{') throw error(WRONGCHAR_X_X, "}", found());
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
    final QNm name = varName();
    final SeqType st = type != null ? type : optAsType();
    return new Var(name, st, qc, sc, ii);
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
    final QNm name = eQName(sc.funcNS, null);
    if(name != null && !reserved(name)) {
      skipWs();
      if(curr('(')) return funcCall(name, info(), argumentList(true));
    }
    pos = p;
    return null;
  }

  /**
   * Returns a function call.
   * @param name function name
   * @param ii input info
   * @param args function arguments
   * @return function call
   * @throws QueryException query exception
   */
  private Expr funcCall(final QNm name, final InputInfo ii, final FuncArgs args)
      throws QueryException {

    final int[] holes = args.holes();
    final Expr[] exprs = args.exprs();
    if(holes == null) return Functions.get(name, exprs, args.keywords(), qc, sc, ii);

    // partial function
    final int arity = exprs.length + holes.length;
    final Expr func = Functions.getLiteral(name, arity, qc, sc, ii, false);
    return dynFuncCall(func == null ? undeclaredLiteral(name, arity, ii) : func, ii, exprs, holes);
  }

  /**
   * Generates a dynamic function call or a partial function application.
   * @param expr function expression
   * @param ii input info
   * @param args arguments
   * @param holes positions of the placeholders
   * @return function call
   */
  private Expr dynFuncCall(final Expr expr, final InputInfo ii, final Expr[] args,
      final int[] holes) {
    return holes == null ? new DynFuncCall(ii, sc, expr, args) :
      new PartFunc(ii, sc, ExprList.concat(args, expr), holes);
  }

  /**
   * Parses the "ArgumentList" rule.
   * @param keywords allow keyword arguments
   * @param exprs arguments
   * @return function arguments
   * @throws QueryException query exception
   */
  private FuncArgs argumentList(final boolean keywords, final Expr... exprs)
      throws QueryException {
    final FuncArgs args  = new FuncArgs(exprs);
    wsCheck("(");
    if(!wsConsumeWs(")")) {
      boolean kw = false;
      do {
        final int p = pos;
        if(keywords) {
          final QNm name = eQName(null, null);
          if(name != null && wsConsume(":=")) {
            final Expr expr = single();
            if(expr == null) throw error(FUNCARG_X, found());
            if(args.add(name, expr)) throw error(KEYWORDTWICE_X, name);
            kw = true;
          } else {
            pos = p;
          }
        }
        if(p == pos && !kw) {
          final Expr expr = single();
          if(expr != null) {
            args.add(expr);
          } else if(wsConsume("?")) {
            args.add(null);
          } else {
            throw error(FUNCARG_X, found());
          }
        }
      } while(wsConsumeWs(","));
      if(!consume(")")) throw error(FUNCARG_X, found());
    }
    return args;
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
        if(expr != null) el.add(Function.STRING_JOIN.get(sc, info(), expr, Str.SPACE));
        skipWs();
        check('}');
        if(constr) check('`');
      } else {
        // fixed part
        pos = p;
        final char ch = consume();
        if(!constr && (ch == '{' || ch == '}' || ch == '`')) check(ch);
        tb.add(ch);
      }
    }
    throw error(INCOMPLETE);
  }

  /**
   * Parses the "DirectConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirConstructor() throws QueryException {
    check('<');
    return consume('!') ? dirComment() : consume('?') ? dirPI() : dirElement();
  }

  /**
   * Parses the "DirElemConstructor" rule.
   * Parses the "DirAttributeList" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElement() throws QueryException {
    // cache namespace information
    final int size = sc.ns.size();
    final byte[] nse = sc.elemNS;
    final int npos = qnames.size();

    final InputInfo ii = info();
    final QNm name = new QNm(qName(ELEMNAME_X));
    qnames.add(name, ii);
    consumeWS();

    final Atts ns = new Atts();
    final ExprList cont = new ExprList();

    // parse attributes...
    boolean xmlDecl = false; // xml prefix explicitly declared?
    ArrayList<QNm> atts = null;
    while(true) {
      final byte[] atn = qName(null);
      if(atn.length == 0) break;

      final ExprList attv = new ExprList();
      consumeWS();
      check('=');
      consumeWS();
      final char delim = consume();
      if(!quote(delim)) throw error(NOQUOTE_X, found());
      final TokenBuilder tb = new TokenBuilder();

      boolean simple = true;
      while(true) {
        while(!consume(delim)) {
          final char ch = curr();
          switch(ch) {
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
        add(cont, new CAttr(sc, info(), false, attn, attv.finish()));
      }
      if(!consumeWS()) break;
    }

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(curr() != '<' || next() != '/') {
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
    return new CElem(sc, info(), false, name, ns, cont.finish());
  }

  /**
   * Parses the "DirElemContent" rule.
   * @param name name of opening element
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElemContent(final byte[] name) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    boolean strip = true;
    while(true) {
      final char ch = curr();
      if(ch == '<') {
        if(wsConsume("<![CDATA[")) {
          tb.add(cDataSection());
          strip = false;
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : next() == '/' ? null : dirConstructor();
        }
      } else if(ch == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : enclosedExpr();
        }
      } else if(ch == '}') {
        consume();
        check('}');
        tb.add('}');
      } else if(ch != 0) {
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
      final char ch = consumeContent();
      if(ch == '-' && consume('-')) {
        check('>');
        return new CComm(sc, info(), false, Str.get(tb.finish()));
      }
      tb.add(ch);
    }
  }

  /**
   * Parses the "DirPIConstructor" rule.
   * Parses the "DirPIContents" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirPI() throws QueryException {
    final byte[] str = ncName(INVALPI);
    if(eq(lc(str), XML)) throw error(PIXML_X, str);

    final boolean space = skipWs();
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final char ch = consumeContent();
      if(ch == '?' && consume('>')) {
        return new CPI(sc, info(), false, Str.get(str), Str.get(tb.finish()));
      }
      if(!space) throw error(PIWRONG);
      tb.add(ch);
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
      final char ch = consumeContent();
      if(ch == ']' && curr(']') && next() == '>') {
        pos += 2;
        return tb.finish();
      }
      tb.add(ch);
    }
  }

  /**
   * Parses the "ComputedConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compConstructor() throws QueryException {
    final int p = pos;
    if(wsConsumeWs(DOCUMENT))  return consume(compDoc(), p);
    if(wsConsumeWs(ELEMENT))   return consume(compElement(), p);
    if(wsConsumeWs(ATTRIBUTE)) return consume(compAttribute(), p);
    if(wsConsumeWs(NAMESPACE)) return consume(compNamespace(), p);
    if(wsConsumeWs(TEXT))      return consume(compText(), p);
    if(wsConsumeWs(COMMENT))   return consume(compComment(), p);
    if(wsConsumeWs(PROCESSING_INSTRUCTION))        return consume(compPI(), p);
    return null;
  }

  /**
   * Consumes the specified expression or resets the query position.
   * @param expr expression
   * @param p query position
   * @return expression or {@code null}
   */
  private Expr consume(final Expr expr, final int p) {
    if(expr == null) pos = p;
    return expr;
  }

  /**
   * Parses the "CompDocConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compDoc() throws QueryException {
    return curr('{') ? new CDoc(sc, info(), false, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompElemConstructor" rule.
   * Parses the "ContextExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compElement() throws QueryException {
    skipWs();

    final Expr name;
    final InputInfo ii = info();
    final QNm qnm = eQName(SKIPCHECK, null);
    if(qnm != null) {
      name = qnm;
      qnames.add(qnm, ii);
    } else {
      if(!wsConsume("{")) return null;
      name = check(expr(), NOELEMNAME);
      wsCheck("}");
    }

    skipWs();
    return curr('{') ? new CElem(sc, info(), true, name, new Atts(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompAttrConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    skipWs();

    final Expr name;
    final InputInfo ii = info();
    final QNm qnm = eQName(SKIPCHECK, null);
    if(qnm != null) {
      name = qnm;
      qnames.add(qnm, false, ii);
    } else {
      if(!wsConsume("{")) return null;
      name = check(expr(), NOATTNAME);
      wsCheck("}");
    }

    skipWs();
    return curr('{') ? new CAttr(sc, info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompNamespaceConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compNamespace() throws QueryException {
    skipWs();

    final Expr name;
    final byte[] str = ncName(null);
    if(str.length == 0) {
      if(!curr('{')) return null;
      name = enclosedExpr();
    } else {
      name = Str.get(str);
    }
    skipWs();
    return curr('{') ? new CNSpace(sc, info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompTextConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compText() throws QueryException {
    return curr('{') ? new CTxt(sc, info(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompCommentConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compComment() throws QueryException {
    return curr('{') ? new CComm(sc, info(), true, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompPIConstructor" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr compPI() throws QueryException {
    skipWs();

    final Expr name;
    final byte[] str = ncName(null);
    if(str.length == 0) {
      if(!wsConsume("{")) return null;
      name = check(expr(), PIWRONG);
      wsCheck("}");
    } else {
      name = Str.get(str);
    }

    skipWs();
    return curr('{') ? new CPI(sc, info(), true, name, enclosedExpr()) : null;
  }

  /**
   * Parses the "SimpleType" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType simpleType() throws QueryException {
    skipWs();
    final QNm name = eQName(sc.elemNS, TYPEINVALID);
    Type type = ListType.find(name);
    if(type == null) {
      type = AtomType.find(name, false);
      if(consume("(")) throw error(SIMPLETYPE_X, name.prefixId(XML));
      if(type == null ? name.eq(AtomType.ANY_SIMPLE_TYPE.qname()) :
        type.oneOf(AtomType.ANY_ATOMIC_TYPE, AtomType.NOTATION))
        throw error(INVALIDCAST_X, name.prefixId(XML));
      if(type == null)
        throw error(WHICHCAST_X, AtomType.similar(name));
    }
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
    if(wsConsumeWs(EMPTY_SEQUENCE, "(", null)) {
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
    // parenthesized item type
    if(wsConsume("(")) {
      final SeqType st = itemType();
      wsCheck(")");
      return st;
    }

    // parse annotations and type name
    final AnnList anns = annotations(false).check(false, false);
    skipWs();
    final QNm name = eQName(null, TYPEINVALID);

    // parse type
    SeqType st = null;
    Type type;
    if(wsConsume("(")) {
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
      if(type == null) throw error(WHICHTYPE_X, FuncType.similar(name));
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(sc.elemNS);
      // atomic type
      type = AtomType.find(name, false);
      // no type found
      if(type == null) throw error(TYPEUNKNOWN_X, AtomType.similar(name));
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

    // map
    if(type instanceof MapType) {
      final Type key = itemType().type;
      if(!key.instanceOf(AtomType.ANY_ATOMIC_TYPE)) throw error(MAPTAAT_X, key);
      wsCheck(",");
      final MapType tp = MapType.get((AtomType) key, sequenceType());
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
      do args = Array.add(args, sequenceType());
      while(wsConsume(","));
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
    final Test tp;
    switch(type) {
      case DOCUMENT_NODE: tp = documentTest(); break;
      case ELEMENT:
      case ATTRIBUTE: tp = elemAttrTest(type); break;
      case PROCESSING_INSTRUCTION: tp = piTest(); break;
      case SCHEMA_ELEMENT:
      case SCHEMA_ATTRIBUTE: tp = schemaTest(); break;
      default: tp = null; break;
    }
    wsCheck(")");
    return tp;
  }

  /**
   * Parses the "DocumentTest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test documentTest() throws QueryException {
    final boolean elem = consume(ELEMENT);
    if(!elem && !consume(SCHEMA_ELEMENT)) return null;

    wsCheck("(");
    skipWs();
    final Test test = elem ? elemAttrTest(NodeType.ELEMENT) : schemaTest();
    wsCheck(")");
    return new DocTest(test != null ? test : KindTest.ELEMENT);
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
    final Test test = nodeTest(type, false);
    if(test != null && wsConsumeWs(",")) {
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
    return test;
  }

  /**
   * Parses the "PITest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test piTest() throws QueryException {
    token.reset();
    final byte[] name;
    if(quote(curr())) {
      name = trim(stringLiteral());
      if(!XMLToken.isNCName(name)) throw error(INVNCNAME_X, name);
    } else if(ncName()) {
      name = token.toArray();
    } else {
      return null;
    }
    return Test.get(NodeType.PROCESSING_INSTRUCTION, new QNm(name), null);
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!wsConsumeWs(TRY)) return null;

    final Expr expr = enclosedExpr();
    wsCheck(CATCH);

    Catch[] catches = { };
    do {
      NameTest[] codes = { };
      do {
        skipWs();
        final Test test = nodeTest(NodeType.ELEMENT, false);
        if(test == null) throw error(NOCATCH);
        codes = Array.add(codes, test instanceof NameTest ? (NameTest) test : null);
      } while(wsConsume("|"));

      final int s = localVars.openScope();
      final int cl = Catch.QNAMES.length;
      final Var[] vs = new Var[cl];
      final InputInfo ii = info();
      for(int c = 0; c < cl; c++) {
        vs[c] = localVars.add(new Var(Catch.QNAMES[c], Catch.TYPES[c], qc, sc, ii));
      }
      final Catch c = new Catch(ii, codes, vs);
      c.expr = enclosedExpr();
      localVars.closeScope(s);

      catches = Array.add(catches, c);
    } while(wsConsumeWs(CATCH));

    return new Try(info(), expr, catches);
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
    if(quote(curr())) {
      e = Str.get(stringLiteral());
    } else if(curr('{')) {
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
    final Expr[] occ = { Int.ZERO, Int.MAX };
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
    while(digit(curr())) token.add(consume());
    if(token.isEmpty()) throw error(INTEXP);
    return Int.get(toLong(token.toArray()));
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
        if(digit(curr())) {
          opt.errors = (int) ((ANum) ftAdditive(true)).itr();
          wsCheck(ERRORS);
        }
      } else {
        throw error(FTMATCH_X, consume());
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
    return new Insert(sc, info(), s, mode, trg);
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
    return new Delete(sc, info(), check(single(), INCOMPLETE));
  }

  /**
   * Parses the "RenameExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr rename() throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(RENAME) || !wsConsumeWs(NODE)) {
      pos = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(AS);
    final Expr n = check(single(), INCOMPLETE);
    qc.updating();
    return new Rename(sc, info(), trg, n);
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
      wsCheck(NODE);
    } else if(!wsConsumeWs(NODE)) {
      pos = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(WITH);
    final Expr src = check(single(), INCOMPLETE);
    qc.updating();
    return new Replace(sc, info(), trg, src, value);
  }

  /**
   * Parses the "CopyModifyExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr copyModify() throws QueryException {
    if(!wsConsumeWs(COPY, "$", INCOMPLETE)) return null;

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
    final boolean upd = wsConsumeWs(UPDATING), ndt = wsConsumeWs(NON_DETERMINISTIC);
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
        return new DynFuncCall(ii, sc, upd, ndt, func, argList.finish());
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "NCName" rule.
   * @param error optional error message
   * @return string
   * @throws QueryException query exception
   */
  private byte[] ncName(final QueryError error) throws QueryException {
    token.reset();
    if(ncName()) return token.toArray();
    if(error != null) {
      final char ch = consume();
      throw error(error, ch == 0 ? "" : ch);
    }
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
    final int p = pos;
    if(consume("Q{")) {
      final byte[] uri = bracedURILiteral(), name = ncName(null);
      if(name.length != 0) return new QNm(name, uri);
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
      if(error != null) {
        final char ch = consume();
        throw error(error, ch == 0 ? "" : ch);
      }
    } else if(consume(':')) {
      if(XMLToken.isNCStartChar(curr())) {
        token.add(':');
        do {
          token.add(consume());
        } while(XMLToken.isNCChar(curr()));
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
    if(!XMLToken.isNCStartChar(curr())) return false;
    do token.add(consume()); while(XMLToken.isNCChar(curr()));
    return true;
  }

  /**
   * Parses and converts entities.
   * @param tb token builder
   * @return true if an entity was found
   * @throws QueryException query exception
   */
  private boolean entity(final TokenBuilder tb) throws QueryException {
    final int i = pos;
    final boolean ent = consume('&');
    if(ent) {
      if(consume('#')) {
        final int b = consume('x') ? 0x10 : 10;
        boolean ok = true;
        int n = 0;
        do {
          final char ch = curr();
          final boolean m = digit(ch);
          final boolean h = b == 0x10 && (ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F');
          if(!m && !h) entityError(i, INVENTITY_X);
          final long nn = n;
          n = n * b + (consume() & 0xF);
          if(n < nn) ok = false;
          if(!m) n += 9;
        } while(!consume(';'));
        if(!ok) entityError(i, INVCHARREF_X);
        if(!XMLToken.valid(n)) entityError(i, INVCHARREF_X);
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
          entityError(i, INVENTITY_X);
        }
        if(!consume(';')) entityError(i, INVENTITY_X);
      }
    } else {
      final char ch = consume();
      int cp = ch;
      if(cp == '\r') {
        cp = '\n';
        if(curr(cp)) consume();
      } else if(Character.isHighSurrogate(ch) && curr() != 0 && Character.isLowSurrogate(curr())) {
        cp = Character.toCodePoint(ch, consume());
      }
      tb.add(cp);
    }
    return ent;
  }

  /**
   * Raises an entity error.
   * @param start start position
   * @param code error code
   * @throws QueryException query exception
   */
  private void entityError(final int start, final QueryError code) throws QueryException {
    final String sub = input.substring(start, Math.min(start + 20, length));
    final int semi = sub.indexOf(';');
    final String ent = semi == -1 ? sub + DOTS : sub.substring(0, semi + 1);
    throw error(code, ent);
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
   * Skips whitespaces, raises an error if the specified string cannot be consumed.
   * @param string expected string
   * @throws QueryException query exception
   */
  private void wsCheck(final String string) throws QueryException {
    if(!wsConsume(string)) throw error(WRONGCHAR_X_X, string, found());
  }

  /**
   * Consumes the next character and normalizes new line characters.
   * @return next character
   * @throws QueryException query exception
   */
  private char consumeContent() throws QueryException {
    char ch = consume();
    if(ch == 0) throw error(NOCONTENT);
    if(ch == '\r') {
      ch = '\n';
      consume('\n');
    }
    return ch;
  }

  /**
   * Consumes the specified string and surrounding whitespaces.
   * @param string string to consume (words must not be followed by letters)
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String string) throws QueryException {
    final int p = pos;
    if(wsConsume(string)) {
      if(skipWs() || !XMLToken.isNCStartChar(string.charAt(0)) || !XMLToken.isNCChar(curr()))
        return true;
      pos = p;
    }
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position. If the strings are
   * found, the cursor is placed after the first token.
   * @param string1 string to consume (words must not be followed by letters)
   * @param string2 second string
   * @param expr alternative error message (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String string1, final String string2, final QueryError expr)
      throws QueryException {

    final int p1 = pos;
    if(!wsConsumeWs(string1)) return false;
    final int p2 = pos;
    alter = expr;
    alterPos = p2;
    final boolean ok = wsConsume(string2);
    pos = ok ? p2 : p1;
    return ok;
  }

  /**
   * Skips whitespaces, consumes the specified string and ignores trailing characters.
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
   * @return true if whitespaces were found
   * @throws QueryException query exception
   */
  private boolean skipWs() throws QueryException {
    final int i = pos;
    while(more()) {
      final char ch = curr();
      if(ch == '(' && next() == ':') {
        comment();
      } else {
        if(ch == 0 || ch > ' ') return i != pos;
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
      docBuilder.setLength(0);
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
      char curr = curr();
      if(curr == '(' && next() == ':') {
        ++pos;
        comment(true, xqdoc);
        curr = curr();
      }
      if(curr == ':' && next() == ')') {
        pos += 2;
        if(!nested && moduleDoc.isEmpty()) {
          moduleDoc = docBuilder.toString().trim();
          docBuilder.setLength(0);
        }
        return;
      }
      if(xqdoc) docBuilder.append(curr);
    }
    throw error(COMCLOSE);
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespaces were found
   */
  private boolean consumeWS() {
    final int i = pos;
    while(more()) {
      final char ch = curr();
      if(ch == 0 || ch > ' ') return i != pos;
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
   * @param ii input info
   * @param arg error arguments
   * @return error
   */
  public QueryException error(final QueryError error, final InputInfo ii, final Object... arg) {
    return error.get(ii, arg);
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
}
