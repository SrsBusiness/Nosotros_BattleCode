package team090;

import battlecode.common.Direction;
import java.lang.Math;

public class Vector {
    private double x;
    private double y;
    private double magnitudeSq;

    public Vector() {
        x = 0;
        y = 0;
        magnitudeSq = 0;
    }

    public Vector(double xGiven, double yGiven) {
        x = xGiven;
        y = yGiven;
        magnitudeSq = x*x + y*y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMagnitude() {
        return Math.pow(magnitudeSq, 0.5);
    }

    public double getMagnitudeSq() {
        return magnitudeSq;
    }

    public Vector getUnitVector() {
        double magnitude = Math.pow(magnitudeSq, 0.5);
        Vector v = new Vector(x/magnitude, y/magnitude);
        return v;
    }

    //Modifier methods
    public void setXY(double x1, double y1) {
        x = x1;
        y = y1;
        magnitudeSq = x*x + y*y;
    }

    //Transforms
    public Vector add(double x1, double y1) {
        return new Vector(x+x1, y+y1);
    }
    public Vector add(Vector v) {
        return new Vector(x+v.getX(), y+v.getY());
    }
    public Vector scale(double s) {
        return new Vector(x*s, y*s);
    }
    public Vector poly(double c0, double c1,
                       double c2, double c3,
                       double c4, double c5,
                       double c6, double c7,
                       double c8) {
        double r = getMagnitude();
        return scale(c0*(r-c1)*(r-c1)+c2*(r-c3)+c4/(r-c5)+c6/((r-c7)*(r-c7))+c8);
    }
    public Vector log(double root, double amplitude) {
        double r = getMagnitude();
        if (r-(root-1) > 0) {
            return scale(Math.E*amplitude*Math.log(r-(root-1))/(r-(root-1)));
        } else {
            return scale(0);
        }
    }
    public Vector logistic(double root, double amplitude, double yShift) {
        return scale(amplitude*2/(1+(Math.pow(Math.E, (root-Math.pow(magnitudeSq, 0.5))))) - amplitude + yShift);
    }
    
    //Return battlecode direction
    public Direction toDirectionEnum() {
        Direction[] directions = {
            Direction.EAST, 
            Direction.SOUTH_EAST,
            Direction.SOUTH, 
            Direction.SOUTH_WEST,
            Direction.WEST,
            Direction.NORTH_WEST, 
            Direction.NORTH,
            Direction.NORTH_EAST
        };
        return directions[((int)(Math.round(4*Math.atan2(y, x)/Math.PI))+8)%8];
    }
}
