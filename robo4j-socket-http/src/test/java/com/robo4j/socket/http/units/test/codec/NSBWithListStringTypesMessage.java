package com.robo4j.socket.http.units.test.codec;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBWithListStringTypesMessage {

    private Integer number;
    private String message;
    private Boolean active;
    private List<String> listText;

    public NSBWithListStringTypesMessage() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getListText() {
        return listText;
    }

    public void setListText(List<String> listText) {
        this.listText = listText;
    }

    @Override
    public String toString() {
        return "NSBWithListStringTypesMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                ", listText=" + listText +
                '}';
    }
}
