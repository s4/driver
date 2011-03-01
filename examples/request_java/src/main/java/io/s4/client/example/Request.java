/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package io.s4.client.example;

import io.s4.client.Driver;
import io.s4.client.Message;
import io.s4.client.ReadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Request {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No host name specified");
            System.exit(1);
        }
        String hostName = args[0];
        
        if (args.length < 2) {
            System.err.println("No port specified");
            System.exit(1);
        }
        
        int port = -1;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.err.println("Bad port number specified: " + args[1]);
            System.exit(1);
        }
        
        if (args.length < 3) {
            System.err.println("No stream name specified");
            System.exit(1);
        }
        String streamName = args[2];
        
        if (args.length < 4) {
            System.err.println("No class name specified");
            System.exit(1);
        }
        String clazz = args[3];   
        
        String outputStreamsString = "";
        if (args.length == 5) {
            outputStreamsString = args[4];
        }

        String[] outputStreams = outputStreamsString.split(" ");
        
        Driver d = new Driver(hostName, port);
        Reader inputReader = null;
        BufferedReader br = null;
        try {
            if (!d.init()) {
                System.err.println("Driver initialization failed");
                System.exit(1);
            }
            
            d.setReadMode(ReadMode.All);
            if (outputStreams.length > 0) {
                d.setReadMode(ReadMode.Select);
                for (String outputStream : outputStreams) {
                    System.out.printf("Registering output stream name '%s'\n", outputStream);
                    d.readInclude(outputStream);
                }
            }
            
            if (!d.connect()) {
                System.err.println("Driver initialization failed");
                System.exit(1);           
            }
            
            inputReader = new InputStreamReader(System.in);
            br = new BufferedReader(inputReader);

            // send all messages
            for  (String inputLine = null; (inputLine = br.readLine()) != null;) {
                Message m = new Message(streamName, clazz, inputLine);
                d.send(m);
            }
            
            // read all responses
            while (true) {
                Message response = d.recv();
                System.out.println(response);
            }
            /*List<Message> responses = d.recvAll(999999999);
            for (Message message : responses) {
                System.out.println(message);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try { d.disconnect(); } catch (Exception e) {}
            try { br.close(); } catch (Exception e) {}
            try { inputReader.close(); } catch (Exception e) {}
        }

    }

}
