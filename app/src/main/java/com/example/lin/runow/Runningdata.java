package com.example.lin.runow;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Date;

@Entity(tableName = "running_table")
public class Runningdata {
    //--- database columns-----------//
    // Room require primarykey is a must
    // autoGenerate means auto increment in this case the id is self generated
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "calorie")
    private double calorie;

    @ColumnInfo(name = "starttime")
    private String starttime;

    @ColumnInfo(name = "distance")
    public double distance;

    //-----get and set functions------//
    public int getId() {
        return id;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String running_starttime) {
        this.starttime = running_starttime;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double running_calorie) {
        this.calorie = running_calorie;
    }

   public double getDistance() { return distance; }

    public void setDistance(double running_distance) {
        this.distance = running_distance;
    }

}