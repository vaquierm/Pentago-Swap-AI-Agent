package Michael_Hybrid;

import java.util.Arrays;
import java.util.HashSet;

public class UtilTools {


    /**
     * Rotates a matrix counter clockwise
     * Note: Must be square
     * @param mat  The matrix
     * @param <T>  Type of data
     */
    public static <T> void rotateMatrixCounterClockwise(T[][] mat)
    {
        int N = mat.length;

        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++)
        {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N-x-1; y++)
            {
                // store current cell in temp variable
                T temp = mat[x][y];

                // move values from right to top
                mat[x][y] = mat[y][N-1-x];

                // move values from bottom to right
                mat[y][N-1-x] = mat[N-1-x][N-1-y];

                // move values from left to bottom
                mat[N-1-x][N-1-y] = mat[N-1-y][x];

                // assign temp to left
                mat[N-1-y][x] = temp;
            }
        }
    }

    /**
     * Rotates a matrix clockwise
     * Note: Must be square
     * @param mat  The matrix
     * @param <T>  Type of data
     */
    public static <T> void rotateMatrixClockwise(T[][] mat)
    {
        int N = mat.length;

        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++)
        {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N-x-1; y++)
            {
                // store current cell in temp variable
                T temp = mat[x][y];

                // move values from left to top
                mat[x][y] = mat[N-1-y][x];

                // move values from bottom to left
                mat[N-1-y][x] = mat[N-1-x][N-1-y];

                // move values from right to bottom
                mat[N-1-x][N-1-y] = mat[y][N-1-x];

                // assign temp to right
                mat[y][N-1-x] = temp;
            }
        }
    }

    /**
     * Rotates the matrix 180 degrees
     * @param mat  The matrix
     * @param <T>  Type of data
     */
    public static <T> void rotateMatrix180(T[][] mat)
    {
        int N = mat.length;

        // rotate matrix by 180 degrees
        for(int i = 0; i < N /2; i++) {
            for (int j = 0; j < N; j++) {
                T temp = mat[i][j];
                mat[i][j] = mat[N - i - 1][N - j - 1];
                mat[N - i - 1][N - j - 1] = temp;
            }
        }
    }


    public static <T> boolean matrixEqual(final T[][] arr1, final T[][] arr2) {

        if (arr1 == null) {
            return (arr2 == null);
        }

        if (arr2 == null) {
            return false;
        }

        if (arr1.length != arr2.length) {
            return false;
        }

        for (int i = 0; i < arr1.length; i++) {
            if (!Arrays.equals(arr1[i], arr2[i])) {
                return false;
            }
        }

        return true;
    }


    /**
     * Get the symetries of a matrix
     * @param arr  The matrix
     * @return  A hashset containing all symmetries present in the matrix
     */
    public static <T> HashSet<Symmetry> checkSymmetry(T[][] arr) {

        int N = arr.length;

        HashSet<Symmetry> symmetries = new HashSet<>(4);
        symmetries.add(Symmetry.VERTICAL);
        symmetries.add(Symmetry.HORIZONTAL);
        symmetries.add(Symmetry.DIAGONAL1);
        symmetries.add(Symmetry.DIAGONAL2);

        // Checking for Horizontal Symmetry.  We compare
        // first row with last row, second row with second
        // last row and so on.
        for (int i = 0, k = N - 1; i < N / 2; i++, k--) {
            // Checking each cell of a column.
            for (int j = 0; j < N; j++) {
                // check if every cell is identical
                if (arr[i][j] != arr[k][j]) {
                    symmetries.remove(Symmetry.HORIZONTAL);
                    break;
                }
            }
        }

        // Checking for Vertical Symmetry.  We compare
        // first column with last column, second xolumn
        // with second last column and so on.
        for (int i = 0, k = N - 1; i < N / 2; i++, k--) {
            // Checking each cell of a row.
            for (int j = 0; j < N; j++) {
                // check if every cell is identical
                if (arr[j][i] != arr[j][k]) {
                    symmetries.remove(Symmetry.VERTICAL);
                    break;
                }
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (arr[i][j] != arr[j][i]) {
                    symmetries.remove(Symmetry.DIAGONAL1);
                    break;
                }
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (arr[i][j] != arr[N - j - 1][N - i - 1]) {
                    symmetries.remove(Symmetry.DIAGONAL2);
                    break;
                }
            }
        }

        return symmetries;

    }

    public enum Symmetry {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL1,
        DIAGONAL2
    }


    public static void main(String[] args) {

        CustomPentagoBoardState boardState = new CustomPentagoBoardState();

        System.out.println(System.currentTimeMillis());
        System.out.println(boardState.getAllLegalMoves().size());
        System.out.println(System.currentTimeMillis());

        System.out.println(System.currentTimeMillis());
        System.out.println(boardState.getAllLegalMovesWithSymmetry().size());
        System.out.println(System.currentTimeMillis());

    }


}
