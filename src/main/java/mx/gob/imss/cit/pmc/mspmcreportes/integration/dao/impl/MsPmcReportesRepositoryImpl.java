package mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.impl;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.*;

import mx.gob.imss.cit.mspmccommons.enums.*;
import mx.gob.imss.cit.mspmccommons.integration.model.*;
import mx.gob.imss.cit.mspmccommons.utils.*;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import mx.gob.imss.cit.mspmccommons.exception.BusinessException;
import mx.gob.imss.cit.pmc.mspmcreportes.MsPmcReportesInput;

@Repository
public class MsPmcReportesRepositoryImpl implements MsPmcReportesRepository {
	@Autowired
	private MongoOperations mongoOperations;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public ReporteTipoRiesgoResponseDTO getConteo(MsPmcReportesInput input) {
		Aggregation aggregation = buildAggregationTipoRiesgo(input);
		logger.info(aggregation.toString());
		AggregationResults<ReporteTipoRiesgoResponseDTO> aggregationResults = mongoOperations.aggregate(aggregation,
				DetalleRegistroDTO.class, ReporteTipoRiesgoResponseDTO.class);
		return aggregationResults.getUniqueMappedResult();
	}

	@Override
	public ReporteTipoRiesgoResponseDTO getAnteriorNuevo(MsPmcReportesInput input) {
		Aggregation aggregation = buildAggregationTipoRiesgoAnteriorNuevo(input);
		logger.info(aggregation.toString());
		AggregationResults<ReporteTipoRiesgoResponseDTO> aggregationResults = mongoOperations.aggregate(aggregation,
				DetalleRegistroDTO.class, ReporteTipoRiesgoResponseDTO.class);
		return aggregationResults.getUniqueMappedResult();
	}

	@Override
	public ReporteCasuisticaConsecuenciaResponseDTO getConteo(MsPmcReportesInput input, List<ConsecuenciaDTO> consecuenciasList) {
		Aggregation aggregation = buildAggregationCasuisticaConsecuencia(input, consecuenciasList);
		logger.info(aggregation.toString());
		AggregationResults<ReporteCasuisticaConsecuenciaResponseDTO> aggregationResults = mongoOperations.aggregate(aggregation,
				DetalleRegistroDTO.class, ReporteCasuisticaConsecuenciaResponseDTO.class);
		return aggregationResults.getUniqueMappedResult();
	}

	public List<ReporteCodigoErrorResponseDTO> getConteoCodigoError(MsPmcReportesInput input) {
		Aggregation aggregation = buildAggregationCodigoError(input);
		logger.info(aggregation.toString());
		AggregationResults<ReporteCodigoErrorResponseDTO> aggregationResults = mongoOperations.aggregate(aggregation,
				DetalleRegistroDTO.class, ReporteCodigoErrorResponseDTO.class);
		return aggregationResults.getMappedResults();
	}

	private List<AggregationOperation> getAggregationList(MsPmcReportesInput input) {
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		Criteria cFecProceso = null;

		if (fecProcesoIni != null && fecProcesoFin != null) {
			cFecProceso = new Criteria().andOperator(Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
							.gt(fecProcesoIni), Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
							.lte(fecProcesoFin));
		}

		Criteria cDel = null;
		Criteria cDelAndSubDel = null;

		if (input.getCveDelegation() != null && input.getCveDelegation() > 0 && input.getCveSubdelegation() != null
				&& input.getCveSubdelegation() > 0) {
			Criteria delAsegurado = Criteria.where(CamposAseguradoEnum.DELEGACION_NSS.getNombreCampo())
					.is(input.getCveDelegation());
			Criteria delPatron = Criteria.where(CamposPatronEnum.DELEGACION.getNombreCampo())
					.is(input.getCveDelegation());

			Criteria subdelAsegurado = Criteria.where(CamposAseguradoEnum.SUBDELEGACION_NSS.getNombreCampo())
					.is(input.getCveSubdelegation());
			Criteria subdelPatron = Criteria.where(CamposPatronEnum.SUBDELEGACION.getNombreCampo())
					.is(input.getCveSubdelegation());

			cDelAndSubDel = new Criteria().orOperator(new Criteria().andOperator(delAsegurado, subdelAsegurado),
					new Criteria().andOperator(delPatron, subdelPatron));

		} else if ((input.getCveDelegation() != null && input.getCveDelegation() > 0)
					&& (input.getCveSubdelegation() == null || input.getCveSubdelegation() == 0)) {
			Criteria delAsegurado = Criteria.where(CamposAseguradoEnum.DELEGACION_NSS.getNombreCampo())
					.is(input.getCveDelegation());
			Criteria delPatron = Criteria.where(CamposPatronEnum.DELEGACION.getNombreCampo())
					.is(input.getCveDelegation());
			cDel = new Criteria().orOperator(delAsegurado, delPatron);
		}
		List<AggregationOperation> aggregationOperations = Arrays.asList(
				AggregationUtils.validateMatchOp(cFecProceso),
				AggregationUtils.validateMatchOp(cDel),
				AggregationUtils.validateMatchOp(cDelAndSubDel)
		);
		aggregationOperations = aggregationOperations.stream().filter(Objects::nonNull).collect(Collectors.toList());
		return aggregationOperations;
	}

	private Aggregation buildAggregationTipoRiesgo(MsPmcReportesInput input) {
		List<AggregationOperation> aggregationOperations = getAggregationList(input);
		Integer ciclo = Calendar.getInstance().get(Calendar.YEAR);
		MatchOperation actual = AggregationUtils.validateMatchOp(Criteria.where("aseguradoDTO.numCicloAnual")
				.is(String.valueOf(ciclo)));
		MatchOperation inmediatoAnterior = AggregationUtils.validateMatchOp(Criteria.where("aseguradoDTO.numCicloAnual")
				.is(String.valueOf(ciclo - 1)));
		MatchOperation anterior = AggregationUtils.validateMatchOp(Criteria.where("aseguradoDTO.numCicloAnual")
				.lt(String.valueOf(ciclo - 1)));
		MatchOperation posterior = AggregationUtils.validateMatchOp(Criteria.where("aseguradoDTO.numCicloAnual")
				.gt(String.valueOf(ciclo)));
		MatchOperation consecuencia = AggregationUtils.validateMatchOp(new Criteria().orOperator(
				Criteria.where("incapacidadDTO.cveConsecuencia").is(4),
				Criteria.where("incapacidadDTO.cveConsecuencia").is("4")));
		String tipoRiesgoJson = "{ $addFields: { cveTipoRiesgo: { $toInt: '$incapacidadDTO.cveTipoRiesgo' } } }";
		CustomAggregationOperation addFieldsTipoRiesgo = new CustomAggregationOperation(tipoRiesgoJson);
		String porcentajeIncapacidadJson = "{ $addFields: { porPorcentajeIncapacidad: { $toInt: '$incapacidadDTO.porPorcentajeIncapacidad' } } }";
		CustomAggregationOperation addFieldsPorcentajeINcapacidad = new CustomAggregationOperation(porcentajeIncapacidadJson);
		String groupJson = "{ $group: { _id: '$cveTipoRiesgo', conteo: {$sum: 1} } }";
		CustomAggregationOperation groupCount = new CustomAggregationOperation(groupJson);
		GroupOperation groupDiasSubsidiados = Aggregation.group("cveTipoRiesgo")
				.sum("incapacidadDTO.numDiasSubsidiados").as("conteo");
		GroupOperation groupPorcentajeIncapacidad = Aggregation.group("cveTipoRiesgo")
				.sum("porPorcentajeIncapacidad").as("conteo");

		FacetOperation facet = Aggregation.facet()
				.and(actual, addFieldsTipoRiesgo, groupCount).as("rActual")
				.and(inmediatoAnterior, addFieldsTipoRiesgo, groupCount).as("rInmediatoAnterior")
				.and(anterior, addFieldsTipoRiesgo, groupCount).as("rAnterior")
				.and(posterior, addFieldsTipoRiesgo, groupCount).as("rPosterior")
				.and(actual, addFieldsTipoRiesgo, groupDiasSubsidiados).as("dsActual")
				.and(inmediatoAnterior, addFieldsTipoRiesgo, groupDiasSubsidiados).as("dsInmediatoAnterior")
				.and(anterior, addFieldsTipoRiesgo, groupDiasSubsidiados).as("dsAnterior")
				.and(posterior, addFieldsTipoRiesgo, groupDiasSubsidiados).as("dsPosterior")
				.and(actual, addFieldsTipoRiesgo, addFieldsPorcentajeINcapacidad, groupPorcentajeIncapacidad).as("piActual")
				.and(inmediatoAnterior, addFieldsTipoRiesgo, addFieldsPorcentajeINcapacidad, groupPorcentajeIncapacidad).as("piInmediatoAnterior")
				.and(anterior, addFieldsTipoRiesgo, addFieldsPorcentajeINcapacidad, groupPorcentajeIncapacidad).as("piAnterior")
				.and(posterior, addFieldsTipoRiesgo, addFieldsPorcentajeINcapacidad, groupPorcentajeIncapacidad).as("piPosterior")
				.and(actual, consecuencia, addFieldsTipoRiesgo, groupCount).as("dActual")
				.and(inmediatoAnterior, consecuencia, addFieldsTipoRiesgo, groupCount).as("dInmediatoAnterior")
				.and(anterior, consecuencia, addFieldsTipoRiesgo, groupCount).as("dAnterior")
				.and(posterior, consecuencia, addFieldsTipoRiesgo, groupCount).as("dPosterior");
		aggregationOperations.add(facet);
		return Aggregation.newAggregation(ReporteTipoRiesgoResponseDTO.class, aggregationOperations);
	}

	private Aggregation buildAggregationTipoRiesgoAnteriorNuevo(MsPmcReportesInput input) {
		List<AggregationOperation> aggregationOperations = getAggregationList(input);
		MatchOperation matchCorrects = Aggregation.match(Criteria.where("aseguradoDTO.cveEstadoRegistro").in(1, 5));
		//Correctos actual
		CustomAggregationOperation conteoActual = buildCount("conteoCorrectos");
		CustomAggregationOperation groupNuevo = buildCountGroup(55, "conteoCorrectos");
		//Correctos anterior
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecProcesoFin);
		calendar.add(Calendar.DATE, - 5);
		calendar.add(Calendar.MONTH, - 1);
		calendar.add(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE) - calendar.get(Calendar.DATE));
		fecProcesoFin = calendar.getTime();
		Criteria cFecProceso = null;
		cFecProceso = new Criteria().andOperator(Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
				.gt(fecProcesoIni), Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
				.lte(fecProcesoFin));
		MatchOperation fechas = Aggregation.match(cFecProceso);
		CustomAggregationOperation conteoAnterior = buildCount("conteoCorrectos");
		CustomAggregationOperation grupoAnterior = buildCountGroup(50, "conteoCorrectos");
		FacetOperation facet = Aggregation.facet()
				.and(matchCorrects, conteoActual, groupNuevo).as("conteoCorrectosActual")
				.and(fechas ,matchCorrects, conteoAnterior, grupoAnterior).as("conteoCorrectosAnterior");
		aggregationOperations.add(facet);
		return Aggregation.newAggregation(ReporteTipoRiesgoResponseDTO.class, aggregationOperations);
	}

	private CustomAggregationOperation buildCount(String fieldName) {
		String countJSON = "{ $count: '" + fieldName + "' }";
		return new CustomAggregationOperation(countJSON);
	}

	private CustomAggregationOperation buildCountGroup(Integer id, String countField) {
		String groupActual = "{ $group: { _id: '" + id + "', conteo: { $first: '$" + countField + "' } } }";
		return new CustomAggregationOperation(groupActual);
	}

	private Aggregation buildAggregationCasuisticaConsecuencia(MsPmcReportesInput input, List<ConsecuenciaDTO> consecuenciasList) {
		List<AggregationOperation> aggregationOperations = getAggregationList(input);
		String addFieldsTipoRiesgoJson = buildAddFieldsToInt("incapacidadDTO.cveTipoRiesgo", "cveTipoRiesgo");
		CustomAggregationOperation addFieldsTipoRiesgo = new CustomAggregationOperation(addFieldsTipoRiesgoJson);
		String addFieldsPorcentajeIncapacidadJson = buildAddFieldsToInt("incapacidadDTO.porPorcentajeIncapacidad", "porPorcentajeIncapacidad");
		CustomAggregationOperation addFieldsPorcentajeIncapacidad = new CustomAggregationOperation(addFieldsPorcentajeIncapacidadJson);
		String addFieldsConsecuenciaJson = buildAddFieldsToInt("incapacidadDTO.cveConsecuencia", "cveConsecuencia");
		CustomAggregationOperation addFieldsConsecuencia = new CustomAggregationOperation(addFieldsConsecuenciaJson);
		String addFieldsDiasSubsidiadosJson = buildAddFieldsToInt("incapacidadDTO.numDiasSubsidiados", "numDiasSubsidiados");
		CustomAggregationOperation addFieldsDiasSubsidiados = new CustomAggregationOperation(addFieldsDiasSubsidiadosJson);
		String groupSum1Json = buildGroupString("1", consecuenciasList);
		CustomAggregationOperation groupSum1 = new CustomAggregationOperation(groupSum1Json);
		String groupSumDiasSubsidiadosJson = buildGroupString("'$numDiasSubsidiados'", consecuenciasList);
		CustomAggregationOperation grouoSumDiasSubsidiados = new CustomAggregationOperation(groupSumDiasSubsidiadosJson);
		String groupSumPorcentajeIncapacidadJson = buildGroupString("'$porPorcentajeIncapacidad'", consecuenciasList);
		CustomAggregationOperation groupSumPorcentajeIncapacidad = new CustomAggregationOperation(groupSumPorcentajeIncapacidadJson);
		FacetOperation facet = Aggregation.facet()
				.and(groupSum1).as("registros")
				.and(addFieldsDiasSubsidiados, grouoSumDiasSubsidiados).as("diasSubsidiados")
				.and(addFieldsPorcentajeIncapacidad, groupSumPorcentajeIncapacidad).as("porcentajeIncapacidad");
		aggregationOperations.add(addFieldsTipoRiesgo);
		aggregationOperations.add(addFieldsConsecuencia);
		aggregationOperations.add(facet);
		return Aggregation.newAggregation(ReporteCasuisticaConsecuenciaResponseDTO.class, aggregationOperations);
	}

	private Aggregation buildAggregationCodigoError(MsPmcReportesInput input) {
		List<AggregationOperation> aggregationOperations = getAggregationList(input);
		//Erroneos
		MatchOperation bitacoraErrores = Aggregation.match(Criteria.where("bitacoraErroresDTO").ne(null));
		UnwindOperation unwind = Aggregation.unwind("bitacoraErroresDTO");
		String groupJson = builGroupString();
		CustomAggregationOperation group = new CustomAggregationOperation(groupJson);
		SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "objectIdArchivoDetalle");
		//Correctos actual
		MatchOperation correctosActual = Aggregation.match(Criteria.where("aseguradoDTO.cveEstadoRegistro").in(1, 5));
		String conteoCorrectos = "{ $count: 'conteoCorrectos' }";
		CustomAggregationOperation conteoActual = new CustomAggregationOperation(conteoCorrectos);
		String groupActual = "{ $group: { _id: 'conteoNuevos', conteo: { $first: '$conteoCorrectos' } } }";
		CustomAggregationOperation groupNuevo = new CustomAggregationOperation(groupActual);
		//Correctos anterior
		Date fecProcesoIni = DateUtils.calculateBeginDate(input.getFromYear(), input.getFromMonth(), null);
		Date fecProcesoFin = DateUtils.calculateEndDate(input.getToYear(), input.getToMonth(), null);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecProcesoFin);
		calendar.add(Calendar.DATE, - 5);
		calendar.add(Calendar.MONTH, - 1);
		calendar.add(Calendar.DATE, calendar.getActualMaximum(calendar.DATE) - calendar.get(Calendar.DATE));
		fecProcesoFin = calendar.getTime();
		Criteria cFecProceso = null;
		cFecProceso = new Criteria().andOperator(Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
						.gt(fecProcesoIni), Criteria.where(CamposAseguradoEnum.FECHA_ALTA.getNombreCampo())
						.lte(fecProcesoFin));
		MatchOperation fechas = Aggregation.match(cFecProceso);
		MatchOperation correctosAnterior = Aggregation.match(Criteria.where("aseguradoDTO.cveEstadoRegistro").in(1, 5));
		String conteoCorrectosAnterior = "{ $count: 'conteoCorrectos' }";
		CustomAggregationOperation conteoAnterior = new CustomAggregationOperation(conteoCorrectosAnterior);
		String groupAnterior = "{ $group: { _id: 'conteoAnterior', conteo: { $first: '$conteoCorrectos' } } }";
		CustomAggregationOperation grupoAnterior = new CustomAggregationOperation(groupAnterior);
		FacetOperation facet = Aggregation.facet()
				.and(bitacoraErrores, unwind, group, sort).as("respuestaErroneos")
				.and(correctosActual, conteoActual, groupNuevo).as("conteoCorrectosActual")
				.and(fechas, correctosAnterior, conteoAnterior, grupoAnterior).as("conteoCorrectosAnterior");
		String project = "{ $project: { respuesta: { $concatArrays: [ '$respuestaErroneos', '$conteoCorrectosActual', '$conteoCorrectosAnterior'] } } },";
		CustomAggregationOperation proyeccion = new CustomAggregationOperation(project);
		String unwindRespuesta = "{ $unwind: { path: '$respuesta' } },";
		CustomAggregationOperation respuesta = new CustomAggregationOperation(unwindRespuesta);
		String root = "{ $replaceRoot: { newRoot: \"$respuesta\" } }";
		CustomAggregationOperation replaceRoot = new CustomAggregationOperation(root);
		aggregationOperations.add(facet);
		aggregationOperations.add(proyeccion);
		aggregationOperations.add(respuesta);
		aggregationOperations.add(replaceRoot);
		return Aggregation.newAggregation(ReporteCodigoErrorResponseDTO.class, aggregationOperations);
	}

	private String buildGroupString(String sumField, List<ConsecuenciaDTO> consecuenciasList) {
		String group = "{ $group: { _id: '$cveTipoRiesgo',";
		int index = 0;
		for (ConsecuenciaDTO consecuencia : consecuenciasList) {
			group = group.concat(" conteo")
					.concat(String.valueOf(index))
					.concat(": { $sum: { $cond: [ { $eq: ['$cveConsecuencia', ")
					.concat(String.valueOf(consecuencia.getCveIdConsecuencia()))
					.concat("] }, { $sum: ")
					.concat(sumField)
					.concat(" }, { $sum: 0 } ] }  },");
			logger.info(group);
			index++;
		}
		group = group.concat(" } }");
		logger.info(group);
		return group;
	}

	private String builGroupString() {
		List<String> cveArchivoList = Arrays.asList("RTT", "ST3", "ST5", "AJU", "COD", "ROD");
		String group = "{ $group: { _id: '$bitacoraErroresDTO.cveIdCodigoError', desError: { $first: '$bitacoraErroresDTO.desCampo'},";
		for(String cveArchivo : cveArchivoList) {
			group = group.concat(cveArchivo)
					.concat("_D")
					.concat(": { $sum: { $cond: [{ $and: [{$eq: ['$cveOrigenArchivo', '")
					.concat(cveArchivo)
					.concat("']}, {$in: ['$aseguradoDTO.cveEstadoRegistro', [1,2,3,4, 10]]}] }, { $sum: 1 }, { $sum: 0 }] } },")
					.concat(cveArchivo)
					.concat("_O")
					.concat(": { $sum: { $cond: [{ $and: [{$eq: ['$cveOrigenArchivo', '")
					.concat(cveArchivo)
					.concat("']}, {$in: ['$aseguradoDTO.cveEstadoRegistro', [5,6,7,8, 11]]}] }, { $sum: 1 }, { $sum: 0 }] } },");
		}
		group = group.concat("} }");
		logger.info(group);
		return group;
	}

	private String buildAddFieldsToInt(String field, String as) {
		String addFields = "{ $addFields: { ";
		return addFields.concat(as)
				.concat(": { $toInt: '$")
				.concat(field)
				.concat("' } } }");
	}

}
