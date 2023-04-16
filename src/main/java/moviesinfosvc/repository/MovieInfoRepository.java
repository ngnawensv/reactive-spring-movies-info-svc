package moviesinfosvc.repository;


import moviesinfosvc.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MovieInfoRepository  extends ReactiveMongoRepository<MovieInfo,String> {
  Flux<MovieInfo> findByYear(Integer year);

}
