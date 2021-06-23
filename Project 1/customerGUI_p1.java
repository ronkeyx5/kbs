package proyect1;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class customerGUI_p1 extends JFrame {	
	private customer_p1 myAgent;
	
	private JTextField itemField, payField;
	
	customerGUI_p1(customer_p1 a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Producto a comprar:"));
		itemField = new JTextField(15);
		p.add(itemField);
		p.add(new JLabel("Metodo de pago:"));
		payField = new JTextField(15);
		p.add(payField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Comprar");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String item = itemField.getText().trim();
					String payment = payField.getText().trim();

                    //Funcion de customer_p1
					myAgent.SolicitarProducto(item, payment);

					itemField.setText("");
					payField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(customerGUI_p1.this, "Valores Invalidos "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
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
