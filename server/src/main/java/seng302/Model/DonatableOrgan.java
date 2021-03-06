package seng302.Model;

import seng302.Model.Attribute.Organ;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DonatableOrgan {

    private LocalDateTime timeOfDeath;
    private Organ organType;
    private long donorId;
    private int id;
    private Duration timeLeft;
    private List<User> topReceivers;
    private Boolean expired;
    private int inTransfer;


    public DonatableOrgan(LocalDateTime timeOfDeath, Organ organType, long donorId, int id, boolean expired, int inTransfer){
        this.timeOfDeath = timeOfDeath;
        this.donorId = donorId;
        this.organType = organType;
        this.id = id;
        this.expired = expired;
        this.inTransfer = inTransfer;
    }

    public DonatableOrgan(LocalDateTime timeOfDeath, Organ organType, long donorId, boolean expired, int inTransfer){
        this.timeOfDeath = timeOfDeath;
        this.donorId = donorId;
        this.organType = organType;
        this.expired = expired;
        this.inTransfer = inTransfer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimeOfDeath() {
        return timeOfDeath;
    }

    public long getDonorId() {
        return donorId;
    }

    public Organ getOrganType() {
        return organType;
    }

    public int getId() {
        return id;
    }

    public void setTimeOfDeath(LocalDateTime timeOfDeath) {
        this.timeOfDeath = timeOfDeath;
    }

//    public Duration getTimeLeft(){
//        return timeLeft;
//    }
//
//    public void setTimeLeft(Duration time) {
//        timeLeft = time;
//    }
//
//    public void tickTimeLeft(){
//        timeLeft.minus(1, SECONDS);
//    }

    public List<User> getTopReceivers() {
        return topReceivers;
    }

    public void setTopReceivers(List<User> topReceivers) {
        this.topReceivers = topReceivers;
    }
}

