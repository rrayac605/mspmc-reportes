package mx.gob.imss.cit.pmc.mspmcreportes.model;

import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;

public class RequestConsultDictamenSist {

	@Setter
	@Getter
	private String refFolioOriginal;
	
	@Setter
	@Getter
	private String numNss;
	
	@Getter
	@Setter
	private ObjectId objectIdOrigen;
}
