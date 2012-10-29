import java.net.*;
import java.io.*;

public void newClient(int timeout, String message)
        throws SocketTimeoutException, SocketException {

    try {

        File file = new File("./video/tmp.raw");
        FileOutputStream fos = new FileOutputStream(file);
        File filein = new File("./video/video.raw");

        InetAddress address = InetAddress.getByName(host);
        byte[] data = message.getBytes();
        byte[] buffer = new byte[64000];

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeout);

        DatagramPacket packet = new DatagramPacket(data, data.length,
                address, port);
        socket.send(packet);

        DatagramPacket rpacket = new DatagramPacket(buffer, buffer.length);

        while (true) {
            socket.receive(rpacket);
            if (rpacket.getLength() <= 9) {
                String cmd = new String(rpacket.getData(), 0,
                        rpacket.getLength());
                if (cmd.equals("END_VIDEO")) {
                    System.out.println("C:Fin de transmission");
                    break;
                }
            }
            fos.write(rpacket.getData());
        }

        System.out.println("video.raw ->" + filein.length());
        System.out.println("tmp.raw -> " + file.length());
        Assert.assertTrue(file.length() == filein.length());

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (UnknownHostException e) {

        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
