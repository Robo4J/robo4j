package com.robo4j.rpi.lcd;

import com.robo4j.core.enums.RoboHardwareEnumI;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author Miro Wengner (@miragemiko)
 * @author Marcus Hirt (@hirt)
 * @since 24.01.2017
 */
//TODO: miro -> replace enum in Button unit
public enum AdaruitButtonPlateEnum implements RoboHardwareEnumI<Integer> {

    // @formatter:off
	SELECT 			(0, "S"),
	LEFT		    (1, "L"),
	RIGHT		    (2, "R"),
	UP      		(3, "U"),
	DOWN    		(4, "D"),
	;
	// @formatter:on

    private volatile static Map<String, AdaruitButtonPlateEnum> defToCommandTargetMapping;
    private int code;
    private String name;

    AdaruitButtonPlateEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private static void initMapping() {
        defToCommandTargetMapping = new HashMap<>();
        for (AdaruitButtonPlateEnum ct : values()) {
            defToCommandTargetMapping.put(ct.getName(), ct);
        }
    }

    public static AdaruitButtonPlateEnum getByName(String def) {
        if (defToCommandTargetMapping == null)
            initMapping();
        return defToCommandTargetMapping.get(def);
    }

    @Override
    public Integer getType() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AdaruitButtonPlateEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
