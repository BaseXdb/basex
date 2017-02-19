package org.basex.gui.view.project;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.WatchEvent.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Project file watcher.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class ProjectWatcher extends Thread {
  /** Project view. */
  private final ProjectView view;
  /** Watch service. */
  private WatchService watcher;

  /**
   * Constructor.
   * @param root root directory
   * @param view project view
   */
  ProjectWatcher(final IOFile root, final ProjectView view) {
    this.view = view;
    try {
      watcher = FileSystems.getDefault().newWatchService();
      register(root);
      setDaemon(true);
      start();
    } catch(final IOException ex) {
      close();
    }
  }

  @Override
  public void run() {
    try {
      while(true) {
        final WatchKey key = watcher.take();
        final Watchable dir = key.watchable();
        for(final WatchEvent<?> event : key.pollEvents()) {
          final Object context = event.context();
          if(dir instanceof Path && context instanceof Path) {
            final IOFile child = new IOFile(((Path) dir).resolve((Path) context).toFile());
            final Kind<?> kind = event.kind();
            if(kind == ENTRY_CREATE) register(child);
            view.refreshTree(child, kind != ENTRY_MODIFY);
          }
        }
        key.reset();
      }
    } catch(final ClosedWatchServiceException ex) {
      // watcher has been closed
      Util.debug(ex);
    } catch(final Exception ex) {
      Util.errln(ex);
    }
  }

  /**
   * Register the given directory, and all its sub-directories, with the WatchService.
   * @param path directory path
   * @throws IOException I/O exception
   */
  private void register(final IOFile path) throws IOException {
    if(path.isDir()) {
      path.toPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      for(final IOFile child : path.children()) register(child);
    }
  }

  /**
   * Closes the watcher.
   */
  void close() {
    if(watcher == null) return;
    try {
      watcher.close();
      watcher = null;
    } catch(final IOException ex) {
      Util.errln(ex);
    }
  }
}
