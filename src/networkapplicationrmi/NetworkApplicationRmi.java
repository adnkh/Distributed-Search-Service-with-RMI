/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package networkapplicationrmi;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author adn
 */
public class NetworkApplicationRmi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)throws RemoteException, UnknownHostException, InterruptedException {
        // TODO code application logic here        
        PrimaryServer server = new PrimaryServer();
        int port = 5000;
        Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind("//localhost:5000/PrimaryServer", server);
        secodaryServersGui();
        server.startMultiCastServer();
    }

    private static void secodaryServersGui() {
        SecondaryJFrame.main(null);
    }
}
