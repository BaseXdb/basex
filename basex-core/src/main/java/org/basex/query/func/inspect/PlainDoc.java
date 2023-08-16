package org.basex.query.func.inspect;

import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for generating a plain XQuery documentation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class PlainDoc extends Inspect {
  /**
   * Constructor.
   * @param qc query context
   * @param info input info
   */
  PlainDoc(final QueryContext qc, final InputInfo info) {
    super(qc, info);
  }

  /**
   * Parses a module and returns an inspection element.
   * @return inspection element
   * @throws QueryException query exception
   */
  FNode context() throws QueryException {
    final FBuilder root = element("context");
    for(final StaticVar sv : qc.vars) root.add(variable(sv));
    for(final StaticFunc sf : qc.functions.funcs()) {
      root.add(function(sf.name, sf, sf.funcType(), sf.anns));
    }
    return root.finish();
  }

  @Override
  public FNode parse(final IOContent content) throws QueryException {
    final AModule module = parseModule(content);
    final FBuilder root = element("module");
    if(module instanceof LibraryModule) {
      final QNm name = module.sc.module;
      root.add(Q_PREFIX, name.string()).add(Q_URI, name.uri());
    }

    final TokenObjMap<TokenList> doc = module.doc();
    if(doc != null) comment(doc, root);
    for(final StaticVar sv : module.vars) root.add(variable(sv));
    for(final StaticFunc sf : module.funcs) root.add(function(sf.name, sf, sf.funcType(), sf.anns));
    return root.finish();
  }

  /**
   * Creates a description for the specified variable.
   * @param sv static variable
   * @return resulting value
   * @throws QueryException query exception
   */
  private FNode variable(final StaticVar sv) throws QueryException {
    final FBuilder variable = element("variable");
    final byte[] name = sv.name.string(), uri = sv.name.uri();
    variable.add(Q_NAME, name);
    if(uri.length != 0) variable.add(Q_URI, uri);
    type(sv.seqType(), variable);
    variable.add(Q_EXTERNAL, sv.external);

    comment(sv, variable);
    annotation(sv.anns, variable, true);
    return variable.finish();
  }

  /**
   * Creates a description for the specified function.
   * @param fname name of function
   * @param sf function reference (can be {@code null})
   * @param ft function type
   * @param anns annotations
   * @return resulting value
   * @throws QueryException query exception
   */
  FNode function(final QNm fname, final StaticFunc sf, final FuncType ft, final AnnList anns)
      throws QueryException {

    final FBuilder function = element("function");
    if(fname != null) {
      function.add(Q_NAME, fname.string());
      if(fname.uri().length != 0) function.add(Q_URI, fname.uri());
    }
    function.add(Q_EXTERNAL, sf != null && sf.expr == null);

    final TokenObjMap<TokenList> doc = sf != null ? sf.doc() : null;
    final int al = ft.argTypes.length;
    QNm[] names = null;
    if(sf != null) {
      names = new QNm[al];
      for(int n = 0; n < al; n++) names[n] = sf.params[n].name;
    }

    for(int a = 0; a < al; a++) {
      final FBuilder argument = element("argument");
      if(names != null) {
        final byte[] name = names[a].string(), uri = names[a].uri(), pdoc = doc(doc, name);
        argument.add(Q_NAME, name);
        if(uri.length != 0) argument.add(Q_URI, uri);

        if(pdoc != null) add(pdoc, argument);
      }
      type(ft.argTypes[a], argument);
      function.add(argument);
    }

    annotation(anns, function, true);

    if(doc != null) {
      for(final byte[] key : doc) {
        if(eq(key, DOC_PARAM, DOC_RETURN)) continue;
        for(final byte[] value : doc.get(key)) {
          final FBuilder elem = eq(key, DOC_TAGS) ? element(string(key)) :
            element("tag").add(Q_NAME, key);
          add(value, elem);
          function.add(elem);
        }
      }
    }

    final FBuilder rtrn = element("return");
    type(ft.declType, rtrn);
    final TokenList returns = doc != null ? doc.get(DOC_RETURN) : null;
    if(returns != null) {
      for(final byte[] val : returns) add(val, rtrn);
    }
    return function.add(rtrn).finish();
  }

  @Override
  protected FBuilder element(final String name) {
    return FElem.build(new QNm(name));
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   * @throws QueryException query exception
   */
  private void comment(final StaticScope scope, final FBuilder parent) throws QueryException {
    final TokenObjMap<TokenList> tags = scope.doc();
    if(tags != null) comment(tags, parent);
  }

  @Override
  protected FBuilder element(final byte[] tag) {
    final String string = string(tag);
    return element(eq(tag, DOC_TAGS) ? string : string + "_tag");
  }

  /**
   * Attaches type information to the specified element.
   * @param st sequence type
   * @param elem element
   */
  private static void type(final SeqType st, final FBuilder elem) {
    if(st != null) {
      elem.add(Q_TYPE, st.typeString());
      final String occ = st.occ.toString();
      if(!occ.isEmpty()) elem.add(Q_OCCURRENCE, occ);
    }
  }
}
