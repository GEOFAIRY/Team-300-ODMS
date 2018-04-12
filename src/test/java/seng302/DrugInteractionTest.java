package seng302;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import seng302.Core.DrugInteraction;
import seng302.Core.Gender;
import seng302.Core.Main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DrugInteractionTest {
    private static DrugInteraction drugInteraction;

    @Before
    public void setUp() {
        try{
            String json = new String(Files.readAllBytes(Paths.get("src/test/java/seng302/DrugInteractionTestingJson")));
            drugInteraction = new DrugInteraction(json);
        }catch(IOException e) {
            System.out.println(e);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void ageInteraction_negativeAge_returnsEmptyHashSet(){
        assertEquals(new HashSet<String>(), drugInteraction.ageInteraction(-1));
    }
    @Test
    public void ageInteraction_lowerBoundaryAge_returnsNANSymptomsAndSymptomsInRange(){
        HashSet<String> symptoms = drugInteraction.ageInteraction(0);
        assertEquals(new HashSet<String>(Arrays.asList("a","h")), symptoms);
    }
    @Test
    public void ageInteraction_upperBoundaryAge_returnsNANSymptomsAndSymptomsInRange(){
        HashSet<String> symptoms = drugInteraction.ageInteraction(1);
        assertEquals(new HashSet<String>(Arrays.asList("a","h")), symptoms);
    }
    @Test
    public void ageInteraction_ageAtLeastSixty_returnsNanSymptomsAndSymptomsInRange(){
        HashSet<String> symptoms = drugInteraction.ageInteraction(60);
        assertEquals(new HashSet<String>(Arrays.asList("g","h")), symptoms);
    }

    @Test
    public void ageRangeInteraction_bottomRange_returnsSymptomsFromNANAndBottomRange(){
        HashSet<String> symptoms = drugInteraction.ageRangeInteraction("0-1");
        assertEquals(new HashSet<String>(Arrays.asList("a","h")), symptoms);
    }
    @Test
    public void ageRangeInteraction_topRange_returnsSymptomsFromNANAndTopRange(){
        HashSet<String> symptoms = drugInteraction.ageRangeInteraction("60+");
        assertEquals(new HashSet<String>(Arrays.asList("g","h")), symptoms);
    }
    @Test
    public void ageRangeInteraction_undefinedRange_returnsSymptomsFromNAN(){
        HashSet<String> symptoms = drugInteraction.ageRangeInteraction("UndefinedRangeKey");
        assertEquals(new HashSet<String>(Arrays.asList("h")), symptoms);
    }

    @Test
    public void nanAgeInteraction_called_returnsSymptomsFromNAN(){
        HashSet<String> symptoms = drugInteraction.nanAgeInteraction();
        assertEquals(new HashSet<String>(Arrays.asList("h")), symptoms);
    }

    @Test
    public void allGenderInteractions_called_returnsMaleAndFemaleSymtoms(){
        HashSet<String> symptoms = drugInteraction.allGenderInteractions();
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c","d","e")), symptoms);
    }

    @Test
    public void maleInteractions_called_returnsMaleSymtoms(){
        HashSet<String> symptoms = drugInteraction.maleInteractions();
        assertEquals(new HashSet<String>(Arrays.asList("c","d","e")), symptoms);
    }

    @Test
    public void femaleInteractions_called_returnsMaleSymtoms(){
        HashSet<String> symptoms = drugInteraction.femaleInteractions();
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c")), symptoms);
    }

    @Test
    public void genderInteraction_validGender_returnsGenderSympmtoms(){
        HashSet<String> symptoms = drugInteraction.genderInteraction(Gender.OTHER);
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c","d","e")), symptoms);
    }
    @Test
    public void genderInteraction_nullGender_returnsAllGenderSymtoms(){
        HashSet<String> symptoms = drugInteraction.genderInteraction(null);
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c","d","e")), symptoms);
    }
}