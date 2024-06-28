package com.alex.ScreenMatch.principal;

import com.alex.ScreenMatch.model.DatosEpisodio;
import com.alex.ScreenMatch.model.DatosSerie;
import com.alex.ScreenMatch.model.DatosTemporada;
import com.alex.ScreenMatch.service.ConsumoAPI;
import com.alex.ScreenMatch.service.ConvierteDatos;

import com.alex.ScreenMatch.model.Episodio;
import org.springframework.cglib.core.Local;

import javax.xml.transform.Source;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    // Busca los datos generales de las series
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();

    private final String API_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4966728d";
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu(){
        System.out.println("Escribe el nombre de la serie que deseas buscar: ");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(API_BASE+nombreSerie.replace(" ", "+")+API_KEY);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);

        // Busca los datos de todas las temporadaas
        List<DatosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= datos.totalDeTemporadas() ; i++) {
            json = consumoApi.obtenerDatos(API_BASE+nombreSerie.replace(" ","+")+"&Season="+i+API_KEY);
            var datosTemporada = conversor.obtenerDatos(json, DatosTemporada.class);
            temporadas.add(datosTemporada);
        }
        //temporadas.forEach(System.out::println);

        //Mostrar solo el titulo de las episodios para los temporadas
        /*for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            List<DatosEpisodio> episodosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodosTemporada.size(); j++) {
                System.out.println(episodosTemporada.get(j).titulo());
            }
        }*/

        // otra forma de reducir codigo
        //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // COnvertir todas las informaciones en una lista del tipo DatosEpidsorio
        List<DatosEpisodio>  datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        // TOp 5 epdisodios
        /*datosEpisodios.stream()
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primer filtro N/A " + e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e -> System.out.println("Segundo filtro Ordenacion M > m" + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("tercer filtro Mayusculas " + e))
                .limit(5)
                .forEach(System.out::println);*/

        //Convirtiendo los datos a una lista del tipo Episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numeroDeTemporada(), d)))
                .collect(Collectors.toList());

        /*episodios.forEach(System.out::println);

        //Busqueda de episodioa a partir de x anho
        /*System.out.println("Indica el anho a partir del cual deseas ver los episodios:");
        var fecha = teclado.nextInt();
        teclado.nextLine();
        LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
                .forEach(e -> System.out.println(
                        "Temporada " + e.getTemporada() +
                                "Episodio " + e.getTitulo() +
                                "Fecha de lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
                ));*/

        //Busca episodios por pedazo de titulo
        /*System.out.println("Escriba el titulo del episodio que desea ver: ");
        var pedazoTitulo = teclado.nextLine();
        Optional<Episodio> espisodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
                .findFirst();

        if(espisodioBuscado.isPresent()){
            System.out.println("Episodio encontrado!");
            System.out.println("Los datos son: " + espisodioBuscado.get());
        }
        else{
            System.out.println("Episodio no encontrado!!");
        }*/

        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));

        System.out.println(evaluacionesPorTemporada);


        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));

        System.out.println("La Media es: " + est.getAverage());
        System.out.println("Episodio Mejor evaluado: " + est.getMax());
        System.out.println("Episodio peor evaluado: " + est.getMin());
    }
}