/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author adnkh
 */
public class P‫‪rimary‬‬Serve‬‬r extends UnicastRemoteObject  implements SearchEngineInterface {
    
    String PATH = "/home/adnkh/SearchEngine/PrimaryServerFiles/";
    String SERVERNAME = "PRIMARYSERVER";
    String pattern;
    
    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8585;

    //Semaphore for send multicast request to secondery 
    private boolean secondarySearch = false;

    //Array list of all secondary servers names
    Hashtable <String,Integer> secondaryServersPorts = new Hashtable <String,Integer> ();
    
    //ArrayList to memorize all search result   
    ArrayList<FileProperty> cashedPatternToResults;
    
    //Hashtable to ensure that all secondaries servers respond
    int counter = 0;
    
    public P‫‪rimary‬‬Serve‬‬r() throws RemoteException, UnknownHostException, InterruptedException{
        super();
        getPrimarySearchPath();
    }

    @Override
    public ArrayList<FileProperty> search(String pattern, boolean recursively) throws RemoteException {
        ArrayList<FileProperty> result;

        result = SearchHelper.searchForPattern(pattern,PATH,SERVERNAME,recursively);
        
        if(result.size() == 0){
            //Multicast Section
            String recursivelyChar = "0";
            if(recursively)
                recursivelyChar = "1";
            
            this.pattern = pattern.concat(recursivelyChar);
            counter = 0;
            cashedPatternToResults = new ArrayList<FileProperty>();
            
            //Do multicasting by notifay send thread
            newSecondarySearch();            
            //whait for response
//            int loopCounter = 0;
            while (secondaryServersPorts.size() != counter) {
//                loopCounter++; 
//                if(loopCounter>10)
//                    break;
                try {
                     Thread.sleep(500);                     
                } catch (InterruptedException ignore) {                     
                }
            }
            
            //get result
            result = cashedPatternToResults;
        }
        if(result == null)
            result = new ArrayList<FileProperty> ();
        return result;
    }

    @Override
    public String getContent(String filePath, String serverName) throws RemoteException, AccessException {
        String result = "Error: Not Found";
        
        //file in Primary Server       
        if(serverName.equals(SERVERNAME)){
            try {
                result = SearchHelper.readFile(filePath, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Logger.getLogger(PrimaryServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //file in a secondary server
        else{
            Registry secondaryRegistry;
            Secondaries obj;
            int secondaryPortNum = secondaryServersPorts.get(serverName);
            secondaryRegistry = LocateRegistry.getRegistry("localhost",secondaryPortNum);
            try {
                String hostName = "//localhost:" + secondaryPortNum + "/" + serverName;
                System.out.println(hostName);
                obj = (Secondaries) secondaryRegistry.lookup(hostName);
                
                result = obj.getContent(filePath);
            } catch (NotBoundException ex) {
                Logger.getLogger(PrimaryServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
            
        return result;
        
    }
    
    @Override
    public void addedNewSecondary(String serverName,int portNumber) {
        secondaryServersPorts.put(serverName, portNumber);
        System.out.println("Added " + serverName);
    }

    @Override
    public void deleteSecondary(String serverName) {
        secondaryServersPorts.remove(serverName);
    }       

    @Override
    public void notFound(String pattern, String serverName) throws RemoteException {
        //Add one to pattern counter to tell this server that one secondary server respone
        responseNumberPlusOne(pattern);     
    }

    @Override
    public void addResult(String pattern, String serverName, ArrayList<FileProperty> fp) {
        //Add one to pattern counter to tell this server that one secondary server respone
        responseNumberPlusOne(pattern);
        
        //Added to cashed result
        cashedPatternToResults.addAll(fp);
    }

    public void startMultiCastServer() throws UnknownHostException, InterruptedException {
        // Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName(INET_ADDR);
     
        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {
            
            while(true){
                //wait untile some one search
                waitUntilSecondarySearch();
                
                String msg = this.pattern;
                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, addr, PORT);
                serverSocket.send(msgPacket);

                System.out.println("Server sent packet with msg: " + msg);
                
                this.secondarySearch = false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized void newSecondarySearch() {
        
        secondarySearch = true;

        this.notifyAll();
    }

    public synchronized void waitUntilSecondarySearch() {

         while (!secondarySearch) {

            try {
                 this.wait();

            } catch (InterruptedException ignore) {
                 // log.debug("interrupted: " + ignore.getMessage());
            }
         }
    }

    

    private void responseNumberPlusOne(String pattern) {
       this.counter++;
    }

    private void getPrimarySearchPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("select folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.showSaveDialog(null);
        
        this.PATH = chooser.getSelectedFile().toString();
        System.out.println(this.PATH);
    }
}
