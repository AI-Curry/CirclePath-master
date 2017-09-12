package com.example.circlepath_library;

/**
 * Helper class for animation and Menu Item cicle center Points
 * Created by LiuLei on 2017/9/12.
 */

public class CirclePoint {

    private float x;
    private float y;
    private float radius = 0.0f;
    private double angle = 0.0f;

    public void setX(float x1) {
        x = x1;
    }

    public float getX() {
        return x;
    }

    public void setY(float y1) {
        y = y1;
    }

    public float getY() {
        return y;
    }

    public void setRadius(float r) {
        radius = r;
    }

    public float getRadius() {
        return radius;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }
}
