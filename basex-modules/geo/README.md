BaseX Geo Module
----------------

The BaseX Geo Module provides geospatial functions based on [GeoTools], enabling spatial queries and operations on geographic data within BaseX. It operates on geometries provided in [GML].

Example
-------

Calculate the distance of two points:

```xquery
import module namespace geo = 'http://expath.org/ns/geo';
declare namespace gml = 'http://expath.org/ns/gml';

geo:distance(
  <gml:Point><gml:pos>0 0</gml:pos></gml:Point>,
  <gml:Point><gml:pos>1 1</gml:pos></gml:Point>
)
```

This returns `1.4142135623730951`.

Supported Functions
-------------------

For details of the functions supported by this module, see [Geo Functions] in the BaseX documentation.

Installation
------------

### Using a pre-built release

Pre-built module jars are available at:
https://files.basex.org/modules/expath/

Download the jar matching your BaseX release and install it with:

```sh
basex -c "repo install <jar>"
```

where `<jar>` must be replaced by the path to the module jar.

### Building from source

The prerequisites for building the module are a JDK 21 and Maven. 

Use the Geo Module release whose version matches the target BaseX version. For building the module, check out the source code and run

```sh
mvn clean install
```
   
This will compile the module, run the tests, and leave the module jar in the `target` folder, as well as in the local Maven repository at `~/.m2/repository/org/basex/modules/modules-geo`.

The module jar contains all required dependencies.

To install the built jar, use the `repo install` command shown above, replacing `<jar>` with the path to the generated file.

License
-------

The Geo Module is provided under the BSD 3-Clause License. It depends on BaseX, JUnit, and GeoTools, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[GeoTools]: https://geotools.org/
[GML]: https://www.ogc.org/de/standards/gml/
[Geo Functions]: https://docs.basex.org/13/Geo_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
