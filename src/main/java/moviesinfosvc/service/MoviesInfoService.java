package moviesinfosvc.service;

import java.lang.System.Logger;
import moviesinfosvc.domain.MovieInfo;
import moviesinfosvc.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {
  private MovieInfoRepository movieInfoRepository;

  public MoviesInfoService(MovieInfoRepository movieInfoRepository) {
    this.movieInfoRepository = movieInfoRepository;
  }

  public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo){
    return movieInfoRepository.save(movieInfo);
  }

  public Flux<MovieInfo> getAllMovieInfos() {
    return movieInfoRepository.findAll();
  }

  public Mono<MovieInfo> getMovieInfosById(String id) {
    return movieInfoRepository.findById(id);
  }

  public Mono<MovieInfo> updateMovieInfo(MovieInfo updateMovieInfo, String id) {
    //flatMap() operation is use because we transform a reactive type to another reactive type
   return movieInfoRepository.findById(id)
        .flatMap(movieInfo -> {
          movieInfo.setYear(updateMovieInfo.getYear());
          movieInfo.setName(updateMovieInfo.getName());
          movieInfo.setCast(updateMovieInfo.getCast());
          movieInfo.setRelease_date(updateMovieInfo.getRelease_date());
          return movieInfoRepository.save(updateMovieInfo);
        });
  }

  public Mono<Void> deleteMovieInfo(String id) {
    return movieInfoRepository.deleteById(id);
  }

  public Flux<MovieInfo> getMovieInfosByYear(Integer year) {
    return movieInfoRepository.findByYear(year);
  }
}
