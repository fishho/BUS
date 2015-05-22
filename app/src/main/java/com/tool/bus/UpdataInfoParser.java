package com.tool.bus;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Created by haoyu on 15-5-10.
 */
public class UpdataInfoParser {
    public static UpdataInfo getUpdataInfo(InputStream is) throws Exception{
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");
        int type = parser.getEventType();
        UpdataInfo info = new UpdataInfo();
        while(type != XmlPullParser.END_DOCUMENT ){
            switch (type) {
                case XmlPullParser.START_TAG:
                    if("version".equals(parser.getName())){
                        info.setVersion(parser.nextText());
                    }

                    break;
            }
            type = parser.next();
        }
        return info;
    }
}
