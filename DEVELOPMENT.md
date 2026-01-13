# Development Guide

## Requirements

- Java 25 (OpenJDK)
- Maven 3.9+

For Raspberry Pi ARM builds, [Liberica JDK](https://www.bell-sw.com/java.html) or [Azul Zulu](https://www.azul.com/downloads/?package=jdk#zulu) are recommended.

## Build Commands

```bash
# Build all modules and run tests
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for a specific module
mvn test -pl robo4j-core

# Run a single test class
mvn test -pl robo4j-core -Dtest=ConfigurationBuilderTest

# Run a single test method
mvn test -pl robo4j-core -Dtest=ConfigurationBuilderTest#testBasicConfiguration
```

## Local Development Setup

### Multicast Routing for Tests

Some tests in `robo4j-core` use multicast for context discovery (e.g., `LookupServiceTests`). On Linux, multicast traffic is routed to the default network interface, which may cause tests to fail when developing locally.

To route multicast traffic to loopback for local testing:

```bash
# Add route for the Robo4j multicast address to loopback
sudo ip route add 238.12.15.254 dev lo

# Verify the route
ip route get 238.12.15.254
# Should show: multicast 238.12.15.254 dev lo ...
```

This route is not persistent across reboots. To make it permanent, add it to your network configuration or a startup script.

### macOS Note

On macOS, you may need to set the JVM to prefer IPv4:

```bash
mvn test -Djava.net.preferIPv4Stack=true
```

## Test Exclusions

Tests matching `**/*ExcludeTest.java` are excluded from the default test run. These are typically hardware-dependent tests that require physical devices (Raspberry Pi, Lego EV3).
