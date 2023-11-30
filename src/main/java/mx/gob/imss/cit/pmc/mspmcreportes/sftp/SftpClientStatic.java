package mx.gob.imss.cit.pmc.mspmcreportes.sftp;

import com.jcraft.jsch.*;

import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class SftpClientStatic{

	private final static Logger logger = LoggerFactory.getLogger(SftpClientStatic.class);

	private JSch jsch;
	private Session session;
	private Channel channel;
	private ChannelSftp channelSftp;
	private static SftpClientStatic sftpClientStatic;

	private SftpClientStatic() {
	}

	public static SftpClientStatic getSftpClientStatic(String host, Integer port, String user, String pass) throws JSchException {
		if (sftpClientStatic == null) {
			sftpClientStatic = new SftpClientStatic();
			sftpClientStatic.connect(host, port, user, pass);
		}
		return sftpClientStatic;
	}

	public static SftpClientStatic getSftpClient() {
		return sftpClientStatic != null ? sftpClientStatic : new SftpClientStatic();
	}

	public void connect(String host, Integer port, String user, String pass) throws JSchException {
		logger.debug("Inicializando jsch");
		jsch = new JSch();
		session = jsch.getSession(user, host, port);
		session.setPassword(pass.getBytes(StandardCharsets.UTF_8));
		logger.debug("Jsch set to StrictHostKeyChecking=no");
		Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		logger.info("Conectando a {}, {}", host, port);
		session.connect();
		logger.info("Conectado !");
		logger.debug("abriendo canal sftp ...");
		channel = session.openChannel("sftp");
		channel.connect();
		channelSftp = (ChannelSftp) channel;
		logger.debug("Canal sftp abierto");
	}


	public void uploadFile(String sourceFile, String destinationFile) throws DownloadException {
		if (channelSftp == null || session == null || !session.isConnected() || !channelSftp.isConnected()) {
			throw new DownloadException("La coneccion con el servidor esta cerrada. Abrela primero.");
		}

		try {
			logger.debug("Uploading file to server");
			channelSftp.put(sourceFile, destinationFile);
			logger.info("Upload successfull.");
		} catch (SftpException e) {
			throw new DownloadException(e);
		}
	}
	
	public void uploadFile(InputStream inputStream, String destinationFile) throws DownloadException {
		if (channelSftp == null || session == null || !session.isConnected() || !channelSftp.isConnected()) {
			throw new DownloadException("La coneccion con el servidor esta cerrada. Abrela primero.");
		}

		try {
			logger.debug("Uploading file to server");
			channelSftp.put(inputStream, destinationFile);
			logger.info("Upload successfull.");
		} catch (SftpException e) {
			throw new DownloadException(e);
		}
	}

	public void retrieveFile(String sourceFile, String destinationFile) throws DownloadException {
		if (channelSftp == null || session == null || !session.isConnected() || !channelSftp.isConnected()) {
			throw new DownloadException("Connection to server is closed. Open it first.");
		}

		try {
			logger.debug("Downloading file to server");
			channelSftp.get(sourceFile, destinationFile);
			logger.info("Download successfull.");
		} catch (SftpException e) {
			throw new DownloadException(e.getMessage(), e);
		}
	}

	public void cd(String directory) throws SftpException {
			channelSftp.cd(directory);
	}

	public void mkdir(String directory) throws SftpException {
		channelSftp.mkdir(directory);
	}

	public void disconnect() {
		try {
			if (channelSftp != null) {
				logger.debug("Disconnecting sftp channel");
				channelSftp.disconnect();
				channelSftp = null;
			}
			if (channel != null) {
				logger.debug("Disconnecting channel");
				channel.disconnect();
				channel = null;
			}
			if (session != null) {
				logger.debug("Disconnecting session");
				session.disconnect();
				session = null;
			}
			if (jsch != null) {
				jsch.removeAllIdentity();
				jsch = null;
			}
			if (sftpClientStatic != null) {
				sftpClientStatic = null;
			}
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}

}