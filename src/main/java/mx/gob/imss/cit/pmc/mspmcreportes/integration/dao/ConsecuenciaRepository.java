package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao;

import java.util.List;
import java.util.Optional;

import mx.gob.imss.cit.mspmccommons.integration.model.ConsecuenciaDTO;

public interface ConsecuenciaRepository {

	Optional<ConsecuenciaDTO> findOneByCve(String cveConsecuencia);

	Optional<List<ConsecuenciaDTO>> findAll();

}
