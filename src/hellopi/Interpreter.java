package hellopi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {

    Boolean debug = false;
    Boolean penPos = false; //false - up, true - down
    
    Axis upperAxis;
    Axis lowerAxis;
    Servomotor serwo;
    
    double posX = 0.0;
    double posY = 0.0;

    public Interpreter(Axis upperAxis, Axis lowerAxis, Servomotor serwo){
        this.upperAxis = upperAxis;
        this.lowerAxis = lowerAxis;
        this.serwo = serwo;
    }

    public Interpreter(Boolean debug, Axis upperAxis, Axis lowerAxis, Servomotor serwo){
        this.upperAxis = upperAxis;
        this.lowerAxis = lowerAxis;
        this.serwo = serwo;
        this.debug = debug;
    }

    public void interpretLine(String line) throws InterruptedException{
        //System.out.println(line.isEmpty());

        if(!line.contains("%") && !line.isEmpty()) {
            String gcodeLine = line.replaceAll("\\((.*?)\\)", "");
            if (gcodeLine.length() > 0) {
                if(debug) {
                    System.out.print("\tGcode: " + gcodeLine);
                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        System.out.print(" //" + matcher.group(1));
                    }
                    System.out.print("\n");
                }

                String[] parts = gcodeLine.split(" ");

                switch (parts[0]){
                    case "G00":
                        g00(parts);
                        break;
                    case "G01":
                        g01(parts);
                        break;
                    case "G02":
                        g02(parts);
                        break;
                    case "G03":
                        g03(parts);
                        break;
                    case "G04":
                        g04(parts);
                        break;
                    case "G21":
                        g21(parts);
                        break;
                    case "M0":
                        m0(parts);
                        break;
                    case "M2":
                        m2(parts);
                        break;
                    case "M3":
                        m3(parts);
                        break;
                    case "M4":
                        m4(parts);
                        break;
                    case "M5":
                        m5(parts);
                        break;
                    default:
                        System.out.println("\tNIEOBSŁUGIWANA KOMENDA: " + parts[0]);
                        break;
                }

            } else {
                if(debug) {
                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        System.out.println("\t//" + matcher.group(1));
                    }
                }
            }
        }else{
            if (debug)
                System.out.println("\t////");
        }
    }

    public void penDown() throws InterruptedException{
        if(!penPos) {
            penPos = true;
            serwo.penDown();
            System.out.println("Pisak w dol");
        }
    }

    public void penUp() throws InterruptedException{
        if(penPos) {
            penPos = false;
            serwo.penUp();
            System.out.println("Pisak w gore");
        }
    }

    public void g00(String[] args) throws InterruptedException{
        penUp();

        switch (args.length) {
            case 2:
                //Z...
                if (args[1].charAt(0) == 'Z') {
                    double z;
                    z = Double.parseDouble(args[1].substring(1));
                    System.out.println("Pozycjonowanie narzedzia os Z: " + z);
                    
                    if(z > 0){
                        penUp();
                    }else{
                        //?? penDown();?
                    }

                } else {
                    System.out.println("Blad funkcji G00. Argument Z.");
                }
                break;
            case 3:
                //X... Y...
                if (args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y') {
                    double x, y;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    System.out.println("Pozycjonowanie narzedzia osie X Y:\t" + x + "\t" + y);
                    
                    //pozycjonowanie:
                    
                    int pathLen = (int)Math.sqrt((x - posX) * (x - posX) + (y - posY) * (y - posY)); //pierwiastek z sum kwadratow rożnic
                    double xStep = ((x - posX)/(double)pathLen)/(double)100;
                    double yStep = ((y - posY)/(double)pathLen)/(double)100;
                    
                    for(int i = 0; i < pathLen*100; i++){
                        lowerAxis.setPos(posX + xStep*i, 40);
                        upperAxis.setPos(posY + yStep*i, 40);
                    }
                    lowerAxis.setPos(x, 40); //ustawienie pozycji koncowej (bez bledu wynikajacego z dzielenia)
                    upperAxis.setPos(y, 40);
                    
                    posX = x; //zapisanie aktualnej pozycji
                    posY = y;
                    
                } else {
                    System.out.println("Blad funkcji G00. Argumenty X Y.");
                }
                break;

            default:
                System.out.println("Funkcja G00. Nieobslugiwana liczba argumentow (" + args.length + ")");
                break;
        }
    }

    public void g01(String[] args) throws InterruptedException{

        switch (args.length){
            case 3:
                //Z... F...
                penDown();
                if(args[1].charAt(0) == 'Z' && args[2].charAt(0) == 'F') {
                    double z, f;
                    z = Double.parseDouble(args[1].substring(1));
                    f = Double.parseDouble(args[2].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji liniowej Z F:\t" + z + "\t" + f);
                }else{
                    System.out.println("Blad funkcji G01. Liczba argumentow 2.");
                }
                break;
            case 4:
                //X... Y... Z...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z') {
                    double x, y, z;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji liniowej X Y Z:\t" + x + "\t" + y + "\t" + z );
                    
                    //pozycjonowanie:
                    
                    int pathLen = (int)Math.sqrt((x - posX) * (x - posX) + (y - posY) * (y - posY)); //pierwiastek z sum kwadratow rożnic
                    double xStep = ((x - posX)/(double)pathLen)/(double)100;
                    double yStep = ((y - posY)/(double)pathLen)/(double)100;
                    
                    for(int i = 0; i < pathLen*100; i++){
                        lowerAxis.setPos(posX + xStep*i, 40);
                        upperAxis.setPos(posY + yStep*i, 40);
                    }
                    lowerAxis.setPos(x, 40); //ustawienie pozycji koncowej (bez bledu wynikajacego z dzielenia)
                    upperAxis.setPos(y, 40);
                    
                    posX = x; //zapisanie aktualnej pozycji
                    posY = y;
                    
                }else{
                    System.out.println("Blad funkcji G01. Liczba argumentow 3.");
                }
                break;
            case 5:
                //X... Y... Z... F...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z' && args[4].charAt(0) == 'F') {
                    double x, y, z, f;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    f = Double.parseDouble(args[4].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji liniowej X Y Z F:\t" + x + "\t" + y + "\t" + z + "\t" + f);
                    
                    //pozycjonowanie:
                    
                    int pathLen = (int)Math.sqrt((x - posX) * (x - posX) + (y - posY) * (y - posY)); //pierwiastek z sum kwadratow rożnic
                    double xStep = ((x - posX)/(double)pathLen)/(double)100;
                    double yStep = ((y - posY)/(double)pathLen)/(double)100;
                    
                    for(int i = 0; i < pathLen*100; i++){
                        lowerAxis.setPos(posX + xStep*i, 40);
                        upperAxis.setPos(posY + yStep*i, 40);
                    }
                    lowerAxis.setPos(x, 40); //ustawienie pozycji koncowej (bez bledu wynikajacego z dzielenia)
                    upperAxis.setPos(y, 40);
                    
                    posX = x; //zapisanie aktualnej pozycji
                    posY = y;
                    
                }else{
                    System.out.println("Blad funkcji G01. Liczba argumentow 4.");
                }
                break;

            default:
                System.out.println("Funkcja G01. Nieobslugiwana liczba argumentow (" + args.length + ")");
                break;
        }

    }

    public void g02(String[] args) throws InterruptedException{

        switch (args.length){
            case 6:
                //X... Y... Z... I... J...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z' && args[4].charAt(0) == 'I' && args[5].charAt(0) == 'J') {
                    double x, y, z, i, j;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    i = Double.parseDouble(args[4].substring(1));
                    j = Double.parseDouble(args[5].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji kolowej zgodnie z ruchem wskazowek zegara X Y Z I J:\t" + x + "\t" + y + "\t" + z + "\t" + i + "\t" + j);
                
                    for(int a = 0; a < 3600; a++){

                        double degrees = a/(double)10;
                        double radians = Math.toRadians(degrees);

//                        double x = Math.sin(radians) * 10 + 20; //R = 10mm, środek w 20mm
//                        double y = Math.cos(radians) * 10 + 20; //R = 10mm, środek w 20mm
//
//                        osGorna.setPos(y, 40); //oś x
//                        osDolna.setPos(x, 40); //oś y
                    }
                     
                    
                }else{
                    System.out.println("Blad funkcji G02. Liczba argumentow 5.");
                }
                break;
            case 7:
                //X... Y... Z... I... J...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z' && args[4].charAt(0) == 'I' && args[5].charAt(0) == 'J' && args[6].charAt(0) == 'F') {
                    double x, y, z, i, j, f;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    i = Double.parseDouble(args[4].substring(1));
                    j = Double.parseDouble(args[5].substring(1));
                    f = Double.parseDouble(args[6].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji kolowej zgodnie z ruchem wskazowek zegara X Y Z I J:\t" + x + "\t" + y + "\t" + z + "\t" + i + "\t" + j + "\t" + f);
                }else{
                    System.out.println("Blad funkcji G02. Liczba argumentow 6.");
                }
                break;

            default:
                System.out.println("Funkcja G02. Nieobslugiwana liczba argumentow (" + args.length + ")");
                break;
        }
    }

    public void g03(String[] args) throws InterruptedException{

        switch (args.length){
            case 6:
                //X... Y... Z... I... J...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z' && args[4].charAt(0) == 'I' && args[5].charAt(0) == 'J') {
                    double x, y, z, i, j;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    i = Double.parseDouble(args[4].substring(1));
                    j = Double.parseDouble(args[5].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji kolowej przeciwnie do ruchu wskazowek zegara X Y Z I J:\t" + x + "\t" + y + "\t" + z + "\t" + i + "\t" + j);
                }else{
                    System.out.println("Blad funkcji G03. Liczba argumentow 5.");
                }
                break;
            case 7:
                //X... Y... Z... I... J...
                penDown();
                if(args[1].charAt(0) == 'X' && args[2].charAt(0) == 'Y' && args[3].charAt(0) == 'Z' && args[4].charAt(0) == 'I' && args[5].charAt(0) == 'J' && args[6].charAt(0) == 'F') {
                    double x, y, z, i, j, f;
                    x = Double.parseDouble(args[1].substring(1));
                    y = Double.parseDouble(args[2].substring(1));
                    z = Double.parseDouble(args[3].substring(1));
                    i = Double.parseDouble(args[4].substring(1));
                    j = Double.parseDouble(args[5].substring(1));
                    f = Double.parseDouble(args[6].substring(1));
                    System.out.println("Ruch narzedzia wedlug interpolacji przeciwnie do ruchu wskazowek zegara X Y Z I J:\t" + x + "\t" + y + "\t" + z + "\t" + i + "\t" + j + "\t" + f);
                }else{
                    System.out.println("Blad funkcji G03. Liczba argumentow 6.");
                }
                break;

            default:
                System.out.println("Funkcja G03. Nieobslugiwana liczba argumentow (" + args.length + ")");
                break;
        }
    }

    public void g04(String[] args){
        System.out.println("Postoj czasowy");
    }

    public void g21(String[] args){
        System.out.println("Wymiarowanie w milimetrach");
    }

    public void m0(String[] args) throws InterruptedException{
        System.out.println("Stop programu bezwarunkowy");
        penUp();
    }

    public void m2(String[] args) throws InterruptedException {
        System.out.println("Koniec programu");
        penUp();
    }

    public void m3(String[] args){
        System.out.println("Wlaczanie prawych obrotow wrzeciona");
    }

    public void m4(String[] args){
        System.out.println("Wlaczanie lewych obrotow wrzeciona");
    }

    public void m5(String[] args){
        System.out.println("Wylaczenie obrotow wrzeciona");
    }

}
