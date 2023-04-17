package moviesinfosvc.intg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import moviesinfosvc.ReactiveSpringMoviesInfoSvcApplication;
import moviesinfosvc.domain.MovieInfo;
import moviesinfosvc.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;


//@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegTest {
  @Autowired
  MovieInfoRepository movieInfoRepository;
  @Autowired
  WebTestClient webTestClient;
  static String URL_MOVIE_INFOS = "/v1/moviesinfo";

  @BeforeEach
  void setup() {
    var movieInfos = List.of(
        new MovieInfo(null, "Batman", 2005, List.of("Chist", "Michael"),
            LocalDate.parse("2005-06-15")),
        new MovieInfo("abc", "Batman1", 2008, List.of("Chist1", "Michael1"),
            LocalDate.parse("2008-06-15")),
        new MovieInfo(null, "Batman2", 20012, List.of("Chist2", "Michael2"),
            LocalDate.parse("2012-06-15"))
    );
    movieInfoRepository.saveAll(movieInfos).blockLast(); //blockLast() only use in the test case
  }

  @AfterEach
  void tearDown() {
    movieInfoRepository.deleteAll().block();
  }

  @Test
  void addMovieInfo() {

    //given
    var movieInfo = new MovieInfo(null, "Batman", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

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
        });
  }

  @Test
  void getAllMovieInfos(){
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
  void getAllMovieInfos_stream(){

    //given
    var movieInfo = new MovieInfo(null, "Batman", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));


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
        });

    //when
    var movieInfosStreamFlux = webTestClient
        .get()
        .uri(URL_MOVIE_INFOS+"/stream")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(MovieInfo.class)
        .getResponseBody();

    StepVerifier.create(movieInfosStreamFlux)
        .assertNext(movieInfo1 -> {
          assert movieInfo1.getMovieInfoId()!=null;
        }).thenCancel().verify();
  }

  @Test
  void getAllMovieInfoByYear(){
    var uri =UriComponentsBuilder.fromUriString(URL_MOVIE_INFOS)
            .queryParam("year",2005)
                .buildAndExpand().toUri();
    webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(MovieInfo.class)
        .hasSize(1);
  }

  @Test
  void getMovieInfosById(){
    var movieInfoId ="abc";
    webTestClient
        .get()
       // .uri(URL_MOVIE_INFOS+"/abc")
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)// best practice
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(MovieInfo.class)
        .consumeWith(movieInfoEntityExchangeResult -> {
          var movieInfo =movieInfoEntityExchangeResult.getResponseBody();
          assertNotNull(movieInfo);
        });
  }

  @Test
  void getMovieInfosById_ApprochWithJson(){
    var movieInfoId ="abc";
    webTestClient
        .get()
        // .uri(URL_MOVIE_INFOS+"/abc")
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)// best practice
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody()
        .jsonPath("$.name").isEqualTo("Batman1");
  }

  @Test
  void updateMovieInfo() {
    //given
    var movieInfoId ="abc";
    var movieInfo = new MovieInfo(null, "Batman updated", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

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
  void updateMovieInfo_notfound() {
    //given
    var movieInfoId ="def";
    var movieInfo = new MovieInfo(null, "Batman updated", 2005, List.of("Chist", "Michael"),
        LocalDate.parse("2005-06-15"));

    //when
    webTestClient
        .put()
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)
        .bodyValue(movieInfo)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void getMovieInfosById_notfound(){
    var movieInfoId ="def";
    webTestClient
        .get()
        .uri(URL_MOVIE_INFOS+"/{id}",movieInfoId)// best practice
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
