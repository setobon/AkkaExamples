import akka.actor.AbstractActor;

public class MyActor extends AbstractActor {

    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
