package com.vortexel.dangerzone.common.curve;

import javax.annotation.Nonnull;

import static com.vortexel.dangerzone.common.curve.WVec2d.setP;

/**
 * Implements a Bezier Curve with 2, 3, or 4 points.
 */
public class BzCurve4 implements BzCurve {

    private static final double TWO_THIRDS = 2D / 3D;
    private static final double ONE_THIRD = 1D / 3D;

    private double[] points;
    private double top = Double.MAX_VALUE;
    private double bottom = Double.MIN_VALUE;
    private double left = Double.MAX_VALUE;
    private double right = Double.MIN_VALUE;

    public BzCurve4(double... inPoints) {
        points = new double[8];
        WVec2d v = new WVec2d(points, 0);
        switch (inPoints.length) {
            case 4:
                setP(points, 0, inPoints, 0);
                setP(points, 6, inPoints, 2);
                // Calculate control points
                // Each is 1/3 of the way down the line from its side.
                double dx = (inPoints[2] - inPoints[0]) * ONE_THIRD;
                double dy = (inPoints[3] - inPoints[1]) * ONE_THIRD;
                v.wrap(points, 2).set(inPoints, 0).add(dx, dy);
                v.wrap(points, 4).set(inPoints, 2).sub(dx, dy);
                break;
            case 6:
                setP(points, 0, inPoints, 0);
                setP(points, 6, inPoints, 4);
                // Calculate control points
                // CP1 = QP0 + (2/3) * (QP1 - QP0)
                // CP2 = QP2 + (2/3) * (QP1 - QP2)
                v.wrap(points, 2).set(inPoints, 2).sub(inPoints, 0).mul(TWO_THIRDS).add(inPoints, 0);
                v.wrap(points, 4).set(inPoints, 2).sub(inPoints, 4).mul(TWO_THIRDS).add(inPoints, 4);
                break;
            case 8:
                setP(points, 0, inPoints, 0);
                setP(points, 2, inPoints, 2);
                setP(points, 4, inPoints, 4);
                setP(points, 6, inPoints, 6);
                break;
            default:
                throw new IllegalArgumentException("number of points must be 4, 6, or 8");
        }

        double[] tmp = new double[2];
        for (int i = 0; i < points.length / 2; i++) {
            getPoint(i, tmp, 0);
            left = Math.min(left, tmp[0]);
            right = Math.max(right, tmp[0]);
            top = Math.min(top, tmp[1]);
            bottom = Math.max(bottom, tmp[1]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate(double t, @Nonnull double[] xy, int offset) {
        double t1 = 1 - t,
                t1_3 = t1 * 3,
                c0 = pow3(t1),
                c1 = pow2(t1) * t1_3,
                c2 = pow2(t) * t1_3,
                c3 = pow3(t),
                xO = (c0 * points[0]) + (c1 * points[2])
                    + (c2 * points[4]) + (c3 * points[6]),
                yO = (c0 * points[1]) + (c1 * points[3])
                    + (c2 * points[5]) + (c3 * points[7]);
        setP(xy, offset, xO, yO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getPoint(int index, @Nonnull double[] xy, int offset) {
        if (index >= points.length / 2 || index < 0) {
            throw new IllegalArgumentException("Point index out of bounds");
        }
        setP(xy, 0, points, index * 2);
    }

    @Override
    public int getPointCount() {
        return 4;
    }

    @Override
    public double getTop() {
        return top;
    }

    @Override
    public double getBottom() {
        return bottom;
    }

    @Override
    public double getLeft() {
        return left;
    }

    @Override
    public double getRight() {
        return right;
    }

    public static double pow2(double v) {
        return v * v;
    }

    public static double pow3(double v) {
        return v * v * v;
    }
}
