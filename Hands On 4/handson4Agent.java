package handson4;

//CLIPS
import net.sf.clipsrules.jni.*;

//GUI
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//JADE
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

//JAVA
import java.util.Scanner;


public class handson4Agent extends Agent   {

    Environment clips;
    private handson4AgentGUI myGui;

    protected void setup()  {
        System.out.println("Agent "+getLocalName()+" started.");
        clips = new Environment();

        myGui = new handson4AgentGUI(this);
        myGui.showGui();

        addBehaviour(new MyGenericBehaviour());
    }

    protected void takeDown()   {
        myGui.dispose();
        System.out.print("Terminando agente...");
    }

    public void addToClips(final String query)    {
        //Checar sentencia
        addBehaviour(new OneShotBehaviour(){
            public void action()    {
                //Ejecutar en clips
                if(query.contains("defrule")||query.contains("deffacts"))   {
                    clips.build(query);
                }
                else{
                    clips.eval(query);
                }
                //System.out.print("\nsisepudoa\n");
            }
        });
    }

    private class MyGenericBehaviour extends Behaviour  {

        Boolean ciclo = true;
        Boolean inputOn = true;
        Scanner myObj = new Scanner (System.in);

        public void action()    {

            if(inputOn == true) {
                //ESTE ES EL BUENO

                System.out.print("CLIPS > ");
                String query = myObj.nextLine();  // Read user input

                if(query.equals("(exit)"))  {
                    ciclo = false;
                    System.out.print("\nSaliendo de CLIPS..");
                }
                else if(query.equals("input off"))   {
                    inputOn = false;
                }
                else if(query.contains("assert") || query.contains("(facts)") || query.contains("(reset)") || query.contains("(run)") || query.contains("(rules)")|| query.contains("(templates)"))   {
                    clips.eval(query);
                }
                else    {
                    clips.build(query);
                }
            }

            /*
            System.out.print("\nCLIPS > ");
            String query = myObj.nextLine();  // Read user input
            
            if(query.equals("(exit)"))  {
                ciclo = false;
                System.out.print("\nSaliendo de CLIPS..");
            }
            else    {
                addToClips(query);
            }
            */
        }

        public boolean done()   {
            if(ciclo)   {
                return false;
            }
            else    {
                return true;
            }
        }

        public int onEnd()  {
            myAgent.doDelete();
            return super.onEnd();
        }

    }

}