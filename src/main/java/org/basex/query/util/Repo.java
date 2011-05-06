package org.basex.query.util;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodeMore;
import org.basex.query.util.Package.Component;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;

/**
 * Repository.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {

  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";
  /** Platform dependent file separator. */
  private static final String SEP = System.getProperty("file.separator");

  /**
   * Map containing namespaces available in the repository and the packages in
   * which they are found.
   */
  public final TokenObjMap<TokenList> nsDict;
  /** Map containing installed packages and their directories. */
  public final TokenMap pkgDict;
  /** Context properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param p context properties
   */
  public Repo(final Prop p) {
    nsDict = new TokenObjMap<TokenList>();
    pkgDict = new TokenMap();
    this.prop = p;
    try {
      readRepo();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Reads the contents of the package repository.
   * @throws IOException IO exception
   */
  private void readRepo() throws IOException {
    final File repoDir = new File(prop.get(Prop.REPOPATH));
    if(repoDir.exists()) {
      final File[] pkgDirs = repoDir.listFiles();
      for(int i = 0; i < pkgDirs.length; i++) {
        if(pkgDirs[i].isDirectory()) {
          final File pkgDesc = new File(pkgDirs[i].getPath() + SEP + PKGDESC);
          if(!pkgDesc.exists()) {
            // TODO: Error: Missing package descriptor.
          } else {
            final IOFile io = new IOFile(pkgDesc);
            final Parser p = Parser.xmlParser(io, prop, "");
            final ANode pkgNode =
              new DBNode(MemBuilder.build(p, prop, ""), 0).children().next();
            final Package pkg = PackageParser.parse(pkgNode);
            // Read package components
            final Iterator<Component> compIt = pkg.comps.iterator();
            Component comp;
            byte[] compNs;
            while(compIt.hasNext()) {
              comp = compIt.next();
              compNs = comp.namespace;
              if(compNs != null) {
                if(nsDict.get(compNs) != null) {
                  nsDict.get(compNs).add(pkg.name);
                } else {
                  final TokenList vals = new TokenList();
                  vals.add(pkg.name);
                  nsDict.add(compNs, vals);
                }
              }
            }
            pkgDict.add(pkg.getName(), pkg.abbrev);
          }
        }
      }
    }
  }
}
