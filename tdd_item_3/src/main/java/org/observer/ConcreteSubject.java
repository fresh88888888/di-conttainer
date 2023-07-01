package org.observer;

import java.util.ArrayList;
import java.util.List;

public class ConcreteSubject implements Subject{
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver(Message message) {
        for (Observer o : observers) {
            o.update(message);
        }
    }
}
