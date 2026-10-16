import java.io.*;
import java.net.*;

class program934
{
    public static int ClientCount = 1;

    public static void main(String A[])
    {
        try
        {
            ServerSocket serversocket = new ServerSocket(9000);

            System.out.println("-------------------------------------");
            System.out.println("----- Marvellous Server Started -----");
            System.out.println("-------------------------------------");

            // Loop for multiple client requests
            while(true)
            {
                System.out.println("Server is waiting for client request");

                Socket clientsocket = serversocket.accept();
                
                System.out.println("Client connected sucesfully");

                // Thread gets created for client
                Thread t = new Thread(() -> HandleClientRequest(clientsocket));

                t.start();
            } // End of while
        }
        catch(Exception e)
        {
            System.out.println("Exception occured : "+e);
        }
    } // End of main

    // Request handler for each client requests
    public static void HandleClientRequest(Socket socket)
    {
        System.out.println("New thread gets created for client no : "+ClientCount);
        ClientCount++;

        try
        {
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("Connected to Marvellous Server");
        
            while(true)
            {
                // Read command from client
                String command = dis.readUTF();

                System.out.println("Command received from client : "+command);

                String parts[] = command.split(" ");

                String operation = parts[0].toUpperCase();

                if(operation.equals("QUIT"))
                {
                    if(parts.length != 1)
                    {
                        dos.writeUTF("Usage : QUIT");
                        
                        continue;
                    }

                    dos.writeUTF("Disconneced from server");
                
                    ClientCount--;
                    
                    break;
                }

                // DONE
                if(operation.equals("GET"))
                {
                    if(parts.length != 2)
                    {
                        dos.writeUTF("Usage : GET <FileName>");
                        
                        continue;
                    }

                    String fileName = parts[1];

                    File file = new File(fileName);

                    if(file.exists() == false || file.isFile() == false)
                    {
                        dos.writeUTF("FILE_NOT_FOUND");

                        continue;
                    }

                    dos.writeUTF("File found");

                    long filesize = file.length();

                    dos.writeLong(filesize);

                    FileInputStream fis = new FileInputStream(file);

                    byte buffer[] = new byte[1024];

                    int bytesread = 0;

                    while((bytesread = fis.read(buffer)) != -1)
                    {
                        dos.write(buffer,0,bytesread);
                    }

                    dos.flush();
                    fis.close();

                    System.out.println("File sent succesfully to the client");
                
                }

                // PUT B.txt (Upload)
                else if(operation.equals("PUT"))
                {
                    if(parts.length != 2)
                    {
                        dos.writeUTF("Usage : PUT <FileName>");
                        
                        continue;
                    }

                    String fileName = parts[1];

                    dos.writeUTF("READY");

                    long filesize = dis.readLong();

FileOutputStream fos = new FileOutputStream(fileName);

                    byte buffer[] = new byte[1024];

                    long received = 0;

                    while(received < filesize)
                    {
                        long remaining = filesize - received;

                        int toread;

                        if(remaining > buffer.length)
                        {
                            toread = buffer.length;
                        }
                        else
                        {
                            toread = (int)remaining;
                        }

                        int bytesread = dis.read(buffer,0,toread);

                        if(bytesread == -1)
                        {
                            break;
                        }

                        fos.write(buffer,0,bytesread);

                        received = received + bytesread;
                    } // End of while

                    fos.close();

                    if(received == filesize)
                    {
                        dos.writeUTF("File uploaded succesfully");
                    }
                    else
                    {
                        dos.writeUTF("File uploaded failed");                       
                    }
                }
                else if(operation.equals("INFO"))
                {
                    if(parts.length != 2)
                    {
                        dos.writeUTF("Usage : INFO <FileName>");
                        
                        continue;
                    }

                    File file = new File(parts[1]);

                    if(file.exists())
                    {
                        String info = "";

                        info = info + "File name : "+file.getName()+"\n";

                        info = info + "File Size : "+file.length()+"\n";

                        info = info + "Readable : "+file.canRead()+"\n";

                        info = info + "Writable : "+file.canWrite()+"\n";
                        
                        dos.writeUTF(info);
                    }
                    else
                    {
                        dos.writeUTF("File does not exist");
                    }
                }
                // SIZE Demo.txt
                else if(operation.equals("SIZE"))
                {
                    if(parts.length != 2)
                    {
                        dos.writeUTF("Usage : SIZE <FileName>");
                        
                        continue;
                    }

                    File file = new File(parts[1]);

                    if(file.exists() && file.isFile())
                    {
                        dos.writeUTF("File size is : "+file.length()+" bytes");
                    }
                    else
                    {
                        dos.writeUTF("File does not exist");
                    }
                }
                else if(operation.equals("EXISTS"))
                {
                    if(parts.length != 2)
                    {
                        dos.writeUTF("Usage : EXISTS <FileName>");
                        
                        continue;
                    }

                    File file = new File(parts[1]);

                    if(file.exists())
                    {
                        dos.writeUTF("File exist");
                    }
                    else
                    {
                        dos.writeUTF("File does not exist");
                    }

                }
                // RENAME Demo.txt DemoX.txt
                else if(operation.equals("RENAME"))
                {
                    if(parts.length != 3)
                    {
                        dos.writeUTF("Usage : RENAME <OldFileName> <NewFileName>");
                        
                        continue;
                    }

                    File oldfile = new File(parts[1]);

                    File newfile = new File(parts[2]);

                    if(oldfile.exists() == false)
                    {
   