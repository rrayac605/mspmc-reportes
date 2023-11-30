package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao;

import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestCambiosDictamen;

public interface CambiosRepository {

	boolean asociarDictamenCambios(RequestCambiosDictamen req);
}
