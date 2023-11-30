package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import mx.gob.imss.cit.mspmccommons.enums.*;
import mx.gob.imss.cit.mspmccommons.integration.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ConsecuenciaRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.MsPmcReportesRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcReportesService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service("msPmcReportesService")
public class MsPmcReportesServiceImpl implements MsPmcReportesService {

	@Autowired
	private MsPmcReportesRepository msPmcReportesRepository;

	@Autowired
	private ConsecuenciaRepository consecuenciaRepository;

	@Autowired
	private ParametroRepository parametroRepository;

	private static final String PATTERN_DDMMYYYY_MS = "dd/MM/yyyy";
	private static final String REGISTROS = "registros";
	private static final String DIAS_SUBSIDIADOS = "diasSubsidiados";
	private static final String PORCENTAJE_INCAPACIDAD = "porcentajeIncapacidad";
	private static final Integer ONE = 1;
	private static final Integer THREE = 3;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Map<String, DetalleConsultaCodErrorDTO> getCodigoError(MsPmcReportesInput input) throws BusinessException {
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, DetalleConsultaCodErrorDTO> errores = new HashMap<String, DetalleConsultaCodErrorDTO>();
		List<ReporteCodigoErrorResponseDTO> conteo = msPmcReportesRepository.getConteoCodigoError(input);
		conteo = conteo.stream().filter(Objects::nonNull).collect(Collectors.toList());
		for (ReporteCodigoErrorResponseDTO error : conteo) {
			if(!error.get_id().equals("conteoNuevos") && !error.get_id().equals("conteoAnterior")) {
				DetalleConsultaCodErrorDTO detalle = new DetalleConsultaCodErrorDTO();
				detalle.setNumError(error.get_id());
				detalle.setDescripcion(error.getDesError());
				detalle.setNumRTT(error.getRTT_D());
				detalle.setNumRTTOtras(error.getRTT_O());
				detalle.setNumST3(error.getST3_D());
				detalle.setNumST3Otras(error.getST3_O());
				detalle.setNumST5(error.getST5_D());
				detalle.setNumST5Otras(error.getST5_O());
				detalle.setNumAJU(error.getAJU_D());
				detalle.setNumAJUOtras(error.getAJU_O());
				detalle.setNumCOD(error.getCOD_D());
				detalle.setNumCODOtras(error.getCOD_O());
				detalle.setNumROD(error.getROD_D());
				detalle.setNumRODOtras(error.getROD_O());
				detalle.setNumTotal(
						error.getRTT_D() + error.getST3_D() + error.getST5_D() + 
						error.getAJU_D() + error.getCOD_D() + error.getROD_D());
				detalle.setNumTotalOtras(
						error.getRTT_O() + error.getST3_O() + error.getST5_O() +
						error.getAJU_O() + error.getCOD_O() + error.getROD_O());
				errores.put(error.get_id(), detalle);
			}else {
				DetalleConsultaCodErrorDTO detalle = new DetalleConsultaCodErrorDTO();
				detalle.setNumError(error.get_id());
				detalle.setNumTotal(error.getConteo());
				errores.put(error.get_id(), detalle);
			}
		}
		return errores;
	}

	@Override
	public String getCodigoErrorPDF(MsPmcReportesInput input) throws BusinessException {
		DateFormat df = new SimpleDateFormat(PATTERN_DDMMYYYY_MS);
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, DetalleConsultaCodErrorDTO> errores = new HashMap<String, DetalleConsultaCodErrorDTO>();
		List<ReporteCodigoErrorResponseDTO> conteo = msPmcReportesRepository.getConteoCodigoError(input);
		List<DetalleConsultaCodErrorDTO> anteriorNuevoList = new ArrayList<>();
		DetalleConsultaCodErrorDTO anteriorNuevo = new DetalleConsultaCodErrorDTO();
		for (ReporteCodigoErrorResponseDTO error : conteo) {
			if(!error.get_id().equals("conteoNuevos") && !error.get_id().equals("conteoAnterior")) {
				DetalleConsultaCodErrorDTO detalle = new DetalleConsultaCodErrorDTO();
				detalle.setNumError(error.get_id());
				detalle.setDescripcion(error.getDesError());
				detalle.setNumRTT(error.getRTT_D());
				detalle.setNumRTTOtras(error.getRTT_O());
				detalle.setNumST3(error.getST3_D());
				detalle.setNumST3Otras(error.getST3_O());
				detalle.setNumST5(error.getST5_D());
				detalle.setNumST5Otras(error.getST5_O());
				detalle.setNumAJU(error.getAJU_D());
				detalle.setNumAJUOtras(error.getAJU_O());
				detalle.setNumCOD(error.getCOD_D());
				detalle.setNumCODOtras(error.getCOD_O());
				detalle.setNumROD(error.getROD_D());
				detalle.setNumRODOtras(error.getROD_O());
				detalle.setNumTotal(error.getRTT_D() + error.getST3_D() + error.getST5_D() + 
									error.getAJU_D() + error.getCOD_D() + error.getROD_D());
				detalle.setNumTotalOtras(error.getRTT_O() + error.getST3_O() + error.getST5_O() +
									     error.getAJU_O() + error.getCOD_O() + error.getROD_O());
				errores.put(error.get_id().toString(), detalle);
			}else if(error.get_id().equals("conteoNuevos")){
				anteriorNuevo.setNumTotal(error.getConteo());
			}else {
				anteriorNuevo.setNumTotalOtras(error.getConteo());
			}
		}
		anteriorNuevoList.add(anteriorNuevo);
		List<DetalleConsultaCodErrorDTO> erroresLista = new ArrayList<DetalleConsultaCodErrorDTO>();
		for (Map.Entry<String, DetalleConsultaCodErrorDTO> entry : errores.entrySet()) {
			erroresLista.add(entry.getValue());
		}
		Collections.sort(erroresLista);

		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion_CC");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion_CC");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion_CC");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion_CC");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte_Errores");

		Map<String, Object> parameters = new HashMap<String, Object>();
		String reporteCadena = null;

		InputStream resourceAsStream = null;
		JasperReport jasperReport = null;
		resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/erroresCarga.jrxml");

		try {
			jasperReport = JasperCompileManager.compileReport(resourceAsStream);
		} catch (JRException e) {
			logger.error(e.getMessage(), e);
		}

		parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
		parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
		parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
		parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
		parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
		parameters.put("nombreReporte", nombreReporte.get().getDesParametro());
		

		parameters.put("fromDate",
				df.format(convertToDateViaInstant(calcularFechaPoceso(input.getFromMonth(), input.getFromYear()))));
		parameters.put("toDate", df.format(convertToDateViaInstantDate(input.getToMonth(), input.getToYear())));


		String tituloDelegacion="";
		String alcanceReporte="";
		if(input.getCveDelegation() == null || input.getCveDelegation().equals("") || input.getCveDelegation().equals("-1")) {
			tituloDelegacion="Nacional";
			alcanceReporte="Nacional";
			
		}else{
			if(input.getDesDelegation() != null   &&  !input.getDesDelegation().equals("") && !input.getDesDelegation().equals("-1")){
				tituloDelegacion= tituloDelegacion + input.getCveDelegation() + " " + input.getDesDelegation();				
				alcanceReporte="Delegacional";
			}
		
			if(input.getDesSubdelegation() != null   &&  !input.getDesSubdelegation().equals("") && !input.getDesSubdelegation().equals("-1")){
				tituloDelegacion= tituloDelegacion + " / " + input.getCveSubdelegation() + " " + input.getDesSubdelegation();				
				alcanceReporte="Subdelegacional";
			}
		
		}
		
		parameters.put("alcanceReporte", alcanceReporte);
		parameters.put("desDelegacionAtencion", tituloDelegacion);
		parameters.put("cveDelegacionAtencion", input.getCveDelegation());
		parameters.put("desSubDelAtencion", input.getDesSubdelegation());
		parameters.put("cveSubDelAtencion", input.getCveSubdelegation());
		parameters.put("erroresLista", erroresLista);
		parameters.put("anteriorNuevos", anteriorNuevoList);

		JasperPrint print = null;
		try {
			print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

			reporteCadena = Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return reporteCadena;
	}

	public DetalleConsultaCodErrorDTO llenarSumatoria(String origenArchivo, DetalleRegistroDTO detalleRegistroDTO,
			DetalleConsultaCodErrorDTO detalleConsultaCodErrorDTO, BitacoraErroresDTO bitacoraErroresDTO) {
		if (detalleConsultaCodErrorDTO.getNumError() == null) {
			detalleConsultaCodErrorDTO.setNumError(bitacoraErroresDTO.getCveIdCodigoError());
		}
		if (detalleConsultaCodErrorDTO.getDescripcion() == null) {
			detalleConsultaCodErrorDTO.setDescripcion(bitacoraErroresDTO.getDesCampo());
		}
		switch (origenArchivo) {
		case "ST3": {
			if (Arrays.asList(1, 2, 3, 4).contains(detalleRegistroDTO.getAseguradoDTO().getCveEstadoRegistro())) {
				detalleConsultaCodErrorDTO.setNumST3(detalleConsultaCodErrorDTO.getNumST3() + 1);
				detalleConsultaCodErrorDTO.setNumTotal(detalleConsultaCodErrorDTO.getNumTotal() + 1);
			} else {
				detalleConsultaCodErrorDTO.setNumST3Otras(detalleConsultaCodErrorDTO.getNumST3Otras() + 1);
				detalleConsultaCodErrorDTO.setNumTotalOtras(detalleConsultaCodErrorDTO.getNumTotalOtras() + 1);
			}
			break;
		}
		case "RTT": {
			if (Arrays.asList(1, 2, 3, 4).contains(detalleRegistroDTO.getAseguradoDTO().getCveEstadoRegistro())) {
				detalleConsultaCodErrorDTO.setNumRTT(detalleConsultaCodErrorDTO.getNumRTT() + 1);
				detalleConsultaCodErrorDTO.setNumTotal(detalleConsultaCodErrorDTO.getNumTotal() + 1);
			} else {
				detalleConsultaCodErrorDTO.setNumRTTOtras(detalleConsultaCodErrorDTO.getNumRTTOtras() + 1);
				detalleConsultaCodErrorDTO.setNumTotalOtras(detalleConsultaCodErrorDTO.getNumTotalOtras() + 1);
			}

			break;
		}
		case "ST5": {
			if (Arrays.asList(1, 2, 3, 4).contains(detalleRegistroDTO.getAseguradoDTO().getCveEstadoRegistro())) {
				detalleConsultaCodErrorDTO.setNumST5(detalleConsultaCodErrorDTO.getNumST5() + 1);
				detalleConsultaCodErrorDTO.setNumTotal(detalleConsultaCodErrorDTO.getNumTotal() + 1);
			} else {
				detalleConsultaCodErrorDTO.setNumST5Otras(detalleConsultaCodErrorDTO.getNumST5Otras() + 1);
				detalleConsultaCodErrorDTO.setNumTotalOtras(detalleConsultaCodErrorDTO.getNumTotalOtras() + 1);
			}
			break;
		}
		default: {
			break;
		}
		}
		return detalleConsultaCodErrorDTO;
	}

	@Override
	public Map<String, List<DetalleConsultaTipoRiesgoDTO>> getTipoRiesgo(MsPmcReportesInput input)
			throws BusinessException {
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, List<DetalleConsultaTipoRiesgoDTO>> tiposRiesgo = new HashMap<String, List<DetalleConsultaTipoRiesgoDTO>>();
		ReporteTipoRiesgoResponseDTO conteo = msPmcReportesRepository.getConteo(input);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo1 = fillResponse(conteo, 1);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo2 = fillResponse(conteo, 2);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo3 = fillResponse(conteo, 3);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo4 = fillResponse1y3(detalleTipoRiesgo1, detalleTipoRiesgo3);
		tiposRiesgo.put("1", detalleTipoRiesgo1);
		tiposRiesgo.put("2", detalleTipoRiesgo2);
		tiposRiesgo.put("3", detalleTipoRiesgo3);
		tiposRiesgo.put("4", detalleTipoRiesgo4);

		return tiposRiesgo;
	}

	private List<DetalleConsultaTipoRiesgoDTO> fillResponse(ReporteTipoRiesgoResponseDTO conteo, Integer tipoRiesgo) {
		List<DetalleConsultaTipoRiesgoDTO> detallesConsultaTipoRiesgoDTO = new ArrayList<DetalleConsultaTipoRiesgoDTO>();
		// Se llenan los registros
		Optional<ReporteTipoRiesgoDetalleDTO> rDActual = conteo.getRActual() != null && conteo.getRActual().size() > 0 ?
				conteo.getRActual().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> rDInmediato = conteo.getRInmediatoAnterior() != null && conteo.getRInmediatoAnterior().size() > 0 ?
				conteo.getRInmediatoAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> rDAnterior = conteo.getRAnterior() != null && conteo.getRAnterior().size() > 0 ?
				conteo.getRAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> rDPosterior = conteo.getRPosterior() != null && conteo.getRPosterior().size() > 0 ?
				conteo.getRPosterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Long rActual = rDActual.isPresent() ? rDActual.get().getConteo() : 0L;
		Long rInmediato = rDInmediato.isPresent() ? rDInmediato.get().getConteo() : 0L;
		Long rAnterior = rDAnterior.isPresent() ? rDAnterior.get().getConteo() : 0L;
		Long rPosterior = rDPosterior.isPresent() ? rDPosterior.get().getConteo() : 0L;
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO1 = new DetalleConsultaTipoRiesgoDTO();
		detalleConsultaTipoRiesgoDTO1.setClave(1);
		detalleConsultaTipoRiesgoDTO1.setDescripcion("Registros");
		detalleConsultaTipoRiesgoDTO1.setNumActual(rActual);
		detalleConsultaTipoRiesgoDTO1.setNumInmediato(rInmediato);
		detalleConsultaTipoRiesgoDTO1.setNumAnteriores(rAnterior);
		detalleConsultaTipoRiesgoDTO1.setNumPosterior(rPosterior);
		detalleConsultaTipoRiesgoDTO1.setNumTotal(rActual + rInmediato + rAnterior + rPosterior);
		// Se llenan los dias subsidiados
		Optional<ReporteTipoRiesgoDetalleDTO> dsDActual = conteo.getDsActual() != null && conteo.getDsActual().size() > 0 ?
				conteo.getDsActual().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dsDInmediato = conteo.getDsInmediatoAnterior() != null && conteo.getDsInmediatoAnterior().size() > 0 ?
				conteo.getDsInmediatoAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dsDAnterior = conteo.getDsAnterior() != null && conteo.getDsAnterior().size() > 0 ?
				conteo.getDsAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dsDPosterior = conteo.getDsPosterior() != null && conteo.getDsPosterior().size() > 0 ?
				conteo.getDsPosterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Long dsActual = dsDActual.isPresent() ? dsDActual.get().getConteo() : 0L;
		Long dsInmediato = dsDInmediato.isPresent() ? dsDInmediato.get().getConteo() : 0L;
		Long dsAnterior = dsDAnterior.isPresent() ? dsDAnterior.get().getConteo() : 0L;
		Long dsPosterior = dsDPosterior.isPresent() ? dsDPosterior.get().getConteo() : 0L;
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO2 = new DetalleConsultaTipoRiesgoDTO();
		detalleConsultaTipoRiesgoDTO2.setClave(2);
		detalleConsultaTipoRiesgoDTO2.setDescripcion("Días subsidiados");
		detalleConsultaTipoRiesgoDTO2.setNumActual(dsActual);
		detalleConsultaTipoRiesgoDTO2.setNumInmediato(dsInmediato);
		detalleConsultaTipoRiesgoDTO2.setNumAnteriores(dsAnterior);
		detalleConsultaTipoRiesgoDTO2.setNumPosterior(dsPosterior);
		detalleConsultaTipoRiesgoDTO2.setNumTotal(dsActual + dsInmediato + dsAnterior + dsPosterior);
		// Se llena el porcentaje de incapacidad
		Optional<ReporteTipoRiesgoDetalleDTO> piDActual = conteo.getPiActual() != null && conteo.getPiActual().size() > 0 ?
				conteo.getPiActual().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> piDInmediato = conteo.getPiInmediatoAnterior() != null && conteo.getPiInmediatoAnterior().size() > 0 ?
				conteo.getPiInmediatoAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> piDAnterior = conteo.getPiAnterior() != null && conteo.getPiAnterior().size() > 0 ?
				conteo.getPiAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> piDPosterior = conteo.getPiPosterior() != null && conteo.getPiPosterior().size() > 0 ?
				conteo.getPiPosterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Long piActual = piDActual.isPresent() ? piDActual.get().getConteo() : 0L;
		Long piInmediato = piDInmediato.isPresent() ? piDInmediato.get().getConteo() : 0L;
		Long piAnterior = piDAnterior.isPresent() ? piDAnterior.get().getConteo() : 0L;
		Long piPosterior = piDPosterior.isPresent() ? piDPosterior.get().getConteo() : 0L;
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO3 = new DetalleConsultaTipoRiesgoDTO();
		detalleConsultaTipoRiesgoDTO3.setClave(3);
		detalleConsultaTipoRiesgoDTO3.setDescripcion("Porcentaje de incapacidad");
		detalleConsultaTipoRiesgoDTO3.setNumActual(piActual);
		detalleConsultaTipoRiesgoDTO3.setNumInmediato(piInmediato);
		detalleConsultaTipoRiesgoDTO3.setNumAnteriores(piAnterior);
		detalleConsultaTipoRiesgoDTO3.setNumPosterior(piPosterior);
		detalleConsultaTipoRiesgoDTO3.setNumTotal(piActual + piInmediato + piAnterior + piPosterior);
		// Se llenan las defunciones
		Optional<ReporteTipoRiesgoDetalleDTO> dDActual = conteo.getDActual() != null && conteo.getDActual().size() > 0 ?
				conteo.getDActual().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dDInmediato = conteo.getDInmediatoAnterior() != null && conteo.getDInmediatoAnterior().size() > 0 ?
				conteo.getDInmediatoAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dDAnterior = conteo.getDAnterior() != null && conteo.getDAnterior().size() > 0 ?
				conteo.getDAnterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteTipoRiesgoDetalleDTO> dDPosterior = conteo.getDPosterior() != null && conteo.getDPosterior().size() > 0 ?
				conteo.getDPosterior().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Long dActual = dDActual.isPresent() ? dDActual.get().getConteo() : 0L;
		Long dInmediato = dDInmediato.isPresent() ? dDInmediato.get().getConteo() : 0L;
		Long dAnterior = dDAnterior.isPresent() ? dDAnterior.get().getConteo() : 0L;
		Long dPosterior = dDPosterior.isPresent() ? dDPosterior.get().getConteo() : 0L;
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO4 = new DetalleConsultaTipoRiesgoDTO();
		detalleConsultaTipoRiesgoDTO4.setClave(4);
		detalleConsultaTipoRiesgoDTO4.setDescripcion("Defunciones");
		detalleConsultaTipoRiesgoDTO4.setNumActual(dActual);
		detalleConsultaTipoRiesgoDTO4.setNumInmediato(dInmediato);
		detalleConsultaTipoRiesgoDTO4.setNumAnteriores(dAnterior);
		detalleConsultaTipoRiesgoDTO4.setNumPosterior(dPosterior);
		detalleConsultaTipoRiesgoDTO4.setNumTotal(dActual + dInmediato + dAnterior + dPosterior);

		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO1);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO2);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO3);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO4);
		return detallesConsultaTipoRiesgoDTO;
	}

	private List<DetalleConsultaTipoRiesgoDTO> fillResponse1y3(List<DetalleConsultaTipoRiesgoDTO> tr1,
															   List<DetalleConsultaTipoRiesgoDTO> tr3) {
		List<DetalleConsultaTipoRiesgoDTO> detallesConsultaTipoRiesgoDTO = new ArrayList<DetalleConsultaTipoRiesgoDTO>();
		// Se llenan los registros
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO1 = new DetalleConsultaTipoRiesgoDTO();
		DetalleConsultaTipoRiesgoDTO detalleActualTr1 = tr1.get(0);
		DetalleConsultaTipoRiesgoDTO detalleActualTr3 = tr3.get(0);
		detalleConsultaTipoRiesgoDTO1.setClave(detalleActualTr1.getClave());
		detalleConsultaTipoRiesgoDTO1.setDescripcion(detalleActualTr1.getDescripcion());
		Long rActual = detalleActualTr1.getNumActual() + detalleActualTr3.getNumActual();
		Long rInmediato = detalleActualTr1.getNumInmediato() + detalleActualTr3.getNumInmediato();
		Long rAnterior = detalleActualTr1.getNumAnteriores() + detalleActualTr3.getNumAnteriores();
		Long rPosterior = detalleActualTr1.getNumPosterior() + detalleActualTr3.getNumPosterior();
		detalleConsultaTipoRiesgoDTO1.setNumActual(rActual);
		detalleConsultaTipoRiesgoDTO1.setNumInmediato(rInmediato);
		detalleConsultaTipoRiesgoDTO1.setNumAnteriores(rAnterior);
		detalleConsultaTipoRiesgoDTO1.setNumPosterior(rPosterior);
		detalleConsultaTipoRiesgoDTO1.setNumTotal(rActual + rInmediato + rAnterior + rPosterior);
		// Se llenan los dias subsidiados
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO2 = new DetalleConsultaTipoRiesgoDTO();
		detalleActualTr1 = tr1.get(1);
		detalleActualTr3 = tr3.get(1);
		detalleConsultaTipoRiesgoDTO2.setClave(detalleActualTr1.getClave());
		detalleConsultaTipoRiesgoDTO2.setDescripcion(detalleActualTr1.getDescripcion());
		Long dsActual = detalleActualTr1.getNumActual() + detalleActualTr3.getNumActual();
		Long dsInmediato = detalleActualTr1.getNumInmediato() + detalleActualTr3.getNumInmediato();
		Long dsAnterior = detalleActualTr1.getNumAnteriores() + detalleActualTr3.getNumAnteriores();
		Long dsPosterior = detalleActualTr1.getNumPosterior() + detalleActualTr3.getNumPosterior();
		detalleConsultaTipoRiesgoDTO2.setNumActual(dsActual);
		detalleConsultaTipoRiesgoDTO2.setNumInmediato(dsInmediato);
		detalleConsultaTipoRiesgoDTO2.setNumAnteriores(dsAnterior);
		detalleConsultaTipoRiesgoDTO2.setNumPosterior(dsPosterior);
		detalleConsultaTipoRiesgoDTO2.setNumTotal(dsActual + dsInmediato + dsAnterior + dsPosterior);
		// Se llena el porcentaje de incapacidad
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO3 = new DetalleConsultaTipoRiesgoDTO();
		detalleActualTr1 = tr1.get(2);
		detalleActualTr3 = tr3.get(2);
		detalleConsultaTipoRiesgoDTO3.setClave(detalleActualTr1.getClave());
		detalleConsultaTipoRiesgoDTO3.setDescripcion(detalleActualTr1.getDescripcion());
		Long piActual = detalleActualTr1.getNumActual() + detalleActualTr3.getNumActual();
		Long piInmediato = detalleActualTr1.getNumInmediato() + detalleActualTr3.getNumInmediato();
		Long piAnterior = detalleActualTr1.getNumAnteriores() + detalleActualTr3.getNumAnteriores();
		Long piPosterior = detalleActualTr1.getNumPosterior() + detalleActualTr3.getNumPosterior();
		detalleConsultaTipoRiesgoDTO3.setNumActual(piActual);
		detalleConsultaTipoRiesgoDTO3.setNumInmediato(piInmediato);
		detalleConsultaTipoRiesgoDTO3.setNumAnteriores(piAnterior);
		detalleConsultaTipoRiesgoDTO3.setNumPosterior(piPosterior);
		detalleConsultaTipoRiesgoDTO3.setNumTotal(piActual + piInmediato + piAnterior + piPosterior);
		// Se llenan las defunciones
		DetalleConsultaTipoRiesgoDTO detalleConsultaTipoRiesgoDTO4 = new DetalleConsultaTipoRiesgoDTO();
		detalleActualTr1 = tr1.get(3);
		detalleActualTr3 = tr3.get(3);
		detalleConsultaTipoRiesgoDTO4.setClave(detalleActualTr1.getClave());
		detalleConsultaTipoRiesgoDTO4.setDescripcion(detalleActualTr1.getDescripcion());
		Long dActual = detalleActualTr1.getNumActual() + detalleActualTr3.getNumActual();
		Long dInmediato = detalleActualTr1.getNumInmediato() + detalleActualTr3.getNumInmediato();
		Long dAnterior = detalleActualTr1.getNumAnteriores() + detalleActualTr3.getNumAnteriores();
		Long dPosterior = detalleActualTr1.getNumPosterior() + detalleActualTr3.getNumPosterior();
		detalleConsultaTipoRiesgoDTO4.setNumActual(dActual);
		detalleConsultaTipoRiesgoDTO4.setNumInmediato(dInmediato);
		detalleConsultaTipoRiesgoDTO4.setNumAnteriores(dAnterior);
		detalleConsultaTipoRiesgoDTO4.setNumPosterior(dPosterior);
		detalleConsultaTipoRiesgoDTO4.setNumTotal(dActual + dInmediato + dAnterior + dPosterior);

		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO1);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO2);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO3);
		detallesConsultaTipoRiesgoDTO.add(detalleConsultaTipoRiesgoDTO4);
		return detallesConsultaTipoRiesgoDTO;
	}

	@Override
	public String getTipoRiesgoPDF(MsPmcReportesInput input) throws BusinessException {
		DateFormat df = new SimpleDateFormat(PATTERN_DDMMYYYY_MS);
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, List<DetalleConsultaTipoRiesgoDTO>> tiposRiesgo = new HashMap<String, List<DetalleConsultaTipoRiesgoDTO>>();

		ReporteTipoRiesgoResponseDTO conteo = msPmcReportesRepository.getConteo(input);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo1 = fillResponse(conteo, 1);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo2 = fillResponse(conteo, 2);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo3 = fillResponse(conteo, 3);
		List<DetalleConsultaTipoRiesgoDTO> detalleTipoRiesgo4 = fillResponse1y3(detalleTipoRiesgo1, detalleTipoRiesgo3);
		tiposRiesgo.put("1", detalleTipoRiesgo1);
		tiposRiesgo.put("2", detalleTipoRiesgo2);
		tiposRiesgo.put("3", detalleTipoRiesgo3);
		tiposRiesgo.put("4", detalleTipoRiesgo4);

		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("tipo1", detalleTipoRiesgo1);
		parameters.put("tipo2", detalleTipoRiesgo2);
		parameters.put("tipo3", detalleTipoRiesgo3);
		parameters.put("tipo4", detalleTipoRiesgo4);

		
		String tituloDelegacion="";
		String alcanceReporte="";
		if(input.getCveDelegation() == null || input.getCveDelegation().equals("") || input.getCveDelegation().equals("-1")) {
			tituloDelegacion="Nacional";
			alcanceReporte="Nacional";
			
		}else{
			if(input.getDesDelegation() != null   &&  !input.getDesDelegation().equals("") && !input.getDesDelegation().equals("-1")){
				tituloDelegacion= tituloDelegacion + input.getCveDelegation() + " " + input.getDesDelegation();				
				alcanceReporte="Delegacional";
			}
		
			if(input.getDesSubdelegation() != null   &&  !input.getDesSubdelegation().equals("") && !input.getDesSubdelegation().equals("-1")){
				tituloDelegacion= tituloDelegacion + " / " + input.getCveSubdelegation() + " " + input.getDesSubdelegation();				
				alcanceReporte="Subdelegacional";
			}
		
		}
		
		parameters.put("alcanceReporte", alcanceReporte);
		Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
		Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion_CC");
		Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion_CC");
		Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion_CC");
		Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion_CC");
		Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte_Riesgo");

		String reporteCadena = null;

		InputStream resourceAsStream = null;
		JasperReport jasperReport = null;
		resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/tipoRiesgo.jrxml");

		try {
			jasperReport = JasperCompileManager.compileReport(resourceAsStream);
		} catch (JRException e) {
			logger.error(e.getMessage(), e);
		}

		parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
		parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
		parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
		parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
		parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
		parameters.put("nombreReporte", nombreReporte.get().getDesParametro());

		parameters.put("fromDate",
				df.format(convertToDateViaInstant(calcularFechaPoceso(input.getFromMonth(), input.getFromYear()))));
		parameters.put("toDate", df.format(convertToDateViaInstantDate(input.getToMonth(), input.getToYear())));
	
		parameters.put("desDelegacionAtencion",tituloDelegacion);
		parameters.put("cveDelegacionAtencion", input.getCveDelegation());
		parameters.put("desSubDelAtencion", input.getDesSubdelegation());
		parameters.put("cveSubDelAtencion", input.getCveSubdelegation());

		ReporteTipoRiesgoResponseDTO anteriorNuevo = msPmcReportesRepository.getAnteriorNuevo(input);

		parameters.put("Parameter1",
				anteriorNuevo.getConteoCorrectosAnterior() != null && anteriorNuevo.getConteoCorrectosAnterior().size() > 0 ?
				anteriorNuevo.getConteoCorrectosAnterior().get(0).getConteo() : 0);
		parameters.put("Parameter2",
				anteriorNuevo.getConteoCorrectosActual() != null && anteriorNuevo.getConteoCorrectosActual().size() > 0 ?
				anteriorNuevo.getConteoCorrectosActual().get(0).getConteo() : 0);

		JasperPrint print = null;
		try {
			print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

			reporteCadena = Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return reporteCadena;
	}

	@Override
	public Map<String, List<DetalleConsultaConcecuenciaDTO>> getConsecuencia(MsPmcReportesInput input)
			throws BusinessException {
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, List<DetalleConsultaConcecuenciaDTO>> tiposRiesgo = new HashMap<String, List<DetalleConsultaConcecuenciaDTO>>();
		Optional<List<ConsecuenciaDTO>> consecuenciaList = consecuenciaRepository.findAll();
		if (consecuenciaList.isPresent()) {
			ReporteCasuisticaConsecuenciaResponseDTO conteos = msPmcReportesRepository.getConteo(input, consecuenciaList.get());
			tiposRiesgo.put("1", fillResponse(conteos, 1, consecuenciaList.get()));
			tiposRiesgo.put("2", fillResponse(conteos, 2, consecuenciaList.get()));
			tiposRiesgo.put("3", fillResponse(conteos, 3, consecuenciaList.get()));
			tiposRiesgo.put("4", fillResponse1y3(conteos, consecuenciaList.get()));
		}
		return tiposRiesgo;
	}

	private List<DetalleConsultaConcecuenciaDTO> fillResponse(ReporteCasuisticaConsecuenciaResponseDTO conteo, Integer tipoRiesgo, List<ConsecuenciaDTO> consecuenciasList) {
		List<DetalleConsultaConcecuenciaDTO> detallesConsultaConsecuenciaDTO = new ArrayList<>();
		// Se llenan los registros
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> registros = conteo.getRegistros() != null && conteo.getRegistros().size() > 0 ?
				conteo.getRegistros().stream().filter(ra -> tipoRiesgo.equals(ra.get_id())).findFirst() : Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> diasSubsidiados = conteo.getDiasSubsidiados() !=  null && conteo.getDiasSubsidiados().size() > 0 ?
				conteo.getDiasSubsidiados().stream().filter(ds -> tipoRiesgo.equals(ds.get_id())).findFirst() : Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> porcentajeIncapacidad = conteo.getPorcentajeIncapacidad() != null && conteo.getPorcentajeIncapacidad().size() > 0 ?
				conteo.getPorcentajeIncapacidad().stream().filter(pi -> tipoRiesgo.equals(pi.get_id())).findFirst() : Optional.empty();
		Long rTotal = 0L;
		Long dsTotal = 0L;
		Long piTotal = 0L;
		for (ConsecuenciaDTO consecuencia : consecuenciasList) {
			Map<String, Long> conteos = getConteos(registros.orElse(null), diasSubsidiados.orElse(null),
					porcentajeIncapacidad.orElse(null), consecuencia.getCveIdConsecuencia());
			rTotal = rTotal + conteos.get(REGISTROS);
			dsTotal = dsTotal + conteos.get(DIAS_SUBSIDIADOS);
			piTotal = piTotal + conteos.get(PORCENTAJE_INCAPACIDAD);
			DetalleConsultaConcecuenciaDTO detalleConsultaConcecuenciaDTO = new DetalleConsultaConcecuenciaDTO();
			detalleConsultaConcecuenciaDTO.setClave(consecuencia.getCveIdConsecuencia());
			detalleConsultaConcecuenciaDTO.setDescripcion(consecuencia.getDesConsecuencia());
			detalleConsultaConcecuenciaDTO.setNumRegistro(conteos.get(REGISTROS));
			detalleConsultaConcecuenciaDTO.setNumDias(conteos.get(DIAS_SUBSIDIADOS));
			detalleConsultaConcecuenciaDTO.setPorcentaje(conteos.get(PORCENTAJE_INCAPACIDAD));
			detallesConsultaConsecuenciaDTO.add(detalleConsultaConcecuenciaDTO);
		}
		return detallesConsultaConsecuenciaDTO;
	}

	private List<DetalleConsultaConcecuenciaDTO> fillResponse1y3(ReporteCasuisticaConsecuenciaResponseDTO conteo, List<ConsecuenciaDTO> consecuenciasList) {
		List<DetalleConsultaConcecuenciaDTO> detallesConsultaConsecuenciaDTO = new ArrayList<>();
		// Se llenan los registros
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> registros1 = Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> diasSubsidiados1 =Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> porcentajeIncapacidad1 =Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> registros3 = Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> diasSubsidiados3 = Optional.empty();
		Optional<ReporteCasuisticaConsecuenciaDetalleDTO> porcentajeIncapacidad3 = Optional.empty();
		if (conteo.getRegistros() != null && conteo.getRegistros().size() > 0) {
			registros1 = conteo.getRegistros().stream().filter(ra -> ONE.equals(ra.get_id())).findFirst();
			registros3 = conteo.getRegistros().stream().filter(ra -> THREE.equals(ra.get_id())).findFirst();
		}
		if (conteo.getDiasSubsidiados() != null && conteo.getDiasSubsidiados().size() > 0) {
			diasSubsidiados1 = conteo.getDiasSubsidiados().stream().filter(ra -> ONE.equals(ra.get_id())).findFirst();
			diasSubsidiados3 = conteo.getDiasSubsidiados().stream().filter(ra -> THREE.equals(ra.get_id())).findFirst();
		}
		if (conteo.getPorcentajeIncapacidad() != null && conteo.getPorcentajeIncapacidad().size() > 0) {
			porcentajeIncapacidad1 = conteo.getPorcentajeIncapacidad().stream().filter(ra -> ONE.equals(ra.get_id())).findFirst();
			porcentajeIncapacidad3 = conteo.getPorcentajeIncapacidad().stream().filter(ra -> THREE.equals(ra.get_id())).findFirst();
		}
		for (ConsecuenciaDTO consecuencia : consecuenciasList) {
			Map<String, Long> conteos1 = getConteos(registros1.orElse(null), diasSubsidiados1.orElse(null),
					porcentajeIncapacidad1.orElse(null), consecuencia.getCveIdConsecuencia());
			Map<String, Long> conteos3 = getConteos(registros3.orElse(null), diasSubsidiados3.orElse(null),
					porcentajeIncapacidad3.orElse(null), consecuencia.getCveIdConsecuencia());
			DetalleConsultaConcecuenciaDTO detalleConsultaConcecuenciaDTO = new DetalleConsultaConcecuenciaDTO();
			detalleConsultaConcecuenciaDTO.setClave(consecuencia.getCveIdConsecuencia());
			detalleConsultaConcecuenciaDTO.setDescripcion(consecuencia.getDesConsecuencia());
			detalleConsultaConcecuenciaDTO.setNumRegistro(conteos1.get(REGISTROS) + conteos3.get(REGISTROS));
			detalleConsultaConcecuenciaDTO.setNumDias(conteos1.get(DIAS_SUBSIDIADOS) + conteos3.get(DIAS_SUBSIDIADOS));
			detalleConsultaConcecuenciaDTO.setPorcentaje(conteos1.get(PORCENTAJE_INCAPACIDAD) + conteos3.get(PORCENTAJE_INCAPACIDAD));
			detallesConsultaConsecuenciaDTO.add(detalleConsultaConcecuenciaDTO);
		}
		return detallesConsultaConsecuenciaDTO;
	}

	private Map<String, Long> getConteos(ReporteCasuisticaConsecuenciaDetalleDTO registros,
										 ReporteCasuisticaConsecuenciaDetalleDTO diasSubsidiados,
										 ReporteCasuisticaConsecuenciaDetalleDTO porcentajeIncapacidad,
										 Integer cveConsecuencia) {
		Map<String, Long> conteos = new HashMap<>();
		switch (cveConsecuencia) {
			case 0: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo0() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo0() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo0() : 0L);
				break;
			}
			case 1: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo1() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo1() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo1() : 0L);
				break;
			}
			case 2: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo2() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo2() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo2() : 0L);
				break;
			}
			case 3: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo3() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo3() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo3() : 0L);
				break;
			}
			case 4: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo4() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo4() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo4() : 0L);
				break;
			}
			case 5: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo5() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo5() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo5() : 0L);
				break;
			}
			case 6: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo6() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo6() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo6() : 0L);
				break;
			}
			case 7: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo7() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo7() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo7() : 0L);
				break;
			}
			case 8: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo8() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo8() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo8() : 0L);
				break;
			}
			case 9: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo9() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo9() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo9() : 0L);
				break;
			}
			case 10: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo10() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo10() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo10() : 0L);
				break;
			}
			case 11: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo11() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo11() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo11() : 0L);
				break;
			}
			case 12: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo12() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo12() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo12() : 0L);
				break;
			}
			case 13: {
				conteos.put(REGISTROS, registros != null ? registros.getConteo13() : 0L);
				conteos.put(DIAS_SUBSIDIADOS, diasSubsidiados != null ? diasSubsidiados.getConteo13() : 0L);
				conteos.put(PORCENTAJE_INCAPACIDAD, porcentajeIncapacidad != null ? porcentajeIncapacidad.getConteo13() : 0L);
				break;
			}
		}
		return conteos;
	}

	@Override
	public String getConsecuenciaPDF(MsPmcReportesInput input) throws BusinessException {
		DateFormat df = new SimpleDateFormat(PATTERN_DDMMYYYY_MS);
		String reporteCadena = null;
		LocalDateTime fecProcesoIni = calcularFechaPoceso(input.getFromMonth(), input.getFromYear());
		logger.info("ArchivoDTORepositoryImpl:getAllListArchivos:fecProcesoInicio: " + fecProcesoIni);
		Map<String, Object> parameters = new HashMap<String, Object>();
		Optional<List<ConsecuenciaDTO>> consecuenciaList = consecuenciaRepository.findAll();
		if (consecuenciaList.isPresent()) {
			ReporteCasuisticaConsecuenciaResponseDTO conteos = msPmcReportesRepository.getConteo(input, consecuenciaList.get());
			parameters.put("consecuencia1", fillResponse(conteos, 1, consecuenciaList.get()));
			parameters.put("consecuencia2", fillResponse(conteos, 2, consecuenciaList.get()));
			parameters.put("consecuencia3", fillResponse(conteos, 3, consecuenciaList.get()));
			parameters.put("consecuencia4", fillResponse1y3(conteos, consecuenciaList.get()));
			Optional<ParametroDTO> nombreInstitucion = parametroRepository.findOneByCve("nombreInstitucion");
			Optional<ParametroDTO> direccionInstitucion = parametroRepository.findOneByCve("direccionInstitucion_CC");
			Optional<ParametroDTO> unidadInstitucion = parametroRepository.findOneByCve("unidadInstitucion_CC");
			Optional<ParametroDTO> coordinacionInstituc = parametroRepository.findOneByCve("coordinacionInstitucion_CC");
			Optional<ParametroDTO> divisionInstitucion = parametroRepository.findOneByCve("divisionInstitucion_CC");
			Optional<ParametroDTO> nombreReporte = parametroRepository.findOneByCve("nombreReporte_Consecuencia");

			InputStream resourceAsStream = null;
			JasperReport jasperReport = null;
			resourceAsStream = ReporteServiceImpl.class.getResourceAsStream("/consecuencia.jrxml");

			try {
				jasperReport = JasperCompileManager.compileReport(resourceAsStream);
			} catch (JRException e) {
				logger.error(e.getMessage(), e);
			}

			parameters.put("nombreInstitucion", nombreInstitucion.get().getDesParametro());
			parameters.put("direccionInstitucion", direccionInstitucion.get().getDesParametro());
			parameters.put("unidadInstitucion", unidadInstitucion.get().getDesParametro());
			parameters.put("coordinacionInstituc", coordinacionInstituc.get().getDesParametro());
			parameters.put("divisionInstitucion", divisionInstitucion.get().getDesParametro());
			parameters.put("nombreReporte", nombreReporte.get().getDesParametro());

			parameters.put("fromDate",
					df.format(convertToDateViaInstant(calcularFechaPoceso(input.getFromMonth(), input.getFromYear()))));
			parameters.put("toDate", df.format(convertToDateViaInstantDate(input.getToMonth(), input.getToYear())));

			String tituloDelegacion="";
			String alcanceReporte="";
			if(input.getCveDelegation() == null || input.getCveDelegation().equals("") || input.getCveDelegation().equals("-1")) {
				tituloDelegacion="Nacional";
				alcanceReporte="Nacional";

			}else{
				if(input.getDesDelegation() != null   &&  !input.getDesDelegation().equals("") && !input.getDesDelegation().equals("-1")){
					tituloDelegacion= tituloDelegacion + input.getCveDelegation() + " " + input.getDesDelegation();
					alcanceReporte="Delegacional";
				}

				if(input.getDesSubdelegation() != null   &&  !input.getDesSubdelegation().equals("") && !input.getDesSubdelegation().equals("-1")){
					tituloDelegacion= tituloDelegacion + " / " + input.getCveSubdelegation() + " " + input.getDesSubdelegation();
					alcanceReporte="Subdelegacional";
				}

			}

			parameters.put("alcanceReporte", alcanceReporte);

			parameters.put("desDelegacionAtencion",tituloDelegacion);
			parameters.put("cveDelegacionAtencion", input.getCveDelegation());
			parameters.put("desSubDelAtencion", input.getDesSubdelegation());
			parameters.put("cveSubDelAtencion", input.getCveSubdelegation());

			JasperPrint print = null;
			try {
				print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

				reporteCadena = Base64.getEncoder().encodeToString(JasperExportManager.exportReportToPdf(print));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return reporteCadena;
	}

	private LocalDateTime calcularFechaPoceso(String month, String year) {
		return LocalDateTime.of(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(1), 0, 0, 0, 0);
	}

	public Date convertToDateViaInstant(LocalDateTime dateToConvert) {
		Date out = Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
		return out;
	}

	private Date convertToDateViaInstantDate(String month, String year) {
		LocalDateTime initial = LocalDateTime.of(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(1), 23,
				59, 59, 59);
		LocalDateTime lastDayOfMonth = initial.with(TemporalAdjusters.lastDayOfMonth());
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
		cal.set(Calendar.YEAR, Integer.valueOf(year));
		cal.set(Calendar.DATE, lastDayOfMonth.getDayOfMonth());
		return cal.getTime();
	}

}
