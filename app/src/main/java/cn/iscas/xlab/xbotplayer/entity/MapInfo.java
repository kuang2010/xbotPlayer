package cn.iscas.xlab.xbotplayer.entity;

import org.json.JSONArray;

/**
 * Created by lisongting on 2017/10/10.
 */

public class MapInfo {

    //地图数据:
    // -1代表未知
    //0代表可达
    //100代表障碍
    private JSONArray data;

    //rviz中原始地图的行数
    private int originMapRows;

    //rviz中原始地图的列数
    private int originMapColumns;

    private float locationX;

    private float locationY;

    public MapInfo(JSONArray data,int originMapRows, int originMapColumns, float locationX, float locationY) {
        this.originMapRows = originMapRows;
        this.originMapColumns = originMapColumns;
        this.data = data;
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public int getOriginMapRows() {
        return originMapRows;
    }

    public void setOriginMapRows(int originMapRows) {
        this.originMapRows = originMapRows;
    }

    public int getOriginMapColumns() {
        return originMapColumns;
    }

    public void setOriginMapColumns(int originMapColumns) {
        this.originMapColumns = originMapColumns;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    public void setLocation(float locationX,float locationY) {
        this.locationX = locationX;
        this.locationY = locationY;

    }

    public float getLocationY() {
        return locationY;
    }
    public float getLocationX() {
        return locationX;
    }

    @Override
    public String toString() {
        return "MapInfo{" +
                "originMapRows=" + originMapRows +
                ", originMapColumns=" + originMapColumns +
                ", locationX=" + locationX +
                ", locationY=" + locationY +
                '}';
    }
}
