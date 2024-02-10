package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.function.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Parses the package descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Rositsa Shadura
 */
public final class PkgParser {
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public PkgParser(final InputInfo info) {
    this.info = info;
  }

  /**
   * Parses package descriptor.
   * @param io XML input
   * @return package container
   * @throws QueryException query exception
   */
  public Pkg parse(final IO io) throws QueryException {
    final ANode node;
    try {
      // checks root node
      node = childElements(new DBNode(io)).next();
      if(!eqNS(E_PACKAGE, node.qname()))
        throw REPO_DESCRIPTOR_X.get(info, Util.info(WHICHELEM, node.qname()));
    } catch(final IOException ex) {
      throw REPO_PARSE_X_X.get(info, io.name(), ex);
    }

    final QueryFunction<QNm, String> attribute = name -> {
      final byte[] value = node.attribute(name);
      if(value == null) throw REPO_DESCRIPTOR_X.get(info, Util.info(MISSATTR, name, E_PACKAGE));
      return string(value);
    };
    final Pkg pkg = new Pkg(attribute.apply(Q_NAME));
    pkg.abbrev = attribute.apply(Q_ABBREV);
    pkg.spec = attribute.apply(Q_SPEC);
    pkg.version = attribute.apply(Q_VERSION);

    parseChildren(node, pkg);
    return pkg;
  }

  /**
   * Parses the children of <package/>.
   * @param node package node
   * @param pkg package container
   * @throws QueryException query exception
   */
  private void parseChildren(final ANode node, final Pkg pkg) throws QueryException {
    final BasicNodeIter iter = childElements(node);
    for(ANode next; (next = iter.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(E_DEPENDENCY, name)) pkg.dep.add(parseDependency(next));
      else if(eqNS(E_XQUERY, name)) pkg.comps.add(parseComp(next));
    }
  }

  /**
   * Parses <dependency/>.
   * @param node node <dependency/> to be parsed
   * @return dependency container
   */
  private static PkgDep parseDependency(final ANode node) {
    final Function<QNm, String> attribute = name -> {
      final byte[] value = node.attribute(name);
      return value == null ? null : string(value);
    };

    final PkgDep dep = new PkgDep(attribute.apply(Q_PACKAGE));
    dep.processor = attribute.apply(Q_PROCESSOR);
    dep.versions = attribute.apply(Q_VERSIONS);
    dep.semver = attribute.apply(Q_SEMVER);
    dep.semverMin = attribute.apply(Q_SEMVER_MIN);
    dep.semverMax = attribute.apply(Q_SEMVER_MAX);
    return dep;
  }

  /**
   * Parses <xquery/>.
   * @param node xquery component
   * @return component container
   * @throws QueryException query exception
   */
  private PkgComponent parseComp(final ANode node) throws QueryException {
    final BasicNodeIter iter = childElements(node);
    final PkgComponent comp = new PkgComponent();
    for(ANode next; (next = iter.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(A_NAMESPACE, name)) comp.uri = string(next.string());
      else if(eqNS(A_FILE, name)) comp.file = string(next.string());
      else throw REPO_DESCRIPTOR_X.get(info, Util.info(WHICHELEM, name));
    }

    // check mandatory children
    if(comp.uri == null) throw REPO_DESCRIPTOR_X.get(info, Util.info(MISSCOMP, A_NAMESPACE));
    if(comp.file == null) throw REPO_DESCRIPTOR_X.get(info, Util.info(MISSCOMP, A_FILE));
    return comp;
  }

  /**
   * Returns an iterator on all child elements
   * (text and other nodes will be skipped).
   * @param node root node
   * @return child element iterator
   */
  private static BasicNodeIter childElements(final ANode node) {
    return new BasicNodeIter() {
      final BasicNodeIter iter = node.childIter();

      @Override
      public ANode next() {
        while(true) {
          final ANode nd = iter.next();
          if(nd == null || nd.type == NodeType.ELEMENT) return nd;
        }
      }
    };
  }

  /**
   * Checks if the specified name equals the qname and if it uses the packaging
   * namespace.
   * @param cmp input
   * @param name name to be compared
   * @return result of check
   */
  private static boolean eqNS(final byte[] cmp, final QNm name) {
    return name.eq(new QNm(cmp, QueryText.PKG_URI));
  }
}
