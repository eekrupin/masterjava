package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int countOfExecutors = getCountOfExecutors(executor);
        int range = matrixSize/countOfExecutors;
        int start = 0;

        List<Future> futureList = new ArrayList<>();

        while (start<matrixSize){
            if (matrixSize < start + range){
                range = matrixSize - start;
            }
            final int begin = start;
            final int endSize = start + range;
            futureList.add( executor.submit( () -> {
                final int thatColumn[] = new int[matrixSize];
                for (int j = 0; j < matrixSize; j++) {
                    for (int k = 0; k < matrixSize; k++) {
                        thatColumn[k] = matrixB[k][j];
                    }
                    for (int i = begin; i < endSize; i++) {
                        int thisRow[] = matrixA[i];
                        int sum = 0;
                        for (int k = 0; k < matrixSize; k++) {
                            sum += thisRow[k] * thatColumn[k];
                        }
                        matrixC[i][j] = sum;
                    }
                }
            }));
            start += range;
        }

        for (Future future:futureList) {
            future.get();
        }

        return matrixC;
    }

    private static int getCountOfExecutors(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)executor).getCorePoolSize();
        }
        else {
            return  1;
        }
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        //final int[][] matrixBTransp = transposeArray(matrixB, matrixSize);

        final int thatColumn[] = new int[matrixSize];

        try {
            for (int j = 0; ; j++) {
                for (int k = 0; k < matrixSize; k++) {
                    thatColumn[k] = matrixB[k][j];
                }

                for (int i = 0; i < matrixSize; i++) {
                    int thisRow[] = matrixA[i];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += thisRow[k] * thatColumn[k];
                    }
                    matrixC[i][j] = sum;
                }
            }
        } catch (IndexOutOfBoundsException ignored) { }

        return matrixC;
    }

//    private static int[][] transposeArray(int[][] matrix, int matrixSize) {
//        final int[][] matrixTransp = new int[matrixSize][matrixSize];
//        for (int i=0; i<matrixSize; i++){
//            for (int j=0; j<matrixSize; j++){
//                matrixTransp[i][j] = matrix[j][i];
//            }
//        }
//        return matrixTransp;
//    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
