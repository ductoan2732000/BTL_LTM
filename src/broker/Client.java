package broker;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client
{
    private Socket socket       = null;
    private Scanner scn         = null;
    private DataInputStream dis = null;
    private DataOutputStream dos= null;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);

            scn = new Scanner(System.in);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
        catch (UnknownHostException uhe)
        {
            System.out.println(uhe);
        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
        }

        String tosend  ="";
        String received="";
        while (true)
        {
            try {
                System.out.print("Message to Server: ");
                tosend = scn.nextLine();
                dos.writeUTF(tosend);

                // printing date or time as requested by client
                received = dis.readUTF();
                System.out.println("Message from Server: " + received);

                if (received.equals("202 " + tosend + " OK"))
                {
                    downloadFile(tosend);
                }

                // If client sends exit,close this connection
                // and then break from the while loop
                if(tosend.equals("bye"))
                {
                    System.out.println("Closing this connection : " + socket);
                    socket.close();
                    System.out.println("Connection closed");
                    break;
                }
            }
            catch(IOException ioe)
            {
                System.out.println(ioe);
            }

        }

        // closing resources
        try {
            scn.close();
            dis.close();
            dos.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
        }
    }

    public void downloadFile(String filename) throws IOException {
        FileOutputStream fos    = new FileOutputStream(filename);
        BufferedOutputStream bos= new BufferedOutputStream(fos);
        byte[] contents         = new byte[1024];
        long fileLength         = dis.readLong();
        int byteRead            = 0;
        int size                = 0;

        while(size < fileLength) {
            byteRead = dis.read(contents);
            bos.write(contents, 0, byteRead);
            size += byteRead;
            System.out.println("Downloading ..." + (size*100)/fileLength + "% complete!");
        }

        System.out.println("Length of file " + filename + " : " + fileLength + "B");

        bos.flush();
        fos.close();
    }

    public static void main(String[] args) throws IOException
    {
        System.out.print("Input address to connecting with Server: ");
        String addr="";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            addr = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Client client = new Client(addr, 5056);
    }
}