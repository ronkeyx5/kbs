package handson4;

//CLIPS
//import net.sf.clipsrules.jni.*;

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

public class handson4AgentGUI extends JFrame {

    private handson4Agent myAgent;

    private JTextField inputText;

    handson4AgentGUI(handson4Agent a)   {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(5, 5));
		p.add(new JLabel("Facts o Rules:"));
		inputText = new JTextField();
		p.add(inputText);

        getContentPane().add(p, BorderLayout.NORTH);

        JButton addButton = new JButton("Agregar");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = inputText.getText().trim();
					//String price = rule.getText().trim();
					
					if(title.contains("deftemplate") || title.contains("deffacts") || title.contains("assert") || title.contains("defrule"))	{ //Insertar Ground Facts o Rules, ninguna otra funcion
						//JOptionPane.showMessageDialog(BookSellerGui.this, "ola si", JOptionPane.ERROR_MESSAGE); 

						//AQUI SE DEBE MANDAR AL AGENTE para que lo ejecute en clips
						//myAgent.updateCatalogue(title, Integer.parseInt(price)); || Aqui agrega los datos al agente.
                        myAgent.addToClips(title);
                        System.out.print("Registrado correctamente\n");
					}
					else {
						//Datos invalidos
						//JOptionPane.showMessageDialog(BookSellerGui.this, "ola si 2", JOptionPane.ERROR_MESSAGE); 
						System.out.print("Vacio o invalido");  
					}

					//myAgent.updateCatalogue(title, Integer.parseInt(price)); || Aqui agrega los datos al agente.
					
					inputText.setText("");
					//rule.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(handson4AgentGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.CENTER);

        
        JButton run = new JButton("Run");
		run.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
                System.out.print("\n > Run < \n\n");
				myAgent.addToClips("(run)");
			}
		} );
		p = new JPanel();
		p.add(run);
		getContentPane().add(p, BorderLayout.SOUTH);
/*
        JButton facts = new JButton("Facts");
		facts.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				myAgent.addToClips("(facts)");
			}
		} );
		p = new JPanel();
		p.add(facts);
		getContentPane().add(p, BorderLayout.SOUTH);

        JButton rules = new JButton("Run");
		rules.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				myAgent.addToClips("(rules)");
			}
		} );
		p = new JPanel();
		p.add(rules);
		getContentPane().add(p, BorderLayout.SOUTH);
        */

        addWindowListener( new WindowAdapter(){
            public void windowClosing(WindowEvent e)    {
                
                System.out.print("\nBye\n");

                /*System.out.print("\n\nRun:\n");
                myAgent.addToClips("(run)");

                
                try {
					Thread.sleep(1000);
				}
				catch (Exception f) {
					System.out.print("error");
				}
                */
                

                myAgent.doDelete();
            }
        });
        setResizable(true);
    }

    public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	

}