package mx.gob.imss.cit.pmc.mspmcreportes.constants;

import mx.gob.imss.cit.pmc.mspmcreportes.dto.DelegacionOOADDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportesConstants {

    public static List<DelegacionOOADDTO> catalogo = new ArrayList();
    static {
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(01).claveSubdelegaciones(Arrays.asList(1, 19)).descripcion("AGUASCALIENTES").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(02).claveSubdelegaciones(Arrays.asList(1, 2, 3, 4)).descripcion("BAJA CALIFORNIA NORTE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(03).claveSubdelegaciones(Arrays.asList(1, 8)).descripcion("BAJA CALIFOR SUR").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(04).claveSubdelegaciones(Arrays.asList(1, 4)).descripcion("CAMPECHE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(8).claveSubdelegaciones(Arrays.asList(1, 3, 5, 8, 22, 60)).descripcion("CHIHUAHUA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(8).claveSubdelegaciones(Arrays.asList(10)).descripcion("JUAREZ 1").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(06).claveSubdelegaciones(Arrays.asList(1, 3, 7)).descripcion("COLIMA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(05).claveSubdelegaciones(Arrays.asList(3, 11, 12, 17, 23)).descripcion("COAHUILA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(07).claveSubdelegaciones(Arrays.asList(1, 2)).descripcion("CHIAPAS").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(39).claveSubdelegaciones(Arrays.asList( 11, 16, 54, 56, 57)).descripcion("D.F. NORTE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(40).claveSubdelegaciones(Arrays.asList( 1, 6, 11, 54, 58)).descripcion("D.F.SUR").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(10).claveSubdelegaciones(Arrays.asList( 1, 13)).descripcion("DURANGO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(12).claveSubdelegaciones(Arrays.asList( 1, 2, 3, 13)).descripcion("GUERRERO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(11).claveSubdelegaciones(Arrays.asList( 1, 5, 8, 14, 17)).descripcion("GUANAJUATO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(27).claveSubdelegaciones(Arrays.asList( 1)).descripcion("HERMOSILLO").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(13).claveSubdelegaciones(Arrays.asList( 1, 5, 7, 10)).descripcion("HIDALGO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(14).claveSubdelegaciones(Arrays.asList( 12, 15, 22, 38, 39, 40, 50)).descripcion("JALISCO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(17).claveSubdelegaciones(Arrays.asList( 3, 9, 13, 17, 27)).descripcion("MICHOACAN").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(16).claveSubdelegaciones(Arrays.asList( 1, 5)).descripcion("MEX. PONIENTE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(20).claveSubdelegaciones(Arrays.asList( 6, 8, 31, 32, 33, 34)).descripcion("NUEVO LEON").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(18).claveSubdelegaciones(Arrays.asList( 1, 11, 15)).descripcion("MORELOS").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(29).claveSubdelegaciones(Arrays.asList( 19)).descripcion("MATAMOROS").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(26).claveSubdelegaciones(Arrays.asList( 5)).descripcion("MAZATLAN").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(19).claveSubdelegaciones(Arrays.asList( 1)).descripcion("NAYARIT").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(21).claveSubdelegaciones(Arrays.asList( 2, 3, 4, 53)).descripcion("OAXACA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(22).claveSubdelegaciones(Arrays.asList( 1, 5, 6, 8, 22)).descripcion("PUEBLA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(24).claveSubdelegaciones(Arrays.asList( 1, 2, 7)).descripcion("QUINTANA ROO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(23).claveSubdelegaciones(Arrays.asList( 1, 3)).descripcion("QUERETARO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(26).claveSubdelegaciones(Arrays.asList( 1, 3, 4)).descripcion("SINALOA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(25).claveSubdelegaciones(Arrays.asList( 1, 3, 5, 60)).descripcion("SAN LUIS POTOSI").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(27).claveSubdelegaciones(Arrays.asList( 3, 7, 10, 13, 51, 57, 70)).descripcion("SONORA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(29).claveSubdelegaciones(Arrays.asList( 1, 4, 10, 13, 18)).descripcion("TAMAULIPAS").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(28).claveSubdelegaciones(Arrays.asList( 1, 2)).descripcion("TABASCO").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(02).claveSubdelegaciones(Arrays.asList(5)).descripcion("TIJUANA").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(15).claveSubdelegaciones(Arrays.asList( 6, 54, 80)).descripcion("MEX. ORIENTE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(05).claveSubdelegaciones(Arrays.asList(9)).descripcion("TORREON").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(30).claveSubdelegaciones(Arrays.asList( 1)).descripcion("TLAXCALA").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(31).claveSubdelegaciones(Arrays.asList( 2, 7, 9, 25)).descripcion("VERACRUZ NORTE").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(31).claveSubdelegaciones(Arrays.asList( 12)).descripcion("VERACRUZ PUERTO").desconcentrada(true).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(32).claveSubdelegaciones(Arrays.asList( 2, 3, 38, 45)).descripcion("VERACRUZ SUR").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(33).claveSubdelegaciones(Arrays.asList( 1, 33)).descripcion("YUCATAN").desconcentrada(false).build());
        catalogo.add(DelegacionOOADDTO.builder().claveDelegacion(34).claveSubdelegaciones(Arrays.asList( 1, 9)).descripcion("ZACATECAS").desconcentrada(false).build());
    }
}
