package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpN.OpN;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.expr.Context;
import org.basex.query.expr.List;
import org.basex.query.ft.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.*;
import org.basex.query.gflwor.GFLWOR.Clause;
import org.basex.query.gflwor.Window.Condition;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.up.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.format.*;
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
  /** Reserved function names (XQuery 1.0). */
  private static final TokenSet KEYWORDS10 = new TokenSet();
  /** Reserved function names (XQuery 3.0). */
  private static final TokenSet KEYWORDS30 = new TokenSet();

  static {
    final byte[][] keys = {
      NodeType.ATT.string(), NodeType.COM.string(), NodeType.DOC.string(),
      NodeType.ELM.string(), token(EMPTY_SEQUENCE), token(IF), AtomType.ITEM.string(),
      NodeType.NOD.string(), NodeType.PI.string(), token(SCHEMA_ATTRIBUTE),
      token(SCHEMA_ELEMENT), NodeType.TXT.string(), token(TYPESWITCH)
    };
    for(final byte[] key : keys) {
      KEYWORDS10.add(key);
      KEYWORDS30.add(key);
    }
    KEYWORDS30.add(FuncType.ANY_FUN.string());
    KEYWORDS30.add(NodeType.NSP.string());
    KEYWORDS30.add(SWITCH);
  }

  /** Modules loaded by the current file. */
  public final TokenSet modules = new TokenSet();
  /** Parsed variables. */
  public final ArrayList<StaticVar> vars = new ArrayList<StaticVar>();
  /** Parsed functions. */
  public final ArrayList<StaticFunc> funcs = new ArrayList<StaticFunc>();
  /** Namespaces. */
  public final TokenMap namespaces = new TokenMap();

  /** Query context. */
  private final QueryContext ctx;
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
  private Err alter;
  /** Function name of alternative error. */
  private QNm alterFunc;
  /** Alternative position. */
  private int alterPos;

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<String>();

  /** Cached QNames. */
  private final ArrayList<QNmCheck> names = new ArrayList<QNmCheck>();

  /** Stack of variable contexts. */
  private final ArrayList<VarContext> localVars = new ArrayList<VarContext>();

  /**
   * Constructor.
   * @param in input
   * @param path file path (if {@code null}, {@link MainOptions#QUERYPATH} will be assigned)
   * @param c query context
   * @param sctx static context
   * @throws QueryException query exception
   */
  public QueryParser(final String in, final String path, final QueryContext c,
      final StaticContext sctx) throws QueryException {

    super(in);
    ctx = c;

    // set path to query file
    final MainOptions opts = c.context.options;
    final String bi = path != null ? path : opts.get(MainOptions.QUERYPATH);
    sc = sctx != null ? sctx : new StaticContext(opts.get(MainOptions.XQUERY3));
    if(!bi.isEmpty()) sc.baseURI(bi);

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
            bind(key, val);
            key.setLength(0);
            val.setLength(0);
            first = true;
            continue;
          }
          // commas are escaped by a second comma
          s++;
        }
        val.append(ch);
      }
    }
    bind(key, val);
  }

  /**
   * Binds the specified variable.
   * If a URI is specified, the query is treated as a module.
   * @param key key
   * @param val value
   * @throws QueryException query exception
   */
  private void bind(final StringBuilder key, final StringBuilder val) throws QueryException {
    final String k = key.toString().trim();
    if(!k.isEmpty()) ctx.bind(k, new Atm(val.toString()), null);
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
      final Expr e = expr();
      if(e == null) throw alter == null ? error(EXPREMPTY) : error();
      final VarScope scope = popVarContext();

      final MainModule mm = new MainModule(e, scope, moduleDoc, sc);
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
      final byte[] pref = ncName(NONAME);
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
      ctx.modParsed.put(p, uri);

      ctx.modStack.push(p);
      prolog1();
      prolog2();

      finish(null, check);

      ctx.modStack.pop();
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
    file = baseIO == null ? null : ctx.context.user.has(Perm.ADMIN) ? baseIO.path() : baseIO.name();
    if(!more()) throw error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    for(int i = 0; i < length;) {
      // only retrieve code points for large character codes (faster)
      int cp = input.charAt(i);
      final boolean hs = cp >= Character.MIN_HIGH_SURROGATE;
      if(hs) cp = input.codePointAt(i);
      if(!XMLToken.valid(cp)) {
        pos = i;
        throw error(QUERYINV, cp);
      }
      i += hs ? Character.charCount(cp) : 1;
    }
  }

  /**
   * Finishes the parsing step.
   * @param mm main module, {@code null} for library modules
   * @param check if functions and variables should be checked
   * @throws QueryException query exception
   */
  private void finish(final MainModule mm, final boolean check) throws QueryException {
    if(more()) {
      if(alter != null) throw error();
      final String rest = rest();
      pos++;
      if(mm == null) throw error(MODEXPR, rest);
      throw error(QUERYEND, rest);
    }

    // completes the parsing step
    assignURI(0);
    if(sc.elemNS != null) sc.ns.add(EMPTY, sc.elemNS, null);

    // set default decimal format
    final byte[] empty = new QNm(EMPTY).id();
    if(sc.decFormats.get(empty) == null) {
      sc.decFormats.put(empty, new DecFormatter());
    }

    if(check) {
      // check function calls and variable references
      ctx.funcs.check(ctx);
      ctx.vars.check();

      // check placement of updating expressions if any have been found
      if(ctx.updates != null) {
        ctx.funcs.checkUp();
        ctx.vars.checkUp();
        if(mm != null) mm.expr.checkUp();
      }
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
      if(ver.equals(XQ10)) sc.xquery3 = false;
      else if(eq(ver, XQ11, XQ30)) sc.xquery3 = true;
      else throw error(XQUERYVER, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if((version || sc.xquery3()) && wsConsumeWs(ENCODING)) {
      final String enc = string(stringLiteral());
      if(!supported(enc)) throw error(XQUERYENC2, enc);
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
             !emptyOrderDecl() && !(sc.xquery3() && decimalFormatDecl(true)))
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
        } else if(sc.xquery3() && wsConsumeWs(DECIMAL_FORMAT)) {
          decimalFormatDecl(false);
        } else if(wsConsumeWs(NSPACE)) {
          namespaceDecl();
        } else if(wsConsumeWs(FT_OPTION)) {
          final FTOpt fto = new FTOpt();
          while(ftMatchOption(fto));
          ctx.ftOpt().copy(fto);
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

      if(sc.xquery3() && wsConsumeWs(CONTEXT)) {
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
          } else if(sc.xquery3() && consume('%')) {
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
    final QNm name = eQName(QNAMEINV, XQURI);

    final ValueBuilder vb = new ValueBuilder();
    if(wsConsumeWs(PAR1)) {
      do {
        final Expr ex = literal();
        if(!(ex instanceof Item)) throw error(ANNVALUE);
        vb.add((Item) ex);
      } while(wsConsumeWs(COMMA));
      wsCheck(PAR2);
    }
    skipWs();
    ann.add(name, vb.value(), info);
  }

  /**
   * Parses the "NamespaceDecl" rule.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final byte[] pref = ncName(NONAME);
    wsCheck(IS);
    final byte[] uri = stringLiteral();
    if(sc.ns.staticURI(pref) != null) throw error(DUPLNSDECL, pref);
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
    if(eq(XMLURI, uri)) throw error(BINDXMLURI, uri, XML);
    if(eq(XMLNSURI, uri)) throw error(BINDXMLURI, uri, XMLNS);

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
    final QNm qnm = eQName(QNAMEINV, sc.xquery3() ? XQURI : URICHECK);
    final byte[] val = stringLiteral();
    final String name = string(qnm.local());

    if(sc.xquery3() && eq(qnm.uri(), OUTPUTURI)) {
      // output declaration
      if(module != null) throw error(MODOUT);

      if(ctx.serialOpts == null) {
        ctx.serialOpts = ctx.context.options.get(MainOptions.SERIALIZER);
      }
      if(!decl.add("S " + name)) throw error(OUTDUPL, name);
      try {
        ctx.serialOpts.assign(name, string(val));
      } catch(final BaseXException ex) {
        for(final Option<?> o : ctx.serialOpts) if(o.name().equals(name)) throw error(SERANY, ex);
        throw error(OUTINVALID, ex);
      }

      if(name.equals(SerializerOptions.PARAMETER_DOCUMENT.name())) {
        final IO io = IO.get(string(resolvedUri(val).string()));
        try {
          final ANode node = new DBNode(io, ctx.context.options).children().next();
          // check parameters and add values to serialization parameters
          final InputInfo info = info();
          FuncOptions.serializer(node, ctx.serialOpts, info);

          final HashMap<String, String> free = ctx.serialOpts.free();
          if(!free.isEmpty()) throw SERWHICH.get(info, free.keySet().iterator().next());
          final StringOption cm = SerializerOptions.USE_CHARACTER_MAPS;
          if(!ctx.serialOpts.get(cm).isEmpty()) throw SERWHICH.get(info, cm.name());
        } catch(final IOException ex) {
          throw error(OUTDOC, val);
        }
      }
    } else if(sc.xquery3() && eq(qnm.uri(), XQURI)) {
      throw error(DECLOPTION, qnm);
    } else if(eq(qnm.uri(), DBURI)) {
      // project-specific declaration
      final String ukey = name.toUpperCase(Locale.ENGLISH);
      final Option<?> opt = ctx.context.options.option(ukey);
      if(opt == null) throw error(BASX_OPTIONS, ukey);
      // cache old value (to be reset after query evaluation)
      ctx.staticOpts.put(opt, ctx.context.options.get(opt));
      ctx.tempOpts.add(name).add(string(val));
    } else if(eq(qnm.uri(), QUERYURI)) {
      // query-specific options
      if(name.equals(READ_LOCK)) {
        for(final byte[] lock : split(val, ','))
          ctx.readLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else if(name.equals(WRITE_LOCK)) {
        for(final byte[] lock : split(val, ','))
          ctx.writeLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else {
        throw error(BASX_OPTIONS, name);
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
    final QNm name = def ? new QNm() : eQName(QNAMEINV, null);

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
        if(map.get(s) != null) throw error(DECDUPLPROP, s);
        wsCheck(IS);
        map.put(s, stringLiteral());
        break;
      }
      if(map.isEmpty()) throw error(NODECLFORM, prop);
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
    sc.collation = Collation.get(stringLiteral(), ctx, sc, info(), WHICHDEFCOLL);
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
      pref = ncName(NONAME);
      if(eq(pref, XML, XMLNS)) throw error(BINDXML, pref);
      wsCheck(IS);
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NSPACE);
    }
    byte[] ns = stringLiteral();
    if(pref != null && ns.length == 0) throw error(NSEMPTY);
    if(!Uri.uri(ns).isValid()) throw error(INVURI, ns);
    if(wsConsumeWs(AT)) {
      do {
        ns = stringLiteral();
        if(!Uri.uri(ns).isValid()) throw error(INVURI, ns);
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
      pref = ncName(NONAME);
      wsCheck(IS);
    }

    final byte[] uri = trim(stringLiteral());
    if(uri.length == 0) throw error(NSMODURI);
    if(!Uri.uri(uri).isValid()) throw error(INVURI, uri);
    if(modules.contains(uri)) throw error(DUPLMODULE, uri);
    modules.add(uri);

    // add non-default namespace
    if(pref != EMPTY) {
      if(sc.ns.staticURI(pref) != null) throw error(DUPLNSDECL, pref);
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
    final byte[] path = ctx.modDeclared.get(uri);
    if(path != null) {
      module(path, uri);
      return;
    }

    // check built-in modules
    for(final byte[] u : Function.URIS.values()) if(eq(uri, u)) return;

    // resolve module uri
    if(ctx.modules.addImport(uri, info(), this)) return;

    throw error(WHICHMODULE, uri);
  }

  /**
   * Parses the specified module.
   * @param path file path
   * @param uri module uri
   * @throws QueryException query exception
   */
  public void module(final byte[] path, final byte[] uri) throws QueryException {
    // get absolute path
    final IO io = sc.io(string(path));
    final byte[] p = token(io.path());

    // check if module has already been parsed
    final byte[] u = ctx.modParsed.get(p);
    if(u != null) {
      if(!eq(uri, u)) throw error(WRONGMODULE, uri,
          ctx.context.user.has(Perm.ADMIN) ? io.path() : io.name());
      if(!sc.xquery3() && ctx.modStack.contains(p)) throw error(CIRCMODULE);
      return;
    }
    ctx.modParsed.put(p, uri);

    // read module
    String qu = null;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      throw error(WHICHMODFILE, ctx.context.user.has(Perm.ADMIN) ? io.path() : io.name());
    }

    ctx.modStack.push(p);
    final StaticContext sub = new StaticContext(sc.xquery3());
    final LibraryModule lib = new QueryParser(qu, io.path(), ctx, sub).parseLibrary(false);
    final byte[] muri = lib.name.uri();

    // check if import and declaration uri match
    if(!eq(uri, muri)) throw error(WRONGMODULE, muri, file);

    // check if context item declaration types are compatible to each other
    if(sub.initType != null) {
      if(sc.initType == null) {
        sc.initType = sub.initType;
      } else if(!sub.initType.eq(sc.initType)) {
        throw error(CITYPES, sub.initType, sc.initType);
      }
    }
    ctx.modStack.pop();
  }

  /**
   * Parses the "ContextItemDecl" rule.
   * @throws QueryException query exception
   */
  private void contextItemDecl() throws QueryException {
    wsCheck(ITEMM);
    if(!decl.add(ITEMM)) throw error(DUPLITEM);

    if(wsConsumeWs(AS)) {
      final SeqType type = itemType();
      if(sc.initType == null) {
        sc.initType = type;
      } else if(!sc.initType.eq(type)) {
        throw error(CITYPES, sc.initType, type);
      }
    }

    if(!wsConsumeWs(EXTERNAL)) wsCheck(ASSIGN);
    else if(!wsConsumeWs(ASSIGN)) return;

    pushVarContext(null);
    final Expr e = check(single(), NOVARDECL);
    final SeqType type = sc.initType == null ? SeqType.ITEM : sc.initType;
    final VarScope scope = popVarContext();
    ctx.ctxItem = new MainModule(e, scope, type, currDoc.toString(), sc, info());

    if(module != null) throw error(DECITEM);
    if(e.has(Flag.UPD)) throw error(UPCTX, e);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void varDecl(final Ann ann) throws QueryException {
    final QNm vn = varName();
    final SeqType tp = optAsType();
    if(module != null && !eq(vn.uri(), module.uri())) throw error(MODNS, vn);

    pushVarContext(null);
    final boolean external = wsConsumeWs(EXTERNAL);
    final Expr bind;
    if(external) {
      bind = sc.xquery3() && wsConsumeWs(ASSIGN) ? check(single(), NOVARDECL) : null;
    } else {
      wsCheck(ASSIGN);
      bind = check(single(), NOVARDECL);
    }

    final VarScope scope = popVarContext();
    vars.add(ctx.vars.declare(vn, tp, ann, bind, external, sc, scope,
        currDoc.toString(), info()));
  }

  /**
   * Parses an optional SeqType declaration.
   * @return type if preceded by {@code as}, {@code null} otherwise
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
    if(sc.xquery3() && keyword(name)) throw error(RESERVED, name.local());
    wsCheck(PAR1);
    if(module != null && !eq(name.uri(), module.uri())) throw error(MODNS, name);

    pushVarContext(null);
    final Var[] args = paramList();
    wsCheck(PAR2);
    final SeqType tp = optAsType();
    if(ann.contains(Ann.Q_UPDATING)) ctx.updating(false);
    final Expr body = wsConsumeWs(EXTERNAL) ? null : enclosed(NOFUNBODY);
    final VarScope scope = popVarContext();

    funcs.add(ctx.funcs.declare(ann, name, args, tp, body, sc, scope,
        currDoc.toString(), ii));
  }

  /**
   * Checks if the specified name equals reserved function names.
   * @param name name to be checked
   * @return result of check
   */
  private boolean keyword(final QNm name) {
    return !name.hasPrefix() &&
        (sc.xquery3() ? KEYWORDS30 : KEYWORDS10).contains(name.string());
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
        if(v.name.eq(var.name)) throw error(FUNCDUPL, var);

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
  private Expr enclosed(final Err err) throws QueryException {
    wsCheck(BRACE1);
    final Expr e = check(expr(), err);
    wsCheck(BRACE2);
    return e;
  }

  /**
   * Parses the "Expr" rule.
   * @return query expression
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
   * @return query expression
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
    if(e == null) e = or();
    return e;
  }

  /**
   * Parses the "FLWORExpr" rule.
   * Parses the "WhereClause" rule.
   * Parses the "OrderByClause" rule.
   * Parses the "OrderSpecList" rule.
   * Parses the "GroupByClause" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr flwor() throws QueryException {
    final int s = openSubScope();
    final LinkedList<Clause> clauses = initialClause(null);
    if(clauses == null) return null;

    final TokenObjMap<Var> curr = new TokenObjMap<Var>();
    for(final Clause fl : clauses)
      for(final Var v : fl.vars()) curr.put(v.name.id(), v);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(int i = size; i < clauses.size(); i++)
          for(final Var v : clauses.get(i).vars()) curr.put(v.name.id(), v);
      } while(size < clauses.size());

      if(wsConsumeWs(WHERE)) {
        alterPos = pos;
        clauses.add(new Where(check(single(), NOWHERE), info()));
        alter = NOWHERE;
      }

      if(sc.xquery3() && wsConsumeWs(GROUP)) {
        wsCheck(BY);
        skipWs();
        alterPos = pos;
        final GroupBy.Spec[] specs = groupSpecs(clauses);

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<VarRef>();
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
          if(v.type().one())
            nv.refineType(SeqType.get(v.type().type, Occ.ONE_MORE), ctx, info());
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

      if(sc.xquery3() && wsConsumeWs(COUNT, DOLLAR, NOCOUNT)) {
        final Var v = addVar(varName(), SeqType.ITR, false);
        curr.put(v.name.id(), v);
        clauses.add(new Count(v, info()));
      }
    } while(sc.xquery3() && size < clauses.size());

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
  private LinkedList<Clause> initialClause(final LinkedList<Clause> clauses)
      throws QueryException {
    LinkedList<Clause> cls = clauses;
    // ForClause / LetClause
    final boolean let = wsConsumeWs(LET, SCORE, NOLET) || wsConsumeWs(LET, DOLLAR, NOLET);
    if(let || wsConsumeWs(FOR, DOLLAR, NOFOR)) {
      if(cls == null) cls = new LinkedList<Clause>();
      if(let) letClause(cls);
      else    forClause(cls);
    } else if(sc.xquery3()) {
      // WindowClause
      final boolean slide = wsConsumeWs(FOR, SLIDING, NOWINDOW);
      if(slide || wsConsumeWs(FOR, TUMBLING, NOWINDOW)) {
        if(cls == null) cls = new LinkedList<Clause>();
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

      final boolean emp = sc.xquery3() && wsConsume(ALLOWING);
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
        if(nm.eq(p)) throw error(DUPLVAR, var);
        if(s != null && p.eq(s)) throw error(DUPLVAR, ps);
      }
      if(s != null && nm.eq(s)) throw error(DUPLVAR, var);

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
      Collation.get(stringLiteral(), ctx, sc, info(), FLWORCOLL) : sc.collation;
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
      final SeqType type = optAsType();

      final Expr by;
      if(type != null || wsConsume(ASSIGN)) {
        if(type != null) wsCheck(ASSIGN);
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
        if(!dec) throw error(GVARNOTDEFINED, '$' + string(name.string()));
        by = vr;
      }

      final Collation coll = wsConsumeWs(COLLATION) ? Collation.get(stringLiteral(),
          ctx, sc, info(), FLWORCOLL) : sc.collation;
      final GroupBy.Spec spec =
          new GroupBy.Spec(ii, addVar(name, type, false), by, coll);
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
   * @return query expression
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!sc.xquery3() || !wsConsumeWs(SWITCH, PAR1, TYPEPAR)) return null;
    wsCheck(PAR1);
    final Expr expr = check(expr(), NOSWITCH);
    SwitchCase[] exprs = { };
    wsCheck(PAR2);

    // collect all cases
    ExprList cases;
    do {
      cases = new ExprList(null);
      while(wsConsumeWs(CASE)) add(cases, single());
      if(cases.size() == 1) {
        // add default case
        if(exprs.length == 0) throw error(WRONGCHAR, CASE, found());
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr typeswitch() throws QueryException {
    if(!wsConsumeWs(TYPESWITCH, PAR1, TYPEPAR)) return null;
    wsCheck(PAR1);
    final Expr ts = check(expr(), NOTYPESWITCH);
    wsCheck(PAR2);

    TypeCase[] cases = { };
    final ArrayList<SeqType> types = new ArrayList<SeqType>();
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
        while(sc.xquery3() && wsConsume(PIPE));
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr iff() throws QueryException {
    if(!wsConsumeWs(IF, PAR1, IFPAR)) return null;
    wsCheck(PAR1);
    final Expr iff = check(expr(), NOIF);
    wsCheck(PAR2);
    if(!wsConsumeWs(THEN)) throw error(NOIF);
    final Expr thn = check(single(), NOIF);
    if(!wsConsumeWs(ELSE)) throw error(NOIF);
    final Expr els = check(single(), NOIF);
    return new If(info(), iff, thn, els);
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    if(!wsConsumeWs(OR)) return e;

    final ExprList el = new ExprList(e);
    do add(el, and()); while(wsConsumeWs(OR));
    return new Or(info(), el.finish());
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr e = modify();
    if(!wsConsumeWs(AND)) return e;

    final ExprList el = new ExprList(e);
    do add(el, modify()); while(wsConsumeWs(AND));
    return new And(info(), el.finish());
  }

  /**
   * Parses the "CopyExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr modify() throws QueryException {
    final Expr e = comparison();
    if(e != null) {
      if(wsConsumeWs(UPDATE)) {
        final int s = openSubScope();
        final boolean u = ctx.updating;
        ctx.updating(false);
        final Expr m = check(single(), COPYEXPR);
        closeSubScope(s);
        ctx.updating = u;
        return new Modify(info(), e, m);
      }
    }
    return e;
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr e = ftContains();
    if(e != null) {
      for(final OpV c : OpV.VALUES) if(wsConsumeWs(c.name))
        return new CmpV(e, check(ftContains(), CMPEXPR), c, sc.collation, info());
      for(final OpN c : OpN.VALUES) if(wsConsumeWs(c.name))
        return new CmpN(e, check(ftContains(), CMPEXPR), c, info());
      for(final OpG c : OpG.VALUES) if(wsConsumeWs(c.name))
        return new CmpG(e, check(ftContains(), CMPEXPR), c, sc.collation, info());
    }
    return e;
  }

  /**
   * Parses the "FTContainsExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr ftContains() throws QueryException {
    final Expr e = stringConcat();

    final int i = pos;
    // extensions to the official extension: "=>" and "<-"
    if(consume('=') && consume('>') || consume('<') && consume('-')) {
      skipWs();
    } else if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      pos = i;
      return e;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      throw error(FTIGNORE);
    }
    return new FTContainsExpr(e, select, info());
  }

  /**
   * Parses the "StringConcatExpr" rule.
   * @return query expression
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr e = additive();
    if(!wsConsumeWs(TO)) return e;
    return new Range(info(), e, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
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
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr value() throws QueryException {
    validate();
    final Expr e = path();
    return e != null ? e : extension();
  }

  /**
   * Parses the "ValidateExpr" rule.
   * @throws QueryException query exception
   */
  private void validate() throws QueryException {
    final int i = pos;
    if(!wsConsumeWs(VALIDATE)) return;

    boolean brace = true;
    if(consume(BRACE1)) {
      brace = false;
    } else if(consume(TYPE)) {
      final QNm qnm = eQName(QNAMEINV, SKIPCHECK);
      names.add(new QNmCheck(qnm));
    } else if(!consume(STRICT) && !consume(LAX)) {
      pos = i;
      return;
    }

    if(brace) wsCheck(BRACE1);
    check(single(), NOVALIDATE);
    wsCheck(BRACE2);
    throw error(IMPLVAL);
  }

  /**
   * Parses the "ExtensionExpr" rule.
   * @return query expression
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
   * @return array of pragmas
   * @throws QueryException query exception
   */
  private Pragma[] pragma() throws QueryException {
    if(!wsConsumeWs(PRAGMA)) return null;

    final ArrayList<Pragma> el = new ArrayList<Pragma>();
    do {
      final QNm name = eQName(QNAMEINV, URICHECK);
      char c = curr();
      if(c != '#' && !ws(c)) throw error(PRAGMAINV);
      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) throw error(PRAGMAINV);
        tok.add(consume());
        c = curr();
      }

      final byte[] v = tok.trim().finish();
      if(eq(name.prefix(), DB)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final Option<?> opt = ctx.context.options.option(key);
        if(opt == null) throw error(BASX_OPTIONS, key);
        el.add(new DBPragma(name, opt, v));
      }
      pos += 2;
    } while(wsConsumeWs(PRAGMA));
    return el.toArray(new Pragma[el.size()]);
  }

  /**
   * Parses the "PathExpr" rule.
   * @return query expression
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
        add(el, descOrSelf());
        mark();
        ex = step();
        if(ex == null) {
          // two slashes, but no following step: error
          if(more()) checkInit();
          throw error(PATHMISS, found());
        }
      } else {
        // one slash: absolute child path
        checkAxis(Axis.CHILD);
        mark();
        ex = step();
        // no more steps: return root expression
        if(ex == null) return root;
      }
      add(el, ex);
      relativePath(el);
    } else {
      // relative path (no preceding slash)
      mark();
      final Expr ex = step();
      if(ex == null) return null;
      // return non-step expression if no path or map operator follows
      final boolean nostep = curr() != '/' && (curr() != '!' || next() == '=');
      if(nostep && !(ex instanceof Step)) return ex;
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
  void relativePath(final ExprList el) throws QueryException {
    while(true) {
      boolean map = false;
      if(consume('/')) {
        if(consume('/')) {
          add(el, descOrSelf());
          checkAxis(Axis.DESC);
        } else {
          checkAxis(Axis.CHILD);
        }
      } else if(next() != '=' && consume('!')) {
        map = true;
      } else {
        return;
      }
      mark();
      Expr st = step();
      if(st == null) throw error(PATHMISS, found());
      if(map) st = new Bang(info(), st);
      add(el, st);
    }
  }

  /**
   * Returns a standard descendant-or-self::node() step.
   * @return step
   */
  private Step descOrSelf() {
    return Step.get(info(), Axis.DESCORSELF, Test.NOD);
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
  protected void checkAxis(final Axis axis) { }

  /**
   * Performs an optional test check.
   * @param test node test
   * @param attr attribute flag
   */
  @SuppressWarnings("unused")
  protected void checkTest(final Test test, final boolean attr) { }

  /**
   * Checks a predicate.
   * @param open open flag
   */
  @SuppressWarnings("unused")
  protected void checkPred(final boolean open) { }

  /**
   * Parses the "StepExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr step() throws QueryException {
    final Expr e = postfix();
    return e != null ? e : axisStep();
  }

  /**
   * Parses the "AxisStep" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Step axisStep() throws QueryException {
    Axis ax = null;
    Test test = null;
    if(wsConsume(DOT2)) {
      ax = Axis.PARENT;
      test = Test.NOD;
      checkTest(test, false);
    } else if(consume('@')) {
      ax = Axis.ATTR;
      test = nodeTest(true, true);
      checkTest(test, true);
      if(test == null) {
        --pos;
        throw error(NOATTNAME);
      }
    } else {
      for(final Axis a : Axis.VALUES) {
        final int i = pos;
        if(!wsConsumeWs(a.name)) continue;
        alter = NOLOCSTEP;
        if(wsConsumeWs(COLS)) {
          alterPos = pos;
          ax = a;
          test = nodeTest(a == Axis.ATTR, true);
          checkTest(test, a == Axis.ATTR);
          break;
        }
        pos = i;
      }
    }

    if(ax == null) {
      ax = Axis.CHILD;
      test = nodeTest(false, true);
      if(test == Test.NSP) throw error(NSNOTALL);
      if(test != null && test.type == NodeType.ATT) ax = Axis.ATTR;
      checkTest(test, ax == Axis.ATTR);
    }
    if(test == null) return null;

    final ExprList el = new ExprList();
    while(wsConsume(BR1)) {
      checkPred(true);
      add(el, expr());
      wsCheck(BR2);
      checkPred(false);
    }
    return Step.get(info(), ax, test, el.finish());
  }

  /**
   * Parses the "NodeTest" rule.
   * Parses the "NameTest" rule.
   * Parses the "KindTest" rule.
   * @param att attribute flag
   * @param all check all tests, or only names
   * @return query expression
   * @throws QueryException query exception
   */
  private Test nodeTest(final boolean att, final boolean all) throws QueryException {
    final int i = pos;
    if(consume('*')) {
      // name test: *
      if(!consume(':')) return new NameTest(att);
      // name test: *:name
      return new NameTest(new QNm(ncName(QNAMEINV)), NameTest.Mode.LN, att, sc.elemNS);
    }

    if(sc.xquery3() && consume(EQNAME)) {
      // name test: Q{...}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        final QNm nm = new QNm(COLON, uri);
        return new NameTest(nm, NameTest.Mode.NS, att, sc.elemNS);
      }
    }
    pos = i;

    final QNm name = eQName(null, SKIPCHECK);
    if(name != null) {
      final int i2 = pos;
      if(all && wsConsumeWs(PAR1)) {
        final NodeType type = NodeType.find(name);
        if(type != null) return kindTest(type);
      } else {
        pos = i2;
        // name test: prefix:name, name
        if(name.hasPrefix() || !consume(':')) {
          skipWs();
          names.add(new QNmCheck(name, !att));
          return new NameTest(name, NameTest.Mode.STD, att, sc.elemNS);
        }
        // name test: prefix:*
        if(consume('*')) {
          final QNm nm = new QNm(concat(name.string(), COLON));
          names.add(new QNmCheck(nm, !att));
          return new NameTest(nm, NameTest.Mode.NS, att, sc.elemNS);
        }
      }
    }
    pos = i;
    return null;
  }

  /**
   * Parses the "FilterExpr" rule.
   * Parses the "Predicate" rule.
   * @return postfix expression
   * @throws QueryException query exception
   */
  private Expr postfix() throws QueryException {
    Expr e = primary(), old;
    do {
      old = e;
      if(wsConsume(BR1)) {
        if(e == null) throw error(PREDMISSING);
        final ExprList el = new ExprList();
        do {
          add(el, expr());
          wsCheck(BR2);
        } while(wsConsume(BR1));
        e = Filter.get(info(), e, el.finish());
      } else if(e != null) {
        if(!wsConsume(PAR1)) break;

        final InputInfo ii = info();
        final ExprList argList = new ExprList();
        final int[] holes = argumentList(argList, e);
        final Expr[] args = argList.finish();
        e = holes == null ? new DynFuncCall(ii, e, args) : new PartFunc(sc, ii, e, args, holes);
      }
    } while(e != old);
    return e;
  }

  /**
   * Parses the "PrimaryExpr" rule.
   * Parses the "VarRef" rule.
   * Parses the "ContextItem" rule.
   * Parses the "Literal" rule.
   * @return query expression
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
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
    // function item
    if(sc.xquery3()) {
      final Expr e = functionItem();
      if(e != null) return e;
    }
    // function call
    Expr e = functionCall();
    if(e != null) return e;
    // computed constructors
    e = compConstructor();
    if(e != null) return e;
    // ordered expression
    if(wsConsumeWs(ORDERED, BRACE1, INCOMPLETE) ||
        wsConsumeWs(UNORDERED, BRACE1, INCOMPLETE)) return enclosed(NOENCLEXPR);
    // map (including legacy syntax)
    if(wsConsumeWs(MAPSTR, BRACE1, INCOMPLETE) || curr('{')) return new LitMap(info(), keyValues());
    // general array constructor
    //if(wsConsumeWs(ARRAY, BRACE1, INCOMPLETE)) return new LitArray(info(), keyValues());

    // context item
    if(c == '.' && !digit(next())) {
      if(next() == '.') return null;
      consume('.');
      return new Context(info());
    }
    // literals
    return literal();
  }

  /**
   * Parses keys and values of maps and arrays.
   * @return map literal
   * @throws QueryException query exception
   */
  private Expr[] keyValues() throws QueryException {
    wsCheck(BRACE1);
    final ExprList el = new ExprList();
    if(!wsConsume(BRACE2)) {
      do {
        add(el, check(single(), INVMAPKEY));
        if(!wsConsume(ASSIGN)) check(':');
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(COMMA));
      wsCheck(BRACE2);
    }
    return el.finish();
  }

  /**
   * Parses the "FunctionItemExpr" rule.
   * Parses the "NamedFunctionRef" rule.
   * Parses the "LiteralFunctionItem" rule.
   * Parses the "InlineFunction" rule.
   * @return query expression, or {@code null}
   * @throws QueryException query exception
   */
  private Expr functionItem() throws QueryException {
    skipWs();
    final int ip = pos;

    // parse annotations; will only be visited for XQuery 3.0 expressions
    final Ann ann = curr('%') ? annotations() : null;
    // inline function
    if(wsConsume(FUNCTION) && wsConsume(PAR1)) {
      if(ann != null) {
        if(ann.contains(Ann.Q_UPDATING)) throw error(UPFUNCITEM);
        if(ann.contains(Ann.Q_PRIVATE) || ann.contains(Ann.Q_PUBLIC)) throw error(INVISIBLE);
      }
      final HashMap<Var, Expr> nonLocal = new HashMap<Var, Expr>();
      pushVarContext(nonLocal);
      final Var[] args = paramList();
      wsCheck(PAR2);
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
      if(keyword(name)) throw error(RESERVED, name.local());
      final Expr ex = numericLiteral(true);
      if(!(ex instanceof Int)) return ex;
      final long card = ((ANum) ex).itr();
      final Expr lit = Functions.getLiteral(name, (int) card, ctx, sc, info());
      return lit != null ? lit : FuncLit.unknown(name, card, ctx, sc, info());
    }

    pos = ip;
    return null;
  }

  /**
   * Parses the "Literal" rule.
   * @return query expression, or {@code null}
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
   * @return query expression
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
      final byte[] t = tok.finish();
      if(t.length == 1 && t[0] == '.') throw error(NUMBERDEC, t);
      return new Dec(t);
    }

    final long l = toLong(tok.finish());
    if(l != Long.MIN_VALUE) return Int.get(l);
    return FNInfo.error(new QueryException(info(), RANGE, tok), SeqType.ITR);
  }

  /**
   * Parses the "DoubleLiteral" rule. Checks if a number is followed by a
   * whitespace.
   * @return expression
   * @throws QueryException query exception
   */
  private Dbl checkDbl() throws QueryException {
    if(!consume('e') && !consume('E')) throw error(NUMBERWS);
    tok.add('e');
    if(curr('+') || curr('-')) tok.add(consume());
    final int s = tok.size();
    while(digit(curr())) tok.add(consume());
    if(s == tok.size()) throw error(NUMBERDBL, tok);

    if(XMLToken.isNCStartChar(curr())) throw error(NUMBERWS);
    return Dbl.get(tok.finish(), info());
  }

  /**
   * Parses the "StringLiteral" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] stringLiteral() throws QueryException {
    skipWs();
    final char del = curr();
    if(!quote(del)) throw error(NOQUOTE, found());
    consume();
    tok.reset();
    while(true) {
      while(!consume(del)) {
        if(!more()) throw error(NOQUOTE, found());
        entity(tok);
      }
      if(!consume(del)) break;
      tok.add(del);
    }
    return tok.finish();
  }

  /**
   * Resolves a relative URI literal against the base uri.
   * @param string uri string
   * @return resolved URI
   * @throws QueryException query exception
   */
  private Uri resolvedUri(final byte[] string) throws QueryException {
    final Uri uri = Uri.uri(string);
    if(!uri.isValid()) throw error(INVURI, string);
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
      if(!more()) throw error(WRONGCHAR, BRACE2, found());
      entity(tok);
    }
    return tok.finish();
  }

  /**
   * Parses the "VarName" rule.
   * @return query expression
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
    wsCheck(PAR1);
    final Expr e = expr();
    wsCheck(PAR2);
    return e == null ? Empty.SEQ : e;
  }

  /**
   * Parses the "FunctionCall" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr functionCall() throws QueryException {
    final int i = pos;
    final QNm name = eQName(null, sc.funcNS);
    if(name != null && !keyword(name)) {
      if(wsConsume(PAR1)) {
        final InputInfo ii = info();
        final ExprList argList = new ExprList();
        final int[] holes = argumentList(argList, name.string());
        final Expr[] args = argList.finish();
        alter = FUNCUNKNOWN;
        alterFunc = name;
        alterPos = pos;

        final Expr ret;
        if(holes != null) {
          final int card = args.length + holes.length;
          final Expr lit = Functions.getLiteral(name, card, ctx, sc, ii),
              f = lit != null ? lit : FuncLit.unknown(name, card, ctx, sc, ii);
          ret = new PartFunc(sc, ii, f, args, holes);
        } else {
          final TypedFunc f = Functions.get(name, args, false, ctx, sc, ii);
          ret = f == null ? null : f.fun;
        }

        if(ret != null) {
          alter = null;
          return ret;
        }
      }
    }

    pos = i;
    return null;
  }

  /**
   * Parses the "ArgumentList" rule after the opening parenthesis was read.
   * @param args list to put the argument expressions into
   * @param name name of the function (item)
   * @return array of arguments, place-holders '?' are represented as {@code null} entries
   * @throws QueryException query exception
   */
  private int[] argumentList(final ExprList args, final Object name) throws QueryException {
    int[] holes = null;
    if(!wsConsume(PAR2)) {
      int i = 0;
      do {
        if(wsConsume(PLHOLDER)) {
          holes = holes == null ? new int[] { i } : Array.add(holes, i);
        } else {
          final Expr e = single();
          if(e == null) throw error(FUNCMISS, name);
          args.add(e);
        }
        i++;
      } while(wsConsume(COMMA));
      if(!wsConsume(PAR2)) throw error(FUNCMISS, name);
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

    final QNm tag = new QNm(qName(TAGNAME));
    names.add(new QNmCheck(tag));
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
      if(!quote(delim)) throw error(NOQUOTE, found());
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
              final byte[] text = tb.finish();
              if(text.length == 0) {
                add(attv, enclosed(NOENCLEXPR));
                simple = false;
              } else {
                add(attv, Str.get(text));
              }
              tb.reset();
            }
          } else if(ch == '}') {
            consume();
            check('}');
            tb.add('}');
          } else if(ch == '<' || ch == 0) {
            throw error(NOQUOTE, found());
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
        if(eq(pref, XML) && eq(uri, XMLURI)) {
          if(xmlDecl) throw error(DUPLNSDEF, XML);
          xmlDecl = true;
        } else {
          if(!Uri.uri(uri).isValid()) throw error(INVURI, uri);
          if(pr) {
            if(uri.length == 0) throw error(NSEMPTYURI);
            if(eq(pref, XML, XMLNS)) throw error(BINDXML, pref);
            if(eq(uri, XMLURI)) throw error(BINDXMLURI, uri, XML);
            if(eq(uri, XMLNSURI)) throw error(BINDXMLURI, uri, XMLNS);
            sc.ns.add(pref, uri);
          } else {
            if(eq(uri, XMLURI)) throw error(XMLNSDEF, uri);
            sc.elemNS = uri;
          }
          if(ns.contains(pref)) throw error(DUPLNSDEF, pref);
          ns.add(pref, uri);
        }
      } else {
        final QNm attn = new QNm(atn);
        if(atts == null) atts = new ArrayList<QNm>(1);
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
        final Expr e = dirElemContent(tag.string());
        if(e == null) continue;
        add(cont, e);
      }
      pos += 2;

      final byte[] close = qName(TAGNAME);
      consumeWS();
      check('>');
      if(!eq(tag.string(), close)) throw error(TAGWRONG, tag.string(), close);
    }

    assignURI(npos);

    // check for duplicate attribute names
    if(atts != null) {
      final int as = atts.size();
      for(int a = 0; a < as - 1; a++) {
        for(int b = a + 1; b < as; b++) {
          if(atts.get(a).eq(atts.get(b))) throw error(ATTDUPL, atts.get(a));
        }
      }
    }

    sc.ns.size(s);
    sc.elemNS = nse;
    return new CElem(sc, info(), tag, ns, cont.finish());
  }

  /**
   * Parses the "DirElemContent" rule.
   * @param tag opening tag
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElemContent(final byte[] tag) throws QueryException {
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
        throw error(NOCLOSING, tag);
      }
    } while(true);
  }

  /**
   * Returns a string item.
   * @param tb token builder
   * @param strip strip flag
   * @return text or {@code null}
   */
  private Str text(final TokenBuilder tb, final boolean strip) {
    final byte[] t = tb.finish();
    return t.length == 0 || strip && !sc.spaces && ws(t) ?
        null : Str.get(t);
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
      while(not('-'))
        tb.add(consume());
      consume();
      if(consume('-')) {
        check('>');
        return new CComm(sc, info(), Str.get(tb.finish()));
      }
      tb.add('-');
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
    if(eq(lc(str), XML)) throw error(PIXML, str);

    final boolean space = skipWs();
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('?')) {
        if(!space) throw error(PIWRONG);
        tb.add(consume());
      }
      consume();
      if(consume('>')) {
        return new CPI(sc, info(), Str.get(str), Str.get(tb.finish()));
      }
      tb.add('?');
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
      while(not(']')) {
        char ch = consume();
        if(ch == '\r') {
          ch = '\n';
          if(curr(ch)) consume();
        }
        tb.add(ch);
      }
      consume();
      if(curr(']') && next() == '>') {
        pos += 2;
        return tb.finish();
      }
      tb.add(']');
    }
  }

  /**
   * Parses the "ComputedConstructor" rule.
   * @return query expression
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
    if(!wsConsume(BRACE1)) return null;
    final Expr e = check(expr(), NODOCCONS);
    wsCheck(BRACE2);
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
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), NOTAG);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
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
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), NOATTNAME);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CAttr(sc, info(), true, name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "CompNamespaceConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compNamespace() throws QueryException {
    if(!sc.xquery3()) return null;
    skipWs();

    final Expr name;
    final byte[] str = ncName(null);
    if(str.length == 0) {
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), NSWRONG);
      wsCheck(BRACE2);
    } else {
      name = Str.get(str);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CNSpace(sc, info(), name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "CompTextConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compText() throws QueryException {
    if(!wsConsume(BRACE1)) return null;
    final Expr e = check(expr(), NOTXTCONS);
    wsCheck(BRACE2);
    return new CTxt(sc, info(), e);
  }

  /**
   * Parses the "CompCommentConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compComment() throws QueryException {
    if(!wsConsume(BRACE1)) return null;
    final Expr e = check(expr(), NOCOMCONS);
    wsCheck(BRACE2);
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
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), PIWRONG);
      wsCheck(BRACE2);
    } else {
      name = Str.get(str);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
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
        if(wsConsume(PAR1)) throw error(SIMPLETYPE, name);
        if(sc.xquery3) {
          if(!AtomType.AST.name.eq(name)) throw error(TYPEUNKNOWN30, name);
          t = AtomType.AST;
        } else {
          throw error(TYPEUNKNOWN, name);
        }
      }
      if(t == AtomType.AST || t == AtomType.AAT || t == AtomType.NOT)
        throw error(CASTUNKNOWN, name);
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
    if(wsConsumeWs(EMPTY_SEQUENCE, PAR1, null)) {
      wsCheck(PAR1);
      wsCheck(PAR2);
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
    if(consume(PAR1)) {
      final SeqType ret = itemType();
      wsCheck(PAR2);
      return ret;
    }

    // parse optional annotation and type name
    final Ann ann = sc.xquery3() && curr('%') ? annotations() : null;
    final QNm name = eQName(TYPEINVALID, null);
    skipWs();
    // check if name is followed by parentheses
    final boolean func = curr('(');

    // item type
    Type t = null;
    if(func) {
      consume(PAR1);
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
      if(t == null) throw error(NOTYPE, name.string());
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(sc.elemNS);
      // atomic types
      t = AtomType.find(name, false);
      // no type found
      if(t == null) throw error(TYPEUNKNOWN, name);
    }

    // annotations are not allowed for remaining types
    if(ann != null) throw error(NOANN);

    // atomic value, or closing parenthesis
    if(!func || wsConsume(PAR2)) return t.seqType();

    // raise error if type different to node is not finalized by a parenthesis
    if(!(t instanceof NodeType)) wsCheck(PAR2);

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
      wsCheck(PAR2);
      return t;
    }

    // map
    if(t instanceof MapType) {
      final Type key = itemType().type;
      if(!key.instanceOf(AtomType.AAT)) throw error(MAPTAAT, key);
      wsCheck(COMMA);
      final Type tp = MapType.get((AtomType) key, sequenceType());
      wsCheck(PAR2);
      return tp;
    }

    // function type
    SeqType[] args = { };
    if(!wsConsume(PAR2)) {
      // function has got arguments
      do args = Array.add(args, sequenceType());
      while(wsConsume(COMMA));
      wsCheck(PAR2);
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
    wsCheck(PAR2);
    if(tp != null) return tp;
    tp = Test.get(t);
    if(tp == Test.NSP && !sc.xquery3) throw error(NSNOTALL);
    return tp;
  }

  /**
   * Parses the "DocumentTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test documentTest() throws QueryException {
    final boolean elem = consume(ELEMENT);
    if(!elem && !consume(SCHEMA_ELEMENT)) return null;

    wsCheck(PAR1);
    skipWs();
    final Test t = elem ? elementTest() : schemaTest();
    wsCheck(PAR2);
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
      final QNm tn = eQName(QNAMEINV, sc.elemNS);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) throw error(TYPEUNDEF, tn);
      // parse optional question mark
      wsConsume(PLHOLDER);
    }
    return new NodeTest(NodeType.ELM, name, type, sc.strip);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test schemaTest() throws QueryException {
    final QNm name = eQName(QNAMEINV, sc.elemNS);
    throw error(SCHEMAINV, name);
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
      final QNm tn = eQName(QNAMEINV, sc.elemNS);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) throw error(TYPEUNDEF, tn);
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
      if(!XMLToken.isNCName(nm)) throw error(INVNCNAME, nm);
    } else if(ncName()) {
      nm = tok.finish();
    } else {
      return null;
    }
    return new NodeTest(NodeType.PI, new QNm(nm));
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!sc.xquery3() || !wsConsumeWs(TRY)) return null;

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
      final Var[] vs = new Var[Catch.NAMES.length];
      for(int i = 0; i < Catch.NAMES.length; i++)
        vs[i] = addVar(Catch.NAMES[i], Catch.TYPES[i], false);
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
          FTUnit unit = null;
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
      first.expr[0] = new FTOrder(info(), first.expr[0]);
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
    if(!Tokenizer.supportFor(fto.ln)) throw error(FTNOTOK, fto.ln);
    if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln)) throw error(FTNOSTEM, fto.ln);

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
      wsCheck(BRACE1);
      final FTExpr e = ftSelection(true);
      wsCheck(BRACE2);
      return new FTExtensionSelection(info(), pragmas, e);
    }

    if(wsConsumeWs(PAR1)) {
      final FTExpr e = ftSelection(false);
      wsCheck(PAR2);
      return e;
    }

    skipWs();
    Expr e = null;
    if(quote(curr())) {
      e = Str.get(stringLiteral());
    } else if(curr('{')) {
      e = enclosed(NOENCLEXPR);
    } else {
      throw error(prg ? NOPRAGMA : NOFTSELECT, found());
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
    while(digit(curr()))
      tok.add(consume());
    if(tok.isEmpty()) throw error(INTEXP);
    return Int.get(toLong(tok.finish()));
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
      if(opt.cs != null) throw error(FTDUP, CASE);
      opt.cs = FTCase.LOWER;
    } else if(wsConsumeWs(UPPERCASE)) {
      if(opt.cs != null) throw error(FTDUP, CASE);
      opt.cs = FTCase.UPPER;
    } else if(wsConsumeWs(CASE)) {
      if(opt.cs != null) throw error(FTDUP, CASE);
      if(wsConsumeWs(SENSITIVE)) {
        opt.cs = FTCase.SENSITIVE;
      } else {
        opt.cs = FTCase.INSENSITIVE;
        wsCheck(INSENSITIVE);
      }
    } else if(wsConsumeWs(DIACRITICS)) {
      if(opt.isSet(DC)) throw error(FTDUP, DIACRITICS);
      opt.set(DC, wsConsumeWs(SENSITIVE));
      if(!opt.is(DC)) wsCheck(INSENSITIVE);
    } else if(wsConsumeWs(LANGUAGE)) {
      if(opt.ln != null) throw error(FTDUP, LANGUAGE);
      final byte[] lan = stringLiteral();
      opt.ln = Language.get(string(lan));
      if(opt.ln == null) throw error(FTNOTOK, lan);
    } else if(wsConsumeWs(OPTION)) {
      optionDecl();
    } else {
      final boolean using = !wsConsumeWs(NO);

      if(wsConsumeWs(STEMMING)) {
        if(opt.isSet(ST)) throw error(FTDUP, STEMMING);
        opt.set(ST, using);
      } else if(wsConsumeWs(THESAURUS)) {
        if(opt.th != null) throw error(FTDUP, THESAURUS);
        opt.th = new ThesQuery();
        if(using) {
          final boolean par = wsConsume(PAR1);
          if(!wsConsumeWs(DEFAULT)) ftThesaurusID(opt.th);
          while(par && wsConsume(COMMA))
            ftThesaurusID(opt.th);
          if(par) wsCheck(PAR2);
        }
      } else if(wsConsumeWs(STOP)) {
        // add union/except
        wsCheck(WORDS);

        if(opt.sw != null) throw error(FTDUP, STOP + ' ' + WORDS);
        opt.sw = new StopWords();
        if(wsConsumeWs(DEFAULT)) {
          if(!using) throw error(FTSTOP);
        } else {
          boolean union = false;
          boolean except = false;
          while(using) {
            if(wsConsume(PAR1)) {
              do {
                final byte[] sl = stringLiteral();
                if(except) opt.sw.delete(sl);
                else if(!union || !opt.sw.contains(sl)) opt.sw.add(sl);
              } while(wsConsume(COMMA));
              wsCheck(PAR2);
            } else if(wsConsumeWs(AT)) {
              final String fn = string(stringLiteral());
              // optional: resolve URI reference
              final IO fl = ctx.stop != null ? ctx.stop.get(fn) : sc.io(fn);
              if(!opt.sw.read(fl, except)) throw error(NOSTOPFILE, fl);
            } else if(!union && !except) {
              throw error(FTSTOP);
            }
            union = wsConsumeWs(UNION);
            except = !union && wsConsumeWs(EXCEPT);
            if(!union && !except) break;
          }
        }
      } else if(wsConsumeWs(WILDCARDS)) {
        if(opt.isSet(WC)) throw error(FTDUP, WILDCARDS);
        if(opt.is(FZ)) throw error(BXFT_MATCH);
        opt.set(WC, using);
      } else if(wsConsumeWs(FUZZY)) {
        // extension to the official extension: "using fuzzy"
        if(opt.isSet(FZ)) throw error(FTDUP, FUZZY);
        if(opt.is(WC)) throw error(BXFT_MATCH);
        opt.set(FZ, using);
      } else {
        throw error(FTMATCH, consume());
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
    final IO fl = ctx.thes != null ? ctx.thes.get(fn) : sc.io(fn);
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
    thes.add(new Thesaurus(fl, rel, min, max, ctx.context));
  }

  /**
   * Parses the "InsertExpr" rule.
   * @return query expression
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
    ctx.updating(true);
    return new Insert(sc, info(), s, first, last, before, after, trg);
  }

  /**
   * Parses the "DeleteExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr deletee() throws QueryException {
    final int i = pos;
    if(!wsConsumeWs(DELETE) || !wsConsumeWs(NODES) && !wsConsumeWs(NODE)) {
      pos = i;
      return null;
    }
    ctx.updating(true);
    return new Delete(sc, info(), check(single(), INCOMPLETE));
  }

  /**
   * Parses the "RenameExpr" rule.
   * @return query expression
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
    ctx.updating(true);
    return new Rename(sc, info(), trg, n);
  }

  /**
   * Parses the "ReplaceExpr" rule.
   * @return query expression
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
    ctx.updating(true);
    return new Replace(sc, info(), t, r, v);
  }

  /**
   * Parses the "TransformExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr transform() throws QueryException {
    if(!wsConsumeWs(COPY, DOLLAR, INCOMPLETE)) return null;
    final int s = openSubScope();
    final boolean u = ctx.updating;
    ctx.updating(false);

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
    ctx.updating = u;
    return new Transform(info(), fl, m, r);
  }

  /**
   * Parses the "NCName" rule.
   * @param err optional error message
   * @return string
   * @throws QueryException query exception
   */
  private byte[] ncName(final Err err) throws QueryException {
    tok.reset();
    if(ncName()) return tok.finish();
    if(err != null) throw error(err, consume());
    return EMPTY;
  }

  /**
   * Parses the "EQName" rule.
   * @param err optional error message. Will be thrown if no EQName is found,
   *   or ignored if set to {@code null}
   * @param def default namespace, or operation mode
   *   ({@link #URICHECK}, {@link #SKIPCHECK})
   * @return string
   * @throws QueryException query exception
   */
  private QNm eQName(final Err err, final byte[] def) throws QueryException {
    final int i = pos;
    if(sc.xquery3() && consume(EQNAME)) {
      final byte[] uri = bracedURILiteral();
      final byte[] name = ncName(null);
      if(name.length != 0) {
        if(def == URICHECK && uri.length == 0) {
          pos = i;
          throw error(NOURI, name);
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
        throw error(NSMISS, name);
      }
      if(name.hasPrefix()) {
        pos = i;
        throw error(NOURI, name);
      }
      name.uri(def);
    }
    return name;
  }

  /**
   * Parses the "QName" rule.
   * @param err optional error message. Will be thrown if no QName is found, and
   *   ignored if set to {@code null}
   * @return string
   * @throws QueryException query exception
   */
  private byte[] qName(final Err err) throws QueryException {
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
    return tok.finish();
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
          if(!m && !h) entityError(i, INVENTITY);
          final long nn = n;
          n = n * b + (consume() & 0xF);
          if(n < nn) ok = false;
          if(!m) n += 9;
        } while(!consume(';'));
        if(!ok) entityError(i, INVCHARREF);
        if(!XMLToken.valid(n)) entityError(i, INVCHARREF);
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
          entityError(i, INVENTITY);
        }
        if(!consume(';')) entityError(i, INVENTITY);
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
   * @param p start position
   * @param c error code
   * @throws QueryException query exception
   */
  private void entityError(final int p, final Err c) throws QueryException {
    final String sub = input.substring(p, Math.min(p + 20, length));
    final int semi = sub.indexOf(';');
    final String ent = semi == -1 ? sub + "..." : sub.substring(0, semi + 1);
    throw error(c, ent);
  }

  /**
   * Raises an error if the specified expression is empty.
   * @param <E> expression type
   * @param expr expression
   * @param err error message
   * @return expression
   * @throws QueryException query exception
   */
  private <E extends Expr> E check(final E expr, final Err err) throws QueryException {
    if(expr == null) throw error(err);
    return expr;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param ch character to be found
   * @throws QueryException query exception
   */
  private void check(final int ch) throws QueryException {
    if(!consume(ch)) throw error(WRONGCHAR, (char) ch, found());
  }

  /**
   * Skips whitespaces, raises an error if the specified string cannot be
   * consumed.
   * @param s string to be found
   * @throws QueryException query exception
   */
  private void wsCheck(final String s) throws QueryException {
    if(!wsConsume(s)) throw error(WRONGCHAR, s, found());
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
   * Resolves a local variable reference.
   * @param name variable name
   * @param ii input info
   * @return variable reference if the variable was found, {@code null} otherwise
   */
  private VarRef resolveLocalVar(final QNm name, final InputInfo ii) {
    int i = localVars.size();
    Var var = null;

    // look up through the scopes until we find the declaring scope
    while(--i >= 0) {
      var = localVars.get(i).stack.get(name);
      if(var != null) break;
    }

    // looked through all scopes, must be a static variable
    if(var == null) return null;

    // go down through the scopes and add bindings to their closures
    while(++i < localVars.size()) {
      final VarContext vctx = localVars.get(i);
      final Var local = vctx.addVar(var.name, var.type(), false);
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

    // accept variable reference if:
    // - XQuery 3.0 is used or variable is declared, and
    // - a variable uses the module or an imported URI or if it is specified in the main module
    if((sc.xquery3() || ctx.vars.declared(name)) &&
        (module == null || eq(module.uri(), uri) || modules.contains(uri)))
      return ctx.vars.newRef(name, sc, ii);

    throw error(VARUNDEF, '$' + string(name.string()));
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
   * Opens a new sub-scope inside the current one. The returned marker has to be supplied to
   * the corresponding call to {@link #closeSubScope(int)} in order to mark the variables
   * as inaccessible.
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
   * Checks if the specified character is not found. An error is raised if the
   * input is exhausted.
   * @param ch character to be found
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean not(final char ch) throws QueryException {
    final char c = curr();
    if(c == 0) throw error(WRONGCHAR, ch, found());
    return c != ch;
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
    if(skipWs() || !XMLToken.isNCStartChar(t.charAt(0))
        || !XMLToken.isNCChar(curr())) return true;
    pos = i;
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position.
   * If the strings are found, the cursor is placed after the first token.
   * @param s1 string to be consumed
   * @param s2 second string
   * @param expr alternative error message
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String s1, final String s2, final Err expr)
      throws QueryException {

    final int i = pos;
    if(!wsConsumeWs(s1)) return false;
    alter = expr;
    alterPos = pos;
    final int i2 = pos;
    final boolean ok = wsConsume(s2);
    pos = ok ? i2 : i;
    return ok;
  }

  /**
   * Skips whitespaces, consumes the specified string and ignores trailing
   * characters.
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
    if(alter != FUNCUNKNOWN) return error(alter);
    final QueryException qe = ctx.funcs.similarError(alterFunc, info());
    return qe == null ? error(alter, alterFunc.string()) : qe;
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param e new expression
   * @throws QueryException query exception
   */
  private void add(final ExprList ar, final Expr e) throws QueryException {
    if(e == null) throw error(INCOMPLETE);
    ar.add(e);
  }

  /**
   * Creates the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return error
   */
  QueryException error(final Err err, final Object... arg) {
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
        if(check && !name.hasURI()) throw error(NOURI, name.string());
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
      final Var var = scope.newLocal(ctx, name, tp, prm);
      stack.push(var);
      return var;
    }
  }
}
