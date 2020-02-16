import lejos.hardware.motor.*; //for motorer
import lejos.hardware.lcd.*; //for å skrive ut på skjermen
import lejos.hardware.port.Port; //for å velge port for sensorer
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.Keys; //for lcd
import lejos.robotics.SampleProvider; //for å hente samples fra sensorer
import lejos.hardware.sensor.*; //for alle sensorer
import lejos.hardware.Sound; //for musikk
import lejos.hardware.Button; //for knapper på brikken (ubrukt)
import java.io.File; //for musikk

public class KoppOgBall {
    public static void main (String[] arg) throws Exception {
		try{
        // Henter inn porter
        Brick brick = BrickFinder.getDefault();
        Port s1 = brick.getPort("S1"); // trykksensor
        Port s2 = brick.getPort("S2"); // trykksensor
        Port s3 = brick.getPort("S3"); // trykksensor

        // Skjerm shit
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		TextLCD lcd = ev3.getTextLCD();
		Keys keys = ev3.getKeys();

        // setter opp for å kunne lese av trykksensorene om de er trykket inn eller ikke
        SampleProvider trykksensor1 = new EV3TouchSensor(s1);
		float[] trykkSample1 = new float[trykksensor1.sampleSize()]; // tabell som inneholder avlest verdi
		SampleProvider trykksensor2 = new EV3TouchSensor(s2);
        float[] trykkSample2 = new float[trykksensor2.sampleSize()]; // tabell som inneholder avlest verdi
        SampleProvider trykksensor3 = new EV3TouchSensor(s3);
		float[] trykkSample3 = new float[trykksensor3.sampleSize()]; // tabell som inneholder avlest verdi

		boolean fortsett  = true; //kan settes til false lenger ned i programmet for å ende programmet (brukt i killswitch)
        int vansklighetsgrad = 0; //hvor vanskelig spillet er

        //Skriver ut på "Velg vansklighetsgrad" skjermen når programmet er klart og kjører
        lcd.drawString("Velg", 0, 3);
        lcd.drawString("vansklighetsgrad", 0, 5);

        //Velger vansklighetsgrad
        while(fortsett){

            //sjekker alle tre trykksensorer, hvis trykkSample1[0] er 1 betyr det at trykksensor 1 er presset inn
            trykksensor1.fetchSample(trykkSample1, 0);
            trykksensor2.fetchSample(trykkSample2, 0);
            trykksensor3.fetchSample(trykkSample3, 0);

            if (trykkSample1[0] > 0){ //Hvis sensor 1 er trykket inn:
                fortsett = false; //stanser løkken slik at programmet går videre
                vansklighetsgrad = 1; //setter vanslighetsgrad
                lcd.drawString("Vansklighetsgrad:", 0, 3); // Skriver ut satt vanslighetsgrad
                lcd.drawString("Lett                 ", 0, 5); // mange mellomrom for at gammel tekst skal fjernes
            }
            else if (trykkSample2[0] > 0){ //Hvis sensor 2 er trykket inn:
                fortsett = false;
                vansklighetsgrad = 2;
                lcd.drawString("Vansklighetsgrad:", 0, 3);
                lcd.drawString("Middels                ", 0, 5);
            }
            else if (trykkSample3[0] > 0){ //Hvis sensor 3 er trykket inn:
                fortsett = false;
                vansklighetsgrad = 3;
                lcd.drawString("Vansklighetsgrad:", 0, 3);
                lcd.drawString("Vanskelig           ", 0, 5);
            }
            //hvis ingen er trykket inn går programmet en ny runde og sjekker om de er trykket inn igjen. Dette skjer veldig mange ganger avhengig av hvor rask personen er til å velge vansklighetsgrad

            //sleeper programmet slik at den ikke kjører for fort (kan kræsje programmet siden det er en nesten evig løkke)
            Thread.sleep(50);
        }

        int runder = 10 + 5*vansklighetsgrad; //antall trekke roboten gjør øker med vanslighetsgraden
        java.util.Random rand = new java.util.Random(); //random number generator
        int temp; //brukes for å midlertidig oppbevare det tilfeldige tallet
        int posisjon = 1; //Brukes for å holde styr på hvor ballen er
        int[] monster = new int[runder+1]; //en array som inneholder tall mellom 1 og 7.
        int[] posTab  = new int[runder]; //en array som inneholder posisjonen for hvert trekk

        //Genererer mønster
		for(int i = 0; i < runder; i++){
            temp = rand.nextInt(6);
            if(temp == 1){ // 1/6 sjanse for at den gjør move 7 (bytter de 2 uten ball)
                monster[i] = 7;
            }
            else { // 5/6 sjanse for at den gjør et annet move

                if(posisjon == 2){ //hvis ballen er i midten gjør den enten move 2 eller 3, 2 bytter med venstre og 3 bytter med høyre
                    temp = rand.nextInt(2);
                    monster[i] = temp+2;
                    if(temp == 0){
                        posisjon = 1;
                    } else {
                        posisjon = 3;
                    }
                }
                else if(posisjon == 3){ //Hvis ballen er helt til høyre gjør den enten move 4 eller 6, som setter koppen i posisjon 1 eller 2
                    temp = rand.nextInt(2);
                    if(temp == 0){
                        monster[i] = 6;
                        posisjon = 1;
                    } else {
                        monster[i] = 4;
                        posisjon = 2;
                    }
                }
                else{ //Hvis ballen er til venstre gjør den enten move 1 eller 5 som bytter med koppen på 2 eller koppen på 3
                    temp = rand.nextInt(2);
                    if(temp == 0){
                        monster[i] = 5;
                        posisjon = 3;
                    } else {
                        monster[i] = 1;
                        posisjon = 2;
                    }
                }
            }
            //lagrer den nye posisjonen i arrayen
            posTab[i] = posisjon;
        }

        //Setter farten for motorene avhengig av vansklighetsgrad
        if(vansklighetsgrad == 1){
            Motor.A.setSpeed(150);
            Motor.B.setSpeed(150);
        }
        else if(vansklighetsgrad == 2){
            Motor.A.setSpeed(250);
            Motor.B.setSpeed(250);
        }
        else{
            Motor.A.setSpeed(325);
            Motor.B.setSpeed(325);
        }

        int graderForover = 225;
        int graderTilbake = 180 - graderForover;

        //Kjører mønsteret
        for(int i = 0; i < runder; i++){

            if(monster[i] == 1 && monster[i+1] == 2){ // Hvis den går fra venstre til midten og deretter tilbake til venstre tar den en 360 istedenfor å ta en pause halveis
                Motor.A.rotate(graderForover*2+graderTilbake); //den går litt for langt for å sette koppene på midten
                Motor.A.rotate(graderTilbake); //går 35 grader tilbake for å rette seg ut
                i++; //Hopper over neste
            }

            else if(monster[i] == 4 && monster[i+1] == 3){ // Samme som forrige bare for høyre
                Motor.B.rotate(graderForover*2+graderTilbake);
                Motor.B.rotate(graderTilbake);
                i++;
            }

            else if(monster[i] == 1 || monster[i] == 2){ // Hvis movet er 1 eller 2 roterer venstre arm 180 grader
                Motor.A.rotate(graderForover);
                Motor.A.rotate(graderTilbake);
            }

            else if(monster[i] == 3 || monster[i] == 4){ // samme bare for høyre arm
                Motor.B.rotate(graderForover);
                Motor.B.rotate(graderTilbake);
            }

            else if(monster[i] == 5){ // Move 5 bytter kopp 1 med kopp 2, deretter kopp 2 med kopp 3 slik at koppen med ballen ender på motsatt side
                Motor.A.rotate(graderForover);
                Motor.A.rotate(graderTilbake);
                Motor.B.rotate(graderForover);
                Motor.B.rotate(graderTilbake);
            }

            else if(monster[i] == 6){ // samme bare for hvis ballen er i posisjon 3 (høyre)
                Motor.B.rotate(graderForover);
                Motor.B.rotate(graderTilbake);
                Motor.A.rotate(graderForover);
                Motor.A.rotate(graderTilbake);
            }

            else{ // Hvis det er move 7 (bytter de 2 koppene som ikke har ball i)
                if(posTab[i] == 1){ //hvis koppen er i posisjon 1, bytter den kopp 2 og 3
                    Motor.B.rotate(graderForover);
                    Motor.B.rotate(graderTilbake);
                }
                else if(posTab[i] == 2){ //hvis koppen er i midten bytter den kopp 2 med kopp 1, kopp 2 med kopp 3 og setter tilslutt kopp 1 tilbake i poisisjon 2 (midten)
                    Motor.A.rotate(graderForover);
                    Motor.A.rotate(graderTilbake);
                    Motor.B.rotate(graderForover);
                    Motor.B.rotate(graderTilbake);
                    Motor.A.rotate(graderForover);
                    Motor.A.rotate(graderTilbake);
                }
                else { //hvis koppen er i posisjon 3, bytter den kopp 2 og 1
                    Motor.A.rotate(graderForover);
                    Motor.A.rotate(graderTilbake);
                }
            }
        }

        //Valg av svar
        fortsett = true; // bruker fortsett på nytt så vi setter den tilbake til true
        int valg = 0; // Hvilken kopp man tror ballen er i

        //Skriver ut til skjermen at du må velge hvor du tror koppen er
        lcd.drawString("Velg kopp         ", 0, 3);
        lcd.drawString("         ", 0, 5);

        while(fortsett){
            //Bruker trykksensorene på nytt for å velge kopp
            trykksensor1.fetchSample(trykkSample1, 0);
            trykksensor2.fetchSample(trykkSample2, 0);
            trykksensor3.fetchSample(trykkSample3, 0);

            if (trykkSample1[0] > 0){
                valg = 1;
                if(valg == posisjon){
                    lcd.drawString("RETT                 ", 0, 5);
                    Thread.sleep(3000);
                    fortsett = false;
                }
                else{
                    lcd.drawString("FEIL                 ", 0, 5);
                    Thread.sleep(3000);
                }

            }
            else if (trykkSample2[0] > 0){
                valg = 2;
                if(valg == posisjon){
                    lcd.drawString("RETT                 ", 0, 5);
                    Thread.sleep(3000);
                    fortsett = false;
                }
                else{
                    lcd.drawString("FEIL                 ", 0, 5);
                    Thread.sleep(3000);
                }
            }
            else if (trykkSample3[0] > 0){
                valg = 3;
                if(valg == posisjon){
                    lcd.drawString("RETT                 ", 0, 5);
                    Thread.sleep(3000);
                    fortsett = false;
                }
                else{
                    lcd.drawString("FEIL                 ", 0, 5);
                    Thread.sleep(3000);
                }
            }
            Thread.sleep(50);
        }
      }catch(Exception e){
          System.out.println("FEIL:" + e);
          Thread.sleep(10000);
      }
        }
    }
