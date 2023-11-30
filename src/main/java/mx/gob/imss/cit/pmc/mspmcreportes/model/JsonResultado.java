package mx.gob.imss.cit.pmc.mspmcreportes.model;

import lombok.Getter;
import lombok.Setter;

public class JsonResultado {

	@Getter
	@Setter
	private String contentType;
	
	@Getter
	@Setter
	private String nombreArchivo;
	
	@Getter
	@Setter
	private String cadenaArchivo;
}
