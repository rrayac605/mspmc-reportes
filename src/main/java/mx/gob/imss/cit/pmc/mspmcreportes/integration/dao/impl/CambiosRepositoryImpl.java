package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import mx.gob.imss.cit.mspmccommons.dto.CambioDTO;
import mx.gob.imss.cit.mspmccommons.utils.ObjectUtils;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.CambiosRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.model.RequestCambiosDictamen;

@Repository
public class CambiosRepositoryImpl implements CambiosRepository {

	@Autowired
	private MongoOperations mongoOperations;
	
	@Override
	public boolean asociarDictamenCambios(RequestCambiosDictamen request) {
		Query query = new Query(Criteria.where("_id").is(request.getIdObject()));
		CambioDTO cambioDTO = mongoOperations.findOne(query, CambioDTO.class);
		if (ObjectUtils.existeValor(cambioDTO)) {
			cambioDTO.getBitacoraDictamen().get(0).setUbicacionArchivo(request.getUrl());
			cambioDTO.getBitacoraDictamen().get(0).setNomArchivo(request.getNomArchivo());
			mongoOperations.save(cambioDTO);
			return true;
		}
		return false;
	}

}
