package proyect1;

//CLIPS
import net.sf.clipsrules.jni.*;

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
import java.lang.reflect.Constructor;
import java.sql.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import java.nio.file.Files;
import java.nio.file.Paths;

public class seller_barnes extends Agent {
    // Catalogo de productos | Lista de Prodcuto
    private List<Producto> catalogue;
    private String AgentName;
    private Producto resurtirProd;

    class Producto {
        public int id = 0;
        public String nombre = "";
        public String tipo = "";
        public int costo = 0;
        public int precio = 0;
        public int existencia = 0;
        public int id_proveedor = 0;
    }

    class VentaKBS {
        public String nombre_prod = "";
        public int precio = 0;
        public String metodo_pago = "";
        public String cliente = "";
    }

    // Put agent initializations here
    protected void setup() {
        // Create the catalogue
        catalogue = new ArrayList<Producto>();

        // Inicializar ambiente clips
        // clips = new Environment();

        AgentName = getAID().getName().split("@")[0];

        System.out.println("[SELLER - " + AgentName + "] El seller [" + AgentName + "] esta listo.");

        // Register the item-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        // item-selling
        sd.setType("item-selling");
        sd.setName("JADE-item-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());

        //addBehaviour(new UpdateDB());

        ConnectDB();
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
        System.out.println("[SELLER - " + AgentName + "] Seller-agent " + getAID().getName() + " terminating.");
    }

    // Conexion con la DB
    private void ConnectDB() {
        // Objecto producto para agregar al catalogo
        Producto producto;

        // Limpiando la lista
        catalogue.clear();

        try {
            // Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to DB
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kbs_barnes", "root", "root");

            // Query
            Statement stmt = con.createStatement();
            // Query Data
            ResultSet rs = stmt.executeQuery("select * from producto");

            // Showing Query Results
            while (rs.next()) {
                // Inicializando el producto
                producto = new Producto();

                // Asignando valores desde la DB
                producto.id = rs.getInt(1);
                producto.nombre = rs.getString(2);
                producto.tipo = rs.getString(3);
                producto.costo = rs.getInt(4);
                producto.precio = rs.getInt(5);
                producto.existencia = rs.getInt(6);
                producto.id_proveedor = rs.getInt(7);

                // Producto agregador al catalogo
                catalogue.add(producto);
            }

            // End DB connection
            con.close();

        } catch (Exception e) {
            System.out.println("[SELLER - " + AgentName + "] " + " ConnectDB() " + e);
        }
    }

    public Producto BuscarProducto(String nombre) {
        ConnectDB();
        for (Producto prod : catalogue) {
            if (nombre.equals(prod.nombre)) {
                return prod;
            }
        }

        System.out.println("[SELLER - " + AgentName + "] Producto no encontrado");

        return new Producto();
    }

    Boolean ActualizarInventario(int id, int existencia) {
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to DB
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kbs_barnes", "root", "root");

            // Query
            Statement stmt = con.createStatement();
            // Query Data
            String query = "UPDATE `producto` SET `existencia`=? WHERE id=?";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setInt(1, existencia);
            preparedStmt.setInt(2, id);

            preparedStmt.executeUpdate();

            // End DB connection
            con.close();
            return true;

        } catch (Exception e) {
            System.out.println("[SELLER - " + AgentName + "] " + " ActualizarInventario() " + e);
        }

        return false;
    }

    int ExisteCliente(String nombre) {
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to DB
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kbs_barnes", "root", "root");

            // Query
            Statement stmt = con.createStatement();
            // Query Data
            ResultSet rs = stmt.executeQuery("SELECT * FROM cliente");

            // Showing Query Results
            while (rs.next()) {
                if (nombre.equals(rs.getString(2))) {
                    return rs.getInt(1);
                }
            }

            // End DB connection
            con.close();

        } catch (Exception e) {
            System.out.println("[SELLER - " + AgentName + "] " + " Existecliente() " + e);
        }

        return 0;
    }

    int AgregarClienteDB(String nombre) {
        int existe = ExisteCliente(nombre);
        if (existe == 0) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Connect to DB
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kbs_barnes", "root", "root");

                // Query
                Statement stmt = con.createStatement();
                // Query Data
                String query = "INSERT INTO cliente VALUES(?, ?)";
                PreparedStatement preparedStmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStmt.setInt(1, 0);
                preparedStmt.setString(2, nombre);

                preparedStmt.executeUpdate();

                ResultSet gen = preparedStmt.getGeneratedKeys();

                gen.next();
                int id_creado = gen.getInt(1);

                // End DB connection
                con.close();

                return id_creado;

            } catch (Exception e) {
                System.out.println("[SELLER - " + AgentName + "] " + " AgregarClienteDB() " + e);
            }
        }

        return existe;
    }

    void AgregarVentaDB(int id_prod, int id_cliente, String forma_pago, String promociones) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to DB
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kbs_barnes", "root", "root");

            // Query
            Statement stmt = con.createStatement();
            // Query Data
            String query = "INSERT INTO venta VALUES(?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setInt(1, 0);
            preparedStmt.setInt(2, id_cliente);
            preparedStmt.setInt(3, id_prod);
            preparedStmt.setString(4, forma_pago);
            preparedStmt.setString(5, promociones);

            preparedStmt.executeUpdate();

            ResultSet gen = preparedStmt.getGeneratedKeys();

            // End DB connection
            con.close();

        } catch (Exception e) {
            System.out.println("[SELLER - " + AgentName + "] " + "AgregarVentaDB() " + e);
        }
    }

    public Boolean Venta(String nombre, String customerName, String payment, String promociones) {
        Producto p = BuscarProducto(nombre); // Producto a vender

        p.existencia--; // Venta concretada

        // Actualizar producto en existencia en la DB
        ActualizarInventario(p.id, p.existencia);

        if (p.existencia < 1) {
            // Se acaba de terminar el producto
            resurtirProd = p;
            addBehaviour(new Resurtir());
        }

        // Registro de venta en DB
        int id_cliente = AgregarClienteDB(customerName);
        AgregarVentaDB(p.id, id_cliente, payment, promociones);

        return true;
    }

    // ######################################

    private class Resurtir extends OneShotBehaviour {
        private AID[] supplyAgents;

        public void action() {
            MessageTemplate mt; // The template to receive replies

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            sd.setType("item-supply");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                // System.out.println("[CUSTOMER - " + AgentName + "] Encontrados los siguientes
                // vendedores:");
                supplyAgents = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    supplyAgents[i] = result[i].getName();
                    // System.out.println("[CUSTOMER - " + AgentName + "] " +
                    // supplyAgents[i].getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Agregar proveedores como recibidores
            for (int i = 0; i < supplyAgents.length; ++i) {
                cfp.addReceiver(supplyAgents[i]);
            }

            cfp.setContent(resurtirProd.nombre + "@kbs_barnes");
            cfp.setConversationId("item-supply");
            cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
            myAgent.send(cfp);
            // Prepare the template to get proposals
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-supply"),
                    MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));

            System.out.println(
                    "[SELLER - " + AgentName + "] Solicitud para resurtir " + resurtirProd.nombre + " enviada");

        }
    }

    /**
     * Inner class OfferRequestsServer. This is the behaviour used by Book-seller
     * agents to serve incoming requests for offer from buyer agents. If the
     * requested book is in the local catalogue the seller agent replies with a
     * PROPOSE message specifying the price. Otherwise a REFUSE message is sent
     * back.
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it

                String title = msg.getContent().split("@")[0];
                String payment = msg.getContent().split("@")[1];

                System.out
                        .println("[SELLER - " + AgentName + "] Producto - " + title + " | Metodo de Pago - " + payment);

                ACLMessage reply = msg.createReply();

                // Buscar si existe el producto
                Producto p = BuscarProducto(title);
                Integer price = p.precio;

                // if (price != 0) {
                if (p.existencia > 0) {
                    // The requested book is available for sale. Reply with the price
                    System.out.println("[SELLER - " + AgentName + "] Producto existente - " + p.nombre);
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    System.out.println(
                            "[SELLER - " + AgentName + "] Producto en el sistema pero sin existencia - " + p.nombre);
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    } // End of inner class OfferRequestsServer

    /**
     * Inner class PurchaseOrdersServer. This is the behaviour used by Book-seller
     * agents to serve incoming offer acceptances (i.e. purchase orders) from buyer
     * agents. The seller agent removes the purchased book from its catalogue and
     * replies with an INFORM message to notify the buyer that the purchase has been
     * sucesfully completed.
     */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);

            String name;
            String promociones = "Ninguna";

            KBS kbs = new KBS();
            VentaKBS venta = new VentaKBS();

            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it

                String title = msg.getContent().split("@")[0];
                String payment = msg.getContent().split("@")[1];

                ACLMessage reply = msg.createReply();

                // Nombre del comprador
                name = msg.getSender().getName().split("@")[0];

                // CLIPS
                // CLIPS

                kbs.Inicializar(); // Comenzando ambiente de clips

                venta.nombre_prod = title;
                venta.cliente = name;
                venta.metodo_pago = payment;
                venta.precio = BuscarProducto(title).precio;

                promociones = kbs.ProcesarVenta(venta);

                // CLIPS
                // CLIPS

                // Restarlo del catalogo
                Boolean success = Venta(title, name, payment, promociones);

                if (success) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println("[SELLER - " + AgentName + "] " + title + " vendido al agente " + name); // Falta
                                                                                                                // metodo
                                                                                                                // de
                                                                                                                // pago

                    // Actualizar el catalogo
                    ConnectDB();
                } else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    System.out.println("[SELLER - " + AgentName + "] Error al tratar de vender " + title);
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    } // End of inner class OfferRequestsServer

    private class UpdateDB extends CyclicBehaviour {
        public void action() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            ConnectDB();
        }
    } // End of inner class UpdateDB

    private class KBS {
        // Clips Enviroment
        Environment clips;
        String vendor = "barnes";
        // ConsoleOutputCapturer console;

        public void Inicializar() {
            clips = new Environment();
            // console = new ConsoleOutputCapturer();

            // Cargar defrules
            // clips.eval("(load src\\clips\\persons\\load-persons.clp)");
            clips.eval("(load src\\clips\\proyect1\\deftemplate.clp)");

            // Cargar reglas - barnes
            clips.eval("(load src\\clips\\proyect1\\" + vendor + "_rules.clp)");
            clips.eval("(reset)");

            // Mostrar reglas
            // clips.eval("(rules)");
        }

        public String ProcesarVenta(VentaKBS venta) {
            String ofertas = "";

            // Agregar venta a clips
            // (assert (venta (nombre_prod "Iphone 12") (precio 22000) (metodo_pago
            // "credito") (cliente Whoan) ) )
            clips.eval("(assert (venta (nombre_prod \"" + venta.nombre_prod + "\") (precio " + venta.precio
                    + ") (metodo_pago \"" + venta.metodo_pago + "\") (cliente \"" + venta.cliente + "\") ) )");

            // Captura de consola
            // console.start();

            // Correr reglas sobre la venta
            clips.eval("(dribble-on \"" + vendor + "results.txt\")");

            clips.eval("(run)");

            //clips.eval("(dribble-off src\\clips\\proyect1\\" + vendor + "_rules_results.txt)");
            clips.eval("(dribble-off)");

            try {
                ofertas = new String(
                        Files.readAllBytes(Paths.get(vendor + "results.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println("[OFERTAS] - Antes de replace: " + ofertas);

            ofertas = ofertas.replace("@", "- ");

            //System.out.println("[OFERTAS] - Despues de replace: " + ofertas);

            return ofertas;
        }
    }

}