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
 * @author BaseX Team 2005-14, BSD License
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
      return error(Util.message(ex));
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }

  /**
   * Exports the current database to the specified path.
   * Files and directories in {@code path} will be possibly overwritten.
   * @param data data reference
   * @param path directory
   * @param export calling instance
   * @throws IOException I/O exception
   */
  public static void export(final Data data, final String path, final Export export)
      throws IOException {
    export(data, path, data.meta.options.get(MainOptions.EXPORTER), export);
  }

  /**
   * Exports the current database to the specified path.
   * Files and directories in {@code path} will be possibly overwritten.
   * @param data data reference
   * @param path directory
   * @param sopts serialization parameters
   * @param export calling instance
   * @throws IOException I/O exception
   */
  public static void export(final Data data, final String path, final SerializerOptions sopts,
      final Export export) throws IOException {

    final IOFile root = new IOFile(path);
    root.md();

    final HashSet<String> exported = new HashSet<>();

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

    if(export != null) {
      export.progPos = 0;
      export.progSize = il.size() + desc.size();
    }

    // XML documents
    final int is = il.size();
    for(int i = 0; i < is; i++) {
      final int pre = il.get(i);
      // create file path
      final IOFile fl = root.resolve(Token.string(data.text(pre, true)));
      if(export != null) {
        export.checkStop();
        export.progFile = fl;
        export.progPos++;
      }
      // create dir if necessary
      fl.parent().md();

      // serialize file
      try(final PrintOutput po = new PrintOutput(unique(exported, fl.path()))) {
        final Serializer ser = Serializer.get(po, sopts);
        ser.serialize(new DBNode(data, pre));
        ser.close();
      }
    }

    // export raw files
    for(final String s : desc) {
      final IOFile fl = new IOFile(root.path(), s);
      if(export != null) {
        export.checkStop();
        export.progFile = fl;
        export.progPos++;
      }
      final String u = unique(exported, fl.path());
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
