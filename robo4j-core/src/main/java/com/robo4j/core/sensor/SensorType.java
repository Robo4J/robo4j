package com.robo4j.core.sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miroslavkopecky on 15/02/16.
 */
public enum SensorType {

    //@formatter:off
    //type      id      mode        source                                        port     elements
    /**
     * Touch pressed 1 : free 0
     */
    TOUCH       (0,     "Touch",    "lejos.hardware.sensor.EV3TouchSensor",       "S1",      1),
    /**
     * Returns an SampleProvider object representing the gyro sensor in angle
     * mode. <br>
     * In rate mode the sensor measures the orientation of the sensor in respect to
     * its start position. A positive angle indicates a orientation to the left. A
     * negative rate indicates a rotation to the right. Angles are expressed in
     * degrees.<br>
     */
    GYRO        (1,     "Angle",    "lejos.hardware.sensor.EV3GyroSensor",        "S4",     1),

    /**
     * size of the array is 3
     */
    COLOR       (2,     "RGB",      "lejos.hardware.sensor.EV3ColorSensor",       "S2",     3),

    /**
     * distance is measured in meter
     */
    SONIC       (3,     "Distance", "lejos.hardware.sensor.EV3UltrasonicSensor",  "S3",     1);
    //@formatter:on

    private int id;
    private String mode;
    private String source;
    private String port;
    private int elements;



    private volatile static Map<Integer, SensorType> codeToSensorTypMapping;
    private volatile static Map<String, SensorType> codeToSensorSourceMapping;
    private volatile static Map<String, SensorType>  codeToSensorPortMapping;

    SensorType(final int id, final String mode, final String source, final String port, final int elements){
        this.id = id;
        this.mode = mode;
        this.source = source;
        this.port = port;
        this.elements = elements;
    }

    public int getId() {
        return id;
    }

    public String getMode() {
        return mode;
    }

    public String getSource() {
        return source;
    }

    public String getPort() {
        return port;
    }

    public int getElements(){
        return elements;
    }

    public static SensorType getById(int id){
        if(codeToSensorTypMapping == null){
            initMapping();
        }
        return codeToSensorTypMapping.get(id);
    }

    public static SensorType getBySource(int name){
        if(codeToSensorSourceMapping == null){
            initSourceMapping();
        }
        return codeToSensorSourceMapping.get(name);
    }

    public static SensorType getByPort(int name){
        if(codeToSensorPortMapping == null){
            initPortMapping();
        }
        return codeToSensorPortMapping.get(name);
    }

    @Override
    public String toString() {
        return "SensorType=(" +
                "Source='" + source + '\'' +
                "Mode='" + mode + '\'' +
                ')';
    }

    //Private Methods
    private static void initMapping(){
        codeToSensorTypMapping = new HashMap<>();
        for(SensorType cmd: values()){
            codeToSensorTypMapping.put(cmd.getId(), cmd);
        }
    }

    private static void initSourceMapping(){
        codeToSensorSourceMapping = new HashMap<>();
        for(SensorType cmd: values()){
            codeToSensorSourceMapping.put(cmd.getSource(), cmd);
        }
    }

    private static void initPortMapping(){
        codeToSensorPortMapping = new HashMap<>();
        for(SensorType cmd: values()){
            codeToSensorPortMapping.put(cmd.getPort(), cmd);
        }
    }

}
