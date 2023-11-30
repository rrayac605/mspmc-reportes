package mx.gob.imss.cit.pmc.mspmcreportes.services;

import java.io.IOException;

public interface StorageService {
	
	String getExtension(String fileName);
	
	byte[] obtenerDictamen(String nss, String fileName) throws IOException;
}
