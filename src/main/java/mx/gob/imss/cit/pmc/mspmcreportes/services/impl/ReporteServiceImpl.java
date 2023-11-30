package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;


import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaOutput;
import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcCifrasControlService;
import mx.gob.imss.cit.pmc.mspmcreportes.services.ReporteService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service("reportService")
public class ReporteServiceImpl implements ReporteService {

	public String exportReport(String reportFormat) throws FileNotFoundException, JRException {

		return "Report Generatade succefully";
	}

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private MsPmcCifrasControlService cifrasControlService;

	@Autowired
	private ParametroRepository parametroRepository;

	DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public Object getCifrasControlReport(MsPmcReportesInput input)
			throws JRException, IOException, BusinessException {

		Page<DetalleSalidaOutput> cifrasControl = cifrasControlService.getCifrasControl(input);

		java.util.Iterator<DetalleSalidaOutput> iterator = cifrasControl.iterator();
		List<DetalleConsultaDTO> detalleConsultaDTOList = null;
		CifrasControlDTO cifrasControlDTO = null;
		while (iterator.hasNext()) {
			DetalleSalidaOutput detalleSalidaOutput = (DetalleSalidaOutput) iterator.next();
			detalleConsultaDTOList = detalleSalidaOutput.getDetalleConsultaDTO();
			cifrasControlDTO = detalleSalidaOutput.getCifrasControlTotales();
			break;

		}

		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporteCasuistica");

		Map<String, Object> parameters = new HashMap<String, Object>();

		InputStream resourceAsStream=null;
		if (input.getDelRegPat()) {
			resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("classpath:/cifrasControlDelegacion.jrxml");
			Optional<ParametroDTO> reporteDelegacional = parametroRepository.findOneByCve("reporteDelegacional");
			parameters.put("reporteDelegacional", reporteDelegacional.get().getDesParametro());
		} else {
			resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/cifrasControlNacional.jrxml");
			Optional<ParametroDTO> reporteNacional = parametroRepository.findOneByCve("reporteNacional");
			parameters.put("reporteDelegacional", " ");
		}
		
		 
		
		JasperReport jasperReport = JasperCompileManager.compileReport(resourceAsStream);

		parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
		parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
		parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
		parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
		parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
		parameters.put("nombreReporte", nombreReporte.get().getDesParametro());
		parameters.put("numTotalRegistros", cifrasControlDTO.getNumTotalRegistros());
		parameters.put("numRegistrosCorrectos", cifrasControlDTO.getNumRegistrosCorrectos());
		parameters.put("numRegistrosCorrectosOtras", cifrasControlDTO.getNumRegistrosCorrectosOtras());
		parameters.put("numRegistrosErrorOtras", cifrasControlDTO.getNumRegistrosErrorOtras());
		parameters.put("numRegistrosError", cifrasControlDTO.getNumRegistrosError());
		parameters.put("numRegistrosSusOtras", cifrasControlDTO.getNumRegistrosSusOtras());
		parameters.put("numRegistrosDupOtras", cifrasControlDTO.getNumRegistrosDupOtras());
		parameters.put("numRegistrosDup", cifrasControlDTO.getNumRegistrosDup());
		parameters.put("numRegistrosSus", cifrasControlDTO.getNumRegistrosSus());

		//************************ BAJAS     *******************************************//
		parameters.put("numRegistrosBaja", cifrasControlDTO.getNumRegistrosBaja());
		parameters.put("numRegistrosBajaOtras",      cifrasControlDTO.getNumRegistrosBajaOtras());
		
		
		parameters.put("fromDate",
				DateUtils.calcularFechaPoceso(input.getFromMonth(), input.getFromYear()).format(europeanDateFormatter));
		parameters.put("toDate",
				DateUtils.calcularFechaPocesoFin(input.getToMonth(), input.getToYear()).format(europeanDateFormatter));
		if (detalleConsultaDTOList.size() > 1) {
			parameters.put("delegacion", "Varias");
		} else {
			parameters.put("delegacion", detalleConsultaDTOList.get(0).getDesDelegacion());
		}

		parameters.put("cifrasDataSource", detalleConsultaDTOList);

		JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
		return Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
	}

	@Override
	public Workbook getCifrasControlReportXls(MsPmcReportesInput input)
			throws JRException, IOException, BusinessException {
		Page<DetalleSalidaOutput> cifrasControl = cifrasControlService.getCifrasControl(input);
		java.util.Iterator<DetalleSalidaOutput> iterator = cifrasControl.iterator();
		List<DetalleConsultaDTO> detalleConsultaDTOList = null;
		CifrasControlDTO cifrasControlDTO = null;
		while (iterator.hasNext()) {
			DetalleSalidaOutput detalleSalidaOutput = (DetalleSalidaOutput) iterator.next();
			detalleConsultaDTOList = detalleSalidaOutput.getDetalleConsultaDTO();
			cifrasControlDTO = detalleSalidaOutput.getCifrasControlTotales();
			break;

		}
		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte");
		Optional<ParametroDTO> reporteNacional = parametroRepository.findOneByCve("reporteNacional");
		Workbook workbook = new XSSFWorkbook();
		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Montserrat");
		font.setFontHeightInPoints((short) 8);
		font.setBold(true);
		
		XSSFFont fontPeriodo = ((XSSFWorkbook) workbook).createFont();
		fontPeriodo.setFontName("Montserrat");
		fontPeriodo.setFontHeightInPoints((short) 8);
		fontPeriodo.setColor(HSSFColor.WHITE.index);
		fontPeriodo.setBold(true);
		CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle rowStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle periodReport = createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, false);
		CellStyle headerStyle = createStyle(font, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.BLACK.index, workbook, false);
		LocalDate localDate = LocalDate.now(ZoneId.of("America/Mexico_City"));
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
		Locale localeMexico = new Locale("es", "MX");
		InputStream inputStream = ReporteServiceImpl.class.getResourceAsStream("/IMSS-logo-.png");
		byte[] bytes = IOUtils.toByteArray(inputStream);
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		inputStream.close();
		// Returns an object that handles instantiating concrete classes
		CreationHelper helper = workbook.getCreationHelper();
		
		if (input.getDelRegPat()) {
			List<Month> months = processMonths(input);
			for (int i = 0; i < months.size(); i++) {
				Month fromMonth = months.get(i);
				input.setFromMonth(String.valueOf(fromMonth.getValue()));
				input.setToMonth(String.valueOf(fromMonth.getValue()));
				
					if (detalleConsultaDTOList!=null && !detalleConsultaDTOList.isEmpty()) {
						Sheet sheetDelegacional = workbook.createSheet("Delegacional " + fromMonth.getDisplayName(TextStyle.SHORT, localeMexico).toUpperCase());
						Header header = sheetDelegacional.getHeader();  
						header.setRight("Hoja " + HeaderFooter.page() + " de " + HeaderFooter.numPages()); 

						sheetDelegacional.setColumnWidth(0, 200);
						sheetDelegacional.setMargin(Sheet.LeftMargin, 0.5 /* inches */ );
						sheetDelegacional.setMargin(Sheet.RightMargin, 0.5 /* inches */ );
	
						Drawing drawing = sheetDelegacional.createDrawingPatriarch();

						ClientAnchor anchor = helper.createClientAnchor();
						// set top-left corner for the image
						anchor.setCol1(1);
						anchor.setRow1(1);

						// Creates a picture
						Picture pict = drawing.createPicture(anchor, pictureIdx);
						// Reset the image to the original size
						pict.resize(1.5, 5);
						
						CellRangeAddress region = CellRangeAddress.valueOf("B2:M7");
						for(int x=region.getFirstRow();x<region.getLastRow();x++){
						    Row row = sheetDelegacional.createRow(x);
						    for(int y=region.getFirstColumn();y<region.getLastColumn();y++){
						        Cell cell = row.createCell(y);
						        cell.setCellValue(" ");
						        cell.setCellStyle(headerStyle);
						    }
						}
						
						Row rowInstitucionDet = sheetDelegacional.getRow(1);
						
						Cell cellInstitucionDet = rowInstitucionDet.getCell(3);
						cellInstitucionDet.setCellValue(nombreInstitucion.get().getDesParametro());
						cellInstitucionDet.setCellStyle(headerStyle);
						
						Row rowDireccionDet = sheetDelegacional.getRow(2);
						Cell cellDireccionDet = rowDireccionDet.getCell(3);
						cellDireccionDet.setCellValue(direccionInstitucion.get().getDesParametro());
						cellDireccionDet.setCellStyle(headerStyle);
	
						Row rowUnidadDet = sheetDelegacional.getRow(3);
						Cell cellUnidadDet = rowUnidadDet.getCell(3);
						cellUnidadDet.setCellValue(unidadInstitucion.get().getDesParametro());
						cellUnidadDet.setCellStyle(headerStyle);
	
						Row rowCoordinacionDet = sheetDelegacional.getRow(4);
						Cell cellCoordinacionDet = rowCoordinacionDet.getCell(3);
						cellCoordinacionDet.setCellValue(coordinacionInstituc.get().getDesParametro());
						cellCoordinacionDet.setCellStyle(headerStyle);
	
						Row rowDivisionDet = sheetDelegacional.getRow(5);
						Cell cellDivisionDet = rowDivisionDet.getCell(3);
						cellDivisionDet.setCellValue(divisionInstitucion.get().getDesParametro());
						cellDivisionDet.setCellStyle(headerStyle);
	
						Cell cellFechaDet = rowDivisionDet.getCell(11);
						cellFechaDet.setCellValue(localDate.format(df));
						cellFechaDet.setCellStyle(headerStyle);
						
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D2:I2"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D3:J3"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D4:J4"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D5:J5"));
						sheetDelegacional.addMergedRegion(CellRangeAddress.valueOf("D6:J6"));
	
						
						createHeaderReport(input, nombreReporte, reporteNacional, sheetDelegacional, true, font, fontPeriodo, workbook, fromMonth.getDisplayName(TextStyle.FULL, localeMexico).toUpperCase());
						
						int counterdet = createHeaderTable(detalleConsultaDTOList, sheetDelegacional, periodReport, font, workbook);
						
						fillDetailReport(cifrasControlDTO, sheetDelegacional, rowColorStyle, rowStyle, counterdet, true, font, workbook);
					}
				}
				
		} else {
			
			String sheetName="Nacional";


			Sheet sheetNacional = workbook.createSheet(sheetName);
			sheetNacional.setColumnWidth(0, 200);
			
			Header header = sheetNacional.getHeader();  
			header.setRight("Página " + HeaderFooter.page() + " of " + HeaderFooter.numPages()); 

			sheetNacional.setMargin(Sheet.LeftMargin, 0.5 /* inches */ );

			sheetNacional.setMargin(Sheet.RightMargin, 0.5 /* inches */ );

			// Creates the top-level drawing patriarch.
			Drawing drawing = sheetNacional.createDrawingPatriarch();

			ClientAnchor anchor = helper.createClientAnchor();
			// set top-left corner for the image
			anchor.setCol1(1);
			anchor.setRow1(1);

			// Creates a picture
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			// Reset the image to the original size
			pict.resize(1.5, 5);
			CellRangeAddress region = input.getDelRegPat()?CellRangeAddress.valueOf("B2:M7"):CellRangeAddress.valueOf("B2:L7");
			for(int i=region.getFirstRow();i<region.getLastRow();i++){
			    Row row = sheetNacional.createRow(i);
			    for(int j=region.getFirstColumn();j<region.getLastColumn();j++){
			        Cell cell = row.createCell(j);
			        cell.setCellValue(" ");
			        cell.setCellStyle(headerStyle);
			    }
			}

			Row rowInstitucion = sheetNacional.getRow(1);
			
			Cell cellInstitucion = rowInstitucion.getCell(3);
			cellInstitucion.setCellValue(nombreInstitucion.get().getDesParametro());
			cellInstitucion.setCellStyle(headerStyle);
			
			Row rowDireccion = sheetNacional.getRow(2);
			Cell cellDireccion = rowDireccion.getCell(3);
			cellDireccion.setCellValue(direccionInstitucion.get().getDesParametro());
			cellDireccion.setCellStyle(headerStyle);

			Row rowUnidad = sheetNacional.getRow(3);
			Cell cellUnidad = rowUnidad.getCell(3);
			cellUnidad.setCellValue(unidadInstitucion.get().getDesParametro());
			cellUnidad.setCellStyle(headerStyle);

			Row rowCoordinacion = sheetNacional.getRow(4);
			Cell cellCoordinacion = rowCoordinacion.getCell(3);
			cellCoordinacion.setCellValue(coordinacionInstituc.get().getDesParametro());
			cellCoordinacion.setCellStyle(headerStyle);

			Row rowDivision = sheetNacional.getRow(5);
			Cell cellDivision = rowDivision.getCell(3);
			cellDivision.setCellValue(divisionInstitucion.get().getDesParametro());
			cellDivision.setCellStyle(headerStyle);

			Cell cellFecha = rowDivision.getCell(10);
			cellFecha.setCellValue(localDate.format(df));
			cellFecha.setCellStyle(headerStyle);
			
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D2:I2"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D3:J3"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D4:J4"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D5:J5"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D6:J6"));

			createHeaderReport(input, nombreReporte, reporteNacional, sheetNacional, false, font, fontPeriodo, workbook, null);
			
			int counter = createHeaderTable(detalleConsultaDTOList, sheetNacional, rowColorStyle, font, workbook);
			
			
			fillDetailReport(cifrasControlDTO, sheetNacional, rowColorStyle, rowStyle, counter, false, font, workbook);
		}
		
		return workbook;
	}

	@Override
	public void getCierreAnualByOOAD(MsPmcReportesInput input) throws IOException {



	}

	private List<Month> processMonths(MsPmcReportesInput input) {
		LocalDate fecProcesoIni = DateUtils.calcularFecPoceso(input.getFromMonth(), input.getFromYear());
		LocalDate fecProcesoFin = DateUtils.calcularFecPocesoFin(input.getToMonth(), input.getToYear());
		List<Month> months = new ArrayList<Month>();
		int initialMonth = fecProcesoIni.getMonthValue();
		int finalMonth = fecProcesoFin.getMonthValue();
		for (int i = initialMonth; i <= finalMonth; i++) {
			months.add(Month.of(i));
		}
		
		
		return months;
	}

	private void fillDetailReport(CifrasControlDTO cifrasControlDTO, Sheet sheetNacional, CellStyle centerStyleCell,
			CellStyle wrapStyle, int counter, boolean delRegaPat, XSSFFont font, Workbook workbook) {
		Row rowTotal = sheetNacional.createRow(counter+14);
		
		CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
		CellStyle rowtyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
		
		CellStyle style = null;
		if ((counter+14) % 2 == 0) {
			style=rowtyle;
		} else {
			style=rowColorStyle;
		}
		
		Cell tipoArchivoDetCell = rowTotal.createCell(1);
		tipoArchivoDetCell.setCellValue("Totales");
		tipoArchivoDetCell.setCellStyle(style);
		
		if (delRegaPat) {

			Cell delegacionDetCell = rowTotal.createCell(2);
			delegacionDetCell.setCellValue("");
			delegacionDetCell.setCellStyle(style);
			
			Cell totalDetCell = rowTotal.createCell(3);
			totalDetCell.setCellValue(cifrasControlDTO.getNumTotalRegistros());
			totalDetCell.setCellStyle(style);

			
			Cell correctosDetCell = rowTotal.createCell(4);
			correctosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosCorrectos());
			correctosDetCell.setCellStyle(style);

			Cell erroneosDetCell = rowTotal.createCell(5);
			erroneosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosError());
			erroneosDetCell.setCellStyle(style);

			Cell duplicadosDetCell = rowTotal.createCell(6);
			duplicadosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosDup());
			duplicadosDetCell.setCellStyle(style);

			Cell susAjusDetCell = rowTotal.createCell(7);
			susAjusDetCell.setCellValue(cifrasControlDTO.getNumRegistrosSus());
			susAjusDetCell.setCellStyle(style);

			Cell correctosOtrasDetCell = rowTotal.createCell(8);
			correctosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosCorrectosOtras());
			correctosOtrasDetCell.setCellStyle(style);

			Cell erroneosOtrasDetCell = rowTotal.createCell(9);
			erroneosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosErrorOtras());
			erroneosOtrasDetCell.setCellStyle(style);

			Cell duplicadosOtrasDetCell = rowTotal.createCell(10);
			duplicadosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosDupOtras());
			duplicadosOtrasDetCell.setCellStyle(style);

			Cell susAjusOtrasDetCell = rowTotal.createCell(11);
			susAjusOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosSusOtras());
			susAjusOtrasDetCell.setCellStyle(style);
		} else {
			Cell totalDetCell = rowTotal.createCell(2);
			totalDetCell.setCellValue(cifrasControlDTO.getNumTotalRegistros());
			totalDetCell.setCellStyle(style);

			Cell correctosDetCell = rowTotal.createCell(3);
			correctosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosCorrectos());
			correctosDetCell.setCellStyle(style);

			Cell erroneosDetCell = rowTotal.createCell(4);
			erroneosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosError());
			erroneosDetCell.setCellStyle(style);

			Cell duplicadosDetCell = rowTotal.createCell(5);
			duplicadosDetCell.setCellValue(cifrasControlDTO.getNumRegistrosDup());
			duplicadosDetCell.setCellStyle(style);

			Cell susAjusDetCell = rowTotal.createCell(6);
			susAjusDetCell.setCellValue(cifrasControlDTO.getNumRegistrosSus());
			susAjusDetCell.setCellStyle(style);

			Cell correctosOtrasDetCell = rowTotal.createCell(7);
			correctosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosCorrectosOtras());
			correctosOtrasDetCell.setCellStyle(style);

			Cell erroneosOtrasDetCell = rowTotal.createCell(8);
			erroneosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosErrorOtras());
			erroneosOtrasDetCell.setCellStyle(style);

			Cell duplicadosOtrasDetCell = rowTotal.createCell(9);
			duplicadosOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosDupOtras());
			duplicadosOtrasDetCell.setCellStyle(style);

			Cell susAjusOtrasDetCell = rowTotal.createCell(10);
			susAjusOtrasDetCell.setCellValue(cifrasControlDTO.getNumRegistrosSusOtras());
			susAjusOtrasDetCell.setCellStyle(style);
		}
	}

	private int createHeaderTable(List<DetalleConsultaDTO> detalleConsultaDTOList, Sheet sheetNacional, CellStyle rowColorStyle2, XSSFFont font, Workbook workbook) {
		int counter = 1;
		for (int i = 0; i < detalleConsultaDTOList.size(); i++) {
			DetalleConsultaDTO detalleConsultaDTO = detalleConsultaDTOList.get(i);
			counter++;
			Row rowDetail = sheetNacional.createRow(i+15);
			
			
			CellStyle rowColorStyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true);
			CellStyle rowtyle = createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true);
			
			CellStyle style = null;
			if (i % 2 == 0) {
				style=rowColorStyle;
			} else {
				style=rowtyle;
			}
			
			
			Cell tipoArchivoDetCell = rowDetail.createCell(1);
			tipoArchivoDetCell.setCellValue(detalleConsultaDTO.getTipoArchivo());
			tipoArchivoDetCell.setCellStyle(style);
			
			if (detalleConsultaDTO.getDesDelegacion()!=null) {
				
				Cell delegacionCell = rowDetail.createCell(2);
				delegacionCell.setCellValue(detalleConsultaDTO.getDesDelegacion());
				delegacionCell.setCellStyle(style);
				
				Cell totalDetCell = rowDetail.createCell(3);
				totalDetCell.setCellValue(detalleConsultaDTO.getNumTotalRegistros());
				totalDetCell.setCellStyle(style);

				Cell correctosDetCell = rowDetail.createCell(4);
				correctosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosCorrectos()!=null?detalleConsultaDTO.getNumRegistrosCorrectos():0);
				correctosDetCell.setCellStyle(style);

				Cell erroneosDetCell = rowDetail.createCell(5);
				erroneosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosError()!=null?detalleConsultaDTO.getNumRegistrosError():0);
				erroneosDetCell.setCellStyle(style);

				Cell duplicadosDetCell = rowDetail.createCell(6);
				duplicadosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosDup()!=null?detalleConsultaDTO.getNumRegistrosDup():0);
				duplicadosDetCell.setCellStyle(style);

				Cell susAjusDetCell = rowDetail.createCell(7);
				susAjusDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosSus()!=null?detalleConsultaDTO.getNumRegistrosSus():0);
				susAjusDetCell.setCellStyle(style);

				Cell correctosOtrasDetCell = rowDetail.createCell(8);
				correctosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosCorrectosOtras()!=null?detalleConsultaDTO.getNumRegistrosCorrectosOtras():0);
				correctosOtrasDetCell.setCellStyle(style);

				Cell erroneosOtrasDetCell = rowDetail.createCell(9);
				erroneosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosErrorOtras()!=null?detalleConsultaDTO.getNumRegistrosErrorOtras():0);
				erroneosOtrasDetCell.setCellStyle(style);

				Cell duplicadosOtrasDetCell = rowDetail.createCell(10);
				duplicadosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosDupOtras()!=null?detalleConsultaDTO.getNumRegistrosDupOtras():0);
				duplicadosOtrasDetCell.setCellStyle(style);

				Cell susAjusOtrasDetCell = rowDetail.createCell(11);
				susAjusOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosSusOtras()!=null?detalleConsultaDTO.getNumRegistrosSusOtras():0);
				susAjusOtrasDetCell.setCellStyle(style);
			} else {
				Cell totalDetCell = rowDetail.createCell(2);
				totalDetCell.setCellValue(detalleConsultaDTO.getNumTotalRegistros());
				totalDetCell.setCellStyle(style);

				Cell correctosDetCell = rowDetail.createCell(3);
				correctosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosCorrectos()!=null?detalleConsultaDTO.getNumRegistrosCorrectos():0);
				correctosDetCell.setCellStyle(style);

				Cell erroneosDetCell = rowDetail.createCell(4);
				erroneosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosError()!=null?detalleConsultaDTO.getNumRegistrosError():0);
				erroneosDetCell.setCellStyle(style);

				Cell duplicadosDetCell = rowDetail.createCell(5);
				duplicadosDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosDup()!=null?detalleConsultaDTO.getNumRegistrosDup():0);
				duplicadosDetCell.setCellStyle(style);

				Cell susAjusDetCell = rowDetail.createCell(6);
				susAjusDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosSus()!=null?detalleConsultaDTO.getNumRegistrosSus():0);
				susAjusDetCell.setCellStyle(style);

				Cell correctosOtrasDetCell = rowDetail.createCell(7);
				correctosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosCorrectosOtras()!=null?detalleConsultaDTO.getNumRegistrosCorrectosOtras():0);
				correctosOtrasDetCell.setCellStyle(style);

				Cell erroneosOtrasDetCell = rowDetail.createCell(8);
				erroneosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosErrorOtras()!=null?detalleConsultaDTO.getNumRegistrosErrorOtras():0);
				erroneosOtrasDetCell.setCellStyle(style);

				Cell duplicadosOtrasDetCell = rowDetail.createCell(9);
				duplicadosOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosDupOtras()!=null?detalleConsultaDTO.getNumRegistrosDupOtras():0);
				duplicadosOtrasDetCell.setCellStyle(style);

				Cell susAjusOtrasDetCell = rowDetail.createCell(10);
				susAjusOtrasDetCell.setCellValue(detalleConsultaDTO.getNumRegistrosSusOtras()!=null?detalleConsultaDTO.getNumRegistrosSusOtras():0);
				susAjusOtrasDetCell.setCellStyle(style);
			}
			
			
		}
		return counter;
	}

	private void createHeaderReport(MsPmcReportesInput input, Optional<ParametroDTO> nombreReporte,
			Optional<ParametroDTO> reporteNacional, Sheet sheetNacional, boolean b, XSSFFont font, XSSFFont fontPeriodo, Workbook workbook, String monthName) {
		Row rowNombreReporte = sheetNacional.createRow(7);
		Cell nombreReporteCell = rowNombreReporte.createCell(1);
		nombreReporteCell.setCellValue(nombreReporte.get().getDesParametro());
		nombreReporteCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat()) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B8:L8"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B8:K8"));
		}

		Row rowReporteNacional = sheetNacional.createRow(8);
		Cell reporteNacionalCell = rowReporteNacional.createCell(1);
		reporteNacionalCell.setCellValue(reporteNacional.get().getDesParametro());
		reporteNacionalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.WHITE.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat()) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B9:L9"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B9:K9"));
		}

		Row rowPeriodoConsultado = sheetNacional.createRow(10);
		Cell periodoConsultado = rowPeriodoConsultado.createCell(1);
		periodoConsultado.setCellValue("Periodo consultado: ");
		periodoConsultado.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B11:C11"));

		Cell fecInicio = rowPeriodoConsultado.createCell(3);
		fecInicio.setCellValue(
				DateUtils.calcularFechaPoceso(input.getFromMonth(), input.getFromYear()).format(europeanDateFormatter));
		fecInicio.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Cell al = rowPeriodoConsultado.createCell(4);
		al.setCellValue(" al ");
		al.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Cell fecFin = rowPeriodoConsultado.createCell(5);
		fecFin.setCellValue(
				DateUtils.calcularFechaPocesoFin(input.getToMonth(), input.getToYear()).format(europeanDateFormatter));
		fecFin.setCellStyle(createStyle(fontPeriodo, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, HSSFColor.GREY_50_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		Row rowRegistros = sheetNacional.createRow(12);
		Cell registrosCell = rowRegistros.createCell(3);
		registrosCell.setCellValue("Registros");
		registrosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		sheetNacional.addMergedRegion(CellRangeAddress.valueOf("B13:C14"));
		
		Cell mesConsultado = rowRegistros.createCell(1);
		mesConsultado.setCellValue(monthName);
		mesConsultado.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		
		if (input.getDelRegPat() && b) {
			Cell registrosCellOtras = rowRegistros.createCell(8);
			registrosCellOtras.setCellValue("Registros");
			registrosCellOtras.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			Cell registrosCellOtras = rowRegistros.createCell(7);
			registrosCellOtras.setCellValue("Registros");
			registrosCellOtras.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		}
		
		
		//Naional
		if (input.getDelRegPat() && b) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D13:H13"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("I13:L13"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D13:G13"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("H13:K13"));
		}

		Row rowDelegacion = sheetNacional.createRow(13);
		Cell delegacionCell = rowDelegacion.createCell(3);
		delegacionCell.setCellValue("Delegación");
		delegacionCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		if (input.getDelRegPat() && b) {
			Cell delegacionOtrasCell = rowDelegacion.createCell(8);
			delegacionOtrasCell.setCellValue("Otras Delegaciones");
			delegacionOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			Cell delegacionOtrasCell = rowDelegacion.createCell(7);
			delegacionOtrasCell.setCellValue("Otras Delegaciones");
			delegacionOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		}
		if (input.getDelRegPat() && b) {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D14:H14"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("I14:L14"));
		} else {
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("D14:G14"));
			sheetNacional.addMergedRegion(CellRangeAddress.valueOf("H14:K14"));
		}

		Row rowEncabezadoNacional = sheetNacional.createRow(14);
		Cell tipoArchivoCell = rowEncabezadoNacional.createCell(1);
		tipoArchivoCell.setCellValue("Tipo Archivo");
		tipoArchivoCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

		if (input.getDelRegPat()) {
			
			Cell delDetCell = rowEncabezadoNacional.createCell(2);
			delDetCell.setCellValue("Delegacion");
			delDetCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			
			Cell totalCell = rowEncabezadoNacional.createCell(3);
			totalCell.setCellValue("Total");
			totalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			Cell correctosCell = rowEncabezadoNacional.createCell(4);
			correctosCell.setCellValue("Correctos");
			correctosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosCell = rowEncabezadoNacional.createCell(5);
			erroneosCell.setCellValue("Erróneos");
			erroneosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosCell = rowEncabezadoNacional.createCell(6);
			duplicadosCell.setCellValue("Duplicados");
			duplicadosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusCell = rowEncabezadoNacional.createCell(7);
			susAjusCell.setCellValue("Susceptibles de Ajuste");
			susAjusCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell correctosOtrasCell = rowEncabezadoNacional.createCell(8);
			correctosOtrasCell.setCellValue("Correctos");
			correctosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosOtrasCell = rowEncabezadoNacional.createCell(9);
			erroneosOtrasCell.setCellValue("Erróneos");
			erroneosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosOtrasCell = rowEncabezadoNacional.createCell(10);
			duplicadosOtrasCell.setCellValue("Duplicados");
			duplicadosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusOtrasCell = rowEncabezadoNacional.createCell(11);
			susAjusOtrasCell.setCellValue("Susceptibles de Ajuste");
			susAjusOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
		} else {
			
			Cell totalCell = rowEncabezadoNacional.createCell(2);
			totalCell.setCellValue("Total");
			totalCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
			Cell correctosCell = rowEncabezadoNacional.createCell(3);
			correctosCell.setCellValue("Correctos");
			correctosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosCell = rowEncabezadoNacional.createCell(4);
			erroneosCell.setCellValue("Erróneos");
			erroneosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosCell = rowEncabezadoNacional.createCell(5);
			duplicadosCell.setCellValue("Duplicados");
			duplicadosCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusCell = rowEncabezadoNacional.createCell(6);
			susAjusCell.setCellValue("Susceptibles de Ajuste");
			susAjusCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell correctosOtrasCell = rowEncabezadoNacional.createCell(7);
			correctosOtrasCell.setCellValue("Correctos");
			correctosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell erroneosOtrasCell = rowEncabezadoNacional.createCell(8);
			erroneosOtrasCell.setCellValue("Erróneos");
			erroneosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell duplicadosOtrasCell = rowEncabezadoNacional.createCell(9);
			duplicadosOtrasCell.setCellValue("Duplicados");
			duplicadosOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));

			Cell susAjusOtrasCell = rowEncabezadoNacional.createCell(10);
			susAjusOtrasCell.setCellValue("Susceptibles de Ajuste");
			susAjusOtrasCell.setCellStyle(createStyle(font, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, HSSFColor.GREY_25_PERCENT.index, false, HSSFColor.WHITE.index, workbook, true));
			
		}
	}
	
	private CellStyle createStyle(XSSFFont font, HorizontalAlignment hAlign, VerticalAlignment vAlign,  short cellColor, boolean cellBorder, short cellBorderColor,Workbook workbook, boolean wrap) {
		 
		CellStyle style = workbook.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(cellColor);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(hAlign);
		style.setVerticalAlignment(vAlign);
		style.setWrapText(wrap);
		
 
		if (cellBorder) {
			style.setBorderTop(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderBottom(BorderStyle.THIN);
 
			style.setTopBorderColor(cellBorderColor);
			style.setLeftBorderColor(cellBorderColor);
			style.setRightBorderColor(cellBorderColor);
			style.setBottomBorderColor(cellBorderColor);
		}
 
		return style;
	}
	

}

