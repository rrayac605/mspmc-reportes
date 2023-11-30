package mx.gob.imss.cit.pmc.mspmcreportes.services;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestConsultDictamenSist;
import mx.gob.imss.cit.pmc.mspmcreportes.model.ResponseConsultDictamenSist;

public interface ConsultaReporteSist {

	ResponseConsultDictamenSist consultarDictamen(RequestConsultDictamenSist req, String usuario) throws BusinessException;
	
	String getMensajeDictamen() throws BusinessException;
	
	String getUrlConsultaDictamen() throws BusinessException;
	
	String getMensajeDictamenIntermitencia() throws BusinessException;
	
	int getTimeOut() throws BusinessException;
	
	String cleanCache();
	
	String defaultCache() throws BusinessException;
}
