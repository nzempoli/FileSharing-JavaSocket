
import java.io.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Server {
    public static void main (String args[]) throws Exception {
        String clientSentence;
        String capitalizedSentence="A";
        String reply;
        Boolean live=true;
        if (args[0].equals("start")) { //Starts the server if given command
            // Create Welcoming Socket at port 5555
            ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[1]));
            Socket socketConnection=null;
            // Wait for contact-request by clients
            while(live==true) {
                try {
                    // Once request arrives allocate new socket
                    socketConnection = welcomeSocket.accept();
                    // Create & attach input stream to new socket
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));
                    // Create & attach output stream to new socket
                    DataOutputStream outToClient = new DataOutputStream(socketConnection.getOutputStream());
                    clientSentence = inFromClient.readLine( );
                        Thread t = new ClientHandler(socketConnection, inFromClient, outToClient,clientSentence);
                        t.start();
                    if (clientSentence.split(",")[0].contentEquals("shutdown")) { //Closes the connection only after shutdown thread is initated.
                        live=false;
                        welcomeSocket.close();
                    }
                } catch (Exception e) {
                    socketConnection.close();
                    e.printStackTrace();
                }
            }// End of while loop, wait for another client to connect
        }
    }
}

class ClientHandler extends Thread { //Thread handler
    final Socket socketConnection;
    final BufferedReader inFromClient;
    final DataOutputStream outToClient;
    final String clientSentence;
    String reply="";
    Boolean live=true;
    String capitalizedSentence;

    public ClientHandler(Socket socketConnection,BufferedReader inFromClient, DataOutputStream outToClient,String clientSentence) {
        this.socketConnection=socketConnection;
        this.inFromClient=inFromClient;
        this.outToClient=outToClient;
        this.clientSentence=clientSentence;
    }

    public void run() {
        try {
            String[] input = clientSentence.split(","); //Compiles the input into different arguments
            if (input[0].equals("mkdir")) { //Make new Directory
                File file = new File(input[1]);
                if (!file.exists()) {
                    if (file.mkdir()) { //If Directory succesfully
                        capitalizedSentence="Directory made\n";
                        outToClient.writeBytes(capitalizedSentence);
                    }else { //Otherwise tell client it failed
                        capitalizedSentence="Error code 10: Server failed to Make Directory\n";
                        outToClient.writeBytes(capitalizedSentence);
                    }
                }else { //If user gave a non-directory
                    capitalizedSentence="Error code 11: Directory already exists\n";
                    outToClient.writeBytes(capitalizedSentence);
                }
            } else if (input[0].equals("download")) { //If client wants to download commence download
                try {
                    downloadFile(input,inFromClient,outToClient,socketConnection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (input[0].equals("upload")) { //If client wants to upload commence upload
                try {
                    uploadFile(input,inFromClient,outToClient,socketConnection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (input[0].equals("dir")) { //If they want directory contents
                File directory = new File(input[1]);
                if ((!directory.exists())||(!directory.isDirectory())) { //If it's not a directory
                    outToClient.writeBytes("Error code 13: Directory does not exist, or looking at file.");
                }else {
                    File[] directoryContents = directory.listFiles();
                    for (File object : directoryContents) { //Loop through all directory files.
                        if (object.isFile()) {
                            reply += (String.format("File: %s  ,", object.getName()));
                        } else if (object.isDirectory()) {
                            reply += (String.format("Directory: %s  ,", object.getName()));
                        }
                    }
                    if (reply.equals("")) { //If empty fill reply.
                        reply = "Directory is empty";
                    }
                    outToClient.writeBytes(reply);
                }
            }else if (input[0].equals("rmdir")) { //If they want to remove a directory
                File file = new File(input[1]);
                if (!file.exists()) { //If it doesnt exist
                    outToClient.writeBytes("Error code 12: File/Directory does not exist");
                }else if (file.isDirectory()) { //If it has files
                    File[] directoryContents = file.listFiles();
                    if (directoryContents.length>=1) {
                        outToClient.writeBytes("Error code 14: Directory has files can't delete");
                    }else {
                        if (file.delete()) { //If succesful delete
                            outToClient.writeBytes("Deleted directory");
                        }else { //If unsucessful delete
                            outToClient.writeBytes("Error code 15: Server unable to delete directory");
                        }
                    }
                }
            }else if (input[0].contentEquals("rm")) { //If they want to remove a file.
                File file = new File(input[1]);
                if (!file.exists()) {
                    outToClient.writeBytes("File does not exist"); //If file doesn't exist
                }else if (!file.isFile()) {
                    outToClient.writeBytes("Error code 16: This is a directory use rmdir"); //If directory
                }else if (file.delete()) {
                    outToClient.writeBytes("File succesfully deleted"); //If deleted
                }else {
                    outToClient.writeBytes("Error code 17: File was not deleted"); //If sucessfully deleted
                }
            }else if (input[0].contentEquals("shutdown")) {//Response just to tell client server shutdown
                outToClient.writeBytes("Shutting down Server");
                socketConnection.close();
                //welcomeSocket.close();
            }else {
                outToClient.writeBytes("Error code 18:Invalid command received, please try again");
            }
            socketConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void uploadFile(String input[],BufferedReader inFromClient,DataOutputStream outToClient,Socket socketConnection) throws Exception {
        byte b[]=new byte[1024];
        BufferedInputStream is= new BufferedInputStream(socketConnection.getInputStream());
        File file = new File(input[2]);
        long fileLength=Long.parseLong(inFromClient.readLine()); //Gets length of client file
        long bytesWritten=0;
        long currentRead=0;
        FileOutputStream backup=null;
        try {
            if (file.exists()) { //If one already exists that is smaller byte size go to stopped point.
                if (file.length() < fileLength) {
                    outToClient.writeBytes(String.valueOf(file.length() + "\n"));
                    bytesWritten=bytesWritten+file.length();
                } else { //If file exists but is larger, replace
                    outToClient.writeBytes("r\n");
                    file.delete();
                }
            } else { //Otherwise just make file
                outToClient.writeBytes("r\n");
            }
            FileOutputStream nf = new FileOutputStream(input[2], true);//Makes it so nf appends
            backup=nf;
            while ((bytesWritten<fileLength) && ((currentRead = is.read(b)) > 0)) { //Occurs until all bytes have been read
                bytesWritten = bytesWritten + currentRead;
                nf.write(b);
                outToClient.writeBytes(String.valueOf(bytesWritten)+"\n");
            }
            outToClient.writeBytes("Upload Complete\n");
            nf.flush();
            nf.close();
            is.close();
        } catch (Exception e) {
            if (backup!=null) {
                backup.close();
            }
        }
    }
    public static void downloadFile(String input[],BufferedReader inFromClient,DataOutputStream outToClient,Socket socketConnection) throws Exception {
        byte b[]=new byte[1024];
        String reply;
        File file = new File(input[1]);
        if (!file.exists()||file.isDirectory()) { //If file doesn't exist tell client
            outToClient.writeBytes("E\n");
        }else {
            outToClient.writeBytes("A\n"); //Otherwise allow download
            FileInputStream fr = new FileInputStream(input[1]);
            long fileSize= new File(input[1]).length();
            String sSize = String.valueOf(fileSize);
            long downloaded=0;
            int spot=0;
            OutputStream os = socketConnection.getOutputStream();
            outToClient.writeBytes(sSize+"\n");
            reply=inFromClient.readLine();
            if (reply.contentEquals("r")) { //remnant, should never delete
                file.delete();
            } else {
                fr.skip(Long.valueOf(reply));
            }
            while (((spot=fr.read(b))<=fileSize)&&(spot>0)) {
                downloaded = spot + downloaded;
                os.write(b);
            }
            fr.close();
            os.close();
            os.flush();
        }
    }

}

