package com.mejestic.bump.ui;

public class PojoLatlng {

  public double lat;
  public double lon;
  public String val;

  public PojoLatlng() {

  }

  public PojoLatlng(double l, double lo) {
    val = "loc";
    lat = l;
    lon = lo;
  }
}
