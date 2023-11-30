package mx.gob.imss.cit.pmc.mspmcreportes.sftp;

import com.jcraft.jsch.SftpException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpReportes {

    private static Integer zero = 0;
    
    private static final Logger logger = LoggerFactory.getLogger(SftpReportes.class);

    public static void createBaseFolder(String basePath) throws SftpException {
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(zero, basePath.length() -1);
        }
        SftpClientStatic sftpClient = SftpClientStatic.getSftpClient();
        sftpClient.cd("/");
        String[] folders = basePath.substring(1).split("/");
        for (String folder : folders) {
            try {
                sftpClient.cd(folder);
            } catch (SftpException e) {
                sftpClient.mkdir(folder);
                sftpClient.cd(folder);
            }
        }
    }
	
}
