package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao;

import java.util.*;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.*;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;

public interface MsPmcReportesRepository {

	ReporteTipoRiesgoResponseDTO getConteo(MsPmcReportesInput input);

	ReporteTipoRiesgoResponseDTO getAnteriorNuevo(MsPmcReportesInput input);

	ReporteCasuisticaConsecuenciaResponseDTO getConteo(MsPmcReportesInput input, List<ConsecuenciaDTO> consecuenciasList);

	List<ReporteCodigoErrorResponseDTO> getConteoCodigoError(MsPmcReportesInput input);

}
