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
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.examples.pm.molecule;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;

public class MoleculeDefiniton implements ModelDefinition<PopulationState> {


    public final static int Na = 0;
    public final static int Cl = 1;
    public final static int NaPositive = 2;
    public final static int ClNegative = 3;

    public final static int SCALE = 100;

    public final static int INIT_Na = 99*SCALE;
    public final static int INIT_Cl = 1*SCALE;
    public final static int INIT_NaPositive = 0*SCALE;
    public final static int INIT_ClNegative = 0*SCALE;

    public final static double N = INIT_Na + INIT_Cl + INIT_NaPositive + INIT_ClNegative;

    public final static double E1RATE = 100;
    public final static double E2RATE = 10;

    public final static double LAMBDA = 10;

    @Override
    public int stateArity() {
        return 0;
    }

    @Override
    public int modelArity() {
        return 0;
    }

    @Override
    public PopulationState state(double... parameters) {
        if (parameters.length != 4) {
            return new PopulationState( new int[] { INIT_Na, INIT_Cl, INIT_NaPositive, INIT_ClNegative } );
        } else {
            return new PopulationState(new int[]{
                    (int) parameters[Na],
                    (int) parameters[Cl],
                    (int) parameters[NaPositive],
                    (int) parameters[ClNegative]});
        }
    }

    @Override
    public Model<PopulationState> createModel(double... args) {

        double lambda = (args.length>0?args[0]:LAMBDA);


        // Na + Cl -> Na+ + Cl-

        PopulationRule rule_Na_Cl__NaP_ClM = new ReactionRule(
                "Na + Cl -> Na+ + Cl-",
                new Population[] { new Population(Na), new Population(Cl)} ,
                new Population[] { new Population(NaPositive), new Population(ClNegative)},
                (t,s) ->s.getOccupancy(Na) * s.getOccupancy(Cl) * lambda * E1RATE);


        // Na+ + Cl- -> Na + Cl

        PopulationRule rule_NaP_ClM__Na_Cl = new ReactionRule(
                "Na+ + Cl- -> Na + Cl",
                new Population[] { new Population(NaPositive), new Population(ClNegative)},
                new Population[] { new Population(Na), new Population(Cl)} ,
                (t,s) -> s.getOccupancy(NaPositive) * s.getOccupancy(ClNegative) * lambda * E2RATE);

        PopulationModel pModel = new PopulationModel();

        pModel.addRule(rule_Na_Cl__NaP_ClM);
        pModel.addRule(rule_NaP_ClM__Na_Cl);

        return pModel;
    }
}
