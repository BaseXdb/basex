package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.QueryText.DOLLAR;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.math.*;
import java.util.*;

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
import org.basex.util.options.*;

/**
 * Parser for XQuery expressions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** QName check: URI is mandatory. */
  private static final byte[] URICHECK = {};
  /** QName check: skip namespace check. */
  private static final byte[] SKIPCHECK = {};
  /** Reserved function names. */
  private static final TokenSet KEYWORDS = new TokenSet();
  /** Decimal declarations. */
  private static final byte[][] DECFORMATS = tokens(
    DF_DEC, DF_DIG, DF_GRP, DF_EXP, DF_INF, DF_MIN, DF_NAN, DF_PAT, DF_PC, DF_PM, DF_ZD
  );

  static {
    final byte[][] keys = {
      NodeType.ATTRIBUTE.qname().string(), NodeType.COMMENT.qname().string(),
      NodeType.DOCUMENT_NODE.qname().string(), NodeType.ELEMENT.qname().string(),
      NodeType.NAMESPACE_NODE.qname().string(), NodeType.NODE.qname().string(),
      NodeType.PROCESSING_INSTRUCTION.qname().string(), NodeType.TEXT.qname().string(),
      NodeType.SCHEMA_ATTRIBUTE.qname().string(), NodeType.SCHEMA_ELEMENT.qname().string(),

      ArrayType.ARRAY, FuncType.FUNCTION, MapType.MAP,
      AtomType.ITEM.qname().string(),

      token(EMPTY_SEQUENCE), token(IF), token(SWITCH), token(TYPESWITCH)
    };
    for(final byte[] key : keys) KEYWORDS.add(key);
  }

  /** Modules loaded by the current file. */
  public final TokenSet modules = new TokenSet();
  /** Namespaces. */
  public final TokenMap namespaces = new TokenMap();

  /** Query context. */
  public final QueryContext qc;
  /** Static context. */
  public final StaticContext sc;

  /** Imported modules. */
  private final TokenSet imports = new TokenSet();
  /** List of modules to be parsed. */
  private final ArrayList<ModInfo> mods = new ArrayList<>();

  /** Parsed variables. */
  private final TokenObjMap<StaticVar> vars = new TokenObjMap<>();
  /** Parsed functions. */
  private final TokenObjMap<StaticFunc> funcs = new TokenObjMap<>();

  /** Temporary token cache. */
  private final TokenBuilder token = new TokenBuilder();
  /** Current XQDoc string. */
  private final StringBuilder currDoc = new StringBuilder();
  /** XQDoc string of module. */
  private String doc = "";

  /** Alternative error. */
  private QueryError alter;
  /** Alternative position. */
  private int alterPos;

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<>();
  /** QName cache. */
  private final QNmCache qnames = new QNmCache();
  /** Local variable. */
  private final LocalVars localVars = new LocalVars(this);

  /**
   * Constructor.
   * @param query query string
   * @param uri base URI (can be {@code null}; only passed on if not bound to static context yet)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   */
  public QueryParser(final String query, final String uri, final QueryContext qctx,
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
   * @return resulting root expression
   * @throws QueryException query exception
   */
  public final MainModule parseMain() throws QueryException {
    init();
    try {
      versionDecl();

      final int p = pos;
      if(wsConsumeWs(MODULE, NAMESPACE, null)) throw error(MAINMOD);
      pos = p;

      prolog1();
      importModules();
      prolog2();

      localVars.pushContext(null);
      final Expr ex = expr();
      if(ex == null) throw alterError(EXPREMPTY);
      final VarScope vs = localVars.popContext();

      final MainModule mm = new MainModule(vs, ex, null, doc, null, funcs, vars, imports);
      finish(mm);
      check(mm);
      return mm;
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses a library module.
   * Parses the "ModuleDecl" rule.
   * @param check check functions and variables
   * @return name of the module
   * @throws QueryException query exception
   */
  public final LibraryModule parseLibrary(final boolean check) throws QueryException {
    init();
    try {
      versionDecl();

      wsCheck(MODULE);
      wsCheck(NAMESPACE);
      skipWs();
      final byte[] pref = ncName(NONAME_X);
      wsCheck(IS);
      final byte[] uri = stringLiteral();
      if(uri.length == 0) throw error(NSMODURI);

      sc.module = new QNm(pref, uri);
      sc.ns.add(pref, uri, info());
      namespaces.put(pref, uri);
      wsCheck(SEMICOL);

      // get absolute path
      final IO baseO = sc.baseIO();
      final byte[] path = token(baseO == null ? "" : baseO.path());
      qc.modParsed.put(path, uri);
      qc.modStack.push(path);

      prolog1();
      importModules();
      prolog2();
      finish(null);
      if(check) check(null);

      qc.modStack.pop();
      return new LibraryModule(doc, funcs, vars, imports, sc);
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
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
   * @param mm main module; {@code null} for library modules
   * @throws QueryException query exception
   */
  private void check(final MainModule mm) throws QueryException {
    // check function calls and variable references
    qc.funcs.check(qc);
    qc.vars.check();

    // check updating semantics (skip if updates and values can be mixed)
    if(qc.updating && !sc.mixUpdates) {
      qc.funcs.checkUp();
      qc.vars.checkUp();
      if(mm != null) mm.expr.checkUp();
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
      if(!ver.equals(XQ10) && !Strings.eq(ver, XQ11, XQ30, XQ31)) throw error(XQUERYVER_X, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if(wsConsumeWs(ENCODING)) {
      final String encoding = string(stringLiteral());
      if(!Strings.supported(encoding)) throw error(XQUERYENC2_X, encoding);
    } else if(!version) {
      pos = p;
      return;
    }
    wsCheck(SEMICOL);
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
      currDoc.setLength(0);
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
      currDoc.setLength(0);
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
        ann = new Ann(info(), Annotation.UPDATING);
      } else if(consume('%')) {
        skipWs();
        final InputInfo ii = info();
        final QNm name = eQName(QNAME_X, XQ_URI);

        final ItemList items = new ItemList();
        if(wsConsumeWs(PAREN1)) {
          do {
            final Expr ex = literal();
            if(!(ex instanceof Item)) {
              if(Function.ERROR.is(ex)) ex.item(qc, ii);
              throw error(ANNVALUE);
            }
            items.add((Item) ex);
          } while(wsConsumeWs(COMMA));
          wsCheck(PAREN2);
        }
        skipWs();

        final Annotation sig = Annotation.get(name);
        // check if annotation is a pre-defined one
        if(sig == null) {
          // reject unknown annotations with pre-defined namespaces, ignore others
          final byte[] uri = name.uri();
          if(NSGlobal.prefix(uri).length != 0 && !eq(uri, LOCAL_URI, ERROR_URI)) {
            throw (NSGlobal.reserved(uri) ? ANNWHICH_X_X : BASEX_ANNOTATION1_X_X).get(
                ii, '%', name.string());
          }
          ann = new Ann(ii, name, items.finish());

        } else {
          // check if annotation is specified more than once
          if(sig.single && anns.contains(sig)) throw BASEX_ANNOTATION3_X_X.get(ii, '%', sig.id());

          final long arity = items.size();
          if(arity < sig.minMax[0] || arity > sig.minMax[1])
            throw BASEX_ANNOTATION2_X_X.get(ii, sig, arguments(arity));
          final int al = sig.args.length;
          for(int a = 0; a < arity; a++) {
            final SeqType st = sig.args[Math.min(al - 1, a)];
            final Item item = items.get(a);
            if(!st.instance(item)) throw BASEX_ANNOTATION_X_X_X.get(ii, sig, st, item.seqType());
          }
          ann = new Ann(ii, sig, items.finish());
        }
      } else {
        break;
      }

      anns.add(ann);
      if(ann.sig == Annotation.UPDATING) qc.updating();
    }
    skipWs();
    return anns;
  }

  /**
   * Parses the "NamespaceDecl" rule.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final byte[] pref = ncName(NONAME_X);
    wsCheck(IS);
    final byte[] uri = stringLiteral();
    if(sc.ns.staticURI(pref) != null) throw error(DUPLNSDECL_X, pref);
    sc.ns.add(pref, uri, info());
    namespaces.put(pref, uri);
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
    final QNm qname = eQName(QNAME_X, XQ_URI);
    final byte[] value = stringLiteral();
    final String name = string(qname.local());

    if(eq(qname.uri(), OUTPUT_URI)) {
      // output declaration
      if(sc.module != null) throw error(OPTDECL_X, qname.string());

      final SerializerOptions sopts = qc.serParams();
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
    wsCheck(COMMA);
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
    final QNm name = def ? QNm.EMPTY : eQName(QNAME_X, null);

    // check if format has already been declared
    if(sc.decFormats.get(name.id()) != null) throw error(DECDUPL);

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
        wsCheck(IS);
        map.put(s, stringLiteral());
        break;
      }
    } while(n != map.size());

    // completes the format declaration
    sc.decFormats.put(name.id(), new DecFormatter(map, info()));
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
    byte[] pref = null;
    if(wsConsumeWs(NAMESPACE)) {
      pref = ncName(NONAME_X);
      if(eq(pref, XML, XMLNS)) throw error(BINDXML_X, pref);
      wsCheck(IS);
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NAMESPACE);
    }
    byte[] ns = stringLiteral();
    if(pref != null && ns.length == 0) throw error(NSEMPTY);
    if(!Uri.uri(ns).isValid()) throw error(INVURI_X, ns);
    if(wsConsumeWs(AT)) {
      do {
        ns = stringLiteral();
        if(!Uri.uri(ns).isValid()) throw error(INVURI_X, ns);
      } while(wsConsumeWs(COMMA));
    }
    throw error(IMPLSCHEMA);
  }

  /**
   * Parses the "ModuleImport" rule.
   * @throws QueryException query exception
   */
  private void moduleImport() throws QueryException {
    byte[] pref = EMPTY;
    if(wsConsumeWs(NAMESPACE)) {
      pref = ncName(NONAME_X);
      wsCheck(IS);
    }

    final byte[] uri = trim(stringLiteral());
    if(uri.length == 0) throw error(NSMODURI);
    if(!Uri.uri(uri).isValid()) throw error(INVURI_X, uri);
    if(modules.contains(uri)) throw error(DUPLMODULE_X, uri);
    modules.add(uri);

    // add non-default namespace
    if(pref != EMPTY) {
      if(sc.ns.staticURI(pref) != null) throw error(DUPLNSDECL_X, pref);
      sc.ns.add(pref, uri, info());
      namespaces.put(pref, uri);
    }

    final ModInfo mi = new ModInfo();
    mi.info = info();
    mi.uri = uri;
    mods.add(mi);

    // check modules at specified locations
    if(wsConsumeWs(AT)) {
      do mi.paths.add(stringLiteral()); while(wsConsumeWs(COMMA));
    } else {
      // check module files that have been pre-declared by a test API
      final byte[] path = qc.modDeclared.get(uri);
      if(path != null) mi.paths.add(path);
    }
  }

  /**
   * Imports all modules parsed in the prolog.
   * @throws QueryException query exception
   */
  private void importModules() throws QueryException {
    for(final ModInfo mi : mods) importModule(mi);
  }

  /**
   * Imports a single module.
   * @param mi module import
   * @throws QueryException query exception
   */
  private void importModule(final ModInfo mi) throws QueryException {
    final byte[] uri = mi.uri;
    if(mi.paths.isEmpty()) {
      // no paths specified: skip statically available modules
      if(Functions.staticURI(uri)) return;
      // try to resolve module uri
      if(qc.resources.modules().addImport(string(uri), this, mi.info)) return;
      // module not found
      throw WHICHMOD_X.get(mi.info, uri);
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
      if(!eq(tUri, pUri)) throw WRONGMODULE_X_X_X.get(ii, io.name(), uri, pUri);
      return;
    }
    qc.modParsed.put(tPath, tUri);
    imports.put(tUri);

    // read module
    final String qu;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      Util.debug(ex);
      throw error(WHICHMODFILE_X, io);
    }

    qc.modStack.push(tPath);
    final QueryParser qp = new QueryParser(qu, io.path(), qc, null);

    // check if import and declaration uri match
    final LibraryModule lib = qp.parseLibrary(false);
    final byte[] muri = lib.sc.module.uri();
    if(!uri.equals(string(muri))) throw WRONGMODULE_X_X_X.get(ii, io.name(), uri, muri);

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

    if(!wsConsumeWs(EXTERNAL)) wsCheck(ASSIGN);
    else if(!wsConsumeWs(ASSIGN)) return;

    localVars.pushContext(null);
    final Expr ex = check(single(), NOCIDECL);
    final SeqType st = sc.contextType != null ? sc.contextType : SeqType.ITEM_O;
    final VarScope vs = localVars.popContext();
    qc.ctxItem = MainModule.get(vs, ex, st, currDoc.toString(), info());

    if(sc.module != null) throw error(DECITEM);
    if(!sc.mixUpdates && ex.has(Flag.UPD)) throw error(UPCTX, ex);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void varDecl(final AnnList anns) throws QueryException {
    final Var var = newVar();
    if(sc.module != null && !eq(var.name.uri(), sc.module.uri())) throw error(MODULENS_X, var);

    localVars.pushContext(null);
    final boolean ext = wsConsumeWs(EXTERNAL);
    final Expr bind;
    if(ext) {
      bind = wsConsumeWs(ASSIGN) ? check(single(), NOVARDECL) : null;
    } else {
      wsCheck(ASSIGN);
      bind = check(single(), NOVARDECL);
    }
    final VarScope vs = localVars.popContext();
    final StaticVar sv = qc.vars.declare(var, anns, bind, ext, currDoc.toString(), vs);
    vars.put(sv.id(), sv);
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
    final QNm name = eQName(FUNCNAME, sc.funcNS);
    if(keyword(name)) throw error(RESERVED_X, name.local());
    wsCheck(PAREN1);
    if(sc.module != null && !eq(name.uri(), sc.module.uri())) throw error(MODULENS_X, name);

    localVars.pushContext(null);
    final Var[] args = paramList();
    wsCheck(PAREN2);

    final SeqType type = optAsType();
    final Expr ex = wsConsumeWs(EXTERNAL) ? null : enclosedExpr();
    final VarScope vs = localVars.popContext();
    final String cd = currDoc.toString();
    final StaticFunc func = qc.funcs.declare(anns, name, args, type, ex, cd, vs, ii);
    funcs.put(func.id(), func);
  }

  /**
   * Checks if the specified name equals reserved function names.
   * @param name name to be checked
   * @return result of check
   */
  private static boolean keyword(final QNm name) {
    return !name.hasPrefix() && KEYWORDS.contains(name.string());
  }

  /**
   * Parses a ParamList.
   * @return declared variables
   * @throws QueryException query exception
   */
  private Var[] paramList() throws QueryException {
    Var[] params = { };
    while(true) {
      skipWs();
      if(curr() != '$' && params.length == 0) break;
      final InputInfo ii = info();
      final Var var = localVars.add(new Var(varName(), optAsType(), true, qc, sc, ii));
      for(final Var param : params) {
        if(param.name.eq(var.name)) throw error(FUNCDUPL_X, var);
      }
      params = Array.add(params, var);
      if(!consume(',')) break;
    }
    return params;
  }

  /**
   * Parses the "EnclosedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr enclosedExpr() throws QueryException {
    wsCheck(CURLY1);
    final Expr ex = expr();
    wsCheck(CURLY2);
    return ex == null ? Empty.VALUE : ex;
  }

  /**
   * Parses the "Expr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr expr() throws QueryException {
    final Expr ex = single();
    if(ex == null) {
      if(more()) return null;
      throw alterError(NOEXPR);
    }

    if(!wsConsume(COMMA)) return ex;
    final ExprList el = new ExprList(ex);
    do add(el, single()); while(wsConsume(COMMA));
    return new List(info(), el.finish());
  }

  /**
   * Parses the "ExprSingle" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr single() throws QueryException {
    alter = null;
    Expr ex = flwor();
    if(ex == null) ex = quantified();
    if(ex == null) ex = switchh();
    if(ex == null) ex = typeswitch();
    if(ex == null) ex = iff();
    if(ex == null) ex = tryCatch();
    if(ex == null) ex = insert();
    if(ex == null) ex = delete();
    if(ex == null) ex = rename();
    if(ex == null) ex = replace();
    if(ex == null) ex = updatingFunctionCall();
    if(ex == null) ex = copyModify();
    if(ex == null) ex = ternaryIf();
    return ex;
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
      for(final Var var : fl.vars()) curr.put(var.name.id(), var);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(final Clause clause : clauses) {
          for(final Var var : clause.vars()) curr.put(var.name.id(), var);
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
        for(final GroupSpec spec : specs) curr.put(spec.var.name.id(), spec.var);
        VARS:
        for(final Var var : curr.values()) {
          for(final GroupSpec spec : specs) {
            if(spec.var.is(var)) continue VARS;
          }
          ng.add(new VarRef(specs[0].info, var));
        }

        // add new copies for all non-grouping variables
        final int ns = ng.size();
        final Var[] ngrp = new Var[ns];
        for(int i = ns; --i >= 0;) {
          final VarRef ref = ng.get(i);
          // if one groups variables such as $x as xs:integer, then the resulting
          // sequence isn't compatible with the type and can't be assigned
          final Var nv = localVars.add(new Var(ref.var.name, null, false, qc, sc, ref.var.info));
          ngrp[i] = nv;
          curr.put(nv.name.id(), nv);
        }
        clauses.add(new GroupBy(specs, ng.toArray(new VarRef[0]), ngrp, specs[0].info));
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
        } while(wsConsume(COMMA));

        final VarRef[] vs = new VarRef[curr.size()];
        int i = 0;
        for(final Var var : curr.values()) vs[i++] = new VarRef(keys[0].info, var);
        clauses.add(new OrderBy(vs, keys, keys[0].info));
      }

      if(wsConsumeWs(COUNT, DOLLAR, NOCOUNT)) {
        final Var var = localVars.add(newVar(SeqType.INTEGER_O));
        curr.put(var.name.id(), var);
        clauses.add(new Count(var));
      }
    } while(size < clauses.size());

    if(!wsConsumeWs(RETURN)) throw alterError(FLWORRETURN);

    final Expr rtrn = check(single(), NORETURN);
    localVars.closeScope(s);

    return new GFLWOR(clauses.peek().info, clauses, rtrn);
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
      final boolean let = wsConsumeWs(LET, SCORE, NOLET) || wsConsumeWs(LET, DOLLAR, NOLET);
      if(let || wsConsumeWs(FOR, DOLLAR, NOFOR)) {
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
      final Expr ex = check(single(), NOVARDECL);
      // declare late because otherwise it would shadow the wrong variables
      clauses.add(new For(localVars.add(var), localVars.add(at), localVars.add(score), ex, emp));
    } while(wsConsumeWs(COMMA));
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
      wsCheck(ASSIGN);
      final Expr ex = check(single(), NOVARDECL);
      clauses.add(new Let(localVars.add(var), ex, score));
    } while(wsConsume(COMMA));
  }

  /**
   * Parses the "TumblingWindowClause" rule.
   * Parses the "SlidingWindowClause" rule.
   * @param slide sliding window flag
   * @return the window clause
   * @throws QueryException parse exception
   */
  private Window windowClause(final boolean slide) throws QueryException {
    wsCheck(slide ? SLIDING : TUMBLING);
    wsCheck(WINDOW);
    skipWs();

    final Var var = newVar();
    wsCheck(IN);
    final Expr ex = check(single(), NOVARDECL);

    // WindowStartCondition
    wsCheck(START);
    final Condition start = windowCond(true);

    // WindowEndCondition
    Condition end = null;
    final boolean only = wsConsume(ONLY), check = slide || only;
    if(check || wsConsume(END)) {
      if(check) wsCheck(END);
      end = windowCond(false);
    }
    return new Window(slide, localVars.add(var), ex, start, only, end);
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
    final Var var = curr('$')             ? newVar(SeqType.ITEM_O)  : null;
    final Var at  = wsConsumeWs(AT)       ? newVar(SeqType.INTEGER_O)   : null;
    final Var prv = wsConsumeWs(PREVIOUS) ? newVar(SeqType.ITEM_ZO) : null;
    final Var nxt = wsConsumeWs(NEXT)     ? newVar(SeqType.ITEM_ZO) : null;
    wsCheck(WHEN);
    return new Condition(start, localVars.add(var), localVars.add(at), localVars.add(prv),
        localVars.add(nxt), check(single(), NOEXPR), ii);
  }

  /**
   * Parses the "OrderSpec" rule.
   * Parses the "OrderModifier" rule.
   * Empty order specs are ignored, {@code order} is then returned unchanged.
   * @return new order key
   * @throws QueryException query exception
   */
  private OrderKey orderSpec() throws QueryException {
    final Expr ex = check(single(), ORDERBY);

    boolean desc = false;
    if(!wsConsumeWs(ASCENDING)) desc = wsConsumeWs(DESCENDING);
    boolean least = !sc.orderGreatest;
    if(wsConsumeWs(EMPTYY)) {
      least = !wsConsumeWs(GREATEST);
      if(least) wsCheck(LEAST);
    }
    final Collation coll = wsConsumeWs(COLLATION) ?
      Collation.get(stringLiteral(), qc, sc, info(), FLWORCOLL_X) : sc.collation;
    return new OrderKey(info(), ex, desc, least, coll);
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
      if(var.declType != null || wsConsume(ASSIGN)) {
        if(var.declType != null) wsCheck(ASSIGN);
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
    } while(wsConsumeWs(COMMA));
    return specs;
  }

  /**
   * Parses the "QuantifiedExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = wsConsumeWs(SOME, DOLLAR, NOSOME);
    if(!some && !wsConsumeWs(EVERY, DOLLAR, NOSOME)) return null;

    final int s = localVars.openScope();
    final LinkedList<Clause> clauses = new LinkedList<>();
    do {
      final Var var = newVar();
      wsCheck(IN);
      final Expr ex = check(single(), NOSOME);
      clauses.add(new For(localVars.add(var), ex));
    } while(wsConsumeWs(COMMA));

    wsCheck(SATISFIES);
    final Expr rtrn = Function.BOOLEAN.get(sc, info(), check(single(), NOSOME));
    localVars.closeScope(s);

    final InputInfo info = clauses.peek().info;
    final GFLWOR flwor = new GFLWOR(info, clauses, rtrn);
    final CmpG cmp = new CmpG(flwor, Bln.get(some), OpG.EQ, null, sc, info);
    return some ? cmp : Function.NOT.get(sc, info, cmp);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!wsConsumeWs(SWITCH, PAREN1, TYPEPAR)) return null;
    final InputInfo ii = info();
    wsCheck(PAREN1);
    final Expr cond = check(expr(), NOSWITCH);
    final ArrayList<SwitchGroup> groups = new ArrayList<>();
    wsCheck(PAREN2);

    // collect all cases
    ExprList exprs;
    do {
      exprs = new ExprList(null);
      while(wsConsumeWs(CASE)) add(exprs, single());
      if(exprs.size() == 1) {
        // add default case
        if(groups.isEmpty()) throw error(WRONGCHAR_X_X, CASE, found());
        wsCheck(DEFAULT);
      }
      wsCheck(RETURN);
      exprs.set(0, check(single(), NOSWITCH));
      groups.add(new SwitchGroup(info(), exprs.finish()));
    } while(exprs.size() != 1);

    return new Switch(ii, cond, groups.toArray(new SwitchGroup[0]));
  }

  /**
   * Parses the "TypeswitchExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!wsConsumeWs(TYPESWITCH, PAREN1, TYPEPAR)) return null;
    final InputInfo ii = info();
    wsCheck(PAREN1);
    final Expr ts = check(expr(), NOTYPESWITCH);
    wsCheck(PAREN2);

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
        } while(wsConsume(PIPE));
      }
      wsCheck(RETURN);
      final Expr rtrn = check(single(), NOTYPESWITCH);
      final SeqType[] st = types.toArray(new SeqType[0]);
      cases = Array.add(cases, new TypeswitchGroup(info(), var, st, rtrn));
      localVars.closeScope(s);
      types.clear();
    } while(cs);
    if(cases.length == 1) throw error(NOTYPESWITCH);
    return new Typeswitch(ii, ts, cases);
  }

  /**
   * Parses the "IfExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr iff() throws QueryException {
    if(!wsConsumeWs(IF, PAREN1, IFPAR)) return null;
    final InputInfo ii = info();
    wsCheck(PAREN1);
    final Expr iff = check(expr(), NOIF);
    wsCheck(PAREN2);
    if(!wsConsumeWs(THEN)) throw error(NOIF);
    final Expr thn = check(single(), NOIF);
    final Expr els = wsConsumeWs(ELSE) ? check(single(), NOIF) : Empty.VALUE;
    return new If(ii, iff, thn, els);
  }

  /**
   * Parses the "TernaryIfExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ternaryIf() throws QueryException {
    final Expr iff = elvis();
    if(!wsConsumeWs(TERNARY1)) return iff;

    final InputInfo ii = info();
    final Expr thn = check(single(), NOTERNARY);
    if(!wsConsumeWs(TERNARY2)) throw error(NOTERNARY);
    final Expr els = check(single(), NOTERNARY);
    return new If(ii, iff, thn, els);
  }

  /**
   * Parses the "ElvisExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr elvis() throws QueryException {
    final Expr ex = or();
    if(!wsConsumeWs(ELVIS)) return ex;
    return Function._UTIL_OR.get(sc, info(), ex, check(single(), NOELVIS));
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr ex = and();
    if(!wsConsumeWs(OR)) return ex;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(ex);
    do add(el, and()); while(wsConsumeWs(OR));
    return new Or(ii, el.finish());
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr ex = update();
    if(!wsConsumeWs(AND)) return ex;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(ex);
    do add(el, update()); while(wsConsumeWs(AND));
    return new And(ii, el.finish());
  }

  /**
   * Parses the "UpdateExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr update() throws QueryException {
    Expr ex = comparison();
    if(ex != null) {
      while(wsConsumeWs(UPDATE)) {
        qc.updating();
        ex = new TransformWith(info(), ex, curr('{') ? enclosedExpr() : check(single(),
            UPDATEEXPR));
      }
    }
    return ex;
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr ex = ftContains();
    if(ex != null) {
      for(final OpV c : OpV.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpV(ex, check(ftContains(), CMPEXPR), c, sc.collation, sc, info());
      }
      for(final OpN c : OpN.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpN(ex, check(ftContains(), CMPEXPR), c, info());
      }
      for(final OpG c : OpG.VALUES) {
        if(wsConsumeWs(c.name))
          return new CmpG(ex, check(ftContains(), CMPEXPR), c, sc.collation, sc, info());
      }
    }
    return ex;
  }

  /**
   * Parses the "FTContainsExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ftContains() throws QueryException {
    final Expr ex = stringConcat();
    final int p = pos;
    if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      pos = p;
      return ex;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      throw error(FTIGNORE);
    }
    return new FTContains(ex, select, info());
  }

  /**
   * Parses the "StringConcatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr stringConcat() throws QueryException {
    final Expr ex = range();
    if(ex == null || !consume(CONCAT)) return ex;

    final ExprList el = new ExprList(ex);
    do add(el, range()); while(wsConsume(CONCAT));
    return Function.CONCAT.get(sc, info(), el.finish());
  }

  /**
   * Parses the "RangeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr ex = additive();
    if(!wsConsumeWs(TO)) return ex;
    return new Range(info(), ex, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr additive() throws QueryException {
    Expr ex = multiplicative();
    while(ex != null) {
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ? Calc.MINUS : null;
      if(c == null) break;
      ex = new Arith(info(), ex, check(multiplicative(), CALCEXPR), c);
    }
    return ex;
  }

  /**
   * Parses the "MultiplicativeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr multiplicative() throws QueryException {
    Expr ex = union();
    while(ex != null) {
      final Calc c = consume('*') ? Calc.MULT : wsConsumeWs(DIV) ? Calc.DIV
          : wsConsumeWs(IDIV) ? Calc.IDIV : wsConsumeWs(MOD) ? Calc.MOD : null;
      if(c == null) break;
      ex = new Arith(info(), ex, check(union(), CALCEXPR), c);
    }
    return ex;
  }

  /**
   * Parses the "UnionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr union() throws QueryException {
    final Expr ex = intersect();
    if(ex == null || !isUnion()) return ex;
    final ExprList el = new ExprList(ex);
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
    if(consume(PIPE) && !consume(PIPE)) return true;
    pos = p;
    return false;
  }

  /**
   * Parses the "IntersectExceptExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr intersect() throws QueryException {
    Expr ex = instanceoff();
    boolean lastIs = false;
    ExprList el = null;
    while(true) {
      final boolean is = wsConsumeWs(INTERSECT);
      if(!is && !wsConsumeWs(EXCEPT)) break;
      if((is != lastIs) && el != null) {
        ex = intersectExcept(lastIs, el);
        el = null;
      }
      lastIs = is;
      if(el == null) el = new ExprList(ex);
      add(el, instanceoff());
    }
    return el != null ? intersectExcept(lastIs, el) : ex;
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
  private Expr instanceoff() throws QueryException {
    final Expr ex = treat();
    if(!wsConsumeWs(INSTANCE)) return ex;
    wsCheck(OF);
    return new Instance(info(), ex, sequenceType());
  }

  /**
   * Parses the "TreatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr treat() throws QueryException {
    final Expr ex = promote();
    if(!wsConsumeWs(TREAT)) return ex;
    wsCheck(AS);
    return new Treat(sc, info(), ex, sequenceType());
  }

  /**
   * Parses the "TreatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr promote() throws QueryException {
    final Expr ex = castable();
    if(!wsConsumeWs(PROMOTE)) return ex;
    wsCheck(TO);
    return new TypeCheck(sc, info(), ex, sequenceType(), true);
  }

  /**
   * Parses the "CastableExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr castable() throws QueryException {
    final Expr ex = cast();
    if(!wsConsumeWs(CASTABLE)) return ex;
    wsCheck(AS);
    return new Castable(sc, info(), ex, simpleType());
  }

  /**
   * Parses the "CastExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr ex = transformWith();
    if(!wsConsumeWs(CAST)) return ex;
    wsCheck(AS);
    return new Cast(sc, info(), ex, simpleType());
  }

  /**
   * Parses the "TransformWithExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr transformWith() throws QueryException {
    final Expr ex = arrow();
    final int p = pos;
    if(ex != null && wsConsume(TRANSFORM) && wsConsume(WITH)) {
      qc.updating();
      return new TransformWith(info(), ex, enclosedExpr());
    }
    pos = p;
    return ex;
  }

  /**
   * Parses the "ArrowExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr arrow() throws QueryException {
    Expr ex = unary();
    if(ex != null) {
      while(wsConsume(ARROW)) {
        skipWs();
        final Expr e;
        if(curr('(')) {
          e = parenthesized();
        } else if(curr('$')) {
          final InputInfo ii = info();
          e = localVars.resolve(varName(), ii);
        } else {
          e = eQName(ARROWSPEC, sc.funcNS);
        }
        final InputInfo ii = info();
        wsCheck(PAREN1);

        if(e instanceof QNm) {
          final QNm name = (QNm) e;
          if(keyword(name)) throw error(RESERVED_X, name.local());
          ex = functionCall(name, new ExprList(ex), ii);
        } else {
          final ExprList argList = new ExprList(ex);
          final int[] holes = argumentList(argList);
          final Expr[] args = argList.finish();
          ex = holes == null ? new DynFuncCall(ii, sc, e, args) :
            new PartFunc(sc, ii, e, args, holes);
        }
      }
    }
    return ex;
  }

  /**
   * Parses the "UnaryExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr unary() throws QueryException {
    boolean minus = false;
    boolean found = false;
    do {
      skipWs();
      if(consume('-')) {
        minus ^= true;
        found = true;
      } else if(consume('+')) {
        found = true;
      } else {
        final Expr ex = value();
        return found ? new Unary(info(), check(ex, EVALUNARY), minus) : ex;
      }
    } while(true);
  }

  /**
   * Parses the "ValueExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr value() throws QueryException {
    validate();
    final Expr ex = extension();
    return ex == null ? map() : ex;
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
      qnames.add(eQName(QNAME_X, SKIPCHECK), ii);
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
    wsCheck(CURLY1);
    Expr ex = check(expr(), NOPRAGMA);
    wsCheck(CURLY2);
    for(int p = pragmas.length - 1; p >= 0; p--) {
      ex = new Extension(info(), pragmas[p], ex);
    }
    return ex;
  }

  /**
   * Parses the "Pragma" rule.
   * @return array of pragmas or {@code null}
   * @throws QueryException query exception
   */
  private Pragma[] pragma() throws QueryException {
    if(!wsConsumeWs(PRAGMA)) return null;

    final ArrayList<Pragma> el = new ArrayList<>();
    do {
      final QNm name = eQName(QNAME_X, null);
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
    } while(wsConsumeWs(PRAGMA));
    return el.toArray(new Pragma[0]);
  }

  /**
   * Parses the "MapExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr map() throws QueryException {
    final Expr ex = path();
    if(ex != null) {
      final int next = next();
      if(next != '=' && next != '!' && wsConsumeWs(EXCL)) {
        final ExprList el = new ExprList(ex);
        do add(el, path()); while(next() != '=' && wsConsumeWs(EXCL));
        return new CachedMap(info(), el.finish());
      }
    }
    return ex;
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
      final Expr ex;
      if(consume('/')) {
        // two slashes: absolute descendant path
        checkAxis(Axis.DESCENDANT);
        add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, KindTest.NOD));
        mark();
        ex = step(true);
      } else {
        // one slash: absolute child path
        checkAxis(Axis.CHILD);
        mark();
        ex = step(false);
        // no more steps: return root expression
        if(ex == null) return root;
      }
      add(el, ex);
      relativePath(el);
    } else {
      // relative path (no preceding slash)
      mark();
      final Expr ex = step(false);
      if(ex == null) return null;
      // return expression if no slash follows
      if(curr() != '/' && !(ex instanceof Step)) return ex;
      el = new ExprList();
      if(ex instanceof Step) add(el, ex);
      else root = ex;
      relativePath(el);
    }
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
          add(el, new CachedStep(info(), Axis.DESCENDANT_OR_SELF, KindTest.NOD));
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
    final Expr ex = postfix();
    return ex != null ? ex : axisStep(error);
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
    if(wsConsume(DOTS2)) {
      axis = Axis.PARENT;
      test = KindTest.NOD;
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
        if(!wsConsumeWs(ax.name)) continue;
        if(wsConsumeWs(COLS)) {
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

      if(axis == null) {
        axis = Axis.CHILD;
        test = nodeTest(NodeType.ELEMENT, true);
        if(test == KindTest.NSP) throw error(NSAXIS);
        if(test != null && test.type == NodeType.ATTRIBUTE) axis = Axis.ATTRIBUTE;
        checkTest(test, axis != Axis.ATTRIBUTE);
      }
      if(test == null) {
        if(error) throw error(STEPMISS_X, found());
        return null;
      }
    }

    final ExprList el = new ExprList();
    while(wsConsume(SQUARE1)) {
      checkPred(true);
      add(el, expr());
      wsCheck(SQUARE2);
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
      if(consume(':')) {
        // name test: *:name
        if(!consume('*')) {
          return new NameTest(new QNm(ncName(QNAME_X)), NamePart.LOCAL, type, sc.elemNS);
        }
      }
      // name test: *
      pos = p;
      return KindTest.get(type);
    }

    if(consume(EQNAME)) {
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        // name test: Q{uri}*
        final QNm name = new QNm(COLON, uri);
        return new NameTest(name, NamePart.URI, type, sc.elemNS);
      }
    }
    pos = p;

    final InputInfo ii = info();
    QNm name = eQName(null, SKIPCHECK);
    if(name != null) {
      p = pos;
      if(all && wsConsumeWs(PAREN1)) {
        final NodeType nt = NodeType.find(name);
        if(nt != null) {
          final Test test = kindTest(nt);
          return test == null ? KindTest.get(nt) : test;
        }
      } else {
        pos = p;
        if(!name.hasPrefix() && consume(COLWC)) {
          // name test: prefix:*
          name = new QNm(concat(name.string(), COLON));
          qnames.add(name, type == NodeType.ELEMENT, ii);
          return new NameTest(name, NamePart.URI, type, sc.elemNS);
        }
        // name test: prefix:name, name, Q{uri}name
        qnames.add(name, type == NodeType.ELEMENT, ii);
        return new NameTest(name, NamePart.FULL, type, sc.elemNS);
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
    Expr ex = primary(), old;
    if(ex != null) {
      do {
        old = ex;
        if(wsConsume(SQUARE1)) {
          // parses the "Predicate" rule
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck(SQUARE2);
          } while(wsConsume(SQUARE1));
          ex = new CachedFilter(info(), ex, el.finish());
        } else if(consume(PAREN1)) {
          // parses the "ArgumentList" rule
          final InputInfo ii = info();
          final ExprList argList = new ExprList();
          final int[] holes = argumentList(argList);
          final Expr[] args = argList.finish();
          ex = holes == null ? new DynFuncCall(ii, sc, ex, args) :
            new PartFunc(sc, ii, ex, args, holes);
        } else {
          final int p = pos;
          if(consume(QUESTION) && !consume(QUESTION) && !consume(':')) {
            // parses the "Lookup" rule
            ex = new Lookup(info(), ex, keySpecifier());
          } else {
            pos = p;
          }
        }
      } while(ex != old);
    }
    return ex;
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
    final char ch = curr();
    // variables
    if(ch == '$') {
      final InputInfo ii = info();
      return localVars.resolve(varName(), ii);
    }
    // parentheses
    if(ch == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(ch == '<') return dirConstructor();
    // string constructor
    if(ch == '`') return stringConstructor();
    // function item
    Expr ex = functionItem();
    if(ex != null) return ex;
    // function call
    ex = functionCall();
    if(ex != null) return ex;
    // computed constructors
    ex = compConstructor();
    if(ex != null) return ex;
    // ordered expression
    int p = pos;
    if(wsConsumeWs(ORDERED) || wsConsumeWs(UNORDERED)) {
      if(curr('{')) return enclosedExpr();
      pos = p;
    }
    // map constructor
    if(wsConsumeWs(MAP, CURLY1, INCOMPLETE)) return new CMap(info(), keyValues());
    // square array constructor
    if(wsConsumeWs(SQUARE1)) return new CArray(info(), true, values());
    // curly array constructor
    if(wsConsumeWs(ARRAY, CURLY1, INCOMPLETE)) {
      wsCheck(CURLY1);
      final Expr exp = expr();
      wsCheck(CURLY2);
      return exp == null ? new CArray(info(), false) : new CArray(info(), false, exp);
    }
    // unary lookup
    p = pos;
    if(consume(QUESTION)) {
      if(!wsConsume(COMMA) && !consume(PAREN2)) {
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
    skipWs();
    final char ch = curr();
    if(ch == '*') {
      consume();
      return Lookup.WILDCARD;
    }
    if(ch == '(') return parenthesized();
    // numeric literal
    final Expr num = numericLiteral(ch);
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
    wsCheck(CURLY1);
    final ExprList el = new ExprList();
    if(!wsConsume(CURLY2)) {
      do {
        add(el, check(single(), INVMAPKEY));
        if(!wsConsume(COL)) throw error(WRONGCHAR_X_X, COL, found());
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(COMMA));
      wsCheck(CURLY2);
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
    if(!wsConsume(SQUARE2)) {
      do {
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(COMMA));
      wsCheck(SQUARE2);
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
    final int p = pos;

    // parse annotations
    final AnnList anns = annotations(false).check(false, true);
    // inline function
    if(wsConsume(FUNCTION) && wsConsume(PAREN1)) {
      if(anns.contains(Annotation.PRIVATE) || anns.contains(Annotation.PUBLIC))
        throw error(NOVISALLOWED);

      final HashMap<Var, Expr> global = new HashMap<>();
      localVars.pushContext(global);
      final Var[] params = paramList();
      wsCheck(PAREN2);
      final SeqType type = optAsType();
      final Expr body = enclosedExpr();
      final VarScope vs = localVars.popContext();
      return new Closure(info(), type, params, body, anns, global, vs);
    }
    // annotations not allowed here
    if(!anns.isEmpty()) throw error(NOANN);

    // named function reference
    pos = p;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && wsConsumeWs(HSH)) {
      if(keyword(name)) throw error(RESERVED_X, name.local());
      final char ch = curr();
      final Expr num = numericLiteral(ch);
      if(Function.ERROR.is(num)) return num;
      if(!(num instanceof Int)) throw error(ARITY_X, num == null ? ch == 0 ? "" : ch : token);
      final long a = ((Int) num).itr();
      if(a > Integer.MAX_VALUE) return FnError.get(RANGE_X.get(info(), num), SeqType.ITEM_ZM, sc);
      final int arity = (int) a;
      final Expr ex = Functions.getLiteral(name, arity, qc, sc, info(), false);
      return ex != null ? ex : undeclaredLiteral(name, arity, info());
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
    final Closure ex = Closure.undeclaredLiteral(name, arity, qc, sc, ii);
    qc.funcs.registerFuncLiteral(ex);
    return ex;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr literal() throws QueryException {
    final char ch = curr();
    final Expr num = numericLiteral(ch);
    return num != null ? num : quote(ch) ? Str.get(stringLiteral()) : null;
  }

  /**
   * Parses the "NumericLiteral" rule.
   * Parses the "DecimalLiteral" rule.
   * Parses the "IntegerLiteral" rule.
   * @param ch current character
   * @return numeric literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr numericLiteral(final char ch) throws QueryException {
    if(!digit(ch) && ch != '.') return null;

    // integer digits
    token.reset();
    while(digit(curr())) token.add(consume());

    // fractional digits?
    final boolean dec = consume('.');
    if(dec) {
      token.add('.');
      if(digit(curr())) {
        do { token.add(consume()); } while(digit(curr()));
      } else if(token.size() == 1) {
        throw error(NUMBER_X, token);
      }
    }

    // double value
    if(XMLToken.isNCStartChar(curr())) {
      if(!consume('e') && !consume('E')) throw error(NUMBERWS_X, token);
      token.add('e');
      if(curr('+') || curr('-')) token.add(consume());
      if(!digit(curr())) throw error(NUMBER_X, token);
      do { token.add(consume()); } while(digit(curr()));

      if(XMLToken.isNCStartChar(curr())) throw error(NUMBERWS_X, token);
      return Dbl.get(token.toArray(), info());
    }

    // decimal value
    if(dec) return Dec.get(new BigDecimal(string(token.toArray())));

    // integer value
    if(token.isEmpty()) throw error(NUMBER_X, token);
    final long l = toLong(token.toArray());
    return l != Long.MIN_VALUE ? Int.get(l) :
      FnError.get(RANGE_X.get(info(), normalize(token, null)), SeqType.INTEGER_O, sc);
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
      if(!more() || curr() == '{') throw error(WRONGCHAR_X_X, CURLY2, found());
      entity(token);
    }
    final byte[] ns = normalize(token.toArray());
    if(eq(ns, XMLNS_URI)) {
      pos = p;
      throw error(ILLEGALEQNAME_X, info(), ns);
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
    return eQName(NOVARNAME, null);
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
    return new Var(name, st, false, qc, sc, ii);
  }

  /**
   * Parses the "ParenthesizedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr parenthesized() throws QueryException {
    check('(');
    final Expr ex = expr();
    wsCheck(PAREN2);
    return ex == null ? Empty.VALUE : ex;
  }

  /**
   * Parses the "FunctionCall" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int p = pos;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && !keyword(name)) {
      final InputInfo ii = info();
      if(wsConsume(PAREN1)) {
        final Expr ex = functionCall(name, new ExprList(), ii);
        if(ex != null) return ex;
      }
    }
    pos = p;
    return null;
  }

  /**
   * Returns a function call.
   * @param name function name
   * @param argList list of arguments
   * @param ii input info
   * @return function or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionCall(final QNm name, final ExprList argList, final InputInfo ii)
      throws QueryException {
    final int[] holes = argumentList(argList);
    final Expr[] args = argList.finish();
    if(holes == null) return Functions.get(name, args, qc, sc, ii);

    // partial function
    final int arity = args.length + holes.length;
    final Expr func = Functions.getLiteral(name, arity, qc, sc, ii, false);
    final Expr ex = func != null ? func : undeclaredLiteral(name, arity, ii);
    return new PartFunc(sc, ii, ex, args, holes);
  }

  /**
   * Parses the "ArgumentList" rule without the opening parenthesis.
   * @param args list to put the arguments into
   * @return array of arguments, place-holders '?' are represented as {@code null} entries
   * @throws QueryException query exception
   */
  private int[] argumentList(final ExprList args) throws QueryException {
    int[] holes = null;
    if(!wsConsume(PAREN2)) {
      int i = args.size();
      do {
        final Expr ex = single();
        if(ex != null) {
          args.add(ex);
        } else if(wsConsume(QUESTION)) {
          holes = holes == null ? new int[] { i } : Array.add(holes, i);
        } else {
          throw error(FUNCARG_X, found());
        }
        i++;
      } while(wsConsume(COMMA));
      if(!wsConsume(PAREN2)) throw error(FUNCARG_X, found());
    }
    return holes;
  }

  /**
   * Parses the "StringConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr stringConstructor() throws QueryException {
    check('`');
    check('`');
    check('[');

    final ExprList el = new ExprList();
    final TokenBuilder tb = new TokenBuilder();
    while(more()) {
      final int p = pos;
      if(consume(']') && consume('`') && consume('`')) {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        return el.size() == 1 ? el.get(0) : Function.CONCAT.get(sc, info(), el.finish());
      }
      pos = p;
      if(consume('`') && consume('{')) {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        final Expr ex = expr();
        if(ex != null) el.add(Function.STRING_JOIN.get(sc, info(), ex, Str.get(" ")));
        skipWs();
        check('}');
        check('`');
      } else {
        pos = p;
        tb.add(consume());
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
      do {
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
      } while(true);

      if(!tb.isEmpty()) add(attv, Str.get(tb.finish()));

      // parse namespace declarations
      final boolean pr = startsWith(atn, XMLNS_COLON);
      if(pr || eq(atn, XMLNS)) {
        if(!simple) throw error(NSCONS);
        final byte[] pref = pr ? local(atn) : EMPTY;
        final byte[] uri = attv.isEmpty() ? EMPTY : ((Str) attv.get(0)).string();
        if(eq(pref, XML) && eq(uri, XML_URI)) {
          if(xmlDecl) throw error(DUPLNSDEF_X, XML);
          xmlDecl = true;
        } else {
          if(!Uri.uri(uri).isValid()) throw error(INVURI_X, uri);
          if(pr) {
            if(uri.length == 0) throw error(NSEMPTYURI);
            if(eq(pref, XML, XMLNS)) throw error(BINDXML_X, pref);
            if(eq(uri, XML_URI)) throw error(BINDXMLURI_X_X, uri, XML);
            if(eq(uri, XMLNS_URI)) throw error(BINDXMLURI_X_X, uri, XMLNS);
            sc.ns.add(pref, uri);
          } else {
            if(eq(uri, XML_URI)) throw error(XMLNSDEF_X, uri);
            sc.elemNS = uri;
          }
          if(ns.contains(pref)) throw error(DUPLNSDEF_X, pref);
          ns.add(pref, uri);
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
        final Expr ex = dirElemContent(name.string());
        if(ex != null) add(cont, ex);
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
    do {
      final char ch = curr();
      if(ch == '<') {
        if(wsConsume(CDATA)) {
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
    } while(true);
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
    do {
      final char ch = consumeContent();
      if(ch == '-' && consume('-')) {
        check('>');
        return new CComm(sc, info(), false, Str.get(tb.finish()));
      }
      tb.add(ch);
    } while(true);
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
    do {
      final char ch = consumeContent();
      if(ch == '?' && consume('>')) {
        return new CPI(sc, info(), false, Str.get(str), Str.get(tb.finish()));
      }
      if(!space) throw error(PIWRONG);
      tb.add(ch);
    } while(true);
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
    final QNm qn = eQName(null, SKIPCHECK);
    if(qn != null) {
      name = qn;
      qnames.add(qn, ii);
    } else {
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NOELEMNAME);
      wsCheck(CURLY2);
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
    final QNm qn = eQName(null, SKIPCHECK);
    if(qn != null) {
      name = qn;
      qnames.add(qn, false, ii);
    } else {
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NOATTNAME);
      wsCheck(CURLY2);
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
    return curr('{') ? new CTxt(sc, info(), true, enclosedExpr()) : null;
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
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), PIWRONG);
      wsCheck(CURLY2);
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
    final QNm name = eQName(TYPEINVALID, sc.elemNS);
    Type type = ListType.find(name);
    if(type == null) {
      type = AtomType.find(name, false);
      if(consume(PAREN1)) throw error(SIMPLETYPE_X, name.prefixId(XML));
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
    if(wsConsumeWs(EMPTY_SEQUENCE, PAREN1, null)) {
      wsCheck(PAREN1);
      wsCheck(PAREN2);
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
    skipWs();

    // parenthesized item type
    if(consume(PAREN1)) {
      final SeqType type = itemType();
      wsCheck(PAREN2);
      return type;
    }

    // parse optional annotation and type name
    final AnnList anns = annotations(false).check(false, false);
    final QNm name = eQName(TYPEINVALID, null);
    skipWs();
    // check if name is followed by parentheses
    final boolean func = curr('(');

    // item type
    Type type = null;
    if(func) {
      consume(PAREN1);
      // item type
      if(name.eq(AtomType.ITEM.qname())) type = AtomType.ITEM;
      // node types
      if(type == null) type = NodeType.find(name);
      // function types
      if(type == null) {
        type = FuncType.find(name);
        if(type != null) return functionTest(anns, type).seqType();
      }
      // no type found
      if(type == null) throw error(WHICHTYPE_X, FuncType.similar(name));
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(sc.elemNS);
      // atomic types
      type = AtomType.find(name, false);
      // no type found
      if(type == null) throw error(TYPEUNKNOWN_X, AtomType.similar(name));
    }

    // annotations are not allowed for remaining types
    if(!anns.isEmpty()) throw error(NOANN);

    // atomic value, or closing parenthesis
    if(!func || wsConsume(PAREN2)) return type.seqType();

    // raise error if type different to node is not finalized by a parenthesis
    if(!(type instanceof NodeType)) wsCheck(PAREN2);

    // return type with an optional kind test for node types
    return SeqType.get(type, Occ.EXACTLY_ONE, kindTest((NodeType) type));
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
    if(wsConsume(ASTERISK)) {
      wsCheck(PAREN2);
      return type;
    }

    // map
    if(type instanceof MapType) {
      final Type key = itemType().type;
      if(!key.instanceOf(AtomType.ANY_ATOMIC_TYPE)) throw error(MAPTAAT_X, key);
      wsCheck(COMMA);
      final MapType tp = MapType.get((AtomType) key, sequenceType());
      wsCheck(PAREN2);
      return tp;
    }
    // array
    if(type instanceof ArrayType) {
      final ArrayType tp = ArrayType.get(sequenceType());
      wsCheck(PAREN2);
      return tp;
    }
    // function type
    SeqType[] args = { };
    if(!wsConsume(PAREN2)) {
      // function has got arguments
      do args = Array.add(args, sequenceType());
      while(wsConsume(COMMA));
      wsCheck(PAREN2);
    }
    wsCheck(AS);
    return FuncType.get(anns, sequenceType(), args);
  }

  /**
   * Parses the "ElementTest" rule without the type name and the opening bracket.
   * @param type type
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test kindTest(final NodeType type) throws QueryException {
    final Test tp;
    switch(type) {
      case DOCUMENT_NODE: tp = documentTest(); break;
      case ELEMENT: tp = elementTest(); break;
      case ATTRIBUTE: tp = attributeTest(); break;
      case PROCESSING_INSTRUCTION:  tp = piTest(); break;
      case SCHEMA_ELEMENT:
      case SCHEMA_ATTRIBUTE: tp = schemaTest(); break;
      default:  tp = null; break;
    }
    wsCheck(PAREN2);
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

    wsCheck(PAREN1);
    skipWs();
    final Test test = elem ? elementTest() : schemaTest();
    wsCheck(PAREN2);
    return new DocTest(test != null ? test : KindTest.ELM);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test elementTest() throws QueryException {
    final QNm name = eQName(null, sc.elemNS);
    final boolean wc = name == null && consume(ASTERISK);
    if(name != null || wc) {
      Type ann = null;
      if(wsConsumeWs(COMMA)) {
        // parse type name
        final QNm tn = eQName(QNAME_X, sc.elemNS);
        ann = ListType.find(tn);
        if(ann == null) ann = AtomType.find(tn, true);
        if(ann == null) throw error(TYPEUNDEF_X, AtomType.similar(tn));
        // parse (and ignore) optional question mark
        wsConsume(QUESTION);
      }
      if(ann != null || !wc) {
        final Test test = Test.get(NodeType.ELEMENT, name, ann, sc.elemNS);
        if(test == null) throw error(STATIC_X, ann);
        return test;
      }
    }
    return null;
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return error (not supported)
   * @throws QueryException query exception
   */
  private Test schemaTest() throws QueryException {
    final QNm name = eQName(QNAME_X, sc.elemNS);
    throw error(SCHEMAINV_X, name);
  }

  /**
   * Parses the "AttributeTest" rule without the leading keyword and its brackets.
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test attributeTest() throws QueryException {
    final QNm name = eQName(null, null);
    final boolean wc = name == null && consume(ASTERISK);
    if(name != null || wc) {
      Type ann = null;
      if(wsConsumeWs(COMMA)) {
        // parse type name
        final QNm tn = eQName(QNAME_X, sc.elemNS);
        ann = ListType.find(tn);
        if(ann == null) ann = AtomType.find(tn, true);
        if(ann == null) throw error(TYPEUNDEF_X, AtomType.similar(tn));
      }
      if(ann != null || !wc) {
        final Test test = Test.get(NodeType.ATTRIBUTE, name, ann, sc.elemNS);
        if(test == null) throw error(STATIC_X, ann);
        return test;
      }
    }
    return null;
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
    return Test.get(NodeType.PROCESSING_INSTRUCTION, new QNm(name));
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!wsConsumeWs(TRY)) return null;

    final Expr ex = enclosedExpr();
    wsCheck(CATCH);

    Catch[] catches = { };
    do {
      NameTest[] codes = { };
      do {
        skipWs();
        final Test test = nodeTest(NodeType.ELEMENT, false);
        if(test == null) throw error(NOCATCH);
        codes = Array.add(codes, test instanceof NameTest ? (NameTest) test : null);
      } while(wsConsumeWs(PIPE));

      final int s = localVars.openScope();
      final int cl = Catch.NAMES.length;
      final Var[] vs = new Var[cl];
      final InputInfo ii = info();
      for(int i = 0; i < cl; i++) {
        vs[i] = localVars.add(new Var(Catch.NAMES[i], Catch.TYPES[i], false, qc, sc, ii));
      }
      final Catch c = new Catch(ii, codes, vs);
      c.expr = enclosedExpr();
      localVars.closeScope(s);

      catches = Array.add(catches, c);
    } while(wsConsumeWs(CATCH));

    return new Try(info(), ex, catches);
  }

  /**
   * Parses the "FTSelection" rules.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftSelection(final boolean prg) throws QueryException {
    FTExpr ex = ftOr(prg), first = null, old;
    boolean ordered = false;
    do {
      old = ex;
      if(wsConsumeWs(ORDERED)) {
        ordered = true;
        old = null;
      } else if(wsConsumeWs(WINDOW)) {
        ex = new FTWindow(info(), ex, additive(), ftUnit());
      } else if(wsConsumeWs(DISTANCE)) {
        final Expr[] rng = ftRange(false);
        if(rng == null) throw error(FTRANGE);
        ex = new FTDistance(info(), ex, rng[0], rng[1], ftUnit());
      } else if(wsConsumeWs(AT)) {
        final FTContents cont = wsConsumeWs(START) ? FTContents.START : wsConsumeWs(END) ?
          FTContents.END : null;
        if(cont == null) throw error(INCOMPLETE);
        ex = new FTContent(info(), ex, cont);
      } else if(wsConsumeWs(ENTIRE)) {
        wsCheck(CONTENT);
        ex = new FTContent(info(), ex, FTContents.ENTIRE);
      } else {
        final boolean same = wsConsumeWs(SAME);
        final boolean diff = !same && wsConsumeWs(DIFFERENT);
        if(same || diff) {
          final FTUnit unit;
          if(wsConsumeWs(SENTENCE)) unit = FTUnit.SENTENCES;
          else if(wsConsumeWs(PARAGRAPH)) unit = FTUnit.PARAGRAPHS;
          else throw error(INCOMPLETE);
          ex = new FTScope(info(), ex, same, unit);
        }
      }
      if(first == null && old != null && old != ex) first = ex;
    } while(old != ex);

    if(ordered) {
      if(first == null) return new FTOrder(info(), ex);
      first.exprs[0] = new FTOrder(info(), first.exprs[0]);
    }
    return ex;
  }

  /**
   * Parses the "FTOr" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftOr(final boolean prg) throws QueryException {
    final FTExpr ex = ftAnd(prg);
    if(!wsConsumeWs(FTOR)) return ex;

    FTExpr[] list = { ex };
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
    final FTExpr ex = ftMildNot(prg);
    if(!wsConsumeWs(FTAND)) return ex;

    FTExpr[] list = { ex };
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
    final FTExpr ex = ftUnaryNot(prg);
    if(!wsConsumeWs(NOT)) return ex;

    FTExpr[] list = { };
    do {
      wsCheck(IN);
      list = Array.add(list, ftUnaryNot(prg));
    } while(wsConsumeWs(NOT));

    // convert "A not in B not in ..." to "A not in (B or ...)"
    final InputInfo ii = info();
    final FTExpr not = list.length == 1 ? list[0] : new FTOr(ii, list);
    if(ex.usesExclude() || not.usesExclude()) throw FTMILD.get(ii);
    return new FTMildNot(ii, ex, not);
  }

  /**
   * Parses the "FTUnaryNot" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftUnaryNot(final boolean prg) throws QueryException {
    final boolean not = wsConsumeWs(FTNOT);
    final FTExpr ex = ftPrimaryWithOptions(prg);
    return not ? new FTNot(info(), ex) : ex;
  }

  /**
   * Parses the "FTPrimaryWithOptions" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimaryWithOptions(final boolean prg) throws QueryException {
    FTExpr ex = ftPrimary(prg);

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
    if(wsConsumeWs(WEIGHT)) ex = new FTWeight(info(), ex, enclosedExpr());

    // skip options if none were specified...
    return found ? new FTOptions(info(), ex, fto) : ex;
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
      wsCheck(CURLY1);
      FTExpr ex = ftSelection(true);
      wsCheck(CURLY2);
      for(int p = pragmas.length - 1; p >= 0; p--) ex = new FTExtension(info(), pragmas[p], ex);
      return ex;
    }

    if(wsConsumeWs(PAREN1)) {
      final FTExpr ex = ftSelection(false);
      wsCheck(PAREN2);
      return ex;
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
        opt.th = new ThesQuery();
        if(using) {
          final boolean par = wsConsume(PAREN1);
          if(!wsConsumeWs(DEFAULT)) ftThesaurusID(opt.th);
          while(par && wsConsume(COMMA))
            ftThesaurusID(opt.th);
          if(par) wsCheck(PAREN2);
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
            if(wsConsume(PAREN1)) {
              do {
                final byte[] sl = stringLiteral();
                if(except) sw.remove(sl);
                else sw.add(sl);
              } while(wsConsume(COMMA));
              wsCheck(PAREN2);
            } else if(wsConsumeWs(AT)) {
              final String fn = string(stringLiteral());
              // optional: resolve URI reference
              final IO fl = qc.resources.stopWords(fn, sc);
              try {
                opt.sw.read(fl, except);
              } catch(final IOException ex) {
                Util.debug(ex);
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
   * @param thes link to thesaurus
   * @throws QueryException query exception
   */
  private void ftThesaurusID(final ThesQuery thes) throws QueryException {
    wsCheck(AT);

    final String fn = string(stringLiteral());
    // optional: resolve URI reference
    final IO fl = qc.resources.thesaurus(fn, sc);
    final byte[] rel = wsConsumeWs(RELATIONSHIP) ? stringLiteral() : EMPTY;
    final Expr[] range = ftRange(true);
    long min = 0, max = Long.MAX_VALUE;
    if(range != null) {
      wsCheck(LEVELS);
      // values will always be integer instances
      min = ((ANum) range[0]).itr();
      max = ((ANum) range[1]).itr();
    }
    thes.add(new Thesaurus(fl, rel, min, max, qc.context));
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
    if(!wsConsumeWs(COPY, DOLLAR, INCOMPLETE)) return null;
    final int s = localVars.openScope();

    Let[] fl = { };
    do {
      final Var var = newVar(SeqType.NODE_O);
      wsCheck(ASSIGN);
      final Expr ex = check(single(), INCOMPLETE);
      fl = Array.add(fl, new Let(localVars.add(var), ex));
    } while(wsConsumeWs(COMMA));
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
      if(wsConsume(PAREN1)) {
        final InputInfo ii = info();
        final ExprList argList = new ExprList();

        if(!wsConsume(PAREN2)) {
          do {
            final Expr ex = single();
            if(ex == null) throw error(FUNCARG_X, found());
            argList.add(ex);
          } while(wsConsume(COMMA));
          if(!wsConsume(PAREN2)) throw error(FUNCARG_X, found());
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
   * @param error optional error message. If not {@code null}, will be raised if no EQName is found
   * @param ns default namespace, or operation mode ({@link #URICHECK}, {@link #SKIPCHECK})
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  private QNm eQName(final QueryError error, final byte[] ns) throws QueryException {
    final int p = pos;
    if(consume(EQNAME)) {
      final byte[] uri = bracedURILiteral(), name = ncName(null);
      if(name.length != 0) {
        if(ns == URICHECK && uri.length == 0) {
          pos = p;
          throw error(NOURI_X, name);
        }
        return new QNm(name, uri);
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
      if(ns == URICHECK) {
        pos = p;
        throw error(NSMISS_X, name);
      }
      if(name.hasPrefix()) {
        pos = p;
        throw error(NOURI_X, name.string());
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
   * Consumes the specified token and surrounding whitespaces.
   * @param string string to consume
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String string) throws QueryException {
    final int p = pos;
    if(!wsConsume(string)) return false;
    if(skipWs() || !XMLToken.isNCStartChar(string.charAt(0)) || !XMLToken.isNCChar(curr()))
      return true;
    pos = p;
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position. If the strings are
   * found, the cursor is placed after the first token.
   * @param string1 string to consume
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
        if(ch <= 0 || ch > ' ') return i != pos;
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
      currDoc.setLength(0);
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
        if(!nested && doc.isEmpty()) {
          doc = currDoc.toString().trim();
          currDoc.setLength(0);
        }
        return;
      }
      if(xqdoc) currDoc.append(curr);
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
      if(ch <= 0 || ch > ' ') return i != pos;
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
   * @param ex new expression
   * @throws QueryException query exception
   */
  private void add(final ExprList ar, final Expr ex) throws QueryException {
    if(ex == null) throw error(INCOMPLETE);
    ar.add(ex);
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
}
