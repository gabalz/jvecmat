package jmatrix;

import java.util.Random;

/**
 * Abstract matrix representation.
 */
public abstract class Matrix {

  /**
   * New row constant for the {@link Matrix#create(double...)} constructor.
   */
  public final static double NR = Double.NaN;

  /**
   * Tolerance value.
   */
  public final static double TOL = 2e-8;

  /**
   * Creates a dense matrix using the provided data.
   *
   * @param data elements of the matrix
   * @return matrix which encapsulates <code>data</code> (not copied)
   */
  public static Matrix create(double[][] data) {
    return new DenseMatrix(data);
  }

  /**
   * Creates a dense matrix of given size having uninitialized elements.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @return matrix with size <code>rows</code> x <code>cols</code>
   *         having uninitialized elements
   */
  public static Matrix create(int rows, int cols) {
    assert(rows >= 0 && cols >= 0);
    if (cols > 0 && (rows == 0 || rows > cols)) {
      return create(new double[cols][rows]).T();
    }
    return create(new double[rows][cols]);
  }

  /**
   * Creates a dense matrix of the given <code>values</code>.
   * New rows can be indicated by the {@link Matrix#NR} constant.
   *
   * The <code>values</code> should not contain any <code>Double.NaN</code> value.
   * Empty rows are silently ignored.
   *
   * @param values matrix elements and {@link Matrix#NR} new row indicators
   * @return new matrix with elements from <code>values</code>
   * @throws IllegalArgumentException if row lengths mismatch
   */
  public static Matrix create(double... values) {
    if (values.length == 0) { return create(0, 0); }

    int rows = 0;
    int cols = 0;
    boolean isFirstNR = true;
    for (int i = 1, iprev = 0; i <= values.length; ++i) {
      if (i == values.length || Double.isNaN(values[i])) {
        if (Double.isNaN(values[i-1])) { // empty row
          ++iprev;
        } else { // non-empty row
          ++rows;
          if (isFirstNR) {
            isFirstNR = false;
            cols = i;
          } else if (cols != i-iprev-1) {
            throw new IllegalArgumentException("Length mismatch of row " + rows + "!");
          }
          iprev = i;
        }
      }
    }
    Matrix result = create(rows, cols);
    int i = 0, j = 0;
    for (double value : values) {
      if (!Double.isNaN(value)) {
        result.set(i, j, value);
        if (++j == cols) { j = 0; ++i; } // new row
      }
    }
    return result;
  }

  /**
   * Creates a matrix of given size and sets all elements to <code>value</code>.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param value element initializer value
   * @return matrix with size <code>rows</code> x <code>cols</code>
   *         having elements set to <code>value</code>
   */
  public static Matrix scalars(int rows, int cols, double value) {
    Matrix m = create(rows, cols);
    m.setToScalars(value);
    return m;
  }

  /**
   * Creates a matrix of size <code>rows</code> x <code>cols</code>
   * and initializes its elements to zero.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @return zero matrix of size <code>rows</code> x <code>cols</code>
   */
  public static Matrix zeros(int rows, int cols) {
    return scalars(rows, cols, 0.0);
  }

  /**
   * Creates a matrix of size <code>rows</code> x <code>cols</code>
   * and initializes its elements to zero.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @return all one matrix of size <code>rows</code> x <code>cols</code>
   */
  public static Matrix ones(int rows, int cols) {
    return scalars(rows, cols, 1.0);
  }

  /**
   * Creates a square diagonal matrix
   * using the elements of vector <code>v</code>.
   *
   * @param v vector forming the diagonal
   * @return square diagonal matrix defined by vector <code>v</code>
   */
  public static Matrix diag(Matrix v) {
    if (v.cols() > 1) { v = v.T(); }
    final int n = v.rows();
    double[][] mat = new double[n][n];
    for (int i = 0; i < n; ++i) {
      mat[i][i] = v.get(i, 0);
      for (int j = 0; j < i; ++j)
        mat[i][j] = mat[j][i] = 0.0;
    }
    return Matrix.create(mat);
  }

  /**
   * Creates an identity matrix of size <code>dim</code> x <code>dim</code>.
   *
   * @param dim dimension
   * @return identity matrix of size <code>dim</code> x <code>dim</code>
   */
  public static Matrix eye(int dim) {
    double[][] mat = new double[dim][dim];
    for (int i = 0; i < dim; ++i) {
      mat[i][i] = 1.0;
      for (int j = 0; j < i; ++j) { mat[i][j] = mat[j][i] = 0.0; }
    }
    return Matrix.create(mat);
  }

  /**
   * Creates a random matrix of size <code>rows</code> x <code>cols</code>
   * drawing its elements from the uniform distribution on [0,1].
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param rng random number generator
   * @return uniform random matrix
   *         of size <code>rows</code> x <code>cols</code>
   */
  public static Matrix rand(int rows, int cols, Random rng) {
    Matrix m = Matrix.create(rows, cols);
    m.setToRand(rng);
    return m;
  }

  /**
   * Creates a random matrix of size <code>rows</code> x <code>cols</code>
   * drawing its elements from the standard normal distribution.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param rng random number generator
   * @return standard normal random matrix
   *         of size <code>rows</code> x <code>cols</code>
   */
  public static Matrix randN(int rows, int cols, Random rng) {
    Matrix m = Matrix.create(rows, cols);
    m.setToRandN(rng);
    return m;
  }

  //----------------------------------------------------------------------------
  // basic matrix `interface'

  /**
   * Returns <code>true</code> if the matrix is empty (having no rows or columns).
   *
   * @return <code>true</code> if the matrix is empty
   */
  public boolean isEmpty() {
    return (rows() == 0) || (cols() == 0);
  }

  /**
   * Returns the number of rows of the matrix.
   *
   * @return number of rows of the matrix
   */
  public abstract int rows();

  /**
   * Returns the number of columns of the matrix.
   *
   * @return number of columns of the matrix
   */
  public abstract int cols();

  /**
   * Returns the element of the matrix at row <code>i</code>
   * and column <code>j</code>.
   *
   * @param i the row index of the matrix element
   * @param j the column index of the matrix element
   * @return the (i,j) element of the matrix
   */
  public abstract double get(int i, int j);

  /**
   * Set the matrix element at row <code>i</code> and column <code>j</code>
   * to <code>value</code>.
   *
   * @param i the row index of the matrix element
   * @param j the column index of the matrix element
   * @param value the new value
   */
  public abstract void set(int i, int j, double value);

  //----------------------------------------------------------------------------
  // NaN and Inf handling

  /**
   * Returns <code>true</code> if the matrix has a NaN element.
   *
   * @return <code>true</code> if there is a NaN element
   */
  public boolean hasNaN() {
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        if (Double.isNaN(get(i,j))) { return true; }
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the matrix has an infinite element.
   *
   * @return <code>true</code> if there is an infinite element
   */
  public boolean hasInf() {
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        if (Double.isInfinite(get(i,j))) { return true; }
      }
    }
    return false;
  }

  /**
   * Replaces the NaN and infinite elements by the specified values.
   *
   * @param nan replacement value for NaN elements
   * @param negInf replacement value for negative infinity elements
   * @param posInf replacement value for positive infinity elements
   */
  public void replaceNaNandInf(double nan, double negInf, double posInf) {
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        double e = get(i,j);
        if (Double.isNaN(e)) { set(i, j, nan); }
        else if (Double.POSITIVE_INFINITY == e) { set(i, j, posInf); }
        else if (Double.NEGATIVE_INFINITY == e) { set(i, j, negInf); }
      }
    }
  }

  //----------------------------------------------------------------------------
  // copy

  /**
   * Copies the matrix into <code>result</code>.
   *
   * @param result appropriately sized storage for the copy (not <code>null</code>)
   * @param rowOffset row offset in <code>result</code>
   * @param colOffset column offset in <code>result</code>
   * @return copy of the matrix
   */
  public Matrix copy(Matrix result, int rowOffset, int colOffset) {
    final int rows = rows(), cols = cols();
    assert(result != null
           && result.rows() >= rowOffset+rows
           && result.cols() >= colOffset+cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        result.set(rowOffset+i, colOffset+j, get(i,j));
      }
    }
    return result;
  }

  /**
   * Returns a copy of the matrix placed into <code>result</code>.
   * The operation is skipped if <code>result</code> is equal to <code>this</code>.
   *
   * @param result appropriately sized storage for the copy (not <code>null</code>)
   * @return copy of the matrix
   */
  public Matrix copy(Matrix result) {
    if (result != this) {
      copy(result, 0, 0);
    }
    return result;
  }

  /**
   * Returns a new copy.
   *
   * @return a copy of the matrix
   */
  public Matrix copy() {
    return copy(create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // block matrix operations

  /**
   * Creates a block diagonal matrix.
   *
   * @param matrices matrices to be used for the blocks
   * @return block diagonal matrix
   */
  public static Matrix blkdiag(Matrix... matrices) {
    if (matrices.length == 0) { return create(0, 0); }

    int rows = 0, cols = 0;
    for (Matrix m : matrices) {
      rows += m.rows();
      cols += m.cols();
    }

    Matrix result = zeros(rows, cols);
    rows = 0; cols = 0;
    for (Matrix m : matrices) {
      m.copy(result, rows, cols);
      rows += m.rows();
      cols += m.cols();
    }
    return result;
  }

  /**
   * Concatenates <code>matrices</code> horizontally.
   *
   * @param matrices matrices to be used concatenated horizontally
   * @return horizontally concatenated matrix
   */
  public static Matrix horzcat(Matrix... matrices) {
    if (matrices.length == 0) { return create(0, 0); }

    final int rows = matrices[0].rows();
    int cols = 0;
    for (Matrix m : matrices) {
      assert (rows == m.rows());
      cols += m.cols();
    }

    Matrix result = create(rows, cols);
    cols = 0;
    for (Matrix m : matrices) {
      m.copy(result, 0, cols);
      cols += m.cols();
    }
    return result;
  }

  /**
   * Concatenates <code>matrices</code> vertically.
   *
   * @param matrices matrices to be used concatenated vertically
   * @return vertically concatenated matrix
   */
  public static Matrix vertcat(Matrix... matrices) {
    if (matrices.length == 0) { return create(0, 0); }

    final int cols = matrices[0].cols();
    int rows = 0;
    for (Matrix m : matrices) {
      assert (cols == m.cols());
      rows += m.rows();
    }

    Matrix result = create(rows, cols);
    rows = 0;
    for (Matrix m : matrices) {
      m.copy(result, rows, 0);
      rows += m.rows();
    }
    return result;
  }

  //----------------------------------------------------------------------------

  /**
   * Sets all elements to <code>c</code>.
   *
   * @param c the new value for all elements
   * @return <code>this</code> matrix
   */
  public Matrix setToScalars(double c) {
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        set(i, j, c);
      }
    }
    return this;
  }

  /**
   * Sets all elements to zero.
   *
   * @return <code>this</code> matrix
   */
  public Matrix setToZeros() {
    return setToScalars(0.0);
  }

  /**
   * Sets all elements to one.
   *
   * @return <code>this</code> matrix
   */
  public Matrix setToOnes() {
    return setToScalars(1.0);
  }

  /**
   * Set the diagonal elements to one and all other elements to zero.
   * If the matrix is square, this sets the identity matrix.
   *
   * @return <code>this</code> matrix
   */
  public Matrix setToEye() {
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        set(i, j, i==j ? 1.0 : 0.0);
      }
    }
    return this;
  }

  /**
   * Sets all elements randomly
   * drawing the new values from the uniform distribution on [0,1].
   *
   * @param rng random number generator (not <code>null</code>)
   * @return <code>this</code> matrix
   */
  public Matrix setToRand(Random rng) {
    assert (rng != null);
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        set(i, j, rng.nextDouble());
      }
    }
    return this;
  }

  /**
   * Sets all elements randomly
   * drawing the new values from the standard normal distribution.
   *
   * @param rng random number generator (not <code>null</code>)
   * @return <code>this</code> matrix
   */
  public Matrix setToRandN(Random rng) {
    assert (rng != null);
    final int rows = rows(), cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        set(i, j, rng.nextGaussian());
      }
    }
    return this;
  }

  //----------------------------------------------------------------------------
  // entrywise operations

  /**
   * Transforms the matrix elementwise by a unary operation <code>op</code>.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix, and might be set to <code>this</code> for in-place operation.
   *
   * @param op unary operation
   * @param result storage of the result (not <code>null</code>)
   * @return transformed matrix in <code>result</code>
   */
  public Matrix ewu(UnaryOperation op, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == rows && result.cols() == cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        result.set(i, j, op.apply(get(i,j)));
      }
    }
    return result;
  }

  /**
   * Transforms the matrix elementwise by a unary operation <code>op</code>.
   *
   * @param op unary operation
   * @return transformed matrix
   */
  public Matrix ewu(UnaryOperation op) {
    return ewu(op, create(rows(), cols()));
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using value <code>v</code> for the first argument.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix, and might be set to <code>this</code> for in-place operation.
   *
   * @param op binary operation
   * @param v transforming value
   * @param result storage of the result (not <code>null</code>)
   * @return transformed matrix in <code>result</code>
   */
  public Matrix ewb1(BinaryOperation op, double v, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == rows && result.cols() == cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        result.set(i, j, op.apply(v, get(i,j)));
      }
    }
    return result;
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using value <code>v</code> for the first argument.
   *
   * @param op binary operation
   * @param v transforming value
   * @return transformed matrix
   */
  public Matrix ewb1(BinaryOperation op, double v) {
    return ewb1(op, v, create(rows(), cols()));
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using value <code>v</code> for the second argument.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix, and might be set to <code>this</code> for in-place operation.
   *
   * @param op binary operation
   * @param v transforming value
   * @param result storage of the result (not <code>null</code>)
   * @return transformed matrix <code>result</code>
   */
  public Matrix ewb2(BinaryOperation op, double v, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == rows && result.cols() == cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        result.set(i, j, op.apply(get(i,j), v));
      }
    }
    return result;
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using value <code>v</code> for the second argument.
   *
   * @param op binary operation
   * @param v transforming value
   * @return transformed matrix
   */
  public Matrix ewb2(BinaryOperation op, double v) {
    return ewb2(op, v, create(rows(), cols()));
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using the elements of matrix <code>m</code> for the second argument.
   *
   * Matrix <code>m</code> has to have either the same size as <code>this</code>,
   * or has to be a row/column vector with appropriate size for singleton expansion.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix, and might be set to <code>this</code> for in-place operation.
   *
   * @param op binary operation
   * @param m transforming matrix
   * @param result storage of the result (not <code>null</code>)
   * @return transformed matrix <code>result</code>
   */
  public Matrix ewb(BinaryOperation op, Matrix m, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == rows && result.cols() == cols);
    if (m.rows() == 1) {
      assert(m.cols() == cols);
      for (int j = 0; j < cols; ++j) {
        double v = m.get(0,j);
        for (int i = 0; i < rows; ++i) {
          result.set(i, j, op.apply(get(i,j), v));
        }
      }
    }
    else if (m.cols() == 1) {
      assert(m.rows() == rows);
      for (int i = 0; i < rows; ++i) {
        double v = m.get(i,0);
        for (int j = 0; j < cols; ++j) {
          result.set(i, j, op.apply(get(i,j), v));
        }
      }
    }
    else {
      assert(m.rows() == rows && m.cols() == cols);
      for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
          result.set(i, j, op.apply(get(i,j), m.get(i,j)));
        }
      }
    }
    return result;
  }

  /**
   * Transforms the matrix elementwise by a binary operation <code>op</code>
   * using the elements of matrix <code>m</code> for the second argument.
   *
   * Matrix <code>m</code> has to have either the same size as <code>this</code>,
   * or has to be a row/column vector with appropriate size for singleton expansion.
   *
   * @param op binary operation
   * @param m transforming matrix
   * @return transformed matrix
   */
  public Matrix ewb(BinaryOperation op, Matrix m) {
    return ewb(op, m, create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // diagonal operations

  /**
   * Returns the diagonal elements as a vector.
   * Otherwise, the length of <code>result</code> has to match the number
   * of diagonal elements (the minimum of the number of rows and columns).
   *
   * @param result storage of the result (not <code>null</code>)
   * @return vector of the diagonal elements
   */
  public Matrix getDiag(Matrix result) {
    int n = Math.min(rows(), cols());
    assert (result != null);
    Matrix diag = (result.cols() == 1) ? result : result.T();
    assert(diag.rows() == n && diag.cols() == 1);
    for (int i = 0; i < n; ++i) { diag.set(i, 0, get(i,i)); }
    return result;
  }

  /**
   * Returns the diagonal elements in a new column vector.
   *
   * @return column vector of the diagonal elements
   */
  public Matrix getDiag() {
    return getDiag(Matrix.create(Math.min(rows(), cols()), 1));
  }

  /**
   * Returns the product of the diagonal elements.
   *
   * @return product of diagonal elements
   */
  public double prodDiag() {
    final int limit = Math.min(rows(), cols());
    double prod = 1.0;
    for (int i = 0; i < limit; ++i) { prod *= get(i,i); }
    return prod;
  }

  /**
   * Returns the trace (sum of diagonal elements).
   *
   * @return trace of <code>this</code> matrix
   */
  public double trace() {
    double trace = 0.0;
    final int limit = Math.min(rows(), cols());
    for (int i = 0; i < limit; ++i) { trace += get(i,i); }
    return trace;
  }

  /**
   * Returns the trace of the result given by <code>this</code> matrix
   * multiplied with matrix <code>m</code> on the right.
   *
   * The row number of <code>m</code> has to be equal to the column number
   * of <code>this</code> matrix.
   *
   * @param m matrix multiplying on the right (not <code>null</code>)
   * @return trace of <code>this</code> times <code>m</code>
   */
  public double traceMul(Matrix m) {
    final int cols = cols();
    assert (m != null && cols == m.rows());
    double trace = 0.0;
    final int limit = Math.min(rows(), m.cols());
    for (int i = 0; i < limit; ++i) {
      for (int j = 0; j < cols; ++j) {
        trace += get(i,j) * m.get(j,i);
      }
    }
    return trace;
  }

  //----------------------------------------------------------------------------
  // triangular submatrix selection

  /**
   * Returns the lower triangular part of the matrix (in <code>result</code>).
   * This includes all elements under and on the diagonal selected by
   * <code>offset</code>. For positive/negative values of <code>offset</code>,
   * the selected diagonal is above/under the main diagonal, respectively.
   * 
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix.
   *
   * @param offset offset of the main diagonal
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> holding the lower triangular part
   */
  public Matrix getTriL(int offset, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == rows && result.cols() == cols);
    int jend = Math.min(offset+1, cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < jend; ++j) {
        result.set(i, j, get(i,j));
      }
      if (jend >= 0) {
        for (int j = jend; j < cols; ++j) {
          result.set(i, j, 0.0);
        }
      }
      if (jend < cols) { ++jend; }
    }
    return result;
  }

  /**
   * Returns the lower triangular part of the matrix (in new matrix).
   * This includes all elements under and on the diagonal selected by
   * <code>offset</code>. For positive/negative values of <code>offset</code>,
   * the selected diagonal is above/under the main diagonal, respectively.
   * 
   * @param offset offset of the main diagonal
   * @return new matrix holding the lower triangular part
   */
  public Matrix getTriL(int offset) {
    return getTriL(offset, create(rows(), cols()));
  }

  /**
   * Returns the lower triangular part of the matrix (in <code>result</code>).
   * This includes all elements under and on the main diagonal.
   * 
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> holding the lower triangular part
   */
  public Matrix getTriL(Matrix result) {
    return getTriL(0, result);
  }

  /**
   * Returns the lower triangular part of the matrix (in new matrix).
   * This includes all elements under and on the main diagonal.
   * 
   * @return new matrix holding the lower triangular part
   */
  public Matrix getTriL() {
    return getTriL(0);
  }

  /**
   * Returns the upper triangular part of the matrix (in <code>result</code>).
   * This includes all elements under and on the diagonal selected by
   * <code>offset</code>. For positive/negative values of <code>offset</code>,
   * the selected diagonal is above/under the main diagonal, respectively.
   * 
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix.
   *
   * @param offset offset of the main diagonal
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> holding the upper triangular part
   */
  public Matrix getTriU(int offset, Matrix result) {
    return T().getTriL(-offset, result).T();
  }

  /**
   * Returns the upper triangular part of the matrix (in new matrix).
   * This includes all elements under and on the diagonal selected by
   * <code>offset</code>. For positive/negative values of <code>offset</code>,
   * the selected diagonal is above/under the main diagonal, respectively.
   * 
   * @param offset offset of the main diagonal
   * @return new matrix holding the upper triangular part
   */
  public Matrix getTriU(int offset) {
    return getTriU(offset, create(rows(), cols()));
  }

  /**
   * Returns the upper triangular part of the matrix (in <code>result</code>).
   * This includes all elements under and on the main diagonal.
   * 
   * Matrix <code>result</code> has to have the same size as <code>this</code>
   * matrix.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> holding the upper triangular part
   */
  public Matrix getTriU(Matrix result) {
    return getTriU(0, result);
  }

  /**
   * Returns the upper triangular part of the matrix (in new matrix).
   * This includes all elements under and on the main diagonal.
   * 
   * @return new matrix holding the upper triangular part
   */
  public Matrix getTriU() {
    return getTriU(0);
  }

  //----------------------------------------------------------------------------
  // submatrix selection

  /**
   * Returns the submatrix having rows from <code>iF</code> to <code>iT</code>
   * and columns from <code>jF</code> to <code>jT</code> (all inclusive).
   * A new matrix is created if <code>result</code> is <code>null</code>.
   * Otherwise, the size of <code>result</code> has to match the submatrix.
   *
   * @param iF index of the first row
   * @param iT index of the last row
   * @param jF index of the first column
   * @param jT index of the last column
   * @param result storage of the result or <code>null</code>
   * @return submatrix formed by rows from <code>iF</code> to <code>iT</code>
   *                   and columns from <code>jF</code> to <code>jT</code>
   */
  public Matrix getMat(int iF, int iT, int jF, int jT, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (0 <= iF && iF <= iT && iT < rows);
    assert (0 <= jF && jF <= jT && jT < cols);
    if (result == null) { result = create(iT-iF+1, jT-jF+1); }
    assert (result.rows() == iT-iF+1);
    assert (result.cols() == jT-jF+1);
    int i, j, ir, jr;
    for (i = iF, ir = 0; i <= iT; ++i, ++ir) {
      for (j = jF, jr = 0; j <= jT; ++j, ++jr) {
        result.set(ir, jr, get(i,j));
      }
    }
    return result;
  }

  /**
   * Returns the submatrix having rows from <code>iF</code> to <code>iT</code>
   * and columns from <code>jF</code> to <code>jT</code> (all inclusive).
   *
   * @param iF index of the first row
   * @param iT index of the last row
   * @param jF index of the first column
   * @param jT index of the last column
   * @return submatrix formed by rows from <code>iF</code> to <code>iT</code>
   *                   and columns from <code>jF</code> to <code>jT</code>
   */
  public Matrix getMat(int iF, int iT, int jF, int jT) {
    return getMat(iF, iT, jF, jT, null);
  }

  /**
   * Sets the region specified by rows from <code>iF</code> to <code>iT</code>
   * and columns from <code>jF</code> to <code>jT</code> (all inclusive)
   * using matrix <code>m</code>.
   * Matrix <code>m</code> has to be the same size as the specified region.
   *
   * @param iF index of the first row
   * @param iT index of the last row
   * @param jF index of the first column
   * @param jT index of the last column
   * @param m matrix containing the new elements
   * @return <code>this</code> matrix
   */
  public Matrix setMat(int iF, int iT, int jF, int jT, Matrix m) {
    final int rows = rows(), cols = cols();
    assert (0 <= iF && iF <= iT && iT < rows);
    assert (0 <= jF && jF <= jT && jT < cols);
    assert (m != null && m.rows() == iT-iF+1 && m.cols() == jT-jF+1);
    for (int i = 0, ii = iF; ii <= iT; ++i, ++ii) {
      for (int j = 0, jj = jF; jj <= jT; ++j, ++jj) {
        set(ii, jj, m.get(i, j));
      }
    }
    return this;
  }

  //----------------------------------------------------------------------------
  // addition

  /**
   * Matrix-matrix addition (in <code>result</code>).
   * Adds matrix <code>m</code> to <code>this</code> matrix.
   *
   * Matrices <code>m</code> and <code>result</code> have to have the same size
   * as <code>this</code>.
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * or <code>m</code> providing in-place operation.
   *
   * @param m matrix to add (not <code>null</code>)
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the sum of
   *         <code>this</code> and <code>m</code>
   */
  public Matrix add(Matrix m, Matrix result) {
    return ewb(BasicBinaryOperation.ADD, m, result);
  }

  /**
   * Matrix-matrix addition (in new matrix).
   * Adds matrix <code>m</code> to <code>this</code> matrix in a new matrix.
   *
   * Matrix <code>m</code> has to have the same size as <code>this</code>.
   *
   * @param m matrix to add (not <code>null</code>)
   * @return the sum of <code>this</code> and <code>m</code> in a new matrix
   */
  public Matrix add(Matrix m) {
    return add(m, create(rows(), cols()));
  }

  /**
   * Matrix-constant addition (in <code>result</code>).
   * Adds constant <code>c</code> to all elements of <code>this</code> matrix.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * providing in-place operation.
   *
   * @param c constant to add
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the values of
   *         <code>this</code> shifted by <code>c</code>
   */
  public Matrix add(double c, Matrix result) {
    return ewb2(BasicBinaryOperation.ADD, c, result);
  }

  /**
   * Constant addition (in new matrix). Adds constant <code>c</code> to all
   * elements of <code>this</code> matrix.
   *
   * @param c constant to add
   * @return new matrix with values of <code>this</code> shifted by <code>c</code>
   */
  public Matrix add(double c) {
    return add(c, create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // subtraction

  /**
   * Matrix-matrix subtraction (in <code>result</code>).
   * Subtracts matrix <code>m</code> from <code>this</code> matrix.
   *
   * Matrices <code>m</code> and <code>result</code> have to have the same size
   * as <code>this</code>.
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * or <code>m</code> providing in-place operation.
   *
   * @param m matrix to subtract (not <code>null</code>)
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the difference of
   *         <code>this</code> and <code>m</code>
   */
  public Matrix sub(Matrix m, Matrix result) {
    return ewb(BasicBinaryOperation.SUB, m, result);
  }

  /**
   * Matrix-matrix subtraction (in new matrix).
   * Subtracts matrix <code>m</code> from <code>this</code> matrix
   * in a new matrix.
   *
   * Matrix <code>m</code> has to have the same size as <code>this</code>.
   *
   * @param m matrix to subtract (not <code>null</code>)
   * @return the difference of <code>this</code> and <code>m</code>
   *         in a new matrix
   */
  public Matrix sub(Matrix m) {
    return sub(m, create(rows(), cols()));
  }

  /**
   * Matrix-constant subtraction (in <code>result</code>).
   * Subtracts constant <code>c</code> from all elements of <code>this</code>
   * matrix.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * providing in-place operation.
   *
   * @param c constant to subtract
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the values of
   *         <code>this</code> shifted by <code>-c</code>
   */
  public Matrix sub(double c, Matrix result) {
    return add(-c, result);
  }

  /**
   * Constant subtraction (in new matrix). Subtracts constant <code>c</code>
   * from all elements of <code>this</code> matrix.
   *
   * @param c constant to subtract
   * @return new matrix with values of <code>this</code> shifted by <code>-c</code>
   */
  public Matrix sub(double c) {
    return sub(c, create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // multiplication

  /**
   * Matrix-constant multiplication (in <code>result</code>). Multiplies all
   * elements of <code>this</code> matrix by constant <code>c</code>.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * providing in-place operation.
   *
   * @param c constant to multiply with
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the values of
   *         <code>this</code> multiplied by <code>c</code>
   */
  public Matrix mul(double c, Matrix result) {
    return ewb2(BasicBinaryOperation.MUL, c, result);
  }

  /**
   * Constant multipication (in new matrix). Multiplies all elements of
   * <code>this</code> by constant <code>c</code>.
   *
   * @param c constant to divide with
   * @return new matrix with values of <code>this</code> multiplied by <code>c</code>
   */
  public Matrix mul(double c) {
    return mul(c, create(rows(), cols()));
  }

  /**
   * Matrix-matrix multiplication (in <code>result</code>).
   *
   * The row number of matrix <code>m</code> has to match the column number
   * of <code>this</code> matrix.
   * The size of <code>result</code> has to match the row number of
   * <code>this</code> matrix and the column number of <code>m</code>.
   *
   * @param m matrix multiplier from the right
   *        (not <code>null</code> and not equal to <code>result</code>)
   * @param result storage of the result
   *        (not <code>null</code>
   *         and not equal to <code>this</code> or <code>m</code>)
   * @return <code>result</code> having <code>this</code> matrix multiplied
   *         by matrix <code>m</code> from the right
   */
  public Matrix mul(Matrix m, Matrix result) {
    final int rows = rows(), cols = cols(), mcols = m.cols();
    assert (m != null && m.rows() == cols);
    assert (result != null && result != this && result != m);
    assert (result.rows() == rows && result.cols() == mcols);
    if (mcols <= rows) {
      double tik;
      for (int i = 0; i < rows; ++i) {
        tik = get(i,0); // k = 0
        for (int j = 0; j < mcols; ++j) { // initialize "result[i:*]"
          result.set(i, j, tik * m.get(0,j));
        }
        for (int k = 1; k < cols; ++k) {
          tik = get(i,k);
          for (int j = 0; j < mcols; ++j) {
            result.set(i, j, result.get(i,j) + tik * m.get(k,j));
          }
        }
      }
    }
    else {
      m.T().mul(T(), result.T());
    }
    return result;
  }

  /**
   * Matrix-matrix multiplication (in new matrix).
   *
   * The row number of matrix <code>m</code> has to match the column number
   * of <code>this</code> matrix.
   *
   * @param m matrix multiplier from the right
   * @return <code>result</code> having <code>this</code> matrix multiplied
   *         by matrix <code>m</code> from the right
   */
  public Matrix mul(Matrix m) {
    return mul(m, create(rows(), m.cols()));
  }

  /**
   * Permutes a vector or the columns of a matrix,
   * i.e. multiplying the matrix from the right with a permutation matrix
   * represented by <code>p</code> (in <code>result</code>).
   *
   * The length of permutation <code>p</code> has to be equal to the column
   * number of <code>this</code> matrix.
   * Furthermore, <code>result</code> has to have the same size
   * as <code>this</code> matrix.
   *
   * @param p permutation (not <code>null</code>)
   * @param result storage of the result (not <code>null</code>
   *                                      and not equal to <code>this</code>)
   * @return <code>result</code> having <code>this</code> matrix multiplied from
   *         the right with a permutation matrix represented by <code>p</code>
   * @see Permutation#mul(Matrix, Matrix)
   */
  public Matrix mul(Permutation p, Matrix result) {
    assert (result != null && result != this &&
            result.rows() == rows() && result.cols() == cols());
    Matrix m = this, r = result;
    if (m.rows() > 1 && m.cols() == 1) {
      m = m.T();
      r = r.T();
    }
    final int rows = m.rows(), cols = m.cols();
    assert (p != null && p.length() == cols);
    for (int j = 0; j < cols; ++j) {
      int col = p.get(j);
      for (int i = 0; i < rows; ++i) {
        r.set(i, j, m.get(i, col));
      }
    }
    return result;
  }

  /**
   * Permutes the columns of the matrix, i.e. multiplying the matrix from
   * the right with a permutation matrix represented by <code>p</code>
   * (in new matrix).
   *
   * The length of permutation <code>p</code> has to be equal to the column
   * number of <code>this</code> matrix.
   *
   * @param p permutation (not <code>null</code>)
   * @return new matrix being equal to <code>this</code> matrix multiplied from
   *         the right with a permutation matrix represented by <code>p</code>
   * @see Permutation#mul(Matrix)
   */
  public Matrix mul(Permutation p) {
    return mul(p, create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // division

  /**
   * Matrix-constant division (in <code>result</code>). Divides all elements of
   * <code>this</code> matrix by constant <code>c</code>.
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * providing in-place operation.
   *
   * @param c constant to divide with
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the values of
   *         <code>this</code> divided by <code>c</code>
   */
  public Matrix div(double c, Matrix result) {
    return mul(1.0 / c, result);
  }

  /**
   * Constant division (in new matrix). Divides all elements of
   * <code>this</code> by constant <code>c</code>.
   *
   * @param c constant to divide with
   * @return new matrix with values of <code>this</code> divided by <code>c</code>
   */
  public Matrix div(double c) {
    return div(c, create(rows(), cols()));
  }

  /**
   * Entrywise multiplication (Hadamard product) by matrix <code>m</code>
   * (in <code>result</code>).
   *
   * Matrices <code>v</code> and <code>result</code> have to have the same size
   * as <code>this</code>.
   * The <code>result</code> parameter can be also set to <code>this</code>
   * providing in-place operation.
   *
   * @param m matrix to multiply with entrywise (not <code>null</code>)
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> having the elements of <code>this</code>
   *         and <code>m</code> multiplied entrywise
   */
  public Matrix emul(Matrix m, Matrix result) {
    return ewb(BasicBinaryOperation.MUL, m, result);
  }

  /**
   * Entrywise multiplication (Hadamard product) by matrix <code>m</code>
   * (in new matrix).
   *
   * Matrix <code>result</code> has to have the same size as <code>this</code>.
   *
   * @param m matrix to multiply with entrywise (not <code>null</code>)
   * @return new matrix having the elements of <code>this</code>
   *         and <code>m</code> multiplied entrywise
   */
  public Matrix emul(Matrix m) {
    return emul(m, create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // matrix norms

  /**
   * Computes the 1-norm (maximum absolute column sum).
   *
   * @return 1-norm of <code>this</code> matrix
   */
  public double norm1() {
    final int rows = rows(), cols = cols();
    double maxs = 0.0;
    for (int j = 0; j < cols; ++j) {
      double s = 0.0;
      for (int i = 0; i < rows; ++i) { s += Math.abs(get(i,j)); }
      if (s > maxs) { maxs = s; }
    }
    return maxs;
  }

  /**
   * Computes the infinity-norm (maximum absolute row sum).
   *
   * @return infinity-norm of <code>this</code> matrix
   */
  public double normI() {
    final int rows = rows(), cols = cols();
    double maxs = 0.0;
    for (int i = 0; i < rows; ++i) {
      double s = 0.0;
      for (int j = 0; j < cols; ++j) { s += Math.abs(get(i,j)); }
      if (s > maxs) { maxs = s; }
    }
    return maxs;
  }

  /**
   * Computes the Frobenius norm (maximum absolute row sum).
   *
   * @return Frobenius norm of <code>this</code> matrix
   */
  public double normF() {
    final int rows = rows(), cols = cols();
    double s = 0.0;
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        double v = get(i,j);
        s += v * v;
      }
    }
    return Math.sqrt(s);
  }

  //----------------------------------------------------------------------------
  // vector norms of rows and columns

  /**
   * Computes the 1-norm of each row.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> 1-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNorms1(Matrix result) {
    final int rows = rows(), cols = cols();
    assert (1 <= cols);
    assert (result != null && result.rows() == rows && result.cols() == 1);
    for (int i = 0; i < rows; ++i) {
      double n = 0.0;
      for (int j = 0; j < cols; ++j) {
        n += Math.abs(get(i,j));
      }
      result.set(i, 0, n);
    }
    return result;
  }

  /**
   * Computes the 1-norm of each row.
   *
   * @return <code>result</code> 1-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNorms1() {
    return rowNorms1(create(rows(),1));
  }

  /**
   * Computes the 2-norm of each row.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> 2-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNorms2(Matrix result) {
    final int rows = rows(), cols = cols();
    assert (1 <= cols);
    assert (result != null && result.rows() == rows && result.cols() == 1);
    for (int i = 0; i < rows; ++i) {
      double n = 0.0;
      for (int j = 0; j < cols; ++j) {
        double v = get(i,j);
        n += v*v;
      }
      result.set(i, 0, Math.sqrt(n));
    }
    return result;
  }

  /**
   * Computes the 2-norm of each row.
   *
   * @return <code>result</code> 2-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNorms2() {
    return rowNorms2(create(rows(),1));
  }

  /**
   * Computes the max-norm of each row.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> max-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNormsI(Matrix result) {
    final int rows = rows(), cols = cols();
    assert (1 <= cols);
    assert (result != null && result.rows() == rows && result.cols() == 1);
    for (int i = 0; i < rows; ++i) {
      double n = 0.0;
      for (int j = 0; j < cols; ++j) {
        double v = Math.abs(get(i,j));
        if (n < v) { n = v; }
      }
      result.set(i, 0, n);
    }
    return result;
  }

  /**
   * Computes the max-norm of each row.
   *
   * @return <code>result</code> max-norm of each row of <code>this</code> matrix
   */
  public Matrix rowNormsI() {
    return rowNormsI(create(rows(),1));
  }

  /**
   * Computes the 1-norm of each column.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> 1-norm of each column of <code>this</code> matrix
   */
  public Matrix colNorms1(Matrix result) {
    return T().rowNorms1(result.T()).T();
  }

  /**
   * Computes the 1-norm of each column.
   *
   * @return <code>result</code> 1-norm of each column of <code>this</code> matrix
   */
  public Matrix colNorms1() {
    return colNorms1(create(1,cols()));
  }

  /**
   * Computes the 2-norm of each column.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> 2-norm of each column of <code>this</code> matrix
   */
  public Matrix colNorms2(Matrix result) {
    return T().rowNorms2(result.T()).T();
  }

  /**
   * Computes the 2-norm of each column.
   *
   * @return <code>result</code> 2-norm of each column of <code>this</code> matrix
   */
  public Matrix colNorms2() {
    return colNorms2(create(1,cols()));
  }

  /**
   * Computes the max-norm of each column.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> max-norm of each column of <code>this</code> matrix
   */
  public Matrix colNormsI(Matrix result) {
    return T().rowNormsI(result.T()).T();
  }

  /**
   * Computes the max-norm of each column.
   *
   * @return <code>result</code> max-norm of each column of <code>this</code> matrix
   */
  public Matrix colNormsI() {
    return colNormsI(create(1,cols()));
  }

  //----------------------------------------------------------------------------
  // row and column summations

  /**
   * Returns the sum of the rows (in <code>result</code>).
   *
   * The length of <code>result</code> has to be the column number of
   * <code>this</code> matrix.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return sum of rows
   */
  public Matrix sumRows(Matrix result) {
    final int rows = rows(), cols = cols();
    assert (result != null && result.rows() == 1 && result.cols() == cols);
    for (int j = 0; j < cols; ++j) {
      double s = 0.0;
      for (int i = 0; i < rows; ++i) { s += get(i,j); }
      result.set(0, j, s);
    }
    return result;
  }

  /**
   * Returns the sum of the rows (in new vector).
   *
   * @return sum of rows
   */
  public Matrix sumRows() {
    return sumRows(create(1, cols()));
  }

  /**
   * Returns the sum of the columns (in <code>result</code>).
   *
   * The length of <code>result</code> has to be the row number of
   * <code>this</code> matrix.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return sum of columns
   */
  public Matrix sumCols(Matrix result) {
    return T().sumRows(result.T()).T();
  }

  /**
   * Returns the sum of the columns (in new vector).
   *
   * @return sum of columns
   */
  public Matrix sumCols() {
    return sumCols(create(rows(), 1));
  }

  //----------------------------------------------------------------------------

  /**
   * Returns the dot product of vectors <code>this</code> and <code>v</code>.
   * The orientations of the vectors are ignored, only their lengths have to match.
   *
   * @param v vector to take the dot product with (not <code>null</code>)
   * @return the dot product of vectors <code>this</code> and <code>v</code>
   */
  public double dot(Matrix v) {
    assert (v != null);
    final Matrix v1 = (rows() != 1) ? T() : this;
    final Matrix v2 = (v.rows() != 1) ? v.T() : v;
    assert (v1.rows() == 1);
    final int length = v1.cols();
    assert (v2.rows() == 1 && v2.cols() == length);
    double s = 0.0;
    for (int j = 0; j < length; ++j) { s += v1.get(0,j) * v2.get(0,j); }
    return s;
  }

  //----------------------------------------------------------------------------
  // Cholesky decomposition

  /**
   * Cholesky decomposition of a (symmetric) positive-definite matrix.
   * Returns the result in matrix <code>L</code> having the same size as
   * <code>this</code>. Matrix <code>L</code> can be set to <code>this</code>
   * supporting in-place operation.
   *
   * @param L the lower triangular factor (not <code>null</code>)
   * @return <code>L</code> holding the lower triangular Cholesky factor
   * @throws UnsupportedOperationException if the matrix is not positive-definite
   */
  public Matrix choleskyL(Matrix L) {
    final int n = rows();
    assert (cols() == n);
    assert (L != null && L.rows() == n && L.cols() == n);
    double Ljj, Lij, v;
    for (int j = 0; j < n; ++j) {
      Ljj = get(j,j);
      for (int k = 0; k < j; ++k) {
        v = L.get(j,k);
        Ljj -= v * v;
      }
      if (Ljj <= 0.0) {
        throw new UnsupportedOperationException("Matrix has to be positive-definite.");
      }
      Ljj = Math.sqrt(Ljj);
      L.set(j, j, Ljj);
      Ljj = 1.0 / Ljj;
      if (Double.isInfinite(Ljj)) {
        throw new UnsupportedOperationException("Matrix has to be positive-definite.");
      }

      for (int i = 0; i < j; ++i) {
        L.set(i, j, 0.0); // zero above the diagonal
      }
      for (int i = j+1; i < n; ++i) {
        Lij = get(i,j);
        for (int k = 0; k < j; ++k) {
          Lij -= L.get(i,k) * L.get(j,k);
        }
        L.set(i, j, Lij * Ljj);
      }
    }
    return L;
  }

  /**
   * Cholesky decomposition of a (symmetric) positive-definite matrix.
   *
   * @return the lower triangular Cholesky factor in a new matrix
   * @throws UnsupportedOperationException if the matrix is not positive-definite
   */
  public Matrix choleskyL() {
    return choleskyL(zeros(rows(), cols()));
  }

  /**
   * LDL (Cholesky) decomposition of a (symmetric) positive-definite matrix.
   * Returns the result in matrix <code>L</code> and vector <code>D</code>.
   *
   * The <code>L</code> matrix should have the same size as <code>this</code>.
   * The diagonal elements of <code>L</code> will be set to one.
   * Matrix <code>L</code> can be set to <code>this</code> supporting in-place
   * operation. The length of column vector D has to be equal to the row/column
   * number of <code>this</code> matrix.
   *
   * @param L the lower triangular LDL factor (not <code>null</code>)
   * @param D the diagonal factor as a vector (not <code>null</code>)
   * @throws UnsupportedOperationException if the matrix is not positive-definite
   */
  public void choleskyLD(Matrix L, Matrix D) {
    final int n = rows();
    assert (cols() == n);
    assert (L != null && L.rows() == n && L.cols() == n);
    assert (D != null && D.rows() == n && D.cols() == 1);
    double Dj, Lij, v;
    for (int j = 0; j < n; ++j) {
      Dj = get(j,j);
      for (int k = 0; k < j; ++k) {
        v = L.get(j,k);
        Dj -= v * v * D.get(k,0);
      }
      if (Dj <= 0.0) {
        throw new UnsupportedOperationException("Matrix has to be positive-definite.");
      }
      D.set(j, 0, Dj);
      Dj = 1.0 / Dj;
      if (Double.isInfinite(Dj)) {
        throw new UnsupportedOperationException("Matrix has to be positive-definite.");
      }
      L.set(j, j, 1.0);
            
      for (int i = 0; i < j; ++i) {
        L.set(i, j, 0.0); // zero above the diagonal
      }
      for (int i = j+1; i < n; ++i) {
        Lij = get(i,j);
        for (int k = 0; k < j; ++k) {
          Lij -= L.get(i,k) * L.get(j,k) * D.get(k,0);
        }
        L.set(i, j, Lij * Dj);
      }
    }
  }

  /**
   * LDL (Cholesky) decomposition of a (symmetric) positive-definite matrix.
   *
   * @return <code>{L,D}</code> matrices, where <code>L</code> is the
   *         lower triangular and <code>D</code> is the diagonal factor
   * @throws UnsupportedOperationException if the matrix is not positive-definite
   */
  public Matrix[] choleskyLD() {
    final int n = rows();
    assert (cols() == n);
    Matrix L = zeros(n, n);
    Matrix D = create(n, 1);
    choleskyLD(L, D);
    return new Matrix[]{L, diag(D)};
  }

  //----------------------------------------------------------------------------
  // QR decomposition

  /**
   * QR decomposition of an arbitrary matrix using Hauseholder transformations.
   *
   * Matrix <code>Q</code> can be <code>null</code> in which case its computation
   * is omitted. Otherwise, <code>Q</code> has to be a square matrix of size
   * <code>rows()</code>. Matrix <code>R</code> has to have the same size as
   * <code>this</code> and can be set to <code>this</code> providing in-place
   * operation. Vector <code>tmpV</code> is a temporary storage with length not
   * smaller than <code>min(rows()-1, cols())</code>.
   * Matrices <code>Q</code> and <code>R</code> cannot be the same.
   *
   * @param Q will be set to the orthogonal factor
   *          (<code>null</code> or with size <code>rows()</code> x <code>rows()</code>)
   * @param R will be set to the upper-triangular factor
   *          (not <code>null</code> with size <code>rows()</code> x <code>cols()</code>
   * @param tmpV temporary vector
   *             (not <code>null</code> with size >= <code>min(rows()-1, cols())</code>
   */
  public void QR(Matrix Q, Matrix R, Matrix tmpV) {
    final int rows = rows(), cols = cols();
    assert (Q == null || (Q.rows() == rows && Q.cols() == rows));
    assert (R != null && R != Q && R.rows() == rows && R.cols() == cols);

    final int t = Math.min(rows-1, cols);
    if (tmpV.cols() > tmpV.rows()) { tmpV = tmpV.T(); }
    assert (t <= tmpV.rows());

    copy(R); // this -> R
    for (int k = 0; k < t; ++k) {
      double norm = 0.0;
      for (int i = k; i < rows; ++i) { norm = hypot(norm, R.get(i, k)); }

      if (norm != 0.0) {
        if (R.get(k, k) < 0) norm = -norm;

        for (int i = k; i < rows; ++i) { R.set(i, k, R.get(i, k) / norm); }
        R.set(k, k, R.get(k, k) + 1.0); // 1 <= R[k,k] <= 2

        for (int j = k+1; j < cols; ++j) {
          double s = 0.0;
          for (int i = k; i < rows; ++i) {
            s += R.get(i, k) * R.get(i, j);
          }
          s = -s / R.get(k, k);
          for (int i = k; i < rows; ++i) {
            R.set(i, j, R.get(i, j) + s*R.get(i, k));
          }
        }
      }
      tmpV.set(k, 0, -norm);
    }

    if (Q != null) { // compute Q only if requested
      Q.setToEye();
      for (int k = t-1; k >= 0; --k) {
        for (int j = k; j < rows; ++j) {
          if (R.get(k, k) != 0.0) {
            double s = 0.0;
            for (int i = k; i < rows; ++i) {
              s += R.get(i, k) * Q.get(i, j);
            }
            s = -s / R.get(k, k);
            for (int i = k; i < rows; ++i) {
              Q.set(i, j, Q.get(i, j) + s*R.get(i, k));
            }
          }
        }
      }
    }

    for (int k = 0; k < t; ++k) {
      R.set(k, k, tmpV.get(k,0));

      // ensure R is upper triangular
      for (int i = k+1; i < rows; ++i) { R.set(i, k, 0.0); }
    }
  }

  /**
   * Computes the hypotenuse of a right triangle
   * having legs <code>x</code> and <code>y</code>.
   */
  private static double hypot(double x, double y) {
    final double absX = Math.abs(x);
    final double absY = Math.abs(y);

    double r = 0.0;
    if (absX > absY) {
      r = y/x;
      r = absX * Math.sqrt(1.0 + r*r);
    }
    else if (y != 0.0) {
      r = x/y;
      r = absY * Math.sqrt(1.0 + r*r);
    }
    return r;
  }

  /**
   * QR decomposition of an arbitrary matrix using Hauseholder transformations.
   *
   * @return <code>{Q,R}</code> matrices, where <code>Q</code> is the orthogonal
   *         and <code>R</code> is the upper-triangular factor
   */
  public Matrix[] QR() {
    final int rows = rows(), cols = cols();
    Matrix Q = create(rows, rows);
    Matrix R = zeros(rows, cols);
    QR(Q, R, Matrix.create(Math.min(rows-1, cols), 1));
    return new Matrix[]{Q, R};
  }

  //----------------------------------------------------------------------------
  // LU decomposition

  /**
   * P'LU decomposition of an arbitrary matrix with partial pivoting.
   * This implementation is the Doolittle algorithm.
   *
   * Matrix <code>L</code> will be set to the unit lower triangular factor
   * and matrix <code>U</code> will be set to the upper triangular factor.
   *
   * Matrix <code>L</code> can be set equal to <code>U</code> in which case
   * the unit diagonal elements of <code>L</code> will not be stored.
   * Both <code>L</code> and <code>U</code> can be set to <code>this</code>
   * supporting in-place operation.
   *
   * @param L the unit lower triangular factor, might be row permuted if
   *          <code>P</code> is set to <code>null</code> (not <code>null</code>
   *          of size <code>rows()</code> x <code>min(rows(), cols())</code>)
   * @param U the upper triangular factor (not <code>null</code>
   *          of size <code>min(rows(), cols())</code> x <code>cols()</code>)
   * @param P row permutations of <code>L</code>
   *          (not <code>null</code> with length <code>rows()</code>)
   * @return the number of row swaps (permutations)
   */
  public int LU(Matrix L, Matrix U, Permutation P) {
    final int rows = rows(), cols = cols();

    int minrc = 0;
    Matrix A = null;
    if (rows > cols) {
      minrc = cols;
      A = L;
    }
    else {
      minrc = rows;
      A = U;
    }

    assert (L != null && L.rows() == rows && L.cols() == minrc);
    assert (U != null && U.rows() == minrc && U.cols() == cols);
    assert(P == null || P.length() == rows); // (P == null) is for internal use only

    copy(A);
    if (P != null) { P.setToEye(); }

    int nperms = 0;
    for (int i = 0; i < minrc; ++i) {
      // find the row pivot
      int p = i;
      double pval = Math.abs(A.get(i,i));
      for (int k = i+1; k < rows; ++k) {
        double val = Math.abs(A.get(k,i));
        if (val > pval) {
          p = k;
          pval = val;
        }
      }

      // skip the column if the pivot is zero
      if (pval <= TOL) { continue; }

      // swap rows if necessary
      if (p != i) {
        int j = 0;
        if (A != L) {
          while (j < i) {
            double val = L.get(i,j);
            L.set(i, j, L.get(p,j));
            L.set(p, j, val);
            ++j;
          }
        }
        while (j < cols) {
          double val = A.get(i,j);
          A.set(i, j, A.get(p,j));
          A.set(p, j, val);
          ++j;
        }
        ++nperms;
        if (P != null) { P.swap(i, p); }
      }

      // update L and A
      pval = 1.0 / A.get(i,i);
      for (int k = i+1; k < rows; ++k) {
        double Lki = A.get(k,i) * pval;
        for (int j = i+1; j < cols; ++j) {
          A.set(k, j, A.get(k,j)-Lki*A.get(i,j));
        }
        L.set(k, i, Lki);
      }
    }

    if (L != U) { // ensure U is upper triangular and L is unit lower triangular
      for (int i = 0; i < minrc; ++i) {
        for (int j = 0; j < i; ++j) { U.set(i, j, 0.0); }
        for (int j = i; j < cols; ++j) { U.set(i, j, A.get(i,j)); }
        for (int k = 0; k < i; ++k) { L.set(k, i, 0.0); }
        L.set(i, i, 1.0);
      }
    }
    return nperms;
  }

  /**
   * P'LU decomposition of an arbitrary matrix
   * using Doolittle's method with partial pivoting.
   *
   * Matrix <code>P</code> will be set tp a permutation matrix,
   * matrix <code>L</code> will be set to the unit lower triangular factor
   * and matrix <code>U</code> will be set to the upper triangular factor.
   *
   * @return <code>{L,U,P}</code> matrices, where <code>P</code> is a permutation
   *         matrix, <code>L</code> is the unit lower triangular factor
   *         and <code>U</code> is the upper triangular factor
   */
  public Matrix[] LU() {
    final int rows = rows(), cols = cols();
    final int n = Math.min(rows, cols);
    Matrix L = create(rows, n);
    Matrix U = create(n, cols);
    Permutation P = new Permutation(new int[rows]);
    LU(L, U, P);
    return new Matrix[]{L, U, P.toMatrix()};
  }

  //----------------------------------------------------------------------------
  // back substitution

  /**
   * Back substitution solving L*x = b for x
   * with L being the lower triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The upper triangular part of matrix <code>this</code> is always ignored.
   * When <code>isUnitDiag</code> is <code>true</code>,
   * the diagonal elements are ignored too.
   *
   * @param b right hand side vector
   * @param isUnitDiag unit diagonal indicator
   * @param result storage of the result (not <code>null</code>)
   * @return x as the solution for L*x = b
   */
  Matrix backsL(Matrix b, boolean isUnitDiag, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (rows == cols);
    assert (b != null && b.rows() == rows && b.cols() == 1);
    assert (result != null && result.rows() == rows && result.cols() == 1);
    for (int i = 0; i < rows; ++i) {
      double v = b.get(i,0);
      for (int j = 0; j < i; ++j) {
        v -= get(i,j) * result.get(j,0);
      }
      result.set(i, 0, isUnitDiag ? v : v / get(i,i));
    }
    return result;
  }

  /**
   * Back substitution solving L*x = b for x
   * with L being the lower triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The upper triangular part of matrix <code>this</code> is always ignored.
   * When <code>isUnitDiag</code> is <code>true</code>,
   * the diagonal elements are ignored too.
   *
   * @param b right hand side vector
   * @param isUnitDiag unit diagonal indicator
   * @return x as the solution for L*x = b
   */
  Matrix backsL(Matrix b, boolean isUnitDiag) {
    return backsL(b, isUnitDiag, create(rows(), 1));
  }

  /**
   * Back substitution solving L*x = b for x
   * with L being the lower triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The upper triangular part of matrix <code>this</code> is always ignored.
   *
   * @param b right hand side vector
   * @return x as the solution for L*x = b
   */
  Matrix backsL(Matrix b) {
    return backsL(b, false, create(rows(), 1));
  }

  /**
   * Back substitution solving U*x = b for x
   * with U being the upper triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The lower triangular part of matrix <code>this</code> is always ignored.
   * When <code>isUnitDiag</code> is <code>true</code>,
   * the diagonal elements are ignored too.
   *
   * @param b right hand side vector
   * @param isUnitDiag unit diagonal indicator
   * @param result storage of the result (not <code>null</code>)
   * @return x as the solution for U*x = b
   */
  Matrix backsU(Matrix b, boolean isUnitDiag, Matrix result) {
    final int rows = rows(), cols = cols();
    assert (rows == cols);
    assert (b != null && b.rows() == rows && b.cols() == 1);
    assert (result != null && result.rows() == rows && result.cols() == 1);
    for (int i = rows-1; i >= 0; --i) {
      double v = b.get(i,0);
      for (int j = i+1; j < cols; ++j) {
        v -= get(i,j) * result.get(j,0);
      }
      result.set(i, 0, isUnitDiag ? v : v / get(i,i));
    }
    return result;
  }

  /**
   * Back substitution solving U*x = b for x
   * with U being the upper triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The lower triangular part of matrix <code>this</code> is always ignored.
   * When <code>isUnitDiag</code> is <code>true</code>,
   * the diagonal elements are ignored too.
   *
   * @param b right hand side vector
   * @param isUnitDiag unit diagonal indicator
   * @return x as the solution for U*x = b
   */
  Matrix backsU(Matrix b, boolean isUnitDiag) {
    return backsU(b, isUnitDiag, create(rows(), 1));
  }

  /**
   * Back substitution solving U*x = b for x
   * with U being the upper triangular of square matrix <code>this</code>.
   *
   * Matrix <code>this</code> should be square and non-singular.
   * The lower triangular part of matrix <code>this</code> is always ignored.
   *
   * @param b right hand side vector
   * @param isUnitDiag unit diagonal indicator
   * @return x as the solution for U*x = b
   */
  Matrix backsU(Matrix b) {
    return backsU(b, false, create(rows(), 1));
  }

  //----------------------------------------------------------------------------
  // determinant

  /**
   * Returns the determinant of a square matrix. An LU decomposition is performed
   * for size larger than <code>3x3</code> using only the provided
   * <code>tmpLU</code> storage, which has to have the same size as
   * <code>this</code> matrix.
   *
   * @param tmpLU storage for the LU decomposition
   *              (not <code>null</code> if size is larger than 3x3)
   * @return determinant
   */
  public double det(Matrix tmpLU) {
    final int rows = rows();
    assert (rows == cols());

    switch (rows) {
    case 0: return 1.0;
    case 1: return get(0,0);
    case 2: return get(0,0)*get(1,1) - get(0,1)*get(1,0);
    case 3: return get(0,0) * (get(1,1)*get(2,2) - get(1,2)*get(2,1))
                 + get(0,1) * (get(1,2)*get(2,0) - get(1,0)*get(2,2))
                 + get(0,2) * (get(1,0)*get(2,1) - get(1,1)*get(2,0));
    default:
      assert (tmpLU.rows() == rows && tmpLU.cols() == rows);
      int nperms = LU(tmpLU, tmpLU, null);
      double det = tmpLU.prodDiag();
      if ((nperms & 1) != 0) { det = -det; }
      return det;
    }
  }

  /**
   * Returns the determinant of a square matrix. An LU decomposition is performed
   * for size larger than <code>3x3</code>.
   *
   * @return determinant
   */
  public double det() {
    final int rows = rows();
    assert (rows == cols());
    return det((rows > 3) ? create(rows, rows) : null);
  }

  //----------------------------------------------------------------------------
  // matrix inverse

  /**
   * Computes the inverse of a square matrix (in <code>result</code>).
   *
   * Matrix <code>result</code> and <code>tmpM</code> should be square matrices
   * of the same size as <code>this</code>. The length of permutation <code>tmpP</code>
   * should match the side length of the square matrices.
   *
   * @param result storage of the result
   *               (not <code>null</code> and not equal to <code>this</code>)
   * @param tmpM temporary matrix (not <code>null</code> and not equal to
   *             <code>this</code>, having size of <code>rows() x cols()</code>)
   * @param tmpP temporary permutation
   *             (not <code>null</code> with size of <code>rows()</code>)
   * @return <code>result</code> holding the matrix inverse
   * @throws UnsupportedOperationException if the matrix is singular
   */
  public Matrix inv(Matrix result, Matrix tmpM, Permutation tmpP) {
    assert (rows() == cols());
    assert (result != null && result.rows() == rows() && result.cols() == cols());

    switch (rows()) {
    case 0:
      break;
    case 1: {
      double rdet = 1.0 / get(0,0);
      if (Double.isInfinite(rdet)) {
        throw new UnsupportedOperationException("Matrix is singular.");
      }
      result.set(0, 0, rdet);
      break;
    }
    case 2: {
      double a = get(0,0), b = get(0,1), c = get(1,0), d = get(1,1);
      double rdet = 1.0 / (a*d - b*c);
      if (Double.isInfinite(rdet)) {
        throw new UnsupportedOperationException("Matrix is singular.");
      }
      result.set(0, 0,  d); result.set(0, 1, -b);
      result.set(1, 0, -c); result.set(1, 1,  a);
      result.mul(rdet, result);
      break;
    }
    case 3: {
      double a = get(0,0), b = get(0,1), c = get(0,2);
      double d = get(1,0), e = get(1,1), f = get(1,2);
      double g = get(2,0), h = get(2,1), k = get(2,2);
      double A = e*k-f*h, D = c*h-b*k, G = b*f-c*e;
      double B = f*g-d*k, E = a*k-c*g, H = c*d-a*f;
      double C = d*h-e*g, F = g*b-a*h, K = a*e-b*d;
      double rdet = 1.0 / (a*A + b*B + c*C);
      if (Double.isInfinite(rdet)) {
        throw new UnsupportedOperationException("Matrix is singular.");
      }
      result.set(0, 0, A); result.set(0, 1, D); result.set(0, 2, G);
      result.set(1, 0, B); result.set(1, 1, E); result.set(1, 2, H);
      result.set(2, 0, C); result.set(2, 1, F); result.set(2, 2, K);
      result.mul(rdet, result);
      break;
    }
    default:
      LU(tmpM, tmpM, tmpP);
      tmpM.invLT(result, false); // inv(L)
      tmpM.T().invLT(result.T(), true); // inv(U)
      result.mulUL(tmpM); // tmpM = inv(U)*inv(L)
      tmpM.mul(tmpP.inv(), result); // result = tmpM*inv(tmpP)
    }
    return result;
  }

  /**
   * Inverts a square lower triangular matrix <code>L</code> in
   * </code>result</code>. If <code>useDiag</code> is set, the diagonal is
   * inverted, otherwise the diagonal entries are considered to be one.
   */
  private Matrix invLT(Matrix result, boolean useDiag) {
    final int n = rows(); // == cols()
    for (int i = 0; i < n; ++i) {
      double Rii = 1.0;
      if (useDiag) { Rii /= get(i,i); }
      if (Double.isInfinite(Rii)) {
        throw new UnsupportedOperationException("Matrix is singular.");
      }
      result.set(i, i, Rii);
      for (int j = 0; j < i; ++j) {
        double Rij = 0.0;
        for (int k = j; k < i; ++k) { Rij -= get(i,k) * result.get(k,j); }
        result.set(i, j, Rii * Rij);
      }
    }
    return result;
  }

  /**
   * Multiplication of a upper triangular (U) and a unit lower triangular
   * matrix (L) as U*L, both stored in <code>this</code> square matrix as
   * <code>[L \ U]</code>.
   */
  private Matrix mulUL(Matrix result) {
    final int n = rows(); // == cols()
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        int k = Math.max(i, j);
        double value = 0.0;
        if (k == j) {
          value = get(i,j);
          ++k;
        }
        while (k < n) {
          value += get(i,k) * get(k,j);
          ++k;
        }
        result.set(i, j, value);
      }
    }
    return result;
  }

  /**
   * Computes the inverse of a square matrix (in new matrix).
   *
   * @return the matrix inverse
   * @throws UnsupportedOperationException if the matrix is singular
   */
  public Matrix inv() {
    return inv(create(rows(), cols()),
               create(rows(), cols()),
               new Permutation(new int[rows()]));
  }

  /**
   * Computes the inverse of a (symmetric) positive-definite matrix
   * (in <code>result</code>). Symmetry is not verified, violating this condition
   * might silently produce an invalid result.
   *
   * Matrix <code>result</code> should be a square matrix of the same size as
   * <code>this</code>. Matrix <code>result</code> can also be set to
   * <code>this</code> supporting in-place operation.
   *
   * @param result storage of the result (not <code>null</code>)
   * @return <code>result</code> holding the matrix inverse
   * @throws UnsupportedOperationException if the matrix is singular
   */
  public Matrix invPsd(Matrix result) {
    final int n = rows();
    assert (cols() == n);
    assert (result != null && result.rows() == n && result.cols() == n);
    // Compute the inverse of the Cholesky factor and store its transpose
    // into the upper triangular half of result.
    choleskyL(result);
    result.invLT(result.T(), true);
    // Multiply the upper triangular half of result with its own transpose
    // and store it into the lower triangular half by overwriting the diagonal
    // in the last step.
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j <= i; ++j) {
        double value = 0.0;
        for (int k = i; k < n; ++k) {
          value += result.get(i,k) * result.get(j,k);
        }
        result.set(j, i, value);
      }
    }
    // Mirror the lower triangular half into the upper one.
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < i; ++j) {
        result.set(i, j, result.get(j,i));
      }
    }
    return result;
  }

  /**
   * Computes the inverse of a (symmetric) positive-definite matrix
   * (in new matrix). Symmetry is not verified, violating this condition
   * might silently produce an invalid result.
   *
   * @return the matrix inverse
   * @throws UnsupportedOperationException if the matrix is singular
   */
  public Matrix invPsd() {
    return invPsd(create(rows(), cols()));
  }

  //----------------------------------------------------------------------------
  // singular value decomposition

  private double stable2norm(double x, double y) {
    double r = 0.0, s = 0.0;
    if (x >= y) {
      r = y/x;
      s = x;
    }
    else {
      r = x/y;
      s = y;
    }
    return s * Math.sqrt(1.0 + r*r);
  }

  /**
   * Reduced singular value decomposition. The zero singular values in
   * <code>S</code> are placed last and their corresponding vectors in
   * <code>U</code> and <code>V</code> are set to zero.
   * The nonzero singular values are not ordered.
   *
   * Implementation:
   * J. Demmel, K. Veselic, Jacobi's method is more accurate than QR,
   * Journal on Matrix Analysis and Applications, 1992, Algorithm 4.1.
   *
   * @param U left singular vectors
   *        (size: rows x min(rows,cols), can be set to <code>this</code>)
   * @param S singular values (size: min(rows,cols) x 1)
   * @param V right singular vectors
   *        (size: cols x min(rows,cols), can be set to <code>this.T()</code>)
   * @return rank of <code>this</code> matrix
   */
  public int reducedSVD(Matrix U, Matrix S, Matrix V) {
    if (rows() < cols()) {
      return T().reducedSVD(V, S, U);
    }
    final int rows = rows(), cols = cols();
    assert (rows >= cols);
    assert (U != null && U.rows() == rows && U.cols() == cols);
    assert (S != null && S.rows() == cols && S.cols() == 1);
    assert (V != null && V.rows() == cols && V.cols() == cols);

    // initialization
    copy(U);
    V.setToEye();

    // scaling
    double scale = 0.0;
    for (int i = 0; i < cols; ++i) {
      for (int k = 0; k < rows; ++k) {
        double s = Math.abs(U.get(k,i));
        if (s > scale) { scale = s; }
      }
    }
    if (scale > 0.0) {
      double recscale = 1.0 / scale;
      for (int i = 0; i < cols; ++i) {
        for (int k = 0; k < rows; ++k) {
          U.set(k, i, U.get(k,i) * recscale);
        }
      }
    }

    boolean isUpdated = true;
    while (isUpdated) {
      isUpdated = false;

      for (int j = 1; j < cols; ++j) {
        for (int i = 0; i < j; ++i) {
          // compute submatrix of U'*U
          double a = 0.0, b = 0.0, c = 0.0;
          for (int k = 0; k < rows; ++k) {
            double u = U.get(k,i);
            double v = U.get(k,j);
            a += u*u;
            b += v*v;
            c += u*v;
          }

          // compute Jacobi rotation
          double ba = 0.5*(b-a);
          double t = 1.0;
          if (ba < 0.0) { t = -t; ba = -ba; }
          if (c < 0.0) { t = -t; c = -c; }
          if (ba <= TOL && c <= TOL) { continue; }
          t *= c / (ba + stable2norm(c, ba));
          double cs = 1.0 / stable2norm(1.0, t);
          double sn = cs * t;

          // update terminal condition
          if (!isUpdated && c > Math.sqrt((TOL*a)*(TOL*b))) {
            isUpdated = true;
          }

          // rotate columns of U
          for (int k = 0; k < rows; ++k) {
            double u = U.get(k,i);
            double v = U.get(k,j);
            U.set(k, i, cs*u - sn*v);
            U.set(k, j, sn*u + cs*v);
          }

          // rotate columns of V
          for (int k = 0; k < cols; ++k) {
            double u = V.get(k,i);
            double v = V.get(k,j);
            V.set(k, i, cs*u - sn*v);
            V.set(k, j, sn*u + cs*v);
          }
        }
      }
    }

    // compute singular values and left singular vectors
    int rank = 0;
    for (int i = 0; i < cols; ++i) {
      double sigma = 0.0;
      for (int k = 0; k < rows; ++k) {
        double t = U.get(k,i);
        sigma += t*t;
      }
      sigma = Math.sqrt(sigma);

      S.set(i, 0, sigma * scale);
      if (sigma > TOL) {
        ++rank;
        double recsigma = 1.0 / sigma;
        for (int k = 0; k < rows; ++k) {
          U.set(k, i, U.get(k,i) * recsigma);
        }
      }
    }

    // move zero singular values to the back
    if (rank < cols) {
      int p = cols-1;
      int i = 0;
      while (i < p) {
        if (S.get(i,0) <= TOL) {
          while (p >= 0 && S.get(p,0) <= TOL) {
            S.set(p, 0, 0.0);
            for (int k = 0; k < rows; ++k) { U.set(k, p, 0.0); }
            for (int k = 0; k < cols; ++k) { V.set(k, p, 0.0); }
            --p;
          }
          if (i >= p) { break; }

          S.set(i, 0, S.get(p,0));
          S.set(p, 0, 0.0);
          for (int k = 0; k < rows; ++k) {
            U.set(k, i, U.get(k,p));
            U.set(k, p, 0.0);
          }
          for (int k = 0; k < cols; ++k) {
            V.set(k, i, V.get(k,p));
            V.set(k, p, 0.0);
          }
          --p;
        }
        ++i;
      }
    }
    return rank;
  }

  /**
   * Reduced singular value decomposition.
   * Zero singular values and their singular vectors are omitted.
   * The nonzero singular values are not ordered.
   *
   * @return array of three matrices {U, S, V}
   *         with size rows x rank, rank x 1, and cols x rank, respectively
   * @see Matrix#reducedSVD(Matrix, Matrix, Matrix)
   */
  public Matrix[] reducedSVD() {
    final int rows = rows(), cols = cols();
    int m = Math.min(rows, cols);

    Matrix U = Matrix.create(rows, m);
    Matrix S = Matrix.create(m, 1);
    Matrix V = Matrix.create(cols, m);

    int rank = reducedSVD(U, S, V);
    if (rank < m) {
      if (rank == 0) {
        U = create(rows, 0);
        S = create(0, 0);
        V = create(cols, 0);
      }
      else {
        U = U.getMat(0, rows-1, 0, rank-1);
        S = S.getMat(0, rank-1, 0, 0);
        V = V.getMat(0, cols-1, 0, rank-1);
      }
    }
    return new Matrix[]{U, S, V};
  }

  //----------------------------------------------------------------------------

  /**
   * Returns the matrix transpose (lazy operation).
   *
   * Each matrix has two interfaces, a normal and a transposed one.
   * This function switches between them, so any changes to the data of the
   * returned object changes the original matrix. To avoid such data sharing,
   * use <code>copy().T()</code> instead.
   *
   * @return transpose of the matrix (new interface with shared data)
   */
  abstract public Matrix T();

  //----------------------------------------------------------------------------

  @Override
  public String toString() {
    final int rows = rows(), cols = cols();
    String str = "[";
    for (int i = 0; i < rows; ++i) {
      if (0 != i) str += "; ";
      for (int j = 0; j < cols; ++j) {
        if (0 != j) str += " ";
        str += get(i,j);
      }
    }
    str += "]";
    return str;
  }
}
