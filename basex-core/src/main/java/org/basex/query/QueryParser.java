package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpN.OpN;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.expr.ContextValue;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.expr.gflwor.GroupBy.Spec;
import org.basex.query.expr.gflwor.OrderBy.Key;
import org.basex.query.expr.gflwor.Window.Condition;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.Kind;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.up.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.format.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Parser for XQuery expressions.
 *
 * @author BaseX Team 2005-16, BSD License
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
      SeqType.ANY_FUN.string(), token(ARRAY), NodeType.ATT.string(), NodeType.COM.string(),
      NodeType.DOC.string(), NodeType.ELM.string(), token(EMPTY_SEQUENCE), token(IF),
      AtomType.ITEM.string(), token(MAP), NodeType.NSP.string(), NodeType.NOD.string(),
      NodeType.PI.string(), token(SCHEMA_ATTRIBUTE), token(SCHEMA_ELEMENT), token(SWITCH),
      NodeType.TXT.string(), token(TYPESWITCH)
    };
    for(final byte[] key : keys) KEYWORDS.add(key);
  }

  /** Imported modules. */
  final TokenSet imports = new TokenSet();
  /** Modules loaded by the current file. */
  public final TokenSet modules = new TokenSet();
  /** List of modules to be parsed. */
  public final ArrayList<ModInfo> mods = new ArrayList<>();
  /** Name of current module. */
  public QNm module;

  /** Parsed variables. */
  public final TokenObjMap<StaticVar> vars = new TokenObjMap<>();
  /** Parsed functions. */
  public final TokenObjMap<StaticFunc> funcs = new TokenObjMap<>();

  /** Namespaces. */
  public final TokenMap namespaces = new TokenMap();

  /** Query context. */
  public final QueryContext qc;
  /** Static context. */
  public final StaticContext sc;

  /** Temporary token cache. */
  private final TokenBuilder tok = new TokenBuilder();
  /** XQDoc cache. */
  private final StringBuilder currDoc = new StringBuilder();
  /** Current XQDoc string. */
  private String moduleDoc = "";

  /** Alternative error code. */
  private QueryError alter;
  /** Function name of alternative error. */
  private QNm alterFunc;
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
   * @param path file path (if {@code null}, {@link MainOptions#QUERYPATH} will be assigned)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   * @throws QueryException query exception
   */
  public QueryParser(final String query, final String path, final QueryContext qctx,
      final StaticContext sctx) throws QueryException {

    super(query);
    qc = qctx;
    sc = sctx != null ? sctx : new StaticContext(qctx);

    // set path to query file
    final MainOptions opts = qctx.context.options;
    final String uri = path != null ? path : opts.get(MainOptions.QUERYPATH);
    if(!uri.isEmpty()) sc.baseURI(uri);

    // bind external variables
    for(final Entry<String, String> entry : opts.toMap(MainOptions.BINDINGS).entrySet()) {
      final String key = entry.getKey();
      final Atm value = new Atm(entry.getValue());
      if(key.isEmpty()) qctx.context(value, sc);
      else qctx.bind(key, value, sc);
    }
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

      final int i = pos;
      if(wsConsumeWs(MODULE, NAMESPACE, null)) throw error(MAINMOD);
      pos = i;

      prolog1();
      importModules();
      prolog2();

      localVars.pushContext(null);
      final Expr expr = expr();
      if(expr == null) throw alter == null ? error(EXPREMPTY) : error();
      final VarScope scope = localVars.popContext();

      final MainModule mm = new MainModule(
          expr, scope, null, moduleDoc, funcs, vars, imports, sc, null);
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
   * @param check if functions and variables should be checked
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

      module = new QNm(pref, uri);
      sc.ns.add(pref, uri, info());
      namespaces.put(pref, uri);
      wsCheck(";");

      // get absolute path
      final IO base = sc.baseIO();
      final byte[] path = token(base == null ? "" : base.path());
      qc.modParsed.put(path, uri);
      qc.modStack.push(path);

      prolog1();
      importModules();
      prolog2();
      finish(null);
      if(check) check(null);

      qc.modStack.pop();
      return new LibraryModule(module, moduleDoc, funcs, vars, imports, sc);
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
    for(int i = 0; i < length;) {
      // only retrieve code points for large character codes (faster)
      int cp = input.charAt(i);
      final boolean hs = cp >= Character.MIN_HIGH_SURROGATE;
      if(hs) cp = input.codePointAt(i);
      if(!XMLToken.valid(cp)) {
        pos = i;
        throw error(QUERYINV_X, cp);
      }
      i += hs ? Character.charCount(cp) : 1;
    }
  }

  /**
   * Finishes the parsing step.
   * @param mm main module; {@code null} for library modules
   * @throws QueryException query exception
   */
  private void finish(final MainModule mm) throws QueryException {
    if(more()) {
      if(alter != null) throw error();
      final String rest = rest();
      pos++;
      if(mm == null) throw error(MODEXPR, rest);
      throw error(QUERYEND_X, rest);
    }

    // completes the parsing step
    qnames.assignURI(this, 0);
    if(sc.elemNS != null) sc.ns.add(EMPTY, sc.elemNS, null);

    // set default decimal format
    final byte[] empty = new QNm(EMPTY).id();
    if(sc.decFormats.get(empty) == null) {
      sc.decFormats.put(empty, new DecFormatter());
    }
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
    final int i = pos;
    if(!wsConsumeWs(XQUERY)) return;

    final boolean version = wsConsumeWs(VERSION);
    if(version) {
      // parse xquery version
      final String ver = string(stringLiteral());
      if(!ver.equals(XQ10) && !Strings.eq(ver, XQ11, XQ30, XQ31)) throw error(XQUERYVER_X, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if(wsConsumeWs(ENCODING)) {
      final String enc = string(stringLiteral());
      if(!Strings.supported(enc)) throw error(XQUERYENC2_X, enc);
    } else if(!version) {
      pos = i;
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
      final int i = pos;
      if(wsConsumeWs(DECLARE)) {
        if(wsConsumeWs(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl() &&
             !emptyOrderDecl() && !decimalFormatDecl(true))
            throw error(DECLINCOMPLETE);
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
          final FTOpt fto = new FTOpt();
          while(ftMatchOption(fto));
          qc.ftOpt().copy(fto);
        } else {
          pos = i;
          return;
        }
      } else if(wsConsumeWs(IMPORT)) {
        if(wsConsumeWs(SCHEMA)) {
          schemaImport();
        } else if(wsConsumeWs(MODULE)) {
          moduleImport();
        } else {
          pos = i;
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
      final int i = pos;
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
          varDecl(anns.check(true));
        } else if(wsConsumeWs(FUNCTION)) {
          functionDecl(anns.check(false));
        } else if(!anns.isEmpty()) {
          throw error(VARFUNC);
        } else {
          pos = i;
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
        final InputInfo info = info();
        final QNm name = eQName(QNAME_X, XQ_URI);

        final ItemList items = new ItemList();
        if(wsConsumeWs(PAREN1)) {
          do {
            final Expr ex = literal();
            if(!(ex instanceof Item)) throw error(ANNVALUE);
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
            throw (NSGlobal.reserved(uri) ? ANNWHICH_X_X : BASX_ANNOT_X_X).get(
                info, '%', name.string());
          }
          ann = new Ann(info, name, items.finish());

        } else {
          // check if annotation is specified more than once
          if(sig.single && anns.contains(sig)) throw BASX_TWICE_X_X.get(info, '%', sig.id());

          final long arity = items.size();
          if(arity < sig.minMax[0] || arity > sig.minMax[1])
            throw BASX_ANNNUM_X_X_X.get(info, sig, arity, arity == 1 ? "" : "s");
          final int al = sig.args.length;
          for(int a = 0; a < arity; a++) {
            final SeqType st = sig.args[Math.min(al - 1, a)];
            final Item it = items.get(a);
            if(!st.instance(it)) throw BASX_ANNTYPE_X_X_X.get(info, sig, st, it.seqType());
          }
          ann = new Ann(info, sig, items.finish());
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
    final QNm qnm = eQName(QNAME_X, XQ_URI);
    final byte[] val = stringLiteral();
    final String name = string(qnm.local());

    if(eq(qnm.uri(), OUTPUT_URI)) {
      // output declaration
      if(module != null) throw error(OPTDECL_X, qnm.string());

      final SerializerOptions sopts = qc.serParams();
      if(!decl.add("S " + name)) throw error(OUTDUPL_X, name);
      sopts.parse(name, val, sc, info());

    } else if(eq(qnm.uri(), DB_URI)) {
      // project-specific declaration
      if(module != null) throw error(BASX_OPTDECL_X, qnm.local());

      final String ukey = name.toUpperCase(Locale.ENGLISH);
      final Option<?> opt = qc.context.options.option(ukey);
      if(opt == null) throw error(BASX_OPTIONS_X, ukey);
      // cache old value (to be reset after query evaluation)
      qc.staticOpts.put(opt, qc.context.options.get(opt));
      qc.tempOpts.add(name).add(string(val));

    } else if(eq(qnm.uri(), QUERY_URI)) {
      // query-specific options
      switch(name) {
        case READ_LOCK:
          for(final byte[] lock : split(val, ','))
            qc.readLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
          break;
        case WRITE_LOCK:
          for(final byte[] lock : split(val, ','))
            qc.writeLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
          break;
        default:
          throw error(BASX_OPTIONS_X, name);
      }
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
    wsCheck(EMPTYORD);
    if(!decl.add(EMPTYORD)) throw error(DUPLORDEMP);
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
    final QNm name = def ? new QNm() : eQName(QNAME_X, null);

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
      if(map.isEmpty()) throw error(NODECLFORM_X, prop);
    } while(n != map.size());

    // completes the format declaration
    sc.decFormats.put(name.id(), new DecFormatter(info(), map));
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
    final byte[] base = stringLiteral();
    if(base.length != 0) sc.baseURI(string(base));
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
      for(final byte[] u : Function.URIS) if(eq(uri, u)) return;
      // try to resolve module uri
      if(qc.resources.modules().addImport(string(uri), mi.info, this)) return;
      // module not found
      throw WHICHMODULE_X.get(mi.info, uri);
    }
    // parse supplied paths
    for(final byte[] path : mi.paths) module(string(path), string(uri), mi.info);
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param path file path
   * @param uri module uri
   * @param info input info
   * @throws QueryException query exception
   */
  public void module(final String path, final String uri, final InputInfo info)
      throws QueryException {

    // get absolute path
    final IO io = sc.resolve(path, uri);
    final byte[] tPath = token(io.path());

    // check if module has already been parsed
    final byte[] tUri = token(uri), pUri = qc.modParsed.get(tPath);
    if(pUri != null) {
      if(!eq(tUri, pUri)) throw WRONGMODULE_X_X_X.get(info, io.name(), uri, pUri);
      return;
    }
    qc.modParsed.put(tPath, tUri);
    imports.put(tUri);

    // read module
    final String qu;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      throw error(WHICHMODFILE_X, io);
    }

    qc.modStack.push(tPath);
    final StaticContext sub = new StaticContext(qc);
    final LibraryModule lib = new QueryParser(qu, io.path(), qc, sub).parseLibrary(false);
    final byte[] muri = lib.name.uri();

    // check if import and declaration uri match
    if(!uri.equals(string(muri))) throw WRONGMODULE_X_X_X.get(info, io.name(), uri, muri);

    // check if context value declaration types are compatible to each other
    if(sub.contextType != null) {
      if(sc.contextType == null) {
        sc.contextType = sub.contextType;
      } else if(!sub.contextType.eq(sc.contextType)) {
        throw error(CITYPES_X_X, sub.contextType, sc.contextType);
      }
    }
    qc.modStack.pop();
  }

  /**
   * Parses the "ContextItemDecl" rule.
   * @throws QueryException query exception
   */
  private void contextItemDecl() throws QueryException {
    wsCheck(ITEMM);
    if(!decl.add(ITEMM)) throw error(DUPLITEM);

    if(wsConsumeWs(AS)) {
      final SeqType declType = itemType();
      if(sc.contextType == null) {
        sc.contextType = declType;
      } else if(!sc.contextType.eq(declType)) {
        throw error(CITYPES_X_X, sc.contextType, declType);
      }
    }

    if(!wsConsumeWs(EXTERNAL)) wsCheck(ASSIGN);
    else if(!wsConsumeWs(ASSIGN)) return;

    localVars.pushContext(null);
    final Expr e = check(single(), NOVARDECL);
    final SeqType declType = sc.contextType == null ? SeqType.ITEM : sc.contextType;
    final VarScope scope = localVars.popContext();
    qc.ctxItem = MainModule.get(e, scope, declType, currDoc.toString(), sc, info());

    if(module != null) throw error(DECITEM);
    if(!sc.mixUpdates && e.has(Flag.UPD)) throw error(UPCTX, e);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param anns annotations
   * @throws QueryException query exception
   */
  private void varDecl(final AnnList anns) throws QueryException {
    final QNm vn = varName();
    final SeqType tp = optAsType();
    if(module != null && !eq(vn.uri(), module.uri())) throw error(MODULENS_X, vn);

    localVars.pushContext(null);
    final boolean external = wsConsumeWs(EXTERNAL);
    final Expr bind;
    if(external) {
      bind = wsConsumeWs(ASSIGN) ? check(single(), NOVARDECL) : null;
    } else {
      wsCheck(ASSIGN);
      bind = check(single(), NOVARDECL);
    }

    final VarScope scope = localVars.popContext();
    final StaticVar var = qc.vars.declare(vn, tp, anns, bind, external, currDoc.toString(), sc,
        scope, info());
    vars.put(var.id(), var);
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
    if(module != null && !eq(name.uri(), module.uri())) throw error(MODULENS_X, name);

    localVars.pushContext(null);
    final Var[] args = paramList();
    wsCheck(PAREN2);

    final SeqType type = optAsType();
    final Expr expr = wsConsumeWs(EXTERNAL) ? null : enclosedExpr();
    final VarScope scope = localVars.popContext();
    final StaticFunc func = qc.funcs.declare(anns, name, args, type, expr, currDoc.toString(), sc,
        scope, ii);
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
    Var[] args = { };
    while(true) {
      skipWs();
      if(curr() != '$') {
        if(args.length == 0) break;
        check('$');
      }
      final Var var = localVars.add(varName(), optAsType(), true);
      for(final Var v : args)
        if(v.name.eq(var.name)) throw error(FUNCDUPL_X, var);

      args = Array.add(args, var);
      if(!consume(',')) break;
    }
    return args;
  }

  /**
   * Parses the "EnclosedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr enclosedExpr() throws QueryException {
    wsCheck(CURLY1);
    final Expr e = expr();
    wsCheck(CURLY2);
    return e == null ? Empty.SEQ : e;
  }

  /**
   * Parses the "Expr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr expr() throws QueryException {
    final Expr e = single();
    if(e == null) {
      if(more()) return null;
      throw alter == null ? error(NOEXPR) : error();
    }

    if(!wsConsume(COMMA)) return e;
    final ExprList el = new ExprList(e);
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
    Expr e = flwor();
    if(e == null) e = quantified();
    if(e == null) e = switchh();
    if(e == null) e = typeswitch();
    if(e == null) e = iff();
    if(e == null) e = tryCatch();
    if(e == null) e = insert();
    if(e == null) e = delete();
    if(e == null) e = rename();
    if(e == null) e = replace();
    if(e == null) e = updatingFunctionCall();
    if(e == null) e = copyModify();
    if(e == null) e = or();
    return e;
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
      for(final Var v : fl.vars()) curr.put(v.name.id(), v);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(final Clause c : clauses) {
          for(final Var v : c.vars()) curr.put(v.name.id(), v);
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
        final Spec[] specs = groupSpecs(clauses);

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<>();
        for(final Spec spec : specs) curr.put(spec.var.name.id(), spec.var);
        vars:
        for(final Var v : curr.values()) {
          for(final Spec spec : specs) if(spec.var.is(v)) continue vars;
          ng.add(new VarRef(specs[0].info, v));
        }

        // add new copies for all non-grouping variables
        final Var[] ngrp = new Var[ng.size()];
        for(int i = ng.size(); --i >= 0;) {
          final VarRef v = ng.get(i);

          // if one groups variables such as $x as xs:integer, then the resulting
          // sequence isn't compatible with the type and can't be assigned
          final Var nv = localVars.add(v.var.name, null, false);
          // [LW] should be done everywhere
          if(v.seqType().one())
            nv.refineType(SeqType.get(v.seqType().type, Occ.ONE_MORE), qc, info());
          ngrp[i] = nv;
          curr.put(nv.name.id(), nv);
        }

        final VarRef[] pre = new VarRef[ng.size()];
        clauses.add(new GroupBy(specs, ng.toArray(pre), ngrp, specs[0].info));
      }

      final boolean stable = wsConsumeWs(STABLE);
      if(stable) wsCheck(ORDER);
      if(stable || wsConsumeWs(ORDER)) {
        wsCheck(BY);
        alterPos = pos;
        Key[] ob = null;
        do {
          final Key key = orderSpec();
          ob = ob == null ? new Key[] { key } : Array.add(ob, key);
        } while(wsConsume(COMMA));

        final VarRef[] vs = new VarRef[curr.size()];
        int i = 0;
        for(final Var v : curr.values()) vs[i++] = new VarRef(ob[0].info, v);
        clauses.add(new OrderBy(vs, ob, ob[0].info));
      }

      if(wsConsumeWs(COUNT, DOLLAR, NOCOUNT)) {
        final Var v = localVars.add(varName(), SeqType.ITR, false);
        curr.put(v.name.id(), v);
        clauses.add(new Count(v, info()));
      }
    } while(size < clauses.size());

    if(!wsConsumeWs(RETURN)) throw alter == null ? error(FLWORRETURN) : error();

    final Expr ret = check(single(), NORETURN);
    localVars.closeScope(s);
    return new GFLWOR(clauses.get(0).info, clauses, ret);
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
   * @param cls list of clauses
   * @throws QueryException parse exception
   */
  private void forClause(final LinkedList<Clause> cls) throws QueryException {
    do {
      final QNm nm = varName();
      final SeqType tp = optAsType();

      final boolean emp = wsConsume(ALLOWING);
      if(emp) wsCheck(EMPTYORD);

      final QNm p = wsConsumeWs(AT) ? varName() : null;
      final QNm s = wsConsumeWs(SCORE) ? varName() : null;

      wsCheck(IN);
      final Expr e = check(single(), NOVARDECL);

      // declare late because otherwise it would shadow the wrong variables
      final Var var = localVars.add(nm, tp, false);
      final Var ps = p != null ? localVars.add(p, SeqType.ITR, false) : null;
      final Var scr = s != null ? localVars.add(s, SeqType.DBL, false) : null;
      if(p != null) {
        if(nm.eq(p)) throw error(DUPLVAR_X, var);
        if(s != null && p.eq(s)) throw error(DUPLVAR_X, ps);
      }
      if(s != null && nm.eq(s)) throw error(DUPLVAR_X, var);

      cls.add(new For(var, ps, scr, e, emp, info()));
    } while(wsConsumeWs(COMMA));
  }

  /**
   * Parses the "LetClause" rule.
   * Parses the "FTScoreVar" rule.
   * @param cls list of clauses
   * @throws QueryException parse exception
   */
  private void letClause(final LinkedList<Clause> cls) throws QueryException {
    do {
      final boolean score = wsConsumeWs(SCORE);
      final QNm nm = varName();
      final SeqType tp = score ? SeqType.DBL : optAsType();
      wsCheck(ASSIGN);
      final Expr e = check(single(), NOVARDECL);
      cls.add(new Let(localVars.add(nm, tp, false), e, score, info()));
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

    final QNm nm = varName();
    final SeqType tp = optAsType();

    wsCheck(IN);
    final Expr e = check(single(), NOVARDECL);

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
    return new Window(info(), slide, localVars.add(nm, tp, false), e, start, only, end);
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
    final Var var = curr('$')             ? localVars.add(varName(), null, false) : null,
              at  = wsConsumeWs(AT)       ? localVars.add(varName(), null, false) : null,
              prv = wsConsumeWs(PREVIOUS) ? localVars.add(varName(), null, false) : null,
              nxt = wsConsumeWs(NEXT)     ? localVars.add(varName(), null, false) : null;
    wsCheck(WHEN);
    return new Condition(start, var, at, prv, nxt, check(single(), NOEXPR), ii);
  }

  /**
   * Parses the "OrderSpec" rule.
   * Parses the "OrderModifier" rule.
   *
   * Empty order specs are ignored, {@code order} is then returned unchanged.
   * @return new order key
   * @throws QueryException query exception
   */
  private Key orderSpec() throws QueryException {
    final Expr e = check(single(), ORDERBY);

    boolean desc = false;
    if(!wsConsumeWs(ASCENDING)) desc = wsConsumeWs(DESCENDING);
    boolean least = !sc.orderGreatest;
    if(wsConsumeWs(EMPTYORD)) {
      least = !wsConsumeWs(GREATEST);
      if(least) wsCheck(LEAST);
    }
    final Collation coll = wsConsumeWs(COLLATION) ?
      Collation.get(stringLiteral(), qc, sc, info(), FLWORCOLL_X) : sc.collation;
    return new Key(info(), e, desc, least, coll);
  }

  /**
   * Parses the "GroupingSpec" rule.
   * @param cl preceding clauses
   * @return new group specification
   * @throws QueryException query exception
   */
  private Spec[] groupSpecs(final LinkedList<Clause> cl) throws QueryException {
    Spec[] specs = null;
    do {
      final InputInfo ii = info();
      final QNm name = varName();
      final SeqType declType = optAsType();

      final Expr by;
      if(declType != null || wsConsume(ASSIGN)) {
        if(declType != null) wsCheck(ASSIGN);
        by = check(single(), NOVARDECL);
      } else {
        final VarRef vr = localVars.resolveLocal(name, ii);
        // the grouping variable has to be declared by the same FLWOR expression
        boolean dec = false;
        if(vr != null) {
          // check preceding clauses
          for(final Clause f : cl) {
            if(f.declares(vr.var)) {
              dec = true;
              break;
            }
          }

          // check other grouping variables
          if(!dec && specs != null) {
            for(final Spec spec : specs) {
              if(spec.var.is(vr.var)) {
                dec = true;
                break;
              }
            }
          }
        }
        if(!dec) throw error(GVARNOTDEFINED_X, '$' + string(name.string()));
        by = vr;
      }

      final Collation coll = wsConsumeWs(COLLATION) ? Collation.get(stringLiteral(),
          qc, sc, info(), FLWORCOLL_X) : sc.collation;
      final Spec spec = new Spec(ii, localVars.add(name, declType, false), by, coll);
      if(specs == null) {
        specs = new Spec[] { spec };
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
    For[] fl = { };
    do {
      final QNm nm = varName();
      final SeqType tp = optAsType();
      wsCheck(IN);
      final Expr e = check(single(), NOSOME);
      fl = Array.add(fl, new For(localVars.add(nm, tp, false), null, null, e, false, info()));
    } while(wsConsumeWs(COMMA));

    wsCheck(SATISFIES);
    final Expr e = check(single(), NOSOME);
    localVars.closeScope(s);
    return new Quantifier(info(), fl, e, !some, sc);
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
    SwitchCase[] cases = { };
    wsCheck(PAREN2);

    // collect all cases
    ExprList exprs;
    do {
      exprs = new ExprList(null);
      while(wsConsumeWs(CASE)) add(exprs, single());
      if(exprs.size() == 1) {
        // add default case
        if(cases.length == 0) throw error(WRONGCHAR_X_X, CASE, found());
        wsCheck(DEFAULT);
      }
      wsCheck(RETURN);
      exprs.set(0, check(single(), NOSWITCH));
      cases = Array.add(cases, new SwitchCase(info(), exprs.finish()));
    } while(exprs.size() != 1);

    return new Switch(ii, cond, cases);
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

    TypeCase[] cases = { };
    final ArrayList<SeqType> types = new ArrayList<>();
    final int s = localVars.openScope();
    boolean cs;
    do {
      types.clear();
      cs = wsConsumeWs(CASE);
      if(!cs) {
        wsCheck(DEFAULT);
        skipWs();
      }
      Var var = null;
      if(curr('$')) {
        var = localVars.add(varName(), null, false);
        if(cs) wsCheck(AS);
      }
      if(cs) {
        do types.add(sequenceType());
        while(wsConsume(PIPE));
      }
      wsCheck(RETURN);
      final Expr ret = check(single(), NOTYPESWITCH);
      cases = Array.add(cases, new TypeCase(info(), var,
          types.toArray(new SeqType[types.size()]), ret));
      localVars.closeScope(s);
    } while(cs);
    if(cases.length == 1) throw error(NOTYPESWITCH);
    return new TypeSwitch(ii, ts, cases);
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
    if(!wsConsumeWs(ELSE)) throw error(NOIF);
    final Expr els = check(single(), NOIF);
    return new If(ii, iff, thn, els);
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    if(!wsConsumeWs(OR)) return e;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(e);
    do add(el, and()); while(wsConsumeWs(OR));
    return new Or(ii, el.finish());
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr e = update();
    if(!wsConsumeWs(AND)) return e;

    final InputInfo ii = info();
    final ExprList el = new ExprList(2).add(e);
    do add(el, update()); while(wsConsumeWs(AND));
    return new And(ii, el.finish());
  }

  /**
   * Parses the "UpdateExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr update() throws QueryException {
    Expr e = comparison();
    if(e != null) {
      while(wsConsumeWs(UPDATE)) {
        qc.updating();
        e = new TransformWith(info(), e, curr('{') ? enclosedExpr() : check(single(), UPDATEEXPR));
      }
    }
    return e;
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr e = ftContains();
    if(e != null) {
      for(final OpV c : OpV.VALUES) if(wsConsumeWs(c.name))
        return new CmpV(e, check(ftContains(), CMPEXPR), c, sc.collation, sc, info());
      for(final OpN c : OpN.VALUES) if(wsConsumeWs(c.name))
        return new CmpN(e, check(ftContains(), CMPEXPR), c, info());
      for(final OpG c : OpG.VALUES) if(wsConsumeWs(c.name))
        return new CmpG(e, check(ftContains(), CMPEXPR), c, sc.collation, sc, info());
    }
    return e;
  }

  /**
   * Parses the "FTContainsExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr ftContains() throws QueryException {
    final Expr e = stringConcat();

    final int i = pos;
    if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      pos = i;
      return e;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      throw error(FTIGNORE);
    }
    return new FTContains(e, select, info());
  }

  /**
   * Parses the "StringConcatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr stringConcat() throws QueryException {
    final Expr e = range();
    if(e == null || !consume(CONCAT)) return e;

    final ExprList el = new ExprList(e);
    do add(el, range()); while(wsConsume(CONCAT));
    return Function.CONCAT.get(sc, info(), el.finish());
  }

  /**
   * Parses the "RangeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr e = additive();
    if(!wsConsumeWs(TO)) return e;
    return new Range(info(), e, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr additive() throws QueryException {
    Expr e = multiplicative();
    while(e != null) {
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ? Calc.MINUS : null;
      if(c == null) break;
      e = new Arith(info(), e, check(multiplicative(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * Parses the "MultiplicativeExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr multiplicative() throws QueryException {
    Expr e = union();
    while(e != null) {
      final Calc c = consume('*') ? Calc.MULT : wsConsumeWs(DIV) ? Calc.DIV
          : wsConsumeWs(IDIV) ? Calc.IDIV : wsConsumeWs(MOD) ? Calc.MOD : null;
      if(c == null) break;
      e = new Arith(info(), e, check(union(), CALCEXPR), c);
    }
    return e;
  }

  /**
   * Parses the "UnionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr union() throws QueryException {
    final Expr e = intersect();
    if(e == null || !isUnion()) return e;
    final ExprList el = new ExprList(e);
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
    final int i = pos;
    if(consume(PIPE) && !consume(PIPE)) return true;
    pos = i;
    return false;
  }

  /**
   * Parses the "IntersectExceptExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr intersect() throws QueryException {
    Expr e = instanceoff();
    boolean lastIs = false;
    ExprList el = null;
    while(true) {
      boolean is = wsConsumeWs(INTERSECT);
      if(!is && !wsConsumeWs(EXCEPT)) break;
      if((is != lastIs) && el != null) {
        e = intersectExcept(lastIs, el);
        el = null;
      }
      lastIs = is;
      if(el == null) el = new ExprList(e);
      add(el, instanceoff());
    }
    return el != null ? intersectExcept(lastIs, el) : e;
  }

  /**
   * Parses the "IntersectExceptExpr" rule.
   * @param intersect intersect flag
   * @param el expression list
   * @return expression
   */
  private Expr intersectExcept(final boolean intersect, final ExprList el) {
    return intersect ? new InterSect(info(), el.finish()) : new Except(info(), el.finish());
  }

  /**
   * Parses the "InstanceofExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr instanceoff() throws QueryException {
    final Expr e = treat();
    if(!wsConsumeWs(INSTANCE)) return e;
    wsCheck(OF);
    return new Instance(info(), e, sequenceType());
  }

  /**
   * Parses the "TreatExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr treat() throws QueryException {
    final Expr e = castable();
    if(!wsConsumeWs(TREAT)) return e;
    wsCheck(AS);
    return new Treat(info(), e, sequenceType());
  }

  /**
   * Parses the "CastableExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr castable() throws QueryException {
    final Expr e = cast();
    if(!wsConsumeWs(CASTABLE)) return e;
    wsCheck(AS);
    return new Castable(sc, info(), e, simpleType());
  }

  /**
   * Parses the "CastExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr e = transformWith();
    if(!wsConsumeWs(CAST)) return e;
    wsCheck(AS);
    return new Cast(sc, info(), e, simpleType());
  }

  /**
   * Parses the "TransformWithExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr transformWith() throws QueryException {
    final Expr e = arrow();
    final int p = pos;
    if(e != null && wsConsume(TRANSFORM) && wsConsume(WITH)) {
      qc.updating();
      return new TransformWith(info(), e, enclosedExpr());
    }
    pos = p;
    return e;
  }

  /**
   * Parses the "ArrowExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr arrow() throws QueryException {
    Expr e = unary();
    if(e != null) {
      while(wsConsume(ARROW)) {
        skipWs();
        final Expr ex = curr('(') ? parenthesized() : curr('$')
            ? localVars.resolve(varName(), info()) : eQName(ARROWSPEC, sc.funcNS);
        final InputInfo ii = info();
        wsCheck(PAREN1);

        if(ex instanceof QNm) {
          final QNm name = (QNm) ex;
          if(keyword(name)) throw error(RESERVED_X, name.local());
          e = function(name, ii, new ExprList(e));
        } else {
          final ExprList argList = new ExprList(e);
          final int[] holes = argumentList(argList, e);
          final Expr[] args = argList.finish();
          if(holes == null) {
            e = new DynFuncCall(ii, sc, ex, args);
          } else {
            e = new PartFunc(sc, ii, ex, args, holes);
          }
        }
      }
    }
    return e;
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
        final Expr e = value();
        return found ? new Unary(info(), check(e, EVALUNARY), minus) : e;
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
    final Expr e = extension();
    return e == null ? map() : e;
  }

  /**
   * Parses the "ValidateExpr" rule.
   * @throws QueryException query exception
   */
  private void validate() throws QueryException {
    final int i = pos;
    if(!wsConsumeWs(VALIDATE)) return;

    if(consume(TYPE)) qnames.add(eQName(QNAME_X, SKIPCHECK));
    consume(STRICT);
    consume(LAX);
    skipWs();
    if(curr('{')) {
      enclosedExpr();
      throw error(IMPLVAL);
    }
    pos = i;
  }

  /**
   * Parses the "ExtensionExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr extension() throws QueryException {
    final Pragma[] pragmas = pragma();
    if(pragmas == null) return null;
    final Expr expr = enclosedExpr();
    return pragmas.length == 0 ? expr : new Extension(info(), pragmas, expr);
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
      final QNm name = eQName(QNAME_X, URICHECK);
      char c = curr();
      if(c != '#' && !ws(c)) throw error(PRAGMAINV);
      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) throw error(PRAGMAINV);
        tok.add(consume());
        c = curr();
      }

      final byte[] value = tok.trim().toArray();
      if(eq(name.prefix(), DB_PREFIX)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final Option<?> opt = qc.context.options.option(key);
        if(opt == null) throw error(BASX_OPTIONS_X, key);
        el.add(new DBPragma(name, opt, value));
      } else if(eq(name.prefix(), BASEX_PREFIX)) {
        // project-specific declaration
        el.add(new BaseXPragma(name, value));
      }
      pos += 2;
    } while(wsConsumeWs(PRAGMA));
    return el.toArray(new Pragma[el.size()]);
  }

  /**
   * Parses the "MapExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr map() throws QueryException {
    final Expr e = path();
    if(e != null && next() != '=' && wsConsumeWs(EXCL)) {
      final ExprList el = new ExprList(e);
      do add(el, path()); while(next() != '=' && wsConsumeWs(EXCL));
      return SimpleMap.get(info(), el.finish());
    }
    return e;
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
      root = new Root(info());
      el = new ExprList();
      final Expr ex;
      if(consume('/')) {
        // two slashes: absolute descendant path
        checkAxis(Axis.DESC);
        add(el, Step.get(info(), Axis.DESCORSELF, Test.NOD));
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
          add(el, Step.get(info(), Axis.DESCORSELF, Test.NOD));
          checkAxis(Axis.DESC);
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
   * @param attr attribute flag
   */
  @SuppressWarnings("unused")
  void checkTest(final Test test, final boolean attr) { }

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
    final Expr e = postfix();
    return e != null ? e : axisStep(error);
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
    if(wsConsume(DOT2)) {
      axis = Axis.PARENT;
      test = Test.NOD;
      checkTest(test, false);
    } else if(consume('@')) {
      axis = Axis.ATTR;
      test = nodeTest(true, true);
      checkTest(test, true);
      if(test == null) {
        --pos;
        throw error(NOATTNAME);
      }
    } else {
      for(final Axis ax : Axis.VALUES) {
        final int i = pos;
        if(!wsConsumeWs(ax.name)) continue;
        if(wsConsumeWs(COLS)) {
          alterPos = pos;
          axis = ax;
          final boolean attr = ax == Axis.ATTR;
          test = nodeTest(attr, true);
          checkTest(test, attr);
          if(test == null) throw error(AXISMISS_X, axis);
          break;
        }
        pos = i;
      }

      if(axis == null) {
        axis = Axis.CHILD;
        test = nodeTest(false, true);
        if(test == Test.NSP) throw error(NSAXIS);
        if(test != null && test.type == NodeType.ATT) axis = Axis.ATTR;
        checkTest(test, axis == Axis.ATTR);
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
    return Step.get(info(), axis, test, el.finish());
  }

  /**
   * Parses the "NodeTest" rule.
   * Parses the "NameTest" rule.
   * Parses the "KindTest" rule.
   * @param att attribute flag
   * @param all check all tests, or only names
   * @return test or {@code null}
   * @throws QueryException query exception
   */
  private Test nodeTest(final boolean att, final boolean all) throws QueryException {
    final int i = pos;
    if(consume('*')) {
      // name test: *
      if(!consume(':')) return new NameTest(att);
      // name test: *:name
      return new NameTest(new QNm(ncName(QNAME_X)), Kind.NAME, att, sc.elemNS);
    }

    if(consume(EQNAME)) {
      // name test: Q{uri}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        final QNm nm = new QNm(COLON, uri);
        return new NameTest(nm, Kind.URI, att, sc.elemNS);
      }
    }
    pos = i;

    final QNm name = eQName(null, SKIPCHECK);
    if(name != null) {
      final int i2 = pos;
      if(all && wsConsumeWs(PAREN1)) {
        final NodeType type = NodeType.find(name);
        if(type != null) return kindTest(type);
      } else {
        pos = i2;
        // name test: prefix:*
        if(consume(":*") && !name.hasPrefix()) {
          final QNm nm = new QNm(concat(name.string(), COLON));
          qnames.add(nm, !att);
          return new NameTest(nm, Kind.URI, att, sc.elemNS);
        }
        // name test: prefix:name, name, Q{uri}name
        skipWs();
        qnames.add(name, !att);
        return new NameTest(name, Kind.URI_NAME, att, sc.elemNS);
      }
    }
    pos = i;
    return null;
  }

  /**
   * Parses the "PostfixExpr" rule.
   * @return postfix expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr postfix() throws QueryException {
    Expr e = primary(), old;
    if(e != null) {
      do {
        old = e;
        if(wsConsume(SQUARE1)) {
          // parses the "Predicate" rule
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck(SQUARE2);
          } while(wsConsume(SQUARE1));
          e = new CachedFilter(info(), e, el.finish());
        } else if(consume(PAREN1)) {
          if(e instanceof Value && !(e instanceof FItem)) throw error(NOPAREN_X_X, e);
          // parses the "ArgumentList" rule
          final InputInfo ii = info();
          final ExprList argList = new ExprList();
          final int[] holes = argumentList(argList, e);
          final Expr[] args = argList.finish();
          if(holes == null) {
            e = new DynFuncCall(ii, sc, e, args);
          } else {
            e = new PartFunc(sc, ii, e, args, holes);
          }
        } else if(consume(QUESTION)) {
          // parses the "Lookup" rule
          e = new Lookup(info(), sc, keySpecifier(), e);
        }
      } while(e != old);
    }
    return e;
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
    final char c = curr();
    // variables
    if(c == '$') {
      final InputInfo ii = info();
      return localVars.resolve(varName(), ii);
    }
    // parentheses
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return dirConstructor();
    // string constructor
    if(c == '`') return stringConstructor();
    // function item
    Expr e = functionItem();
    if(e != null) return e;
    // function call
    e = functionCall();
    if(e != null) return e;
    // computed constructors
    e = compConstructor();
    if(e != null) return e;
    // ordered expression
    final int p = pos;
    if(wsConsumeWs(ORDERED) || wsConsumeWs(UNORDERED)) {
      if(curr('{')) return enclosedExpr();
      pos = p;
    }
    // map constructor
    if(wsConsumeWs(MAP, CURLY1, INCOMPLETE)) return new CMap(info(), keyValues());
    // square array constructor
    if(wsConsumeWs(SQUARE1)) return new CArray(info(), false, values());
    // curly array constructor
    if(wsConsumeWs(ARRAY, CURLY1, INCOMPLETE)) {
      wsCheck(CURLY1);
      final Expr a = expr();
      wsCheck(CURLY2);
      return a == null ? new CArray(info(), true) : new CArray(info(), true, a);
    }
    // unary lookup
    final int ip = pos;
    if(consume(QUESTION)) {
      if(!wsConsume(COMMA) && !consume(PAREN2)) return new Lookup(info(), sc, keySpecifier());
      pos = ip;
    }
    // context value
    if(c == '.') {
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
    final int c = curr();
    if(c == '*') {
      consume();
      return Str.WC;
    }
    return c == '(' ? parenthesized() :
      digit(c) ? numericLiteral(true) :
      Str.get(ncName(KEYSPEC));
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
    final int ip = pos;

    // parse annotations; will only be visited for XQuery 3.0 expressions
    final AnnList anns = annotations(false).check(false);
    // inline function
    if(wsConsume(FUNCTION) && wsConsume(PAREN1)) {
      if(anns.contains(Annotation.PRIVATE) || anns.contains(Annotation.PUBLIC))
        throw error(NOVISALLOWED);

      final HashMap<Var, Expr> global = new HashMap<>();
      localVars.pushContext(global);
      final Var[] args = paramList();
      wsCheck(PAREN2);
      final SeqType type = optAsType();
      final Expr body = enclosedExpr();
      final VarScope scope = localVars.popContext();
      return new Closure(info(), type, args, body, anns, global, sc, scope);
    }
    // annotations not allowed here
    if(!anns.isEmpty()) throw error(NOANN);

    // named function reference
    pos = ip;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && wsConsumeWs("#")) {
      if(keyword(name)) throw error(RESERVED_X, name.local());
      final Expr ex = numericLiteral(true);
      if(!(ex instanceof Int)) return ex;
      final int card = (int) ((ANum) ex).itr();
      final Expr lit = Functions.getLiteral(name, card, qc, sc, info(), false);
      return lit != null ? lit : unknownLit(name, card, info());
    }

    pos = ip;
    return null;
  }

  /**
   * Creates and registers a function literal.
   * @param name function name
   * @param card cardinality
   * @param ii input info
   * @return the literal
   * @throws QueryException query exception
   */
  private Closure unknownLit(final QNm name, final int card, final InputInfo ii)
      throws QueryException {
    final Closure lit = Closure.unknownLit(name, card, qc, sc, ii);
    qc.funcs.registerFuncLit(lit);
    return lit;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr literal() throws QueryException {
    final char c = curr();
    // literals
    if(digit(c) || c == '.') return numericLiteral(false);
    // strings
    return quote(c) ? Str.get(stringLiteral()) : null;
  }

  /**
   * Parses the "NumericLiteral" rule.
   * Parses the "DecimalLiteral" rule.
   * Parses the "IntegerLiteral" rule.
   * @param itr integer flag
   * @return numeric literal
   * @throws QueryException query exception
   */
  private Expr numericLiteral(final boolean itr) throws QueryException {
    tok.reset();
    while(digit(curr())) tok.add(consume());

    final boolean dot = consume('.');
    if(dot) {
      // decimal literal
      tok.add('.');
      if(itr) throw error(NUMBERITR, tok);
      while(digit(curr())) tok.add(consume());
    }

    if(XMLToken.isNCStartChar(curr())) {
      if(!consume('e') && !consume('E')) throw error(NUMBERWS);
      // double literal
      tok.add('e');
      if(curr('+') || curr('-')) tok.add(consume());
      final int s = tok.size();
      while(digit(curr())) tok.add(consume());
      if(s == tok.size()) throw error(NUMBER_X, tok);

      if(XMLToken.isNCStartChar(curr())) throw error(NUMBERWS);
      return Dbl.get(tok.toArray(), info());
    }

    final byte[] tmp = tok.toArray();
    final int tl = tmp.length;
    if(tl == 0) throw error(NUMBER_X, tmp);
    if(dot) {
      if(tl == 1 && tmp[0] == '.') throw error(NUMBER_X, tmp);
      return Dec.get(new BigDecimal(string(trim(tmp))));
    }

    final long l = toLong(tmp);
    if(l != Long.MIN_VALUE) return Int.get(l);

    final InputInfo ii = info();
    return FnError.get(RANGE_X.get(ii, chop(tok, ii)), SeqType.ITR, sc);
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
    tok.reset();
    while(true) {
      while(!consume(del)) {
        if(!more()) throw error(NOQUOTE_X, found());
        entity(tok);
      }
      if(!consume(del)) break;
      tok.add(del);
    }
    return tok.toArray();
  }

  /**
   * Parses the "BracedURILiteral" rule without the "Q{" prefix.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] bracedURILiteral() throws QueryException {
    tok.reset();
    while(!consume('}')) {
      if(!more()) throw error(WRONGCHAR_X_X, CURLY2, found());
      entity(tok);
    }
    return tok.toArray();
  }

  /**
   * Parses the "VarName" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private QNm varName() throws QueryException {
    check('$');
    skipWs();
    return eQName(NOVARNAME, null);
  }

  /**
   * Parses the "ParenthesizedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr parenthesized() throws QueryException {
    check('(');
    final Expr e = expr();
    wsCheck(PAREN2);
    return e == null ? Empty.SEQ : e;
  }

  /**
   * Parses the "FunctionCall" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int i = pos;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && !keyword(name)) {
      final InputInfo ii = info();
      if(wsConsume(PAREN1)) {
        final Expr ret = function(name, ii, new ExprList());
        if(ret != null) return ret;
      }
    }
    pos = i;
    return null;
  }

  /**
   * Returns a function.
   * @param name function name
   * @param ii input info
   * @param argList list of arguments
   * @return function or {@code null}
   * @throws QueryException query exception
   */
  private Expr function(final QNm name, final InputInfo ii, final ExprList argList)
      throws QueryException {
    final int[] holes = argumentList(argList, name.string());
    final Expr[] args = argList.finish();
    alter = WHICHFUNC_X;
    alterFunc = name;
    alterPos = pos;

    final Expr ret;
    if(holes != null) {
      final int card = args.length + holes.length;
      final Expr lit = Functions.getLiteral(name, card, qc, sc, ii, false);
      final Expr f = lit != null ? lit : unknownLit(name, card, ii);
      ret = new PartFunc(sc, ii, f, args, holes);
    } else {
      final TypedFunc f = Functions.get(name, args, qc, sc, ii);
      ret = f != null ? f.fun : null;
    }

    if(ret != null) alter = null;
    return ret;
  }

  /**
   * Parses the "ArgumentList" rule without the opening parenthesis.
   * @param args list to put the argument expressions into
   * @param name name of the function (item); only required for error messages
   * @return array of arguments, place-holders '?' are represented as {@code null} entries
   * @throws QueryException query exception
   */
  private int[] argumentList(final ExprList args, final Object name) throws QueryException {
    int[] holes = null;
    if(!wsConsume(PAREN2)) {
      int i = args.size();
      do {
        final Expr e = single();
        if(e != null) {
          args.add(e);
        } else if(wsConsume(QUESTION)) {
          holes = holes == null ? new int[] { i } : Array.add(holes, i);
        } else {
          throw funcMiss(name);
        }
        i++;
      } while(wsConsume(COMMA));
      if(!wsConsume(PAREN2)) throw funcMiss(name);
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
        return Function.CONCAT.get(sc, info(), el.finish());
      }
      pos = p;
      if(consume('`') && consume('{')) {
        if(!tb.isEmpty()) el.add(Str.get(tb.next()));
        final Expr e = expr();
        if(e != null) el.add(Function.STRING_JOIN.get(sc, info(), e, Str.get(" ")));
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
    final int s = sc.ns.size();
    final byte[] nse = sc.elemNS;
    final int npos = qnames.size();

    final QNm name = new QNm(qName(ELEMNAME_X));
    qnames.add(name);
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
          if(ch == '{') {
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
          } else if(ch == '}') {
            consume();
            check('}');
            tb.add('}');
          } else if(ch == '<' || ch == 0) {
            throw error(NOQUOTE_X, found());
          } else if(ch == '\n' || ch == '\t') {
            tb.add(' ');
            consume();
          } else if(ch == '\r') {
            if(next() != '\n') tb.add(' ');
            consume();
          } else {
            entity(tb);
          }
        }
        if(!consume(delim)) break;
        tb.add(delim);
      } while(true);

      if(!tb.isEmpty()) add(attv, Str.get(tb.finish()));

      // parse namespace declarations
      final boolean pr = startsWith(atn, XMLNSC);
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
        qnames.add(attn, false);
        add(cont, new CAttr(sc, info(), false, attn, attv.finish()));
      }
      if(!consumeWS()) break;
    }

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(curr() != '<' || next() != '/') {
        final Expr e = dirElemContent(name.string());
        if(e != null) add(cont, e);
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

    sc.ns.size(s);
    sc.elemNS = nse;
    return new CElem(sc, info(), name, ns, cont.finish());
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
      final char c = curr();
      if(c == '<') {
        if(wsConsume(CDATA)) {
          tb.add(cDataSection());
          strip = false;
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : next() == '/' ? null : dirConstructor();
        }
      } else if(c == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : enclosedExpr();
        }
      } else if(c == '}') {
        consume();
        check('}');
        tb.add('}');
      } else if(c != 0) {
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
    final byte[] t = tb.toArray();
    return t.length == 0 || strip && !sc.spaces && ws(t) ? null : Str.get(t);
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
        return new CComm(sc, info(), Str.get(tb.finish()));
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
        return new CPI(sc, info(), Str.get(str), Str.get(tb.finish()));
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
    final int i = pos;
    if(wsConsumeWs(DOCUMENT))  return consume(compDoc(), i);
    if(wsConsumeWs(ELEMENT))   return consume(compElement(), i);
    if(wsConsumeWs(ATTRIBUTE)) return consume(compAttribute(), i);
    if(wsConsumeWs(NAMESPACE))    return consume(compNamespace(), i);
    if(wsConsumeWs(TEXT))      return consume(compText(), i);
    if(wsConsumeWs(COMMENT))   return consume(compComment(), i);
    if(wsConsumeWs(PI))        return consume(compPI(), i);
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
    return curr('{') ? new CDoc(sc, info(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompElemConstructor" rule.
   * Parses the "ContextExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compElement() throws QueryException {
    skipWs();

    final Expr name;
    final QNm qn = eQName(null, SKIPCHECK);
    if(qn != null) {
      name = qn;
      qnames.add(qn);
    } else {
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NOELEMNAME);
      wsCheck(CURLY2);
    }

    skipWs();
    return curr('{') ? new CElem(sc, info(), name, null, enclosedExpr()) : null;
  }

  /**
   * Parses the "CompAttrConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    skipWs();

    final Expr name;
    final QNm qn = eQName(null, SKIPCHECK);
    if(qn != null) {
      name = qn;
      qnames.add(qn, false);
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
   * @return query expression
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
    return curr('{') ? new CNSpace(sc, info(), name, enclosedExpr()) : null;
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
    return curr('{') ? new CComm(sc, info(), enclosedExpr()) : null;
  }

  /**
   * Parses the "CompPIConstructor" rule.
   * @return query expression
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
    return curr('{') ? new CPI(sc, info(), name, enclosedExpr()) : null;
  }

  /**
   * Parses the "SimpleType" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType simpleType() throws QueryException {
    skipWs();
    final QNm name = eQName(TYPEINVALID, sc.elemNS);
    Type t = ListType.find(name);
    if(t == null) {
      t = AtomType.find(name, false);
      if(t == null) {
        if(wsConsume(PAREN1)) throw error(SIMPLETYPE_X, name);
        if(!AtomType.AST.name.eq(name)) throw error(TYPE30_X, name.prefixId(XML));
        t = AtomType.AST;
      }
      if(t == AtomType.AST || t == AtomType.AAT || t == AtomType.NOT)
        throw error(CASTUNKNOWN_X, name.prefixId(XML));
    }
    skipWs();
    return SeqType.get(t, consume('?') ? Occ.ZERO_ONE : Occ.ONE);
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
      return SeqType.get(AtomType.ITEM, Occ.ZERO);
    }

    // parse item type and occurrence indicator
    final SeqType tw = itemType();
    skipWs();
    final Occ occ = consume('?') ? Occ.ZERO_ONE : consume('+') ? Occ.ONE_MORE :
      consume('*') ? Occ.ZERO_MORE : Occ.ONE;
    skipWs();
    return tw.withOcc(occ);
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
    final AnnList anns = annotations(false).check(false);
    final QNm name = eQName(TYPEINVALID, null);
    skipWs();
    // check if name is followed by parentheses
    final boolean func = curr('(');

    // item type
    Type t = null;
    if(func) {
      consume(PAREN1);
      // item type
      if(name.eq(AtomType.ITEM.name)) t = AtomType.ITEM;
      // node types
      if(t == null) t = NodeType.find(name);
      // function types
      if(t == null) {
        t = FuncType.find(name);
        if(t != null) return functionTest(anns, t).seqType();
      }
      // no type found
      if(t == null) throw error(WHICHTYPE_X, name.prefixId(XML));
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(sc.elemNS);
      // atomic types
      t = AtomType.find(name, false);
      // no type found
      if(t == null) throw error(TYPEUNKNOWN_X, name.prefixId(XML));
    }

    // annotations are not allowed for remaining types
    if(!anns.isEmpty()) throw error(NOANN);

    // atomic value, or closing parenthesis
    if(!func || wsConsume(PAREN2)) return t.seqType();

    // raise error if type different to node is not finalized by a parenthesis
    if(!(t instanceof NodeType)) wsCheck(PAREN2);

    // return type with an optional kind test for node types
    return SeqType.get(t, Occ.ONE, kindTest((NodeType) t));
  }

  /**
   * Parses the "FunctionTest" rule.
   * @param anns annotations
   * @param t function type
   * @return resulting type
   * @throws QueryException query exception
   */
  private Type functionTest(final AnnList anns, final Type t) throws QueryException {
    // wildcard
    if(wsConsume(ASTERISK)) {
      wsCheck(PAREN2);
      return t;
    }

    // map
    if(t instanceof MapType) {
      final Type key = itemType().type;
      if(!key.instanceOf(AtomType.AAT)) throw error(MAPTAAT_X, key);
      wsCheck(COMMA);
      final MapType tp = MapType.get((AtomType) key, sequenceType());
      wsCheck(PAREN2);
      return tp;
    }
    // array
    if(t instanceof ArrayType) {
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
   * @param t type
   * @return arguments
   * @throws QueryException query exception
   */
  private Test kindTest(final NodeType t) throws QueryException {
    Test tp = null;
    switch(t) {
      case DOC: tp = documentTest(); break;
      case ELM: tp = elementTest(); break;
      case ATT: tp = attributeTest(); break;
      case PI:  tp = piTest(); break;
      case SCE:
      case SCA: tp = schemaTest(); break;
      default:  break;
    }
    wsCheck(PAREN2);
    return tp == null ? Test.get(t) : tp;
  }

  /**
   * Parses the "DocumentTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test documentTest() throws QueryException {
    final boolean elem = consume(ELEMENT);
    if(!elem && !consume(SCHEMA_ELEMENT)) return null;

    wsCheck(PAREN1);
    skipWs();
    final NodeTest t = elem ? elementTest() : schemaTest();
    wsCheck(PAREN2);
    return new DocTest(t != null ? t : Test.ELM);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private NodeTest elementTest() throws QueryException {
    final QNm name = eQName(null, sc.elemNS);
    if(name == null && !consume(ASTERISK)) return null;

    Type type = null;
    if(wsConsumeWs(COMMA)) {
      // parse type name
      final QNm tn = eQName(QNAME_X, sc.elemNS);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) throw error(TYPEUNDEF_X, tn);
      // parse optional question mark
      wsConsume(QUESTION);
    }
    return new NodeTest(NodeType.ELM, name, type);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private NodeTest schemaTest() throws QueryException {
    final QNm name = eQName(QNAME_X, sc.elemNS);
    throw error(SCHEMAINV_X, name);
  }

  /**
   * Parses the "AttributeTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test attributeTest() throws QueryException {
    final QNm name = eQName(null, null);
    if(name == null && !consume(ASTERISK)) return null;

    Type type = null;
    if(wsConsumeWs(COMMA)) {
      // parse type name
      final QNm tn = eQName(QNAME_X, sc.elemNS);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) throw error(TYPEUNDEF_X, tn);
    }
    return new NodeTest(NodeType.ATT, name, type);
  }

  /**
   * Parses the "PITest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test piTest() throws QueryException {
    tok.reset();
    final byte[] nm;
    if(quote(curr())) {
      nm = trim(stringLiteral());
      if(!XMLToken.isNCName(nm)) throw error(INVNCNAME_X, nm);
    } else if(ncName()) {
      nm = tok.toArray();
    } else {
      return null;
    }
    return new NodeTest(NodeType.PI, new QNm(nm));
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
        final NameTest test = (NameTest) nodeTest(false, false);
        if(test == null) throw error(NOCATCH);
        codes = Array.add(codes, test);
      } while(wsConsumeWs(PIPE));

      final int s = localVars.openScope();
      final int cl = Catch.NAMES.length;
      final Var[] vs = new Var[cl];
      for(int i = 0; i < cl; i++) vs[i] = localVars.add(Catch.NAMES[i], Catch.TYPES[i], false);
      final Catch c = new Catch(info(), codes, vs);
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
    FTExpr expr = ftOr(prg);
    FTExpr old;
    FTExpr first = null;
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
    final FTExpr e = ftAnd(prg);
    if(!wsConsumeWs(FTOR)) return e;

    FTExpr[] list = { e };
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
    final FTExpr e = ftMildNot(prg);
    if(!wsConsumeWs(FTAND)) return e;

    FTExpr[] list = { e };
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
    final FTExpr e = ftUnaryNot(prg);
    if(!wsConsumeWs(NOT)) return e;

    FTExpr[] list = { };
    do {
      wsCheck(IN);
      list = Array.add(list, ftUnaryNot(prg));
    } while(wsConsumeWs(NOT));
    // convert "A not in B not in ..." to "A not in(B or ...)"
    return new FTMildNot(info(), e, list.length == 1 ? list[0] : new FTOr(
        info(), list));
  }

  /**
   * Parses the "FTUnaryNot" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftUnaryNot(final boolean prg) throws QueryException {
    final boolean not = wsConsumeWs(FTNOT);
    final FTExpr e = ftPrimaryWithOptions(prg);
    return not ? new FTNot(info(), e) : e;
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
    if(fto.ln == null) fto.ln = Language.def();
    if(!Tokenizer.supportFor(fto.ln)) throw error(FTNOTOK_X, fto.ln);
    if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln)) throw error(FTNOSTEM_X, fto.ln);

    // consume weight option
    if(wsConsumeWs(WEIGHT)) expr = new FTWeight(info(), expr, enclosedExpr());

    // skip options if none were specified...
    return found ? new FTOpts(info(), expr, fto) : expr;
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
      final FTExpr e = ftSelection(true);
      wsCheck(CURLY2);
      return new FTExtensionSelection(info(), pragmas, e);
    }

    if(wsConsumeWs(PAREN1)) {
      final FTExpr e = ftSelection(false);
      wsCheck(PAREN2);
      return e;
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr[] ftRange(final boolean i) throws QueryException {
    final Expr[] occ = { Int.get(0), Int.get(Long.MAX_VALUE) };
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
    tok.reset();
    while(digit(curr())) tok.add(consume());
    if(tok.isEmpty()) throw error(INTEXP);
    return Int.get(toLong(tok.toArray()));
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
        opt.sw = new StopWords();
        if(wsConsumeWs(DEFAULT)) {
          if(!using) throw error(FTSTOP);
        } else {
          boolean union = false, except = false;
          while(using) {
            if(wsConsume(PAREN1)) {
              do {
                final byte[] sl = stringLiteral();
                if(except) opt.sw.delete(sl);
                else if(!union || !opt.sw.contains(sl)) opt.sw.add(sl);
              } while(wsConsume(COMMA));
              wsCheck(PAREN2);
            } else if(wsConsumeWs(AT)) {
              final String fn = string(stringLiteral());
              // optional: resolve URI reference
              final IO fl = qc.resources.stopWords(fn, sc);
              if(!opt.sw.read(fl, except)) throw error(NOSTOPFILE_X, fl);
            } else if(!union && !except) {
              throw error(FTSTOP);
            }
            union = wsConsumeWs(UNION);
            except = !union && wsConsumeWs(EXCEPT);
            if(!union && !except) break;
          }
        }
      } else if(wsConsumeWs(WILDCARDS)) {
        if(opt.isSet(WC)) throw error(FTDUP_X, WILDCARDS);
        if(opt.is(FZ)) throw error(BXFT_MATCH);
        opt.set(WC, using);
      } else if(wsConsumeWs(FUZZY)) {
        // extension to the official extension: "using fuzzy"
        if(opt.isSet(FZ)) throw error(FTDUP_X, FUZZY);
        if(opt.is(WC)) throw error(BXFT_MATCH);
        opt.set(FZ, using);
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
    long min = 0;
    long max = Long.MAX_VALUE;
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
    final int i = pos;
    if(!wsConsumeWs(INSERT) || !wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      pos = i;
      return null;
    }

    final Expr s = check(single(), INCOMPLETE);
    boolean first = false;
    boolean last = false;
    boolean before = false;
    boolean after = false;
    if(wsConsumeWs(AS)) {
      first = wsConsumeWs(FIRST);
      if(!first) {
        wsCheck(LAST);
        last = true;
      }
      wsCheck(INTO);
    } else if(!wsConsumeWs(INTO)) {
      after = wsConsumeWs(AFTER);
      before = !after && wsConsumeWs(BEFORE);
      if(!after && !before) throw error(INCOMPLETE);
    }
    final Expr trg = check(single(), INCOMPLETE);
    qc.updating();
    return new Insert(sc, info(), s, first, last, before, after, trg);
  }

  /**
   * Parses the "DeleteExpr" rule.
   * @return query expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr delete() throws QueryException {
    final int i = pos;
    if(!wsConsumeWs(DELETE) || !wsConsumeWs(NODES) && !wsConsumeWs(NODE)) {
      pos = i;
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
    final int i = pos;
    if(!wsConsumeWs(RENAME) || !wsConsumeWs(NODE)) {
      pos = i;
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
    final int i = pos;
    if(!wsConsumeWs(REPLACE)) return null;

    final boolean v = wsConsumeWs(VALUEE);
    if(v) {
      wsCheck(OF);
      wsCheck(NODE);
    } else if(!wsConsumeWs(NODE)) {
      pos = i;
      return null;
    }

    final Expr t = check(single(), INCOMPLETE);
    wsCheck(WITH);
    final Expr r = check(single(), INCOMPLETE);
    qc.updating();
    return new Replace(sc, info(), t, r, v);
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
      final QNm name = varName();
      wsCheck(ASSIGN);
      final Expr e = check(single(), INCOMPLETE);
      fl = Array.add(fl, new Let(localVars.add(name, SeqType.NOD, false), e, false, info()));
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
            final Expr e = single();
            if(e == null) throw funcMiss(func);
            argList.add(e);
          } while(wsConsume(COMMA));
          if(!wsConsume(PAREN2)) throw funcMiss(func);
        }
        // skip if primary expression cannot be a function
        if(func instanceof Value && !(func instanceof FItem)) throw error(NOPAREN_X_X, func);
        if(upd) qc.updating();
        return new DynFuncCall(ii, sc, upd, ndt, func, argList.finish());
      }
    }
    pos = p;
    return null;
  }

  /**
   * Parses the "NCName" rule.
   * @param err optional error message
   * @return string
   * @throws QueryException query exception
   */
  private byte[] ncName(final QueryError err) throws QueryException {
    tok.reset();
    if(ncName()) return tok.toArray();
    if(err != null) throw error(err, consume());
    return EMPTY;
  }

  /**
   * Parses the "EQName" rule.
   * @param err optional error message. Will be thrown if no EQName is found, or ignored if set to
   * {@code null}
   * @param def default namespace, or operation mode ({@link #URICHECK}, {@link #SKIPCHECK})
   * @return QName or {@code null}
   * @throws QueryException query exception
   */
  private QNm eQName(final QueryError err, final byte[] def) throws QueryException {
    final int i = pos;
    if(consume(EQNAME)) {
      final byte[] uri = bracedURILiteral();
      final byte[] name = ncName(null);
      if(name.length != 0) {
        if(def == URICHECK && uri.length == 0) {
          pos = i;
          throw error(NOURI_X, name);
        }
        return new QNm(name, uri);
      }
      pos = i;
    }

    // parse QName
    final byte[] nm = qName(err);
    if(nm.length == 0) return null;
    if(def == SKIPCHECK) return new QNm(nm);

    // create new EQName and set namespace
    final QNm name = new QNm(nm, sc);
    if(!name.hasURI()) {
      if(def == URICHECK) {
        pos = i;
        throw error(NSMISS_X, name);
      }
      if(name.hasPrefix()) {
        pos = i;
        throw error(NOURI_X, name.string());
      }
      name.uri(def);
    }
    return name;
  }

  /**
   * Parses the "QName" rule.
   * @param err optional error message. Will be thrown if no QName is found, and ignored if set to
   * {@code null}
   * @return QName string
   * @throws QueryException query exception
   */
  private byte[] qName(final QueryError err) throws QueryException {
    tok.reset();
    if(!ncName()) {
      if(err != null) throw error(err, consume());
    } else if(consume(':')) {
      if(XMLToken.isNCStartChar(curr())) {
        tok.add(':');
        do {
          tok.add(consume());
        } while(XMLToken.isNCChar(curr()));
      } else {
        --pos;
      }
    }
    return tok.toArray();
  }

  /**
   * Helper method for parsing NCNames.
   * @return true for success
   */
  private boolean ncName() {
    if(!XMLToken.isNCStartChar(curr())) return false;
    do tok.add(consume()); while(XMLToken.isNCChar(curr()));
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
          final char c = curr();
          final boolean m = digit(c);
          final boolean h = b == 0x10 && (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');
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
      final char c = consume();
      int ch = c;
      if(Character.isHighSurrogate(c) && curr() != 0
          && Character.isLowSurrogate(curr())) {
        ch = Character.toCodePoint(c, consume());
      }
      if(ch == '\r') {
        ch = '\n';
        if(curr(ch)) consume();
      }
      tb.add(ch);
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
    final String ent = semi == -1 ? sub + "..." : sub.substring(0, semi + 1);
    throw error(code, ent);
  }

  /**
   * Raises an error if the specified expression is {@code null}.
   * @param <E> expression type
   * @param expr expression
   * @param err error message
   * @return expression
   * @throws QueryException query exception
   */
  private <E extends Expr> E check(final E expr, final QueryError err) throws QueryException {
    if(expr == null) throw error(err);
    return expr;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param ch character to be found
   * @throws QueryException query exception
   */
  private void check(final int ch) throws QueryException {
    if(!consume(ch)) throw error(WRONGCHAR_X_X, (char) ch, found());
  }

  /**
   * Skips whitespaces, raises an error if the specified string cannot be consumed.
   * @param s string to be found
   * @throws QueryException query exception
   */
  private void wsCheck(final String s) throws QueryException {
    if(!wsConsume(s)) throw error(WRONGCHAR_X_X, s, found());
  }

  /**
   * Consumes the next character and normalizes new line characters.
   * @return next character
   * @throws QueryException query exception
   */
  private char consumeContent() throws QueryException {
    char ch = consume();
    if(ch == 0) throw error(WRONGCHAR_X_X, ch, found());
    if(ch == '\r') {
      ch = '\n';
      consume('\n');
    }
    return ch;
  }

  /**
   * Consumes the specified token and surrounding whitespaces.
   * @param t token to consume
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String t) throws QueryException {
    final int i = pos;
    if(!wsConsume(t)) return false;
    if(skipWs() || !XMLToken.isNCStartChar(t.charAt(0)) || !XMLToken.isNCChar(curr())) return true;
    pos = i;
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position. If the strings are
   * found, the cursor is placed after the first token.
   * @param s1 string to be consumed
   * @param s2 second string
   * @param expr alternative error message (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String s1, final String s2, final QueryError expr)
      throws QueryException {

    final int i1 = pos;
    if(!wsConsumeWs(s1)) return false;
    final int i2 = pos;
    alter = expr;
    alterPos = i2;
    final boolean ok = wsConsume(s2);
    pos = ok ? i2 : i1;
    return ok;
  }

  /**
   * Skips whitespaces, consumes the specified string and ignores trailing characters.
   * @param str string to consume
   * @return true if string was found
   * @throws QueryException query exception
   */
  private boolean wsConsume(final String str) throws QueryException {
    skipWs();
    return consume(str);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespaces were found
   * @throws QueryException query exception
   */
  private boolean skipWs() throws QueryException {
    final int i = pos;
    while(more()) {
      final int c = curr();
      if(c == '(' && next() == ':') {
        comment();
      } else {
        if(c <= 0 || c > ' ') return i != pos;
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
    final boolean doc = next() == '~';
    if(doc) {
      currDoc.setLength(0);
      ++pos;
    }
    comment(false, doc);
  }

  /**
   * Consumes a comment.
   * @param nested nested flag
   * @param doc xqdoc flag
   * @throws QueryException query exception
   */
  private void comment(final boolean nested, final boolean doc) throws QueryException {
    while(++pos < length) {
      char curr = curr();
      if(curr == '(' && next() == ':') {
        ++pos;
        comment(true, doc);
        curr = curr();
      }
      if(curr == ':' && next() == ')') {
        pos += 2;
        if(!nested && moduleDoc.isEmpty()) {
          moduleDoc = currDoc.toString().trim();
          currDoc.setLength(0);
        }
        return;
      }
      if(doc) currDoc.append(curr);
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
      final int c = curr();
      if(c <= 0 || c > ' ') return i != pos;
      ++pos;
    }
    return true;
  }

  /**
   * Creates an alternative error.
   * @return error
   */
  private QueryException error() {
    pos = alterPos;
    if(alter != WHICHFUNC_X) return error(alter);
    final QueryException qe = qc.funcs.similarError(alterFunc, info());
    return qe == null ? error(alter, alterFunc.string()) : qe;
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
   * Creates a function parsing error.
   * @param func function
   * @return error
   */
  private QueryException funcMiss(final Object func) {
    Object name = func;
    if(func instanceof XQFunctionExpr) {
      final QNm qnm = ((XQFunctionExpr) func).funcName();
      if(qnm != null) name = qnm.prefixId();
    }
    return error(FUNCMISS_X, name);
  }

  /**
   * Creates the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return error
   */
  public QueryException error(final QueryError err, final Object... arg) {
    return err.get(info(), arg);
  }
}
