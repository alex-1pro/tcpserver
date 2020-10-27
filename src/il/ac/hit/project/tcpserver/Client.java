package il.ac.hit.project.tcpserver;

import il.ac.hit.project.algorithms.Index;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket =new Socket("127.0.0.1",8010);
        System.out.println("client::Socket");

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream toServer=new ObjectOutputStream(outputStream);
        ObjectInputStream fromServer=new ObjectInputStream(inputStream);

        // sending #1 matrix
        int[][] source = {
                {0, 1, 0},
                {1, 0, 1},
                {1, 0, 1}
        };
        toServer.writeObject("matrix");
        toServer.writeObject(source);

        // sending #3 index for getAdjacentIndices
        toServer.writeObject("AdjacentIndices");
        toServer.writeObject(new Index(1,1));
        // receiving #1 getAdjacentIndices
        Collection<Index> AdjacentIndices =
                new ArrayList<Index>((Collection<Index>) fromServer.readObject());
        System.out.println("client::getAdjacentIndices:: "+ AdjacentIndices);

        // sending #4 index for getReachables
        toServer.writeObject("Reachables");

        toServer.writeObject(new Index(1,1));
        // receiving #2 getReachables
        Collection<Index> ReachablesIndices =
                new ArrayList<Index>((Collection<Index>) fromServer.readObject());
        System.out.println("client::ReachablesIndices:: "+ ReachablesIndices);

        //****************** Task1******************************************
        toServer.writeObject("Task1");
        Collection<Index> res1 =
                new ArrayList<Index>((Collection<Index>) fromServer.readObject());
        System.out.println("client::Task1:: "+ res1);
//***********************END TASK1******************************************************

//*** ****************** Task2**********************************************************/
        int[][] matrix1 = new int[][] {
                {1, 0, 1, 1},
                {0, 1, 0, 1},
                {1, 1, 1, 1}
        };
        int[][] matrix2= new int[][] {
                {1, 0, 0, 1, 0},
                {0, 1, 0, 0, 1},
                {1, 0, 1, 1, 1}
        };

        int[][] matrix3 = new int[][] {
                {1, 0, 1 },
                {1, 0, 1},
                {0, 1, 1}
        };

        toServer.writeObject("start Index");
        toServer.writeObject(new Index(2, 2));

        toServer.writeObject("end Index");
        toServer.writeObject(new Index(0, 0));

        toServer.writeObject("Task2");
        toServer.writeObject(matrix3);

        Collection<Index> res2 =
                new ArrayList<Index>((Collection<Index>) fromServer.readObject());
        System.out.println("client::Task2:: "+ res2);
//**************************END TASK2******************************************************

//**************************Task3**********************************************************
    int[][] matrix4 = new int[][] {
                {1, 0, 1, 1},
                {0, 1, 0, 1},
                {1, 1, 1, 1}
        };
        toServer.writeObject("start Index");
        toServer.writeObject(new Index(0, 0));

        toServer.writeObject("end Index");
        toServer.writeObject(new Index(1, 3));


        toServer.writeObject("Task3");
        toServer.writeObject(matrix4);
        Collection<Index> res3 =
                new ArrayList<Index>((Collection<Index>) fromServer.readObject());
        System.out.println("client::Task3:: "+ res3);

//**************************END TASK3*******************************************************

// **************************Task4**********************************************************
        int[][] matrix5 = new int[][] {
                {1, 0, 0, 1, 1, 0, 0},
                {1, 0, 0, 1, 1, 0, 1},
                {0, 0, 0, 1, 1, 1, 1}
        };
        toServer.writeObject("Task4");
        toServer.writeObject(matrix5);
        Integer numOfSubmarines=(Integer) fromServer.readObject();
        System.out.println("client::Task4:: "+ numOfSubmarines);
//***************************End TASK4******************************************************
        toServer.writeObject("stop");

        System.out.println("client::Close all streams!!!!");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client::Close socket!!!!");
    }
}