package moviesinfosvc.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import moviesinfosvc.domain.MovieInfo;
import moviesinfosvc.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1/moviesinfo")
@Slf4j
public class MoviesInfoController {

  private MoviesInfoService moviesInfoService;
  Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

  public MoviesInfoController(MoviesInfoService moviesInfoService) {
    this.moviesInfoService = moviesInfoService;
  }

  @GetMapping(value = "/stream",produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<MovieInfo> getAllMovieInfosStream(){
    return movieInfoSink.asFlux().log(); // here is the attach of subscribe
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){
    return moviesInfoService.addMovieInfo(movieInfo)
        .doOnNext(savedInfo->movieInfoSink.tryEmitNext(savedInfo));
    /*
    Fondamental idea: any time we add a new movieInfo, publish that movie to something.
    Subscribe to this movieInfo. Ref https://projectreactor.io/docs/core/release/reference/#sinks
    */
  }

  @GetMapping
  public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value="year",required = false) Integer year){
    log.info("Year is : {}",year);
    if(year!=null){
      return moviesInfoService.getMovieInfosByYear(year);
    }
    return moviesInfoService.getAllMovieInfos().log();
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<MovieInfo>> getMovieInfosById(@PathVariable String id){
    return moviesInfoService.getMovieInfosById(id)
        .map(ResponseEntity.ok()::body)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        .log();
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updateMovieInfo,@PathVariable String id){
    return moviesInfoService.updateMovieInfo(updateMovieInfo,id)
        .map(ResponseEntity.ok()::body)// this operation transform Mono<MovieInfo> to Mono<ResponseEntity<MovieInfo>>
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        .log();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteMovieInfo(@PathVariable String id){
    return moviesInfoService.deleteMovieInfo(id).log();
  }

}
