package org.basex.query.func.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryText.DOLLAR;
import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for generating a xqDoc documentation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class XQDoc extends Inspect {
  /** Namespace uri. */
  private static final byte[] URI = token("http://www.xqdoc.org/1.0");
  /** Prefix. */
  private static final byte[] PREFIX = token("xqdoc");

  /** Namespaces. */
  private final TokenMap nsCache = new TokenMap();

  /**
   * Constructor.
   * @param qc query context
   * @param info input info
   */
  XQDoc(final QueryContext qc, final InputInfo info) {
    super(qc, info);
  }

  @Override
  public FElem parse(final IO io) throws QueryException {
    final QueryParser qp = parseQuery(io);
    final FElem xqdoc = new FElem(PREFIX, PREFIX, URI).declareNS();
    final FElem control = elem("control", xqdoc);
    elem("date", control).add(qc.dateTime().datm.string(info));
    elem("version", control).add("1.1");

    final String type = module instanceof LibraryModule ? "library" : "main";
    final FElem mod = elem("module", xqdoc).add("type", type);
    if(module instanceof LibraryModule) {
      final QNm name = module.sc.module;
      elem("uri", mod).add(name.uri());
      elem("name", mod).add(io.name());
    } else {
      elem("uri", mod).add(io.name());
    }
    comment(module, mod);

    // imports
    final FElem imports = elem("imports", xqdoc);
    for(final byte[] imp : qp.modules) {
      elem("uri", elem("import", imports).add("type", "library")).add(imp);
    }

    // namespaces
    final FElem namespaces = elem("namespaces", xqdoc);
    for(final byte[] pref : qp.namespaces) nsCache.put(pref, qp.namespaces.get(pref));

    // variables
    final FElem variables = elem("variables", xqdoc);
    for(final StaticVar sv : module.vars().values()) {
      final FElem variable = elem("variable", variables);
      elem("name", variable).add(sv.name.string());
      if(sv.name.hasPrefix()) nsCache.put(sv.name.prefix(), sv.name.uri());
      comment(sv, variable);
      annotations(sv.anns, variable);
      type(sv.seqType(), variable);
    }

    // functions
    final FElem functions = elem("functions", xqdoc);
    for(final StaticFunc sf : module.funcs().values()) {
      final int al = sf.arity();
      final QNm name = sf.funcName();
      final FuncType tp = sf.funcType();
      final FElem function = elem("function", functions).add("arity", token(al));
      comment(sf, function);
      elem("name", function).add(name.string());
      if(name.hasPrefix()) nsCache.put(name.prefix(), name.uri());
      annotations(sf.anns, function);

      final QueryString qs = new QueryString();
      qs.token(DECLARE).token(sf.anns).token(FUNCTION).token(name.string()).token('(');
      for(int i = 0; i < al; i++) {
        final Var var = sf.params[i];
        if(i > 0) qs.token(SEP);
        qs.concat(DOLLAR, var.name.string()).token(AS).token(tp.argTypes[i]);
      }
      qs.token(')').token(AS).token(tp.declType);
      if(sf.expr == null) qs.token("external");

      elem("signature", function).add(qs.toString());
      if(al != 0) {
        final FElem fparameters = elem("parameters", function);
        for(int a = 0; a < al; a++) {
          final FElem fparameter = elem("parameter", fparameters);
          final Var var = sf.params[a];
          elem("name", fparameter).add(var.name.string());
          type(tp.argTypes[a], fparameter);
        }
      }
      type(sf.seqType(), elem("return", function));
    }

    // add namespaces
    for(final byte[] pref : nsCache) {
      final FElem namespace = elem("namespace", namespaces);
      namespace.add("prefix", pref).add("uri", nsCache.get(pref));
    }

    return xqdoc;
  }

  @Override
  protected FElem elem(final String name, final FElem parent) {
    final FElem elem = new FElem(PREFIX, token(name), URI);
    parent.add(elem);
    return elem;
  }

  @Override
  protected FElem elem(final byte[] name, final FElem parent) {
    return eq(name, DOC_TAGS) ? elem(string(name), parent) :
      elem("custom", parent).add("tag", name);
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   */
  private void comment(final StaticScope scope, final FElem parent) {
    final TokenObjMap<TokenList> map = scope.doc();
    if(map != null) comment(map, elem("comment", parent));
  }

  /**
   * Creates annotation elements.
   * @param anns annotations
   * @param parent parent node
   * @throws QueryException query exception
   */
  private void annotations(final AnnList anns, final FElem parent) throws QueryException {
    if(!anns.isEmpty()) annotation(anns, elem("annotations", parent), false);
    for(final Ann ann : anns) {
      final byte[] uri = ann.name().uri();
      if(uri.length > 0) nsCache.put(NSGlobal.prefix(uri), uri);
    }
  }

  /**
   * Creates a type element.
   * @param st sequence type
   * @param parent parent node
   */
  private void type(final SeqType st, final FElem parent) {
    if(st == null) return;
    final FElem type = elem("type", parent).add(st.typeString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) type.add("occurrence", occ);
  }
}
