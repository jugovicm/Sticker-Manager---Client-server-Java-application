package albumclient;

import java.util.*;


public class MenjazaKlijent {
    String userName;
    ArrayList<String> duplicates;
    ArrayList<String> missingCards;
    
    public MenjazaKlijent() {
        this.userName = new String();
        this.duplicates = new ArrayList<>();
        this.missingCards = new ArrayList<>();
    }
    
    public void setUserName(String s) {
        this.userName = s;
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public void setDuplicates(String[] list) {
        this.duplicates.addAll(Arrays.asList(list));
    }
    
    public ArrayList<String> getDuplicates() {
        return this.duplicates;
    }
    
    public void setMissingCards(String[] list) {
        this.missingCards.addAll(Arrays.asList(list));
    }
    
    public ArrayList<String> getMissingCards() {
        return this.missingCards;
    }
}
