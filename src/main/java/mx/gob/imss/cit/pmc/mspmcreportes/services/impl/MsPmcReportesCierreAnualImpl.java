package mx.gob.imss.cit.pmc.mspmcreportes.services.impl;

import lombok.extern.log4j.Log4j2;


import mx.gob.imss.cit.pmc.mspmcreportes.constants.ReportesConstants;
import mx.gob.imss.cit.pmc.mspmcreportes.dto.DelegacionOOADDTO;
import mx.gob.imss.cit.pmc.mspmcreportes.exception.DownloadException;
import mx.gob.imss.cit.pmc.mspmcreportes.integration.dao.ReporteCierreAnualRequestModel;
import mx.gob.imss.cit.pmc.mspmcreportes.services.FtpClientService;
import mx.gob.imss.cit.pmc.mspmcreportes.services.MsPmcReportesCierreAnual;

import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MsPmcReportesCierreAnualImpl implements MsPmcReportesCierreAnual {




    @Autowired
    private FtpClientService ftpClientService;

    @Value("${fileReportes}")
    private String path;

    @Override
    public InputStreamResource obtenerReporte(ReporteCierreAnualRequestModel request) throws IOException {

        String fileDownload = null;
        if(request.isGlobal()){
            fileDownload = path + "/" + request.getCicloActual()+ "_GLOBAL"+(request.isRfc() ? "_RFC" : "")+".xlsx";
        }else{
            DelegacionOOADDTO delegacionOOADDTO = getDelegacion(request.getOoad(), request.getSubDelegacion());
            fileDownload = path + "/" + request.getCicloActual()+"_" + request.getOoad()  +delegacionOOADDTO.getDescripcion() +
                    (request.isRfc() ? "_RFC" : "")+".xlsx";

        }
        try {
            return new InputStreamResource(new FileInputStream(ftpClientService.copyFileFromFTP(fileDownload)));
        } catch (DownloadException e) {
            log.log(Level.INFO, "Error al descargar archivo {0}", e.getMessage());
            return null;
        }
    }

    private DelegacionOOADDTO getDelegacion(int ooad, int subdelegacion){

        List<DelegacionOOADDTO> collect = ReportesConstants.catalogo.stream().filter(del -> del.getClaveDelegacion() == ooad).collect(Collectors.toList());

        if(collect.size() == 1){
            return collect.get(0);
        }else{

        	if(subdelegacion == 0) {
        		return collect.stream().filter(del-> del.isDesconcentrada() == false).findFirst().get();	
        	}
        	
           return collect.stream().filter(del-> del.getClaveSubdelegaciones().stream().anyMatch(subDel -> subDel == subdelegacion)).findFirst().get();

        }

    }


}
