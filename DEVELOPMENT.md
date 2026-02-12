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

## Raspberry Pi Hardware Setup

When developing with hardware units (`robo4j-hw-rpi`, `robo4j-units-rpi`), you need to enable the appropriate interfaces on your Raspberry Pi.

### Enabling I2C

I2C is required for many sensors and devices (pressure sensors, accelerometers, PWM controllers, etc.).

**Option 1: Using raspi-config (recommended)**

```bash
sudo raspi-config
# Navigate to: Interface Options → I2C → Enable
sudo reboot
```

**Option 2: Manual configuration**

Edit `/boot/firmware/config.txt` (or `/boot/config.txt` on older Pi OS versions):

```bash
sudo nano /boot/firmware/config.txt
```

Uncomment or add:
```
dtparam=i2c_arm=on
```

Then reboot:
```bash
sudo reboot
```

**Verify I2C is enabled:**

```bash
# Check for I2C devices (bus 1 is the user-accessible GPIO header bus)
ls /dev/i2c-*

# Scan for connected devices
i2cdetect -y 1
```

Note: On Raspberry Pi 5, you may also see buses 13 and 14 which are internal system buses.

### Enabling SPI

SPI is required for some displays and high-speed sensors.

```bash
sudo raspi-config
# Navigate to: Interface Options → SPI → Enable
sudo reboot
```

Or add to `/boot/firmware/config.txt`:
```
dtparam=spi=on
```

### User Permissions

Your user must be in the appropriate groups to access hardware interfaces without sudo:

```bash
# Add user to hardware groups
sudo usermod -aG i2c,spi,gpio $USER

# Log out and back in for changes to take effect
```

Verify group membership:
```bash
groups
# Should include: i2c spi gpio
```

### Common I2C Device Addresses

| Device | Default Address | Alternate Address |
|--------|-----------------|-------------------|
| BMP581 (pressure) | 0x47 | 0x46 |
| PCA9685 (PWM) | 0x40 | 0x40-0x7F (configurable) |
| MPU-6050 (IMU) | 0x68 | 0x69 |
| HMC5883L (compass) | 0x1E | - |

## Test Exclusions

Tests matching `**/*ExcludeTest.java` are excluded from the default test run. These are typically hardware-dependent tests that require physical devices (Raspberry Pi, Lego EV3).
