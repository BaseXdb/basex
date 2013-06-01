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
import org.basex.query.expr.Expr.Use;
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

/**
 * Parser for XQuery expressions.
 *
 * @author BaseX Team 2005-12, BSD License
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
  final QueryContext ctx;

  /** XQDoc cache. */
  private final StringBuilder xqdoc = new StringBuilder();
  /** Temporary token cache. */
  private final TokenBuilder tok = new TokenBuilder();

  /** Name of current module. */
  private QNm module;

  /** Alternative error output. */
  private Err alter;
  /** Alternative error description. */
  private QNm alterFunc;
  /** Alternative position. */
  private int ap;

  /** Declared flags. */
  private final HashSet<String> decl = new HashSet<String>();

  /** Cached QNames. */
  private final ArrayList<QNmCheck> names = new ArrayList<QNmCheck>();
  /** Current variable scope. */
  private VarScope scope;

  /**
   * Constructor.
   * @param in input
   * @param path file path (if {@code null}, {@link Prop#QUERYPATH} will be assigned)
   * @param c query context
   * @throws QueryException query exception
   */
  public QueryParser(final String in, final String path, final QueryContext c)
      throws QueryException {

    super(in);
    ctx = c;

    // set path to query file
    final Prop prop = c.context.prop;
    final String bi = path != null ? path : prop.get(Prop.QUERYPATH);
    if(!bi.isEmpty()) c.sc.baseURI(bi);

    // parse pre-defined external variables
    final String bind = prop.get(Prop.BINDINGS).trim();
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
  private void bind(final StringBuilder key, final StringBuilder val)
      throws QueryException {

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
    file(ctx.sc.baseIO(), ctx.context);
    checkValidChars();

    try {
      String doc = xqDoc("");
      versionDecl();

      doc = xqDoc(doc);
      final int i = ip;
      if(wsConsumeWs(MODULE, NSPACE, null)) error(MAINMOD);
      ip = i;

      doc = xqDoc(doc);
      prolog1();
      prolog2();

      scope = new VarScope();
      final Expr e = expr();
      if(e == null) {
        if(alter != null) error();
        else error(EXPREMPTY);
      }

      if(doc.equals(xqdoc.toString())) doc = null;
      final MainModule mm = new MainModule(e, scope, doc);
      scope = null;
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
    file(ctx.sc.baseIO(), ctx.context);
    checkValidChars();

    try {
      String doc = xqDoc("");
      versionDecl();

      doc = xqDoc(doc);
      wsCheck(MODULE);
      wsCheck(NSPACE);
      skipWS();
      final byte[] pref = ncName(NONAME);
      wsCheck(IS);
      final byte[] uri = stringLiteral();
      if(uri.length == 0) error(NSMODURI);
      module = new QNm(pref, uri);
      ctx.sc.ns.add(pref, uri, info());
      namespaces.add(pref, uri);
      wsCheck(";");

      doc = xqDoc(doc);
      prolog1();
      prolog2();

      finish(null, check);
      if(doc.equals(xqdoc.toString())) doc = null;
      return new LibraryModule(module, doc);
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Tries to parse the module documentation.
   * @param doc old documentation string
   * @return resulting root expression
   * @throws QueryException query exception
   */
  private String xqDoc(final String doc) throws QueryException {
    skipWS();
    final String str = xqdoc.toString();
    // no documentation exists: return current string
    if(doc.isEmpty()) return str;

    // nothing has changed: reset documentation buffer
    if(doc.equals(str)) xqdoc.setLength(0);
    return doc;
  }

  /**
   * Checks the input string for illegal characters.
   * @throws QueryException query exception
   */
  private void checkValidChars() throws QueryException {
    if(!more()) error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    for(int i = 0; i < il;) {
      // only retrieve code points for large character codes (faster)
      int cp = input.charAt(i);
      final boolean hs = cp >= Character.MIN_HIGH_SURROGATE;
      if(hs) cp = input.codePointAt(i);
      if(!XMLToken.valid(cp)) {
        ip = i;
        error(QUERYINV, cp);
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
      if(alter != null) error();
      final String rest = rest();
      ip++;
      if(mm == null) error(MODEXPR, rest);
      error(QUERYEND, rest);
    }

    // completes the parsing step
    assignURI(0);
    if(ctx.sc.nsElem != null) ctx.sc.ns.add(EMPTY, ctx.sc.nsElem, null);

    // set default decimal format
    final byte[] empty = new QNm(EMPTY).id();
    if(ctx.sc.decFormats.get(empty) == null) {
      ctx.sc.decFormats.add(empty, new DecFormatter());
    }

    if(check) {
      // check function calls and variable references
      ctx.funcs.check(ctx);
      ctx.vars.check(ctx);

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
    final int i = ip;
    if(!wsConsumeWs(XQUERY)) return;

    final boolean version = wsConsumeWs(VERSION);
    if(version) {
      // parse xquery version
      final String ver = string(stringLiteral());
      if(ver.equals(XQ10)) ctx.sc.xquery3 = false;
      else if(eq(ver, XQ11, XQ30)) ctx.sc.xquery3 = true;
      else error(XQUERYVER, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if((version || ctx.sc.xquery3()) && wsConsumeWs(ENCODING)) {
      final String enc = string(stringLiteral());
      if(!supported(enc)) error(XQUERYENC2, enc);
    } else if(!version) {
      ip = i;
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
      final int i = ip;
      if(wsConsumeWs(DECLARE)) {
        if(wsConsumeWs(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl() &&
             !emptyOrderDecl() && !(ctx.sc.xquery3() && decimalFormatDecl(true)))
            error(DECLINCOMPLETE);
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
        } else if(ctx.sc.xquery3() && wsConsumeWs(DECIMAL_FORMAT)) {
          decimalFormatDecl(false);
        } else if(wsConsumeWs(NSPACE)) {
          namespaceDecl();
        } else if(wsConsumeWs(FT_OPTION)) {
          final FTOpt fto = new FTOpt();
          while(ftMatchOption(fto));
          ctx.ftOpt().copy(fto);
        } else {
          ip = i;
          return;
        }
      } else if(wsConsumeWs(IMPORT)) {
        if(wsConsumeWs(SCHEMA)) {
          schemaImport();
        } else if(wsConsumeWs(MODULE)) {
          moduleImport();
        } else {
          ip = i;
          return;
        }
      } else {
        return;
      }
      xqdoc.setLength(0);
      skipWS();
      check(';');
    }
  }

  /**
   * Parses the "Prolog" rule.
   * @throws QueryException query exception
   */
  private void prolog2() throws QueryException {
    while(true) {
      final int i = ip;
      if(!wsConsumeWs(DECLARE)) break;

      if(ctx.sc.xquery3() && wsConsumeWs(CONTEXT)) {
        contextItemDecl();
      } else if(wsConsumeWs(OPTION)) {
        optionDecl();
      } else if(wsConsumeWs(DEFAULT)) {
        error(PROLOGORDER);
      } else {
        final Ann ann = new Ann();
        while(true) {
          if(wsConsumeWs(UPDATING)) {
            ann.add(Ann.Q_UPDATING, Empty.SEQ);
          } else if(ctx.sc.xquery3() && consume('%')) {
            annotation(ann);
          } else {
            break;
          }
        }
        if(wsConsumeWs(VARIABLE)) {
          // variables cannot be updating
          if(ann.contains(Ann.Q_UPDATING)) error(UPDATINGVAR);
          checkAnnotations(ann, true);
          varDecl(ann);
        } else if(wsConsumeWs(FUNCTION)) {
          checkAnnotations(ann, false);
          functionDecl(ann);
        } else if(!ann.isEmpty()) {
          error(VARFUNC);
        } else {
          ip = i;
          break;
        }
      }
      xqdoc.setLength(0);
      skipWS();
      check(';');
    }

    // check if specified features are supported or can be prohibited
    for(final String d : decl) {
      if(!d.endsWith("_R") && !d.endsWith("_P")) continue;
      final boolean pf = d.charAt(d.length() - 1) == 'P';
      final String k = d.substring(0, d.length() - 2);
      if(eq(k, F_STATIC_TYPING, F_SCHEMA_AWARE, F_ALL_OPTIONAL_FEATURES))
        error(pf ? FEATPROH : FEATNOTSUPP, k);
      if(pf && eq(k, F_MODULE, F_HIGHER_ORDER_FUNCTION, F_ALL_EXTENSIONS,
          F_ALL_OPTIONAL_FEATURES)) error(FEATPROH, k);
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
    checkAnnotations(ann, false);
    skipWS();
    return ann;
  }

  /**
   * Parses a single annotation.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void annotation(final Ann ann) throws QueryException {
    skipWS();
    final QNm name = eQName(QNAMEINV, XQURI);
    final ValueBuilder vb = new ValueBuilder();
    if(wsConsumeWs(PAR1)) {
      do {
        final Expr ex = literal();
        if(ex == null || !(ex instanceof Item)) error(ANNVALUE);
        vb.add((Item) ex);
      } while(wsConsumeWs(COMMA));
      wsCheck(PAR2);
    }
    skipWS();
    ann.add(name, vb.value());
  }

  /**
   * Checks all annotations.
   * @param ann annotations
   * @param var variable flag
   * @throws QueryException query exception
   */
  private void checkAnnotations(final Ann ann, final boolean var) throws QueryException {
    boolean up = false, vis = false;
    for(int a = 0; a < ann.size(); a++) {
      final QNm name = ann.names[a];
      if(name.eq(Ann.Q_UPDATING)) {
        if(up) error(DUPLUPD);
        up = true;
      } else if(name.eq(Ann.Q_PUBLIC) || name.eq(Ann.Q_PRIVATE)) {
        // only one visibility modifier allowed
        if(vis) error(var ? DUPLVARVIS : DUPLVIS);
        vis = true;
      } else if(NSGlobal.reserved(name.uri())) {
        // no global namespaces allowed
        error(ANNRES, name);
      }
    }
  }

  /**
   * Parses the "NamespaceDecl" rule.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final byte[] pref = ncName(NONAME);
    wsCheck(IS);
    final byte[] uri = stringLiteral();
    if(ctx.sc.ns.staticURI(pref) != null) error(DUPLNSDECL, pref);
    ctx.sc.ns.add(pref, uri, info());
    namespaces.add(pref, uri);
  }

  /**
   * Parses the "RevalidationDecl" rule.
   * @throws QueryException query exception
   */
  private void revalidationDecl() throws QueryException {
    if(!decl.add(REVALIDATION)) error(DUPLREVAL);
    if(wsConsumeWs(STRICT) || wsConsumeWs(LAX)) error(NOREVAL);
    wsCheck(SKIP);
  }

  /**
   * Parses the "BoundarySpaceDecl" rule.
   * @throws QueryException query exception
   */
  private void boundarySpaceDecl() throws QueryException {
    if(!decl.add(BOUNDARY_SPACE)) error(DUPLBOUND);
    final boolean spaces = wsConsumeWs(PRESERVE);
    if(!spaces) wsCheck(STRIP);
    ctx.sc.spaces = spaces;
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
    if(eq(XMLURI, uri)) error(BINDXMLURI, uri, XML);
    if(eq(XMLNSURI, uri)) error(BINDXMLURI, uri, XMLNS);

    if(elem) {
      if(!decl.add(ELEMENT)) error(DUPLNS);
      ctx.sc.nsElem = uri.length == 0 ? null : uri;
    } else {
      if(!decl.add(FUNCTION)) error(DUPLNS);
      ctx.sc.nsFunc = uri.length == 0 ? null : uri;
    }
    return true;
  }

  /**
   * Parses the "OptionDecl" rule.
   * @throws QueryException query exception
   */
  private void optionDecl() throws QueryException {
    skipWS();
    final QNm name = eQName(QNAMEINV, ctx.sc.xquery3() ? XQURI : URICHECK);
    final byte[] val = stringLiteral();
    final String key = string(name.local());

    if(ctx.sc.xquery3() && eq(name.uri(), OUTPUTURI)) {
      // output declaration
      if(module != null) error(MODOUT);

      if(ctx.serProp == null) {
        final Prop prop = ctx.context.prop;
        ctx.serProp = new SerializerProp(prop.get(Prop.SERIALIZER));
      }
      if(ctx.serProp.get(key) == null) error(OUTWHICH, key);
      if(!decl.add("S " + key)) error(OUTDUPL, key);
      if(key.equals(SerializerProp.S_PARAMETER_DOCUMENT[0].toString())) {
        final IO io = IO.get(string(resolvedUri(val).string()));
        try {
          final ANode node = new DBNode(io, ctx.context.prop).children().next();
          // check parameters and add values to properties
          final InputInfo info = info();
          final TokenMap tm = FuncParams.serializerMap(node, info);
          FuncParams.serializerProp(tm, info);
          for(final byte[] sk : tm) ctx.serProp.set(string(sk), string(tm.get(sk)));
        } catch(final IOException ex) {
          OUTDOC.thrw(info(), val);
        }
      }

      ctx.serProp.set(key, string(val));
    } else if(ctx.sc.xquery3() && eq(name.uri(), XQURI)) {
      // query-specific options
      final boolean pf = key.equals(PROHIBIT_FEATURE);
      final boolean rf = !pf && key.equals(REQUIRE_FEATURE);
      if(!pf && !rf) error(DECLOPTION, name);

      for(final byte[] vl : split(val, ' ')) {
        if(!XMLToken.isQName(vl)) error(DECLQNAME, val);
        final QNm qn = new QNm(vl, ctx);
        if(!qn.hasURI()) {
          if(qn.hasPrefix()) error(NOURI, qn.prefix());
          qn.uri(XQURI);
        }
        final String k = string(qn.local());
        if(eq(qn.uri(), XQURI) && eq(k, F_SCHEMA_AWARE, F_STATIC_TYPING, F_MODULE,
            F_HIGHER_ORDER_FUNCTION, F_ALL_EXTENSIONS, F_ALL_OPTIONAL_FEATURES)) {

          if(module != null && eq(k, F_MODULE)) error(FEATMODULE, k);
          if(rf && eq(k, F_ALL_EXTENSIONS)) error(FEATREQUALL, k);
          if(decl.contains(k + (pf ? "_R" : "_P"))) error(FEATREQPRO, k);
          decl.add(k + (pf ? "_P" : "_R"));
        } else {
          error(DECLFEAT, vl);
        }
      }

    } else if(eq(name.uri(), DBURI)) {
      // project-specific declaration
      final String ukey = key.toUpperCase(Locale.ENGLISH);
      final Object obj = ctx.context.prop.get(ukey);
      if(obj == null) error(BASX_OPTIONS, ukey);
      // cache old value (to be reset after query evaluation)
      ctx.globalOpt.put(ukey, obj);
      ctx.dbOptions.add(ukey);
      ctx.dbOptions.add(string(val));
    } else if(eq(name.uri(), QUERYURI)) {
      // Query-specific options
      if(key.equals(READ_LOCK)) {
        for(final byte[] lock : split(val, ','))
          ctx.readLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else if(key.equals(WRITE_LOCK)) {
        for(final byte[] lock : split(val, ','))
          ctx.writeLocks.add(DBLocking.USER_PREFIX + string(lock).trim());
      } else {
        error(BASX_OPTIONS, key);
      }
    }
    // ignore unknown options
  }

  /**
   * Parses the "OrderingModeDecl" rule.
   * @throws QueryException query exception
   */
  private void orderingModeDecl() throws QueryException {
    if(!decl.add(ORDERING)) error(DUPLORD);
    ctx.sc.ordered = wsConsumeWs(ORDERED);
    if(!ctx.sc.ordered) wsCheck(UNORDERED);
  }

  /**
   * Parses the "emptyOrderDecl" rule.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean emptyOrderDecl() throws QueryException {
    if(!wsConsumeWs(ORDER)) return false;
    wsCheck(EMPTYORD);
    if(!decl.add(EMPTYORD)) error(DUPLORDEMP);
    ctx.sc.orderGreatest = wsConsumeWs(GREATEST);
    if(!ctx.sc.orderGreatest) wsCheck(LEAST);
    return true;
  }

  /**
   * Parses the "copyNamespacesDecl" rule.
   * Parses the "PreserveMode" rule.
   * Parses the "InheritMode" rule.
   * @throws QueryException query exception
   */
  private void copyNamespacesDecl() throws QueryException {
    if(!decl.add(COPY_NAMESPACES)) error(DUPLCOPYNS);
    ctx.sc.nsPreserve = wsConsumeWs(PRESERVE);
    if(!ctx.sc.nsPreserve) wsCheck(NO_PRESERVE);
    wsCheck(COMMA);
    ctx.sc.nsInherit = wsConsumeWs(INHERIT);
    if(!ctx.sc.nsInherit) wsCheck(NO_INHERIT);
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
    if(ctx.sc.decFormats.get(name.id()) != null) error(DECDUPL);

    // create new format
    final TokenMap map = new TokenMap();
    // collect all property declarations
    int n;
    do {
      n = map.size();
      skipWS();
      final byte[] prop = ncName(null);
      for(final byte[] s : DECFORMATS) {
        if(!eq(prop, s)) continue;
        if(map.get(s) != null) error(DECDUPLPROP, s);
        wsCheck(IS);
        map.add(s, stringLiteral());
        break;
      }
      if(map.isEmpty()) error(NODECLFORM, prop);
    } while(n != map.size());

    // completes the format declaration
    ctx.sc.decFormats.add(name.id(), new DecFormatter(info(), map));
    return true;
  }

  /**
   * Parses the "DefaultCollationDecl" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private boolean defaultCollationDecl() throws QueryException {
    if(!wsConsumeWs(COLLATION)) return false;
    if(!decl.add(COLLATION)) error(DUPLCOLL);
    final byte[] coll = resolvedUri(stringLiteral()).string();
    if(!eq(URLCOLL, coll)) error(COLLWHICH, coll);
    return true;
  }

  /**
   * Parses the "BaseURIDecl" rule.
   * @throws QueryException query exception
   */
  private void baseURIDecl() throws QueryException {
    if(!decl.add(BASE_URI)) error(DUPLBASE);
    final byte[] base = stringLiteral();
    if(base.length != 0) ctx.sc.baseURI(string(base));
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
      if(eq(pref, XML, XMLNS)) error(BINDXML, pref);
      wsCheck(IS);
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NSPACE);
    }
    byte[] ns = stringLiteral();
    if(pref != null && ns.length == 0) error(NSEMPTY);
    if(!Uri.uri(ns).isValid()) error(INVURI, ns);
    if(wsConsumeWs(AT)) {
      do {
        ns = stringLiteral();
        if(!Uri.uri(ns).isValid()) error(INVURI, ns);
      } while(wsConsumeWs(COMMA));
    }
    error(IMPLSCHEMA);
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
    if(uri.length == 0) error(NSMODURI);
    if(!Uri.uri(uri).isValid()) error(INVURI, uri);
    if(modules.contains(uri)) error(DUPLMODULE, uri);
    modules.add(uri);

    // add non-default namespace
    if(pref != EMPTY) {
      if(ctx.sc.ns.staticURI(pref) != null) error(DUPLNSDECL, pref);
      ctx.sc.ns.add(pref, uri, info());
      namespaces.add(pref, uri);
    }

    // check modules at specified locations
    if(wsConsumeWs(AT)) {
      do {
        module(stringLiteral(), uri, true);
      } while(wsConsumeWs(COMMA));
      return;
    }

    // check pre-declared module files
    final byte[] path = ctx.modDeclared.get(uri);
    if(path != null) {
      module(path, uri, true);
      return;
    }

    // check built-in modules
    for(final byte[] u : Function.URIS.values()) if(eq(uri, u)) return;

    // resolve module uri
    if(ctx.modules.addImport(uri, info(), this)) return;

    error(NOMODULE, uri);
  }

  /**
   * Parses the specified module.
   * @param path file path
   * @param uri module uri
   * @throws QueryException query exception
   */
  public void module(final byte[] path, final byte[] uri) throws QueryException {
    module(path, uri, false);
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param path file path
   * @param uri module uri
   * @param imprt if this is an imported module
   * @throws QueryException query exception
   */
  private void module(final byte[] path, final byte[] uri, final boolean imprt)
      throws QueryException {
    // get absolute path
    final IO io = ctx.sc.io(string(path));
    final byte[] p = token(io.path());

    // check if module has already been parsed
    final byte[] u = ctx.modParsed.get(p);
    if(u != null) {
      if(!eq(uri, u)) error(WRONGMODULE, uri,
          ctx.context.user.has(Perm.ADMIN) ? io.path() : io.name());
      if(!ctx.sc.xquery3() && ctx.modStack.contains(p)) error(CIRCMODULE);
      return;
    }
    ctx.modParsed.add(p, uri);

    // read module
    String qu = null;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      error(NOMODULEFILE, ctx.context.user.has(Perm.ADMIN) ? io.path() : io.name());
    }

    ctx.modStack.push(p);
    final StaticContext sc = ctx.sc;
    ctx.sc = new StaticContext(sc.xquery3());
    final LibraryModule lib = new QueryParser(qu, io.path(), ctx).parseLibrary(!imprt);
    final byte[] muri = lib.name.uri();

    // check if import and declaration uri match
    if(!eq(uri, muri)) error(WRONGMODULE, muri, file);
    // check if context item declaration types are compatible to each other

    if(ctx.sc.initType != null) {
      if(sc.initType == null) {
        sc.initType = ctx.sc.initType;
      } else if(!ctx.sc.initType.eq(sc.initType)) {
        error(CITYPES, ctx.sc.initType, sc.initType);
      }
    }
    ctx.sc = sc;
    ctx.modStack.pop();
  }

  /**
   * Parses the "ContextItemDecl" rule.
   * @throws QueryException query exception
   */
  private void contextItemDecl() throws QueryException {
    wsCheck(ITEMM);
    if(!decl.add(ITEMM)) error(DUPLITEM);

    if(wsConsumeWs(AS)) {
      final SeqType type = itemType();
      if(ctx.sc.initType == null) {
        ctx.sc.initType = type;
      } else if(!ctx.sc.initType.eq(type)) {
        error(CITYPES, ctx.sc.initType, type);
      }
    }

    if(!wsConsumeWs(EXTERNAL)) wsCheck(ASSIGN);
    else if(!wsConsumeWs(ASSIGN)) return;
    scope = new VarScope();
    final Expr e = check(single(), NOVARDECL);
    ctx.ctxItem = new MainModule(e, scope, xqdoc.toString());
    scope = null;
    if(module != null) error(DECITEM);
    if(e.uses(Use.UPD)) error(UPCTX, e);
  }

  /**
   * Parses the "VarDecl" rule.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void varDecl(final Ann ann) throws QueryException {
    final QNm vn = varName();
    final SeqType tp = optAsType();
    if(module != null && !eq(vn.uri(), module.uri())) error(MODNS, vn);

    final Expr bind;
    scope = new VarScope();
    final boolean external = wsConsumeWs(EXTERNAL);
    if(external) {
      bind = ctx.sc.xquery3() && wsConsumeWs(ASSIGN) ? check(single(), NOVARDECL) : null;
    } else {
      wsCheck(ASSIGN);
      bind = check(single(), NOVARDECL);
    }

    vars.add(ctx.vars.declare(vn, tp, ann, bind, external, ctx.sc, scope,
        xqdoc.toString(), info()));
    scope = null;
  }

  /**
   * Creates and registers a new local variable in the current scope.
   * @param name variable name
   * @param tp variable type
   * @param prm if the variable is a function parameter
   * @return registered variable
   */
  private Var addLocal(final QNm name, final SeqType tp, final boolean prm) {
    return scope.newLocal(ctx, name, tp, prm);
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
    if(!decl.add(CONSTRUCTION)) error(DUPLCONS);
    ctx.sc.strip = wsConsumeWs(STRIP);
    if(!ctx.sc.strip) wsCheck(PRESERVE);
  }

  /**
   * Parses the "FunctionDecl" rule.
   * @param ann annotations
   * @throws QueryException query exception
   */
  private void functionDecl(final Ann ann) throws QueryException {
    final InputInfo ii = info();
    final QNm name = eQName(FUNCNAME, ctx.sc.nsFunc);
    if(ctx.sc.xquery3() && keyword(name)) error(RESERVED, name.local());
    if(module != null && !eq(name.uri(), module.uri())) error(MODNS, name);

    wsCheck(PAR1);
    scope = new VarScope();
    final Var[] args = paramList();
    wsCheck(PAR2);
    final SeqType tp = optAsType();
    if(ann.contains(Ann.Q_UPDATING)) ctx.updating(false);

    final Expr body = wsConsumeWs(EXTERNAL) ? null : enclosed(NOFUNBODY);
    funcs.add(ctx.funcs.declare(ann, name, args, tp, body, ctx.sc, scope,
        xqdoc.toString(), ii));
    scope = null;
  }

  /**
   * Checks if the specified name equals reserved function names.
   * @param name name to be checked
   * @return result of check
   */
  private boolean keyword(final QNm name) {
    return !name.hasPrefix() &&
        (ctx.sc.xquery3() ? KEYWORDS30 : KEYWORDS10).contains(name.string());
  }

  /**
   * Parses a ParamList.
   * @return declared variables
   * @throws QueryException query exception
   */
  private Var[] paramList() throws QueryException {
    Var[] args = { };
    while(true) {
      skipWS();
      if(curr() != '$') {
        if(args.length == 0) break;
        check('$');
      }
      final Var var = addLocal(varName(), optAsType(), true);
      for(final Var v : args)
        if(v.name.eq(var.name)) error(FUNCDUPL, var);

      args = Array.add(args, var);
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
      if(alter != null) error();
      else error(NOEXPR);
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
    final int s = scope.open();
    final LinkedList<Clause> clauses = initialClause(null);
    if(clauses == null) return null;

    final TokenObjMap<Var> curr = new TokenObjMap<Var>();
    for(final Clause fl : clauses)
      for(final Var v : fl.vars()) curr.add(v.name.id(), v);

    int size;
    do {
      do {
        size = clauses.size();
        initialClause(clauses);
        for(int i = size; i < clauses.size(); i++)
          for(final Var v : clauses.get(i).vars()) curr.add(v.name.id(), v);
      } while(size < clauses.size());

      if(wsConsumeWs(WHERE)) {
        ap = ip;
        clauses.add(new Where(check(single(), NOWHERE), info()));
        alter = NOWHERE;
      }

      if(ctx.sc.xquery3() && wsConsumeWs(GROUP)) {
        wsCheck(BY);
        ap = ip;
        GroupBy.Spec[] grp = null;
        do grp = groupSpec(clauses, grp); while(wsConsume(COMMA));

        // find all non-grouping variables that aren't shadowed
        final ArrayList<VarRef> ng = new ArrayList<VarRef>();
        for(final GroupBy.Spec spec : grp) curr.add(spec.var.name.id(), spec.var);
        vars: for(int i = 0; i < curr.size(); i++) {
          // weird quirk of TokenObjMap
          final Var v = curr.value(i + 1);
          for(final GroupBy.Spec spec : grp) if(spec.var.is(v)) continue vars;
          ng.add(new VarRef(grp[0].info, v));
        }

        // add new copies for all non-grouping variables
        final Var[] ngrp = new Var[ng.size()];
        for(int i = ng.size(); --i >= 0;) {
          final VarRef v = ng.get(i);

          // if one groups variables such as $x as xs:integer, then the resulting
          // sequence isn't compatible with the type and can't be assigned
          final Var nv = addLocal(v.var.name, null, false);
          // [LW] should be done everywhere
          if(v.type().one())
            nv.refineType(SeqType.get(v.type().type, Occ.ONE_MORE), ctx, info());
          ngrp[i] = nv;
          curr.add(nv.name.id(), nv);
        }

        final VarRef[] pre = new VarRef[ng.size()];
        clauses.add(new GroupBy(grp, ng.toArray(pre), ngrp, grp[0].info));
        alter = GRPBY;
      }

      final boolean stable = wsConsumeWs(STABLE);
      if(stable) wsCheck(ORDER);
      if(stable || wsConsumeWs(ORDER)) {
        wsCheck(BY);
        ap = ip;
        OrderBy.Key[] ob = null;
        do ob = orderSpec(ob); while(wsConsume(COMMA));

        final VarRef[] vs = new VarRef[curr.size()];
        for(int i = 0; i < vs.length; i++)
          vs[i] = new VarRef(ob[0].info, curr.value(i + 1));
        clauses.add(new OrderBy(vs, ob, stable, ob[0].info));
        alter = ORDERBY;
      }

      if(ctx.sc.xquery3() && wsConsumeWs(COUNT, DOLLAR, NOCOUNT)) {
        final Var v = addLocal(varName(), SeqType.ITR, false);
        curr.add(v.name.id(), v);
        clauses.add(new Count(v, info()));
      }
    } while(ctx.sc.xquery3() && size < clauses.size());

    if(!wsConsumeWs(RETURN)) throw alter != null ? error() : error(FLWORRETURN);

    final Expr ret = check(single(), NORETURN);
    scope.close(s);
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
    } else if(ctx.sc.xquery3()) {
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

      final boolean emp = ctx.sc.xquery3() && wsConsume(ALLOWING);
      if(emp) wsCheck(EMPTYORD);

      final Var ps = wsConsumeWs(AT) ? addLocal(varName(), SeqType.ITR, false) : null;
      final Var sc = wsConsumeWs(SCORE) ? addLocal(varName(), SeqType.DBL, false) : null;

      wsCheck(IN);
      final Expr e = check(single(), NOVARDECL);

      // declare late because otherwise it would shadow the wrong variables
      final Var var = addLocal(nm, tp, false);
      if(ps != null && nm.eq(ps.name)) error(DUPLVAR, var);
      if(sc != null) {
        if(nm.eq(sc.name)) error(DUPLVAR, var);
        if(ps != null && ps.name.eq(sc.name)) error(DUPLVAR, ps);
      }

      cls.add(new For(var, ps, sc, e, emp, info()));
    } while(wsConsume(COMMA));
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
      cls.add(new Let(addLocal(nm, tp, false), e, score, info()));
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
    return new Window(info(), slide, addLocal(nm, tp, false), e, start, only, end);
  }

  /**
   * Parses the "WindowVars" rule.
   * @param start start condition flag
   * @return an array containing the current, positional, previous and next variable name
   * @throws QueryException parse exception
   */
  private Condition windowCond(final boolean start) throws QueryException {
    skipWS();
    final InputInfo ii = info();
    final Var var = curr('$')             ? addLocal(varName(), null, false) : null,
              pos = wsConsumeWs(AT)       ? addLocal(varName(), null, false) : null,
              prv = wsConsumeWs(PREVIOUS) ? addLocal(varName(), null, false) : null,
              nxt = wsConsumeWs(NEXT)     ? addLocal(varName(), null, false) : null;
    wsCheck(WHEN);
    return new Condition(start, var, pos, prv, nxt, check(single(), NOEXPR), ii);
  }

  /**
   * Parses the "OrderSpec" rule.
   * Parses the "OrderModifier" rule.
   *
   * Empty order specs are ignored, {@code order} is then returned unchanged.
   * @param order order array
   * @return new order array
   * @throws QueryException query exception
   */
  private OrderBy.Key[] orderSpec(final OrderBy.Key[] order) throws QueryException {
    final Expr e = check(single(), ORDERBY);

    boolean desc = false;
    if(!wsConsumeWs(ASCENDING)) desc = wsConsumeWs(DESCENDING);
    boolean least = !ctx.sc.orderGreatest;
    if(wsConsumeWs(EMPTYORD)) {
      least = !wsConsumeWs(GREATEST);
      if(least) wsCheck(LEAST);
    }
    if(wsConsumeWs(COLLATION)) {
      final Uri uri = resolvedUri(stringLiteral());
      if(!eq(URLCOLL, uri.string()))
        error(uri.isValid() ? WHICHCOLL : INVURI, uri.string());
    }
    final OrderBy.Key ord = new OrderBy.Key(info(), e, desc, least);
    return order == null ? new OrderBy.Key[] { ord } : Array.add(order, ord);
  }

  /**
   * Parses the "GroupingSpec" rule.
   * @param cl preceding clauses
   * @param group grouping specification
   * @return new group array
   * @throws QueryException query exception
   */
  private GroupBy.Spec[] groupSpec(final LinkedList<Clause> cl,
      final GroupBy.Spec[] group) throws QueryException {

    final InputInfo ii = info();
    final QNm name = varName();
    final SeqType type = optAsType();

    final Expr by;
    if(type != null || wsConsume(ASSIGN)) {
      if(type != null) wsCheck(ASSIGN);
      by = check(single(), NOVARDECL);
    } else {
      final VarRef vr = scope.resolve(name, ctx, ii);
      // the grouping variable has to be declared by the same FLWOR expression
      boolean dec = false;
      if(vr != null) {
        for(final Clause f : cl) {
          if(f.declares(vr.var)) {
            dec = true;
            break;
          }
        }
      }
      if(!dec) error(GVARNOTDEFINED, '$' + string(name.string()));
      by = vr;
    }

    if(wsConsumeWs(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!eq(URLCOLL, coll)) error(WHICHCOLL, coll);
    }

    final GroupBy.Spec grp = new GroupBy.Spec(ii, addLocal(name, type, false), by);
    if(group == null) return new GroupBy.Spec[] { grp };
    for(int i = group.length; --i >= 0;) {
      if(group[i].var.name.eq(name)) {
        group[i].occluded = true;
        break;
      }
    }
    return Array.add(group, grp);
  }

  /**
   * Parses the "QuantifiedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = wsConsumeWs(SOME, DOLLAR, NOSOME);
    if(!some && !wsConsumeWs(EVERY, DOLLAR, NOSOME)) return null;

    final int s = scope.open();
    For[] fl = { };
    do {
      final QNm nm = varName();
      final SeqType tp = optAsType();
      wsCheck(IN);
      final Expr e = check(single(), NOSOME);
      fl = Array.add(fl, new For(addLocal(nm, tp, false), null, null, e, false, info()));
    } while(wsConsume(COMMA));

    wsCheck(SATISFIES);
    final Expr e = check(single(), NOSOME);
    scope.close(s);
    return new Quantifier(info(), fl, e, !some);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!ctx.sc.xquery3() || !wsConsumeWs(SWITCH, PAR1, TYPEPAR)) return null;
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
        if(exprs.length == 0) error(WRONGCHAR, CASE, found());
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
    final int s = scope.open();
    boolean cs;
    do {
      types.clear();
      cs = wsConsumeWs(CASE);
      if(!cs) wsCheck(DEFAULT);
      skipWS();
      Var var = null;
      if(curr('$')) {
        var = addLocal(varName(), null, false);
        if(cs) wsCheck(AS);
      }
      if(cs) {
        do types.add(sequenceType());
        while(ctx.sc.xquery3() && wsConsume(PIPE));
      }
      wsCheck(RETURN);
      final Expr ret = check(single(), NOTYPESWITCH);
      cases = Array.add(cases, new TypeCase(info(), var,
          types.toArray(new SeqType[types.size()]), ret));
      scope.close(s);
    } while(cs);
    if(cases.length == 1) error(NOTYPESWITCH);
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
    if(!wsConsumeWs(THEN)) error(NOIF);
    final Expr thn = check(single(), NOIF);
    if(!wsConsumeWs(ELSE)) error(NOIF);
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
    final Expr e = comparison();
    if(!wsConsumeWs(AND)) return e;

    final ExprList el = new ExprList(e);
    do add(el, comparison()); while(wsConsumeWs(AND));
    return new And(info(), el.finish());
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
        return new CmpV(e, check(ftContains(), CMPEXPR), c, info());
      for(final OpN c : OpN.VALUES) if(wsConsumeWs(c.name))
        return new CmpN(e, check(ftContains(), CMPEXPR), c, info());
      for(final OpG c : OpG.VALUES) if(wsConsumeWs(c.name))
        return new CmpG(e, check(ftContains(), CMPEXPR), c, info());
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

    final int i = ip;
    // extensions to the official extension: "=>" and "<-"
    if(consume('=') && consume('>') || consume('<') && consume('-')) {
      skipWS();
    } else if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      ip = i;
      return e;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      error(FTIGNORE);
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
    return Function.CONCAT.get(info(), el.finish());
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
    final int i = ip;
    if(consume(PIPE) && !consume(PIPE)) return true;
    ip = i;
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
    return new Castable(info(), e, simpleType());
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
    return new Cast(info(), e, simpleType());
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
      skipWS();
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
    final int i = ip;
    if(!wsConsumeWs(VALIDATE)) return;

    boolean brace = true;
    if(consume(BRACE1)) {
      brace = false;
    } else if(consume(TYPE)) {
      final QNm qnm = eQName(QNAMEINV, SKIPCHECK);
      names.add(new QNmCheck(qnm));
    } else if(!consume(STRICT) && !consume(LAX)) {
      ip = i;
      return;
    }

    if(brace) wsCheck(BRACE1);
    check(single(), NOVALIDATE);
    wsCheck(BRACE2);
    error(IMPLVAL);
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
      if(c != '#' && !ws(c)) error(PRAGMAINV);
      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) error(PRAGMAINV);
        tok.add(consume());
        c = curr();
      }

      final byte[] v = tok.trim().finish();
      if(eq(name.prefix(), DB)) {
        // project-specific declaration
        final String key = string(uc(name.local()));
        final Object obj = ctx.context.prop.get(key);
        if(obj == null) error(BASX_OPTIONS, key);
        el.add(new DBPragma(name, v));
      }
      ip += 2;
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
          error(PATHMISS, found());
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
      boolean b = false;
      if(consume('/')) {
        if(consume('/')) {
          add(el, descOrSelf());
          checkAxis(Axis.DESC);
        } else {
          checkAxis(Axis.CHILD);
        }
      } else if(next() != '=' && consume('!')) {
        b = true;
      } else {
        return;
      }
      mark();
      Expr st = step();
      if(st == null) error(PATHMISS, found());
      if(b) st = new Bang(info(), st);
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
  protected void checkInit() { }

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
        --ip;
        error(NOATTNAME);
      }
    } else {
      for(final Axis a : Axis.VALUES) {
        final int i = ip;
        if(!wsConsumeWs(a.name)) continue;
        alter = NOLOCSTEP;
        if(wsConsumeWs(COLS)) {
          ap = ip;
          ax = a;
          test = nodeTest(a == Axis.ATTR, true);
          checkTest(test, a == Axis.ATTR);
          break;
        }
        ip = i;
      }
    }

    if(ax == null) {
      ax = Axis.CHILD;
      test = nodeTest(false, true);
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
    final int i = ip;
    if(consume('*')) {
      // name test: *
      if(!consume(':')) return new NameTest(att);
      // name test: *:name
      return new NameTest(new QNm(ncName(QNAMEINV)), NameTest.Mode.NAME, att);
    }

    if(ctx.sc.xquery3() && consume(EQNAME)) {
      // name test: Q{...}*
      final byte[] uri = bracedURILiteral();
      if(consume('*')) {
        final QNm nm = new QNm(COLON, uri);
        return new NameTest(nm, NameTest.Mode.NS, att);
      }
    }
    ip = i;

    final QNm name = eQName(null, SKIPCHECK);
    if(name != null) {
      final int i2 = ip;
      if(all && wsConsumeWs(PAR1)) {
        final NodeType type = NodeType.find(name);
        if(type != null) return kindTest(type);
      } else {
        ip = i2;
        // name test: prefix:name, name
        if(name.hasPrefix() || !consume(':')) {
          skipWS();
          names.add(new QNmCheck(name, !att));
          return new NameTest(name, NameTest.Mode.STD, att);
        }
        // name test: prefix:*
        if(consume('*')) {
          final QNm nm = new QNm(concat(name.string(), COLON));
          names.add(new QNmCheck(nm, !att));
          return new NameTest(nm, NameTest.Mode.NS, att);
        }
      }
    }
    ip = i;
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
        if(e == null) error(PREDMISSING);
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

        e = holes == null ? new DynFuncCall(ii, e, args) :
          new PartFunc(ii, e, args, holes);
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
    skipWS();
    final char c = curr();
    // variables
    if(c == '$') return checkVar(info(), varName());
    // parentheses
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
    // function item
    if(ctx.sc.xquery3()) {
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
    // map literal
    if(wsConsumeWs(MAPSTR, BRACE1, INCOMPLETE)) return mapLiteral();
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
   * Parses a literal map.
   * @return map literal
   * @throws QueryException query exception
   */
  private Expr mapLiteral() throws QueryException {
    wsCheck(BRACE1);
    final ExprList el = new ExprList();

    if(!wsConsume(BRACE2)) {
      do {
        add(el, check(single(), INVMAPKEY));
        wsCheck(ASSIGN);
        add(el, check(single(), INVMAPVAL));
      } while(wsConsume(COMMA));
      wsCheck(BRACE2);
    }
    return new LitMap(info(), el.finish());
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
    skipWS();
    final int pos = ip;

    // parse annotations; will only be visited for XQuery 3.0 expressions
    final Ann ann = curr('%') ? annotations() : null;
    // inline function
    if(wsConsume(FUNCTION) && wsConsume(PAR1)) {
      final VarScope inner = scope = scope.child();
      final Var[] args = paramList();
      wsCheck(PAR2);
      final SeqType type = optAsType();
      if(ann != null && (ann.contains(Ann.Q_PRIVATE) || ann.contains(Ann.Q_PUBLIC)))
        error(INVISIBLE);
      final Expr body = enclosed(NOFUNBODY);
      scope = scope.parent();
      return new InlineFunc(info(), type, args, body, ann, ctx.sc, inner);
    }
    // annotations not allowed here
    if(ann != null) error(NOANN);

    // named function reference
    ip = pos;
    final QNm name = eQName(null, ctx.sc.nsFunc);
    if(name != null && consume('#')) {
      if(keyword(name)) error(RESERVED, name.local());
      final Expr ex = numericLiteral(true);
      if(!(ex instanceof Int)) return ex;
      final long card = ex instanceof Int ? ((Int) ex).itr() : -1;
      if(card < 0 || card > Integer.MAX_VALUE) error(FUNCUNKNOWN, name);
      final Expr lit = Functions.getLiteral(name, (int) card, ctx, info());
      return lit != null ? lit : FuncLit.unknown(name, card, ctx, info());
    }

    ip = pos;
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
    if(!quote(c)) return null;

    final int i = ip;
    final byte[] s = stringLiteral();
    final int p2 = ip;
    if(consume(':')) {
      // check for EQName
      if(!consume('=')) {
        ip = i;
        return null;
      }
      ip = p2;
    }
    return Str.get(s);
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
      if(itr) error(NUMBERITR);
      tok.add('.');
      while(digit(curr()))
        tok.add(consume());
    }
    if(XMLToken.isNCStartChar(curr())) return checkDbl();

    if(dec) return new Dec(tok.finish());

    final long l = toLong(tok.finish());
    if(l == Long.MIN_VALUE) {
      return FNInfo.error(new QueryException(null, RANGE, tok), info());
    }
    return Int.get(l);
  }

  /**
   * Parses the "DoubleLiteral" rule. Checks if a number is followed by a
   * whitespace.
   * @return expression
   * @throws QueryException query exception
   */
  private Dbl checkDbl() throws QueryException {
    if(!consume('e') && !consume('E')) error(NUMBERWS);
    tok.add('e');
    if(curr('+') || curr('-')) tok.add(consume());
    final int s = tok.size();
    while(digit(curr()))
      tok.add(consume());
    if(s == tok.size()) error(NUMBERINC, tok);

    if(XMLToken.isNCStartChar(curr())) error(NUMBERWS);
    return Dbl.get(tok.finish(), info());
  }

  /**
   * Parses the "StringLiteral" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] stringLiteral() throws QueryException {
    skipWS();
    final char del = curr();
    if(!quote(del)) error(NOQUOTE, found());
    consume();
    tok.reset();
    while(true) {
      while(!consume(del)) {
        if(!more()) error(NOQUOTE, found());
        entity(tok);
      }
      if(!consume(del)) break;
      tok.add(del);
    }
    return tok.finish();
  }

  /**
   * Potentially resolves a URI literal.
   * @param string uri string
   * @return resolved URI
   * @throws QueryException query exception
   */
  private Uri resolvedUri(final byte[] string) throws QueryException {
    final Uri uri = Uri.uri(string);
    if(!uri.isValid()) error(INVURI, string);
    return uri.isAbsolute() ? uri : ctx.sc.baseURI().resolve(uri, info());
  }

  /**
   * Parses the "BracedURILiteral" rule without the "Q{" prefix.
   * @return query expression
   * @throws QueryException query exception
   */
  private byte[] bracedURILiteral() throws QueryException {
    tok.reset();
    while(!consume('}')) {
      if(!more()) error(WRONGCHAR, BRACE2, found());
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
    wsCheck(DOLLAR);
    skipWS();
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
    final int i = ip;
    final QNm name = eQName(null, ctx.sc.nsFunc);
    if(name != null && !keyword(name)) {
      if(wsConsume(PAR1)) {
        final InputInfo ii = info();
        final ExprList argList = new ExprList();
        final int[] holes = argumentList(argList, name.string());
        final Expr[] args = argList.finish();
        alter = FUNCUNKNOWN;
        alterFunc = name;
        ap = ip;

        final Expr ret;
        if(holes != null) {
          final int card = args.length + holes.length;
          final Expr lit = Functions.getLiteral(name, card, ctx, ii),
              f = lit != null ? lit : FuncLit.unknown(name, card, ctx, ii);
          ret = new PartFunc(ii, f, args, holes);
        } else {
          final TypedFunc f = Functions.get(name, args, false, ctx, ii);
          ret = f == null ? null : f.fun;
        }

        if(ret != null) {
          alter = null;
          return ret;
        }
      }
    }

    ip = i;
    return null;
  }

  /**
   * Parses the "ArgumentList" rule after the opening parenthesis was read.
   * @param args list to put the argument expressions into
   * @param name name of the function (item)
   * @return array of arguments, place-holders '?' are represented as {@code null} entries
   * @throws QueryException query exception
   */
  private int[] argumentList(final ExprList args, final Object name)
      throws QueryException {

    int[] holes = null;
    if(!wsConsume(PAR2)) {
      int i = 0;
      do {
        if(wsConsume(PLHOLDER)) {
          holes = holes == null ? new int[] { i } : Array.add(holes, i);
        } else {
          final Expr e = single();
          if(e == null) error(FUNCMISS, name);
          args.add(e);
        }
        i++;
      } while(wsConsume(COMMA));
      if(!wsConsume(PAR2)) error(FUNCMISS, name);
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
    final int s = ctx.sc.ns.size();
    final byte[] nse = ctx.sc.nsElem;
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
      if(!quote(delim)) error(NOQUOTE, found());
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
              if(text.length != 0) {
                add(attv, Str.get(text));
              } else {
                add(attv, enclosed(NOENCLEXPR));
                simple = false;
              }
              tb.reset();
            }
          } else if(ch == '}') {
            consume();
            check('}');
            tb.add('}');
          } else if(ch == '<' || ch == 0) {
            error(NOQUOTE, found());
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
        if(!simple) error(NSCONS);
        final byte[] pref = pr ? local(atn) : EMPTY;
        final byte[] uri = attv.isEmpty() ? EMPTY : ((Str) attv.get(0)).string();
        if(eq(pref, XML) && eq(uri, XMLURI)) {
          if(xmlDecl) error(DUPLNSDEF, XML);
          xmlDecl = true;
        } else {
          if(!Uri.uri(uri).isValid()) error(INVURI, uri);
          if(pr) {
            if(uri.length == 0) error(NSEMPTYURI);
            if(eq(pref, XML, XMLNS)) error(BINDXML, pref);
            if(eq(uri, XMLURI)) error(BINDXMLURI, uri, XML);
            if(eq(uri, XMLNSURI)) error(BINDXMLURI, uri, XMLNS);
            ctx.sc.ns.add(pref, uri);
          } else {
            if(eq(uri, XMLURI)) error(XMLNSDEF, uri);
            ctx.sc.nsElem = uri;
          }
          if(ns.contains(pref)) error(DUPLNSDEF, pref);
          ns.add(pref, uri);
        }
      } else {
        final QNm attn = new QNm(atn);
        if(atts == null) atts = new ArrayList<QNm>(1);
        atts.add(attn);
        names.add(new QNmCheck(attn, false));
        add(cont, new CAttr(info(), false, attn, attv.finish()));
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
      ip += 2;

      final byte[] close = qName(TAGNAME);
      consumeWS();
      check('>');
      if(!eq(tag.string(), close)) error(TAGWRONG, tag.string(), close);
    }

    assignURI(npos);

    // check for duplicate attribute names
    if(atts != null) {
      final int as = atts.size();
      for(int a = 0; a < as - 1; a++) {
        for(int b = a + 1; b < as; b++) {
          if(atts.get(a).eq(atts.get(b))) ATTDUPL.thrw(info(), atts.get(a));
        }
      }
    }

    ctx.sc.ns.size(s);
    ctx.sc.nsElem = nse;
    return new CElem(info(), tag, ns, cont.finish());
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
        error(NOCLOSING, tag);
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
    return t.length == 0 || strip && !ctx.sc.spaces && ws(t) ?
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
        return new CComm(info(), Str.get(tb.finish()));
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
    if(eq(lc(str), XML)) error(PIXML, str);

    final boolean space = skipWS();
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('?')) {
        if(!space) error(PIWRONG);
        tb.add(consume());
      }
      consume();
      if(consume('>')) {
        return new CPI(info(), Str.get(str), Str.get(tb.finish()));
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
        ip += 2;
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
    final int i = ip;
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
    if(expr == null) ip = p;
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
    return new CDoc(info(), e);
  }

  /**
   * Parses the "CompElemConstructor" rule.
   * Parses the "ContextExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compElement() throws QueryException {
    skipWS();

    Expr name;
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
    return new CElem(info(), name, null, e == null ? new Expr[0] : new Expr[] { e });
  }

  /**
   * Parses the "CompAttrConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    skipWS();

    Expr name;
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
    return new CAttr(info(), true, name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "CompNamespaceConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compNamespace() throws QueryException {
    if(!ctx.sc.xquery3()) return null;
    skipWS();

    Expr name;
    final byte[] str = ncName(null);
    if(str.length != 0) {
      name = Str.get(str);
    } else {
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), NSWRONG);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CNSpace(info(), name, e == null ? Empty.SEQ : e);
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
    return new CTxt(info(), e);
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
    return new CComm(info(), e);
  }

  /**
   * Parses the "CompPIConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compPI() throws QueryException {
    skipWS();

    Expr name;
    final byte[] str = ncName(null);
    if(str.length != 0) {
      name = Str.get(str);
    } else {
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), PIWRONG);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CPI(info(), name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "SimpleType" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType simpleType() throws QueryException {
    skipWS();
    final QNm name = eQName(TYPEINVALID, ctx.sc.nsElem);
    Type t = ListType.find(name);
    if(t == null) {
      t = AtomType.find(name, false);
      if(t == null) {
        if(wsConsume(PAR1)) error(SIMPLETYPE, name);
        if(ctx.sc.xquery3) {
          if(!AtomType.AST.name.eq(name)) error(TYPEUNKNOWN30, name);
          t = AtomType.AST;
        } else {
          error(TYPEUNKNOWN, name);
        }
      }
      if(t == AtomType.AST || t == AtomType.AAT || t == AtomType.NOT)
        error(CASTUNKNOWN, name);
    }
    skipWS();
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
      return SeqType.get(AtomType.ITEM, Occ.ZERO, null);
    }

    // parse item type and occurrence indicator
    final SeqType tw = itemType();
    skipWS();
    final Occ occ = consume('?') ? Occ.ZERO_ONE : consume('+') ? Occ.ONE_MORE :
      consume('*') ? Occ.ZERO_MORE : Occ.ONE;
    skipWS();
    return tw.withOcc(occ);
  }

  /**
   * Parses the "ItemType" rule.
   * Parses the "ParenthesizedItemType" rule.
   * @return item type
   * @throws QueryException query exception
   */
  private SeqType itemType() throws QueryException {
    skipWS();

    // parenthesized item type
    if(consume(PAR1)) {
      final SeqType ret = itemType();
      wsCheck(PAR2);
      return ret;
    }

    // parse optional annotation and type name
    final Ann ann = ctx.sc.xquery3() && curr('%') ? annotations() : null;
    final QNm name = eQName(TYPEINVALID, null);
    skipWS();
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
      if(t == null) error(NOTYPE, name.string());
    } else {
      // attach default element namespace
      if(!name.hasURI()) name.uri(ctx.sc.nsElem);
      // atomic types
      t = AtomType.find(name, false);
      // no type found
      if(t == null) error(TYPEUNKNOWN, name);
    }

    // annotations are not allowed for remaining types
    if(ann != null) error(NOANN);

    // atomic value, or closing parenthesis
    if(!func || wsConsume(PAR2)) return t.seqType();

    // raise error if type different to node is not finalized by a parenthesis
    if(!(t instanceof NodeType)) wsCheck(PAR2);

    // return type with an optional kind test for node types
    return SeqType.get(t, SeqType.Occ.ONE, kindTest((NodeType) t));
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
      if(!key.instanceOf(AtomType.AAT)) error(MAPTAAT, key);
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
    if(tp == Test.NSP && !ctx.sc.xquery3) error(NSNOTALL);
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
    final QNm name = eQName(null, ctx.sc.nsElem);
    if(name == null && !consume(ASTERISK)) return null;

    Type type = null;
    if(wsConsumeWs(COMMA)) {
      // parse type name
      final QNm tn = eQName(QNAMEINV, ctx.sc.nsElem);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) error(TYPEUNDEF, tn);
      // parse optional question mark
      wsConsume(PLHOLDER);
    }
    return new ExtTest(NodeType.ELM, name, type, ctx.sc.strip);
  }

  /**
   * Parses the "ElementTest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test schemaTest() throws QueryException {
    final QNm name = eQName(QNAMEINV, ctx.sc.nsElem);
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
      final QNm tn = eQName(QNAMEINV, ctx.sc.nsElem);
      type = ListType.find(tn);
      if(type == null) type = AtomType.find(tn, true);
      if(type == null) error(TYPEUNDEF, tn);
    }
    return new ExtTest(NodeType.ATT, name, type, ctx.sc.strip);
  }

  /**
   * Parses the "PITest" rule without the leading keyword and its brackets.
   * @return arguments
   * @throws QueryException query exception
   */
  private Test piTest() throws QueryException {
    final byte[] nm;
    tok.reset();
    if(quote(curr())) {
      nm = trim(stringLiteral());
      if(!XMLToken.isNCName(nm)) error(INVNCNAME, nm);
    } else if(ncName()) {
      nm = tok.finish();
    } else {
      return null;
    }
    return new ExtTest(NodeType.PI, new QNm(nm));
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!ctx.sc.xquery3() || !wsConsumeWs(TRY)) return null;

    final Expr tr = enclosed(NOENCLEXPR);
    wsCheck(CATCH);

    Catch[] ct = { };
    do {
      QNm[] codes = { };
      do {
        skipWS();
        final Test test = nodeTest(false, false);
        if(test == null) error(NOCATCH);
        codes = Array.add(codes, test.name);
      } while(wsConsumeWs(PIPE));

      final int s = scope.open();
      final Catch c = new Catch(info(), codes, ctx, scope);
      c.expr = enclosed(NOENCLEXPR);
      scope.close(s);

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
        if(rng == null) error(FTRANGE);
        expr = new FTDistance(info(), expr, rng, ftUnit());
      } else if(wsConsumeWs(AT)) {
        final boolean start = wsConsumeWs(START);
        final boolean end = !start && wsConsumeWs(END);
        if(!start && !end) error(INCOMPLETE);
        expr = new FTContent(info(), expr, start, end);
      } else if(wsConsumeWs(ENTIRE)) {
        wsCheck(CONTENT);
        expr = new FTContent(info(), expr, false, false);
      } else {
        final boolean same = wsConsumeWs(SAME);
        final boolean diff = !same && wsConsumeWs(DIFFERENT);
        if(same || diff) {
          FTUnit unit = null;
          if(wsConsumeWs(SENTENCE)) unit = FTUnit.SENTENCE;
          else if(wsConsumeWs(PARAGRAPH)) unit = FTUnit.PARAGRAPH;
          else error(INCOMPLETE);
          expr = new FTScope(info(), expr, unit, same);
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
    if(!Tokenizer.supportFor(fto.ln)) error(FTNOTOK, fto.ln);
    if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln))
      error(FTNOSTEM, fto.ln);

    // consume weight option
    if(wsConsumeWs(WEIGHT)) expr = new FTWeight(info(), expr, enclosed(NOENCLEXPR));

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

    skipWS();
    final Expr e = curr('{') ? enclosed(NOENCLEXPR)
        : quote(curr()) ? Str.get(stringLiteral()) : null;
    if(e == null) error(prg ? NOPRAGMA : NOENCLEXPR);

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
      if(occ == null) error(FTRANGE);
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
    final Expr[] occ = { Int.get(1), Int.get(Long.MAX_VALUE)};
    if(wsConsumeWs(EXACTLY)) {
      occ[0] = ftAdditive(i);
      occ[1] = occ[0];
    } else if(wsConsumeWs(AT)) {
      if(wsConsumeWs(LEAST)) {
        occ[0] = ftAdditive(i);
      } else {
        wsCheck(MOST);
        occ[0] = Int.get(0);
        occ[1] = ftAdditive(i);
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
    skipWS();
    tok.reset();
    while(digit(curr()))
      tok.add(consume());
    if(tok.isEmpty()) error(INTEXP);
    return Int.get(toLong(tok.finish()));
  }

  /**
   * Parses the "FTUnit" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private FTUnit ftUnit() throws QueryException {
    if(wsConsumeWs(WORDS)) return FTUnit.WORD;
    if(wsConsumeWs(SENTENCES)) return FTUnit.SENTENCE;
    if(wsConsumeWs(PARAGRAPHS)) return FTUnit.PARAGRAPH;
    error(INCOMPLETE);
    return null;
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
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS)) error(FTDUP, CASE);
      opt.set(CS, true);
      opt.set(LC, true);
    } else if(wsConsumeWs(UPPERCASE)) {
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS)) error(FTDUP, CASE);
      opt.set(CS, true);
      opt.set(UC, true);
    } else if(wsConsumeWs(CASE)) {
      if(opt.isSet(LC) || opt.isSet(UC) || opt.isSet(CS)) error(FTDUP, CASE);
      opt.set(CS, wsConsumeWs(SENSITIVE));
      if(!opt.is(CS)) wsCheck(INSENSITIVE);
    } else if(wsConsumeWs(DIACRITICS)) {
      if(opt.isSet(DC)) error(FTDUP, DIACRITICS);
      opt.set(DC, wsConsumeWs(SENSITIVE));
      if(!opt.is(DC)) wsCheck(INSENSITIVE);
    } else if(wsConsumeWs(LANGUAGE)) {
      if(opt.ln != null) error(FTDUP, LANGUAGE);
      final byte[] lan = stringLiteral();
      opt.ln = Language.get(string(lan));
      if(opt.ln == null) error(FTNOTOK, lan);
    } else if(wsConsumeWs(OPTION)) {
      optionDecl();
    } else {
      final boolean using = !wsConsumeWs(NO);

      if(wsConsumeWs(STEMMING)) {
        if(opt.isSet(ST)) error(FTDUP, STEMMING);
        opt.set(ST, using);
      } else if(wsConsumeWs(THESAURUS)) {
        if(opt.th != null) error(FTDUP, THESAURUS);
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

        if(opt.sw != null) error(FTDUP, STOP + ' ' + WORDS);
        opt.sw = new StopWords();
        if(wsConsumeWs(DEFAULT)) {
          if(!using) error(FTSTOP);
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
              final IO fl = ctx.stop != null ? ctx.stop.get(fn) : ctx.sc.io(fn);
              if(!opt.sw.read(fl, except)) error(NOSTOPFILE, fl);
            } else if(!union && !except) {
              error(FTSTOP);
            }
            union = wsConsumeWs(UNION);
            except = !union && wsConsumeWs(EXCEPT);
            if(!union && !except) break;
          }
        }
      } else if(wsConsumeWs(WILDCARDS)) {
        if(opt.isSet(WC)) error(FTDUP, WILDCARDS);
        if(opt.is(FZ)) error(BXFT_MATCH);
        opt.set(WC, using);
      } else if(wsConsumeWs(FUZZY)) {
        // extension to the official extension: "using fuzzy"
        if(opt.isSet(FZ)) error(FTDUP, FUZZY);
        if(opt.is(WC)) error(BXFT_MATCH);
        opt.set(FZ, using);
      } else {
        error(FTMATCH, consume());
        return false;
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
    final IO fl = ctx.thes != null ? ctx.thes.get(fn) : ctx.sc.io(fn);
    final byte[] rel = wsConsumeWs(RELATIONSHIP) ? stringLiteral() : EMPTY;
    final Expr[] range = ftRange(true);
    long min = 0;
    long max = Long.MAX_VALUE;
    if(range != null) {
      wsCheck(LEVELS);
      // values will always be integer instances
      min = ((Int) range[0]).itr();
      max = ((Int) range[1]).itr();
    }
    thes.add(new Thesaurus(fl, rel, min, max, ctx.context));
  }

  /**
   * Parses the "InsertExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr insert() throws QueryException {
    final int i = ip;
    if(!wsConsumeWs(INSERT) || !wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      ip = i;
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
      if(!after && !before) error(INCOMPLETE);
    }
    final Expr trg = check(single(), INCOMPLETE);
    ctx.updating(true);
    return new Insert(info(), s, first, last, before, after, trg);
  }

  /**
   * Parses the "DeleteExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr deletee() throws QueryException {
    final int i = ip;
    if(!wsConsumeWs(DELETE) || !wsConsumeWs(NODES) && !wsConsumeWs(NODE)) {
      ip = i;
      return null;
    }
    ctx.updating(true);
    return new Delete(info(), check(single(), INCOMPLETE));
  }

  /**
   * Parses the "RenameExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr rename() throws QueryException {
    final int i = ip;
    if(!wsConsumeWs(RENAME) || !wsConsumeWs(NODE)) {
      ip = i;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(AS);
    final Expr n = check(single(), INCOMPLETE);
    ctx.updating(true);
    return new Rename(info(), trg, n);
  }

  /**
   * Parses the "ReplaceExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr replace() throws QueryException {
    final int i = ip;
    if(!wsConsumeWs(REPLACE)) return null;

    final boolean v = wsConsumeWs(VALUEE);
    if(v) {
      wsCheck(OF);
      wsCheck(NODE);
    } else if(!wsConsumeWs(NODE)) {
      ip = i;
      return null;
    }

    final Expr t = check(single(), INCOMPLETE);
    wsCheck(WITH);
    final Expr r = check(single(), INCOMPLETE);
    ctx.updating(true);
    return new Replace(info(), t, r, v);
  }

  /**
   * Parses the "TransformExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr transform() throws QueryException {
    if(!wsConsumeWs(COPY, DOLLAR, INCOMPLETE)) return null;
    final int s = scope.open();
    final boolean u = ctx.updating;
    ctx.updating(false);

    Let[] fl = { };
    do {
      final QNm name = varName();
      wsCheck(ASSIGN);
      final Expr e = check(single(), INCOMPLETE);
      fl = Array.add(fl, new Let(addLocal(name, SeqType.NOD, false), e, false, info()));
    } while(wsConsumeWs(COMMA));
    wsCheck(MODIFY);

    final Expr m = check(single(), INCOMPLETE);
    wsCheck(RETURN);
    final Expr r = check(single(), INCOMPLETE);

    scope.close(s);
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
    if(err != null) error(err, consume());
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
    final int i = ip;
    if(ctx.sc.xquery3() && consume(EQNAME)) {
      final byte[] uri = bracedURILiteral();
      final byte[] name = ncName(null);
      if(name.length != 0) {
        if(def == URICHECK && uri.length == 0) {
          ip = i;
          error(NOURI, name);
        }
        return new QNm(name, uri);
      }
      ip = i;
    }
    final byte[] nm = qName(err);
    if(nm.length == 0) return null;

    if(def == SKIPCHECK) return new QNm(nm);

    // create new EQName and set namespace
    final QNm name = new QNm(nm, ctx);
    if(!name.hasURI()) {
      if(def == URICHECK) {
        ip = i;
        error(NSMISS, name);
      }
      if(name.hasPrefix()) {
        ip = i;
        error(NOURI, name);
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
      if(err != null) error(err, consume());
    } else if(consume(':')) {
      if(!XMLToken.isNCStartChar(curr())) {
        --ip;
      } else {
        tok.add(':');
        do {
          tok.add(consume());
        } while(XMLToken.isNCChar(curr()));
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
    final int i = ip;
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
    final String sub = input.substring(p, Math.min(p + 20, il));
    final int sc = sub.indexOf(';');
    final String ent = sc != -1 ? sub.substring(0, sc + 1) : sub + "...";
    error(c, ent);
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
    if(expr == null) error(err);
    return expr;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param ch character to be found
   * @throws QueryException query exception
   */
  private void check(final int ch) throws QueryException {
    if(!consume(ch)) error(WRONGCHAR, (char) ch, found());
  }

  /**
   * Skips whitespaces, raises an error if the specified string cannot be
   * consumed.
   * @param s string to be found
   * @throws QueryException query exception
   */
  private void wsCheck(final String s) throws QueryException {
    if(!wsConsume(s)) error(WRONGCHAR, s, found());
  }

  /**
   * Checks if a referenced variable is defined and throws the specified error if not.
   * @param ii input info
   * @param name variable name
   * @return referenced variable
   * @throws QueryException if the variable isn't defined
   */
  private Expr checkVar(final InputInfo ii, final QNm name) throws QueryException {
    // local variable
    final VarRef local = scope.resolve(name, ctx, ii);
    if(local != null) return local;

    // static variable
    final byte[] uri = name.uri();

    // in XQuery 1.0 only forward declarations are allowed (except for implicit variables)
    final boolean main = module == null, implicit = main && uri.length == 0;
    if(!(ctx.sc.xquery3() || ctx.vars.declared(name) || implicit)) {
      throw error(VARUNDEF, '$' + string(name.string()));
    }

    // variable has to be declared by the same or a directly imported module
    if(!(main || eq(module.uri(), uri) || modules.contains(uri)))
      throw error(VARUNDEF, '$' + string(name.string()));

    return ctx.vars.newRef(name, ctx.sc, ii);
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
    if(c == 0) error(WRONGCHAR, ch, found());
    return c != ch;
  }

  /**
   * Consumes the specified token and surrounding whitespaces.
   * @param t token to consume
   * @return true if token was found
   * @throws QueryException query exception
   */
  private boolean wsConsumeWs(final String t) throws QueryException {
    final int i = ip;
    if(!wsConsume(t)) return false;
    if(skipWS() || !XMLToken.isNCStartChar(t.charAt(0))
        || !XMLToken.isNCChar(curr())) return true;
    ip = i;
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

    final int i = ip;
    if(!wsConsumeWs(s1)) return false;
    alter = expr;
    ap = ip;
    final int i2 = ip;
    final boolean ok = wsConsume(s2);
    ip = ok ? i2 : i;
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
    skipWS();
    return consume(str);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespaces were found
   * @throws QueryException query exception
   */
  private boolean skipWS() throws QueryException {
    final int i = ip;
    while(more()) {
      final int c = curr();
      if(c == '(' && next() == ':') {
        comment();
      } else {
        if(c <= 0 || c > ' ') return i != ip;
        ++ip;
      }
    }
    return i != ip;
  }

  /**
   * Consumes a comment.
   * @throws QueryException query exception
   */
  private void comment() throws QueryException {
    ++ip;
    final boolean doc = next() == '~';
    if(doc) {
      xqdoc.setLength(0);
      ++ip;
    }
    while(++ip < il) {
      char curr = curr();
      if(curr == '(' && next() == ':') {
        comment();
        curr = curr();
      }
      if(curr == ':' && next() == ')') {
        ip += 2;
        return;
      }
      if(doc) xqdoc.append(curr);
    }
    error(COMCLOSE);
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespaces were found
   */
  private boolean consumeWS() {
    final int i = ip;
    while(more()) {
      final int c = curr();
      if(c <= 0 || c > ' ') return i != ip;
      ++ip;
    }
    return true;
  }

  /**
   * Throws the alternative error message.
   * @return never
   * @throws QueryException query exception
   */
  private QueryException error() throws QueryException {
    ip = ap;
    if(alter != FUNCUNKNOWN) error(alter);
    ctx.funcs.errorIfSimilar(alterFunc, info());
    throw error(alter, alterFunc.string());
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param e new expression
   * @throws QueryException query exception
   */
  private void add(final ExprList ar, final Expr e) throws QueryException {
    if(e == null) error(INCOMPLETE);
    ar.add(e);
  }

  /**
   * Throws the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return never
   * @throws QueryException query exception
   */
  public QueryException error(final Err err, final Object... arg) throws QueryException {
    throw err.thrw(info(), arg);
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
        name.uri(ctx.sc.ns.uri(name.prefix()));
        if(check && !name.hasURI()) error(NOURI, name);
      } else if(nsElem) {
        name.uri(ctx.sc.nsElem);
      }
      return name.hasURI();
    }
  }
}
