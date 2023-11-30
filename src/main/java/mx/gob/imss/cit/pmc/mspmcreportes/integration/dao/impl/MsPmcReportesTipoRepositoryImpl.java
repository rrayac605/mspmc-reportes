package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.impl;

import org.springframework.stereotype.Repository;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaReporteTipoOut;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.MsPmcReportesTipoRepository;

@Repository
public class MsPmcReportesTipoRepositoryImpl implements MsPmcReportesTipoRepository {
	
	@Override
	public DetalleSalidaReporteTipoOut getReporteTipo(MsPmcReportesInput input) throws BusinessException {
  		return null;
	}

}
