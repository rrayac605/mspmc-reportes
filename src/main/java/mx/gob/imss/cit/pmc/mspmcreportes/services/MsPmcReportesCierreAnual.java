package mx.gob.imss.cit.pmc.mspmcreportes.services;

import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ReporteCierreAnualRequestModel;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;

public interface MsPmcReportesCierreAnual {

    InputStreamResource obtenerReporte(ReporteCierreAnualRequestModel request) throws IOException;

}
