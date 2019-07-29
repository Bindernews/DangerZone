package com.vortexel.dangerzone.common.curve;

import com.vortexel.dangerzone.common.Consts;
import lombok.Getter;
import lombok.Setter;

public class DifficultyCurve {

    @Getter
    private BzCurve[] curves;

    @Getter @Setter
    private double epsilon;

    public DifficultyCurve(BzCurve... curves) {
        this.curves = curves;
        this.epsilon = Consts.EPSILON;
    }

    public double evaluate(double x) {
        for (int i = 0; i < curves.length; i++) {
            BzCurve cr = curves[i];
            if ((cr.getLeft() - epsilon) < x && x < cr.getRight()) {
                return findYforX(cr, x);
            }
        }
        return Double.NaN;
    }

    private double findYforX(BzCurve cr, double x) {
        double minT = 0,
                maxT = 1,
                t = 0;
        double[] xy = new double[2];
        // Binary search to find t s.t. Bx(cr, t) == x
        while (true) {
            t = (minT + maxT) / 2;
            cr.evaluate(t, xy, 0);
            if (feq(xy[0], x, epsilon)) {
                return xy[1];
            }
            else if (x < xy[0]) {
                maxT = t;
            }
            else /* if (x > xy[0]) */ {
                minT = t;
            }
            if (minT > maxT) {
                return Double.NaN;
            }
        }
    }

    public static boolean feq(double a, double b, double epsilon) {
        double d = a - b;
        if (Double.isNaN(d)) {
            return false;
        }
        return (d < 0 ? -d : d) < epsilon;
    }
}
