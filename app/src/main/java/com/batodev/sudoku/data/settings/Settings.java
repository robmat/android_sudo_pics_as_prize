package com.batodev.sudoku.data.settings;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root
public class Settings {
    @ElementList(required = false)
    public List<String> uncoveredPics = new ArrayList<>();
    @Element(required = false)
    public Integer lastSeenPic = 0;
    @Element(required = false)
    public Integer addCounter = 0;
    @Element(required = false)
    public Integer displayAddEveryXPicView = 2;
}
