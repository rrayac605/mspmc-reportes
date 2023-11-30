package mx.gob.imss.cit.pmc.mspmcreportes.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;


import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ReporteCierreAnualRequestModel;
import mx.gob.imss.cit.pmc.mspmcreportes.model.ModelVersion;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestCambiosDictamen;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestConsultDictamenSist;
import mx.gob.imss.cit.pmc.mspmcreportes.services.ConsultaReporteSist;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcReportesCierreAnual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mx.gob.imss.cit.mspmccommons.dto.ErrorResponse;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaCodErrorDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaConcecuenciaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaTipoRiesgoDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcReportesService;
import mx.gob.imss.cit.pmc.mspmcreportes.services.StorageService;
import mx.gob.imss.cit.pmc.mspmcreportes.sftp.SftpUploadFile;

@RestController
@RequestMapping("/msreportes/v1")
public class MsPmcReportesController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MsPmcReportesService msPmcReportesService;

	@Autowired
	private MsPmcReportesCierreAnual msPmcReportesCierreAnual;

	@Autowired
	private StorageService storageService;

	@Autowired
	private SftpUploadFile sftUp;
	
	@Autowired
	private ConsultaReporteSist consultRepSist;
	
	private final static String version_service = "mspmc-reportes-1.0.4";
	
	private final static String folio_service = "WO30904";
	
	private final static String nota_service = "Consulta dictamen servicio SIST";
	
	@ApiOperation(value = "version", nickname = "version", notes = "version", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/version")
	public ModelVersion version() throws Exception {
		return new ModelVersion(version_service, folio_service, nota_service);
	}	
	
	@RequestMapping("/health/ready")
	@ResponseStatus(HttpStatus.OK)
	public void ready() {
	}

	@RequestMapping("/health/live")
	@ResponseStatus(HttpStatus.OK)
	public void live() {
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/reportesConsultaCodError")
	public Object reportesConsultaCodError(@RequestBody MsPmcReportesInput input) {

		Object respuesta = null;

		logger.debug("mspmccapados service ready to return");

		Map<String, DetalleConsultaCodErrorDTO> model = null;

		try {
			model = msPmcReportesService.getCodigoError(input);

			respuesta = new ResponseEntity<Map<String, DetalleConsultaCodErrorDTO>>(model, HttpStatus.OK);
		} catch (BusinessException be) {
			ErrorResponse errorResponse = be.getErrorResponse();

			int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());

			respuesta = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));

		}

		return respuesta;
	}

	@ApiOperation(value = "reportesConsultaCodErrorPDF", nickname = "reportesConsultaCodErrorPDF", notes = "reportesConsultaCodErrorPDF", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "String"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = "/reportesConsultaCodErrorPDF", produces = MediaType.APPLICATION_JSON_VALUE)
	public Object reportesConsultaCodErrorPDF(@RequestBody MsPmcReportesInput input) {

		String respuesta = null;
		try {
			respuesta = msPmcReportesService.getCodigoErrorPDF(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respuesta;
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/reportesConsultaTipoRiesgo")
	public Object reportesConsultaTipoRiesgo(@RequestBody MsPmcReportesInput input) {

		Object respuesta = null;

		logger.debug("mspmccapados service ready to return");

		Map<String, List<DetalleConsultaTipoRiesgoDTO>> model = null;

		try {
			model = msPmcReportesService.getTipoRiesgo(input);

			respuesta = new ResponseEntity<Map<String, List<DetalleConsultaTipoRiesgoDTO>>>(model, HttpStatus.OK);

		} catch (BusinessException be) {

			ErrorResponse errorResponse = be.getErrorResponse();

			int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());

			respuesta = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));

		}

		return respuesta;
	}

	@ApiOperation(value = "reportesConsultaTipoRiesgoPDF", nickname = "reportesConsultaTipoRiesgoPDF", notes = "reportesConsultaTipoRiesgoPDF", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "String"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/reportesConsultaTipoRiesgoPDF")
	public Object reportesConsultaTipoRiesgoPDF(@RequestBody MsPmcReportesInput input) {

		String respuesta = null;
		try {
			respuesta = msPmcReportesService.getTipoRiesgoPDF(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respuesta;
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/reportesConsultaConcecuencia")
	public Object reportesConsultaConcecuencia(@RequestBody MsPmcReportesInput input) {

		Object respuesta = null;

		logger.debug("mspmccapados service ready to return");

		Map<String, List<DetalleConsultaConcecuenciaDTO>> model = null;

		try {
			model = msPmcReportesService.getConsecuencia(input);

			respuesta = new ResponseEntity<Map<String, List<DetalleConsultaConcecuenciaDTO>>>(model, HttpStatus.OK);

		} catch (BusinessException be) {

			ErrorResponse errorResponse = be.getErrorResponse();

			int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());

			respuesta = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));

		}

		return respuesta;
	}

	@ApiOperation(value = "reportesConsultaConcecuenciaPDF", nickname = "reportesConsultaConcecuenciaPDF", notes = "reportesConsultaConcecuenciaPDF", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "String"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/reportesConsultaConcecuenciaPDF")
	public Object reportesConsultaConcecuenciaPDF(@RequestBody MsPmcReportesInput input) {

		String respuesta = null;
		try {
			respuesta = msPmcReportesService.getConsecuenciaPDF(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respuesta;
	}



	@ApiOperation(value = "reportesConsultaCierreAnual", nickname = "reportesConsultaCierreAnual", notes = "reportesConsultaCierreAnual", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/reportecierreanual",produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	public ResponseEntity<InputStreamResource> generar(@RequestParam("ooad") int ooad, @RequestParam("cicloActual") int ciclo, @RequestParam("rfc") boolean rfc, @RequestParam("subdelegacion") int descripcion, @RequestParam("global") boolean isGlobal ) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition","attachment; filename=test.xlsx");
		ReporteCierreAnualRequestModel input = new ReporteCierreAnualRequestModel();
				input.setCicloActual(ciclo);
				input.setRfc(rfc);
				input.setOoad(ooad);
                input.setSubDelegacion(descripcion);
				input.setGlobal(isGlobal);

		InputStreamResource streamResource = msPmcReportesCierreAnual.obtenerReporte(input);

		if( streamResource == null){
			return ResponseEntity.status(204).build();
		}

		return ResponseEntity
				.ok()
				.headers(headers)
				.body(streamResource);
	}

	@ApiOperation(value = "guardarArchivo", nickname = "guardarArchivo", notes = "guardarArchivo", response = Object.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "String"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("upload")
	public Object uploadFile(@RequestParam("file") MultipartFile mult,
			@RequestHeader HttpHeaders headers ) throws BusinessException{
		
		
		RequestCambiosDictamen req = new RequestCambiosDictamen();
		req.setIdObject(headers.getFirst("origenAlta"));
		req.setNss(headers.getFirst("nss"));
		req.setCveOrigenArchivo(headers.getFirst("cveOrigenArchivo"));
		
		Object resp = null;
		RequestCambiosDictamen model = null;
		try {
			model = sftUp.uploadFile(mult, req);
			resp = new ResponseEntity<RequestCambiosDictamen>(model, HttpStatus.OK);
		} catch (BusinessException e) {
			
			ErrorResponse errorResponse = e.getErrorResponse();
			int numberHTTPDesired = Integer.parseInt(errorResponse.getCode());
			resp = new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.valueOf(numberHTTPDesired));
		}

		return resp;
	}
	
	@ApiOperation(value = "consultaDictamen", nickname = "consultaDictamen", notes = "reportesConsultaCierreAnual", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/dictamen")
	public String dictamen(@RequestParam("nss") String nss, @RequestParam("nameFile") String nameFile) throws Exception {
		try {
			byte[] streamResource = storageService.obtenerDictamen(nss, nameFile);
			String encodedString = Base64.getEncoder().encodeToString(streamResource);
			return encodedString;	
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	@ApiOperation(value = "consultaDictamenSIST", nickname = "consultaDictamenSIST", notes = "reportesConsultaCierreAnual", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = "/consultaDictamen")
	public Object consultaDictamen(@RequestBody RequestConsultDictamenSist req, 
			@RequestHeader HttpHeaders headers) throws Exception {
		try {
			String usuario = headers.getFirst("usuario");
			return consultRepSist.consultarDictamen(req, usuario);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "mensajeDictamen", nickname = "mensajeDictamen", notes = "mensajeDictamen", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/mensajeDictamen")
	public String mensajeDictamen() throws Exception {
		return consultRepSist.getMensajeDictamen();
	}
	
	@ApiOperation(value = "mensajeDictamenIntermitencia", nickname = "mensajeDictamenIntermitencia", notes = "mensaje", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/mensajeDictamenIntermitencia")
	public String mensajeDictamenIntermitencia() throws Exception {
		return consultRepSist.getMensajeDictamenIntermitencia();
	}
	
	@ApiOperation(value = "timeOut", nickname = "timeOut", notes = "timeOut", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/timeOut")
	public int timeOut() throws Exception {
		return consultRepSist.getTimeOut();
	}
	
	@ApiOperation(value = "urlConsultaDictamen", nickname = "urlConsultaDictamen", notes = "urlConsultaDictamen", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/urlConsultaDictamen")
	public String urlConsultaDictamen() throws Exception {
		return consultRepSist.getUrlConsultaDictamen();
	}
	
	@ApiOperation(value = "cleanCache", nickname = "cleanCache", notes = "cleanCache", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/cleanCache")
	public String cleanCache() throws Exception {
		return consultRepSist.cleanCache();
	}
	
	@ApiOperation(value = "defaultCache", nickname = "defaultCache", notes = "defaultCache", response = Object.class, responseContainer = "binary", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Respuesta exitosa", response = ResponseEntity.class, responseContainer = "byte"),
			@ApiResponse(code = 204, message = "Sin resultados", response = ResponseEntity.class),
			@ApiResponse(code = 500, message = "Describe un error general del sistema", response = ErrorResponse.class) })
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = "/defaultCache")
	public String defaultCache() throws Exception {
		return consultRepSist.defaultCache();
	}
}
