package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;

import mx.gob.imss.cit.pmc.mspmcreportes.common.dto.SftpParamsDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;
import mx.gob.imss.cit.pmc.mspmcreportes.services.FtpClientService;

import mx.gob.imss.cit.pmc.mspmcreportes.services.SftpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class FtpServiceImpl implements FtpClientService {
    private final static Logger logger = LoggerFactory.getLogger(FtpClientService.class);

    @Value("${sftpHost}")
    private String sftpHost;

    @Value("${sftpPort}")
    private String sftpPort;

    @Value("${sftpUser}")
    private String sftpUser;

    @Value("${sftpPassword}")
    private String sftpPassword;

    @Value("${sftp}")
    private boolean sftp;


    public String copyFileFromFTP(String nombre) throws DownloadException {

        SftpClient client = null;
        sftp = false;
        String fileName = "";
        try {
            client = new SftpClient();
            SftpParamsDTO sftpParamsDTO = new SftpParamsDTO();
            sftpParamsDTO.setSftpHost(sftpHost);
            sftpParamsDTO.setSftpPort(Integer.parseInt(sftpPort));
            sftpParamsDTO.setSftpPasword(sftpPassword);
            sftpParamsDTO.setSftpUser(sftpUser);
            File tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".xlsx").toFile();
            fileName = tempFile.getAbsolutePath();
            client.connect(sftpParamsDTO);
            client.retrieveFile(
                    nombre,
                    tempFile.getAbsolutePath()
            );
        } catch (DownloadException e) {
            logger.error("", e);
            throw new DownloadException(e.getMessage());
        } catch (IOException e) {
            logger.error("", e);
            throw new DownloadException(e.getMessage());
        } finally {
            client.disconnect();
        }
        return fileName;
    }

    public void uploadFile(String source, String destinationFile) {
        SftpClient client = null;
        try {
            client = new SftpClient();
            SftpParamsDTO sftpParamsDTO = new SftpParamsDTO();

            sftpParamsDTO.setSftpHost(sftpHost);
            sftpParamsDTO.setSftpPort(Integer.parseInt(sftpPort));
            sftpParamsDTO.setSftpPasword(sftpPassword);
            sftpParamsDTO.setSftpUser(sftpUser);
            client.connect(sftpParamsDTO);
            client.uploadFile(source, destinationFile);
            client.disconnect();

        }
        catch (DownloadException ex){

        }
    }
    
    public void createBaseFolder(String basePath) throws SftpException {
        String value = "/";
        if (basePath.endsWith(value)) {
            basePath = basePath.substring(0, basePath.length() -1);
        }
        
        SftpClient client = null;
        client = new SftpClient();
		SftpParamsDTO sftpParamsDTO = new SftpParamsDTO();

		sftpParamsDTO.setSftpHost(sftpHost);
		sftpParamsDTO.setSftpPort(Integer.parseInt(sftpPort));
		sftpParamsDTO.setSftpPasword(sftpPassword);
		sftpParamsDTO.setSftpUser(sftpUser);
		client.connect(sftpParamsDTO);
	
		client.cd("/");
        String[] folders = basePath.substring(1).split("/");
        for (String folder : folders) {
            try {
            	client.cd(folder);
            } catch (SftpException e) {
            	client.mkdir(folder);
            	client.cd(folder);
            }
        }
       
    }
    
    
}