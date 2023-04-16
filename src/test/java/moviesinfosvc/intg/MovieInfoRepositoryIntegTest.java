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
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

//@DataMongoTest@ActiveProfiles("test")
@SpringBootTest(classes = ReactiveSpringMoviesInfoSvcApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MovieInfoRepositoryIntegTest {

  @Autowired
  MovieInfoRepository movieInfoRepository;

  @BeforeEach
  void setup(){
    var movieInfos = List.of(
        new MovieInfo(null,"Batman",2005,List.of("Chist","Michael"), LocalDate.parse("2005-06-15")),
        new MovieInfo("abc","Batman1",2008,List.of("Chist1","Michael1"), LocalDate.parse("2008-06-15")),
        new MovieInfo(null,"Batman2",20012,List.of("Chist2","Michael2"), LocalDate.parse("2012-06-15"))
    );
    movieInfoRepository.saveAll(movieInfos).blockLast(); //only use in the test case
  }

  @AfterEach
  void tearDown(){
    movieInfoRepository.deleteAll().block();
  }

  @Test
  void findAll(){

    //given

    //when
   var moviesInfoFlux= movieInfoRepository.findAll();

    //then

    StepVerifier.create(moviesInfoFlux)
        .expectNextCount(3)
        .verifyComplete();
  }

  @Test
  void findById(){

    //given

    //when
    var moviesInfoMono= movieInfoRepository.findById("abc").log();

    //then

    StepVerifier.create(moviesInfoMono)
       // .expectNextCount(1)
        .assertNext(movieInfo -> assertEquals("Batman1",movieInfo.getName()))
        .verifyComplete();
  }

  @Test
  void saveMovieInfo(){

    //given
    var movieInfo = new MovieInfo(null,"Batman2",20012,List.of("Chist2","Michael2"), LocalDate.parse("2012-06-15"));

    //when
    var moviesInfoMono= movieInfoRepository.save(movieInfo).log();

    //then

    StepVerifier.create(moviesInfoMono)
        // .expectNextCount(1)
        .assertNext(movieInfo1 -> {
          assertNotNull(movieInfo1.getMovieInfoId());
          assertEquals("Batman2", movieInfo1.getName());
        })
        .verifyComplete();
  }

  @Test
  void updateMovieInfo(){

    //given
    var movieInfo= movieInfoRepository.findById("abc").block();
    movieInfo.setYear(2021);

    //when
    var movieInfoMono= movieInfoRepository.save(movieInfo).log();

    //then

    StepVerifier.create(movieInfoMono)
        // .expectNextCount(1)
        .assertNext(movieInfo1 -> {
          assertNotNull(movieInfo1.getMovieInfoId());
          assertEquals(2021, movieInfo1.getYear());
        })
        .verifyComplete();
  }

  @Test
  void deleteMovieInfo(){

    //given

    //when
     movieInfoRepository.deleteById("abc").block();
     var moviesInfoFlux = movieInfoRepository.findAll().log();

    //then

    StepVerifier.create(moviesInfoFlux)
        .expectNextCount(2)
        .verifyComplete();
  }

  @Test
  void findByYear(){

    //given

    //when
    var moviesInfoFlux= movieInfoRepository.findByYear(2005).log();

    //then

    StepVerifier.create(moviesInfoFlux)
        .expectNextCount(1)
        .verifyComplete();
  }

}
