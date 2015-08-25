package com.gemstone.gemfire.test.examples;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class AssertJExampleJUnitTest {

  private Character aragorn;
  private Character boromir;
  private Character elrond;
  private Character frodo;
  private Character galadriel;
  private Character gandalf;
  private Character gimli;
  private Character legolas;
  private Character merry;
  private Character pippin;
  private Character sauron;
  private Character sam;
  
  private Ring narya;
  private Ring nenya;
  private Ring oneRing;
  private Ring vilya;
  
  private Set<Character> fellowshipOfTheRing;
  private Map<Ring, Character> ringBearers;
  
  @Before
  public void setUp() {
    this.aragorn = new Character("Aragorn");
    this.boromir = new Character("Boromir");
    this.elrond = new Character("Elrond");
    this.frodo = new Character("Frodo");
    this.galadriel = new Character("Galadriel");
    this.gandalf = new Character("Gandalf");
    this.gimli = new Character("Gimli");
    this.legolas = new Character("Legolas");
    this.merry = new Character("Merry");
    this.pippin = new Character("Pippin");
    this.sauron = new Character("Sauron");
    this.sam = new Character("Sam");
    
    this.narya = new Ring();
    this.nenya = new Ring();
    this.oneRing = new Ring();
    this.vilya = new Ring();
    
    this.fellowshipOfTheRing = new HashSet<Character>();
    this.fellowshipOfTheRing.add(this.aragorn);
    this.fellowshipOfTheRing.add(this.boromir);
    this.fellowshipOfTheRing.add(this.frodo);
    this.fellowshipOfTheRing.add(this.gandalf);
    this.fellowshipOfTheRing.add(this.gimli);
    this.fellowshipOfTheRing.add(this.legolas);
    this.fellowshipOfTheRing.add(this.merry);
    this.fellowshipOfTheRing.add(this.pippin);
    this.fellowshipOfTheRing.add(this.sam);

    this.ringBearers = new HashMap<Ring, Character>();
    this.ringBearers.put(this.oneRing, this.frodo);
    this.ringBearers.put(this.nenya, this.galadriel);
    this.ringBearers.put(this.narya, this.gandalf);
    this.ringBearers.put(this.vilya, this.elrond);
  }
  
  @Test
  public void exampleShouldPass() {
    // common assertions
    assertThat(frodo.getName()).isEqualTo("Frodo");
    assertThat(frodo).isNotEqualTo(sauron)
                     .isIn(fellowshipOfTheRing);

    // String specific assertions
    assertThat(frodo.getName()).startsWith("Fro")
                               .endsWith("do")
                               .isEqualToIgnoringCase("frodo");

    // collection specific assertions
    assertThat(fellowshipOfTheRing).hasSize(9)
                                   .contains(frodo, sam)
                                   .doesNotContain(sauron);

    // using extracting magical feature to check fellowshipOfTheRing characters name :)
    assertThat(fellowshipOfTheRing).extracting("name").contains("Boromir", "Gandalf", "Frodo", "Legolas")
                                                      .doesNotContain("Sauron", "Elrond");

    // map specific assertions, ringBearers initialized with the elves rings and the one ring bearers.
    assertThat(ringBearers).hasSize(4)
                           .contains(entry(oneRing, frodo), entry(nenya, galadriel))
                           .doesNotContainEntry(oneRing, aragorn);  
  }
  
  protected static class Character {
    private final String name;
    public Character(final String name) {
      this.name = name;
    }
    public String getName() {
      return this.name;
    }
  }
  
  protected static class Ring {
    public Ring() {
    }
  }
}
