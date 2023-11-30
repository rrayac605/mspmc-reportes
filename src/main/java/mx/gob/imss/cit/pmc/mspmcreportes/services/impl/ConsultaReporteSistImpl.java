package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import mx.gob.imss.cit.mspmccommons.dto.BitacoraConsultaST3;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.mspmccommons.utils.StringUtils;
import mx.gob.imss.cit.pmc.mspmcreportes.constants.Constants;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.BitacoraConsultaST3Repository;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ParametroRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestConsultDictamenSist;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestSistService;
import mx.gob.imss.cit.pmc.mspmcreportes.model.ResponseConsultDictamenSist;
import mx.gob.imss.cit.pmc.mspmcreportes.model.ResponseSistService;
import mx.gob.imss.cit.pmc.mspmcreportes.services.ConsultaReporteSist;

@Service("ConsultaReporteSistImpl")
public class ConsultaReporteSistImpl implements ConsultaReporteSist {

	@Autowired
	private ParametroRepository parametroRepository;
	
	@Autowired
	private BitacoraConsultaST3Repository bitacoraRepository;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Override
	public ResponseConsultDictamenSist consultarDictamen(RequestConsultDictamenSist req, String usuario) throws BusinessException {
		return getResponse(validaReq(req), usuario);
	}

	private RequestConsultDictamenSist validaReq(RequestConsultDictamenSist req) throws BusinessException {
		if(!StringUtils.isValid(req.getNumNss())) {
			throw new BusinessException("El nss es necesario");
		}
		
		if(!StringUtils.isValid(req.getRefFolioOriginal())) {
			throw new BusinessException("El folio es necesario");
		}
		
		if(!StringUtils.isValid(req.getObjectIdOrigen().toString())) {
			throw new BusinessException("El object id origen es necesario");
		}
		return req;
	}
	
	private ResponseConsultDictamenSist getResponse(RequestConsultDictamenSist req, String usuario) throws BusinessException {
		RequestSistService request = new RequestSistService(req.getNumNss(), req.getRefFolioOriginal());
    	
		try {
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
			ResponseSistService resp = restTemplate.postForObject(getUrlConsultaDictamen(), request, ResponseSistService.class);
			
			BitacoraConsultaST3 bitacora = getBitacoraConsultaSt3(req);
			bitacora.setCodigoRespuesta(resp.getCodigo());
			bitacora.setNomUsuario(usuario);
			bitacora.setFecConsulta(new Date());
			bitacoraRepository.saveBitacora(bitacora);
			
			ResponseConsultDictamenSist respSist = new ResponseConsultDictamenSist();
			respSist.setCodigo(resp.getCodigo());
			respSist.setMensajeDictamen(resp.getMensaje());
			respSist.setMensaje(getMensajeDictamen());
			if(resp.getCodigo() == 0) {
				respSist.setMensaje(resp.getMensaje());
				respSist.setDictamen(resp.getJsonResultado().getCadenaArchivo());
				respSist.setNameArchivo(resp.getJsonResultado().getNombreArchivo());
			}
			
			return respSist;
		} catch (Exception e) {
			BitacoraConsultaST3 bitacora = getBitacoraConsultaSt3(req);
			bitacora.setCodigoRespuesta(Constants.DOSCIENTOS_TRES);
			bitacora.setNomUsuario(usuario);
			bitacora.setFecConsulta(new Date());
			bitacoraRepository.saveBitacora(bitacora);
			throw new BusinessException(getMensajeDictamenIntermitencia());
		}
	}
	
	private BitacoraConsultaST3 getBitacoraConsultaSt3(RequestConsultDictamenSist req) {
		BitacoraConsultaST3 bitacora = new BitacoraConsultaST3();
		bitacora.setNumNss(req.getNumNss());
		bitacora.setRefFolioOriginal(req.getRefFolioOriginal());
		bitacora.setObjectIdOrigen(req.getObjectIdOrigen());
		return bitacora;
	}
	
	private SimpleClientHttpRequestFactory getClientHttpRequestFactory() throws BusinessException 
	{
	    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
	    try {
			clientHttpRequestFactory.setConnectTimeout(getTimeOut());
		    clientHttpRequestFactory.setReadTimeout(getTimeOut());
		    return clientHttpRequestFactory;
		} catch (BusinessException e) {
			throw new BusinessException("Inconveniente al configurar timeout en el consumo servicio SIST");
		}
	}
	
	@Override
	@Cacheable(Constants.CACHE_MSG035)
	public String getMensajeDictamen() throws BusinessException {
		Optional<ParametroDTO> smsConsultaDictamenOk = parametroRepository.findOneByCve(Constants.PARAMETRO_MSG035);
    	if(!smsConsultaDictamenOk.isPresent()) {
			throw new BusinessException("El mensaje de folio no sea configurado (mensaje 035)");
    	}
    	return smsConsultaDictamenOk.get().getDesParametro();
	}
	
	@Override
	@Cacheable(Constants.CACHE_MSG034)
	public String getMensajeDictamenIntermitencia() throws BusinessException {
		Optional<ParametroDTO> smsConsultaDictamen = parametroRepository.findOneByCve(Constants.PARAMETRO_MSG034);
    	if(!smsConsultaDictamen.isPresent()) {
			throw new BusinessException("El mensaje de folio no sea configurado (mensaje 034)");
    	}
    	return smsConsultaDictamen.get().getDesParametro();
	}
	
	@Override
	@Cacheable(Constants.CACHE_URL_DICTAMEN)
	public String getUrlConsultaDictamen() throws BusinessException {
		Optional<ParametroDTO> urlConsultaDictamen = parametroRepository.findOneByCve(Constants.PARAMETRO_URL_CONSULTA_SIST);
    	if(!urlConsultaDictamen.isPresent()) {
			throw new BusinessException("La url de consulta no se encuentra configurada en el documento de parametros");
    	}
    	return urlConsultaDictamen.get().getDesParametro();
	}
	
	@Override
	@Cacheable(Constants.CACHE_TIME_OUT)
	public int getTimeOut() throws BusinessException {
		try {
			Optional<ParametroDTO> timeConsultaDictamen = parametroRepository.findOneByCve(Constants.PARAMETRO_TIME_OUT_SIST);
	    	if(!timeConsultaDictamen.isPresent()) {
				throw new BusinessException("El timeout no se encuentra configurada en el documento de parametros");
	    	}
	    	
	    	return Integer.parseInt(timeConsultaDictamen.get().getDesParametro());
		} catch (Exception e) {
			throw new BusinessException("Inconveniente al obtener timeout " + e.getMessage());
		}
	}
	
	@Override
	public String cleanCache(){
		cacheManager.getCache(Constants.CACHE_MSG035).clear();
		cacheManager.getCache(Constants.CACHE_MSG034).clear();
		cacheManager.getCache(Constants.CACHE_URL_DICTAMEN).clear();
		cacheManager.getCache(Constants.CACHE_TIME_OUT).clear();
		return Constants.EXITO;
	}
	
	@Override
	public String defaultCache() throws BusinessException {
		getMensajeDictamen();
		getMensajeDictamenIntermitencia();
		getUrlConsultaDictamen();
		getTimeOut();
		return Constants.EXITO;
	}
}
