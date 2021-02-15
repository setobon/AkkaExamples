import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static akka.pattern.PatternsCS.ask;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AkkaTest {

    private static ActorSystem system = null;

    @BeforeEach
    public void setup(){
        system = ActorSystem.create("test-system");
    }

    @AfterEach
    public void tearDown(){
        TestKit.shutdownActorSystem(system, Duration.apply(1000, TimeUnit.MILLISECONDS), true);

        system = null;
    }

    @Test
    public void givenAnActor_sendHimAMessageUsingAsk() throws ExecutionException, InterruptedException {
        final TestKit probe = new TestKit(system);
        ActorRef wordCounterActorRef = probe.childActorOf(Props.create(WordCounterActor.class));

        CompletableFuture<Object> future = ask(wordCounterActorRef,
                new WordCounterActor.CountWords("This is a text"), 1000).toCompletableFuture();

        Integer numberOfWords = (Integer) future.get();
        System.out.println("number of word = " + numberOfWords);

        assertTrue(numberOfWords == 4, "the actor count four words");
    }

    @Test
    public void givenAnActor_whenTheMessageIsNull_respondWithException() throws ExecutionException, InterruptedException {
        final TestKit probe = new TestKit(system);
        ActorRef wordCounterActorRef = probe.childActorOf(Props.create(WordCounterActor.class));

        CompletableFuture<Object> future = ask(wordCounterActorRef,
                new WordCounterActor.CountWords(null), 1000).toCompletableFuture();

        try {
            future.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            assertTrue(e.getMessage().contains("The text to process can't be null!"));
        }catch (InterruptedException | TimeoutException e){
            fail("Actor should respond with an exception instead of timing out !");
        }
    }

    private static String TEXT = "Lorem Ipsum is simply dummy text\n" +
            "of the printing and typesetting industry.\n" +
            "Lorem Ipsum has been the industry's standard dummy text\n" +
            "ever since the 1500s, when an unknown printer took a galley\n" +
            "of type and scrambled it to make a type specimen book.\n" +
            " It has survived not only five centuries, but also the leap\n" +
            "into electronic typesetting, remaining essentially unchanged.\n" +
            " It was popularised in the 1960s with the release of Letraset\n" +
            " sheets containing Lorem Ipsum passages, and more recently with\n" +
            " desktop publishing software like Aldus PageMaker including\n" +
            "versions of Lorem Ipsum.";

    @Test
    public void givenAnAkkaSystem_countTheWordsInAText(){
        ActorSystem system = ActorSystem.create("test-system");

        ActorRef myActorRef = system.actorOf(Props.create(MyActor.class), "myActor");
        myActorRef.tell("command null", null);

        ActorRef readingActorRef = system.actorOf(ReadingActor.props(TEXT), "readingActor");
        readingActorRef.tell(new ReadingActor.ReadLines(), ActorRef.noSender());
    }
}
