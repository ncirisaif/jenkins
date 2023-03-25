package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;
@Log4j2
class ClassTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Test
    public void async() {

        Flux<Integer> integers = Flux.create(integerFluxSink -> this.launch(integerFluxSink, 5));
        StepVerifier
                .create(integers.doFinally(signalType -> this.executorService.shutdown()))
                .expectNextCount(5)
                .verifyComplete();
    }

    private void launch(FluxSink<Integer> integerFluxSink,
                        int count
    ) {
        this.executorService.submit(() -> {
            var integer = new AtomicInteger();
            Assertions.assertNotNull(integerFluxSink);
            while (integer.get() < count) {
                double random = Math.random();
                integerFluxSink.next(integer.incrementAndGet());
                this.sleep((long) (random * 1_000));
            }
            integerFluxSink.complete();
        });
    }

    private void sleep(long s) {
        try {
            Thread.sleep(s);
        } catch (Exception e) {
            log.error(e);
        }
    }


    @Test
    public void emitterProcessor() {
        EmitterProcessor<String> processor = EmitterProcessor.create();
        produce(processor.sink());
        consume(processor);
    }

    private void produce(FluxSink<String> sink) {
        sink.next("1");
        sink.next("2");
        sink.next("3");
        sink.complete();
    }

    private void consume(Flux<String> publisher) {
        StepVerifier //
                     .create(publisher)//
                     .expectNext("1")//
                     .expectNext("2")//
                     .expectNext("3")//
                     .verifyComplete();
    }
    @Test
    public void transform() {
        var finished = new AtomicBoolean();
        var letters = Flux//
                          .just("A", "B", "C").transform(
                        stringFlux -> stringFlux.doFinally(signal -> finished.set(false))
                );
        StepVerifier.create(letters).expectNextCount(3).verifyComplete();
        Assertions.assertTrue(finished.get(), "the finished Boolean must be true.");
    }

    @Test
    public void thenMany() {
        var letters = new AtomicInteger();
        var numbers = new AtomicInteger();
        Flux<String> lettersPublisher = Flux.just("a", "b", "c")
                                            .doOnNext(value -> letters.incrementAndGet());
        Flux<Integer> numbersPublisher = Flux.just(1, 2, 3)
                                             .doOnNext(number -> numbers.incrementAndGet());
        Flux<Integer> thisBeforeThat =  numbersPublisher.thenMany(numbersPublisher);
        StepVerifier.create(thisBeforeThat).expectNext(1, 2, 3).verifyComplete();
        Assertions.assertEquals(letters.get(), 3);
        Assertions.assertEquals(numbers.get(), 3);
    }

    @Test
    public void flatMap() {
        Flux<Integer> data = Flux
                .just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
.flatMap(id -> this.delayReplyFor(id.id, id.delay));
        StepVerifier//
                    .create(data)//
                    .expectNext(3, 2, 1)//
                    .verifyComplete();
    }
    private Flux<Integer> delayReplyFor(Integer i, long delay) {
        return Flux.just(i).delayElements(Duration.ofMillis(delay));
    }
    @AllArgsConstructor
    static class Pair {
        private int id;
        private long delay;
    }

    public static void main(String[] args) {

    }
}