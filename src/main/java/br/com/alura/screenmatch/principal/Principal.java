package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=a80e83b8";
    private Scanner sc = new Scanner(System.in);

    public void exibeMenu() {
        System.out.println("Digite o nome de uma série: ");
        String nomeSerie = sc.nextLine();

        var json = consumoApi.obterDados(ENDERECO + nomeSerie
                .replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoApi.obterDados(ENDERECO + nomeSerie
                    .replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        //laço que repete o nome de todos episodios de todas temporadas
//        for (DadosTemporada temporada : temporadas) { //itera todas temporadas
//            for (DadosEpisodio episodio : temporada.episodios()) { //itera todos episodios de todas temporadas
//                System.out.println(episodio.titulo());
//                System.out.println(episodio.dataLancamento());//imprime o nome de todos os nomes dos episodios das temporadas
//            }
//        }

        temporadas.forEach(t -> t.episodios().forEach
                (e -> System.out.println(e.titulo())));

//        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());

//        System.out.println("Top 10 episódios: ");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Filtragem N/A " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println("Ordenagem " + e))
//                .limit(10)
//                .peek(e -> System.out.println("Classificação " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                        .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        //data dos episodios
//        System.out.println("A partir de qual data deseja ver os episódios: ");
//        int ano = sc.nextInt();
//        sc.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento()
//                        .isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "\nTemporada: " + e.getTemporada() +
//                                "\nEpisódio: " + e.getTitulo() +
//                                "\nData lançamento: " + e.getDataLancamento().format(formatter)
//                ));

//        Busca pelo o nome
//        System.out.println("Digite o trecho do titulo para buscar: ");
//        var trechoTitulo = sc.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase()
//                        .contains(trechoTitulo
//                                .toUpperCase()))
//                .findFirst();
//
//        if (episodioBuscado.isPresent()) {
//            System.out.println("Episódio encontrado!");
//            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//            System.out.println("Episódio: " + episodioBuscado.get().getTitulo());
//        } else {
//            System.out.println("Episódio não encontrado!");
//        }

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0)
                .collect(Collectors.groupingBy
                        (Episodio::getTemporada,
                                Collectors.averagingDouble(Episodio::getAvaliacao)));
        avaliacoesPorTemporada.forEach((temporada, avaliacao) -> {
            System.out.printf("\nTemporada: %d\nAvaliação: %.2f\n", temporada, avaliacao);
        });

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Maior nota: " + est.getMax()
        + "\nMenor nota: " + est.getMin()
        + "\nMédia: " + est.getAverage()
        + "\nQuantidade de episódios avaliados: " +est.getCount());
    }
}