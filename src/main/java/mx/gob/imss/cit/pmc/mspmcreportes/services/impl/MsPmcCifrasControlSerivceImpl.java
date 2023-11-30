package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaOutput;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.MsPmcCifrasControlRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcCifrasControlService;



@Service("msPmcCifrasControlService")
public class MsPmcCifrasControlSerivceImpl implements MsPmcCifrasControlService {
	
	@Autowired
	private MsPmcCifrasControlRepository msPmcCifrasControlRepository;
	
	@Override
	public Page<DetalleSalidaOutput> getCifrasControl(MsPmcReportesInput input) throws BusinessException {
		Page<DetalleSalidaOutput> cifrasControlDTO = msPmcCifrasControlRepository.getCifrasControl(input);
		return cifrasControlDTO;
	}

}
