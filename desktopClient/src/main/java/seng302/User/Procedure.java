package seng302.User;

import seng302.User.Attribute.Organ;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Class contains all the information for a given procedure used in the Medical History (Procedures) section.
 */
public class Procedure {

    private String summary;
    private String description;
    private LocalDate date;
    private ArrayList<Organ> organsAffected;

    /**
     * method to create a new instance of a procedure object
     *
     * @param summary          the given procedure summary
     * @param description      the given procedure description
     * @param date             the given procedure date
     * @param organsAffected   the given organs to be operated on in the procedure
     */
    public Procedure(String summary, String description, LocalDate date, ArrayList<Organ> organsAffected) {
        this.summary = summary;
        this.description = description;
        this.date = date;
        this.organsAffected = new ArrayList<>();
        this.organsAffected.addAll(organsAffected);
    }

    /**
     * returns the procedures affected organs
     *
     * @return list of organs
     */
    public ArrayList<Organ> getOrgansAffected() {
        return organsAffected;
    }

    /**
     * Sets the organs affected
     *
     * @param organsAffected the organs to change it to
     */
    public void setOrgansAffected(ArrayList<Organ> organsAffected) {
        this.organsAffected = organsAffected;
    }

    /**
     * returns the procedure summary
     *
     * @return String the procedure summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * sets the procedure summary
     *
     * @param summary given procedure summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * returns the procedure summary
     *
     * @return String the procedure description
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the procedure description
     *
     * @param description String given procedure summary
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * returns the date of the procedure given
     *
     * @return date of the procedure
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * sets the procedure date
     *
     * @param date date to set for the procedure
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return summary;
    }


}
