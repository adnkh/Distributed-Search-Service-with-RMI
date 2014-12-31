/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkapplicationrmi;


import java.io.Serializable;

/**
 *
 * @author adnkh
 */
public class FileProperty implements Serializable{
    
    private String Path;
    private String FileName;
    private String ServerName;

    public FileProperty(String Path, String FileName, String ServerName) {
        this.Path = Path;
        this.FileName = FileName;
        this.ServerName = ServerName;
    }

    public String getFileName() {
        return FileName;
    }

    public String getPath() {
        return Path;
    }

    public String getServerName() {
        return ServerName;
    }                
    
}
