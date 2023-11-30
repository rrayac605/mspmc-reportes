package mx.gob.imss.cit.pmc.mspmcreportes.services;

import java.util.List;
import java.util.Map;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaCodErrorDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaConcecuenciaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaTipoRiesgoDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;

public interface MsPmcReportesService {

	Map<String, DetalleConsultaCodErrorDTO> getCodigoError(MsPmcReportesInput input) throws BusinessException;
	
	String getCodigoErrorPDF(MsPmcReportesInput input) throws BusinessException;

	Map<String, List<DetalleConsultaTipoRiesgoDTO>> getTipoRiesgo(MsPmcReportesInput input) throws BusinessException;
	
	String getTipoRiesgoPDF(MsPmcReportesInput input) throws BusinessException;
	
	Map<String, List<DetalleConsultaConcecuenciaDTO>> getConsecuencia(MsPmcReportesInput input) throws BusinessException;
	
	String getConsecuenciaPDF(MsPmcReportesInput input) throws BusinessException;

}
