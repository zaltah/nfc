package com.ticketapp.auth.ticket;

import com.ticketapp.auth.R;
import com.ticketapp.auth.app.main.TicketActivity;
import com.ticketapp.auth.app.ulctools.Commands;
import com.ticketapp.auth.app.ulctools.Utilities;

import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Arrays;
/**
 * TODO:
 * Complete the implementation of this class. Most of the code are already implemented. You will
 * need to change the keys, design and implement functions to issue and validate tickets. Keep your
 * code readable and write clarifying comments when necessary.
 */
public class Ticket {

    /** Default keys are stored in res/values/secrets.xml **/
    private static final byte[] defaultAuthenticationKey = TicketActivity.outer.getString(R.string.default_auth_key).getBytes();
    private static final byte[] defaultHMACKey = TicketActivity.outer.getString(R.string.default_hmac_key).getBytes();

    /** TODO: Change these according to your design. Diversify the keys. */
    private static final byte[] authenticationKey = defaultAuthenticationKey; // 16-byte key
    private static final byte[] hmacKey = defaultHMACKey; // 16-byte key

    public static byte[] data = new byte[192];

    private static TicketMac macAlgorithm; // For computing HMAC over ticket data, as needed
    private static Utilities utils;
    private static Commands ul;

    private final Boolean isValid = false;
    private final int remainingUses = 0;
    private final int expiryTime = 0;

    private static String infoToShow = "-"; // Use this to show messages



    // Pages we want to write to


    private static int ticketPage = 4;
    private static int timePage = 7;  //will take this +1 page after
    private static int keyPage = 44;
    //private static int versionPage = ???;
    //private static int tagPage = ???;

    private static int maximumTickets = 100;

    private static int howLongTicketLasts = 0; // in days

    private static String secretMessage = "aaaabbbbccccddddd";
    private static String defaultKey = "BREAKMEIFYOUCAN!";

    /** Create a new ticket */
    public Ticket() throws GeneralSecurityException {
        // Set HMAC key for the ticket
        macAlgorithm = new TicketMac();
        macAlgorithm.setKey(hmacKey);

        ul = new Commands();
        utils = new Utilities(ul);
    }

    /** After validation, get ticket status: was it valid or not? */
    public boolean isValid() {
        return isValid;
    }

    /** After validation, get the number of remaining uses */
    public int getRemainingUses() {
        return remainingUses;
    }

    /** After validation, get the expiry time */
    public int getExpiryTime() {
        return expiryTime;
    }

    /** After validation/issuing, get information */
    public static String getInfoToShow() {
        return infoToShow;
    }

    /**
     * Issue new tickets
     *
     * TODO: IMPLEMENT
     */


    //can be scaled back to just being year, month, day, hour
    private byte[] getDate(){
        byte[] date = new byte[8];
        utils.readPages(timePage,2,date,0);
        return date;
    }

    private boolean checkNoExpiryDate(byte[] date){
        byte[] noDate = new byte[8];
        //array of -1s 
        Arrays.fill(noDate, (byte) -1);
        return Arrays.equals(noDate, date);
    }

    private int remainingTickets(){
        byte[] ticketCount = new byte[4];
        utils.readPages(ticketPage,1,ticketCount,0);
        return byteToInt(ticketCount);
    }

    private void addTicketsToExisting(){
        int ticketsToAdd = remainingTickets() + 5;
        if(ticketsToAdd >= maximumTickets +5){
            infoToShow += "You have reached the maximum number of tickets";
        } else{
            writeTickets(ticketsToAdd);
        }
        infoToShow += "current tickets:" + remainingTickets();
    }

    private byte[] intToByte(int number){
        byte[] intAsByteArray = {(byte)(number & 0xff),(byte)((number >>8) & 0xff), (byte) (number >> 16), (byte) (number >> 24)};
        return intAsByteArray;
    }


    private int byteToInt(byte[] bytes){
        int number = bytes[0] & 0xFF;
        number |= (bytes[1] & 0xFF) << 8;
        number |= (bytes[2] & 0xFF) << 16;
        number |= (bytes[3] & 0xFF) << 24;
        return number;
    }

    // variable setExpiryDate command
    /* 
    private void setExpiryDate(){
        byte[] date = new byte[8];
        //get current date
        Calendar calendar = Calendar.getInstance();

        //take each part of the date
        byte year = (byte) (calendar.get(Calendar.YEAR)-2000); // converting to 2 digit year
        byte month = (byte) calendar.get(Calendar.MONTH) +1; //must add 1 because calendar starts at 0
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byte minute = (byte) calendar.get(Calendar.MINUTE);
        byte second = (byte) calendar.get(Calendar.SECOND);

        //add to array
        date[0] = year;
        date[1] = month;
        date[2] = day;
        date[3] = hour;
        date[4] = minute;
        date[5] = second;

        //write to card
        utils.writePages(date,0,timePage,2);

    }
    */
    
    //expiryDate normalized to end of day
    private void setExpiryDate(){
        byte[] date = new byte[8];
        //get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, howLongTicketLasts);

        //calendar.add(Calendar.DAY_OF_MONTH, -5);

        //take each part of the date
        byte year = (byte) (calendar.get(Calendar.YEAR)-2000); // converting to 2 digit year
        byte month = (byte) (calendar.get(Calendar.MONTH) +1); //must add 1 because calendar starts at 0, mainly just for printing purposes
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte hour = 23;
        byte minute = 59;
        byte second = 59;

        //add to array
        date[0] = year;
        date[1] = month;
        date[2] = day;
        date[3] = hour;
        date[4] = minute;
        date[5] = second;

        //write to card
        utils.writePages(date,0,timePage,2);
    }


    private void setToNoExpiryDate(){
        byte[] date = new byte[8];
        //array of -1
        Arrays.fill(date, (byte) -1);
        utils.writePages(date,0,timePage,2);
    }


    private void writeTickets(int number) {
        byte[] tickets = intToByte(number);
        utils.writePages(tickets, 0, ticketPage,1);
    }
    
    private void formatTicket(){
        //check if first time formatting the card


        if(firstFormat()){
            infoToShow += "Default key detected, issuing secret key";
            //issueKey(genereateKey());
            //writeTag();
            //writeVersion();
            //issueMAC(genereateMAC());
        }
        //writeTag();
        //writeVersion();
        setToNoExpiryDate();
        writeTickets(5);
        //add stuff to counters ect
    }


    //TODO Change stuff to return true/false if they fail
    //implement methods:

    /*private byte[] generateKey(){
        byte[] key = new byte[16];

    }
    private void issueKey(){
        
    }

    private void generateMAC(){

    }
    private void issueMAC(){

    }
    private boolean checkMAC(){

    }
    private void writeTag(){

    }
    private void writeVersion(){

    }
    */

    //Generate transaction log, pass-back protection via notifier if card was just used by previous user
    //timer + timestamps for each transaction, if delay between last transaction and current transaction is too low do not decrease ticket count





    private boolean firstFormat(){
        byte[] data = new byte[40];
        utils.readPages(4,10,data,0);
        if(Arrays.equals(data, new byte[40])){
            return true;
        } else{
            return false;
        }
        
        
    }

    private void resetCard(){
        byte[] data = new byte[40];
        utils.writePages(data,0,4,10);
    }
    private void resetKey(){
        byte[] key = defaultKey.getBytes();
        utils.writePages(key,0,keyPage,4);
    }




    public boolean issue(int daysValid, int uses) throws GeneralSecurityException {
        boolean res;
        // Authenticate
        res = utils.authenticate(authenticationKey);
        if (!res) {
            Utilities.log("Authentication failed in issue()", true);
            infoToShow = "Authentication failed";
            return false;
        }
        infoToShow = "";
        // Example of writing:
        /*byte[] message = "info".getBytes();
        res = utils.writePages(message, 0, 6, 1);
        */

        //reset card


        //Check if no expiry date        
        if (checkNoExpiryDate(getDate())){
            infoToShow += "no expiry set increasing the number of tickets";
            addTicketsToExisting();
        }
        else{
            formatTicket();
            infoToShow += "Formatting the card, the number of tickets on card:" + remainingTickets() + " the expiry date was reset";
        }
        

        // Set information to show for the user
        /*
        if (res) {
            infoToShow = infoToShow;
        } else {
            infoToShow = "Failed to write";
        }
        */
        return true;
    }

    /**
     * Use ticket once
     *
     * TODO: IMPLEMENT
     */
    public boolean use() throws GeneralSecurityException {
        boolean res;
        // Authenticate
        res = utils.authenticate(authenticationKey);
        if (!res) {
            Utilities.log("Authentication failed in issue()", true);
            infoToShow = "Authentication failed";
            return false;
        }
        byte[] date = getDate();
        int remainingTickets = remainingTickets();


        //check if there are remaining tickets
        if(remainingTickets == 0){
            infoToShow = "No tickets remaining";
            return false;
        }
        
        //check if card has been activated
        if(checkNoExpiryDate(date)){
            infoToShow = "Activating the card";
            setExpiryDate();
            writeTickets(remainingTickets -1);
            int updatedRemainingTickets = remainingTickets();
            if(updatedRemainingTickets == remainingTickets){
                infoToShow = "Failed to write";
                return false;
            }
            else {
                infoToShow = "Ticket used, remaining tickets: " + updatedRemainingTickets + " expiry date set to:" + expiryDateToString();
                return true;
            }
        }

        //check if the ticket is expired
        if(checkExpiryDateValid(date)){
            infoToShow = "Card expired on" + expiryDateToString();
            return false;
        }
        else {
            writeTickets(remainingTickets - 1);
            int updatedRemainingTickets = remainingTickets();
            if(updatedRemainingTickets == remainingTickets){
                infoToShow = "Failed to write";
                return false;
            }
            else {
                infoToShow = "Ticket used, remaining tickets:" + updatedRemainingTickets;
                return true;
            }
        }
        
        
        


        // Example of reading:
        /*
        byte[] message = new byte[4];
        res = utils.readPages(6, 1, message, 0);
        */

        // Set information to show for the user
        /* 
        if (res) {
            infoToShow = "Read: " + new String(message);
        } else {
            infoToShow = "Failed to read";
        }
        */ 
    }

    public Boolean checkExpiryDateValid(byte[] date){
        Calendar calendar = Calendar.getInstance();
        //date to value
        // year, month, day, hour, minute, second
        int dateAsInt = date[0] * 365 + date[1] * 30 + date[2] * 24 + date[3] * 60 + date[4] * 60 + date[5];
        //calendar to int
        int calendarAsInt = (calendar.get(Calendar.YEAR)-2000) * 365 + (calendar.get(Calendar.MONTH)+1) * 30 + calendar.get(Calendar.DAY_OF_MONTH) * 24 + calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);

        if(calendarAsInt > dateAsInt){
            return true;
        }
        else{
            return false;
        }
    }



    public String expiryDateToString(){
        byte[] date = getDate();
        return "Expiry date: " + (date[2]) + "/" + date[1] + "/" + (date[0]+2000) + " " + date[3] + ":" + date[4] + ":" + date[5];
    }

}