/*
 * Project Name: Deep learning based on Hadoop (MapReduce)
 * 
 * Class Description: 
 * 	This class tries to solve all the Matrix calculation including element by element 
 * 	multiplication, minus, plus, division and linear algebraic matrix multiplication.
 * 	Other things such as dimension detection, matrix copy and transform between double array
 * 	type and Matrix type object are also included.
 * 
 * Author: Gengtao Jia
 * Last Update Date: Nov. 10, 2013 
 */

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.text.FieldPosition;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StreamTokenizer;

class Matrix{
    private double[][] A;
    private int m, n;
    
    //constructors
    public Matrix (int m, int n) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
    }
    
    public Matrix (int m, int n, double num) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = num;
            }
        }
    }
    
    
    public Matrix (double[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }
    
    public Matrix (double value[], int m) {
        this.m = m;
        n = (m != 0 ? value.length/m : 0);
        //if m != 0 is ture, n = value.length/m, else n = 0
        if (m * n != value.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = value[i + j * m];
            }
        }
    }
    
    //error detection
    public Matrix (double[][] A) {
        m = A.length;
        n = A[0].length;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All Row length should be the same");
            }
        }
        this.A = A;
    }
    
    //check whether two matrixes can be performed arithmetic computing with each other.
    private void checkMatrixDimensions (Matrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }
    
    //public Matrix Calculation Methods
    //copy Matrix type and return Matrix type object
    public static Matrix constructWithCopy(double[][] A) {
        int m = A.length;
        int n = A[0].length;
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException
                ("All rows must have the same length.");
            }
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }
    
    //Make a deep copy of a matrix
    public Matrix copy () {
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return X;
    }
    
    //copy double array type and return double array type
    public double[][] getArrayCopy () {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return C;
    }
    
    //return the double array stored in matrix object
    public double[][] getArray () {
        return A;
    }
    
    //Make a one-dimensional column packed copy of the internal array.
    public double[] getColumnPackedCopy () {
        double[] value = new double[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                value[i + j * m] = A[i][j];
            }
        }
        return value;
    }
    
    //Make a one-dimensional row packed copy of the internal array.
    public double[] getRowPackedCopy () {
        double[] value = new double[m * n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                value[i * n + j] = A[i][j];
            }
        }
        return value;
    }
    
    //get the dimensions of array, or any single element in the array
    public int getRowDimension () {
        return m;
    }
    public int getColumnDimension () {
        return n;
    }
    public double get (int i, int j) {
        return A[i][j];
    }
    
    //set a single element in a double array
    public void set (int i, int j, double value) {
        A[i][j] = value;
    }
    
    //get the transpose of a Matrix
    public Matrix transpose () {
        Matrix X = new Matrix(n,m);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    //return the result of the element by element plus, the result is assigned to another variable.
    public Matrix plus (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }
    
    //return the result of the element by element plus, the result is assigned to original matrix.
    public Matrix plusEquals (Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return this;
    }
    
    //return the result of the element by element minus, the result is assigned to another variable.
    public Matrix minus (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return X;
    }
    
    //return the result of the element by element minus, the result is assigned to original matrix.
    public Matrix minusEquals (Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return this;
    }
    
    //return the result of the element by element multiplication, the result is assigned to original matrix or another variable.
    public Matrix arrayTimes (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return X;
    }
    
    public Matrix arrayTimesEquals (Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return this;
    }
    
    //return the result of the element by element minus, the result is assigned to original matrix or another variable.
    public Matrix arrayRightDivide (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return X;
    }
    
    public Matrix arrayRightDivideEquals (Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return this;
    }
    
    //Multiply a matrix by a scalar, C = s*A or A = s*A
    public Matrix times (double s) {
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = s * A[i][j];
            }
        }
        return X;
    }
    
    public Matrix timesEquals (double s) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s * A[i][j];
            }
        }
        return this;
    }
    
    //Linear algebraic matrix multiplication, A * B
    public Matrix times (Matrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix X = new Matrix(m,B.n);
        double[][] C = X.getArray();
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.A[k][j];
            }
            for (int i = 0; i < m; i++) {
                double[] Arowi = A[i];
                double value = 0;
                for (int k = 0; k < n; k++) {
                    value += Arowi[k]*Bcolj[k];
                }
                C[i][j] = value;
            }
        }
        return X;
    }
}