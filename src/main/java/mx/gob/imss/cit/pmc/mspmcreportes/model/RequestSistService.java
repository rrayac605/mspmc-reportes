package mx.gob.imss.cit.pmc.mspmcreportes.model;

import lombok.Getter;
import lombok.Setter;

public class RequestSistService {

	public RequestSistService() {}

	public RequestSistService(String numNss, String folioOriginal) {
		this.setNss(numNss);
		this.setFolio(folioOriginal);
	}
	
	@Getter
	@Setter
	private String nss;
	
	@Getter
	@Setter
	private String folio;
	
}
