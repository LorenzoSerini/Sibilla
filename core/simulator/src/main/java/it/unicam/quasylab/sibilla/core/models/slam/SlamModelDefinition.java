/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

public class SlamModelDefinition extends AbstractModelDefinition<SlamState> {

    @Override
    protected void clearCache() {

    }

    @Override
    public ParametricDataSet<Function<RandomGenerator, SlamState>> getStates() {
        return null;
    }

    @Override
    public Model<SlamState> createModel() {
        return null;
    }
}
