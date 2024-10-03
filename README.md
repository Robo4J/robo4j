![Robo4j-master](https://github.com/Robo4j/robo4j/actions/workflows/robo4j-build-actions.yml/badge.svg?branch=master)

# Robo4J
Robo4J provides an easy way of getting started with building custom hardware and creating software for it running on the JVM.

* [Robo4j.io][] is a robotics framework running on the JVM
* [Robo4j.io][] provides a library of hardware abstractions for RaspberryPi and Lego EV3 out of the box
* [Robo4j.io][] provides a library of configurable units that allows hardware to be enabled and configured through XML
* [Robo4j.io][] provides a threading model controlled by annotations

The current [Robo4j.io][] version is 0.6-SNAPSHOT

<a href="https://foojay.io/today/works-with-openjdk"><img align="left" src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png" width="100"></a>
<br><br><br>

## Requirements
[Git][], [Maven][], [OpenJDK 21][]

If you are looking for a JDK 21 ARM hard float build for Raspbian, we recommend looking into [Liberica JDK][] or [Azul Zulu][].

## Documentation
See current [Robo4j documentation][].
> **Note:** Under construction.

## Building from Source
The Robo4j framework uses  [Maven][] for building.

The following will build all components:

```bash
./mvn install
```
The individual bundles will be available under robo4j/&lt;component&gt;/build/libs.
To install the bundles and make them available to downstream dependencies, run the following:

```bash
$ mvn clean install
```

> **Note:** If you are not using Robo4J as the standard user (pi) on a Raspberry Pi, you will have to specify the path to the local maven repository in the file _**libraries.gradle**_, variable: _mavenRepository_

> **Note:** Robo4J currently requires OpenJDK 21. Ensure that you build and run with OpenJDK 21.

## Staying in Touch
Follow [@robo4j][] or authors: [@miragemiko][], [@hirt][] on Twitter. 

In-depth articles can be found at [Robo4j.io][], [miragemiko blog][] or [marcus blog][]

## License
Robo4J is released under [General Public License][] v3.

[Robo4j.io]: https://www.robo4j.io
[miragemiko blog]: https://www.miroslavkopecky.com
[marcus blog]: https://hirt.se/blog/
[General Public License]: https://www.gnu.org/licenses/gpl-3.0-standalone.html
[@robo4j]: https://twitter.com/robo4j
[@miragemiko]: https://twitter.com/miragemiko
[@hirt]: https://twitter.com/hirt
[Maven]: https://maven.apache.org/
[OpenJDK 21]: https://openjdk.java.net/projects/jdk/21/
[Git]: https://git-scm.com/
[Robo4j documentation]: https://www.robo4j.io/p/documentation.html
[Liberica JDK]: https://www.bell-sw.com/java.html
[Azul Zulu Embedded]: https://www.azul.com/downloads/zulu-embedded/
