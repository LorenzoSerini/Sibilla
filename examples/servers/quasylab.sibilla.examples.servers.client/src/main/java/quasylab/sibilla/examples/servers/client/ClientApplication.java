package quasylab.sibilla.examples.servers.client;

import org.apache.commons.math3.random.AbstractRandomGenerator;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;
import quasylab.sibilla.core.server.NetworkInfo;
import quasylab.sibilla.core.server.client.ClientSimulationEnvironment;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.server.util.SSLUtils;
import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.Serializable;

public class ClientApplication implements Serializable {

    public final static int S = 0;
    public final static int E = 1;
    public final static int I = 2;
    public final static int R = 3;
    public final static int INIT_S = 99;
    public final static int INIT_E = 0;
    public final static int INIT_I = 1;
    public final static int INIT_R = 0;
    public final static double N = INIT_S + INIT_E + INIT_I + INIT_R;
    public final static double LAMBDA_E = 1;
    public final static double LAMBDA_I = 1 / 3.0;
    public final static double LAMBDA_R = 1 / 7.0;
    public final static int SAMPLINGS = 100;
    public final static double DEADLINE = 600;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int REPLICA = 10000;

    private static final AbstractRandomGenerator RANDOM_GENERATOR = new DefaultRandomGenerator();
    private static final String MODEL_NAME = ClientApplication.class.getName();
    private static final NetworkInfo MASTER_SERVER_INFO = new NetworkInfo(NetworkUtils.getLocalIp(), 10001,
            TCPNetworkManagerType.SECURE);

    public static void main(String[] argv) throws Exception {

        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("clientKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("clientPass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("clientTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("clientPass");

        SEIRModelDefinition modelDefinition = new SEIRModelDefinition();
        SamplingCollection<PopulationState> collection = new SamplingCollection<>();
        collection.add(StatisticSampling.measure("S", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfS));
        collection.add(StatisticSampling.measure("E", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfE));
        collection.add(StatisticSampling.measure("I", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfI));
        collection.add(StatisticSampling.measure("R", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfR));

        ClientSimulationEnvironment<PopulationState> client = new ClientSimulationEnvironment<PopulationState>(
                RANDOM_GENERATOR, modelDefinition, modelDefinition.createModel(), modelDefinition.state(), collection,
                REPLICA, DEADLINE, MASTER_SERVER_INFO);

    }

}
