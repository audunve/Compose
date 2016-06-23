package no.ntnu.idi.compose.test;

public class SubClass extends SuperClass {

    // overrides printMethod in Superclass
    public void printMethod() {
        super.printMethod();
        //System.out.println("Printed in Subclass");
    }
    public static void main(String[] args) {
        SubClass s = new SubClass();
        s.printMethod();    
    }
}
