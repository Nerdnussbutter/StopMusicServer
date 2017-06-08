import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by jonas on 27.03.17.
 */
public class Handler implements Runnable {

    private void createSocket() throws IOException, InterruptedException {
        DatagramSocket datagramSocket = new DatagramSocket(8002);
        byte[] receiveData = new byte[1024];
        while(true){
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            datagramSocket.receive(receivedPacket);
            String message = new String(receivedPacket.getData());
            if(message.contains("stop")){
                System.out.println("Stopping music");
                stopMusic();
            }else if(message.contains("discovery")&&datagramSocket.getBroadcast()){
                sendDiscoveryAnswer(receivedPacket.getAddress(), datagramSocket);
            }else if(message.contains("abort")){
                break;
            }
        }
    }

    private void sendDiscoveryAnswer(InetAddress address, DatagramSocket socket){
        try {
            System.out.println("Discovery");
            socket.connect(address, 8002);
            byte[] buf= "discovery_ack".getBytes();
            DatagramPacket packet= new DatagramPacket(buf, buf.length);
            socket.send(packet);
            socket.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic(){
        Process p = null;
        Process q = null;
        try {
            String[] cmd = { "/usr/bin/notify-send",
                    "-t",
                    "3",
                    "Your music was stopped!"};

            p = Runtime.getRuntime().exec("dbus-send --print-reply --dest=org.mpris.MediaPlayer2.spotify /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.Pause");
            p.waitFor();

            q = Runtime.getRuntime().exec(cmd);
            q.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(p!=null) p.destroy();
            if(q!=null) q.destroy();
        }
    }

    @Override
    public void run() {
        try {
            createSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
