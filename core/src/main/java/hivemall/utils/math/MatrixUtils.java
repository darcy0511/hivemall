/*
 * Hivemall: Hive scalable Machine Learning Library
 *
 * Copyright (C) 2015 Makoto YUI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hivemall.utils.math;

import hivemall.utils.lang.Preconditions;

import javax.annotation.Nonnull;

public final class MatrixUtils {

    private MatrixUtils() {}

    /**
     * Solve Yule-walker equation by Levinson-Durbin Recursion.
     * 
     * R_j = ∑_{i=1}^{k} A_i R_{j-i} where j = 1..k, R_{-i} = R'_i
     * 
     * @param R autocovariance where |R| >= order
     * @param A coefficient to be solved where |A| >= order + 1
     * @return E variance of prediction error
     */
    @Nonnull
    public static double[] aryule(@Nonnull final double[] R, @Nonnull final double[] A,
            final int order) {
        Preconditions.checkArgument(R.length > order, "|C| MUST be greater than or equals to "
                + order + ": " + R.length);
        Preconditions.checkArgument(A.length >= order + 1, "|A| MUST be greater than or equals to "
                + (order + 1) + ": " + A.length);

        final double[] E = new double[order + 1];
        A[0] = 1.0d;
        E[0] = R[0];
        for (int k = 0; k < order; k++) {
            double lambda = 0.d;
            for (int j = 0; j <= k; j++) {
                lambda -= A[j] * R[k + 1 - j];
            }
            final double Ek = E[k];
            if (Ek == 0.d) {
                lambda = 0.d;
            } else {
                lambda /= Ek;
            }

            for (int n = 0, last = (k + 1) / 2; n <= last; n++) {
                final int i = k + 1 - n;
                double tmp = A[i] + lambda * A[n];
                A[n] += lambda * A[i];
                A[i] = tmp;
            }

            E[k + 1] = Ek * (1.0d - lambda * lambda);
        }

        return E;
    }

    @Deprecated
    @Nonnull
    public static double[] aryule2(@Nonnull final double[] R, @Nonnull final double[] A,
            final int order) {
        Preconditions.checkArgument(R.length > order, "|C| MUST be greater than or equals to "
                + order + ": " + R.length);
        Preconditions.checkArgument(A.length >= order + 1, "|A| MUST be greater than or equals to "
                + (order + 1) + ": " + A.length);

        final double[] E = new double[order + 1];
        A[0] = E[0] = 1.0d;
        A[1] = -R[1] / R[0];
        E[1] = R[0] + R[1] * A[1];
        for (int k = 1; k < order; k++) {
            double lambda = 0.d;
            for (int j = 0; j <= k; j++) {
                lambda -= A[j] * R[k + 1 - j];
            }
            lambda /= E[k];

            final double[] U = new double[k + 2];
            final double[] V = new double[k + 2];
            U[0] = 1.0; // V[0] = 0.0;
            for (int i = 1; i <= k; i++) {
                U[i] = A[i];
                V[k + 1 - i] = A[i];
            }
            V[k + 1] = 1.0; // U[k + 1] = 0.0;            
            for (int i = 0, threshold = k + 2; i < threshold; i++) {
                A[i] = U[i] + lambda * V[i];
            }

            E[k + 1] = E[k] * (1.0d - lambda * lambda);
        }

        return E;
    }

}