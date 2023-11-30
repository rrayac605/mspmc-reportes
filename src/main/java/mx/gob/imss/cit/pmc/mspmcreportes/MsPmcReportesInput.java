package mx.gob.imss.cit.pmc.mspmcreportes;

import lombok.Data;

@Data
public class MsPmcReportesInput {

	private String fromMonth;

	private String fromYear;

	private String toMonth;

	private String toYear;

	private String cveTipoArchivo;

	private Integer cveDelegation;

	private Integer cveSubdelegation;

	private Boolean delRegPat;

	private Boolean isPdfReport;

	private Integer page;
	
	private String desSubdelegation;
	
	private String desDelegation;
	
	private Boolean isDelegacional;

}
