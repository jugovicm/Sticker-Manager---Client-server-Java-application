/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package albumserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author eminovicm
 */
class ConnectedClient implements Runnable {

    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String userName;
    private ArrayList<String> dups;
    private ArrayList<String> missings;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedClient> allClients;

    //getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<String> getDups() {
        return dups;
    }

    public void setDups(String[] list) {
        this.dups.clear();
        for (String s : list) {
            this.dups.add(s);
        }
    }

    public ArrayList<String> getMissings() {
        return missings;
    }

    public void setMissings(String[] list) {
        this.missings.clear();
        for (String s : list) {
            this.missings.add(s);
        }
    }

    //Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectedClient(Socket socket, ArrayList<ConnectedClient> allClients) {
        this.socket = socket;
        this.allClients = allClients;
        dups = new ArrayList<String>();
        missings = new ArrayList<String>();

        //iz socket-a preuzmi InputStream i OutputStream
        try {
            //posto se salje tekst, napravi BufferedReader i PrintWriter
            //kojim ce se lakse primati/slati poruke (bolje nego da koristimo Input/Output stream
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            //zasad ne znamo user name povezanog klijenta
            this.userName = "";
        } catch (IOException ex) {
            Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda prolazi i pravi poruku sa trenutno povezanik korisnicima u formatu
     * Users: Prvi Drugi Treci ... kada se napravi poruka tog formata, ona se
     * salje svim povezanim korisnicima
     */
    void connectedClientsUpdateStatus() {
        //priprema string sa trenutno povezanim korisnicima u formatu 
        //Users:User1 dup1,dup2,... mis1,mis2,..;
        //User2 dup1,dup2,... mis1,mis2,...;
        //i salje svim korisnicima koji se trenutno nalaze u Menjazi
        String connectedUsers = "Users:";
        for (ConnectedClient c : this.allClients) {
            connectedUsers += c.getUserName() + " ";
            for (String s : c.getDups()) {
                connectedUsers += s + ",";
            }
            connectedUsers = connectedUsers.substring(0, connectedUsers.length() - 1);
            connectedUsers += " ";
            for (String s : c.getMissings()) {
                connectedUsers += s + ",";
            }
            connectedUsers = connectedUsers.substring(0, connectedUsers.length() - 1);
            // zbog poslednjeg space
            connectedUsers += ";";
        }

        //prodji kroz sve klijente i svakom posalji info o novom stanju u sobi
        for (ConnectedClient svimaUpdateCB : this.allClients) {
            svimaUpdateCB.pw.println(connectedUsers);
        }

    }

    void clientUpdateStatus(String receiver) {

        String connectedUsers = "UsersUpdated:";
        for (ConnectedClient c : this.allClients) {
            connectedUsers += c.getUserName() + " ";
            for (String s : c.getDups()) {
                connectedUsers += s + ",";
            }
            connectedUsers = connectedUsers.substring(0, connectedUsers.length() - 1);
            connectedUsers += " ";
            for (String s : c.getMissings()) {
                connectedUsers += s + ",";
            }
            connectedUsers = connectedUsers.substring(0, connectedUsers.length() - 1);
            connectedUsers += ";";
        }

        //prodji kroz sve klijente i svakom posalji info o novom stanju u sobi
        for (ConnectedClient cc : this.allClients) {
            if (cc.getUserName().equals(receiver)) {
                cc.pw.println(connectedUsers);
            }
        }

    }

    @Override
    public void run() {
        //Server prima od svakog korisnika najpre njegovo korisnicko ime
        //a kasnije poruke koje on salje ostalim korisnicima 
        while (true) {
            try {
                //ako nije poslato ime, najpre cekamo na njega
                if (this.userName.equals("")) {
                    String line = this.br.readLine();
                    String[] startInfo = line.split(":");
                    String[] dupsArray = startInfo[1].split(",");
                    String[] missArray = startInfo[2].split(",");

                    this.userName = startInfo[0].trim();

                    for (String s : dupsArray) {
                        this.dups.add(s);
                    }
                    for (String s : missArray) {
                        this.missings.add(s);
                    }

                    if (this.userName != null) {
                        System.out.println("Connected user: " + this.userName);
                        //informisi svim povezanim klijentima
                        connectedClientsUpdateStatus();
                    } else {
                        //ako je userName null to znaci da je terminiran klijent thread
                        Iterator<ConnectedClient> it = this.allClients.iterator();
                        while (it.hasNext()) {
                            if (it.next().getUserName().equals(this.userName)) {
                                it.remove();
                            }
                        }
                        connectedClientsUpdateStatus();
                        this.socket.close();
                        break;
                    }
                    ////////CEKAMO PORUKU/////////
                } else {

                    System.out.println("Cekam poruku.");
                    String line = this.br.readLine();
                    System.out.println(line);
                    System.out.println("Poruka je stigla.");

                    if (line != null) {
                        if (line.startsWith("Request:")) {  // request koji se salje od ComboBox-a
                            ConnectedClient reqU = null;
                            ConnectedClient sendU = null;
                            String[] s = line.split(":");
                            String sender = s[1];
                            String requestedUser = s[2];

                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(requestedUser)) {
                                    reqU = clnt;

                                } else if (clnt.getUserName().equals(sender)) {
                                    sendU = clnt;

                                } else {
                                    //ispisi da je korisnik kome je namenjena poruka odsutan
                                    if (requestedUser.equals("")) {
                                        this.pw.println("Korisnik " + requestedUser + " je odsutan!");
                                        return;
                                    }
                                }
                            }
                            sendU.pw.println("cmbRequest:" + requestedUser + ":" + reqU.getDups().stream().map(Object::toString).collect(Collectors.joining(",")));

                        } else if (line.startsWith("RequestUsers:")) {     // request koji se dobija od dugmeta "Moguce razmene"
                            String sender = line.split(":")[1];
                            clientUpdateStatus(sender);

                        } else if (line.startsWith("RequestExchange:")) {  // request koji se dobija od dugmeta "Posalji zahtev"
                            String[] receivedInfo = line.split(":");
                            String sender = receivedInfo[1];
                            String receiver = receivedInfo[2];
                            String senderDuplicates = receivedInfo[3];
                            String reqCards = receivedInfo[4];

                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(receiver)) {
                                    clnt.pw.println("ExchangeRequest:" + sender + ":" + senderDuplicates + ":" + reqCards);
                                }
                            }

                        } else if (line.startsWith("ExchangeAccepted:")) {  // poruka koja se dobija prihvatanjem zahteva za razmenu
                            String[] receivedInfo = line.split(":");
                            String exchangeReceiver = receivedInfo[1];
                            String exchangeInitiator = receivedInfo[2];
                            String initiatorDups = receivedInfo[3];
                            String initiatorMissingCards = receivedInfo[4];

                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(exchangeReceiver)) {
                                    for (String s : initiatorDups.split(",")) {
                                        s = s.trim();
                                        clnt.missings.remove(s);
                                    }

                                    for (String s : initiatorMissingCards.split(",")) {
                                        s = s.trim();
                                        clnt.dups.remove(s);
                                    }
                                } else if (clnt.getUserName().equals(exchangeInitiator)) {
                                    for (String s : initiatorDups.split(",")) {
                                        s = s.trim();
                                        clnt.dups.remove(s);
                                    }

                                    for (String s : initiatorMissingCards.split(",")) {
                                        s = s.trim();
                                        clnt.missings.remove(s);
                                    }

                                    clnt.pw.println("ExchangeAccepted:" + initiatorDups + ":" + initiatorMissingCards);
                                }
                            }
                            connectedClientsUpdateStatus();

                        } else if (line.startsWith("ExchangeDeclined:")) {     // poruka koja se dobija odbijanjem zahteva za razmenu
                            String[] receivedInfo = line.split(":");
                            String exchangeReceiver = receivedInfo[1];
                            String exchangeInitiator = receivedInfo[2];

                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(exchangeInitiator)) {
                                    clnt.pw.println("ExchangeDeclined:" + exchangeReceiver + ":" + exchangeInitiator);
                                }
                            }

                        } else if (line.startsWith("UpdateAfterErase:")) {     // poruka koja se dobija brisanjem slicica na dugme "Obrisi"
                            String[] receivedInfo = line.split(":");
                            String user = receivedInfo[1];
                            String duplicates = receivedInfo[2];
                            String miss = receivedInfo[3];

                            for (ConnectedClient clnt : this.allClients) {
                                if (clnt.getUserName().equals(user)) {
                                    clnt.setDups(duplicates.split(","));
                                    clnt.setMissings(miss.split(","));
                                    connectedClientsUpdateStatus();
                                }
                            }

                        }

                    } else {
                        //slicno kao gore, ako je line null, klijent se diskonektovao
                        //ukloni tog korisnika iz liste povezanih korisnika u chat room-u
                        //i obavesti ostale da je korisnik napustio sobu
                        System.out.println("Disconnected user: " + this.userName);

                        Iterator<ConnectedClient> it = this.allClients.iterator();
                        while (it.hasNext()) {
                            if (it.next().getUserName().equals(this.userName)) {
                                it.remove();
                            }
                        }
                        connectedClientsUpdateStatus();

                        this.socket.close();
                        break;
                    }

                }
            } catch (IOException ex) {
                System.out.println("Disconnected user: " + this.userName);
                //npr, ovakvo uklanjanje moze dovesti do izuzetka, pogledajte kako je 
                //to gore uradjeno sa iteratorom
                for (ConnectedClient cl : this.allClients) {
                    if (cl.getUserName().equals(this.userName)) {
                        this.allClients.remove(cl);
                        connectedClientsUpdateStatus();
                        return;
                    }
                }

            }

        }
    }

}
