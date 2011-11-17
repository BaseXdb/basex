package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.serial.SerializerProp;
import org.basex.query.expr.And;
import org.basex.query.expr.Arith;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CComm;
import org.basex.query.expr.CDoc;
import org.basex.query.expr.CElem;
import org.basex.query.expr.CPI;
import org.basex.query.expr.CTxt;
import org.basex.query.expr.Calc;
import org.basex.query.expr.Cast;
import org.basex.query.expr.Castable;
import org.basex.query.expr.Catch;
import org.basex.query.expr.CmpG;
import org.basex.query.expr.CmpN;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Concat;
import org.basex.query.expr.Context;
import org.basex.query.expr.DynFuncCall;
import org.basex.query.expr.Except;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Extension;
import org.basex.query.expr.Filter;
import org.basex.query.expr.For;
import org.basex.query.expr.ForLet;
import org.basex.query.expr.GFLWOR;
import org.basex.query.expr.If;
import org.basex.query.expr.InlineFunc;
import org.basex.query.expr.Instance;
import org.basex.query.expr.InterSect;
import org.basex.query.expr.Let;
import org.basex.query.expr.List;
import org.basex.query.expr.LitFunc;
import org.basex.query.expr.LitMap;
import org.basex.query.expr.Or;
import org.basex.query.expr.OrderBy;
import org.basex.query.expr.OrderByExpr;
import org.basex.query.expr.OrderByStable;
import org.basex.query.expr.PartFunApp;
import org.basex.query.expr.Pragma;
import org.basex.query.expr.Quantifier;
import org.basex.query.expr.Range;
import org.basex.query.expr.Root;
import org.basex.query.expr.Switch;
import org.basex.query.expr.SwitchCase;
import org.basex.query.expr.Treat;
import org.basex.query.expr.Try;
import org.basex.query.expr.TypeCase;
import org.basex.query.expr.TypeSwitch;
import org.basex.query.expr.Unary;
import org.basex.query.expr.Union;
import org.basex.query.expr.UserFunc;
import org.basex.query.expr.VarRef;
import org.basex.query.ft.FTAnd;
import org.basex.query.ft.FTContains;
import org.basex.query.ft.FTContent;
import org.basex.query.ft.FTDistance;
import org.basex.query.ft.FTExpr;
import org.basex.query.ft.FTExtensionSelection;
import org.basex.query.ft.FTMildNot;
import org.basex.query.ft.FTNot;
import org.basex.query.ft.FTOptions;
import org.basex.query.ft.FTOr;
import org.basex.query.ft.FTOrder;
import org.basex.query.ft.FTScope;
import org.basex.query.ft.FTWeight;
import org.basex.query.ft.FTWindow;
import org.basex.query.ft.FTWords;
import org.basex.query.ft.FTWords.FTMode;
import org.basex.query.ft.ThesQuery;
import org.basex.query.ft.Thesaurus;
import org.basex.query.func.Variable;
import org.basex.query.item.Atm;
import org.basex.query.item.AtomType;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Empty;
import org.basex.query.item.FuncType;
import org.basex.query.item.Itr;
import org.basex.query.item.MapType;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Types;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisStep;
import org.basex.query.path.KindTest;
import org.basex.query.path.NameTest;
import org.basex.query.path.Path;
import org.basex.query.path.Test;
import org.basex.query.up.expr.Delete;
import org.basex.query.up.expr.Insert;
import org.basex.query.up.expr.Rename;
import org.basex.query.up.expr.Replace;
import org.basex.query.up.expr.Transform;
import org.basex.query.util.Err;
import org.basex.query.util.NSLocal;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.query.util.format.DecFormatter;
import org.basex.query.util.pkg.JarDesc;
import org.basex.query.util.pkg.JarParser;
import org.basex.query.util.pkg.Package;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.query.util.pkg.PkgParser;
import org.basex.query.util.pkg.PkgText;
import org.basex.query.util.pkg.PkgValidator;
import org.basex.util.Array;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.InputParser;
import org.basex.util.JarLoader;
import org.basex.util.Reflect;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.XMLToken;
import org.basex.util.ft.FTOpt;
import org.basex.util.ft.FTUnit;
import org.basex.util.ft.Language;
import org.basex.util.ft.Stemmer;
import org.basex.util.ft.StopWords;
import org.basex.util.ft.Tokenizer;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.ObjList;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * Parser for XQuery expressions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class QueryParser extends InputParser {
  /** Query context. */
  public final QueryContext ctx;

  /** Temporary token builder. */
  private final TokenBuilder tok = new TokenBuilder();
  /** Modules loaded by the current file. */
  private final TokenList modules = new TokenList();

  /** Name of current module. */
  private QNm module;

  /** Alternative error output. */
  private Err alter;
  /** Alternative error description. */
  private QNm alterFunc;
  /** Alternative position. */
  private int ap;

  /** Declared serialization options. */
  private final StringList serial = new StringList();
  /** Declaration flag. */
  private boolean declElem;
  /** Declaration flag. */
  private boolean declFunc;
  /** Declaration flag. */
  private boolean declColl;
  /** Declaration flag. */
  private boolean declConstr;
  /** Declaration flag. */
  private boolean declSpaces;
  /** Declaration flag. */
  private boolean declOrder;
  /** Declaration flag. */
  private boolean declReval;
  /** Declaration flag. */
  private boolean declGreat;
  /** Declaration flag. */
  private boolean declPres;
  /** Declaration flag. */
  private boolean declBase;
  /** Declaration flag. */
  private boolean declItem;
  /** Declaration flag. */
  private boolean declVars;

  /**
   * Constructor.
   * @param q query
   * @param c query context
   * @throws QueryException query exception
   */
  public QueryParser(final String q, final QueryContext c)
      throws QueryException {

    super(q, c.base());
    ctx = c;

    // parse pre-defined external variables
    final String bind = ctx.context.prop.get(Prop.BINDINGS).trim();
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
  public final void bind(final StringBuilder key, final StringBuilder val)
      throws QueryException {

    final String k = key.toString().trim();
    if(!k.isEmpty()) ctx.bind(k, new Atm(token(val.toString())));
  }

  /**
   * Parses the specified query or module.
   * If a URI is specified, the query is treated as a module.
   * @param input optional input file
   * @param uri module uri.
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr parse(final IO input, final Uri uri) throws QueryException {
    file = input;
    if(!more()) error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    int cp;
    for(int p = 0; p < ql; p += Character.charCount(cp)) {
      cp = query.codePointAt(p);
      if(XMLToken.valid(cp)) continue;
      qp = p;
      error(QUERYINV, cp);
    }
    final Expr expr = parse(uri);
    if(more()) {
      if(alter != null) error();
      error(QUERYEND, rest());
    }

    // completes the parsing step
    ctx.funcs.check();
    ctx.vars.check();
    ctx.ns.finish(ctx.nsElem);

    // set default decimal format
    final byte[] empty = new QNm(EMPTY).full();
    if(ctx.decFormats.get(empty) == null) {
      ctx.decFormats.add(empty, new DecFormatter());
    }
    return expr;
  }

  /**
   * Parses the specified query and starts with the "Module" rule.
   * If a URI is specified, the query is treated as a module.
   * @param u module uri
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr parse(final Uri u) throws QueryException {
    try {
      Expr expr = null;
      versionDecl();
      if(u == null) {
        expr = mainModule();
        if(expr == null) if(alter != null) error();
        else error(EXPREMPTY);
      } else {
        moduleDecl(u);
      }
      return expr;
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses the "VersionDecl" rule.
   * @throws QueryException query exception
   */
  private void versionDecl() throws QueryException {
    final int p = qp;
    if(!wsConsumeWs(XQUERY)) return;

    final boolean version = wsConsumeWs(VERSION);
    if(version) {
      // parse xquery version
      final String ver = string(stringLiteral());
      if(ver.equals(XQ10)) ctx.xquery3 = false;
      else if(eq(ver, XQ11, XQ30)) ctx.xquery3 = true;
      else error(XQUERYVER, ver);
    }
    // parse xquery encoding (ignored, as input always comes in as string)
    if((version || ctx.xquery3) && wsConsumeWs(ENCODING)) {
      final String enc = string(stringLiteral());
      if(!supported(enc)) error(XQUERYENC2, enc);
    } else if(!version) {
      qp = p;
      return;
    }
    wsCheck(";");
  }

  /**
   * Parses the "MainModule" rule. Parses the "Setter" rule. Parses the
   * "QueryBody (= Expr)" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr mainModule() throws QueryException {
    prolog1();
    prolog2();
    return expr();
  }

  /**
   * Parses the "ModuleDecl" rule.
   * @param u module uri
   * @throws QueryException query exception
   */
  private void moduleDecl(final Uri u) throws QueryException {
    wsCheck(MODULE);
    wsCheck(NSPACE);
    module = new QNm(ncName(XPNAME));
    wsCheck(IS);
    module.uri(stringLiteral());
    if(module.uri() == Uri.EMPTY) error(NSMODURI);

    ctx.ns.add(module, input());
    skipWS();
    check(';');
    prolog1();
    prolog2();
    // check if import and declaration uri match
    if(u != Uri.EMPTY && !u.eq(module.uri())) error(WRONGMODULE, module.uri(),
        file);
  }

  /**
   * Parses the "Prolog" rule. Parses the "Setter" rule.
   * @throws QueryException query exception
   */
  private void prolog1() throws QueryException {
    while(true) {
      final int p = qp;
      if(wsConsumeWs(DECLARE)) {
        if(wsConsumeWs(DEFAULT)) {
          if(!defaultNamespaceDecl() && !defaultCollationDecl()
              && !emptyOrderDecl() && !decFormatDecl(true))
            error(DECLINCOMPLETE);
        } else if(wsConsumeWs(BOUNDARY)) {
          boundarySpaceDecl();
        } else if(wsConsumeWs(BASEURI)) {
          baseURIDecl();
        } else if(wsConsumeWs(CONSTRUCTION)) {
          constructionDecl();
        } else if(wsConsumeWs(ORDERING)) {
          orderingModeDecl();
        } else if(wsConsumeWs(REVALIDATION)) {
          revalidationDecl();
        } else if(wsConsumeWs(COPYNS)) {
          copyNamespacesDecl();
        } else if(wsConsumeWs(DECFORMAT)) {
          decFormatDecl(false);
        } else if(wsConsumeWs(NSPACE)) {
          namespaceDecl();
        } else if(wsConsumeWs(FTOPTION)) {
          final FTOpt fto = new FTOpt();
          while(ftMatchOption(fto));
          ctx.ftopt.init(fto);
        } else {
          qp = p;
          return;
        }
      } else if(wsConsumeWs(IMPORT)) {
        if(wsConsumeWs(SCHEMA)) {
          schemaImport();
        } else if(wsConsumeWs(MODULE)) {
          moduleImport();
        } else {
          qp = p;
          return;
        }
      } else {
        return;
      }
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
      final int p = qp;
      if(!wsConsumeWs(DECLARE)) return;

      if(ctx.xquery3 && wsConsumeWs(CONTEXT)) {
        contextItemDecl();
      } else if(wsConsumeWs(VARIABLE)) {
        varDecl();
      } else if(wsConsumeWs(UPDATING)) {
        ctx.updating = true;
        wsCheck(FUNCTION);
        functionDecl(true);
      } else if(wsConsumeWs(FUNCTION)) {
        functionDecl(false);
      } else if(wsConsumeWs(OPTION)) {
        optionDecl();
      } else if(wsConsumeWs(DEFAULT)) {
        error(PROLOGORDER);
      } else {
        qp = p;
        return;
      }
      skipWS();
      check(';');
    }
  }

  /**
   * Parses the "NamespaceDecl" rule.
   * @throws QueryException query exception
   */
  private void namespaceDecl() throws QueryException {
    final QNm name = new QNm(ncName(XPNAME));
    wsCheck(IS);
    name.uri(stringLiteral());
    if(ctx.ns.find(name.ln()) != null) error(DUPLNSDECL, name);
    ctx.ns.add(name, input());
  }

  /**
   * Parses the "RevalidationDecl" rule.
   * @throws QueryException query exception
   */
  private void revalidationDecl() throws QueryException {
    if(declReval) error(DUPLREVAL);
    declReval = true;
    if(wsConsumeWs(STRICT) || wsConsumeWs(LAX)) error(NOREVAL);
    wsCheck(SKIP);
  }

  /**
   * Parses the "BoundarySpaceDecl" rule.
   * @throws QueryException query exception
   */
  private void boundarySpaceDecl() throws QueryException {
    if(declSpaces) error(DUPLBOUND);
    declSpaces = true;
    final boolean spaces = wsConsumeWs(PRESERVE);
    if(!spaces) wsCheck(STRIP);
    ctx.spaces = spaces;
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
    if(elem) {
      if(declElem) error(DUPLNS);
      declElem = true;
      ctx.nsElem = uri;
    } else {
      if(declFunc) error(DUPLNS);
      declFunc = true;
      ctx.nsFunc = uri;
    }
    return true;
  }

  /**
   * Parses the "OptionDecl" rule.
   * @throws QueryException query exception
   */
  private void optionDecl() throws QueryException {
    skipWS();
    final QNm name = new QNm(qName(QNAMEINV), ctx, input());
    final byte[] val = stringLiteral();
    if(!name.ns()) error(NSMISS, name);

    if(ctx.xquery3 && eq(name.pref(), OUTPUT)) {
      // output declaration
      final String key = string(name.ln());
      if(module != null) error(MODOUT);

      if(ctx.serProp == null) ctx.serProp = new SerializerProp();
      if(ctx.serProp.get(key) == null) error(OUTWHICH, key);
      if(serial.contains(key)) error(OUTDUPL, key);

      ctx.serProp.set(key, string(val));
      serial.add(key);
    } else if(eq(name.pref(), DB)) {
      // project-specific declaration
      final String key = string(uc(name.ln()));
      final Object obj = ctx.context.prop.get(key);
      if(obj == null) error(NOOPTION, key);
      // cache old value (to be reset after query evaluation)
      if(ctx.queryOpt == null) {
        ctx.queryOpt = new HashMap<String, String>();
        ctx.globalOpt = new HashMap<String, Object>();
      }
      ctx.globalOpt.put(key, obj);
      ctx.queryOpt.put(key, string(val));
    }
  }

  /**
   * Parses the "OrderingModeDecl" rule.
   * @throws QueryException query exception
   */
  private void orderingModeDecl() throws QueryException {
    if(declOrder) error(DUPLORD);
    declOrder = true;
    ctx.ordered = wsConsumeWs(ORDERED);
    if(!ctx.ordered) wsCheck(UNORDERED);
  }

  /**
   * Parses the "emptyOrderDecl" rule.
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean emptyOrderDecl() throws QueryException {
    if(!wsConsumeWs(ORDER)) return false;
    wsCheck(EMPTYORD);
    if(declGreat) error(DUPLORDEMP);
    declGreat = true;
    ctx.orderGreatest = wsConsumeWs(GREATEST);
    if(!ctx.orderGreatest) wsCheck(LEAST);
    return true;
  }

  /**
   * Parses the "copyNamespacesDecl" rule. Parses the "PreserveMode" rule.
   * Parses the "InheritMode" rule.
   * @throws QueryException query exception
   */
  private void copyNamespacesDecl() throws QueryException {
    if(declPres) error(DUPLCOPYNS);
    declPres = true;
    ctx.nsPreserve = wsConsumeWs(PRESERVE);
    if(!ctx.nsPreserve) wsCheck(NOPRESERVE);
    consume(',');
    ctx.nsInherit = wsConsumeWs(INHERIT);
    if(!ctx.nsInherit) wsCheck(NOINHERIT);
  }

  /**
   * Parses the "DecimalFormatDecl" rule.
   * @param def default flag
   * @return true if declaration was found
   * @throws QueryException query exception
   */
  private boolean decFormatDecl(final boolean def) throws QueryException {
    if(def && !wsConsumeWs(DECFORMAT)) return false;

    // use empty name for default declaration
    final byte[] name = new QNm(def ? EMPTY : qName(QNAMEINV)).full();
    if(ctx.decFormats.get(name) != null) error(DECDUPL);

    // create new format
    final HashMap<String, String> sl = new HashMap<String, String>();
    // collect all property declarations
    int n;
    do {
      n = sl.size();
      final String prop = string(ncName(null));
      for(final String s : DECFORMATS) {
        if(prop.equals(s)) {
          final String key = s;
          if(sl.get(key) != null) error(DECDUPLPROP, key);
          wsCheck(IS);
          sl.put(key, string(stringLiteral()));
          break;
        }
      }
      if(sl.size() == 0) error(NODECLFORM, prop);
    } while(n != sl.size());

    // completes the format declaration
    ctx.decFormats.add(name, new DecFormatter(input(), sl));
    return true;
  }

  /**
   * Parses the "DefaultCollationDecl" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private boolean defaultCollationDecl() throws QueryException {
    if(!wsConsumeWs(COLLATION)) return false;
    if(declColl) error(DUPLCOLL);
    declColl = true;
    final byte[] coll = ctx.baseURI.resolve(Uri.uri(stringLiteral())).atom();
    if(!eq(URLCOLL, coll)) error(COLLWHICH, coll);
    return true;
  }

  /**
   * Parses the "BaseURIDecl" rule.
   * @throws QueryException query exception
   */
  private void baseURIDecl() throws QueryException {
    if(declBase) error(DUPLBASE);
    declBase = true;
    ctx.baseURI = Uri.uri(stringLiteral());
  }

  /**
   * Parses the "SchemaImport" rule. Parses the "SchemaPrefix" rule.
   * @throws QueryException query exception
   */
  private void schemaImport() throws QueryException {
    if(wsConsumeWs(NSPACE)) {
      ncName(XPNAME);
      wsCheck(IS);
    } else if(wsConsumeWs(DEFAULT)) {
      wsCheck(ELEMENT);
      wsCheck(NSPACE);
    }
    final byte[] ns = stringLiteral();
    if(ns.length == 0) error(NSEMPTY);
    if(wsConsumeWs(AT)) {
      do stringLiteral(); while(wsConsumeWs(COMMA));
    }
    error(IMPLSCHEMA);
  }

  /**
   * Parses the "ModuleImport" rule.
   * @throws QueryException query exception
   */
  private void moduleImport() throws QueryException {
    byte[] ns = EMPTY;
    if(wsConsumeWs(NSPACE)) {
      ns = ncName(XPNAME);
      wsCheck(IS);
    }
    final byte[] uri = stringLiteral();
    if(uri.length == 0) error(NSMODURI);
    if(modules.contains(uri)) error(DUPLMODULE, uri);

    final QNm name = new QNm(ns, uri);
    if(ns != EMPTY) ctx.ns.add(name, input());

    try {
      if(wsConsumeWs(AT)) {
        do {
          module(stringLiteral(), name.uri());
        } while(wsConsumeWs(COMMA));
      } else {
        // search for uri in namespace dictionary
        final TokenSet pkgs = ctx.context.repo.nsDict().get(uri);
        if(pkgs != null) {
          // load packages with modules having the given uri
          for(final byte[] pkg : pkgs) {
            if(pkg != null) loadPackage(pkg, new TokenSet(), new TokenSet());
          }
        } else {
          // check statically known modules
          boolean found = false;
          for(final byte[] u : MODULES) found |= eq(uri, u);
          // check pre-declared modules
          final byte[] path = ctx.modDeclared.get(uri);
          if(path != null) module(path, name.uri());
          // module not found: show error
          else if(!found) error(NOMODULE, uri);
        }
      }
    } catch(final StackOverflowError ex) {
      error(CIRCMODULE);
    }
  }

  /**
   * Parses the specified module.
   * @param path file path
   * @param uri module uri
   * @throws QueryException query exception
   */
  private void module(final byte[] path, final Uri uri) throws QueryException {
    final byte[] u = ctx.modParsed.get(path);
    if(u != null) {
      if(!eq(uri.atom(), u)) error(WRONGMODULE, uri, path);
      return;
    }
    ctx.modParsed.add(path, uri.atom());

    // check specified path and path relative to query file
    final IO io = io(string(path));
    String qu = null;
    try {
      qu = string(io.read());
    } catch(final IOException ex) {
      error(NOMODULEFILE, io);
    }

    final NSLocal ns = ctx.ns;
    ctx.ns = new NSLocal();
    new QueryParser(qu, ctx).parse(io, uri);
    ctx.ns = ns;
    modules.add(uri.atom());
  }

  /**
   * Loads a package from package repository.
   * @param pkgName package name
   * @param pkgsToLoad list with packages to be loaded
   * @param pkgsLoaded already loaded packages
   * @throws QueryException query exception
   */
  private void loadPackage(final byte[] pkgName, final TokenSet pkgsToLoad,
      final TokenSet pkgsLoaded) throws QueryException {

    // return if package is already loaded
    if(pkgsLoaded.id(pkgName) != 0) return;

    // find package in package dictionary
    final byte[] pDir = ctx.context.repo.pkgDict().get(pkgName);
    if(pDir == null) error(NECPKGNOTINST, pkgName);
    final IOFile pkgDir = ctx.context.repo.path(string(pDir));

    // parse package descriptor
    final IO pkgDesc = new IOFile(pkgDir, PkgText.DESCRIPTOR);
    if(!pkgDesc.exists()) Util.debug(PkgText.MISSDESC, string(pkgName));

    final Package pkg = new PkgParser(ctx.context.repo, input()).parse(pkgDesc);
    // check if package contains a jar descriptor
    final IO jarDesc = new IOFile(pkgDir, PkgText.JARDESC);
    // add jars to classpath
    if(jarDesc.exists()) loadJars(jarDesc, pkgDir, string(pkg.abbrev));

    // package has dependencies -> they have to be loaded first => put package
    // in list with packages to be loaded
    if(pkg.dep.size() != 0) pkgsToLoad.add(pkgName);
    for(final Dependency d : pkg.dep) {
      if(d.pkg != null) {
      // we consider only package dependencies here
      final byte[] depPkg = new PkgValidator(
          ctx.context.repo, input()).depPkg(d);
      if(depPkg == null) {
        error(NECPKGNOTINST, string(d.pkg));
      } else {
        if(pkgsToLoad.id(depPkg) != 0) error(CIRCMODULE);
        loadPackage(depPkg, pkgsToLoad, pkgsLoaded);
      }
     }
    }
    for(final Component comp : pkg.comps) {
      final String path = new IOFile(new IOFile(pkgDir, string(pkg.abbrev)),
          string(comp.file)).path();
      module(token(path), Uri.uri(comp.uri));
    }
    if(pkgsToLoad.id(pkgName) != 0) pkgsToLoad.delete(pkgName);
    pkgsLoaded.add(pkgName);
  }

  /**
   * Loads the jar files registered in jarDesc.
   * @param jarDesc jar descriptor
   * @param pkgDir package directory
   * @param modDir module directory
   * @throws QueryException query exception
   */
  private void loadJars(final IO jarDesc,
      final IOFile pkgDir, final String modDir) throws QueryException {

    final JarDesc desc = new JarParser(ctx.context, input()).parse(jarDesc);
    final URL[] urls = new URL[desc.jars.size()];
    // Collect jar files
    int i = 0;
    for(final byte[] jar : desc.jars) {
      // Assumes that jar is in the directory containing the xquery modules
      final IOFile path = new IOFile(new IOFile(pkgDir, modDir), string(jar));
      try {
        urls[i++] = new URL(IO.FILEPREF + path);
      } catch(final MalformedURLException ex) {
        Util.debug(ex.getMessage());
      }
    }
    // Add jars to classpath
    Reflect.setJarLoader(new JarLoader(urls));
    // Load public classes
    for(final byte[] c : desc.classes)
          ctx.ns.add(new QNm(concat(token("java:"), c)), input());
  }
  /**
   * Parses the "VarDecl" rule.
   * @throws QueryException query exception
   */
  private void contextItemDecl() throws QueryException {
    wsCheck(ITEMM);
    if(declItem) error(DUPLITEM);
    declItem = true;
    if(module != null) error(DECITEM);

    final SeqType st = optAsType();
    if(st != null && st.type == AtomType.EMP) error(NOTYPE, st);
    ctx.initType = st;
    if(!wsConsumeWs(EXTERNAL)) wsCheck(ASSIGN);
    else if(!wsConsumeWs(ASSIGN)) return;
    ctx.initExpr = check(single(), NOVARDECL);
  }

  /**
   * Parses the "VarDecl" rule.
   * @throws QueryException query exception
   */
  private void varDecl() throws QueryException {
    final Var v = typedVar();
    if(module != null && !v.name.uri().eq(module.uri())) error(MODNS, v);

    // check if variable has already been declared
    final Var old = ctx.vars.get(v.name);
    // throw no error if a variable has been externally bound
    if(old != null && old.declared) error(VARDEFINE, old);
    (old != null ? old : v).declared = true;

    if(wsConsumeWs(EXTERNAL)) {
      // bind value with new type
      if(old != null && v.type != null) old.reset(v.type, ctx);
      // bind default value
      if(ctx.xquery3 && wsConsumeWs(ASSIGN)) {
        v.bind(check(single(), NOVARDECL), ctx);
      }
    } else {
      wsCheck(ASSIGN);
      v.bind(check(single(), NOVARDECL), ctx);
    }

    // bind variable if not done yet
    if(old == null) ctx.vars.setGlobal(v);
  }

  /**
   * Parses a variable declaration with optional type.
   * @return parsed variable
   * @throws QueryException query exception
   */
  private Var typedVar() throws QueryException {
    return Var.create(ctx, input(), varName(), optAsType());
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
    if(declConstr) error(DUPLCONS);
    declConstr = true;
    ctx.construct = wsConsumeWs(PRESERVE);
    if(!ctx.construct) wsCheck(STRIP);
  }

  /**
   * Parses the "FunctionDecl" rule.
   * @param up updating flag
   * @throws QueryException query exception
   */
  private void functionDecl(final boolean up) throws QueryException {
    skipWS();
    final InputInfo ii = input();
    final QNm name = new QNm(qName(FUNCNAME));
    name.uri(name.ns() ? ctx.ns.uri(name.pref(), false, input()) : ctx.nsFunc);
    if(module != null && !name.uri().eq(module.uri())) error(MODNS, name);

    wsCheck(PAR1);
    final int s = ctx.vars.size();

    final Var[] args = paramList();
    wsCheck(PAR2);

    final UserFunc func = new UserFunc(ii, name, args, optAsType(), true);
    func.updating = up;

    ctx.funcs.add(func, this);
    if(!wsConsumeWs(EXTERNAL)) func.expr = enclosed(NOFUNBODY);
    ctx.vars.reset(s);
  }

  /**
   * Parses a ParamList.
   * @return declared variables
   * @throws QueryException query exception
   */
  private Var[] paramList() throws QueryException {
    Var[] args = { };
    skipWS();
    while(true) {
      if(curr() != '$') {
        if(args.length == 0) break;
        check('$');
      }
      final Var var = typedVar();
      ctx.vars.add(var);
      for(final Var v : args)
        if(v.name.eq(var.name)) error(FUNCDUPL, var);

      args = Array.add(args, var);
      if(!consume(',')) break;
      skipWS();
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
    Expr[] l = { e };
    do l = add(l, single()); while(wsConsume(COMMA));
    return new List(input(), l);
  }

  /**
   * Parses the "ExprSingle" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr single() throws QueryException {
    alter = null;
    Expr e = gflwor();
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
   * Parses the "FLWORExpr" rule. Parses the "WhereClause" rule. Parses the
   * "OrderByClause" rule. Parses the "OrderSpecList" rule. Parses the
   * "GroupByClause"
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr gflwor() throws QueryException {
    final int s = ctx.vars.size();

    final ForLet[] fl = forLet();
    if(fl == null) return null;

    Expr where = null;
    if(wsConsumeWs(WHERE)) {
      ap = qp;
      where = check(single(), NOWHERE);
      alter = NOWHERE;
    }

    Var[][] group = null;
    if(ctx.xquery3 && wsConsumeWs(GROUP)) {
      wsCheck(BY);
      ap = qp;
      Var[] grp = null;
      do grp = groupSpec(fl, grp); while(wsConsume(COMMA));

      // find all non-grouping variables that aren't shadowed
      final ObjList<Var> ng = new ObjList<Var>();
      Map ngp = Map.EMPTY;
      for(final ForLet f : fl) {
        vars: for(final Var v : f.vars()) {
          final Value old = ngp.get(v.name, null);
          final int pos = old.item() ? (int) old.itemAt(0).itr(null) : -1;

          // number of entries is expected to be tiny, so linear search is fast
          for(final Var g : grp) {
            if(v.is(g)) {
              if(pos >= 0) {
                ng.delete(pos);
                ngp = ngp.delete(old.itemAt(0), null);
              }
              continue vars;
            }
          }

          if(pos >= 0) ng.set(pos, v);
          else {
            ng.add(v);
            ngp = ngp.insert(v.name, Itr.get(ng.size() - 1), null);
          }
        }
      }

      // add new copies for all non-grouping variables
      final Var[] ngrp = new Var[ng.size()];
      for(int i = ng.size(); --i >= 0;) {
        final Var v = ng.get(i);

        // if one groups variables such as $x as xs:integer, then the resulting
        // sequence isn't compatible with the type and can't be assigned
        ngrp[i] = Var.create(ctx, input(), v.name, v.type != null
            && v.type.one() ? SeqType.get(v.type.type, Occ.OM) : null);
        ctx.vars.add(ngrp[i]);
      }

      group = new Var[][]{ grp, ng.toArray(new Var[ng.size()]), ngrp };
      alter = GRPBY;
    }

    OrderBy[] order = null;
    final boolean stable = wsConsumeWs(STABLE);
    if(stable) wsCheck(ORDER);

    if(stable || wsConsumeWs(ORDER)) {
      wsCheck(BY);
      ap = qp;
      do order = orderSpec(order); while(wsConsume(COMMA));
      if(order != null) order = Array.add(order, new OrderByStable(input()));
      alter = ORDERBY;
    }

    if(!wsConsumeWs(RETURN)) {
      if(alter != null) error();
      error(where == null ? FLWORWHERE : order == null ? FLWORORD : FLWORRET);
    }
    final Expr ret = check(single(), NORETURN);
    ctx.vars.reset(s);
    return GFLWOR.get(fl, where, order, group, ret, input());
  }

  /**
   * Parses the "ForClause" rule. Parses the "PositionalVar" rule. Parses the
   * "LetClause" rule. Parses the "FTScoreVar" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private ForLet[] forLet() throws QueryException {
    ForLet[] fl = null;
    boolean comma = false;

    while(true) {
      final boolean fr = wsConsumeWs(FOR, DOLLAR, NOFOR);
      boolean score = !fr && wsConsumeWs(LET, SCORE, NOLET);
      if(score) wsCheck(SCORE);
      else if(!fr && !wsConsumeWs(LET, DOLLAR, NOLET)) return fl;

      do {
        if(comma && !fr) score = wsConsumeWs(SCORE);

        final QNm name = varName();
        final SeqType type = score ? SeqType.DBL : optAsType();
        final Var var = Var.create(ctx, input(), name, type);

        final Var ps = fr && wsConsumeWs(AT) ? Var.create(ctx, input(),
            varName(), SeqType.ITR) : null;
        final Var sc = fr && wsConsumeWs(SCORE) ? Var.create(ctx, input(),
            varName(), SeqType.DBL) : null;

        wsCheck(fr ? IN : ASSIGN);
        final Expr e = check(single(), NOVARDECL);
        ctx.vars.add(var);

        if(ps != null) {
          if(name.eq(ps.name)) error(DUPLVAR, var);
          ctx.vars.add(ps);
        }
        if(sc != null) {
          if(name.eq(sc.name)) error(DUPLVAR, var);
          if(ps != null && ps.name.eq(sc.name)) error(DUPLVAR, ps);
          ctx.vars.add(sc);
        }

        fl = fl == null ? new ForLet[1] : Arrays.copyOf(fl, fl.length + 1);
        fl[fl.length - 1] = fr ? new For(input(), e, var, ps, sc) : new Let(
            input(), e, var, score);

        score = false;
        comma = true;
      } while(wsConsume(COMMA));

      comma = false;
    }
  }

  /**
   * Parses the "OrderSpec" rule. Parses the "OrderModifier" rule.
   * @param order order array
   * @return new order array
   * @throws QueryException query exception
   */
  private OrderBy[] orderSpec(final OrderBy[] order) throws QueryException {
    final Expr e = check(single(), ORDERBY);

    boolean desc = false;
    if(!wsConsumeWs(ASCENDING)) desc = wsConsumeWs(DESCENDING);
    boolean least = !ctx.orderGreatest;
    if(wsConsumeWs(EMPTYORD)) {
      least = !wsConsumeWs(GREATEST);
      if(least) wsCheck(LEAST);
    }
    if(wsConsumeWs(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!eq(URLCOLL, coll)) error(INVCOLL, coll);
    }
    if(e.empty()) return order;
    final OrderBy ord = new OrderByExpr(input(), e, desc, least);
    return order == null ? new OrderBy[] { ord} : Array.add(order, ord);
  }

  /**
   * Parses the "GroupingSpec" rule.
   * @param fl for/let clauses
   * @param group grouping specification
   * @return new group array
   * @throws QueryException query exception
   */
  private Var[] groupSpec(final ForLet[] fl, final Var[] group)
      throws QueryException {
    final Var v = checkVar(varName(), GVARNOTDEFINED);

    // the grouping variable has to be declared by the same FLWOR expression
    boolean dec = false;
    for(final ForLet f : fl) {
      if(f.declares(v)) {
        dec = true;
        break;
      }
    }
    if(!dec) error(GVARNOTDEFINED, v);

    if(wsConsumeWs(COLLATION)) {
      final byte[] coll = stringLiteral();
      if(!eq(URLCOLL, coll)) error(INVCOLL, coll);
    }
    return group == null ? new Var[] { v } : Array.add(group, v);
  }

  /**
   * Parses the "QuantifiedExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr quantified() throws QueryException {
    final boolean some = wsConsumeWs(SOME, DOLLAR, NOSOME);
    if(!some && !wsConsumeWs(EVERY, DOLLAR, NOSOME)) return null;

    final int s = ctx.vars.size();
    For[] fl = { };
    do {
      final Var var = typedVar();
      wsCheck(IN);
      final Expr e = check(single(), NOSOME);
      ctx.vars.add(var);
      fl = Array.add(fl, new For(input(), e, var));
    } while(wsConsume(COMMA));

    wsCheck(SATISFIES);
    final Expr e = check(single(), NOSOME);
    ctx.vars.reset(s);
    return new Quantifier(input(), fl, e, !some);
  }

  /**
   * Parses the "SwitchExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr switchh() throws QueryException {
    if(!wsConsumeWs(SWITCH, PAR1, TYPEPAR)) return null;
    wsCheck(PAR1);
    final Expr expr = check(expr(), NOSWITCH);
    SwitchCase[] exprs = { };
    wsCheck(PAR2);

    // collect all cases
    Expr[] cases = {};
    do {
      cases = new Expr[1];
      while(wsConsumeWs(CASE)) cases = add(cases, single());
      if(cases.length == 1) {
        // add default case
        if(exprs.length == 0) error(WRONGCHAR, CASE, found());
        wsCheck(DEFAULT);
      }
      wsCheck(RETURN);
      cases[0] = single();
      exprs = Array.add(exprs, new SwitchCase(input(), cases));
    } while(cases.length != 1);

    return new Switch(input(), expr, exprs);
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
    final int s = ctx.vars.size();
    boolean cs = true;
    do {
      cs = wsConsumeWs(CASE);
      if(!cs) wsCheck(DEFAULT);
      skipWS();
      QNm name = null;
      if(curr('$')) {
        name = varName();
        if(cs) wsCheck(AS);
      }
      final Var v = Var.create(ctx, input(), name, cs ? sequenceType() : null);
      if(name != null) ctx.vars.add(v);
      wsCheck(RETURN);
      final Expr ret = check(single(), NOTYPESWITCH);
      cases = Array.add(cases, new TypeCase(input(), v, ret));
      ctx.vars.reset(s);
    } while(cs);
    if(cases.length == 1) error(NOTYPESWITCH);
    return new TypeSwitch(input(), ts, cases);
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
    return new If(input(), iff, thn, els);
  }

  /**
   * Parses the "OrExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    if(!wsConsumeWs(OR)) return e;

    Expr[] list = { e };
    do list = add(list, and()); while(wsConsumeWs(OR));
    return new Or(input(), list);
  }

  /**
   * Parses the "AndExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr and() throws QueryException {
    final Expr e = comparison();
    if(!wsConsumeWs(AND)) return e;

    Expr[] list = { e };
    do list = add(list, comparison()); while(wsConsumeWs(AND));
    return new And(input(), list);
  }

  /**
   * Parses the "ComparisonExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr comparison() throws QueryException {
    final Expr e = ftContains();
    if(e != null) {
      for(final CmpV.Op c : CmpV.Op.values())
        if(wsConsumeWs(c.name)) return new CmpV(input(), e, check(ftContains(),
            CMPEXPR), c);
      for(final CmpN.Op c : CmpN.Op.values())
        if(wsConsumeWs(c.name)) return new CmpN(input(), e, check(ftContains(),
            CMPEXPR), c);
      for(final CmpG.Op c : CmpG.Op.values())
        if(wsConsume(c.name)) return new CmpG(input(), e, check(ftContains(),
            CMPEXPR), c);
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

    final int p = qp;
    // use "=>" and "<-" as unofficial shortcuts for full-text expressions
    if(consume('=') && consume('>') || consume('<') && consume('-')) {
      skipWS();
    } else if(!wsConsumeWs(CONTAINS) || !wsConsumeWs(TEXT)) {
      qp = p;
      return e;
    }

    final FTExpr select = ftSelection(false);
    if(wsConsumeWs(WITHOUT)) {
      wsCheck(CONTENT);
      union();
      error(FTIGNORE);
    }
    return new FTContains(e, select, input());
  }

  /**
   * Parses the "StringConcatExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr stringConcat() throws QueryException {
    final Expr e = range();
    if(!consume(CONCAT)) return e;

    Expr[] list = { e };
    do list = add(list, range()); while(wsConsume(CONCAT));
    return new Concat(input(), list);
  }

  /**
   * Parses the "RangeExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr range() throws QueryException {
    final Expr e = additive();
    if(!wsConsumeWs(TO)) return e;
    return new Range(input(), e, check(additive(), INCOMPLETE));
  }

  /**
   * Parses the "AdditiveExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr additive() throws QueryException {
    Expr e = multiplicative();

    while(true) {
      final Calc c = consume('+') ? Calc.PLUS : consume('-') ? Calc.MINUS
          : null;
      if(c == null) break;
      e = new Arith(input(), e, check(multiplicative(), CALCEXPR), c);
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
      e = new Arith(input(), e, check(union(), CALCEXPR), c);
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
    Expr[] list = { e };
    do list = add(list, intersect()); while(isUnion());
    return new Union(input(), list);
  }

  /**
   * Checks if a union operator is found.
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean isUnion() throws QueryException {
    if(wsConsumeWs(UNION)) return true;
    final int p = qp;
    if(consume(PIPE) && !consume(PIPE)) return true;
    qp = p;
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
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(wsConsumeWs(INTERSECT));
      return new InterSect(input(), list);
    } else if(wsConsumeWs(EXCEPT)) {
      Expr[] list = { e };
      do list = add(list, instanceoff()); while(wsConsumeWs(EXCEPT));
      return new Except(input(), list);
    } else {
      return e;
    }
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
    return new Instance(input(), e, sequenceType());
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
    return new Treat(input(), e, sequenceType());
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
    return new Castable(input(), e, simpleType());
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
    return new Cast(input(), e, simpleType());
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
        return found ? new Unary(input(), check(e, EVALUNARY), minus) : e;
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
    if(wsConsumeWs(VALIDATE)) {
      if(!wsConsumeWs(STRICT) && !wsConsumeWs(LAX) && wsConsumeWs(TYPE)) {
        qName(Err.QNAMEINV);
      }
      wsCheck(BRACE1);
      check(single(), NOVALIDATE);
      wsCheck(BRACE2);
      error(IMPLVAL);
    }
  }

  /**
   * Parses the "ExtensionExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr extension() throws QueryException {
    final Expr[] pragmas = pragma();
    return pragmas.length == 0 ? null : new Extension(input(), pragmas,
        enclosed(NOPRAGMA));
  }

  /**
   * Parses the "Pragma" rule.
   * @return array of pragmas
   * @throws QueryException query exception
   */
  private Expr[] pragma() throws QueryException {
    Expr[] pragmas = { };
    while(wsConsumeWs(PRAGMA)) {
      skipWS();
      final QNm name = new QNm(qName(PRAGMAINV), ctx, input());
      if(!name.ns()) error(NSMISS, name);
      char c = curr();
      if(c != '#' && !ws(c)) error(PRAGMAINV);

      tok.reset();
      while(c != '#' || next() != ')') {
        if(c == 0) error(PRAGMAINV);
        tok.add(consume());
        c = curr();
      }
      pragmas = add(pragmas, new Pragma(name, tok.trim().finish(), input()));
      qp += 2;
    }
    return pragmas;
  }

  /**
   * Parses the "PathExpr" rule. Parses the "RelativePathExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr path() throws QueryException {
    checkInit();
    final int s = consume('/') ? consume('/') ? 2 : 1 : 0;
    if(s > 0) checkAxis(s == 2 ? Axis.DESC : Axis.CHILD);
    qm = qp;

    final Expr ex = step();
    if(ex == null) {
      if(s == 2) {
        if(more()) checkInit();
        error(PATHMISS);
      }
      return s == 1 ? new Root(input()) : null;
    }

    final boolean slash = consume('/');
    final boolean step = ex instanceof AxisStep;
    if(!slash && s == 0 && !step) return ex;

    Expr[] list = { };
    if(s == 2) list = add(list, descOrSelf());

    final Expr root = s > 0 ? new Root(input()) : !step ? ex : null;
    if(root != ex) list = add(list, ex);

    if(slash) {
      do {
        final boolean desc = consume('/');
        qm = qp;
        if(desc) list = add(list, descOrSelf());
        checkAxis(desc ? Axis.DESC : Axis.CHILD);

        final Expr st = step();
        if(st == null) error(PATHMISS);
        // skip context nodes
        if(!(st instanceof Context)) list = add(list, st);
      } while(consume('/'));
    }
    // if no location steps have been added, add trailing self::node() step as
    // replacement for context node to bring results in order
    if(list.length == 0) {
      list = add(list, AxisStep.get(input(), Axis.SELF, Test.NOD));
    }
    return Path.get(input(), root, list);
  }

  /**
   * Returns a standard descendant-or-self::node() step.
   * @return step
   */
  private AxisStep descOrSelf() {
    return AxisStep.get(input(), Axis.DESCORSELF, Test.NOD);
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
    return e != null ? e : axis();
  }

  /**
   * Parses the "AxisStep" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private AxisStep axis() throws QueryException {
    Axis ax = null;
    Test test = null;
    if(wsConsume(DOT2)) {
      ax = Axis.PARENT;
      test = Test.NOD;
      checkTest(test, false);
    } else if(consume('@')) {
      ax = Axis.ATTR;
      test = test(true);
      checkTest(test, true);
      if(test == null) {
        --qp;
        error(NOATTNAME);
      }
    } else {
      for(final Axis a : Axis.values()) {
        if(wsConsumeWs(a.name, COLS, NOLOCSTEP)) {
          wsConsume(COLS);
          ap = qp;
          ax = a;
          test = test(a == Axis.ATTR);
          checkTest(test, a == Axis.ATTR);
          break;
        }
      }
    }
    if(ax == null) {
      ax = Axis.CHILD;
      test = test(false);
      if(test != null && test.type == NodeType.ATT) ax = Axis.ATTR;
      checkTest(test, ax == Axis.ATTR);
    }
    if(test == null) return null;

    Expr[] pred = { };
    while(wsConsume(BR1)) {
      checkPred(true);
      pred = add(pred, expr());
      wsCheck(BR2);
      checkPred(false);
    }
    return AxisStep.get(input(), ax, test, pred);
  }

  /**
   * Parses the "NodeTest" rule. Parses the "NameTest" rule. Parses the
   * "KindTest" rule.
   * @param att attribute flag
   * @return query expression
   * @throws QueryException query exception
   */
  private Test test(final boolean att) throws QueryException {
    final int p = qp;
    final char ch = curr();
    if(XMLToken.isNCStartChar(ch)) {
      final byte[] name = qName(null);
      final int p2 = qp;
      if(wsConsumeWs(PAR1)) {
        final NodeType type = NodeType.find(new QNm(name, ctx, input()));
        if(type != null) {
          tok.reset();
          while(!consume(PAR2)) {
            if(curr(0)) error(TESTINCOMPLETE);
            tok.add(consume());
          }
          skipWS();
          return tok.trim().size() == 0 ? Test.get(type) : kindTest(type,
              tok.finish());
        }
      } else {
        qp = p2;
        // name test "pre:tag"
        if(contains(name, ':')) {
          skipWS();
          return new NameTest(new QNm(name, ctx, input()), NameTest.Name.STD,
              att, input());
        }
        // name test "tag"
        if(!consume(':')) {
          skipWS();
          final QNm nm = new QNm(name, att ? Uri.EMPTY : null);
          return new NameTest(nm, NameTest.Name.STD, att, input());
        }
        // name test "pre:*"
        if(consume('*')) {
          final QNm nm = new QNm(EMPTY, ctx.ns.uri(name, false, input()));
          return new NameTest(nm, NameTest.Name.NS, att, input());
        }
      }
    } else if(consume('*')) {
      // name test "*"
      if(!consume(':')) return new NameTest(att, input());
      // name test "*:tag"
      return new NameTest(new QNm(qName(QNAMEINV)), NameTest.Name.NAME, att,
          input());
    }
    qp = p;
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
        Expr[] pred = { };
        do {
          pred = add(pred, expr());
          wsCheck(BR2);
        } while(wsConsume(BR1));
        e = new Filter(input(), e, pred);
      } else if(e != null) {
        final Expr[] args = argumentList(e);
        if(args == null) break;

        final Var[] part = new Var[args.length];
        final boolean pt = partial(args, part);
        e = new DynFuncCall(input(), e, args);
        if(pt) e = new PartFunApp(input(), e, part);
      }
    } while(e != old);
    return e;
  }

  /**
   * Fills gaps from place-holders with variable references.
   * @param args argument array
   * @param vars variables array
   * @return variables bound
   */
  private boolean partial(final Expr[] args, final Var[] vars) {
    final InputInfo ii = input();
    boolean found = false;
    for(int i = 0; i < args.length; i++) {
      if(args[i] == null) {
        vars[i] = ctx.uniqueVar(ii, null);
        args[i] = new VarRef(ii, vars[i]);
        found = true;
      }
    }
    return found;
  }

  /**
   * Parses the "PrimaryExpr" rule. Parses the "VarRef" rule. Parses the
   * "ContextItem" rule. Parses the "Literal" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr primary() throws QueryException {
    skipWS();
    final char c = curr();
    // variables
    if(c == '$') return new VarRef(input(), checkVar(varName(), VARUNDEF));
    // parentheses
    if(c == '(' && next() != '#') return parenthesized();
    // direct constructor
    if(c == '<') return constructor();
    // function calls and computed constructors
    if(XMLToken.isNCStartChar(c)) {
      Expr e;
      if(ctx.xquery3) {
        e = functionItem();
        if(e != null) return e;
      }
      e = functionCall();
      if(e != null) return e;
      e = compConstructor();
      if(e != null) return e;
      // ordered expression
      if(wsConsumeWs(ORDERED, BRACE1, INCOMPLETE)
          || wsConsumeWs(UNORDERED, BRACE1, INCOMPLETE))
        return enclosed(NOENCLEXPR);

      if(wsConsumeWs(MAPSTR, BRACE1, INCOMPLETE)) return mapLiteral();
    }
    // context item
    if(c == '.' && !digit(next())) {
      if(next() == '.') return null;
      consume('.');
      return new Context(input());
    }
    // literals
    if(digit(c) || c == '.') return numericLiteral(false);
    // strings
    return quote(c) ? Str.get(stringLiteral()) : null;
  }

  /**
   * Parses a literal map. [LW] adapt to actual spec when available.
   * @return map literal
   * @throws QueryException query exception
   */
  private Expr mapLiteral() throws QueryException {
    wsCheck(BRACE1);
    Expr[] args = { };

    if(!wsConsume(BRACE2)) {
      do {
        args = Array.add(args, check(single(), INVMAPKEY));
        wsCheck(ASSIGN);
        args = Array.add(args, check(single(), INVMAPVAL));
      } while(wsConsume(COMMA));
      wsCheck(BRACE2);
    }

    return new LitMap(input(), args);
  }

  /**
   * Parses the "FunctionItemExpr" rule. Parses the "LiteralFunctionItem" rule.
   * Parses the "InlineFunction" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr functionItem() throws QueryException {
    final int pos = qp;
    if(wsConsume(FUNCTION) && wsConsume(PAR1)) {
      final int s = ctx.vars.size();
      final Var[] args = paramList();
      wsCheck(PAR2);

      final SeqType type = optAsType();
      final Expr body = enclosed(NOFUNBODY);
      ctx.vars.reset(s);
      return new InlineFunc(input(), type, args, body);
    }
    qp = pos;

    skipWS();
    final byte[] fn = qName(null);
    if(fn.length > 0 && consume(HASH)) {
      final QNm name = new QNm(fn);
      if(name.ns()) ctx.ns.uri(name);
      else name.uri(ctx.nsFunc);
      final long cardinal = ((Itr) numericLiteral(true)).itr(null);
      if(cardinal < 0 || cardinal > Integer.MAX_VALUE) error(FUNCUNKNOWN, fn);

      final Expr[] args = new Expr[(int) cardinal];
      final Var[] vars = new Var[args.length];
      partial(args, vars);
      final TypedFunc f = ctx.funcs.get(name, args, ctx, this);
      if(f == null) error(FUNCUNKNOWN, fn);
      return new LitFunc(input(), name, f, vars);
    }

    qp = pos;
    return null;
  }

  /**
   * Parses the "NumericLiteral" rule. Parses the "IntegerLiteral" rule.
   * @param itr integer flag
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr numericLiteral(final boolean itr) throws QueryException {
    tok.reset();
    while(digit(curr()))
      tok.add(consume());
    if(consume('.')) {
      if(itr) error(NUMBERITR);
      return decimalLiteral();
    }
    if(XMLToken.isNCStartChar(curr())) return checkDbl();
    final long l = toLong(tok.finish());
    if(l == Long.MIN_VALUE) error(RANGE, tok);
    return Itr.get(l);
  }

  /**
   * Parses the "DecimalLiteral" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr decimalLiteral() throws QueryException {
    tok.add('.');
    if(XMLToken.isNCStartChar(curr())) return checkDbl();
    while(digit(curr()))
      tok.add(consume());
    return XMLToken.isNCStartChar(curr()) ? checkDbl() : new Dec(tok.finish());
  }

  /**
   * Parses the "DoubleLiteral" rule. Checks if a number is followed by a
   * whitespace.
   * @return expression
   * @throws QueryException query exception
   */
  private Expr checkDbl() throws QueryException {
    if(!consume('e') && !consume('E')) error(NUMBERWS);
    tok.add('e');
    if(curr('+') || curr('-')) tok.add(consume());
    final int s = tok.size();
    while(digit(curr()))
      tok.add(consume());
    if(s == tok.size()) error(NUMBERINC);
    if(XMLToken.isNCStartChar(curr())) error(NUMBERWS);
    return Dbl.get(tok.finish(), input());
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
        if(curr(0)) error(NOQUOTE, found());
        entity(tok);
      }
      if(!consume(del)) break;
      tok.add(del);
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
    final QNm name = new QNm(qName(NOVARNAME));
    if(name.ns()) name.uri(ctx.ns.uri(name.pref(), false, input()));
    ctx.ns.uri(name);
    return name;
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
    final int p = qp;
    final QNm name = new QNm(qName(null), ctx, input());

    Expr[] args;
    if(NodeType.find(name) != null
        || (args = argumentList(name.atom())) == null) {
      qp = p;
      return null;
    }

    alter = FUNCUNKNOWN;
    alterFunc = name;
    ap = qp;
    ctx.ns.uri(name);
    name.uri(name.ns() ? ctx.ns.uri(name.pref(), false, input()) : ctx.nsFunc);

    final Var[] vars = new Var[args.length];
    final boolean part = partial(args, vars);
    final TypedFunc f = ctx.funcs.get(name, args, ctx, this);
    if(f != null) {
      alter = null;
      return part ? new PartFunApp(input(), f, vars) : f.fun;
    }
    qp = p;
    return null;
  }

  /**
   * Parses the "ArgumentList" rule.
   * @param name name of the function (item)
   * @return array of arguments, place-holders '?' are represented as
   *         {@code null} entries
   * @throws QueryException query exception
   */
  private Expr[] argumentList(final Object name) throws QueryException {
    if(!wsConsume(PAR1)) return null;
    Expr[] args = { };
    if(!wsConsume(PAR2)) {
      do {
        Expr arg = null;
        if(!wsConsume(PLHOLDER) && (arg = single()) == null) error(FUNCMISS,
            name);
        args = Array.add(args, arg);
      } while(wsConsume(COMMA));
      if(!wsConsume(PAR2)) error(FUNCMISS, name);
    }
    return args;
  }

  /**
   * Parses the "Constructor" rule. Parses the "DirectConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr constructor() throws QueryException {
    check('<');
    return consume('!') ? dirComment() : consume('?') ? dirPI() : dirElement();
  }

  /**
   * Parses the "DirElemConstructor" rule. Parses the "DirAttributeList" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElement() throws QueryException {
    if(skipWS()) error(NOTAGNAME);
    final QNm tag = new QNm(qName(NOTAGNAME));
    consumeWSS();

    Expr[] cont = { };
    final Atts ns = new Atts();
    final int s = ctx.ns.size();

    // parse attributes...
    boolean xmlDef = false; // xml prefix explicitly declared?
    while(XMLToken.isNCStartChar(curr())) {
      final byte[] atn = qName(null);
      Expr[] attv = { };

      consumeWSS();
      check('=');
      consumeWSS();
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
                attv = add(attv, Str.get(text));
              } else {
                attv = add(attv, enclosed(NOENCLEXPR));
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

      if(tb.size() != 0) attv = add(attv, Str.get(tb.finish()));

      if(eq(atn, XMLNS)) {
        if(!simple) error(NSCONS);
        final byte[] v = attv.length == 0 ? EMPTY : ((Str) attv[0]).atom();
        if(!tag.ns()) tag.uri(v);
        addNS(ns, EMPTY, v);
      } else if(startsWith(atn, XMLNSC)) {
        if(!simple) error(NSCONS);
        final byte[] v = attv.length == 0 ? EMPTY : ((Str) attv[0]).atom();
        if(v.length == 0) error(NSEMPTYURI);
        final QNm nsd = new QNm(atn, v);
        final byte[] pref = nsd.ln();
        if(!eq(pref, XML) || !eq(v, XMLURI)) {
          ctx.ns.add(nsd, input());
          addNS(ns, pref, v);
          if(eq(pref, tag.pref())) tag.uri(v);
        } else {
          if(xmlDef) error(DUPLNSDEF, XML);
          xmlDef = true;
        }
      } else {
        cont = add(cont,
            new CAttr(input(), false, new QNm(atn, contains(atn, ':') ? null
                : Uri.EMPTY), attv));
      }
      if(!consumeWSS()) break;
    }

    if(consume('/')) {
      check('>');
    } else {
      check('>');
      while(curr() != '<' || next() != '/') {
        final Expr e = dirElemContent(tag);
        if(e == null) continue;
        cont = add(cont, e);
      }
      qp += 2;

      if(skipWS()) error(NOTAGNAME);
      final byte[] close = qName(NOTAGNAME);
      consumeWSS();
      check('>');
      if(!eq(tag.atom(), close)) error(TAGWRONG, tag.atom(), close);
    }

    ctx.ns.size(s);
    return new CElem(input(), tag, ns, false, cont);
  }

  /**
   * Checks the uniqueness of the namespace and adds it to the attributes.
   * @param ns namespace array
   * @param k namespace prefix
   * @param v uri
   * @throws QueryException query exception
   */
  private void addNS(final Atts ns, final byte[] k, final byte[] v)
      throws QueryException {

    if(ns.get(k) != -1) error(DUPLNSDEF, k);
    ns.add(k, v);
  }

  /**
   * Parses the "DirElemContent" rule.
   * @param tag opening tag
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirElemContent(final QNm tag) throws QueryException {
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
        error(NOCLOSING, tag.atom());
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
    return t.length == 0 || strip && !ctx.spaces && ws(t) ? null : Str.get(t);
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
        return new CComm(input(), Str.get(tb.finish()));
      }
      tb.add('-');
    } while(true);
  }

  /**
   * Parses the "DirPIConstructor" rule. Parses the "DirPIContents" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr dirPI() throws QueryException {
    if(consumeWSS()) error(PIXML, EMPTY);
    final byte[] str = trim(qName(PIWRONG));
    if(str.length == 0 || eq(lc(str), XML)) error(PIXML, str);

    final boolean space = skipWS();
    final TokenBuilder tb = new TokenBuilder();
    do {
      while(not('?')) {
        if(!space) error(PIWRONG);
        tb.add(consume());
      }
      consume();
      if(consume('>')) {
        if(!XMLToken.isNCName(str)) error(INVALPI, str);
        return new CPI(input(), Str.get(str), Str.get(tb.finish()));
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
        qp += 2;
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
    final int p = qp;
    if(wsConsumeWs(DOCUMENT)) return consume(compDoc(), p);
    else if(wsConsumeWs(ELEMENT)) return consume(compElemConstructor(), p);
    else if(wsConsumeWs(ATTRIBUTE)) return consume(compAttribute(), p);
    else if(wsConsumeWs(TEXT)) return consume(compText(), p);
    else if(wsConsumeWs(COMMENT)) return consume(compComment(), p);
    else if(wsConsumeWs(PI)) return consume(compPI(), p);
    return null;
  }

  /**
   * Consumes the specified expression or resets the query position.
   * @param expr expression
   * @param p query position
   * @return expression or {@code null}
   */
  private Expr consume(final Expr expr, final int p) {
    if(expr == null) qp = p;
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
    return new CDoc(input(), e);
  }

  /**
   * Parses the "CompElemConstructor" rule. Parses the "ContextExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compElemConstructor() throws QueryException {
    skipWS();

    Expr name;
    if(XMLToken.isNCStartChar(curr())) {
      name = new QNm(qName(null));
    } else {
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), NOTAGNAME);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CElem(input(), name, new Atts(), true, e == null ? new Expr[0]
        : new Expr[] { e });
  }

  /**
   * Parses the "CompAttrConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compAttribute() throws QueryException {
    skipWS();

    Expr nm;
    if(XMLToken.isNCStartChar(curr())) {
      nm = new QNm(qName(null));
    } else {
      if(!wsConsume(BRACE1)) return null;
      nm = expr();
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CAttr(input(), true, nm, e == null ? Empty.SEQ : e);
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
    return new CTxt(input(), e);
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
    return new CComm(input(), e);
  }

  /**
   * Parses the "CompPIConstructor" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr compPI() throws QueryException {
    skipWS();
    Expr name;
    if(XMLToken.isNCStartChar(curr())) {
      name = Str.get(ncName(null));
    } else {
      if(!wsConsume(BRACE1)) return null;
      name = check(expr(), PIWRONG);
      wsCheck(BRACE2);
    }

    if(!wsConsume(BRACE1)) return null;
    final Expr e = expr();
    wsCheck(BRACE2);
    return new CPI(input(), name, e == null ? Empty.SEQ : e);
  }

  /**
   * Parses the "SimpleType" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType simpleType() throws QueryException {
    skipWS();
    final QNm type = new QNm(qName(TYPEINVALID));
    type.uri(ctx.ns.uri(type.pref(), false, input()));
    skipWS();
    final Type t = Types.find(type, true);
    if(t == AtomType.AAT || t == AtomType.NOT) error(CASTUNKNOWN, type);
    if(t == null) error(TYPEUNKNOWN, type);
    return SeqType.get(t, consume('?') ? Occ.ZO : Occ.O);
  }

  /**
   * Parses the "SequenceType" rule. Parses the "OccurrenceIndicator" rule.
   * Parses the "KindTest" rule.
   * @return sequence type
   * @throws QueryException query exception
   */
  private SeqType sequenceType() throws QueryException {
    skipWS();
    final Type t = itemType();

    // parse occurrence indicator
    final Occ occ = consume('?') ? Occ.ZO : consume('+') ? Occ.OM
        : consume('*') ? Occ.ZM : Occ.O;
    skipWS();

    if(t == AtomType.EMP && occ != Occ.O) error(EMPTYSEQOCC, t);

    final KindTest kt = tok.size() == 0 ? null : kindTest(t, tok.finish());
    tok.reset();

    // use empty name test if types are different
    return SeqType.get(t, occ, kt == null ? null : kt.extype == null
        || t == kt.extype || !kt.extype.node() ? kt.name : new QNm(EMPTY));
  }

  /**
   * Parses the "ItemType" rule.
   * @return item type
   * @throws QueryException query exception
   */
  private Type itemType() throws QueryException {
    skipWS();

    // parenthesized type
    if(consume(PAR1)) {
      final Type ret = itemType();
      wsCheck(PAR2);
      return ret;
    }

    final QNm type = new QNm(qName(TYPEINVALID));
    type.uri(ctx.ns.uri(type.pref(), false, input()));
    // parse non-atomic types
    final boolean atom = !wsConsumeWs(PAR1);

    Type t = Types.find(type, atom);

    tok.reset();
    if(!atom) {
      if(t != null && t.func()) {
        // function type
        if(!wsConsume(ASTERISK)) {
          if(t.map()) {
            final Type keyType = itemType();
            if(keyType == null) throw error(MAPTKV, type.atom());
            if(!keyType.instance(AtomType.AAT)) throw error(MAPTAAT, keyType);
            wsCheck(COMMA);
            t = MapType.get((AtomType) keyType, sequenceType());
            if(!wsConsume(PAR2)) error(FUNCMISS, type.atom());
          } else {
            // function type
            SeqType[] args = { };
            if(!wsConsume(PAR2)) {
              // function has got arguments
              do {
                args = Array.add(args, sequenceType());
              } while(wsConsume(COMMA));

              if(!wsConsume(PAR2)) error(FUNCMISS, type.atom());
            }
            wsCheck(AS);
            t = FuncType.get(args, sequenceType());
          }
        } else if(!wsConsume(PAR2)) {
          error(FUNCMISS, type.atom());
        }
      } else {
        int par = 0;
        while(par != 0 || !wsConsumeWs(PAR2)) {
          switch(curr()) {
            case '(':
              par++;
              break;
            case ')':
              par--;
              break;
            case '\0':
              error(FUNCMISS, type.atom());
          }
          tok.add(consume());
        }
      }
    }

    if(t == null) {
      if(atom) error(TYPEUNKNOWN, type);
      error(NOTYPE,
          new TokenBuilder(type.atom()).add('(').add(tok.finish()).add(')'));
    }

    return t;
  }

  /**
   * Checks the arguments of the kind test.
   * @param t type
   * @param k kind arguments
   * @return arguments
   * @throws QueryException query exception
   */
  private KindTest kindTest(final Type t, final byte[] k)
  throws QueryException {

    byte[] nm = trim(k);
    if(t == NodeType.PI) {
      final boolean s = startsWith(k, '\'') || startsWith(k, '"');
      nm = trim(delete(delete(k, '\''), '"'));
      if(!XMLToken.isNCName(nm)) {
        if(s) error(XPINVNAME, nm);
        error(TESTINVALID, t, k);
      }
      return new KindTest((NodeType) t, new QNm(nm, ctx, input()), null);
    }
    if(t != NodeType.ELM && t != NodeType.ATT) error(TESTINVALID, t, k);

    Type tp = t;
    final int i = indexOf(nm, ',');
    if(i != -1) {
      final QNm test = new QNm(trim(substring(nm, i + 1)), ctx, input());
      if(!eq(test.uri().atom(), XSURI)) error(TYPEUNDEF, test);

      final byte[] ln = test.ln();
      tp = Types.find(test, true);
      if(tp == null && !eq(ln, AtomType.ATY.nam()) &&
          !eq(ln, AtomType.AST.nam()) && !eq(ln, AtomType.UTY.nam()))
        error(VARUNDEF, test);
      if(tp == AtomType.ATM || tp == AtomType.AAT) tp = null;
      nm = trim(substring(nm, 0, i));
    }
    if(nm.length == 1 && nm[0] == '*') return new KindTest((NodeType) t, null,
        tp);
    if(!XMLToken.isQName(nm)) error(TESTINVALID, t, k);
    return new KindTest((NodeType) t, new QNm(nm, ctx, input()), tp);
  }

  /**
   * Parses the "TryCatch" rules.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr tryCatch() throws QueryException {
    if(!ctx.xquery3 || !wsConsumeWs(TRY)) return null;

    final Expr tr = enclosed(NOENCLEXPR);
    wsCheck(CATCH);

    Catch[] ct = { };
    do {
      QNm[] codes = { };
      do {
        skipWS();
        if(XMLToken.isNCStartChar(curr())) {
          codes = Array.add(codes, new QNm(qName(QNAMEINV)));
        } else {
          wsCheck("*");
          codes = Array.add(codes, (QNm) null);
        }
      } while(wsConsumeWs(PIPE));

      final Catch c = new Catch(input(), codes, ctx);
      final int s = c.prepare(ctx);
      c.expr = enclosed(NOENCLEXPR);
      c.finish(s, ctx);

      ct = Array.add(ct, c);
    } while(wsConsumeWs(CATCH));

    return new Try(input(), tr, ct);
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
        expr = new FTWindow(input(), expr, additive(), ftUnit());
      } else if(wsConsumeWs(DISTANCE)) {
        final Expr[] rng = ftRange(false);
        if(rng == null) error(FTRANGE);
        expr = new FTDistance(input(), expr, rng, ftUnit());
      } else if(wsConsumeWs(AT)) {
        final boolean start = wsConsumeWs(START);
        final boolean end = !start && wsConsumeWs(END);
        if(!start && !end) error(INCOMPLETE);
        expr = new FTContent(input(), expr, start, end);
      } else if(wsConsumeWs(ENTIRE)) {
        wsCheck(CONTENT);
        expr = new FTContent(input(), expr, false, false);
      } else {
        final boolean same = wsConsumeWs(SAME);
        final boolean diff = !same && wsConsumeWs(DIFFERENT);
        if(same || diff) {
          FTUnit unit = null;
          if(wsConsumeWs(SENTENCE)) unit = FTUnit.SENTENCE;
          else if(wsConsumeWs(PARAGRAPH)) unit = FTUnit.PARAGRAPH;
          else error(INCOMPLETE);
          expr = new FTScope(input(), expr, unit, same);
        }
      }
      if(first == null && old != null && old != expr) first = expr;
    } while(old != expr);

    if(ordered) {
      if(first == null) return new FTOrder(input(), expr);
      first.expr[0] = new FTOrder(input(), first.expr[0]);
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
    return new FTOr(input(), list);
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
    return new FTAnd(input(), list);
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
    return new FTMildNot(input(), e, list.length == 1 ? list[0] : new FTOr(
        input(), list));
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
    return not ? new FTNot(input(), e) : e;
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
    if(!Tokenizer.supportFor(fto.ln)) error(Err.FTNOTOK, fto.ln);
    if(fto.is(ST) && fto.sd == null && !Stemmer.supportFor(fto.ln))
      error(Err.FTNOSTEM, fto.ln);

    // consume weight option
    if(wsConsumeWs(WEIGHT)) expr = new FTWeight(input(), expr,
        enclosed(NOENCLEXPR));

    // skip options if none were specified...
    return found ? new FTOptions(input(), expr, fto) : expr;
  }

  /**
   * Parses the "FTPrimary" rule.
   * @param prg pragma flag
   * @return query expression
   * @throws QueryException query exception
   */
  private FTExpr ftPrimary(final boolean prg) throws QueryException {
    final Expr[] pragmas = pragma();
    if(pragmas.length != 0) {
      wsCheck(BRACE1);
      final FTExpr e = ftSelection(true);
      wsCheck(BRACE2);
      return new FTExtensionSelection(input(), pragmas, e);
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
    FTMode mode = FTMode.M_ANY;
    if(wsConsumeWs(ALL)) {
      mode = wsConsumeWs(WORDS) ? FTMode.M_ALLWORDS : FTMode.M_ALL;
    } else if(wsConsumeWs(ANY)) {
      mode = wsConsumeWs(WORD) ? FTMode.M_ANYWORD : FTMode.M_ANY;
    } else if(wsConsumeWs(PHRASE)) {
      mode = FTMode.M_PHRASE;
    }

    // FTTimes
    Expr[] occ = null;
    if(wsConsumeWs(OCCURS)) {
      occ = ftRange(false);
      if(occ == null) error(FTRANGE);
      wsCheck(TIMES);
    }
    return new FTWords(input(), e, mode, occ);
  }

  /**
   * Parses the "FTRange" rule.
   * @param i accept only integers ("FTLiteralRange")
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr[] ftRange(final boolean i) throws QueryException {
    final Expr[] occ = { Itr.get(1), Itr.get(Long.MAX_VALUE)};
    if(wsConsumeWs(EXACTLY)) {
      occ[0] = ftAdditive(i);
      occ[1] = occ[0];
    } else if(wsConsumeWs(AT)) {
      if(wsConsumeWs(LEAST)) {
        occ[0] = ftAdditive(i);
      } else {
        wsCheck(MOST);
        occ[0] = Itr.get(0);
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
    if(tok.size() == 0) error(INTEXP);
    return Itr.get(toLong(tok.finish()));
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
                else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
              } while(wsConsume(COMMA));
              wsCheck(PAR2);
            } else if(wsConsumeWs(AT)) {
              String fn = string(stringLiteral());
              if(ctx.stop != null) fn = ctx.stop.get(fn);
              final IO fl = io(fn);
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
        if(opt.is(FZ)) error(FTFZWC);
        opt.set(WC, using);
      } else if(wsConsumeWs(FUZZY)) {
        if(opt.isSet(FZ)) error(FTDUP, FUZZY);
        if(opt.is(WC)) error(FTFZWC);
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

    String fn = string(stringLiteral());
    if(ctx.thes != null) fn = ctx.thes.get(fn);
    final IO fl = io(fn);
    final byte[] rel = wsConsumeWs(RELATIONSHIP) ? stringLiteral() : EMPTY;
    final Expr[] range = ftRange(true);
    long min = 0;
    long max = Long.MAX_VALUE;
    if(range != null) {
      wsCheck(LEVELS);
      // values will always be integer instances
      min = ((Itr) range[0]).itr(input());
      max = ((Itr) range[1]).itr(input());
    }
    thes.add(new Thesaurus(fl, rel, min, max, ctx.context));
  }

  /**
   * Parses the "InsertExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr insert() throws QueryException {
    final int p = qp;
    if(!wsConsumeWs(INSERT) || !wsConsumeWs(NODE) && !wsConsumeWs(NODES)) {
      qp = p;
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
    ctx.updating = true;
    return new Insert(input(), s, first, last, before, after, trg);
  }

  /**
   * Parses the "DeleteExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr deletee() throws QueryException {
    final int p = qp;
    if(!wsConsumeWs(DELETE) || !wsConsumeWs(NODES) && !wsConsumeWs(NODE)) {
      qp = p;
      return null;
    }
    ctx.updating = true;
    return new Delete(input(), check(single(), INCOMPLETE));
  }

  /**
   * Parses the "RenameExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr rename() throws QueryException {
    final int p = qp;
    if(!wsConsumeWs(RENAME) || !wsConsumeWs(NODE)) {
      qp = p;
      return null;
    }

    final Expr trg = check(single(), INCOMPLETE);
    wsCheck(AS);
    final Expr n = check(single(), INCOMPLETE);
    ctx.updating = true;
    return new Rename(input(), trg, n);
  }

  /**
   * Parses the "ReplaceExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr replace() throws QueryException {
    final int p = qp;
    if(!wsConsumeWs(REPLACE)) return null;

    final boolean v = wsConsumeWs(VALUEE);
    if(v) {
      wsCheck(OF);
      wsCheck(NODE);
    } else if(!wsConsumeWs(NODE)) {
      qp = p;
      return null;
    }

    final Expr t = check(single(), INCOMPLETE);
    wsCheck(WITH);
    final Expr r = check(single(), INCOMPLETE);
    ctx.updating = true;
    return new Replace(input(), t, r, v);
  }

  /**
   * Parses the "TransformExpr" rule.
   * @return query expression
   * @throws QueryException query exception
   */
  private Expr transform() throws QueryException {
    if(!wsConsumeWs(COPY, DOLLAR, INCOMPLETE)) return null;
    final boolean u = ctx.updating;
    ctx.updating = false;
    final int s = ctx.vars.size();

    Let[] fl = { };
    do {
      final Var v = Var.create(ctx, input(), varName());
      wsCheck(ASSIGN);
      final Expr e = check(single(), INCOMPLETE);
      ctx.vars.add(v);
      fl = Array.add(fl, new Let(input(), e, v));
    } while(wsConsumeWs(COMMA));
    wsCheck(MODIFY);

    final Expr m = check(single(), INCOMPLETE);
    wsCheck(RETURN);
    final Expr r = check(single(), INCOMPLETE);

    ctx.vars.reset(s);
    ctx.updating = u;
    return new Transform(input(), fl, m, r);
  }

  /**
   * Parses the "NCName" rule.
   * @param err optional error message
   * @return string
   * @throws QueryException query exception
   */
  private byte[] ncName(final Err err) throws QueryException {
    skipWS();
    tok.reset();
    if(ncName()) return tok.finish();
    if(err != null) error(err);
    return EMPTY;
  }

  /**
   * Parses the "QName" rule.
   * @param err optional error message. Will be thrown if no QName is found, and
   *          ignored if set to {@code null}
   * @return string
   * @throws QueryException query exception
   */
  private byte[] qName(final Err err) throws QueryException {
    tok.reset();
    final boolean ok = ncName();
    if(ok && consume(':')) ncName2();
    if(!ok && err != null) error(err);
    return tok.finish();
  }

  /**
   * Helper method for parsing NCNames.
   * @return true for success
   */
  private boolean ncName2() {
    char c = curr();
    if(!XMLToken.isNCStartChar(c)) {
      --qp;
      return false;
    }
    tok.add(':');
    do {
      tok.add(consume());
      c = curr();
    } while(XMLToken.isNCChar(c));
    return true;
  }

  /**
   * Helper method for parsing NCNames.
   * @return true for success
   */
  private boolean ncName() {
    if(!XMLToken.isNCStartChar(curr())) return false;
    do {
      tok.add(consume());
    } while(XMLToken.isNCChar(curr()));
    return true;
  }

  /**
   * Parses and converts entities.
   * @param tb token builder
   * @return true if an entity was found
   * @throws QueryException query exception
   */
  private boolean entity(final TokenBuilder tb) throws QueryException {
    final int p = qp;
    final boolean ent = consume('&');
    if(ent) {
      if(consume('#')) {
        final int b = consume('x') ? 16 : 10;
        int n = 0;
        do {
          final char c = curr();
          final boolean m = digit(c);
          final boolean h = b == 16
              && (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');
          if(!m && !h) entityError(p, INVENTITY);
          final long nn = n;
          n = n * b + (consume() & 15);
          if(n < nn) entityError(p, INVCHARREF);
          if(!m) n += 9;
        } while(!consume(';'));
        if(!XMLToken.valid(n)) entityError(p, INVCHARREF);
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
          entityError(p, INVENTITY);
        }
        if(!consume(';')) entityError(p, INVENTITY);
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
    final String sub = query.substring(p, Math.min(p + 20, ql));
    final int sc = sub.indexOf(';');
    final String ent = sc != -1 ? sub.substring(0, sc + 1) : sub;
    error(c, ent);
  }

  /**
   * Returns an IO instance for the specified file, or {@code null}.
   * @param fn filename
   * @return io instance
   */
  private IO io(final String fn) {
    IO fl = IO.get(fn);
    // if file does not exist, try base uri
    if(!fl.exists()) {
      final IO base = ctx.base();
      if(base != null) fl = base.merge(fn);
    }
    // if file does not exist, try query directory
    if(!fl.exists() && file != null) fl = file.merge(fn);
    return fl;
  }

  /**
   * Raises an error if the specified expression is empty.
   * @param <E> expression type
   * @param expr expression
   * @param err error message
   * @return expression
   * @throws QueryException query exception
   */
  private <E extends Expr> E check(final E expr, final Err err)
      throws QueryException {
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
   * Checks if a referenced variable is defined and throws the specified error
   * if not.
   * @param name variable name
   * @param err error to throw
   * @return referenced variable
   * @throws QueryException if the variable isn't defined
   */
  private Var checkVar(final QNm name, final Err err) throws QueryException {
    Var v = ctx.vars.get(name);
    // dynamically assign variables from function modules
    if(v == null && !declVars) {
      declVars = true;
      Variable.init(ctx);
      v = ctx.vars.get(name);
    }
    if(v == null) error(err, '$' + string(name.atom()));
    return v;
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
    final int p = qp;
    if(!wsConsume(t)) return false;
    if(skipWS() || !XMLToken.isNCStartChar(t.charAt(0))
        || !XMLToken.isNCChar(curr())) return true;
    qp = p;
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
    final int p = qp;
    if(!wsConsumeWs(s1)) return false;
    alter = expr;
    ap = qp;
    final int p2 = qp;
    final boolean ok = wsConsume(s2);
    qp = ok ? p2 : p;
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
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c == '(' && next() == ':') {
        comment();
      } else {
        if(c <= 0 || c > ' ') return p != qp;
        ++qp;
      }
    }
    return p != qp;
  }

  /**
   * Consumes a comment.
   * @throws QueryException query exception
   */
  private void comment() throws QueryException {
    ++qp;
    while(++qp < ql) {
      if(curr('(') && next() == ':') comment();
      if(curr(':') && next() == ')') {
        qp += 2;
        return;
      }
    }
    error(COMCLOSE);
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespaces were found
   */
  private boolean consumeWSS() {
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c <= 0 || c > ' ') return p != qp;
      ++qp;
    }
    return true;
  }

  /**
   * Throws the alternative error message.
   * @return never
   * @throws QueryException query exception
   */
  private QueryException error() throws QueryException {
    qp = ap;
    if(alter != FUNCUNKNOWN) throw error(alter);
    ctx.funcs.funError(alterFunc, this);
    throw error(alter, alterFunc.atom());
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param e new expression
   * @return new array
   * @throws QueryException query exception
   */
  private Expr[] add(final Expr[] ar, final Expr e) throws QueryException {
    if(e == null) error(INCOMPLETE);
    final int a = ar.length;
    final Expr[] tmp = new Expr[a + 1];
    System.arraycopy(ar, 0, tmp, 0, a);
    tmp[a] = e;
    return tmp;
  }

  /**
   * Throws the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return never
   * @throws QueryException query exception
   */
  public QueryException error(final Err err, final Object... arg)
      throws QueryException {
    throw err.thrw(input(), arg);
  }
}
