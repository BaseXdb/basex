BaseX
-----

[![Build](https://github.com/BaseXdb/basex/actions/workflows/build.yml/badge.svg)](https://github.com/BaseXdb/basex/actions/workflows/build.yml)

BaseX is a light-weight, high-performance XML database and XQuery processor. It turns large XML, JSON and CSV documents into indexed databases and lets you query, update and serve them with XQuery 4.0:

* **XQuery 4.0**, including the Update and Full Text extensions, and a rich set of modules for files, HTTP, JSON, CSV, archives, cryptography and more
* **Indexes** for text, attribute, token and full-text queries, applied automatically by the optimizer
* **Graphical user interface** with an XQuery editor and interactive data visualizations
* **Client/server architecture** with transactions and user management, and client bindings for many languages
* **Web applications** via REST, RESTXQ, WebDAV and WebSockets, and a browser-based admin interface (DBA)

Getting Started
---------------

* Download the latest release from [basex.org](https://basex.org/download/).
* Or start the HTTP server with Docker, and open the admin interface at http://localhost:8080/dba:

      docker run -p 8080:8080 -e BASEX_ADMIN_PASSWORD=secret ghcr.io/basexdb/basex

* Evaluate a first query on the command line: `basex "(1 to 10) => sum()"`
* Read the [documentation](https://docs.basex.org/) for tutorials and the full reference.

Feedback
--------

We are interested in your feedback:

* Please send new suggestions and bug reports to our [basex-talk](https://basex.org/open-source/) mailing list.
* Confirmed bugs and feature requests are discussed in our [issue tracker](https://github.com/BaseXdb/basex/issues).
* We invite you to contribute to our [documentation](https://docs.basex.org/).

Compiling BaseX
---------------

The code base of BaseX is hosted on [GitHub](https://github.com/BaseXdb/basex).

JDK 21 and Maven are required to compile the sources. Run `mvn install -DskipTests` in the root directory to build all modules.

You can launch the following classes, which are all placed in the `org.basex` main package:

    BaseX        : console mode
    BaseXServer  : server instance, waiting for requests
    BaseXClient  : console mode, interacting with the server
    BaseXGUI     : graphical user interface
    BaseXHTTP    : HTTP server (basex-api)

All classes except `BaseXHTTP` are found in the `basex-core` directory. Use `-h` to list the available command line options. For example, you can use BaseX to process XQuery expressions without entering the console.

Using Eclipse
-------------

BaseX is developed with Eclipse. The project settings, including code style and compiler warnings, are part of the sources and become effective as soon as you import the projects.

### Running BaseX

The following steps can be performed to start BaseX with Eclipse:

 - Choose `Run` → `Run Configurations…`
 - Create a new `Java Application` launch configuration
 - Select `basex-core` as project (or `basex-api` for `BaseXHTTP`)
 - Choose a `Main class` (e.g., `org.basex.BaseXGUI`)
 - Launch the project via `Run`

License
-------

BaseX is released under the [BSD 3-Clause License](LICENSE).

Have fun,
Your BaseX Team
