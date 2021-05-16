package com.cast.tv.screen.mirroring.iptv.utils.iptv;

import java.io.Serializable;
import java.util.HashMap;

public class ChannelItem implements Serializable {
    public int duration;
    public String name, url;
    public HashMap<String, String> metadata;

    public ChannelItem() {
        metadata = new HashMap<String, String>();
    }
}