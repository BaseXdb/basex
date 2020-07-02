Introduction
------------

Welcome to the BaseX open source project. We are interested in your feedback:

* Please send new suggestions and bug reports to our
  [basex-talk](https://basex.org/open-source/) mailing list.
* Confirmed bugs and feature requests are discussed in our issue tracker.
* We invite you to contribute to our
  [Documentation](https://docs.basex.org/).

Compiling BaseX
---------------

JDK 1.8 and JUnit are currently required to compile the complete sources of the
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

Docker Image
------------

### Docker images for versions 9.x

The BaseX server is also available as automated build [`basex/basexhttp`]
on the Docker Hub, providing both release and nightly builds. All images are
automatically rebuild if Docker provides updated base images.

To start a BaseX container based on the latest development release publishing
the BaseX server and HTTP ports `1984` and `8984` and bind-mounting your user's
`basex/data` directory, run:

    docker run -d \
        --name basexhttp \
        --publish 1984:1984 \
        --publish 8984:8984 \
        --volume "$HOME/basex/data":/srv/basex/data \
        basex/basexhttp:latest

Once the container is running, you can connect to its REST, RESTXQ and WebDAV
services.

You can use the BaseX client to connect to the database in the container:

    docker exec -ti basexhttp basexclient

The container directories `/srv/basex/data`, `/srv/basex/repo` and
`/srv/basex/webapp` are persisted as volumes, which means they cannot be
preinitialized in the application image.

You can bind-mount each of them to directories in your work environment:

    docker run -d \
        --name basexhttp \
        --publish 1984:1984 \
        --publish 8984:8984 \
        --volume "$HOME/basex/data":/srv/basex/data \
        --volume "$HOME/basex/repo":/srv/basex/repo \
        --volume "$HOME/basex/webapp":/srv/basex/webapp \
        basex/basexhttp:latest

BaseX runs as user `basex` with fixed user ID 1984. Your data directory needs
to have write permission for this user in order to store data and manage
databases.

If you want to add your own application, create an image
`FROM basex/basexhttp:[tag]` with `[tag]` being the BaseX version you're
developing against. Usually, you will add your application code to
`/srv/basex/webapp` and modules to `/srv/basex/repo`.

If you are in the cloned directory of the BaseX git repository, you can include
the DBA application in the running container :

    docker run -d \
    	--name basexhttp \
    	--publish 1984:1984 \
    	--publish 8984:8984 \
    	--volume "$HOME/basex/data":/srv/basex/data \
    	--volume "$(pwd)/basex-api/src/main/webapp":/srv/basex/webapp \
      basex/basexhttp:latest

### Docker images for versions 8.x

Up to version 8.x, BaseX used directories `BaseXData`, `BaseXRepo` and
`BaseXWeb`, typically in your home directory.

If you are using a Docker image for version 8.x (e.g. 8.5) the mount path
in the container is slightly different:

        docker run -ti \
            --name basexhttp \
            --publish 1984:1984 \
            --publish 8984:8984 \
            --volume ~/BaseXData:/srv/BaseXData \
            basex/basexhttp:8.5

If you prefer the DBA web interface, this can also be linked against your
server container:

    docker run -d \
        --name basex-dba \
        --publish 18984:8984 \
        --link basexhttp:basexhttp \
        basex/dba

If you want to add your own application, create an image
`FROM basex/basexhttp:[tag]` with `[tag]` being the BaseX version you're
developing against. Usually, you will add your application code to
`/srv/BaseXWeb` and modules to `/srv/BaseXRepo`. `BaseXData` is persisted as
a volume, which means it cannot be preinitialized in the application image.

### Further information

For further information on using the Docker image, refer to the
[BaseX Docker documentation].

[`basex/basexhttp`]: https://hub.docker.com/r/basex/basexhttp/
[BaseX Docker documentation]: https://docs.basex.org/wiki/Docker
[BaseX Web Application documentation]: https://docs.basex.org/wiki/Web_Application

Best regards
Your BaseX Team

[documentation]: https://docs.basex.org
[bug tracker]: https://github.com/BaseXdb/BaseX/issues
