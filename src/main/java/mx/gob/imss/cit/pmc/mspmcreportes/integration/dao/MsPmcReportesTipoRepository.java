package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao;


import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaReporteTipoOut;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;


public interface MsPmcReportesTipoRepository {

	DetalleSalidaReporteTipoOut getReporteTipo(MsPmcReportesInput input) throws BusinessException;
	
}
