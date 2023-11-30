package mx.gob.imss.cit.pmc.mspmcreportes.sftp;

import org.springframework.web.multipart.MultipartFile;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestCambiosDictamen;

public interface SftpUploadFile {

	RequestCambiosDictamen uploadFile(MultipartFile file, RequestCambiosDictamen req) throws BusinessException;
}
