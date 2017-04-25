BaseX Changelog
===============

Welcome to the changelog. This file contains all code changes since BaseX 6.5. On our [download page][] you will find the current release; [former releases][] are available, too.

[download page]:   http://basex.org/products/download/all-downloads/
[former releases]: http://files.basex.org/releases/

Version 7.0.2 (November 11, 2011)
---------------------------------

 - **Full Text**
    - Stemming support for Japanese text corpora
      (thanks to Toshio HIRAI!)

 - **Startup**
    - Updated start scripts (thanks to Ralf Jung!)
    - System property `org.basex.path` added to specify
      project's home directory (thanks to malamut2!)

 - **XQuery**
    - Numerous minor XQuery 3.0 enhancements and fixes
    - Fix for `db:optimize()` calls (thanks to Martin Hillert!)

 - **Storage**
    - Fix to retain newly introduced namespaces
      (thanks to Laurent Chevalier!)

 - **Users**
    - Default privileges for new users set to "none"
      (thanks to Pascal Heus!)

 - **REST**
    - Query base URI for evaluated queries is now `$HTTPPATH`
      (thanks to Florent Georges!)

Version 7.0.1 (October 23, 2011)
--------------------------------

 - **Distributions**
    - Windows installer was updated to support latest features
    - ZIP file was updated (initial config & directories added)
    - Short directory names are chosen if config file resides in `app.dir`
    - Start scripts have been improved

 - **XQuery**
    - much faster execution of `count()` when applied to opened databases

 - **Server**
    - Flag `-c` connects to an existing database server
    - Flag `-s` specifies a port for stopping the HTTP server (Jetty)
    - Flag `-S` starts the HTTP server as a service
    - Running write operations will be completed before server is stopped

 - **API**
    - Ruby, Python, PHP, Java: clients updated

Version 7.0 (October 14, 2010) TEI Edition
------------------------------------------

 - **API**
    - Native and tightly integrated REST implementation replaces JAXRX
    - WebDAV provides a file system like access to BaseX databases

 - **XQuery**
    - Parsing and serializing JSON documents
    - SQL module builds a JDBC bridge to access relational databases
    - EXPath Cryptographic Module for encryption and XML Signatures
    - Full text engine tokenizes Japanese texts (thx to Toshio Hirai!)
    - `db:retrieve()` and `db:store()` handle raw data
    - `util:uuid()` to create random universally unique identifier
    - `db:content-type()` retrieves the content type of a resource
    - `db:exists()` checks if the specified database or resource exists
    - `db:is-raw()`, `db:is-xml()` check existence and type of a resource
    - `db:list()`, `db:open()` uses two separate arguments to specify
      database and resource path
    - further modifications: `db:add()`

 - **Server**
    - BaseX HTTP Server activates the REST and WebDAV services
    - `ITER` command returns all results in one go and the client
      handles the iterative execution

 - **Commands**
    - `FLUSH` command to write all database buffers to disk
    - `STORE` command to store raw data in a database
    - `RETRIEVE` command to get raw data from the database
    - Modified `ADD` command

 - **Options**
    - `SERVERHOST`: to specify a server
    - `KEEPALIVE`: optional timeout to close inactive client sessions
    - `AUTOFLUSH`: autoflush database buffers
    - `QUERYPATH`: path to executed query

Version 6.7.1 (July 28, 2011) Balisage Edition
----------------------------------------------

 - **XQuery**
    - New [database functions]() for adding, deleting, renaming
      and replacing documents, and optimizing databases:
    - [XSLT transformations]() via Java or Saxon:
    - All [XQuery 3.0 functions]() are now supported:
    - Tail-call optimizations to speed up recursive functions

 - **Storage**
    - Use `ADDARCHIVES` to parse files within archives
    - Use [`SKIPCORRUPT`][options] to skip non-well-formed files when
      creating a database
    - Max. level depth limit (256) removed
    - The document index is now incrementally updated

 - **GUI**
    - "Manage Database" dialog now supports operations on
      multiple databases and the command-line [glob syntax]()
    - Drag and drop operations introduced for opening new files
    and copying file paths

 - **Client/Server**
    - Delay clients that repeatedly fail to login
    - All remaining plain-text password operations now use
      MD5 to send and log passwords

[database functions]:   http://docs.basex.org/wiki/Database_Functions
[XQuery 3.0 functions]: http://docs.basex.org/wiki/XQuery_3.0
[XSLT transformations]: http://docs.basex.org/wiki/XSLT_Functions
[options]:              http://docs.basex.org/wiki/Options
[glob syntax]:          http://docs.basex.org/wiki/Commands#Glob_Syntax

Version 6.7 (June 30, 2011)
---------------------------

  - **Main Features**
    - Native support for the EXPath [packaging system]()
    - Client/server [event]() notification
    - Persistent document index added to speed up
      access to large collections

 - **XQuery**
    - New [database functions]() and [full-text functions]()
    - Event function added to fire events
    - Index optimizations, better cost estimations

 - **Commands**
    - Glob syntax introduced to database [commands]()
    - New commands added: `REPLACE`, `RENAME`,
      `REPO DELETE/INSTALL/LIST`, `CREATE/DROP EVENT`
    - `BACKUP` optimized, renamed to `CREATE BACKUP`

[packaging system]:    http://docs.basex.org/wiki/Packaging
[event]:               http://docs.basex.org/wiki/Events
[database functions]:  http://docs.basex.org/wiki/Database_Functions
[full-text functions]: http://docs.basex.org/wiki/Full-Text_Functions
[commands]:            http://docs.basex.org/wiki/Commands

Version 6.6.2 (May 13, 2011) Linuxtag Release
---------------------------------------------

 - **API**
    - [JAX-RX]() API now supports basic user authentication:
    - The [`COPY`][copy] command creates identical database copies
    - The [`OPTIMIZE ALL`][optimize] command minimizes all database structures

 - **XQuery**
    - [Map]() expressions and functions added
    - [File module]() aligned with latest EXPath specification
    - Speedup of full-text queries with keyword lists.
      Example: `$x contains text { 'a', 'b', 'c', ...}`
    - XQuery Update optimizations for replacing nodes; tree-aware updates.
    - XQuery optimizations to avoid materialization of sequences.

 - **GUI**
    - Multiple editor tabs added
    - Database management: copy databases

 - **Core**
    - Internal XML parser: HTML entities added
    - Glob syntax: support for multiple file suffixes

[JAX-RX]:      http://docs.basex.org/wiki/JAX-RX_API
[copy]:        http://docs.basex.org/wiki/Commands#COPY
[optimize]:    http://docs.basex.org/wiki/Commands#OPTIMIZE
[map]:         http://docs.basex.org/wiki/Map_Functions
[file module]: http://docs.basex.org/wiki/File_Functions

Version 6.6.1 (March 30, 2011) XML Prague Release
-------------------------------------------------

 - **XQuery**
    - Index rewritings added for `.../text()[. = ..]` syntax
    - Optimizations of mixed axis path expressions, e.g.: `//x/name()`
    - Index rewritings on collections fixed and generalized
    - Faster evaluation of filters with pos. predicates, e.g.: `$x[5]`
    - Fixed relocation of let clauses in GFLWOR expressions
    - Trace function returned wrong original results
    - Variables in catch clauses were discarded
    - HOF optimizations and fixes

 - **GUI**
    - language option (for Japanese, German, etc. interface) fixed

Version 6.6 (March 23, 2011)
----------------------------

 - **XQuery 3.0**
    - Full support of Higher Order Functions
      (dynamic function invocation, inlined functions, etc.)
    - Context item and decimal-format declarations
 - **XQuery**
    - Full support of the EXPath ZIP and HTTP modules
	- Utility functions added:
      `util:format()`, `util:crc32()`, `util:md5()`, `util:sha1()`, etc.
    - XQuery Update: numerous speedups, memory consumption reduced
 - **Commands**
    - `COPY` command added to clone existing databases
 - **Core**
    - CSV/Test importers revised, `ENCODING` option added to CSV/Text parsers
    - Storage and update features revised, bugs fixed

Version 6.5 (November 17, 2010)
-------------------------------

 - **Commands**
    - `LIST` extended by optional database `[path]` argument
	- Allow hierarchical paths in `OPEN` command
 - **JAX-RX**
    - Full database path is now used to list documents within
      a database. Use `query` parameter to show document contents
    - Bind external variables to query and run parameter
 - **Distributions**
    - Windows Installer: creates startmenu entries,
      sets file associations and path environment entries
 - **XQuery**
    - Context choice in filter predicates
 - **GUI**
    - Create: support different input formats: XML, HTML, Text, ..
 - **API**
    - UTF-8 encoding added to Java binding
 - **Storage**
    - text decompression synchronized
 - **JavaDoc**
    - `package.html` files added and updated

------------------------------------------------------------------------
