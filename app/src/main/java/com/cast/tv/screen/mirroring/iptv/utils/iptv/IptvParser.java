package com.cast.tv.screen.mirroring.iptv.utils.iptv;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IptvParser {

    static String CHANNEL_REGEX = "EXTINF:(.+),(.+)(?:\\R)(.+)$";
    static String METADATA_REGEX = "(\\S+?)=\"(.+?)\"";
    private static Pattern metadata_pattern, channel_pattern;

    public static ChannelList parse(InputStream playlist) {
        if (playlist == null) return  null;

        metadata_pattern = Pattern.compile(METADATA_REGEX);
        channel_pattern = Pattern.compile(CHANNEL_REGEX, Pattern.MULTILINE);
        ChannelList cl = new ChannelList();
        Scanner s = new Scanner(playlist).useDelimiter("#");
        if (!s.next().trim().contains("EXTM3U")) {
            return cl;
        }
        while (s.hasNext()) {
            String line = s.next();
            Matcher matcher = channel_pattern.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            ChannelItem item = new ChannelItem();
            parseMetadata(item, matcher.group(1));
            item.name = matcher.group(2);
            item.url = matcher.group(3);
            cl.add(item);
        }
        return cl;
    }

    private static void parseMetadata(ChannelItem item, String metadata) {
        Matcher matcher = metadata_pattern.matcher(metadata);
        while (matcher.find()) {
            item.metadata.put(matcher.group(1), matcher.group(2));
        }
    }
}