package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ReporteCierreAnualRequestModel {

    private int ooad;
    private int subDelegacion;
    private int cicloActual;
    private boolean rfc;
    private boolean global;


    
}
