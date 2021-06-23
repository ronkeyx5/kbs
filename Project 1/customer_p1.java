package proyect1;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class customer_p1 extends Agent {
	// The title of item to buy
	private String targetItem;
	private String paymentMethod;

	private String composeItemPayment;

	// The list of known seller agents
	private AID[] sellerAgents;
	//GUI
	private customerGUI_p1 myGui;

	//Nombre del Agente
	private String AgentName;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		AgentName = getAID().getName().split("@")[0];
		System.out.println("[CUSTOMER - "+AgentName+"] El customer [" + AgentName + "] esta listo.");

		myGui = new customerGUI_p1(this);
		myGui.showGui();

	}

    //TODO - Falta implementacion de metodo de pago
    public void SolicitarProducto(final String item, final String payment) {
        if (item != null && item.length() > 0) {
			targetItem = item;
			paymentMethod = payment;
			System.out.println("[CUSTOMER - "+AgentName+"] El producto a comprar es: "+targetItem);
			System.out.println("[CUSTOMER - "+AgentName+"] Con metodo de pago: "+payment);

			// Add a TickerBehaviour that schedules a request to seller agents every minute
			addBehaviour(new TickerBehaviour(this, 2000) {
				protected void onTick() {
					System.out.println("[CUSTOMER - "+AgentName+"] Tratando de comprar: "+targetItem);
					// Update the list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("item-selling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("[CUSTOMER - "+AgentName+"] Encontrados los siguientes vendedores:");
						sellerAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							sellerAgents[i] = result[i].getName();
							System.out.println("[CUSTOMER - "+AgentName+"] " + sellerAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Perform the request
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println("[CUSTOMER - "+AgentName+"] Error al elegir un producto para compra");
			doDelete();
		}
    }

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("[CUSTOMER - "+AgentName+"] Customer "+getAID().getName()+" terminando.");
		myGui.dispose();
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller 
	   agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestSeller; // The agent who provides the best offer 
		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			composeItemPayment = (targetItem+"@"+paymentMethod);

			switch (step) {
			case 0: // Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				} 
				cfp.setContent(composeItemPayment);
				cfp.setConversationId("item-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1: // Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer 
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || price < bestPrice) {
							// This is the best offer at present
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
						// We received all replies
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2: // Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(composeItemPayment);
				order.setConversationId("item-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3: // Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println("[CUSTOMER - "+AgentName+"] "+targetItem+" adquirido satisfactoriamente de "+reply.getSender().getName());
						System.out.println("[CUSTOMER - "+AgentName+"] Precio $"+bestPrice);
						myAgent.doDelete();
					}
					else {
						System.out.println("[CUSTOMER - "+AgentName+"] El producto solicitado ya fue vendido");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

		public boolean done() {
			if (step == 2 && bestSeller == null) {
				System.out.println("[CUSTOMER - "+AgentName+"] Intento de compra fallido de: "+targetItem+" | No disponible para compra");
			}
			return ((step == 2 && bestSeller == null) || step == 4);
		}
	}  // End of inner class RequestPerformer
}