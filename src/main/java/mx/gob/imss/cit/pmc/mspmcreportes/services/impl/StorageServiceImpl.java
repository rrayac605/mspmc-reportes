package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;
import mx.gob.imss.cit.pmc.mspmcreportes.services.FtpClientService;
import mx.gob.imss.cit.pmc.mspmcreportes.services.StorageService;

@Service
@Log4j2
public class StorageServiceImpl implements StorageService {
	
    @Value("${fileDictamen}")
    private String path;
    
    @Autowired
    private FtpClientService ftpService;

    public final String getExtension(final String fileName) {
        if(fileName.toLowerCase().endsWith("pdf"))
        	return "pdf";
        
        if(fileName.toLowerCase().endsWith("png"))
        	return "png";
        
        if(fileName.toLowerCase().endsWith("jpg"))
        	return "jpg";
        
        if(fileName.toLowerCase().endsWith("xlsx"))
        	return "xlsx";        
        
        if(fileName.toLowerCase().endsWith("xls"))
        	return "xls";      
    	
		throw new RuntimeException("Inconveniente al obtener extencion del documento");
    }
    
    @Override
    public byte[] obtenerDictamen(String nss, String fileName) throws IOException {

        String fileDownload = null;
        fileDownload = path + "/" + nss+ "/" + fileName;
        try {
        	
        	FileInputStream fileInp= new FileInputStream(ftpService.copyFileFromFTP(fileDownload));
        	
    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    		int nRead;
    		byte[] data = new byte[4];
    		
    		while ((nRead = fileInp.read(data, 0, data.length)) != -1) {
    			buffer.write(data, 0, nRead);
    		}

    		buffer.flush();
    		byte[] targetArray = buffer.toByteArray();
    		return targetArray;        	
        } catch (DownloadException e) {
            log.log(Level.INFO, "Error al descargar archivo {0}", e.getMessage());
    		throw new RuntimeException("Inconveniente al intentar descargar archivo");
        }
    }
}
