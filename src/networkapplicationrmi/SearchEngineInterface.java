/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author adnkh
 */
public interface SearchEngineInterface extends Remote{
    ArrayList<FileProperty> search (String fileName,boolean recursively)throws RemoteException;
    String getContent (String filePath, String serverName)throws RemoteException;
    
    void addedNewSecondary(String serverName,int portNumber)throws RemoteException;
    void deleteSecondary(String serverName)throws RemoteException;
    
    void notFound(String pattern, String serverName) throws RemoteException;
    void addResult(String pattern, String serverName,ArrayList<FileProperty> fp) throws RemoteException;
}
