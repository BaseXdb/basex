package org.basex.art;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * A pluggable XQuery engine for {@link MathlingArtTest}. BaseX is the reference implementation;
 * further engines are discovered via {@link java.util.ServiceLoader} and their results compared
 * against it. Implementations need a public no-argument constructor.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public interface XQueryProcessor {
  /**
   * Short engine id, naming the output directory (e.g. {@code basex}).
   * @return the id
   */
  String id();

  /**
   * Display name for reports (e.g. {@code BaseX}).
   * @return the name
   */
  default String name() {
    return id();
  }

  /**
   * Whether this is the reference engine that others are compared against.
   * @return result of check
   */
  default boolean reference() {
    return false;
  }

  /**
   * Whether the engine can run in the current environment.
   * @return result of check
   */
  default boolean available() {
    return true;
  }

  /**
   * Prepares the engine (loaders, processors); called once if {@link #available()}.
   * @throws Exception setup exception
   */
  @SuppressWarnings("unused")
  default void init() throws Exception {
  }

  /**
   * Releases any resources held by the engine.
   * @throws Exception teardown exception
   */
  @SuppressWarnings("unused")
  default void close() throws Exception {
  }

  /**
   * Compiles and evaluates a module, serializing the result to {@code result} and any
   * trace / error output to {@code err}.
   * @param module module path (base URI)
   * @param moduleText module source
   * @param modDir the module's result folder
   * @param result result file to write
   * @param bindings external variable bindings
   * @param err sink for captured trace / error output
   * @return {@code {compile, eval}} times in seconds
   * @throws Exception execution exception
   */
  double[] run(Path module, String moduleText, Path modDir, Path result,
      Map<String, String> bindings, OutputStream err) throws Exception;
}
