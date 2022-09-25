import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.util.Arrays;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

public class Iperfer {

    public static void runClientMode(String[] args) {
        // All error checking for formatting and missing arguments
        if (args.length != 7 || !args[1].equals("-h") || !args[3].equals("-p") || !args[5].equals("-t")) {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        }
        String host = args[2];
        int port = Integer.parseInt(args[4]);
        double seconds = Double.parseDouble(args[6]);
        if (port < 1024 || port > 65535) {
            System.out.println("Error: port number must be in the range 1024 to 65535");
            System.exit(2);
        }
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (!addr.isReachable(1000)) {
                System.out.println("Error: hostname invalid");
                System.exit(2);
            }

            byte[] data = new byte[1000];
            long totalKBytes = 0;
            Socket echoSocket = new Socket(host, port);
            OutputStream out = echoSocket.getOutputStream();
            long time = System.currentTimeMillis();
            long end = time + (long) (seconds * 1000);
            while (System.currentTimeMillis() < end) {
                out.write(data);
                out.flush();
                totalKBytes += 1;
            }
            
            long sent = totalKBytes;
            float rate = (float) totalKBytes * 8 / 1000 / seconds;
            System.out.println("sent=" + sent + " KB rate=" + String.format("%.3f", rate) + " Mbps");

            echoSocket.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void runServerMode(String[] args) {
        // All error checking for formatting and missing arguments
        if (args.length != 3 || !args[1].equals("-p")) {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        }
        int port = Integer.parseInt(args[2]);
        if (port < 1024 || port > 65535) {
            System.out.println("Error: port number must be in the range 1024 to 65535");
            System.exit(2);
        }
        
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            byte buffer[] = new byte[1000];

            // Setup input stream 
            InputStream in = clientSocket.getInputStream();
            long timeBeforeRead = System.currentTimeMillis();
            long totalData = 0;
            // Read data into buffer
            long newData = in.read(buffer);
            while (newData != -1) {
                newData = in.read(buffer);
                totalData += newData;
            }

            long timeDifference = System.currentTimeMillis() - timeBeforeRead;
            long totalBytes = totalData / 1000;
            float rate = (float) totalBytes * 8 / (timeDifference);
            String roundedRate = String.format("%.3f", rate);
            System.out.println("received=" + totalBytes + " KB rate="
					+ roundedRate + " Mbps");

            // close connection 
            clientSocket.close();
            serverSocket.close(); 
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        if (args[0].equals("-c")) {
            runClientMode(args);
        }
        else if (args[0].equals("-s")) {
            runServerMode(args);
        }
        else {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        }
    }

}
