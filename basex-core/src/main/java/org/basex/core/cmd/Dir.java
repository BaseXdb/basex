package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'dir' command and returns a directory representation of resources of the
 * currently opened database.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Dir extends Command {
  /**
   * Default constructor.
   * @param path database path (can be {@code null})
   */
  public Dir(final String path) {
    super(Perm.READ, true, path != null ? path : "");
  }

  @Override
  protected boolean run() throws IOException {
    String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);
    if(!path.isEmpty() && !Strings.endsWith(path, '/')) path += '/';

    final Table table = new Table();
    table.description = ENTRIES_X;
    table.header.add(INPUT_PATH).add(TYPE).add(DataText.CONTENT_TYPE).add(SIZE);
    final ArrayList<TokenList> contents = table.contents;

    final Data data = context.data();
    final HashSet<String> set = new HashSet<>();
    final Resources resources = data.resources;

    // list XML resources
    final IntList docs = resources.docs(path, true);
    final long ds = docs.size();
    for(int d = 0; d < ds; d++) {
      final int pre = docs.get(d);
      String name = string(substring(data.text(pre, true), path.length()));
      final int i = name.indexOf('/');
      final boolean dir = i >= 0;
      if(dir) name = name.substring(0, i);
      if(set.add(name)) contents.add(entry(dir, name, data.size(pre, Data.DOC), ResourceType.XML));
    }
    // list file resources
    if(!data.inMemory()) {
      for(final ResourceType type : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, type);
        for(final IOFile file : bin.children()) {
          final boolean dir = file.isDir();
          final String name = dir ? file.name() : type.dbPath(file.name());
          if(set.add(name)) contents.add(entry(dir, name, file.length(), type));
        }
      }
    }
    out.println(table.sort().finish());
    return true;
  }

  /**
   * Creates a table entry.
   * @param dir directory flag
   * @param name name of resource
   * @param size file size (can be {@code null})
   * @param type resource type
   * @return table entry
   */
  private TokenList entry(final boolean dir, final String name, final long size,
      final ResourceType type) {
    final TokenList tl = new TokenList(4).add(name);
    if(dir) {
      tl.add(S_DIR).add("").add("");
    } else {
      tl.add(type.toString()).add(type.contentType(name).toString()).add(size);
    }
    return tl;
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }
}
