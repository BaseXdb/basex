package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
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
 * @author BaseX Team 2005-21, BSD License
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
      export(data, args[0], options, this);
      return info(DB_EXPORTED_X, data.meta.name, jc().performance);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  /**
   * Exports the current database to the specified path.
   * Files and directories in {@code path} will be possibly overwritten.
   * @param data data reference
   * @param path directory
   * @param options main options
   * @param export calling instance
   * @throws IOException I/O exception
   */
  public static void export(final Data data, final String path, final MainOptions options,
      final Export export) throws IOException {
    export(data, path, options.get(MainOptions.EXPORTER), export);
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

    // XML documents
    final IntList docs = data.resources.docs();
    // raw files
    final IOFile source;
    final StringList files;
    if(data.inMemory()) {
      source = null;
      files = new StringList();
    } else {
      source = data.meta.binaryDir();
      files = source.descendants();
    }

    if(export != null) {
      export.progPos = 0;
      export.progSize = docs.size() + files.size();
    }

    // XML documents
    final HashSet<String> target = new HashSet<>();
    final int is = docs.size();
    for(int i = 0; i < is; i++) {
      final int pre = docs.get(i);
      // create file path
      final IOFile io = root.resolve(Token.string(data.text(pre, true)));
      if(export != null) {
        export.checkStop();
        export.progFile = io;
      }
      // create dir if necessary
      io.parent().md();

      // serialize file
      try(PrintOutput po = new PrintOutput(unique(target, io.path()))) {
        try(Serializer ser = Serializer.get(po, sopts)) {
          ser.serialize(new DBNode(data, pre));
        }
      }

      if(export != null) export.progPos++;
    }

    // export raw files
    for(final String file : files) {
      final IOFile io = new IOFile(root.path(), file);
      if(export != null) {
        export.checkStop();
        export.progFile = io;
      }
      new IOFile(source, file).copyTo(unique(target, io.path()));

      if(export != null) export.progPos++;
    }
  }

  @Override
  public double progressInfo() {
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
  public String shortInfo() {
    return EXPORT + DOTS;
  }

  @Override
  public String detailedInfo() {
    return progFile == null ? EXPORT : "(" + progPos + '/' + progSize + "): " + progFile;
  }

  /**
   * Returns a unique file path.
   * @param exp exported names
   * @param file file path
   * @return unique path
   */
  private static IOFile unique(final HashSet<String> exp, final String file) {
    int c = 1;
    String path = file;
    while(exp.contains(path)) {
      path = file.indexOf('.') == -1 ? file + '(' + ++c + ')' :
           file.replaceAll("(.*)\\.(.*)", "$1(" + ++c + ").$2");
    }
    exp.add(path);
    return new IOFile(path);
  }
}
