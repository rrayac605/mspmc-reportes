package mx.gob.imss.cit.pmc.mspmcreportes.sftp;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.jcraft.jsch.JSchException;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.common.dto.SftpParamsDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.CambiosRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestCambiosDictamen;

@Service
public class SftpUploadFileImpl implements SftpUploadFile {

	private static final Logger logger = LoggerFactory.getLogger(SftpUploadFileImpl.class);

	@Value("${sftpHost}")
	private String sftpHost;

	@Value("${sftpPort}")
	private String sftpPort;

	@Value("${sftpUser}")
	private String sftpUser;

	@Value("${sftpPassword}")
	private String sftpPassword;

	@Value("${fileDictamen}")
	private String path;

	@Value("${sftp}")
	private boolean sftp;
	
	@Autowired
	private HttpServletRequest request;

	@Autowired
	private CambiosRepository cambiosRepo;
	
	private SftpParamsDTO getParams() {
		SftpParamsDTO sftpParamsDTO = new SftpParamsDTO();

		sftpParamsDTO.setSftpHost(sftpHost);
		sftpParamsDTO.setSftpPort(Integer.parseInt(sftpPort));
		sftpParamsDTO.setSftpPasword(sftpPassword);
		sftpParamsDTO.setSftpUser(sftpUser);
		return sftpParamsDTO;
	}

	public RequestCambiosDictamen uploadFile(MultipartFile file, RequestCambiosDictamen req) throws BusinessException {

		SftpClientStatic sftpClient;

		SftpParamsDTO sftpP = getParams();
		try {
			sftpClient = SftpClientStatic.getSftpClientStatic(sftpP.getSftpHost(), sftpP.getSftpPort(),
					sftpP.getSftpUser(), sftpP.getSftpPasword());
		} catch (JSchException e) {
			throw new RuntimeException("Inconveniente al conectarse al servidor", e);
		}
		try {
			req.setNomArchivo(file.getOriginalFilename());
			
			String fullBasePath = path + "/" + req.getNss() + "/";;
			SftpReportes.createBaseFolder(fullBasePath);
			logger.info("Ruta donde se depositara el archivo {}", fullBasePath);
			logger.info("Nombre del archivo {} para el nss {}", req.getNomArchivo(), req.getNss());
			
			String rutaName = fullBasePath.concat(req.getNomArchivo());
		    sftpClient.uploadFile(file.getInputStream() ,rutaName);
			
			String host = request.getRequestURL().toString().replace(request.getRequestURI(), "");
			
			String url = ServletUriComponentsBuilder
					.fromHttpUrl(host)
					.path("/msreportes/v1/dictamen/" + req.getNss()+ "/" + req.getNomArchivo())
					.toUriString();
			
			sftpClient.disconnect();

			req.setUrl(url);
			sendUrlCambio(req);
			return req;
		} catch (Exception e) {
			sftpClient.disconnect();
			throw new RuntimeException("Inconveniente al conectarse al servidor", e);
		}
	}

    private void sendUrlCambio(RequestCambiosDictamen request) {
    	if(!cambiosRepo.asociarDictamenCambios(request)) {
    		throw new RuntimeException("Inconveniente al asociar documento dicamen con riesgo de trabajo");
    	}
    }
	
}
