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

import java.util.*;

import java.sql.*;

public class supplier_samsung extends Agent {
    List<String> productos = Arrays.asList("Samsung Galaxy S20", "Samsung Galaxy Tab");
    String db;
    String producto;
    String AgentName = "Samsung";
    int ID_proveedor = 3;

    // Put agent initializations here
    protected void setup() {
        // Register the supplier service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        // item-selling
        sd.setType("item-supply");
        sd.setName("JADE-item-supply");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Supplier-agent " + getAID().getName() + " terminando.");
    }

    private void Surtir() {
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to DB
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+db, "root", "root");

            // SURTIENDO EN PRODUCTO
            // Query
            Statement stmt = con.createStatement();
            // Query Data
            String query = "UPDATE `producto` SET `existencia`=? WHERE nombre=?";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, 2);
            preparedStmt.setString(2, producto);

            preparedStmt.executeUpdate();

            // GENERANDO ORDEN DE SURTIDO
            // Query
            stmt = con.createStatement();
            // Query Data
            query = "INSERT INTO orden_surtido (`id`, `producto`, `cantidad`) VALUES (?, ?, ?)";
            preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, 0);
            preparedStmt.setString(2, producto);
            preparedStmt.setInt(3, 2);

            preparedStmt.executeUpdate();

            // End DB connection
            con.close();

        } catch (Exception e) {
            System.out.println("[SUPPLIER - " + AgentName + "] " + " Surtir() " + e);
        }

        System.out.println("[SUPPLIER - " + AgentName + "] Pedido entregado de " + producto);
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // CFP Message received. Process it
                String title = msg.getContent().split("@")[0];

                producto = title;
                db = msg.getContent().split("@")[1];

                ACLMessage reply = msg.createReply();

                if (productos.contains(title)) {
                    // The requested item is available for supply. Reply with confirmation.
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println("[SUPPLIER - " + AgentName + "] Recibida solicitud para proveer " + producto);

                    // Esperar 10 segundos
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }

                    // Ejecutar update de SQL
                    Surtir();

                } else {
                    // The requested item is NOT available.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    } // End of inner class OfferRequestsServer
}
