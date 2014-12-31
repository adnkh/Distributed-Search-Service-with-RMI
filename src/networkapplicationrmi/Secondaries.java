/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author adnkh
 */
public interface Secondaries extends Remote{    
    String getContent(String PATH)throws RemoteException;
}
