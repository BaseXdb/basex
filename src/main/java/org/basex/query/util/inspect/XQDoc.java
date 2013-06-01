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

  /**
   * Constructor.
   * @param ii input info
   * @param qc query context
   */
  public XQDoc(final QueryContext qc, final InputInfo ii) {
    super(qc, ii);
  }

  /**
   * Parses a module and returns an xqdoc element.
   * @param io input reference
   * @return xqdoc element
   * @throws QueryException query exception
   */
  public FElem parse(final IO io) throws QueryException {
    final QueryParser qp = parseQuery(io);
    final FElem xqdoc = new FElem(PREFIX, PREFIX, URI).declareNS();
    final FElem control = elem("control", xqdoc);
    elem("date", control).add(ctx.initDateTime().dtm.string(info));
    elem("version", control).add("1.1");

    final String type = module instanceof LibraryModule ? "library" : "main";
    final FElem modulee = elem("module", xqdoc).add("type", type);
    if(module instanceof LibraryModule) {
      final QNm name = ((LibraryModule) module).name;
      elem("name", modulee).add(name.string());
      elem("uri", modulee).add(name.uri());
    } else {
      elem("uri", modulee).add(io.name());
    }
    comment(module, modulee);

    // imports
    final FElem imports = elem("imports", xqdoc);
    for(final byte[] imp : qp.modules) {
      elem("uri", elem("import", imports).add("type", "library")).add(imp);
    }

    // namespaces
    if(!qp.namespaces.isEmpty()) {
      final FElem namespaces = elem("namespaces", xqdoc);
      for(final byte[] pref : qp.namespaces) {
        final FElem namespace = elem("namespace", namespaces).add("prefix", pref);
        namespace.add("uri", qp.namespaces.get(pref));
      }
    }

    // variables
    final FElem variables = elem("variables", xqdoc);
    for(final StaticVar sv : qp.vars) {
      final FElem variable = elem("variable", variables);
      elem("name", variable).add(sv.name.string());
      comment(sv, variable);
      annotations(sv.ann, variable);
      type(sv.declType, variable);
    }

    // functions
    final FElem functions = elem("functions", xqdoc);
    for(final StaticFunc sf : qp.funcs) {
      final int al = sf.args.length;
      final FElem function = elem("function", functions).add("arity", token(al));
      comment(sf, function);
      elem("name", function).add(sf.name.string());
      annotations(sf.ann, function);

      elem("signature", function).add(sf.toString().replaceAll(" \\{.*| \\w+;.*", ""));
      if(al != 0) {
        final FElem fparameters = elem("parameters", function);
        for(int a = 0; a < al; a++) {
          final FElem fparameter = elem("parameter", fparameters);
          final Var v = sf.args[a];
          elem("name", fparameter).add(v.name.string());
          type(v.declType, fparameter);
        }
      }
      if(sf.declType != null) type(sf.declType, elem("return", function));
    }
    return xqdoc;
  }

  @Override
  protected FElem elem(final String name, final FElem parent) {
    final FElem elem = new FElem(PREFIX, token(name), URI);
    parent.add(elem);
    return elem;
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

  @Override
  protected FElem tag(final byte[] tag, final FElem parent) {
    return eq(tag, DOC_TAGS) ? elem(string(tag), parent) :
      elem("tag", parent).add("name", tag);
  }

  /**
   * Creates annotation elements.
   * @param ann annotations
   * @param parent parent node
   * @throws QueryException query exception
   */
  private void annotations(final Ann ann, final FElem parent) throws QueryException {
    if(ann.size() != 0) annotation(ann, elem("annotations", parent));
  }

  /**
   * Creates a type element.
   * @param st sequence type
   * @param parent parent node
   */
  protected void type(final SeqType st, final FElem parent) {
    if(st == null) return;
    final FElem type = elem("type", parent).add(st.type.toString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) type.add("occurrence", occ);
  }
}
