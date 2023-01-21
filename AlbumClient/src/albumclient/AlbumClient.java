/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package albumclient;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.*;

        
/**
 *
 * @author eminovicm
 */
public class AlbumClient extends javax.swing.JFrame {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private ReceiveMessageFromServer rmfs;
    private HashMap<String, javax.swing.JCheckBox> duplicates;
    private HashMap<String, javax.swing.JCheckBox> missingCards;
    private ArrayList<MenjazaKlijent> tradingClients;
    String requestedUser;                   // predstavlja korisnika kojeg smo selektovali u ComboBox-u
    String sender;                          // predstavlja korisnika koji nam je poslao zahtev za razmenu
    ArrayList<String> requestedCards;       // predstavlja slicice koje "sender" zeli
    ArrayList<String> senderDuplicates;     // predstavlja slicice koje "sender" nudi       
    private Random rnd;
    /*
     * nizovi za cuvanje brojeva random generisanih slicica
     * koriste se samo zbog sortiranog prikazivanja CheckBox-eva na GUI-u
     */
    private ArrayList<Integer> randDuplicatesNums;      
    private ArrayList<Integer> randMissingNums;
    
    /**
     * Creates new form CardsTradingClient
     */
    public AlbumClient() {
        duplicates = new HashMap<String, javax.swing.JCheckBox>();
        missingCards = new HashMap<String, javax.swing.JCheckBox>();
        tradingClients = new ArrayList<>();
        requestedUser = new String();
        sender = new String();
        requestedCards = new ArrayList<>();
        senderDuplicates = new ArrayList<>();
        rnd = new Random();
        initComponents();
        initCheckBoxes();
    }
    /**
     * Creates new form AlbumClient
     */
    public String getUserName() {
        return this.tfUsername.getText();
    }
   
    public BufferedReader getBr() {
        return br;
    }
    
    public Socket getSoc() {
        return socket;
    }
    
    public JComboBox<String> getCbUsers() {
        return cmbUsers;
    }
    
    public void setTaReceivedMessages(String poruka) {
        taInfo.append(poruka + "\n");
    }
    
    public void clearTaReceivedMessages() {
        taInfo.setText("");
    }
    
    public String getTaReceivedMessages() {
        return taInfo.getText();
    }
    
     public HashMap<String, javax.swing.JCheckBox> getDuplicates() {
        return duplicates;
    }
    
    public HashMap<String, javax.swing.JCheckBox> getMissingCards() {
        return missingCards;
    }
    
    private void initCheckBoxes() {
        
        initRandDuplicatesNums();
        
        for (int i = 0; i < randDuplicatesNums.size(); i++) {
            javax.swing.JCheckBox cb = new javax.swing.JCheckBox(randDuplicatesNums.get(i) + "");
        //    cb.setBounds(offsetX + (i*50)%(9*50), offsetY + (i/9)*25, 40, 20);
            
            duplicates.put("cb" + randDuplicatesNums.get(i), cb);
        }
        
        initRandMissingCards();
        
        for (int i = 0; i < randMissingNums.size(); i++) {
            javax.swing.JCheckBox cb = new javax.swing.JCheckBox(randMissingNums.get(i) + "");
        //    cb.setBounds(offsetX + (i*50)%(9*50), offsetY + (i/9)*25, 40, 20);
            
            missingCards.put("cb" + randMissingNums.get(i), cb);
        }
    }
    private void initRandDuplicatesNums() {
        int dupNum = 1 + rnd.nextInt(97 + 1);       // do 98 duplikata jer mora da fali bar 1 slicica
        Set<Integer> rands = new LinkedHashSet<Integer>();
        
        while (rands.size() < dupNum) {
            int dupRnd = 1 + rnd.nextInt(99);
            rands.add(dupRnd);
        }
        randDuplicatesNums = new ArrayList<>(rands);
        Collections.sort(randDuplicatesNums);
    }
    private void initRandMissingCards() {
        int missNum = 1 + rnd.nextInt(duplicates.size() + 1);   // 1+ jer mora bar jedna da fali
        Set<Integer> rands = new LinkedHashSet<Integer>();
        
        while (rands.size() < missNum) {
            int missRnd = 1 + rnd.nextInt(99);
            if (!duplicates.containsKey("cb"+missRnd))
                rands.add(missRnd);
        }
        randMissingNums = new ArrayList<>(rands);
        Collections.sort(randMissingNums);
    }
    
    public void removeDuplicateCard(String s) {
        if (this.duplicates.containsKey(s))
            this.duplicates.remove(s);
    }
    
    public void removeMissingCard(String s) {
        if (this.missingCards.containsKey(s))
            this.missingCards.remove(s);
    }
    
    public void addTradingClient(MenjazaKlijent mk) {
        this.tradingClients.add(mk);
    }
    
    public ArrayList<MenjazaKlijent> getTradingClients() {
        return this.tradingClients;
    }
    
     public String getRequestedUser() {
        String[] comboBoxText = this.cmbUsers.getSelectedItem().toString().split("\\(");
        String reqUser = comboBoxText[0].trim();
        
        return reqUser;
    }
    
    public void setSender(String s) {
        this.sender = s;
    }
    
    public String getSender() {
        return this.sender;
    }
    
    public void setRequestedCards(String[] list) {
        this.requestedCards.clear();
        for (String s : list) {
            this.requestedCards.add(s);
        }
    }
    
    public ArrayList<String> getRequestedCards() {
        return this.requestedCards;
    }
    
    public void setSenderDuplicates(String[] list) {
        this.senderDuplicates.clear();
        for (String s : list) {
            this.senderDuplicates.add(s);
        }
    }
    
    public ArrayList<String> getSenderDuplicates() {
        return this.senderDuplicates;
    }
   
    
    public void showCards() {
        pnlDuplicates.removeAll();
        pnlMissing.removeAll();
       
        
        int offsetX = 15, offsetY = 25;
        ArrayList<Integer> dupKeys = new ArrayList<Integer>();
        ArrayList<Integer> missKeys = new ArrayList<Integer>();
        String regexPattern = "\\d+";
        Pattern pat = Pattern.compile(regexPattern);
        
        for (String s : duplicates.keySet()) {
            Matcher mat = pat.matcher(s);
            if (mat.find()) {
                dupKeys.add(Integer.parseInt(mat.group())); 
            }
        }
        Collections.sort(dupKeys);
        for (String s : missingCards.keySet()) {
            Matcher mat = pat.matcher(s);
            if (mat.find()) {
                missKeys.add(Integer.parseInt(mat.group())); 
            }
        }
        Collections.sort(missKeys);
        
        for (int i = 0; i < dupKeys.size(); i++) {
            duplicates.get("cb"+dupKeys.get(i)).setBounds(offsetX + (i*50)%(9*50), offsetY + (i/9)*25, 40, 20);
            pnlDuplicates.add(duplicates.get("cb"+dupKeys.get(i)));
        }
        for (int i = 0; i < missKeys.size(); i++) {
            missingCards.get("cb"+missKeys.get(i)).setBounds(offsetX + (i*50)%(9*50), offsetY + (i/9)*25, 40, 20);
            pnlMissing.add(missingCards.get("cb"+missKeys.get(i)));
        }
        pnlDuplicates.revalidate();
        pnlDuplicates.repaint();
        pnlMissing.revalidate();
        pnlMissing.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlDuplicates = new javax.swing.JPanel();
        pnlMissing = new javax.swing.JPanel();
        tfUsername = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        lbUsername = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taInfo = new javax.swing.JTextArea();
        cmbUsers = new javax.swing.JComboBox<>();
        btnTradeOptions = new javax.swing.JButton();
        btnRequest = new javax.swing.JButton();
        btnAccept = new javax.swing.JButton();
        btnDecline = new javax.swing.JButton();
        btnErase = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlDuplicates.setBorder(javax.swing.BorderFactory.createTitledBorder("Duplikati"));

        javax.swing.GroupLayout pnlDuplicatesLayout = new javax.swing.GroupLayout(pnlDuplicates);
        pnlDuplicates.setLayout(pnlDuplicatesLayout);
        pnlDuplicatesLayout.setHorizontalGroup(
            pnlDuplicatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 378, Short.MAX_VALUE)
        );
        pnlDuplicatesLayout.setVerticalGroup(
            pnlDuplicatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlMissing.setBorder(javax.swing.BorderFactory.createTitledBorder("Slicice koje nedostaju"));

        javax.swing.GroupLayout pnlMissingLayout = new javax.swing.GroupLayout(pnlMissing);
        pnlMissing.setLayout(pnlMissingLayout);
        pnlMissingLayout.setHorizontalGroup(
            pnlMissingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        pnlMissingLayout.setVerticalGroup(
            pnlMissingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 311, Short.MAX_VALUE)
        );

        tfUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUsernameActionPerformed(evt);
            }
        });

        btnConnect.setText("Konektuj se");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        lbUsername.setText("Korisnik:");

        taInfo.setColumns(20);
        taInfo.setRows(5);
        jScrollPane1.setViewportView(taInfo);

        btnTradeOptions.setText("Moguce razmene");
        btnTradeOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTradeOptionsActionPerformed(evt);
            }
        });

        btnRequest.setText("Posalji zahtev");
        btnRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRequestActionPerformed(evt);
            }
        });

        btnAccept.setText("Prihvati zahtev");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
            }
        });

        btnDecline.setText("Odbij zahtev");
        btnDecline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeclineActionPerformed(evt);
            }
        });

        btnErase.setText("Obrisi");
        btnErase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEraseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(pnlDuplicates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(btnErase)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnConnect))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cmbUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnTradeOptions)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRequest)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnAccept)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDecline, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                            .addComponent(jScrollPane1))))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlMissing, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlDuplicates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnErase)
                        .addGap(155, 155, 155)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbUsername))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConnect))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTradeOptions)
                    .addComponent(btnRequest)
                    .addComponent(btnAccept)
                    .addComponent(btnDecline))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        // TODO add your handling code here:
        lbUsername.setForeground(Color.black);

        if (this.tfUsername.getText().equals(""))
            lbUsername.setForeground(Color.red);
        else
        {
            try {
                //Kreiraj novi socket (ako nije localhost, treba promeniti IP adresu)
                this.socket = new Socket("127.0.0.1", 6001);
                //napravi BufferedReader i PrintWriter kako bi slao i primao poruke
                this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
                //za prijem poruka od servera (stizace asinhrono) koristi poseban thread
                //da bismo u novom thread-u mogli da menjamo sadrzaj komponenti (npr Combo Box-a)
                //konstruktoru novog thread-a se prosledjuje this
                this.rmfs = new ReceiveMessageFromServer(this);
                Thread thr = new Thread(rmfs);
                thr.start();

                String startInfo = tfUsername.getText() + ":";
                for (String s : duplicates.keySet()) {
                    startInfo += s + ',';
                }
                startInfo = startInfo.substring(0, startInfo.length() - 1);
                startInfo += ':';
                for (String s : missingCards.keySet()) {
                    startInfo += s + ',';
                }
                startInfo = startInfo.substring(0, startInfo.length() - 1);
                
                this.pw.println(startInfo);

                //tbInterface.setEnabled(true);
                pnlDuplicates.setEnabled(true);
                pnlMissing.setEnabled(true);
                //pnlAlbum.setEnabled(true);
   
                
                lbUsername.setEnabled(false);
                
                tfUsername.setEnabled(false);
                btnConnect.setEnabled(false);
                taInfo.setEnabled(true);
                cmbUsers.setEnabled(true);
                btnTradeOptions.setEnabled(true);
                btnRequest.setEnabled(true);
                btnAccept.setEnabled(true);
                btnDecline.setEnabled(true);
                //btnErase.setEnabled(true);*/
                

               showCards();
            } catch (IOException ex) {
                Logger.getLogger(AlbumClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRequestActionPerformed
        // TODO add your handling code here:
                if (this.cmbUsers.getSelectedItem() != null) {
            String[] comboBoxTxt = this.cmbUsers.getSelectedItem().toString().split("\\(");
            String selectedMissingCards = new String();
            String selectedDuplicates = new String();
            int selMissNum = 0;
            int selDupsNum = 0;
            boolean reqError = false;

            String receiverName = comboBoxTxt[0].trim();
            MenjazaKlijent receiver = new MenjazaKlijent();
            for (MenjazaKlijent mk : this.tradingClients) {
                if (mk.getUserName().equals(receiverName))
                    receiver = mk;
            }
            
            for (String s : this.duplicates.keySet()) {
                if (duplicates.get(s).isSelected()) {
                    selectedDuplicates += s + ",";
                    selDupsNum++;
                }
            }
            for (String s : this.missingCards.keySet()) {
                if (missingCards.get(s).isSelected()) {
                    selectedMissingCards += s + ",";
                    selMissNum++;
                }
            }
            if (selMissNum == 0 || selDupsNum == 0) {
                this.clearTaReceivedMessages();
                this.setTaReceivedMessages("Moras selektovati slicice za razmenu!");
                reqError = true;
               
            } else if (selDupsNum >= selMissNum) {
                selectedMissingCards = selectedMissingCards.substring(0, selectedMissingCards.length() - 1); // removing the last comma
                selectedDuplicates = selectedDuplicates.substring(0, selectedDuplicates.length() - 1); // removing the last comma

                for (String s : selectedMissingCards.split(",")) {
                    if (!receiver.getDuplicates().contains(s)) {
                        this.clearTaReceivedMessages();
                        this.setTaReceivedMessages("Korisnik " + receiverName + " nema neku od slicica koje zahtevas!");
                        reqError = true;
                    }
                }
                
                for (String s : selectedDuplicates.split(",")) {
                    if (!receiver.getMissingCards().contains(s)) {
                        this.clearTaReceivedMessages();
                        this.setTaReceivedMessages("Neke od selektovanih duplikata korisnik " + receiverName + " vec ima!");
                        reqError = true;
                    }
                }
                
            } else {
                this.clearTaReceivedMessages();
                this.setTaReceivedMessages("Broj slicica koje trazis je veci od broja slicica koje nudis.");
                reqError = true;
            }

            if (reqError) {
                System.out.println("nije ok");
            } else {
                System.out.println("ok je");
                this.setTaReceivedMessages("Zahtev je poslat korisniku " + receiverName);
                String msgToBeSend = "RequestExchange:" + this.getUserName() + ":" + receiverName + ":" + selectedDuplicates + ":" + selectedMissingCards;
                // format je <RequestEchange:Posiljalac:Primalac:duplikati:sliciceKojeFale>
                System.out.println(msgToBeSend);
                this.pw.println(msgToBeSend);
            }
            
        } else {
            this.clearTaReceivedMessages();
            this.setTaReceivedMessages("Odaberi korisnika sa kojim zelis da menjas slicice!");
        }
    }//GEN-LAST:event_btnRequestActionPerformed

    private void tfUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUsernameActionPerformed
        // TODO add your handling code here:
        if (this.cmbUsers.getItemCount() > 0) {        
            String msgToBeSend = "Request:" + this.getUserName() + ":" + this.getRequestedUser();
            this.pw.println(msgToBeSend);
        }
    }//GEN-LAST:event_tfUsernameActionPerformed

    private void btnTradeOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTradeOptionsActionPerformed
        // TODO add your handling code here:
        if (this.cmbUsers.getSelectedItem() != null) 
            this.pw.println("RequestUsers:" + this.getUserName());
        else {
            this.clearTaReceivedMessages();
            this.setTaReceivedMessages("Odaberi korisnika sa kojim zelis da menjas slicice!");
        }
    }//GEN-LAST:event_btnTradeOptionsActionPerformed

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed
        // TODO add your handling code here:
         if (!this.sender.equals("")) {
            for (String s : this.senderDuplicates) {
                s = s.trim();
                if (this.missingCards.containsKey(s)) {
                    this.missingCards.remove(s);
                }
            }        
            for (String s : this.requestedCards) {
                s = s.trim();
                if (this.duplicates.containsKey(s)) {
                    this.duplicates.remove(s);
                }
            } 

            this.showCards();
            this.clearTaReceivedMessages();
            this.setTaReceivedMessages("Razmena je izvrsena!");
            this.pw.println("ExchangeAccepted:" + this.getUserName() + ":" + this.getSender() + ":" 
                    + this.getSenderDuplicates().stream().map(Object::toString).collect(Collectors.joining(", ")) + ":"
                    + this.getRequestedCards().stream().map(Object::toString).collect(Collectors.joining(", ")));
            // format je <ExchangeAccepted:primalacZahteva:posiljalacZahteva:duplikatiPosiljaoca:sliciceKojeMuFale>
            this.sender = "";
            this.requestedCards.clear();
            this.senderDuplicates.clear();
        }
    }//GEN-LAST:event_btnAcceptActionPerformed

    private void btnDeclineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeclineActionPerformed
        // TODO add your handling code here:
        String sndr = this.sender;
        this.requestedCards.clear();
        this.senderDuplicates.clear();
        this.sender = "";
        this.pw.println("ExchangeDeclined:" + this.getUserName() + ":" + sndr);
    }//GEN-LAST:event_btnDeclineActionPerformed

    private void btnEraseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEraseActionPerformed
        String dups = new String();
        String miss = new String();
        String selMiss = new String();
        String selDups = new String();
        int selDupsNum = 0;
        int selMissNum = 0;
        
        for (String s : this.duplicates.keySet()) {
            if (this.duplicates.get(s).isSelected()) {
                selDups += s + ",";
                selDupsNum++;
            } else {
                dups += s + ",";
            }
        }
        for (String s : this.missingCards.keySet()) {
            if (this.missingCards.get(s).isSelected()) {
                selMiss += s + ",";
                selMissNum++;
            } else {
                miss += s + ",";
            }
        }
        
        if (selDupsNum > 0 && selMissNum > 0) {
            dups = dups.substring(0, dups.length() - 1);
            miss = miss.substring(0, miss.length() - 1);
            selDups = selDups.substring(0, selDups.length() - 1);
            selMiss = selMiss.substring(0, selMiss.length() - 1);
            
            for (String s : selDups.split(",")) {
                if (this.duplicates.containsKey(s)) {
                    this.duplicates.remove(s);
                }
            }
            for (String s : selMiss.split(",")) {
                if (this.missingCards.containsKey(s)) {
                    this.missingCards.remove(s);
                }
            }
            
            this.showCards();
            String msgToBeSend = "UpdateAfterErase:" + this.getUserName() + ":" + dups + ":" + miss;
            this.pw.println(msgToBeSend);
            // format je <UpdateAfterErase:posiljalacPoruke:njegoviDuplikati:njegoveSliciceKojeFale>
        } else {
            this.clearTaReceivedMessages();
            this.setTaReceivedMessages("Moras selektovati bar jedan duplikat i bar jednu slicicu koja ti fali!");
        }
    }//GEN-LAST:event_btnEraseActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AlbumClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AlbumClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AlbumClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AlbumClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AlbumClient().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDecline;
    private javax.swing.JButton btnErase;
    private javax.swing.JButton btnRequest;
    private javax.swing.JButton btnTradeOptions;
    private javax.swing.JComboBox<String> cmbUsers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbUsername;
    private javax.swing.JPanel pnlDuplicates;
    private javax.swing.JPanel pnlMissing;
    private javax.swing.JTextArea taInfo;
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
}
