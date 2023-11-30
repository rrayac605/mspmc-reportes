package mx.gob.imss.cit.pmc.mspmcreportes.model;

import lombok.Getter;
import lombok.Setter;

public class ResponseConsultDictamenSist {

	public ResponseConsultDictamenSist() {
		
	}
	
	public ResponseConsultDictamenSist(int codigo, String strDictamen) {
		this.setCodigo(codigo);
		this.setDictamen(strDictamen);
	}
	
	@Getter
	@Setter
	private int codigo;
	
	@Getter
	@Setter
	private String dictamen;
	
	@Getter
	@Setter	
	private String mensaje;
	
	@Getter
	@Setter	
	private String nameArchivo;
	
	@Getter
	@Setter	
	private String mensajeDictamen;
}
