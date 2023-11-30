package mx.gob.imss.cit.pmc.mspmcreportes.model;

import lombok.Getter;
import lombok.Setter;

public class RequestCambiosDictamen {

	@Getter
	@Setter
	private String idObject;
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private String nss;
	
	@Getter
	@Setter
	private String nomArchivo;
	
	@Getter
	@Setter
	private String cveOrigenArchivo;
	
	@Getter
	@Setter
	private String sms;
}