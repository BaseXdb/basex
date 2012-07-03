package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'export' command and saves the currently opened database
 * to disk.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Export extends Command {
  /** Currently exported file. */
  private IO progFile;
  /** Current number of exported file. */
  private int progPos;
  /** Total number of files to be exported. */
  private int progSize;

  /**
   * Default constructor, specifying a target path.
   * @param path export path
   */
  public Export(final String path) {
    super(Perm.CREATE, true, path);
  }

  @Override
  protected boolean run() {
    try {
      final Data data = context.data();
      export(data, args[0], this);
      return info(DB_EXPORTED_X, data.meta.name, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  protected boolean databases(final StringList db) {
    db.add("");
    return true;
  }

  /**
   * Exports the current database to the specified path.
   * Files and directories in {@code path} will be possibly overwritten.
   * @param data data reference
   * @param target directory
   * @param e calling instance
   * @throws IOException I/O exception
   */
  public static void export(final Data data, final String target,
      final Export e) throws IOException {

    final String exp = data.meta.prop.get(Prop.EXPORTER);
    final SerializerProp sp = new SerializerProp(exp);
    final IOFile root = new IOFile(target);
    root.md();

    final HashSet<String> exported = new HashSet<String>();

    // XML documents
    final IntList il = data.resources.docs();
    // raw files
    final IOFile bin;
    final StringList desc;
    if(data.inMemory()) {
      bin = null;
      desc = new StringList();
    } else {
      bin = data.meta.binaries();
      desc = bin.descendants();
    }

    if(e != null) {
      e.progPos = 0;
      e.progSize = il.size() + desc.size();
    }

    // XML documents
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      // create file path
      final IO f = root.merge(Token.string(data.text(pre, true)));
      if(e != null) {
        e.checkStop();
        e.progFile = f;
        e.progPos++;
      }
      // create dir if necessary
      final IOFile dir = new IOFile(f.dir());
      if(!dir.exists()) dir.md();

      // serialize file
      final PrintOutput po = new PrintOutput(unique(exported, f.path()));
      final Serializer ser = Serializer.get(po, sp);
      ser.serialize(new DBNode(data, pre));
      ser.close();
      po.close();
    }

    // export raw files
    for(final String s : desc) {
      final IOFile f = new IOFile(root.path(), s);
      if(e != null) {
        e.checkStop();
        e.progFile = f;
        e.progPos++;
      }
      final String u = unique(exported, f.path());
      new IOFile(bin, s).copyTo(new IOFile(u));
    }
  }

  @Override
  public double prog() {
    return progSize == 0 ? 0 : (double) progPos / progSize;
  }

  @Override
  public boolean stoppable() {
    return true;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public String det() {
    return progFile == null ? EXPORT : progFile.path();
  }

  /**
   * Returns a unique file path.
   * @param exp exported names
   * @param fp file path
   * @return unique path
   */
  private static String unique(final HashSet<String> exp, final String fp) {
    int c = 1;
    String path = fp;
    while(exp.contains(path)) {
      path = fp.indexOf('.') == -1 ? fp + '(' + ++c + ')' :
           fp.replaceAll("(.*)\\.(.*)", "$1(" + ++c + ").$2");
    }
    exp.add(path);
    return path;
  }
}
