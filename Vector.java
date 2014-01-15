package Nosotros_BattleCode;

import battlecode.common.Direction;
import java.lang.Math;

public class Vector {
    private double x;
    private double y;
    private double magnitudeSq;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMagnitudeSq() {
        return magnitudeSq;
    }

    public Vector() {
        x = 0;
        y = 0;
        magnitudeSq = 0;
    }

    public Vector(double xGiven, double yGiven) {
        x = xGiven;
        y = yGiven;
        magnitudeSq = Math.pow(x, 2) + Math.pow(y, 2);
    }

    public void addVector(double x1, double y1) {
        x += x1;
        y += y1;
        magnitudeSq = Math.pow(x, 2) + Math.pow(y, 2);
    }

    public void addVector(Vector v) {
        x += v.getX();
        y += v.getY();
        magnitudeSq = Math.pow(x, 2) + Math.pow(y, 2);
    }

    public void setXY(double x1, double y1) {
        x = x1;
        y = y1;
        magnitudeSq = Math.pow(x, 2) + Math.pow(y, 2);
    }

    public Vector getUnitVector() {
        double magnitude = Math.pow(magnitudeSq, 0.5);
        Vector v = new Vector(x/magnitude, y/magnitude);
        return v;
    }

    public Direction toDirectionEnum() {
        Direction[] directions = {
            Direction.WEST, 
            Direction.NORTH_WEST,
            Direction.NORTH, 
            Direction.NORTH_EAST, 
            Direction.EAST, 
            Direction.SOUTH_EAST, 
            Direction.SOUTH, 
            Direction.SOUTH_WEST,
        };
        return directions[(int)(4*Math.atan2(y, x)/Math.PI)];
    }
}
