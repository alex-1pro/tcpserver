package il.ac.hit.project.tcpserver;


import il.ac.hit.project.algorithms.Algorithms;
import il.ac.hit.project.algorithms.Index;
import il.ac.hit.project.algorithms.InvalidSubmarineMatrixAlgorithmException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MatrixIHandler implements IHandler {

    private Matrix matrix;
    private Index start, end;
   private int[][] primitiveMatrix;
    public MatrixIHandler() {
        this.resetParams();
    }
    private void resetParams(){
        this.matrix = null;
        this.start = null;
        this.end = null;
    }

    @Override
    public void handle(InputStream inClient, OutputStream outClient) throws Exception {
        System.out.println("server::start handle");

        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outClient);
        ObjectInputStream objectInputStream = new ObjectInputStream(inClient);

        this.resetParams();

        boolean dowork = true;
        while (dowork) {
            switch (objectInputStream.readObject().toString()) {
                case "stop":{
                    dowork= false;
                    break;
                }
                case "matrix": {
                    primitiveMatrix = (int[][]) objectInputStream.readObject();
                    this.matrix = new Matrix(primitiveMatrix);
                    this.matrix.printMatrix();
                    break;
                }
                case "start Index": {
                    this.start = (Index) objectInputStream.readObject();
                    break;
                }
                case "end Index": {
                    this.end = (Index) objectInputStream.readObject();
                    break;
                }
                case "AdjacentIndices": {
                    // receiving index for getAdjacentIndices
                    Index indexAdjacentIndices = (Index) objectInputStream.readObject();
                    Collection<Index> adjacentIndices = new ArrayList<>();
                    if (this.matrix != null){
                        adjacentIndices.addAll(this.matrix.getAdjacentIndices(indexAdjacentIndices));
                    }
                    // sending getAdjacentIndices
                    System.out.println("server::getAdjacentIndices:: " + adjacentIndices);
                    objectOutputStream.writeObject(adjacentIndices);
                    break;
                }
                case "Reachables": {
                    // receiving index for getReachables
                    Index indexReachables = (Index) objectInputStream.readObject();
                    Collection<Index> reachables = new ArrayList<>();
                    if (this.matrix != null){
                        reachables.addAll(this.matrix.getReachables(indexReachables));
                    }
                    // sending getReachables
                    System.out.println("server::getReachables:: " + reachables);
                    objectOutputStream.writeObject(reachables);
                    break;
                }
                case "Task1": {
                    List<HashSet<Index>> res1 = Algorithms.findAllConnectedComponents(primitiveMatrix);
                    System.out.println("server::Task1:: " + res1);
                    objectOutputStream.writeObject(res1);
                    break;
                }
                case "Task2":{
                    int[][] matrix= (int[][]) objectInputStream.readObject();
                    List<List<Index>> res2=Algorithms.findAllPathsFromSourceToDest(matrix,start,end);
                    System.out.println("server::Task2:: " + res2);
                    objectOutputStream.writeObject(res2);
                    break;
                }
                case "Task3":{
                  //  Integer num=(Integer) objectInputStream.readObject();
                   // int[][] matrix= Algorithms.matrixGenerator(num);
                    int[][] matrix= (int[][]) objectInputStream.readObject();
                    List<List<Index>> res3 = Algorithms.findShortestPathsFromSourceToDest(matrix,start,end);
                    System.out.println("server::Task3:: " + res3);
                    objectOutputStream.writeObject(res3);
                    break;
                }
                case "Task4":{
                    int[][] matrix= (int[][]) objectInputStream.readObject();
                    int numOfSubmarine=Algorithms.countSubmarines(matrix);
                    System.out.println("server::Task4:: " + numOfSubmarine);
                    objectOutputStream.writeObject(numOfSubmarine);
                    System.out.println("Input invalid");
                    break;
                }

            }
        }
    }
}