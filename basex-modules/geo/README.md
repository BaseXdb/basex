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

The version matching your BaseX release is retrieved from the BaseX Maven repository:

```sh
basex -c "repo install geo"
```

### Building from source

The module is built with BaseX: `mvn install` in the repository root (or `mvn install -pl basex-modules/geo` for the module only) compiles the module, runs the tests, and leaves the module jar in the `target` folder. The jar contains all required dependencies and can be installed with `repo install <jar>`.

License
-------

The Geo Module is provided under the BSD 3-Clause License. It depends on BaseX and GeoTools, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[GeoTools]: https://geotools.org/
[GML]: https://www.ogc.org/de/standards/gml/
[Geo Functions]: https://docs.basex.org/13/Geo_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
