package moviesinfosvc.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import moviesinfosvc.ReactiveSpringMoviesInfoSvcApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

//@WebFluxTest(controllers = FluxAndMonoController.class)
@SpringBootTest(classes = ReactiveSpringMoviesInfoSvcApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FluxAndMonoControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @Test
  void flux() {
    webTestClient.get()
        .uri("/flux")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Integer.class)
        .hasSize(3);
  }

  @Test
  void flux_approach2() {
    var flux = webTestClient.get()
        .uri("/flux")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Integer.class)
        .getResponseBody();

    StepVerifier.create(flux).expectNext(1, 2, 3).verifyComplete();
  }

  @Test
  void flux_approach3() {
    webTestClient.get()
        .uri("/flux")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Integer.class)
        .consumeWith(listEntityExchangeResult -> {
          var responseBody = listEntityExchangeResult.getResponseBody();
          assert (responseBody.size() == 3);
        });
  }

  @Test
  void mono() {
    webTestClient.get()
        .uri("/mono")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .consumeWith(stringEntityExchangeResult -> {
          var responseBody = stringEntityExchangeResult.getResponseBody();
          assertEquals("hello-world",responseBody);
        });
  }


  @Test
  void stream() {
    var flux = webTestClient.get()
        .uri("/stream")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Long.class)
        .getResponseBody();

    StepVerifier.create(flux)
        .expectNext(0L,1L, 2L, 3L)
        .thenCancel()//
        .verify();
  }

}
