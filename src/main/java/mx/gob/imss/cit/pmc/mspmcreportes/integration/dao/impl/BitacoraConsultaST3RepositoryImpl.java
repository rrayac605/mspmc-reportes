package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import mx.gob.imss.cit.mspmccommons.dto.BitacoraConsultaST3;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.BitacoraConsultaST3Repository;

@Repository
public class BitacoraConsultaST3RepositoryImpl implements BitacoraConsultaST3Repository {

	@Autowired
	private MongoOperations mongoOperations;
	
	@Override
	public void saveBitacora(BitacoraConsultaST3 bitacora) {
		mongoOperations.save(bitacora);
	}

}
