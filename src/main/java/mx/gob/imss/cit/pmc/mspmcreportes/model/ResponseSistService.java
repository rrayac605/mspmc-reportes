package mx.gob.imss.cit.pmc.mspmcreportes.model;


import lombok.Getter;
import lombok.Setter;

public class ResponseSistService {

	public ResponseSistService() {
		
	}
	
	@Getter
	@Setter
	private int codigo;
	
	@Getter
	@Setter
	private String mensaje;

	@Getter
	@Setter
	private JsonResultado jsonResultado;

	@Getter
	@Setter
	private String salt;
	
}
