package com.djekgrif.alternativeradio.network.model;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by djek-grif on 1/31/17.
 */

public class StationData implements Parent<Channel>{

    private ArrayList<Channel> channels;
    private String name;

    public void setChannels(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public String getName() {
        return name;
    }

    @Override
    public List<Channel> getChildList() {
        return channels;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
