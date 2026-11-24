package org.basex.query;

import java.util.*;
import java.util.concurrent.*;

import org.basex.io.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.parse.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Registry of parsed and pre-declared modules.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ModuleRegistry {
  /** URIs of parsed modules, indexed by module path. */
  private final TokenObjectMap<byte[]> parsed = new TokenObjectMap<>();
  /** Library modules, indexed by module path. */
  private final TokenObjectMap<LibraryModule> libraries = new TokenObjectMap<>();
  /** Type declarations of all parsed modules. */
  private final ArrayList<TypeDecl> types = new ArrayList<>();
  /** Public type declarations of library modules, indexed by type name. */
  private final QNmMap<TypeDecl> publicTypes = new QNmMap<>();
  /** Pre-declared module files, indexed by module URI. */
  private final TokenObjectMap<TokenList> declared = new TokenObjectMap<>();
  /** Normalized module paths, indexed by file path. */
  private Map<String, String> realPaths;

  /** Custom URI resolver for parsed modules (can be {@code null}). */
  public UriResolver resolver;
  /** Indicates if the root library module is supplied as string, without location. */
  public boolean inlineModule;

  /**
   * Constructor.
   * @param parent modules of the parent context (can be {@code null})
   */
  ModuleRegistry(final ModuleRegistry parent) {
    realPaths = parent != null ? parent.realPaths : new ConcurrentHashMap<>();
    resolver = parent != null ? parent.resolver : null;
  }

  /**
   * Returns the path that identifies a module, resolving capitalization and symbolic links.
   * @param io module reference (can be {@code null})
   * @return path
   */
  public byte[] path(final IO io) {
    return Token.token(io instanceof final IOFile file ?
      realPaths.computeIfAbsent(file.path(), p -> file.normalize().path()) :
      io == null ? "" : io.path());
  }

  /**
   * Assigns an external cache for normalized module paths.
   * @param paths normalized module paths, indexed by file path
   */
  public void realPaths(final Map<String, String> paths) {
    realPaths = paths;
  }

  /**
   * Registers a parsed module.
   * @param path module path
   * @param uri module URI
   */
  public void uri(final byte[] path, final byte[] uri) {
    parsed.put(path, uri);
  }

  /**
   * Returns the URI of a parsed module.
   * @param path module path
   * @return module URI (can be {@code null})
   */
  public byte[] uri(final byte[] path) {
    return parsed.get(path);
  }

  /**
   * Returns the URI of the parsed module with the specified index.
   * @param index index of the module (starts with {@code 1})
   * @return module URI
   */
  public byte[] uri(final int index) {
    return parsed.value(index);
  }

  /**
   * Returns the number of parsed modules.
   * @return number of modules
   */
  public int size() {
    return parsed.size();
  }

  /**
   * Returns the paths of all parsed modules.
   * @return module paths
   */
  public Iterable<byte[]> paths() {
    return parsed;
  }

  /**
   * Registers a library module.
   * @param path module path
   * @param library library module
   */
  public void library(final byte[] path, final LibraryModule library) {
    libraries.put(path, library);
  }

  /**
   * Returns a library module.
   * @param path module path
   * @return library module (can be {@code null})
   */
  public LibraryModule library(final byte[] path) {
    return libraries.get(path);
  }

  /**
   * Returns all library modules.
   * @return library modules
   */
  public Iterable<LibraryModule> libraries() {
    return libraries.values();
  }

  /**
   * Registers a type declaration.
   * @param type type declaration
   */
  void type(final TypeDecl type) {
    types.add(type);
  }

  /**
   * Returns the type declarations of all parsed modules.
   * @return type declarations
   */
  Iterable<TypeDecl> types() {
    return types;
  }

  /**
   * Registers a public type declaration of a library module.
   * @param type type declaration
   * @return {@code false} if a public type with the same name has already been registered
   */
  boolean publicType(final TypeDecl type) {
    if(publicTypes.contains(type.name)) return false;
    publicTypes.put(type.name, type);
    return true;
  }

  /**
   * Returns the public type declarations of all library modules.
   * @return type declarations, indexed by type name
   */
  QNmMap<TypeDecl> publicTypes() {
    return publicTypes;
  }

  /**
   * Pre-declares a module file.
   * @param uri module URI
   * @param file file path
   */
  public void declare(final byte[] uri, final byte[] file) {
    declared.computeIfAbsent(uri, TokenList::new).add(file);
  }

  /**
   * Adopts the pre-declared module files of other modules.
   * @param modules modules
   */
  public void declare(final ModuleRegistry modules) {
    for(final byte[] uri : modules.declared) declared.put(uri, modules.declared.get(uri));
  }

  /**
   * Returns the pre-declared files of a module.
   * @param uri module URI
   * @return file paths (can be {@code null})
   */
  public TokenList declared(final byte[] uri) {
    return declared.get(uri);
  }
}
