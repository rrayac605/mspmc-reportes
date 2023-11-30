package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import io.micrometer.core.instrument.util.StringUtils;
import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.mspmccommons.integration.model.ArchivoDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.CifrasControlDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleConsultaDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleRegistroDTO;
import mx.gob.imss.cit.mspmccommons.integration.model.DetalleSalidaOutput;
import mx.gob.imss.cit.mspmccommons.integration.model.MctArchivo;
import mx.gob.imss.cit.mspmccommons.integration.model.ParametroDTO;
import mx.gob.imss.cit.mspmccommons.utils.DateUtils;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.MsPmcCifrasControlRepository;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ParametroRepository;

@Repository
public class MsPmcCifrasControlRepositoryImpl implements MsPmcCifrasControlRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private ParametroRepository parametroRepository;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Page<DetalleSalidaOutput> getCifrasControl(MsPmcReportesInput input) throws BusinessException {
		List<DetalleSalidaOutput> detalleOutput = new ArrayList<DetalleSalidaOutput>();

		// Se calculan las fechas inicio y fin para la consulta
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		Criteria cFecProcesoCarga = null;

		if (fecProcesoIni != null && fecProcesoFin != null) {
			cFecProcesoCarga = new Criteria().andOperator(Criteria.where("fecProcesoCarga").gt(fecProcesoIni),
					Criteria.where("fecProcesoCarga").lte(fecProcesoFin));
		}
		logger.info("cveDelegacion recibida: " + input.getCveDelegation());

		Query query = new Query();
		if (cFecProcesoCarga != null) {
			query.addCriteria(cFecProcesoCarga);
		}
		query.addCriteria(Criteria.where("cveEstadoArchivo").is("2"));

		if (input.getCveDelegation() != null && input.getCveDelegation() > 0
				&& Integer.valueOf(input.getCveDelegation()) > 0 && input.getCveSubdelegation() != null
				&& input.getCveSubdelegation() > 0 && Integer.valueOf(input.getCveSubdelegation()) > 0) {
			Criteria delAsegurado = Criteria.where("detalleRegistroDTO.aseguradoDTO.cveDelegacionNss")
					.is(input.getCveDelegation());
			Criteria delPatron = Criteria.where("detalleRegistroDTO.patronDTO.cveDelRegPatronal")
					.is(input.getCveDelegation());

			Criteria subdelAsegurado = Criteria.where("detalleRegistroDTO.patronDTO.cveSubDelRegPatronal")
					.is(input.getCveDelegation());
			Criteria subdelPatron = Criteria.where("detalleRegistroDTO.aseguradoDTO.cveSubdelNss")
					.is(input.getCveSubdelegation());

			query.addCriteria(new Criteria().andOperator(
					new Criteria().orOperator(new Criteria().orOperator(delAsegurado, delPatron)),
					new Criteria().andOperator(new Criteria().orOperator(subdelAsegurado, subdelPatron))));

		} else {
			if ((input.getCveDelegation() != null && input.getCveDelegation() > 0
					&& Integer.valueOf(input.getCveDelegation()) > 0)
					&& (input.getCveSubdelegation() == null || input.getCveSubdelegation() > 0
							|| Integer.valueOf(input.getCveSubdelegation()) == 0)) {
				Criteria delAsegurado = Criteria.where("detalleRegistroDTO.aseguradoDTO.cveDelegacionNss")
						.is(input.getCveDelegation());
				Criteria delPatron = Criteria.where("detalleRegistroDTO.patronDTO.cveDelRegPatronal")
						.is(input.getCveDelegation());
				query.addCriteria(new Criteria().orOperator(new Criteria().orOperator(delAsegurado, delPatron)));
			}
		}

		if (StringUtils.isNotBlank(input.getCveTipoArchivo()) && StringUtils.isNotBlank(input.getCveTipoArchivo())) {
			query.addCriteria(Criteria.where("cveOrigenArchivo").is(input.getCveTipoArchivo()));
		}

		logger.info("cveDelegacion recibida: " + input.getCveDelegation());
		logger.info(query.toString());

		Long count = mongoOperations.count(query, MctArchivo.class);

		Optional<ParametroDTO> elementsPaginator = parametroRepository.findOneByCve("elementsPaginator");
		Pageable pageable = null;
		if (input.getPage() == null) {
			pageable = PageRequest.of(0, count.intValue() > 0 ? count.intValue() : 1);
		} else {

			pageable = PageRequest.of(input.getPage(),
					Integer.valueOf(elementsPaginator != null ? elementsPaginator.get().getDesParametro() : "0"));

		}

		List<ArchivoDTO> listArchivos = mongoOperations.find(query.with(pageable), ArchivoDTO.class);
		Page<DetalleSalidaOutput> resultPage = null;

		resultPage = llenaDatosCifras(pageable, detalleOutput, count, listArchivos);

		return resultPage;
	}

	private Page<DetalleSalidaOutput> llenaDatosCifras(Pageable pageable, List<DetalleSalidaOutput> detalleOutput,
			long count, List<ArchivoDTO> listArchivos) {
		Page<DetalleSalidaOutput> resultPage = null;
		if (listArchivos != null && !listArchivos.isEmpty()) {
			List<DetalleConsultaDTO> detalleConsultaDTO = new ArrayList<DetalleConsultaDTO>();
			DetalleSalidaOutput detalleSalidaOutput = new DetalleSalidaOutput();
			CifrasControlDTO cifrasControlDTO = new CifrasControlDTO();
			// ************** CONTADORES GENERALES ******************************
			int contCorrecG = 0;
			int contErrorG = 0;
			int contDupG = 0;
			int contSusG = 0;
			int contCorrecOtrasG = 0;
			int contErrorOtrasG = 0;
			int contDupOtrasG = 0;
			int contSusOtrasG = 0;
			int contBajaG = 0;
			int contBajaOtrasG = 0;

			int contTotaG = 0;

			for (int i = 0; i < listArchivos.size(); i++) {
				ArchivoDTO archivo = (ArchivoDTO) listArchivos.get(i);

				DetalleConsultaDTO consultaDTO = new DetalleConsultaDTO();
				consultaDTO.setTipoArchivo(archivo.getCveOrigenArchivo());

				int contCorrec = 0;
				int contError = 0;
				int contDup = 0;
				int contSus = 0;
				int contCorrecOtras = 0;
				int contErrorOtras = 0;
				int contDupOtras = 0;
				int contSusOtras = 0;
				int contBaja = 0;
				int contBajaOtras = 0;
				int cotDetalle = 0;

				for (int j = 0; j < archivo.getDetalleRegistroDTO().size(); j++) {
					DetalleRegistroDTO detalle = (DetalleRegistroDTO) archivo.getDetalleRegistroDTO().get(j);

					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 1) {
						contCorrecG++;
						cotDetalle++;
						if (contCorrec == 0) {
							consultaDTO.setNumRegistrosCorrectos(new Long(1));
							contCorrec++;
						} else {
							contCorrec = contCorrec + 1;
							consultaDTO.setNumRegistrosCorrectos(new Long(contCorrec));

						}
					} else {
						if (contCorrec == 0) {
							consultaDTO.setNumRegistrosCorrectos(new Long(0));
						}

					}
					// ***** EstadoRegistro = 2 ERRORES *************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 2) {
						contErrorG++;
						cotDetalle++;
						if (contError == 0) {
							consultaDTO.setNumRegistrosError(new Long(1));
							contError++;
						} else {
							contError = contError + 1;
							consultaDTO.setNumRegistrosError(new Long(contError));

						}

					} else {
						if (contError == 0) {
							consultaDTO.setNumRegistrosError(new Long(0));
						}
					}

					// ***** EstadoRegistro = 3 DUPLICADOS **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 3) {
						contDupG++;
						cotDetalle++;
						if (contDup == 0) {
							consultaDTO.setNumRegistrosDup(new Long(1));
							contDup++;
						} else {
							contDup = contDup++;
							consultaDTO.setNumRegistrosDup(new Long(contDup));

						}

					} else {
						if (contDup == 0) {
							consultaDTO.setNumRegistrosDup(new Long(0));
						}

					}

					// ***** EstadoRegistro = 4 SUCEPTIBLES **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 4) {
						contSusG++;
						cotDetalle++;
						if (contSus == 0) {

							consultaDTO.setNumRegistrosSus(new Long(1));
							contSus++;
						} else {
							contSus = contSus + 1;
							consultaDTO.setNumRegistrosSus(new Long(contSus));

						}

					} else {
						if (contSus == 0) {
							consultaDTO.setNumRegistrosSus(new Long(0));
						}

					}
					// ***** EstadoRegistro = 5 Correctos otras delegaciones **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 5) {
						contCorrecOtrasG++;
						cotDetalle++;
						if (contCorrecOtras == 0) {

							consultaDTO.setNumRegistrosCorrectosOtras(new Long(1));
							contCorrecOtras++;
						} else {
							contCorrecOtras = contCorrecOtras + 1;
							consultaDTO.setNumRegistrosCorrectosOtras(new Long(contCorrecOtras));
						}

					} else {
						if (contCorrecOtras == 0) {
							consultaDTO.setNumRegistrosCorrectosOtras(new Long(0));
						}

					}

					// ***** EstadoRegistro = 6 Erróneos otras delegaciones **********************

					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 6) {
						contErrorOtrasG++;
						cotDetalle++;
						if (contErrorOtras == 0) {

							consultaDTO.setNumRegistrosErrorOtras(new Long(1));
							contErrorOtras++;
						} else {
							contErrorOtras = contErrorOtras + 1;
							consultaDTO.setNumRegistrosErrorOtras(new Long(contErrorOtras));
						}

					} else {
						if (contErrorOtras == 0) {
							consultaDTO.setNumRegistrosErrorOtras(new Long(0));
						}

					}

					// ***** EstadoRegistro = 7 Duplicados otras delegaciones **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 7) {
						contDupOtrasG++;
						cotDetalle++;
						if (contDupOtras == 0) {
							consultaDTO.setNumRegistrosDupOtras(new Long(1));
							contDupOtras++;
						} else {
							contDupOtras = contDupOtras + 1;
							consultaDTO.setNumRegistrosDupOtras(new Long(contDupOtras));
						}

					} else {
						if (contDupOtras == 0) {
							consultaDTO.setNumRegistrosDupOtras(new Long(0));
						}

					}
					// ***** EstadoRegistro = 8 Susceptibles de ajuste otras
					// delegaciones**********************

					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 8) {
						contSusOtrasG++;
						cotDetalle++;
						if (contSusOtras == 0) {
							consultaDTO.setNumRegistrosSusOtras(new Long(1));
							contSusOtras++;
						} else {
							contSusOtras = contSusOtras + 1;
							consultaDTO.setNumRegistrosSusOtras(new Long(contSusOtras));
						}

					} else {
						if (contSusOtras == 0) {
							consultaDTO.setNumRegistrosSusOtras(new Long(0));
						}

					}

					// ***** EstadoRegistro = 9 Duplicados otras delegaciones **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 9) {
						contDupOtrasG++;
						cotDetalle++;
						if (contDupOtras == 0) {
							consultaDTO.setNumRegistrosDupOtras(new Long(1));
							contDupOtras++;
						} else {
							contDupOtras = contDupOtras + 1;
							consultaDTO.setNumRegistrosDupOtras(new Long(contDupOtras));
						}

					} else {
						if (contDupOtras == 0) {
							consultaDTO.setNumRegistrosDupOtras(new Long(0));
						}

					}

					// ***** EstadoRegistro = 10 Bajas **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 10) {
						contBajaG++;
						cotDetalle++;
						if (contBaja == 0) {
							consultaDTO.setNumRegistrosBaja(new Long(1));
							contBaja++;
						} else {
							contBaja = contBaja + 1;
							consultaDTO.setNumRegistrosBaja(new Long(contBaja));
						}

					} else {
						if (contBaja == 0) {
							consultaDTO.setNumRegistrosBaja(new Long(0));
						}

					}

					// ***** EstadoRegistro = 11 Baja otras delegaciones **********************
					if (detalle.getAseguradoDTO().getCveEstadoRegistro() == 10) {
						contBajaOtrasG++;
						cotDetalle++;
						if (contBajaOtras == 0) {
							consultaDTO.setNumRegistrosBajaOtras(new Long(1));
							contBajaOtras++;
						} else {
							contBajaOtras = contBajaOtras + 1;
							consultaDTO.setNumRegistrosBajaOtras(new Long(contBajaOtras));

						}

					} else {

						consultaDTO.setNumRegistrosBajaOtras(new Long(0));
					}

				}

				consultaDTO.setNumTotalRegistros(new Long(cotDetalle));

				contTotaG = contTotaG + cotDetalle;

				detalleConsultaDTO.add(consultaDTO);

			}
			cifrasControlDTO.setNumTotalRegistros(new Long(contTotaG));
			cifrasControlDTO.setNumRegistrosBaja(new Long(contBajaG));
			cifrasControlDTO.setNumRegistrosBajaOtras(new Long(contBajaOtrasG));
			cifrasControlDTO.setNumRegistrosCorrectos(new Long(contCorrecG));
			cifrasControlDTO.setNumRegistrosCorrectosOtras(new Long(contCorrecOtrasG));
			cifrasControlDTO.setNumRegistrosDup(new Long(contDupG));
			cifrasControlDTO.setNumRegistrosDupOtras(new Long(contDupOtrasG));
			cifrasControlDTO.setNumRegistrosError(new Long(contErrorG));
			cifrasControlDTO.setNumRegistrosErrorOtras(new Long(contErrorOtrasG));
			cifrasControlDTO.setNumRegistrosSus(new Long(contSusG));
			cifrasControlDTO.setNumRegistrosSusOtras(new Long(contSusOtrasG));

			detalleSalidaOutput.setDetalleConsultaDTO(detalleConsultaDTO);
			detalleSalidaOutput.setCifrasControlTotales(cifrasControlDTO);
			detalleOutput.add(detalleSalidaOutput);
		}
		resultPage = new PageImpl<DetalleSalidaOutput>(detalleOutput, pageable, count);
		return resultPage;
	}

}
