package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class ComputationResultSerializer {

    //numero di sample nella traiettoria successiva - 4
    //traiettoria
    public static <S extends State> byte[] serialize(ComputationResult<S> toSerialize, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, toSerialize, model);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, ComputationResult<S> toSerialize, Model<S> model) throws IOException {
        for (Trajectory<S> trajectory : toSerialize.getResults()) {
            TrajectorySerializer.serialize(toSerializeInto, trajectory, model);
        }
    }

    public static <S extends State> ComputationResult<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        //System.out.println(String.format("Computation To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        //System.out.println(String.format("Number of trajectories:%d - Size of trajectories:%d", numberOfTrajectories, sizeOfTrajectories));
        LinkedList<Trajectory<S>> trajectories = new LinkedList<>();
        while (bais.available() > 0) {
            int numberOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            Trajectory<S> trajectory = TrajectorySerializer.deserialize(bais.readNBytes(TrajectorySerializer.getByteSize(model, numberOfSamples)), model, numberOfSamples);
            trajectories.add(trajectory);
        }
        bais.close();
        return new ComputationResult<>(trajectories);
    }

    public static <S extends State> ComputationResult<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        LinkedList<Trajectory<S>> trajectories = new LinkedList<>();
        while (toDeserializeFrom.available() > 0) {
            Trajectory<S> trajectory = TrajectorySerializer.deserialize(toDeserializeFrom, model);
            trajectories.add(trajectory);
        }
        return new ComputationResult<>(trajectories);
    }


}
