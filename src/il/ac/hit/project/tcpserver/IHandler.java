package il.ac.hit.project.tcpserver;

import java.io.*;

public interface IHandler {
    public void handle(InputStream inClient, OutputStream outClient) throws IOException, ClassNotFoundException, Exception;
}