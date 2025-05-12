package servicios;

import entidades.RegistroTemperatura;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemperaturaServicio {

public static List<RegistroTemperatura> cargarDatos(String nombreArchivo) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            Stream<String> lineas = Files.lines(Paths.get(nombreArchivo));
            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(partes -> new RegistroTemperatura(
                            partes[0],
                            LocalDate.parse(partes[1], formato),
                            Double.parseDouble(partes[2])
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static List<String> getCiudades(List<RegistroTemperatura> datos) {
        return datos.stream()
                .map(RegistroTemperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<RegistroTemperatura> filtrar(LocalDate desde, LocalDate hasta, List<RegistroTemperatura> datos) {
        return datos.stream()
                .filter(d -> !d.getFecha().isBefore(desde) && !d.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public static Par<List<String>, List<Double>> extraerPromediosPorCiudad(List<RegistroTemperatura> datos) {
        Map<String, Double> promedioPorCiudad = datos.stream()
                .collect(Collectors.groupingBy(
                        RegistroTemperatura::getCiudad,
                        LinkedHashMap::new,
                        Collectors.averagingDouble(RegistroTemperatura::getTemperatura)
                ));

        List<String> ciudades = promedioPorCiudad.keySet().stream().collect(Collectors.toList());
        List<Double> promedios = promedioPorCiudad.values().stream().collect(Collectors.toList());

        return new Par<>(ciudades, promedios);
    }

    public static Map<String, String> getResumen(LocalDate fecha, List<RegistroTemperatura> datos) {
        var datosFiltrados = datos.stream()
                .filter(d -> d.getFecha().equals(fecha))
                .collect(Collectors.toList());

        Optional<RegistroTemperatura> max = datosFiltrados.stream()
                .max(Comparator.comparingDouble(RegistroTemperatura::getTemperatura));

        Optional<RegistroTemperatura> min = datosFiltrados.stream()
                .min(Comparator.comparingDouble(RegistroTemperatura::getTemperatura));

        Map<String, String> resultado = new LinkedHashMap<>();
        resultado.put("Ciudad más calurosa", max.map(RegistroTemperatura::getCiudad).orElse("N/A"));
        resultado.put("Ciudad menos calurosa", min.map(RegistroTemperatura::getCiudad).orElse("N/A"));

        return resultado;
    }
}
