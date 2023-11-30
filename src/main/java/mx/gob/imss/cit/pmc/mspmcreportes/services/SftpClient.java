package mx.gob.imss.cit.pmc.mspmcreportes.services;

import com.jcraft.jsch.*;

import mx.gob.imss.cit.pmc.mspmcreportes.common.dto.SftpParamsDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SftpClient {

	private final static Logger logger = LoggerFactory.getLogger(SftpClient.class);

	private JSch jsch;
	private Session session;
	private Channel channel;
	private ChannelSftp c;
	
	public void connect(SftpParamsDTO sftpParamsDTO) {
		try {
			logger.debug("Inicializando jsch");
			jsch = new JSch();
			session = jsch.getSession(sftpParamsDTO.getSftpUser(), sftpParamsDTO.getSftpHost(),
					sftpParamsDTO.getSftpPort());

			session.setPassword(sftpParamsDTO.getSftpPasword().getBytes(Charset.forName("UTF-8")));

			logger.debug("Jsch set to StrictHostKeyChecking=no");
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			logger.info("Conectando a {[]}, {[]}", sftpParamsDTO.getSftpHost(), sftpParamsDTO.getSftpPort());
			session.connect();
			logger.info("Conectado !");

			logger.debug("abriendo canal sftp ...");
			channel = session.openChannel("sftp");
			channel.connect();
			c = (ChannelSftp) channel;
			logger.debug("Canal sftp abierto");

		} catch (JSchException e) {
			logger.error("", e);
		}
	}

	public void uploadFile(String sourceFile, String destinationFile) throws DownloadException {
		if (c == null || session == null || !session.isConnected() || !c.isConnected()) {
			throw new DownloadException("La coneccion con el servidor esta cerrada. Abrela primero.");
		}
		
		try {
			
			logger.debug("Uploading file to server");
			c.put(sourceFile, destinationFile);
			logger.info("Upload successfull.");
		} catch (SftpException e) {
			throw new DownloadException(e);
		}
	}

	public void retrieveFile(String sourceFile, String destinationFile) throws DownloadException {
		if (c == null || session == null || !session.isConnected() || !c.isConnected()) {
			throw new DownloadException("Connection to server is closed. Open it first.");
		}

		try {
			logger.debug("Downloading file to server");
			c.get(sourceFile, destinationFile);
			logger.info("Download successfull.");
		} catch (SftpException e) {
			throw new DownloadException(e.getMessage(), e);
		}
	}
	
	public void cd(String directory) throws SftpException {
		c.cd(directory);
	}

	public void mkdir(String directory) throws SftpException {
		c.mkdir(directory);
	}

	public void disconnect() {
		if (c != null) {
			logger.debug("Disconnecting sftp channel");
			c.disconnect();
		}
		if (channel != null) {
			logger.debug("Disconnecting channel");
			channel.disconnect();
		}
		if (session != null) {
			logger.debug("Disconnecting session");
			session.disconnect();
		}
	}

}