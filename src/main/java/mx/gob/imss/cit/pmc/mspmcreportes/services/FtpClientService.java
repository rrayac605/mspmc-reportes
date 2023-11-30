package mx.gob.imss.cit.pmc.mspmcreportes.services;

import com.jcraft.jsch.SftpException;

import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;

public interface FtpClientService {

    String copyFileFromFTP(String nombre) throws DownloadException;

    void uploadFile(String source, String destinationFile);
        
    void createBaseFolder(String destino) throws SftpException;
}
