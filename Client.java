
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;

class Client {
    public static void main (String args[]) throws Exception { //Catcher for if exceptions thrown
        String sentence;
        String modifiedSentence;
        String reply;
        String commandCheck;
        commandCheck = validCommand(args);
        String getFileName;
        if (commandCheck.contentEquals("G")) {
            // BufferedInputStream
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            // Create client socket to connect to server , uses port from environmental variable
            String port=System.getenv("PA1_SERVER"); //Looks to PA1_SERVER for socket.
            String portArguments[]=port.split(":"); //Splits String into usable parts
            Socket clientSocket = new Socket(portArguments[0], Integer.parseInt(portArguments[1])); //sets socket to Hostname:port
            // Create output stream attached to socket
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            // Create input stream attached to socket
            BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
            // Concactonate arguments into a line to send off to server.
            if ((args[0].equals("download"))||(args[0].equals("upload"))) {
                getFileName = args[1].substring(args[1].lastIndexOf("/"));
                args[2] = args[2]+getFileName;
            }
            sentence=String.join(",", args);
            if(args[0].equals("download")) { //If download follow through
                outToServer.writeBytes(sentence+ '\n'); //Sends input to server
                byte b[]=new byte[1024];
                FileOutputStream backup = null;
                reply = inFromServer.readLine(); //Gets reply that says if there's a file to download.
                if (reply.contentEquals("E")) {
                    System.out.println("Error code 1: File is not on server, or attempting to download a directory.");
                }else {
                    try { //If there is a file to download try downloading.
                        BufferedInputStream is= new BufferedInputStream(clientSocket.getInputStream());
                        long fileLength=Long.parseLong(inFromServer.readLine());
                        System.out.println("File is size: " + fileLength);
                        long bytesWritten=0;
                        int currentRead=0;
                        float progress;
                        File file = new File(args[2]);
                        if (file.exists()) {
                            System.out.println("Checking local file");
                            if (file.length()<fileLength) {
                                outToServer.writeBytes(String.valueOf(file.length()+"\n"));
                                bytesWritten=file.length();
                                System.out.println("Interrupted download found, resuming download");
                            }else {
                                System.out.println("Replacing existing file");
                                outToServer.writeBytes("r\n");
                                file.delete();
                            }
                        }else {
                            outToServer.writeBytes("r\n");
                        }
                        FileOutputStream nf = new FileOutputStream(args[2],true);
                        while (bytesWritten<fileLength&&((currentRead=is.read(b))>0)) {
                            bytesWritten = bytesWritten + currentRead;
                            progress = ((float)bytesWritten/(float)fileLength)*100;
                            System.out.print(String.format("Download progress: %.2f\r",progress));
                            nf.write(b);
                        }
                        System.out.println("\nDownload Complete");
                        nf.flush();
                        nf.close();
                        is.close();
                    }catch(Exception e) {
                        System.out.println("Error code 2: Download not complete please try again");
                        if (backup!=null) {
                            backup.close();
                        }
                    }
                }
            }else if (args[0].equals("upload")) {
                byte b[]=new byte[1024];
                File file = new File(args[1]);
                FileInputStream backup=null;
                if (!file.exists()||file.isDirectory()) {
                    System.out.println("Error code 3: File does not exist or attempting to upload a directory. Aborting.");
                }else {
                    try {
                        float progress;
                        outToServer.writeBytes(sentence+ '\n');
                        FileInputStream fr = new FileInputStream(args[1]);
                        backup=fr;
                        long fileSize= new File(args[1]).length();
                        System.out.println(args[1]);
                        String sSize = String.valueOf(fileSize);
                        System.out.println("File Size is: " + fileSize);
                        long uploaded=0;
                        float total;
                        int spot=0;
                        OutputStream os = clientSocket.getOutputStream();
                        outToServer.writeBytes(sSize+"\n");
                        reply=inFromServer.readLine();
                        if (reply.contentEquals("r")) {
                            System.out.println("Replace existing file");
                        } else {
                            fr.skip(Long.valueOf(reply));
                            uploaded=uploaded+Long.valueOf(reply);
                            System.out.println("Partial update detected, starting from point.");
                        }
                        while (((spot=fr.read(b))<=fileSize)&&(spot>0)) {
                            os.write(b);
                            uploaded = Long.valueOf(inFromServer.readLine());
                            total = (float)uploaded/(float)fileSize*(float)100;
                            System.out.print(String.format("Upload progress: %.2f\r",total));
                        }
                        System.out.println("\n"+inFromServer.readLine());
                    }catch (Exception e) {
                        System.out.println("Error code 4: Upload to server unsuccessful");
                        if (backup!=null) {
                            backup.close();
                        }
                    }
                }
            }else {
                try {
                    outToServer.writeBytes(sentence + '\n');
                    modifiedSentence = inFromServer.readLine();
                    System.out.println(modifiedSentence);
                }catch (Exception e) {
                    System.out.println(errorMessage(args[0]));
                }
            }
            clientSocket.close();
        }else {
            System.out.println(commandCheck);
            System.out.println("Quitting program");
        }
    }
    public static String errorMessage(String input) {
        if (input.contentEquals("rmdir")){
            return "Error code 5: Connection Issue-Unable to remove directory";
        }else if (input.contentEquals("dir")) {
            return "Error code 6: Connection Issue-Unable to retrieve getting directory contents";
        }else if (input.contentEquals("rm")) {
            return "Error code 7: Connection Issue-Unable to remove file";
        }else if (input.contentEquals("shutdown")) {
            return "Error code 8: Connection Issue-Couldn't complete shutdown request";
        }else if (input.contentEquals("mkdir")) {
            return "Error code 9: Connection Issue-Couldn't make directory";
        }
        return "Error: Unknown issue occured, please try again.";
    }

    public static String validCommand(String input[]) {
        String s;
        if (!(input[0].contentEquals("upload")||input[0].contentEquals("download")||input[0].contentEquals("rmdir")||input[0].contentEquals("rm")||input[0].contentEquals("dir")||input[0].contentEquals("shutdown")||input[0].contentEquals("mkdir"))) {
            s = "Invalid command, available commands are: upload,download,rmdir,rm,dir,shutdown and mkdir";
        }else if ((input[0].contentEquals("upload")||input[0].contentEquals("download"))&&(input.length!=3)) {
            s= "File Transfer invalid, not enough parameters";
        }else if ((input[0].contentEquals("rmdir")||input[0].contentEquals("rm")||input[0].contentEquals("dir")||input[0].contentEquals("mkdir"))&&input.length!=2) {
            s = "Command invalid, please specify the file/drive you would like to interact with.";
        }else {
            s="G";
        }
        return s;
    }
}

