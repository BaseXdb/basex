package org.basex.gui.view.project;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.WatchEvent.*;
import java.util.*;
import java.util.Map.*;
import java.util.Timer;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Project file watcher.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class ProjectWatcher extends Thread {
  /** Paths to be refreshed (with reset flag). */
  private final Map<String, Boolean> queue = new HashMap<>();
  /** Project view. */
  private final ProjectView view;
  /** Timer. */
  private final Timer timer = new Timer(true);
  /** Watch service. */
  private WatchService watcher;
  /** Timer. */
  private TimerTask task;

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
            schedule(child, kind != ENTRY_MODIFY);
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
   * Closes the watcher.
   */
  void close() {
    if(watcher == null) return;
    try {
      timer.cancel();
      watcher.close();
      watcher = null;
    } catch(final IOException ex) {
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
   * Creates a new timer task.
   * @param file file to be refreshed
   * @param reset reset flag
   */
  private void schedule(final IOFile file, final boolean reset) {
    if(task != null) task.cancel();

    // register new path: create new map without redundant child paths
    final HashMap<String, Boolean> map = new HashMap<>();
    // append slash to avoid that prefix path check does not confuse files and directories
    final String path = file.path() + (file.isDir() ? "/" : "");
    synchronized(queue) {
      map.put(path, reset);
      for(final Entry<String, Boolean> entry : queue.entrySet()) {
        final String key = entry.getKey();
        final boolean value = entry.getValue();
        if(key.startsWith(path)) {
          map.put(path, reset || value || (map.containsKey(path) && map.get(path)));
        } else {
          map.put(key, value);
        }
      }
      queue.clear();
      queue.putAll(map);
    }

    task = new TimerTask() {
      @Override
      public void run() {
        for(final Entry<String, Boolean> entry : map.entrySet()) {
          view.refreshTree(new IOFile(entry.getKey()), entry.getValue());
          synchronized(queue) {
            queue.remove(entry.getKey());
          }
        }
      }
    };
    timer.schedule(task, 50);
  }
}
