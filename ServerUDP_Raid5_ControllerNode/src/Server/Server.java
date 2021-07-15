/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mario
 */
public class Server extends Thread{
    final int PORT;
    byte[] buffer = new byte[1024];
    DatagramSocket socketUDP;
    String mensaje;
 
   public Server(int port){
       PORT = port;
   }
   
    @Override
    public void run(){
        try {
            System.out.println("Iniciado el servidor UDP");
            socketUDP = new DatagramSocket(PORT);
            
            while (true) {
                
                send(receive());
                
                this.sleep(500);
            }
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DatagramPacket receive(){
        try {
            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(peticion);
            
            mensaje = new String(peticion.getData());
            System.out.println(mensaje);
            
            return peticion;
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void send(DatagramPacket peticion){
        try {
            int puertoCliente = peticion.getPort();
            InetAddress direccion = peticion.getAddress();
            
            mensaje = "¡Hola mundo desde el servidor!";
            buffer = mensaje.getBytes();
            
            DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
            
            socketUDP.send(respuesta);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
