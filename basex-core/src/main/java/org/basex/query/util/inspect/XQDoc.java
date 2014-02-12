package org.basex.query.util.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class XQDoc extends Inspect {
  /** Namespace uri. */
  private static final byte[] URI = token("http://www.xqdoc.org/1.0");
  /** Prefix. */
  private static final byte[] PREFIX = token("xqdoc");

  /** Namespaces. */
  private final TokenMap nsCache = new TokenMap();

  /**
   * Constructor.
   * @param qc query context
   * @param ii input info
   */
  public XQDoc(final QueryContext qc, final InputInfo ii) {
    super(qc, ii);
  }

  @Override
  public FElem parse(final IO io) throws QueryException {
    final QueryParser qp = parseQuery(io);
    final FElem xqdoc = new FElem(PREFIX, PREFIX, URI).declareNS();
    final FElem control = elem("control", xqdoc);
    elem("date", control).add(ctx.initDateTime().dtm.string(info));
    elem("version", control).add("1.1");

    final String type = module instanceof LibraryModule ? "library" : "main";
    final FElem mod = elem("module", xqdoc).add("type", type);
    if(module instanceof LibraryModule) {
      final QNm name = ((LibraryModule) module).name;
      elem("uri", mod).add(name.uri());
      elem("name", mod).add(io.name());
    } else {
      elem("uri", mod).add(io.name());
    }
    comment(module, mod);

    // namespaces
    final FElem namespaces = elem("namespaces", xqdoc);
    for(final byte[] pref : qp.namespaces) nsCache.put(pref, qp.namespaces.get(pref));

    // imports
    final FElem imports = elem("imports", xqdoc);
    for(final byte[] imp : qp.modules) {
      elem("uri", elem("import", imports).add("type", "library")).add(imp);
    }

    // variables
    final FElem variables = elem("variables", xqdoc);
    for(final StaticVar sv : qp.vars) {
      final FElem variable = elem("variable", variables);
      elem("name", variable).add(sv.name.string());
      if(sv.name.hasPrefix()) nsCache.put(sv.name.prefix(), sv.name.uri());
      comment(sv, variable);
      annotations(sv.ann, variable);
      type(sv.type(), variable);
    }

    // functions
    final FElem functions = elem("functions", xqdoc);
    for(final StaticFunc sf : qp.funcs) {
      final int al = sf.arity();
      final QNm name = sf.funcName();
      final FuncType t = sf.funcType();
      final FElem function = elem("function", functions).add("arity", token(al));
      comment(sf, function);
      elem("name", function).add(name.string());
      if(name.hasPrefix()) nsCache.put(name.prefix(), name.uri());
      annotations(sf.ann, function);

      final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(sf.ann);
      tb.add(FUNCTION).add(' ').add(name.string()).add(PAR1);
      for(int i = 0; i < al; i++) {
        final Var v = sf.args[i];
        if(i > 0) tb.add(SEP);
        tb.add(DOLLAR).add(v.name.string()).add(' ').add(AS).add(' ').addExt(t.args[i]);
      }
      tb.add(PAR2).add(' ' + AS + ' ' + t.ret);
      if(sf.expr == null) tb.add(" external");

      elem("signature", function).add(tb.toString());
      if(al != 0) {
        final FElem fparameters = elem("parameters", function);
        for(int a = 0; a < al; a++) {
          final FElem fparameter = elem("parameter", fparameters);
          final Var v = sf.args[a];
          elem("name", fparameter).add(v.name.string());
          type(t.args[a], fparameter);
        }
      }
      type(sf.type(), elem("return", function));
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
  protected FElem tag(final byte[] tag, final FElem parent) {
    return eq(tag, DOC_TAGS) ? elem(string(tag), parent) :
      elem("custom", parent).add("tag", tag);
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
   * @param ann annotations
   * @param parent parent node
   * @throws QueryException query exception
   */
  private void annotations(final Ann ann, final FElem parent) throws QueryException {
    if(!ann.isEmpty()) annotation(ann, elem("annotations", parent), false);
    for(int a = 0; a < ann.size(); a++) {
      final QNm name = ann.names[a];
      if(name.hasPrefix()) nsCache.put(name.prefix(), name.uri());
    }
  }

  /**
   * Creates a type element.
   * @param st sequence type
   * @param parent parent node
   */
  void type(final SeqType st, final FElem parent) {
    if(st == null) return;
    final FElem type = elem("type", parent).add(st.typeString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) type.add("occurrence", occ);
  }
}
