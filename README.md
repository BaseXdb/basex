BaseX
===============================================================================

Welcome to the source code of BaseX.

To get the project running as smooth as possible, please consider the following
notes:

Compiling BaseX
---------------

JDK 1.7 and JUnit is currently required to compile the complete sources of the
main project. If you are using another environment than Eclipse or don't have
JUnit installed, just delete the `test` packages inside the project and rebuild
the project.

Please take a look at the [Maven documentation] for information on how to use
Maven.

You can launch the following classes, which are all placed in the basex-core
directory and the `org.basex` main package:

    BaseX        : console mode
    BaseXServer  : server instance, waiting for requests
    BaseXClient  : console mode, interacting with the server
    BaseXGUI     : graphical user interface

Moreover, try `-h` to list the available command line options. For example, you
can use BaseX to process XQuery expressions without entering the console.

[Maven documentation]: https://docs.basex.org/wiki/Maven

Docker Image
------------

The BaseX server is also available as automated build
[`basex/basexhttp`]
on the Docker Hub, providing both release and nightly builds. All images are
automatically rebuild if Docker provides updated base images.

To start a BaseX container based on the latest development release publishing
the BaseX server and HTTP ports `1984` and `8984` and bind-mounting your user's
`BaseXData` directory, run

    docker run -ti \
        --name basexhttp \
        --publish 1984:1984 \
        --publish 8984:8984 \
        --volume ~/BaseXData:/srv/BaseXData \
        basex/basexhttp:latest

By passing any other BaseX executable, you can also for example run a BaseX
client connecting to the linked BaseX server for management operations on the
BaseX command line:

    docker run -ti \
        --link basexhttp:basexhttp \
        basex/basexhttp:latest basexclient -nbasexhttp

If you prefer the DBA web interface, this can also be linked against your
server container:

    docker run -d \
        --name basex-dba \
        --publix 18984:8984 \
        --link basexhttp:basexhttp \
        basex/dba

If you want to add your own application, create an image
`FROM basex/basexhttp:[tag]` with `[tag]` being the BaseX version you're
developing against. Usually, you will add your application code to
`/srv/BaseXWeb` and modules to `/srv/BaseXRepo`. `BaseXData` is persisted as
a volume, which means it cannot be preinitialized in the application image.

For further information on using the Docker image, refer to the
[BaseX Docker documentation].

[`basex/basexhttp`]: https://hub.docker.com/r/basex/basexhttp/
[BaseX Docker documentation]: http://docs.basex.org/wiki/Docker

Using Eclipse
-------------

BaseX is being developed with the Eclipse environment. Some style guidelines
are integrated in the sources of BaseX; they are being embedded as soon as you
open the project.

### Running BaseX

The following steps can be performed to start BaseX with Eclipse:

 - Press `Run` -> `Run...`
 - Create a new `Java Application` launch configuration
 - Select `basex` as Project
 - Choose a `Main class` (e.g., org.basex.BaseXGUI)
 - Launch the project via `Run`

### Adding Checkstyle

Some additional Checkstyle guidelines are defined in the project:

 - Open Eclipse
 - Press `Help` -> `Install new Software...`
 - Press `Search for new features to install`
 - Enter the URL: `http://eclipse-cs.sourceforge.net/update`
 - Follow the installation procedure and restart Eclipse

Using Git
---------

The code base of BaseX can be accessed via [GitHub].

[GitHub]: https://github.com/BaseXdb/basex

Feedback
--------

Any kind of feedback is welcome; please check out the [documentation].

Tell us if you run into any troubles installing BaseX:

<basex-talk@mailman.uni-konstanz.de>

You are as well invited to contribute to our [bug tracker].

All the best,  
BaseX Team, 2016

[documentation]: (https://docs.basex.org)
[bug tracker]: (https://github.com/BaseXdb/BaseX/issues)
