package mx.gob.imss.cit.pmc.mspmcreportes.model;

import lombok.Getter;
import lombok.Setter;

public class ModelVersion {

	public ModelVersion(String version, String folio, String nota) {
		this.setVersion_service(version);
		this.setFolios(folio);
		this.setNotas(nota);
	}
	
	@Getter
	@Setter
	private String version_service;

	@Getter
	@Setter
	private String folios;
	
	@Getter
	@Setter
	private String notas;
}
