package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpN.OpN;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.expr.Context;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
import org.basex.query.expr.gflwor.Window.Condition;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.up.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.format.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** QName check: URI is mandatory. */
  private static final byte[] URICHECK = {};
  /** QName check: skip namespace check. */
  private static final byte[] SKIPCHECK = {};
  /** Reserved function names (XQuery 3.0). */
  private static final TokenSet KEYWORDS30 = new TokenSet();

  static {
    final byte[][] keys = {
      FuncType.ANY_FUN.string(), ARRAY, NodeType.ATT.string(), NodeType.COM.string(),
      NodeType.DOC.string(), NodeType.ELM.string(), token(EMPTY_SEQUENCE), token(IF),
      AtomType.ITEM.string(), MAP, NodeType.NSP.string(), NodeType.NOD.string(),
      NodeType.PI.string(), token(SCHEMA_ATTRIBUTE), token(SCHEMA_ELEMENT), token(SWITCH),
      NodeType.TXT.string(), token(TYPESWITCH)
    };
    for(final byte[] key : keys) KEYWORDS30.add(key);
  }

  /** Modules loaded by the current file. */
  public final TokenSet modules = new TokenSet();
  /** Parsed variables. */
  public final ArrayList<StaticVar> vars = new ArrayList<>();
  /** Parsed functions. */
  public final ArrayList<StaticFunc> funcs = new ArrayList<>();
  /** Namespaces. */
  public final TokenMap namespaces = new TokenMap();

  /** Query context. */
  private final QueryContext qc;
  /** Static context. */
  private final StaticContext sc;

  /** Temporary token cache. */
  private final TokenBuilder tok = new TokenBuilder();
  /** XQDoc cache. */
  private final StringBuilder currDoc = new StringBuilder();
  /** Current XQDoc string. */
  private String moduleDoc = "";

  /** Name of current module. */
  private QNm module;

  /** Alternative error code. */
  private QueryError alter;
  /** Function name of alternative error. */
  private QNm alterFunc;
  /** Alternative position. */
  private int alterPos;

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<>();
  /** Cached QNames. */
  private final ArrayList<QNmCheck> names = new ArrayList<>();
  /** Stack of variable contexts. */
  private final ArrayList<VarContext> localVars = new ArrayList<>();

  /**
   * Constructor.
   * @param query query string
   * @param path file path (if {@code null}, {@link MainOptions#QUERYPATH} will be assigned)
   * @param qc query context
   * @param sc static context
   * @throws QueryException query exception
   */
  public QueryParser(final String query, final String path, final QueryContext qc,
      final StaticContext sc) throws QueryException {

    super(query);
    this.qc = qc;

    // set path to query file
    final MainOptions opts = qc.context.options;
    final String bi = path != null ? path : opts.get(MainOptions.QUERYPATH);
    final StaticContext sctx = sc != null ? sc : new StaticContext(qc.context);
    if(!bi.isEmpty()) sctx.baseURI(bi);
    this.sc = sctx;

    // parse pre-defined external variables
    final String bind = opts.get(MainOptions.BINDINGS).trim();
    final StringBuilder key = new StringBuilder();
    final StringBuilder val = new StringBuilder();
    boolean first = true;
    final int sl = bind.length();
    for(int s = 0; s < sl; s++) {
      final char ch = bind.charAt(s);
      if(first) {
        if(ch == '=') {
          first = false;
        } else {
          key.append(ch);
        }
      } else {
        if(ch == ',') {
          if(s + 1 == sl || bind.charAt(s + 1) != ',') {
            qc.bind(key.toString().trim(), new Atm(val.toString()), sc);
            key.setLength(0);
            val.setLength(0);
            first = true;
            continue;
          }
          // literal commas are escaped by a second comma
          s++;
        }
        val.append(ch);
      }
    }
    if(key.length() != 0) qc.bind(key.toString().trim(), new Atm(val.toString()), sc);
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
      if(wsConsumeWs(MODULE, NSPACE, null)) throw error(MAINMOD);
      pos = i;

      prolog1();
      prolog2();

      pushVarContext(null);
      final Expr expr = expr();
      if(expr == null) throw alter == null ? error(EXPREMPTY) : error();
      final VarScope scope = popVarContext();

      final MainModule mm = new MainModule(expr, scope, moduleDoc, sc);
      finish(mm, true);
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
      wsCheck(NSPACE);
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
      final byte[] p = token(base == null ? "" : base.path());
      qc.modParsed.put(p, uri);

      qc.modStack.push(p);
      prolog1();
      prolog2();

      finish(null, check);

      qc.modStack.pop();
      return new LibraryModule(module, moduleDoc, sc);
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
    file = baseIO == null ? null : qc.context.user().has(Perm.ADMIN) ? baseIO.path() :
      baseIO.name();
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
   * @param check check function calls and variable references and update constraints
   * @throws QueryException query exception
   */
  private void finish(final MainModule mm, final boolean check) throws QueryException {
    if(more()) {
      if(alter != null) throw error();
      final String rest = rest();
      pos++;
      if(mm == null) throw error(MODEXPR, rest);
      throw error(QUERYEND_X, rest);
    }

    // completes the parsing step
    assignURI(0);
    if(sc.elemNS != null) sc.ns.add(EMPTY, sc.elemNS, null);

    // set default decimal format
    final byte[] empty = new QNm(EMPTY).id();
    if(sc.decFormats.get(empty) == null) {
      sc.decFormats.put(empty, new DecFormatter());
    }

    if(check) qc.check(mm, sc);
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
        } else if(wsConsumeWs(NSPACE)) {
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
        final Ann ann = new Ann();
        while(true) {
          if(wsConsumeWs(UPDATING)) {
            ann.add(Ann.Q_UPDATING, Empty.SEQ, info());
          } else if(consume('%')) {
            annotation(ann);
          } else {
            break;
          }
        }
        if(wsConsumeWs(VARIABLE)) {
          // variables cannot be updating
          if(ann.contains(Ann.Q_UPDATING)) throw error(UPDATINGVAR);
          ann.check(true);
          varDecl(ann);
        } else if(wsConsumeWs(FUNCTION)) {
          ann.check(false);
          functionDecl(ann);
        } else if(!ann.isEmpty()) {
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
   * @return annotations
   * @throws QueryException query exception
   */
  private Ann annotations() throws QueryException {
    final Ann ann = new Ann();
    while(wsConsume("%")) annotation(ann);
    ann.check(false);
    skipWs();
    return ann;
  }

  /**
   * Parses a single annotation.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void annotation(final Ann ann) throws QueryException {
    skipWs();
    final InputInfo info = info();
    final QNm name = eQName(QNAME_X, XQ_URI);

    final ValueBuilder vb = new ValueBuilder();
    if(wsConsumeWs(PAREN1)) {
      do {
        final Expr ex = literal();
        if(!(ex instanceof Item)) throw error(ANNVALUE);
        vb.add((Item) ex);
      } while(wsConsumeWs(COMMA));
      wsCheck(PAREN2);
    }
    skipWs();
    ann.add(name, vb.value(), info);
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
    wsCheck(NSPACE);
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
      if(module != null) throw error(MODOUT);

      if(qc.serialOpts == null) {
        qc.serialOpts = new SerializerOptions(qc.context.options.get(MainOptions.SERIALIZER));
      }
      if(!decl.add("S " + name)) throw error(OUTDUPL_X, name);
      try {
        qc.serialOpts.assign(name, string(val));
        if(name.equals(SerializerOptions.USE_CHARACTER_MAPS.name()))
          throw error(OUTMAP_X, val);
      } catch(final BaseXException ex) {
        for(final Option<?> o : qc.serialOpts) if(o.name().equals(name)) throw error(SER_X, ex);
        throw error(OUTINVALID_X, ex);
      }

      if(name.equals(SerializerOptions.PARAMETER_DOCUMENT.name())) {
        final IO io = IO.get(string(resolvedUri(val).string()));
        try {
          // check parameters and add values to serialization parameters
          final InputInfo info = info();
          FuncOptions.serializer(new DBNode(io).children().next(), qc.serialOpts, info);

          final HashMap<String, String> free = qc.serialOpts.free();
          if(!free.isEmpty()) throw SEROPTION_X.get(info, free.keySet().iterator().next());
          final StringOption cm = SerializerOptions.USE_CHARACTER_MAPS;
          if(!qc.serialOpts.get(cm).isEmpty()) throw SEROPTION_X.get(info, cm.name());
        } catch(final IOException ex) {
          throw error(OUTDOC_X, val);
        }
      }
    } else if(eq(qnm.uri(), XQ_URI)) {
      throw error(DECLOPTION_X, qnm);
    } else if(eq(qnm.uri(), DB_URI)) {
      // project-specific declaration
      final String ukey = name.toUpperCase(Locale.ENGLISH);
      final Option<?> opt = qc.context.options.option(ukey);
      if(opt == null) throw error(BASX_OPTIONS_X, ukey);
      // cache old value (to be reset after query evaluation)
      qc.staticOpts.put(opt, qc.context.options.get(opt));
      qc.tempOpts.add(name).add(string(val));
    } else if(eq(qnm.uri(), QUERY_URI)) {
      // query-specific options
      if(name.equals(READ_LOCK)) {
        for(final byte[] lock : split(val, ','))
          qc.readLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else if(name.equals(WRITE_LOCK)) {
        for(final byte[] lock : split(val, ','))
          qc.writeLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else {
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
    if(wsConsumeWs(NSPACE)) {
      pref = ncName(NONAME_X);
      if(eq(pref, XML, XMLNS)) throw error(BINDXML_X, pref);
      wsCheck(IS);
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NSPACE);
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
    if(wsConsumeWs(NSPACE)) {
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

    // check modules at specified locations
    if(wsConsumeWs(AT)) {
      do {
        module(stringLiteral(), uri);
      } while(wsConsumeWs(COMMA));
      return;
    }

    // check pre-declared module files
    final byte[] path = qc.modDeclared.get(uri);
    if(path != null) {
      module(path, uri);
      return;
    }

    // check built-in modules
    for(final byte[] u : Function.URIS.values()) if(eq(uri, u)) return;

    // resolve module uri
    if(qc.resources.modules().addImport(uri, info(), this)) return;

    throw error(WHICHMODULE_X, uri);
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param path file path
   * @param uri module uri
   * @throws QueryException query exception
   */
  public void module(final byte[] path, final byte[] uri) throws QueryException {
    // get absolute path
    final IO io = sc.io(string(path));
    final byte[] p = token(io.path());

    // check if module has already been parsed
    final byte[] u = qc.modParsed.get(p);
    if(u != null) {
      if(!eq(uri, u)) throw error(WRONGMODULE_X_X, uri,
          qc.context.user().has(Perm.ADMIN) ? io.path() : io.name());
      return;
    }
    qc.modParsed.put(p, uri);

    // read module
    final String qu;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      throw error(WHICHMODFILE_X, qc.context.user().has(Perm.ADMIN) ? io.path() : io.name());
    }

    qc.modStack.push(p);
    final StaticContext sub = new StaticContext(qc.context);
    final LibraryModule lib = new QueryParser(qu, io.path(), qc, sub).parseLibrary(false);
    final byte[] muri = lib.name.uri();

    // check if import and declaration uri match
    if(!eq(uri, muri)) throw error(WRONGMODULE_X_X, muri, file);

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

    pushVarContext(null);
    final Expr e = check(single(), NOVARDECL);
    final SeqType declType = sc.contextType == null ? SeqType.ITEM : sc.contextType;
    final VarScope scope = popVarContext();
    qc.ctxItem = new MainModule(e, scope, declType, currDoc.toString(), sc, info());

    if(module != null) throw error(DECITEM);
    if(!sc.mixUpdates && e.has(Flag.UPD)) throw error(UPCTX, e);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void varDecl(final Ann ann) throws QueryException {
    final QNm vn = varName();
    final SeqType tp = optAsType();
    if(module != null && !eq(vn.uri(), module.uri())) throw error(MODULENS_X, vn);

    pushVarContext(null);
    final boolean external = wsConsumeWs(EXTERNAL);
    final Expr bind;
    if(external) {
      bind = wsConsumeWs(ASSIGN) ? check(single(), NOVARDECL) : null;
    } else {
      wsCheck(ASSIGN);
      bind = check(single(), NOVARDECL);
    }

    final VarScope scope = popVarContext();
    vars.add(qc.vars.declare(vn, tp, ann, bind, external, sc, scope, currDoc.toString(), info()));
  }

  /**
   * Parses an optional SeqType declaration.
   * @return type if preceded by {@code as} (may be {@code null})
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
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void functionDecl(final Ann ann) throws QueryException {
    final InputInfo ii = info();
    final QNm name = eQName(FUNCNAME, sc.funcNS);
    if(keyword(name)) throw error(RESERVED_X, name.local());
    wsCheck(PAREN1);
    if(module != null && !eq(name.uri(), module.uri())) throw error(MODULENS_X, name);

    pushVarContext(null);
    final Var[] args = paramList();
    wsCheck(PAREN2);
    final SeqType type = optAsType();
    if(ann.contains(Ann.Q_UPDATING)) qc.updating();
    final Expr expr = wsConsumeWs(EXTERNAL) ? null : enclosed(NOFUNBODY);
    final VarScope scope = popVarContext();
    funcs.add(qc.funcs.declare(ann, name, args, type, expr, sc, scope, currDoc.toString(), ii));
  }

  /**
   * Checks if the specified name equals reserved function names.
   * @param name name to be checked
   * @return result of check
   */
  private static boolean keyword(final QNm name) {
    return !name.hasPrefix() && KEYWORDS30.contains(name.string());
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
      final Var var = addVar(varName(), optAsType(), true);
      for(final Var v : args)
        if(v.name.eq(var.name)) throw error(FUNCDUPL_X, var);

      args = Array.add(args, new Var[args.length + 1], var);
      if(!consume(',')) break;
    }
    return args;
  }

  /**
   * Parses the "EnclosedExpr" rule.
   * @param err error message
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr enclosed(final QueryError err) throws QueryException {
    wsCheck(CURLY1);
    final Expr e = check(expr(), err);
    wsCheck(CURLY2);
    return e;
  }

  /**
   * Parses the "Expr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
    if(e == null) e = deletee();
    if(e == null) e = rename();
    if(e == null) e = replace();
    if(e == null) e = transform();
    if(e == null) e = updatingFunctionCall();
    if(e == null) e = or();
    return e;
  }

  /**
   * Parses the "FLWORExpr" rule.
   * Parses the "WhereClause" rule.
   * Parses the "OrderByClause" rule.
   * Parses the "OrderSpecList" rule.
   * Parses the "GroupByClause" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr flwor() throws QueryException {
    final int s = openSubScope();
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
        alter = NOWHERE;
      }

      if(wsConsumeWs(GROUP)) {
        wsCheck(BY);
        skipWs();
        alterPos = pos;
        final GroupBy.Spec[] specs = groupSpecs(clauses);

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<>();
        for(final GroupBy.Spec spec : specs) curr.put(spec.var.name.id(), spec.var);
        vars:
        for(final Var v : curr.values()) {
          for(final GroupBy.Spec spec : specs) if(spec.var.is(v)) continue vars;
          ng.add(new VarRef(specs[0].info, v));
        }

        // add new copies for all non-grouping variables
        final Var[] ngrp = new Var[ng.size()];
        for(int i = ng.size(); --i >= 0;) {
          final VarRef v = ng.get(i);

          // if one groups variables such as $x as xs:integer, then the resulting
          // sequence isn't compatible with the type and can't be assigned
          final Var nv = addVar(v.var.name, null, false);
          // [LW] should be done everywhere
          if(v.seqType().one())
            nv.refineType(SeqType.get(v.seqType().type, Occ.ONE_MORE), qc, info());
          ngrp[i] = nv;
          curr.put(nv.name.id(), nv);
        }

        final VarRef[] pre = new VarRef[ng.size()];
        clauses.add(new GroupBy(specs, ng.toArray(pre), ngrp, specs[0].info));
        alter = GRPBY;
      }

      final boolean stable = wsConsumeWs(STABLE);
      if(stable) wsCheck(ORDER);
      if(stable || wsConsumeWs(ORDER)) {
        wsCheck(BY);
        alterPos = pos;
        OrderBy.Key[] ob = null;
        do {
          final OrderBy.Key key = orderSpec();
          ob = ob == null ? new OrderBy.Key[] { key } : Array.add(ob, key);
        } while(wsConsume(COMMA));

        final VarRef[] vs = new VarRef[curr.size()];
        int i = 0;
        for(final Var v : curr.values()) vs[i++] = new VarRef(ob[0].info, v);
        clauses.add(new OrderBy(vs, ob, ob[0].info));
        alter = ORDERBY;
      }

      if(wsConsumeWs(COUNT, DOLLAR, NOCOUNT)) {
        final Var v = addVar(varName(), SeqType.ITR, false);
        curr.put(v.name.id(), v);
        clauses.add(new Count(v, info()));
      }
    } while(size < clauses.size());

    if(!wsConsumeWs(RETURN)) throw alter == null ? error(FLWORRETURN) : error();

    final Expr ret = check(single(), NORETURN);
    closeSubScope(s);
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
    // ForClause / LetClause
    final boolean let = wsConsumeWs(LET, SCORE, NOLET) || wsConsumeWs(LET, DOLLAR, NOLET);
    if(let || wsConsumeWs(FOR, DOLLAR, NOFOR)) {
      if(cls == null) cls = new LinkedList<>();
      if(let) letClause(cls);
      else    forClause(cls);
    } else {
      // WindowClause
      final boolean slide = wsConsumeWs(FOR, SLIDING, NOWINDOW);
      if(slide || wsConsumeWs(FOR, TUMBLING, NOWINDOW)) {
        if(cls == null) cls = new LinkedList<>();
        cls.add(windowClause(slide));
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
      final Var var = addVar(nm, tp, false);
      final Var ps = p != null ? addVar(p, SeqType.ITR, false) : null;
      final Var scr = s != null ? addVar(s, SeqType.DBL, false) : null;
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
      cls.add(new Let(addVar(nm, tp, false), e, score, info()));
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
    return new Window(info(), slide, addVar(nm, tp, false), e, start, only, end);
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
    final Var var = curr('$')             ? addVar(varName(), null, false) : null,
              at  = wsConsumeWs(AT)       ? addVar(varName(), null, false) : null,
              prv = wsConsumeWs(PREVIOUS) ? addVar(varName(), null, false) : null,
              nxt = wsConsumeWs(NEXT)     ? addVar(varName(), null, false) : null;
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
  private OrderBy.Key orderSpec() throws QueryException {
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
    return new OrderBy.Key(info(), e, desc, least, coll);
  }

  /**
   * Parses the "GroupingSpec" rule.
   * @param cl preceding clauses
   * @return new group specification
   * @throws QueryException query exception
   */
  private GroupBy.Spec[] groupSpecs(final LinkedList<Clause> cl) throws QueryException {
    GroupBy.Spec[] specs = null;
    do {
      final InputInfo ii = info();
      final QNm name = varName();
      final SeqType declType = optAsType();

      final Expr by;
      if(declType != null || wsConsume(ASSIGN)) {
        if(declType != null) wsCheck(ASSIGN);
        by = check(single(), NOVARDECL);
      } else {
        final VarRef vr = resolveLocalVar(name, ii);
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
            for(final GroupBy.Spec spec : specs) {
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
      final GroupBy.Spec spec = new GroupBy.Spec(ii, addVar(name, declType, false), by, coll);
      if(specs == null) {
        specs = new GroupBy.Spec[] { spec };
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = wsConsumeWs(SOME, DOLLAR, NOSOME);
    if(!some && !wsConsumeWs(EVERY, DOLLAR, NOSOME)) return null;

    final int s = openSubScope();
    For[] fl = { };
    do {
      final QNm nm = varName();
      final SeqType tp = optAsType();
      wsCheck(IN);
      final Expr e = check(single(), NOSOME);
      fl = Array.add(fl, new For(addVar(nm, tp, false), null, null, e, false, info()));
    } while(wsConsumeWs(COMMA));

    wsCheck(SATISFIES);
    final Expr e = check(single(), NOSOME);
    closeSubScope(s);
    return new Quantifier(info(), fl, e, !some);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!wsConsumeWs(SWITCH, PAREN1, TYPEPAR)) return null;
    wsCheck(PAREN1);
    final Expr expr = check(expr(), NOSWITCH);
    SwitchCase[] exprs = { };
    wsCheck(PAREN2);

    // collect all cases
    ExprList cases;
    do {
      cases = new ExprList(null);
      while(wsConsumeWs(CASE)) add(cases, single());
      if(cases.size() == 1) {
        // add default case
        if(exprs.length == 0) throw error(WRONGCHAR_X_X, CASE, found());
        wsCheck(DEFAULT);
      }
      wsCheck(RETURN);
      cases.set(0, check(single(), NOSWITCH));
      exprs = Array.add(exprs, new SwitchCase(info(), cases.finish()));
    } while(cases.size() != 1);

    return new Switch(info(), expr, exprs);
  }

  /**
   * Parses the "TypeswitchExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!wsConsumeWs(TYPESWITCH, PAREN1, TYPEPAR)) return null;
    wsCheck(PAREN1);
    final Expr ts = check(expr(), NOTYPESWITCH);
    wsCheck(PAREN2);

    TypeCase[] cases = { };
    final ArrayList<SeqType> types = new ArrayList<>();
    final int s = openSubScope();
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
        var = addVar(varName(), null, false);
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
      closeSubScope(s);
    } while(cs);
    if(cases.length == 1) throw error(NOTYPESWITCH);
    return new TypeSwitch(info(), ts, cases);
  }

  /**
   * Parses the "IfExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr iff() throws QueryException {
    if(!wsConsumeWs(IF, PAREN1, IFPAR)) return null;
    wsCheck(PAREN1);
    final Expr iff = check(expr(), NOIF);
    wsCheck(PAREN2);
    if(!wsConsumeWs(THEN)) throw error(NOIF);
    final Expr thn = check(single(), NOIF);
    if(!wsConsumeWs(ELSE)) throw error(NOIF);
    final Expr els = check(single(), NOIF);
    return new If(info(), iff, thn, els);
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    if(!wsConsumeWs(OR)) return e;

    final ExprList el = new ExprList(2).add(e);
    do add(el, and()); while(wsConsumeWs(OR));
    return new Or(info(), el.finish());
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr e = update();
    if(!wsConsumeWs(AND)) return e;

    final ExprList el = new ExprList(2).add(e);
    do add(el, update()); while(wsConsumeWs(AND));
    return new And(info(), el.finish());
  }

  /**
   * Parses the "UpdateExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr update() throws QueryException {
    final Expr e = comparison();
    if(e != null) {
      if(wsConsumeWs(UPDATE)) {
        final int s = openSubScope();
        qc.updating();
        final Expr m = check(single(), COPYEXPR);
        closeSubScope(s);
        return new Modify(info(), e, m);
      }
    }
    return e;
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr e = additive();
    if(!wsConsumeWs(TO)) return e;
    return new Range(info(), e, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr intersect() throws QueryException {
    final Expr e = instanceoff();

    if(wsConsumeWs(INTERSECT)) {
      final ExprList el = new ExprList(e);
      do add(el, instanceoff()); while(wsConsumeWs(INTERSECT));
      return new InterSect(info(), el.finish());
    }
    if(wsConsumeWs(EXCEPT)) {
      final ExprList el = new ExprList(e);
      do add(el, instanceoff()); while(wsConsumeWs(EXCEPT));
      return new Except(info(), el.finish());
    }
    return e;
  }

  /**
   * Parses the "InstanceofExpr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr cast() throws QueryException {
    final Expr e = unary();
    if(!wsConsumeWs(CAST)) return e;
    wsCheck(AS);
    return new Cast(sc, info(), e, simpleType());
  }

  /**
   * Parses the "UnaryExpr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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

    boolean brace = true;
    if(consume(CURLY1)) {
      brace = false;
    } else if(consume(TYPE)) {
      final QNm qnm = eQName(QNAME_X, SKIPCHECK);
      names.add(new QNmCheck(qnm));
    } else if(!consume(STRICT) && !consume(LAX)) {
      pos = i;
      return;
    }

    if(brace) wsCheck(CURLY1);
    check(single(), NOVALIDATE);
    wsCheck(CURLY2);
    throw error(IMPLVAL);
  }

  /**
   * Parses the "ExtensionExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr extension() throws QueryException {
    final Pragma[] pragmas = pragma();
    if(pragmas == null) return null;
    final Expr expr = enclosed(NOPRAGMA);
    return pragmas.length == 0 ? expr : new Extension(info(), pragmas, expr);
  }

  /**
   * Parses the "Pragma" rule.
   * @return array of pragmas (may be {@code null})
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

      final byte[] v = tok.trim().toArray();
      if(eq(name.prefix(), DB_PREFIX)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final Option<?> opt = qc.context.options.option(key);
        if(opt == null) throw error(BASX_OPTIONS_X, key);
        el.add(new DBPragma(name, opt, v));
      }
      pos += 2;
    } while(wsConsumeWs(PRAGMA));
    return el.toArray(new Pragma[el.size()]);
  }

  /**
   * Parses the "MapExpr" rule.
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr step(final boolean error) throws QueryException {
    final Expr e = postfix();
    return e != null ? e : axisStep(error);
  }

  /**
   * Parses the "AxisStep" rule.
   * @param error show error if nothing is found
   * @return step (may be {@code null})
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
        if(test == Test.NSP) throw error(NSNOTALL);
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
   * @return test (may be {@code null})
   * @throws QueryException query exception
   */
  private Test nodeTest(final boolean att, final boolean all) throws QueryException {
    final int i = pos;
    if(consume('*')) {
      // name test: *
      if(!consume(':')) return new NameTest(att);
      // name test: *:name
      return new NameTest(new QNm(ncName(QNAME_X)), NameTest.Kind.NAME, att, sc.elemNS);
    }

    if(consume(EQNAME)) {
      // name test: Q{uri}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        final QNm nm = new QNm(COLON, uri);
        return new NameTest(nm, NameTest.Kind.URI, att, sc.elemNS);
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
        // name test: prefix:name, name, Q{uri}name
        if(name.hasPrefix() || !consume(':')) {
          skipWs();
          names.add(new QNmCheck(name, !att));
          return new NameTest(name, NameTest.Kind.URI_NAME, att, sc.elemNS);
        }
        // name test: prefix:*
        if(consume('*')) {
          final QNm nm = new QNm(concat(name.string(), COLON));
          names.add(new QNmCheck(nm, !att));
          return new NameTest(nm, NameTest.Kind.URI, att, sc.elemNS);
        }
      }
    }
    pos = i;
    return null;
  }

  /**
   * Parses the "PostfixExpr" rule.
   * @return postfix expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr postfix() throws QueryException {
    Expr e = primary(), old;
    if(e != null) {
      do {
        old = e;
        if(wsConsume(SQUARE1)) {
          // parses the "Predicate" rule
          if(e == null) throw error(PREDMISSING);
          final ExprList el = new ExprList();
          do {
            add(el, expr());
            wsCheck(SQUARE2);
          } while(wsConsume(SQUARE1));
          e = Filter.get(info(), e, el.finish());
        } else if(consume(PAREN1)) {
          // parses the "ArgumentList" rule
          final InputInfo ii = info();
          final ExprList argList = new ExprList();
          final int[] holes = argumentList(argList, e);
          final Expr[] args = argList.finish();
          // only set updating flag if updating and non-updating expressions are mixed
          Ann ann = null;
          if(e instanceof FuncItem) ann = ((FuncItem) e).annotations();
          else if(e instanceof FuncLit) ann = ((FuncLit) e).annotations();
          else if(e instanceof PartFunc) ann = ((PartFunc) e).annotations();
          final boolean up = sc.mixUpdates && ann != null && ann.contains(Ann.Q_UPDATING);
          if(up) qc.updating();
          e = holes == null ? new DynFuncCall(ii, sc, up, e, args) :
            new PartFunc(sc, ii, e, args, holes);
        } else if(consume(QUESTION)) {
          // parses the "Lookup" rule
          e = new Lookup(info(), keySpecifier(), e);
        } else if(consume(ARROW)) {
          final Expr ex = arrowPostfix();
          wsCheck(PAREN1);

          if(ex instanceof QNm) {
            final QNm name = (QNm) ex;
            if(keyword(name)) throw error(RESERVED_X, name.local());
            e = function(name, e);
          } else {
            final InputInfo ii = info();
            final ExprList argList = new ExprList(e);
            final int[] holes = argumentList(argList, e);
            final Expr[] args = argList.finish();
            e = holes == null ? new DynFuncCall(ii, sc, false, ex, args) :
              new PartFunc(sc, ii, ex, args, holes);
          }
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr primary() throws QueryException {
    skipWs();
    final char c = curr();
    // variables
    if(c == '$') {
      final InputInfo ii = info();
      return resolveVar(varName(), ii);
    }
    // parentheses
    if(next() != '#' && consume('(')) return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
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
    if(wsConsumeWs(ORDERED, CURLY1, INCOMPLETE) || wsConsumeWs(UNORDERED, CURLY1, INCOMPLETE))
      return enclosed(NOENCLEXPR);
    // map (including legacy syntax)
    if(wsConsumeWs(MAPSTR, CURLY1, INCOMPLETE) || curr('{')) return new CMap(info(), keyValues());
    // square array constructor
    if(wsConsumeWs(SQUARE1)) return new CArray(info(), false, values());
    // curly array constructor
    if(wsConsumeWs(ARRAYSTR, CURLY1, INCOMPLETE)) {
      wsCheck(CURLY1);
      final Expr a = expr();
      wsCheck(CURLY2);
      return a == null ? new CArray(info(), true) : new CArray(info(), true, a);
    }
    // unary lookup
    if(wsConsumeWs(QUESTION)) return new Lookup(info(), keySpecifier());
    // context value
    if(c == '.' && !digit(next())) {
      if(next() == '.') return null;
      consume('.');
      return new Context(info());
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
    return wsConsume(ASTERISK) ? Str.WC :
      consume(PAREN1) ? parenthesized() :
      digit(curr()) ? numericLiteral(true) :
      Str.get(ncName(KEYSPEC));
  }

  /**
   * Parses the "ArrowPostfix" rule.
   * @return function specifier
   * @throws QueryException query exception
   */
  private Expr arrowPostfix() throws QueryException {
    return wsConsume(PAREN1) ? parenthesized() : curr() == '$' ? resolveVar(varName(), info()) :
      eQName(ARROWSPEC, sc.funcNS);
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
        if(!wsConsume(ASSIGN)) check(':');
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
   * @return function item (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr functionItem() throws QueryException {
    skipWs();
    final int ip = pos;

    // parse annotations; will only be visited for XQuery 3.0 expressions
    final Ann ann = curr('%') ? annotations() : null;
    // inline function
    if(wsConsume(FUNCTION) && wsConsume(PAREN1)) {
      if(ann != null) {
        if(ann.contains(Ann.Q_PRIVATE) || ann.contains(Ann.Q_PUBLIC)) throw error(INVISIBLE);
      }
      final HashMap<Var, Expr> nonLocal = new HashMap<>();
      pushVarContext(nonLocal);
      final Var[] args = paramList();
      wsCheck(PAREN2);
      final SeqType type = optAsType();
      final Expr body = enclosed(NOFUNBODY);
      final VarScope scope = popVarContext();
      return new Closure(info(), type, args, body, ann, nonLocal, sc, scope);
    }
    // annotations not allowed here
    if(ann != null) throw error(NOANN);

    // named function reference
    pos = ip;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && consume('#')) {
      if(keyword(name)) throw error(RESERVED_X, name.local());
      final Expr ex = numericLiteral(true);
      if(!(ex instanceof Int)) return ex;
      final int card = (int) ((ANum) ex).itr();
      final Expr lit = Functions.getLiteral(name, card, qc, sc, info());
      return lit != null ? lit : FuncLit.unknown(name, card, qc, sc, info());
    }

    pos = ip;
    return null;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression (may be {@code null})
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

    final boolean dec = consume('.');
    if(dec) {
      // decimal literal
      if(itr) throw error(NUMBERITR);
      tok.add('.');
      while(digit(curr())) tok.add(consume());
    }
    if(XMLToken.isNCStartChar(curr())) return checkDbl();

    if(dec) {
      final byte[] t = tok.toArray();
      if(t.length == 1 && t[0] == '.') throw error(NUMBERDEC_X, t);
      return Dec.get(new BigDecimal(string(trim(t))));
    }

    final long l = toLong(tok.toArray());
    if(l != Long.MIN_VALUE) return Int.get(l);
    final InputInfo ii = info();
    return FnError.get(RANGE_X.get(ii, chop(tok, ii)), SeqType.ITR);
  }

  /**
   * Parses the "DoubleLiteral" rule. Checks if a number is followed by a whitespace.
   * @return expression
   * @throws QueryException query exception
   */
  private Dbl checkDbl() throws QueryException {
    if(!consume('e') && !consume('E')) throw error(NUMBERWS);
    tok.add('e');
    if(curr('+') || curr('-')) tok.add(consume());
    final int s = tok.size();
    while(digit(curr())) tok.add(consume());
    if(s == tok.size()) throw error(NUMBERDBL_X, tok);

    if(XMLToken.isNCStartChar(curr())) throw error(NUMBERWS);
    return Dbl.get(tok.toArray(), info());
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
   * Resolves a relative URI literal against the base uri.
   * @param string uri string
   * @return resolved URI
   * @throws QueryException query exception
   */
  private Uri resolvedUri(final byte[] string) throws QueryException {
    final Uri uri = Uri.uri(string);
    if(!uri.isValid()) throw error(INVURI_X, string);
    return uri.isAbsolute() ? uri : sc.baseURI().resolve(uri, info());
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private QNm varName() throws QueryException {
    check('$');
    skipWs();
    return eQName(NOVARNAME, null);
  }

  /**
   * Parses the "ParenthesizedExpr" rule without the opening parenthesis.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr parenthesized() throws QueryException {
    final Expr e = expr();
    wsCheck(PAREN2);
    return e == null ? Empty.SEQ : e;
  }

  /**
   * Parses the "FunctionCall" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int i = pos;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && !keyword(name)) {
      if(wsConsume(PAREN1)) {
        final Expr ret = function(name);
        if(ret != null) return ret;
      }
    }
    pos = i;
    return null;
  }

  /**
   * Returns a function.
   * @param name function name
   * @param exprs initial expressions
   * @return function (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr function(final QNm name, final Expr... exprs) throws QueryException {
    final InputInfo ii = info();
    final ExprList argList = new ExprList().add(exprs);
    final int[] holes = argumentList(argList, name.string());
    final Expr[] args = argList.finish();
    alter = FUNCUNKNOWN_X;
    alterFunc = name;
    alterPos = pos;

    final Expr ret;
    if(holes != null) {
      final int card = args.length + holes.length;
      final Expr lit = Functions.getLiteral(name, card, qc, sc, ii);
      final Expr f = lit != null ? lit : FuncLit.unknown(name, card, qc, sc, ii);
      ret = new PartFunc(sc, ii, f, args, holes);
      if(lit != null && (lit instanceof FuncItem ? ((FuncItem) f).annotations() :
        ((FuncLit) lit).annotations()).contains(Ann.Q_UPDATING)) qc.updating();
    } else {
      final TypedFunc f = Functions.get(name, args, qc, sc, ii);
      if(f == null) {
        ret = null;
      } else {
        if(f.ann.contains(Ann.Q_UPDATING)) qc.updating();
        ret = f.fun;
      }
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
        if(wsConsume(QUESTION)) {
          holes = holes == null ? new int[] { i } : Array.add(holes, i);
        } else {
          final Expr e = single();
          if(e == null) throw error(FUNCMISS_X, name);
          args.add(e);
        }
        i++;
      } while(wsConsume(COMMA));
      if(!wsConsume(PAREN2)) throw error(FUNCMISS_X, name);
    }
    return holes;
  }

  /**
   * Parses the "Constructor" rule.
   * Parses the "DirectConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr constructor() throws QueryException {
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
    final int npos = names.size();

    final QNm name = new QNm(qName(ELEMNAME_X));
    names.add(new QNmCheck(name));
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
                add(attv, enclosed(NOENCLEXPR));
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
        names.add(new QNmCheck(attn, false));
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

    assignURI(npos);

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
          return txt != null ? txt : next() == '/' ? null : constructor();
        }
      } else if(c == '{') {
        if(next() == '{') {
          tb.add(consume());
          consume();
        } else {
          final Str txt = text(tb, strip);
          return txt != null ? txt : enclosed(NOENCLEXPR);
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
   * @return string item (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr compConstructor() throws QueryException {
    final int i = pos;
    if(wsConsumeWs(DOCUMENT))  return consume(compDoc(), i);
    if(wsConsumeWs(ELEMENT))   return consume(compElement(), i);
    if(wsConsumeWs(ATTRIBUTE)) return consume(compAttribute(), i);
    if(wsConsumeWs(NSPACE))    return consume(compNamespace(), i);
    if(wsConsumeWs(TEXT))      return consume(compText(), i);
    if(wsConsumeWs(COMMENT))   return consume(compComment(), i);
    if(wsConsumeWs(PI))        return consume(compPI(), i);
    return null;
  }

  /**
   * Consumes the specified expression or resets the query position.
   * @param expr expression
   * @param p query position
   * @return expression (may be {@code null})
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
    if(!wsConsume(CURLY1)) return null;
    final Expr e = check(expr(), NODOCCONS);
    wsCheck(CURLY2);
    return new CDoc(sc, info(), e);
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
      names.add(new QNmCheck(qn));
    } else {
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NOELEMNAME);
      wsCheck(CURLY2);
    }

    if(!wsConsume(CURLY1)) return null;
    final Expr e = expr();
    wsCheck(CURLY2);
    return new CElem(sc, info(), name, null, e == null ? new Expr[0] : new Expr[] { e });
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
      names.add(new QNmCheck(qn, false));
    } else {
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NOATTNAME);
      wsCheck(CURLY2);
    }

    if(!wsConsume(CURLY1)) return null;
    final Expr e = expr();
    wsCheck(CURLY2);
    return new CAttr(sc, info(), true, name, e == null ? Empty.SEQ : e);
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
      if(!wsConsume(CURLY1)) return null;
      name = check(expr(), NSWRONG);
      wsCheck(CURLY2);
    } else {
      name = Str.get(str);
    }

    if(!wsConsume(CURLY1)) return null;
    final Expr e = expr();
    wsCheck(CURLY2);
    return new CNSpace(sc, info(), name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "CompTextConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compText() throws QueryException {
    if(!wsConsume(CURLY1)) return null;
    final Expr e = check(expr(), NOTXTCONS);
    wsCheck(CURLY2);
    return new CTxt(sc, info(), e);
  }

  /**
   * Parses the "CompCommentConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compComment() throws QueryException {
    if(!wsConsume(CURLY1)) return null;
    final Expr e = check(expr(), NOCOMCONS);
    wsCheck(CURLY2);
    return new CComm(sc, info(), e);
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

    if(!wsConsume(CURLY1)) return null;
    final Expr e = expr();
    wsCheck(CURLY2);
    return new CPI(sc, info(), name, e == null ? Empty.SEQ : e);
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
      final SeqType ret = itemType();
      wsCheck(PAREN2);
      return ret;
    }

    // parse optional annotation and type name
    final Ann ann = curr('%') ? annotations() : null;
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
        if(t != null) return functionTest(ann, t).seqType();
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
    if(ann != null) throw error(NOANN);

    // atomic value, or closing parenthesis
    if(!func || wsConsume(PAREN2)) return t.seqType();

    // raise error if type different to node is not finalized by a parenthesis
    if(!(t instanceof NodeType)) wsCheck(PAREN2);

    // return type with an optional kind test for node types
    return SeqType.get(t, Occ.ONE, kindTest((NodeType) t));
  }

  /**
   * Parses the "FunctionTest" rule.
   * @param ann annotations
   * @param t function type
   * @return resulting type
   * @throws QueryException query exception
   */
  private Type functionTest(final Ann ann, final Type t) throws QueryException {
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
    final SeqType st = sequenceType();
    return FuncType.get(ann, st, args);
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
    final Test t = elem ? elementTest() : schemaTest();
    wsCheck(PAREN2);
    return new DocTest(t != null ? t : Test.ELM);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test elementTest() throws QueryException {
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
    return new NodeTest(NodeType.ELM, name, type, sc.strip);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test schemaTest() throws QueryException {
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
    return new NodeTest(NodeType.ATT, name, type, sc.strip);
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!wsConsumeWs(TRY)) return null;

    final Expr tr = enclosed(NOENCLEXPR);
    wsCheck(CATCH);

    Catch[] ct = { };
    do {
      NameTest[] codes = { };
      do {
        skipWs();
        final NameTest test = (NameTest) nodeTest(false, false);
        if(test == null) throw error(NOCATCH);
        codes = Array.add(codes, test);
      } while(wsConsumeWs(PIPE));

      final int s = openSubScope();
      final int cl = Catch.NAMES.length;
      final Var[] vs = new Var[cl];
      for(int i = 0; i < cl; i++) vs[i] = addVar(Catch.NAMES[i], Catch.TYPES[i], false);
      final Catch c = new Catch(info(), codes, vs);
      c.expr = enclosed(NOENCLEXPR);
      closeSubScope(s);

      ct = Array.add(ct, c);
    } while(wsConsumeWs(CATCH));

    return new Try(info(), tr, ct);
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
    if(wsConsumeWs(WEIGHT)) expr = new FTWeight(info(), expr, enclosed(NOENCLEXPR));

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
      e = enclosed(NOENCLEXPR);
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
          boolean union = false;
          boolean except = false;
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
              final IO fl = qc.stop != null ? qc.stop.get(fn) : sc.io(fn);
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
    final IO fl = qc.thes != null ? qc.thes.get(fn) : sc.io(fn);
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr deletee() throws QueryException {
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
   * @return query expression (may be {@code null})
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
   * @return query expression (may be {@code null})
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
   * Parses the "TransformExpr" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr transform() throws QueryException {
    if(!wsConsumeWs(COPY, DOLLAR, INCOMPLETE)) return null;
    final int s = openSubScope();

    Let[] fl = { };
    do {
      final QNm name = varName();
      wsCheck(ASSIGN);
      final Expr e = check(single(), INCOMPLETE);
      fl = Array.add(fl, new Let(addVar(name, SeqType.NOD, false), e, false, info()));
    } while(wsConsumeWs(COMMA));
    wsCheck(MODIFY);

    final Expr m = check(single(), INCOMPLETE);
    wsCheck(RETURN);
    final Expr r = check(single(), INCOMPLETE);

    closeSubScope(s);
    qc.updating();
    return new Transform(info(), fl, m, r);
  }

  /**
   * Parses the "UpdatingFunctionCall" rule.
   * @return query expression (may be {@code null})
   * @throws QueryException query exception
   */
  private Expr updatingFunctionCall() throws QueryException {
    final int p = pos;
    if(wsConsumeWs(UPDATING)) {
      final Expr func = primary();
      if(wsConsume(PAREN1)) {
        final InputInfo ii = info();
        final ExprList argList = new ExprList();

        if(!wsConsume(PAREN2)) {
          do {
            final Expr e = single();
            if(e == null) throw error(FUNCMISS_X, func);
            argList.add(e);
          } while(wsConsume(COMMA));
          if(!wsConsume(PAREN2)) throw error(FUNCMISS_X, func);
        }
        qc.updating();
        return new DynFuncCall(ii, sc, true, func, argList.finish());
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
   * @return QName (may be {@code null})
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
        throw error(NOURI_X, name);
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
   * Creates and registers a new local variable in the current scope.
   * @param name variable name
   * @param tp variable type
   * @param prm if the variable is a function parameter
   * @return registered variable
   */
  private Var addVar(final QNm name, final SeqType tp, final boolean prm) {
    return localVars.get(localVars.size() - 1).addVar(name, tp, prm);
  }

  /**
   * Tries to resolve a local variable reference.
   * @param name variable name
   * @param ii input info
   * @return variable reference (may be {@code null})
   */
  private VarRef resolveLocalVar(final QNm name, final InputInfo ii) {
    int l = localVars.size();
    Var var = null;

    // look up through the scopes until we find the declaring scope
    while(--l >= 0) {
      var = localVars.get(l).stack.get(name);
      if(var != null) break;
    }

    // looked through all scopes, must be a static variable
    if(var == null) return null;

    // go down through the scopes and add bindings to their closures
    final int ls = localVars.size();
    while(++l < ls) {
      final VarContext vctx = localVars.get(l);
      final Var local = vctx.addVar(var.name, var.seqType(), false);
      vctx.nonLocal.put(local, new VarRef(ii, var));
      var = local;
    }

    // return the properly propagated variable reference
    return new VarRef(ii, var);
  }

  /**
   * Resolves the referenced variable as a local or static variable and returns a reference to it.
   * IF the variable is not declared, the specified error is thrown.
   * @param name variable name
   * @param ii input info
   * @return referenced variable
   * @throws QueryException if the variable isn't defined
   */
  private Expr resolveVar(final QNm name, final InputInfo ii) throws QueryException {
    // local variable
    final VarRef local = resolveLocalVar(name, ii);
    if(local != null) return local;

    // static variable
    final byte[] uri = name.uri();

    // accept variable reference...
    // - if a variable uses the module or an imported URI, or
    // - if it is specified in the main module
    if(module == null || eq(module.uri(), uri) || modules.contains(uri))
      return qc.vars.newRef(name, sc, ii);

    throw error(VARUNDEF_X, '$' + string(name.string()));
  }

  /**
   * Pushes a new variable context onto the stack.
   * @param nonLocal mapping for non-local variables
   */
  private void pushVarContext(final HashMap<Var, Expr> nonLocal) {
    localVars.add(new VarContext(nonLocal));
  }

  /**
   * Pops one variable context from the stack.
   * @return the removed context's variable scope
   */
  private VarScope popVarContext() {
    return localVars.remove(localVars.size() - 1).scope;
  }

  /**
   * Opens a new sub-scope inside the current one. The returned marker has to be supplied to the
   * corresponding call to {@link #closeSubScope(int)} in order to mark the variables as
   * inaccessible.
   * @return marker for the current bindings
   */
  private int openSubScope() {
    return localVars.get(localVars.size() - 1).stack.size();
  }

  /**
   * Closes the sub-scope and marks all contained variables as inaccessible.
   * @param marker marker for the start of the sub-scope
   */
  private void closeSubScope(final int marker) {
    localVars.get(localVars.size() - 1).stack.size(marker);
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
   * @param expr alternative error message
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
    while(++pos < length) {
      char curr = curr();
      if(curr == '(' && next() == ':') {
        comment();
        curr = curr();
      }
      if(curr == ':' && next() == ')') {
        pos += 2;
        if(moduleDoc.isEmpty()) {
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
    if(alter != FUNCUNKNOWN_X) return error(alter);
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
   * Creates the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return error
   */
  QueryException error(final QueryError err, final Object... arg) {
    return err.get(info(), arg);
  }

  /**
   * Finalizes the QNames by assigning namespace URIs.
   * @param npos first entry to be checked
   * @throws QueryException query exception
   */
  private void assignURI(final int npos) throws QueryException {
    for(int i = npos; i < names.size(); i++) {
      if(names.get(i).assign(npos == 0)) names.remove(i--);
    }
  }

  /** Cache for checking QNames after their construction. */
  private class QNmCheck {
    /** QName to be checked. */
    final QNm name;
    /** Flag for assigning default element namespace. */
    final boolean nsElem;

    /**
     * Constructor.
     * @param nm qname
     */
    QNmCheck(final QNm nm) {
      this(nm, true);
    }

    /**
     * Constructor.
     * @param nm qname
     * @param nse default check
     */
    QNmCheck(final QNm nm, final boolean nse) {
      name = nm;
      nsElem = nse;
    }

    /**
     * Assigns the namespace URI that is currently in scope.
     * @param check check if prefix URI was assigned
     * @return true if URI has a URI
     * @throws QueryException query exception
     */
    boolean assign(final boolean check) throws QueryException {
      if(name.hasURI()) return true;

      if(name.hasPrefix()) {
        name.uri(sc.ns.uri(name.prefix()));
        if(check && !name.hasURI()) throw error(NOURI_X, name.string());
      } else if(nsElem) {
        name.uri(sc.elemNS);
      }
      return name.hasURI();
    }
  }

  /** Variable context for resolving local variables. */
  private class VarContext {
    /** Stack of local variables. */
    final VarStack stack = new VarStack();
    /** Current scope containing all variables and the closure. */
    final VarScope scope = new VarScope(sc);
    /** Non-local variable bindings for closures. */
    final HashMap<Var, Expr> nonLocal;

    /**
     * Constructor.
     * @param bindings non-local variable bindings for closures
     */
    public VarContext(final HashMap<Var, Expr> bindings) {
      nonLocal = bindings;
    }

    /**
     * Adds a new variable to this context.
     * @param name variable name
     * @param tp variable type
     * @param prm promotion flag
     * @return the variable
     */
    public Var addVar(final QNm name, final SeqType tp, final boolean prm) {
      final Var var = scope.newLocal(qc, name, tp, prm);
      stack.push(var);
      return var;
    }
  }
}
