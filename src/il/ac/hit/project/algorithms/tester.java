package il.ac.hit.project.algorithms;

import org.w3c.dom.ls.LSOutput;

import java.util.HashSet;
import java.util.List;

public class tester {
    public static void main(String[] args) throws Exception{
        int[][] matrix1 = new int[][] {
                {1, 1, 0},
                {0, 0, 1},//row=1 col=1
                {1, 0, 0},
                {0, 1, 1}
        };
        try {
            List<HashSet<Index>> res1 = Algorithms.findAllConnectedComponents(matrix1);
            System.out.println(res1);
            System.out.println(res1.size());
        } catch(AlgorithmThreadingException e) {
            System.err.println(e.getMessage());
        }


        System.out.println("***");

        int[][] matrix2 = new int[][] {
                {1, 0, 0, 1, 0},
                {0, 1, 0, 0, 1},
                {1, 0, 1, 1, 1}
        };

        List<List<Index>> res2 = Algorithms.findAllPathsFromSourceToDest(matrix2,
                new Index(0, 0), new Index(2, 4));
        System.out.println(res2);
        System.out.println(res2.size());

        int[][] matrix3 = new int[][] {
                {0, 0, 0},
                {0, 1, 0},
                {1, 1, 1}
        };

        System.out.println("***");

        List<List<Index>> res3 =
                Algorithms.findShortestPathsFromSourceToDest(matrix3, new Index(2, 0), new Index(2, 2));

        System.out.println(res3);
        System.out.println(res3.size());

        System.out.println("***");


        int[][] matrix4 = new int[][] {
                {1, 1, 0, 1, 1},
                {0, 0, 0, 1, 1},
                {1, 1, 0, 1, 1}
        };

        int res4 =
                Algorithms.countSubmarines(matrix4);
        System.out.println(res4);
        System.out.println("***");
        int[][] matrix5 = new int[][] {
                {1, 0, 0, 1, 1},
                {1, 0, 0, 1, 1},
                {0, 0, 0, 1, 1}
        };
        int res5 =
                Algorithms.countSubmarines(matrix5);
        System.out.println(res5);
       // int[][] matrixTest=Algorithms.matrixGenerator(50);
    }



}