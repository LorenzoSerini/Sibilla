/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server;

import quasylab.sibilla.core.server.compression.Compressor;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.CustomClassLoader;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.server.slave.SlaveCommand;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Represent a simple server that executes the simulations passed by a master server
 */
public class BasicSimulationServer implements SimulationServer {

    private static final Logger LOGGER = Logger.getLogger(BasicSimulationServer.class.getName());

    private final TCPNetworkManagerType networkManagerType;
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private int simulationPort;
    protected NetworkInfo localServerInfo;

    /**
     * Creates a simulation server with the given network manager type
     *
     * @param networkManagerType type of the network manager
     */
    public BasicSimulationServer(TCPNetworkManagerType networkManagerType) {
        this.networkManagerType = networkManagerType;
        LOGGER.info(String.format("Creating a new BasicSimulationServer that uses: [%s - %s]",
                this.networkManagerType.getClass(), this.networkManagerType.name()));
    }

    @Override
    public void start(int port) {
        try {
            this.simulationPort = port;
            this.localServerInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), this.simulationPort, this.networkManagerType);
            LOGGER.info(String.format("The BasicSimulationServer will accept simulation requests on port [%d]", this.simulationPort));
            this.startSimulationServer();
        } catch (
                SocketException e) {
            LOGGER.severe(String.format("Network interfaces exception - %s", e.getMessage()));
        }
    }

    /**
     * Starts a simulation server
     */
    private void startSimulationServer() {
        try {
            ServerSocket serverSocket = TCPNetworkManager.createServerSocket(networkManagerType, simulationPort);
            LOGGER.info(String.format("The BasicSimulationServer is now listening for servers on port: [%d]", simulationPort));
            while (true) {
                Socket socket = serverSocket.accept();
                connectionExecutor.execute(() -> {
                    manageMasterMessage(socket);
                });
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the server socket startup - %s", e.getMessage()));
        }
    }

    /**
     * Manages the messages that come from the master server
     *
     * @param socket socket where the server listens for master messages
     */
    private void manageMasterMessage(Socket socket) {
        try {
            TCPNetworkManager master = TCPNetworkManager.createNetworkManager(networkManagerType, socket);
            AtomicBoolean masterIsActive = new AtomicBoolean(true);

            Map<MasterCommand, Runnable> map = Map.of(
                    MasterCommand.PING, () -> respondPingRequest(master),
                    MasterCommand.INIT, () -> loadModelClass(master),
                    MasterCommand.TASK, () -> handleTaskExecution(master),
                    MasterCommand.CLOSE_CONNECTION, () -> closeConnectionWithMaster(masterIsActive, master));
            while (masterIsActive.get()) {
                MasterCommand request = (MasterCommand) ObjectSerializer.deserializeObject(master.readObject());
                LOGGER.info(String.format("[%s] command received by server - %s", request, master.getServerInfo().toString()));
                map.getOrDefault(request, () -> {
                }).run();
            }

        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during master communication - %s", e.getMessage()));
        }
    }


    /**
     * Closes the connection with the master server
     *
     * @param masterActive boolean that tells if the master is active or not
     * @param master       server of the master
     */
    private void closeConnectionWithMaster(AtomicBoolean masterActive, TCPNetworkManager master) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(master.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by server - %s", modelName, master.getServerInfo().toString()));
            masterActive.set(false);
            CustomClassLoader.classes.remove(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            LOGGER.info("Master closed the connection");
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the connection closure - %s", e.getMessage()));
        }
    }

    /**
     * Loads the model class in the memory with the CustomClassLoader
     *
     * @param master server of the master
     */
    private void loadModelClass(TCPNetworkManager master) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(master.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - %s", modelName, master.getServerInfo().toString()));
            byte[] myClass = master.readObject();
            CustomClassLoader.defClass(modelName, myClass);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
        } catch (ClassNotFoundException e) {
            LOGGER.severe(String.format("The simulation model was not loaded with success - %s", e.getMessage()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the simulation model loading - %s", e.getMessage()));
        }
    }

    /**
     * Handles the simulation execution sent by the server and sends its results to the master
     *
     * @param master server of the master
     */
    private void handleTaskExecution(TCPNetworkManager master) {
        try {
            NetworkTask<?> networkTask = (NetworkTask<?>) ObjectSerializer.deserializeObject(master.readObject());
            List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
            LinkedList<Trajectory<?>> results = new LinkedList<>();
            CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
            for (int i = 0; i < tasks.size(); i++) {
                futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
            }
            CompletableFuture.allOf(futures).join();
            for (SimulationTask<?> task : tasks) {
                results.add(task.getTrajectory());
            }
            master.writeObject(Compressor.compress(ObjectSerializer.serializeObject(new ComputationResult(results))));
            LOGGER.info(String.format("Computation's results have been sent to the server - %s",
                    master.getServerInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the task handling - %s", e.getMessage()));
        }
    }

    /**
     * Responds to a ping request from the master
     *
     * @param master server of the master
     */
    private void respondPingRequest(TCPNetworkManager master) {
        try {
            master.writeObject(ObjectSerializer.serializeObject(SlaveCommand.PONG));
            LOGGER.info(String.format("Ping request answered, it was sent by the server - %s",
                    master.getServerInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the ping response - %s", e.getMessage()));
        }
    }

}