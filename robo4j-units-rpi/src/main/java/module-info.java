module robo4j.units.rpi {
    requires transitive robo4j.core;
    requires transitive robo4j.hw.rpi;
    requires robo4j.prometheus;
    requires jdk.jfr;
    requires org.slf4j;

    exports com.robo4j.units.rpi.accelerometer;
    exports com.robo4j.units.rpi.camera;
    exports com.robo4j.units.rpi.gps;
    exports com.robo4j.units.rpi.gyro;
    exports com.robo4j.units.rpi.imu;
    exports com.robo4j.units.rpi.lcd;
    exports com.robo4j.units.rpi.led;
    exports com.robo4j.units.rpi.lidarlite;
    exports com.robo4j.units.rpi.pad;
    exports com.robo4j.units.rpi.pwm;
    exports com.robo4j.units.rpi.roboclaw;

    uses com.robo4j.math.jfr.ScanEvent;
    uses com.robo4j.math.jfr.JfrUtils;
    uses com.robo4j.math.geometry.Tuple3d;
    uses com.robo4j.math.geometry.Tuple3f;
    uses com.robo4j.math.geometry.ScanResult2D;
    uses com.robo4j.math.geometry.Point2f;
    uses jdk.jfr.Event;
}