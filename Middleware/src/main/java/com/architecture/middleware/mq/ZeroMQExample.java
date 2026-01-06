package com.architecture.middleware.mq;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.springframework.stereotype.Component;

@Component
public class ZeroMQExample {

    public void publisherExample() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket publisher = context.createSocket(SocketType.PUB);
            publisher.bind("tcp://*:5555");

            for (int i = 0; i < 10; i++) {
                String message = "Message " + i;
                publisher.send(message.getBytes(ZMQ.CHARSET), 0);
                System.out.println("ZeroMQ published: " + message);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscriberExample() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5555");
            subscriber.subscribe("".getBytes(ZMQ.CHARSET));

            while (!Thread.currentThread().isInterrupted()) {
                String message = subscriber.recvStr(0);
                System.out.println("ZeroMQ received: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestReplyServer() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket responder = context.createSocket(SocketType.REP);
            responder.bind("tcp://*:5556");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] request = responder.recv(0);
                System.out.println("ZeroMQ server received: " + new String(request, ZMQ.CHARSET));
                
                String reply = "Response to: " + new String(request, ZMQ.CHARSET);
                responder.send(reply.getBytes(ZMQ.CHARSET), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestReplyClient() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket requester = context.createSocket(SocketType.REQ);
            requester.connect("tcp://localhost:5556");

            for (int i = 0; i < 5; i++) {
                String request = "Request " + i;
                requester.send(request.getBytes(ZMQ.CHARSET), 0);
                System.out.println("ZeroMQ client sent: " + request);

                byte[] reply = requester.recv(0);
                System.out.println("ZeroMQ client received: " + new String(reply, ZMQ.CHARSET));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
