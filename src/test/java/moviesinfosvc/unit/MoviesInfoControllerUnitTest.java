package moviesinfosvc.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import moviesinfosvc.ReactiveSpringMoviesInfoSvcApplication;
import moviesinfosvc.domain.MovieInfo;
import moviesinfosvc.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@WebFluxTest(controllers = MoviesInfoController.class)
@SpringBootTest(classes = ReactiveSpringMoviesInfoSvcApplication.class,webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

  @Autowired
  WebTestClient webTestClient;

  @MockBean
  private MoviesInfoService moviesInfoService;

  static String URL_MOVIE_INFOS = "/v1/movieinfos";

  @Test
  void getAllMovieInfos(){

    var movieInfos = List.of(
        new MovieInfo(null,"Batman",2005,List.of("Chist","Michael"), LocalDate.parse("2005-06-15")),
        new MovieInfo("abc","Batman1",2008,List.of("Chist1","Michael1"), LocalDate.parse("2008-06-15")),
        new MovieInfo(null,"Batman2",20012,List.of("Chist2","Michael2"), LocalDate.parse("2012-06-15"))
    );

    when(moviesInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

    webTestClient
        .get()
        .uri(URL_MOVIE_INFOS)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(3);
  }

  @Test
  void getMovieInfosById(){

    var movieInfoById ="abc";
    var movieInfo = new MovieInfo("abc","Batman1",2008,List.of("Chist1","Michael1"), LocalDate.parse("2008-06-15"));

    when(moviesInfoService.getMovieInfosById(movieInfoById)).thenReturn(Mono.just(movieInfo));

    webTestClient
        .get()
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoById)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var movieInfo1 = movieInfoEntityExchangeResult.getResponseBody();
          assertEquals(movieInfo.getName(),movieInfo1.getName());
        });
  }

  @Test
  void addMovieInfo() {

    //given
    var movieInfo = new MovieInfo(null, "Batman", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    when(moviesInfoService.addMovieInfo(movieInfo))
        .thenReturn(Mono.just(new MovieInfo("mockId" ,"Batman", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"))));

    //when
    webTestClient
        .post()
        .uri(URL_MOVIE_INFOS)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(MovieInfo.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var saveMovieinfo = movieInfoEntityExchangeResult.getResponseBody();
          assert saveMovieinfo != null;
          assert saveMovieinfo.getMovieInfoId() != null;
          assertEquals("mockId",saveMovieinfo.getMovieInfoId());
        });
  }


  @Test
  void updateMovieInfo() {
    //given
    var movieInfoId ="mockId";
    var movieInfo = new MovieInfo(null, "Batman updated", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    when(moviesInfoService.updateMovieInfo(movieInfo,movieInfoId))
        .thenReturn(Mono.just(new MovieInfo("mockId" ,"Batman updated", 2005, List.of("Chist", "Michael"),
            LocalDate.parse("2005-06-15"))));

    //when
    webTestClient
        .put()
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var updateMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
          assert updateMovieInfo != null;
          assert updateMovieInfo.getMovieInfoId() != null;
          assertEquals("Batman updated",updateMovieInfo.getName());
        });
  }

  @Test
  void deleteMovieInfo() {
    //given
    var movieInfoId ="abc";
    when(moviesInfoService.deleteMovieInfo(movieInfoId))
        .thenReturn(Mono.empty());
    //when
    webTestClient
        .delete()
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody(Void.class);
  }

  @Test
  void addMovieInfo_validation() {

    //given
    var movieInfo = new MovieInfo(null, "", -2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    when(moviesInfoService.addMovieInfo(movieInfo))
        .thenReturn(Mono.just(new MovieInfo("mockId" ,"Batman", 2005, List.of("Chist", "Michael"),
            LocalDate.parse("2005-06-15"))));

    //when
    webTestClient
        .post()
        .uri(URL_MOVIE_INFOS)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void addMovieInfo_validation2() {

    //given
    var movieInfo = new MovieInfo(null, "", -2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    when(moviesInfoService.addMovieInfo(movieInfo))
        .thenReturn(Mono.just(new MovieInfo("mockId" ,"Batman", 2005, List.of("Chist", "Michael"),
            LocalDate.parse("2005-06-15"))));

    //when
    webTestClient
        .post()
        .uri(URL_MOVIE_INFOS)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var responseBody = movieInfoEntityExchangeResult.getResponseBody();
          System.out.println("responseBody : "+ responseBody);
          assert responseBody!=null;
        });
  }

  @Test
  void addMovieInfo_validation_with_error_message() {

    //given
    var movieInfo = new MovieInfo(null, "", -2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    //when
    webTestClient
        .post()
        .uri(URL_MOVIE_INFOS)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var responseBody = movieInfoEntityExchangeResult.getResponseBody();
          System.out.println("responseBody : "+ responseBody);
          var errorMessageExpected = "movieInfo.name must be present,movieInfo.year must be a positive value";
          assertEquals( errorMessageExpected,responseBody);
        });
  }

  @Test
  void addMovieInfo_validation_with_error_message_on_the_list() {

    //given
    var movieInfo = new MovieInfo(null, "", -2005, List.of(""),
        LocalDate.parse("2005-06-15"));

    //when
    webTestClient
        .post()
        .uri(URL_MOVIE_INFOS)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var responseBody = movieInfoEntityExchangeResult.getResponseBody();
          System.out.println("responseBody : "+ responseBody);
          var errorMessageExpected = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
          assertEquals( errorMessageExpected,responseBody);
        });
  }

}
