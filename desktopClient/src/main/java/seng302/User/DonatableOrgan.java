package seng302.User;

import seng302.User.Attribute.Organ;

import java.time.LocalDateTime;

public class DonatableOrgan {

    private LocalDateTime dateOfDeath;
    private Organ organType;
    private long donorId;
    private int id;


    public DonatableOrgan(LocalDateTime dateOfDeath, Organ organType, long donorId, int id){
        this.dateOfDeath = dateOfDeath;
        this.donorId = donorId;
        this.organType = organType;
        this.id = id;
    }

    public DonatableOrgan(LocalDateTime dateOfDeath, Organ organType, long donorId){
        this.dateOfDeath = dateOfDeath;
        this.donorId = donorId;
        this.organType = organType;
    }

    public LocalDateTime getDateOfDeath() {
        return dateOfDeath;
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

    public void setDateOfDeath(LocalDateTime dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }
}
