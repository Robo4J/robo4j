package com.robo4j.socket.http.dto;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ListElementDTO<T> {

    private List<T> list;

    public ListElementDTO() {
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ListElementDTO{" +
                "list=" + list +
                '}';
    }
}
