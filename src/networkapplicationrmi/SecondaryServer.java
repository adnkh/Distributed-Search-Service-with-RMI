/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author adnkh
 */
public class SecondaryServer extends UnicastRemoteObject implements Secondaries{
    
    String SERVERNAME = "SECONDARYSERVER";
    String PATH = "/home/adnkh/SearchEngine/SecondaryServerFiles/";
    
    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8585;
        
    Registry myRegistry;
    SearchEngineInterface obj;
    
    static int myPort = 5001;
    Registry registry;
    

//    public static void main(String[] args) throws UnknownHostException, RemoteException, NotBoundException {
//        // Get the address that we are going to connect to.
//        SecondaryServer ss = new SecondaryServer();
//    }

//    public SecondaryServer() throws UnknownHostException, RemoteException, NotBoundException {
//        myPort++;
//        registry = LocateRegistry.createRegistry(myPort);
//        String localhost = "//localhost:" + myPort + "/" + this.SERVERNAME;
//        System.out.println("Localhost is: " + localhost);
//        registry.rebind(localhost, this);
//        
//        initSearchEnginePrimarySeconderyObj();
//        startListiningToMultiServer();                        
//    }

    public SecondaryServer(String SERVERNAME, String PATH) throws UnknownHostException, RemoteException, NotBoundException {       
        myPort++;
        
        this.PATH = PATH;
        this.SERVERNAME = SERVERNAME;                                          
        
        registry = LocateRegistry.createRegistry(myPort);
        String localhost = "//localhost:" + myPort + "/" + this.SERVERNAME;
        System.out.println(localhost);
        registry.rebind(localhost, this);
        
        initSearchEnginePrimarySeconderyObj();
        startListiningToMultiServer();            

    }    
    
    private void startListiningToMultiServer() throws UnknownHostException{
        InetAddress address = InetAddress.getByName(INET_ADDR);
        
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[256];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
            //Joint the Multicast group.
            clientSocket.joinGroup(address);
     
            while (true) {
                buf = new byte[256];

                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);

                String pattern = new String( buf, StandardCharsets.UTF_8 );
                pattern = pattern.replace("\0", "");
                
                char recursivelyChar = pattern.charAt(pattern.length()-1);
                
                boolean recursively = true;
                if(recursivelyChar == '0')
                    recursively = false;
                
                pattern = spliteLastChar(pattern);
                
                ArrayList<FileProperty> result;
                result = SearchHelper.searchForPattern(pattern,PATH,SERVERNAME,recursively);   
                
                if(result.size() == 0)
                    obj.notFound(pattern, SERVERNAME);                
                else
                    obj.addResult(pattern, SERVERNAME, result);               
                    
                
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
            
        } finally{
            System.out.println("Exiting");
        }
        
    }

    private void initSearchEnginePrimarySeconderyObj() throws RemoteException, NotBoundException {
        myRegistry = LocateRegistry.getRegistry("localhost",5000);
        obj = (SearchEngineInterface) myRegistry.lookup("//localhost:5000/PrimaryServer");
        obj.addedNewSecondary(SERVERNAME, this.myPort);
    }

    @Override
    public String getContent(String PATH) throws RemoteException {
        String result = "Error: Not Found";

        try {
            result = SearchHelper.readFile(PATH, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(PrimaryServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;                
    }

    private String spliteLastChar(String str) {
        if (str.length() > 0) {
          str = str.substring(0, str.length()-1);
        }
        return str;
    }
    
    
}