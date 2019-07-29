package com.vortexel.dangerzone.common.curve;

import javax.annotation.Nonnull;

public interface BzCurve {

    /**
     * Evaluate the curve at a given point {@code t}, placing X and Y in {@code xy[offset]} and {@code xy[offset + 1]}
     * respectively.
     * @param t the point on the curve to evaluate
     * @param xy output array
     * @param offset offset at which to place X and Y.
     */
    void evaluate(double t, @Nonnull double[] xy, int offset);

    /**
     * Retrieve the X and Y coordinates of the {@code index}-th point and place them in {@code xy} at
     * index {@code offset}.
     * @param index index of the point to query
     * @param xy output array
     * @param offset offset into output array
     */
    void getPoint(int index, @Nonnull double[] xy, int offset);

    /**
     * Returns how many points are in this curve.
     */
    int getPointCount();

    double getTop();
    double getBottom();
    double getLeft();
    double getRight();
}
