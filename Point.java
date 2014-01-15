package Nosotros_BattleCode;

import java.lang.Math;

public class Point {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point(double xGiven, double yGiven) {
        x = xGiven;
        y = yGiven;
    }

    public double distanceSq(double x1, double y1) {
        return Math.pow(x1-x, 2) + Math.pow(y1-y, 2);
    }

    public void setLocation(double x1, double y1) {
        x = x1;
        y = y1;
    }
}
