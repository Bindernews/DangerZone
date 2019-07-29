package com.vortexel.dangerzone.common.curve;

public class WVec2d {

    public double[] dest;
    public int idx;

    public WVec2d(double[] dest, int idx) {
        this.dest = dest;
        this.idx = idx;
    }

    public WVec2d wrap(double[] dest, int idx) {
        this.dest = dest;
        this.idx = idx;
        return this;
    }

    public WVec2d add(double x, double y) {
        dest[idx] += x;
        dest[idx + 1] += y;
        return this;
    }

    public WVec2d add(double[] src, int iSrc) {
        return add(src[iSrc], src[iSrc + 1]);
    }

    public WVec2d add(double v) {
        return add(v, v);
    }

    public WVec2d sub(double x, double y) {
        dest[idx] -= x;
        dest[idx + 1] -= y;
        return this;
    }

    public WVec2d sub(double[] src, int iSrc) {
        return sub(src[iSrc], src[iSrc + 1]);
    }

    public WVec2d sub(double v) {
        return sub(v, v);
    }

    public WVec2d mul(double x, double y) {
        dest[idx] *= x;
        dest[idx + 1] *= y;
        return this;
    }

    public WVec2d mul(double[] src, int iSrc) {
        return mul(src[iSrc], src[iSrc + 1]);
    }

    public WVec2d mul(double v) {
        return mul(v, v);
    }

    public WVec2d div(double x, double y) {
        dest[idx] /= x;
        dest[idx + 1] /= y;
        return this;
    }

    public WVec2d div(double[] src, int iSrc) {
        return div(src[iSrc], src[iSrc + 1]);
    }

    public WVec2d div(double v) {
        return div(v, v);
    }

    public WVec2d set(double x, double y) {
        dest[idx] = x;
        dest[idx + 1] = y;
        return this;
    }

    public WVec2d set(double[] src, int iSrc) {
        return set(src[iSrc], src[iSrc + 1]);
    }

    public WVec2d set(double v) {
        return set(v, v);
    }

    public double x() {
        return dest[idx];
    }

    public double y() {
        return dest[idx + 1];
    }

    public static void setP(double[] dest, int iDest, double[] src, int iSrc) {
        dest[iDest] = src[iSrc];
        dest[iDest + 1] = src[iSrc + 1];
    }

    public static void setP(double[] dest, int iDest, double x, double y) {
        dest[iDest] = x;
        dest[iDest + 1] = y;
    }
}
