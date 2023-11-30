package mx.gob.imss.cit.pmc.mspmcreportes.services;


import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import net.sf.jasperreports.engine.JRException;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ReporteService {

	Object getCifrasControlReport(MsPmcReportesInput input) throws FileNotFoundException, JRException, IOException, BusinessException;

	Workbook getCifrasControlReportXls(MsPmcReportesInput input)throws JRException, IOException, BusinessException;

	void getCierreAnualByOOAD(MsPmcReportesInput input) throws IOException;

}
