## Introduction
Robo4J provides an easy way of getting started with building custom hardware and creating software for it running on the JVM.

* [Robo4j.io][] is a robotics framework running on the JVM
* [Robo4j.io][] provides a library of hardware abstractions for RaspberryPi and Lego EV3 out of the box
* [Robo4j.io][] provides a library of configurable units that allows hardware to be enabled and configured through XML
* [Robo4j.io][] provides a threading model controlled by annotations

The current [Robo4j.io][] version is alpha-0.4

## Requirements
[Git][] and [Java JDK 8][]

## Documentation
See current [Robo4j documentation][].
> **Note:** Under construction.

## Building from Source
The Robo4j framework uses [Gradle][] for building.

The following will build all components:

```bash
./gradlew jar
```
The individual bundles will be available under robo4j/&lt;component&gt;/build/libs.
To install the bundles and make them available to downstream dependencies, run the following:

```bash
./gradlew install
```

> **Note:** If you are not using Robo4J as the standard user (pi) on a Raspberry Pi, you will have to specify the path to the local maven repository in the file _**libraries.gradle**_, variable: _mavenRepository_
> **Note:** Robo4J currently requires JDK 8. Ensure that you build and run with JDK 8.

[![Build Status](https://travis-ci.org/Robo4J/robo4j.svg?branch=master)](https://travis-ci.org/Robo4J/robo4j)

## Staying in Touch
Follow [@robo4j][] or authors: [@miragemiko][], [@hirt][]
on Twitter. In-depth articles can be found at [Robo4j.io][], [miragemiko blog][] or [marcus blog][]

## License
Robo4J is released under [General Public License][] v3.

[Robo4j.io]: http://www.robo4j.io
[miragemiko blog]: http://www.miroslavkopecky.com
[marcus blog]: http://hirt.se/blog/
[General Public License]: http://www.gnu.org/licenses/gpl-3.0-standalone.html
[@robo4j]: https://twitter.com/robo4j
[@miragemiko]: https://twitter.com/miragemiko
[@hirt]: https://twitter.com/hirt
[Gradle]: http://gradle.org
[Java JDK 8]: http://www.oracle.com/technetwork/java/javase/downloads
[Git]: http://help.github.com/set-up-git-redirect
[Robo4j documentation]: http://www.robo4j.io/p/documentation.html
