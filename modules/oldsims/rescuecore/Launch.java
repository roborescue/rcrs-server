/*
 * Last change: $Date: 2005/06/14 21:55:50 $
 * $Revision: 1.9 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import rescuecore.commands.Command;

/**
 * The Launch class is responsible for starting rescue components.
 */
public class Launch {
  private static Collection components;

  private final static int TIMEOUT = 60000;

  private final static String UDP_FLAG = "-u";
  private final static String UDP_LONG_FLAG = "--udp";

  /**
   * Launch some rescue components
   */
  public static void main(String[] args) {
    boolean tcp = true;
    components = new ArrayList();
    InetAddress kernel = null;
    int kernelPort = -1;
    if (args.length < 3) {
      printUsage();
      return;
    }
    try {
      kernel = InetAddress.getByName(args[0]);
      kernelPort = Integer.parseInt(args[1]);
    } catch (UnknownHostException e) {
      System.out.println("Bad host name: " + args[0]);
      printUsage();
      return;
    } catch (NumberFormatException e) {
      System.out.println("Bad host port: " + args[1]);
      printUsage();
      return;
    }
    Collection launch = new ArrayList();
    for (int i = 2; i < args.length; ++i) {
      // Process the next argument
      if (args[i].startsWith("-")) {
        // It is a switch
        if (args[i].equalsIgnoreCase(UDP_FLAG) || args[i].equalsIgnoreCase(UDP_LONG_FLAG))
          tcp = false;
        else {
          System.out.println("Unrecognised option: " + args[i]);
          printUsage();
        }
      } else {
        // Is it a file?
        File file = new File(args[i]);
        if (file.exists()) {
          System.out.println("I don't handle files yet. Try again later.");
        } else {
          // It's a command line component
          try {
            Info info = new Info(args[i]);
            launch.add(info);
          } catch (Exception e) {
            e.printStackTrace();
            return;
          }
        }
      }
    }
    // Launch the components
    long start = System.currentTimeMillis();
    for (Iterator it = launch.iterator(); it.hasNext();) {
      // Open the connection and input thread
      Info next = (Info) it.next();
      try {
        System.out.println("Launching " + (next.number == 0 ? "lots of" : "" + next.number) + " components of class "
            + next.clazz.getName());
        int max = next.number;
        if (max < 1)
          max = Integer.MAX_VALUE;
        for (int j = 0; j < max; ++j) {
          RescueComponent component = instantiate(next);
          Connection connection;
          if (tcp)
            connection = new TCPConnection(kernel, kernelPort);
          else
            connection = new LongUDPConnection(kernel, kernelPort);
          component.setConnection(connection);
          if (launch(component, connection)) {
            InputThread input = new InputThread(connection, component);
            input.start();
            components.add(new ComponentInfo(component, input, connection));
          } else {
            connection.close();
            break;
          }
        }
        System.out.println("Finished launching components of class " + next.clazz.getName());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("Launched all components in " + ((end - start) / 1000.0) + " s");
    // Wait for one of them to terminate
    boolean running = true;
    while (running) {
      // Sleep for a bit
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      for (Iterator it = components.iterator(); it.hasNext();) {
        RescueComponent c = ((ComponentInfo) it.next()).component;
        if (!c.isRunning())
          running = false;
      }
    }
    shutdown();
  }

  private static void printUsage() {
    System.out.println("Usage: Launch hostname portnumber components [options]");
    System.out.println("hostname:\tThe name of the machine running the simulation kernel");
    System.out.println("portnumber:\tThe port number that the kernel is listening on");
    System.out.println(
        "components:\tA set of components that should be launched. These are all of the form \"[number] classname arguments\". This will launch <number> (default 1) objects of type <classname>, passing <arguments> to each component.");
    System.out.println("Options");
    System.out.println("=======");
    System.out.println(UDP_FLAG + "\t" + UDP_LONG_FLAG + "\tUse UDP instead of TCP");
  }

  private static RescueComponent instantiate(Info info) throws InstantiationException, IllegalAccessException,
      ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
    RescueComponent result;
    // System.out.print("Instantiating "+info+": ");
    if (info.args.length != 0) {
      // Try to find a constructor
      Constructor c;
      try {
        c = info.clazz.getDeclaredConstructor(new Class[] { String[].class });
        result = (RescueComponent) c.newInstance(new Object[] { info.args });
      } catch (NoSuchMethodException e) {
        try {
          Class[] classes = new Class[info.args.length];
          for (int j = 0; j < classes.length; ++j)
            classes[j] = String.class;
          c = info.clazz.getDeclaredConstructor(classes);
          result = (RescueComponent) c.newInstance((Object[]) info.args);
        } catch (NoSuchMethodException ex) {
          result = (RescueComponent) info.clazz.getDeclaredConstructor().newInstance();
        }
      }
    } else {
      result = (RescueComponent) info.clazz.getDeclaredConstructor().newInstance();
    }
    // System.out.println(result);
    return result;
  }

  private static boolean launch(RescueComponent c, Connection connection) throws IOException {
    System.out.println("Launching " + c);
    RescueMessage msg = new RescueMessage();
    msg.append(c.generateConnectCommand());
    int okReply = -1;
    int errorReply = -1;
    switch (c.getComponentType()) {
      case RescueConstants.COMPONENT_TYPE_AGENT:
        okReply = RescueConstants.KA_CONNECT_OK;
        errorReply = RescueConstants.KA_CONNECT_ERROR;
        break;
      case RescueConstants.COMPONENT_TYPE_SIMULATOR:
        okReply = RescueConstants.KS_CONNECT_OK;
        errorReply = RescueConstants.KS_CONNECT_ERROR;
        break;
      case RescueConstants.COMPONENT_TYPE_VIEWER:
        okReply = RescueConstants.KV_CONNECT_OK;
        errorReply = RescueConstants.KV_CONNECT_ERROR;
        break;
      default:
        throw new RuntimeException("Unknown component type");
    }
    connection.send(msg.toByteArray());
    // Wait for reply
    long timeout = System.currentTimeMillis() + 60000;
    boolean success = false;
    do {
      // System.out.println("Waiting for reply...");
      try {
        byte[] reply = connection.receive(TIMEOUT);
        if (reply != null) {
          InputBuffer in = new InputBuffer(reply);
          Command[] messages = in.readCommands();
          // System.out.println("Received "+messages.length+" messages");
          for (int i = 0; i < messages.length; ++i) {
            if (success)
              c.handleMessage(messages[i]);
            else {
              if (messages[i].getType() == okReply)
                success = c.handleConnectOK(messages[i]);
              if (messages[i].getType() == errorReply) {
                String reason = c.handleConnectError(messages[i]);
                if (reason != null) {
                  System.out.println("Error connecting " + c + ": " + reason);
                  return false;
                }
              }
            }
          }
        }
      } catch (InterruptedException e) {
        return false;
      }
    } while (!success && System.currentTimeMillis() < timeout);
    if (!success)
      System.out.println("Timeout trying to connect " + c);
    return success;
  }

  private static void shutdown() {
    // Terminate all components
    for (Iterator it = components.iterator(); it.hasNext();) {
      ComponentInfo next = (ComponentInfo) it.next();
      // Shut down the component
      next.component.shutdown();
      // Kill the input thread
      next.input.kill();
    }
    // Wait for components to finish terminating
    for (Iterator it = components.iterator(); it.hasNext();) {
      ComponentInfo next = (ComponentInfo) it.next();
      next.input.killAndWait();
    }
    // Exit the VM
    System.exit(0);
  }

  private static class InputThread extends Thread {
    private volatile boolean running, alive;
    private final Object aliveLock = new Object();
    private RescueComponent target;
    private Connection connection;

    InputThread(Connection connection, RescueComponent target) {
      this.connection = connection;
      this.target = target;
      running = alive = true;
    }

    public void kill() {
      running = false;
      interrupt();
    }

    public void killAndWait() {
      kill();
      synchronized (aliveLock) {
        while (alive)
          try {
            aliveLock.wait(1000);
          } catch (InterruptedException e) {
          }
      }
      connection.close();
    }

    public void run() {
      while (running) {
        try {
          // System.out.println(target+" waiting for input");
          byte[] msg = connection.receive(1000);
          if (msg != null) {
            // Handy.printBytes("RECEIVED",msg.getData());
            // long start = System.currentTimeMillis();
            Command[] messages = new InputBuffer(msg).readCommands();
            for (int i = 0; i < messages.length; ++i) {
              // System.out.println(target+" received "+messages[i]);
              target.handleMessage(messages[i]);
            }
            // long end = System.currentTimeMillis();
            // System.out.println(target+" took "+(end-start)+"ms to process a message");
          }
        } catch (InterruptedException e) {
        } catch (IOException e) {
          e.printStackTrace();
          running = false;
        }
      }
      synchronized (aliveLock) {
        alive = false;
        aliveLock.notifyAll();
      }
    }
  }

  private static class ComponentInfo {
    RescueComponent component;
    InputThread input;
    Connection connection;

    public ComponentInfo(RescueComponent c, InputThread i, Connection connection) {
      component = c;
      input = i;
      this.connection = connection;
    }
  }

  private static class Info {
    Class clazz;
    int number;
    String[] args;

    Info(String line) throws ClassNotFoundException, NoSuchElementException {
      StringTokenizer tokens = new StringTokenizer(line);
      // Try to parse the first token as an integer
      String first = tokens.nextToken();
      String second;
      number = 1;
      try {
        number = Integer.parseInt(first);
        second = tokens.nextToken();
      } catch (NumberFormatException e) {
        // Couldn't parse it, we'll assume no number was given
        second = first;
      }
      clazz = Class.forName(second);
      args = new String[tokens.countTokens()];
      for (int i = 0; i < args.length; ++i)
        args[i] = tokens.nextToken();
    }

    public String toString() {
      StringBuffer result = new StringBuffer();
      result.append(clazz.getName());
      result.append("(");
      for (int i = 0; i < args.length; ++i) {
        result.append("\"");
        result.append(args[i]);
        result.append("\"");
        if (i < args.length - 1)
          result.append(",");
      }
      result.append(")");
      return result.toString();
    }
  }
}