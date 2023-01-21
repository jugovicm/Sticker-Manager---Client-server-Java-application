/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package albumclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author eminovicm
 */
public class ReceiveMessageFromServer implements Runnable {
    AlbumClient parent;
    BufferedReader br;
    
    public ReceiveMessageFromServer(AlbumClient parent) {
        this.parent = parent;
        this.br = parent.getBr();
    }
    
    @Override
    public void run() {while (true) {
            String line;
            try {

                line = this.br.readLine();

                if (line.startsWith("Users:")) {
                    /* 
                    1. parsiraj pristiglu poruku, 
                    2. prepoznaj korisnike koji su trenutno u Menjazi
                    3. azuriraj ComboBox sa spiskom korisnika koji su trenutno u Menjazi
                     */

                    String[] users = line.split(":")[1].split(";");
                    parent.getCbUsers().removeAllItems();
                    parent.getTradingClients().clear();

                    HashMap<String, javax.swing.JCheckBox> missings = parent.getMissingCards();
                    HashMap<String, javax.swing.JCheckBox> dups = parent.getDuplicates();
                    
                    for (String user : users) {
                        if (!user.equals(parent.getUserName())) {
                            int i = 0;
                            String[] info = user.split(" ");
                            String ime = info[0];
                            String[] userDuplicates = info[1].split(",");
                            String[] userMissingCards = info[2].split(",");

                            for (String s : userDuplicates) {
                                if (missings.containsKey(s)) 
                                    i++;
                            }

                            if (!ime.equals("") && !ime.equals(parent.getUserName())) {
                                parent.getCbUsers().addItem(ime.trim() + " (" + i + " slicica)");
                            }
                            
                            MenjazaKlijent ct = new MenjazaKlijent();
                            ct.setUserName(ime);
                            ct.setDuplicates(userDuplicates);
                            ct.setMissingCards(userMissingCards);
                            parent.addTradingClient(ct);
                            
                        }
                    }
                    

                } else if (line.startsWith("cmbRequest:")){     // odgovor na request of ComboBox-a
                    String[] s = line.split(":");
                    String reqUser = s[1];
                    String[] reqUserDups = s[2].split(",");
                    ArrayList<Integer> matchingCards = new ArrayList<>();
                    
                    String strPattern = "\\d+";
                    Pattern pattern = Pattern.compile(strPattern);  
                    for (String reqUserDup : reqUserDups) {
                        if (parent.getMissingCards().containsKey(reqUserDup)) {
                            Matcher matcher = pattern.matcher(reqUserDup);
                            if (matcher.find())
                                matchingCards.add(Integer.parseInt(matcher.group()));
                        }
                    }
                    Collections.sort(matchingCards);
                    
                    System.out.println(parent.getTaReceivedMessages());
                    if (!parent.getTaReceivedMessages().equals("Razmena je izvrsena!\n") && !parent.getTaReceivedMessages().equals("Zahtev za razmenom je prihvacen!\n")) {
                        parent.clearTaReceivedMessages();
                        if (matchingCards.isEmpty())
                            parent.setTaReceivedMessages("Korisnik " + reqUser + " nema slicice koje tebi fale.");
                        else
                            parent.setTaReceivedMessages("Korisnik " + reqUser + " ima za tebe slicice: " + matchingCards.stream().map(Object::toString).collect(Collectors.joining(", ")));
                    }
                    
                } else if (line.startsWith("UsersUpdated")) {       // odgovor na request od "Moguce razmene" dugmeta
                    
                    String[] users = line.split(":")[1].split(";");
                    parent.getTradingClients().clear();
                    
                    for (String user : users) {
                        if (!user.equals(parent.getUserName())) {
                            
                            String[] info = user.split(" ");
                            String ime = info[0];
                            String[] userDuplicates = info[1].split(",");
                            String[] userMissingCards = info[2].split(",");

                            
                            MenjazaKlijent ct = new MenjazaKlijent();
                            ct.setUserName(ime);
                            ct.setDuplicates(userDuplicates);
                            ct.setMissingCards(userMissingCards);
                            parent.addTradingClient(ct);
                        }
                    }
                  
                    String reqUser = parent.getRequestedUser();
                    ArrayList<MenjazaKlijent> traders = parent.getTradingClients();
  
                    String printInfo;
                    String forMe = new String();
                    String forReqUser = new String();
                    
                    for (MenjazaKlijent ct : traders) {
                        if (ct.getUserName().equals(reqUser)) {
                            
                            for (String s : ct.getDuplicates()) {
                                if (parent.getMissingCards().containsKey(s)) {
                                    forMe += s + ",";
                                }
                            }
                            if (forMe.endsWith(",")) {
                                forMe = forMe.substring(0, forMe.length() - 1);
                                ArrayList<Integer> forMeNums = cardsStringToCardsNums(forMe.split(","));
                                
                                printInfo = "Korisnik " + reqUser + " ima za tebe slicice: " + forMeNums.stream().map(Object::toString).collect(Collectors.joining(", ")) + "\n"; //magic found on StackOverflow;
                                
                            } else {
                                printInfo = "Korisnik " + reqUser + " nema slicice koje tebi fale.\n";
                            }
                            
                            for (String s : ct.getMissingCards()) {
                                if (parent.getDuplicates().containsKey(s)) {
                                    forReqUser += s + ",";
                                }
                            }
                            if (forReqUser.endsWith(",")) {
                                forReqUser = forReqUser.substring(0, forReqUser.length() - 1);
                                ArrayList<Integer> forReqUserNums = cardsStringToCardsNums(forReqUser.split(","));
                                
                                printInfo += "Ti za korisnika " + reqUser + " imas slicice: " + forReqUserNums.stream().map(Object::toString).collect(Collectors.joining(", ")); //magic found on StackOverflow;
                                
                            } else {
                                printInfo = "Ti nemas slicice koje fale korisniku " + reqUser;
                            }
                            
                            parent.clearTaReceivedMessages();
                            parent.setTaReceivedMessages(printInfo);
                        }                       
                                              
                        
                    }
                    
                } else if (line.startsWith("ExchangeRequest:")) {   // prosledjen zahtev za razmenu od servera
                    String[] receivedInfo = line.split(":");        // format je <ExchangeRequest:sender:senderDuplicates:requestedCards>
                    parent.setSender(receivedInfo[1]);
                    parent.setSenderDuplicates(receivedInfo[2].split(","));
                    parent.setRequestedCards(receivedInfo[3].split(","));
                    
                    ArrayList<Integer> senderDupsNums = cardsStringToCardsNums(receivedInfo[2].split(","));
                    ArrayList<Integer> reqCardsNums = cardsStringToCardsNums(receivedInfo[3].split(","));
                    
                    parent.setTaReceivedMessages("\nKorisnik " + parent.getSender() + " zeli tvoje slicice: " 
                                + reqCardsNums.stream().map(Object::toString).collect(Collectors.joining(", "))
                                + "\nSlicice koje ti on/ona nudi su: " + senderDupsNums.stream().map(Object::toString).collect(Collectors.joining(", ")));
                 
                } else if (line.startsWith("ExchangeAccepted:")) {      // povratna informacija da je zahtev za razmenu prihvacen
                    String[] receivedInfo = line.split(":");
                    String dups = receivedInfo[1];
                    String missings = receivedInfo[2];
                    
                    System.out.println(line);
                    
                    for (String s : dups.split(",")) {
                        s = s.trim();   // just in case 
                        if (parent.getDuplicates().containsKey(s)) {
                            parent.removeDuplicateCard(s);
                        }
                    }
                    for (String s : missings.split(",")) {
                        s = s.trim();   // just in case 
                        if (parent.getMissingCards().containsKey(s)) {
                            parent.removeMissingCard(s);
                        }
                    }
                    parent.showCards();
                    parent.clearTaReceivedMessages();
                    parent.setTaReceivedMessages("Zahtev za razmenom je prihvacen!");
                    
                } else if (line.startsWith("ExchangeDeclined:")) {      // povratna informacija da je zahtev za razmenu odbijen
                    String[] receivedInfo = line.split(":");
                    
                    parent.clearTaReceivedMessages();
                    parent.setTaReceivedMessages("Korisnik " + receivedInfo[1] + " je odbio tvoj zahtev za razmenu slicica!");
                    
                } 
                
            } catch (IOException ex) {
                Logger.getLogger(ReceiveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
     ArrayList<Integer> cardsStringToCardsNums(String[] s) {
        ArrayList<Integer> cardsNums = new ArrayList<>();
        
        String strPattern = "\\d+";
        Pattern pattern = Pattern.compile(strPattern);                    
        for (String requestedCard : s) {
            Matcher matcher = pattern.matcher(requestedCard);
            if (matcher.find())
                cardsNums.add(Integer.parseInt(matcher.group()));
        }
        Collections.sort(cardsNums);
        
        return cardsNums;
    }
}
