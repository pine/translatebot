package moe.pine.translatebot.slack;

@FunctionalInterface
public interface EventListener {
    void accept(Event t) throws InterruptedException;
}
