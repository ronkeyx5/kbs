package handson5;

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


public class handson5Agent extends Agent   {

    Environment clips;
    //private handson4AgentGUI myGui;

    protected void setup()  {
        System.out.println("Agent "+getLocalName()+" started.");
        clips = new Environment();

        //myGui = new handson4AgentGUI(this);
        //myGui.showGui();

        addBehaviour(new MyGenericBehaviour());
    }   

    protected void takeDown()   {
        //myGui.dispose();
        System.out.print("\n\n############################\n### Terminando Agente... ###\n############################\n\n");
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

        Integer section = 0;
        Scanner myObj = new Scanner (System.in);

        public void action()    {

            switch(section) {
                case 0:
                System.out.print("\n##########################################\n### Cargando agents-repo/clips/persons ###\n##########################################\n\n");

                System.out.print(" > load-persons.clp <\n\n");
                clips.eval("(load src\\clips\\persons\\load-persons.clp)");
                System.out.print("\n\n > load-persons-rules.clp <\n\n");
                clips.eval("(load src\\clips\\persons\\load-persons-rules.clp)");
                System.out.print("\n\n > Reset <\n");

                clips.eval("(reset)");
                System.out.print("\n > Run <\n\n");
                clips.eval("(run)");
                System.out.print("\n\n");

                /*
                try{
                    System.in.read();
                }
                catch(Exception f) {
                    System.out.print(f);
                }
                */

                try {
					Thread.sleep(10000);
				}
				catch (Exception f) {
					System.out.print("error");
				}
                
                section++;
                    break;

                case 1:
                System.out.print("\n###########################################\n### Cargando agents-repo/clips/prodcust ###\n###########################################\n\n");
                clips = new Environment();
                //clips.eval("clear");

                System.out.print(" > load-prod-cust.clp <\n\n");
                clips.eval("(load src\\clips\\prodcust\\load-prod-cust.clp)");
                System.out.print("\n\n > load-prodcust-rules.clp <\n\n");
                clips.eval("(load src\\clips\\prodcust\\load-prodcust-rules.clp)");
                System.out.print("\n\n > Reset <\n");

                clips.eval("(reset)");
                System.out.print("\n > Run <\n\n");
                clips.eval("(run)");
                System.out.print("\n\n");

                /*
                try{
                    System.in.read();
                }
                catch(Exception f) {
                    System.out.print(f);
                }
                */

                try {
					Thread.sleep(10000);
				}
				catch (Exception f) {
					System.out.print("error");
				}

                section++;
                    break;

                case 2:
                System.out.print("\n#########################################\n### Cargando agents-repo/clips/market ###\n#########################################\n\n");
                clips = new Environment();
                //clips.eval("clear");

                System.out.print(" > templates.clp <\n\n");
                clips.eval("(load src\\clips\\market\\templates.clp)");
                System.out.print("\n\n > facts.clp <\n\n");
                clips.eval("(load src\\clips\\market\\facts.clp)");
                System.out.print("\n\n > persons.clp <\n\n");
                clips.eval("(load src\\clips\\market\\persons.clp)");
                System.out.print("\n\n > rules.clp <\n\n");
                clips.eval("(load src\\clips\\market\\rules.clp)");
                System.out.print("\n\n > Reset <\n");

                clips.eval("(reset)");
                System.out.print("\n > Run <\n\n");
                clips.eval("(run)");
                System.out.print("\n\n");

                /*
                try{
                    System.in.read();
                }
                catch(Exception f) {
                    System.out.print(f);
                }
                */

                try {
					Thread.sleep(10000);
				}
				catch (Exception f) {
					System.out.print("error");
				}

                section++;
                    break;
            }
        }

        public boolean done()   {
            if(section==3)   {
                return true;
            }
            else    {
                return false;
            }
        }

        public int onEnd()  {
            myAgent.doDelete();
            return super.onEnd();
        }

    }

}