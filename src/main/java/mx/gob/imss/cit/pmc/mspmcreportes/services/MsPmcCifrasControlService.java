package mx.gob.imss.cit.pmc.mspmcreportes.services;


import org.springframework.data.domain.Page;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaOutput;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;


public interface MsPmcCifrasControlService {

	Page<DetalleSalidaOutput> getCifrasControl(MsPmcReportesInput input) throws BusinessException;

}

